# "Build" steps:

- Edit icon (artboard named 'freeplane') and splash screen in Sketch (*Freeplane Artwork.sketch*)
- Export icon and/or splash screen from Sketch, overwriting Freeplane_splash.png and freeplane (.svg, @4x.png)
  into `outputs` folder — i.e. drag and drop from 'Freeplane' dartboard to ~/d2/dev/Freeplane/artwork/outputs

- Launch Asset Catalog Creator Pro
- Set output to *macOS App Icon*
- Set output directory in Asset Catalog Creator Pro to ~/d2/dev/Freeplane/artwork
  - The text "/Users/stuart/d2/dev/Freeplane/artwork/Assets.xcassets" should be displayed
    in the "Select your project folder, an existing catalog or directory" panel 
- Drag and drop ~/d2/dev/Freeplane/artwork/outputs/freeplane@8x.png onto centre panel
  of Asset Catalog Creator Pro.
- Click on Update Asset Catalog
