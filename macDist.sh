. ./setup.sh
gradle clean             || exit $?
gradle build             || exit $?
gradle dist              || exit $?
gradle macosapp_makeapp  || exit $?
gradle dmg4mac           || exit $?

# echo "Copy to /Applications"
# sudo cp -r BIN4mac_jre/Freeplane.app /Applications                 || exit $?
# echo "Signing:"
# sudo codesign --force --deep --sign - /Applications/Freeplane.app  || exit $?
