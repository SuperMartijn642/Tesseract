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

### Tesseract 1.0.36a
- Fixed packet error when channels names are one character more than should be allowed

### Tesseract 1.0.36
- Initial release of Tesseract for Minecraft 1.21.4
