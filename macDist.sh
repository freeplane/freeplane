gradle clean
gradle build
gradle dist
gradle macDist

echo "Copy to /Applications"
sudo cp -r BIN4mac_jre/Freeplane.app /Applications
echo "Signing:"
sudo codesign --force --deep --sign - /Applications/Freeplane.app
