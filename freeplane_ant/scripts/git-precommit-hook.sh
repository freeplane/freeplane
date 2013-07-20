#!/bin/sh

git diff-index --name-only --cached HEAD | grep .properties | tr \\n \\0 | xargs -0 java -cp ".git/hooks/freeplaneant.jar;.git/hooks/ant.jar" -Dorg.freeplane.ant.FormatTranslation.eolStyle=unix org.freeplane.ant.FormatTranslation

exit
