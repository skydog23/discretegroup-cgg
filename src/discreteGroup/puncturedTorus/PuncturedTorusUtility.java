/*
 * Created on Feb 18, 2006
 *
 */
package discreteGroup.puncturedTorus;

import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.border.BevelBorder;

import charlesgunn.math.CP1;
import charlesgunn.math.Cn;
import charlesgunn.math.Complex;
import charlesgunn.math.ComplexFunction;
import charlesgunn.math.PSL2C;
import de.jreality.math.Matrix;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.data.Attribute;
import de.jreality.util.Rectangle3D;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.java2d.CoordinateGrid;
import de.jtem.java2d.SceneComponent;
import de.jtem.java2d.Viewer2D;
import de.jtem.java2d.ViewportChangeEvent;
import de.jtem.java2d.ViewportChangeListener;

public class PuncturedTorusUtility {

	private static boolean debug = false;

	public static PSL2C[] getGrandmaGenerators(Complex Ta, Complex Tb)	{
		Complex Tab = solveForTab(Ta, Tb)[0];
		Complex z0 = Complex.divide(null,
				Complex.times(null, Complex.subtract(null, Tab, new Complex(2,0)), Tb),
				Complex.add(null,
						Complex.subtract(null, Complex.times(null, Tb, Tab), Complex.times(null, 2, Ta)),
						Complex.times(null, new Complex(0,2), Tab)
				));
		Complex[] aa = new Complex[4];
		aa[0] = Complex.times(null, .5, Ta);
		aa[1] = Complex.divide(null, 
				Complex.add(null,
					Complex.subtract(null, Complex.times(null, Ta, Tab), Complex.times(null, 2, Tb)),
					new Complex(0,4)),
				Complex.times(null, 
					Complex.add(null,
						Complex.times(null, 2,Tab), 
						new Complex(4,0)),
					z0));
		aa[2] = Complex.divide(null, 
				Complex.times(null, 
					Complex.subtract(null,
							Complex.subtract(null, Complex.times(null, Ta, Tab), Complex.times(null, 2, Tb)),
							new Complex(0,4)),
					z0),
				Complex.subtract(null,
						Complex.times(null, 2,Tab), 
						new Complex(4,0)));
		aa[3] = aa[0];
		
		Complex[] bb = new Complex[4];
		bb[0] = Complex.times(null, .5, Complex.subtract(null, Tb, new Complex(0,2)));
		bb[1] = Complex.times(null, .5, Tb);
		bb[2] = bb[1];
		bb[3] = Complex.times(null, .5, Complex.add(null, Tb, new Complex(0,2)));
		
//		Complex[] cc = new Complex[4];
//		cc[0] = Complex.times(null, .5, Tab);
//		cc[1] = Complex.divide(null, Complex.subtract(null, Tab, new Complex(2,0)), Complex.times(null, 2, z0));
//		cc[2] = Complex.times(null, .5,
//				Complex.times(null,
//					Complex.add(null, Tab, new Complex(2,0)),
//					z0));
//		cc[3] = cc[0];
//		
//		Complex[] ab = Cn.times(null, aa, bb);
//		Complex[] error = Cn.times(null, ab, Cn.invert(null, cc));
//		if (debug) System.err.println("a*b*(ab)^-1 = "+new PSL2C(error).toString());
		// TODO: If I don't do this conjugation I get a bug appearing when drawing a sphere on a fixed point, located
		// presumably at "infinity" the north pole.
		Complex[] conj = (new PSL2C(new Complex(0,0), new Complex(1,0), new Complex(1,0), new Complex(0,0))).m;
		aa = Cn.conjugateBy(null, aa, conj);
		bb = Cn.conjugateBy(null, bb, conj);
		aa=Cn.normalize(null, aa);
		bb=Cn.normalize(null,bb);
		PSL2C[] gens = new PSL2C[4];
		gens[0] = new PSL2C(aa, DiscreteGroupUtility.genNames[0]);
		gens[1] = new PSL2C(bb, DiscreteGroupUtility.genNames[1]);
		gens[2] = PSL2C.invert(null, gens[0]);
		gens[3] = PSL2C.invert(null, gens[1]);
		if (debug) 
			for (int i = 0; i<4; ++i)	
				System.err.println("Generator "+0+"\n"+gens[i].toString());
		
		return gens;
	}

