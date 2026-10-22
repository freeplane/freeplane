# Repository Guidelines

## Project Structure & Module Organization
- Gradle multi-project: `freeplane/` (app), `freeplane_api/`, `freeplane_framework/`, platform helpers (e.g., `freeplane_mac/`), and plugins in `freeplane_plugin_*`.
- Build artifacts and launchers in `BIN/` (e.g., `BIN/freeplane.sh`, `BIN/freeplane.bat`, `BIN/core/...`).
- Tests under each module’s `src/test/java` (e.g., `freeplane/src/test/java/...`, `freeplane_api/src/test/java/...`).
- Packaging helpers live in `*.dist.gradle` and `DIST/`.
- Project task directory is `ai-specs/tasks`

## Build, Test, and Development Commands
- For build use `java` from Java from `~/.sdkman/candidates/java/21.0.8-zulu`
- Use `gradle` (not `gradlew`/`maven`). Examples:
  - Compile app: `gradle :freeplane:compileJava`
  - Test all or module: `gradle test` / `gradle :freeplane:test`
  - Distributions: `gradle dist` | `gradle mac.dist` | `gradle win.dist` | `gradle linux-packages`
- After a full build, run:
  - Unix/macOS: `BIN/freeplane.sh`
  - Windows: `BIN\freeplane.bat`

## Coding Style & Naming Conventions
- Java 8 target; runtime Java 8 or 11–23. Encoding: UTF-8. Indent: 4 spaces.
- Packages: `org.freeplane.*`. New plugins: `freeplane_plugin_<feature>`; configure OSGi exports/bnd in module `build.gradle`.
- Keep public APIs in `freeplane_api`; avoid leaking internals across modules.

## Testing Guidelines
- JUnit 4, AssertJ, Mockito. Name tests `*Test` or behavior-style (e.g., `RuleReferenceShould`).
- Add `-PTestLoggingFull` for verbose failures

## Translations & i18n
- Files: `freeplane/src/editor/resources/translations/Resources_*.properties`, `freeplane/src/viewer/resources/translations/Resources_en.properties`.
- Encoding: ISO-8859-1 with `\uXXXX` escapes. Convert using `native2ascii`.
- Always run: `gradle format_translation` after edits.
- Quick validation:
  - `file Resources_*.properties | grep -v "ASCII text"` (no output expected)
  - `grep -n "u[0-9][0-9][0-9][0-9]" Resources_*.properties`

## Commit & Pull Request Guidelines
- Commits: imperative subject (e.g., "Fix outline scroll stutter"), link issues (`#123`). Verify diffs: `git diff`, `git diff --cached`.
- Commit identifiers (repository policy): task-related commits must include a Ticket ID when one exists. If no Ticket ID exists, use the Task Identifier. Updates not related to any task may omit identifiers. This repository policy informs the constitution's "non-task updates" exception.
- PRs: clear description, rationale, tests/coverage notes, and screenshots/gifs for UI changes. Keep scope focused to one feature/module.

## Agent-Specific Instructions
- Ask for help immediately when blocked or uncertain; don’t guess.
- Apply minimal-change fixes; avoid refactors unless approved.
- Use verification gates before logic changes/refactors; describe planned diffs first.
- Answer user questions fully before coding. Pause only when blocked by missing scope/constraints or rule conflict; when pausing, state the exact blocking rule and exact missing input in one sentence.
- Never modify core logic without explicit approval; prefer parameterization over restructuring.

## Legacy Removal Policy
- Remove obsolete code by default when behavior or structure changes.
- Do not keep compatibility fallbacks unless explicitly requested by the user.
- If retaining legacy code is considered, get explicit user approval before implementation.
- If a legacy path is intentionally retained, document why and when it will be removed.
- Prefer one active execution path over parallel old/new implementations.
