    // Engine_Thirtythree

// Inherit methods from CroneEngine
Engine_Thirtythree : CroneEngine {

    // Thirtythree specific v0.1.0
    var sampleBuffThirtythree;
    var playerThirtythree;
    // var osfunThirtyThree;
    // Thirtythree ^

    *new { arg context, doneCallback;
        ^super.new(context, doneCallback);
    }

    alloc {
        // Thirtythree specific v0.0.1
        sampleBuffThirtythree = Array.fill(64, { arg i; 
            Buffer.new(context.server);
        });

        (0..25).do({arg i; 
            SynthDef("playerThirtythree"++i,{ 
                arg bufnum, amp=0, ampLag=0,t_trig=0,t_trigtime=0,fadeout=0.05,
                sampleStart=0,sampleEnd=1,samplePos=0,
                rate=0,rateSlew=0,bpm_sample=1,bpm_target=1,
                bitcrush=0,bitcrush_bits=8,bitcrush_rate=23000,
                fx_scratch=0,fx_strobe=0,vinyl=0,loop=0,
                timestretch=0,timestretchSlowDown=1,timestretchWindowBeats=1,
                pan=0,lpf=20000,lpflag=0,hpf=10,hpflag=0,lpf_resonance=1,hpf_resonance=1,
                use_envelope=1,fx_reverse=0,fx_autopan=0,fx_octaveup=0,fx_octavedown=0;
    
                // vars
                var snd,pos,timestretchPos,timestretchWindow,env;

                env=EnvGen.ar(
                    Env.new(
                        levels: [0,1,1,0], 
                        times: [0.01,(sampleEnd-sampleStart)*(BufDur.kr(bufnum)/(BufRateScale.kr(bufnum)*rate))-fadeout-0.01-0.01,fadeout],
                        curve:\sine,
                    ), 
                    gate: t_trig,
                );

                // reverse effect
                rate = ((fx_reverse<1)*rate)+((fx_reverse>0)*rate.neg);

                // octave up
                rate = ((fx_octaveup<1)*rate)+((fx_octaveup>0)*rate*2);

                // octave down
                rate = ((fx_octavedown<1)*rate)+((fx_octavedown>0)*rate*0.5);

                // rate slew
                rate = Lag.kr(rate,rateSlew);

                // scratch effect
                rate = (fx_scratch<1*rate) + (fx_scratch>0*LFTri.kr(bpm_target/60*2));

                pos = Phasor.ar(
                    trig:t_trig,
                    rate:BufRateScale.kr(bufnum)*rate,
                    start:((sampleStart*(rate>0))+(sampleEnd*(rate<0)))*BufFrames.kr(bufnum),
                    end:((sampleEnd*(rate>0))+(sampleStart*(rate<0)))*BufFrames.kr(bufnum),
                    resetPos:samplePos*BufFrames.kr(bufnum)
                );
                // timestretchPos = Phasor.ar(
                //     trig:t_trigtime,
                //     rate:BufRateScale.kr(bufnum)*rate/timestretchSlowDown,
                //     start:((sampleStart*(rate>0))+(sampleEnd*(rate<0)))*BufFrames.kr(bufnum),
                //     end:((sampleEnd*(rate>0))+(sampleStart*(rate<0)))*BufFrames.kr(bufnum),
                //     resetPos:pos
                // );
                // timestretchWindow = Phasor.ar(
                //     trig:t_trig,
                //     rate:BufRateScale.kr(bufnum)*rate,
                //     start:timestretchPos,
                //     end:timestretchPos+((60/bpm_target/timestretchWindowBeats)/BufDur.kr(bufnum)*BufFrames.kr(bufnum)),
                //     resetPos:timestretchPos,
                // );

                snd=BufRd.ar(2,bufnum,pos,
                    loop:0,
                    interpolation:1
                );
                // timestretch=Lag.kr(timestretch,0.2);
                // snd=((1-timestretch)*snd)+(timestretch*BufRd.ar(2,bufnum,
                //  `   timestretchWindow,
                //     loop:0,
                //     interpolation:1
                // ));

                snd = RLPF.ar(snd,Lag.kr(lpf,lpflag),lpf_resonance);
                snd = RHPF.ar(snd,Lag.kr(hpf,hpflag),hpf_resonance);

                // fx_strobe
                // snd = ((fx_strobe<1)*snd)+((fx_strobe>0)*snd*LFPulse.ar(bpm_target/60*2));

                // bitcrush
                // snd = (snd*(1-bitcrush))+(bitcrush*Decimator.ar(snd,bitcrush_rate,bitcrush_bits));

                // manual panning
                amp = Lag.kr(amp,ampLag)*(((use_envelope>0)*env)+(use_envelope<1));
                snd = Balance2.ar(snd[0],snd[1],
                    pan+SinOsc.kr(60/bpm_target*16,mul:fx_autopan*0.5),
                    level:amp,
                );

                // // send position message for player 1 only
                // if (i<2,{
                //     SendTrig.kr(Impulse.kr(30),amp,A2K.kr(((1-timestretch)*pos)+(timestretch*timestretchPos))/BufFrames.kr(bufnum)/BufRateScale.kr(bufnum));                        
                // },{});

                Out.ar(0,snd)
            }).add; 
        });

        // osfunThirtyThree = OSCFunc({ 
        //     arg msg, time; 
        //         // [time, msg].postln;
        //     NetAddr("127.0.0.1", 10111).sendMsg("tt_pos",msg[2],msg[3]);  
        // },'/tr', context.server.addr);

        playerThirtythree = Array.fill(25,{arg i;
            Synth("playerThirtythree"++i, target:context.xg);
        });

        
        this.addCommand("tt_load","is", { arg msg;
            // lua is sending 1-index
            sampleBuffThirtythree[msg[1]-1].free;
            sampleBuffThirtythree[msg[1]-1] = Buffer.read(context.server,msg[2]);
        });

        this.addCommand("tt_play","iiffffffff", { arg msg;
            // lua is sending 1-index
            playerThirtythree[msg[1]-1].set(
                \t_trig,1,
                \bufnum,sampleBuffThirtythree[msg[2]-1],
                \amp,msg[3],
                \rate,msg[4],
                \samplePos,msg[5],
                \sampleStart,msg[5],
                \sampleEnd,msg[6],
                \lpf,msg[7],
                \lpf_resonance,msg[8],
                \lpflag,0,
                \hpf,msg[9],
                \hpf_resonance,msg[10],
                \hpflag,0,
                \use_envelope,1,
                // turn off effects
                \fx_strobe,0,
                \fx_scratch,0,
                \fx_reverse,0,
                \fx_octaveup,0,
                \fx_autopan,0,
                \fx_octavedown,0,
                \bitcrush,0,
                \timestretch,0,
            );
        });

        this.addCommand("tt_amp","iff", { arg msg;
            // lua is sending 1-index
            playerThirtythree[msg[1]-1].set(
                \amp,msg[2],
                \ampLag,msg[3],
            );
        });

        this.addCommand("tt_rate","iff", { arg msg;
            // lua is sending 1-index
            playerThirtythree[msg[1]-1].set(
                \rate,msg[2],
                \rateSlew,msg[3],
            );
        });

        this.addCommand("tt_bpm","f", { arg msg; 
            (0..25).do({arg i; 
                playerThirtythree[i].set(
                    \bpm_target,msg[1],
                );
            }); 
        });

        this.addCommand("tt_lpf","ifff", { arg msg;
            // lua is sending 1-index
            playerThirtythree[msg[1]-1].set(
                \lpf,msg[2],
                \lpflag,msg[3],
                \hpf,20,
                \hpflag,msg[3],
                \lpf_resonance,msg[4],
                \hpf_resonance,1,
            );
        });
        
	   this.addCommand("tt_hpf","ifff", { arg msg;
            // lua is sending 1-index
            playerThirtythree[msg[1]-1].set(
                \lpf,20000,
                \lpflag,msg[3],
                \hpf,msg[2],
                \hpflag,msg[3],
                \hpf_resonance,msg[4],
                \lpf_resonance,1,
            );
        });

        this.addCommand("tt_bitcrush_all","ifff", { arg msg;
            // lua is sending 1-index
            (0..25).do({arg i; 
              playerThirtythree[i].set(
        		\bitcrush,msg[2],
        		\bitcrush_bits,msg[3],
        		\bitcrush_rate,msg[4],
            )});
        });


        this.addCommand("tt_fx_loop","iffff", { arg msg;
            // lua is sending 1-index
            playerThirtythree[msg[1]-1].set(
                \samplePos,msg[2],
                \sampleStart,msg[3],
                \sampleEnd,msg[4],
                \use_envelope,msg[5], // effectively used to turn on and off
            );
        });


        this.addCommand("tt_bitcrush","ifff", { arg msg;
            // lua is sending 1-index
            playerThirtythree[msg[1]-1].set(
                \bitcrush,msg[2],
                \bitcrush_bits,msg[3],
                \bitcrush_rate,msg[4],
             );
        });

        this.addCommand("tt_fx_reverse","if", { arg msg;
            // lua is sending 1-index
            playerThirtythree[msg[1]-1].set(
                \fx_reverse,msg[2],
            );
        });

        this.addCommand("tt_fx_octaveup","if", { arg msg;
            // lua is sending 1-index
            playerThirtythree[msg[1]-1].set(
                \fx_octaveup,msg[2],
            );
        });

        this.addCommand("tt_fx_octavedown","if", { arg msg;
            // lua is sending 1-index
            playerThirtythree[msg[1]-1].set(
                \fx_octavedown,msg[2],
            );
        });

        this.addCommand("tt_fx_timestretch","ifff", { arg msg;
            // lua is sending 1-index
            playerThirtythree[msg[1]-1].set(
                \timestretch,msg[2],
                \timestretchWindowBeats,msg[3],
                \timestretchSlowDown,msg[4],
            );
        });

        this.addCommand("tt_fx_autopan","if", { arg msg;
            // lua is sending 1-index
            playerThirtythree[msg[1]-1].set(
                \fx_autopan,msg[2],
            );
        });

        this.addCommand("tt_fx_scratch","if", { arg msg;
            // lua is sending 1-index
            playerThirtythree[msg[1]-1].set(
                \fx_scratch,msg[2],
            );
        });

        this.addCommand("tt_fx_strobe","if", { arg msg;
            // lua is sending 1-index
            playerThirtythree[msg[1]-1].set(
                \fx_strobe,msg[2],
            );
        });

        this.addCommand("tt_use_envelope","if", { arg msg;
            // lua is sending 1-index
            playerThirtythree[msg[1]-1].set(
                \use_envelope,msg[2],
            );
        });

        // ^ Thirtythree specific

    }

    free {
        // Thirtythree Specific v0.0.1
        (0..64).do({arg i; sampleBuffThirtythree[i].free});
        (0..25).do({arg i; playerThirtythree[i].free});
        // osfunThirtyThree.free;
        // ^ Thirtythree specific
    }
}
