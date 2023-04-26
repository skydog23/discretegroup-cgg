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
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import discreteGroup.tools.MidiSoundEffects;

/*
 *  in many cases, Quartz animations involve 
 *  a list of sgcs that form a finite cyclic group
 *	The strategy is to use visibility controls and smooth interpolation via exp
 * 	of biquaternion log to animate the generation of all the copies, one-by-one. 
 */
public class QuartzAnimation {

	MidiSoundEffects mse = new MidiSoundEffects();

	AnimatedIsometry[] animIsomL = null;
	Timer timer;
	boolean started = false;
	double duration, delay, totalDuration,
		t = 0, dt = .01;
	List<SceneGraphComponent> sglList;
	Transformation[] tlist;
	boolean[] visible;
	public QuartzAnimation(
			SceneGraphComponent parent,
			double dur,
			double del
			) {
		mse.setDoSound(true);
		duration= dur;
		delay = del;
		sglList = parent.getChildComponents();
		int n = sglList.size();
		if (n<2) {
			System.err.println("QuartzAnimation: Not enough elements to animate.");
			return;
		}
		tlist = new Transformation[n-1];
		animIsomL = new AnimatedIsometry[n-1];
		totalDuration = (n-1)*duration + (n-2)*delay;
		// make all elements invisible but the first
		SceneGraphComponent base = sglList.get(0);
		double[] baseM = base.getTransformation().getMatrix();
		for (int i = 1; i<n; ++i)	{
			SceneGraphComponent target = sglList.get(i);
			tlist[i-1] = target.getTransformation();
			double[] targetM = target.getTransformation().getMatrix();
			animIsomL[i-1] = new AnimatedIsometry(baseM, targetM, Pn.EUCLIDEAN);
			baseM = targetM;
			base = target;
		}
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
				System.err.println("QuartzAnimation: step = "+step+" time = "+lt);
				double[] m = animIsomL[step].getValueAtTime(lt/duration);
				tlist[step].setMatrix(m);
				mse.playMoving(lt/duration);
				if (!sglList.get(step+1).isVisible()) {
					sglList.get(step+1).setVisible(true);
				}
			}
		});
	}

	public AnimatedIsometry[] getAnimIsomL() {
		return animIsomL;
	}

	private void startAnimation() {
		started = true;
		t = 0;
		mse.setDoSound(true);
		mse.initMoving();
		started = true;
		for (int i = 1; i<sglList.size(); ++i) {
			sglList.get(i).setVisible(false);
		}
	}
	private void endAnimation() {
		timer.stop();
		started = false;
		mse.setDoSound(false);
		for (int i = 0; i<sglList.size(); ++i) {
			sglList.get(i).setVisible(true);
		}
	}

	public void start() {startAnimation(); timer.start();}
	public void stop() {timer.stop(); endAnimation(); }

}
