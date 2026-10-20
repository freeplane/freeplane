#!/usr/bin/env python3
"""Compile and run the bounded X11 popup-coordinate acceptance probe."""

from __future__ import annotations

import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shlex
import signal
import subprocess
import sys
import tempfile
import time
from typing import Dict, Iterable, List, Optional, Sequence, Tuple


EVIDENCE_DIR = Path(__file__).resolve().parent
REPOSITORY_ROOT = EVIDENCE_DIR.parents[3]
LOG_DIR = EVIDENCE_DIR / "logs"
JAVA_HOME_DEFAULT = Path("/home/henry/.sdkman/candidates/java/21.0.8-zulu")
PROBE_CLASS = "org.freeplane.core.ui.components.PopupCoordinateAcceptanceProbe"
PRODUCTION_FILES = (
    REPOSITORY_ROOT / "freeplane/src/main/java/org/freeplane/core/ui/components/FrameResynchronizer.java",
    REPOSITORY_ROOT / "freeplane/src/main/java/org/freeplane/core/ui/components/PopupCoordinateCalibrator.java",
    REPOSITORY_ROOT / "freeplane/src/main/java/org/freeplane/core/ui/components/X11PopupFactory.java",
)
PRIVATE_DISPLAY_START = 77
PRIVATE_DISPLAY_END = 100
PRIVATE_SCREEN = "2240x1400x24"
SCENARIOS = ("startup-normal", "maximize", "restore", "remaximize")
MAX_PROBE_ATTEMPTS = 3
# Linear backoff: after failed attempt N, wait N seconds before the next attempt.
PROBE_RETRY_BACKOFF_SECONDS = 1.0
DIAGNOSIS_TAIL_LINES = 8


def classification_produced(*outputs: str) -> bool:
    return any(line.startswith("failure-class=")
               for output in outputs for line in output.splitlines())


def retryable_pre_classification_failure(stdout: str, stderr: str) -> bool:
    return not classification_produced(stdout, stderr)


def classifications_match(stdout: str, expected_class: str) -> bool:
    lines = [line for line in stdout.splitlines() if line.startswith("failure-class=")]
    classifications = re.findall(
        r"^failure-class=(PRODUCT|ENVIRONMENT|NONE) reason=.+ scenario=(\S+)$", stdout, re.MULTILINE
    )
    return len(lines) == len(SCENARIOS) and sorted(classifications) == sorted(
        (expected_class, scenario) for scenario in SCENARIOS
    )


def scenario_records_match(stdout: str, expected: Dict[str, str]) -> bool:
    records = [dict(re.findall(r"(\S+?)=(\S+)", line))
               for line in stdout.splitlines() if line.startswith("scenario=")]
    try:
        return (
            len(records) == len(SCENARIOS)
            and sorted(record.get("scenario", "") for record in records) == sorted(SCENARIOS)
            and all(
                all(record.get(key) == value for key, value in expected.items())
                and int(record.get("persistence-ms", "-1")) >= 800
                for record in records
            )
        )
    except ValueError:
        return False


def shell_command(command: Sequence[str]) -> str:
    return shlex.join([str(part) for part in command])


def completed(command: Sequence[str], env: Optional[Dict[str, str]] = None) -> subprocess.CompletedProcess:
    return subprocess.run(
        [str(part) for part in command],
        cwd=str(REPOSITORY_ROOT),
        env=env,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        encoding="utf-8",
        errors="replace",
        check=False,
    )


def find_files(roots: Iterable[Path], pattern: str) -> List[Path]:
    existing_roots = [str(root) for root in roots if root.exists()]
    if not existing_roots:
        return []
    result = completed(["find", *existing_roots, "-type", "f", "-name", pattern])
    if result.returncode not in (0, 1):
        raise RuntimeError("find failed for " + pattern + ": " + result.stderr.strip())
    return sorted({Path(line) for line in result.stdout.splitlines() if line.strip()})


def first_required(paths: Iterable[Path], description: str) -> Path:
    candidates = list(paths)
    if not candidates:
        raise RuntimeError("could not find " + description)
    return candidates[0]


def command_output(command: Sequence[str], env: Dict[str, str]) -> str:
    result = completed(command, env)
    output = result.stdout
    if result.stderr:
        output += result.stderr
    return output.strip()


