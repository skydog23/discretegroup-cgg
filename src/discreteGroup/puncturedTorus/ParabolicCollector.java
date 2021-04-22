/*
 * Author	gunn
 * Created on Mar 15, 2006
 *
 */
package discreteGroup.puncturedTorus;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import charlesgunn.math.PSL2C;

public class ParabolicCollector{
	int wordLength;
	double tolerance;
	boolean debug = true;
	PSL2C[] parabolics = null;
	Vector list = new Vector();
	PuncturedTorusGroupFactory ptgf = null;
	public ParabolicCollector(PuncturedTorusGroupFactory p, int wl, double t)	{
		super();
		ptgf = p;
		wordLength = wl;
		tolerance = t;
	}
	
	public void visit()	{
		list.clear();
		for (int i = 0; i<4; ++i) for (int j=0;j<2;++j) list.add(ptgf.commutators[i][j]);
		if (ptgf.lookForAlmostParabolics)	{
//			PSL2C aB = PSL2C.times(null, ptgf.genspsl2cs[0], ptgf.genspsl2cs[3]);
			PSL2C[] possibleEls = PuncturedTorusUtility.fareySequence(wordLength,  ptgf.genspsl2cs[0], ptgf.genspsl2cs[3]); //aB);
			int n = possibleEls.length;
			for (int i = 0; i<n; ++i)	{
				PSL2C g = possibleEls[i];
				//if (g.getWord().length() < 6) System.err.println("Word: "+g.getWord()+" Trace: "+Cn.trace(g.m));
				if ( PSL2C.isNearlyParabolic(g, tolerance))  {
					if (debug) System.err.println("nearly parabolic "+g.getWord());
					list.addAll(allPermutationsOf(g));
				}
			}			
		}
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				String s1 = ((PSL2C) o1).getWord();
				String s2 = ((PSL2C) o2).getWord();
				int l1 = s1.length();
				int l2 = s2.length();
				int matchLength = matchFromEnd(s1,s2);
				char lastMatched = ' ';
				if (matchLength > 0)	{
					lastMatched = s1.charAt(l1-matchLength);
				}
				char c1 = ' ';
				char c2 = ' ';
				if (matchLength < l1) c1 = s1.charAt(l1-matchLength-1);
				if (matchLength < l2) c2 = s2.charAt(l2-matchLength-1);
				if (c1 == c2) return 0;
				switch(lastMatched)	{
				case ' ':
					if (c1 == 'a') return -1;
					if (c2 == 'a') return 1;
					if (c1 == 'b') return -1;
					if (c2 == 'b') return 1;
					if (c1 == 'A') return -1;
					if (c2 == 'A') return 1;
					break;
				case 'a':
					if (c1 == 'B') return 1;
					if (c2 == 'B') return -1;
					if (c1 == 'b') return -1;
					if (c2 == 'b') return 1;
					break;
				case 'b':
					if (c1 == 'a') return 1;
					if (c2 == 'a') return -1;
					if (c1 == 'A') return -1;
					if (c2 == 'A') return 1;
					break;
				case 'A':
					if (c1 == 'b') return 1;
					if (c2 == 'b') return -1;
					if (c1 == 'B') return -1;
					if (c2 == 'B') return 1;
					break;
				case 'B':
					if (c1 == 'A') return 1;
					if (c2 == 'A') return -1;
					if (c1 == 'a') return -1;
					if (c2 == 'a') return 1;
					break;
				default:
					System.err.println("Invalid comparison"+s1+" "+s2);
				}
				return 0;
				
			}

			private int matchFromEnd(String s1, String s2) {
				int length = 0;
				int c1 = s1.length()-1;
				int c2 = s2.length()-1;
				while (s1.charAt(c1) == s2.charAt(c2))		{
					c1--;c2--;length++;
					if (c1 < 0 || c2 < 0) return length;
				}
				return length;
			}
			
		});
	}

	public PSL2C[] getParabolics()	{
		Iterator iter = list.iterator();
		if (debug)	while (iter.hasNext()) System.err.println(((PSL2C) iter.next()).getWord());
		parabolics = new PSL2C[list.size()];
		return (PSL2C[]) list.toArray(parabolics);
	}
	
	
	private List allPermutationsOf(PSL2C g) {
		int n = g.getWord().length();
		Vector v = new Vector(2 * n);
		String w = g.getWord();
		v.add(g);
		v.add(PSL2C.invert(null, g));
		PSL2C currentG = g;
		int which = 0;
		for (int i = 0; i<n-1; ++i)	{
			char m = w.charAt(i);
			if (m == 'a')	which = 2;
			else if (m == 'b') which = 3;
			else if (m == 'A') which = 0;
			else if (m == 'B') which = 1;
			currentG = PSL2C.conjugateBy(null, currentG, ptgf.genspsl2cs[which]);
			v.add(currentG);
			if (debug) System.err.println("parabolic word: "+currentG.getWord());
			PSL2C xxx = PSL2C.invert(null, currentG);
			v.add(xxx);
			if (debug) System.err.println("parabolic word: "+xxx.getWord());
		}
		return v;
	}

}
