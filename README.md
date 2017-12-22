# NeonPulse
A Retro-Futuristic Battle Arena

## Running on Processing

Open NeonPulse.pde in processing.
In the Preferences window (File > Preferences) set the sketchbook location to the sketchbook directory included in the source. This directory contains all the necessary plugins, as well as the Neon Pulse game packaged as a library.

This library is then called from the sketch.

This packaging method, although originally a hack, has since shown itself to be an
interesting way to expose parts of the game as an engine and open it to 
modification by others.
 
I hoped to include this functionality in the alpha, but I haven't had enough time to
work on the extraneous parts of the program.

## How to build:

Download and extract a zip, or clone the repository with `git clone https://github.com/raulgrell/NeonPulse.git`

Open the project, NeonPulse.iml in IntelliJ IDEA.

Add the processing libraries, including native libraries and the net library.
These will be in the processing installation directory.

Add the plugin libraries for sound, postfx, and gamecontrols plus.
These will be in the sketchbook directory.

Add the NeonPulse class as a module, and run.

