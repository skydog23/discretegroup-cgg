package discreteGroup.maniview;

import static de.jreality.shader.CommonAttributes.BACKEND_RETAIN_GEOMETRY;
import static de.jreality.shader.CommonAttributes.BACKGROUND_COLOR;
import static de.jreality.shader.CommonAttributes.BACKGROUND_COLORS;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.FOG_DENSITY;
import static de.jreality.shader.CommonAttributes.FOG_ENABLED;
import static de.jreality.shader.CommonAttributes.LIGHTING_ENABLED;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.METRIC;
import static de.jreality.shader.CommonAttributes.ONE_TEXTURE2D_PER_IMAGE;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.RMAN_GLOBAL_INCLUDE_FILE;
import static de.jreality.shader.CommonAttributes.RMAN_PROXY_COMMAND;
import static de.jreality.shader.CommonAttributes.SPECULAR_COEFFICIENT;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.iharder.dnd.FileDrop;
import charlesgunn.anim.jreality.SceneGraphAnimator;
import charlesgunn.jreality.CameraUtilityOverflow;
import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.jreality.geometry.OneArmedTinManFactory;
import charlesgunn.jreality.geometry.SnakeFactory;
import charlesgunn.jreality.newtools.AllroundTool;
import charlesgunn.jreality.newtools.FlyTool2;
import charlesgunn.jreality.texture.RopeTextureFactory;
import charlesgunn.jreality.texture.SimpleTextureFactory;
import charlesgunn.jreality.tools.MotionManager;
import charlesgunn.jreality.tools.RotateShapeTool;
import charlesgunn.jreality.viewer.GlobalProperties;
import charlesgunn.util.TextSlider;
import de.jreality.backends.label.LabelUtility;
import de.jreality.geometry.GeometryMergeFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.SphereUtility;
import de.jreality.jogl.plugin.HelpOverlay;
import de.jreality.jogl.plugin.InfoOverlay;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.experimental.ViewerKeyListener;
import de.jreality.plugin.scene.SceneShrinkPanel;
import de.jreality.portal.PortalCoordinateSystem;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.pick.Graphics3D;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.GlslPolygonShader;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.swing.jrwindows.JRWindow;
import de.jreality.swing.jrwindows.JRWindowManager;
import de.jreality.tools.ClickWheelCameraZoomTool;
import de.jreality.tools.RotateTool;
import de.jreality.toolsystem.ToolSystem;
import de.jreality.util.CameraUtility;
import de.jreality.util.DefaultMatrixSupport;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.SystemProperties;
import de.jreality.vr.AppearancePanel;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.core.DiscreteGroupViewportConstraint;
import de.jtem.discretegroup.core.FiniteStateAutomaton;
import de.jtem.discretegroup.core.FiniteStateAutomatonUtility;
import de.jtem.discretegroup.core.ImportGroup;
import de.jtem.discretegroup.groups.BorromeanUtility;
import de.jtem.discretegroup.groups.CrystallographicGroup;
import de.jtem.discretegroup.groups.Platycosm;
import de.jtem.discretegroup.groups.Spherical3DGroup;
import de.jtem.discretegroup.util.WingedEdge;
import de.jtem.discretegroup.util.WingedEdgeUtility;
import discreteGroup.ResourceClass;
import discreteGroup.spacegroups.GroupGeneratorFactory;
import discreteGroup.tools.CopyClickTool;

public class Maniview {

	SceneGraphComponent 
		theWorld, 
		mainGenerators,
		replicatorGenerators,
		generators,
		theCameraSGN, 
			portalScaleSGC1,
				avatarRepn,
				spaceShip,
		collector,
			beams,
			geometrySGC, 
				scaledDD, 
				letterSGC,
			skewerSGC,
			stickTipSGC,
		avatarNode,
			portalScaleSGC2,
			inspectorTriggerSGC = new SceneGraphComponent("trigger");

	DiscreteGroupSceneGraphRepresentation theMainRepn, theDuplicatorRepn;
	DiscreteGroup theGroup;
	IndexedFaceSet standardFundDomain, scaledFundDomain;
	Viewer viewer;
	FlyTool2 flyTool;
	SceneGraphPath pathToWorld;
	TinManTool tmt;
	OneArmedTinManFactory tinmanfactory;
	SnakeFactory sf;
	DiscreteGroupElement[] masterList, officialList;
	ToolSystem toolSystem;
	
	double radius = .06,		// size of beams
		stretchFactor = .2,		// scale of reduced fundamental domain
		letterStretch = 1.0,
		minD = 4.5, 			// viewport constraint: minimum distance (if smaller, always drawn)
		maxD = 8.0,			// viewport constraint: maximum distance (if larger, never drawn)
		flySpeed = .15, 		
		portalScale = 1.0,
		spaceShipScale = 2.5,
		fogDensity = .05,
		generatorThickness = 0.03,
		zTlate = 0;
	int minW = 2, maxW = 12,	// word lengths for constraint: not currently used
		maxNumElements = 500,	// begin with a group of this size
		maxDirDomOrbitSize= 75,
		pickCopies = 500;
	boolean copycat = true,
		componentDisplayLists = true,
		preTS = false,
		postTS = false,		// this is important! don't change
		followCamera = true,
		clipToCamera = true,
		showAvatar = false,
		showSpaceShip = !showAvatar,
		showBeams = true,
		showGenerators = false,
		showGeometry = true,
		showDirDom = true,
		showLetters = false,
		fogEnabled = true,
		useGLSL = true,
		useVertexArrays = false,
		singlePeer = true,
		doRenderman = false,
		doTexture = true,
		showCameraRepn = false,
		showTrigger = false,
		doDuplicator = false,
		doNotKnot = false;
	Viewer iv;
	InfoOverlay info;
	TraceTool traceTool;
	Appearance reflectionAp, beamsWithTex;
	CubeMap rm;
	private Texture2D tex2d;
	private AllroundTool allRoundTool;
	InfoOverlay perfInfo;
	HelpOverlay helpOverlay;
	ToolEnum currentTool = ToolEnum.TILE;
	double[] ztlates = {.15, 0.5, 1.0}, initialAvatarTlates = {.7, 1.0, .5};

	private double[] portalTlate = new double[]{PortalCoordinateSystem.getxDimPORTAL()/2, .4, -1.24};

	static protected String[] noneuclideanNames =  new String[]{"borromean order-4","120 cell","600-cell"};

	private PointLight pointLight1, pointLight2;


	private SceneGraphComponent lightSGC1, lightSGC2;
	private SceneGraphComponent euclideanLights, hyperbolicLights, ellipticLights;
	private Transformation tileAvatarTF = new Transformation(), dupAvatarTF = new Transformation();
//	TermesSphere termes;
	static String curvedSpaceExamples = System.getProperty("user.home")+"/Software/Curved Spaces/Sample Spaces/";
	JFileChooser fc = new JFileChooser(curvedSpaceExamples);

	static String[] toolnames = {"tile","skewer","duplicator"};
	static public enum ToolEnum {
		TILE(toolnames[0]),
		SKEWER(toolnames[1]),
		DUPLICATOR(toolnames[2]);
		private String name;
		ToolEnum(String name) {
			this.name = name;
		};
		
		public String getName()	{
			return name;
		}
		
		public static ToolEnum toolForName(String n)	{
			if (n.equals(toolnames[0])) return ToolEnum.TILE;
			if (n.equals(toolnames[1])) return ToolEnum.SKEWER;
			if (n.equals(toolnames[2])) return ToolEnum.DUPLICATOR;
			throw new IllegalArgumentException("invalid name: "+n);
		}
		
	}

	ManiviewVR mvr;
	public Maniview(ManiviewVR m)	{
		super();
		mvr = m;
	}
	
