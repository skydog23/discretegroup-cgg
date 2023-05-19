/*
 * Created on 12 Apr 2023
 *
 */
package discreteGroup.quartz;

import static discreteGroup.quartz.QuartzConstants.axis3Pts;
import static discreteGroup.quartz.QuartzConstants.vclrs;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedLineSet;

public class QuartzGeometry {


	protected static double[][] tetpts = { { 1, 1, 1 }, { 1, -1, -1 }, { -1, 1, -1 }, { -1, -1, 1 } };

	protected static int[][] tetrahedronIndices = { { 0, 1, 2 }, { 2, 1, 3 }, { 1, 0, 3 }, { 0, 2, 3 } };
	protected static int[][] edgeIndices = { { 0, 1 }, { 0, 2 }, { 0, 3 }, { 1, 2 }, { 1, 3 }, { 2, 3 } };
	protected static int[][] edgeIndices4 = { { 0, 1 }, { 0, 2 }, { 1, 3 }, { 2, 3 } };

	protected static double[][] halftetpts = { { 1, 1, 1 }, { 1, -1, -1 }, {0,1,0},{0,0,1},{0,-1,0},{0,0,-1}};

	protected static int[][] halftetrahedronIndices = { { 0, 1, 5, 2 }, { 0, 2, 3 }, { 0,3,4,1 }, { 1,4,5 } };
	static private int[][] halfedgeIndices = { { 0, 1 }, {1,5},  { 0, 2 }, { 0, 3 }, {1,4} };
	protected static int[][] halfedgeIndices4 = { { 0, 1 }, { 0, 2 }, { 1,3} };

	double sq3 = QuartzConstants.sq3;

	double a = 1.0, b = 1.0, c = 1.25485;
	double tetraYTlate = 0.035083, 
			tetraAngle = 0.28405,
			tetraScale = 0.216834;

	Matrix tetraM = new Matrix(),
			screw3[] = new Matrix[3];
	
	QuartzCrystal owner;
	BASTetrahedron basTetra = new QuartzBASTetrahedron();
	boolean showFaceColors = true;
	
	QuartzGeometry(QuartzCrystal owner)	{
		super();
		this.owner = owner;
	}
	
	protected static Geometry getTetrahedron() {
		return getTetrahedron(true);
	}
	
	protected static Geometry getTetrahedron(boolean fc) {
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(4);
		ifsf.setEdgeCount(edgeIndices4.length);
		ifsf.setFaceCount(4);
		ifsf.setVertexCoordinates(tetpts);
		ifsf.setVertexColors(vclrs);
		ifsf.setEdgeIndices(edgeIndices4);
		ifsf.setEdgeColors(QuartzConstants.eclrs4);
		ifsf.setFaceIndices(tetrahedronIndices);
		if (fc) ifsf.setFaceColors(QuartzConstants.fclrs);
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		return ifsf.getIndexedFaceSet();
	}

	protected  Geometry getHalfTetrahedron() {
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(halftetpts.length);
		ifsf.setEdgeCount(halfedgeIndices.length);
		ifsf.setFaceCount(halftetrahedronIndices.length);
		ifsf.setVertexCoordinates(halftetpts);
		ifsf.setEdgeIndices(halfedgeIndices);
		ifsf.setEdgeColors(QuartzConstants.heclrs);
		ifsf.setFaceIndices(halftetrahedronIndices);
		if (showFaceColors) ifsf.setFaceColors(QuartzConstants.hfclrs);
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		return ifsf.getIndexedFaceSet();
	}
	
	protected BASTetrahedron getBASTetrahedron() {
		return basTetra;
	}
	IndexedLineSetFactory cellILSF;

