### Tesseract 1.0.39
- Improved performance when inserting into/extracting from tesseracts on large channels 
- Improved performance when querying capabilities for neighboring blocks
- Improved performance when adding/removing channels when there are a lot of online players
- Tesseract block entities are now cached rather than queried from the world
- Client will now only be sent channels that are public or created by its player 
- Reduced packet sizes for tesseract and channel data
- Added checks to prevent malicious actors from sending packets the wrong direction
- Added checks to prevent excessive memory allocation from malicious packets
- Fixed tesseracts inserting into the wrong side of neighboring blocks
- Fixed 2 buttons on the left of the tesseract interface not highlighting correctly when hovered

### Tesseract 1.0.38
- Improved memory allocation for tesseract handling
- Fixed crash when narrating lock button in channel creation screen
- Fixed crash when a neighboring block proxies the tesseract's storage back to the tesseract

### Tesseract 1.0.37
- Added support for WTHIT
- Added Turkish translations (thanks to RuyaSavascisi!)

### Tesseract 1.0.36
- Fixed data for last placed tesseract sometimes getting overwritten when placing a new tesseract after loading a save
- Added Simplified Chinese translations (thanks to YCJ-GuLi!)
- Updated Russian translations (thanks to Vladislove99!)

### Tesseract 1.0.35a
- Fixed crash with Tom's Simple Storage

### Tesseract 1.0.35
- Fixed player skins inside the tesseract screen not rendering sometimes

### Tesseract 1.0.34
- Added Ukrainian translations (thanks to SKZGx!)
- Fixed tesseracts getting stuck not transferring
- Fixed error message when loading a world without previous tesseract data

### Tesseract 1.0.33
- Fixed rare crash when invalid tesseract references are removed

### Tesseract 1.0.32a
- Fixed missing atlas entries for the tesseract's textures

### Tesseract 1.0.32
- Fixed rare stack overflow error

### Tesseract 1.0.31
- Fixed tesseract save data loading

### Tesseract 1.0.30
- Initial release of Tesseract for Fabric
