/*
 * Created on 13 Apr 2023
 *
 */
package discreteGroup.quartz;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.util.TextSlider;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupTranslationConstraint;

public class QuartzGroup {

	DiscreteGroup spaceGroup = new DiscreteGroup(),
			spaceGroup2 = new DiscreteGroup();
	DiscreteGroup twoGroup = new DiscreteGroup();
	DiscreteGroupElement id, gen;
	DiscreteGroupElement gens[];
	DiscreteGroupElement singleTon[], full[];
	DiscreteGroupSceneGraphRepresentation spaceRep, sixcellRep;

	int an = 1, bn = 1, cn = 10;
	int groups[][] = {{1,1,1},{an,bn,cn},{2,2,1}};
	
	double linePts[][] = {{0,0,0,1}, {0,1,0,1}};

	boolean showHalfTurn = true;
	
	QuartzCrystal owner;
	QuartzGeometry geom;
	QuartzGroup(QuartzCrystal o, QuartzGeometry g) {
		owner = o;
		geom = g;
		init();
	}
	public void init() {
		gens = new DiscreteGroupElement[8];
		Matrix[] gensM = geom.getTransGens();
		gens[0] = new DiscreteGroupElement(Pn.EUCLIDEAN, gensM[0].getArray(), "a");
		gens[1] = gens[0].getInverse();
		gens[2] = new DiscreteGroupElement(Pn.EUCLIDEAN, gensM[1].getArray(), "b");
		gens[3] = gens[2].getInverse();
		double[] bA = Rn.times(null, gensM[1].getArray(), gens[1].getArray());
		gens[4] = new DiscreteGroupElement(Pn.EUCLIDEAN, bA, "bA");
		gens[5] = gens[4].getInverse();
		gens[6] = new DiscreteGroupElement(Pn.EUCLIDEAN, gensM[2].getArray(), "c");
		gens[7] = gens[6].getInverse();
		spaceGroup2.setGenerators(gens);
		spaceGroup2.setDimension(3);
		spaceGroup2.setMetric(Pn.EUCLIDEAN);
		spaceGroup2.setFinite(false);
		spaceGroup2.setName("alpha quartz group for tess content");

		gens = new DiscreteGroupElement[3];
		gens[0] = new DiscreteGroupElement(Pn.EUCLIDEAN, gensM[0].getArray(), "a");
//		gens[1] = gens[0].getInverse();
		gens[1] = new DiscreteGroupElement(Pn.EUCLIDEAN, gensM[1].getArray(), "b");
//		gens[3] = gens[2].getInverse();
		gens[2] = new DiscreteGroupElement(Pn.EUCLIDEAN, gensM[2].getArray(), "c");
//		gens[5] = gens[4].getInverse();
		spaceGroup.setGenerators(gens);
		spaceGroup.setDimension(3);
		spaceGroup.setMetric(Pn.EUCLIDEAN);
		spaceGroup.setFinite(false);
		spaceGroup.setName("alpha quartz group");
		spaceGroup.setConstraint(new DiscreteGroupSimpleConstraint(1,1,1));
		spaceGroup.update();
		singleTon = spaceGroup.getElementList();

		spaceGroup.setConstraint(new DiscreteGroupTranslationConstraint(an, bn, cn, "abc"));
		spaceGroup.update();
		full = spaceGroup.getElementList();

		spaceRep = new DiscreteGroupSceneGraphRepresentation(spaceGroup);
		
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
		Matrix m = MatrixBuilder.euclidean().rotate(linePts[0], linePts[1], Math.PI).getMatrix();
		gen = new DiscreteGroupElement(Pn.EUCLIDEAN, m.getArray(), "r");
		gen.setColorIndex(1);
		twoGroup.setElementList(new DiscreteGroupElement[] {id,gen});
		twoGroup.update();
		sixcellRep = new DiscreteGroupSceneGraphRepresentation(twoGroup);

		Appearance ap2list[] = {new Appearance(), new Appearance()};
//		ap2list[0].setAttribute("lineShader.diffuseColor", Color.yellow);
//		ap2list[1].setAttribute("lineShader.diffuseColor", Color.green);
//		ap2list[0].setAttribute("polygonShader.diffuseColor", Color.red);
//		ap2list[1].setAttribute("polygonShader.diffuseColor", Color.cyan);
		sixcellRep.setAppList(ap2list);

	}

	public void updateC(double c) {
		double axis[][] = {{0,0,0,1}, {0,1,0,1}};
		Matrix m = MatrixBuilder.euclidean().translate(0,0,c).rotate(axis[0], axis[1], Math.PI).getMatrix();
		gen.setArray(m.getArray());
		twoGroup.setElementList(new DiscreteGroupElement[] {id, gen});
		twoGroup.update();
		sixcellRep.setElementList(twoGroup.getElementList());
		sixcellRep.update();

		m = MatrixBuilder.euclidean().translate(0,0,c).getMatrix();
		gens[2] = new DiscreteGroupElement(Pn.EUCLIDEAN, m.getArray(), "c");
//		gens[5] = gens[4].getInverse();
		spaceGroup.update();
		spaceRep.setElementList(null);
		spaceRep.update();
		
	}
	
	public void toggleHalfTurn()	{
		showHalfTurn = !showHalfTurn;
		if (showHalfTurn) twoGroup.setElementList(new DiscreteGroupElement[] {id, gen});
		else  twoGroup.setElementList(new DiscreteGroupElement[] {id});
		twoGroup.update();
		sixcellRep.setElementList(twoGroup.getElementList());
		sixcellRep.update();

	}
	public void updateDims()	{
		spaceGroup.setConstraint(new DiscreteGroupTranslationConstraint(an, bn, cn, "abc"));
//		spaceGroup.update();
//		full = spaceGroup.getElementList();
//		System.err.println("Group has # elements "+full.length);
		spaceRep.setElementList(null);
		spaceRep.update();
		System.gc();
	}
	
	public void setSingle(boolean single) {
		if (single) spaceGroup.setConstraint(new DiscreteGroupSimpleConstraint(1,1,1));
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
		return doTessCont ? spaceGroup2 : spaceGroup;
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