	public IndexedLineSet getCellOutline() {
		double xx =-1/3.0;
		double[][] hexpts = {
				{xx,0,0,1},
				{xx+1/3.0,sq3,0,1}, 
				{xx+1, sq3,0,1},
				{xx+4/3.0,0,0,1},
				{xx+1, -sq3,0,1},
				{xx+1/3.0, -sq3, 0, 1},
				{ xx, 0, c, 1 }, 
				{xx+1/3.0, sq3, c, 1 }, 
				{ xx+1, sq3, c, 1 }, 
				{xx+4/3.0, 0, c, 1 },
				{xx+1, -sq3,c, 1 }, 
				{ xx+1/3.0, -sq3, c, 1 } };
		int[][] indices = {{0,1,2,3,4,5,0},{6,7,8,9,10,11,6},
				{0,6},{1,7},{2,8},{3,9},{4,10},{5,11}};
		if (cellILSF == null) {
			cellILSF = new IndexedLineSetFactory();
			cellILSF.setVertexCount(hexpts.length);
			cellILSF.setEdgeCount(indices.length);
			cellILSF.setEdgeIndices(indices);
		}
		cellILSF.setVertexCoordinates(hexpts);
		cellILSF.update();
		
		return cellILSF.getIndexedLineSet();
	}



	static boolean debug = true;

	/*
	 * The basic idea is to move the tetrahedron in the only allowable ways motions
	 * that preserve the half-turn symmetry axis joining origin to y-direction. From
	 * this angle we calculate a scaling factor for the tetrahedron and a
	 * translation in the y-direction so that the corner of the tetrahedron maps
	 * under the 3-fold screw motion to another vertex, thus creating the desired
	 * spiral. We begin with the given scale factor to calculate the y-translation,
	 * then adjust the scale factor to get perfect match of the vertex pair.
	 */	
	public void updateTetras() {
		Matrix tm = MatrixBuilder.euclidean().rotate(tetraAngle, 0, 1, 0).scale(tetraScale).getMatrix();
		double[][] tpts = Rn.matrixTimesVector(null, tm.getArray(), tetpts);
		if (debug) System.err.println("tetra pts = "+Rn.toString(tpts));
		// calculate y-translation so the image tetra shares a vertex
		// with the source tetra
		double[] P0 = tpts[1], P1 = tpts[0], P2 = axis3Pts[0];
		double dx12 = P1[0] - P2[0],
				dx02 = P0[0] - P2[0],
				dz = P1[2] - P0[2],
				y0 = P0[1],
				y1 = P1[1],
				y = (dx12*dx12 - dx02*dx02 + y0*y0 - y1*y1)/(2*(y0-y1));
		tetraYTlate = y;
		// estimate the scaling needed to get 120 degree visual angle
		// first project the two end-points down to the horizontal plane
		double[] V1 = Rn.subtract(null,P1, axis3Pts[0]),
				V0 = Rn.subtract(null,P0, axis3Pts[0]);
		V1[1] += tetraYTlate; V1[2] = 0;
		V0[1] += tetraYTlate; V0[2] = 0;
		// V0 and V1 are now  vectors from the center of rotation to the ends
		// of the tetrahedron edge of interest, projected to plane z = 0
		Rn.normalize(V0, V0);
		Rn.normalize(V1, V1);
		double angle = Math.acos(Rn.innerProduct(V0, V1));
		if (debug) System.err.println("Visual angle is "+angle*(180/Math.PI));
		double one80 = (2.0/3.0)*Math.PI;
		int count = 0;
		while (count < 10 && Math.abs(angle - (2.0/3.0)*Math.PI) > .00001) {
			tetraScale = tetraScale * (1 + (one80/angle))/2.0;
//			tm = MatrixBuilder.euclidean().translate(0, tetraYTlate, 0).rotate(tetraAngle, 0, 1, 0).scale(tetraScale).getMatrix();
			tm = MatrixBuilder.euclidean().rotate(tetraAngle, 0, 1, 0).scale(tetraScale).getMatrix();
			tpts = Rn.matrixTimesVector(null, tm.getArray(), tetpts);
			angle = angleFor3Points(tpts[1],P2,tpts[0], true);
			if (debug) System.err.println("tetrascale=\t"+tetraScale+"\tVisual angle is "+angle*(180/Math.PI));
			count++;
		}
		// compute the vertical translation and distribute that to the whole app
		P0 = tpts[1]; P1 = tpts[0];
		dz = P1[2] - P0[2];
		owner.updateC(3*dz);
		// calculate the full transformation for the 
		tm = MatrixBuilder.euclidean().translate(0, tetraYTlate, 0).rotate(tetraAngle, 0, 1, 0).scale(tetraScale).getMatrix();
		tetraM = tm;
		tpts = Rn.matrixTimesVector(null, tm.getArray(), tetpts);

		double[] siAtom2 = new double[4], OAtom = new double[4];
		if (debug) System.err.println("ytlate = "+tetraYTlate+"\tangle = "+(2*Math.PI)*tetraAngle+
				"\tscale = "+tetraScale+"\tc = "+c*Math.sqrt(3/4.0));
		double[] transO = new double[]{0,tetraYTlate,0,1};
		OAtom = tpts[0];
		siAtom2 = owner.quartzGroup.getTriChannelM()[1].multiplyVector(transO);
		// calculate the bond angle Si-O-Si
		angle = angleFor3Points(transO, OAtom, siAtom2, false);
		if (debug) System.err.println("bond angle = "+angle*(180/Math.PI));
		// calculate the projected length of the "order-6" edges of the tetrahedron
		double x0 = tpts[0][0] - tpts[2][0],
				x1 = tpts[1][0] - tpts[3][0];
		if (debug) System.err.println("ratio of blue edges: "+(x1/x0));
		// calculate the density of the 3 tetrahedra within the crystal cell
		// (each of the 6 tetrahedron count as half-there, since each one belongs
		// to two adjacent cells
		double totalV = c*(2*sq3), tetV = 3*Math.pow(2*tetraScale,3)/(6*Math.sqrt(2));
		if (debug) System.err.println("filled fraction = "+tetV/totalV);
	}

