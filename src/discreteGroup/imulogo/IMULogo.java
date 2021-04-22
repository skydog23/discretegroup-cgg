/*
 * Created on Jan 29, 2004
 *
 */
package discreteGroup.imulogo;

import static de.jreality.shader.CommonAttributes.BACKEND_RETAIN_GEOMETRY;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LIGHTING_ENABLED;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.MANY_DISPLAY_LISTS;
import static de.jreality.shader.CommonAttributes.OPAQUE_TUBES_AND_SPHERES;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.RMAN_GLOBAL_INCLUDE_FILE;
import static de.jreality.shader.CommonAttributes.RMAN_OUTPUT_DISPLAY_FORMAT;
import static de.jreality.shader.CommonAttributes.RMAN_PROXY_COMMAND;
import static de.jreality.shader.CommonAttributes.RMAN_SEARCHPATH_SHADER;
import static de.jreality.shader.CommonAttributes.RMAN_SHADOWS_ENABLED;
import static de.jreality.shader.CommonAttributes.RMAN_TEXTURE_FILE;
import static de.jreality.shader.CommonAttributes.SPECULAR_EXPONENT;
import static de.jreality.shader.CommonAttributes.SPHERES_DRAW;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY_ENABLED;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import charlesgunn.anim.core.FramedCurve;
import charlesgunn.anim.sets.AnimatedDoubleSet;
import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.jreality.texture.RopeTextureFactory;
import charlesgunn.jreality.viewer.GlobalProperties;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.math.HomotopyFactory;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.PolygonalTubeFactory;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.renderman.RIBViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.util.CameraUtility;
import de.jreality.util.DefaultMatrixSupport;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.Secure;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.groups.PointGroup3S2;
import de.jtem.discretegroup.groups.TriangleGroup;
import de.jtem.discretegroup.util.WingedEdge;

/**
 * @author gunn
 *
 */
public class IMULogo extends LoadableScene {

	static String texHome = "http://page.math.tu-berlin.de/~gunn/Pictures/textures/";
	static String scratchDisk = "/Users/gunn/";//"/home/gunn/"; //
	public static double initialFocus = 6.0;
	public static double frontOfScreenFactor = 1.2;	// force stereo pair to appear in front of screen
	public static double eyeSeparationFactor = 12.0;	// eye separation defined as focus/eyeSeparationFactor
	public static double eyeSeparation = initialFocus / eyeSeparationFactor;
    public static double zfactor = 1.0;
	public static  double initialFOV = 30.0;
	public static  double initialZ = 6.0;
	static boolean nullNullFunf = true;
	static {
		String foo = Secure.getProperty("charlesgunn.texhome");
		if (foo != null) texHome = foo;
	        if (nullNullFunf)   {
	            initialFocus = 16.0;
	            eyeSeparation = .43;
	            eyeSeparationFactor = 16/eyeSeparation;
	            initialFOV = 15;
	            initialZ = 12.0;
	            zfactor = initialZ/6.0; //2.5; //
	        }
	}
	double magicNumber = Math.sqrt(2.0);
	double phi = Math.sqrt(5.0)/2.0 + .5;
	private  double pivotPathRadius = .01;
	SceneGraphComponent 
	// a primitive attempt to show the scene graph structure used
	toLogo,
	theWorld, 
		baSGCNoFiveFold,		// this has a 3-fold symmetry around (1,1,1)
			baSGC, 
			 	// ba3Fold[3]
					baSGCLocal,
						baSGCLogo,			// the final optimized form of the logo
	            		baSGCEvolution,		// contains curve evolution geometry
	            		baSGCGeometry,		// rigid rectangular geometry in two versions
							baSGCGeometry2Segments,		// one with only one pair of edges drawn
							baSGCGeometryLoop,			// the other with both
		polyhedralPart, 		// the basic jitterbug geometry
			DGRepn, 			// this contains the 3*2 group structure: 24 matrices
				pivotPaths, 	
				quadKit, 
					animatedTriangle,
						animatedTriangleFace, 
						animatedTriangleEdges,
		baSGCFiveFoldParent,
			// baSGCFiveFold[5]		// here is a 5-fold symmetry around (phi, 1, 0) (or permutation thereof)
				// baSGC			// see above for definition
		spaceDiagonalsSGC,
		creditsSurfaceSGC
		;
	WingedEdge we;
	PointGroup3S2 pointGroup3S3 ;
	TriangleGroup pointGroupS222;
	IndexedLineSet borromeanRectangleLoop = null, borromeanRectangle2Segments = null;
	IndexedLineSet borrCurveIFS = null;
	IndexedFaceSet animatedQuads = null;
	SceneGraphComponent[] bcAnim = null;
	SceneGraphComponent[] bc3Fold = null,
		bc3FoldTextured = new SceneGraphComponent[3],
		bc3FoldUntextured = new SceneGraphComponent[3],
		baSGCFiveFold = new SceneGraphComponent[5];
	BorromeanRingsKnot borrCurveFactory = new BorromeanRingsKnot();
	PolygonalTubeFactory logoTubeFactory = null;
	Texture2D[] ropeTexture2d = new Texture2D[3];
	Matrix originalMat = null;
	boolean automate = false,
		timerRunning = false,
		showBorromeanRects = false,
		showAnimatedPolyhedron = true,
		showEdgeLoops = false,
		showPivotPaths = false,
		playAnimation = false,
		showTexturesOnFaces = false,
		writeRenderman = false,
		rotateWorld = true, 
		shadowsEnabled = true,
		drawConvexHullEdges = true,
		useAnimationController = false,
		writeRmanTextures = true,
		stretchBackground = false,
		renderBackground = false,
		brunnianTorusRman = true,
		captureLoResLogo = true,
		PAL = true;
    RopeTextureFactory[] textureFactory = new RopeTextureFactory[3];
    Appearance[] texturedRingAps = new Appearance[3],
    	untexturedRingAps = new Appearance[3];
	FramedCurve finalMotion = null, evolveMotion = null;
    // global constants
	final String optimalRopeLengthRib = "ReadArchive "+ quote("tightRings.rib");
	final String optimalLogoRib = "ReadArchive "+quote("optimalLogo-02.rib");
	final double smallEdgeTubeRadius = .02, bigEdgeTubeRadius = .036;
	final double brunnianThickener = 1.8;
	final double logoTubeRadiusFactor = .315;
	final double moebiusZ = zfactor*7.0;
	final double tightZ = zfactor*8.0;
	final double logoFOV = zfactor*10.0;
	final double worldYRotation = 0.0;
	final double uStretch = 7, vStretch = 27; 
	final double bandWidth = .6,
		shadowWidth = .18,
		blendFactor = 0.0;
    Color band1color = new Color(1f, 1f, .8f),
    	shadowColor = new Color(0,0,0,255), 
		gapColor = new Color(0,0,0,0);
    
	double[] goldenAxis = new double[]//{0,1,-phi}; // this is the upper vertex of right, vertical edge
			{1,phi,0};		// this is the closer vertex of top, pointing-towards-viewer edge
			//-phi,0,1};			// this is the right vertex of the middle horizontal edge
	double bgndSX = 1.1;
	final Color URBackground = new Color(.8f, .85f, .68f); //new Color(215, 215, 190);
	final Color ULBackground  = new Color(1f, .98f, .8f); //new Color(255, 255, 200);  // bg[1];
	final Color LLBackground  = new Color(.1f, .1f, .25f); //new Color(20,20,60);
	final Color LRBackground  = new Color(0.05f, .15f, .35f); //new Color(25, 25, 100);  //bg[2];
	final private Color jitterbugEdgeColor =  new Color(.9f, .2f, .1f); //new Color(1f, 1f, .4f); 
	final double[] triangleColor =  {1, 1, .2, 1}; //{.9,.3,.1,1};  //{.4, 1, .3,1}; //
	final Color borromeanRectColor = new Color(1f, 1f, .5f); // new Color(1f, 1f, .95f); //new Color(.6f, 1f, .05f);
	final double rectangleAlpha = .9;
	final double[][] borromeanRectColors = {{1.0, 1.0, .5, rectangleAlpha}}; //{{1.0, 1.0, .95, .9}};

	final Color borromeanEdgeColor = new Color(.2f, 1f, .4f); // new Color(.2f, .8f, .6f); //new Color(.6f, 1f, .05f);
	final double[] squareColor = {.2, 1, .4, 1}; //{.1, .7, .5, 1}; //{.5,.8,.1,1};
	final double[] extraTriangleColor = squareColor; //{1, .9, 0}; //{.5, .5, 1};
	double[][] faceColors = {(double[])triangleColor.clone(), (double[])squareColor.clone()};
//	final Color[] borrColors3 = {new Color(150,0,255), new Color(255, 150, 0), new Color(150, 255, 0)};
	final Color[] borrColors3 = {new Color(.7f, .2f, .5f), new Color(1f, .8f, 0f), new Color(0f, .4f, .1f)};
	final Color stadiumColorSecondColor = new Color(1f, 1f, 1f);
	public static final Color[] logoColors = { new Color(.25f, .55f, .85f),new Color(.45f, .85f, 1f), new Color(.1f, .2f, .45f)};
	final double[] logoSpecular = {1, .8, .4};
	//{.45,.85,1.,1},{.25,.55,.85,.8},{.1,.2,.45,.4}
	final Color[] fiveFoldColors = { 
			borromeanEdgeColor,		// lime green
			new Color( .1f, .3f, 1f),  // blue
			new Color(.6f, .25f, 1f),	// purple
			new Color(1f, .2f, 0f),  
			new Color(1f, .7f, .1f)	// yellow
	};	
	

	private Timer timer;
	private IndexedFaceSetFactory borromean2SegmentFactory, borromeanLoopFactory;
	CreditsSurface 			creditsSurface = new CreditsSurface();
	Viewer viewer = null;	
	private PointSetFactory triangleVertexFactory;
	private Texture2D creditsTexture2D;
	Matrix creditsTMOrig = new Matrix();
	double[][] triangleVertex = new double[1][4];
	DiscreteGroupSceneGraphRepresentation sgr = null;
	boolean firstTime = true;