	public void makeWorld(Viewer v) {

		viewer = v;
		viewer.getSceneRoot().getAppearance().setAttribute(ONE_TEXTURE2D_PER_IMAGE, true);
//		viewer.getSceneRoot().addTool(new PickShowTool());
		if (viewer instanceof de.jreality.jogl.JOGLViewer) {
//			termes = new TermesSphere(viewer);
//			viewer.getSceneRoot().addChild(termes.getSceneGraphComponent());
			perfInfo =  InfoOverlay.perfInfoOverlayFor();
			perfInfo.setInstrumentedViewer((de.jreality.jogl.InstrumentedViewer) v);
			perfInfo.setVisible(true);
			info = new InfoOverlay();
			info.setInstrumentedViewer((de.jreality.jogl.InstrumentedViewer) v);
			info.setPosition(InfoOverlay.LOWER_LEFT);
			info.setVisible(true);
			info.setInfoProvider(new InfoOverlay.InfoProvider() {

				List<String> infoStrings = new Vector<String>();
				public void updateInfoStrings(InfoOverlay io)	{
					//JOGLConfiguration.theLog.log(Level.INFO,"Providing info strings");
					infoStrings.clear();
					if (theGroup != null && theMainRepn != null && theMainRepn.getElementList() != null)	{
						infoStrings.add("# elements rendered: "+theMainRepn.getCopyCatCount());
						infoStrings.add("min/max dist:"+String.format("%4.2g %4.2g",minD,maxD));
						infoStrings.add("# elements: "+theMainRepn.getElementList().length);
						infoStrings.add("group name: "+theGroup.getName());
						//System.err.println(theMainRepn.getCopyCatCount());
					}
					io.setInfoStrings(infoStrings);
				}
			});
		}
		if (viewer.hasViewingComponent() && 
				viewer.getViewingComponent() instanceof Component) {
			ViewerKeyListener vkl = new ViewerKeyListener(viewer, helpOverlay, perfInfo);
			((Component) viewer.getViewingComponent()).addKeyListener(vkl);
//			if (termes != null) 
//				((Component) viewer.getViewingComponent()).addKeyListener(termes.getKeyListener());

		}

		updateFog();
		reflectionAp = new Appearance();
		try {
			rm = TextureUtility.createReflectionMap(
			          reflectionAp,
			          "polygonShader",
			          "http://www.math.tu-berlin.de/~gunn/Pictures/textures/maniviewCubeMap/cubeMapTest_",//"/homes/geometer/gunn/Pictures/textures/cubeMapTest_", 
			          new String[]{"rt","lf","up", "dn","bk","ft"},
			          ".png");
			rm.setBlendColor(new Color(255, 255, 255, 160));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setupInspectorTrigger();
		((Component)viewer.getViewingComponent()).addComponentListener(new ComponentAdapter() {

			public void componentResized(ComponentEvent e) {
				updateInspectorTrigger();
			}

			
		});
		theWorld = SceneGraphUtility.createFullSceneGraphComponent("world");
		theWorld.getAppearance().setAttribute("polygonShader.diffuseColor", Color.white);
		collector = new SceneGraphComponent("Collector");
		generators = new SceneGraphComponent("Generators");
		generators.setAppearance(new Appearance());
		mainGenerators = SceneGraphUtility.createFullSceneGraphComponent("main generators");
		mainGenerators.addChild(generators);
		replicatorGenerators = SceneGraphUtility.createFullSceneGraphComponent("replicator generators");
		replicatorGenerators.addChild(generators);
		Appearance ap = viewer.getSceneRoot().getAppearance();
		ap.setAttribute(VERTEX_DRAW, false);
		ap.setAttribute(EDGE_DRAW, false);
		ap.setAttribute(RMAN_GLOBAL_INCLUDE_FILE,"quality.rib");
		ap.setAttribute(BACKGROUND_COLOR, new Color(0,0,20));
		ap.setAttribute(BACKGROUND_COLORS, Appearance.INHERITED);
		ap.setAttribute(BACKEND_RETAIN_GEOMETRY, true);
		ap.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR,Color.white);
		ap.setAttribute(POLYGON_SHADER+"."+SPECULAR_COEFFICIENT,.3);
		portalScaleSGC1 = new SceneGraphComponent();
		theCameraSGN = new SceneGraphComponent("camera SGN");
		theCameraSGN.setTransformation(new Transformation());
		theCameraSGN.setAppearance(new Appearance());
		theCameraSGN.getAppearance().setAttribute("polygonShader.diffuseColor", new Color(255,200,0));
		theCameraSGN.getAppearance().setAttribute(EDGE_DRAW, false);
		theCameraSGN.setPickable(false);
		if ( GlobalProperties.isPortal)	{
			tmt = new TinManTool();
			avatarRepn = new SceneGraphComponent("avatar Repn");
			avatarRepn.addTool(tmt);
			avatarRepn.addChild(tmt.getTinManFactory().getTinMan());  
			tmt.setActive(showAvatar);
			tinmanfactory = tmt.getTinManFactory();
		} else {
//			tinmanfactory = new OneArmedTinManFactory();
//			tinmanfactory.setFlatten(false);
//			tinmanfactory.update();
//			avatarRepn = tinmanfactory.getTinMan();
			avatarRepn = new SceneGraphComponent();
		}
		stickTipSGC = SceneGraphUtility.createFullSceneGraphComponent("stick tip");
		stickTipSGC.setVisible(false);
		ap = stickTipSGC.getAppearance();
		ap.setAttribute(VERTEX_DRAW, false);
		ap.setAttribute(EDGE_DRAW, true);
		ap.setAttribute("lineShader."+TUBES_DRAW, false);
//		ap.setAttribute("pointShader.polygonShader.diffuseColor", new Color(0,255,255));
//		ap.setAttribute("pointShader.pointRadius", .1);
		ap.setAttribute("lineShader.diffuseColor", new Color(0,255,255));
		ap.setAttribute("lineShader.polygonShader.diffuseColor", new Color(0,155,55));
		ap.setAttribute("lineShader.polygonShader.specularCoefficient", .6);
		ap.setAttribute("lineShader.polygonShader.specularColor", new Color(255,255,255));
		ap.setAttribute("lineShader.polygonShader.specularExponent", 64.0);
//		ap.setAttribute("lineShader.polygonShader.smoothShading", false);
		ap.setAttribute("lineShader.tubeRadius", .01);
		ap.setAttribute("lineShader.lineWidth", 3);
		ap.setAttribute("lineShader.polygonShader.vertexShadername", "default");
		try {
			CubeMap rm2 = TextureUtility.createReflectionMap(ap, "polygonShader",
					TextureUtility.getCubeMapImages(rm));
			rm2.setBlendColor(new java.awt.Color(1f, 1f, 1f, .6f));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		generators.setPickable(false);
		ap = generators.getAppearance();
		try {
			CubeMap rm3 = TextureUtility.createReflectionMap(ap, "polygonShader",
					TextureUtility.getCubeMapImages(rm));
			rm3.setBlendColor(new Color(255,255,255,150));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		generators.setVisible(showGenerators);

		sf = new SnakeFactory(100, 3);
		sf.update();
		stickTipSGC.setGeometry(sf.getSnake());
//		avatarRepn.addChild(stickTipSGC);
		traceTool = new TraceTool(sf, tinmanfactory,stickTipSGC.getAppearance(), theCameraSGN.getTransformation());
		try {
			spaceShip = Readers.read(ResourceClass.class.getResource("resources/geom/cam.off"));
			spaceShip = new SceneGraphComponent();
			spaceShip.setAppearance(new Appearance());
			//spaceShip.getAppearance().setAttribute(SMOOTH_SHADING, false);
			spaceShip.setGeometry(getBetterCamera());
			spaceShip.setVisible(showSpaceShip);
		} catch (IOException e) {
			e.printStackTrace();
		}
		avatarRepn.setVisible(showAvatar);		
		portalScaleSGC1.addChildren(avatarRepn, spaceShip);
		theCameraSGN.addChild(portalScaleSGC1);
		viewer.getSceneRoot().addChild(theWorld);
		SceneGraphPath sgp = new SceneGraphPath(viewer.getSceneRoot(), theWorld);
		toolSystem.setEmptyPickPath(sgp);
		theWorld.addTool(new RotateShapeTool());
		initTabs();
	}
	
	private void updateFog() {
		viewer.getSceneRoot().getAppearance().setAttribute(FOG_ENABLED, fogEnabled);
		viewer.getSceneRoot().getAppearance().setAttribute(FOG_DENSITY, fogDensity);
	}

	Graphics3D context3D;
	DiscreteGroupViewportConstraint viewportConstraint;
	String textureFile = null;
	String groupName;
	public void replaceGroup(String string) {
		DiscreteGroup dg = null;
		groupName = string;
		doTexture = true;
		portalScale = 1.0;
		if (string.equals("load"))	{
			//System.out.println("FCI resource dir is: "+resourceDir);
			int result = fc.showOpenDialog(new JFrame());
			if (result == JFileChooser.APPROVE_OPTION)	{
				File file = fc.getSelectedFile();
				dg = ImportGroup.initFromFile(file, Pn.PROJECTIVE); //120cell.gens");			
				fc.setCurrentDirectory(file);
			} else {
				System.out.println("Unable to open file");
				return;
			}
			initializeGroup(dg);
		} else {
			if (string.equals(noneuclideanNames[0]))	{
//			dg = DiscreteGroupIO.initFromResource("resources/groups/borrom4.gens", Pn.HYPERBOLIC); //120cell.gens");			
//			FiniteStateAutomaton fsa = new FiniteStateAutomaton("borrom4.wa");
//			dg.setFsa(fsa);
				dg = BorromeanUtility.borromeanGroupOfOrder(4);
				dg.setName(string);
				portalScale = .2;
				radius = .08;
				flySpeed = .15;
				DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(pickCopies);
				officialList = DiscreteGroupUtility.generateElements(dg, dgsc);
				dgsc = new DiscreteGroupSimpleConstraint(8000);
				DiscreteGroupElement[] rawlist = DiscreteGroupUtility.generateElements(dg, dgsc);
				DiscreteGroupUtility.sort(rawlist, Pn.HYPERBOLIC);
				maxNumElements = 3000;
				dgsc = new DiscreteGroupSimpleConstraint(4.3, -1, maxNumElements);
				masterList = DiscreteGroupUtility.applyConstraint(dgsc, rawlist);
				clipToCamera = true;
//			doTexture = false;
				textureFile = "resources/textures/wood_boards.jpg";
				minW = 2; maxW = 12; minD = 2.5; maxD = 4.5;
			} else if (string.equals(noneuclideanNames[1])) {
				dg = ImportGroup.initFromResource("resources/groups/120cell.gens", Pn.ELLIPTIC); //120cell.gens");		
				dg.setFinite(true);
				dg.setName(string);
				minD = maxD = -1;
				radius = .015;
				flySpeed = .2;
				portalScale = .2;
				fogEnabled = false;
				clipToCamera = false;
				followCamera = false;
				DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(120);
				masterList = DiscreteGroupUtility.generateElements(dg, dgsc);
				officialList = masterList;
				textureFile = "resources/textures/wood_boards.jpg";
				viewportConstraint = null;
			} else if (string.equals(noneuclideanNames[2])) {
				dg = Spherical3DGroup.instanceOf("335");	
				dg.setFinite(true);
//				dg.setName(string);
				dg.setMaxDirDomOrbitSize(600);
				minD = maxD = -1;
				radius = .007;
				flySpeed = .2;
				portalScale = .2;
				fogEnabled = false;
				clipToCamera = false;
				followCamera = false;
				DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(600);
				masterList = Spherical3DGroup.towerOfTetrahedra().getElementList(); //DiscreteGroupUtility.generateElements(dg, dgsc);
				dg.setName("tetrahedraTower");
				officialList = masterList;
				textureFile = "resources/textures/wood_boards.jpg";
				viewportConstraint = null;
			} else {  // platycosm
				dg = Platycosm.instanceOfGroup(string);
				FiniteStateAutomaton fsa = FiniteStateAutomaton.fsaForName(string+".wa", de.jtem.discretegroup.ResourceClass.class);
				if (fsa != null && fsa.getTransitions().length != 0) dg.setFsa(fsa);
				clipToCamera = true;
				followCamera = true;
				radius = .05;
				maxNumElements = string == "c3" ? 500 : 3000;
				flySpeed = .5;
				DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(pickCopies);
				officialList = DiscreteGroupUtility.generateElements(dg, dgsc);
				dgsc = new DiscreteGroupSimpleConstraint(maxNumElements);
				masterList = DiscreteGroupUtility.generateElements(dg, dgsc);
				minW = 2; maxW = 10; minD = 4.5; maxD = 12.0;
				textureFile = null;
			}
		}
		JRadioButton button = nameToButton.get(string);
		if (button != null) button.setSelected(true);
		replaceGroup(dg);
	}

	private void initializeGroup(DiscreteGroup dg) {
		fogEnabled = true;
		doTexture = true;
		masterList = null;
		portalScale = 1.0;
		maxDirDomOrbitSize = 75;
		if (dg.getMetric() == Pn.EUCLIDEAN) {
			radius = .05;
			flySpeed = .5;
//			FiniteStateAutomaton fsa = FiniteStateAutomaton.generateFiniteStateAutomatonForGroup(dg);
//			dg.setFsa(fsa);
			DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(pickCopies);
			officialList = DiscreteGroupUtility.generateElements(dg, dgsc);
			minW = 2; maxW = 16; minD = 4.5; maxD = 16.0;
			maxNumElements = 800;
			maxDirDomOrbitSize = 800;
			//dg.setDirichletDomainOrbit(800);
		}
		else if (dg.getMetric() == Pn.ELLIPTIC) {
			radius = .02;
			flySpeed = .2;
			clipToCamera = false; followCamera = false;
			portalScale = .2;
		}
		else {  // hyperbolic
			radius = .07;
			flySpeed = .15;
			minW = 2; maxW = 12; minD = 2.5; maxD = 4.5;
			portalScale = .2;
			DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(pickCopies);
			officialList = DiscreteGroupUtility.generateElements(dg, dgsc);
			if (dg.getFsa() == null)	{
				FiniteStateAutomaton fsa = FiniteStateAutomatonUtility.generateFiniteStateAutomatonForGroup(dg);
				dg.setFsa(fsa);				
			}
			dgsc = new DiscreteGroupSimpleConstraint(3000);
			DiscreteGroupElement[] rawlist = DiscreteGroupUtility.generateElements(dg, dgsc);
			DiscreteGroupUtility.sort(rawlist, Pn.HYPERBOLIC);
			maxNumElements = 3000;
			dgsc = new DiscreteGroupSimpleConstraint(4.3, -1, maxNumElements);
			masterList = DiscreteGroupUtility.applyConstraint(dgsc, rawlist);
			// with 175 below, one small hyperbolic manifold doesn't find all faces
			maxDirDomOrbitSize = 225;
			//dg.setDirichletDomainOrbit(225);
		}  
		if (dg.getMetric() != Pn.ELLIPTIC && masterList == null) {
			DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(dg.getFsa() != null ? maxNumElements : 500);//maxNumElements)); //setMaxNumberElements(300);
			masterList = DiscreteGroupUtility.generateElements(dg, dgsc);			
		}
		dg.setMaxDirDomOrbitSize(maxDirDomOrbitSize);
		if (officialList == null) officialList = masterList;
	}
	
	DirichletDomain dirdom, scaledDirDom;
	public void replaceGroup(DiscreteGroup dg)	{
		System.err.println("replacing group "+dg.getName());
		context3D = new Graphics3D(viewer);
		SceneGraphNode.setThreadSafe(preTS);
		theGroup = dg;		
		maxDirDomOrbitSize = dg.getMaxDirDomOrbitSize();
		dirdom = new DirichletDomain(theGroup);
		dirdom.setDirichletDomainOrbit(maxDirDomOrbitSize);
		dirdom.update();
		// sigh .. copy visitor doesn't seem to work correctly
		scaledDirDom = new DirichletDomain(theGroup);
		scaledDirDom.setDirichletDomainOrbit(maxDirDomOrbitSize);
		scaledDirDom.update();
//		if (theGroup.getName().startsWith("borromean") && useGLSL) 
//				textureFile = null;
		if (theMainRepn != null) {
			theMainRepn.dispose();
			theMainRepn = null;
			DefaultMatrixSupport.getSharedInstance().restoreDefaultMatrices(viewer.getSceneRoot(), false);
			SceneGraphUtility.removeChildren(collector);
		}
		if (theDuplicatorRepn != null) {
			theDuplicatorRepn.dispose();
			theDuplicatorRepn = null;
		}
		PortalCoordinateSystem.setPortalScale(portalScale);		
		if (portalSl != null) portalSl.setValue(portalScale);
		Appearance	ap = theWorld.getAppearance();

		flyTool.setGain(flySpeed);
		flyTool.setMetric(theGroup.getMetric());
		updateSpaceShipTform();
		theCameraSGN.setVisible(showCameraRepn);

		updateGeometry();
		updateShader();

		theMainRepn = new  DiscreteGroupSceneGraphRepresentation(theGroup,  copycat && !theGroup.isFinite(), "main");
		if (theGroup.getName().startsWith("tetrahedraTower")) {
			Color[] colors = {Color.red, Color.yellow, new Color(0,200,140)};
			Appearance[] aps = new Appearance[3];
			for (int i = 0; i<3; ++i) {
				aps[i ] = new Appearance();
				aps[i].setAttribute("polygonShader.diffuseColor", colors[i]);
			}
			theMainRepn.setAppList(aps);			
		} else 
			theMainRepn.setAppList(null);
		
		zTlate = ztlates[theGroup.getMetric()+1];
		updateGroupConstraint();
		List l = SceneGraphUtility.getPathsBetween(viewer.getSceneRoot(), theWorld);
		pathToWorld = (SceneGraphPath) l.get(0);
		theMainRepn.setClipDelay(500);
		theMainRepn.setFollowDelay(500);
		theMainRepn.setFollowsCamera(followCamera);
		theMainRepn.setClipToCamera(clipToCamera);
		if (followCameraBox != null) followCameraBox.setSelected(followCamera);
		if (clipCameraBox != null) clipCameraBox.setSelected(clipToCamera);
		
		theMainRepn.setOfficialElementList(officialList);
		if (masterList != null) {
			theMainRepn.setElementList(masterList);
		}
		theMainRepn.attachToViewer(viewer, pathToWorld); 
		theMainRepn.getDropBox().setComponentDisplayLists( componentDisplayLists);
		theMainRepn.setCameraRepn(theCameraSGN);
		theMainRepn.setWorldNode(collector);
		theMainRepn.getRepresentationRoot().addChild(mainGenerators);
		
		theDuplicatorRepn = new DiscreteGroupSceneGraphRepresentation(theGroup, false, "duplicator");
		DiscreteGroupSimpleConstraint identity = new DiscreteGroupSimpleConstraint(1);
		DiscreteGroupElement[] idList = DiscreteGroupUtility.generateElements(theGroup, identity);
		theDuplicatorRepn.setElementList(idList);
		theDuplicatorRepn.setWorldNode(collector);
		theDuplicatorRepn.update();
		theDuplicatorRepn.attachToViewer(viewer, pathToWorld, false, 0, false, 0);
		replicatorRoot = theDuplicatorRepn.getRepresentationRoot();
		replicatorRoot.addChild(replicatorGenerators);
		replicatorTool = new CopyClickTool(theGroup, theDuplicatorRepn);
		replicatorInspector.removeAll();
		replicatorInspector.add(replicatorTool.getInspector());
		SceneGraphComponent toolDD = replicatorTool.getSceneGraphComponent();
		theDuplicatorRepn.getFundamentalRegion().addChild(toolDD);
		repRotateTool = SystemProperties.isPortal ? new RotateTool() : new RotateShapeTool();
		skewerTool = new SkewerTool(theGroup, theMainRepn, viewer);
		skewerSGC = skewerTool.getFundamentalDomain();
		skewerSGC.setVisible(false);
		theMainRepn.getFundamentalRegion().addChild(skewerSGC);
		// place a rib proxy command with lots of copies in the scene graph
		if (doRenderman)	{
			DiscreteGroupSceneGraphRepresentation rmanDGSGR = new  DiscreteGroupSceneGraphRepresentation(theGroup, false);
			rmanDGSGR.setWorldNode(theMainRepn.getWorldNode());
			rmanDGSGR.setCameraRepn(theMainRepn.getCameraRepn());
			DiscreteGroupSimpleConstraint  dgsc = new DiscreteGroupSimpleConstraint(50000);
			DiscreteGroupElement[] rawlist = DiscreteGroupUtility.generateElements(dg, dgsc);
			DiscreteGroupUtility.sort(rawlist, Pn.HYPERBOLIC);
			maxNumElements = 10000;
			dgsc = new DiscreteGroupSimpleConstraint(5.5, -1, maxNumElements);
//			DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(10000);
			rmanDGSGR.setElementList(DiscreteGroupUtility.applyConstraint(dgsc, rawlist));
			System.err.println("# = "+rmanDGSGR.getElementList().length);
			rmanDGSGR.update();
			theMainRepn.getRepresentationRoot().getAppearance().setAttribute(RMAN_PROXY_COMMAND, rmanDGSGR.getRepresentationRoot());			
		}
		ap = theMainRepn.getFundamentalRegion().getAppearance(); //new Appearance();
		ap.setAttribute(SceneGraphAnimator.ANIMATED, false);
		ap.setAttribute("singlePeer", singlePeer);
		ap.setAttribute("polygonShader.vertexShadername", "simple");
		ap.setAttribute("vertexShadername", "simple");
		theMainRepn.update();
		DiscreteGroupUtility.sort(theGroup.getElementList(), theGroup.getMetric());
		updatePortalScale();
//		viewer.renderAsync();
		setComponentDisplayLists();
		setupAvatarTforms();
		CameraUtilityOverflow.reset(CameraUtility.getCamera(viewer), theGroup.getMetric());
		DefaultMatrixSupport.getSharedInstance().storeDefaultMatrices(viewer.getSceneRoot());
		SceneGraphUtility.setMetric(viewer.getSceneRoot(), theGroup.getMetric());
		setTool(currentTool);
		SceneGraphNode.setThreadSafe(postTS);
		if (mvr != null) mvr.animationPlugin.resetSceneGraph();
		
	}

	private void setupAvatarTforms() {
		MatrixBuilder.init(null, theGroup.getMetric()).
			translate(0,0,initialAvatarTlates[theGroup.getMetric()+1]).assignTo(tileAvatarTF);
		MatrixBuilder.init(null,theGroup.getMetric()).
			translate(0,0,unitD[theGroup.getMetric()+1]).assignTo(dupAvatarTF);
		avatarNode.setTransformation(currentTool == ToolEnum.DUPLICATOR ? dupAvatarTF : tileAvatarTF);
	}

	protected void setTool(ToolEnum tool) {
		theMainRepn.getRepresentationRoot().setVisible(false);
		theDuplicatorRepn.getRepresentationRoot().setVisible(false);
		replicatorRoot.setVisible(false);
		skewerSGC.setVisible(false);
		skewerSGC.setPickable(false);
		scaledDD.setPickable(false);
		theMainRepn.setActive(false);
		theDuplicatorRepn.setActive(false);
		SceneGraphPath sgp = new SceneGraphPath(viewer.getSceneRoot(), theWorld);
		toolSystem.setEmptyPickPath(sgp);
		if (currentTool != null)	{
			switch(currentTool)	{
			case TILE:
				if (theMainRepn.getRepresentationRoot().getTools().contains(allRoundTool))
						theMainRepn.getRepresentationRoot().removeTool(allRoundTool);
				allRoundTool.setActive(false, theMainRepn.getRepresentationRoot(), null);	
				mainGenerators.setVisible(false);
				break;
			case SKEWER:
				if (theMainRepn.getRepresentationRoot().getTools().contains(skewerTool))
					theMainRepn.getRepresentationRoot().removeTool(skewerTool);
				mainGenerators.setVisible(false);
				skewerTool.deactivate();
				break;
			case DUPLICATOR:
				if (theDuplicatorRepn.getRepresentationRoot().getTools().contains(replicatorTool))
					theDuplicatorRepn.getRepresentationRoot().removeTool(replicatorTool);
				if (theDuplicatorRepn.getRepresentationRoot().getTools().contains(repRotateTool))
					theDuplicatorRepn.getRepresentationRoot().removeTool(repRotateTool);
				replicatorGenerators.setVisible(false);
				MotionManager mm = MotionManager.motionManagerForViewer(viewer);
				mm.clearMotions();
				break;
			}			
		}
//		Matrix avatarM = avatarTform.get(tool == ToolEnum.DUPLICATOR ? ToolEnum.DUPLICATOR : ToolEnum.TILE);
//		if (avatarM != null) avatarNode.getTransformation().setMatrix(avatarM.getArray());
		avatarNode.setTransformation(tool == ToolEnum.DUPLICATOR ?  dupAvatarTF : tileAvatarTF );
		switch(tool)	{
			case TILE:
				scaledDD.setPickable(true);
				allRoundTool.setActive(false, theMainRepn.getRepresentationRoot(), null);	
				theMainRepn.getRepresentationRoot().addTool(allRoundTool);
				theMainRepn.getRepresentationRoot().setVisible(true);
				theMainRepn.setActive(true);
				mainGenerators.setVisible(true);
				fogEnabled = true;
				updateFog();
				break;
			case SKEWER:
				fogEnabled = true;
				updateFog();
				theMainRepn.getRepresentationRoot().setVisible(true);
				theMainRepn.getRepresentationRoot().setPickable(true);
				skewerSGC.setVisible(true);
				skewerSGC.setPickable(true);
				theMainRepn.setActive(true);
				mainGenerators.setVisible(true);
				theMainRepn.getRepresentationRoot().addTool(skewerTool);
				skewerTool.activate();
				break;
			case DUPLICATOR:
				sgp = new SceneGraphPath(viewer.getSceneRoot(), theWorld, theDuplicatorRepn.getRepresentationRoot());
				toolSystem.setEmptyPickPath(sgp);
				theDuplicatorRepn.getRepresentationRoot().addTool(replicatorTool);
				theDuplicatorRepn.getRepresentationRoot().addTool(repRotateTool);
				theDuplicatorRepn.getRepresentationRoot().setVisible(true);
				theDuplicatorRepn.setActive(true);
				replicatorGenerators.setVisible(true);
				fogEnabled = false;
				updateFog();
				break;
		}
		System.err.println("Avatar node = "+Rn.matrixToString(avatarNode.getTransformation().getMatrix()));
		currentTool = tool;
	}

	public void updateSpaceShipTform()	{
		if (shipSizeSl != null) shipSizeSl.setValue(spaceShipScale);
		double[] pointToMotion = P3.makeRotationMatrix(null, new double[]{0,0,1}, flyTool.getLastDirection());
		if (GlobalProperties.isPortal)
			MatrixBuilder.euclidean(new Matrix(pointToMotion)).scale(PortalCoordinateSystem.getPortalScale()*spaceShipScale).assignTo(spaceShip);
		else MatrixBuilder.euclidean(new Matrix(pointToMotion)).scale(spaceShipScale).assignTo(spaceShip);
//		viewer.renderAsync();
	}
	
	private void updateGroupConstraint() {
		context3D = new Graphics3D(viewer);
		viewportConstraint = new DiscreteGroupViewportConstraint(minD, minW, maxD, maxW, context3D);
		viewportConstraint.setZtlate(zTlate);
		if (zTlateSlider != null) {
			if (minD > 0) minDSl.setValue(minD);
			if (maxD > 0) maxDSl.setValue(maxD);
			maxNSl.setValue((double)maxNumElements);
			zTlateSlider.setValue( zTlate);
		}
		theMainRepn.setViewportConstraint(viewportConstraint);
		theMainRepn.setClipToCamera(clipToCamera);
		theMainRepn.update();			
	}
	
	public void updateGeometry()	{
		if (beamRad != null) beamRad.setValue(radius);
		dirdom.update();
//		standardFundDomain =  DiscreteGroupUtility.calculateDirichletDomain(null, theGroup);
		standardFundDomain =  dirdom.getDirichletDomain();
		((WingedEdge)scaledDirDom.getDirichletDomain()).setColoredFaces(false);
		scaledDirDom.update();
		scaledFundDomain =  scaledDirDom.getDirichletDomain();
		if (doNotKnot) standardFundDomain.setGeometryAttributes("doNotKnot", true);
//		double[][] verts = standardFundDomain.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
//		double[] center = Rn.average(null, verts);
//		theGroup.setCenterPoint(center);
//		System.err.println("setting center to "+Rn.toString(center));
//		standardFundDomain =  DiscreteGroupUtility.calculateDirichletDomain(null, theGroup);
//		scaledFundDomain =  DiscreteGroupUtility.calculateDirichletDomain(null, theGroup);

		if (theGroup.getName().startsWith("borromean")) {
			BorromeanUtility.colorEdges((WingedEdge) standardFundDomain);
			BorromeanUtility.colorFaces((WingedEdge) scaledFundDomain);
		}
		letterSGC = SceneGraphUtility.createFullSceneGraphComponent("letterSGC");
//		MatrixBuilder.euclidean().scale(2).assignTo(letterSGC);
//		facesAndEdgesSGC = SceneGraphUtility.createFullSceneGraphComponent("faces and edges");
//		standardFundDomain.setFaceAttributes(Attribute.COLORS, null);
//		Appearance ap = facesAndEdgesSGC.getAppearance();
//		ap.setAttribute(TRANSPARENCY, 1.0);
//		ap.setAttribute(OPAQUE_TUBES_AND_SPHERES, false);
//		ap.setAttribute(FACE_DRAW, true);
//		ap.setAttribute(EDGE_DRAW, true);
//		ap.setAttribute(VERTEX_DRAW, false);
		
//		facesAndEdgesSGC.setGeometry(standardFundDomain);
		scaledDD = new SceneGraphComponent("scaledDD");
		if (theGroup.getMetric() == Pn.EUCLIDEAN)	{
			IndexedFaceSet square = GeometryUtilityOverflow.plainQuadMesh(.75, .75, 2, 2);
			MatrixBuilder.euclidean().translate(-1,1,0).rotateZ(Math.PI).rotateY(Math.PI).assignTo(scaledDD);
			letterSGC.setGeometry(square);
			letterSGC.setVisible(showLetters);
			Appearance ap = new Appearance();
			ap.setAttribute(FACE_DRAW, true);
			ap.setAttribute(EDGE_DRAW, false);
//			ap.setAttribute(FAST_AND_DIRTY, false);
			letterSGC.setAppearance(ap);
			BufferedImage im = LabelUtility.createImageFromString("d",new Font("Sans Serif",Font.BOLD,192), Color.yellow);
			Texture2D texture2d = TextureUtility.createTexture(ap, POLYGON_SHADER,new ImageData(im));
			texture2d.setRepeatS(Texture2D.GL_CLAMP);
			texture2d.setRepeatT(Texture2D.GL_CLAMP);			
		} //else {
		updateGeometryScale();
//		scaledFundDomain.setGeometryAttributes(GeometryUtility.METRIC, theGroup.getMetric());
//		IndexedFaceSetUtility.calculateAndSetFaceNormals(scaledFundDomain);
		scaledFundDomain.setName("scaled fundamental domain");
		//			MatrixBuilder.init(null,theGroup.getMetric()).scale(stretchFactor).assignTo(scaledDD);
		scaledDD.setGeometry(scaledFundDomain);
		scaledDD.setTransformation(new Transformation());
		//}
//		AABBTree aabb = AABBTree.construct(standardFundDomain, 10);
//		standardFundDomain.setGeometryAttributes("AABBTree", aabb);
		geometrySGC = new SceneGraphComponent("theDD");
		geometrySGC.addChildren(scaledDD, letterSGC);
		geometrySGC.setVisible(showGeometry);
		Appearance ap = new Appearance();
		ap.setAttribute(LINE_SHADER+"."+DIFFUSE_COLOR, java.awt.Color.WHITE);
		scaledDD.setVisible(showDirDom);
		letterSGC.setVisible(showLetters);
		geometrySGC.setAppearance(ap);
		beams = WingedEdgeUtility.createBeamsOnEdges((WingedEdge) standardFundDomain, null, radius, 4, 5);
		beams.setVisible(showBeams);
		beams.setPickable(false);
//		beamsWithoutTex = new Appearance();
		beamsWithTex = new Appearance();
//		beamsWithoutTex.setAttribute(EDGE_DRAW, false);
		beamsWithTex.setAttribute(EDGE_DRAW, false);
		beams.setAppearance(beamsWithTex);
//		if (doTexture) {
			if (true || textureFile == null)	{
				RopeTextureFactory stf = new RopeTextureFactory(beamsWithTex);
				stf.setN(15);
				stf.setM(1);
				stf.setBand2color(new Color(255,255,50));
				stf.setShadowwidth(.05);
				stf.setBandwidth(.8);
				stf.update();
				tex2d = stf.getTexture2D();
				System.err.println("Using rope texture");
	//			SimpleTextureFactory stf = new SimpleTextureFactory();.01
	//			stf.update();
	//			ImageData id = stf.getImageData();
	//			double scale = 12;
	//
			} else  {
				tex2d = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, 
									"polygonShader.texture2d", beamsWithTex, true);
				// try {
				URL is = ResourceClass.class.getResource("resources/textures/wood_boards.jpg");
			    ImageData id = null;
				try {
					id = ImageData.load(new Input(is));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //weaveRGBABright.png"));
				  //Image id = Toolkit.getDefaultToolkit().getImage("/homes/geometer/gunn/Software/workspace/discreteGroup/src/discreteGroup/resources/textures/wood_boards.jpg");
			      //tex2d.setImage(new ImageData(id));
			    tex2d.setImage(id);
			}
			thumbnailImage = SimpleTextureFactory.getScaledImage(tex2d.getImage().getOriginalImage(), thumbnailSize, (thumbnailSize*tex2d.getImage().getHeight())/tex2d.getImage().getWidth());
		tex2d.setApplyMode(Texture2D.GL_MODULATE);
	    tex2d.setMinFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
	    tex2d.setMagFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);			
		updateTexture();

	    collector.setAppearance(new Appearance());
		collector.addChildren(beams, geometrySGC, stickTipSGC);

		SceneGraphUtility.removeChildren(generators);
		MatrixBuilder.euclidean().assignTo(generators);
		if (theGroup.getMetric() == Pn.EUCLIDEAN) 
			Platycosm.getGeneratorsAsSGC(generators, theGroup.getName(), generatorThickness);
		
		if (allRoundTool == null)	{
			allRoundTool = new AllroundTool();
			//		st.setAttachmentPath(pathToWorld);
			allRoundTool.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					//AllroundTool  at = ((AllroundTool) e.getSource());
					setComponentDisplayLists();
//					if (!InteractiveViewer.environment.equals("desktop")) {
//						if (!allRoundTool.isActive())		{		// test out stick tip tracing
//							if (!theWorld.getTools().contains(traceTool) )
//									theWorld.addTool(traceTool);
//						} else  {
//							if (theWorld.getTools().contains(traceTool) )
//									theWorld.removeTool(traceTool);
//						}						
//					}
					System.err.println("Allround tool active changed to "+allRoundTool.isActive());
					//System.err.println("trace tool is there: "+theWorld.getTools().contains(traceTool));
					double matrix[] = new double[16];
					if (allRoundTool.getSelection() == null) return;
					allRoundTool.getSelection().getMatrix(matrix);
					
					MatrixBuilder.euclidean(new Matrix(matrix)).assignTo(generators);
					viewer.renderAsync();
				}
				
			});
//			if (!InteractiveViewer.environment.equals("desktop")) 
//				theWorld.addTool(traceTool);
		}
	}

	private void updateTexture() {
		if (doTexture)	{
			TextureUtility.createTexture(beamsWithTex, "polygonShader", tex2d.getImage());
			thumbnailImage = SimpleTextureFactory.getScaledImage(tex2d.getImage().getOriginalImage(), thumbnailSize, (thumbnailSize*tex2d.getImage().getHeight())/tex2d.getImage().getWidth());
		} else {
			beamsWithTex.setAttribute("polygonShader.texture2d", Appearance.INHERITED);			
		}
		if (filedropButton != null) {
			filedropButton.setIcon(new ImageIcon(thumbnailImage));
			filedropButton.setSelected(doTexture);
		}
	}
	
	double[][] falloffs = {{1,.15,0},{.5,.3,0},{.5, 1, 0}};
	double distance = 1.5;
	double[] unitD = {Math.tanh(2.0), 3.0, Math.tan(1.5)};
	private void updateShader() {
		Appearance ap = theWorld.getAppearance();
		ap.setAttribute("useGLSL",useGLSL && theGroup.getMetric() != Pn.EUCLIDEAN);
		ap.setAttribute("oneGLSL",true);
		if (useGLSL && theGroup.getMetric() != Pn.EUCLIDEAN)	{
			ap.setAttribute("useVertexArrays", false); //useVertexArrays);
			if (useVertexArrays) {
				ap.setAttribute("polygonShader", GlslPolygonShader.class);	
				ap.setAttribute("lineShader.polygonShader",  GlslPolygonShader.class);							
			}
			ap.setAttribute("useGLSL", useGLSL);				
		} else {
			ap.setAttribute("polygonShader", Appearance.INHERITED);	
			ap.setAttribute("lineShader.polygonShader", Appearance.INHERITED);
			ap.setAttribute("useGLSL", false);
		}
//		beams.setAppearance(beamsWithTex);
		ap.setAttribute(LIGHTING_ENABLED, theGroup.getMetric() == Pn.EUCLIDEAN || useGLSL);
		ellipticLights.setVisible(theGroup.getMetric() == Pn.ELLIPTIC);
		hyperbolicLights.setVisible(theGroup.getMetric() == Pn.HYPERBOLIC);
		euclideanLights.setVisible(theGroup.getMetric() == Pn.EUCLIDEAN);
		
		pointLight1.setFalloff(falloffs[theGroup.getMetric()+1]);
		pointLight2.setFalloff(falloffs[theGroup.getMetric()+1]);
		if (attenSl != null && attenSl[0] != null) {
			attenSl[0].setValue(falloffs[theGroup.getMetric()+1][0]);
			attenSl[1].setValue(falloffs[theGroup.getMetric()+1][1]);
		}
		MatrixBuilder.init(null, theGroup.getMetric()).
			translate(0,0,unitD[theGroup.getMetric()+1]).assignTo(lightSGC1);
		MatrixBuilder.init(null, theGroup.getMetric()).
			translate(0,.5*unitD[theGroup.getMetric()+1],.5*unitD[theGroup.getMetric()+1]).assignTo(lightSGC2);
	}
	protected void setComponentDisplayLists() {
		theMainRepn.getDropBox().setComponentDisplayLists( componentDisplayLists &&
			!allRoundTool.isActive()  &&
			!(showCameraRepn));
	}

	JRWindow win;
	JRWindowManager winMan;
	boolean active = true;

	private TextSlider zTlateSlider,
		maxNSl,
		maxDSl,
		minDSl,
		attenSl[],
		fogSl,
		portalSl,
		speedSl,
		beamRad, 
		stretch, 
		genthickSl, 
		shipSizeSl;

	private JCheckBox clipCameraBox;

	private JCheckBox followCameraBox;
	
	SceneGraphPath camPath=null;
	public  SceneGraphPath createDefaultCameraPath(final SceneGraphComponent sceneRoot)	{
		if (camPath != null) return camPath;
		avatarNode = SceneGraphUtility.createFullSceneGraphComponent("avatar");
		sceneRoot.addChild(avatarNode);
		SceneGraphComponent cameraNode = new SceneGraphComponent("camera");
		cameraNode.setTransformation(new Transformation());
		Camera c = new Camera();
		avatarNode.addChild(cameraNode);
		cameraNode.setCamera(c);
		SceneGraphPath p = new SceneGraphPath(); // (SceneGraphPath) l.get(0);
		p.push(sceneRoot);
		p.push(avatarNode);
		p.push(cameraNode);
		p.push(c);
		flyTool = new FlyTool2();
		flyTool.setGain(flySpeed);
		flyTool.addChangeListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				updateSpaceShipTform();
			}
			
		});
		avatarNode.addTool(flyTool);			