	private static Complex[] solveForTab(Complex Ta, Complex Tb) {
		Complex[] solns = Complex.quadraticSolve(null, 
				new Complex(1,0), 
				Complex.negate(null, Complex.times(null, Ta, Tb)), 
				Complex.add(null, Complex.times(null, Ta, Ta), Complex.times(null, Tb,Tb)));
		//Complex.copy(Tab, solns[1]);
		return solns;
	}
	
	public static DiscreteGroupElement[] convertGeneratorsToSO31(PSL2C[] genspsl2cs) {
		DiscreteGroupElement[] gens = new DiscreteGroupElement[4];
		for (int i = 0; i<2; ++i)	{
			gens[i] = new DiscreteGroupElement();
			gens[i].setWord(DiscreteGroupUtility.genNames[i]);
			gens[i].setArray(CP1.convertPSL2CToSO31(null, genspsl2cs[i].m));
			if (debug) {
				System.err.println("Generator "+i+"\n"+Rn.matrixToString(gens[i].getArray()));
				System.err.println("Determinant "+i+"\n"+Rn.determinant(gens[i].getArray()));				
			}
		}
		gens[2] = (DiscreteGroupElement) gens[0].getInverse();
		gens[3] = (DiscreteGroupElement) gens[1].getInverse();
			
		return gens;
	}
	
	public static PSL2C[][] calculateCommutators(PSL2C[] gens)	{
		PSL2C[][] commutators = new PSL2C[4][2];
		for (int i =0; i<4; ++i)  {
			commutators[i][0] = 
				PSL2C.times(null, 
					PSL2C.times(null, 
						PSL2C.times(null, gens[(i+3)%4], gens[(i+2)%4]),
						gens[(i+1)%4]),
					gens[i]);
			commutators[i][1] = 
				PSL2C.times(null, 
					PSL2C.times(null, 
						PSL2C.times(null, gens[(i+1)%4], gens[(i+2)%4]),
						gens[(i+3)%4]),
					gens[i]);
		}
		return commutators;
	}
	
	public static Complex tracePolynomial(int p, int q, Complex Ta, Complex Tb, Complex Tab)	{
		if (p==0 && q == 1) return Ta;
		if (p==1 && q == 0) return Tb;
		
		int p1 = 0, q1 = 1, p2 = 1, q2 = 0, p3 = 1, q3 = 1;
		Complex tu = new Complex(Ta),
			tv = new Complex(Tb),
			tuv = new Complex(Tab);
		Complex tmp = null;
		while (q * p3 != p * q3)	{
			if (p*q3 < p3*q)	{
				p2 = p3; q2 = q3; p3 = p1+p3; q3 = q1+q3;
				tmp = Complex.copy(tmp, tuv);
				Complex.subtract(tuv, Complex.times(null, tu, tuv), tv);
				Complex.copy(tv, tmp);
			} else {
				p1 = p3; q1 = q3; p3 = p2+p3; q3 = q2+q3;
				tmp = Complex.copy(tmp, tuv);
				Complex.subtract(tuv, Complex.times(null, tv, tuv), tu);
				Complex.copy(tu, tmp);
			}
		}
		return tuv;
	}
	
	public static String wordForFraction(int p, int q, String a, String b)	{
		if (p==0 && q == 1) return a;
		if (p==1 && q == 0) return b;
		String left = a, right = b;
		
		int p1 = 0, q1 = 1, p2 = 1, q2 = 0, p3 = 1, q3 = 1;
		while (q * p3 != p * q3)	{
			if (p*q3 < p3*q)	{
				p2 = p3; q2 = q3; p3 = p1+p3; q3 = q1+q3;
				right = left+right;
			} else {
				p1 = p3; q1 = q3; p3 = p2+p3; q3 = q2+q3;
				left = left+right;
			}
		}
		return left+right;
	}
		
	public static ComplexFunction traceEquation(final int p, final int q, final Complex trace)	{
		return traceEquation(p,q,trace, Complex.TWO);
	}

	public static ComplexFunction traceEquation(final int p, final int q, final Complex tB, final Complex c)	{
		return new ComplexFunction()	{
		public Complex valueAt(Complex z) {
//			Complex iMu = Complex.times(null, Complex.I, z);
//			Complex.negate(iMu, iMu);
			final Complex taB = solveForTab(z, tB)[1];
			return Complex.subtract(null, 
					tracePolynomial(
							p, 
							q, 
							z, //iMu,
							tB, // Complex.TWO, 
							taB), // iMu)), 	
					c);
		}
	};
	}
	
