/*
 * Created on 12 Apr 2023
 *
 */
package discreteGroup.quartz;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.util.TextSlider;
import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

public class QuartzGeometry {

	Color fclrs[] = {Color.cyan, Color.cyan, Color. yellow, Color.yellow};
	Color eclrs[] = {Color.orange, Color.cyan, Color.magenta, Color.magenta, Color.cyan, Color.orange}; 
	Color eclrs4[] = { Color.orange, Color.cyan, Color.cyan, Color.orange }; // Color.yellow, Color. green, Color.blue};

	protected double[][] tetpts = { { 1, 1, 1 }, { 1, -1, -1 }, { -1, 1, -1 }, { -1, -1, 1 } };

	protected int[][] tetrahedronIndices = { { 0, 1, 2 }, { 2, 1, 3 }, { 1, 0, 3 }, { 0, 2, 3 } };
	static private int[][] edgeIndices = { { 0, 1 }, { 0, 2 }, { 0, 3 }, { 1, 2 }, { 1, 3 }, { 2, 3 } };
	protected int[][] edgeIndices4 = { { 0, 1 }, { 0, 2 }, { 1, 3 }, { 2, 3 } };

	Color hfclrs[] = {Color.yellow, Color.yellow, Color.cyan, Color. cyan}; 
	Color heclrs[] = {Color.cyan, Color.magenta,  Color.yellow, Color.magenta, Color.cyan}; 
	protected double[][] halftetpts = { { 1, 1, 1 }, { 1, -1, -1 }, {0,1,0},{0,0,1},{0,-1,0},{0,0,-1}};

	protected int[][] halftetrahedronIndices = { { 0, 1, 5, 2 }, { 0, 2, 3 }, { 0,3,4,1 }, { 1,4,5 } };
	static private int[][] halfedgeIndices = { { 0, 1 }, {1,5},  { 0, 2 }, { 0, 3 }, {1,4} };
	protected int[][] halfedgeIndices4 = { { 0, 1 }, { 0, 2 }, { 1,3} };


	protected double axis3Pts[][] = {{1.0/3.0,0,0,1}, {1.0/3.0,0,1,1}};
	final double sq3 = 1/Math.sqrt(3.0);
	double[][] rhombpts = {{1,0,0,1},{0,sq3,0,1},{-1,0,0,1},{0,-sq3,0,1}};
	IndexedFaceSet rhomb = IndexedFaceSetUtility.constructPolygon(rhombpts);

	double a = 1.0, b = 1.0, c = 1.25486;
	double tetraYTlate = 0.0289284, 
			tetraAngle = 0.23509,
			tetraScale = 0.21506;

	Matrix tetraM = new Matrix(),
			screw3[] = new Matrix[3],
			transGens[] = new Matrix[3];
	{
		transGens[0] = MatrixBuilder.euclidean().translate(1,sq3,0).getMatrix();
		transGens[1] = MatrixBuilder.euclidean().translate(1,-sq3,0).getMatrix();
		transGens[2] = MatrixBuilder.euclidean().translate(0,0,c).getMatrix();
	}
	
	QuartzCrystal owner;
	boolean showFaceColors = true;
	
	QuartzGeometry(QuartzCrystal owner)	{
		super();
		this.owner = owner;
	}
	
	protected  Geometry getTetrahedron() {
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(4);
		ifsf.setEdgeCount(edgeIndices4.length);
		ifsf.setFaceCount(4);
		ifsf.setVertexCoordinates(tetpts);
		ifsf.setEdgeIndices(edgeIndices4);
		ifsf.setEdgeColors(eclrs4);
		ifsf.setFaceIndices(tetrahedronIndices);
		if (showFaceColors) ifsf.setFaceColors(fclrs);
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
		ifsf.setEdgeColors(heclrs);
		ifsf.setFaceIndices(halftetrahedronIndices);
		if (showFaceColors) ifsf.setFaceColors(hfclrs);
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		return ifsf.getIndexedFaceSet();
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
		// attempt to calculate y-translation so the image tetra shares a vertex
		double[] P0 = tpts[1], P1 = tpts[0], P2 = axis3Pts[0];
		double dx12 = P1[0] - P2[0],
				dx02 = P0[0] - P2[0],
				dz = P1[2] - P0[2],
				y0 = P0[1],
				y1 = P1[1],
				y = (dx12*dx12 - dx02*dx02 + y0*y0 - y1*y1)/(2*(y0-y1));
		tetraYTlate = y;
		// estimate the scaling needed to get 120 degree visual angle
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
			tm = MatrixBuilder.euclidean().rotate(tetraAngle, 0, 1, 0).scale(tetraScale).getMatrix();
			tpts = Rn.matrixTimesVector(null, tm.getArray(), tetpts);
			angle = angleFor3Points(tpts[1],P2,tpts[0], true);
			if (debug) System.err.println("tetrascale=\t"+tetraScale+"\tVisual angle is "+angle*(180/Math.PI));
			count++;
		}
		tetraM = tm;
		P0 = tpts[1]; P1 = tpts[0];
		dz = P1[2] - P0[2];
		owner.updateC(3*dz);
		double[] siAtom2 = new double[4], OAtom = new double[4];
		System.err.println("ytlate = "+tetraYTlate+"\tangle = "+(2*Math.PI)*tetraAngle+
				"\tscale = "+tetraScale+"\tc = "+c*Math.sqrt(3/4.0));
		for (int i = 0; i<3; ++i)	{
			screw3[i] = MatrixBuilder.euclidean().
				rotate(axis3Pts[0], axis3Pts[1], -2*(i/3.0)*Math.PI).
				translate(0,0,i*dz).
				translate(0,tetraYTlate,0).getMatrix();
			if (i == 0) {
				OAtom = screw3[i].multiplyVector(tpts[0]);
			}
			if (i==1) {
				siAtom2 = screw3[i].multiplyVector(P3.originP3);
			}
		}
		// calculate the bond angle Si-O-Si
		angle = angleFor3Points(P3.originP3, OAtom, siAtom2, false);
		System.err.println("bond angle = "+angle*(180/Math.PI));
		// calculate the projected length of the "order-6" edges of the tetrahedron
		double x0 = tpts[0][0] - tpts[2][0],
				x1 = tpts[1][0] - tpts[3][0];
		System.err.println("ratio of blue edges: "+(x1/x0));
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