//		} else 
		if (GlobalProperties.isPortal) {
			c.setNear(.1);
			c.setFar(50.0);
			c.setOnAxis(false);
			c.setStereo(true);
			c.setEyeSeparation(PortalCoordinateSystem
					.convertMeters(PortalCoordinateSystem
							.getEyeSeparationMeters())); // based on eye
															// separation of 7
															// cm = .07 meters

//			RemotePortalHeadMoveTool rphmt = new RemotePortalHeadMoveTool();
//			cameraNode.addTool(rphmt);
//			avatarNode.addTool(new PointerDisplayTool());
		} 
		else {
			sceneRoot.addTool(new ClickWheelCameraZoomTool());
			//avatarNode.addTool(new PointerDisplayTool());
		}

		// TODO
		//SimpleScaleTool sc =  new SimpleScaleTool();
		//scalerNode.addTool(sc);
		// prepare two lists of lights: one euclidean, one noneuclidean
		SceneGraphComponent lightNode=new SceneGraphComponent("light 1");
		SceneGraphComponent lightNode2=new SceneGraphComponent("light 2");
		SceneGraphComponent lightNode3=new SceneGraphComponent("light 3");
		SceneGraphComponent lightNode4=new SceneGraphComponent("light 4");
		euclideanLights = new SceneGraphComponent("Lights");
		DirectionalLight light = new DirectionalLight();
		double intensity = .6;
		light.setIntensity(intensity);
		lightNode.setLight(light);
		MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{1,-1,1}).assignTo(lightNode);
		euclideanLights.addChild(lightNode);

		lightNode2.setLight(light);
		MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{-1,1,1}).assignTo(lightNode2);
		euclideanLights.addChild(lightNode2);

		lightNode3.setLight(light);
		MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{1,1,-1}).assignTo(lightNode3);
		euclideanLights.addChild(lightNode3);

		lightNode4.setLight(light);
				MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{-1,-1,-1}).assignTo(lightNode4);
		euclideanLights.addChild(lightNode4);
		avatarNode.addChild(euclideanLights);				

		hyperbolicLights = new SceneGraphComponent();
		lightSGC1 = SceneGraphUtility.createFullSceneGraphComponent("l1");
