/*
 * Created on Jan 1, 2008
 *
 */
package discreteGroup.demo;

import static de.jreality.shader.CommonAttributes.BACKGROUND_COLOR;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.GeometryCollector;
import charlesgunn.jreality.SelectionComponent;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.plugin.TermesSpherePlugin;
import charlesgunn.jreality.texture.RopeTextureFactory;
import charlesgunn.jreality.texture.SimpleTextureFactory;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.Utility;
import charlesgunn.math.p5.PlueckerLineGeometry;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.BezierPatchMesh;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.geometry.QuadMeshUtility;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.RootAppearance;
import de.jreality.shader.Texture2D;
import de.jreality.swing.jrwindows.JRWindow;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jtem.beans.InspectorPanel;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.jrworkspace.plugin.simplecontroller.SimpleController.PropertiesMode;

public class SchatzCube extends Assignment {

	private transient SceneGraphComponent
	topdog,
		superworld,
			world,
				otherStuffSGC, 
					zaxisSGC, 
					deltoidGridSGC, 
					inverseDeltoidSGC, 
						diagonalsSGC,
					diagonalSGC, 
					deltoidSGC,
				schatzLinkSGC,
				  // 6-fold symmetry group D3
					// geomSGC, subclass of SGC
						schatzGeomSGC,
						stickGeomSGC, 
						tetraGeomSGC, 
						hexagonGeomSGC, 
						regulusGeomSGC,
					hingesSGC,
				oloidSGC,
					oloidProperSGC,
						oloidProper,
					mma2eclipseSGC,
						polarizingSphereSGC,
						// discrete group 8-fold oloid symmetry
							dgSGC,
								oloidGC,
									oloidBeyondCurve,
									oloidGCSGC,
								polarOloidGC,
									polarOloidToCurve,
									polarOloidGCSGC,
									polarOloidBeyondCurve,
				oloid2SGC,
					//oloidProperSGC,
				sphereSGC 
		;

	transient SelectionComponent 		geomSGC = new SelectionComponent();
	transient IndexedFaceSetFactory deltoidFactory = new IndexedFaceSetFactory();
	transient IndexedLineSetFactory diagonalFactory = new IndexedLineSetFactory(),
			diagonalsFactory = new IndexedLineSetFactory(),
			deltoidGridFactory = new IndexedLineSetFactory();
	transient GeometryCollector axisHistorySGC;
	transient PointRangeFactory axis1Factory;
	enum Geomtype {SCHATZ, TETRA, STICK, REGULUS, HEXAGON};
	transient SceneGraphPath w2FixedEdge, w2M1, w2M2;
	transient private SimpleTextureFactory oloidTextureFactory = new SimpleTextureFactory();

	transient boolean running = false,
		showXYZ = true,
		showHinges = true,
		showOloid = false,
		showOloid2 = false,
		symmetricParameter = true,
		showOnlyOne = false,
		showDeltoid = false,
		showSphere = false,
		showPSphere = false,
		showDiagonal = false,
		showAxisHistory = false,
//		polarize = false,
		showGrid = false,
		showGC = false,			
		showPolarGC = false,
		showPolarOloidToCurve = false,
		showPolarOloidBeyondCurve = false,
		showOloidBeyondCurve = false,
		fixOrigin = true,
		invert = false,    // allow deltoid to balloon out by handling projective coordinates carefully
		useMatrix = false,
		allowDeltoidJump = true,
		fixedPoints = false,
		animateOloidGeneration = true,
		oloidHasBeenGenerated = false;

	transient double originalAngle = Math.PI/4, angle = originalAngle, dt = .005;
	transient int numberSegments = 2;
	transient private Timer timer;
	transient int numSegs = 640;
	transient double[][][] cverts = new double[numSegs][2][];
	transient double phi = Math.sqrt(3.0)/3.0; //(Math.sqrt(5)-1)/2.0;
	// the vertices for one tetrahedron of the Schatz cube
	transient double[][] schatzverts = {
			{0,0,0,1}, 
			{0,0,2*phi,1},
			{0,2,0,1}, 
			{-2*phi,2,0,1}};
	transient double[][] fiveCubeVerts = new double[5][],
			fiveDeltoidVerts = new double[5][],
			originalDeltoidVerts = new double[8][],
			deltoidVerts = new double[8][];
	transient double[][] verts = {
			{0,0,-2*phi,1}, 
			{0,0,2*phi,1},
			{2*phi,2,0,1}, 
			{-2*phi,2,0,1},
			{0,2,0,1},
			{0,0,0,1}};
	transient double[] m1 = new double[16], m2 = new double[16], m3 = new double[16];
	transient int[] whichVerts = {0,2,3,6,7};
	transient int[][] deltoidInd = {{6,0,1,2},{6,2,3,4},{6,4,5,0},{7,1,2,3},{7,3,4,5},{7,5,0,1}};
	transient int[][] deltoidInd2 = {{6,0,1,2},{6,2,3,4},{6,4,5,0},{4,5,7,3},{0,1,7,5},{2,3,7,1}};
	transient double[] planeAtinfinity = P3.originP3;
	
	transient int gridCount = 10;
	transient double[][] gridVerts = new double[6*gridCount*gridCount][];
	transient Matrix projForm = new Matrix();
	transient double beginOloidGen = -1.0;
	
	transient static JRWindow win;
	transient static JTabbedPane tabs = new JTabbedPane();

	boolean doVR = false;
	transient static SchatzCube schatzcube = new SchatzCube();
	transient private DiscreteGroupElement[] gens;
	transient private DiscreteGroup symmetryGroup, oloidGroup;
	transient private DiscreteGroupSceneGraphRepresentation oloidDGSGR;
	private double[] schatzMotion;
	
	// following matrix is needed to convert from mathematica notebook coordinate system
	// to the coordinate system used here
	private transient double[] coa = {
			0,0,-2,0,
			2,0,0,2,
			0,2,0,0,
			0,0,0,1
	};


