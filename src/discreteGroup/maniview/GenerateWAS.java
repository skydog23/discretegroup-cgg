package discreteGroup.maniview;

import junit.framework.TestCase;
import de.jreality.math.Rn;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.groups.BorromeanUtility;

public class GenerateWAS extends TestCase {

//	public void testFSA1()	{
//		System.err.println("test1");
//		DiscreteGroup dg = Platycosm.instanceOfGroup("c3");
//		DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(100);
//		String[] dups = DiscreteGroupUtility.getDuplicates(dg, dgsc);
//		for (String s : dups) System.err.println(s);
//	}
//	
	
	public void testInfBorromeanFSA()	{
		System.err.println("testInfBorrom");
		DiscreteGroup dg = BorromeanUtility.borromeanGroupOfOrder(-1);
		dg.getFsa().debugPrint();	
		
	}

	public void testBorromean2()	{
		System.err.println("testBorromean2");
		DiscreteGroup dg = BorromeanUtility.borromeanGroupOfOrder(2);
		dg.getFsa().debugPrint();	
		for (int j = 0; j<dg.getGenerators().length; ++j)	{
			System.err.println("generator "+j+"=");
			System.err.println(Rn.matrixToString(dg.getGenerators()[j].getArray(), "%8.4f"));
		}
		
	}

	public void testBorromeanFSA()	{
		System.err.println("test2");
		for (int i = 2; i<10; ++i)	{
			System.err.println("order="+i);
			DiscreteGroup dg = BorromeanUtility.borromeanGroupOfOrder(i);
//			dg.getFsa().debugPrint();	
//			for (int j = 0; j<dg.getGenerators().length; ++j)	{
//				System.err.println("generator "+j+" + "+Rn.matrixToString(dg.getGenerators()[j].getMatrix(), "%8.4f"));
//			}
			DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(1000);
			String[] dups = DiscreteGroupUtility.getDuplicates(dg, dgsc);
			for (String s : dups) System.err.println(s);
		}
	}
}