//		lightSGC1.addChild(Primitives.sphere(.05, 0,0,0));
		lightSGC1.getAppearance().setAttribute("polygonShader.diffuseColor",Color.WHITE);
 		pointLight1 = new PointLight();
		pointLight1.setColor(Color.white);
 		pointLight1.setIntensity(1.0);
   		lightSGC1.setLight(pointLight1);
  		hyperbolicLights.addChild(lightSGC1);
		lightSGC2 = SceneGraphUtility.createFullSceneGraphComponent("l2");
//		lightSGC2.addChild(Primitives.sphere(.05, 0,0,0));
		lightSGC2.getAppearance().setAttribute("polygonShader.diffuseColor",new Color(255, 255, 200));
 		pointLight2 = new PointLight();
  		pointLight2.setColor(new Color(255, 255, 200));
 		pointLight2.setIntensity(1.0);
  		lightSGC2.setLight(pointLight2);
  		hyperbolicLights.addChild(lightSGC2);
  		MatrixBuilder.hyperbolic().translate(.2, .2, .2).assignTo(lightSGC2);
		avatarNode.addChild(hyperbolicLights);
			
		ellipticLights = new SceneGraphComponent();
		ellipticLights.setName("elliptic lights");
		double[][] positions = {{0,0,0,1},{-1, -1,.5,0},{0,1,.5,0}, {-1,-1,-1,0}};
		double[] zaxis = {0,0,1,0}, mzaxis = {0,0,-1,0};
		for (int i = 0; i<3; ++i)	{
			double[] axis = {-1.2,-1.6,-2,1};
			if (i > 0) axis[i-1] = 1; 
			else axis = new double[]{1.8,1.3,1,1};
			SceneGraphComponent l0 = SceneGraphUtility.createFullSceneGraphComponent("light0");
//			PointLight dl = new PointLight();
			DirectionalLight dl = new DirectionalLight();
			int tc[] = {255, 255, 255};
			if (i < 3) tc[i] = 200;
			dl.setColor(new Color(tc[0], tc[1], tc[2]));
			dl.setIntensity(.45);
			l0.getTransformation().setMatrix( P3.makeTranslationMatrix(null, zaxis, positions[i], Pn.ELLIPTIC));		
			l0.setLight(dl);
			ellipticLights.addChild(l0);
//			l0 = SceneGraphUtility.createFullSceneGraphComponent("mlight0");
//			l0.getTransformation().setMatrix( P3.makeTranslationMatrix(null, mzaxis, positions[i], Pn.ELLIPTIC));		
//			lightNode.addChild(l0);
		}
 		avatarNode.addChild(ellipticLights);

		
		avatarNode.setPickable(true);