	@Override
	public SceneGraphComponent getContent() {
		hingesSGC = SceneGraphUtility.createFullSceneGraphComponent("lines");
		sphereSGC = SceneGraphUtility.createFullSceneGraphComponent("sphere");
		polarizingSphereSGC = SceneGraphUtility.createFullSceneGraphComponent("polarizing sphere");
		superworld = SceneGraphUtility.createFullSceneGraphComponent("superworld");
		dgSGC = SceneGraphUtility.createFullSceneGraphComponent("discrete group");
		schatzLinkSGC = SceneGraphUtility.createFullSceneGraphComponent("schatzLink");
		oloidProperSGC = SceneGraphUtility.createFullSceneGraphComponent("oloid proper sgc");
		oloidProper = SceneGraphUtility.createFullSceneGraphComponent("oloid proper");
		oloidSGC = SceneGraphUtility.createFullSceneGraphComponent("oloid");
		oloid2SGC = SceneGraphUtility.createFullSceneGraphComponent("oloid2");
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		world.getAppearance().setAttribute(SMOOTH_SHADING, false);
		mma2eclipseSGC = SceneGraphUtility.createFullSceneGraphComponent("mma2eclipse");
		

		geomSGC.setName("geometry");
		Appearance ap = new Appearance();
		geomSGC.setAppearance(ap);
		ap.setAttribute("lineShader.polygonShader.diffuseColor", Color.white);
		ap.setAttribute("pointShader.polygonShader.diffuseColor", Color.white);
		ap.setAttribute(VERTEX_DRAW, true);
		// set up lines
		PointRangeFactory prf = new PointRangeFactory();
		axis1Factory = prf;
		prf.setElement0(verts[0]);
		prf.setElement1(verts[1]);
		prf.setFiniteSphere(true);
		prf.setSphereRadius(5.0);
		prf.setNumberOfSamples(numberSegments);
		prf.update();
		SceneGraphComponent line1 = new SceneGraphComponent("line1");
		line1.setAppearance(new Appearance());
		line1.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", new Color(255,10,10));
		line1.getAppearance().setAttribute("lineShader.diffuseColor", new Color(255,10,10));
		line1.setGeometry(prf.getLine());
		
		prf = new PointRangeFactory();
		prf.setElement0(verts[2]);
		prf.setElement1(verts[3]);
		prf.setFiniteSphere(true);
		prf.setSphereRadius(5.0);
		prf.setNumberOfSamples(6);
		prf.update();
		SceneGraphComponent line2 = new SceneGraphComponent("line2");
		line2.setAppearance(new Appearance());
		line2.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", new Color(10,10,255));
		line2.getAppearance().setAttribute("lineShader.diffuseColor", new Color(10,10,255));
		line2.setGeometry(prf.getLine());
		hingesSGC.addChildren(line1, line2);
//		hingesSGC.setTransformation(geomSGC.getTransformation());
		
		ap = sphereSGC.getAppearance();
		ap.setAttribute(CommonAttributes.FACE_DRAW, false);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute("lineShader.diffuseColor", Color.white);
		ap.setAttribute("lineShader.lineWidth", .5);
		ap.setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		sphereSGC.setGeometry(SphereUtility.tessellatedIcosahedronSphere(4));
		sphereSGC.setVisible(showSphere);
		polarizingSphereSGC.setGeometry(SphereUtility.tessellatedIcosahedronSphere(4));
		polarizingSphereSGC.setVisible(showPSphere);
//		polarizingSphereSGC.setTransformation(geomSGC.getTransformation());
		polarizingSphereSGC.setAppearance(sphereSGC.getAppearance());
		
		
		symmetryGroup = new DiscreteGroup();
		symmetryGroup.setMetric(Pn.EUCLIDEAN);
		symmetryGroup.setDimension(3);
		gens = new DiscreteGroupElement[6];
		for (int i = 0; i<6; ++i)	{
			gens[i] = new DiscreteGroupElement();			
		}
		MatrixBuilder.euclidean().reflect(new double[]{1,0,0,0}).assignTo(gens[5].getArray());
		gens[5].setWord("a");
		MatrixBuilder.euclidean().rotate(Math.PI*2.0/3.0, new double[]{0,0,1}).assignTo(gens[2].getArray());
		gens[2].setWord("b");
		MatrixBuilder.euclidean().rotate(Math.PI*2.0/3.0, new double[]{0,0,1}).reflect(new double[]{1,0,0,0}).assignTo(gens[1].getArray());
		gens[1].setWord("ba");
		MatrixBuilder.euclidean().rotate(Math.PI*4.0/3.0, new double[]{0,0,1}).assignTo(gens[4].getArray());
		gens[4].setWord("bb");
		MatrixBuilder.euclidean().rotate(Math.PI*4.0/3.0, new double[]{0,0,1}).reflect(new double[]{1,0,0,0}).assignTo(gens[3].getArray());
		gens[3].setWord("bba");
		symmetryGroup.setElementList(gens);
		DiscreteGroupElement[] list = DiscreteGroupUtility.generateElements(symmetryGroup, null);
		for (int i = 0; i<list.length; ++i)	{
			SceneGraphComponent sgc = new SceneGraphComponent("link"+i);
			sgc.addChild(geomSGC);
			schatzLinkSGC.addChild(sgc);
			sgc.setTransformation(new Transformation(list[i].getArray()));
		}
		for (int i = 0; i<3; ++i)	{
			SceneGraphComponent sgc = new SceneGraphComponent("hingepair"+i);
			sgc.addChild(hingesSGC);
			schatzLinkSGC.addChild(sgc);
			sgc.setTransformation(new Transformation(list[2*i].getArray()));
			
		}
		world.addChild(schatzLinkSGC);
		timer = new Timer(10,new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setA(angle);
				angle += dt;
//				System.err.println("t = "+angle);
			}
			
		});
		
		w2FixedEdge = new SceneGraphPath();
		w2FixedEdge.push(world);
		w2FixedEdge.push(schatzLinkSGC);
		w2FixedEdge.push(schatzLinkSGC.getChildComponent(1));
		w2FixedEdge.push(geomSGC);
		w2M1 = new SceneGraphPath();
//		w2M1.push(superworld);
		w2M1.push(world);
		w2M1.push(schatzLinkSGC);
		w2M1.push(schatzLinkSGC.getChildComponent(0));
		w2M1.push(geomSGC);
		w2M2 = new SceneGraphPath();
//		w2M2.push(superworld);
		w2M2.push(world);
		w2M2.push(schatzLinkSGC);
		w2M2.push(schatzLinkSGC.getChildComponent(2));
		w2M2.push(geomSGC);
		superworld.addChild(world);
		topdog = new SceneGraphComponent();
		topdog.addChild(superworld);
		otherStuffSGC = SceneGraphUtility.createFullSceneGraphComponent("accessory");
		otherStuffSGC.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", Color.white);
		otherStuffSGC.getAppearance().setAttribute("lineShader.diffuseColor", Color.white);
//		otherStuffSGC.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
//		SurfaceElement.setDiskRadius(5);
//		SurfaceElement se = new SurfaceElement(new double[]{0,0,0,1}, new double[]{0,0,1,0});
//		SceneGraphComponent sgc = se.getRepresentation();
//		sgc.setName("surfaceElement");
//		sgc.getAppearance().setAttribute(TRANSPARENCY_ENABLED, true);
//		sgc.getAppearance().setAttribute(TRANSPARENCY, .8);
//		otherStuffSGC.addChild(sgc);
		prf = new PointRangeFactory();
		prf.setElement0(new double[]{0,0,0,1});
		prf.setElement1(new double[]{0,0,1,1});
		prf.setFiniteSphere(true);
		prf.setSphereRadius(5.0);
		prf.setNumberOfSamples(6);
		prf.update();
		zaxisSGC = SceneGraphUtility.createFullSceneGraphComponent("zaxis");
		zaxisSGC.setGeometry(prf.getLine());
		
		deltoidSGC = SceneGraphUtility.createFullSceneGraphComponent("deltoid");
		diagonalsSGC = SceneGraphUtility.createFullSceneGraphComponent("deltoid diagonals");
		deltoidSGC.addChild(diagonalsSGC);
		deltoidGridSGC = SceneGraphUtility.createFullSceneGraphComponent("deltoid grid");
//		deltoidSGC.addChild(deltoidGridSGC);
		deltoidGridSGC.setVisible(showGrid);
		diagonalsSGC.getAppearance().setAttribute(LINE_SHADER+"."+
				POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.cyan);
		diagonalsSGC.setGeometry(diagonalsFactory.getGeometry());
		diagonalsFactory.setVertexCount(8);
		diagonalsFactory.setEdgeCount(4);
		diagonalsFactory.setEdgeIndices(new int[][]{{0,3},{1,4},{2,5},{6,7}});

		updateSchatzMotion(originalAngle);
		updateDeltoid();
		fiveCubeVerts[0] = new double[]{1,0,0,0};
		fiveCubeVerts[1] = new double[]{0,1,0,0};
		fiveCubeVerts[2] = deltoidVerts[2]; //new double[]{0,0,0,1};
		fiveCubeVerts[3] = deltoidVerts[6];
		fiveCubeVerts[4] = deltoidVerts[7];
//		for (int i = 0; i<5; ++i)	{
//			fiveCubeVerts[i] = deltoidVerts[whichVerts[i]];
//		}
		for (int i = 0; i<8; ++i)	{
			originalDeltoidVerts[i] = deltoidVerts[i];
		}
		
		deltoidFactory.setVertexCount(deltoidVerts.length);
		deltoidFactory.setVertexCoordinates(deltoidVerts);
		deltoidFactory.setFaceCount(deltoidInd.length);
		deltoidFactory.setFaceIndices(deltoidInd);
		deltoidFactory.setFaceColors(new Color[]{colors[0], colors[0], colors[0], colors[1], colors[1], colors[1]});
		deltoidFactory.setGenerateEdgesFromFaces(true);
		deltoidFactory.setGenerateFaceNormals(true);
		deltoidFactory.setGenerateVertexNormals(true);
		deltoidFactory.update();
		deltoidSGC.setGeometry(deltoidFactory.getIndexedFaceSet());
		deltoidSGC.setVisible(showDeltoid);
