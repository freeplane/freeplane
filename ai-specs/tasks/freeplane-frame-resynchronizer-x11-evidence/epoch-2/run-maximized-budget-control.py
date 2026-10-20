#!/usr/bin/env python3
"""Falsify the maximized-target budget without triggering the popup-write gate.

Run after run-acceptance.py so logs/commands.txt supplies the verified classpath.
Only a disposable copy of the acceptance probe receives the mutation.
"""

from pathlib import Path
import difflib
import os
import re
import runpy
import sys
import tempfile


EVIDENCE_DIR = Path(__file__).resolve().parent
RUNNER = runpy.run_path(str(EVIDENCE_DIR / "run-acceptance.py"))
LOG_DIR = EVIDENCE_DIR / "logs"
PREFIX = "falsifiability-maximized-target-budget-11"


def main() -> int:
    classpaths = [line.split("=", 1)[1] for line in (LOG_DIR / "commands.txt").read_text().splitlines()
                  if line.startswith("compile-classpath=")]
    if len(classpaths) != 1:
        raise RuntimeError("run the canonical acceptance runner first to record its classpath")
    classpath = classpaths[0]
    env = os.environ.copy()
    java_home = Path(env.get("JAVA_HOME", str(RUNNER["JAVA_HOME_DEFAULT"])))
    source = EVIDENCE_DIR / "PopupCoordinateAcceptanceProbe.java"
    original = source.read_text(encoding="utf-8")
    anchor = "        Thread.sleep(STATE_SETTLE_MILLIS);\n    }"
    mutation = """        Thread.sleep(STATE_SETTLE_MILLIS);
        if (maximized) {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    final Rectangle unchangedTarget = frame.getBounds();
                    for (int request = 0; request < 11; request++) {
                        frame.setBounds(unchangedTarget);
                    }
                }
            });
        }
    }"""
    if original.count(anchor) != 1:
        raise RuntimeError("could not identify the state-settlement mutation site")
    mutated = original.replace(anchor, mutation)
    RUNNER["write_log"](LOG_DIR / (PREFIX + ".patch"), "".join(difflib.unified_diff(
        original.splitlines(keepends=True), mutated.splitlines(keepends=True),
        fromfile="PopupCoordinateAcceptanceProbe.java", tofile="disposable-budget-control.java", n=0,
    )))
    display_logs = LOG_DIR / "budget-control-display"
    display_logs.mkdir(exist_ok=True)
    display_session = RUNNER["IsolatedDisplay"](display_logs, env)
    try:
        display = display_session.start()
        env["DISPLAY"] = display
        RUNNER["write_log"](LOG_DIR / (PREFIX + ".runtime-identity.txt"), "\n".join(
            RUNNER["build_runtime_identity"](env, java_home / "bin/java", display)
        ) + "\n")
        with tempfile.TemporaryDirectory(prefix="freeplane-popup-budget-control-") as temporary_directory:
            temporary_root = Path(temporary_directory)
            mutant_source = temporary_root / source.name
            mutant_source.write_text(mutated, encoding="utf-8")
            classes = temporary_root / "classes"
            classes.mkdir()
            compile_command = [java_home / "bin/javac", "--release", "8", "-encoding", "UTF-8",
                               "-cp", classpath, "-d", classes, mutant_source]
            probe_command = [java_home / "bin/java",
                             "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
                             "--add-opens=java.desktop/sun.awt.X11=ALL-UNNAMED",
                             "-cp", str(classes) + os.pathsep + classpath, RUNNER["PROBE_CLASS"]]
            RUNNER["write_log"](LOG_DIR / (PREFIX + ".commands.txt"),
                                "probe-source-sha256=" + RUNNER["sha256"](source) + "\n"
                                + "compile-command=" + RUNNER["shell_command"](compile_command) + "\n"
                                + "probe-command=" + RUNNER["shell_command"](probe_command) + "\n")
            compiled = RUNNER["completed"](compile_command, env)
            RUNNER["write_log"](LOG_DIR / (PREFIX + ".javac.stdout"), compiled.stdout)
            RUNNER["write_log"](LOG_DIR / (PREFIX + ".javac.stderr"), compiled.stderr)
            if compiled.returncode != 0:
                raise RuntimeError("budget control did not compile")
            exit_code, stdout, stderr, timed_out = RUNNER["bounded_run"](probe_command, env, 90)
        RUNNER["write_log"](LOG_DIR / (PREFIX + ".stdout"), stdout)
        RUNNER["write_log"](LOG_DIR / (PREFIX + ".stderr"), stderr)
        actual = re.findall(r"^failure-class=(NONE|PRODUCT|ENVIRONMENT) reason=(.+) scenario=(\S+)$",
                            stdout, re.MULTILINE)
        expected = [("PRODUCT", "maximized frame target budget exceeded", scenario)
                    if scenario in ("maximize", "remaximize")
                    else ("NONE", "all scenario gates passed", scenario)
                    for scenario in RUNNER["SCENARIOS"]]
        assertion_failures = [line for line in stdout.splitlines() if line.startswith("assertion-failure=")]
        valid = (
            exit_code == 1 and not timed_out and not stderr and actual == expected
            and RUNNER["scenario_records_match"](stdout, {
                "frame-correction-setBounds-delta": "0", "pointer-selection": "true",
                "placement-error-max": "0", "pointer-click-inside-item-bounds": "true",
            })
            and len(assertion_failures) == 2
            and all(line.startswith("assertion-failure=maximized frame target budget exceeded ")
                    for line in assertion_failures)
        )
        result_path = LOG_DIR / (PREFIX + ".result.txt")
        RUNNER["write_probe_result"](result_path, display, compiled.returncode, exit_code, timed_out, True)
        with result_path.open("a", encoding="utf-8") as result:
            result.write("independent-budget-validation=" + str(valid) + "\n")
        print("budget-control-exit-code=" + str(exit_code))
        print("budget-control-timeout=" + str(timed_out))
        print("independent-budget-validation=" + str(valid))
        return 0 if valid else 1
    finally:
        display_session.close()


if __name__ == "__main__":
    sys.exit(main())
