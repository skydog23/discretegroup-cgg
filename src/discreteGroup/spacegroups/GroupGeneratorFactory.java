/*
 * Created on Jun 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package discreteGroup.spacegroups;

import java.util.ArrayList;
import java.util.logging.Level;

import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupUtility;

/**
 * @author weissman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GroupGeneratorFactory {

	private static String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static int ind=0;
	private static void resetUniqueWord() { ind = 0; }
	private static String generateUniqueWord() {
		return new String(""+alphabet.charAt(ind++));
	}
	
	
	/** 
	 * be careful !!!
	 * 
	 */
	public static void resetWordIndex() {
		ind = 0;
	}
	
	public static DiscreteGroupElement[] generateT1() {
		double[] dir0 = {1,1,1,0};
		double[] dir1 = {1,-1,-1,0};
		double[] dir2 = {-1,-1,1,0};
		double[] dir3 = {-1,1,-1,0};
		double[] p2 = { 0.5*D4.scaleFactor, 0.5*D4.scaleFactor, 0.5*D4.scaleFactor, 1};
		
		double[] rot1 = P3.makeRotationMatrix(null,  dir0, Math.PI*2.0/3.0);
		double[] rot2 = P3.makeRotationMatrix(null,  dir1, Math.PI*2.0/3.0);
//		double[] rot3 = P3.makeRotationMatrix(null,  dir2, Math.PI*2.0/3.0);
//		double[] rot4 = P3.makeRotationMatrix(null,  dir3, Math.PI*2.0/3.0);
		double[] t1 = P3.makeTranslationMatrix(null, p2, Pn.EUCLIDEAN);
		resetUniqueWord();
		DiscreteGroupElement[] gens = new DiscreteGroupElement[6];
        gens[0] = new DiscreteGroupElement();
        gens[0].setArray(rot1);
        gens[0].setWord(generateUniqueWord());

        gens[1] = new DiscreteGroupElement();
        gens[1].setArray(rot2);
        gens[1].setWord(generateUniqueWord());

//		gens[2] = new DiscreteGroupElement();
//		gens[2].setMatrix(rot3);
//		gens[2].setWord(generateUniqueWord());
//
//		gens[3] = new DiscreteGroupElement();
//		gens[3].setMatrix(rot4);
//		gens[3].setWord(generateUniqueWord());
      
        gens[2] = new DiscreteGroupElement();
        gens[2].setArray(t1);
        gens[2].setWord(generateUniqueWord());

        for (int i =0; i<3; ++i)	{
        	gens[3+i] = (DiscreteGroupElement) gens[i].getInverse();
        }
		logGens(gens);
		return gens;
	}

	public static DiscreteGroupElement[] generateT2() {
		double[] p0 = {0,0,0,1};
		double[] dir0 = {1,1,1,0};
		double[] p1 = {0,D4.scaleFactor,0,1};
		double[] dir1 = {1,-1,-1,0};
		double[] p2 = { D4.scaleFactor, D4.scaleFactor, D4.scaleFactor, 1};
		
		double[] rot1 = P3.makeRotationMatrix(null, p0, dir0, Math.PI*2.0/3.0, Pn.EUCLIDEAN);
		double[] rot2 = P3.makeRotationMatrix(null, p1, dir1, Math.PI*2.0/3.0, Pn.EUCLIDEAN);
		double[] t1 = P3.makeTranslationMatrix(null, p2, Pn.EUCLIDEAN);
		resetUniqueWord();
		
		DiscreteGroupElement[] gens = new DiscreteGroupElement[6];
		gens[0] = new DiscreteGroupElement();
		gens[0].setArray(rot1);
		gens[0].setWord(generateUniqueWord());
		
		gens[1] = new DiscreteGroupElement();
		gens[1].setArray(rot2);
		gens[1].setWord(generateUniqueWord());

		gens[2] = new DiscreteGroupElement();
		gens[2].setArray(t1);
		gens[2].setWord(generateUniqueWord());

		for (int i =0; i<3; ++i)	{
        	gens[3+i] = (DiscreteGroupElement) gens[i].getInverse();
        }
		logGens(gens);
		return gens;
	}
	
	private static DiscreteGroupElement[] mergeGenerators(DiscreteGroupElement[] gens1, DiscreteGroupElement[] gens2) {
		DiscreteGroupElement[] ret = new DiscreteGroupElement[gens1.length + gens2.length];
		System.arraycopy(gens1, 0, ret, 0, gens1.length);
		System.arraycopy(gens2, 0, ret, gens1.length, gens2.length);
		for (int i = 0; i < ret.length; i++) DiscreteGroupUtility.logger.log(Level.INFO,ret[i].getWord());
		logGens(ret);
		return ret;
	}
	/**
	 * These are the groups which contain -1, point reflection in the origin. Names end with ':2'
	 * @param order
	 * @param sign
	 * @return
	 */
	public static DiscreteGroupElement[] generateD4_2_Subgroup(int order, int sign) {
		DiscreteGroupElement[] minus_one = new DiscreteGroupElement[1];
		minus_one[0] = (DiscreteGroupElement) D4.getElement("-");
		if (order == 1) return minus_one;
		DiscreteGroupElement[] gens1 = generateD4Subgroup(order, sign, "-");
//		for (int i = 0; i < gens1.length; i++) {
//			gens1[i].setMatrix(Rn.times(null, minus_one[0].getMatrix(), gens1[i].getMatrix()));
//		}
//		DiscreteGroupElement[] ret = new DiscreteGroupElement[2*gens1.length];
//		System.arraycopy(gens1, 0, ret, 0, gens1.length);
//		for (int i = 0; i < gens1.length; i++) {
//			DiscreteGroupElement mgen =  new DiscreteGroupElement();
//			mgen.setMatrix(Rn.times(null, minus_one[0].getMatrix(), gens1[i].getMatrix()));
//			//mgen.setWord("-"+gens1[i].getWord());
//			mgen.setWord(gens1[i].getWord());
//			ret[gens1.length+i] = mgen;
//		}
//		return ret;
		return mergeGenerators(generateD4Subgroup(order, sign), minus_one);
	}

	/**
	 * These are the groups generated by the subgroups of the dihedral group
	 * of order 4
	 * @param order
	 * @param sign
	 * @return
	 */
	public static DiscreteGroupElement[] generateD4Subgroup(int order, int sign) {
		return generateD4Subgroup(order, sign, "");
	}
	public static DiscreteGroupElement[] generateD4Subgroup(int order, int sign, String pre) {
		DiscreteGroupElement[] ret = null;
		int i = 0;
		switch (order) {
			case 8:
				ret = new DiscreteGroupElement[5];
//				ret[0] = (DiscreteGroupElement) D4.getElement("(02)(13)");
				ret[0] = (DiscreteGroupElement) D4.getElement(pre+"(13)");
				ret[1] = (DiscreteGroupElement) D4.getElement(pre+"(02)");
				ret[2] = (DiscreteGroupElement) D4.getElement(pre+"(0123)");
//				ret[4] = (DiscreteGroupElement) D4.getElement("(0321)");
				ret[3] = (DiscreteGroupElement) D4.getElement(pre+"(01)(23)");
				ret[4] = (DiscreteGroupElement) D4.getElement(pre+"(03)(12)");
				break;
			case 4:
				if (sign < 0) {
					ret = new DiscreteGroupElement[3];
					ret[0] = (DiscreteGroupElement) D4.getElement(pre+"(02)(13)");
					ret[1] = (DiscreteGroupElement) D4.getElement(pre+"(13)");
					ret[2] = (DiscreteGroupElement) D4.getElement(pre+"(02)");
				}
				if (sign == 0) {
					ret = new DiscreteGroupElement[2];
					ret[0] = (DiscreteGroupElement) D4.getElement(pre+"(02)(13)");
					ret[1] = (DiscreteGroupElement) D4.getElement(pre+"(0123)");
//					ret[2] = (DiscreteGroupElement) D4.getElement("(0321)");
				}
				if (sign > 0) {
					ret = new DiscreteGroupElement[2];
					ret[0] = (DiscreteGroupElement) D4.getElement(pre+"(02)(13)");
					ret[1] = (DiscreteGroupElement) D4.getElement(pre+"(01)(23)");
//					ret[2] = (DiscreteGroupElement) D4.getElement("(03)(12)");
				}
				break;
			case 2:
				ret = new DiscreteGroupElement[1];
				if (sign < 0) {
					ret[0] = (DiscreteGroupElement) D4.getElement(pre+"(13)");
				}
				if (sign == 0) {
					ret[0] = (DiscreteGroupElement) D4.getElement(pre+"(02)(13)");
				}
				if (sign > 0) {
					ret[0] = (DiscreteGroupElement) D4.getElement(pre+"(01)(23)");
				}
				break;
			default:
				throw new IllegalArgumentException("values represent no group!");
		}
		ret = addInverses(ret);
		logGens(ret);
		return ret;
	}
	
	private static DiscreteGroupElement[] addInverses(DiscreteGroupElement[] ret) {
		ArrayList<DiscreteGroupElement> glist = new ArrayList<DiscreteGroupElement>();
		for (DiscreteGroupElement gen: ret)	{
			glist.add(gen);
			if (Rn.isIdentityMatrix(Rn.times(null, gen.getArray(), gen.getArray()), 10E-8)) continue;
			glist.add((DiscreteGroupElement) gen.getInverse());
		}
		DiscreteGroupElement[] foo = new DiscreteGroupElement[glist.size()];
		return (DiscreteGroupElement[]) glist.toArray(foo);
	}
	
	public static DiscreteGroupElement[] generateD4_2_Subgroup(int order, int sign1, int sign2) {
		DiscreteGroupElement[] ret = null;
		int i = 0;
		switch (order) {
			case 8:
				if (sign1 < 0) {
					ret = new DiscreteGroupElement[5];
//					ret[0] = (DiscreteGroupElement) D4.getElement("(02)(13)");
					ret[0] = (DiscreteGroupElement) D4.getElement("(13)");
					ret[1] = (DiscreteGroupElement) D4.getElement("(02)");
					ret[2] = (DiscreteGroupElement) D4.getElement("-(0123)");
//					ret[4] = (DiscreteGroupElement) D4.getElement("-(0321)");
					ret[3] = (DiscreteGroupElement) D4.getElement("-(01)(23)");
					ret[4] = (DiscreteGroupElement) D4.getElement("-(03)(12)");
				}
				if (sign1 == 0) {
					ret = new DiscreteGroupElement[5];
//					ret[0] = (DiscreteGroupElement) D4.getElement("(02)(13)");
					ret[0] = (DiscreteGroupElement) D4.getElement("-(13)");
					ret[1] = (DiscreteGroupElement) D4.getElement("-(02)");
					ret[2] = (DiscreteGroupElement) D4.getElement("(0123)");
//					ret[4] = (DiscreteGroupElement) D4.getElement("(0321)");
					ret[3] = (DiscreteGroupElement) D4.getElement("-(01)(23)");
					ret[4] = (DiscreteGroupElement) D4.getElement("-(03)(12)");
				}
				if (sign1 > 0) {
					ret = new DiscreteGroupElement[5];
//					ret[0] = (DiscreteGroupElement) D4.getElement("(02)(13)");
					ret[0] = (DiscreteGroupElement) D4.getElement("-(13)");
					ret[1] = (DiscreteGroupElement) D4.getElement("-(02)");
					ret[2] = (DiscreteGroupElement) D4.getElement("-(0123)");
//					ret[4] = (DiscreteGroupElement) D4.getElement("-(0321)");
					ret[3] = (DiscreteGroupElement) D4.getElement("(01)(23)");
					ret[4] = (DiscreteGroupElement) D4.getElement("(03)(12)");
				}
				break;
				
			case 4:
				if (sign1 < 0) {
					if (sign2 < 0) {
						ret = new DiscreteGroupElement[2];
						ret[0] = (DiscreteGroupElement) D4.getElement("(13)");
						ret[1] = (DiscreteGroupElement) D4.getElement("-(02)(13)");
//						ret[2] = (DiscreteGroupElement) D4.getElement("-(02)");
					}
				}
				if (sign1 == 0) {
					if (sign2 < 0) {
						ret = new DiscreteGroupElement[2];
						ret[0] = (DiscreteGroupElement) D4.getElement("-(13)");
						ret[1] = (DiscreteGroupElement) D4.getElement("(02)(13)");
//						ret[2] = (DiscreteGroupElement) D4.getElement("-(02)");
					}
					if (sign2 == 0) {
						ret = new DiscreteGroupElement[2];
						ret[0] = (DiscreteGroupElement) D4.getElement("-(0123)");
						ret[1] = (DiscreteGroupElement) D4.getElement("(02)(13)");
//						ret[2] = (DiscreteGroupElement) D4.getElement("-(0321)");
					}
					if (sign2 > 0) {
						ret = new DiscreteGroupElement[2];
						ret[0] = (DiscreteGroupElement) D4.getElement("-(01)(23)");
						ret[1] = (DiscreteGroupElement) D4.getElement("(02)(13)");
//						ret[2] = (DiscreteGroupElement) D4.getElement("-(03)(12)");
					}
				}
				if (sign1 > 0) {
					if (sign2 > 0) {
						ret = new DiscreteGroupElement[2];
						ret[0] = (DiscreteGroupElement) D4.getElement("(01)(23)");
						ret[1] = (DiscreteGroupElement) D4.getElement("-(02)(13)");
//						ret[2] = (DiscreteGroupElement) D4.getElement("-(03)(12)");
					}
				}
				break;
			case 2:
				ret = new DiscreteGroupElement[order-1];
				if (sign1 == 0) {
					if (sign2 < 0) {
						ret[0] = (DiscreteGroupElement) D4.getElement("-(13)");
					}
					if (sign2 == 0) {
						ret[0] = (DiscreteGroupElement) D4.getElement("-(02)(13)");
					}
					if (sign2 > 0) {
						ret[0] = (DiscreteGroupElement) D4.getElement("-(01)(23)");
					}
				}
				break;
			default:
				throw new IllegalArgumentException("values represent no group!");
		}
		ret = addInverses(ret);
		logGens(ret);
		return ret;
	}
 
	public static DiscreteGroupElement[] generateQuarterGroup(int order, int sign) {
		if (order == 1) return generateT2();
		return mergeGenerators(generateD4Subgroup(order, sign), generateT2());
	}
	
	public static DiscreteGroupElement[] generateSingleSignedFullSubgroup(int order, int sign) {
		if (order == 1) return generateT1();
		return mergeGenerators(generateD4Subgroup(order, sign), generateT1());
	}

	public static DiscreteGroupElement[] generateDoubleFullSubgroup(int order, int sign) {
		return mergeGenerators(generateD4_2_Subgroup(order, sign), generateT1());
	}

	public static DiscreteGroupElement[] generateDoubleSignedDoubleFullSubgroup(int order, int sign1, int sign2) {
		return mergeGenerators(generateD4_2_Subgroup(order, sign1, sign2), generateT1());
	}

	private static void logGens(DiscreteGroupElement[] elems) {
		DiscreteGroupUtility.logger.log(Level.INFO,"Array of "+elems.length+" group elements.");
		for (int j = 0; j < elems.length; j++) DiscreteGroupUtility.logger.log(Level.INFO,elems[j].getWord());
		DiscreteGroupUtility.logger.log(Level.INFO,"===");
	}
	
	String[] D8Names = {"8.","4-","4.","4+","2-","2.","2+","1."};
	public static  DiscreteGroup getD8Group(String name)	{
		DiscreteGroup dg = new DiscreteGroup();
		dg.setName(name);
		dg.setDimension(3);
		dg.setMetric(Pn.EUCLIDEAN);
		DiscreteGroupElement[] gens = null;
		// parse first two characters; all names have these
		int order = Integer.parseInt(name.substring(0,1));	
		int sign1 = 1;
		if (name.charAt(1) == '.') sign1 = 0;
		else if (name.charAt(1) == '-') sign1 = -1;
		switch(name.length())	{
		case 2:		// positive subgroups of D4
			System.err.println("order:sign = "+order+" "+sign1);
			gens = generateSingleSignedFullSubgroup(order, sign1);
			break;
		case 3:		// change signs of selected elements
			int sign2 = 1;
			if (name.charAt(2) == '.') sign2 = 0;
			else if (name.charAt(2) == '-') sign2 = -1;
			System.err.println("order:sign1:sign2 = "+order+" "+sign1+" "+sign2);
			gens = generateDoubleSignedDoubleFullSubgroup(order, sign1, sign2);
			break;
		case 4:
			if (name.charAt(2) == '/')		{	// quarter group
				gens = generateQuarterGroup(order,sign1);
			} else if (name.charAt(2) == ':')	{ 	// e.g. "8.:2"
				gens = generateDoubleFullSubgroup(order,sign1);				
			}
			break;
		}
		dg.setGenerators(gens);
		dg.setCenterPoint(new double[]{.07*D4.scaleFactor,.05*D4.scaleFactor,.03*D4.scaleFactor,1});
		return dg;
	}
}
