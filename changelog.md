### Tesseract 1.0.39
- Improved performance when inserting into/extracting from tesseracts on large channels 
- Improved performance when querying capabilities for neighboring blocks
- Improved performance when adding/removing channels when there are a lot of online players
- Tesseract block entities are now cached rather than queried from the world
- Client will now only be sent channels that are public or created by its player 
- Reduced packet sizes for tesseract and channel data
- Added checks to prevent malicious actors from sending packets the wrong direction
- Added checks to prevent excessive memory allocation from malicious packets
- Fixed 2 buttons on the left of the tesseract interface not highlighting correctly when hovered

### Tesseract 1.0.38
- Improved memory allocation for tesseract handling
- Fixed crash when narrating lock button in channel creation screen

### Tesseract 1.0.37
- Added support for WTHIT
- Added Turkish translations (thanks to RuyaSavascisi!)

### Tesseract 1.0.36
- Fixed data for last placed tesseract sometimes getting overwritten when placing a new tesseract after loading a save
- Added Simplified Chinese translations (thanks to YCJ-GuLi!)
- Updated Russian translations (thanks to Vladislove99!)

### Tesseract 1.0.35a
- Fixed tesseract info not getting synced to the client properly

### Tesseract 1.0.35
- Initial release of Tesseract for Minecraft 1.20.5 & 1.20.6
