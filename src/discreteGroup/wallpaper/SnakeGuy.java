/*
 * Created on Feb 14, 2012
 *
 */
package discreteGroup.wallpaper;

import java.awt.Graphics2D;

public class SnakeGuy {

	double phase = 0.0,
		length = 3.0,
		amplitude = 1.0,
		decay = .5,
		doofle = .5,
		scale = .02;

	int numSegs = 50;
	
	double[][] points = new double[numSegs][2];
	
	public void draw(Graphics2D g) {
		for (int i =0; i<numSegs; ++i)	{
			double t = (i/(numSegs-1.0));
			double angle = phase + length * Math.PI*2 * t;
			double s = Math.sin(angle);
			double x = scale * angle;
			double y = scale * amplitude * ( 1 - decay * t) * s;
			if (i > 0) {
			}
		}
	}


	public double getPhase() {
		return phase;
	}


	public void setPhase(double phase) {
		this.phase = phase;
	}


	public double getLength() {
		return length;
	}


	public void setLength(double length) {
		this.length = length;
	}


	public double getDecay() {
		return decay;
	}


	public void setDecay(double decay) {
		this.decay = decay;
	}


	public double getDoofle() {
		return doofle;
	}


	public void setDoofle(double doofle) {
		this.doofle = doofle;
	}

	
	public double getAmplitude() {
		return amplitude;
	}


	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
	}


	public double getScale() {
		return scale;
	}


	public void setScale(double scale) {
		this.scale = scale;
	}


	public int getNumSegs() {
		return numSegs;
	}


	public void setNumSegs(int numSegs) {
		this.numSegs = numSegs;
	}


}
