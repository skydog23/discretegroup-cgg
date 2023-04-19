/*
 * Created on 13 Apr 2023
 *
 */
package discreteGroup.quartz;

import static discreteGroup.quartz.QuartzConstants.axis3Pts;
import static discreteGroup.quartz.QuartzConstants.hex3Pts;
import static discreteGroup.quartz.QuartzConstants.yAxisPts;
import static discreteGroup.quartz.QuartzConstants.triTrans;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import charlesgunn.jreality.newtools.AnimatedIsometry;
import charlesgunn.util.TextSlider;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupTranslationConstraint;

public class QuartzGroup {

	{
//		double sq3 = 1.0/Math.sqrt(3);
//		double[] zDir = {0,0,1,0},
//				C1 = {1.0/3.0, 0, 0, 1},
//				C2 = {0, sq3, 0, 1};
//		screw33[0] = new Matrix(P3.makeScrewMotionMatrix(null, C1, zDir, Math.PI/3.0, Pn.EUCLIDEAN));
//		screw33[1] = new Matrix(P3.makeScrewMotionMatrix(null, C2, zDir, Math.PI/3.0, Pn.EUCLIDEAN));

	}
	DiscreteGroup fullGroup, hexChannelGroup, triChannelGroup, order2Group;
	Matrix[] fullGroupGens = new Matrix[8], 
			triTrans3M = new Matrix[3],
			hexChannelM = new Matrix[3], 
			triChannelM = new Matrix[3], 
			order2M = new Matrix[2];
	Matrix zTranslate = new Matrix();
	
	double c = 1.25;
	{
		for (int i = 0; i<3; ++i) {
			triChannelM[i] = MatrixBuilder.euclidean().
					rotate(axis3Pts[0], axis3Pts[1], -2*(i/3.0)*Math.PI).
					translate(0,0,i*c).
					getMatrix();
			triTrans3M[i] = new Matrix(P3.makeTranslationMatrix(null, triTrans[i], Pn.EUCLIDEAN));
			hexChannelM[i] = new Matrix(P3.makeScrewMotionMatrix(null, hex3Pts[0], hex3Pts[1], i*(2*Math.PI/3), Pn.EUCLIDEAN));
		}
	}

	DiscreteGroup triTrans3Group = new DiscreteGroup(),
			spaceGroup4gens = new DiscreteGroup(),
			spaceGroup33 = new DiscreteGroup();
	DiscreteGroup twoGroup = new DiscreteGroup();
	DiscreteGroupElement id, gen;
	DiscreteGroupElement triTransGens[];
	DiscreteGroupElement singleton[], full[];
	DiscreteGroupSceneGraphRepresentation spaceRep, sixcellRep;
	
	SceneGraphComponent sixcellsgc = SceneGraphUtility.createFullSceneGraphComponent("six cell");
	
	int an = 1, bn = 1, cn = 5;
	int groups[][] = {{1,1,1},{an,bn,cn},{2,2,1}};
	
	boolean showHalfTurn = true,
			use4gens = false;
	
	AnimatedIsometry ai;
	
