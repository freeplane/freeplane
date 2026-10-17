# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build System and Common Commands

Freeplane uses Gradle as its primary build system. **Always use `gradle` command** (not `gradlew` or `maven`).

### Commands for Claude Code
- `gradle :freeplane:compileJava` - Compile freeplane subproject (run before committing)
- `gradle :subproject:compileJava` - Compile specific subproject if modified
- `gradle :freeplane:test` - Run tests for validation
- `gradle format_translation` - **Always run after changing language resource files**

### Distribution Commands
- `gradle dist` - Create distribution packages
- `gradle mac.dist` - Create macOS distribution
- `gradle win.dist` - Create Windows distribution
- `gradle linux-packages` - Create Linux packages

## Project Architecture

Freeplane is a Java-based mind mapping application built with OSGi architecture and Swing UI.

### Core Architecture
- **OSGi Framework**: Uses Knopflerfish OSGi framework for modular plugin system
- **Multi-module Gradle project**: 17+ submodules with clear separation of concerns
- **Java 8 compatibility**: Targets Java 8 for broad compatibility - avoid Java 9+ features
- **Plugin system**: Extensible through OSGi plugins for features like LaTeX, scripting, SVG support

### Key Modules
- `freeplane` - Core application (org.freeplane.core bundle)
- `freeplane_api` - Public API for plugin development
- `freeplane_framework` - OSGi framework and launcher
- `freeplane_plugin_*` - Feature plugins (script, latex, markdown, etc.)
- `freeplane_mac` - macOS-specific functionality
- `JOrtho_0.4_freeplane` - Spell checking component

### Source Structure
- `src/main/java` - Production code
- `src/test/java` - Unit tests
- `src/editor/resources` - Editor-specific resources (images, translations, CSS)
- `src/viewer/resources` - Viewer-specific resources
- `src/external/resources` - External resources (templates, XSLT transformations)

### Project Directory Structure
**CRITICAL**: From the git repository root (where development happens), the structure is:

```
. (git repository root - where Claude Code starts)
├── freeplane/                               ← Main Gradle subproject  
│   ├── src/editor/resources/translations/   ← Translation files location
│   ├── src/viewer/resources/
│   └── src/main/java/
├── freeplane_api/                           ← API subproject  
├── freeplane_framework/                     ← Framework subproject
├── JOrtho_0.4_freeplane/                    ← Spell checking component
└── other subprojects...
```

**Key Paths from Git Root**:
- **Translation files**: `freeplane/src/editor/resources/translations/Resources_*.properties`
- **Main source**: `freeplane/src/main/java/`
- **Build output**: `BIN/` (global build directory)

**Path Usage**:
- All development paths are relative to git repository root
- The `freeplane/` subdirectory contains the main application subproject
- Absolute paths vary between installations and should not be used in committed files

### Build Output
- `BIN/` - Global build output directory containing the complete application
- Plugin JARs are copied to `BIN/plugins/{plugin.id}/lib/`
- Core application JARs go to `BIN/core/org.freeplane.core/lib/`

## Development Guidelines

### Code Standards (from .cursor/rules)
- **No comments or Javadoc** in final code - use self-documenting naming
- **Incremental refactoring** - small testable steps with commits after each step
- **Extract only what's used** - avoid speculative generality
- **Single responsibility** - each extracted class should have clear purpose
- **Remove unused imports** - Clean up imports after coding changes to keep code tidy

### Feature-Driven Testing Process (MANDATORY)

**CORE PRINCIPLE**: Tests follow features, not vice versa. Every feature development session MUST follow this pattern:

1. **Extract Business Logic First** - Pull complex logic out of UI event handlers into testable classes
2. **Test Extracted Logic Immediately** - Write focused unit tests for the extracted business logic  
3. **Implement Feature Using Tested Components** - Build the feature using tested, reliable building blocks
4. **Minimal UI Integration** - Keep UI code as thin wiring that delegates to tested logic

**CLAUDE'S RESPONSIBILITY**: 
- **ALWAYS suggest extraction opportunities** when seeing complex logic in UI code
- **ALWAYS propose testing approach** before implementing any non-trivial business logic
- **PROACTIVELY identify** when feature work could benefit from extract-test-integrate pattern
- **INTERRUPT implementation** if business logic is being mixed directly into UI event handlers
- **REMIND** about testing utilities and patterns available for current work

**DEVELOPER RESPONSIBILITY**:
- **FOLLOW the extract-test-integrate pattern** for all feature development
- **EXPECT Claude to urge** this approach and take the suggestions seriously
- **RESIST the temptation** to modify complex UI code directly without extraction

