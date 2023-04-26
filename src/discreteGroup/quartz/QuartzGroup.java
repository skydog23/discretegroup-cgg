/*
 * Created on 13 Apr 2023
 *
 */
package discreteGroup.quartz;

import static discreteGroup.quartz.QuartzConstants.axis3Pts;
import static discreteGroup.quartz.QuartzConstants.chan31Color;
import static discreteGroup.quartz.QuartzConstants.chan32Color;
import static discreteGroup.quartz.QuartzConstants.hex2Trans;
import static discreteGroup.quartz.QuartzConstants.hex3Pts;
import static discreteGroup.quartz.QuartzConstants.hexTrans;
import static discreteGroup.quartz.QuartzConstants.yAxisPts;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import charlesgunn.jreality.newtools.AnimatedIsometry;
import charlesgunn.math.Biquaternion;
import charlesgunn.math.Biquaternion.Metric;
import charlesgunn.math.IsometryAxis;
import charlesgunn.util.TextSlider;
import charlesgunn.util.TextSlider.IntegerLog;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jtem.discretegroup.core.AbstractDGSGR;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupConstraint;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.SimpleDGSGR;

public class QuartzGroup {

	{
//		double sq3 = 1.0/Math.sqrt(3);
//		double[] zDir = {0,0,1,0},
//				C1 = {1.0/3.0, 0, 0, 1},
//				C2 = {0, sq3, 0, 1};
//		screw33[0] = new Matrix(P3.makeScrewMotionMatrix(null, C1, zDir, Math.PI/3.0, Pn.EUCLIDEAN));
//		screw33[1] = new Matrix(P3.makeScrewMotionMatrix(null, C2, zDir, Math.PI/3.0, Pn.EUCLIDEAN));

	}
	/*
	 * There are four levels of hierarchy here:
	 *   0: order-2 rotation around the y-axis
	 *   1: order-3 screw motion around the triangle channel vertical axis thru (1/3,0,0,1)
	 *   2: order-3 screw motion around the hexagonal channel vertical axis through (0, .57..., 0, 1)
	 *   3: translation group
	 * The code refers to these levels with the prefixes L0, L1 L2, L3, resp..
	 */
//	DiscreteGroup L3G, L2G, L1G, L0G;
//	DiscreteGroup[] grps = {L0G, L1G, L2G, L3G};
	Matrix[] L3M = new Matrix[8], 
			L2M = new Matrix[3],
			L1M = new Matrix[3], 
			L0M = new Matrix[2];
	//Matrix[][] gens = {L0Gens,L1Gens,L2Gens, L3Gens};
	Matrix zTranslate = new Matrix();
	
	double c = 1.25;
	{
		for (int i = 0; i<3; ++i) {
			L1M[i] = MatrixBuilder.euclidean().
					rotate(axis3Pts[0], axis3Pts[1], -2*(i/3.0)*Math.PI).
					translate(0,0,i*c).
					getMatrix();
			L2M[i] = MatrixBuilder.euclidean().
					translate(QuartzConstants.hex2Trans[i]).
					getMatrix();
		}
		for (int i = 0; i<4; ++i) {
			L3M[i] = new Matrix(P3.makeTranslationMatrix(null, hexTrans[i], Pn.EUCLIDEAN));
		}
	}

	DiscreteGroup L3G = new DiscreteGroup();
	DiscreteGroupElement[] L0Gens = new DiscreteGroupElement[2],
			L1Gens = new DiscreteGroupElement[3],
			L2Gens = new DiscreteGroupElement[3],
			L3Gens = new DiscreteGroupElement[8];
	DiscreteGroupElement id, gen;
	DiscreteGroupSceneGraphRepresentation L3SGR;
	AbstractDGSGR  L0SGR, L1SGR, L2SGR;
	AbstractDGSGR[] sgrList = new AbstractDGSGR[4];
	DiscreteGroupSimpleConstraint bigC, prunerC;
	
	int maxL = 1, numEl = 100;
	double maxD = 3;
	
	boolean showHalfTurn = true,
			startBig = true;
	
	AnimatedIsometry ai[] = new AnimatedIsometry[3];
	
