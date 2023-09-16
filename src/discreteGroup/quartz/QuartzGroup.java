/*
 * Created on 13 Apr 2023
 *
 */
package discreteGroup.quartz;

import static discreteGroup.quartz.QuartzConstants.axis3Pts;
import static discreteGroup.quartz.QuartzConstants.chan31Color;
import static discreteGroup.quartz.QuartzConstants.chan32Color;
import static discreteGroup.quartz.QuartzConstants.hex3Pts;
import static discreteGroup.quartz.QuartzConstants.hexTrans;
import static discreteGroup.quartz.QuartzConstants.yAxisPts;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

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
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jtem.discretegroup.ResourceClass;
import de.jtem.discretegroup.core.AbstractDGSGR;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupConstraint;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.FiniteStateAutomaton;
import de.jtem.discretegroup.core.SelectionDGSGR;
import de.jtem.discretegroup.core.SimpleDGSGR;

public class QuartzGroup {

	Matrix[]  L2M = new Matrix[3],
			L1M = new Matrix[3], 
			L0M = new Matrix[2];
	//Matrix[][] gens = {L0Gens,L1Gens,L2Gens, L3Gens};
	Matrix zTranslate = new Matrix();
	
	double c = 1.25;

	DiscreteGroup L2G = new DiscreteGroup(),
			L3G = new DiscreteGroup(),
			L23G = new DiscreteGroup();
	DiscreteGroupElement[] L0Gens = new DiscreteGroupElement[2],
			L1Gens = new DiscreteGroupElement[3],
			L2Gens = new DiscreteGroupElement[6],
			L3Gens = new DiscreteGroupElement[2],
			L23Gens = new DiscreteGroupElement[6],
			L3Gens1G = new DiscreteGroupElement[1],
			L23Huge, L2Big, L23Small;
	DiscreteGroupElement id, gen;
	AbstractDGSGR  L0SGR, L1SGR, L3SGR, L2OnlySGR;
	DiscreteGroupSceneGraphRepresentation L23SGR;
	SelectionDGSGR L2SGR;
	AbstractDGSGR[] sgrList = new AbstractDGSGR[5];
	DiscreteGroupSimpleConstraint smallC, bigC, hugeC, pruneCL2, pruneCL3;
	ZConstraint  groupCL3;
	
	int maxL = 1, numEl = 7, l23cutoff = 200;
	double maxD = -1;
	
	boolean showHalfTurn = true,
			startBig = true,
			zUpOnly = true,
			useHugeGroup = false,
			useXYZGroup = false;
	
	AnimatedIsometry ai[] = new AnimatedIsometry[3];
	
	QuartzCrystal owner;
	QuartzGeometry geom;
	QuartzGroup(QuartzCrystal o) {
		owner = o;
		geom = owner.quartzGeom;
//		init();
	}
	
