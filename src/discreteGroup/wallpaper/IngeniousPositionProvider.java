/*
 * Created on Sep 7, 2008
 *
 */
package discreteGroup.wallpaper;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

import charlesgunn.util.TextSlider;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jtem.discretegroup.groups.WallpaperGroup;

public class IngeniousPositionProvider extends AbstractPositionProvider {
	double 		globalSpeed = .056,
		acceleration = .06, oscillation = 3.7, beginningAngle = 0.0;
	double[] velocity, currentAcceleration;
	double t = 0.01, dt = .001;
	double[][] fdCorners, diffs;
	double[] weights;
	private double factor = .9;
	private TextSlider aslider;
	
	public IngeniousPositionProvider(double[] initP, double[] initV) {
		position =  initP.clone();
		velocity =  initV.clone();
		double dd = Rn.euclideanNorm(velocity);
		Rn.times(velocity, .03/dd, velocity);
	}

	public void update()	{
 		t+=dt; 
		double x = position[0], y = position[1];
//	    currentVelocity[0] += globalSpeed*currentAcceleration[0];
//	    currentVelocity[1] += globalSpeed*currentAcceleration[1];
	    x += globalSpeed*velocity[0];
	    y += globalSpeed*velocity[1]; 
//	    double s = Math.sin(t) * curvature*globalSpeed;
	    double f = Rn.euclideanNorm(velocity);
//	    velocity[0] += -s * velocity[1];
//	    velocity[1] += s * velocity[0];
//	    weights[0] = .2* Math.sin(t+.5) * Math.cos(2*t);
//	    weights[1] = .5* Math.sin(.7*t) * Math.sin(1.4*t);
//	    weights[2] = .7* Math.cos(t);
	    double[] total = new double[diffs[0].length];
	    double minDistance = 10.0;
	    for (int i = 0; i<fdCorners.length; ++i)	{
	    		Rn.subtract(diffs[i], position, fdCorners[i]);
	    		for (int j = 2; j<diffs[i].length; ++j) diffs[i][j] = 0.0;
	    		double d = Rn.euclideanNorm(diffs[i]);
	    		if (d < minDistance) minDistance = d;
	    		// use an inverse square law
	    		Rn.times(diffs[i], 1.0/(d*d), diffs[i]);
	    		Rn.add(total,total,diffs[i]);
//	    		System.err.println("d = "+d);
	    		weights[(i)%fdCorners.length] = d; //1.0/d;
	    }
	    double rangle = 0.0;
	    if (oscillation != 0.0) rangle = Math.sin(oscillation*t) * Math.PI * factor;
	    else rangle = beginningAngle;
	    aslider.setValue(rangle);
	    double[] mat = MatrixBuilder.euclidean().rotateZ(rangle).getArray();
	    double[] perpVec = Rn.times(null, minDistance, Rn.matrixTimesVector(null, mat, total));
	    	Rn.add(velocity, velocity, 
	    		Rn.times(null, acceleration, perpVec));
//	    			Rn.linearCombination(null, osc*weights[i],diffs[i], (1-osc)*weights[i], diffs[(i+1)%diffs.length])));
//	    }
//	    Rn.add(velocity, velocity, Rn.times(null, acceleration, total));
	    Rn.setToLength(velocity, velocity, 1);//f);	
	    position[0] = x; position[1] = y;
	}
	@Override
	public void applyMatrix(double[] m) {
		super.applyMatrix(m);
		Rn.matrixTimesVector(velocity, m, velocity);
	}
	
	public double[] getVelocity() { return velocity; }
	public void setVelocity(double[] v)	{
		velocity = v.clone();
	}
	
	public double getGlobalSpeed() {
		return globalSpeed;
	}

	public void setGlobalSpeed(double globalSpeed) {
		this.globalSpeed = globalSpeed;
	}

	public double getCurvature() {
		return acceleration;
	}

	public void setCurvature(double curvature) {
		this.acceleration = curvature;
	}
	Box box;
	@Override
	public JComponent getInspector() {
		if (box != null) return super.getInspector();
		Insets insets = new Insets(1,0,1,0);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = insets;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.anchor = GridBagConstraints.WEST;
		box = Box.createVerticalBox();
		TextSlider gslider = new TextSlider.DoubleLog("speed",SwingConstants.HORIZONTAL,0.001,4.0, globalSpeed);
		gslider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				globalSpeed = ((TextSlider) e.getSource()).getValue().doubleValue();
			}
		});
		box.add(gslider,c);
		TextSlider kslider = new TextSlider.DoubleLog("acceleration",SwingConstants.HORIZONTAL,0.001,1, acceleration);
		kslider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				acceleration = ((TextSlider) e.getSource()).getValue().doubleValue();
			}
		});
		box.add(kslider,c);

		TextSlider oslider = new TextSlider.DoubleLog("oscillation",SwingConstants.HORIZONTAL,0.1,1000, oscillation+.1);
		oslider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				oscillation = ((TextSlider) e.getSource()).getValue().doubleValue()-.1;
			}
		});
		box.add(oslider,c);

		aslider = new TextSlider.Double("angle",SwingConstants.HORIZONTAL,-Math.PI, Math.PI, beginningAngle);
		aslider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				beginningAngle = ((TextSlider) e.getSource()).getValue().doubleValue()-.1;
			}
		});
		box.add(aslider,c);
		TextSlider fslider = new TextSlider.Double("angle factor",SwingConstants.HORIZONTAL,0, 1.0, factor);
		fslider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				factor = ((TextSlider) e.getSource()).getValue().doubleValue()-.1;
			}
		});
		box.add(fslider,c);
		super.getInspector().add(box,c);
		return super.getInspector();
	}

	@Override
	public void setWallpaperGroup(WallpaperGroup dg) {
		super.setWallpaperGroup(dg);
		fdCorners = ((IndexedFaceSet) dg.getDefaultFundamentalRegion()).getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		diffs = new double[fdCorners.length][fdCorners[0].length];
		weights = new double[fdCorners.length];
	}


}
