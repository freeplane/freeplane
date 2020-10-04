# export JAVA_HOME=~/Apps/mac/jdk-11.0.7.jdk/Contents/Home
# export JAVA_HOME=~/Apps/mac/jdk-12.0.2.jdk/Contents/Home
export JAVA_HOME=~/Apps/mac/jdk-14.0.1.jdk/Contents/Home
export GRADLE_BIN=/Users/stuart/Apps/mac/gradle-6.5.1/bin
export PATH=${GRADLE_BIN}:${JAVA_HOME}/bin:${PATH}

cp settings.gradle settings.gradle.orig
sed -i -e "s{'freeplane_debughelper',{//'freeplane_debughelper',{" settings.gradle

# cp ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/MenuBuildProcessFactoryTest.java ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/MenuBuildProcessFactoryTest.java.orig
# echo "" > ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/MenuBuildProcessFactoryTest.java

# cp ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/JMenuItemBuilderTest.java ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/JMenuItemBuilderTest.java.orig
# echo "" > ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/JMenuItemBuilderTest.java