//		avatarNode.getAppearance().setAttribute("metric", Pn.EUCLIDEAN);
		if (GlobalProperties.isPortal) {
			portalScaleSGC2 = new SceneGraphComponent("portalScale");
			avatarNode.addChild(portalScaleSGC2);
			Appearance ap = new Appearance();
			ap.setAttribute(METRIC, Pn.EUCLIDEAN);
			portalScaleSGC2.setAppearance(ap);
			PortalCoordinateSystem.addChangeListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					updatePortalScale();
				}
				
			});
		} else {
			SceneGraphComponent windowSGC = new SceneGraphComponent("window");
			cameraNode.addChild((windowSGC));
		}
		camPath = p;
		return p;

		
	}

	private void setupInspectorTrigger() {
//		inspectorTriggerSGC = new SceneGraphComponent("trigger");
		inspectorTriggerSGC.setAppearance(new Appearance());
		inspectorTriggerSGC.getAppearance().setAttribute(CommonAttributes.METRIC, Pn.EUCLIDEAN);
		inspectorTriggerSGC.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		inspectorTriggerSGC.setGeometry(
				Primitives.texturedQuadrilateral(new double[]{0,0,0, -1,0,0,  -1,1,0,  0,1,0})); //ifsf.getIndexedFaceSet());
		BufferedImage bi = LabelUtility.createImageFromString("M",new Font("SansSerif",Font.BOLD,64),Color.green);
		ImageData id = new ImageData(bi);
//		Texture2D tex2d2 = //(Texture2D) AttributeEntityUtility.createAttributeEntity(
//				//Texture2D.class, "polygonShader.texture2d", inspectorTriggerSGC.getAppearance(), true);
		Texture2D tex2d2 = TextureUtility.createTexture(inspectorTriggerSGC.getAppearance(), POLYGON_SHADER,id);
//		tex2d2.setImage(id);
//		tex2d2.setApplyMode(Texture2D.GL_DECAL);
		Matrix mm = new Matrix();
		MatrixBuilder.euclidean().scale(1,-1,1).assignTo(mm);
		tex2d2.setTextureMatrix(mm);
		updateInspectorTrigger();
		AbstractTool atool = new AbstractTool()	{
			{
				addCurrentSlot(InputSlot.getDevice("PointerTransformation"));
			}
			public void perform(ToolContext tc)	{
				PickResult pr = tc.getCurrentPick();
				System.err.println("picking trigger");
//				if (pr != null && 
//					pr.getPickPath() != null &&
//					pr.getPickPath().getLastComponent() == inspectorTriggerSGC) {
//					if (active == false)	{
//						active = true;
//						if (win != null) win.getFrame().setVisible(true);
//					}
//				} else {
//					if (active == true) {
//						active = false;							
//					}
//				}
			}
		};
//		inspectorTriggerSGC.addTool(atool);
		inspectorTriggerSGC.setVisible(showTrigger);
		avatarNode.addChild(inspectorTriggerSGC);
	}

	double scale = .25;

	private void updateInspectorTrigger() {
		double s = 1.0;
		double[] tlate;
		if (GlobalProperties.isPortal) {
			s = PortalCoordinateSystem.getPortalScale();
			tlate = portalTlate;
		} else {
			Rectangle2D vp = CameraUtility.getViewport(CameraUtility.getCamera(viewer), CameraUtility.getAspectRatio(viewer));
			tlate = new double[]{vp.getMaxX(), -vp.getMaxY(), -1.01};
		}
		MatrixBuilder.euclidean().scale(s).translate(tlate).scale(scale).assignTo(inspectorTriggerSGC);
	}

	private void updatePortalScale() {
		double s = PortalCoordinateSystem.getPortalScale();
		updateSpaceShipTform();
		if (tinmanfactory != null) tinmanfactory.setStandingEyeLevel(s*1.7);
		if ( !SystemProperties.isPortal) return;
		System.err.println("In updateportalscale() "+s);
		MatrixBuilder.euclidean().scale(s).assignTo(portalScaleSGC2);
		MatrixBuilder.euclidean().scale(s).translate(portalTlate).scale(scale).assignTo(inspectorTriggerSGC);
		CameraUtility.getCamera(viewer).setEyeSeparation(0.07*s);
	}
	public void cleanupTrigger(JRViewer jrv)	{
		// following is a bit pointless since it's already had its undesired effect
		SceneGraphPath sgp = SceneGraphUtility.getPathsBetween(viewer.getSceneRoot(), inspectorTriggerSGC).get(0);
		jrv.getPlugin(Scene.class).setBackdropPath(sgp);
		// this is right however
		spp.setTriggerComponent(inspectorTriggerSGC);
	}
	public void setupTabs(JRViewer jrv)	{
		//JScrollPane scroll = new JScrollPane(tabs);
		spp = JRViewer.createSceneShrinkPanel(tabs, "maniview");
		// following gets written over by the scene's install method, but 
		// I can't overwrite scene's "backdropPath" until I've created the scene graph,
		// but for that I apparaently need the viewer.  so ... see cleanupTrigger() above.
		spp.setTriggerComponent(inspectorTriggerSGC);
		jrv.registerPlugin(spp);
	}
	JTabbedPane tabs  = new JTabbedPane();;

	private JRadioButton[] groupButtons;
	private HashMap<String, JRadioButton> nameToButton = new HashMap<String, JRadioButton>();

	private JButton loadB;

	private ButtonGroup geometryBG;

	private Box camRepnBox;

	private Box geomBox;

	private Image thumbnailImage = null, noneThumbnailImage;
	private int thumbnailSize = 64;

	private JButton filedropButton;

	private JCheckBox doTexCB;

	private Box replicatorInspector = Box.createHorizontalBox();
	private CopyClickTool replicatorTool;
	private SkewerTool skewerTool;

	private SceneGraphComponent replicatorRoot;

	private Tool repRotateTool;

	private boolean pullDownMenusWork = true;

	private SceneShrinkPanel spp;
	
	Timer portalScaleTimer = new Timer();
	public void insertTabs(final JRWindow win) {
		if (tabs == null) initTabs();
		win.getFrame().getContentPane().add(tabs);
	}
	void initTabs()	{
		
		
		Box bigBox = Box.createVerticalBox();
		tabs.addTab("Content", bigBox);
		
		Box vbox = Box.createVerticalBox();
		TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Parameters");
		vbox.setBorder(title);
		bigBox.add(vbox);
		beamRad = new TextSlider.DoubleLog("beam radius",
				SwingConstants.HORIZONTAL,.001,.3,radius);
		beamRad.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				radius = beamRad.getValue().doubleValue();
				WingedEdgeUtility.createBeamsOnEdges(beams,(WingedEdge) standardFundDomain,(IndexedFaceSet) beams.getGeometry(), radius, 4, 5);
				viewer.renderAsync();
			}
			
		});
		vbox.add(beamRad);
		stretch = new TextSlider.DoubleLog("geometry scale",
				SwingConstants.HORIZONTAL,.001,1,stretchFactor);
		stretch.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				stretchFactor = stretch.getValue().doubleValue();
				updateGeometryScale();
				viewer.renderAsync();
			}
			
		});
		vbox.add(stretch);
								
		genthickSl = new TextSlider.DoubleLog("generator thickness",
				SwingConstants.HORIZONTAL,.001,.2,generatorThickness);
		genthickSl.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				generatorThickness = genthickSl.getValue().doubleValue();
				SceneGraphNode.setThreadSafe(preTS);
				SceneGraphUtility.removeChildren(generators);
				Platycosm.getGeneratorsAsSGC(generators, groupName, generatorThickness);
				SceneGraphNode.setThreadSafe(postTS);
				viewer.renderAsync();
			}
			
		});
		vbox.add(genthickSl);
				
		
		shipSizeSl = new TextSlider.DoubleLog("ship scale",
				SwingConstants.HORIZONTAL,.01,10,spaceShipScale);
		shipSizeSl.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				spaceShipScale = shipSizeSl.getValue().doubleValue();
				updateSpaceShipTform();
			}
		});
		vbox.add(shipSizeSl);

		Box hbox = Box.createHorizontalBox();
		bigBox.add(hbox);
		title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Toggles");
		hbox.setBorder(title);
		Box vbox2 = Box.createVerticalBox();
		hbox.add(Box.createHorizontalGlue());
		hbox.add(vbox2);