	boolean inited = false;
	public void init() {
		if (inited) return;
		inited = true;
		L0Gens[0] = new DiscreteGroupElement(Pn.EUCLIDEAN, Rn.identityMatrix(4), "");
		L0Gens[0].setColorIndex(0);
		Matrix m = getAxis2M();
		L0Gens[1] = new DiscreteGroupElement(Pn.EUCLIDEAN, m.getArray(), "r");
		L0Gens[1].setColorIndex(1);
		L0SGR = new SimpleDGSGR();
		L0SGR.getRepresentationRoot().setName("Level 0");
		L0SGR.setElementList(L0Gens);
		
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

		// translations in x-y plane
		L2OnlySGR = new SimpleDGSGR();
		L2OnlySGR.getRepresentationRoot().setName("Level 2");

		// split the translation group into two pieces, the x-y plane and the z-direction
		String[] enames = {"a","b","c","d"};
		for (int i = 0; i<3; ++i) {
			L2M[i] = MatrixBuilder.euclidean().
					translate(QuartzConstants.hexTrans[i]).
					getMatrix();
			L2Gens[i] = new DiscreteGroupElement(Pn.EUCLIDEAN, L2M[i].getArray(), enames[i]);
			L2Gens[i+3] = L2Gens[i].getInverse();
		}
		L2G.setGenerators(L2Gens);
		L2G.setDimension(3);
		L2G.setMetric(Pn.EUCLIDEAN);
		L2G.setFinite(false);
		L3G.setCenterPoint(new double[] {1/3.0,0,0,1});
		L2G.setName("x-y alpha quartz group");

//		bigC = new DiscreteGroupSimpleConstraint(7, 7, 1000);
		bigC = new DiscreteGroupSimpleConstraint(7,7,500);
		bigC.setManhattan(true);
						
		L2G.setConstraint(bigC);
		L2G.update();
		L2Big = L2G.getElementList();
		System.err.println("big xy group # = "+L2G.getElementList().length);

		L2OnlySGR = new SimpleDGSGR(); //DiscreteGroupSceneGraphRepresentation(L2G);
		L2OnlySGR.setElementList(L2Big);
		L2OnlySGR.getRepresentationRoot().setName("Level 2");
		pruneCL2 = new DiscreteGroupSimpleConstraint(maxD, maxL, numEl);
		L2OnlySGR.setConstraint(pruneCL2);
		L2OnlySGR.update();
		System.err.println("xy DGSGR # = "+L2OnlySGR.getElementList().length);

		// the highest level is the z-translation group
		double[] ztranslate = MatrixBuilder.euclidean().translate(0,0, c).getArray();
		L3Gens[0] = new DiscreteGroupElement(Pn.EUCLIDEAN, ztranslate, "d");
		L3Gens[1] = L3Gens[0].getInverse();
		L3G.setGenerators( L3Gens);
		L3G.setDimension(3);
		L3G.setMetric(Pn.EUCLIDEAN);
		L3G.setFinite(false);
		L3G.setName("z alpha quartz group");
				
		groupCL3 = new ZConstraint(10,20, 20);
		groupCL3.setUpOnly(true);
		L3G.setConstraint(groupCL3);
		L3G.update();
		DiscreteGroupElement[] els = L3G.getElementList();
		System.err.println("full z group # = "+els.length);
//		for (int i = 0; i<els.length; ++i) {
//			System.err.println("dge word = "+els[i].getWord());
//		}
		
		L3SGR = new SimpleDGSGR(L3G);
		L3SGR.getRepresentationRoot().setName("Level 3");
		L3SGR.update();
		System.err.println("z DGSGR # = "+L3SGR.getElementList().length);
		pruneCL3 = new DiscreteGroupSimpleConstraint(10,20, 1);
		L3SGR.setConstraint(pruneCL3);

		L23Gens[0] = L2Gens[0];
		L23Gens[1] = L2Gens[1];
		L23Gens[3] = L2Gens[3];
		L23Gens[4] = L2Gens[4];
		L23Gens[2] = new DiscreteGroupElement(Pn.EUCLIDEAN, ztranslate, "c");
		L23Gens[5] = L23Gens[2].getInverse();
		L23G.setGenerators( L23Gens);
		L23G.setDimension(3);
		L23G.setMetric(Pn.EUCLIDEAN);
		L23G.setFinite(false);
		FiniteStateAutomaton fsa = FiniteStateAutomaton.fsaForName("c1.wa", ResourceClass.class);
		L23G.setFsa(fsa);

		L23G.setName("xyz alpha quartz group");


		smallC = new DiscreteGroupSimpleConstraint(-1,-1,50);
		L23G.setConstraint(smallC);
		L23G.update();
		L23Small = L2G.getElementList();
		
		System.err.println("small xy group # = "+L2G.getElementList().length);
		hugeC = new DiscreteGroupSimpleConstraint(16,-1,15000);
		L23G.setConstraint(hugeC);
		L23G.update();
		System.err.println("xyz DG # = "+L23G.getElementList().length);

		L23SGR = new DiscreteGroupSceneGraphRepresentation(L23G, true);
		L23SGR.getRepresentationRoot().setName("Level 23");
		L23SGR.setOfficialElementList(L23Small);
		L23SGR.getDropBox().setCutoff(l23cutoff);
		L23SGR.setElementList(L23G.getElementList());
		
		L23SGR.update();
		L23SGR.setConstraint(pruneCL2);
		System.err.println("xyz DGSGR # = "+L23SGR.getElementList().length);

		L2SGR = new SelectionDGSGR();
		L2SGR.addSGR(L2OnlySGR);
		L2SGR.addSGR(L23SGR);
		L2SGR.setSelected(0);
		sgrList = new AbstractDGSGR[] {L0SGR, L1SGR, L2SGR, L3SGR, L23SGR};
		// start with everything reduced
		setSingle(0, true);
		setSingle(1, true);
		setSingle(2, true);
		setSingle(3, true);
		
		anims[0] = new QuartzAnimation(L0SGR.getSceneGraphRepn(), duration, delay, 1);
		anims[1] = new QuartzAnimation(L1SGR.getSceneGraphRepn(), duration, delay, 2);
		anims[2] = new QuartzAnimation(L2SGR.getSceneGraphRepn(), duration, delay, 6);
		anims[3] = new QuartzAnimation(L3SGR.getSceneGraphRepn(), duration, delay, 3);
		
		System.err.println("finished group init");
	}

