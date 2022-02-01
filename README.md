## Create Aeronautics
This is an addon for the create mod that aims to extend the contraption system to include flying machines.
For example: airships, hot air balloons, airplanes, helicopters and more.

You start off by making a regular create contraption using superglue and chassis blocks and so on.
Then, you add envelopes, blaze burners, stirling engines/furnace engines, propellers, rudders and so on.
Finally, you can take to the skies in the fun and interesting craft you just built yourself.

### How it works behind the scenes
All the blocks that make up the contraption gets moved into a separate dimension.
In there they can sit and tick away on their own while the actual contraption entity flies around the world.
This arrangment allows for things like tile entities, redstone and even other contraptions to appear to work while on the moving airship, something that cannot be done in regular Create.
Some of these blocks like propeller bearings and blaze burners have custom behaviours that alter the movement of the airship in the regular world.
