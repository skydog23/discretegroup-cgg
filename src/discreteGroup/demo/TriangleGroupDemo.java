/*
 * Created on Jan 29, 2004
 *
 */
package discreteGroup.demo;

import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.LIGHTING_ENABLED;
import static de.jreality.shader.CommonAttributes.SPHERES_DRAW;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY_ENABLED;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import net.iharder.dnd.FileDrop;
import charlesgunn.jreality.texture.SimpleTextureFactory;
import charlesgunn.jreality.texture.SimpleTextureFactory.TextureType;
import charlesgunn.jreality.tools.RotateShapeTool;
import charlesgunn.jreality.tools.TranslateShapeTool;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import charlesgunn.jreality.worlds.FundamentalTetrahedron;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.jogl.JOGLFBOViewer;
import de.jreality.jogl.shader.NoneuclideanGLSLShader;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.scene.Sky;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.event.CameraEvent;
import de.jreality.scene.event.CameraListener;
import de.jreality.scene.pick.Graphics3D;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.CameraUtility;
import de.jreality.util.DefaultMatrixSupport;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.core.DiscreteGroupViewportConstraint;
import de.jtem.discretegroup.groups.TriangleGroup;
import de.jtem.jrworkspace.plugin.Plugin;
import discreteGroup.ResourceClass;


public class TriangleGroupDemo extends Assignment {
	SceneGraphComponent 
		myroot, 
			theWorld,
				theWorldsChild,
					oneCopy,
				DGRepn,
			hyperbolicBoundary,
			diskHolder2,
				bullsEyeSGC,
			// ... in discrete group
			quadkit, 
				quadkitHolder,
					polarPlaneSGC,
				dragToolSGC, 
					sphereIconSGC,
				threeChildren[] = new SceneGraphComponent[3];
	// if I load in a geometry
	SceneGraphComponent loadedGeometry = null, readSGC = null;
	TriangleGroup theGroup ;
	int numCopies = 1000;
	double invisiblePointRadius = .1,
		blendFactor = 1,
		scale = 1.0;
	boolean showPoincare = false,
		flattenDGSGR = false,
		showOneCopy = false,
		draggingCenter = false,
		convertToProj = true,
		separateQuads = false,
		projectOntoSphere = false,
		componentDisplayLists = false,
		anyDisplayLists = true,
		copycat = true,
		threadSafe = true,
		singlePeer = true,
//		showPolarPlane = true,
		groupNeedsUpdated = false;
	PointSet thePoint;
	IndexedFaceSet 
		quads,
		bullsEye,
		face[] = new IndexedFaceSet[3];
	Texture2D[] textures = new Texture2D[3];
	ImageData[] images = new ImageData[4];
	String[] textureNames = {"gridSmall.jpg","cameraPath-01.jpg","butterfly.png"},
		textureUnitNames = {"","[1]","[2]"};
	SceneGraphPath root2worldPath;
	Timer camTimer = null;

	DiscreteGroupSceneGraphRepresentation sgr = null;
	String currentName = "*236";
	Graphics3D graphicsContext = null;
	DiscreteGroupViewportConstraint dgvc;

