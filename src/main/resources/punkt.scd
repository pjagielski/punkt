(
var synthsPath = PathName.new(thisProcess.nowExecutingPath).pathOnly +/+ "punkt-synths.scd";
s.reboot { // server options are only updated on reboot
	// configure the sound server: here you could add hardware specific options
	// see http://doc.sccode.org/Classes/ServerOptions.html
	s.options.numBuffers = 1024 * 256; // increase this if you need to load more samples
	s.options.memSize = 8192 * 32; // increase this if you get "alloc failed" messages
	s.options.numWireBufs = 64; // increase this if you get "exceeded number of interconnect buffers" messages
	s.options.maxNodes = 1024 * 32; // increase this if you are getting drop outs and the message "too many nodes"
	s.options.numOutputBusChannels = 2; // set this to your hardware output channel size, if necessary
	s.options.numInputBusChannels = 2; // set this to your hardware output channel size, if necessary
	s.options.maxLogins = 8;
	s.latency = 0.2;
	// boot the server
	s.waitForBoot {
		synthsPath.load();
		s.sync;
    }
})

(
s.latency = 0;
x = { Out.ar(0, SoundIn.ar([0, 1])) }.play;
x.free;
)

MIDIClient.init;

(
MIDIClient.init;
m = MIDIOut(1);
m.connect(1);
m.latency = 0;

OSCdef(\midi_player, {
	arg msg, time, addr, port;
	var note, vel, sus, channel, nudge;

	// msg.postln;

	channel = msg[1];
	note    = msg[2];
	vel     = msg[3];
	sus     = msg[4];
	nudge   = 0.0;

	SystemClock.schedAbs(time + nudge, {m.noteOn(channel, note, vel)});
	SystemClock.schedAbs(time + nudge + sus, {m.noteOff(channel, note, vel)});

	// "reception time: %\nscheduling time: %\ndelta: %\n\n".postf(Main.elapsedTime, time, time - Main.elapsedTime)

}, '/midi/note');
)

m.noteOn(16, 40, 60);
m.noteOff(16, 40, 60);


(
Ndef(\mix, {
	var level = 3.5;
	var distortion = 0.3;

	var dirt = InBus.ar(0, 2);
	var mix = FreeVerb.ar(dirt, 0.2, 0.3);
	mix = LeakDC.ar(mix);
	//LocalOut.ar(mix);
	mix = Compander.ar(mix, mix, 0.5, slopeBelow:1, slopeAbove:0.5, clampTime:0.01, relaxTime:0.01);
	mix = (mix * distortion).tanh * (level / distortion.max(1));
	Out.ar(0, mix);
}).play;
)

(
Ndef(\master, {
	var level = 0.5;
	var distortion = 0.3;
	var reverbFeedback = 0.3;
	var mix = InBus.ar(0, 2);
/*	var mix = all.sum { |x|
		var d = { 0.01.rand } ! x.size;
		DelayN.ar(x, d, d);
	};*/
	var loop = LocalIn.ar(2);
	2.do { loop = AllpassL.ar(loop, 0.15, { ExpRand(0.03, 0.05) } ! 2, 3) };
	mix = loop * reverbFeedback + mix;
	mix = LeakDC.ar(mix);
	LocalOut.ar(mix);
	mix = Compander.ar(mix, mix, 0.5, slopeBelow:1, slopeAbove:0.5, clampTime:0.01, relaxTime:0.01);
	mix = (mix * distortion).tanh * (level / distortion.max(1));
	Out.ar(0, mix);
}).play;
)