//		deltoidSGC.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true)
		Appearance app = deltoidSGC.getAppearance();
		app.setAttribute(CommonAttributes.TUBES_DRAW, false);
		if (!allowDeltoidJump)	{
			app.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
			app.setAttribute(CommonAttributes.TRANSPARENCY, .5);			
		}
		app.setAttribute("lineShader.diffuseColor", Color.black);
		app.setAttribute("lineShader.lineWidth", 2);
		app.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		deltoidFactory.getIndexedFaceSet().setGeometryAttributes(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		deltoidGridFactory.getIndexedLineSet().setGeometryAttributes(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		
		updateDeltoidGrid(deltoidVerts);
		int[][] gridInd = new int[3*gridCount*gridCount][2];
		int offset = 3*gridCount*gridCount;
		for (int i = 0; i< gridInd.length; ++i)	{
			gridInd[i][0] = i;
			gridInd[i][1] = offset + i;
		}
		deltoidGridFactory.setEdgeCount(gridInd.length);
		deltoidGridFactory.setEdgeIndices(gridInd);
		deltoidGridFactory.update();
		deltoidGridSGC.setGeometry(deltoidGridFactory.getGeometry());
		app = deltoidGridSGC.getAppearance();
		app.setAttribute("lineShader.diffuseColor", Color.black);
		app.setAttribute("lineShader.lineWidth", 1);

		inverseDeltoidSGC = SceneGraphUtility.createFullSceneGraphComponent("inverse deltoid");
		inverseDeltoidSGC.addChild(deltoidSGC);
		new Matrix(Rn.times(null, -1, Rn.identityMatrix(4))).assignTo(inverseDeltoidSGC);
		
		diagonalSGC = SceneGraphUtility.createFullSceneGraphComponent("diagonal");
		diagonalSGC.getAppearance().setAttribute(LINE_SHADER+"."+
				POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.cyan);
		diagonalFactory = 
				IndexedLineSetUtility.createCurveFactoryFromPoints(
						new double[][]{schatzverts[0],schatzverts[2]}, false);
		diagonalSGC.setGeometry(diagonalFactory.getGeometry());
		diagonalSGC.setVisible(showDiagonal || showOloid || showOloid2);

		axisHistorySGC = new GeometryCollector(100);
		axisHistorySGC.setAppearance(new Appearance());
		axisHistorySGC.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
		axisHistorySGC.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
		axisHistorySGC.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
		axisHistorySGC.setVisible(showAxisHistory);
		
		setupGeometry();
		updateSphere();
//		setA(angle);

		otherStuffSGC.addChildren(zaxisSGC, deltoidGridSGC, inverseDeltoidSGC, diagonalSGC, axisHistorySGC, deltoidSGC);
		hingesSGC.setAppearance(otherStuffSGC.getAppearance());
		world.addChildren(sphereSGC, otherStuffSGC, oloidSGC, oloid2SGC);

		generateOloid();
		oloidSGC.setTransformation(schatzLinkSGC.getChildComponent(1)
				.getTransformation());
		oloidProperSGC.setVisible(showOloid);
		oloid2SGC.setTransformation(schatzLinkSGC.getChildComponent(4)
				.getTransformation());
		oloid2SGC.setVisible(showOloid2);
		oloidSGC.addChild(oloidProperSGC);
		oloid2SGC.addChild(oloidProperSGC);
		oloidProperSGC.addChild(oloidProper);
//		oloidProper.setTransformation(geomSGC.getTransformation());
//		dgSGC.setTransformation(geomSGC.getTransformation());
		oloid2SGC.setAppearance(oloidSGC.getAppearance());
		oloidSGC.addChild(mma2eclipseSGC);
		mma2eclipseSGC.addChild(polarizingSphereSGC);
		MatrixBuilder.euclidean().translate(-.5,0,0).assignTo(polarizingSphereSGC);
		new Matrix(coa).assignTo(mma2eclipseSGC);



		return topdog;
	}
	transient Color[] colors = {Color.red, Color.green, Color.blue, Color.yellow};
	private void setupGeometry() {
		schatzGeomSGC = SceneGraphUtility.createFullSceneGraphComponent("schatzCubeGeom");
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		int[][] indices  = {{0,1,2},{0,2,3},{0,3,1},{1,2,3}};
		ifsf.setVertexCount(schatzverts.length);
		ifsf.setVertexCoordinates(schatzverts);
		ifsf.setFaceCount(indices.length);
		ifsf.setFaceIndices(indices);
		ifsf.setFaceColors(colors);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setGenerateVertexNormals(true);
		ifsf.update();
		schatzGeomSGC.setGeometry(ifsf.getIndexedFaceSet());
		
		tetraGeomSGC = SceneGraphUtility.createFullSceneGraphComponent("tetraGeom");
		ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(verts.length);
		ifsf.setVertexCoordinates(verts);
		ifsf.setFaceCount(indices.length);
		ifsf.setFaceIndices(indices);
		ifsf.setFaceColors(colors);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setGenerateVertexNormals(true);
		ifsf.update();
		tetraGeomSGC.setGeometry(ifsf.getIndexedFaceSet());
		
		stickGeomSGC = SceneGraphUtility.createFullSceneGraphComponent("stickGeom");
		stickGeomSGC.getAppearance().setAttribute(LINE_SHADER+"."+
				POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.yellow);
		stickGeomSGC.setGeometry(
				IndexedLineSetUtility.createCurveFromPoints(
						new double[][]{schatzverts[0],schatzverts[2]}, false));
		
		regulusGeomSGC = SceneGraphUtility.createFullSceneGraphComponent("regulusGeom");
		Appearance ap = regulusGeomSGC.getAppearance();
		ap.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.white);
		ap.setAttribute(VERTEX_DRAW, false);
		ap.setAttribute(EDGE_DRAW, false);
		ap.setAttribute(LINE_SHADER+"."+TUBES_DRAW, false);
		RopeTextureFactory rtf = new RopeTextureFactory(ap);
		rtf.update();
		Texture2D tex2d = rtf.getTexture2D();
		Matrix foo = new Matrix();
		MatrixBuilder.euclidean().scale(8,10,1).assignTo(foo);
		tex2d.setTextureMatrix(foo);
		BezierPatchMesh thing = new BezierPatchMesh(1, 1, new double[][][]{{verts[0],verts[1]},{verts[2],verts[3]}});
		for (int i = 0; i<6; ++i) thing.refine();
		IndexedFaceSet ifs = BezierPatchMesh.representBezierPatchMeshAsQuadMesh(thing);
		QuadMeshUtility.generateAndSetEdgesFromQuadMesh(ifs);
		regulusGeomSGC.setGeometry(ifs);

		hexagonGeomSGC = SceneGraphUtility.createFullSceneGraphComponent("hexagonGeom");
		int[][] hexInd = {{0,1,4},{2,3,5}};
		ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(verts.length);
		ifsf.setVertexCoordinates(verts);
		ifsf.setFaceCount(hexInd.length);
		ifsf.setFaceIndices(hexInd);
		ifsf.setFaceColors(new Color[]{colors[0], colors[1]});
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setGenerateVertexNormals(true);
		ifsf.update();
		hexagonGeomSGC.setGeometry(ifsf.getIndexedFaceSet());
		
		
		geomSGC.addChildren(new SceneGraphComponent("none"),
				stickGeomSGC, 
				schatzGeomSGC, 
				tetraGeomSGC, 
				hexagonGeomSGC, 
				regulusGeomSGC );
		geomSGC.setSelectedChild(2);
		geomSGC.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
		geomSGC.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, true);

		
	}
	private void setA(double unsymmetricA)	{

		updateSchatzMotion(unsymmetricA);
		Matrix mm = new Matrix(schatzMotion);
		mm.assignTo(geomSGC);
		mm.assignTo(hingesSGC);
		mm.assignTo(oloidProper);
		updateDeltoid();
		fiveDeltoidVerts[0] = new double[]{1,0,0,0};
		fiveDeltoidVerts[1] = new double[]{0,1,0,0};
		fiveDeltoidVerts[2] = deltoidVerts[2];
		fiveDeltoidVerts[3] = deltoidVerts[6];
		fiveDeltoidVerts[4] = deltoidVerts[7];


		projForm.assignFrom(Pn.projectivity(null, fiveCubeVerts, fiveDeltoidVerts));
		double determinant = Rn.determinant(projForm.getArray());
		if (determinant != 0.0) Rn.times(projForm.getArray(), 1.0/determinant, projForm.getArray());
//		System.err.println("Proj = \n"+Rn.matrixToString(projForm.getArray()));
		double[] planeForm = Rn.inverse(null, Rn.transpose(null, projForm.getArray()));
		planeAtinfinity = Rn.matrixTimesVector(null, planeForm, P3.originP3);
//		System.err.println("plane at infinity = "+Rn.toString(planeAtinfinity));
		//
		if (fixOrigin) {
			projForm.setEntry(2,3, 0.0);
		}
		if (useMatrix) {
			projForm.assignTo(deltoidSGC);
		} else {
			deltoidFactory.setVertexCoordinates(deltoidVerts);
			deltoidFactory.update();			
		}
//		System.err.println("matrix = \n"+Rn.matrixToString(projForm.getArray()));
//		double[][] tformedV = Rn.matrixTimesVector(null, projForm.getArray(), fiveCubeVerts);
//		System.err.println("tformed verts given:\n"+Rn.toString(deltoidVerts));
//		System.err.println("tformed verts matrix:\n"+Rn.toString(Pn.dehomogenize(tformedV, tformedV)));
//		System.err.println("diff verts:\n"+Rn.toString(
//				Rn.subtract(null,fiveDeltoidVerts, Pn.dehomogenize(tformedV, tformedV))));
		updateDeltoidGrid(deltoidVerts);
		
		double[][] diagverts = new double[2][];
		diagverts[0] = deltoidVerts[0];
		diagverts[1] = deltoidVerts[3];
		diagonalFactory.setVertexAttribute(Attribute.COORDINATES, diagverts);
		diagonalFactory.update();
		// check position of "center point"
		double[] diag1 = PlueckerLineGeometry.lineFromPoints(null, diagverts[0], diagverts[1]);
		double[] diag2 = PlueckerLineGeometry.lineFromPoints(null, deltoidVerts[1], deltoidVerts[4]);
		double[] center = PlueckerLineGeometry.intersectionPointUnchecked(null, diag1, diag2);
		Pn.dehomogenize(center, center);
//		System.err.println("center point = "+Rn.toString(center));
		
		double[][] peep = axis1Factory.getLine().getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		peep = Rn.matrixTimesVector(null, schatzMotion, peep);
		IndexedLineSet ils = IndexedLineSetUtility.createCurveFromPoints(peep, false);
		if (count % 10 == 0) axisHistorySGC.addGeometry(ils);
		count++;
		
		w2FixedEdge.getMatrix(m1);
		double[] im1 = Rn.inverse(null, m1);
		if (fixedPoints) superworld.getTransformation().setMatrix(im1);
		else superworld.getTransformation().setMatrix(Rn.identityMatrix(4));
		
		// if we are showing the generation of the oloid and it hasn't finished yet ...
		if (showOloid && !oloidHasBeenGenerated) {
			if (beginOloidGen < 0.0) beginOloidGen = unsymmetricA;
			
		}

	}
	private void updateSchatzMotion(double t) {
		double a = t;
		if (symmetricParameter) a = a-.5*Math.asin(Math.sin(2*a)/3.0);
		double s = Math.sin(a),
			c = Math.cos(a),
			k = Math.sqrt(1+3*s*s), 
			k1 = Math.sqrt(3.0);
			
		schatzMotion = new double[] {
				k1*s, 1, 0, 0,
				-s, k1* s * s, k * c, -2*k1*k*k/3,
				c, -k1*c*s, k * s, k1*c*s,
				0,0,0,k};
		Rn.times(schatzMotion, 1.0/k, schatzMotion);
	}
	
	private double[][] updateDeltoid() {
		// update the deltoid geometry
		double[] vert0 = Rn.matrixTimesVector(null, schatzMotion, schatzverts[0]);
		double[] vert1 = Rn.matrixTimesVector(null, schatzMotion, schatzverts[2]);
		for (int i = 0; i<3; ++i)	{
			deltoidVerts[2*i] = Rn.matrixTimesVector(null, gens[2*i].getArray(), vert0);
			deltoidVerts[2*i+1] = Rn.matrixTimesVector(null, gens[2*i].getArray(), vert1);
		}
		double[][] dv = deltoidVerts;
		double[] plane1 = P3.planeFromPoints(null, dv[0], dv[1], dv[2]);
		deltoidVerts[6] = P3.lineIntersectPlane(null, P3.originP3, new double[]{0,0,1,0}, plane1);
		plane1 = P3.planeFromPoints(null, dv[5], dv[0], dv[1]);
		deltoidVerts[7] = P3.lineIntersectPlane(null, P3.originP3, new double[]{0,0,1,0}, plane1);
		if (invert)	{
			double modo = angle % (Math.PI*2);
			if (Math.PI/2 <= modo && modo <= 3*Math.PI/2 )	{
				Rn.times(deltoidVerts[6], -1, deltoidVerts[6]);
			}
			if (Math.PI <= modo && modo <= 2*Math.PI )	{
				Rn.times(deltoidVerts[7], -1, deltoidVerts[7]);
			}
		}
		Utility.dehomogenizePreserveWSign(deltoidVerts);
		updateSphere();
		diagonalsFactory.setVertexCoordinates(deltoidVerts);
		diagonalsFactory.update();
		return dv;
	}
	private void updateSphere() {
		// update the sphere
		double[] tmp1 = Pn.dehomogenize(null, deltoidVerts[6]),
				tmp2 = Pn.dehomogenize(null, deltoidVerts[7]),
				mp = Rn.add(null, tmp1, tmp2);
		double d = Pn.distanceBetween(tmp1, mp, Pn.EUCLIDEAN);
		MatrixBuilder.euclidean().translate(mp).scale(d).assignTo(sphereSGC);
	}
	
