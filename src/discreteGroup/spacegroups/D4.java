/*
 * Created on Jun 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package discreteGroup.spacegroups;

import java.util.HashMap;
import java.util.Hashtable;

import de.jreality.math.Matrix;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.groups.Platycosm;

/**
 * This class provides a factory method to create generatorsd for D4 and its subgroups.
 * see {@link discreteGroup.spacegroups.GroupGeneratorFactory}.
 * 
 * @author weissman
 */
class D4 {

  static HashMap<String, Matrix> generators = new HashMap<String, Matrix>();
  static Hashtable<String, String> translator = new Hashtable<String, String>();
  public static double scaleFactor = 2.0;
  
  static {
    translator.put("(02)(13)", "Z");
    translator.put("(0123)", "Y");
    translator.put("(0321)", "y");
    translator.put("(02)", "W");
    translator.put("(13)", "V");
    translator.put("(01)(23)", "U");
    translator.put("(03)(12)", "T");

    translator.put("-", "S");

    translator.put("-(02)(13)", "R");
    translator.put("-(0123)", "Q");
    translator.put("-(0321)", "q");
    translator.put("-(02)", "P");
    translator.put("-(13)", "O");
    translator.put("-(01)(23)", "N");
    translator.put("-(03)(12)", "M");

     double[] q0 = {0,0,0,1},
   		q1 = {.5*scaleFactor,.5*scaleFactor,.5*scaleFactor,1},
       	q2 = {scaleFactor,0,0,1},
    	q3 = {.5*scaleFactor,.5*scaleFactor,-.5*scaleFactor,1};
	double[] mq23 = Rn.linearCombination(null, .5, q2, .5, q3), //){.75,.25,-.25,1},
		mq01 = Rn.linearCombination(null, .5, q0, .5, q1), //{.25, .25, .25, 1};
		mq12 = Rn.linearCombination(null, .5, q1, .5, q2), //{.75,.25,.25,1},
		mq03 =  Rn.linearCombination(null, .5, q3, .5, q0), //{.25, .25, -.25, 1};
		mq02 =  Rn.linearCombination(null, .5, q0, .5, q2), //{.5,0,0,1},
		mq13 =  Rn.linearCombination(null, .5, q1, .5, q3), //{.5,.5,0,1};
		bis02 = P3.planeFromPoints(null, q1, q3, mq02),
		bis13 = P3.planeFromPoints(null, q0, q2, mq13);

	Matrix m = new Matrix(P3.makeReflectionMatrix(null, new double[]{0,1,0,0},  Pn.EUCLIDEAN)); 
    generators.put(translator.get("-"), m);
    
    Matrix mm02 = new Matrix(P3.makeReflectionMatrix(null, bis02, Pn.EUCLIDEAN));
    generators.put(translator.get("-(02)"), mm02);

    // (02) 
    Matrix m02 = new Matrix(P3.makeRotationMatrix(null, q1, q3, Math.PI/2, Pn.EUCLIDEAN));
    generators.put(translator.get("(02)"), m02);

    // -(13) 
    Matrix mm13 = new Matrix(P3.makeReflectionMatrix(null, bis13, Pn.EUCLIDEAN));
    generators.put(translator.get("-(13)"), mm13);

    // (13)
    Matrix m13 = new Matrix(P3.makeRotationMatrix(null, q0, q2, Math.PI/2, Pn.EUCLIDEAN));
    generators.put(translator.get("(13)"), m13);

    // (02)(13) what about combining (02) and (13)??
    Matrix m02m13 = //new Matrix(Rn.times(null, m02.getArray(), m13.getArray()));
    	new Matrix(P3.makeRotationMatrix(null, mq01, mq23, Math.PI, Pn.EUCLIDEAN));
    generators.put(translator.get("(02)(13)"),m02m13);

    // -(02)(13) what about combining (02) and (13)??
    Matrix mm02m13 =  new Matrix(makePointReflectionMatrix(null, mq02, Pn.EUCLIDEAN));
    generators.put(translator.get("-(02)(13)"),mm02m13);

	// (03)(12)
	Matrix m03m12 = new Matrix(P3.makeRotationMatrix(null, mq03, mq12, Math.PI, Pn.EUCLIDEAN));
	generators.put(translator.get("(03)(12)"), m03m12);

	// -(03)(12)
	Matrix mm03m12 = new Matrix(Rn.times(null, m03m12.getArray(),m.getArray()));
	generators.put(translator.get("-(03)(12)"), mm03m12);

    // (01)(23) 
    Matrix m01m23 =  new Matrix(P3.makeRotationMatrix(null, mq01, mq23, Math.PI, Pn.EUCLIDEAN));
    generators.put(translator.get("(01)(23)"), m01m23);
 
    // -(01)(23) 
    Matrix mm01m23 = new Matrix(Rn.times(null, m01m23.getArray(),m.getArray()));
    generators.put(translator.get("-(01)(23)"), mm01m23);
 
    // (0123)
	Matrix m0123 = new Matrix(Platycosm.screwMotion(mq02, mq13, Math.PI/2));
	generators.put(translator.get("(0123)"), m0123);

	// -(0123)
	Matrix mm0123 = new Matrix(Rn.times(null, m0123.getArray(), m.getArray()));
	generators.put(translator.get("-(0123)"), mm0123);

	Matrix m0321 = new Matrix(Platycosm.screwMotion(mq02, mq13, -Math.PI/2));
	generators.put(translator.get("(0321)"), m0321);

	// -(0321)
	Matrix mm0321 = new Matrix(Rn.times(null, m0321.getArray(), m.getArray()));
	generators.put(translator.get("-(0321)"), mm0123);


  }

  static final double[] reflOrigin = P3.makeScaleMatrix(null, -1);
  private static double[] makePointReflectionMatrix(double[] dst, double[] point, int metric)	{
	  if (dst == null) dst = new double[16];
	  double[] trans = P3.makeTranslationMatrix(null, point, metric);
	  return Rn.conjugateByMatrix(dst, P3.makeScaleMatrix(null, -1), trans);
  }
  static DiscreteGroupElement getElement(String name) {
//    boolean neg = false;
    DiscreteGroupElement ret = new DiscreteGroupElement();
    String word = (String) translator.get(name);
//    if (name.equals("-")) {
//      ret.setMatrix(((Matrix) generators.get(translator.get(name))).getArray());
//      ret.setWord(word);
//      return ret;
//    }
//    if (name.startsWith("-"))
//      neg = true;
//    Matrix element = new Matrix((Matrix) generators.get(translator.get(name
//        .substring(neg ? 1 : 0))));
//    if (neg)
//      element.multiplyOnLeft((Matrix) generators.get(translator.get("-")));
    Matrix element = new Matrix(generators.get(translator.get(name)));
    ret.setArray(element.getArray());
   // ret.setWord("-"+word);
    ret.setWord(word);
   return ret;
  }

}
