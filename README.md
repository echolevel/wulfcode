Wulfcode
========

## A Midi live-coding environment for performance or composition

What is Wulfcode for? Ostensibly live-coding, but it’s dramatically simplified compared to the SuperCollider and Csound-based environments used by the live-coding community proper. It’s an object-oriented, text-based MIDI sequencer with its own syntax, and a repertoire of commands and structures that allow interesting looping motifs and polyrythmic phrases to be easily generated and manipulated on the fly. 

Using Wulfcode involves creating a Note Object - a string of semitone-increment note pitches from 0-f (in hex - the letters a-f correspond to 10-16) - with a given speed multiplier and transposition command, then running that in sync with an incoming MIDI clock. This usually comes from a DAW in ‘play’ mode, which is also accepting incoming MIDI from Wulfcode so it can play those notes from its own softsynths or sampler devices. Creating a Note Object also involves specifying a channel, so if I have e.g. an Ableton Live set with a synth or a sampler on channels 1 to 10, I can target a Note Object at channel 3 by typing and evaluating the following:

	mynoteobject c3 {0.23-7c-} 12 0

‘mynoteobject’ is a unique name for the Note Object and is used to refer to this NO in future. Anything can be used, as long as it doesn’t contain special characters (alphanumeric only) and doesn’t clash with one of the other Wulfcode commands (which can be displayed in the message window by typing and evaluating ‘help’). ‘c3’ targets channel 3, the dot in the note string causes a noteoff (the previous note is suddenly killed) while the hyphen causes a rest or sustained note - the previous note is allowed to ring out. ‘12’ is the speed multiplier relative to the incoming BPM; as a reference, 12 gives 8 quavers to a 4/4 bar at 128bpm, while 6 gives 16 semiquavers (much faster) and 24 gives 4 crotchets (slower) in the same. Hopefully that makes sense. This means that Wulfcode, while being as tied to regular beats as most electronic music tools, is largely time-signature agnostic and can run Note Objects with different speeds, counter-rhythms or triplet timings simultaneously.

Evaluating: this is how a command is run. After typing a command on a single, blank line, select all of the text on that line (using Ctrl/Cmd + Shift + arrowkeys) and then press Ctrl+1 or Cmd+1 to send the command to the interpreter. The command will remain in place, and you can keep selecting it and reevaluating it - perhaps after tweaking it a little - or you can copy and paste it to a textfile for later use. You can leave commands (or messages/reminders to yourself) lying around wherever you like in the text entry box; no harm is caused as long as they’re not on a line that contains a command you want to run. You can run multiple commands on a line if you separate them with a semicolon and a trailing space, e.g.:

    hoof mynoteobject; unmute mynoteobject

This is good for setting up a Note Object but immediately muting it, then tweaking it further before unmuting it back into the mix. You can also run multiple longer commands simultaneously by selecting multiple lines and evaluating, e.g.:

    noter c0 {0237a3------} 12 0
    noted c0 {------ac75a7} 12 -12

if selected and evaluated will run a seamless 12-note sequence on the same MIDI channel from two Note Objects.

### To Do:

* Lerping of CC values over time - needs to be optional, but it might be cool to have smooth modulations on slow sequences. Meanwhile, the low-res steppiness afforded by the a-f hex range is nice enough.
* Bug testing - there might still be some opportunities for stuck/hung MIDI notes, and various other command-parsing bear traps.
* Ensure Windows/Linux compatibility

### To Not Do:
* Shuffle/swing timing: it's possible already, but involves a bit of lateral thinking.
* Chaining sequences - nope. This is not meant to be a hands-off performance tool, or a full-blown sequencer. The most hands-off it gets is using limited repeats on a CC Object. Again, though, a lot of tricks can be learnt for arranging long and complex phrases with greater pitch intervals than 0-f hex. Experiment!

### May Never Be Done:
* MMC/transport commands in OS X - OS X's Java MIDI implementation is horribly, horribly mangled and simply doesn't handle system-level messages from the MIDI spec. It'll probably be easy to do start/stop/BPM controls from Windows/Linux versions of Wulfcode (when I'm able to test on those platforms), but it currently seems impossible on OS X (despite the various community-created libraries that claim to be able to fix these issues).

### Available commands:

NOTE OBJECT:
    e.g. noter c0 {7.53a30077232325} 6 0