def text_output(output: object) -> str:
    if isinstance(output, bytes):
        return output.decode("utf-8", errors="replace")
    return str(output)


def terminate_process_group(process: Optional[subprocess.Popen]) -> None:
    if process is None or process.poll() is not None:
        return
    try:
        os.killpg(os.getpgid(process.pid), signal.SIGTERM)
    except ProcessLookupError:
        return
    try:
        process.wait(timeout=5)
    except subprocess.TimeoutExpired:
        try:
            os.killpg(os.getpgid(process.pid), signal.SIGKILL)
        except ProcessLookupError:
            pass
        process.wait(timeout=5)


class IsolatedDisplay:
    def __init__(self, log_dir: Path, base_env: Dict[str, str]) -> None:
        self.log_dir = log_dir
        self.base_env = dict(base_env)
        self.display: Optional[str] = None
        self.xvfb: Optional[subprocess.Popen] = None
        self.window_manager: Optional[subprocess.Popen] = None
        self.log_streams: List[object] = []

    def _wait_for(self, display: str, command: Sequence[str], timeout_seconds: float) -> bool:
        command_env = dict(self.base_env)
        command_env["DISPLAY"] = display
        deadline = time.monotonic() + timeout_seconds
        while time.monotonic() < deadline:
            result = completed(command, command_env)
            if result.returncode == 0:
                return True
            time.sleep(0.1)
        return False

    def _stop_candidate(self, window_manager: Optional[subprocess.Popen],
                        xvfb: Optional[subprocess.Popen], streams: Sequence[object]) -> None:
        terminate_process_group(window_manager)
        terminate_process_group(xvfb)
        for stream in streams:
            stream.close()

    def start(self) -> str:
        last_error = "no free display in range"
        for display_number in range(PRIVATE_DISPLAY_START, PRIVATE_DISPLAY_END):
            display = ":" + str(display_number)
            if self._wait_for(display, ["xdpyinfo"], 0.2):
                continue

            xvfb_log = self.log_dir / ("xvfb-display-" + str(display_number) + ".log")
            xvfb_stream = xvfb_log.open("w", encoding="utf-8")
            xvfb = subprocess.Popen(
                ["Xvfb", display, "-screen", "0", PRIVATE_SCREEN, "-nolisten", "tcp", "-ac"],
                env=self.base_env,
                stdout=xvfb_stream,
                stderr=subprocess.STDOUT,
                start_new_session=True,
            )
            if not self._wait_for(display, ["xdpyinfo"], 5.0):
                last_error = "Xvfb did not become ready on " + display
                self._stop_candidate(None, xvfb, [xvfb_stream])
                continue

            window_manager_log = self.log_dir / ("openbox-display-" + str(display_number) + ".log")
            window_manager_stream = window_manager_log.open("w", encoding="utf-8")
            window_manager_env = dict(self.base_env)
            window_manager_env["DISPLAY"] = display
            window_manager = subprocess.Popen(
                ["openbox", "--sm-disable"],
                env=window_manager_env,
                stdout=window_manager_stream,
                stderr=subprocess.STDOUT,
                start_new_session=True,
            )
            if not self._wait_for(display, ["wmctrl", "-m"], 5.0):
                last_error = "Openbox did not become ready on " + display
                self._stop_candidate(window_manager, xvfb, [window_manager_stream, xvfb_stream])
                continue

            self.display = display
            self.xvfb = xvfb
            self.window_manager = window_manager
            self.log_streams = [window_manager_stream, xvfb_stream]
            return display

        raise RuntimeError(last_error)

    def close(self) -> None:
        terminate_process_group(self.window_manager)
        terminate_process_group(self.xvfb)
        for stream in self.log_streams:
            stream.close()
        self.window_manager = None
        self.display = None
        self.xvfb = None
        self.log_streams = []


