(
SynthDef(\plaits,
	{|bus, amp=1, freq = 220, dur=1, pan=0, harm=0.5, timbre=0.5, morph=0.5, engine=7, atk=0.2, sus=0.3, mul=1.0|
	var env = EnvGen.ar(Env.linen(atk, 1-atk-sus, sus, 1, -3), timeScale:dur, doneAction:0);
	var sound = MiPlaits.ar(pitch: freq.cpsmidi, engine: engine, harm: harm, timbre: timbre, morph: morph, mul: mul, trigger: 1, lpg_colour: 0.1);
	ReplaceOut.ar(bus, Pan2.ar(sound[0] * amp, pan, env));
}).add;

SynthDef.new(\globalReverb, {
	|bus, drywet = 0.000001, damp = 0.000001|
	var sound = In.ar(bus, 2);
	var verb = MiVerb.ar(sound, drywet: drywet, damp: damp);
	ReplaceOut.ar(bus, verb);
}).add;

/*
SynthDef.new(\lpf, {
	|bus, cutoff=0.5, res=0.5, drive=0.3|
	var osc;
	osc = In.ar(bus, 2);
	osc = MiRipples.ar(osc, cutoff, res, drive);
	ReplaceOut.ar(bus, osc);
}).add;

SynthDef.new(\globalChorus, {
	|bus, bright=0.0, pos=0.0|
	var sound = In.ar(bus, 2);
	var chorus = MiRings.ar(in: sound, bright: bright, pos: pos, damp: 0.1, poly: 1, model: 1, easteregg: 1);
	ReplaceOut.ar(bus, chorus);
}).add;
*/

)