**SUCCESS METRICS**:
- Feature development feels faster due to immediate feedback loops
- Fewer debugging sessions needed (logic verified through tests)
- More confidence when making changes to existing functionality
- Gradual accumulation of testable, reusable business logic components

**VIOLATION CONSEQUENCES**: Technical debt accumulates, debugging becomes harder, feature development slows down, regression risk increases.

### OSGi Bundle Configuration
- Plugin bundles require `Bundle-Activator` and `Require-Bundle: org.freeplane.core`
- Bundle dependencies are externalized via `Bundle-ClassPath`
- Core module exports packages for plugin consumption

### Translation System
- **Translation files location**: `freeplane/src/editor/resources/translations/Resources_*.properties` (relative to git root)
- **Gradle tasks**: `gradle check_translation` and `gradle format_translation` 
- **Language support**: 25+ languages including RTL languages (Arabic, Hebrew)

#### Translation File Encoding
- **CRITICAL**: Properties files use ISO-8859-1 encoding with Unicode escapes
- **Non-ASCII characters**: Must be Unicode-escaped (e.g., `\u041E` for Cyrillic О)
- **Examples**: 
  - Russian: `OptionPanel.enabled=\u0412\u043A\u043B\u044E\u0447\u0435\u043D\u043E`
  - Chinese: `OptionPanel.immediate=\u7ACB\u5373`
  - Arabic: `OptionPanel.disabled=\u0645\u0639\u0637\u0644`
- **Tool requirement**: Use tools that properly handle Unicode escaping for non-Latin scripts
- **Verification**: Check existing translations in target language for proper escape patterns

#### Translation File Encoding Requirements
- **Properties files MUST use ISO-8859-1 encoding** with Unicode escapes for non-ASCII characters
- **All non-ASCII characters must be Unicode-escaped**: Use `\uXXXX` format (e.g., `\u041E` for Cyrillic О)
- **Arabic text example**: `مفعل` becomes `\u0645\u0641\u0639\u0644`
- **Accented characters example**: `Activé` becomes `Activ\u00E9`

#### MANDATORY: Unicode Conversion Tool Usage
- **ALWAYS use `native2ascii` tool** for converting UTF-8 text to proper Unicode escapes
- **NEVER use generic text editors** or automated translation tools directly on .properties files
- **Required workflow**: UTF-8 temp file → `native2ascii input.txt output.properties` → merge into target file
- **Validation step**: After changes, verify with `file *.properties` (must show "ASCII text", not binary)
- **Corruption prevention**: Prevents double UTF-8 encoding, broken escapes, and null byte injection
- **Always run**: `gradle format_translation` after any translation file modifications

**Why Critical**: Automated tools without proper Java properties support cause systematic Unicode corruption (null bytes, double encoding, broken escapes) that affects all translation files and breaks Weblate integration.

#### MANDATORY: Automated Translation Validation
**For any automated translation work (Claude Code, translation tools), ALWAYS validate after editing .properties files:**

```bash
# 1. Check file integrity (must be ASCII text)
file freeplane/src/editor/resources/translations/Resources_*.properties | grep -v "ASCII text"

# 2. Check for broken Unicode escapes  
cd freeplane/src/editor/resources/translations/
grep -l 'u[0-9][0-9][0-9][0-9]' *.properties

# 3. Validate git diff before commit
git diff | head -20  # Verify only expected changes, no deletions
```

**Validation Rules for Automated Tools:**
- **STOP immediately** if any file shows as binary/HTML instead of ASCII text
- **STOP immediately** if broken Unicode patterns found (u0159 instead of \u0159)
- **STOP immediately** if git diff shows unexpected content deletions
- **Always run** `gradle format_translation` after any translation changes

#### Translation Key Conventions
- **OptionPanel prefix**: UI preference keys use `OptionPanel.{key}={value}` format
- **Separator titles**: Use `OptionPanel.separator.{name}={title}` for section headers
- **Choice values**: Combo box options use same key as preference choice value
- **Alphabetical sorting**: Properties are sorted alphabetically ignoring case (intentional)
- **Always run format_translation**: After any translation file changes to maintain sorting

### Testing
- JUnit 4.13.2 with Hamcrest and Mockito
- AssertJ for fluent assertions
- Use `gradle test` or `gradle :module:test` for specific modules
- Test logging can be enabled with `-PTestLoggingFull`

## Plugin Development
- Extend from OSGi bundle structure
- Use `freeplane_api` for public interfaces
- Follow naming convention: `org.freeplane.plugin.{name}`
- Bundle activator required for OSGi lifecycle management

## Code Structure Learnings