	public SceneGraphComponent makeWorld()	{
		if (firstTime)	{
//			borrCurveFactory.getCurve(2/3.0);
			System.err.println("stadium curve radius: "+stadiumCurveScale*borrCurveFactory.getRadius(2/3.0));
//			System.err.println("Stadium curve: "+BorromeanRingsKnot.coloredStadiumCurve(.5, Color.RED));
			theWorld = SceneGraphUtility.createFullSceneGraphComponent("theWorld");
			MatrixBuilder.euclidean().rotateX(Math.PI/8).rotateY(-Math.PI/10).assignTo(theWorld);
//			SLShader sls = new SLShader("ambientOcclusion2");
//			sls.addParameter("maxvariation", new Float(.0223));
//			sls.addParameter("coneangle", new Float(Math.PI/4));
//			theWorld.getAppearance().setAttribute(RMAN_SURFACE_SHADER, sls);
			
			originalMat = new Matrix(theWorld.getTransformation().getMatrix());
			quadKit = new SceneGraphComponent();
			pivotPaths = SceneGraphUtility.createFullSceneGraphComponent("pivot path");
			animatedTriangleFace = SceneGraphUtility.createFullSceneGraphComponent("animated face");
			animatedTriangleEdges = SceneGraphUtility.createFullSceneGraphComponent("animated edges");
			baSGC = SceneGraphUtility.createFullSceneGraphComponent("borromean axes");
		    baSGCLocal = new SceneGraphComponent();
		    baSGCLocal.setName("organizer");
		    baSGCGeometry = new SceneGraphComponent();
		    baSGCGeometry.setName("basgcRigid");
		    baSGCGeometry2Segments = new SceneGraphComponent();
		    baSGCGeometry2Segments.setName("basgcRigid2Segments");
		    Appearance ap = new Appearance();
		    ap.setAttribute("polygonShader.diffuseColor", new Color(1f, .8f, .8f));
		    baSGCGeometry2Segments.setAppearance(ap);
		    baSGCGeometryLoop = new SceneGraphComponent();
		    baSGCGeometryLoop.setName("basgcRigidLoop");
		    ap = new Appearance();
		    baSGCGeometryLoop.setAppearance(ap);
		    baSGCGeometry.addChild(baSGCGeometry2Segments);
		    baSGCGeometry.addChild(baSGCGeometryLoop);
		    baSGCEvolution = new SceneGraphComponent();
		    baSGCEvolution.setName("basgcEvolution");
		    bcAnim = new SceneGraphComponent[1];
			bcAnim[0] = new SceneGraphComponent();
			baSGCEvolution.addChild(bcAnim[0]);
		    baSGCLogo = new SceneGraphComponent();
		    baSGCLogo.setName("basgcLogo");
		    baSGCLocal.addChild(baSGCGeometry);
		    baSGCLocal.addChild(baSGCEvolution);
		    baSGCLocal.addChild(baSGCLogo);
		    for (int i = 0; i<3; ++i)	{
				bc3FoldTextured[i] = SceneGraphUtility.createFullSceneGraphComponent("threeFold"+i);
				MatrixBuilder.euclidean().rotate( i*2*Math.PI/3, new double[]{1,1,1}).assignTo(bc3FoldTextured[i]);
				bc3FoldUntextured[i] = SceneGraphUtility.createFullSceneGraphComponent("threeFold"+i);
				MatrixBuilder.euclidean().rotate( i*2*Math.PI/3, new double[]{1,1,1}).assignTo(bc3FoldUntextured[i]);
				baSGC.addChild(bc3FoldTextured[i]);
				baSGC.addChild(bc3FoldUntextured[i]);
				bc3FoldTextured[i].addChild(baSGCLocal);	
				bc3FoldUntextured[i].addChild(baSGCLocal);	
				untexturedRingAps[i] = bc3FoldUntextured[i].getAppearance();
				texturedRingAps[i] = bc3FoldTextured[i].getAppearance();
		    }
			baSGCNoFiveFold = SceneGraphUtility.createFullSceneGraphComponent("Borromean rings no five fold");
			baSGCNoFiveFold.setTransformation(null);
			baSGCNoFiveFold.addChild(baSGC);
			theWorld.addChild(baSGCNoFiveFold);
			polyhedralPart = new SceneGraphComponent();
			polyhedralPart.setName("polyhedral part");
			animatedTriangle = SceneGraphUtility.createFullSceneGraphComponent("animated triangle");
			animatedTriangle.setTransformation(null);
			quadKit.addChild(animatedTriangle);
			animatedTriangle.addChild(animatedTriangleFace);
//			animatedTriangle.addChild(animatedTriangleEdges);
			quadKit.addChild(pivotPaths);
			theWorld.addChild(polyhedralPart);
			baSGCFiveFoldParent = SceneGraphUtility.createFullSceneGraphComponent("Borromean rings five-fold parent");
			theWorld.addChild(baSGCFiveFoldParent);
			for (int j = 0; j<5; ++j)		{
				baSGCFiveFold[j] = SceneGraphUtility.createFullSceneGraphComponent("fiveFold"+j);
				baSGCFiveFold[j].addChild(baSGC);
				baSGCFiveFoldParent.addChild(baSGCFiveFold[j]);
			}
			// add space diagonals
			spaceDiagonalsSGC = SceneGraphUtility.createFullSceneGraphComponent("space diagonals");
			spaceDiagonalsSGC.getAppearance().setAttribute(VERTEX_DRAW, true);
			spaceDiagonalsSGC.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", 
					new Color(.3f, 1f, 0f));
			spaceDiagonalsSGC.getAppearance().setAttribute("pointShader.polygonShader.diffuseColor", 
					new Color(1f, 1f, 0f));
			spaceDiagonalsSGC.getAppearance().setAttribute("pointShader.pointRadius", .06);
			IndexedLineSet oneDiag = IndexedLineSetUtility.createCurveFromPoints(new double[][]{{1,1,1},{-1,-1,-1}}, false);
			MatrixBuilder.euclidean().scale((1+.03)*2/3.0).assignTo(spaceDiagonalsSGC);
			for (int i = 0; i<4; ++i)	{
				SceneGraphComponent sgc = new SceneGraphComponent();
				sgc.setName("diag"+i);
				sgc.setGeometry(oneDiag);
				MatrixBuilder.euclidean().rotateX(i*Math.PI/2).assignTo(sgc);
				spaceDiagonalsSGC.addChild(sgc);
			}
			theWorld.addChild(spaceDiagonalsSGC);
			setupTubeCrossSection();
			setupTriangleEdges();
			
			pointGroup3S3 = (PointGroup3S2) TriangleGroup.instanceOfGroup("3*2");
			pointGroup3S3.setConstrained(true);
			pointGroup3S3.setFaceColors(faceColors);
			
			pointGroupS222 = TriangleGroup.instanceOfGroup("*222");
			sgr = new DiscreteGroupSceneGraphRepresentation(pointGroupS222);
			sgr.setWorldNode(quadKit);
			sgr.update();
			DGRepn =  sgr.getRepresentationRoot();
			polyhedralPart.addChild(DGRepn);
			creditsSurface.update();
			creditsSurfaceSGC = SceneGraphUtility.createFullSceneGraphComponent("Credits");
			creditsSurfaceSGC.setGeometry(creditsSurface.getGeometry());
			creditsSurfaceSGC.getAppearance().setAttribute("lighting",false);

			MatrixBuilder.euclidean().scale(2.8).assignTo(creditsSurfaceSGC);
			ap = creditsSurfaceSGC.getAppearance();
			ap.setAttribute(LIGHTING_ENABLED, false);
			ap.setAttribute(TRANSPARENCY_ENABLED, true);
			creditsTexture2D = (Texture2D) AttributeEntityUtility
					       .createAttributeEntity(Texture2D.class, "polygonShader.texture2d", ap, true);
				try {
		  			ImageData id = ImageData.load(Input.getInput(this.getClass().getResource("abspannBold.png"))); //"grid256rgba.png")); //
		  			creditsTexture2D.setImage(id);
		    } catch (IOException e) {
		    		e.printStackTrace();
		    }		
//		    SimpleTextureFactory stf = new SimpleTextureFactory();
//		    stf.setType(TextureType.WEAVE);
//		    stf.update();
//		    creditsTexture2D.setImage(stf.getImageData());
			MatrixBuilder.euclidean().assignTo(creditsTMOrig);
		    creditsTexture2D.setExternalSource("Abspann");
		    creditsTexture2D.setTextureMatrix(creditsTMOrig);
		    creditsTexture2D.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
		    creditsTexture2D.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
		    theWorld.addChild(creditsSurfaceSGC);
			firstTime = false;
		}
		initializeAnimation();
		showAnimatedPolyhedron = true;
		animatedTriangle.setVisible(showAnimatedPolyhedron);
		return theWorld;
	}

	private void initializeAnimation() {
		theWorld.getAppearance().setAttribute(POLYGON_SHADER+"."+SPECULAR_EXPONENT, 60.0);
		theWorld.getAppearance().setAttribute(LINE_SHADER+"."+TUBES_DRAW, true);
		theWorld.getAppearance().setAttribute(POINT_SHADER+"."+SPHERES_DRAW, true);
		theWorld.getAppearance().setAttribute(LINE_SHADER+"."+TUBE_RADIUS, smallEdgeTubeRadius);
		theWorld.getAppearance().setAttribute(POINT_SHADER+"."+POINT_RADIUS, smallEdgeTubeRadius);
		theWorld.getAppearance().setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.WHITE);
		theWorld.getAppearance().setAttribute(LINE_SHADER+"."+
				POLYGON_SHADER+"."+DIFFUSE_COLOR, jitterbugEdgeColor);
		theWorld.getAppearance().setAttribute(POINT_SHADER+"."+
				POLYGON_SHADER+"."+DIFFUSE_COLOR, jitterbugEdgeColor);

		pivotPaths.getAppearance().setAttribute(VERTEX_DRAW, false);
		pivotPaths.getAppearance().setAttribute(LINE_SHADER+"."+TUBES_DRAW, true);
		pivotPaths.getAppearance().setAttribute(LINE_SHADER+"."+TUBE_RADIUS, pivotPathRadius);
		pivotPaths.getAppearance().setAttribute(LINE_SHADER+"."+POLYGON_SHADER
				+"."+DIFFUSE_COLOR, new Color(255, 200, 0));

		Appearance ap = animatedTriangleFace.getAppearance();
		ap.setAttribute(VERTEX_DRAW, true);
		ap.setAttribute(EDGE_DRAW, true);
		ap.setAttribute(FACE_DRAW,true);
		if (showTexturesOnFaces)	{
			   Texture2D tex2d = (Texture2D) AttributeEntityUtility
		       .createAttributeEntity(Texture2D.class, "polygonShader.texture2d", ap, true);		
		  		try {
		  			ImageData id = ImageData.load(Input.getInput(texHome+"weave.png")); //"grid256rgba.png")); //
		  			tex2d.setImage(id);
		    } catch (IOException e) {
		      e.printStackTrace();
		    }			
			ap.setAttribute(RMAN_TEXTURE_FILE, "weaveFromPNG.tex");
		}
		ap = animatedTriangleEdges.getAppearance();
		ap.setAttribute(VERTEX_DRAW, true);
		ap.setAttribute(EDGE_DRAW, false);
		ap.setAttribute(FACE_DRAW, false);
//		 SLShader whitted = new SLShader("whitted");
//		 whitted.addParameter("Kd",new Double(1.0));
//		 whitted.addParameter("eta", new Double(1.3));
//		 ap.setAttribute(POLYGON_SHADER,"free");
//		 ap.setAttribute(RMAN_SURFACE_SHADER, whitted);

		for (int i = 0; i<3; ++i)		{
//			bc3Fold[i].setAppearance(null);
//			ap = texturedRingAps[i];
////			if (ap == null) continue;
//			ap.setAttribute("pointShader.polygonShader.diffuseColor",Appearance.INHERITED);
//			ap.setAttribute("lineShader.polygonShader.diffuseColor",Appearance.INHERITED);
//			ap.setAttribute("polygonShader.diffuseColor",Appearance.INHERITED);
			ap = bc3FoldUntextured[i].getAppearance();
//			if (ap == null) continue;
			ap.setAttribute("polygonShader", "default");
			ap.setAttribute("pointShader.polygonShader.diffuseColor",Appearance.INHERITED);
			ap.setAttribute("lineShader.polygonShader.diffuseColor",Appearance.INHERITED);
			ap.setAttribute("polygonShader.diffuseColor",Appearance.INHERITED);
			ap.setAttribute("transparency",Appearance.INHERITED);
			ap = bc3FoldTextured[i].getAppearance();
//			ap = new Appearance();
			textureFactory[i] = new RopeTextureFactory(ap);
			textureFactory[i].setN(Math.sqrt(2)*uStretch);
			textureFactory[i].setM(Math.sqrt(2)*vStretch);
			textureFactory[i].setAngle(Math.PI/4);
			textureFactory[i].setBand1color(band1color);
			textureFactory[i].setBand2color(borrColors3[i]);
			textureFactory[i].setShadowcolor(shadowColor);
			textureFactory[i].setGapcolor(gapColor);
			textureFactory[i].setBlendcolor(borrColors3[i]);
			textureFactory[i].setBandwidth(bandWidth);
			textureFactory[i].setShadowwidth(shadowWidth);
			textureFactory[i].setBlendfactor(blendFactor);
			textureFactory[i].update();
			ropeTexture2d[i] = textureFactory[i].getTexture2D();
//			ropeTexture2d[i] = tu[i].makeTextureAppearance(ap, ropeTexture2d[i], uStretch, vStretch, Math.PI/4,
//		    		bandWidth, shadowWidth, blendFactor, 
//		    		band1color, borrColors3[i], shadowColor, gapColor, borrColors3[i]);
//			ap.setAttribute("polygonShader.textureMatrix",MatrixBuilder.euclidean().scale(uStretch, vStretch, 1.0).rotateZ(Math.PI/4).getArray());
			if (!writeRmanTextures){
				ap.setAttribute("band1color",band1color);
				ap.setAttribute("band2color",borrColors3[i]);	
				ap.setAttribute("blendfactor",blendFactor);	
				ap.setAttribute("blendcolor", Color.WHITE);	
				ap.setAttribute("shadowcolor",shadowColor);	
				ap.setAttribute("gapcolor",gapColor);	
				ap.setAttribute("gapalpha", gapColor.getAlpha());
				ap.setAttribute("polygonShader", "rope");
			} 
			bc3FoldTextured[i].setVisible(false);
		}
		animatedTriangle.getAppearance().setAttribute(MANY_DISPLAY_LISTS, false);
//		theWorld.addChild(baSGC);
		for (int j = 0; j<5; ++j)		{
			MatrixBuilder.euclidean().rotate(j*2*Math.PI/5, goldenAxis).assignTo(baSGCFiveFold[j]);
			ap = baSGCFiveFold[j].getAppearance();
			ap.setAttribute("lineShader.polygonShader.diffuseColor",fiveFoldColors[j]);
			ap.setAttribute("pointShader.pointRadius",Appearance.INHERITED);	
			ap.setAttribute("pointShader.pointRadius",Appearance.INHERITED);	
			ap.setAttribute(MANY_DISPLAY_LISTS, true);
			ap.setAttribute("lineShader.polygonShader.diffuseColor",Appearance.INHERITED);
			ap.setAttribute("pointShader.polygonShader.diffuseColor",Appearance.INHERITED);					
			baSGCFiveFold[j].setVisible(false);
		}
		showEdgeLoops = false;
		borromean2SegmentFactory = null;
		borromeanLoopFactory = null;
//		borromeanRectColors[0][3] = 1;
		borromeanRectColors[0][3] = rectangleAlpha;
		updateBorromeanRectangles();
		showBorromeanRects = false;
		showPivotPaths = false;
		showAnimatedPolyhedron = false;
		animatedTriangle.setVisible(showAnimatedPolyhedron);
		pivotPaths.setVisible(showPivotPaths);
		baSGCNoFiveFold.setVisible(showBorromeanRects);
		baSGCEvolution.setVisible(false);
		baSGCLogo.setVisible(false);
		baSGCLocal.setVisible(true);
		spaceDiagonalsSGC.setVisible(false);
		baSGCGeometry.setVisible(true);
		baSGCGeometryLoop.setVisible(false);
		baSGCGeometry2Segments.setVisible(true);
		baSGCGeometryLoop.getAppearance().setAttribute(FACE_DRAW, false);
		creditsSurfaceSGC.setVisible(false);
		
		jitterbugAtTime(0.0);
		// the remaining attributes, etc., should probably be set in "cleanUp" methods on 
		// the animation segment that changed the values
		faceColors[1][3] = 0.0;  // default is transparent
		faceColors[0][3] = 1;	// default is opaque
		pointGroup3S3.setFaceColors(faceColors);
		drawConvexHullEdges = true;
