/*
 * Author	gunn
 * Created on Feb 8, 2006
 *
 */
package discreteGroup.puncturedTorus;

import charlesgunn.math.Complex;
import de.jreality.math.Pn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jtem.discretegroup.core.DiscreteGroup;

// this class is currently out of date but I'm keeping it to merge the generators used here into
// PuncturedTorusGroupFactory
public class PuncturedTorusGroup extends DiscreteGroup {

	Complex[] initialVertices = new Complex[3];
	Complex[] initialAngles = new Complex[3];
	boolean verticesChanged=true, anglesChanged=true;
	Complex[][]		generatorsPSL2C = null;
	Complex[] fixedPoints;
	Complex[][] commutatorFixedPoints;
	Complex[][] repetands;
	
	IndexedFaceSet triangle;
	
	public PuncturedTorusGroup() {
		super();
		initializeValues();
	}

	private static double[][][] iv = {
		{ {-1.73205,0},  {0,0},  {1.73205,0}},
		{  {-1,0}, {0,-2},{0,.5}} // product needs to be -1
	};
	private void initializeValues() {
		metric = Pn.EUCLIDEAN; //HYPERBOLIC;
		dimension = 3;
		isFree = true;
		for (int i = 0; i<3; ++i) 	{
			initialVertices[i] = new Complex(iv[0][i][0], iv[0][i][1]);
			initialAngles[i] = new Complex(iv[1][i][0], iv[1][i][1]);
		}
		update();
	}

	/**
	 * 
	 */
	public void update() {
		
//		if (verticesChanged || anglesChanged)	{
//			DiscreteGroupElement[] gens = new DiscreteGroupElement[6];
//			Complex[] IV = initialVertices;
//			for (int i = 0; i<3; ++i)	{
//				Complex z4 = CP1.completeCrossRatio(null, IV[i], IV[(i+1)%3], IV[(i+2)%3], initialAngles[(i+1)%3]);
//				System.err.println("z4 "+i+"\n"+z4.toString());
//				Complex[] gen = CP1.generalMoebius3PointsTo3Points(null, 
//						IV[i], IV[(i+1)%3], IV[(i+2)%3], 
//						IV[(i+2)%3], z4, IV[i]);
//				PSL2C.normalize(gen,gen);
//				System.err.println("Generator "+i+"\n"+(new PSL2C(gen)).toString());
//				gens[(i+1)%3] = new DiscreteGroupElement();
//				gens[(i+1)%3].setWord(DiscreteGroup.genNames[i]);
//				gens[(i+1)%3].setMatrix(CP1.convertPSL2CToSO31(null, gen));
//				gens[(i+1)%3+3] = (DiscreteGroupElement) gens[(i+1)%3].getInverse();
//				System.err.println("Generator "+i+"\n"+Rn.matrixToString(gens[(i+1)%3].getMatrix()));
//			}
//			setGenerators(gens);
//			double[][] verts = new double[3][4];
//			for (int i = 0; i<3; ++i) verts[i] = CP1.r2ToUnitSphere(null, initialVertices[i]);
//			triangle = IndexedFaceSetUtility.constructPolygon(verts);			
//		}
	}
	
//	public Geometry getDefaultFundamentalRegion() {
//		return triangle;
//	}
//	
	public SceneGraphComponent getLimitSet()	{
		SceneGraphComponent sgc=null;
		return sgc;
	}

	public Complex[] getInitialAngles() {
		return initialAngles;
	}

	public void setInitialAngles(Complex[] initialAngles) {
		this.initialAngles = initialAngles;
	}

	public Complex[] getInitialVertices() {
		return initialVertices;
	}

	public void setInitialVertices(Complex[] initialVertices) {
		this.initialVertices = initialVertices;
	}

	public Complex[][] getGeneratorsPSL2C() {
		return generatorsPSL2C;
	}

	public void setGeneratorsPSL2C(Complex[][] generatorsPSL2C) {
		this.generatorsPSL2C = generatorsPSL2C;
	}

}