	public Matrix getAxis2M() {
		double axis[][] = {{0,0,c/2,1}, {0,1,0,0}};
		return MatrixBuilder.euclidean().rotate(axis[0], axis[1], Math.PI).getMatrix();
	}
	public void updateC(double cc) {
		c = cc;
		Matrix m = getAxis2M();
		L0Gens[1] = new DiscreteGroupElement(Pn.EUCLIDEAN, m.getArray(), "r");
		L0SGR.setElementList(L0Gens);
		L0SGR.update();
		
		for (int i = 0; i<3; ++i) {
			L1Gens[i].setArray(MatrixBuilder.euclidean().
					rotate(axis3Pts[0], axis3Pts[1], -2*(i/3.0)*Math.PI).
					translate(0,0,i*c/3.0).
					getMatrix().getArray());
		}
		L1SGR.setElementList(L1Gens);
		L1SGR.update();
		
		
		double[] ztranslate = MatrixBuilder.euclidean().translate(0,0, c).getArray();
		L3Gens[0] = new DiscreteGroupElement(Pn.EUCLIDEAN, ztranslate, "d");
		L3Gens[1] = L3Gens[0].getInverse();
		L3G.setGenerators(L3Gens);
		L3G.update();
		
		L3SGR.setElementList(L3G.getElementList());
		L3SGR.setConstraint(pruneCL3);
		L3SGR.update();
		
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
	public void setSingle(int j, boolean b) {
		if (j >= 4) return;
		AbstractDGSGR sgr = sgrList[j]; 
		singleState[j] = b;
		
		if (b) {
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
	
	QuartzAnimation[] anims = new QuartzAnimation[4];
	double duration = 2.0, delay = .5;
	
	boolean[] singleState = {true, true, false, true, false};
	DiscreteGroupConstraint oldC[] = new DiscreteGroupConstraint[4];
	
	String[] animNames = {"2-fold","3-fold tri", "x-y translate", "z-translate"};
	boolean[] animState = new boolean[anims.length];
	TextSlider<Integer> dSlider;
	JCheckBox[] ssCB = new JCheckBox[4];
	Box inspector = null;
	Component xyzInsp = null;
	Box xyGroupInsp = null, zGroupInsp = null;

	public Component getInspector() {
		init();
		if (inspector == null) {
			inspector = Box.createVerticalBox();	
		} else return inspector;

		Box buttons = Box.createHorizontalBox();
		buttons.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				BorderFactory.createTitledBorder(BorderFactory
						.createEtchedBorder(), "Animations")));
		Box container = Box.createVerticalBox();
		inspector.add(container);
		container.add(buttons);
		for (int i = 0; i < anims.length; ++i) {
			final int j = i;
			JButton jb = new JButton(animNames[i]);
			jb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					animState[j] = !animState[j];
					if (animState[j]) {
//							anims[j] = new QuartzAnimation(L0SGR.getSceneGraphRepn(), duration, delay);
						anims[j].start();
					} else
						anims[j].stop();
				}
			});
			buttons.add(jb);
		}

		buttons = Box.createHorizontalBox();
		container.add(buttons);
		buttons.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				BorderFactory.createTitledBorder(BorderFactory
						.createEtchedBorder(), "Show single copy")));


		for (int i = 0; i<4; ++i)	{
			final int j = i;
			final JCheckBox jb = new JCheckBox("Level"+j);
			jb.setSelected(singleState[j]);
			ssCB[i] = jb;
			jb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setSingle(j, jb.isSelected());			
				}
			});
			buttons.add(jb);
		}
		
		xyGroupInsp = Box.createVerticalBox();
		xyGroupInsp.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				BorderFactory.createTitledBorder(BorderFactory
						.createEtchedBorder(), "x-y group constraints")));
		container.add(xyGroupInsp);
		final TextSlider<Double> aSlider = new TextSlider.Double("max dist",  SwingConstants.HORIZONTAL,-1,10, maxD);
		aSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				maxD = aSlider.getValue().doubleValue();
				pruneCL2.setMaxDistance(maxD);	
				L2SGR.setConstraint(pruneCL2);
			}
		});
		xyGroupInsp.add(aSlider);
		final TextSlider<Integer> bSlider = new TextSlider.Integer("max word",  SwingConstants.HORIZONTAL, 1, 10, maxL);
		bSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				maxL = bSlider.getValue().intValue();
				pruneCL2.setMaxWordLength(maxL);	
				L2SGR.setConstraint(pruneCL2);
			}
		});
		xyGroupInsp.add(bSlider);
		final TextSlider cSlider = new TextSlider.Integer("num Els",  SwingConstants.HORIZONTAL, 1, 50, numEl);
		cSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				numEl = cSlider.getValue().intValue();
				pruneCL2.setMaxNumberElements(numEl);
				L2SGR.setConstraint(pruneCL2);
				System.err.println("L2SGR # = "+L2SGR.getElementList().length);
			}
		});
		xyGroupInsp.add(cSlider);
		zGroupInsp = Box.createHorizontalBox();
		container.add(zGroupInsp);
		dSlider = new TextSlider.Integer("z-copies",  SwingConstants.HORIZONTAL, 1, 20, 1);
		dSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int foo = dSlider.getValue().intValue();
				pruneCL3.setMaxNumberElements(foo);
				L3SGR.setConstraint(pruneCL3);
				L3SGR.update();
				System.err.println("z-count ="+foo+" gp el # = "+L3G.getElementList().length);
				System.err.println(" sgr el # = "+L3SGR.getElementList().length);
			}
		});
		zGroupInsp.add(dSlider);
		final JCheckBox jb = new JCheckBox("up only");
		jb.setSelected(zUpOnly);
		jb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				zUpOnly = jb.isSelected();
				groupCL3.setUpOnly(zUpOnly);
				L3G.update();
				L3SGR.setElementList(L3G.getElementList());
				L3SGR.update();
				System.err.println("L3SGR # = "+L3SGR.getElementList().length);
			}
		});
		zGroupInsp.add(jb);

		
		Box hbox = Box.createHorizontalBox();
		container.add(hbox);
		final JCheckBox tgb = new JCheckBox("use xyz group");
		tgb.setSelected(useXYZGroup);
		tgb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				useXYZGroup = tgb.isSelected();
				updateLevel2();
			}
		});
		hbox.add(tgb);

		inspector.add(L23SGR.getInspector());
		L23SGR.getInspector().setVisible(useXYZGroup);
		xyGroupInsp.setVisible(!useXYZGroup);
		zGroupInsp.setVisible(!useXYZGroup);
		return inspector;
	}

	private void updateLevel2() {
		L2SGR.setSelected( useXYZGroup ? 1 : 0);
		L23SGR.getInspector().setVisible(useXYZGroup);
		xyGroupInsp.setVisible(!useXYZGroup);
		zGroupInsp.setVisible(!useXYZGroup);
		if (useXYZGroup) {
			L23SGR.setConstraint(pruneCL2);
			L2OnlySGR.setConstraint(trivialC);
			ssCB[2].setSelected(true);
			singleState[2] = true;
		}
		else {
			L2OnlySGR.setConstraint(pruneCL2);
			L23SGR.setConstraint(pruneCL3);
		}
	}

	private class ZConstraint extends DiscreteGroupSimpleConstraint {
		
		ZConstraint(double d, int mw, int mn) {
			super(d,mw,mn);
		}
		boolean upOnly = false;
		void setUpOnly(boolean b) {
			upOnly = b;
		}
		@Override
		public boolean acceptElement(DiscreteGroupElement dge) {
//			System.err.println("accept "+dge.getWord());
			boolean accept =  super.acceptElement(dge);
			if (!upOnly) return accept;
			if (!accept) return false;
			boolean down = (dge.getWord().contains("D"));
//			System.err.println(dge.getWord()+" contains D "+down);
			return !down;
		}
		
		

	}

}