//	private void initializeDeltoidGrid() {
//		for (int i = 0; i<6; ++i)	{
//			for (int j = 0; j<gridCount; ++j)	{
//				for (int k = 0; k<gridCount; ++k)	{
//					int ll = deltoidInd2[i][0],
//						lr = deltoidInd2[i][1],
//						ul = deltoidInd2[i][3],
//						ur = deltoidInd2[i][2];
//					double u = j/(gridCount - 1.0);
//					double v = k/(gridCount - 1.0);
//					gridVerts[i*gridCount*gridCount + (j*gridCount+k)] = Utility.pointWithCoordinate(null, 
//							Utility.pointWithCoordinate(null,originalDeltoidVerts[ll], originalDeltoidVerts[lr], v, planeAtinfinity),
//							//Rn.linearCombination(null, v, originalDeltoidVerts[ll], 1.0-v, originalDeltoidVerts[lr]), 
//							Utility.pointWithCoordinate(null, originalDeltoidVerts[ul], originalDeltoidVerts[ur], v, planeAtinfinity), 
//							u, planeAtinfinity);
//				}
//			}
//		}
//	}
	private void updateDeltoidGrid(double[][] verts) {
		if (!showGrid) return;
		for (int i = 0; i<6; ++i)	{
			for (int j = 0; j<gridCount; ++j)	{
				for (int k = 0; k<gridCount; ++k)	{
					int ll = deltoidInd2[i][0],
						lr = deltoidInd2[i][1],
						ul = deltoidInd2[i][3],
						ur = deltoidInd2[i][2];
					double u = j/(gridCount - 1.0);
					double v = k/(gridCount - 1.0);
					gridVerts[i*gridCount*gridCount + (j*gridCount+k)] = Utility.pointWithCoordinate(null, 
							Utility.pointWithCoordinate(null,verts[ll], verts[lr], v, planeAtinfinity),
							//Rn.linearCombination(null, v, originalverts[ll], 1.0-v, originalverts[lr]), 
							Utility.pointWithCoordinate(null, verts[ul], verts[ur], v, planeAtinfinity), 
							u, planeAtinfinity);
				}
			}
		}
//		double[][] gridVerts2 = Rn.matrixTimesVector(null, projForm.getArray(), gridVerts);
		dehomogenizePreserveWSign(gridVerts);
		deltoidGridFactory.setVertexCount(gridVerts.length);
		deltoidGridFactory.setVertexCoordinates(gridVerts);
		deltoidGridFactory.update();
	}

	private double[][] dehomogenizePreserveWSign(double[][] vv) {
		int[] signs = new int[vv.length];
		for (int i = 0; i<vv.length; ++i)	{
			signs[i] = (vv[i][3] < 0) ? -1 : 1;
		}
		double[][] vvh = Pn.dehomogenize(vv, vv);
		for (int i = 0; i<vv.length; ++i)	{
			if (signs[i] < 0) Rn.times(vvh[i], -1, vvh[i]);
		}
		return vvh;
	}
	int count = 0;
	@Override
	public Component getInspector() {

		
		Box container = Box.createVerticalBox();
		final TextSlider aSlider = new TextSlider.Double("angle",  SwingConstants.HORIZONTAL, 0.0, Math.PI*2, angle);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				setA(angle = aSlider.getValue().doubleValue());
				//viewer.renderAsync();
			}
		});
		container.add(aSlider);
		final TextSlider eSlider = new TextSlider.Double("epsilon",  SwingConstants.HORIZONTAL, -.1,.1,epsilon);
		eSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				epsilon = eSlider.getValue().doubleValue();
				Matrix m = new Matrix();
				MatrixBuilder.euclidean().scale(5, 25, 1).translate(epsilon,epsilon,0).assignTo(m);
				tex2d.setTextureMatrix(m);

