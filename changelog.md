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
- Fixed The One Probe showing all channels as inactive on dedicated servers
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
- Added Turkish translations (thanks to RuyaSavascisi!)

### Tesseract 1.0.36
- Fixed data for last placed tesseract sometimes getting overwritten when placing a new tesseract after loading a save
- Added Simplified Chinese translations (thanks to YCJ-GuLi!)
- Updated Russian translations (thanks to Vladislove99!)

### Tesseract 1.0.35a
- Fixed tesseract resource transferring with neighboring blocks sometimes breaking

### Tesseract 1.0.35
- Fixed player skins inside the tesseract screen not rendering sometimes

### Tesseract 1.0.34
- Added Ukrainian translations (thanks to SKZGx!)
- Fixed tesseracts getting stuck not transferring
- Fixed error message when loading a world without previous tesseract data

### Tesseract 1.0.33
- Fixed rare crash when invalid tesseract references are removed

### Tesseract 1.0.32
- Fixed rare stack overflow error

### Tesseract 1.0.31
- Fixed tesseract save data loading

### Tesseract 1.0.30a
- Fixed crash on startup

### Tesseract 1.0.30
- Significant performance improvements
- Added confirmation screen when removing channels
- Tesseract data will now be saved more frequently
- Fixed player heads not rendering correct skins
- Fixed being able to delete other players' channels

### Tesseract 1.0.29
- Added integration with Jade and The One Probe
- Updated Russian translations (thanks to Big Energy!)

### Tesseract 1.0.28a
- Fix rendering offsets inside the tesseract's gui

### Tesseract 1.0.28
- Updated to core library 1.1
- Removed the WIP info button in the tesseract menu
- Added the tesseract to the creative menu decorations tab
- Added text coloring for transfer and redstone settings