	QuartzCrystal owner;
	QuartzGeometry geom;
	QuartzGroup(QuartzCrystal o) {
		owner = o;
		geom = owner.quartzGeom;
//		init();
	}
	
	
	public void init() {
//		gens4 = new DiscreteGroupElement[8];
//		Matrix[] gensM = geom.getTransGens();
//		gens4[0] = new DiscreteGroupElement(Pn.EUCLIDEAN, gensM[0].getArray(), "a");
//		gens4[1] = gens4[0].getInverse();
//		gens4[2] = new DiscreteGroupElement(Pn.EUCLIDEAN, gensM[1].getArray(), "b");
//		gens4[3] = gens4[2].getInverse();
//		double[] bA = Rn.times(null, gensM[1].getArray(), gens4[1].getArray());
//		gens4[4] = new DiscreteGroupElement(Pn.EUCLIDEAN, bA, "d");
//		gens4[5] = gens4[4].getInverse();
//		gens4[6] = new DiscreteGroupElement(Pn.EUCLIDEAN, gensM[2].getArray(), "c");
//		gens4[7] = gens4[6].getInverse();
//		spaceGroup4gens.setGenerators(gens4);
//		spaceGroup4gens.setDimension(3);
//		spaceGroup4gens.setMetric(Pn.EUCLIDEAN);
//		spaceGroup4gens.setFinite(false);
//		spaceGroup4gens.setName("alpha quartz group with 4 generators");
		
//		gens33 = new DiscreteGroupElement[4];
//		gensM = geom.getScrew33();
//		gens33[0] = new DiscreteGroupElement(Pn.EUCLIDEAN, gensM[0].getArray(), "s");
//		gens33[1] = gens33[0].getInverse();
//		gens33[2] = new DiscreteGroupElement(Pn.EUCLIDEAN, gensM[1].getArray(), "t");
//		gens33[3] = gens33[2].getInverse();
//		spaceGroup33.setGenerators(gens33);
//		spaceGroup33.setDimension(3);
//		spaceGroup33.setMetric(Pn.EUCLIDEAN);
//		spaceGroup33.setFinite(false);
//		spaceGroup33.setName("alpha quartz group with two order-3 generators");
		
		triTransGens = new DiscreteGroupElement[3];
		triTransGens[0] = new DiscreteGroupElement(Pn.EUCLIDEAN, triTrans3M[0].getArray(), "a");
//		gens[1] = gens[0].getInverse();
		triTransGens[1] = new DiscreteGroupElement(Pn.EUCLIDEAN, triTrans3M[1].getArray(), "b");
//		gens[3] = gens[2].getInverse();
		triTransGens[2] = new DiscreteGroupElement(Pn.EUCLIDEAN, triTrans3M[2].getArray(), "c");
//		gens[5] = gens[4].getInverse();
		triTrans3Group.setGenerators(triTransGens);
		triTrans3Group.setDimension(3);
		triTrans3Group.setMetric(Pn.EUCLIDEAN);
		triTrans3Group.setFinite(false);
		triTrans3Group.setName("alpha quartz group");
		triTrans3Group.setConstraint(new DiscreteGroupSimpleConstraint(1,1,1));
		triTrans3Group.update();
		singleton = triTrans3Group.getElementList();

		triTrans3Group.setConstraint(new DiscreteGroupTranslationConstraint(an, bn, cn, "abc"));
		triTrans3Group.update();
		full = triTrans3Group.getElementList();

		spaceRep = new DiscreteGroupSceneGraphRepresentation(triTrans3Group);
		
//		DirichletDomain dd = new DirichletDomain(spaceGroup);
//		dd.update();
//		dgsgr = new DiscreteGroupSceneGraphRepresentation(spaceGroup);
//		ddsgc.setGeometry(dd.getDirichletDomain());
//		Appearance ap  = ddsgc.getAppearance();
//		ap.setAttribute(CommonAttributes.FACE_DRAW, false);
//		ap.setAttribute("lineShader.diffuseColor", Color.white);

		twoGroup.setFinite(true);
		twoGroup.setDimension(3);
		twoGroup.setMetric(Pn.EUCLIDEAN);
		twoGroup.setName("Two group");
		id = new DiscreteGroupElement(Pn.EUCLIDEAN, Rn.identityMatrix(4), "");
		id.setColorIndex(0);
		Matrix m = MatrixBuilder.euclidean().rotate(yAxisPts[0], yAxisPts[1], Math.PI).getMatrix();
		gen = new DiscreteGroupElement(Pn.EUCLIDEAN, m.getArray(), "r");
		gen.setColorIndex(1);
		twoGroup.setElementList(new DiscreteGroupElement[] {id,gen});
		twoGroup.update();
		sixcellRep = new DiscreteGroupSceneGraphRepresentation(twoGroup);
		
		Appearance ap2list[] = {new Appearance(), new Appearance()};
		ap2list[0].setAttribute("lineShader.diffuseColor", new Color(255,255,150));
		ap2list[1].setAttribute("lineShader.diffuseColor", new Color(200,255,200));
//		ap2list[0].setAttribute("polygonShader.diffuseColor", Color.red);
//		ap2list[1].setAttribute("polygonShader.diffuseColor", Color.cyan);
		sixcellRep.setAppList(ap2list);
		sixcellRep.update();
	

	}