	public static ComplexFunction traceEquation(final int p, final int q, final Complex tB, final Complex taB, final Complex c)	{
		return new ComplexFunction()	{
		public Complex valueAt(Complex z) {
//			Complex iMu = Complex.times(null, Complex.I, z);
//			Complex.negate(iMu, iMu);
			return Complex.subtract(null, 
					tracePolynomial(
							p, 
							q, 
							z, //iMu,
							tB, // Complex.TWO, 
							taB), // iMu)), 	
					c);
		}
	};
	}
	
	
	public static int[][] fareySequence(int n)		{
		int[][] begin = {{0,1},{1,1}};
		int[][] row = begin;
		for (int i = 0; i<n; ++i)	{
			row = _fareySequenceOneStep(row);
		}
		return row;
	}
	
	private static int[][] _fareySequenceOneStep(int[][] row)	{
		int n = row.length;
		
		int[][] newRow = new int[n*2-1][];
		int[] left = row[0];
		int count = 0;
		newRow[count++] = left;
		for (int i = 1; i<n; ++i)	{
			int[] right = row[i];
			newRow[count++] = new int[]{left[0]+right[0], left[1]+right[1]};
			newRow[count++] = right;
			left = right;
		}
		return newRow;
	}
	
	public static PSL2C[] fareySequence(int n, PSL2C a, PSL2C b)		{
		PSL2C[] begin = {a,b};
		PSL2C[] row = begin;
		for (int i = 0; i<n; ++i)	{
			row = _fareySequenceOneStepPSL2C(row);
		}
		return row;
	}
	
	private static PSL2C[] _fareySequenceOneStepPSL2C(PSL2C[] row)	{
		int n = row.length;
		
		PSL2C[] newRow = new PSL2C[n*2-1];
		PSL2C left = row[0];
		int count = 0;
		newRow[count++] = left;
		for (int i = 1; i<n; ++i)	{
			PSL2C right = row[i];
			newRow[count++] = PSL2C.times(null, left, right);
			newRow[count++] = right;
			left = right;
		}
		return newRow;
	}
	
	private static class Typedef {
		int p, q;
		Complex fp;
		static Complex tb = Complex.TWO;
		
		public  static Typedef[] fareySequence(int n, Typedef a, Typedef b)		{
			Typedef[] begin = {a,b};
			Typedef[] row = begin;
			for (int i = 0; i<n; ++i)	{
				row = _fareySequenceOneStep(row);
			}
			return row;
		}
		
		private static Typedef[] _fareySequenceOneStep(Typedef[] row)	{
			int n = row.length;
			
			Typedef[] newRow = new Typedef[n*2-1];
			Typedef left = row[0], right = new Typedef(), newone;
			int count = 0;
			newRow[count++] = left;
			for (int i = 1; i<n; ++i)	{
				right = row[i];
				newone = newRow[count] = new Typedef();
				newRow[count].p = left.p+right.p;
				newRow[count].q = left.q+right.q;
				ComplexFunction cf = PuncturedTorusUtility.traceEquation(newone.p, newone.q, tb, Complex.TWO);
				Complex seed = Complex.times(null, .5, Complex.add(null, left.fp,right.fp));
				newRow[count].fp = Complex.newtonsMethod(cf, seed);
				newRow[count+1] = right;
				count += 2;
				left = right;
			}
			return newRow;
		}
		
	}
	
	public static Complex[] allParabolicsForFraction(int p, int q)	{
		return allParabolicsForFraction(p,q,null);
	}
	public static Complex[] allParabolicsForFraction(int p, int q, Complex seed)	{
		if (seed == null) seed = new Complex(0,0);
		ComplexFunction cf = PuncturedTorusUtility.traceEquation(p, q, new Complex(2,0));
		return Complex.rootSet(cf, q, seed);
	}
	
	public static Complex[] cuspsNEW(int n, Complex tb)	{
		Typedef a = new Typedef(), b = new Typedef();
		a.p = 0; a.q = 1;  
		ComplexFunction cf = PuncturedTorusUtility.traceEquation(0,1, tb, Complex.TWO);
		a.fp = Complex.newtonsMethod(cf, Complex.TWO);
		b.p = 1; b.q = 1;
		cf = PuncturedTorusUtility.traceEquation(1,1, tb, Complex.TWO);
		b.fp = Complex.newtonsMethod(cf, new Complex(2,2));
		System.err.println("Trace 0"+a.fp.toString());
		System.err.println("Trace 1"+b.fp.toString());
		Typedef.tb = tb;
		Typedef[] result = Typedef.fareySequence(n, a, b);
		int m = result.length;
		Complex[] traces = new Complex[m];
		for (int i = 0; i<m; ++i)	{
			traces[i] = result[i].fp;
		}
		return traces;
	}