//		Box vbox2 = Box.createHorizontalBox();
		title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Visibility");
		vbox2.setBorder(title);

		JCheckBox jcb = new JCheckBox("Beams");
		jcb.setSelected(showBeams);
		jcb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				showBeams = ((JCheckBox)e.getSource()).isSelected();
				beams.setVisible(showBeams);
				viewer.renderAsync();
			}
			
		});
		vbox2.add(jcb);
		
		jcb = new JCheckBox("Geometry");
		jcb.setSelected(showGeometry);
		jcb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				showGeometry = ((JCheckBox)e.getSource()).isSelected();
				geometrySGC.setVisible(showGeometry);
				geomBox.setEnabled(showGeometry);
				viewer.renderAsync();
			}
			
		});
		vbox2.add(jcb);
		JCheckBox genCB = new JCheckBox("Generators");
		genCB.setSelected(showGenerators);
		genCB.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				showGenerators = ((JCheckBox)e.getSource()).isSelected();
				generators.setVisible(showGenerators);
				viewer.renderAsync();
			}
			
		});
		vbox2.add(genCB);
		JCheckBox camRCB = new JCheckBox("Camera Repn");
		camRCB.setSelected(showCameraRepn);
		camRCB.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				showCameraRepn = ((JCheckBox)e.getSource()).isSelected();
				theCameraSGN.setVisible(showCameraRepn);
				camRepnBox.setEnabled(showCameraRepn);
				viewer.renderAsync();
			}
			
		});
		vbox2.add(camRCB);
		
		geomBox = Box.createVerticalBox();
		geomBox.setEnabled(showGeometry);
		title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Geometry");
		geomBox.setBorder(title);
		hbox.add(Box.createHorizontalGlue());
		hbox.add(geomBox);
		geometryBG = new  ButtonGroup();
		jcb = new JCheckBox("Dirichlet domain");
		jcb.setSelected(showDirDom);
		jcb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				showDirDom = ((JCheckBox)e.getSource()).isSelected();
				showLetters = !showDirDom;
				handleGeometryDisplay();
			}
			
		});
		geomBox.add(jcb);
		geometryBG.add(jcb);
		
		jcb = new JCheckBox("Letter");
		jcb.setSelected(showLetters);
		jcb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				showLetters = ((JCheckBox)e.getSource()).isSelected();
				showDirDom = !showLetters;
				handleGeometryDisplay();
			}
			
		});
		geometryBG.add(jcb);
		geomBox.add(jcb);
		
		camRepnBox = Box.createVerticalBox();
		camRepnBox.setEnabled(showCameraRepn);
		title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Camera Repn");
		camRepnBox.setBorder(title);
		hbox.add(Box.createHorizontalGlue());
		hbox.add(camRepnBox);
		ButtonGroup bg = new  ButtonGroup();
		jcb = new JCheckBox("Space Ship");
		jcb.setSelected(showSpaceShip);
		jcb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				showSpaceShip = (((JCheckBox)e.getSource()).isSelected());
				handleCameraRepn(1,showSpaceShip);
			}
			
		});
		bg.add(jcb);
		camRepnBox.add(jcb);
		jcb = new JCheckBox("Avatar");
		jcb.setSelected(showAvatar);
		jcb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				showAvatar = (((JCheckBox)e.getSource()).isSelected());
				handleCameraRepn(2,showAvatar);
			}
			
		});
		bg.add(jcb);
		camRepnBox.add(jcb);
		hbox.add(Box.createHorizontalGlue());
		
		hbox = Box.createHorizontalBox();
		title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Texture drag and drop");
		hbox.setBorder(title);
		
		hbox.add(Box.createHorizontalGlue());
		noneThumbnailImage = LabelUtility.createImageFromString("None", null, Color.white);
		final JButton noneButton = new JButton(new ImageIcon(noneThumbnailImage));
		noneButton.setSelected(!doTexture);
		hbox.add(noneButton);
		hbox.add(Box.createHorizontalGlue());
		ButtonGroup texbut = new ButtonGroup();
		texbut.add(noneButton);
		noneButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				doTexture = false;
				updateTexture();
			}
			
		});
		//ImageIcon icon = new ImageIcon(thumbnailImage);
		filedropButton = new JButton();
		if (thumbnailImage != null) filedropButton.setIcon(new ImageIcon(thumbnailImage));
