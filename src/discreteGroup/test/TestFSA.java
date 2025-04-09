package discreteGroup.test;

import junit.framework.TestCase;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.core.FiniteStateAutomaton;
import de.jtem.discretegroup.core.FiniteStateAutomatonUtility;
import de.jtem.discretegroup.spacegroups.GroupGeneratorFactory;


public class TestFSA extends TestCase {

	public void testFSA()	{
		DiscreteGroup dg = GroupGeneratorFactory.getD8Group("8.");
		for (DiscreteGroupElement dge : dg.getGenerators())	{
//			System.err.println("gen = "+dge.getWord()+"="+Rn.matrixToString(dge.getMatrix()));
			System.err.println("gen = "+dge.getWord()+"inverse  = "+dg.getGeneratorInverseWord(dge.getWord()));		
		}
		FiniteStateAutomaton fsa = FiniteStateAutomatonUtility.generateFiniteStateAutomatonForGroup(dg);
//		FiniteStateAutomaton fsa = FiniteStateAutomaton.fsaForName("/tmp/1..wa", DiscreteGroupUtility.class);
		dg.setFsa(fsa);
		String[] newDups = DiscreteGroupUtility.getDuplicates(dg, new DiscreteGroupSimpleConstraint(-1.0, 5));
		System.err.println("Found dups "+newDups.length);
		
	}
}