	public static Complex[] cusps(int n, Complex tb)	{
		int[][] pq = fareySequence(n);
		int m = pq.length;
		Complex[] traces = new Complex[m];
		Complex seed = new Complex(2,0);
		double lastDsp=0, totalDsp=0, avgDsp=0, oldLastDsp=0, oldTotalDsp=0;
		for (int i = 0; i<m; ++i)	{
			ComplexFunction cf = PuncturedTorusUtility.traceEquation(pq[i][0], pq[i][1], tb, Complex.TWO);

			for (int repeats = 0; repeats<pq[i][1]; ++repeats)	{
				traces[i] = Complex.newtonsMethod(cf, seed);
				if (i>0)	{
					lastDsp = Complex.abs(Complex.subtract(null, traces[i], traces[i-1]));
					totalDsp += lastDsp;
					avgDsp = totalDsp/((double) i);
				}		
				if ( i < 4 || ( (totalDsp * 10) > (i*lastDsp))) break;		
				lastDsp = oldLastDsp;
				totalDsp = oldTotalDsp;
				cf = Complex.deflate(cf, traces[i]);
				if (repeats+1 == pq[i][1]) System.err.println("Failed to find good cusp "+pq[i][0]+"/"+pq[i][1]);
			} 
			seed=traces[i];
			oldLastDsp = lastDsp;
			oldTotalDsp = totalDsp;
		}
		return traces;
	}
	
	public static Complex[][] maskitParameters(int d, double max, int steps, Complex tb) {
		int[][] pq = fareySequence(d);
		int m = pq.length;
//		if (Complex.abs(Complex.subtract(null, new Complex(2,0), tb)) > .01) 
//			steps = 0;
		Complex[][] muValues = new Complex[m][steps+1];
		Complex seed2 = new Complex(2,0);
		double dp = (steps > 0) ? (max-2.0)/steps : 0;
		for (int i = 0; i<m; ++i)	{
			for (int j = 0; j<=steps; ++j)	{
				//double t = 2+j*dp;
				Complex t = Complex.add(null, Complex.TWO, new Complex(j*dp,0));
//				double s = .5*(t + Math.sqrt(t*t-4));
//				double qi = pq[i][1];
//				t = Math.pow(s,qi) + Math.pow(s,-qi);
				ComplexFunction cf = traceEquation(
						pq[i][0], 
						pq[i][1], 
						tb, t);
				seed2 = muValues[i][j] = Complex.newtonsMethod(cf, seed2);
				if (j > 0) {
					seed2 = Complex.subtract(null, Complex.times(null, 2, muValues[i][j]), muValues[i][j-1]);
				} else if (j>1)	{
					seed2 = Complex.add(null,
							Complex.subtract(null, 
									Complex.times(null, 3.0, muValues[i][j]), 
									Complex.times(null, 3.0, muValues[i][j-1])),
							muValues[i][j-2]);
				}
			}
			seed2 = muValues[i][0];
			//System.err.println("produced point "+seed2.toString());
		}
//		for (int i = 0; i<m; ++i)	
//			for (int j = 0; j<=steps; ++j)	 muValues[i][j].im += 2.0;
//			
		return muValues;
	}
	
	public static Complex[][] boundaryParameters(int d, double max, int steps, Complex tb, Complex tab) {
		int[][] pq = fareySequence(d);
		int m = pq.length;
		Complex[][] muValues = new Complex[m][steps+1];
		Complex seed2 = new Complex(2,0);
		double dp = (steps > 0) ? (max-2.0)/steps : 0;
		for (int i = 0; i<m; ++i)	{
			for (int j = 0; j<=steps; ++j)	{
				Complex t = Complex.add(null, Complex.TWO, new Complex(j*dp,0));
				ComplexFunction cf = traceEquation(
						pq[i][0], 
						pq[i][1], 
						tb, 
						tab,
						t);
				seed2 = muValues[i][j] = Complex.newtonsMethod(cf, seed2);
			}
			seed2 = muValues[i][0];
		}
		return muValues;
	}
	