//		baSGCNoFiveFold.setAppearance(new Appearance());
		baSGCNoFiveFold.getAppearance().setAttribute(FACE_DRAW,Appearance.INHERITED);
		baSGCNoFiveFold.getAppearance().setAttribute(EDGE_DRAW,Appearance.INHERITED);
		baSGCNoFiveFold.getAppearance().setAttribute(VERTEX_DRAW,Appearance.INHERITED);
		baSGCNoFiveFold.getAppearance().setAttribute(TRANSPARENCY, Appearance.INHERITED);
		baSGCGeometry2Segments.getAppearance().setAttribute(TRANSPARENCY, Appearance.INHERITED);
		baSGCGeometry2Segments.getAppearance().setAttribute(FACE_DRAW,Appearance.INHERITED);
		baSGCGeometry2Segments.getAppearance().setAttribute(VERTEX_DRAW,Appearance.INHERITED);
		baSGCGeometry2Segments.getAppearance().setAttribute(TRANSPARENCY, Appearance.INHERITED);
		baSGCGeometryLoop.getAppearance().setAttribute(TRANSPARENCY, Appearance.INHERITED);
		baSGCGeometryLoop.getAppearance().setAttribute(FACE_DRAW,Appearance.INHERITED);
		baSGCGeometryLoop.getAppearance().setAttribute(VERTEX_DRAW,Appearance.INHERITED);
		borromeanRectangleLoop.setGeometryAttributes(RMAN_PROXY_COMMAND, null);
		baSGCNoFiveFold.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor",Appearance.INHERITED );
		baSGCNoFiveFold.getAppearance().setAttribute("polygonShader.diffuseColor",Appearance.INHERITED);
		baSGCNoFiveFold.getAppearance().setAttribute("lineShader.tubeRadius", Appearance.INHERITED);
		baSGCNoFiveFold.getAppearance().setAttribute("pointShader.pointRadius",Appearance.INHERITED);
		baSGCFiveFoldParent.getAppearance().setAttribute(TRANSPARENCY, Appearance.INHERITED);
		animatedTriangle.getAppearance().setAttribute(TRANSPARENCY,  Appearance.INHERITED);
		animatedTriangle.getAppearance().setAttribute(EDGE_DRAW,Appearance.INHERITED);
		baSGCLocal.setTransformation(null);
	   
		if (viewer != null) 
			CameraUtility.getCameraNode(viewer).getTransformation().setMatrix(originalCamera);
		baSGCNoFiveFold.getAppearance().setAttribute(BACKEND_RETAIN_GEOMETRY, false);
		if (viewer != null) CameraUtility.getCameraNode(viewer).getTransformation().setMatrix(originalCamera);
		theWorld.getTransformation().setMatrix(Rn.times(null, 
				originalMat.getArray(),
				P3.makeRotationMatrixY(tmp,wrYC[animationSegment])));
		System.err.println("Setting world rotation to "+((wrYC[animationSegment]*180/3.14159)));
		for (int i = 0; i<numberAnimationSegments; ++i)	{
			System.err.println("Segment rotation: "+wrY[i]);
			System.err.println("Cumulative rotation: "+wrYC[i]);
		}
	}

	private void setupTriangleEdges() {
		triangleVertexFactory = new PointSetFactory();
		triangleVertexFactory.setVertexCount(triangleVertex.length);
		triangleVertexFactory.setVertexCoordinates(triangleVertex);
		triangleVertexFactory.update();
		animatedTriangleEdges.setGeometry(triangleVertexFactory.getPointSet());
	}


	double[][][] circle = new double[3][][];
	private void setupTubeCrossSection() {
		int size = 12;
		for (int j = 0; j<3; ++j)	{
			circle[j] = new double[size][3];
			for (int i = 0; i<size; ++i)	{
				double angle = i*Math.PI*2.0/(size-1.0);
				circle[j][i][0] = Math.cos(angle);
				circle[j][i][1] = Math.sin(angle);
				circle[j][i][2] = 0.0;
			}	
			size += 12;
		}
	}


	int numPoints = 30;
	// this number is derived from the fact that the golden rectangles we use in the first 
	// scenes are derived from the jitterbug motion of a rigid triangle. 

	double[][] jitterbugEdgeVerts = new double[4][4];
	int[][] jitterbugSegmentIndices1 = {{0,1},{2,3}}; 
	int[][] jitterbugSegmentIndices2 = {{0,3},{1,2}}; 
	int[][] jitterbugLoopIndices = {{0,1,2,3,0}};
	int[][] jitterbugFaceIndices = {{0,1,2,3}};
