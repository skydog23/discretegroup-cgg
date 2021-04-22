package discreteGroup.tools;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.sound.midi.Instrument;
import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.util.MyMidiSynth;
import charlesgunn.util.TextSlider;

public class MidiSoundEffects {
	MyMidiSynth midi = new MyMidiSynth();
	int[] indices = { 12, 13, 15, 106, 107, 108, 114,115,116};
	String[] names = {"marimba","xylophone","dulcimer",
			"shamisen","koto", "kalimba",
			"steel drum", "woodblock", "taiko drum"
	};
	int currentInstrument = indices[0], currentChannel = 0, moveCh = 0, endCh = 1;
	int moveInst = 112, endInst =  115;
	double pitch = 45, velocity = 50;
	boolean doSound = true;
	static boolean canDoMidi = true;
	public void setDoSound(boolean doSound) {
		if (!canDoMidi) return;
		if (doSound)	{
			canDoMidi = midi.open();
			if (!canDoMidi) return;
			Instrument[] insts = midi.getInstruments();
			for (int j = 0; j<127; ++j)	{
				midi.getSynthesizer().loadInstrument(insts[j]);
//				midi.getChannels()[j].channel.programChange(indices[j]);			
			}
			} else midi.close();
	}

	public void initMoving()	{
		if (!(canDoMidi && doSound)) return;
		midi.getChannels()[moveCh].channel.allSoundOff();
		midi.getChannels()[endCh].channel.allSoundOff();
		midi.getChannels()[moveCh].channel.allNotesOff();
		midi.getChannels()[endCh].channel.allNotesOff();
		midi.getChannels()[moveCh].channel.programChange(moveInst);	
		System.err.println("setting program change to "+moveInst);
		midi.getChannels()[moveCh].setVelocity((int) velocity);
	}
	double pitch1, pitch2;
	public void playMoving(double t)	{
		if (canDoMidi && doSound) {
			midi.getChannels()[moveCh].channel.noteOn(
					(int) ((1+t)*pitch), 
					midi.getChannels()[moveCh].getVelocity());
		}
		
	}
	
	public void playEnd()	{
		if (canDoMidi && doSound) {
//			try {
//				Thread.sleep(15);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			System.err.println("Playing end");
			midi.getChannels()[endCh].channel.programChange(endInst);			
			midi.getChannels()[endCh].setVelocity((int) (2*velocity));
			midi.getChannels()[endCh].channel.allSoundOff();
			midi.getChannels()[endCh].channel.programChange(endInst);					
			midi.getChannels()[endCh].channel.noteOn(
					(int) (pitch), 
					midi.getChannels()[endCh].getVelocity());
//			try {
//				Thread.sleep(25);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			midi.getChannels()[currentChannel].channel.allNotesOff();
		}
	}
	
	public Component getInspector()	{
		Box vbox = Box.createVerticalBox();
		final TextSlider moving = new TextSlider.Integer("midi moving",SwingConstants.HORIZONTAL,0, 127, moveInst);
		moving.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				moveInst = moving.getValue().intValue();
				System.err.println("got move inst = "+moveInst);
				midi.getChannels()[currentChannel].channel.programChange(moveInst);					
			}
			
		});
		vbox.add(moving);
		
		final TextSlider end = new TextSlider.Integer("midi end",SwingConstants.HORIZONTAL,0, 127, endInst);
		end.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				endInst = end.getValue().intValue();
//				midi.getChannels()[currentChannel].channel.programChange(endInst);					
			}
			
		});
		vbox.add(end);
		final TextSlider ptch = new TextSlider.Double("pitch",SwingConstants.HORIZONTAL,20, 255, pitch);
		ptch.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				pitch = ptch.getValue().intValue();
			}
			
		});
		vbox.add(ptch);
		final TextSlider vel = new TextSlider.Double("velocity",SwingConstants.HORIZONTAL,20, 255, velocity);
		vel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				velocity = vel.getValue().intValue();
			}
			
		});
		vbox.add(vel);
		return vbox;
	}
}