	protected double angleFor3Points(double[] P0, double[] P2, double[] P1, boolean special) {
		double[] V1 = Rn.subtract(null,P1, P2);
		double[] V0 = Rn.subtract(null,P0, P2);
		if (special) {
			V1[1] += tetraYTlate; V1[2] = 0;
			V0[1] += tetraYTlate; V0[2] = 0;
		}
		// V0 and V1 are now  vectors from the center of rotation to the ends
		// of the tetrahedron edge of interest, projected to plane z = 0
		Rn.normalize(V0, V0);
		Rn.normalize(V1, V1);
	    return Math.acos(Rn.innerProduct(V0, V1));
	}

	public Matrix getTetraM() {
		return tetraM;
	}

	public Matrix getAxis3M() {
		double axis[][] = {{0,0,c/2,1}, {0,1,0,0}};
		return MatrixBuilder.euclidean().rotate(axis[0], axis[1], Math.PI).getMatrix();
	}

	public Geometry get3Axis() {
		double axis[][] = {{1/3.0,0,0,1}, {1.0/3.0,0,c,1}};
		IndexedLineSet ils = IndexedLineSetUtility.createCurveFromPoints(axis, false);
		return ils;
	}
	
	public Geometry get6Axis() {
		double axis[][] = {{0,sq3,0,1}, {0,sq3,c,1}};
		return IndexedLineSetUtility.createCurveFromPoints(axis, false);
	}
	
	Box inspector = null;
	public Component getInspector() {
		if (inspector == null) {
			inspector = Box.createVerticalBox();	
		} else return inspector;
		final TextSlider<Double> tsSlider = new TextSlider.Double("tetra scale",  SwingConstants.HORIZONTAL,0.0, .3, tetraScale);
		tsSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				tetraScale = tsSlider.getValue().doubleValue();
				updateTetras();	
			}
		});
//		container.add(tsSlider);
		final TextSlider<Double> taSlider = new TextSlider.Double("tetra angle",  SwingConstants.HORIZONTAL,0.0, 1.0, tetraAngle);
		taSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				tetraAngle = taSlider.getValue().doubleValue();
				owner.updateTetras();	
			}
		});
		inspector.add(taSlider);
		final TextSlider<Double> ttSlider = new TextSlider.Double("tetra y-tlate",  SwingConstants.HORIZONTAL, -.5, .5, tetraYTlate);
		ttSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				tetraYTlate = ttSlider.getValue().doubleValue();
				owner.updateTetras();	
			}
		});
//		container.add(ttSlider);
		
		inspector.add(basTetra.getInspector());
		
		return inspector;
	}

}