	public static Complex[][] isolatedPoints(int k)	{
		int[][] fracs = PuncturedTorusUtility.fareySequence(k);
		int n = fracs.length;
		Complex[][] points = new Complex[n][];
		for (int j = 0; j<n; ++j)	{
			Complex seed = (j == 0) ? new Complex(2,0) : points[j-1][0];
			points[j] = PuncturedTorusUtility.allParabolicsForFraction(fracs[j][0], fracs[j][1], seed);	
//			System.err.println("Found "+roots.length+" roots for "+fracs[j][0]+"/"+fracs[j][1]);
//			for (int i = 0; i<roots.length; ++i)	{
//				System.err.println(roots[i].toString());
//			}
		}
		return points;
	}
	public static Viewer2D maskitSliceComponent( 
			int loRes, 
			int hiRes, 
			int cuspRes, 
			double maxTrace, 
			int steps, 
			Complex Tb) {

		Viewer2D self = getViewer2D();

		final SceneComponent root = self.getRoot();
		root.setOutlinePaint( java.awt.Color.RED);
		final SceneComponent[] levels = new SceneComponent[hiRes-loRes+1];
		final int number = hiRes-loRes+1;
		for (int i = loRes; i<= hiRes; ++i)	{
			Complex[][] maskitParameters = PuncturedTorusUtility.maskitParameters(i, maxTrace, steps, Tb);	
			levels[i-loRes]=sceneComponentFor(fareySequence(i), maskitParameters);
		}
		root.addChild(levels[0]);
		Complex[] cusps = PuncturedTorusUtility.cusps(hiRes, Tb);
		final SceneComponent cuspsSC = sceneComponentFor( cusps);
		root.addChild(cuspsSC);
		
		self.addViewportChangeListener(new ViewportChangeListener()	{
			int oldLevel = -1, level;
			public void viewportChange(ViewportChangeEvent e) {
				Rectangle2D vp = e.getViewport();
				double area = vp.getWidth() * vp.getHeight();
				//System.err.println("Area of viewport is "+area);
				oldLevel = level;
				level = calculateLevelForArea(area);
				if (level == oldLevel) return;
				//root.removeAllChildren();
				root.removeChild(levels[oldLevel]);
				//root.addChild(cuspsSC);
				root.addChild(levels[level]);
			}		
			
			public int calculateLevelForArea(double area)	{
				double x = Math.sqrt(area);
				x = 1.0/x;
				int level = (int) x;
				if (level >= number) level = number-1;
				return level;
			}
		});
		return self;
	}

	/**
	 * @return
	 */
	public static Viewer2D getViewer2D() {
		Viewer2D self=new Viewer2D();
		self.setBorder(
		  new javax.swing.border.BevelBorder(
		    BevelBorder.LOWERED,
		    java.awt.Color.white,
		    java.awt.Color.black
		  )
		);
		self.setBackground(new java.awt.Color(225,225,225));
//		self.setMaximumSize(
//		  new java.awt.Dimension(32767,32767)
//		);
//		self.setMinimumSize(new Dimension(200,200));
//		self.encompass(self.getBounds2D());
//		self.setEncompassMargin(20);
		final CoordinateGrid grid = new CoordinateGrid();
		self.addViewportChangeListener(
		  new ViewportChangeListener() {
		    public void viewportChange(ViewportChangeEvent event) {
		      grid.setRectangle(event.getViewport());
		      grid.fireAppearanceChange();
		    }

			  }
		);
		self.getBackdrop().addChild(grid);
		return self;
	}

	/**
	 * @param fractions
	 * @param cusps
	 * @param parameters
	 * @return
	 */
	private static SceneComponent sceneComponentFor(int[][] fractions,  Complex[][] parameters) {
		SceneComponent sc =new de.jtem.java2d.SceneComponent();
		sc.setFilled(Boolean.FALSE);
		sc.setOutlinePaint( java.awt.Color.RED);
		int n = parameters.length;

		GeneralPath path = new GeneralPath();
		for (int i = 0; i<n; ++i)	{
			path.moveTo((float) parameters[i][0].re, (float) parameters[i][0].im);
			for (int j = 1; j<parameters[i].length; ++j)	{
			    path.lineTo((float) parameters[i][j].re, (float) parameters[i][j].im);
			}
		}
//		TextArray ta = new TextArray(n);
//		for (int i = 0; i<n; ++i)	{
//			ta.setAnchor(i,Annotation.NORTH);
//			ta.setPosition(i, parameters[i][0].re,parameters[i][0].im);
//			ta.setText(i, fractions[i][0]+"/"+fractions[i][1]);
//			if (debug) System.err.println("Text is "+ta.getText(i));
//		}
//		sc.setAnnotation(ta);
		sc.setAnnotated(new Boolean(true));

		sc.setShape(path);
		return sc;
	}

