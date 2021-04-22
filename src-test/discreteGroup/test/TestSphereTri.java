/*
 * Created on Jul 16, 2012
 *
 */
package discreteGroup.test;

import junit.framework.TestCase;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.groups.TriangleGroup;


public class TestSphereTri extends TestCase {
	
	public void test1() {
		DiscreteGroup dg = TriangleGroup.instanceOfGroup("*233");
		dg = TriangleGroup.instanceOfGroup("*234");
		dg = TriangleGroup.instanceOfGroup("*235");
	}
}