//				updateOloidExtendedOloid();
				//viewer.renderAsync();
			}
		});
		container.add(eSlider);
		container.add(Box.createVerticalGlue());
		runningCB = new JCheckBox("stop/start animation");
		runningCB.setSelected(running);
		runningCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				running = !running;
				if (running) timer.start();
				else timer.stop();
			}	
		});
		container.add(runningCB);
		
		fixCoordCB = new JCheckBox("fix link");
		fixCoordCB.setSelected(fixedPoints);
		fixCoordCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fixedPoints = fixCoordCB.isSelected();
				if (fixedPoints) superworld.getTransformation().setMatrix(w2FixedEdge.getInverseMatrix(null));
				else superworld.getTransformation().setMatrix(Rn.identityMatrix(4));
			}	
		});
		container.add(fixCoordCB);

		symmetricCB = new JCheckBox("use symmetric parametrization");
		symmetricCB.setSelected(symmetricParameter);
		symmetricCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				symmetricParameter = symmetricCB.isSelected();
			}	
		});
		container.add(symmetricCB);

		Box vbox = Box.createVerticalBox();
		TitledBorder title = BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Visibility");
		vbox.setBorder(title);
		showXYZCB = new JCheckBox("show coordinate system");
		showXYZCB.setSelected(showXYZ);
		showXYZCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showXYZ = showXYZCB.isSelected();
				zaxisSGC.setVisible(showXYZ);
			}	
		});
		vbox.add(showXYZCB);
		
		showHingesCB = new JCheckBox("show hinge lines");
		showHingesCB.setSelected(showHinges);
		showHingesCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showHinges = showHingesCB.isSelected();
				hingesSGC.setVisible(showHinges);
			}	
		});
		vbox.add(showHingesCB);

		showDiagonalCB = new JCheckBox("show diagonal");
		showDiagonalCB.setSelected(showDiagonal);
		showDiagonalCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showDiagonal = showDiagonalCB.isSelected();
				diagonalSGC.setVisible(showDiagonal);
			}	
		});
		vbox.add(showDiagonalCB);
		 
		// the following is a nice idea but the accelerator key seems to
		// have no effect since I don't attach the action to a menu item, 
		// but to a check box.  And, there's also the question of keyboard focus.
		AbstractAction showOloidAction = new AbstractAction("show oloid") {
			
			public void actionPerformed(ActionEvent e) {
				showOloid = showOloidCB.isSelected();
				oloidProperSGC.setVisible(showOloid);
			}
			
		};
		showOloidAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
		        KeyEvent.VK_4, 0));
		showOloidCB = new JCheckBox(showOloidAction);
		showOloidCB.setSelected(showOloid);
		vbox.add(showOloidCB);
				
		showOloid2CB = new JCheckBox("show oloid #2");
		showOloid2CB.setSelected(showOloid2);
		showOloid2CB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showOloid2 = showOloid2CB.isSelected();
				oloid2SGC.setVisible(showOloid2);
			}	
		});
		vbox.add(showOloid2CB);

		final JCheckBox showOneCB = new JCheckBox("extra oloid geometry: show 1/8");
		showOneCB.setSelected(showOnlyOne);
		showOneCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showOnlyOne = showOneCB.isSelected();
				DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(showOnlyOne ? 1 : 8);
				oloidGroup.setConstraint(dgsc);
				oloidGroup.update();
				oloidDGSGR.setElementList(oloidGroup.getElementList());
				oloidDGSGR.update();
			}	
		});
		vbox.add(showOneCB);

		final JCheckBox showgc = new JCheckBox("show oloid generating curve");
		showgc.setSelected(showGC);
		showgc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showGC = showgc.isSelected();
				oloidGCSGC.setVisible(showGC);
		}	
		});
		vbox.add(showgc);
		
		final JCheckBox showsbo = new JCheckBox("show full oloid");
		showsbo.setSelected(showOloidBeyondCurve);
		showsbo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showOloidBeyondCurve = showsbo.isSelected();
				oloidBeyondCurve.setVisible(showOloidBeyondCurve);
		}	
		});
		vbox.add(showsbo);
		
		final JCheckBox showpgc = new JCheckBox("show polar generating curve");
		showpgc.setSelected(showPolarGC);
		showpgc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showPolarGC = showpgc.isSelected();
				polarOloidGCSGC.setVisible(showPolarGC);
		}	
		});
		vbox.add(showpgc);
		
		final JCheckBox showstc = new JCheckBox("show polar oloid");
		showstc.setSelected(showPolarOloidToCurve);
		showstc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showPolarOloidToCurve = showstc.isSelected();
				polarOloidToCurve.setVisible(showPolarOloidToCurve);
		}	
		});
		vbox.add(showstc);
		