	public static SceneComponent sceneComponentFor(Complex[] cusps) {
		SceneComponent sc =new de.jtem.java2d.SceneComponent();
		sc.setFilled(Boolean.FALSE);
		//sc.setOutlinePaint( java.awt.Color.RED);
		int n = cusps.length;
		GeneralPath path = new GeneralPath();
		path.moveTo((float) cusps[0].re, (float) cusps[0].im);
		for (int i = 1; i<n; ++i)	{
		    path.lineTo((float) cusps[i].re, (float) cusps[i].im);
		}

		sc.setShape(path);
		return sc;
	}

	public static SceneComponent sceneComponentForPoints(Complex[] cusps) {
		SceneComponent sc =new de.jtem.java2d.SceneComponent();
		sc.setFilled(Boolean.FALSE);
		//sc.setOutlinePaint( java.awt.Color.RED);
		int n = cusps.length;
		GeneralPath path = new GeneralPath();
		path.moveTo((float) cusps[0].re, (float) cusps[0].im);
		for (int i = 1; i<n; ++i)	{
		    path.moveTo((float) cusps[i].re, (float) cusps[i].im);
		    path.lineTo((float) cusps[i].re, (float) cusps[i].im);
		}

		sc.setShape(path);
		return sc;
	}

	public static void writePS(Matrix m, IndexedLineSet ils, File file, double epslinewidth, String comment) {
	    PrintWriter writer;
	    try {
	        writer = new PrintWriter(new FileWriter(file));
	    } catch (IOException e) {
	        throw new RuntimeException(e);
	    }
	    double[][] p = ils.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
	    p = Rn.matrixTimesVector(null, m.getArray(), p);
	    Rectangle3D bbox = new Rectangle3D(p);
	    double xmin = bbox.getMinX();
	    double ymin = bbox.getMinY();
	    double xmax = bbox.getExtent()[0];
	    double ymax = bbox.getExtent()[1];
	    double xc = bbox.getCenter()[0];
	    double yc = bbox.getCenter()[1];
	    int xsize = 612, ysize = 792;
	    double scale = 1.2;
	    double xr = xsize/xmax, yr = ysize/ymax;
	    double ff = (xr < yr) ? xr : yr;
	    ff = ff / scale;
	    
	    int n = p.length;
		writer.println("%!PS-Adobe-3.0 EPSF-3.0\n%%Creator: PuncturedTorusDemo");
		writer.println("%%LanguageLevel: 3");
		writer.println("%%BoundingBox: 0 0 612 792\n%%DocumentPaperSizes: Letter");
		// %%BoundingBox: "+scale*xmin+" "+scale*ymin+" "+scale*xmax+" "+scale*ymax);
		
		if (comment != null) writer.println("%%"+comment);
		writer.println("%%EndComments");
		writer.println("gsave\n");
		writer.println(String.format("%d %d translate", xsize/2, ysize/2));
		writer.println(String.format("%g %g scale", ff, ff));
		writer.println(String.format("%g %g translate", -xc, -yc));
		writer.println("1 setlinejoin");
		writer.println("1 setlinecap");
		writer.println(epslinewidth+" setlinewidth");
	
		n = ils.getNumEdges();
		for (int j = 0; j<n; ++j)	{
			int[] ed = ils.getEdgeAttributes(Attribute.INDICES).item(j).toIntArray(null);
			int mm = ed.length;
			int k = ed[0];
			double lastz = p[k][2];
			double zero = -(10E-8);
			
			if (lastz < 0) writer.println(".5 setgray");
			else writer.println("0 setgray");

			for(int i =0;i<mm;i++) {
				k = ed[i];
			    String formatx = PuncturedTorusUtility.format(p[k][0]);
				String formaty = PuncturedTorusUtility.format(p[k][1]);
				double thisz = p[k][2];
				if (i == 0 || lastz * thisz < 0)	{
					writer.println("stroke");
					if (thisz < 0) writer.println(".5 setgray");
					else 		writer.println("0 setgray");
					writer.println(formatx+" "+formaty+" moveto");
					lastz = thisz;
				}
			    if (i!=0) writer.println(formatx+" "+formaty+" lineto");
			}
			writer.println("stroke");
			
		}
		writer.println(" \ngrestore");	
		writer.close();
	}

	static String format(double d)	{
			String sd = Double.toString(d);
	//		return sd;
			if (sd.indexOf('E') != -1 || sd.indexOf('e') != -1) return sd;
			int n = sd.length();
			return sd.substring(0, Math.min(8, n));
		}

}
