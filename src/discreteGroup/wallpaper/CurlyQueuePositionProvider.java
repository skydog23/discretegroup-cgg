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
import de.jreality.math.Rn;

public class CurlyQueuePositionProvider extends AbstractPositionProvider {
	double 		globalSpeed = 1.0,
		curvature = .05;
	double[] velocity = {Math.random(), Math.random(), 0};
	{
			double dd = Rn.euclideanNorm(velocity);
			Rn.times(velocity, .03/dd, velocity);
	}

	double t = 0, dt = .01;
	// a bit of a hack, rather than automatically detecting all subclasses in class path
	static {
		AbstractPositionProvider.registerSubclass(new CurlyQueuePositionProvider());
	}
	public void update()	{
 		t+=dt*globalSpeed;
		double x = position[0], y = position[1];
//	    currentVelocity[0] += globalSpeed*currentAcceleration[0];
//	    currentVelocity[1] += globalSpeed*currentAcceleration[1];
	    x += globalSpeed*velocity[0];
	    y += globalSpeed*velocity[1]; 
	    double s = Math.sin(t) * curvature*globalSpeed;
	    double f = Rn.euclideanNorm(velocity);
	    velocity[0] += -s * velocity[1];
	    velocity[1] += s * velocity[0];
	    Rn.setToLength(velocity, velocity, f);	
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
		if (velocity.length >= 3) velocity[2] = 0.0;
		double dd = Rn.euclideanNorm(velocity);
		Rn.times(velocity, .03/dd, velocity);
	}
	
	public double getGlobalSpeed() {
		return globalSpeed;
	}

	public void setGlobalSpeed(double globalSpeed) {
		this.globalSpeed = globalSpeed;
	}

	public double getCurvature() {
		return curvature;
	}

	public void setCurvature(double curvature) {
		this.curvature = curvature;
	}
	@Override
	public JComponent getInspector() {
		Box box = Box.createVerticalBox(); //(Box) super.getInspector();
		Insets insets = new Insets(1,0,1,0);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = insets;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.anchor = GridBagConstraints.WEST;
		TextSlider gslider = new TextSlider.Double("speed",SwingConstants.HORIZONTAL,0.0,4.0, globalSpeed);
		gslider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				globalSpeed = ((TextSlider) e.getSource()).getValue().doubleValue();
			}
		});
		box.add(gslider,c);
		TextSlider kslider = new TextSlider.Double("curvature",SwingConstants.HORIZONTAL,0.0,.2, curvature);
		kslider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				curvature = ((TextSlider) e.getSource()).getValue().doubleValue();
			}
		});
		box.add(kslider,c);
		super.getInspector().add(box,c);
		return super.getInspector();
	}

	
}
