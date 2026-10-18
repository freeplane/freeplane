#!/usr/bin/env python3
"""Compile and run the bounded X11 popup-coordinate acceptance probe."""

from __future__ import annotations

import argparse
import hashlib
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


def classifications_match(stdout: str, expected_class: str) -> bool:
    lines = [line for line in stdout.splitlines() if line.startswith("failure-class=")]
    classifications = re.findall(
        r"^failure-class=(PRODUCT|ENVIRONMENT|NONE) reason=.+ scenario=(\S+)$", stdout, re.MULTILINE
    )
    return len(lines) == len(SCENARIOS) and sorted(classifications) == sorted(
        (expected_class, scenario) for scenario in SCENARIOS
    )


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
        partial_stdout = timeout.stdout or ""
        partial_stderr = timeout.stderr or ""
        terminate_process_group(process)
        stdout, stderr = process.communicate()
        return 124, partial_stdout + stdout, partial_stderr + stderr, True


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


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--mutant-offset",
        help="also run a menu.show X,Y offset mutant on the same isolated display",
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

    display_session = IsolatedDisplay(LOG_DIR, base_env)
    selected_display = display_session.start()
    run_env = dict(base_env)
    run_env["DISPLAY"] = selected_display
    try:
        runtime_identity = build_runtime_identity(run_env, java, selected_display)
        write_log(LOG_DIR / "runtime-identity.txt", "\n".join(runtime_identity) + "\n")

        classpath = os.pathsep.join(str(path) for path in (build_classes, flatlaf, jna, jna_platform))
        commands: List[str] = [
            "repository-root=" + str(REPOSITORY_ROOT),
            "probe-source=" + str(probe_source),
            "host-display=" + base_env.get("DISPLAY", ""),
            "selected-display=" + selected_display,
            "display-selection=private Xvfb with Openbox window manager",
            "xvfb-command=" + shell_command(
                ["Xvfb", selected_display, "-screen", "0", PRIVATE_SCREEN, "-nolisten", "tcp", "-ac"]
            ),
            "window-manager-command=" + shell_command(["openbox", "--sm-disable"]),
            "flatlaf-jar=" + str(flatlaf),
            "jna-jar=" + str(jna),
            "jna-platform-jar=" + str(jna_platform),
            "compile-classpath=" + classpath,
        ]

        canonical_exit_code: Optional[int] = None
        canonical_timed_out = False
        canonical_stdout = ""
        mutant_exit_code: Optional[int] = None
        mutant_timed_out = False
        mutant_stdout = ""
        mutant_compile_code: Optional[int] = None

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
                compile_result = completed(compile_command, run_env)
                mutant_suffix = "-shift-" + str(mutant_offset[0]) + "-" + str(mutant_offset[1]) if mutant_offset else ""
                if label == "canonical":
                    stdout_path = LOG_DIR / "javac.stdout"
                    stderr_path = LOG_DIR / "javac.stderr"
                    result_path = LOG_DIR / "probe.result.txt"
                else:
                    stdout_path = LOG_DIR / ("falsifiability" + mutant_suffix + ".javac.stdout")
                    stderr_path = LOG_DIR / ("falsifiability" + mutant_suffix + ".javac.stderr")
                    result_path = LOG_DIR / ("falsifiability" + mutant_suffix + ".result.txt")
                write_log(stdout_path, compile_result.stdout)
                write_log(stderr_path, compile_result.stderr)
                if label == "canonical":
                    canonical_compile_code = compile_result.returncode
                else:
                    mutant_compile_code = compile_result.returncode
                if compile_result.returncode != 0:
                    write_probe_result(
                        result_path,
                        selected_display,
                        compile_result.returncode,
                        None,
                        False,
                        expected_mutant_failure=label == "mutant",
                    )
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
                commands.append(label + "-probe-command=" + shell_command(probe_command))
                exit_code, stdout, stderr, timed_out = bounded_run(probe_command, run_env, 90)
                if label == "canonical":
                    stdout_path = LOG_DIR / "probe.stdout"
                    stderr_path = LOG_DIR / "probe.stderr"
                    canonical_exit_code = exit_code
                    canonical_timed_out = timed_out
                    canonical_stdout = stdout
                else:
                    stdout_path = LOG_DIR / ("falsifiability" + mutant_suffix + ".stdout")
                    stderr_path = LOG_DIR / ("falsifiability" + mutant_suffix + ".stderr")
                    mutant_exit_code = exit_code
                    mutant_timed_out = timed_out
                    mutant_stdout = stdout
                write_log(stdout_path, stdout)
                write_log(stderr_path, stderr)
                write_probe_result(
                    result_path,
                    selected_display,
                    compile_result.returncode,
                    exit_code,
                    timed_out,
                    expected_mutant_failure=label == "mutant",
                )

        write_log(LOG_DIR / "commands.txt", "\n".join(commands) + "\n")
        print("runtime-identity=" + str(LOG_DIR / "runtime-identity.txt"))
        print("probe-stdout=" + str(LOG_DIR / "probe.stdout"))
        print("probe-stderr=" + str(LOG_DIR / "probe.stderr"))
        print("probe-exit-code=" + str(canonical_exit_code))
        print("probe-timeout=" + str(canonical_timed_out))
        if canonical_exit_code != 0:
            print("probe failed", file=sys.stderr)
            return 1 if canonical_exit_code is None else canonical_exit_code
        canonical_classifications_valid = classifications_match(canonical_stdout, "NONE")
        print("canonical-classification-validation=" + str(canonical_classifications_valid))
        if not canonical_classifications_valid:
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
            print("mutant-validation=" + str(mutant_valid))
            if not mutant_valid:
                print("mutant validation failed", file=sys.stderr)
                return 1
        return 0
    finally:
        display_session.close()


if __name__ == "__main__":
    try:
        sys.exit(main())
    except Exception as exception:
        print("runner-error=" + exception.__class__.__name__ + ": " + str(exception), file=sys.stderr)
        sys.exit(1)
