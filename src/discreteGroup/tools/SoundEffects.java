/*
 * Created on May 11, 2009
 *
 */
package discreteGroup.tools;

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import discreteGroup.ResourceClass;


public class SoundEffects {
	Clip clip;
	public SoundEffects()	{
		init();
	}
	private void init() {
		BufferedInputStream is = new BufferedInputStream(ResourceClass.class.getResourceAsStream("resources/sounds/kiss.wav"));
        AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(is);
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		AudioFormat format = audioInputStream.getFormat();
		DataLine.Info info = new DataLine.Info(Clip.class, format); // format is an AudioFormat object
		if (!AudioSystem.isLineSupported(info)) {
		    // Handle the error.
		    }
		    // Obtain and open the line.
		try {
		    clip = (Clip) AudioSystem.getLine(info);
		    clip.open(audioInputStream);
		    System.err.println("Got clip");
		} catch (LineUnavailableException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void play()	{
		clip.loop(1);
		clip.start();
	}
}