//		final JCheckBox showsbc = new JCheckBox("show polar oloid beyond curve");
//		showsbc.setSelected(showPolarOloidBeyondCurve);
//		showsbc.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				showPolarOloidBeyondCurve = showsbc.isSelected();
//				polarOloidBeyondCurve.setVisible(showPolarOloidBeyondCurve);
//		}	
//		});
//		vbox.add(showsbc);
		
		
		showDeltoidCB = new JCheckBox("show deltoid");
		showDeltoidCB.setSelected(showDeltoid);
		showDeltoidCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showDeltoid = showDeltoidCB.isSelected();
				deltoidSGC.setVisible(showDeltoid);
			}	
		});
		vbox.add(showDeltoidCB);
		showSphereCB = new JCheckBox("show deltoid sphere");
		showSphereCB.setSelected(showSphere);
		showSphereCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showSphere = showSphereCB.isSelected();
				sphereSGC.setVisible(showSphere);
			}	
		});
		vbox.add(showSphereCB);
		showPSphereCB = new JCheckBox("show polarizing sphere");
		showPSphereCB.setSelected(showPSphere);
		showPSphereCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showPSphere = showPSphereCB.isSelected();
				polarizingSphereSGC.setVisible(showPSphere);
			}	
		});
		vbox.add(showPSphereCB);
		container.add(vbox);
		
		vbox = Box.createVerticalBox();
		title = BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Geometry selection");
		vbox.setBorder(title);
		String[] geomNames = { "None", "Stick", "Schatz cube","Maximal tetrahedron", "Hexagon","Regulus"};
		for (int i = 0; i<geomNames.length; ++i)	{
			geomB[i] = new JRadioButton(geomNames[i]);
			final int j = i;
			geomB[i].addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					geomSGC.setSelectedChild(j);
				}
				
			});
			bg.add(geomB[i]);
			vbox.add(geomB[i]);
		}
		geomB[2].setSelected(true);
		container.add(vbox);
		
		InspectorPanel ip = new InspectorPanel();
		ip.setObject(oloidTextureFactory, "update");
		container.add(ip);
		
		JPanel panel = new JPanel();
		panel. setName("Parameters");
		panel.add(container);
		return panel;
	}
	transient JCheckBox runningCB, 
		showXYZCB, 
		showHingesCB, 
		showDiagonalCB,
		showOloidCB, 
		showOloid2CB, 
		fixCoordCB,
		symmetricCB,
		showDeltoidCB,
		showSphereCB,
		showPSphereCB;
	transient JRadioButton[] geomB = new JRadioButton[6];
	transient ButtonGroup bg = new ButtonGroup();

	
	@Override
	public void setupJRViewer(JRViewer v) {
		super.setupJRViewer(v);
		v.registerPlugin(new TermesSpherePlugin());
		v.getController().setPropertiesMode(PropertiesMode.StaticPropertiesFile);
		InputStream is = SchatzCube.class.getResourceAsStream("schatzCubeVR.xml");
		v.getController().setPropertiesInputStream(is);
		
	}
	
	@Override
	public void setValueAtTime(double d) {
		// TODO Auto-generated method stub
//		angle = AnimationUtility.linearInterpolation(2*d, 0, 1, Math.PI*2+Math.PI/4,  Math.PI/4);
		angle = AnimationUtility.linearInterpolation(d, 0, 1, 0, 2*Math.PI);
		generateOloidProper(d);
		setA(angle);
	}


	@Override
	public void display() {
//		psl.getJRViewer().setPropertiesFile("schatzCubeVR.xml");
		super.display();
		
		animationPlugin.getAnimationPanel().setResourceDir("src/discreteGroup/demo/");
		Viewer viewer = jrviewer.getViewer();
		CameraUtility.encompass(viewer);
//		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.METRIC, Pn.ELLIPTIC);
		setBGC(viewer);
		attachKeyListener(viewer);
		viewer.renderAsync();

	}
	private void setBGC(Viewer viewer) {
		final Color URBackground = new Color(.8f, .85f, .68f); //new Color(215, 215, 190);
		final Color ULBackground  = new Color(1f, .98f, .8f); //new Color(255, 255, 200);  // bg[1];
		final Color LLBackground  = new Color(.1f, .1f, .25f); //new Color(20,20,60);
		final Color LRBackground  = new Color(0.05f, .15f, .35f); //new Color(25, 25, 100);  //bg[2];
		Color[] backgroundArray = new Color[4];
		backgroundArray[0] = URBackground;
		backgroundArray[1] = ULBackground;// bg[1];
		backgroundArray[2] = LLBackground;
		backgroundArray[3] = LRBackground;  //bg[2];
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColors", backgroundArray);
	}
	private void attachKeyListener(Viewer viewer) {
		((Component) viewer.getViewingComponent()).addKeyListener( new KeyAdapter()	{
			int which = 0;
			boolean drag = false;
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode())	{
				
				case KeyEvent.VK_H:
					System.err.println(" 1: toggle animate");
					break;
				case KeyEvent.VK_1:
					running = !running;
					if (running) timer.start();
					else timer.stop();
					break;
				case KeyEvent.VK_2:
					otherStuffSGC.setVisible(!otherStuffSGC.isVisible());
					break;
				case KeyEvent.VK_3:
					hingesSGC.setVisible(!hingesSGC.isVisible());
					break;
				case KeyEvent.VK_4:
					oloidSGC.setVisible(!oloidSGC.isVisible());
					break;
				case KeyEvent.VK_5:
					oloid2SGC.setVisible(!oloid2SGC.isVisible());
					break;
				case KeyEvent.VK_6:
					fixedPoints = !fixedPoints;
					if (fixedPoints) superworld.getTransformation().setMatrix(w2FixedEdge.getInverseMatrix(null));
					else superworld.getTransformation().setMatrix(Rn.identityMatrix(4));
					break;
				case KeyEvent.VK_7:
					int selected = geomSGC.getSelectedChild();
					selected = (selected+1)%5;
					geomSGC.setSelectedChild(selected);
					geomB[selected].setSelected(true);
					break;
				case KeyEvent.VK_8:
					showGrid = !showGrid;
					deltoidGridSGC.setVisible(showGrid);
					updateDeltoidGrid(deltoidVerts);
					break;
				}
			}	
			
		});
	}


	public static void main(String[] args) {
//		Secure.setProperty(SystemProperties.VIEWER,GlobalProperties.DEFAULT_VIEWER); 
//		Secure.setProperty("doOwnTools", "false"); 
//		schatzcube.doViewerThing();
		new SchatzCube().display();
	}
	
	public Component getReadMePanel()	{
		JPanel mypanel = new JPanel();
		mypanel.setName("ReadMe");
		JTextArea textarea = new JTextArea(10,30);
		textarea.setEditable(false);
		textarea.append("Enjoy the fascinating motion\n" +
				"of the 6-linkage ring, the Schatz cube\n" +
				"(named for its discoverer Paul Schatz.\n" +
				"\nThe following keys are active:\n" +
				"    1: toggle animated motion\n" +
				"    2: toggle xyz display\n" +
				"    3: toggle display of hinge lines\n" +
				"    4: toggle display of first oloid\n" +
				"    5: toggle display of second oloid\n" +
				"    6: fix the first oloid\n" +
				"    7: cycle the geometry\n\n" +
				"'h' shows a help overlay for viewer\n" +
				"'e' encompasses the scene\n" +
				"\nAuthor: Charles Gunn\n"+
				"    gunn at math.tu-berlin.de\n");
		mypanel.add(textarea);
		return mypanel;
	}

	private void generateOloid()	{
		generateOloidProper(1.0);
		
//		if (true) return;
		// from here almost everything has to do with the polar of the oloid
		polarOloidGC = SceneGraphUtility.createFullSceneGraphComponent("polar generatingCurve");
		oloidGC = SceneGraphUtility.createFullSceneGraphComponent("generatingCurve");
		MatrixBuilder.euclidean().scale(.99).assignTo(polarOloidGC);
		oloidGCSGC = SceneGraphUtility.createFullSceneGraphComponent("generatingCurveItself");
		polarOloidGCSGC = SceneGraphUtility.createFullSceneGraphComponent("generatingCurveItself");
		polarOloidToCurve = SceneGraphUtility.createFullSceneGraphComponent("surfaceToCurve");
		oloidBeyondCurve = SceneGraphUtility.createFullSceneGraphComponent("surfaceBeyondOloid");
		polarOloidBeyondCurve = SceneGraphUtility.createFullSceneGraphComponent("surfaceBeyondCurve");
		polarOloidGC.addChildren(polarOloidGCSGC, polarOloidToCurve, polarOloidBeyondCurve);
		oloidGC.addChildren(oloidBeyondCurve, oloidGCSGC);
		dgSGC.addChildren(oloidGC, polarOloidGC);
		
		//		oloidProper.addChild(genCurveSGC);
		int number = updateOloidExtendedOloid();
		oloidGCSGC.getAppearance().setAttribute("lineShader.diffuseColor", Color.red);
		polarOloidGCSGC.getAppearance().setAttribute("lineShader.diffuseColor", Color.red);
//		polarOloidToCurve.getAppearance().setAttribute(CommonAttributes.FLIP_NORMALS_ENABLED, true);
		polarOloidToCurve.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		polarOloidToCurve.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		polarOloidBeyondCurve.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		polarOloidBeyondCurve.getAppearance().setAttribute(CommonAttributes.FLIP_NORMALS_ENABLED, true);
		oloidBeyondCurve.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		oloidBeyondCurve.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		polarOloidGCSGC.setVisible(showPolarGC);
		oloidGCSGC.setVisible(showGC);
		polarOloidToCurve.setVisible(showPolarOloidToCurve);
		polarOloidBeyondCurve.setVisible(showPolarOloidBeyondCurve);
		oloidBeyondCurve.setVisible(showOloidBeyondCurve);
	// The full generating curve is created by the symmetry group 2*2 of order 8 
		// generated by an order-2 rotation around a line angled at
		// 45 degrees to the x-axis, and a reflection in the xz plane
		oloidGroup = new DiscreteGroup();
		DiscreteGroupElement gens[] = new DiscreteGroupElement[2];
		double[] rot = P3.makeRotationMatrix(null, new double[]{-.5,0,0,1}, new double[]{-.5,.5,.5,1}, Math.PI, Pn.EUCLIDEAN);
		gens[0] = new DiscreteGroupElement(Pn.EUCLIDEAN, rot, "a");
		double[] refl = P3.makeReflectionMatrix(null, new double[]{0,1,0,0}, Pn.EUCLIDEAN);
		gens[1] = new DiscreteGroupElement(Pn.EUCLIDEAN, refl, "b");
		oloidGroup.setGenerators(gens);
		oloidGroup.setFinite(true);
		oloidGroup.setConstraint(new DiscreteGroupSimpleConstraint(showOnlyOne ?  1 : 8));
		oloidGroup.update();
		oloidDGSGR = new DiscreteGroupSceneGraphRepresentation(oloidGroup, false);
//		symm.update();
		oloidDGSGR.setWorldNode(dgSGC);
		oloidDGSGR.update();
		Appearance ap1 = oloidDGSGR.getRepresentationRoot().getAppearance();
//		tex2d = (Texture2D) AttributeEntityUtility.createAttributeEntity(
//				Texture2D.class, "polygonShader.texture2d", ap1, true);
//		tex2d.setImage(oloidTextureFactory.getImageData());
		Matrix m = new Matrix();
		MatrixBuilder.euclidean().scale(5, 25, 1).assignTo(m);
		tex2d.setTextureMatrix(m);

//		new Matrix(coa).assignTo(oloidDGSGR.getRepresentationRoot());
		mma2eclipseSGC.addChild(oloidDGSGR.getRepresentationRoot());
	}
	private int updateOloidExtendedOloid() {
		int number = 2*cverts.length/8;
		oloidGCSGC.setGeometry(generateOloidGeometry(number, false, false, 0));
		oloidBeyondCurve.setGeometry(generateOloidGeometry(number, true, false, 3));
		polarOloidGCSGC.setGeometry(generateOloidGeometry(number, false, true, 0));
		polarOloidToCurve.setGeometry(generateOloidGeometry(number, true, true, 0));
		polarOloidBeyondCurve.setGeometry(generateOloidGeometry(number, true, true, 1));
		return number;
	}
	

	private SceneGraphComponent generateOloidProper(double oloidGenParam)	{
		double foofoo = angle;
		// estimate number of segments needed
		double[][][] verts = cverts;
		if (oloidGenParam < 1.0) {
			int numSegs2 = (int) ((oloidGenParam) * (numSegs-2) +2);
			verts =  new double[numSegs2][2][]; 
		}
		for (int ctr = 0; ctr < verts.length; ++ctr)	{
			double a = oloidGenParam*ctr*Math.PI*2.0/verts.length;
			setA(a);
			a += dt;
			w2FixedEdge.getMatrix(m1);
			double[] im1 = Rn.inverse(null, m1);
			w2M1.getMatrix(m2);
			m2 = Rn.times(null, im1, m2);
			double[][]  tverts1 = Rn.matrixTimesVector(null, m2, schatzverts);
			w2M2.getMatrix(m3);
			m3 = Rn.times(null, im1, m3);
			double[][] tverts3 = Rn.matrixTimesVector(null, m3, schatzverts);
			verts[ctr][0] = tverts1[0];
			verts[ctr][1] = tverts3[2];			
		}
		setA(foofoo);
//		System.err.println("Oloid verts = \n"+Rn.toString(cverts));
		QuadMeshFactory qmf = new QuadMeshFactory();
		qmf.setClosedInUDirection(false);
		qmf.setClosedInVDirection(false);
		qmf.setULineCount(2);
		qmf.setVLineCount(verts.length);
		qmf.setVertexCoordinates(verts);
		qmf.setGenerateFaceNormals(true);
		qmf.setGenerateVertexNormals(true);
		qmf.setGenerateTextureCoordinates(true);
		qmf.update();
		Appearance ap1 = oloidSGC.getAppearance();
		ap1.setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		tex2d = (Texture2D) AttributeEntityUtility.createAttributeEntity(
				Texture2D.class, "polygonShader.texture2d", ap1, true);
		tex2d.setMinFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
		tex2d.setMagFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
		tex2d.setMipmapMode(true);
		// the following doesn't work -- rgba switches to agbr.  But it doesn't seem to matter
//		tex2d.setAnimated(true);
		Color transpblack = new Color(0,0,0,0),
				yellow = new Color(255, 255, 72);

		oloidTextureFactory.setType(SimpleTextureFactory.TextureType.GRAPH_PAPER); // LINE); //
		oloidTextureFactory.setColor(0, new Color(255, 255, 255, 0));
		oloidTextureFactory.setColor(0, transpblack);
		oloidTextureFactory.setColor(1, yellow);
		oloidTextureFactory.setColor(2, new Color(155,255,255));
		oloidTextureFactory.setColor(3,  yellow);
		oloidTextureFactory.setSize(512);
		oloidTextureFactory.setAppearance(ap1);
		oloidTextureFactory.update();
		tex2d.setImage(oloidTextureFactory.getImageData());
		Matrix m = new Matrix();
		MatrixBuilder.euclidean().scale(5, oloidGenParam*25, 1).assignTo(m);
		tex2d.setTextureMatrix(m);
		ap1.setAttribute("polygonShader.diffuseColor", Color.white);
		ap1.setAttribute(VERTEX_DRAW, false);
		oloidProper.setGeometry(qmf.getQuadMesh());
		return oloidProper;
	}
	private double epsilon = .0;

	private Texture2D tex2d;
	/**
	 * type:
	 *  0: surface to curve
	 *  1: surface beyond curve
	 *  2: surface beyond oloid (on other side)
	 */
	private Geometry generateOloidGeometry(int n, boolean qm, boolean polarize, int type) {
		double[][][] pts = new double[3][n][];	// for the surface: 3 variations
		double[][] curve = new double[n][];		// for the curve
		double[][][] texcoords = new double[3][n][3];
		int index0 = 0, //flipcoords ? 1 : 0,
				index1 = 2- index0;
		Matrix xtrans = MatrixBuilder.euclidean().translate(-.5,0,0).getMatrix(),
				ixtrans = MatrixBuilder.euclidean().translate(.5,0,0).getMatrix();;
		double[] xtrans5 = PlueckerLineGeometry.inducedP5ProjFromP3Proj(null, xtrans.getArray()),
				ixtrans5 = PlueckerLineGeometry.inducedP5ProjFromP3Proj(null, ixtrans.getArray());
		for (int i = 0; i<n; ++i)	{
			double t = AnimationUtility.linearInterpolation(i, 0, n-1.0,epsilon	, Math.PI/2.0+epsilon);
			if (symmetricParameter) t = t-.5*Math.asin(Math.sin(2*t)/3.0);

			// following code came from Mathematica and gives homogeneous coordinates 
			// for one eighth of the generating curve
			pts[index0][i] = new double[]{Math.cos(t), Math.sin(t), 0, 1};
			pts[index1][i] = new double[]{-1, 0, Math.sqrt(1 + 2*Math.cos(t)), 1+Math.cos(t)};
			double[] oloidLine = new double[] {
					   Math.sqrt(1 + 2*Math.cos(t))*Math.sin(t),-(Math.cos(t)*Math.sqrt(1 + 2*Math.cos(t))),Math.sin(t),
					   Math.sqrt(1 + 2*Math.cos(t)),(-1 - Math.cos(t))*Math.sin(t),-1 - Math.cos(t) - Math.pow(Math.cos(t),2)
			};
			oloidLine = PlueckerLineGeometry.permuteCoordinates(null, oloidLine, new int[]{3,0,1,2});
			oloidLine = PlueckerLineGeometry.dualizeLine(null, oloidLine);
			if (polarize) {
				oloidLine = Rn.matrixTimesVector(null, ixtrans5,oloidLine);
				oloidLine = PlueckerLineGeometry.polarize(null, oloidLine, Pn.HYPERBOLIC);
				oloidLine = Rn.matrixTimesVector(null, xtrans5,oloidLine);				
			}
			PointRangeFactory prf = new PointRangeFactory();
			prf.setPluckerLine(oloidLine);
			prf.setCenter(new double[]{-.5,0,0});	// coordinate system madness
			prf.setFiniteSphere(true);
			prf.setNumberOfSamples(2);
			prf.setSphereRadius(5.0);
			prf.update();
			double[][] pairOfPoints = prf.getVertices();
			double[] genCurve = null;
			if (polarize)	{
				// thanks to mathematica
				genCurve = new double[]{
					   4*(1 + Math.cos(t) + Math.cos(2*t)),(4. + 8.*Math.cos(t))*Math.sin(t),
					   4.*Math.pow(1 + 2*Math.cos(t),0.5)*
					    (1. + 2.*Math.cos(t)),
						1. + 7.*Math.pow(Math.cos(t),2) + 6.*Math.pow(Math.cos(t),3) + 
					    3.*Math.pow(Math.sin(t),2) + Math.cos(t)*(4. + 6.*Math.pow(Math.sin(t),2))
					    };
				genCurve = Rn.matrixTimesVector(null, xtrans.getArray(), genCurve);
				pts[1][i] = genCurve;
			}
			else {
			   genCurve = new double[]{(1)*(-4 - 9*Math.cos(t) + Math.cos(3*t))/(4.*Math.sqrt(1 + 2*Math.cos(t))),
			   -Math.pow(Math.sin(t),3)/Math.sqrt(1 + 2*Math.cos(t)),
			   -(-1 - 2*Math.cos(t)),
			   (6)*Math.pow(Math.cos(t/2.),2)*Math.cos(t)/Math.sqrt(1 + 2*Math.cos(t))};
			   pts[1][i] = Rn.times(null, .5, Rn.add(null, pts[0][i], pts[2][i]));
			}
			curve[i] = genCurve;
//			if (beyondOloid) Rn.times(genCurve, -1, genCurve);
			double d = Pn.distanceBetween(pts[index0][i], genCurve, Pn.EUCLIDEAN);
			if (qm) {
				if (!polarize) {
					// we always go from one of the two points on the oloid segment to the curve
					// the projective segment we choose is controlled by the sign of the w-coordinate of one of the endpoints
					switch(type)	{
					case 0:		// goes from pt1 to curve
					default:
						pts[index0][i] = curve[i];
						break;
					case 1:		// goes from curve to ideal plane
						pts[index1][i] = curve[i];
						Rn.times(pts[index0][i], -1, pts[index0][i]);
						break;
					case 2: 		// the line that extends from the oloid to the "other side" 
						pts[index1][i] = curve[i];
						Rn.times(genCurve, -1, genCurve);
						break;
					case 3: 		// ignore the surface, draw the segment 
						int i0 = index0, i1 = index1;
						if (i>0)	{
							double d0 = Pn.distanceBetween(pairOfPoints[index0], pts[index0][i-1], Pn.EUCLIDEAN),
									d1 = Pn.distanceBetween(pairOfPoints[index0], pts[index1][i-1], Pn.EUCLIDEAN);
							if (d1 < d0)	{  // flip the indices of the endpoints
								i0 = index1;
								i1 = index0;
							}
							pts[index0][i] = pairOfPoints[i0/2];
							pts[index1][i] = pairOfPoints[i1/2];				
						}
						break;
					}	

				}
				else {
					// make sure the endpoints of the "quadmesh" are consistent with one another
					int i0 = index0, i1 = index1;
					if (i>0)	{
						double d0 = Pn.distanceBetween(pairOfPoints[index0], pts[index0][i-1], Pn.EUCLIDEAN),
								d1 = Pn.distanceBetween(pairOfPoints[index0], pts[index1][i-1], Pn.EUCLIDEAN);
						if (d1 < d0)	{  // flip the indices of the endpoints
							i0 = index1;
							i1 = index0;
						}
					}
					pts[index0][i] = pairOfPoints[i0/2];
					pts[index1][i] = pairOfPoints[i1/2];				
				} 
			}
//			System.err.println(i+" d = "+d);
			texcoords[1][i][0] = texcoords[index0][i][0] = texcoords[index1][i][0] = 2*t/Math.PI;				
			if (polarize)	{
				texcoords[1][i][1] = 0;
				texcoords[index1][i][1] = .25*Pn.distanceBetween(pts[1][i], pts[index1][i], Pn.EUCLIDEAN);
				texcoords[index0][i][1] = -.25*Pn.distanceBetween(pts[1][i], pts[index0][i], Pn.EUCLIDEAN);				
			} else {
				texcoords[index0][i][1] = 0;
				texcoords[index1][i][1] = type == 2 ? 5 : d/Math.sqrt(3.0);
				texcoords[1][i][1] = (texcoords[index0][i][1]+texcoords[index1][i][1])/2;				
			}
		}
//		System.err.println("surface = \n"+Rn.toString(pts));
		if (!qm) {
			Pn.dehomogenize(curve, curve);
			return IndexedLineSetUtility.createCurveFromPoints(curve, false);
		}
		QuadMeshFactory qmf = new QuadMeshFactory();
		qmf.setULineCount(n);
		qmf.setVLineCount(3);
		qmf.setVertexCoordinates(pts);
		qmf.setGenerateEdgesFromFaces(true);
		qmf.setClosedInUDirection(false);
		qmf.setClosedInVDirection(false);
		qmf.setGenerateFaceNormals(true);
		if (polarize) {
			qmf.setGenerateTextureCoordinates(false);
			qmf.setVertexTextureCoordinates(texcoords);			
		}
		else
			qmf.setGenerateTextureCoordinates(true);
		qmf.update();
//		qmf.getIndexedFaceSet().setVertexCountAndAttributes(Attribute.TEXTURE_COORDINATES, 
//				StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(texcoords));
//		double[][] tc = qmf.getIndexedFaceSet().getVertexAttributes(Attribute.TEXTURE_COORDINATES).toDoubleArrayArray(null);
//		System.err.println("tex coords = "+Rn.toString(tc));
		return qmf.getIndexedFaceSet();
	}

}
