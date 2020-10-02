# "Build" steps:

- Edit icon and splash screen in Sketch (*Freeplane Artwork.sketch*)
- Export icon and/or splash screen from Sketch, overwriting Freeplane_splash.png and freeplane (.svg, @4x.png) in `outputs`.
- Use **Asset Catalog Creator Pro** (setting: *macOS App Icon*, with project folder: `artwork/Assets.xcassets`), then drag freeplane@4x.png into **Asset Catalog Creator Pro** and click **Create Asset Catalog**.
- The result is an updated `artwork/Assets.xcassets/AppIcon.appiconset`.
- Now run `create_icns.sh` to generate `freemind.icns`
- Finally, run `copyArtwork.sh` to copy new artwork into the source tree in the appropriate locations.