	QuartzCrystal owner;
	QuartzGeometry geom;
	QuartzGroup(QuartzCrystal o) {
		owner = o;
		geom = owner.quartzGeom;
//		init();
	}
	
	
	public void init() {
		
		id = new DiscreteGroupElement(Pn.EUCLIDEAN, Rn.identityMatrix(4), "");
		id.setColorIndex(0);
		Matrix m = MatrixBuilder.euclidean().rotate(yAxisPts[0], yAxisPts[1], Math.PI).getMatrix();
		gen = new DiscreteGroupElement(Pn.EUCLIDEAN, m.getArray(), "r");
		gen.setColorIndex(1);
		L0SGR = new SimpleDGSGR();
		L0SGR.getRepresentationRoot().setName("Level 0");
		L0SGR.setElementList(new DiscreteGroupElement[] {id,gen});
		
		Appearance ap2list[] = {new Appearance(), new Appearance()};
		ap2list[0].setAttribute("lineShader.diffuseColor", chan32Color);
		ap2list[1].setAttribute("lineShader.diffuseColor", chan31Color);
		L0SGR.setAppList(ap2list);
		L0SGR.update();
	
		L1SGR = new SimpleDGSGR();
		L1SGR.getRepresentationRoot().setName("Level 1");
		String l1n[] = {"","s","ss"};
		for (int i = 0; i<3; ++i) {
			L1M[i] = MatrixBuilder.euclidean().
					rotate(axis3Pts[0], axis3Pts[1], -2*(i/3.0)*Math.PI).
					translate(0,0,i*c/3.0).
					getMatrix();
			L1Gens[i] = new DiscreteGroupElement(Pn.EUCLIDEAN, L1M[i].getArray(), l1n[i]);
		}
		L1SGR.setElementList(L1Gens);
		L1SGR.update();

		L2SGR = new SimpleDGSGR();
		L2SGR.getRepresentationRoot().setName("Level 2");
		String l2n[] = {"","t","tt"};
		for (int i = 0; i<3; ++i) {
			L2M[i] = MatrixBuilder.euclidean().
					translate(hex2Trans[i]).
//					rotate(hex3Pts[0], hex3Pts[1], -2*(i/3.0)*Math.PI).
//					translate(0,0,i*c/3.0).
					getMatrix();
			L2Gens[i] = new DiscreteGroupElement(Pn.EUCLIDEAN, L2M[i].getArray(), l2n[i]);
		}
		L2SGR.setElementList(L2Gens);
		L2SGR.update();

		String[] enames = {"a","b","c","d"};
		for (int i = 0; i<4; ++i) {
			L3Gens[i] = new DiscreteGroupElement(Pn.EUCLIDEAN, L3M[i].getArray(), enames[i]);
			L3Gens[i+4] = L3Gens[i].getInverse();
		}
		L3G.setGenerators(L3Gens);
		L3G.setDimension(3);
		L3G.setMetric(Pn.EUCLIDEAN);
		L3G.setFinite(false);
		L3G.setName("alpha quartz group");

//		bigC = new DiscreteGroupSimpleConstraint(7, 7, 1000);
		bigC = new DiscreteGroupSimpleConstraint(3,3,1);
		bigC.setManhattan(true);

		
//		L3G.setConstraint(new DiscreteGroupTranslationConstraint(an, bn, cn, dn, "abcd"));
		L3G.setConstraint(bigC);
		L3G.update();
		System.err.println("big group # = "+L3G.getElementList().length);
	
		// we use the full blown DGSGR for the highest level group
		L3SGR = new DiscreteGroupSceneGraphRepresentation(L3G);
		L3SGR.getRepresentationRoot().setName("Level 3");

		prunerC = new DiscreteGroupSimpleConstraint(maxD, maxL, numEl);
		L3SGR.setConstraint(prunerC);
		L3SGR.update();
		sgrList = new AbstractDGSGR[] {L0SGR, L1SGR, L2SGR, L3SGR};
	}