def bounded_run(command: Sequence[str], env: Dict[str, str], timeout_seconds: int) -> Tuple[int, str, str, bool]:
    process = subprocess.Popen(
        [str(part) for part in command],
        cwd=str(REPOSITORY_ROOT),
        env=env,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        encoding="utf-8",
        errors="replace",
        start_new_session=True,
    )
    try:
        stdout, stderr = process.communicate(timeout=timeout_seconds)
        return process.returncode, stdout, stderr, False
    except subprocess.TimeoutExpired as timeout:
        partial_stdout = text_output(timeout.stdout or "")
        partial_stderr = text_output(timeout.stderr or "")
        terminate_process_group(process)
        stdout, stderr = process.communicate()
        return 124, partial_stdout + text_output(stdout), partial_stderr + text_output(stderr), True


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for block in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def write_log(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def display_identity(display: str, env: Dict[str, str]) -> Tuple[str, str, str]:
    display_env = dict(env)
    display_env["DISPLAY"] = display
    xdpyinfo = completed(["xdpyinfo"], display_env)
    xdpy_output = xdpyinfo.stdout + xdpyinfo.stderr
    dimensions_match = re.search(r"dimensions:\s+([0-9]+x[0-9]+)\s+pixels", xdpy_output)
    dimensions = dimensions_match.group(1) if dimensions_match else "UNAVAILABLE"
    xprop = completed(["xprop", "-root", "_NET_WORKAREA"], display_env)
    xprop_output = (xprop.stdout + xprop.stderr).strip()
    workarea_match = re.search(r"_NET_WORKAREA.*?=\s*(.*)", xprop_output)
    workarea = workarea_match.group(1) if workarea_match else "UNAVAILABLE"
    return dimensions, workarea, xprop_output or "UNAVAILABLE"


def build_runtime_identity(run_env: Dict[str, str], java: Path, selected_display: str) -> List[str]:
    lines = [
        "DISPLAY=" + run_env.get("DISPLAY", ""),
        "XDG_SESSION_TYPE=" + run_env.get("XDG_SESSION_TYPE", ""),
        "display-selected=" + selected_display,
        "display-selected-kind=private Xvfb with Openbox window manager",
        "display-selected-screen=" + PRIVATE_SCREEN,
    ]
    displays: List[str] = []
    for display in (selected_display, ":1", ":2"):
        if display not in displays:
            displays.append(display)
    for display in displays:
        dimensions, workarea, raw_workarea = display_identity(display, run_env)
        lines.append("display=" + display + " dimensions=" + dimensions)
        lines.append("display=" + display + " _NET_WORKAREA=" + workarea)
        lines.append("display=" + display + " _NET_WORKAREA-raw=" + raw_workarea)

    for process_name in ("kwin_wayland", "kwin_x11"):
        result = completed(["pgrep", "-a", process_name], run_env)
        process_output = result.stdout.strip() or "NONE"
        lines.append(process_name + "=" + process_output)

    java_version_command = [java, "--version"]
    lines.append("java-version-command=" + shell_command(java_version_command))
    java_version = command_output(java_version_command, run_env) or "UNAVAILABLE"
    lines.extend("java-version=" + line for line in java_version.splitlines())

    git_head = command_output(["git", "rev-parse", "HEAD"], run_env)
    lines.append("git-head=" + (git_head or "UNAVAILABLE"))
    for production_file in PRODUCTION_FILES:
        relative = production_file.relative_to(REPOSITORY_ROOT)
        lines.append("source-sha256=" + str(relative) + " " + sha256(production_file))
    return lines


def parse_mutant_offset(raw_offset: Optional[str]) -> Optional[Tuple[int, int]]:
    if raw_offset is None:
        return None
    match = re.fullmatch(r"([+-]?[0-9]+),([+-]?[0-9]+)", raw_offset)
    if match is None:
        raise RuntimeError("mutant offset must have the form X,Y")
    return int(match.group(1)), int(match.group(2))


def write_probe_result(path: Path, display: str, compile_result: int, probe_exit_code: Optional[int],
                       timed_out: bool, expected_mutant_failure: bool = False) -> None:
    result = "UNAVAILABLE" if probe_exit_code is None else ("PASS" if probe_exit_code == 0 else "FAIL")
    lines = [
        "display=" + display,
        "javac-exit-code=" + str(compile_result),
        "probe-exit-code=" + ("UNAVAILABLE" if probe_exit_code is None else str(probe_exit_code)),
        "probe-timeout=" + str(timed_out),
        "probe-result=" + result,
    ]
    if expected_mutant_failure:
        lines.append("expected-mutant-failure=true")
    write_log(path, "\n".join(lines) + "\n")


def append_json_log(path: Path, record: Dict[str, object]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("a", encoding="utf-8") as log:
        log.write(json.dumps(record, sort_keys=True) + "\n")


def output_tail(output: str) -> List[str]:
    return output.splitlines()[-DIAGNOSIS_TAIL_LINES:]


def emit_probe_diagnosis(attempt_log: Path, run_id: str, variant: str, attempt: int,
                         phase: str, exit_code: Optional[int], timed_out: bool,
                         stdout: str, stderr: str, reason: str,
                         display: Optional[str]) -> None:
    record = {
        "run_id": run_id,
        "variant": variant,
        "attempt": attempt,
        "phase": phase,
        "exit_code": exit_code,
        "timeout": timed_out,
        "classification_produced": classification_produced(stdout, stderr),
        "stdout_tail": output_tail(stdout),
        "stderr_tail": output_tail(stderr),
        "reason": reason,
        "display": display or "UNAVAILABLE",
    }
    append_json_log(attempt_log, {"type": "probe-diagnosis", **record})
    print("probe-diagnosis=" + json.dumps(record, sort_keys=True), file=sys.stderr, flush=True)


def record_probe_attempt(attempt_log: Path, run_id: str, variant: str, attempt: int,
                         phase: str, outcome: str, reason: str, exit_code: Optional[int],
                         timed_out: bool, stdout: str, stderr: str,
                         display: Optional[str]) -> None:
    record = {
        "run_id": run_id,
        "variant": variant,
        "attempt": attempt,
        "phase": phase,
        "outcome": outcome,
        "reason": reason,
        "exit_code": exit_code,
        "timeout": timed_out,
        "classification_produced": classification_produced(stdout, stderr),
        "display": display or "UNAVAILABLE",
    }
    append_json_log(attempt_log, {"type": "probe-attempt", **record})
    print("probe-attempt=" + json.dumps(record, sort_keys=True), flush=True)


def run_probe_with_retries(label: str, probe_command: Sequence[str], base_env: Dict[str, str],
                           java: Path, display_session: IsolatedDisplay, log_prefix: str,
                           compile_code: int, expected_mutant_failure: bool, run_id: str,
                           attempt_log: Path, commands: List[str],
                           timeout_seconds: int = 90) -> Tuple[Optional[int], str, str, bool, Optional[str], int]:
    last_exit_code: Optional[int] = None
    last_stdout = ""
    last_stderr = ""
    last_timed_out = False
    last_display: Optional[str] = None
    for attempt in range(1, MAX_PROBE_ATTEMPTS + 1):
        selected_display = display_session.display
        if selected_display is None:
            try:
                selected_display = display_session.start()
            except Exception as exception:
                reason = "display acquisition failed: " + exception.__class__.__name__ + ": " + str(exception)
                emit_probe_diagnosis(attempt_log, run_id, label, attempt, "display-acquisition", None, False,
                                     "", "", reason, None)
                outcome = "retry" if attempt < MAX_PROBE_ATTEMPTS else "failed"
                record_probe_attempt(attempt_log, run_id, label, attempt, "display-acquisition", outcome,
                                     reason, None, False, "", "", None)
                if attempt < MAX_PROBE_ATTEMPTS:
                    time.sleep(PROBE_RETRY_BACKOFF_SECONDS * attempt)
                    continue
                return last_exit_code, last_stdout, last_stderr, last_timed_out, last_display, attempt

        run_env = dict(base_env)
        run_env["DISPLAY"] = selected_display
        last_display = selected_display
        try:
            runtime_identity = build_runtime_identity(run_env, java, selected_display)
        except Exception as exception:
            reason = "runtime identity collection failed: " + exception.__class__.__name__ + ": " + str(exception)
            emit_probe_diagnosis(attempt_log, run_id, label, attempt, "runtime-identity", None, False,
                                 "", "", reason, selected_display)
            outcome = "retry" if attempt < MAX_PROBE_ATTEMPTS else "failed"
            record_probe_attempt(attempt_log, run_id, label, attempt, "runtime-identity", outcome,
                                 reason, None, False, "", "", selected_display)
            if attempt < MAX_PROBE_ATTEMPTS:
                display_session.close()
                time.sleep(PROBE_RETRY_BACKOFF_SECONDS * attempt)
                continue
            return last_exit_code, last_stdout, last_stderr, last_timed_out, last_display, attempt
        write_log(LOG_DIR / (log_prefix + ".attempt-" + str(attempt) + ".runtime-identity.txt"),
                  "\n".join(runtime_identity) + "\n")
        write_log(LOG_DIR / "runtime-identity.txt", "\n".join(runtime_identity) + "\n")
        commands.append(label + "-attempt-" + str(attempt) + "-selected-display=" + selected_display)
        commands.append(label + "-attempt-" + str(attempt) + "-probe-command=" + shell_command(probe_command))

        try:
            exit_code, stdout, stderr, timed_out = bounded_run(probe_command, run_env, timeout_seconds)
        except Exception as exception:
            exit_code = None
            stdout = ""
            stderr = ""
            timed_out = False
            invocation_reason = "probe invocation failed: " + exception.__class__.__name__ + ": " + str(exception)
        else:
            invocation_reason = ""

        last_exit_code = exit_code
        last_stdout = stdout
        last_stderr = stderr
        last_timed_out = timed_out
        attempt_prefix = log_prefix + ".attempt-" + str(attempt)
        write_log(LOG_DIR / (attempt_prefix + ".stdout"), stdout)
        write_log(LOG_DIR / (attempt_prefix + ".stderr"), stderr)
        write_probe_result(LOG_DIR / (attempt_prefix + ".result.txt"), selected_display,
                           compile_code, exit_code, timed_out, expected_mutant_failure)
        write_log(LOG_DIR / (log_prefix + ".stdout"), stdout)
        write_log(LOG_DIR / (log_prefix + ".stderr"), stderr)
        write_probe_result(LOG_DIR / (log_prefix + ".result.txt"), selected_display,
                           compile_code, exit_code, timed_out, expected_mutant_failure)

        has_classification = classification_produced(stdout, stderr)
        probe_failed = exit_code is None or exit_code != 0 or timed_out or not has_classification
        if probe_failed:
            reason = invocation_reason or (
                "classified probe failure; retry disabled" if has_classification
                else "pre-classification harness failure"
            )
            emit_probe_diagnosis(attempt_log, run_id, label, attempt, label + "-probe", exit_code,
                                 timed_out, stdout, stderr, reason, selected_display)
        if has_classification:
            reason = "classification produced; retry disabled"
            outcome = "classified-failure" if probe_failed else "classified-success"
            record_probe_attempt(attempt_log, run_id, label, attempt, label + "-probe", outcome, reason,
                                 exit_code, timed_out, stdout, stderr, selected_display)
            return exit_code, stdout, stderr, timed_out, selected_display, attempt

        reason = invocation_reason or "pre-classification harness failure"
        outcome = "retry" if attempt < MAX_PROBE_ATTEMPTS else "failed"
        record_probe_attempt(attempt_log, run_id, label, attempt, label + "-probe", outcome, reason,
                             exit_code, timed_out, stdout, stderr, selected_display)
        if attempt == MAX_PROBE_ATTEMPTS:
            return exit_code, stdout, stderr, timed_out, selected_display, attempt
        display_session.close()
        time.sleep(PROBE_RETRY_BACKOFF_SECONDS * attempt)

    return last_exit_code, last_stdout, last_stderr, last_timed_out, last_display, MAX_PROBE_ATTEMPTS


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--mutant-offset",
        help="also run a menu.show X,Y offset mutant on the same isolated display",
    )
    parser.add_argument(
        "--negative-controls", action="store_true",
        help="also run the committed frame-writes-11 and consuming-item probe modes",
    )
    arguments = parser.parse_args()
    mutant_offset = parse_mutant_offset(arguments.mutant_offset)

    LOG_DIR.mkdir(parents=True, exist_ok=True)
    base_env = os.environ.copy()
    java_home = Path(base_env.get("JAVA_HOME", str(JAVA_HOME_DEFAULT)))
    if not java_home.is_dir():
        raise RuntimeError("JAVA_HOME does not exist: " + str(java_home))
    base_env["JAVA_HOME"] = str(java_home)
    base_env["PATH"] = str(java_home / "bin") + os.pathsep + base_env.get("PATH", "")

    java = java_home / "bin/java"
    javac = java_home / "bin/javac"
    if not java.is_file() or not javac.is_file():
        raise RuntimeError("Java tools are missing under " + str(java_home))

    gradle_cache = Path.home() / ".gradle/caches"
    flatlaf = first_required(find_files([gradle_cache], "flatlaf-3.7.2.jar"), "flatlaf-3.7.2.jar")
    jna_candidates = find_files([REPOSITORY_ROOT / "BIN/core", REPOSITORY_ROOT / "BIN", gradle_cache], "jna-*.jar")
    jna = first_required(
        [path for path in jna_candidates if re.fullmatch(r"jna-[0-9][^/]*\.jar", path.name)],
        "a JNA jar",
    )
    jna_platform = first_required(
        [path for path in jna_candidates if re.fullmatch(r"jna-platform-[0-9][^/]*\.jar", path.name)],
        "a JNA platform jar",
    )

    build_classes = REPOSITORY_ROOT / "freeplane/build/classes/java/main"
    if not build_classes.is_dir():
        raise RuntimeError("built Freeplane classes are missing: " + str(build_classes))
    probe_source = EVIDENCE_DIR / "PopupCoordinateAcceptanceProbe.java"
    if not probe_source.is_file():
        raise RuntimeError("probe source is missing: " + str(probe_source))

    run_id = str(time.time_ns())
    attempt_log = LOG_DIR / "attempts.jsonl"
    write_log(LOG_DIR / "runtime-identity.txt", "run-id=" + run_id + "\n")
    classpath = os.pathsep.join(str(path) for path in (build_classes, flatlaf, jna, jna_platform))
    commands: List[str] = [
        "run-id=" + run_id,
        "repository-root=" + str(REPOSITORY_ROOT),
        "probe-source=" + str(probe_source),
        "host-display=" + base_env.get("DISPLAY", ""),
        "display-selection=private Xvfb with Openbox window manager",
        "retry-policy=pre-classification-harness-failures-only",
        "retry-max-attempts=" + str(MAX_PROBE_ATTEMPTS),
        "retry-backoff=linear seconds=attempt*" + str(PROBE_RETRY_BACKOFF_SECONDS),
        "xvfb-command=" + shell_command(
            ["Xvfb", "<selected-display>", "-screen", "0", PRIVATE_SCREEN, "-nolisten", "tcp", "-ac"]
        ),
        "window-manager-command=" + shell_command(["openbox", "--sm-disable"]),
        "flatlaf-jar=" + str(flatlaf),
        "jna-jar=" + str(jna),
        "jna-platform-jar=" + str(jna_platform),
        "compile-classpath=" + classpath,
    ]

    display_session = IsolatedDisplay(LOG_DIR, base_env)
    try:
        canonical_compile_code: Optional[int] = None
        canonical_exit_code: Optional[int] = None
        canonical_timed_out = False
        canonical_stdout = ""
        canonical_stderr = ""
        canonical_attempts = 0
        mutant_exit_code: Optional[int] = None
        mutant_timed_out = False
        mutant_stdout = ""
        mutant_stderr = ""
        mutant_compile_code: Optional[int] = None
        mutant_attempts = 0
        negative_results = {}
        negative_attempts = {}

        with tempfile.TemporaryDirectory(prefix="freeplane-popup-acceptance-") as temporary_directory:
            temporary_root = Path(temporary_directory)
            source_variants: List[Tuple[str, Path]] = [("canonical", probe_source)]
            if mutant_offset is not None:
                offset_x, offset_y = mutant_offset
                mutant_source_directory = temporary_root / "mutant-source"
                mutant_source_directory.mkdir()
                mutant_source = mutant_source_directory / "PopupCoordinateAcceptanceProbe.java"
                source_text = probe_source.read_text(encoding="utf-8")
                original_call = "menu.show(owner, evidence.popupLocal.x, evidence.popupLocal.y);"
                mutant_call = (
                    "menu.show(owner, evidence.popupLocal.x + " + str(offset_x)
                    + ", evidence.popupLocal.y + " + str(offset_y) + ");"
                )
                if source_text.count(original_call) != 1:
                    raise RuntimeError("could not identify the single menu.show call for mutation")
                mutant_source.write_text(source_text.replace(original_call, mutant_call), encoding="utf-8")
                source_variants.append(("mutant", mutant_source))
            if arguments.negative_controls:
                source_variants.extend((mode, probe_source) for mode in ("frame-writes-11", "consuming-item"))

            for label, source in source_variants:
                class_directory = temporary_root / (label + "-classes")
                class_directory.mkdir()
                compile_command: List[str] = [
                    javac,
                    "--release",
                    "8",
                    "-encoding",
                    "UTF-8",
                    "-cp",
                    classpath,
                    "-d",
                    class_directory,
                    source,
                ]
                commands.append(label + "-compile-command=" + shell_command(compile_command))
                compile_result = completed(compile_command, base_env)
                mutant_suffix = "-shift-" + str(mutant_offset[0]) + "-" + str(mutant_offset[1]) if mutant_offset else ""
                log_prefix = ("probe" if label == "canonical" else
                              "falsifiability" + mutant_suffix if label == "mutant" else
                              "falsifiability-" + label)
                stdout_path = LOG_DIR / ("javac.stdout" if label == "canonical" else log_prefix + ".javac.stdout")
                stderr_path = LOG_DIR / ("javac.stderr" if label == "canonical" else log_prefix + ".javac.stderr")
                result_path = LOG_DIR / (log_prefix + ".result.txt")
                write_log(stdout_path, compile_result.stdout)
                write_log(stderr_path, compile_result.stderr)
                if label == "canonical":
                    canonical_compile_code = compile_result.returncode
                elif label == "mutant":
                    mutant_compile_code = compile_result.returncode
                else:
                    negative_results[label] = (compile_result.returncode, None, "", "", False)
                    negative_attempts[label] = 1
                if compile_result.returncode != 0:
                    selected_display = display_session.display or "UNAVAILABLE"
                    write_probe_result(
                        result_path,
                        selected_display,
                        compile_result.returncode,
                        None,
                        False,
                        expected_mutant_failure=label != "canonical",
                    )
                    emit_probe_diagnosis(
                        attempt_log, run_id, label, 1, label + "-compile", compile_result.returncode, False,
                        compile_result.stdout, compile_result.stderr,
                        "probe compilation failed; no classification produced", display_session.display,
                    )
                    record_probe_attempt(
                        attempt_log, run_id, label, 1, label + "-compile", "failed",
                        "probe compilation failed; no retry", compile_result.returncode, False,
                        compile_result.stdout, compile_result.stderr, display_session.display,
                    )
                    if label == "canonical":
                        canonical_attempts = 1
                        canonical_stdout = compile_result.stdout
                        canonical_stderr = compile_result.stderr
                    elif label == "mutant":
                        mutant_attempts = 1
                        mutant_stdout = compile_result.stdout
                        mutant_stderr = compile_result.stderr
                    continue

                runtime_classpath = os.pathsep.join((str(class_directory), classpath))
                probe_command: List[str] = [
                    java,
                    "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
                    "--add-opens=java.desktop/sun.awt.X11=ALL-UNNAMED",
                    "-cp",
                    runtime_classpath,
                    PROBE_CLASS,
                ]
                if label not in ("canonical", "mutant"):
                    probe_command.insert(1, "-Dfreeplane.acceptance.mode=" + label)
                exit_code, stdout, stderr, timed_out, selected_display, attempts_used = run_probe_with_retries(
                    label, probe_command, base_env, java, display_session, log_prefix,
                    compile_result.returncode, label != "canonical", run_id, attempt_log, commands,
                )
                if label == "canonical":
                    canonical_exit_code = exit_code
                    canonical_timed_out = timed_out
                    canonical_stdout = stdout
                    canonical_stderr = stderr
                    canonical_attempts = attempts_used
                elif label == "mutant":
                    mutant_exit_code = exit_code
                    mutant_timed_out = timed_out
                    mutant_stdout = stdout
                    mutant_stderr = stderr
                    mutant_attempts = attempts_used
                else:
                    negative_results[label] = (compile_result.returncode, exit_code, stdout, stderr, timed_out)
                    negative_attempts[label] = attempts_used

        print("runtime-identity=" + str(LOG_DIR / "runtime-identity.txt"))
        print("probe-stdout=" + str(LOG_DIR / "probe.stdout"))
        print("probe-stderr=" + str(LOG_DIR / "probe.stderr"))
        print("probe-exit-code=" + str(canonical_exit_code))
        print("probe-timeout=" + str(canonical_timed_out))
        print("canonical-classification-present=" + str(classification_produced(canonical_stdout, canonical_stderr)))
        if canonical_exit_code != 0:
            emit_probe_diagnosis(
                attempt_log, run_id, "canonical", canonical_attempts or 1, "canonical-validation",
                canonical_exit_code, canonical_timed_out, canonical_stdout, canonical_stderr,
                "canonical probe did not complete successfully", display_session.display,
            )
            print("canonical-probe-validation=False")
            return 1 if canonical_exit_code is None else canonical_exit_code
        canonical_classifications_valid = (
            not canonical_timed_out
            and classifications_match(canonical_stdout, "NONE")
            and scenario_records_match(canonical_stdout, {
                "pointer-selection": "true", "placement-error-max": "0",
                "frame-correction-setBounds-delta": "0", "menu-showing-before-pointer": "true",
            })
        )
        print("canonical-classification-validation=" + str(canonical_classifications_valid))
        if not canonical_classifications_valid:
            emit_probe_diagnosis(
                attempt_log, run_id, "canonical", canonical_attempts, "canonical-validation",
                canonical_exit_code, canonical_timed_out, canonical_stdout, canonical_stderr,
                "canonical classification validation failed", display_session.display,
            )
            print("canonical classification validation failed", file=sys.stderr)
            return 1
        if mutant_offset is not None:
            offset_x, offset_y = mutant_offset
            expected_error = "placement-error=(" + str(offset_x) + "," + str(offset_y) + ")"
            mutant_valid = (
                mutant_compile_code == 0
                and mutant_exit_code is not None
                and mutant_exit_code != 0
                and not mutant_timed_out
                and expected_error in mutant_stdout
                and classifications_match(mutant_stdout, "PRODUCT")
            )
            print("mutant-offset=" + str(offset_x) + "," + str(offset_y))
            print("mutant-exit-code=" + str(mutant_exit_code))
            print("mutant-timeout=" + str(mutant_timed_out))
            print("mutant-classification-present=" + str(classification_produced(mutant_stdout, mutant_stderr)))
            print("mutant-validation=" + str(mutant_valid))
            if not mutant_valid:
                emit_probe_diagnosis(
                    attempt_log, run_id, "mutant", mutant_attempts or 1, "mutant-validation",
                    mutant_exit_code, mutant_timed_out, mutant_stdout, mutant_stderr,
                    "mutant validation failed", display_session.display,
                )
                print("mutant validation failed", file=sys.stderr)
                return 1
        negative_controls_valid = True
        for mode, (compile_code, exit_code, stdout, stderr, timed_out) in negative_results.items():
            expected_records = {
                "placement-error-max": "0", "menu-showing-before-pointer": "true",
                "pointer-selection": "true" if mode == "frame-writes-11" else "false",
                "frame-correction-setBounds-delta": "11" if mode == "frame-writes-11" else "0",
                "pointer-press-events": "1", "pointer-release-events": "1",
            }
            valid = (
                compile_code == 0 and exit_code is not None and exit_code != 0
                and not timed_out and not stderr and classifications_match(stdout, "PRODUCT")
                and scenario_records_match(stdout, expected_records)
            )
            if mode == "frame-writes-11":
                valid = valid and len(re.findall(
                    r"^failure-class=PRODUCT reason=popup-triggered frame correction scenario=\S+$",
                    stdout, re.MULTILINE,
                )) == len(SCENARIOS)
            else:
                valid = valid and len(re.findall(
                    r"^popup-pointer-event scenario=\S+ id=50[12] delivered=true consumed=true ",
                    stdout, re.MULTILINE,
                )) == 2 * len(SCENARIOS)
            print(mode + "-exit-code=" + str(exit_code))
            print(mode + "-timeout=" + str(timed_out))
            print(mode + "-classification-present=" + str(classification_produced(stdout, stderr)))
            print(mode + "-validation=" + str(valid))
            if not valid:
                emit_probe_diagnosis(
                    attempt_log, run_id, mode, negative_attempts.get(mode, 1), mode + "-validation",
                    exit_code, timed_out, stdout, stderr, mode + " validation failed", display_session.display,
                )
            negative_controls_valid = negative_controls_valid and valid
        return 0 if negative_controls_valid else 1
    finally:
        write_log(LOG_DIR / "commands.txt", "\n".join(commands) + "\n")
        display_session.close()


if __name__ == "__main__":
    try:
        sys.exit(main())
    except Exception as exception:
        print("runner-error=" + exception.__class__.__name__ + ": " + str(exception), file=sys.stderr)
        sys.exit(1)
