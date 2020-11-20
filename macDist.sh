. ./setupPaths.sh
gradle clean             || exit $?
gradle build             || exit $?
gradle dist              || exit $?
gradle macosapp_makeapp  || exit $?
gradle dmg4mac           || exit $?