	DragCenterTool dragCenterTool;
	TranslateShapeTool translateTool;
	RotateShapeTool rotateTool;
	Viewer viewer;
	
		
	public SceneGraphComponent getContent()	{
		SceneGraphNode.setThreadSafe(threadSafe);
		myroot = SceneGraphUtility.createFullSceneGraphComponent("root");
		theWorld = SceneGraphUtility.createFullSceneGraphComponent("theWorld");
//		myroot.getAppearance().setAttribute(CommonAttributes.DEPTH_FUDGE_FACTOR, .9995);
		myroot.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		theWorldsChild = SceneGraphUtility.createFullSceneGraphComponent("theWorldsChild");
		theWorld.addChild(theWorldsChild);	
//		theWorld.addTool(new PickShowTool());
		
		hyperbolicBoundary = SceneGraphUtility.createFullSceneGraphComponent("boundary");
		hyperbolicBoundary.getAppearance().setAttribute("lineShader.diffuseColor",Color.WHITE);
		hyperbolicBoundary.setGeometry(IndexedLineSetUtility.circle(100)); 
		hyperbolicBoundary.setVisible(false);

		bullsEyeSGC = SceneGraphUtility.createFullSceneGraphComponent("disk holder 1");
		bullsEyeSGC.getAppearance().setAttribute(CommonAttributes.DIFFUSE_COLOR, Color.white);
		diskHolder2 = SceneGraphUtility.createFullSceneGraphComponent("disk holder 2");
		diskHolder2.addChild(bullsEyeSGC);
		diskHolder2.setVisible(false);
		bullsEyeSGC.getAppearance().setAttribute(TRANSPARENCY_ENABLED, true);
		bullsEyeSGC.getAppearance().setAttribute(EDGE_DRAW, false);
		PickUtility.setPickable(bullsEyeSGC, false, false, true);
		tex2d = (Texture2D) AttributeEntityUtility
	       .createAttributeEntity(Texture2D.class, "polygonShader.texture2d", bullsEyeSGC.getAppearance(), true);	
		SimpleTextureFactory stf = new SimpleTextureFactory();
		stf.setSize(128);
		stf.setType(TextureType.RING);
		stf.update();
		tex2d.setImage(stf.getImageData());
		tex2d.setApplyMode(Texture2D.GL_REPLACE);
		tex2d.setBlendColor(new Color(0f,0f,0f,0f));

		myroot.addChildren(theWorld,hyperbolicBoundary,diskHolder2);
		
//		dragTool.addChild(diskHolder);
		dragToolSGC = SceneGraphUtility.createFullSceneGraphComponent("drag tool");
		dragToolSGC.getAppearance().setAttribute(VERTEX_DRAW, true);
		dragToolSGC.getAppearance().setAttribute(SPHERES_DRAW, true);
		dragToolSGC.getAppearance().setAttribute(LIGHTING_ENABLED, true);
		dragToolSGC.getAppearance().setAttribute(TRANSPARENCY_ENABLED, true);
		dragToolSGC.getAppearance().setAttribute(TRANSPARENCY, 1.0);
		dragToolSGC.getAppearance().setAttribute("pointShader.pointRadius", invisiblePointRadius);
		dragToolSGC.getAppearance().setAttribute("pointShader.polygonShader.diffuseColor",Color.green);

		// this is just for show: smaller and non-pickable than the real thing
//		sphereIconSGC = SceneGraphUtility.createFullSceneGraphComponent("boundary");
//		Appearance ap = sphereIconSGC.getAppearance();
//		sphereIconSGC.setPickable(false);
//		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
//		ap.setAttribute(CommonAttributes.SPHERES_DRAW, true);
//		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, true);
//		ap.setAttribute("pointShader.pointRadius", .02);
//		ap.setAttribute("pointShader.polygonShader.diffuseColor",Color.yellow);
		for (int i = 0; i<3; ++i)	{
			threeChildren[i] = new SceneGraphComponent("face"+i);
			threeChildren[i].setAppearance(new Appearance());
			threeChildren[i].setVisible(separateQuads);
		}

		quadkit = SceneGraphUtility.createFullSceneGraphComponent("quadkit");
		quadkitHolder = SceneGraphUtility.createFullSceneGraphComponent("quadkitHolder");		
		polarPlaneSGC = SceneGraphUtility.createFullSceneGraphComponent("polarPlane");	
		PickUtility.setPickable(quadkitHolder, false, false, true);
		polarPlaneSGC.setPickable(false);
		quadkit.addChildren(dragToolSGC, quadkitHolder, polarPlaneSGC);
//		for (int i = 0; i<3; ++i)	{
//			app = threeChildren[i].getAppearance();
//		  			id = images[i];//ImageData.load(Input.getInput(texture)); //"grid256rgba.png")); //weaveRGBABright.png"));
//					tex2d = (Texture2D) TextureUtility.createTexture(app, "polygonShader",id);		
//		  			thumbnailImage[i] = getScaledImage(id.getOriginalImage(), thumbnailSize, (thumbnailSize*id.getHeight())/id.getWidth());
//		    foo = new Matrix();
//		    tex2d.setTextureMatrix(foo);
//		   tex2d.setBlendColor(new Color(0f,0f,0f,.5f));
//		   textures[i] = tex2d;	
//		}
//	   
		dragCenterTool = new DragCenterTool();
		theWorld.addTool(dragCenterTool);
		translateTool = new TranslateShapeTool() {

			@Override
			public void activate(ToolContext tc) {
				super.activate(tc);
				if (theGroup.getDimension() == 3 || draggingCenter) {
					tc.reject();
				} 
				else System.err.println("Activating translate");
			}
			
		};
		theWorld.addTool(translateTool);
		
		rotateTool = new RotateShapeTool() {

			@Override
			public void activate(ToolContext tc) {
				super.activate(tc);
				if (theGroup.getDimension() == 2 || draggingCenter) {
					tc.reject(); 
				}
				else System.err.println("Activating rotate");
			}
			
		};
		theWorld.addTool(rotateTool);
		
		myroot.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
		camTimer = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (groupNeedsUpdated)	{
					updateGroupElements();
					groupNeedsUpdated = false;
				}
			}
		});
		camTimer.start();
		oneCopy = new SceneGraphComponent("onecopy");
		theWorldsChild.addChild(oneCopy);
		oneCopy.setVisible(showOneCopy);
		oneCopy.addChild(quadkit);
		MatrixBuilder.euclidean().translate(0,0,.01).scale(1.01).assignTo(oneCopy);
		thePoint = Primitives.point(nullpoint);
		thePoint.setName("point");
		dragToolSGC.setGeometry(thePoint);
		// can't do this now since the viewer is required