	public void updateC(double cc) {
		c = cc;

		for (int i = 0; i<3; ++i) {
			triChannelM[i] = MatrixBuilder.euclidean().
					rotate(axis3Pts[0], axis3Pts[1], -2*(i/3.0)*Math.PI).
					translate(0,0,i*c/3.0).
					getMatrix();
		}

//		Matrix m = MatrixBuilder.euclidean().rotate(yAxisPts[0], yAxisPts[1], Math.PI).getMatrix();
//		gen.setArray(m.getArray());
//		twoGroup.setElementList(new DiscreteGroupElement[] {id, gen});
//		twoGroup.update();
//		sixcellRep.setElementList(twoGroup.getElementList());
//		sixcellRep.update();

		zTranslate = MatrixBuilder.euclidean().translate(0,0,c).getMatrix();
		triTransGens[2] = new DiscreteGroupElement(Pn.EUCLIDEAN, zTranslate.getArray(), "c");
		triTrans3Group.update();
		
//		gens4[6] = new DiscreteGroupElement(Pn.EUCLIDEAN, m.getArray(), "c");
//		gens4[7] = gens4[6].getInverse();
//		spaceGroup4gens.update();		
		spaceRep.setElementList(null);
		spaceRep.update();
		
	}
	
	public void toggleHalfTurn()	{
		showHalfTurn = !showHalfTurn;
//		Transformation tt = new Transformation(showHalfTurn ? gen.getArray() : id.getArray());
//		sixcellRep.getSceneGraphRepn().getChildComponent(1).setTransformation(tt);
		sixcellRep.getSceneGraphRepn().getChildComponent(1).setVisible(showHalfTurn);
	}


	public Matrix[] getTriChannelM() {
		return triChannelM;
	}



	Timer halfTurnTimer = null;
	ActionListener al = null;
	public void runAI(boolean run) {
		
		if (!run) {
			if (ai == null) return;
			ai.reset();
			return;
		}
		SceneGraphComponent sgc = sixcellRep.getSceneGraphRepn().getChildComponent(1);
		ai = new AnimatedIsometry(gen.getArray(), Pn.EUCLIDEAN, sgc);
		if (halfTurnTimer != null) {
			halfTurnTimer.stop();
			halfTurnTimer.removeActionListener(al);
		}
		al = new ActionListener() {
			double t = 0;
			@Override
			public void actionPerformed(ActionEvent e) {
				t += .003;
				if (t >= 1.0) {
					t = 1.0;
					halfTurnTimer.stop();
					ai.endAnimation();
				}
				ai.setValueAtTime(t);
			}
			
		};
		halfTurnTimer = new Timer(10, al);
		ai.startAnimation();
		System.err.println("ai based on "+sgc.getName());
		halfTurnTimer.start();
	}
	
	boolean isRunning = false;
	

	public void updateDims()	{
		triTrans3Group.setConstraint(new DiscreteGroupTranslationConstraint(an, bn, cn, "abc"));
//		spaceGroup.update();
//		full = spaceGroup.getElementList();
//		System.err.println("Group has # elements "+full.length);
		spaceRep.setElementList(null);
		spaceRep.update();
		System.gc();
	}
	
	public void setSingle(boolean single) {
		if (single) triTrans3Group.setConstraint(new DiscreteGroupSimpleConstraint(1,1,1));
		else updateDims();
		spaceRep.setElementList(null);
		spaceRep.update();
	}
	public DiscreteGroupSceneGraphRepresentation getSpaceRep() {
		return spaceRep;
	}
	public DiscreteGroupSceneGraphRepresentation getSixcellRep() {
		return sixcellRep;
	}

	public DiscreteGroup getSpaceGroup(boolean doTessCont) {
		return (use4gens || doTessCont) ? spaceGroup4gens : triTrans3Group;
	}
	public DiscreteGroup getTwoGroup() {
		return twoGroup;
	}
	public Component getInspector() {
		Box container = Box.createVerticalBox();
		final TextSlider<Integer> aSlider = new TextSlider.Integer("p",  SwingConstants.HORIZONTAL, 1, 10, an);
		aSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				an = aSlider.getValue().intValue();
				updateDims();	
			}
		});
		container.add(aSlider);
		final TextSlider<Integer> bSlider = new TextSlider.Integer("p",  SwingConstants.HORIZONTAL, 1, 10, bn);
		bSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				bn = bSlider.getValue().intValue();
				updateDims();		
			}
		});
		container.add(bSlider);
		final TextSlider<Integer> cSlider = new TextSlider.Integer("p",  SwingConstants.HORIZONTAL, 1, 10, cn);
		cSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				cn = cSlider.getValue().intValue();
				updateDims();		
			}
		});
		container.add(cSlider);
		return container;
	}


}
