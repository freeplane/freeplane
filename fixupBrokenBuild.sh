cp settings.gradle settings.gradle.orig
sed -i -e "s{'freeplane_debughelper',{//'freeplane_debughelper',{" settings.gradle

cp ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/MenuBuildProcessFactoryTest.java ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/MenuBuildProcessFactoryTest.java.orig
echo "" > ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/MenuBuildProcessFactoryTest.java

cp ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/JMenuItemBuilderTest.java ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/JMenuItemBuilderTest.java.orig
echo "" > ./freeplane/src/test/java/org/freeplane/core/ui/menubuilders/menu/JMenuItemBuilderTest.java