	public void updateC(double cc) {
		c = cc;

		for (int i = 0; i<3; ++i) {
			L1Gens[i].setArray(MatrixBuilder.euclidean().
					rotate(axis3Pts[0], axis3Pts[1], -2*(i/3.0)*Math.PI).
					translate(0,0,i*c/3.0).
					getMatrix().getArray());
			L2Gens[i].setArray(MatrixBuilder.euclidean().
					rotate(hex3Pts[0], hex3Pts[1], -2*(i/3.0)*Math.PI).
					translate(0,0,i*c).
					getMatrix().getArray());
		}
		L1SGR.setElementList(L1Gens);
		L1SGR.update();
		L2SGR.setElementList(L2Gens);
		L2SGR.update();
		zTranslate = MatrixBuilder.euclidean().translate(0,0,c).getMatrix();
		L3Gens[6] = new DiscreteGroupElement(Pn.EUCLIDEAN, zTranslate.getArray(), "d");
		L3Gens[7] = L3Gens[6].getInverse();
		L3G.update();
		
//		gens4[6] = new DiscreteGroupElement(Pn.EUCLIDEAN, m.getArray(), "c");
//		gens4[7] = gens4[6].getInverse();
//		spaceGroup4gens.update();		
		L3SGR.setElementList(null);
		L3SGR.update();
		
		anims[0] = new QuartzAnimation(L0SGR.getSceneGraphRepn(), duration, delay);
		anims[1] = new QuartzAnimation(L1SGR.getSceneGraphRepn(), duration, delay);
		anims[2] = new QuartzAnimation(L2SGR.getSceneGraphRepn(), duration, delay);
	}
	
	public void toggleHalfTurn()	{
		showHalfTurn = !showHalfTurn;
//		Transformation tt = new Transformation(showHalfTurn ? gen.getArray() : id.getArray());
//		sixcellRep.getSceneGraphRepn().getChildComponent(1).setTransformation(tt);
		L1SGR.getSceneGraphRepn().getChildComponent(1).setVisible(showHalfTurn);
	}


	public Matrix[] getTriChannelM() {
		return L1M;
	}



		
	DiscreteGroupConstraint trivialC = new DiscreteGroupSimpleConstraint(1,1,1);
	public void setSingle(int j) {
		AbstractDGSGR sgr = sgrList[j]; 
		boolean single = singleState[j];
		
		if (single) {
			oldC[j] = sgr.getConstraint();
			sgr.setConstraint(trivialC);
		}
		else {
			sgr.setConstraint(oldC[j]);	
			oldC[j] = null;
		}
		sgr.update();
	}
	
	public AbstractDGSGR[] getLevels() {
		return sgrList;
	}
	
	QuartzAnimation[] anims = new QuartzAnimation[3];
	double duration = 3.0, delay = .5;
	
	
	boolean[] singleState = new boolean[4];
	DiscreteGroupConstraint oldC[] = new DiscreteGroupConstraint[4];
	
	String[] animNames = {"anim 2-fold","anim 3-fold tri", "anim 3-fold hex"};
	boolean[] animState = new boolean[3];

	public Component getInspector() {
		Box container = Box.createVerticalBox();
		Box buttons = Box.createHorizontalBox();
		container.add(buttons);
			for (int i = 0; i<3; ++i)	{
				final int j = i;
				JButton jb = new JButton(animNames[i]);
				jb.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						animState[j] = !animState[j];
						if (animState[j]) anims[j].start();
						else anims[j].stop();
					}
				});
				buttons.add(jb);
			}
		buttons = Box.createHorizontalBox();
		container.add(buttons);

		for (int i = 0; i<sgrList.length; ++i)	{
			final int j = i;
			JButton jb = new JButton("Level"+j);
			jb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					singleState[j] = !singleState[j];
					setSingle(j);			
				}
			});
			buttons.add(jb);
		}
		
		final TextSlider<Double> aSlider = new TextSlider.Double("max dist",  SwingConstants.HORIZONTAL,1,10, maxD);
		aSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				maxD = aSlider.getValue().doubleValue();
				prunerC.setMaxDistance(maxD);	
				L3SGR.setConstraint(prunerC);
			}
		});
		container.add(aSlider);
		final TextSlider<Integer> bSlider = new TextSlider.Integer("max word",  SwingConstants.HORIZONTAL, 1, 10, maxL);
		bSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				maxL = bSlider.getValue().intValue();
				prunerC.setMaxWordLength(maxL);	
				L3SGR.setConstraint(prunerC);
			}
		});
		container.add(bSlider);
		final TextSlider cSlider = new TextSlider.IntegerLog("num Els",  SwingConstants.HORIZONTAL, 1, 10, 100);
		cSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				numEl = cSlider.getValue().intValue();
				prunerC.setMaxNumberElements(numEl);
				L3SGR.setConstraint(prunerC);
			}
		});
		container.add(cSlider);
//		final TextSlider<Integer> dSlider = new TextSlider.Integer("d",  SwingConstants.HORIZONTAL, 1, 10, dn);
//		dSlider.addActionListener(new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				dn = dSlider.getValue().intValue();
//				updateDims();		
//			}
//		});
//		container.add(dSlider);
		return container;
	}


}
