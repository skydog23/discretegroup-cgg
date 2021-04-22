/*
 * Created on Feb 26, 2006
 *
 */
package discreteGroup.test;

import junit.framework.TestCase;
import charlesgunn.math.Complex;
import charlesgunn.math.ComplexFunction;
import discreteGroup.puncturedTorus.PuncturedTorusUtility;

public class TestPuncturedTorus extends TestCase {
	
	public void testTracePolynomial()	{
		ComplexFunction cf = PuncturedTorusUtility.traceEquation(1,2, new Complex(2,0));
		Complex root = Complex.newtonsMethod(cf, new Complex(0,1));
		System.err.println("Root is "+root);
		System.err.println("Value is "+cf.valueAt(root));
	}
	
	public void testFareySequence()	{
		int[][] farey = PuncturedTorusUtility.fareySequence(4);
		for (int i = 0; i<farey.length; ++i)	{
			System.err.print("i "+i+":"+farey[i][0]+":"+farey[i][1]);
		}
	}
	
	public void testCuspTraces()	{
		Complex[] traces = PuncturedTorusUtility.cusps(4, new Complex(2,0));
		int n = traces.length;
		for (int i = 0; i<n; ++i)	{
			System.err.println("i "+i+":"+traces[i].toString());
		}
	}
	
	public void testAllParabolics()	{
		int[][] fracs = PuncturedTorusUtility.fareySequence(6);
		int n = fracs.length;
		for (int j = 0; j<n; ++j)	{
			Complex[] roots = PuncturedTorusUtility.allParabolicsForFraction(fracs[j][0], fracs[j][1]);	
			System.err.println("Found "+roots.length+" roots for "+fracs[j][0]+"/"+fracs[j][1]);
			for (int i = 0; i<roots.length; ++i)	{
				System.err.println(roots[i].toString());
			}
		}

	}
}
