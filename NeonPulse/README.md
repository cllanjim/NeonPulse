# NeonPulse
A Retro-Futuristic Battle Arena

## Running on Processing

In the Preferences window (File > Preferences) set the sketchbook location to the 
sketchbook directory included in the source. This directory contains all 
the necessary plugins, as well as the Neon Pulse game packaged as a library.

The game is fully contained in `core/NP.jar` file. This file is then called from the sketch.

## How to build:

Download and extract a zip, or clone the repository 
with `git clone https://github.com/raulgrell/NeonPulse.git`

Open the project, NeonPulse.iml in IntelliJ IDEA.

## Updating the game library

In order to package all the class files in `out/production/NeonPulse` into
the game library, run `build.bat`

### Configuring IntelliJ

Under File > Projects Settings > Libraries, add the `core` 
and `net` directories to your project.
These are in the `processing/` directory.

Add the plugin libraries for sound, postfx, and gamecontrols plus.
These are in the `sketchbook/` directory.

Add the NeonPulse class as a module, and run.

