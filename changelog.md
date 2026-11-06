### Tesseract 1.0.40
- Operators can now remove channels of other players by holding shift
- Improved loading of channel creator names and heads in the tesseract screen
- Improved performance when transferring fluids
- Fixed an error when unloading chunks with tesseracts, which made tesseracts keep transferring resources while unloaded
- Fixed crash when a tesseract has both a fluid and an energy channel
- Fixed modified clients being able to connect tesseracts to other players' private channels
- Fixed modified clients being able to change tesseracts from anywhere
- Fixed players getting disconnected when joining a server with more than 500 channels
- Fixed items being extracted from send only tesseracts rather than receive only tesseracts
- Fixed tesseracts checking the wrong slot for whether an item is allowed, sometimes causing crashes
- Fixed tesseracts checking the wrong tank for whether a fluid is allowed, sometimes causing crashes
- Fixed errors when draining exactly the amount of fluid that is requested from a tesseract
- Fixed broken tesseracts still transferring resources for pipes that were connected to them
- Fixed tesseracts losing their settings when placed right after a tesseract was removed at the same position
- Fixed tesseracts sometimes losing their channels when loading partially saved data
- Fixed worlds staying in memory after leaving them
- Fixed stored energy and energy capacity becoming negative with very large energy storages
- Fixed the channel list not scrolling with touchpads and smooth scrolling mice
- Fixed the hover highlight of channels in the tesseract screen only covering part of the channel

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
- Initial release of Tesseract for Forge 1.21.4