//		filedropButton.setPreferredSize(new Dimension(50,50));
//		filedropButton.setBackground(Color.red);
		hbox.add(filedropButton);
		texbut.add(filedropButton);
		filedropButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				doTexture = true;
				updateTexture();
			}
			
		});
		FileDrop fileDrop = new FileDrop(filedropButton, new FileDrop.Listener() {

			public void filesDropped(File[] arg0) {
				if (arg0.length == 0) return;
	 			try {
					ImageData id2 = ImageData.load(Input.getInput(arg0[0]));
					tex2d.setImage(id2);
					doTexture  = true;
					updateTexture();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.err.println("File dropped"+arg0[0].getName());
			}
			
		});
		hbox.add(Box.createHorizontalGlue());
		bigBox.add(hbox);
		
		bigBox.add(Box.createVerticalGlue());
		
		JPanel panel = new JPanel(new BorderLayout());
		Box newvbox = Box.createVerticalBox();
		
		
		// Group selection tab
		vbox = Box.createVerticalBox();
		title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Group selection");
		vbox.setBorder(title);
		
		if (pullDownMenusWork)	{
			// list all the groups in the menu
			hbox = Box.createHorizontalBox();
			vbox.add(hbox);
			JMenuBar menubar = new JMenuBar();
			menubar.setMaximumSize(new Dimension(1000,30));
			hbox.add(menubar);
			JMenu groupM = new JMenu("Platycosm");
			bg = new ButtonGroup();
			final String[] gnames = Platycosm.names;;
			for (int i = 0; i<gnames.length; ++i)	{
				final int j = i;
				JMenuItem jm = groupM.add(new JRadioButtonMenuItem(gnames[i]));
				jm.addActionListener( new ActionListener() {
					public void actionPerformed(ActionEvent e)	{
						replaceGroup(gnames[j]);
					}
				});
				bg.add(jm);
			}
//			theMenuBar.add(testM);
			menubar.add(groupM);

			int[] borromExamples = {2,3,4,5,6,7,8,10,15, -1};
			groupM = new JMenu("Borromean");
			bg = new ButtonGroup();
			for (int i = 0; i<borromExamples.length; ++i)	{
				final int j = borromExamples[i];
				JMenuItem jm = groupM.add(new JRadioButtonMenuItem("order "+j));
				jm.addActionListener( new ActionListener() {
					public void actionPerformed(ActionEvent e)	{
						DiscreteGroup dg = BorromeanUtility.borromeanGroupOfOrder(j);
						initializeGroup(dg);
						replaceGroup(dg);
					}
				});
				bg.add(jm);
			}
//			theMenuBar.add(testM);
			menubar.add(groupM);

			groupM = new JMenu("Irred. 3D Euc");
			bg = new ButtonGroup();
			final String[] irredEuc = {"1.","2.","2+","1.:2","2+:2","2..","4..","8..","1./4","2-/4"};
			for (int i = 0; i<irredEuc.length; ++i)	{
				final int j = i;
				JMenuItem rb = groupM.add( new JRadioButtonMenuItem(irredEuc[i]));
				rb.addActionListener( new ActionListener() {
					public void actionPerformed(ActionEvent e)	{
						DiscreteGroup dg = GroupGeneratorFactory.getD8Group(irredEuc[j]);
						initializeGroup(dg);
						replaceGroup(dg);
					}
				});
				
				bg.add(rb);
			}
			menubar.add(groupM);
			
			groupM = new JMenu("Other");
			JMenuItem jm = groupM.add(new JMenuItem("120-cell"));
			jm.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					replaceGroup(noneuclideanNames[1]);
				}
				
			});
			groupM.addSeparator();
			jm = groupM.add(new JMenuItem("600-cell"));
			jm.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					replaceGroup(noneuclideanNames[2]);
				}
				
			});
			groupM.addSeparator();
			jm = groupM.add(new JMenuItem("**"));
			jm.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					DiscreteGroup dg = CrystallographicGroup.instanceOfGroup("**");
					dg.setCenterPoint(new double[]{.2,.2,.2});
					initializeGroup(dg);
					replaceGroup(dg);
				}
				
			});
			groupM.addSeparator();
			jm = groupM.add(new JMenuItem("load..."));
			jm.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					replaceGroup("load");
				}
				
			});
			menubar.add(groupM);
			vbox.add(Box.createVerticalGlue());			
		}	else {
			vbox2 = Box.createVerticalBox();
			title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Platycosms");
			vbox2.setBorder(title);
			vbox.add(vbox2);
			Box hbox3 = Box.createHorizontalBox(),
				hbox4 = Box.createHorizontalBox();
			vbox2.add(hbox3);
			vbox2.add(hbox4);
			bg = new ButtonGroup();
			final String[] names = Platycosm.names;
			for (int i = 0; i<names.length; ++i)	{
				final int j = i;
				JRadioButton rb = new JRadioButton(names[i]);
				nameToButton.put(names[i], rb);
				rb.addActionListener( new ActionListener() {
					public void actionPerformed(ActionEvent e)	{
						replaceGroup(names[j]);
					}
				});
				
				bg.add(rb);
				if (i < names.length/2) hbox3.add(rb);
				else hbox4.add(rb);
			}
	
			vbox2 = Box.createVerticalBox();
//			vbox.add(vbox2);
			title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Irreducible Euclidean");
			vbox2.setBorder(title);
			vbox.add(vbox2);
			hbox3 = Box.createHorizontalBox();
			hbox4 = Box.createHorizontalBox();
			vbox2.add(hbox3);
			vbox2.add(hbox4);
			bg = new ButtonGroup();
			final String[] irredEuc = {"1.","2.","2+","1.:2","2+:2","2..","4..","8..","1./4","2-/4"};
			for (int i = 0; i<irredEuc.length; ++i)	{
				final int j = i;
				JRadioButton rb = new JRadioButton(irredEuc[i]);
				nameToButton.put(names[i], rb);
				rb.addActionListener( new ActionListener() {
					public void actionPerformed(ActionEvent e)	{
						DiscreteGroup dg = GroupGeneratorFactory.getD8Group(irredEuc[j]);
						initializeGroup(dg);
						replaceGroup(dg);
					}
				});
				
				bg.add(rb);
				if (i < names.length/2) hbox3.add(rb);
				else hbox4.add(rb);
			}
			JButton jm = new JButton("Other...");
			hbox4.add(jm);
			jm.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)	{
					String name = JOptionPane.showInputDialog("Enter a name for an irreducible 3D euclidean group:");		
					if (name != null && name.length() > 0) {
						DiscreteGroup dg = GroupGeneratorFactory.getD8Group(name);
						initializeGroup(dg);
						replaceGroup(dg);
					}
				}
			});

			
			vbox2 = Box.createVerticalBox();
			title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Borromean");
			vbox2.setBorder(title);
			vbox.add(vbox2);
			hbox3 = Box.createHorizontalBox();
			hbox4 = Box.createHorizontalBox();
			vbox2.add(hbox3);
			vbox2.add(hbox4);

			int[] borromExamples = {2,3,4,5,6,7,8,10,15, -1};
			bg = new ButtonGroup();
			for (int i = 0; i<borromExamples.length; ++i)	{
				final int j = borromExamples[i];
				JRadioButton rb = new JRadioButton("O("+borromExamples[i]+")");
				rb.addActionListener( new ActionListener() {
					public void actionPerformed(ActionEvent e)	{
						DiscreteGroup dg = BorromeanUtility.borromeanGroupOfOrder(j);
						initializeGroup(dg);
						replaceGroup(dg);
					}
				});
				bg.add(rb);
				if (i < borromExamples.length/2) hbox3.add(rb);
				else hbox4.add(rb);
			}
			
			vbox2 = Box.createVerticalBox();
			title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Other");
			vbox2.setBorder(title);
			vbox.add(vbox2);
			hbox3 = Box.createHorizontalBox();
			vbox2.add(hbox3);
			
			bg = new ButtonGroup();
			JRadioButton rb = new JRadioButton(noneuclideanNames[1]);
			bg.add(rb);
			rb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					replaceGroup(noneuclideanNames[1]);
				}
				
			});
			hbox3.add(rb);
			
			rb = new JRadioButton("*2222");
			bg.add(rb);
			rb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					DiscreteGroup dg = CrystallographicGroup.instanceOfGroup("*2222");
					dg.setCenterPoint(new double[]{.2,.2,.2,1});
					initializeGroup(dg);
					replaceGroup(dg);
				}
				
			});
			hbox3.add(rb);
			
			rb = new JRadioButton("load");
			rb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					replaceGroup("load");
				}
				
			});
			bg.add(rb);
			hbox3.add(rb);
						
		}


		
		newvbox.add(vbox);
		panel.add(BorderLayout.CENTER, newvbox);
		
		vbox = Box.createVerticalBox();
		title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Viewport constraint");
		vbox.setBorder(title);
		panel.add(BorderLayout.SOUTH, vbox);
		String[] labels = {"min word:", "max word:", "min distance", "max distance", "max number", "z tlate"};
		
		minDSl = new TextSlider.DoubleLog(labels[2],
				SwingConstants.HORIZONTAL,.1,10,minD);
		minDSl.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				minD = minDSl.getValue().doubleValue();
				System.err.println("Setting minD  to "+minD);
				updateGroupConstraint();
			}
			
		});
		vbox.add(minDSl);
					
		maxDSl = new TextSlider.DoubleLog(labels[3],
				SwingConstants.HORIZONTAL,.1,30,maxD);
		maxDSl.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				maxD = maxDSl.getValue().doubleValue();
				System.err.println("Setting maxD  to "+maxD);
				updateGroupConstraint();
			}
			
		});
		vbox.add(maxDSl);
		
		
		maxNSl = new TextSlider.IntegerLog(labels[4],
				SwingConstants.HORIZONTAL, 1, 10000,maxNumElements);
		maxNSl.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				maxNumElements = maxNSl.getValue().intValue();
				System.err.println("Setting maxN  to "+maxNumElements);
				DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(maxNumElements);
				masterList = DiscreteGroupUtility.generateElements(theGroup, dgsc);
				if (masterList != null) {
					theMainRepn.setElementList(masterList);
				}
			}
			
		});
		vbox.add(maxNSl);
				
		zTlateSlider = new TextSlider.DoubleLog(labels[5],
				SwingConstants.HORIZONTAL, .01, 3,ztlates[1]);
		zTlateSlider.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				zTlate = zTlateSlider.getValue().doubleValue();
				System.err.println("Setting ztlate to: "+zTlate);
				viewportConstraint.setZtlate(zTlate);
				viewer.renderAsync();
			}
			
		});
		vbox.add(zTlateSlider);
				
		tabs.addTab("Group", panel);
	
		JPanel globalP  = new JPanel(new BorderLayout());

		// global settings: fog, fly speed
		globalP = new JPanel();
		vbox = Box.createVerticalBox();
		globalP.add(vbox);
		Box hbox2 = Box.createHorizontalBox();
		hbox = Box.createVerticalBox();
		hbox2.add(Box.createHorizontalGlue());
		hbox2.add(hbox);
		vbox.add(hbox2);
		title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Flags");
		hbox.setBorder(title);
		followCameraBox = new JCheckBox("Follow camera");
		followCameraBox.setSelected(followCamera);
		followCameraBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				followCamera = (((JCheckBox)e.getSource()).isSelected());
				theMainRepn.setFollowsCamera(followCamera);
			}
			
		});
//		hbox.add(Box.createHorizontalGlue());
		hbox.add(followCameraBox);
//		hbox.add(Box.createHorizontalGlue());
		clipCameraBox = new JCheckBox("clip to camera");
		clipCameraBox.setSelected(clipToCamera);
		clipCameraBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				clipToCamera = (((JCheckBox)e.getSource()).isSelected());
				theMainRepn.setClipToCamera(clipToCamera);
			}
			
		});
		hbox.add(clipCameraBox);