//		replaceGroup(currentName);
//		if (psl != null) {
//			viewer = psl.getViewer();
//			replaceGroup(currentName);
//		}
		return myroot;
	}
	
	private static String[] imageNames = {"azalea.jpg","green.jpg","lila.jpg","orange.jpg","red.jpg","threeInOne.jpg"};
	int whichOnes[] = {0,4,2};
	 private void setupImages() {
		 for (int i = 0; i<3; ++i)	{
				URL is = ResourceClass.class.getResource("resources/textures/"+imageNames[whichOnes[i]]);
			    ImageData id = null;
				try {
					id = ImageData.load(new Input(is));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				images[i] = id;
				thumbnailImage[i] = getScaledImage(id.getOriginalImage(), thumbnailSize, (thumbnailSize*id.getHeight())/id.getWidth());
		 }
	}

		private void updateTextures() {
			int width = images[0].getWidth();
			int height = images[0].getHeight();
			BufferedImage combined = new BufferedImage(3*width, height,BufferedImage.TYPE_4BYTE_ABGR);
			 for (int i = 0; i<3; ++i)	{
				 Graphics2D g2d = (Graphics2D) combined.getGraphics();
				 g2d.drawImage(images[i].getOriginalImage().getScaledInstance(
							width, height, BufferedImage.SCALE_SMOOTH), 
							width*i,0, null);	
			 }
			 images[3] = new ImageData(combined);
			Appearance app = quadkit.getAppearance();
			tex2d = (Texture2D) TextureUtility.createTexture(app, "polygonShader",images[3]);		
			tex2d.setBlendColor(new Color(0f,0f,0f,.5f));
		}
		
	private double[] blendedcolor = new double[3],
		origColor = TriangleGroup.faceColors.clone()[0];
	private double[] white = {1,1,1};
	void updateFaceColors()		{
		if (quads == null || separateQuads) return;
		Rn.linearCombination(blendedcolor, 1-blendFactor, origColor, blendFactor, white);
//		System.err.println("Setting face colors to "+Rn.toString(blendedcolor));
		Color c = new Color((float)blendedcolor[0],(float)blendedcolor[1],(float)blendedcolor[2]);
		quadkit.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, c);
	}

	private double[][] fundamentalTriangleVerts;

	void updateFundamentalRegion()	{
		updateTriangleScaling(scale);
		Scene.executeWriter(quadkit, new Runnable() {


			public void run() {
				updatePoint(theGroup.getCenterPoint());
				quads = (IndexedFaceSet) TriangleGroup.getSplitFundamentalRegion(theGroup, quads);
				quads.setFaceAttributes(Attribute.COLORS, null);
				updateFaceColors();
//				System.err.println("Updating quadkit"+quads.getNumFaces());
				double[][] verts = quads.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
				fundamentalTriangleVerts = new double[][] {verts[1], verts[2], verts[3]};
				if (!separateQuads && quadkitHolder.getGeometry() != quads) {
//					updateFaceColors();
					quads.setName("quads");
//					quads.setGeometryAttributes("textureUnits", textures);
//					int[] tt = {0,1,2};
//					quads.setFaceAttributes(Attribute.attributeForName("textureUnits"), StorageModel.INT_ARRAY.createReadOnly(tt));
					quadkitHolder.setGeometry(quads);
				}	
				if (separateQuads) {
					for (int i = 0; i<3; ++i)	{
						face[i] = IndexedFaceSetUtility.extractFace(quads, i);
						threeChildren[i].setGeometry(face[i]);
					}			
				}
				if (projectOntoSphere) {
					IndexedFaceSet ifs = IndexedFaceSetUtility.constructPolygon(fundamentalTriangleVerts);
					IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(ifs);
					if (theGroup.getDimension() == 3) {
						for (int i = 0; i<4; ++i)
							ifs = IndexedFaceSetUtility.binaryRefine(ifs);
						verts = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
						int vlength = GeometryUtility.getVectorLength(ifs);
						Pn.setToLength(verts, verts, 1.0, Pn.EUCLIDEAN);
						double[][] nv = verts;
						if (verts[0].length == 4) {
							nv = new double[verts.length][3];
							Pn.dehomogenize(nv, verts);
						}
						ifs.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(vlength).createReadOnly(verts));
						IndexedFaceSetUtility.calculateAndSetNormals(ifs);
				}
				quadkit.setGeometry(ifs);
				}
			}
		});
		
	}

    protected void updateTriangleScaling(double scale) {
    		if (fundamentalTriangleVerts == null) return;
    		double[] center = Rn.average(null,fundamentalTriangleVerts );
    		int metric = theGroup.getMetric();
    		double[] m = Rn.conjugateByMatrix(null, P3.makeScaleMatrix(null, scale), 
    						P3.makeTranslationMatrix(null, center, metric));
    		new Matrix(m).assignTo(quadkit);
    }


	public void replaceGroup(String name)	{
//		viewer.render();
		currentName = name;
		if (DGRepn != null && theWorld.isDirectAncestor(DGRepn)) {
			DGRepn.setVisible(false);
			theWorld.removeChild(DGRepn);
		}
		if (name.startsWith("22"))	{
			convertToProj = false;
			spherecb.setSelected(true);
		}
		MatrixBuilder.euclidean().assignTo(theWorld);
		theGroup = TriangleGroup.instanceOfGroup(name);
		if (convertToProj) theGroup = theGroup.convertToProjective();
		sky.setShowSky(theGroup.getDimension() == 3);
		// set up the center point and the bulls eye
		double scaleFactor = 1.0;
		if (theGroup.getMetric() == Pn.HYPERBOLIC) scaleFactor = .5;
		else if (theGroup.getDimension() == 3) scaleFactor = .7;
		// reconfigure the GUI for the relevant metric
		int metric = theGroup.getMetric();
		spherecb.setVisible(metric == Pn.ELLIPTIC || (!theGroup.getName().startsWith("22") && (metric == Pn.EUCLIDEAN && theGroup.getDimension() == 3)));
		poincarecb.setVisible(metric == Pn.HYPERBOLIC);
		flattencb.setVisible(metric == Pn.HYPERBOLIC);
		if (metric != Pn.HYPERBOLIC) {
			flattenDGSGR = false;
			if (sgr != null) sgr.setFlatten(flattenDGSGR);
		}
		double xy = scaleFactor * invisiblePointRadius;
		bullsEye = Primitives.texturedQuadrilateral(new double[]{-xy,-xy,0, xy,-xy,0, xy,xy,0, -xy,xy,0});
		bullsEye.setName("theDisk");
		bullsEyeSGC.setGeometry(bullsEye);
		// initialize the matrices
		MatrixBuilder.euclidean().assignTo(bullsEyeSGC);
		MatrixBuilder.euclidean().assignTo(diskHolder2);
		updatePoint(getWeightedCenterPoint());

		// initialize the geometry of the fundamental regious
		quads = null;
		if (separateQuads)  {
			for (int i = 0; i<3; ++i)	{
				quadkitHolder.addChild(threeChildren[i]);	
//				extractFace(i);
//				threeChildren[i].setGeometry(face[i]);
			}			
		}
		updateFundamentalRegion();

		// reset the tool
		dragCenterTool.reset();
		DefaultMatrixSupport.getSharedInstance().storeAsDefault(CameraUtility.getCameraNode(viewer).getTransformation());
//		DefaultMatrixSupport.getSharedInstance().restoreDefaultMatrices(viewer.getSceneRoot(), false);
		SceneGraphUtility.setMetric(myroot, theGroup.getMetric());
		MatrixBuilder.euclidean().assignTo(myroot);
		// get scene graph repn of this group
		sgr = new DiscreteGroupSceneGraphRepresentation(theGroup, copycat);
		sgr.getDropBox().setComponentDisplayLists(componentDisplayLists);
		sgr.setWorldNode(quadkit);
		sgr.setFlatten(flattenDGSGR);
		
		// set up a constraint to determine how many elements are created
		if (!(theGroup.getMetric() == Pn.EUCLIDEAN && theGroup.getDimension()==2))
			theGroup.setConstraint(new DiscreteGroupSimpleConstraint(numCopies));
		else {
			graphicsContext = new Graphics3D(viewer.getCameraPath(), 
					new SceneGraphPath(viewer.getSceneRoot(), theWorld), 
					1.0);		
			dgvc = new DiscreteGroupViewportConstraint(1,1,-1,-1,graphicsContext);
			dgvc.setFudge(1.25);
			dgvc.setMaxNumberElements(2*numCopies);
			theGroup.setConstraint(dgvc);
		}
		updateGroupElements();

		DGRepn =  sgr.getRepresentationRoot();
		theWorld.addChild(DGRepn);
		updateDGAppearance();
		
		if (theGroup.getDimension()  == 3)	{
			CameraUtility.getCamera(viewer).setPerspective( true); 
			myroot.getAppearance().setAttribute(LIGHTING_ENABLED, true);
			CameraUtility.encompass(viewer);
			charlesgunn.jreality.tools.ToolManager.toolManagerForViewer(viewer).activateTool(charlesgunn.jreality.tools.ToolManager.ROTATION_TOOL);
		} else {
			CameraUtility.getCamera(viewer).setPerspective( false); 
			myroot.getAppearance().setAttribute(LIGHTING_ENABLED, false);
			charlesgunn.jreality.tools.ToolManager.toolManagerForViewer(viewer).activateTool(charlesgunn.jreality.tools.ToolManager.TRANSLATION_TOOL);
			if (theGroup.getMetric() == Pn.HYPERBOLIC) 	{
				CameraUtility.getCamera(viewer).setFocus(2.0); 
			} else {
				MatrixBuilder.euclidean().translate(0,0,1).assignTo(CameraUtility.getCameraNode(viewer));
				CameraUtility.getCamera(viewer).setFocus(2.5); 				
			}
		}
		hyperbolicBoundary.setVisible(theGroup.getMetric() == Pn.HYPERBOLIC);
		
		viewer.renderAsync();
	}
	private void updateDGAppearance() {
		Appearance ap = DGRepn.getAppearance();
		ap.setAttribute("singlePeer", singlePeer);
		boolean value = theGroup.getMetric() == Pn.HYPERBOLIC && showPoincare;
		ap.setAttribute(NoneuclideanGLSLShader.POINCARE_MODEL, value);
//		ap.setAttribute("stereographicProjection", value);
		ap.setAttribute("useGLSL", theGroup.getMetric() != Pn.EUCLIDEAN);
		ap.setAttribute("oneGLSL", theGroup.getMetric() != Pn.EUCLIDEAN);
//		ap.setAttribute(CommonAttributes.DEPTH_FUDGE_FACTOR, .995);
		ap.setAttribute(CommonAttributes.ANY_DISPLAY_LISTS, anyDisplayLists);
		List l = SceneGraphUtility.getPathsBetween(viewer.getSceneRoot(), myroot);
		if (l.size() == 0) return;
		SceneGraphPath root2dg = (SceneGraphPath) l.get(0);
		ap.setAttribute(NoneuclideanGLSLShader.POINCARE_PATH, root2dg);
//		System.err.println("setting poincare path");
	}
	
	double[] getWeightedCenterPoint() {
		double[][] vertices = theGroup.getTriangle();
		double[] center = new double[4];
		Rn.barycentricTriangleInterp(center, vertices, new double[]{.33333,.33333,.33333});
		Pn.dehomogenize(center, center);
		return center;
	}

	double[] nullpoint = {0,0,0,1};
	void updatePoint(double[] p)	{
		thePoint.setVertexAttributes(Attribute.COORDINATES, 
					StorageModel.DOUBLE_ARRAY.array().createReadOnly(new double[][]{p}));
		if (theGroup.getMetric() == Pn.EUCLIDEAN && theGroup.getDimension() == 3)	{
			p = Pn.setToLength(null, p, 1.0, Pn.EUCLIDEAN);
		}
		theGroup.setCenterPoint(p);
		if (theGroup.getDimension() == 3)	{
			MatrixBuilder.euclidean().rotateFromTo( new double[]{0,0,1,1}, p).translate(0,0,1).assignTo(bullsEyeSGC);
//			if (showPolarPlane)
//				SphericalTriangleFactory.dualPlane(polarPlaneSGC, p, 12);
		} else {
			MatrixBuilder.init(null, theGroup.getMetric()).translate(p).translate(0,0,.02).assignTo(bullsEyeSGC);	
		}
		DefaultMatrixSupport.getSharedInstance().storeAsDefault(bullsEyeSGC.getTransformation());
//		System.err.println("Setting disk holder 1 to "+Rn.matrixToString(diskHolder1.getTransformation().getMatrix()));
	}

	void updateGroupElements() {
		theGroup.getConstraint().setMaxNumberElements(numCopies);
		if (theGroup.getMetric() == Pn.EUCLIDEAN && theGroup.getDimension()==2) {
			double aspectRatio = CameraUtility.getAspectRatio(viewer);
			graphicsContext.setAspectRatio(aspectRatio);
			dgvc.setCenterPoint(theGroup.getCenterPoint());
//			dgvc.setMaxNumberElements(2*numCopies);
			dgvc.update();
		}
		theGroup.update();
		sgr.setElementList(theGroup.getElementList());
		sgr.update();
		System.err.println("Setting dg el list");
	}

	final transient InputSlot timerSlot = InputSlot.SYSTEM_TIME;
	 final transient InputSlot pointerSlot = InputSlot.POINTER_TRANSFORMATION;
	 final transient InputSlot leftButton = InputSlot.LEFT_BUTTON;
	class DragCenterTool extends AbstractTool {
		SceneGraphPath rootToPick;
		double[] pick2world = new double[16],
			pick2root = new double[16],
			root2world = new double[16];
		{
	   		addCurrentSlot(pointerSlot);
	    }
		@Override
		public void perform(ToolContext tc) {
			PickResult pick = tc.getCurrentPick();
			if (pick == null) return;
			boolean pointPicked = pick.getPickPath().contains(thePoint),
				diskPicked = pick.getPickPath().contains(bullsEyeSGC),
				dragging = tc.getAxisState(leftButton).isPressed();
			rootToPick = pick.getPickPath();
			viewer.renderAsync();
			if (pointPicked) {
				// near a center point; move the "bullseye" to this copy of the group
				// don't move these lines of code; important to change visibility first
				dragToolSGC.setVisible(false);
				diskHolder2.setVisible(true);
				rootToPick.getMatrix(pick2root);
				root2worldPath.getInverseMatrix(root2world);
				Rn.times(pick2world, root2world, pick2root);
				diskHolder2.getTransformation().setMatrix(pick2world);
				DefaultMatrixSupport.getSharedInstance().storeAsDefault(diskHolder2.getTransformation());
				viewer.renderAsync();
				draggingCenter = true;
				return;
			} else if (!diskPicked)	{
				draggingCenter = false;
				dragToolSGC.setVisible(true);
				diskHolder2.setVisible(false);
				viewer.renderAsync();
				return;					
			} else if (dragging)	{
				draggingCenter = true;
				double[] position = Rn.copy(null, pick.getObjectCoordinates());
				if (position.length == 4) Pn.dehomogenize(position, position);
				position[2] = position[3] = 0.0;
				Rn.add(position, position, theGroup.getCenterPoint());
				Pn.dehomogenize(position, position);
				updatePoint(position);
				updateFundamentalRegion();
			}
		}
		void reset() {
			draggingCenter = false;
			dragToolSGC.setVisible(true);
			diskHolder2.setVisible(false);				
		}
		
	}
	@Override
	public List<Plugin> getPluginsToRegister() {
		// TODO Auto-generated method stub
		 super.getPluginsToRegister();
			sky = new Sky();
			pluginsToLoad.add(sky);
//		sky.setEnvironment("Grace Cross");
		sky.setShowSky(true);
		return pluginsToLoad;
//		psl.getVRPanel().setShowPanel(true);
//		psl.getJRViewer().registerPlugin(sky);
////		sky.setShowSky(false);
//		try {
//			sky.install(psl.getController());
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

	}

	@Override
	public void display(){
		setupImages();
		super.display();
		updateTextures();

		jrviewer.setPropertiesResource(this.getClass(), "discreteGroupCGG/src/discreteGroup/demo/triangleGroup.xml");
		viewer = jrviewer.getViewer();
		root2worldPath = SceneGraphUtility.getPathsBetween(viewer.getSceneRoot(), myroot).get(0);
		final Color URBackground = new Color(.8f, .85f, .68f);
		final Color ULBackground  = new Color(1f, .98f, .8f); 
		final Color LLBackground  = new Color(.1f, .1f, .25f);
		final Color LRBackground  = new Color(0.05f, .15f, .35f);
		Color[] backgroundArray = new Color[4];
		backgroundArray[0] = URBackground;
		backgroundArray[1] = ULBackground;// bg[1];
		backgroundArray[2] = LLBackground;
		backgroundArray[3] = LRBackground;  //bg[2];
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColors", backgroundArray);
		viewer.getSceneRoot().getAppearance().setAttribute("polygonShader.reflectionMap:blendColor",
				new Color(1f, 1f, 1f, (float) .3));
		CameraUtility.getCamera(viewer).setFar(50.0);
		CameraUtility.getCamera(viewer).setFieldOfView(80.0);
		double distanceToScreen = 5.0;		// distance to screen (scales tessellation)
		MatrixBuilder.euclidean().translate(0,0,distanceToScreen).assignTo(CameraUtility.getCameraNode(viewer));
		CameraUtility.getCamera(viewer).setFocus(distanceToScreen);
		CameraUtility.getCamera(viewer).addCameraListener(new CameraListener() {
			public void cameraChanged(CameraEvent ev) {
				if (theGroup.getDimension() == 2 && theGroup.getMetric() == Pn.EUCLIDEAN)
					groupNeedsUpdated = true;
			}
			
		});
		((Component) viewer.getViewingComponent()).addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				if (theGroup.getDimension() == 2 && theGroup.getMetric() == Pn.EUCLIDEAN)
					groupNeedsUpdated = true;
			}
			
		});
		((Component) viewer.getViewingComponent()).addKeyListener( new KeyAdapter()	{
			int which = 0;
			boolean drag = false;
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode())	{
				
				case KeyEvent.VK_H:
					System.err.println(" 0: cycle groups forward");
					System.err.println(" 1: cycle groups backward");
//					System.err.println(" 2: toggle elliptic repregetIndexedsentation");
					break;
				case KeyEvent.VK_1:
					which++;
					activate(which);
					break;

//				case KeyEvent.VK_2:
//					convertToProj = !convertToProj;
//					replaceGroup(currentName);
//					break;
										
				}
			}	
			
		});

		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(10,10,200,0));
		charlesgunn.jreality.tools.ToolManager.toolManagerForViewer(viewer).setActive(false); //activateTool(charlesgunn.jreality.tools.ToolManager.TRANSLATION_TOOL);//
		charlesgunn.jreality.tools.ToolManager.toolManagerForViewer(viewer).getToolbar().setVisible(false);