;
	private void updateBorromeanRectangles() {
//		double[][] edgeColors = (showEdgeLoops) ? edgeColorsLoops : edgeColorsSegments;
		if (borromean2SegmentFactory == null)	{
			borromean2SegmentFactory = new IndexedFaceSetFactory();
			borromean2SegmentFactory.setVertexCount(jitterbugEdgeVerts.length);
			borromean2SegmentFactory.setVertexCoordinates(jitterbugEdgeVerts);	
			borromean2SegmentFactory.setFaceCount(1);
			borromean2SegmentFactory.setFaceIndices(jitterbugFaceIndices);	
			borromean2SegmentFactory.setFaceColors(borromeanRectColors);
			borromean2SegmentFactory.setGenerateFaceNormals(true);
			borromean2SegmentFactory.setEdgeCount( jitterbugSegmentIndices1.length);
			borromean2SegmentFactory.setEdgeIndices( jitterbugSegmentIndices1);
//			ilsf.setEdgeColors(edgeColors);
	//		System.err.println("Borrom edge loops is "+showEdgeLoops);
			borromean2SegmentFactory.update();
			borromeanRectangle2Segments = borromean2SegmentFactory.getIndexedLineSet();			
			baSGCGeometry2Segments.setGeometry(borromeanRectangle2Segments);
		}
		if (borromeanLoopFactory == null)	{
			borromeanLoopFactory = new IndexedFaceSetFactory();
			borromeanLoopFactory.setVertexCount(jitterbugEdgeVerts.length);
			borromeanLoopFactory.setVertexCoordinates(jitterbugEdgeVerts);	
			borromeanLoopFactory.setFaceCount(1);
			borromeanLoopFactory.setFaceIndices(jitterbugFaceIndices);	
			borromeanLoopFactory.setFaceColors(borromeanRectColors);
			borromeanLoopFactory.setGenerateFaceNormals(true);
			borromeanLoopFactory.setEdgeCount(jitterbugLoopIndices.length);
			borromeanLoopFactory.setEdgeIndices(jitterbugLoopIndices);
			borromeanLoopFactory.update();
			borromeanRectangleLoop = borromeanLoopFactory.getIndexedLineSet();		
			baSGCGeometryLoop.setGeometry(borromeanRectangleLoop);
		}
	}
	

	public void customize(JMenuBar theMenuBar, final Viewer v) {
		JMenu testM = new JMenu("Actions");
		JCheckBoxMenuItem jca = new JCheckBoxMenuItem("Pause/Unpause");
		jca.setSelected(automate);
		testM.add(jca);
		jca.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0));
		jca.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				automate = !automate;
				pointGroup3S3.setConstrained(automate);
				if (!automate) {
					timer.stop();
				} else {
					timer.start();
				}
			}
		});
		jca = new JCheckBoxMenuItem("Stop/Start");
		jca.setSelected(automate);
		testM.add(jca);
		jca.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.SHIFT_DOWN_MASK));
		jca.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				automate = !automate;
				pointGroup3S3.setConstrained(automate);
				if (!automate) {
					timer.stop();
				} else {
					timerCounter = 0;
					timer.start();
				}
			}
		});

		jca = new JCheckBoxMenuItem("Toggle render background");
		testM.add(jca);
		jca.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0));
		jca.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				renderBackground = !renderBackground;
				viewer.getSceneRoot().getAppearance().setAttribute(RMAN_OUTPUT_DISPLAY_FORMAT, renderBackground ? "rgb" : "rgba");
			}
		});

		JMenuItem pb= new JMenuItem("one frame forward");
		testM.add(pb);
		pb.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_9,0));
		pb.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				localFrameCount++;
				doCurrentFrame();
				System.err.println("Current frame is "+timerCounter);
			}
		});
		
		pb= new JMenuItem("one frame back");
		testM.add(pb);
		pb.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.SHIFT_DOWN_MASK));
		pb.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				localFrameCount--;
				doCurrentFrame();
				System.err.println("Current frame is "+timerCounter);
			}
		});

		pb= new JMenuItem("toggle renderman writing");
		testM.add(pb);
		pb.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, 0));
		pb.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				writeRenderman = !writeRenderman;
				rendermanWritePath = null;
				System.err.println("Writing renderman is "+writeRenderman);
			}
		});
		theMenuBar.add(testM);
		initializeViewer(v);

	}

    double timerCounter = 0, localFrameCount = 0;
	public void initializeViewer(Viewer v) {
		GlobalProperties.saveResourceDir = "/Users/gunn/Movies/IMULogo/ribs/";
//		JOGLConfiguration.localScratchDisk = "/home/gunn/"; 
		viewer = v;
//		viewer.getSceneRoot().getAppearance().setAttribute(BACKGROUND_COLOR, new Color(10,10,200));
		viewer.getSceneRoot().getAppearance().setAttribute(OPAQUE_TUBES_AND_SPHERES, false);
		viewer.getSceneRoot().getAppearance().setAttribute(RMAN_SHADOWS_ENABLED, shadowsEnabled);
		viewer.getSceneRoot().getAppearance().setAttribute(RMAN_OUTPUT_DISPLAY_FORMAT, renderBackground ? "rgb" : "rgba");
		viewer.getSceneRoot().getAppearance().setAttribute(RMAN_SEARCHPATH_SHADER, "/homes/geometer/gunn/Documents/Movies/ICMLogo/shaders");
		viewer.getSceneRoot().getAppearance().setAttribute(RMAN_GLOBAL_INCLUDE_FILE, "quality.rib");
		backgroundArray = new Color[4];
		backgroundArray[0] = URBackground;
		backgroundArray[1] = ULBackground;// bg[1];
		backgroundArray[2] = LLBackground;
		backgroundArray[3] = LRBackground;  //bg[2];
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColors", backgroundArray);

		Camera cam = CameraUtility.getCamera(viewer);
		cam.setPerspective(true);
		cam.setNear(1.0);
		cam.setFar(30.0);
		DefaultMatrixSupport.getSharedInstance().storeAsDefault(CameraUtility.getCameraNode(viewer).getTransformation());
		SceneGraphUtility.setMetric(theWorld, pointGroup3S3.getMetric());
//		charlesgunn.jreality.newtools.ToolManager.toolManagerForViewer(viewer).activateTool(charlesgunn.jreality.newtools.ToolManager.ROTATION_TOOL);
		cam.setFieldOfView(.7*initialFOV);
//		CameraUtility.encompass(viewer);
		cam.setFieldOfView(initialFOV);
		setCameraDistance(initialZ);
		if (stretchBackground)	{
			double e = cam.getEyeSeparation()/2.0;
			double f = cam.getFocus();
			double d = f*Math.tan(Math.PI*cam.getFieldOfView()/360.0);
			double F = cam.getFar();
			bgndSX = 1+e*(F-f)/(d*F);
			viewer.getSceneRoot().getAppearance().setAttribute("backgroundColorsStretchX", bgndSX);
			System.err.println("constant = "+bgndSX );			
		}
		DefaultMatrixSupport.getSharedInstance().storeDefaultMatrices(viewer.getSceneRoot());
		originalCamera = CameraUtility.getCameraNode(viewer).getTransformation().getMatrix();
		System.err.println("original cam: "+Rn.matrixToString(originalCamera));
		System.err.println("original eyes and foc: "+cam.getEyeSeparation()+": "+cam.getFocus());
		cam.setEyeSeparation(frontOfScreenFactor*eyeSeparation);
		cam.setFocus(frontOfScreenFactor*initialFocus);
//		FactoredMatrix fm = new FactoredMatrix();
		GlobalProperties.saveResourceDir = "/homes/geometer/gunn/Documents/Movies/ICMLogo/ribs/";
		initializeAnimatedDoubleSets();
		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		
		timer = new Timer(1000/fps, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doCurrentFrame();
				localFrameCount += speedup;
			}
			
			
		});

		((Component) ( viewer).getViewingComponent()).addKeyListener( new KeyAdapter()	{
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode())	{
				
				case KeyEvent.VK_V:
					if (e.isShiftDown()) break;
					animationSegment = (animationSegment+1) % numberAnimationSegments;
					timer.stop();
					automate = false;
					localFrameCount = 0;
					for (int i = 0; i<animationSegment; ++i)	
						localFrameCount += totalFrames[i];
					System.err.println("Version is "+animationNames[animationSegment]);
					System.err.println("Time is "+localFrameCount/25.0);
					break;
	
				case KeyEvent.VK_4:
					if (e.isShiftDown()) speedup/= 2;
					else speedup *= 2;
					System.err.println("Speed up is "+speedup);
					break;
				}
			}	
			
		});

	}
	
	public boolean addBackPlane() {return false; }
	public boolean isEncompass() {return false; }

	boolean rotate = false;
	int oldTimes = 0;
	private void jitterbugAtTime(double angle) {
		int times = 0;
		if (rotate)		{
			times = (int) (angle/(Math.PI));
			double done = times * Math.PI;
			double frac = angle-done;
			if (frac > Math.PI/2) angle  = frac - Math.PI/2;
			times = times % 3;
			oldTimes = times;
		}
		double c = Math.cos(angle);
		double s = Math.sin(angle);
		// set the centerpoint along the arc between (1,0,0,1) and (0,1,0,1)
		double[] cp = {s,c,0,1};
		pointGroup3S3.setConstrained(true);
		pointGroup3S3.setCenterPoint(cp);

		animatedQuads = (IndexedFaceSet) TriangleGroup.getSplitFundamentalRegion(pointGroup3S3, animatedQuads);
		IndexedFaceSet bar = convert3S3ToS222(animatedQuads);
		double[][] verts = bar.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
//		System.err.println("coords = "+Rn.toString(verts));
		animatedTriangleFace.setGeometry(bar);
		cp = pointGroup3S3.getCenterPoint();
		double x =cp[0],y = cp[1],z = cp[2];
		if (cp[0] == 0 && cp[2] != 0)	{
			x = cp[1]; y = cp[2]; z = cp[0];
		} 
		for (int i = 0; i<1; ++i)	{
			for (int j = 0; j<4; ++j)	{
				int sign0 = (((j+1)%4)/2)%2 == 1 ? -1 : 1;
				int sign1 = (j/2)%2 == 1 ? -1 : 1;
				int k = (3*j)%4;
				jitterbugEdgeVerts[i*4+k][(i)%3] = sign0 *x;
				jitterbugEdgeVerts[i*4+k][(i+1)%3] = sign1 * y;
				jitterbugEdgeVerts[i*4+k][(i+2)%3] = sign0 *z;
				jitterbugEdgeVerts[i*4+k][3] = 1.0;
			}
		}
		// the following code forces the drawn edge to be on the convex hull of the polyhedron
		if (drawConvexHullEdges && !showEdgeLoops)
			if (x<y) {
				double[] foo = jitterbugEdgeVerts[1];
				jitterbugEdgeVerts[1] = jitterbugEdgeVerts[3];
				jitterbugEdgeVerts[3] = foo;
			}
		borromean2SegmentFactory.setVertexCoordinates(jitterbugEdgeVerts);
		borromean2SegmentFactory.update();
		borromeanLoopFactory.setVertexCoordinates(jitterbugEdgeVerts);
		borromeanLoopFactory.update();
		
		triangleVertex[0] = jitterbugEdgeVerts[0];
		triangleVertexFactory.setVertexCoordinates(triangleVertex);
		triangleVertexFactory.update();
	}

	
	private IndexedFaceSet convert3S3ToS222(IndexedFaceSet animatedQuads2) {
		double[][] verts = animatedQuads2.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		double[][] nv = new double[6][4];
		for (int i = 0; i<3; ++i)	{
			for (int j = 0; j<3; ++j) {
				nv[i][j] = verts[0][(i+j)%3];
				nv[3+i][j] = verts[3][(i+j)%3];
			}
			nv[3+i][3] = nv[i][3] = 1.0;
		}
		double[][] cc = animatedQuads2.getFaceAttributes(Attribute.COLORS).toDoubleArrayArray(null);
		double[][] nc = new double[4][];
		nc[0] = cc[0]; nc[1] = nc[2] = nc[3] = cc[1];
		int[][] indices = {{0,1,2}, {0,3,1},{1,4,2},{2,5,0}};
		int[][] edges = {{0,1},{1,2},{2,0}};
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(6);
		ifsf.setVertexCoordinates(nv);
		ifsf.setFaceCount(4);
		ifsf.setFaceIndices(indices);
		ifsf.setFaceColors(nc);
		ifsf.setEdgeCount(3);
		ifsf.setEdgeIndices(edges);
//		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		IndexedFaceSet foo = ifsf.getIndexedFaceSet();
		foo.setVertexAttributes(Attribute.INDICES, StorageModel.INT_ARRAY.createReadOnly(
				new int[]{1,1,1,0,0,0}));

		return foo;
	}

	public SceneGraphComponent makeLights() {
		SceneGraphComponent lightNode = new SceneGraphComponent();
		lightNode.setName("lights");
		SceneGraphComponent l0 = SceneGraphUtility.createFullSceneGraphComponent("light0");
		DirectionalLight dl = new DirectionalLight();
		dl.setColor(new Color(250, 225, 200));
		dl.setIntensity(.4);
		double[] zaxis = {0,0,1};
		double[] other = {.8,1.2,1};
		l0.getTransformation().setMatrix( P3.makeRotationMatrix(null, zaxis, other));
		System.err.println("Light0 position is "+Rn.toString(Rn.matrixTimesVector(null,l0.getTransformation().getMatrix(), zaxis)));
		l0.setLight(dl);
		lightNode.addChild(l0);
				
		dl = new DirectionalLight();
		dl.setColor(new Color(250, 250, 250));
		dl.setIntensity(.6);
		l0 = SceneGraphUtility.createFullSceneGraphComponent("light1");
//		double[] other2 = {-.6,-.2,.5};
//		l0.getTransformation().setMatrix( P3.makeRotationMatrix(null, zaxis, other2));
		l0.setLight(dl);
		lightNode.addChild(l0);
		l0.getAppearance().setAttribute(RMAN_SHADOWS_ENABLED, false);
		System.err.println("Light1 position is "+Rn.toString(Rn.matrixTimesVector(null,l0.getTransformation().getMatrix(), zaxis)));
		
		
		return lightNode;
	}

	final static int JBUG_NO_STOP = 0;
	final static int JBUG_STOP		= 1;
	final static int FIVE_FOLD		= 2;
	final static int BRUNNIAN		= 3;
	final static int EVOLVE_MOEBIUS	= 4;
	final static int EVOLVE			= 5;
	final static int TO_LOGO		= 6;
	final static int CREDITS 		= 7;
	int animationSegment = 0;
	String[] animationNames = {
			"jitterbugNoStops",
			"jitterbugStops",
			"fiveFold", 
			"brunnian",
			"evolveMoebius",
			"evolveLink",
			"linkToLogo",
			"credits"};
	int numberAnimationSegments = animationNames.length;
	AnimatedDoubleSet[] ads = new AnimatedDoubleSet[numberAnimationSegments];
	double[] animatedFrames = new double[numberAnimationSegments];
	double[] transitionFrames = new double[numberAnimationSegments];
	double[] totalFrames = new double[numberAnimationSegments];
	double[] wrY = new double[numberAnimationSegments],
		wrYC = new double[numberAnimationSegments+1];;
	int fps = 25;
	// one degree per second
	final double dt = (1.0/6)*Math.PI/fps;	
	double previous = -1;
    double[] mat = P3.makeRotationMatrixY(null,.35*dt);
	PolygonalTubeFactory ptf = null;
	int previousAnimationSegment = -1;
	double fraction = 0.0;
	double cumulativeWorldAngle = 0.0;
	public void setValueAtTime(double t)	{
		double time = t % numberAnimationSegments;
		animationSegment = (int) time;
		fraction = time % 1.0;
		if (animationSegment < 0) animationSegment = 0;
		if (animationSegment >= numberAnimationSegments) {
			animationSegment = numberAnimationSegments-1;
			fraction = 1-10E-8;
		}
		timerCounter = fraction * totalFrames[animationSegment];
		System.err.println("setAnimationTime:"+animationSegment+":"+timerCounter);
		useAnimationController = true;
		doCurrentFrame();
	}
	double speedup = 1;
	private void doCurrentFrame() {
		if (!useAnimationController)	{
			double xxx = localFrameCount; // % totalFrameCount;
			if (xxx > totalFrameCount) {
				timer.stop();
				return;
			}
			
			for (animationSegment = 0; animationSegment < numberAnimationSegments; animationSegment++){
				if (xxx < totalFrames[animationSegment])	{
					fraction = xxx/totalFrames[animationSegment];
					break;
				}
				xxx -= totalFrames[animationSegment];
			}
//			time = time % numberAnimationSegments;
//			animationSegment = (int) time;
//			double fraction = time % 1.0;
//			while (totalFrames[animationSegment] == 0) {
//				animationSegment = (animationSegment+1)%numberAnimationSegments;
//				time = animationSegment;
//			}
			if (animationSegment == numberAnimationSegments) {
				timer.stop();
				return;
			}
			timerCounter = fraction * totalFrames[animationSegment];
			System.err.println(animationSegment+":"+timerCounter);	
		}
		if (previousAnimationSegment != animationSegment)	{
			initializeAnimation(animationSegment);
			previousAnimationSegment = animationSegment;
		}
		
		switch(animationSegment)	{
		case JBUG_NO_STOP:
			jitterbugAnimationWithoutStops();
			break;
		case JBUG_STOP:
			jitterbugAnimationWithStops();
			break;
		case BRUNNIAN:
			brunnianAnimation();
			break;
		case FIVE_FOLD:
			fiveFoldAnimation();
			break;
		case EVOLVE_MOEBIUS:
			evolveMoebiusAnimation();
			break;
		case EVOLVE:
			evolveLinkAnimation();
			break;
		case TO_LOGO:
			linkToLogoAnimation();
			break;
		case CREDITS:
			creditsAnimation();
			break;
		}
		viewer.render();
//		if ( (timerCounter % skipFrames[quality]) != 0) return;
		if (writeRenderman)	{
			writeRenderman();
		}
		
		
	}
	RIBViewer ribViewer = null;
	int dirCount = 0;
	double[] jawosDKeys = {0, 
			fps,		// octahedron, still
			6.5*fps, 		// jitterbug, no movement, no space diags.
			0*fps, 		// octahedron, fade in space diags
			6.5*fps, 		// jitterbug, no movement, no space diags.
			0*fps, 		// octahedron, fade in space diags
			6.5*fps, 		// jitterbug with rotation and space diags.
			0*fps};		// fade out space diags
	 // world rotation
	double[] jawosYRotDKeys = {0,6.5*fps, 13*fps, 1*fps};
	double[] jawosYRotValues = {0, 0, wrY[JBUG_NO_STOP], wrY[JBUG_NO_STOP]};
	double[] jawosJbugDKeys = jawosDKeys;
	// jitterbug values
	double[] jawosJbugValues = { Math.PI/2, Math.PI/2, 0,  0,  Math.PI/2, Math.PI/2, 0, 0};
	double[][] jawosDKeySets = {jawosJbugDKeys, jawosYRotDKeys};
	double[][] jawosValueSets = {jawosJbugValues, jawosYRotValues};
	private void jitterbugAnimationWithoutStops()	{
		double timerCounter2 = timerCounter;
		values = ads[JBUG_NO_STOP].getValuesAtTime(timerCounter2, values);
		double t = AnimationUtility.hermiteInterpolation(timerCounter2, 6.5*fps, 19.5*fps, wrYC[JBUG_NO_STOP],wrYC[JBUG_NO_STOP+1]);
		if (rotateWorld)	
			theWorld.getTransformation().setMatrix(Rn.times(null, 
					originalMat.getArray(),
					P3.makeRotationMatrixY(tmp, t)));
//		if (rotateWorld) theWorld.getTransformation().setMatrix(Rn.times(null, 
//				originalMat.getArray(),
//				P3.makeRotationMatrixY(tmp, values[0])));
		System.err.println("Jbug is "+values[0]);
		jitterbugAtTime(values[0]);
	}
	
	double icoAngle = .553574;
	double pause = (int) (fps* (85.0/25.0));
	double opacPause = (int) (fps * (20.0/25.0));
	private static final double ico2cubo = 3;
	double[] jbugDKeys = {0, 	// start
			4*fps, 				// time to first icosa
			pause, 			// hold first icosa
			ico2cubo*fps, 			// time to cubuocta
			pause, 			// hold cubocta
			ico2cubo*fps, 			// time to second icosa
			pause,			// hold second icosa
//			2*fps,			// time to final octa
//			0,			// hold final octa
//			2*fps,			// back to icosa
			pause,			// hold edge
			ico2cubo*fps,			// back to cubocta
			pause,			// hold cubocta edge
			ico2cubo*fps,			// back to icosa,
			pause			// hold icosa long edge
		};
	// WARNING: Following two values have to be kept uptodate with jbugDKeys!
	double toReversal = 3*pause + (4+2*ico2cubo)*fps -20;  // == 20.2 s
	double afterReversal = 3*pause + (2*ico2cubo)*fps +20;  // = 16.2
	double[] jbugValues = {0,  
			icoAngle, 
			icoAngle, 
			Math.PI/4, 
			Math.PI/4, 
			Math.PI/2-icoAngle, 
			Math.PI/2-icoAngle, 
//			Math.PI/2, 
//			Math.PI/2,
//			Math.PI/2-icoAngle, 
			Math.PI/2-icoAngle, 
			Math.PI/4, 
			Math.PI/4, 
			icoAngle, 
			icoAngle,
			};
	double[] reverseDKeys = { 0, toReversal,  afterReversal};
	double[] reverseValues = {0, 0,  1};
	int magicN = (int) (fps * (462.0)/25.0);
	double[] showEdgeDKeys = { 0, magicN, opacPause, toReversal+afterReversal-magicN-opacPause};
	double[] showEdgeValues = {0, 0, 1, 1};
	int[] whichColor = {0, 0, 0,0, 1,1,1,
			//1,1, 0, 
			0, 0, 0, 0, 0};
	double[] opacDKeys = {0, 		
			jbugDKeys[1], 			
			opacPause,				
			jbugDKeys[2]-2* opacPause,
			opacPause,
			jbugDKeys[3],
			opacPause,
			jbugDKeys[4]-2*opacPause,
			opacPause,
			jbugDKeys[5],
			opacPause,
			jbugDKeys[6]-2*opacPause,
			opacPause,
//			jbugDKeys[7],
//			jbugDKeys[8],
//			jbugDKeys[7],
			opacPause, 
			jbugDKeys[6]-2*opacPause,
			opacPause,
			jbugDKeys[5],
			opacPause,
			jbugDKeys[4]-2*opacPause,
			opacPause,
			jbugDKeys[3],
			opacPause,
			jbugDKeys[2]-2* opacPause,
			opacPause
			};
	double[] opacValues = {0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 
			0, 0, 0, 0,0, 0, 0, 0, 0, 0, 0, 0};
	double[] opacEdgeValues = {0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 
			1, 1, 1, 1,1, 1, 1, 1, 1, 1, 1,1};
	double[][] dkeySets = {jbugDKeys, opacDKeys, reverseDKeys, opacDKeys, showEdgeDKeys};
	double[][] valueSets = {jbugValues, opacValues, reverseValues, opacEdgeValues, showEdgeValues};
	double[] values = new double[valueSets.length];
	boolean reversed = false, previousReversed = false,
		showEdges = false, previousShowEdges = false,
		reachedEnd = false;
	// the following segment draws the jitterbug with stops for the special values where
	// the resulting polyhedron has extra symmetry: icosahedron and cuboctahedron.
	// it's complicated by the fact that the new faces belong to the animatedTriangle branch of the
	// scene graph (coming from the 3*2 group) while the edges come from baSGCNoFiveFold branch (C3 group)
	boolean atEnd = false;
	double jbugRotateFrames = 8 *fps;			
	double oneTrans = 3*fps;
	double fadeInShortEdges = oneTrans;
	double fadeInRectangle = oneTrans;
	double fadeOutTriangle = oneTrans;		
	double jbugStopTransitionFrames = jbugRotateFrames + 3*oneTrans;		// 17
	private void jitterbugAnimationWithStops()	{
		double timerCounter2 = (timerCounter); //% (totalFrames);
		if (timerCounter2 < animatedFrames[JBUG_STOP])	{
			values = ads[JBUG_STOP].getValuesAtTime(timerCounter2, values);
			reversed = (values[2] != 0.0);
			if (reversed != previousReversed)	{
				if (reversed)	{
					drawConvexHullEdges = false;
				} else {
					drawConvexHullEdges = true;
				}
				previousReversed = reversed;
			}
			
				if ( values[4] > 0.0 && !reachedEnd) {
					if (values[4] >= 1.0) reachedEnd = true;
					double rad = AnimationUtility.hermiteInterpolation(smallEdgeTubeRadius, bigEdgeTubeRadius, values[4]);
					Color color = AnimationUtility.linearInterpolation(jitterbugEdgeColor, fiveFoldColors[0], values[4]);
					baSGCNoFiveFold.getAppearance().setAttribute("lineShader.tubeRadius", rad);
					baSGCNoFiveFold.getAppearance().setAttribute("pointShader.pointRadius", rad);
					baSGCNoFiveFold.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor",color);
					baSGCNoFiveFold.getAppearance().setAttribute("pointShader.polygonShader.diffuseColor",color);
					baSGCNoFiveFold.setVisible(true);
				}
			jitterbugAtTime(values[0]);
			baSGCGeometry2Segments.getAppearance().setAttribute(TRANSPARENCY, 1.0 - values[3]);
			double[] foo = null;
			showBorromeanRects = false;
			if (!reversed &&  Math.abs(values[0] - Math.PI/4) > 10E-2) {
				foo = extraTriangleColor;
				showBorromeanRects = true;		// turns on the edges
			} else {
				foo = squareColor;
			}
			baSGCNoFiveFold.setVisible(reversed || showBorromeanRects);
//			if (!reversed)	{
				for (int j=0;j<3;++j) faceColors[1][j] = foo[j];				
				faceColors[1][3] = values[1];
//			}
			return;
		} 
		timerCounter2 -= animatedFrames[JBUG_STOP];
		if (timerCounter2 < jbugRotateFrames)	{
			double angle = AnimationUtility.hermiteInterpolation(timerCounter2, 0, jbugRotateFrames, wrYC[JBUG_STOP], wrYC[JBUG_STOP+1]);
			if (rotateWorld)	
				theWorld.getTransformation().setMatrix(Rn.times(null, 
						originalMat.getArray(),
						P3.makeRotationMatrixY(tmp, angle)));
			if (timerCounter2 < fadeOutTriangle)	{
				double fade = AnimationUtility.hermiteInterpolation(timerCounter2, 0, fadeOutTriangle, 0, 1);
				faceColors[0][3] = 1 - fade;
				jitterbugAtTime(values[0]);
			}
			return;
		}
		timerCounter2 -= jbugRotateFrames;
		if (timerCounter2 < fadeInShortEdges)	{
			double fade = AnimationUtility.linearInterpolation(timerCounter2, 0, fadeInShortEdges, 0, 1);
			baSGCGeometryLoop.getAppearance().setAttribute(VERTEX_DRAW,true);
			baSGCGeometryLoop.setVisible(true);
			baSGCGeometryLoop.getAppearance().setAttribute(TRANSPARENCY, 1.0-fade);
			return;
		} 
		timerCounter2 -= fadeInShortEdges;
		if (timerCounter2 < fadeOutTriangle)	{
			double fade = AnimationUtility.linearInterpolation(timerCounter2, 0, fadeOutTriangle, 0, 1);
//			baSGCGeometry2Segments.setVisible(false);
			animatedTriangle.getAppearance().setAttribute(TRANSPARENCY, fade);
			System.err.println("Fade factor is "+fade);
			return;
		}
		timerCounter2 -= fadeOutTriangle;
		if (timerCounter2 < fadeInRectangle)	{
			if (!drawConvexHullEdges){
				drawConvexHullEdges = true;
				jitterbugAtTime(icoAngle);				
			}
			double fade = AnimationUtility.linearInterpolation(timerCounter2, 0, fadeInRectangle, 0, 1);
			baSGCNoFiveFold.getAppearance().setAttribute(FACE_DRAW, true);
			borromean2SegmentFactory = null;
			borromeanLoopFactory = null;
			borromeanRectColors[0][3] = rectangleAlpha*fade;
			// this actually creates a new copy	 of the rectangle geometry.
			updateBorromeanRectangles();
			baSGCGeometryLoop.getAppearance().setAttribute(FACE_DRAW, false);
			baSGCGeometryLoop.getAppearance().setAttribute(TRANSPARENCY, fade);
			return;
		} 
	}


	double pause1 = fps;
	double rotatex = 60, expand = 20, hold = 0, contract = 20, pausex = 15;
	double[] radiusDKeys = {0, 	
			expand,			// expand, change color
			hold,			// hold new size and color
			contract,		// deflate but hold color
			pausex,			// pause
			rotatex,			// rotate to new position (1/5 turn)
			expand,			// expand, change color
			hold,			// hold new size and color
			contract,		// deflate but hold color
			pausex,			// pause
			rotatex,			// rotate to new position (1/5 turn)
			expand,			// expand, change color
			hold,			// hold new size and color
			contract,		// deflate but hold color
			pausex,			// pause
			rotatex,			// rotate to new position (1/5 turn)
			expand,			// expand, change color
			hold,			// hold new size and color
			contract,		// deflate but hold color
			pausex,			// pause
			rotatex,			// rotate to new position (1/5 turn)
			expand,			// expand, change color
			hold,			// hold new size and color
			contract		// deflate but hold color
	};
	double fadeOutLongEdges = expand + hold + contract;		// done now at beginning of five-fold
	double smallRad = bigEdgeTubeRadius+.001,
			bigRad = 2.5*bigEdgeTubeRadius;
	double[] radiusValues = {
			          smallRad, bigRad, bigRad, smallRad,				// expand, hold, shrink, pause
			smallRad, smallRad, bigRad, bigRad, smallRad,	// rotate, expand, hold, shrink, pause
			smallRad, smallRad, bigRad, bigRad, smallRad,
			smallRad, smallRad, bigRad, bigRad, smallRad,
			smallRad, smallRad, bigRad, bigRad, smallRad,
			};
	int totalPhaseFrames = (int) (radiusDKeys[1]+radiusDKeys[2]+radiusDKeys[3]+radiusDKeys[4]+radiusDKeys[5]);
	int colorChangeFrames = (int) (radiusDKeys[1]+radiusDKeys[2]+radiusDKeys[3]);
	int totalRotatingFrames = (int) (4*totalPhaseFrames + colorChangeFrames);
	double[] angleDKeys = {0, colorChangeFrames, 
			radiusDKeys[4], radiusDKeys[5], colorChangeFrames,
			radiusDKeys[4], radiusDKeys[5], colorChangeFrames,
			radiusDKeys[4], radiusDKeys[5], colorChangeFrames,
			radiusDKeys[4], radiusDKeys[5], colorChangeFrames
			};
	double fifth = Math.PI*2.0/5.0;
	double[] angleValues = {0, 0, 
			0, fifth, fifth,
			fifth, 2*fifth, 2*fifth,
			2*fifth, 3*fifth, 3*fifth,
			3*fifth, 4*fifth, 4*fifth};
	
	double[] phaseDKeys = {0, colorChangeFrames, 
			0, totalPhaseFrames,
			0, totalPhaseFrames,
			0, totalPhaseFrames,
			0, totalPhaseFrames
			};
	double[] phaseValues = {0, 0, 
			1, 1,
			2,2,
			3,3,
			4,4
			};
	
	double[] colorMixDKeys = radiusDKeys;
	double[] colorMixValues = { 
			    1, 0, 0, 0,
			1, 1, 0, 0, 0, 
			1, 1, 0, 0, 0, 
			1, 1, 0, 0, 0, 
			1, 1, 0, 0, 0
			};
	double[][] dkey4Sets = {angleDKeys, radiusDKeys, colorMixDKeys, phaseDKeys};
	double[][] value4Sets = {angleValues, radiusValues, colorMixValues, phaseValues};
	double[] tmp = new double[16];
	double[] originalCamera = null;
	double rotateIcosa = 5 * fps;
	double fadeOutIcosa = 2 * fps;
	double fadeInRect = 3 * fps;
	double fadeInRingColors = 5 *fps;
	double fiveFoldTransitionFrames =  rotateIcosa + fadeOutIcosa  +fadeInRect + fadeInRingColors;
	double fiveFoldFrameCount = ((int) animatedFrames[FIVE_FOLD])+ fiveFoldTransitionFrames;
	private void fiveFoldAnimation()	{
		double timerCounter2 = (timerCounter); // % ((int) key3Sets[0][key3Sets[0].length-1]);
		if (timerCounter2 < animatedFrames[FIVE_FOLD]){
			double subCounter = (timerCounter2);
			values = ads[FIVE_FOLD].getValuesAtTime(subCounter, values);
			int phase = (int) (values[3]);
			baSGCNoFiveFold.getAppearance().setAttribute(FACE_DRAW,false);
			baSGCFiveFold[phase].setVisible(true);
			baSGCFiveFold[phase].getAppearance().setAttribute(VERTEX_DRAW, true);	
//			if (phase == 0)	{
//				double fade = AnimationUtility.hermiteInterpolation(timerCounter2, 0, fadeOutLongEdges, 0, 1);
//				System.err.println("Fade factor is "+fade);
//				baSGCGeometryLoop.getAppearance().setAttribute(TRANSPARENCY, fade);
//			}
			if (phase > 0)  {
				baSGCFiveFold[phase-1].getAppearance().setAttribute(FACE_DRAW, false);	
//					baSGCFiveFold[phase-1].getAppearance().setAttribute("pointShader.pointRadius",bigEdgeTubeRadius*1.001);	
				baSGCFiveFold[phase-1].getAppearance().setAttribute("pointShader.polygonShader.diffuseColor",borromeanRectColor);	
//					baSGCFiveFold[phase-1].getAppearance().setAttribute(VERTEX_DRAW, false);						
			}
			Appearance ap = baSGCFiveFold[phase].getAppearance();
			MatrixBuilder.euclidean().rotate(values[0], goldenAxis).assignTo(baSGCFiveFold[phase]);
			ap.setAttribute("lineShader.tubeRadius", values[1]);
			ap.setAttribute("pointShader.pointRadius", values[1]);
			if (phase > 0)	{
				Color mix = AnimationUtility.linearInterpolation(fiveFoldColors[phase], borromeanRectColor, values[2]);
				ap.setAttribute("lineShader.polygonShader.diffuseColor",mix);
				ap.setAttribute("pointShader.polygonShader.diffuseColor",mix);					
			}
			return;
		}
		timerCounter2 -= totalRotatingFrames;
		if (timerCounter2 < rotateIcosa){
			// fade out the rectangle
			double fade = AnimationUtility.hermiteInterpolation(timerCounter2, 0, 1*fps,  0,1);
			if (fade > 1) fade = 1;
			for (int i = 0; i<3; ++i)	{
				borromean2SegmentFactory = null;
				borromeanLoopFactory = null;
				borromeanRectColors[0][3] = rectangleAlpha*(1-fade);
				// this actually creates a new copy	 of the rectangle geometry.
				updateBorromeanRectangles();
			}
			double t = AnimationUtility.hermiteInterpolation(timerCounter2, 1*fps, rotateIcosa, wrYC[FIVE_FOLD],wrYC[FIVE_FOLD+1]);
			if (rotateWorld) theWorld.getTransformation().setMatrix(Rn.times(null, 
						originalMat.getArray(),
						P3.makeRotationMatrixY(tmp, t)));
			return;
		}
		timerCounter2 -= rotateIcosa;
		baSGCNoFiveFold.setVisible(true);
		baSGCGeometry.setVisible(true);			
		baSGCFiveFold[4].getAppearance().setAttribute(FACE_DRAW, false);
		baSGCNoFiveFold.getAppearance().setAttribute(VERTEX_DRAW,true);
		baSGCNoFiveFold.getAppearance().setAttribute(FACE_DRAW,false);
		// fade out the icosahedron
		if (timerCounter2 < fadeOutIcosa)	{
			double fade = (timerCounter2)/(fadeOutIcosa-1.0);
			if (fade > 1) fade = 1;
			baSGCFiveFoldParent.getAppearance().setAttribute(TRANSPARENCY,  fade);
			baSGCGeometryLoop.getAppearance().setAttribute(TRANSPARENCY, fade);
			return;
		}
		timerCounter2 -= fadeOutIcosa;
		baSGCFiveFoldParent.setVisible(false);
		baSGCGeometryLoop.setVisible(true);
		baSGCGeometryLoop.getAppearance().setAttribute(VERTEX_DRAW,true);
		baSGCGeometry2Segments.getAppearance().setAttribute(FACE_DRAW, false);	
		baSGCNoFiveFold.getAppearance().setAttribute(FACE_DRAW,true);
		if (timerCounter2 < fadeInRect)	{
			if (borromeanRectColors[0][3] != rectangleAlpha)
				for (int i = 0; i<3; ++i)	{
					borromean2SegmentFactory = null;
					borromeanLoopFactory = null;
					borromeanRectColors[0][3] = rectangleAlpha;
					// this actually creates a new copy	 of the rectangle geometry.
					updateBorromeanRectangles();
				}
			double fade = (timerCounter2)/(fadeInRect-1.0);
			if (fade > 1) fade = 1;
			baSGCGeometryLoop.getAppearance().setAttribute(TRANSPARENCY, 1.0-fade);
			return;
		}
		timerCounter2 -= fadeInRect;
		baSGCGeometry2Segments.setVisible(false);
		if (timerCounter2 < fadeInRingColors)	{
			double fade = (timerCounter2)/(fadeInRingColors-1.0);
			if (fade > 1)fade = 1;
			double edgeRadius = AnimationUtility.linearInterpolation(fade, 0, 1, 1, brunnianThickener);
			for (int i = 0; i<3; ++i)	{
				Appearance ap = untexturedRingAps[i];
				Color mix = AnimationUtility.linearInterpolation(borromeanEdgeColor, borrColors3[i], fade);
				ap.setAttribute("lineShader.polygonShader.diffuseColor",mix);
				ap.setAttribute("pointShader.polygonShader.diffuseColor",mix);
				ap.setAttribute("lineShader.tubeRadius", edgeRadius*bigEdgeTubeRadius);
				ap.setAttribute("pointShader.pointRadius", edgeRadius*bigEdgeTubeRadius);
		}
			return;
		}
	}

	// go from the three linked rectangles
	double[] brunOpac1DKeys = {0, 
			1.5*fps, 	// fade out one ring
			2*fps, 	// move apart the two remaining
			2*fps,		// move them back
			1.5*fps,	// fade in the ring
			0*fps,	// hold
			1.5*fps,		// fade out second
			2*fps,
			2*fps,
			1.5*fps	,
			1*fps		// final hold
			};	
	double oneCycle = 7*fps;
	double[] brunOpac1Values = {1, 0,  0,   0, 1,      1,      0,0,0,1,1};
	double[] brunTrans1Values = {0, 0, 2.0, 0, 0,    0,     0,  2.0, 0, 0, 0};
	double[] translate = new double[16];
	double[] tvec1 = {0,1,0};
	double[] tvec2 = {1,0,0};
	double[][] brunDKeys = {brunOpac1DKeys, brunOpac1DKeys};
	double[][] brunValues = {brunOpac1Values, brunTrans1Values};
	int[] brunInterp = {1, 2};
	public void brunnianAnimation()	{
		values = ads[BRUNNIAN].getValuesAtTime(timerCounter, values);
		if (timerCounter <= oneCycle) {
			bc3Fold[0].getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, 1-values[0]);
			MatrixBuilder.euclidean().translate(Rn.times(null, values[1], tvec2)).rotate(4*Math.PI/3.0, 1, 1, 1).assignTo(bc3Fold[2]);
			return;
		}
		bc3Fold[1].getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, 1-values[0]);
		MatrixBuilder.euclidean().translate(Rn.times(null, values[1], tvec1)).rotate(0*Math.PI/3.0, 1, 1, 1).assignTo(bc3Fold[0]);
	}

	double stadiumCurveScale = (2+2*phi)/(4+2*(Math.PI)); //Math.sqrt(2)/2;
	String stadiumCurveRib = 
		"TransformBegin\n" +
		"Rotate 90 1 0 0\n"+
		"Scale "+stadiumCurveScale+" "+stadiumCurveScale+" "+stadiumCurveScale+"\n"+
		"TransformBegin\n" +
		"Translate 1 0 0\n"+
		"Cylinder .25 -1 1 360\n" +
		"TransformEnd\n" +
		"TransformBegin\n" +
		"Translate -1 0 0\n"+
		"Cylinder .25 -1 1 360\n" +
		"TransformEnd\n" +
		"TransformBegin\n" +
		"Rotate 90 1 0 0\n"+
		"TransformBegin\n" +
		"Translate 0 1 0\n"+
		"Torus 1 .25 0 360 180\n" +
		"TransformEnd\n" +
		"TransformBegin\n" +
		"Translate 0 -1 0\n"+
		"Rotate 180 0 0 1\n"+
		"Torus 1 .25 0 360 180\n" +
		"TransformEnd\n"+
		"TransformEnd\n"+
		"TransformEnd";
	
	int evolveFrameCount = 26*fps;
	int totalEvolutionCurves = 8*fps;

	double evolveMobFrameCount = 17*fps;
