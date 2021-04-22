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
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.groups.WallpaperGroup;

public class FeedbackPositionProvider extends AbstractPositionProvider {
	double 		globalSpeed = .03,
		curvature = .05,
		mixer = .5;
	double orient = 1;
	double[][] verts;
	double t = 0, dt = .01;
	int elCount = 10;
	DiscreteGroupElement[] grpels;
	// a bit of a hack, rather than automatically detecting all subclasses in class path
	static {
		AbstractPositionProvider.registerSubclass(new FeedbackPositionProvider());
	}
	public void update()	{
		double[] pos4 = {position[0], position[1], 0, position[2]};
		double[][] orbit = new double[elCount][];
		double[] sum = new double[4];
		for (int i = 0; i<elCount; ++i)	{
			orbit[i] = Rn.matrixTimesVector(null, grpels[i+1].getArray(), pos4);
			Rn.subtract(orbit[i], orbit[i], pos4);
			Rn.add(sum, sum, orbit[i]);
		}
		Rn.times(sum, dt, sum);
		Rn.add(pos4, pos4, sum);
		position[0] = pos4[0];
		position[1] = pos4[1];
		position[2] = pos4[3];
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
		TextSlider gslider = new TextSlider.Double("speed",SwingConstants.HORIZONTAL,0.0,.2, globalSpeed);
		gslider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				globalSpeed = ((TextSlider) e.getSource()).getValue().doubleValue();
			}
		});
		box.add(gslider,c);
		TextSlider kslider = new TextSlider.Double("curvature",SwingConstants.HORIZONTAL,0.0,1, curvature);
		kslider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				curvature = ((TextSlider) e.getSource()).getValue().doubleValue();
			}
		});
		box.add(kslider,c);
		TextSlider mslider = new TextSlider.Double("randomness",SwingConstants.HORIZONTAL,0.0,1, mixer);
		mslider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				mixer = ((TextSlider) e.getSource()).getValue().doubleValue();
			}
		});
		box.add(mslider,c);
		super.getInspector().add(box,c);
		return super.getInspector();
	}
	@Override
	public void setWallpaperGroup(WallpaperGroup dg) {
		super.setWallpaperGroup(dg);
		IndexedFaceSet fd = (IndexedFaceSet) dg.getDefaultFundamentalRegion();
		verts = fd.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		grpels = DiscreteGroupUtility.generateElements(theGroup, new DiscreteGroupSimpleConstraint(elCount+1));
		
	}

	
}
