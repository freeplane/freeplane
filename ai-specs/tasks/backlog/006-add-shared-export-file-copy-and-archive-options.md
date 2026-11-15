# Task: Add shared HTML export file-copy and ZIP options

- **Task Identifier:** 2026-11-13-export-options
- **Scope:** Add shared options for existing HTML exports and the dynamic
  SVG HTML export when available:
  - Optional ZIP output containing the generated HTML and accompanying
    files, while retaining the existing non-ZIP output.
  - An independent setting for including regular files directly
    referenced by relative hyperlinks, applicable with or without ZIP.
  - Files needed to display the exported content remain part of the
    output independently of the setting for hyperlink targets.
  - Include the supporting automated tests and any required translation
    and documentation updates in this task.
- **Motivation:** Allow HTML and accompanying files to be distributed
  together without requiring ZIP, and allow linked local documents to be
  included explicitly rather than automatically. Shared options avoid
  implementing different file-copy and archive behavior in each HTML
  exporter.
- **Constraints:**
  - Keep each HTML export's existing non-ZIP output available. ZIP is
    optional, not a replacement for that output.
  - Default the setting for including relative hyperlink targets to
    disabled. It is independent of the ZIP choice.
  - Limit hyperlink-target copying to directly referenced regular local
    files. Do not recursively follow links inside those files or include
    whole directories.
  - Preserve relative links and the directory structure needed by them;
    do not flatten the copied files into one directory.
  - Recipients extract ZIP output before opening its HTML in a browser.
  - The options must be useful for existing HTML exports independently of
    the dynamic SVG exporter. Neither task requires the other to ship.
