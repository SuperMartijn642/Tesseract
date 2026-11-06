### Tesseract 1.0.40
- Operators can now remove channels of other players by holding shift
- Improved loading of channel creator names and heads in the tesseract screen
- Fixed modified clients being able to connect tesseracts to other players' private channels
- Fixed modified clients being able to change tesseracts from anywhere
- Fixed players getting disconnected when joining a server with more than 500 channels
- Fixed some mods ignoring the send and receive settings of tesseracts when extracting from them
- Fixed tesseracts placed next to each other sometimes transferring resources between their channels
- Fixed crash when another mod accesses a tesseract's storage client-side
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
- Fixed tesseracts inserting into the wrong side of neighboring blocks
- Fixed 2 buttons on the left of the tesseract interface not highlighting correctly when hovered

## Update to Minecraft 26.3

### Tesseract 1.0.38a
- Fixed crash on dedicated server

## Update to Minecraft 26.2

## Update to Minecraft 26.1

### Tesseract 1.0.38
- Initial release of Tesseract for Minecraft 1.21.11
