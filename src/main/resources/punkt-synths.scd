(
SynthDef.new(\play1,
    {|amp=1, sus=1, blur=1, pan=0, rate=1.0, bus=0, buf=0|
        var osc, env;
        sus = sus * blur;
        osc = PlayBuf.ar(1, buf, (BufRateScale.ir(buf) * rate));
        osc = (osc * amp);
        osc = Mix(osc) * 0.5;
        osc = Pan2.ar(osc, pan);
        ReplaceOut.ar(bus, osc)
}).add;

SynthDef.new(\play2,
    {|amp=1, sus=1, pan=0, rate=1.0, bus=0, blur=1, buf=0|
        var osc, env;
        sus = sus * blur;
        osc = PlayBuf.ar(2, buf, (BufRateScale.ir(buf) * rate));
        osc = (osc * amp);
        osc = Mix(osc) * 0.5;
        osc = Pan2.ar(osc, pan);
        ReplaceOut.ar(bus, osc)
}).add;

SynthDef.new(\shape,
    {|bus=0,freq=440, amp=0.5, dur=0.2, cutoff=1200, mix=0.2, release=0.2|
        var osc1, osc2, sine, adsr, snd, env, level;
        osc1 = LFTri.ar(freq);
        osc2 = LFSaw.ar(freq, 0.88);
        sine = SinOsc.ar(freq / 2);
        adsr = Env.adsr(0.01, 0.6, 0.1, release);
        snd = (osc2 * mix) + ((1 - mix) * osc1);
        env = EnvGen.kr(adsr, Line.kr(1.0, 0.0, dur/2), doneAction:0);
        // level = EnvGen.kr(adsr, Line.kr(1.0, 0.0, dur), levelScale: cutoff);
        snd = LPF.ar(snd, cutoff);
        snd = snd + (0.2 * sine);
        ReplaceOut.ar(bus, Pan2.ar(FreeVerb.ar(amp * env * snd, room: 0.2)));
}).add;

SynthDef.new(\sampler,
    {|bus=0, bpm=120, total=4, beats=4, start=0, amp=1, cutoff=10000, buf=0|
        var beatLen, dur, env, rate, frames, pos, snd;
        beatLen = 60 / bpm;
        dur = beats * beatLen;
        env = EnvGen.ar(Env([1,1,0], [dur-0.2, 0.2], 'welch'));
        rate = BufDur.kr(buf) / (total * beatLen);
        frames = BufFrames.kr(buf);
        pos = (start / total) * frames;
        snd = PlayBuf.ar(2, buf, (rate * BufRateScale.ir(buf)), startPos: pos);
        snd = LPF.ar(snd, cutoff);
        ReplaceOut.ar(bus, Pan2.ar(snd * amp, 0, env));
}).add;

SynthDef(\plucklead, {
    |bus=0, dur=1, amp=1, freq=440, cutoff=5000, start=1000, sus=0.4, rel=0.8|
    var sound;
    var voices = 13;
    var env = Env.adsr(0.1, 0.7, sus, rel);
    var envGen = EnvGen.kr(env, Line.kr(1.0, 0.0, dur), doneAction:0);
    var freqs = {Rand(-1,1)*2**(freq/2200)}!voices;
    var phases = Array.fill(voices, { 1.0.rand2 });
	var filEnv = Env.adsr(0.1, 1, 0.5, 0.5);
	var filGen = EnvGen.kr(filEnv, Line.kr(1.0, 0.0, dur), levelScale: cutoff);

    sound = LFSaw.ar(freq + freqs, phases * filGen);
    sound = LPF.ar(sound, start + cutoff);
    ReplaceOut.ar(bus, sound * amp * envGen);
}).add;

SynthDef(\lead, {
    |bus=0, dur=1, amp=1, freq=440, cutoff=1000, start=2000, sus=0.5, rel=0.5|
    var sound;
    var voices = 13;
    var env = Env.adsr(0.1, 0.7, sus, rel);
    var envGen = EnvGen.ar(env, Line.kr(1.0, 0.0, dur), doneAction:0);
    var freqs = {Rand(-1,1)*2**(freq/2200)}.dup(voices);
    var phases = Array.fill(voices, { 1.0.rand2 });
    sound =  LFSaw.ar(freq + freqs, phases);
    sound = LPF.ar(sound, start + cutoff);
	ReplaceOut.ar(bus, Splay.ar(sound * amp, 0.5) * envGen);
}).add;

SynthDef(\tb303, {
    |bus=0, freq=440, wave=1, cutoff=400, res=0.1, start=200, sus=0.75, dec=0.5, dur=1, amp=1|
    var sound;
    var line = Line.ar(1, 0, dur*2, doneAction:0);
    var volEnv = EnvGen.ar(Env.perc(10e-10, dur), doneAction: 0);
    var filEnv = EnvGen.ar(Env.adsr(10e-10, dec, sus, 0.2), line);
    var waves = [Saw.ar(freq, volEnv), Pulse.ar(freq, 0.5, volEnv)];
	sound = RLPF.ar(Select.ar(wave, waves), start + (filEnv * cutoff), res);
	//sound = (sound.clip2(0.1) * 12).distort();
	ReplaceOut.ar(bus, Pan2.ar(sound) * amp);
}).add;

SynthDef(\tr808, {
    |bus=0, dur=1, amp=1, freq=440, cutoff=1200, start=1000, rel=0.3|
    var sound, filEnv, filGen;
    var ampEnv = Env.adsr(0.01, 0.5, 0.2, rel);
    var ampGen = EnvGen.kr(ampEnv, Line.kr(1.0, 0.0, dur/2), doneAction: 0);
    var osc1 = SinOsc.ar(freq);
	var osc2 = Pulse.ar(freq*2);
    sound =  Mix.ar(osc1, 0.8 * osc2);
    sound =  Splay.ar(sound);
    filEnv = Env.adsr(0.2, 0.4, 0.1, 0.5);
    filGen = EnvGen.kr(filEnv, Line.kr(1.0, 0.0, dur), levelScale: cutoff);
    sound = LPF.ar(sound, start + filGen);
	// sound = BPeakEQ.ar(sound, 500, 1, 1);
    ReplaceOut.ar(bus, Pan2.ar(sound * amp * ampGen));
}).add;

SynthDef(\dafunk, {
    |bus=0, dur=1, amp=1, freq=440, cutoff=1200, start=250, sus=0.5, rel=0.2|
    var osc = Mix.ar([Saw.ar(freq), Saw.ar(freq * 0.7491535384383409)]);
    var env = Env.adsr(0.3, 0.2, sus, rel);
    var envGen = EnvGen.ar(env, Line.kr(1.0, 0.0, dur/2), doneAction:0);
    var filEnv = Env.adsr(0.4, 0.5, 0.2, 0.5);
    var filGen = EnvGen.kr(filEnv, Line.kr(1.0, 0.0, dur/2), levelScale: cutoff);
    var sound = BPF.ar(osc, start + filGen, 0.5);
    sound = Pan2.ar(sound * amp, 0, envGen);
	sound = (sound.clip2(0.007) * 24).distort();
	// sound = FreeVerb.ar(sound, mix: 0.3, room: 0.5);
    ReplaceOut.ar(bus, sound);
}).add;

SynthDef(\bass8, {
	|bus=0, dur=1, freq=200, sus=1, rel=0.01, amp=0.1, pan=0|
	var sig, sound, aenv, aenvGen, line;
	var osc = Array.newClear(6);
	var env = Array.newClear(6);

	line = Line.kr(1.0, 0.0, dur);
	env[0] = EnvGen.kr(Env([0,1,0.051,0],[0.001,0.01,0.8], [4,-8]), line);
	env[1] = EnvGen.kr(Env([0,1,0.051,0],[0.005,0.5,1.5], [0,-8], releaseNode:2), line);
	env[2] = EnvGen.kr(Env([0,1,1,0],[0.01,0.01,0.2], [0,0,-4], releaseNode:2), line);
	env[3] = EnvGen.kr(Env([0,1,0],[0.002,2.8], [0,-4]), line);
	env[4] = EnvGen.kr(Env([0,1,1,0],[0.001,0.1,0.8], [4,0,-4], releaseNode:2), line);
	env[5] = EnvGen.kr(Env([0,1,0],[0.001,3.0], [0,-4]), line);

	freq = (freq / 4) * ((0..1)/1 - 0.5 * 0.0007 + 1);

	osc[0] = SinOsc.ar(freq * 11 + 0) * env[0];
	osc[1] = SinOsc.ar(freq * 6 * ( osc[0] * 2.5 + 1 )) * env[1];
	osc[2] = SinOsc.ar(freq * 2 * 1 + 0) * env[2];
	osc[3] = SinOsc.ar(freq * 1 * ( osc[2] * 2.5 + 1 ) + 0) * env[3];
	osc[4] = SinOsc.ar(freq * 1 * ( osc[1] * 2.5 + 1 ) * (osc[3] * 2.5 + 1)) * env[4];
	osc[5] = SinOsc.ar(freq * 2) * env[5];

	aenv = Env.adsr(0.001, 0.02, sus, rel, 1, -1);
	aenvGen = EnvGen.ar(aenv, line, doneAction:0);

	sig = osc * DC.ar([0.0, 0.0, 0.0,  0.0, 0.5, 0.5]);
	sig = sig * 0.5;
	sig = sig.flop.sum;
	sig = sig * aenvGen;
	// sig = sig * AmpComp.kr(freq);
	sig = sig.sum;

	sound = Pan2.ar(sig, pan, amp);
	ReplaceOut.ar(bus, sound);
}).add;

SynthDef(\piano,{
	|bus, dur=1, pan, amp=1, detune=0.1, muffle=1, stereo=0.2, freq=440 |
	var env = EnvGen.ar(Env.linen(0.002, 0.996, 0.002, 1,-3), timeScale:dur, doneAction:0);
	// the +0.01 to freq is because of edge case rounding internal to the MdaPiano synth
	var sound = MdaPiano.ar(freq+0.01, vel:amp*100, hard:0.8*amp, decay:0.1*dur,
		tune:0.5, random:0.05, stretch:detune, muffle:0.8*muffle, stereo:stereo);
	ReplaceOut.ar(bus, Pan2.ar(sound, pan, env))
}).add;


// stolen from stealthissound.scd

SynthDef(\rhodes,{
	|bus, dur=1, freq=440, amp=0.1, lforate=4.85, lfowidth=0.5, cutoff=2000, res=0.5, pan=0.0|

	var pulse, filter, volEnv, filEnv, filGen;

	pulse = Pulse.ar(freq*[1,33.5.midiratio],[0.2,0.1],[0.7,0.3]);

    volEnv = EnvGen.ar(Env.perc(10e-10, dur), doneAction: 0);

	// filEnv = Env.adsr(0.0,1.0,0.8,3.0);
	// filGen = EnvGen.kr(filEnv, Line.kr(1.0, 0.0, dur), levelScale: cutoff);
	filEnv = EnvGen.ar(Env.adsr(0.0,1.0,0.8,3.0),doneAction:2);

	//keyboard tracking filter cutoff
    filter = BLowPass4.ar(pulse,(cutoff*(filEnv.squared))+200+freq,res);

	// filter = BLowPass4.ar(pulse, filGen + 200 + freq, res);

	ReplaceOut.ar(bus, Pan2.ar(Mix(filter) * amp, pan, volEnv));
}).add;

SynthDef.new(\organs, {
	|bus, freq=440, dur=1, amp=0.1, lforate=10, lfowidth=0.0, cutoff=100, res=0.5, pan=0.0|

	var vibrato, pulse, filter, env;

	vibrato = SinOsc.ar(lforate,Rand(0,2.0));

	//up octave, detune by 4 cents
	//11.96.midiratio = 1.9953843530485
	//up octave and a half, detune up by 10 cents
	//19.10.midiratio = 3.0139733629359

	//Pulse version
	//pulse = Mix(Pulse.ar(([1,1.9953843530485,3.0139733629359]*freq)*(1.0+(lfowidth*vibrato)),Rand(0.4,0.6)!3,[1.0,0.7,0.3]))*0.5;

	//better alternative
	pulse = Mix(VarSaw.ar(([1,1.9953843530485,3.0139733629359]*freq)*(1.0+(lfowidth*vibrato)),Rand(0.0,1.0)!3,Rand(0.3,0.5)!3,[1.0,0.7,0.3]))*0.5;

	filter = RLPF.ar(pulse,cutoff,res);

	env = EnvGen.ar(Env.linen(0.002, 0.996, 0.002, 1,-3), timeScale:dur, doneAction:0);

	ReplaceOut.ar(bus, Pan2.ar(filter * amp, pan, env));

}).add;

SynthDef.new(\lpf, {
	|bus, cutoff=1000, res=0.9|
	var osc;
	osc = In.ar(bus, 2);
	osc = RLPF.ar(osc, cutoff, res);
	ReplaceOut.ar(bus, osc);
}).add;

SynthDef.new(\hpf, {
	|bus, cutoff=2000, res=0.9|
	var osc;
	osc = In.ar(bus, 2);
	osc = RHPF.ar(osc, cutoff, res);
	ReplaceOut.ar(bus, osc);
}).add;

SynthDef.new(\bpf, {
	|bus, cutoff=2000, res=0.9|
	var osc;
	osc = In.ar(bus, 2);
	osc = BPF.ar(osc, cutoff, res);
	ReplaceOut.ar(bus, osc);
}).add;

SynthDef(\djf, {
	|bus, cutoff=0.5, res=0.9|
	var osc;
	var lpfCutoffFreq = cutoff.linexp(0, 0.5, 20, 10000);
	var hpfCutoffFreq = cutoff.linexp(0.5, 1, 20, 10000);
	osc = In.ar(bus, 2);
	osc = RHPF.ar(RLPF.ar(osc,lpfCutoffFreq), hpfCutoffFreq);
	ReplaceOut.ar(bus, osc);
}).add;

SynthDef.new(\delay, {
	|bus, echo=0.4, bpm=95, echotime=2|
	var osc;
	var beat_dur = 60 / bpm;
	osc = In.ar(bus, 2);
	osc = osc + CombL.ar(osc, delaytime: echo * beat_dur, maxdelaytime: 2 * beat_dur, decaytime: echotime * beat_dur);
	ReplaceOut.ar(bus, osc);
}).add;

SynthDef.new(\dist,	{
	|bus, drive=0.1|
	var osc;
	osc = In.ar(bus, 2);
	osc = (osc * (drive * 50)).clip(0,0.2).fold2(2);
	ReplaceOut.ar(bus, osc);
}).add;

SynthDef.new(\squiz, {
    |bus, ratio = 1|
    var sig = In.ar(bus, 2);
    sig = Squiz.ar(sig, ratio);
    ReplaceOut.ar(bus, sig)
}).add;

SynthDef.new(\krush, {
    |bus, bits = 8, sus = 1.0, amp = 1.0, crush = 1.0|
    var osc;
    osc = In.ar(bus, 2);
    osc = Decimator.ar(osc, rate: 44100/crush, bits: bits);
    osc = osc * Line.ar(amp * 0.85, 0.0001, sus * 2);
    ReplaceOut.ar(bus, osc)
}).add;

SynthDef.new(\waveDist, {
    |bus, shape = 0.2|
    var osc;
    osc = In.ar(bus, 2);
    osc = (osc * (shape * 50)).fold2(1).distort / 5;
    ReplaceOut.ar(bus, osc)
}).add;

SynthDef.new(\chop,{
	|bus, chop=2, sus=1.0|
	var osc;
	osc = In.ar(bus, 2);
	osc = osc * LFPulse.kr(chop / sus, add: 0.01);
	ReplaceOut.ar(bus, osc);
}).add;

SynthDef.new(\reverb, {
	|bus, room, mix|
	var osc = In.ar(bus, 2);
	osc = FreeVerb.ar(osc, mix, room);
	ReplaceOut.ar(bus, osc);
}).add;

SynthDef.new(\freeGroup, {
	|bus, sus, outBus=10|
	var osc;
	osc = In.ar(bus, 2);
	osc = EnvGen.ar(Env([1,1,0],[sus * 8, 0.1]), doneAction: 14) * osc;
	DetectSilence.ar(osc, amp:0.000000001, time: 0.5, doneAction: 14);
	Out.ar(outBus, osc[0]);
	Out.ar(outBus+1, osc[1]);
}).add;

SynthDef.new(\fxTransfer, {
	|bus, amp=0.75|
	Out.ar(0, amp * In.ar(bus, 2));
}).add;

SynthDef.new(\globalDelay, {
	|bus, level=0.0, echo=0.75, bpm=100, echotime=2|
	var osc;
	var beat_dur = 60 / bpm;
	osc = In.ar(bus, 2);
	osc = osc + (level * CombL.ar(osc, delaytime: echo * beat_dur, maxdelaytime: 2 * beat_dur, decaytime: echotime * beat_dur));
	ReplaceOut.ar(bus, osc);
}).add;

SynthDef.new(\globalReverb, {
	|bus, level=0.0, room = 2, mix = 0.5|
	var osc = In.ar(bus, 2);
	osc = osc + (level * FreeVerb.ar(osc, mix, room));
	ReplaceOut.ar(bus, osc);
}).add;

SynthDef.new(\globalDjf, {
	|bus, cutoff=0.5, res=0.9|
	var osc;
	var lpfCutoffFreq = cutoff.linexp(0, 0.5, 20, 10000);
	var hpfCutoffFreq = cutoff.linexp(0.5, 1, 20, 10000);
	osc = In.ar(bus, 2);
	osc = RHPF.ar(RLPF.ar(osc,lpfCutoffFreq), hpfCutoffFreq);
	ReplaceOut.ar(bus, osc);
}).add;

SynthDef.new(\globalCompressor, {
    |bus, level=1, dist=1|

	var signal = In.ar(bus, 2);
	signal = Compander.ar(signal, signal, 0.3,
		slopeBelow: 1,
        slopeAbove: 0.5,
        clampTime: 0.01,
        relaxTime: 0.01
	);
	signal = (signal * dist).tanh * (level / dist.max(1));

	ReplaceOut.ar(bus, signal);
}).add;

SynthDef.new(\globalChorus, {
	|bus, level=0.0|
	var source, chorus, signal;

	source = In.ar(bus, 2);
	chorus = Mix.fill(7, {
		var maxdelaytime = rrand(0.005, 0.02);

		DelayC.ar(source, maxdelaytime, LFNoise1.kr(Rand(4.5, 10.5), 0.25*maxdelaytime,0.75*maxdelaytime) )
	});

	XOut.ar(bus, level, chorus);

}).add;

)