### Mouse Event Handling Architecture
- **NodeSelector** - handles node selection timing and behavior
- **NodeFolder** - handles node folding timing and behavior (independent from selection)
- **DefaultNodeMouseMotionListener** - coordinates mouse events between NodeSelector and NodeFolder
- Events are routed based on mouse regions (folding vs selection areas)

### Filter and Map Context Patterns
- **Always get Filter from MapView**: Use `map.getFilter()` from the MapView context
- **After map.select()**: Can use `controller.getSelection().getFilter()` - they're equivalent
- **Follow existing patterns**: Most code operates on selected node/map view with consistent filter usage
- **Method signatures matter**: `setFolded(node, boolean, filter)` vs `unfoldAndScroll(node, filter)` vs `toggleFoldedAndScroll(node)` (handles filter internally)

### Property Management and Migration
- **ApplicationResourceController.isPropertySetByUser()**: Checks if property exists in user's props (reliable way to detect user customization)
- **Static migration blocks**: Use for one-time property updates on startup
- **Self-documenting method names**: Replace explanatory comments with well-named methods
- **Default properties**: Add to `freeplane.properties` for system defaults

### Mouse Event Coordination
- **Separate timing from behavior**: Shared timing (`immediate`/`delayed`) used by both selection and folding
- **Regional event routing**: `isInFoldingControl()` vs `isInside()` determines which handler processes the event
- **Legacy compatibility**: Support both old `selection_method` and new granular configuration

### OSGi and Complex Architecture
- Freeplane is complex software without comprehensive tests
- Follow existing patterns rather than reinventing
- Check method signatures carefully (compilation catches parameter mismatches)
- Use existing controller methods rather than lower-level operations

### Java Code Quality Standards
- **Reduce visibility**: Use package-private classes for internal UI components, only expose what clients need
- **Static organization**: Static blocks first, constants grouped after, inner classes positioned strategically
- **Inner class optimization**: Static when no outer instance access needed, non-static when accessing outer fields
- **Migration pattern**: Clean separation of initialization logic from business logic using static blocks with helper methods at bottom
- **Field organization**: Instance fields grouped logically after constants, methods ordered by visibility

### Logging System
- **Freeplane uses proprietary logging utilities** (not standard Java logging)
- Standard Java logging configuration in `logging.properties` only affects Swing and other Java log records
- For Freeplane-specific logging, use the proprietary logging utilities directly
- Use `LogUtils.warn()` and similar methods for Freeplane logging (as seen in existing codebase)

## Git Workflow

### Branches
- **`1.13.x`**: Main branch of the fork (tracks `upstream/1.13.x`). Never push to it unless asked.
- **`feature/xxx`**: Feature branches, started from `1.13.x`. `origin/1.13.x` can be merged into a feature branch to avoid diverging.
- Feature branches must always be prefixed with `feature/` and named **by subject** rather than by precise change (e.g. `feature/treeviewer`, not `feature/treetable-borders-and-rename`), so that follow-ups naturally attach to it.
- **Never delete a Git branch** without being explicitly asked.

### Choosing the feature branch
An improvement or fix that continues an existing subject must be done **on the existing feature branch of that subject**, not on a new one, so that the history shows it is the continuation.

Before creating a branch, look for an existing one on the same subject:
- not yet merged into `origin/1.13.x`: `git branch -r --no-merged origin/1.13.x`;
- already merged into `origin/1.13.x`: `git branch -r --merged origin/1.13.x`.

Then:
- **A branch deals with the same subject**: resume it, announcing it in one sentence ("resuming `feature/xxx`"). Only merge `origin/1.13.x` into it when necessary (dependency on newer code, or conflict), since each merge adds a useless node to the graph.
- **The subject matches a feature already merged into `1.13.x`, OR there is doubt between several branches**: ask, proposing the candidate branch.
- **No branch matches**: create a new one.

### Commits, push and merge
- **Commits**: Messages in casual French, single line, no Conventional Commits, no Co-Authored-By. Stage files automatically when committing (except files not managed by Git). Never merge automatically while committing.
- **Push**: On a **feature branch**, commit and push automatically without asking. On any other branch, never push unless asked.
- **Merge**: Never fast-forward (except when pulling). Never merge unless asked.

### Git ignore rules
Never manage in Git, unless explicitly asked:
- Maven `target/` folders
- Binary files (`.class`, `.jar`, `.war`, etc.)
- Folders starting with `.` (`.vscode`, `.settings`, etc.)

For other files starting with `.`, ask first.

## Distribution and Packaging
- Multi-platform support (Windows, macOS, Linux)
- Portable application support via PortableApps format
- Windows installer via Inno Setup
- macOS DMG with codesigning support
- Linux packages for Debian-based systems