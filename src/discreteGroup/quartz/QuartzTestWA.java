/*
 * Created on 7 May 2023
 *
 */
package discreteGroup.quartz;

import charlesgunn.jreality.viewer.Assignment;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.ResourceClass;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.FiniteStateAutomaton;
import de.jtem.discretegroup.groups.Platycosm;

public class QuartzTestWA extends Assignment {

	@Override
	public SceneGraphComponent getContent() {
		DiscreteGroup dg = Platycosm.instanceOfGroup("c1");
		FiniteStateAutomaton fsa = FiniteStateAutomaton.fsaForName("c1.wa", ResourceClass.class);
		dg.setFsa(fsa);
		DiscreteGroupSimpleConstraint sc = new DiscreteGroupSimpleConstraint(15, -1, 15000);
		dg.setConstraint(sc);
		dg.update();
		System.err.println("elem list # = "+dg.getElementList().length);
		DirichletDomain dd = new DirichletDomain(dg);
		dd.setDirichletDomainOrbit(30);
		dd.update();
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		world.setGeometry(dd.getDirichletDomain());
		Appearance ap = world.getAppearance();
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		DiscreteGroupSceneGraphRepresentation dgsgr = new DiscreteGroupSceneGraphRepresentation(dg, true);
		dgsgr.setWorldNode(world);
		dgsgr.update();
		return dgsgr.getRepresentationRoot();
	}

	public static void main(String[] args) {
		new QuartzTestWA().display();
	}

}
