(
SynthDef.new(\lpf,
	{|bus, cutoff=1000, res=0.9|
		var osc;
		osc = In.ar(bus, 2);
		osc = RLPF.ar(osc, cutoff, res);
		ReplaceOut.ar(bus, osc)
	}
).add;

SynthDef.new(\hpf,
	{|bus, cutoff=2000, res=0.9|
		var osc;
		osc = In.ar(bus, 2);
		osc = RHPF.ar(osc, cutoff, res);
		ReplaceOut.ar(bus, osc)
	}
).add;

SynthDef.new(\delay,
    {|bus, echo=0.4, bpm=95, echotime=4|
		var osc;
		var beat_dur = 60 / bpm;
		osc = In.ar(bus, 2);
		osc = osc + CombL.ar(osc, delaytime: echo * beat_dur, maxdelaytime: 2 * beat_dur, decaytime: echotime * beat_dur);
		ReplaceOut.ar(bus, osc)
    }
).add;

SynthDef.new(\dist,
	{|bus, drive=0.1|
		var osc;
		osc = In.ar(bus, 2);
		osc = (osc * (drive * 50)).clip(0,0.2).fold2(2);
		ReplaceOut.ar(bus, osc)
	}
).add;

SynthDef.new(\chop,
	{|bus, chop=2, sus=1.0|
		var osc;
		osc = In.ar(bus, 2);
		osc = osc * LFPulse.kr(chop / sus, add: 0.01);
		ReplaceOut.ar(bus, osc)
	}
).add;

SynthDef.new(\makeSound,
	{ arg bus, sus; var osc;
		osc = In.ar(bus, 2);
		osc = EnvGen.ar(Env([1,1,0],[sus * 8, 0.1]), doneAction: 14) * osc;
		DetectSilence.ar(osc, amp:0.000000001, time: 0.5, doneAction: 14);
		OffsetOut.ar(0, osc[0]);
		OffsetOut.ar(1, osc[1]);
}).add;

)