//		hbox.add(Box.createHorizontalGlue());
//		hbox = Box.createHorizontalBox();
//		title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "GLSL");
//		hbox.setBorder(title);
		jcb = new JCheckBox("Enable GLSL");
		jcb.setSelected(useGLSL);
		jcb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				useGLSL = (((JCheckBox)e.getSource()).isSelected());
				updateShader();
			}
			
		});
		hbox.add(jcb);
//		vbox.add(hbox);
		hbox = Box.createVerticalBox();
		hbox2.add(Box.createHorizontalGlue());
		hbox2.add(hbox);
		hbox2.add(Box.createHorizontalGlue());
		title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Tools");
		hbox.setBorder(title);
		bg = new ButtonGroup();
		for (int i = 0; i<toolnames.length; ++i)	{
			final JRadioButton jrb = new JRadioButton(toolnames[i]);
			hbox.add(jrb);
			bg.add(jrb);
			final int j = i;
			jrb.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					setTool(ToolEnum.toolForName(toolnames[j]));
				}
				
			});
		}
		
		JButton resetAvatarB = new JButton("Reset avatar");
		hbox2.add(resetAvatarB);
		resetAvatarB.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				setupAvatarTforms();
			}
		});
		
		speedSl = new TextSlider.DoubleLog("fly speed",
				SwingConstants.HORIZONTAL, 0.01, 2.0, flySpeed);
		speedSl.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				flySpeed = speedSl.getValue().doubleValue();
				System.err.println("Setting speedSl  to "+flySpeed);
				flyTool.setGain(flySpeed);
			}
			
		});
		vbox.add(speedSl);
				
		portalSl = new TextSlider.DoubleLog("portalScale",
				SwingConstants.HORIZONTAL, 0.01, 2.0, portalScale);
		portalSl.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				portalScale = portalSl.getValue().doubleValue();
				TimerTask tt = new TimerTask() {
					
					@Override
					public void run() {
						PortalCoordinateSystem.setPortalScale(portalScale);
					}
				};
				portalScaleTimer.schedule(tt, 2000);
			}
			
		});
		vbox.add(portalSl);
				
		
		vbox2 = Box.createVerticalBox();
		title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Fog");
		vbox2.setBorder(title);
		hbox = Box.createHorizontalBox();
		vbox2.add(hbox);
		hbox.add(Box.createHorizontalGlue());
		jcb = new JCheckBox("Enabled");
		jcb.setSelected(fogEnabled);
		jcb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				fogEnabled = (((JCheckBox)e.getSource()).isSelected());
				updateFog();
			}
			
		});
		hbox.add(jcb);
		hbox.add(Box.createHorizontalGlue());

		fogSl = new TextSlider.Double("density",
				SwingConstants.HORIZONTAL, 0.0, 1.0, fogDensity);
		fogSl.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				fogDensity = fogSl.getValue().doubleValue();
				updateFog();
			}
			
		});
		vbox2.add(fogSl);
		vbox.add(vbox2);	
		
		vbox2 = Box.createVerticalBox();
		title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Light attenuation");
		vbox2.setBorder(title);
		vbox.add(vbox2);	
		String[] attlabels = {"Constant", "Linear", "Quadratic"};
		attenSl = new TextSlider[2];
		for (int i = 0; i<2; ++i)	{

			final int j = i;
			attenSl[i] = new TextSlider.DoubleLog(attlabels[i],
					SwingConstants.HORIZONTAL, 0.01, 2.0, falloffs[1][i]);
			attenSl[i].addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					double value =attenSl[j].getValue().doubleValue();
					if (j == 0)	{
						pointLight1.setFalloffA0(value);
						pointLight2.setFalloffA0(value);						
					} else if (j==1) {
						pointLight1.setFalloffA1(value);
						pointLight2.setFalloffA1(value);												
					} else if (j==2) {
						pointLight1.setFalloffA2(value);
						pointLight2.setFalloffA2(value);																		
					}
				}
				
			});
			vbox2.add(attenSl[i]);
		}
//		JButton but = new JButton("Make Cube Map");
//		but.addActionListener(new ActionListener() {
//			String[] suffixes = {"rt","lf","up","dn","ft","bk"};
//			public void actionPerformed(ActionEvent e) {
//				CameraUtility.getCameraNode(viewer).setVisible(false);
//				viewer.render();
//				BufferedImage[] images = ((de.jreality.jogl.JOGLViewer)viewer).renderCubeMap(256);
//				for (int i = 0; i<6; ++i)	{
//					ImageUtility.writeBufferedImage(new File("/homes/geometer/gunn/Pictures/grabs/cubeMapTest_"+suffixes[i]+".png"), images[i]);
//				}
//				CameraUtility.getCameraNode(viewer).setVisible(true);
//				viewer.render();
//			}
//			
//		});
//		vbox.add(but);
		vbox.add(replicatorInspector);
		
		tabs.addTab("Global", globalP);
				
		AppearancePanel apanel = new AppearancePanel();
		apanel.setAppearance(theWorld.getAppearance());
		tabs.addTab("App", apanel);

		JPanel mypanel = new JPanel();
		mypanel.setName("ReadMe");
		JTextArea textarea = new JTextArea(10,20);
		textarea.setEditable(false);
		textarea.append("This application allows the user to navigate.\n"+
				"within three dimensional manifold (orbifold) \n"+
				"of euclidean, hyperbolic or elliptic type.\n\n"+
				"Activate noneuclidean shading by clicking on\n"+
				"on 'GLSL' checkbox on 'Global' tab\n\n"+
				"To improve framerate, lower 'Max distance' on 'Group' tab\n\n"+
				"To fly, use the arrow keys \n"+
				"    (shift-left/right rolls.)\n\n"+
				"Right mouse click toggles shape tool:\n"+
				"When active, copy under cursor is selected\n"+
				"and left mouse rotates; middle mouse drags.\n\n"+
				"Explore other tabs on this inspector to set \n"+
				"parameters and load other manifolds.\n\n"+
				"'h' key toggles display of menu of \n"+
				"keystroke shortcuts for the viewer.\n\n"+
				"Use mouse click wheel to zoom in and out.\n"+
				"'z' key toggles stereo mode.\n"+
				"Shift-cntl-f  toggles fullscreen mode.\n"+
				"\nUse the 'View' menu to access \nscene graph navigator.\n\n"+
				"This application is a reincarnation of \n"+
				"the original maniview program developed at \n"+
				"the Geometry Center, UMn, in 1988-92.\n\n"+
				"\nAuthor: Charles Gunn\n"+
				"    gunn at math.tu-berlin.de\n");
		mypanel.add(textarea);
		JScrollPane scroller = new JScrollPane(mypanel);
		tabs.addTab("ReadMe",scroller);
		tabs.setSelectedComponent(globalP);
	}
	
	protected void handleGeometryDisplay()	{
		System.err.println("button group "+geometryBG.getSelection().toString());
		scaledDD.setVisible(showDirDom);
		letterSGC.setVisible(showLetters);
	}
	
	protected void handleCameraRepn(int i, boolean b) {
		switch(i)	{
		case 1:		// ship
			showSpaceShip = b;
			if (showSpaceShip)	{
				showAvatar = false;
			}
			break;
		case 2:		// avatar
			showAvatar = b;
			if (showAvatar)	{
				showSpaceShip = false;
			}
			break;
		}
		spaceShip.setVisible(showSpaceShip);
		avatarRepn.setVisible(showAvatar);
		
		if (GlobalProperties.isPortal)	{
			tmt.setActive(showAvatar);
		}
		setComponentDisplayLists();
		viewer.renderAsync();
	}

	private static IndexedFaceSet getBetterCamera()	{
		IndexedFaceSet result;
		SceneGraphComponent all = new SceneGraphComponent();
		MatrixBuilder.euclidean().scale(.15).assignTo(all);
		SceneGraphComponent baseCyl = SphereUtility.tessellatedCubeSphere(1); //closedCylinder(20, 1.0, -1, 1, Math.PI*2.0);
		SceneGraphComponent wings, body, tail, asymmetry;
		wings = SceneGraphUtility.createFullSceneGraphComponent("wings");
		wings.getAppearance().setAttribute("polygonShader.diffuseColor", Color.red);
		wings.getAppearance().setAttribute("pointShader.polygonShader.diffuseColor", Color.red);
		wings.addChild(baseCyl);
		MatrixBuilder.euclidean().translate(0,0,.8).rotateX(Math.PI/2).scale(1.2,.3, .025).assignTo(wings);
		asymmetry = SceneGraphUtility.createFullSceneGraphComponent("asymmetry");
		asymmetry.getAppearance().setAttribute("polygonShader.diffuseColor", Color.green);
		asymmetry.getAppearance().setAttribute("pointShader.polygonShader.diffuseColor", Color.green);
		asymmetry.addChild(baseCyl);
		MatrixBuilder.euclidean().translate(0,0,.8).rotateX(Math.PI/2).translate(.6,0,-.2).scale(.1,.1,.2).assignTo(asymmetry);
		body =  SceneGraphUtility.createFullSceneGraphComponent("body");
		body.getAppearance().setAttribute("polygonShader.diffuseColor", Color.yellow);
		body.getAppearance().setAttribute("pointShader.polygonShader.diffuseColor", Color.yellow);
		body.addChild(baseCyl);
		MatrixBuilder.euclidean().translate(0,0,1).rotateY(Math.PI/2).scale(1,.2, .1).assignTo(body);
		tail =  SceneGraphUtility.createFullSceneGraphComponent("wings");
		tail.getAppearance().setAttribute("polygonShader.diffuseColor", new Color(200, 150, 100));
		tail.getAppearance().setAttribute("pointShader.polygonShader.diffuseColor", new Color(200, 150, 100));
		tail.addChild(baseCyl);
		MatrixBuilder.euclidean().translate(0,.1,2).rotateX(-Math.PI/4).rotateY(Math.PI/2).scale(.2,.1, .02).assignTo(tail);
		all.addChildren(wings,body,tail,asymmetry);
		GeometryMergeFactory gmf = new GeometryMergeFactory();
		result = gmf.mergeGeometrySets(all);
		return result;
	}

	private void updateGeometryScale() {
		if (stretch != null) stretch.setValue(stretchFactor);
		double[] mat = MatrixBuilder.init(null,theGroup.getMetric()).scale(stretchFactor).getArray();
//		double[] tlate = P3.makeTranslationMatrix(null,theGroup.getCenterPoint(), theGroup.getMetric());
//		Rn.conjugateByMatrix(null, mat, tlate);
//		CopyVisitor cv = new CopyVisitor();
//		cv.visit(standardFundDomain);
//		scaledFundDomain = (IndexedFaceSet) cv.getCopy();
		
		double[][] verts = standardFundDomain.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		double[] center = Rn.average(null, verts);
		double[] tlate = P3.makeTranslationMatrix(null, center, theGroup.getMetric());
		mat = Rn.conjugateByMatrix(null, mat, tlate);
		verts = Rn.matrixTimesVector(null, mat, verts);
		scaledFundDomain.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(verts[0].length).createReadOnly(verts));
		IndexedFaceSetUtility.calculateAndSetFaceNormals(scaledFundDomain, Pn.EUCLIDEAN);//theGroup.getMetric());
		MatrixBuilder.euclidean().scale(stretchFactor).assignTo(letterSGC);
	}
}
