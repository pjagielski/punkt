(
    var idx = 1;
    m = MIDIOut(0);
	m.connect(idx);
	m.latency = 0.0;
)


MIDIClient.init;
MIDIClient.destinations;
m = MIDIOut(1, MIDIClient.destinations[1].uid);


m.connect(0);
m.noteOn(0, 48, 127);
m.noteOff(0, 48, 127);
m.allNotesOff(0);

s.latency = 0.0;

(
  m = MIDIOut(0);
  m.latency = 0.0;

  o = OSCFunc(
	{
		arg msg, time, addr, recvPort;
		var note, vel, sus, channel, nudge;

		// msg.postln;

		channel = msg[1];
		note    = msg[2];
		vel     = msg[3];
		sus     = msg[4];
		nudge   = msg[5];

		SystemClock.schedAbs(time - 0.08, {m.noteOn(channel, note, vel)});
		SystemClock.schedAbs(time - 0.08 + sus, {m.noteOff(channel, note, vel)});

	},
	'/midi/note'
  );
)
o.free;

s.latency = 0.2;

n = NetAddr("127.0.0.1", NetAddr.langPort);
n.sendMsg("/midi/note", 16, 60, 60, 0.5, -0.15);