//		if (psl == null) 
			replaceGroup(currentName);
		
		
	}

	static String[][] names = {
		{"233","*233","234","*234"},
		{"3*2","235","*235"},
		{"236","*236","244","*244"},
		{"237", "*237","245","*245"},
		{"224","*224","*225","225"},
		{"*226","226","*228","228"}};
	@Override
	public Component getInspector() {
		JPanel panel = new JPanel();
		panel.setName("Triangle group");
		ButtonGroup bg = new ButtonGroup();
		Box container = Box.createVerticalBox();
		panel.add(container);
		Box vbox = Box.createVerticalBox();
		vbox.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				BorderFactory.createTitledBorder(BorderFactory
						.createEtchedBorder(), "Groups")));
		for (int j = 0; j<names.length; ++j)	{
			final int jj = j;
			Box hbox = Box.createHorizontalBox();
			for (int i = 0; i<names[j].length; ++i)	{
				final int k = i;
				JRadioButton jm = new JRadioButton(names[jj][i]);
				hbox.add(jm);
				jm.addActionListener( new ActionListener() {
					public void actionPerformed(ActionEvent e)	{
						replaceGroup(names[jj][k]);
					}
				});
				bg.add(jm);
			}	
			vbox.add(hbox);
		}
		JButton jm = new JButton("Other...");
		vbox.add(jm);
		jm.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				getInput();
			}
		});
		
		container.add(vbox);
		vbox = Box.createVerticalBox();
		container.add(vbox);
		vbox.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				BorderFactory.createTitledBorder(BorderFactory
						.createEtchedBorder(), "parameters")));
		
		final TextSlider rSlider = new TextSlider.IntegerLog("max copies",  SwingConstants.HORIZONTAL,1, 1000, numCopies);
		rSlider.addActionListener(new ActionListener()  {
		  public void actionPerformed(ActionEvent e)  {
		    numCopies = rSlider.getValue().intValue();
		    updateGroupElements();
		    viewer.renderAsync();
		  }
		});
		vbox.add(rSlider);
		final TextSlider sSlider = new TextSlider.Double("scale",  SwingConstants.HORIZONTAL,0, 1, 1);
		sSlider.addActionListener(new ActionListener()  {
		  public void actionPerformed(ActionEvent e)  {
		    scale = sSlider.getValue().doubleValue();
		    updateTriangleScaling(scale);
		    viewer.renderAsync();
		  }
		});
		vbox.add(sSlider);
		
		Box hbox = Box.createHorizontalBox();
		vbox.add(hbox);
		
		poincarecb = new JCheckBox("conformal model");
		poincarecb.setSelected(showPoincare);
		poincarecb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				showPoincare = ((JCheckBox)e.getSource()).isSelected();
				updateDGAppearance();
			}
			
		});
		hbox.add(Box.createHorizontalGlue());
		hbox.add(poincarecb);
		hbox.add(Box.createHorizontalGlue());
		flattencb = new JCheckBox("flatten");
		flattencb.setSelected(flattenDGSGR);
		flattencb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				flattenDGSGR = ((JCheckBox)e.getSource()).isSelected();
				sgr.setFlatten(flattenDGSGR);
