# Task: Fix autosave backups across filesystems
- **Ticket:** [#3009](https://github.com/freeplane/freeplane/issues/3009)
- **Status:** Design approved in chat; implemented and locally
  verified. Ubuntu 26.04 CI and real cross-filesystem validation remain
  pending. The user will push the commit; this session does not push.
- **Scope:** Make overwrite-mode autosave work when the map and its
  backup directory are on different filesystems, recover autosave
  after backup preparation fails, and isolate the autosave regression
  tests from process-global backup-directory state.
- **Motivation:** Issue #3009 reports five autosave test failures on
  Ubuntu 26.04 while the same revision passes on Ubuntu 24.04. Source
  inspection also identifies a user-facing limitation: overwrite-mode
  autosave moves the existing map into its backup directory with
  `File.renameTo()`, which can fail across filesystems. After backup
  preparation fails, the callback leaves its timer stopped.
- **Scenario:**
  - A map is stored on an external drive while the central backup
    directory is in the user's home directory. Autosave preserves the
    previous on-disk map in the backup directory, then writes the
    current map to its original location.
  - Backup preparation fails temporarily. The original map remains
    available, the map remains dirty, and autosave can retry at its
    configured interval without requiring another edit or manual save.
  - The autosave tests run alone or after tests that initialize the
    real application. Their backup files stay inside their own
    temporary directory and do not depend on another test's settings.
- **Constraints:**
  - Different directories are not the discriminator: directories on
    one filesystem can be renamed between. Do not use directory
    equality as a proxy for whether a move can succeed.
  - Keep the existing backup names, rotation count, central-backup
    preference, delete-on-exit policy, and backup-only mode.
  - Do not weaken the successful-overwrite assertions: a successful
    overwrite autosave must mark the map saved.
  - Do not write the current map if preparing its backup failed.
  - Keep filesystem operations separate from dirty-state and timer
    policy. Do not put autosave state or Swing timers in a generic
    file utility.
  - Do not create duplicate timers, restart a superseded timer, or
    restart autosave for a closed map.
  - Do not pin the CI runner as the fix, change dependencies, rewrite
    backup rotation, or redesign map serialization or atomic saves.
  - Preserve the successful-save timer reset introduced for #2740.
  - Do not change source or test files until this design is approved.
- **Briefing (before implementation):**
  - `DoAutomaticSave.actionPerformed(...)` checks the change count,
    stops the firing timer, rotates autosave backups, and either
    overwrites the map or writes a backup-only autosave file.
  - In overwrite mode it currently deletes the selected backup slot,
    calls `file.renameTo(tempFile)`, and saves only if that succeeds.
  - `MFileManager.save(...)` schedules a replacement timer after a
    successful full save resets the map's change count.
    `saveInternal(...)` also replaces the timer after backup-only
    writes and write failures.
  - `MMapModel` owns the active autosave timer and its enabled state.
    Closing the map disables autosave and stops its timer.
  - `MFileManager.singleBackupDirectory` is static. The autosave test
    replaces the current controller but neither sets nor restores
    that cached directory.
- **Research (before implementation):**
  - The default configuration enables a central backup directory at
    `{freeplaneuserdir}/.backup`.
  - Menu tests initialize `HeadlessFreeplaneRunner`, which installs a
    real `MFileManager` and can populate `singleBackupDirectory`
    before `MFileManagerAutosaveTest` runs.
  - `ResourceController.getResourceController()` delegates through the
    current controller, so the test does expose its resource mock to
    the autosave callback. The separately cached backup directory is
    not replaced by changing that controller.
  - The test uses one autosave backup. At that count, the rotation
    branches that return `null` after a failed internal rename cannot
    execute. Under its overwrite configuration, the reported generic
    backup-failure message points to the original-map rename.
  - Commons IO 2.19.0 is already a dependency. Inspection of the
    bundled `org.apache.commons.io.FileUtils.moveFile(File, File)`
    implementation confirms that it attempts `renameTo()` first and
    falls back to copying the file and deleting the source. It
    requires an absent destination and throws `IOException` on
    failure. If source deletion fails after copying, it attempts to
    remove the destination and reports failure.
  - Freeplane also has `org.freeplane.core.util.FileUtils`; that class
    currently has a copy operation but no move operation. These are
    different utility classes.
  - The exact Ubuntu 26.04 filesystem layout and the reason for its
    rename failure have not been verified. The CI-log download was
    denied with HTTP 403. Cross-filesystem moves are a confirmed
    production limitation, not a confirmed diagnosis of that runner.
- **Analysis:**
  - Prefer the existing Commons IO move utility over the earlier
    copy-only proposal. It preserves the existing rename path when
    that operation succeeds and handles a failed rename without a
    separate filesystem-detection heuristic.
  - This remains a move, not a retained copy: after successful backup
    preparation, the source path is absent until the normal save
    writes it again, as in the existing implementation. Atomic map
    replacement is not added by this task.
  - No new utility class or wrapper is needed solely to delegate to
    `org.apache.commons.io.FileUtils.moveFile(...)`.
  - Restarting a timer alone is insufficient for a retry without more
    edits: the current callback records the attempted change count
    before backup preparation. A failed attempt must not be treated
    as a successfully handled change count.
  - Replacing the timer on every exit is also unsafe: save operations
    already schedule replacements. Timer recovery must respect the
    active timer's identity and the map's lifecycle.
- **Design:**
  1. **Use the existing filesystem-independent move utility.**
     - Keep backup-directory selection and rotation in
       `MFileManager`; keep autosave orchestration in
       `DoAutomaticSave`.
     - Check directory creation and removal of an existing backup
       slot. Report failures with the source, destination and
       available exception instead of ignoring failed operations.
     - Replace the original-map `renameTo(...)` call with
       `org.apache.commons.io.FileUtils.moveFile(...)`. Do not add a
       same-directory or same-filesystem test first.
     - Call `MFileManager.save(model)` only after the move succeeds.
       Emit the autosave success status only when the save succeeds.
     - Leave same-directory backup-rotation renames unchanged.
  2. **Recover the stopped timer without claiming a failed backup.**
     - Read the observed change count into a local variable. Preserve
       the zero-change handling and read-only checks, but do not
       advance the handled change count before backup preparation.
     - Keep the previous handled count when directory creation,
       rotation, target cleanup, or the move fails. That keeps the
       same unsaved changes eligible for a later timer callback.
     - Put guarded timer recovery in a `finally` path. `MMapModel`
       remains responsible for allowing recovery only while autosave
       is enabled and the stopped timer is still the active timer.
       Add only the smallest model operation needed for that guard.
     - Resume the existing timer at its configured interval, not by
       immediately invoking the callback or creating a tight retry
       loop.
     - If a save operation has already installed a replacement timer,
       do not restart the old timer. Preserve the replacement's
       baseline and existing write-failure scheduling behavior.
     - Treat successful backup-only writes as handled without marking
       the map saved. Full saves retain the reset-to-zero baseline
       managed by `MFileManager.save(...)`.
  3. **Make the test fixture own its backup destination.**
     - Capture `MFileManager.singleBackupDirectory`, set it to a
       directory beneath the test's `TemporaryFolder`, and restore it
       in teardown, including after failures.
     - Use test-local reflective access to this existing private
       static field rather than exposing a production setter solely
       for testing or refactoring the global configuration in this
       task.
     - Keep original controller restoration. Stop every timer created
       by the fixture in teardown.
     - Extend the headless timer fixture as needed to test recovery
       and replacement identity; do not replace the save assertions
       with weaker checks for any backup file.
- **Callback flow:**
  ```text
  read the observed change count
  return for a read-only map or an already handled count
  record zero and return if the map was saved since the last callback
  validate the current mode and file manager before stopping the timer
  stop the firing timer
  try:
      prepare the backup directory and rotate backups
      if overwrite mode:
          remove the selected old backup slot, checking for failure
          move the original map into that slot using Commons IO
          save the current map; report success only if it succeeded
      otherwise:
          write the backup-only autosave through saveInternal
          report success only if it succeeded
      record the observed count only for a successful operation
  catch a preparation or I/O failure:
      report the failure; do not mark the map saved
  finally:
      ask MMapModel to resume the stopped timer only if it is still
      the active timer and autosave is still enabled
  ```
- **Test specification:**
  - **Automated tests:**
    - Retain all eight existing `MFileManagerAutosaveTest` cases,
      including matching change counts, manual saves, idle callbacks,
      backup-only behavior and failed writes.
    - Verify that backup preparation preserves the previous file's
      non-empty contents and that a successful overwrite writes the
      current contents to the original map location.
    - Verify that the autosave path uses the move utility, not a raw
      original-map `renameTo(...)` call.
    - Exercise the utility's failed-rename copy/delete behavior
      deterministically with real temporary files and a source file
      test double whose `renameTo(...)` returns false. This must not
      require a second mounted filesystem to run the normal suite.
    - Verify that failed backup preparation leaves the original file
      unchanged, the map dirty and the old change count eligible for
      another autosave attempt.
    - After a transient preparation failure is removed, fire the next
      scheduled callback without another edit and verify that it
      saves successfully.
    - Cover directory creation, backup rotation and move failures;
      none may permanently stop autosave or report success.
    - Verify that successful full saves, backup-only writes and
      existing write-failure paths do not leave two active timers.
    - Verify that a superseded timer and a timer belonging to a closed
      map are not restarted.
    - Run the autosave tests alone and with the menu tests that
      initialize the application; verify that the fixture restores
      the previous backup-directory state.
  - **Manual / environment validation:**
    - Run the targeted tests and full `freeplane` suite on Ubuntu
      26.04 without changing `ubuntu-latest`.
    - With a disposable map and backup directory on different real
      filesystems, verify repeated overwrite autosaves, backup
      contents and a manual save between autosave intervals.
    - Make the backup destination unavailable temporarily, then
      restore it; verify retry without another edit or manual save.
    - Do not claim the Ubuntu-specific cause confirmed until its
      actual source and destination paths/filesystems are checked.
- **Implementation and validation:**
  - [x] Add isolated fixture setup and failing regression coverage.
  - [x] Replace the original-map move and check preparation failures.
  - [x] Add guarded timer recovery and successful-count handling.
  - [x] Run targeted and full module tests locally.
  - [x] Review the source and test diff against the approved design.
  - [ ] Push and run Ubuntu 26.04 CI (user action).
  - [ ] Validate actual cross-filesystem autosaves and recovery.
- **Local verification:**
  - Before the production change: 22 autosave tests ran, with ten
    failing regression cases; all eight original cases passed with
    the isolated fixture.
  - After the production change: all 22 autosave tests passed.
  - Menu initialization and autosave tests in the same test run:
    23 tests passed.
  - Full `freeplane` suite: 710 tests, 708 passed, two skipped,
    zero failures and zero errors.
  - The forced-rename-failure test uses the real Commons IO move
    utility and real temporary files; it verifies copy/delete fallback
    without requiring another mounted filesystem.
  - Local execution used macOS and Zulu Java 21.0.8, not Ubuntu.
    CI image behavior and real cross-filesystem behavior remain
    unverified; no CI workflow or runner pin was changed.
  - Commands used (with
    `JAVA_HOME=/Users/dimitry/.sdkman/candidates/java/21.0.8-zulu`):
    ```sh
    gradle -Djava.net.preferIPv4Stack=true -Djava.awt.headless=true \
      :freeplane:test \
      --tests org.freeplane.features.url.mindmapmode.MFileManagerAutosaveTest \
      -PTestLoggingFull
    gradle -Djava.net.preferIPv4Stack=true -Djava.awt.headless=true \
      :freeplane:test \
      --tests org.freeplane.core.ui.menubuilders.MenuBuilderAcceptanceTest \
      --tests org.freeplane.features.url.mindmapmode.MFileManagerAutosaveTest \
      -PTestLoggingFull
    gradle -Djava.net.preferIPv4Stack=true -Djava.awt.headless=true \
      :freeplane:test -PTestLoggingFull
    ```