* Object name, user definable
* Midi Channel
* Note values, 0-f (hex 0-15)
* Speed multiplier - lower is faster, higher is slower
* Transposition factor - in semitones, can be positive or negative
* ‘.’ in note braces signifies a noteoff, while ‘-’ signifies a rest.
* You can change the channel and reevaluate a Note Object command string, but beware of stuck notes on the previous channel. Usually, doing an mtoggle or a mute on the Note Object *before* changing its channel will allow old notes to end (unless they have infinite release time…) The recreated Note Object on the new channel will default to being unmuted.

CHORD OBJECT:
    e.g. chord g {02357e}

* Must be named alphabetically from ‘g’ to ‘z’
* simply creates a chord ‘note’ which can be used in a Note Object

VELOCITY OBJECT:
    e.g. velo noter {aba777ba}

* acts on an existing Note Object and applies sequence of velocities, mapped from hex 0-f to 0-127
will loop indefinitely; can be used to create interesting polyrhythms if shorter/longer than target Note Object.

CC OBJECT:
    e.g. cc panner c0 10 {020410ab0c32} 12 0

* acts on a target channel, sending a sequence of control values to a defined CC number(0-f hex mapped to 0-127)
loops indefinitely by default, or if loop count is set to 0, otherwise will stop after the desired number of repeats
* should fix problems with DAWs’ knob pickup modes by sending min and max values before sequence values.

HOOF:
    e.g. hoof noter

* randomises note order of existing Note or CC Object
* Be aware that there might be some bugs (e.g. values on the display not updating) if this is used with objects that are semicolon-separated on the same line. Note and CC Objects are best run on their own lines if Hoof is to be used, though they can be evaluated simultaneously from a multi-line text selection.

PAUSE/UNPAUSE:
    e.g. pause noter

* Pauses or unpauses the MIDI output of a Note Object

SEMICOLON SEPARATOR:
    e.g. pause noter; unpause snare

* Allows multiple commands to be entered on the same line and evaluated simultaneously
* Great for chaining up short utility commands and toggles
* Less good for ‘hoofable’ commands (creating or updating Note and CC Objects) - ideally, these should be run on their own lines, although they can still be triggered simultaneously from a multi-line text selection.

MULTIPLY/DIVIDE NOTE OBJECT RATE
    e.g. /2 noter; *4 snares

* Multiplies or divides an extant Note Object’s speed multiplier by the given factor

NUDGE
    e.g. << drumr;   >> noter

* Nudge the current Note Object or CC Object’s position back or forward by one (relative to the notes in its {} array.

KEY:
    e.g. key -24; key 24

* Global transpose, though beware - there’s no protection against some objects’ notes going out of MIDI note range. These notes will just be locked to 0 or 127, and repeated out-of-range transpositions will eventually squish all notes to 0 or 127. Could be fun!

KILL:
    e.g. kill noter; kill chordy

* Kill removes a Note or CC Object completely, and should disappear it gracefully without leaving stuck MIDI notes, but if your input window still contains the command originally used to instantiate the object, you can easily revive it. 

OPACITY:
    e.g. opacity 80

* Sets the opacity of Wulfcode’s two UI windows (default is 80)

MIDIDEVICE:
* Use with the argument ‘list’ to list available hardware and software MIDI devices on your system
Run the command again with input and output device numbers (from the list) as the arguments, e.g.
'mididevice 2 3'
to set input [2] as the input device, and output [2] as the output device.
Input will usually need to correspond to the device on which your DAW is outputting its MIDI clock signal. Output is the device which your DAW has set as an input, through which it’ll get all the notedata to route to its instruments. If you’re unsure, a little trial and error goes a long way.

UNDO/REDO:

* Ctrl+z (Cmd+z on Mac) is undo
* Shift+Ctrl+z (Shift+Cmd+z on Mac) is redo

CONFIG FILE:
* Wulfcode looks in your home directory (~/yourname on Mac or Linux, presumably something else on Windows - I forget) for ‘wulfcode_config.txt’, which will be bundled with the app/source and should be copied manually to your homedir.
In it, you can define:
MIDI input and output device numbers 
the initial window opacity
window orientation (side by side or vertically stacked) and order (input window then output, or vice versa)
starting x/y screen positions for Wulfcode
Width/height values, background and text colours and text sizes for each window.