//				sgr.update();
			}
			
		});
		hbox.add(flattencb);
		hbox.add(Box.createHorizontalGlue());
		
		spherecb = new JCheckBox("sphere view");
		spherecb.setSelected(!convertToProj);
		spherecb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				convertToProj = !((JCheckBox)e.getSource()).isSelected();
				replaceGroup(currentName);
			}
			
		});
		hbox.add(spherecb);
		hbox.add(Box.createHorizontalGlue());
		vbox = Box.createVerticalBox();
		hbox = Box.createHorizontalBox();
		container.add(vbox);
		vbox.add(hbox);
		vbox.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				BorderFactory.createTitledBorder(BorderFactory
						.createEtchedBorder(), "textures")));
//		final JButton colorsb[] = new JButton[3];
		for (int i = 0; i<3; ++i)	{
			final int j = i;
//			hbox = Box.createHorizontalBox();
//			vbox.add(hbox);
//			colorsb[i] = new JButton("color "+i);
//			Color tmp = new Color((float)origColors[i][0], (float)origColors[i][1],(float)origColors[i][2]);
//			colorsb[i].setBackground(tmp);
//			colorsb[i].addActionListener(new ActionListener()	{
//				public void actionPerformed(ActionEvent e)	{
//					Color color = JColorChooser.showDialog((Component) viewer.getViewingComponent(), "Select color ",  null);
//					if (color != null) {
//						origColors[j] = new double[]{color.getRed()/255.0,color.getGreen()/255.0, color.getBlue()/255.0};
//						updateFaceColors();
//						colorsb[j].setBackground(color);
//					}
//				}
//			});
//			hbox.add(colorsb[i]);
			hbox.add(Box.createHorizontalStrut(20));
			ImageIcon icon = new ImageIcon(thumbnailImage[i]);
			final JButton filedropButton = new JButton(icon);
			hbox.add(filedropButton);
			new FileDrop(filedropButton, new FileDrop.Listener() {
				public void filesDropped(File[] arg0) {
					if (arg0.length == 0) return;
		 			try {
						ImageData id2 = ImageData.load(Input.getInput(arg0[0]));
						//textures[j].setImage(id2);
						images[j] = id2;
						updateTextures();
						thumbnailImage[j] = getScaledImage(id2.getOriginalImage(), thumbnailSize, (thumbnailSize*id2.getHeight())/id2.getWidth());
						filedropButton.setIcon(new ImageIcon(thumbnailImage[j]));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.err.println("File dropped"+arg0[0].getName());
				}
						
			});		
		}
		hbox = Box.createHorizontalBox();
		final JButton colorsb = new JButton("blend color");
		Color tmp = new Color((float)origColor[0], (float)origColor[1],(float)origColor[2]);
		colorsb.setBackground(tmp);
		colorsb.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				Color color = JColorChooser.showDialog((Component) viewer.getViewingComponent(), "Select color ",  null);
				if (color != null) {
					origColor = new double[]{color.getRed()/255.0,color.getGreen()/255.0, color.getBlue()/255.0};
					updateFaceColors();
					colorsb.setBackground(color);
				}
			}
		});
