/*
 * Created on Feb 17, 2010
 *
 */
package discreteGroup.test;

import junit.framework.TestCase;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.groups.CrystallographicGroup;
import de.jtem.discretegroup.util.WingedEdge;


public class TestDGU extends TestCase{

	public void testCanonRep()	{
		CrystallographicGroup wg = CrystallographicGroup.instanceOfGroup(16);
		wg.setConstraint(new DiscreteGroupSimpleConstraint(100));
		wg.update();
		DirichletDomain dd = new DirichletDomain(wg);
		dd.update();
		WingedEdge we = (WingedEdge) dd.getDirichletDomain();
		DiscreteGroupElement dge = new DiscreteGroupElement();
		double[] tpoint = DiscreteGroupUtility.getCanonicalRepresentative(
				we, dge, new double[]{80,0,0,1}, .001);
		System.err.println("Tested "+dge.getWord());
	}
}
