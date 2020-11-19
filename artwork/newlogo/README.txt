# "Build" steps:

1. Edit icon and splash screen in Sketch (*Freeplane Artwork.sketch*)

2. Export icon and/or splash screen from Sketch, overwriting Freeplane_splash.png and freeplane (.svg, @4x.png) in `outputs`.

3. Use **Asset Catalog Creator Pro** 
  - Setting:        *macOS App Icon*
  - Project Folder: `artwork/Assets.xcassets`
  - Drag freeplane@4x.png into **Asset Catalog Creator Pro**
  - Click **Create Asset Catalog**.

4. The result is an updated `artwork/Assets.xcassets/AppIcon.appiconset`.

5. Now run `create_icns.sh` to generate `freemind.icns`

6. Finally, run `copyArtwork.sh` to copy new artwork into the source tree in the appropriate locations.