//		hbox.add(Box.createHorizontalGlue());
//		hbox.add(colorsb);
//		hbox.add(Box.createHorizontalGlue());
		vbox.add(Box.createVerticalStrut(5));
		vbox.add(hbox);
		vbox.add(Box.createVerticalStrut(5));
		hbox = Box.createHorizontalBox();
		final TextSlider bSlider = new TextSlider.Double("texture blend",  SwingConstants.HORIZONTAL,0,1, blendFactor);
		bSlider.addActionListener(new ActionListener()  {
		  public void actionPerformed(ActionEvent e)  {
		    blendFactor = bSlider.getValue().doubleValue();
		    updateFaceColors();
		    // just to get the display lists to be dirty!
//		    quads.setGeometryAttributes("foo", 1.0);
		    viewer.renderAsync();
		  }
		});
//		hbox.add(bSlider);
		vbox.add(hbox);
		inspector.add(panel);
		return inspector;
	}
		protected Image getScaledImage(Image srcImg, int w, int h){
	        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	        Graphics2D g2 = resizedImg.createGraphics();
	        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	        g2.drawImage(srcImg, 0, 0, w, h, null);
	        g2.dispose();
	        return resizedImg;
	    }

		private Image[] thumbnailImage = new Image[3];
		private int thumbnailSize = 50;


	String[] builtins = {"*236","*235", "*237",  "*244","*234","*245"};
	private Texture2D tex2d;
	private static JOGLFBOViewer fboViewer;
	private static TriangleGroupDemo tgd;
	private Sky sky;
	private JCheckBox poincarecb;
	private JCheckBox flattencb;
	private JCheckBox spherecb;
	void activate(int which)	{
		if (which < 0) which += builtins.length;
		which = which % builtins.length;
		replaceGroup(builtins[which]);
	}
	
	void loadFile() {
//		showDefaultDomain = showDirichletDomain = false;
		JFileChooser fc = new JFileChooser(DiscreteGroupUtility.triangleGroupFiles);
		int result = fc.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION)	{
			try {
				readSGC = Readers.read(fc.getSelectedFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
			IndexedFaceSetUtility.calculateFaceNormals(readSGC);
			IndexedFaceSetUtility.calculateVertexNormals(readSGC);
			updateFundamentalRegion();
		} else {
			System.out.println("Unable to open file");
			return;
		}
		viewer.render();
	}
	public void getInput()	{
		String name = JOptionPane.showInputDialog("Enter a name for a triangle group:");		
		if (name != null && name.length() > 0) replaceGroup(name);
	}
	
	public boolean addBackPlane() {return true; }
	public boolean isEncompass() {return false; }
	

	public static Component getReadMePanel()	{
		JPanel mypanel = new JPanel();
		mypanel.setName("ReadMe");
		JTextArea textarea = new JTextArea(10,20);
		textarea.setEditable(false);
		textarea.append("Triangle groups are kaleidoscopes\n"+
				"generated in reflections in the sides of \n"+
				"a triangle.\n"+
				"Such groups exist in all 3 classic geometries\n"+
				"\nFor example, triangle group 23n is\n " +
				"a triangle with angles Pi/2, Pi/3, Pi/n.\n" +
				"    Geometry    n\n" +
				"    --------    -\n"+
				"    euclidean   6\n" +
				"    elliptic    5\n" +
				"    hyperbolic  7\n" +
				"The program begins with the 236 group\n" +
				"and a tessellation based on\n" +
				"the position of a movable point\n"+
				"\nIf you move the curson near the meeting of 3 colors\n" +
				"a bulls-eye appears; you can drag this around.\n" +
				"Otherwise dragging translates (or rotates) the pattern.\n"+
				"\nKeystroke commands:\n"+
				"    '1':    advance to next example.\n"+
				"    '2':    toggle flat elliptic/3D-spherical.\n"+
				"Shift-cntl-f:  toggles fullscreen mode.\n"+
				"Scroll wheel zooms in and out\n"+
				"\nFor more groups, click on the 'Groups' tab\n"+
				"\nClick on 'Scene Graph' tab to explore structure\n"+
				"\nAuthor: Charles Gunn\n"+
				"    gunn at math.tu-berlin.de\n");
		mypanel.add(textarea);
		return mypanel;
	}
    public static void main(String[] args) {
		new TriangleGroupDemo().display();
	}
}
//void updateGeometry() {
//if (DGRepn == null || theWorldsChild == null) return;
//if (loadedGeometry != null && quadkit.isDirectAncestor(loadedGeometry)) quadkit.removeChild(loadedGeometry);
//		quads = null;
//updateFundamentalRegion();
//updatePoint(tg.getCenterPoint());
//SceneGraphPath sel = new SceneGraphPath(viewer.getSceneRoot(), myroot, theWorld);
//if (DGRepn.isVisible()) sel.push(DGRepn);
//SelectionManager.selectionManagerForViewer(viewer).setSelectionPath(sel);
//viewer.render();
//}

