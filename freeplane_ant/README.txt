Custom Ant tasks for use in freeplane.
======================================

 - target "jar" builds build/libs/freeplaneant-<version>.jar
 - target "test" performs some tests

Task FormatTranslation
----------------------
(e.g. "gradle format_translation")
formats a translation file and writes the result to another file.

The following transformations are made:
 - sort lines (case insensitive)
 - remove duplicates
 - if a key is present multiple times, then entries marked as
   [translate me] and [auto] are removed in favor of normal entries.
 - newline style is changed to the platform default.

Attributes:
 - dir: the input directory (default: ".")
 - outputDir: the output directory. Overwrites existing files if
   outputDir equals the input directory (default: the input directory)
 - includes: wildcard pattern (default: all regular files).
 - excludes: wildcard pattern, overrules includes (default: no
   excludes).
 - writeIfUnchanged: by default sorted files are not written if they
   are unchanged after formatting. Set to "true" to change this
   behaviour.

Task FormatTranslationCheck
---------------------------
(e.g. "gradle check_translation")
Does the same thing as FormatTranslation with the following exceptions:

- no files are written or changed.
- only the formatting status of each file is reported.
- the build fails unless failonerror="false"

Attributes:
 - all attributes of the FormatTranslation task without
   "outputDir" and "writeIfUnchanged"
 - failonerror: boolean. By default the build fails if some wrongly
   formatted file is found. Set to "false" to change this behaviour.
