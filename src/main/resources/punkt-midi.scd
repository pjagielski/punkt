(
	var port=1;

	MIDIClient.init;

	midiout = MIDIOut(port, MIDIClient.destinations[port].uid);

	OSCFunc(
		{
			arg msg, time, addr, port;
			var note, vel, sus, channel, nudge;

			// listen for specific MIDI trigger messages from FoxDot

			note    = msg[2];
			vel     = msg[3];
			sus     = msg[4];
			channel = msg[5];
			nudge   = msg[6];

			SystemClock.schedAbs(time + nudge, {midiout.noteOn(channel, note, vel)});
			SystemClock.schedAbs(time + nudge + sus, {midiout.noteOff(channel, note, vel)});

		},
		'foxdot_midi'

	);

	("Sending FoxDot MIDI messages to" + MIDIClient.destinations[port].name).postln;

)

MIDIClient.init;

m = MIDIOut(1, MIDIClient.destinations[1].uid)
m.noteOn(16, 60, 60);
m.noteOn(16, 61, 60);
m.noteOff(16, 61, 60);
m.allNotesOff(16);
m.latency = 0;

s.latency = 0;