//public  Component getInspector() {
//Box container = Box.createVerticalBox();
//final TextSlider RSlider = new TextSlider.Double("reg",  SwingConstants.HORIZONTAL, 0.0, 1, regular);
//RSlider.addActionListener(new ActionListener()  {
//  public void actionPerformed(ActionEvent e)  {
//   regular = RSlider.getValue().doubleValue();
//   updateFundamentalRegion();
//  }
//});
//container.add(RSlider);
//final TextSlider rSlider = new TextSlider.Double("dual",  SwingConstants.HORIZONTAL, 0.0, 1, dual);
//rSlider.addActionListener(new ActionListener()  {
//  public void actionPerformed(ActionEvent e)  {
//    dual = rSlider.getValue().doubleValue();
//    updateFundamentalRegion();
//  }
//});
//container.add(rSlider);
//final TextSlider rtSlider = new TextSlider.Double("truncate",  SwingConstants.HORIZONTAL, 0.0, 1, truncate);
//rtSlider.addActionListener(new ActionListener()  {
//  public void actionPerformed(ActionEvent e)  {
//    truncate = rtSlider.getValue().doubleValue();
//    updateFundamentalRegion();
//  }
//});
//final TextSlider stSlider = new TextSlider.Double("stellate",  SwingConstants.HORIZONTAL, 0.0, 1, stellate);
//stSlider.addActionListener(new ActionListener()  {
//  public void actionPerformed(ActionEvent e)  {
//    stellate = stSlider.getValue().doubleValue();
//    updateFundamentalRegion();
//  }
//});
//container.add(stSlider);
//container.setName("Parameters");
//return container;
//}
//double regular = 1, truncate = 0, dual = 0, stellate = .98;
//WingedEdge wingedEdge;
//double[] parameters = {1,0,0,0};
//void updateFundamentalRegion()	{
//	if (tg.getDimension() == 3) {
//		double[] parms = new double[7];
//		parms[0] = regular;
//		parms[1] = dual;
//		parms[2] = truncate;
//		parms[3] = stellate;
//		wingedEdge = TriangleGroup.getTenPlaneRegion(tg, parms);
////		SceneGraphComponent qk2 = new SceneGraphComponent("quadkit");
////		int n = quadkit.getChildComponentCount();
////		for (int i= 0; i<n; ++i)	{
////			qk2.addChild(quadkit.getChildComponent(i));
////		}
////		quadkit = qk2;
//		quadkit.setGeometry(wingedEdge);
////		sgr.setWorldNode(quadkit);
//	}
//	else {
//	Scene.executeWriter(quadkit, new Runnable() {
//
//		public void run() {
//			System.err.println("Updating quadkit");
//			updatePoint(tg.getCenterPoint());
//			quads = (IndexedFaceSet) TriangleGroup.getSplitFundamentalRegion(tg, quads);
//			if (quadkitHolder.getGeometry() != quads) {
//				quadkitHolder.setGeometry(quads);
//				quads.setName("quads");
//			}			
//		}
//	});
//	}
//	if (!separateQuads) {
//		quadkit.setGeometry(quads);
//			double[][] verts = quads.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
//			double[][] triangle = {verts[1], verts[2], verts[3]};
//			IndexedFaceSet ifs = IndexedFaceSetUtility.constructPolygon(triangle);
//			IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(ifs);
//			if (tg.getDimension() == 3) {
//				for (int i = 0; i<4; ++i)
//					ifs = IndexedFaceSetUtility.binaryRefine(ifs);
//				verts = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
//				int vlength = GeometryUtility.getVectorLength(ifs);
//				Pn.setToLength(verts, verts, 1.0, Pn.EUCLIDEAN);
//				double[][] nv = verts;
//				if (verts[0].length == 4) {
//					nv = new double[verts.length][3];
//					Pn.dehomogenize(nv, verts);
//				}
//				ifs.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(vlength).createReadOnly(verts));
//				IndexedFaceSetUtility.calculateAndSetNormals(ifs);
//			}
//		quadkit.setGeometry(ifs);
//		return;
//	}
//	for (int i = 0; i<3; ++i)	{
//		IndexedFaceSet bar = IndexedFaceSetUtility.extractFace(quads, i);
//		if (tg.getDimension() == 3 && tg.isMirrorGroup()) {
//			int[] ind = bar.getFaceAttributes(Attribute.INDICES).item(0).toIntArray(null);
//			double[][] verts = bar.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
//			if (i == 0) System.err.println("Verts[0] = "+Rn.toString(verts[ind[0]]));
//			double[][][] reordered = {{verts[ind[0]],verts[ind[1]]},{verts[ind[3]],verts[ind[2]]}};
//			BezierPatchMesh thing = new BezierPatchMesh(1, 1, reordered);
////			thing.refine();
////			thing.refine();
////			thing.refine();
//			IndexedFaceSet ifs = BezierPatchMesh.representBezierPatchMeshAsQuadMesh(thing);
//			threeChildren[i].setGeometry(ifs);	
//		} else {
//			threeChildren[i].setGeometry(bar);
//			System.err.println("Setting bar "+i);
//		}
//	}
//}
//double dt = Math.PI/180.0;
//double angle = 0.0;
//JCheckBoxMenuItem showHBndy, showDirDom;
//if (showDirDom != null) 
//	showDirDom.setEnabled(tg.getDimension() == 2 && name.charAt(0) != '*');
//
//showSphere = true,
//showDefaultDomain = true,
//showDirichletDomain = false,
//showEl = true,
//automate = false,
//globbed = false,
//showCenterPoint = false,
//timerRunning = false,
//timer = new Timer(20, new ActionListener() {
//
//public void actionPerformed(ActionEvent e) {
//	angle += dt;
//	if (angle > Math.PI/2) angle -= Math.PI/2;
//	double c = Math.cos(angle);
//	double s = Math.sin(angle);
//	double[] cp = {s,c,0,1};
//	tg.setCenterPoint(cp);
//	getSplitFundamentalRegion();
//	viewer.render();
//}
//
//});
//
//JMenu testM = new JMenu("Geometry");
//ButtonGroup bg = new ButtonGroup();
//final JCheckBoxMenuItem jcc = new JCheckBoxMenuItem("Show Default Domain");
//jcc.setSelected(showDirichletDomain);
//testM.add(jcc);
//jcc.addActionListener( new ActionListener() {
//public void actionPerformed(ActionEvent e)	{
//	showDefaultDomain = jcc.isSelected();
//	showDirichletDomain = !showDefaultDomain;
//	updateGeometry();
//}
//});
//bg.add(jcc);
//showDirDom = new JCheckBoxMenuItem("Show Dirichlet Domain");
//showDirDom.setSelected(showDirichletDomain);
//showDirDom.setEnabled(false);
//testM.add(showDirDom);
//showDirDom.addActionListener( new ActionListener() {
//public void actionPerformed(ActionEvent e)	{
//	showDirichletDomain = showDirDom.isSelected();
//	showDefaultDomain = !showDirichletDomain;
//	updateGeometry();
//}
//});
//bg.add(showDirDom);
//final JCheckBoxMenuItem jca = new JCheckBoxMenuItem("Automate");
//jca.setSelected(automate);
//testM.add(jca);
//jca.addActionListener( new ActionListener() {
//public void actionPerformed(ActionEvent e)	{
//	automate = jca.isSelected();
////		if (tg.getName() != "3*2") return;
//	tg.setConstrained(automate);
//	quads = null;
//	if (automate) timer.start();
//	else timer.stop();
//	if (!automate) {
//		getSplitFundamentalRegion();
//	}
//}
//});
//final JCheckBoxMenuItem jcg = new JCheckBoxMenuItem("Merge planar polygons");
//jcg.setSelected(globbed);
//testM.add(jcg);
//jcg.addActionListener( new ActionListener() {
//public void actionPerformed(ActionEvent e)	{
//	globbed = jcg.isSelected();
//	//System.err.println("Flipping globbed: "+globbed);
//	updateGeometry();
//}
//});
//showHBndy = new JCheckBoxMenuItem("Show Sphere");
//showHBndy.setSelected(showSphere);
//showHBndy.setEnabled(true);
//testM.add(showHBndy);
//showHBndy.addActionListener( new ActionListener() {
//public void actionPerformed(ActionEvent e)	{
//	showSphere = showHBndy.isSelected();
//	hyperbolicBoundary.setVisible(showSphere);
//	viewer.render();
//}
//});
//theMenuBar.add(testM);
//void updateGeometry() {
//	if (DGRepn == null || theWorldsChild == null || quadkit == null) return;
//if (!showDirichletDomain && !showDefaultDomain ) {
//		DGRepn.setVisible(true);
//		quadkit.setGeometry(null);
//		theWorldsChild.setGeometry(null);
//		if (readSGC != null) {
//			if (loadedGeometry != null && quadkit.isDirectAncestor(loadedGeometry)) quadkit.removeChild(loadedGeometry);
//			loadedGeometry = readSGC;
//			quadkit.addChild(loadedGeometry);
//			loadedGeometry.setVisible(true);
//			readSGC = null;
//		}
//	} else {
//		if (loadedGeometry != null && quadkit.isDirectAncestor(loadedGeometry)) quadkit.removeChild(loadedGeometry);
//		if (showDefaultDomain) {
//			quads = null;
//			updateFundamentalRegion();
//			updatePoint(tg.getCenterPoint());
//		}
//		else {
//			quads = (IndexedFaceSet) DiscreteGroupUtility.calculateDirichletDomain(tg);
//			quads.setName("Quads");
//		}
//		if (tg.getDimension() == 3 && tg.getMetric()==Pn.EUCLIDEAN && globbed)	{
//			wingedEdge = WingedEdge.convertConvexPolyhedronToWingedEdge(DiscreteGroupUtility.actOnIndexedFaceSet(tg, quads));
//			theWorldsChild.setGeometry(wingedEdge);	
//			DGRepn.setVisible(false);
//			quadkit.setGeometry(null);
//		} else {
			//quadkit.setGeometry(quads);
//			theWorldsChild.setGeometry(null);
//			DGRepn.setVisible(true);
//		}
//	}
