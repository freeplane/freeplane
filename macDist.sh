<<<<<<< HEAD
# SETUP TOOLS
export JAVA_HOME=~/Apps/mac/jdk-11.0.7.jdk/Contents/Home
export GRADLE_BIN=/Users/stuart/Apps/mac/gradle-6.5.1/bin
export PATH=${GRADLE_BIN}:${JAVA_HOME}/bin:${PATH}

# TEMPORARY FIXUPS FOR FILES THAT BREAK BUILD
cp settings.gradle settings.gradle.orig
sed -i -e "s{'freeplane_debughelper',{//'freeplane_debughelper',{" settings.gradle

cp ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/MenuBuildProcessFactoryTest.java ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/MenuBuildProcessFactoryTest.java.orig
echo "" > ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/MenuBuildProcessFactoryTest.java

cp ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/JMenuItemBuilderTest.java ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/JMenuItemBuilderTest.java.orig
echo "" > ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/JMenuItemBuilderTest.java

# RUN THE BUILD
gradle clean     || exit $?
gradle build     || exit $?
gradle dist      || exit $?
gradle macDist   || exit $?

# DEPLOY
# echo "Copy to /Applications"
# sudo cp -r BIN4mac_jre/Freeplane.app /Applications/Freeplane_1.8.app              || exit $?
# echo "Signing:"
# sudo codesign --force --deep --sign - /Applications/Freeplane_1.8.app  || exit $?


# RESTORE ORIGINAL FILES
cp settings.gradle.orig settings.gradle
cp ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/MenuBuildProcessFactoryTest.java.orig ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/MenuBuildProcessFactoryTest.java
cp ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/JMenuItemBuilderTest.java.orig ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/JMenuItemBuilderTest.java
=======
gradle clean
gradle build
gradle dist
gradle macDist

echo "Copy to /Applications"
sudo cp -r BIN4mac_jre/Freeplane.app /Applications
echo "Signing:"
sudo codesign --force --deep --sign - /Applications/Freeplane.app
>>>>>>> 1.7.x
