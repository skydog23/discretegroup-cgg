/*
 * Created on 26 Apr 2023
 *
 */
package discreteGroup.quartz;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Timer;

import charlesgunn.jreality.newtools.AnimatedIsometry;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import discreteGroup.tools.MidiSoundEffects;
import jogamp.opengl.GLDebugMessageHandler.StdErrGLDebugListener;

/*
 *  in many cases, Quartz animations involve 
 *  a list of sgcs that form a finite cyclic group
 *	The strategy is to use visibility controls and smooth interpolation via exp
 * 	of biquaternion log to animate the generation of all the copies, one-by-one. 
 */
public class QuartzAnimation {

	MidiSoundEffects mse = new MidiSoundEffects();
	boolean doSound = true;
	AnimatedIsometry[] animIsomL = null;
	Timer timer;
	boolean started = false;
	double duration, delay, totalDuration,
		t = 0, dt = .01;
	List<SceneGraphComponent> sglList;
	Transformation[] tlist;
	boolean[] visible;
	int steps = 0;
	// default is to do all the childrend
//	public QuartzAnimation(SceneGraphComponent parent, double dur, double del) {
//		new QuartzAnimation(parent, dur, del, parent.getChildComponentCount()-1);
//	}
	
	public QuartzAnimation(SceneGraphComponent parent, double dur, double del, int s) {
		steps = s;
		if (doSound) mse.setDoSound(true);
		duration= dur;
		delay = del;
		sglList = parent.getChildComponents();
		int n = sglList.size();
		if (steps < 1 || steps >= n) {
			System.err.println("QuartzAnimation: Not enough elements to animate.");
			return;
		}
		tlist = new Transformation[steps];
		animIsomL = new AnimatedIsometry[steps];
		totalDuration = (steps)*duration + (steps-1)*delay;
		// make all elements invisible but the first
		update();
		System.err.println("QA: "+parent.getName()+" sglList #"+sglList.size());
		timer = new Timer(10, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!started) {
					startAnimation();
				}
				if (t>totalDuration) {
					started = false;
					timer.stop();
					endAnimation();
					return;
				}
				t += dt;
				int step = (int) ( t/(duration+delay));
				double lt = t - step * (duration+delay);
//				System.err.println("QuartzAnimation: step = "+step+" time = "+lt);
				double[] m = animIsomL[step].getValueAtTime(lt/duration);
				tlist[step].setMatrix(m);
				if (doSound) mse.playMoving(lt/duration);
				if (!sglList.get(step+1).isVisible()) {
					sglList.get(step+1).setVisible(true);
				}
			}
		});
	}

	public void update() {
		SceneGraphComponent base = sglList.get(0);
		double[] baseM = base.getTransformation().getMatrix();
		for (int i = 1; i<=steps; ++i)	{
			SceneGraphComponent target = sglList.get(i);
			tlist[i-1] = target.getTransformation();
			double[] targetM = target.getTransformation().getMatrix();
			animIsomL[i-1] = new AnimatedIsometry(baseM, targetM, Pn.EUCLIDEAN);
			baseM = targetM;
			base = target;
		}
	}

	public AnimatedIsometry[] getAnimIsomL() {
		return animIsomL;
	}

	private void startAnimation() {
//		update();
		started = true;
		t = 0;
		mse.setDoSound(doSound);
		if (doSound) mse.initMoving();
		started = true;
		for (int i = 1; i<=steps; ++i) {
			sglList.get(i).setVisible(false);
		}
	}
	private void endAnimation() {
		timer.stop();
		started = false;
		mse.setDoSound(false);
		for (int i = 0; i<=steps; ++i) {
			sglList.get(i).setVisible(true);
		}
	}

	public void start() {startAnimation(); timer.start();}
	public void stop() {timer.stop(); endAnimation(); }

}
