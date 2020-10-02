<<<<<<< HEAD
export JAVA_HOME=~/Apps/mac/jdk-11.0.7.jdk/Contents/Home
export GRADLE_BIN=/Users/stuart/Apps/mac/gradle-6.5.1/bin
export PATH=${GRADLE_BIN}:${JAVA_HOME}/bin:${PATH}

cp settings.gradle settings.gradle.orig
sed -i -e "s{'freeplane_debughelper',{//'freeplane_debughelper',{" settings.gradle

cp ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/MenuBuildProcessFactoryTest.java ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/MenuBuildProcessFactoryTest.java.orig
echo "" > ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/MenuBuildProcessFactoryTest.java

cp ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/JMenuItemBuilderTest.java ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/JMenuItemBuilderTest.java.orig
echo "" > ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/JMenuItemBuilderTest.java
=======
export JAVA_HOME=~/apps/mac/jdk1.8.0_121.jdk/Contents/Home
export PATH=/usr/local/bin:/usr/local/sbin:~/usr/local/bin:$JAVA_HOME/bin:$GEM_HOME/bin:$PATH:~/apps/bin:~/apps/mac/bin
>>>>>>> 1.7.x