//	double evolveMobiusFrameCount = totalEvolutionCurves + 10*fps;
	double mobGrowTime = 5*fps;
	double[] mobAllDKeys = {0, 
			mobGrowTime, 		// evolve to stadium curve with half-size radius
			1*fps, 				// hold
			2*fps, 				// shorten cylinders
			2*fps,				// lengthen
			evolveMobFrameCount - 5*fps - mobGrowTime};
	double[] mobCameraZDKeys = {0,mobGrowTime};
	double[] mobCameraZValues = {initialZ, moebiusZ};
//	double[] mobFadeOutValues = {0, 0, 0, 1, 1, 1, 0, 0};
//	double[] mobFadeInCylColorValues = {0, 0, 0, 0, .5, .5, 0, 0};
	double[] mobCylRadValues = {1,  1, 1, 0.3, 1, 1};
//	double[] mobTorusRadValues = {1, 1, 1,1.5, 1, 1, 1, 1};
//	double[] mobFadeInTransparencyValues = {0, 0, 0, 0, 1, 1};
	double[][] mobDKeys = {mobAllDKeys, mobCameraZDKeys};
	double[][] mobValues = {mobCylRadValues, mobCameraZValues};
	int[] mobInterp = {1, 1};
	double[] myvalues = null;
	double curveLengthFudgeFactor = 1.0;
	private void evolveMoebiusAnimation()	{
		myvalues = ads[EVOLVE_MOEBIUS].getValuesAtTime(timerCounter, myvalues);
		if (timerCounter <  mobGrowTime) {
			curveLengthFudgeFactor = 1 + (timerCounter/mobGrowTime) *.01523;
			getLinkAtTime(2/3.0 * (timerCounter/mobGrowTime));
		} else {
			curveLengthFudgeFactor = 1.0;
			bcAnim[0].getGeometry().setGeometryAttributes(RMAN_PROXY_COMMAND,
						stadiumCurveRib);				
			if (myvalues[0] != 1.0)	{
//				Color mix = AnimationUtility.linearInterpolation(borrColors3[0],stadiumColorSecondColor, myvalues[2]);
//				untexturedRingAps[1].setAttribute("lineShader.polygonShader.diffuseColor",mix);
//				untexturedRingAps[1].setAttribute("pointShader.polygonShader.diffuseColor",mix);
				double cylR = myvalues[0];
				double torR = 1.0; //myvalues[4];
				bcAnim[0].getGeometry().setGeometryAttributes(RMAN_PROXY_COMMAND,
						//BorromeanRingsKnot.coloredStadiumCurve(stadiumCurveScale,cylR, torR, mix ));	
					BorromeanRingsKnot.stadiumCurve(stadiumCurveScale,cylR, torR ));	
			} 
		}
		setCameraDistance(myvalues[1]);
		double angle = AnimationUtility.hermiteInterpolation(timerCounter, mobGrowTime, animatedFrames[EVOLVE_MOEBIUS], wrYC[EVOLVE_MOEBIUS], wrYC[EVOLVE_MOEBIUS+1]);
		if (rotateWorld) theWorld.getTransformation().setMatrix(Rn.times(null, 
					originalMat.getArray(),
					P3.makeRotationMatrixY(tmp,angle)));

		// a stand-in for the final version which should show the parts of the stadium curve
//		double t = AnimationUtility.linearInterpolation(timerCounter, (2/3.0)*totalEvolutionCurves, evolveMobiusFrameCount-2*fps, 0, 1);
//		if (t < .5) t *= 2;
//		else if (t >= .5) t = 2 * (1-t);
//		baSGCNoFiveFold.getAppearance().setAttribute(TRANSPARENCY, t);
	}
	
	void setCameraDistance(double d)	{
		CameraUtility.getCameraNode(viewer).getTransformation().setMatrix(
				P3.makeTranslationMatrix(null, new double[]{0,0,d},Pn.EUCLIDEAN));
		CameraUtility.getCamera(viewer).setFocus(frontOfScreenFactor*d);
		CameraUtility.getCamera(viewer).setEyeSeparation(frontOfScreenFactor*d/eyeSeparationFactor);
	}
	double[] bandwidthDKeys = {0, 
			totalEvolutionCurves, 		// optimal shape
			0*fps,					// leave alone
			2*fps,					// shrink 1 bandwidth
			3*fps,					// hold
			2.4*fps,					// shrink 2nd bandwidth
			2.2*fps,					// hold
			2.4*fps,					// bring back one to default
			1*fps,					// hold
			3*fps,					// bring back second to default
			evolveFrameCount - 16*fps-totalEvolutionCurves};
	double[] bandwidthValues = {1, 1,1,  0, 0, 0,0, 0, 0,1,1};
	double[] bandwidth2Values = {1, 1,1, 1, 1, 0,0, 1,1, 1, 1};
	double[] radDKeys = {0, totalEvolutionCurves};
	double[] radValues = {0, 1};
	double[] blendDKeys = {20, totalEvolutionCurves/2};
	double[] blendValues = {0, 1};
	double[] evlCameraZDKeys = {0, totalEvolutionCurves*.7, 404-totalEvolutionCurves*.7, evolveFrameCount-404};
	double[] evlCameraZValues = {initialZ, tightZ, (PAL ? 1.1 : 1.0)*tightZ, tightZ};
	double[] evlYRotDKeys = {0,evolveFrameCount-fps, fps};
	double[] evlYRotDValues = {0,0, fps*Math.PI/180.0};
	double[][] evlDKeys = {bandwidthDKeys, bandwidthDKeys, radDKeys, evlCameraZDKeys, evlYRotDKeys, blendDKeys};
	double[][] evlValues = {bandwidthValues, bandwidth2Values, radValues,  evlCameraZValues, evlYRotDValues, blendValues};
	int[] evlInterps = {1,1,1, 1, 1, 1};		// linear!
	double globalRadiusScale = 1.0;
	boolean needsMatrix = true;
	double[] tmat = null;
	private void evolveLinkAnimation()	{
//		if (timerCounter < totalEvolutionCurves) timerCounter = totalEvolutionCurves;
		values = ads[EVOLVE].getValuesAtTime(timerCounter, values);
//		double timerCounter2 = timerCounter % (evolveFrameCount);
//		if (rotateWorld) 
//			theWorld.getTransformation().setMatrix(Rn.times(null, 
//					originalMat.getArray(),
//					P3.makeRotationMatrixY(tmp,angle)));
		if (values[4] != 0)	{
			if (needsMatrix)	{
				tmat = theWorld.getTransformation().getMatrix();
				needsMatrix = false;
			}
			theWorld.getTransformation().setMatrix(Rn.times(null, 
					tmat,
					P3.makeRotationMatrixY(tmp, values[4])));
			System.err.println("Angle is "+values[4]*180/Math.PI);
		} else {
			needsMatrix = true;
			double angle = AnimationUtility.linearInterpolation(timerCounter, totalEvolutionCurves, evolveFrameCount-fps, 0, 1);
			double[] tmat = evolveMotion.getValueAtTime(angle).getArray();
			theWorld.getTransformation().setMatrix(tmat);
		}

//		if (values[2] != tightZ) 
		// TODO why aren't stereo parameters adjusted here?
		setCameraDistance(values[3]);
		System.err.println("val3 = "+values[3]);
		for (int i = 0; i<3; ++i)						{
				Color mix = AnimationUtility.linearInterpolation( borrColors3[i], band1color, values[5]);
				Color mix2 = AnimationUtility.linearInterpolation( borrColors3[i], gapColor,values[5]);
				Color mix3 = AnimationUtility.linearInterpolation( borrColors3[i], shadowColor,values[5]);
				texturedRingAps[i].setAttribute("band2color",mix);	// "blendcolor"
//				texturedRingAps[i].setAttribute("blendfactor",1-t);	
				double bw = bandWidth;
				if (i != 0) {
					bw = bandWidth*values[i-1];
				}
				if (bw == 0) bc3FoldTextured[i].setVisible(false);
				else bc3FoldTextured[i].setVisible(true);
				textureFactory[i].setBandwidth(bw);
				textureFactory[i].setBlendfactor(0);
				textureFactory[i].setBand1color(mix);
				textureFactory[i].setShadowcolor(mix3);
				textureFactory[i].setGapcolor(mix2);
				textureFactory[i].setBlendcolor(Color.WHITE);
				textureFactory[i].update();
//				ropeTexture2d[i] = tu[i].makeTextureAppearance(texturedRingAps[i], ropeTexture2d[i], uStretch, vStretch, Math.PI/4,
//			    		bw, shadowWidth, 0, mix,borrColors3[i], mix3, mix2, Color.WHITE);
		}			
//		System.err.println("Time is: "+t);
		if (previous == values[2]) return;
//		double t = (which)/(totalEvolutionCurves-1.0);
		// avoid sharp corners in the tubes by adjusting radius downward for first phase of evolution
		curveLengthFudgeFactor = 1;
		getLinkAtTime(values[2]);
//		else bcAnim[which].setVisible(true);
		previous = values[2];
	}

	private void getLinkAtTime(double t) {
		double fudge =  AnimationUtility.linearInterpolation(t, 0, 1.0/3.0, 1, 0);
		double shrinkTubeFactor = fudge * .9 + (1-fudge) * 1.0;
		//borrCurveFactory.setCurrentTime(t);
		BorromeanEvolutionDescriptor bed = borrCurveFactory.getCurveDescriptor(t, globalRadiusScale);
		double[][] theCurve  = bed.getCurve(); //borrCurveFactory.getCurve(t);
//		if (curveLengthFudgeFactor != 1.0)
//			for (int i =0;i<theCurve.length; ++i)	{
//				Rn.times(theCurve[i], curveLengthFudgeFactor, theCurve[i]);
//			}

		System.err.println("Curve length: "+HomotopyFactory.getLength(theCurve, borrCurveFactory.htf.isClosed()));
		PolygonalTubeFactory ptf = new PolygonalTubeFactory(theCurve);
		double d = shrinkTubeFactor  * bed.getTubeRadius()*bed.getScaleFactor(); //borrCurveFactory.getRadius(t);
//		if (d < bigEdgeTubeRadius) d = bigEdgeTubeRadius;
		int resolution = 0;
		if (d < .1) resolution = 0;
		else if (d < .2) resolution = 1;
		else resolution = 2;
		ptf.setCrossSection(circle[resolution]);
		ptf.setRadius(d); //edgeTubeRadius);
//		System.err.println("Radius is "+borrCurveFactory.getRadius(t));
		ptf.setGenerateTextureCoordinates(true);
		ptf.setArcLengthTextureCoordinates(true);
		ptf.update();
		IndexedFaceSet tube = ptf.getTube();

//		if (t == 1) tube.setGeometryAttributes(RMAN_PROXY_COMMAND,optimalRopeLengthRib);
//		else 	tube.setGeometryAttributes(RMAN_PROXY_COMMAND,null);
		tube.setGeometryAttributes(RMAN_PROXY_COMMAND,bed.getRendermanProxy());
		bcAnim[0].setGeometry(tube); //borrCurveIFS);
	}

	private String quote(String str) {
		return "\""+str+"\"";
	}
	double[] ltlRadDKeys = {0, 5*fps, 5*fps};
	double[] ltlRadiusValues = {1,  1,  logoTubeRadiusFactor};
	// blending of colors to logo colors
	double[] ltlMotionDKeys = {0, 
			fps,
			3*fps, 		// move to logo size
			6*fps};		// move to final position
	double[] ltlMotionValues = {0, 0, 1, 1};
	double toLZ = 1.1*initialZ;
	double[] ltlCameraDKeys = {0, 5*fps, 5*fps};
	double[] ltlFOVValues = {initialFOV, initialFOV, logoFOV};
	double[] ltlZTransValues = {tightZ, tightZ,  toLZ};
	double[] ltlYRotDKeys = {0, fps};
	double[] ltlYRotValues = {-fps*Math.PI/180.0, 0};
	double[][] ltlDKeySets = {ltlRadDKeys, ltlMotionDKeys,ltlCameraDKeys, ltlCameraDKeys, ltlYRotDKeys};
	double[][] ltlValueSets = { 
			ltlRadiusValues, 
			ltlMotionValues,
			ltlFOVValues,
			ltlZTransValues,
			ltlYRotValues};
	int[] ltlInterps = {1, 1, 1, 1, 1};  // linear
	double maxRadius = 0, oldRadius = -1;
	boolean testADS = true;
	double[][] optcurve = null;
	double optRad = borrCurveFactory.getRadius(1);
	double w1d3=  Math.sqrt(1.0/3);
    Matrix logoPosition = MatrixBuilder.euclidean().rotateZ(Math.PI/30).rotate(Math.asin(w1d3), 1,0,0).
    	rotate(Math.PI/4, 0,1,0).rotateY(Math.PI).getMatrix();//getMatrix(); //
    double k0 = toLZ/Math.tan((initialFOV/2.0)*(Math.PI/180.0));
	double tan0 = Math.tan(.5*initialFOV*Math.PI/180.0);
	int linkToLogoResolution = captureLoResLogo ? 0 : 2;
	private void linkToLogoAnimation() {
//		if (timerCounter < 10) localFrameCount += 8*fps;
		values = ads[TO_LOGO].getValuesAtTime(timerCounter, values);
//		if (rotateWorld) {
//			double angle = AnimationUtility.hermiteInterpolation(timerCounter, 8*fps, 12*fps,wrYC[TO_LOGO], wrYC[TO_LOGO+1]);
//			theWorld.getTransformation().setMatrix(Rn.times(null, 
//					originalMat.getArray(),
//					P3.makeRotationMatrixY(tmp,angle)));
//		}
		if (values[4] != 0)	{
			theWorld.getTransformation().setMatrix(Rn.times(null, 
					originalMat.getArray(),
					P3.makeRotationMatrixY(tmp, values[4])));
			System.err.println("Angle is "+values[4]*180/Math.PI);
		}
		if (values[1] != 0)	{
			double[] tmat = finalMotion.getValueAtTime(values[1]).getArray();
			theWorld.getTransformation().setMatrix(tmat);
		}
		CameraUtility.getCamera(viewer).setFieldOfView(values[2]);
		if (values[2] != initialFOV)	{
			double tann = Math.tan(.5* values[2]*Math.PI/180.0);
			double z = tightZ*tan0/tann;
			// TODO why aren't stereo parameters adjusted here?
			setCameraDistance(z);
//		} else
//		if (values[5] != initialFOV)	{
//			double z = k0 * Math.tan(.5* values[5]*Math.PI/180.0);
//			CameraUtility.getCameraNode(viewer).getTransformation().setMatrix(
//					P3.makeTranslationMatrix(null, new double[]{0,0,z},Pn.EUCLIDEAN));						
		} else 
			setCameraDistance(values[3]);
			
		if (values[0] != oldRadius)	{
			if (logoTubeFactory == null)	{
				logoTubeFactory = new PolygonalTubeFactory(optcurve);
				logoTubeFactory.setCrossSection(circle[linkToLogoResolution]);
				logoTubeFactory.setGenerateTextureCoordinates(true);
				logoTubeFactory.setArcLengthTextureCoordinates(true);
				logoTubeFactory.setMatchClosedTwist(true);
				}
			logoTubeFactory.setRadius(maxRadius*values[0]);
			logoTubeFactory.update();
			System.err.println("Tube has vertex count: "+logoTubeFactory.getTube().getNumPoints());
			System.err.println("Setting tube radius to: "+maxRadius*values[1]);
			IndexedFaceSet tube = logoTubeFactory.getTube();
		    if (captureLoResLogo) GeometryUtilityOverflow.removeBoundaryDuplicates(tube);
			baSGCLogo.setGeometry(tube); //borrCurveIFS);
//			if (values[0] == logoTubeRadiusFactor)	
//				logoTubeFactory.getTube().setGeometryAttributes(RMAN_PROXY_COMMAND,optimalLogoRib);
//			else if (values[0] == 1.0)	
//				logoTubeFactory.getTube().setGeometryAttributes(RMAN_PROXY_COMMAND,optimalRopeLengthRib);
//			else 
			String proxy = BorromeanRingsKnot.tightConfiguration(values[0]);
			tube.setGeometryAttributes(RMAN_PROXY_COMMAND,proxy);
			oldRadius = values[0];
		}
//		for (int i = 0; i<3; ++i)						{
//			Color mix = AnimationUtility.linearInterpolation( borrColors3[i], logoColors[i], values[1]);
//			Color mix2 = AnimationUtility.linearInterpolation(  Color.WHITE,logoColors[i], values[2]);
//			Appearance ap = bc3Fold[i].getAppearance();
//			if (i==0) System.err.println("Mixed color is "+mix.toString());
//			untexturedRingAps[i].setAttribute("polygonShader.diffuseColor",mix);
//			ropeTexture2d[i] = tu.makeTextureAppearance(texturedRingAps[i], ropeTexture2d[i], uStretch, vStretch, Math.PI/4,
//		    		bandWidth, shadowWidth, values[2], band1color,mix, shadowColor, gapColor, mix2);
//			texturedRingAps[i].setAttribute("band2color",mix);	
//			texturedRingAps[i].setAttribute("blendcolor",mix2);	
//			texturedRingAps[i].setAttribute("blendfactor",values[1]);	
//			texturedRingAps[i].setAttribute("specularCoefficient", (1-values[1])*.7 + values[1]* logoSpecular[i]);
//		}
	}

	double magicSquash = .7;
	double[] creditsDKeys = {0,45*fps};
	double[] creditsValues = {-.85/magicSquash,.6/magicSquash}; //.666,.666};
	double[][] creditDKeySets = {creditsDKeys};
	double[][] creditValueSets = {creditsValues};
	int[] creditInterps = {1};
	private void creditsAnimation() {
		values = ads[CREDITS].getValuesAtTime(timerCounter, values);
		Matrix tm = new Matrix();
		MatrixBuilder.euclidean().translate(0, values[0],0).scale(1, 1.0/magicSquash,1).assignTo(tm);
		creditsTexture2D.setTextureMatrix(tm);
	}
	
	void initializeAnimatedDoubleSets()	{
		ads[JBUG_NO_STOP] = new AnimatedDoubleSet( jawosDKeySets, jawosValueSets);
		ads[JBUG_STOP] = new AnimatedDoubleSet(dkeySets,valueSets);
		ads[BRUNNIAN] = new AnimatedDoubleSet(brunDKeys, brunValues);
		ads[FIVE_FOLD] = new AnimatedDoubleSet(dkey4Sets, value4Sets); 
		ads[EVOLVE_MOEBIUS] = new AnimatedDoubleSet( mobDKeys, mobValues, mobInterp);
		ads[EVOLVE] = new AnimatedDoubleSet(evlDKeys, evlValues, evlInterps);
		ads[TO_LOGO] = new AnimatedDoubleSet( ltlDKeySets, ltlValueSets, ltlInterps);
		ads[CREDITS] = new AnimatedDoubleSet( creditDKeySets, creditValueSets, creditInterps);
		animatedFrames[JBUG_NO_STOP] =   ads[JBUG_NO_STOP].getTMax();
		animatedFrames[JBUG_STOP] = ads[JBUG_STOP].getTMax();
		animatedFrames[BRUNNIAN] = ads[BRUNNIAN].getTMax();
		animatedFrames[FIVE_FOLD] = ads[FIVE_FOLD].getTMax();
		animatedFrames[EVOLVE_MOEBIUS] = ads[EVOLVE_MOEBIUS].getTMax();
		animatedFrames[EVOLVE] = ads[EVOLVE].getTMax();
		animatedFrames[TO_LOGO] = ads[TO_LOGO].getTMax();
		animatedFrames[CREDITS] = ads[CREDITS].getTMax();
		wrY[JBUG_NO_STOP] = Math.PI/2.0;
		wrY[JBUG_STOP] = Math.PI;
		wrY[FIVE_FOLD] = Math.PI/2.0;
		wrY[BRUNNIAN] = 0;
		wrY[EVOLVE_MOEBIUS] = Math.PI;
		wrY[EVOLVE] = 0; //Math.PI/2.0;
		wrY[TO_LOGO] = 0; //Math.PI;
		wrY[CREDITS] = 0; //Math.PI;
		transitionFrames[JBUG_STOP] = jbugStopTransitionFrames;
		transitionFrames[FIVE_FOLD] = fiveFoldTransitionFrames;
		wrYC[0] = 0;
		for (int i = 0; i<numberAnimationSegments; ++i)	{
			totalFrames[i] = transitionFrames[i] + animatedFrames[i];
			if (i>0) wrYC[i] = wrY[i-1] + wrYC[i-1];
//			System.err.println("Cumulative rotation: "+wrYC[i]);
		}
		
		totalFrameCount = 0;
		for (int i = 0; i<numberAnimationSegments; ++i) {
			totalFrameCount += totalFrames[i];
			System.err.println(i+":"+totalFrameCount);
		}
		System.err.println("Total frames: "+totalFrameCount);
	}

	private void initializeAnimation(int which)	{
		initializeAnimation();
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColors", backgroundArray);
		System.err.println("Initialize "+animationNames[which]);
		switch(which)	{
		case JBUG_NO_STOP:
			animatedTriangle.setVisible(true);
			baSGCNoFiveFold.getAppearance().setAttribute(FACE_DRAW,false);
			break;
		case JBUG_STOP:
			animatedTriangle.setVisible(true);
			baSGCNoFiveFold.getAppearance().setAttribute(FACE_DRAW,false);
			baSGCNoFiveFold.getAppearance().setAttribute(VERTEX_DRAW,true);
			borromean2SegmentFactory = null;
			borromeanLoopFactory = null;
			borromeanRectColors[0][3] = rectangleAlpha;
			// this actually creates a new copy of the rectangle geometry.
			updateBorromeanRectangles();
			previousReversed = false;
			atEnd = false;
			reversed = false;
			break;
		case FIVE_FOLD:
			baSGCNoFiveFold.setVisible(false);
			animatedTriangle.setVisible(false);
			baSGCGeometry2Segments.setVisible(true);
			baSGCGeometryLoop.setVisible(false);
			baSGCGeometryLoop.getAppearance().setAttribute(FACE_DRAW,false);
			baSGCNoFiveFold.getAppearance().setAttribute(FACE_DRAW,true);
			baSGCNoFiveFold.getAppearance().setAttribute(VERTEX_DRAW,true);
			baSGCFiveFoldParent.getAppearance().setAttribute(VERTEX_DRAW,true);
			baSGCNoFiveFold.getAppearance().setAttribute("lineShader.tubeRadius", bigEdgeTubeRadius);
			baSGCGeometryLoop.getAppearance().setAttribute(VERTEX_DRAW,false);
			baSGCNoFiveFold.getAppearance().setAttribute("pointShader.pointRadius", bigEdgeTubeRadius);
			baSGCNoFiveFold.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor",borromeanEdgeColor);
			baSGCNoFiveFold.getAppearance().setAttribute("pointShader.polygonShader.diffuseColor",borromeanEdgeColor);
			baSGCFiveFoldParent.getAppearance().setAttribute("lineShader.tubeRadius", bigEdgeTubeRadius);
			baSGCFiveFoldParent.getAppearance().setAttribute("pointShader.pointRadius", bigEdgeTubeRadius);
			baSGCFiveFold[0].getAppearance().setAttribute("lineShader.polygonShader.diffuseColor",fiveFoldColors[0]);
			baSGCFiveFold[0].getAppearance().setAttribute("pointShader.polygonShader.diffuseColor",fiveFoldColors[0]);
			jitterbugAtTime(icoAngle);
			borromean2SegmentFactory = null;
			borromeanLoopFactory = null;
			borromeanRectColors[0][3] = rectangleAlpha;
			// this actually creates a new copy	 of the rectangle geometry.
			updateBorromeanRectangles();
			faceColors[1][3] = 1.0;  // draw the extra faces
			for (int j=0;j<3;++j) faceColors[1][j] = triangleColor[j];
			faceColors[1][3] = 1.0;
			drawConvexHullEdges = true;
			baSGCNoFiveFold.getAppearance().setAttribute(TRANSPARENCY, 0.0);
			break;
		case BRUNNIAN:
			baSGCGeometry2Segments.setVisible(false);
			baSGCGeometryLoop.setVisible(true);
			baSGCNoFiveFold.setVisible(true);
			animatedTriangle.setVisible(false);
			baSGCNoFiveFold.getAppearance().setAttribute(FACE_DRAW,false);
			baSGCNoFiveFold.getAppearance().setAttribute(EDGE_DRAW,true);
			baSGCNoFiveFold.getAppearance().setAttribute(VERTEX_DRAW,true);
			baSGCNoFiveFold.getAppearance().setAttribute("lineShader.tubeRadius", brunnianThickener*bigEdgeTubeRadius);
			baSGCNoFiveFold.getAppearance().setAttribute("pointShader.pointRadius", brunnianThickener*bigEdgeTubeRadius);
			baSGCNoFiveFold.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor",borromeanEdgeColor);
			baSGCNoFiveFold.getAppearance().setAttribute("pointShader.polygonShader.diffuseColor",borromeanEdgeColor);
			bc3Fold = bc3FoldUntextured;
			jitterbugAtTime(icoAngle);
			if (brunnianTorusRman)	{
				BorromeanEvolutionDescriptor bed = borrCurveFactory.getCurveDescriptor(1.55*brunnianThickener*bigEdgeTubeRadius, 1.0);
				SceneGraphComponent sgc = (SceneGraphComponent) bed.getRendermanProxy();
				MatrixBuilder.euclidean(sgc).scale(.99*magicNumber).assignTo(sgc);
				borromeanRectangleLoop.setGeometryAttributes(RMAN_PROXY_COMMAND, sgc);				
			}

			for (int i = 0; i<3; ++i)	{
				untexturedRingAps[i].setAttribute("lineShader.polygonShader.diffuseColor",borrColors3[i]);
				untexturedRingAps[i].setAttribute("pointShader.polygonShader.diffuseColor",borrColors3[i]);
				if (brunnianTorusRman) untexturedRingAps[i].setAttribute("polygonShader.diffuseColor",borrColors3[i]);
			}
//					BorromeanRingsKnot.stadiumCurve(stadiumCurveScale,.9,1));

			break;
//			baSGCNoFiveFold.setVisible(true);
//			baSGCGeometryLoop.setVisible(true);
//			baSGCGeometryLoop.getAppearance().setAttribute(FACE_DRAW,false);
//			baSGCNoFiveFold.getAppearance().setAttribute(FACE_DRAW,true);
//			baSGCNoFiveFold.getAppearance().setAttribute(VERTEX_DRAW,true);
//			baSGCNoFiveFold.getAppearance().setAttribute("lineShader.tubeRadius", bigEdgeTubeRadius);
//			baSGCNoFiveFold.getAppearance().setAttribute("pointShader.pointRadius", bigEdgeTubeRadius);
//			jitterbugAtTime(icoAngle);
//			break;
		case TO_LOGO:
//			logoTubeFactory = new PolygonalTubeFactory(borrCurveFactory.getCurve(1));
//			double d = borrCurveFactory.getRadius(1);
//			logoTubeFactory.setCrossSection(circle[2]);
//			logoTubeFactory.setRadius(d); //edgeTubeRadius);
//			logoTubeFactory.setGenerateTextureCoordinates(true);
//			logoTubeFactory.setArcLengthTextureCoordinates(true);
//			logoTubeFactory.update();
//			IndexedFaceSet tube = logoTubeFactory.getTube();
//			baSGCLogo.setGeometry(tube); //borrCurveIFS);
			if (captureLoResLogo )BorromeanRingsKnot.setCounts(10, 3, 5);
			optcurve = borrCurveFactory.getCurveDescriptor(1).getCurve();
			if (finalMotion == null)	{
				finalMotion = new FramedCurve();
				FactoredMatrix starting = new FactoredMatrix(Rn.times(null, 
						originalMat.getArray(),
						P3.makeRotationMatrixY(tmp,wrYC[TO_LOGO])));
				FactoredMatrix prestarting = new FactoredMatrix(Rn.times(null, 
						starting.getArray(),
						P3.makeRotationMatrixY(tmp,-Math.PI/6)));
				FactoredMatrix poststarting = new FactoredMatrix(Rn.times(null, 
						starting.getArray(),
						P3.makeRotationMatrixY(tmp,Math.PI/6)));
				FramedCurve.ControlPoint cp = new FramedCurve.ControlPoint(
						starting, 0);
				FramedCurve.ControlPoint cp1 = new FramedCurve.ControlPoint(
						prestarting, -.4);
				FramedCurve.ControlPoint cp2 = new FramedCurve.ControlPoint(
						poststarting, .4);
				FramedCurve.ControlPoint cp3 = new FramedCurve.ControlPoint(
						new FactoredMatrix(logoPosition.getArray()), 1);
				finalMotion.addControlPoint(cp1);
				finalMotion.addControlPoint(cp);
				finalMotion.addControlPoint(cp2);			
				finalMotion.addControlPoint(cp3);			
			}
			baSGCNoFiveFold.getAppearance().setAttribute(EDGE_DRAW,false);
			baSGCLogo.setVisible(true);
			maxRadius = borrCurveFactory.getRadius(1.0);
			System.err.println("Max radius is "+maxRadius);
		case EVOLVE_MOEBIUS:
		case EVOLVE:
			showBorromeanRects = true;
			baSGCGeometry.setVisible(false);
			baSGCNoFiveFold.setVisible(showBorromeanRects);
			baSGCNoFiveFold.getAppearance().setAttribute(FACE_DRAW,true);
			MatrixBuilder.euclidean().scale(magicNumber).assignTo(baSGCLocal);
			for (int i = 0; i<3; ++i)	{
				untexturedRingAps[i].setAttribute("polygonShader.diffuseColor", borrColors3[i]);
//				texturedRingAps[i].setAttribute("polygonShader.diffuseColor", borrColors3[i]);
			}
//			if (!useAnimationController)	{
//				FactoredMatrix fm = new FactoredMatrix(CameraUtility.getCameraNode(viewer).getTransformation());
//				double[] tlate = fm.getTranslation();
//				for (int i = 0; i<3; ++i) tlate[i] *= 1.3;
//				fm.setTranslation(tlate);
//				MatrixBuilder.euclidean(new Matrix(fm.getArray())).assignTo(CameraUtility.getCameraNode(viewer));				
//			}
			baSGCNoFiveFold.getAppearance().setAttribute("rendermanRetainGeometry", true);
			if (which == EVOLVE  || which == EVOLVE_MOEBIUS) {
				baSGCEvolution.setVisible(true);
			} 
			boolean originalVersion = true;
			if (evolveMotion == null)	{
				if (originalVersion)	{
				evolveMotion = new FramedCurve();
				FactoredMatrix tmfm = new FactoredMatrix(Rn.times(null, 
						originalMat.getArray(),
						P3.makeRotationMatrixY(tmp,wrYC[EVOLVE])));
				FramedCurve.ControlPoint cp = new FramedCurve.ControlPoint(
						tmfm, 0);
				double[] m1 = {-1,0,0,0,  0, 1, 0, 0,   0,0,-1,0, 0,0,0,1};
				double[] m3 = {0,-1,0,0,  -1, 0, 0,0,	0,0,-1,3,  0,0,0,1};
				double[] m4 = {0.024262,	-0.810395,	 0.585382,	0,
						-0.885466,	 0.254398,	 0.388885,	 0,
						-0.464070,	-0.527771,	-0.711405,	 0,
						 0.000000,	 0.000000,	 0.000000,	 1.000000};

				FramedCurve.ControlPoint cp2 = new FramedCurve.ControlPoint(
						new FactoredMatrix(m1), .05);
				FramedCurve.ControlPoint cp3 = new FramedCurve.ControlPoint(
						new FactoredMatrix(m3),.48);
				FramedCurve.ControlPoint cp4 = new FramedCurve.ControlPoint(
						new FactoredMatrix(m3),.5);
				FramedCurve.ControlPoint cp5 = new FramedCurve.ControlPoint(
						new FactoredMatrix(m4),.7);
				FramedCurve.ControlPoint cp6 = new FramedCurve.ControlPoint(
						tmfm, 1.0);
				evolveMotion.addControlPoint(cp);
				evolveMotion.addControlPoint(cp2);			
				evolveMotion.addControlPoint(cp3);			
				evolveMotion.addControlPoint(cp4);			
				evolveMotion.addControlPoint(cp5);		
				evolveMotion.addControlPoint(cp6);		
				//evolveMotion.writeToFile("/homes/geometer/gunn/Documents/Movies/ICMLogo/scripts/evolveMotion.txt");
				}
			else {
			// following code was generated after the version used in the final video 
				evolveMotion = new FramedCurve();
				FactoredMatrix tmfm = new FactoredMatrix(Rn.times(null, 
						originalMat.getArray(),
						P3.makeRotationMatrixY(tmp,wrYC[EVOLVE])));
				FramedCurve.ControlPoint cp = new FramedCurve.ControlPoint(
						tmfm, 0);
				double[] m1 = {-1,0,0,0,  0, 1, 0, 0,   0,0,-1,0, 0,0,0,1};
				double[] m3 = {0,-1,0,0,  -1, 0, 0,0,	0,0,-1,2.5,  0,0,0,1};
				FactoredMatrix tmfm2 = new FactoredMatrix(Rn.times(null, 
						tmfm.getArray(),
						P3.makeRotationMatrixY(tmp,Math.PI)));
				FactoredMatrix tmfm3 = new FactoredMatrix(Rn.times(null, 
						tmfm2.getArray(),
						P3.makeRotationMatrixY(tmp,Math.PI/6)));
				FactoredMatrix tmfm2a = new FactoredMatrix(Rn.times(null, 
						tmfm2.getArray(),
						P3.makeRotationMatrixY(tmp,-Math.PI/6)));
				FramedCurve.ControlPoint cp2 = new FramedCurve.ControlPoint(
						new FactoredMatrix(m1), .09);
				FramedCurve.ControlPoint cp3 = new FramedCurve.ControlPoint(
						new FactoredMatrix(m3),.42);
				FramedCurve.ControlPoint cp4 = new FramedCurve.ControlPoint(
						new FactoredMatrix(m3),.43);
				FramedCurve.ControlPoint cp6a = new FramedCurve.ControlPoint(
						tmfm2a,.9);
				FramedCurve.ControlPoint cp6 = new FramedCurve.ControlPoint(
						tmfm2, 1.0);
				FramedCurve.ControlPoint cp7 = new FramedCurve.ControlPoint(
						tmfm3, (totalEvolutionCurves+((double)fps))/totalEvolutionCurves);
				evolveMotion.addControlPoint(cp);
				evolveMotion.addControlPoint(cp2);			
				evolveMotion.addControlPoint(cp3);			
				evolveMotion.addControlPoint(cp4);			
//				evolveMotion.addControlPoint(cp5);		
				evolveMotion.addControlPoint(cp6a);		
				evolveMotion.addControlPoint(cp6);		
				evolveMotion.addControlPoint(cp7);		
				evolveMotion.printRotationValues();
//				evolveMotion.writeToFile("/homes/geometer/gunn/Documents/Movies/ICMLogo/scripts/evolveMotion.txt");
			}
			}
			if (which == EVOLVE_MOEBIUS || which == TO_LOGO)	{
				globalRadiusScale = .5;
				for (int i = 0; i<3; ++i)	{
					bc3FoldTextured[i].setVisible(false);
					bc3FoldUntextured[i].setVisible(true);
				}
			} else if (which == EVOLVE)	{
				globalRadiusScale = 1.0;				
				for (int i = 0; i<3; ++i)	{
					bc3FoldTextured[i].setVisible(true);
					bc3FoldUntextured[i].setVisible(false);
				}
			}

			break;
		case CREDITS:
			viewer.getSceneRoot().getAppearance().setAttribute("backgroundColors",Appearance.INHERITED);
			viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor",Color.white);
			setCameraDistance(initialZ);
			CameraUtility.getCamera(viewer).setFieldOfView(initialFOV);
			theWorld.getTransformation().setMatrix(Rn.identityMatrix(4));
			creditsSurfaceSGC.setVisible(true);
			creditsTexture2D.setTextureMatrix(creditsTMOrig);
			break;
		default:
			System.err.println("No such segment "+which);
		}
	}
	String rendermanWritePath = null;
	private double totalFrameCount;
	private int rendermanCount;
	private Color[] backgroundArray;
	private void writeRenderman() {
		if (rendermanWritePath == null)	{
			Calendar c = Calendar.getInstance();
			String dir = String.format("%ty_%tm_%td_%tH_%tM",c,c,c,c,c); //new Integer((int) System.currentTimeMillis()});
			
			rendermanWritePath = (scratchDisk == null) ? 
					GlobalProperties.saveResourceDir :
						scratchDisk+"/Movies/IMULogo/ribs/"+dir+"/";
			boolean success = (new File(rendermanWritePath)).mkdirs();
			if (!success)
				try {
					throw new IOException("Unable to create directory");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		if (ribViewer==null)	{
			ribViewer = new RIBViewer();
			ribViewer.initializeFrom(viewer);
		}
		ribViewer.setFileName(rendermanWritePath+String.format("s%02ds-%04d",new Object[]{ new Integer(((int)animationSegment)), new Integer(((int)(localFrameCount/speedup)))})+".rib");			
		rendermanCount++;
		viewer.getSceneRoot().getAppearance().setAttribute(TRANSPARENCY_ENABLED, true);
		ribViewer.render();
	}
	public boolean hasInspector() {return true; }
	public Component getInspector(final Viewer viewer) {
		Box container = Box.createVerticalBox();
		final TextSlider aSlider = new TextSlider.Double("focus",  SwingConstants.HORIZONTAL, 1.0, 20.0,CameraUtility.getCamera(viewer).getFocus());
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				CameraUtility.getCamera(viewer).setFocus(aSlider.getValue().doubleValue());
				viewer.renderAsync();
			}
		});
		container.add(aSlider);
		final TextSlider bSlider = new TextSlider.Double("eye separation",  SwingConstants.HORIZONTAL, 0.0, 2.0, CameraUtility.getCamera(viewer).getEyeSeparation());
		bSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				CameraUtility.getCamera(viewer).setEyeSeparation(bSlider.getValue().doubleValue());
				viewer.renderAsync();
			}
		});
		container.add(bSlider);

		container.add(Box.createVerticalGlue());
		container.setName("IMULogo");
		return creditsSurface.getInspector(); //container;
	}

}