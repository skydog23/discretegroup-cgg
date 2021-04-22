/*
 * Author	gunn
 * Created on Mar 15, 2006
 *
 */
package discreteGroup.puncturedTorus;

import junit.framework.TestCase;
import charlesgunn.math.Cn;
import charlesgunn.math.Complex;
import charlesgunn.math.PSL2C;

public class PuncturedTorusTest extends TestCase {

	public void testMaskitBoundary()	{
		int mix = 4;
		Complex tB = new Complex(2.1,0);
		int[][] fs = PuncturedTorusUtility.fareySequence(mix);
		Complex[] cusps = PuncturedTorusUtility.cusps(mix, tB);
		int n = cusps.length;
		for (int i = 0; i<n; ++i)	{
//			System.err.println(fs[i][0]+"/"+fs[i][1]+":"+PuncturedTorusUtility.wordForFraction(fs[i][0],fs[i][1], "a", "B"));	
			PSL2C[] gg = PuncturedTorusUtility.getGrandmaGenerators(cusps[i], tB);
			PSL2C el = PSL2C.elementForWord(PuncturedTorusUtility.wordForFraction(fs[i][0],fs[i][1], "a", "B"), gg[0], gg[3]);
			System.err.println("Trace of a is "+cusps[i]+" \tTrace of element is "+Cn.trace(el.m));
		}

	}
}