	public Matrix[] getScrew3() {
		return screw3;
	}

	public IndexedFaceSet getRhomb() {
		return rhomb;
	}

	public Matrix[] getTransGens() {
		return transGens;
	}

	public Geometry getAxis() {
		double points[][] = {{1.0/3,0,0,1},{1.0/3,0,c,1}};
		IndexedLineSet ils = IndexedLineSetUtility.createCurveFromPoints(points, false);
		return ils;
	}
	
	public Component getInspector() {
		Box container = Box.createVerticalBox();
		final TextSlider<Double> tsSlider = new TextSlider.Double("tetra scale",  SwingConstants.HORIZONTAL,0.0, .3, tetraScale);
		tsSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				tetraScale = tsSlider.getValue().doubleValue();
				updateTetras();	
			}
		});
		container.add(tsSlider);
		final TextSlider<Double> taSlider = new TextSlider.Double("tetra angle",  SwingConstants.HORIZONTAL,0.0, 1.0, tetraAngle);
		taSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				tetraAngle = taSlider.getValue().doubleValue();
				owner.updateTetras();	
			}
		});
		container.add(taSlider);
		final TextSlider<Double> ttSlider = new TextSlider.Double("tetra y-tlate",  SwingConstants.HORIZONTAL, -.5, .5, tetraYTlate);
		ttSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				tetraYTlate = ttSlider.getValue().doubleValue();
				owner.updateTetras();	
			}
		});
		container.add(ttSlider);
		final TextSlider<Double> bsSlider = new TextSlider.Double("ball-stick scale",  SwingConstants.HORIZONTAL, 1,10, basScale);
		bsSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				basScale = bsSlider.getValue().doubleValue();
				updateBallAndStick();				
			}
		});
		container.add(bsSlider);
		return container;
	}
	
	 protected double[][] baspts = {{0,0,0}, { 1, 1, 1 }, { 1, -1, -1 }, { -1, 1, -1 }, { -1, -1, 1 }  };
	 protected int[][] basIndices =  {{0,1},{0,2},{0,3},{0,4}};
	 double str = .06, or = .15, sr = .3, basScale = 1;
	 protected double[] pointRadii = {sr, or, or, or, or};
	 Color oc = Color.yellow, sc = Color.cyan;
	 protected Color[] pointClr = {sc, oc, oc, oc, oc};
	 BallAndStickFactory basf  = null;
	IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
	SceneGraphComponent ilssgc = SceneGraphUtility.createFullSceneGraphComponent();
	boolean doBAS = false;
	 public SceneGraphComponent getBallAndStick()	{
		ilsf.setVertexCount(5);
		ilsf.setEdgeCount(4);
		ilsf.setVertexCoordinates(baspts);
		ilsf.setEdgeIndices(basIndices);
		ilsf.setVertexColors(pointClr);
		ilsf.setVertexAttribute(Attribute.RELATIVE_RADII, Rn.times(null, basScale, pointRadii));
		ilsf.setVertexAttribute(Attribute.POINT_SIZE, Rn.times(null, basScale, pointRadii));
		ilsf.update();
		IndexedLineSet ils = ilsf.getIndexedLineSet();
		SceneGraphComponent ret = null;
		if (doBAS) {
			if (basf == null) basf = new BallAndStickFactory(ils);
			basf.setRealSpheres(true);
			basf.setShowBalls(true);
			basf.setShowSticks(true);
			basf.setStickColor(Color.lightGray);
			basf.setStickRadius(basScale*str);
			basf.update();
			ret = basf.getSceneGraphComponent();			
		} else {
			Appearance ap = ilssgc.getAppearance();
			ap.setAttribute(CommonAttributes.VERTEX_DRAW,true);
			updateBallAndStick();
			ilssgc.setGeometry(ilsf.getIndexedLineSet());
			ret =  ilssgc;
		}
		return ret;
	}
	
	private void updateBallAndStick() {
		ilsf.setVertexAttribute(Attribute.POINT_SIZE, Rn.times(null, basScale, pointRadii));
		ilsf.setVertexAttribute(Attribute.RELATIVE_RADII, Rn.times(null, basScale, pointRadii));
		ilsf.update();
		Appearance ap = ilssgc.getAppearance();
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, basScale*str);
		ap.setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, 1.0);
		if (doBAS) {
			basf.setStickRadius(basScale * str);
			basf.update();		
		}
	}

}
