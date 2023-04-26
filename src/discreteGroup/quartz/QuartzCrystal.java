/*
 * Created on 10 Apr 2023
TODO:
    subclass QuartzCrystal to make different applications
    add buttons to display only the different spiral curves
    add an animated point/sphere that moves along the spiral curves
        at unit speed -- this shows that the two 3-fold channels are 
        polar to each other
    animate the 3-fold generators
    add the sodium chloride crystal as an example of a mirror group
        and contrast to quartz
    use clipping planes to slice through the structure
    saturate the axis colors to make them brighter
    control all the tetra edges and colors separately in GUI
    Refine the center camera command to be more surgical
    
 DONE
 26.04.23
    created SimpleDGSGR and AbstractDGSGR classes to provide light-weight scene graphs
    added a constraint to the DGSGR that allows SGC's to be selectively turned on and off
    	rather than gnerating new scene graphs. First generate a huge scene graph and then
    	use the DGSGR constraint to only show the parts you're interested in.
    cleaned up QuartzGroup to have 4 levels and simplified naming, so that QuartzCrystal
     	can wire the levels together as needed
    fixed problems with TermesSphere due to drawFaces=true
    got a short animation fly-through rendered, loaded into Final Cut Pro (new!), and recorded.
    straightened out the face and edge colors on tetra and molecule,
        so colors are consistent across models
    added axis around the hexagon channels
 *
 */
package discreteGroup.quartz;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import charlesgunn.anim.jreality.SceneGraphAnimator;
import charlesgunn.anim.plugin.AnimationPlugin;
import charlesgunn.jreality.newtools.FlyTool;
import charlesgunn.jreality.plugin.TermesSpherePlugin;
import charlesgunn.jreality.viewer.Assignment;
import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.geometry.SliceBoxFactory;
import de.jreality.geometry.TubeUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.Shell;
import de.jreality.plugin.basic.ViewPreferences;
import de.jreality.plugin.content.ContentLoader;
import de.jreality.plugin.experimental.ViewerKeyListenerPlugin;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.ClickWheelCameraZoomTool;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.AbstractDGSGR;
import de.jtem.discretegroup.plugin.FogPlugin;
import de.jtem.discretegroup.plugin.TessellatedContent;
import de.jtem.jrworkspace.plugin.Plugin;

public class QuartzCrystal extends Assignment {

	protected transient SceneGraphComponent 
		world = SceneGraphUtility.createFullSceneGraphComponent("world"),
			ddsgc = SceneGraphUtility.createFullSceneGraphComponent("dd"),
			axis3sgc = SceneGraphUtility.createFullSceneGraphComponent("axis 3"),
			axis6sgc = SceneGraphUtility.createFullSceneGraphComponent("axis 6"),
			order3chan = SceneGraphUtility.createFullSceneGraphComponent("order 3 chan"),
			tetraGeomSGC = SceneGraphUtility.createFullSceneGraphComponent("tetra geom"),
			tetraHalfGeomSGC = SceneGraphUtility.createFullSceneGraphComponent("half tetra geom"),
				tetrasgc = SceneGraphUtility.createFullSceneGraphComponent("tetrasgc"),
				bassgc = SceneGraphUtility.createFullSceneGraphComponent("bassgc"),
			trianglesgc = SceneGraphUtility.createFullSceneGraphComponent("rhombsgc");
	transient boolean single = false,
			showAxes = true,
			showRhomb = true,
			showTetra = true,
			showBAS = false,
			doSliceBox = false,
			doTessellatedContent = false,
			doHalfTetra = false;
	transient public QuartzGeometry quartzGeom = new QuartzGeometry(this);
	transient public QuartzGroup quartzGroup = new QuartzGroup(this);
	transient AbstractDGSGR[] sgrLevels = null;
	protected Transformation avatarT;
	
	transient SliceBoxFactory sbf;	
	transient TessellatedContent tessellatedContent = new TessellatedContent();
	
	@Override
	public SceneGraphComponent getContent() {
		quartzGroup.init();
		
		sgrLevels = quartzGroup.getLevels();
		
		tetrasgc.setVisible(showTetra);
		bassgc.addChild(quartzGeom.getBallAndStick());
		bassgc.setVisible(showBAS);
		tetraGeomSGC.addChildren(tetrasgc, bassgc);
		tetraHalfGeomSGC.addChildren(tetrasgc, bassgc);
		setDoHalfTetra(doHalfTetra);
		
		Appearance ap = axis3sgc.getAppearance();
		int xsecN = 9;
		ap.setName("a3");
		ap.setAttribute("crossSection", TubeUtility.getNgon(9));
		ap.setAttribute("lineShader.tubeRadius", .0125);
		ap = axis6sgc.getAppearance();
		ap.setName("a6");
		ap.setAttribute("crossSection", TubeUtility.getNgon(9));
		ap.setAttribute("lineShader.tubeRadius", .01);
//		ap.setAttribute("lineShader.diffuseColor", Color.white);

		trianglesgc.setGeometry(quartzGeom.getTriangle());
		ap = trianglesgc.getAppearance();
		ap.setAttribute(CommonAttributes.FACE_DRAW, false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute("pointShader.diffuseColor", Color.white);
		ap.setAttribute("pointShader.pointRadius", .01);
//		ap.setAttribute("lineShader.diffuseColor", Color.white);
//		ap.setAttribute("lineShader.tubeRadius", .02);
	
		axis3sgc.setGeometry(quartzGeom.get3Axis());
		axis6sgc.setGeometry(quartzGeom.get6Axis());
		ap = axis6sgc.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", QuartzConstants.chan6Color);

		
		updateTetras();
				
		ap = world.getAppearance();
		ap.setAttribute("polygonShader.diffuseColor", Color.white);
		ap.setAttribute("lineShader.diffuseColor", Color.white);
		ap.setAttribute(SceneGraphAnimator.ANIMATED, false);

		sgrLevels[0].getFundamentalRegion().addChildren(axis3sgc);
		sgrLevels[0].getFundamentalRegion().addChildren(tetraHalfGeomSGC);
		sgrLevels[1].getFundamentalRegion().addChild(sgrLevels[0].getRepresentationRoot());
		sgrLevels[1].getFundamentalRegion().addChildren(tetraGeomSGC);
		sgrLevels[1].getFundamentalRegion().addChildren(axis6sgc);
		sgrLevels[2].getFundamentalRegion().addChildren(sgrLevels[1].getRepresentationRoot(), trianglesgc);
		sgrLevels[3].getFundamentalRegion().addChild(sgrLevels[2].getRepresentationRoot());

		world.addChildren(sgrLevels[3].getRepresentationRoot());
		
		if (doSliceBox) {
		  	sbf = new SliceBoxFactory(world);
		  	sbf.setSeparation(.4);
		  	sbf.update();
			inspector.add(sbf.getInspector());
			SceneGraphComponent foo = sbf.getSliceBoxSGC();
			foo.addTool(new ClickWheelCameraZoomTool());
			return foo;
		}
		return world;
	}
	
	public SceneGraphComponent updateTetras() {
		quartzGeom.updateTetras();
		quartzGeom.getTetraM().assignTo(tetraGeomSGC);
		return tetraGeomSGC;
	}
	
	public void setDoHalfTetra(boolean b) {
		doHalfTetra = b;
		tetrasgc.setGeometry(doHalfTetra ? quartzGeom.getHalfTetrahedron() : quartzGeom.getTetrahedron());
		tetraGeomSGC.setVisible(!doHalfTetra);
		tetraHalfGeomSGC.setVisible(doHalfTetra);
	}
	
	public void updateC(double c) {
		quartzGroup.updateC(c);

	}
	
	protected transient SceneGraphComponent lights = new SceneGraphComponent("lights");
	
	protected transient double[][] positions = { {-1,-1,-1}, {-.1, 1, .2},{1, .3, -.1}, {.2, -.1, 1}}; //, 
//	{1,1,1}, {1,-1,-1},{-1,1,-1}, {-1,-1,1}}; //{ {-1,-1,-1}, {-.3, 1, .2},{1, .3, -.4}, {.2, -.4, 1}};
	protected transient double intensity = .5;

	public void setLightIntensity(double i) {
		intensity = i;
		setupLights();
	}

	public void setupLights()	{

		if (lights == null)
			lights = new SceneGraphComponent("Euclidean Lights");
		for (int i = 0; i < positions.length; ++i) {
			SceneGraphComponent lightNode = new SceneGraphComponent("light" + i);
			DirectionalLight light = new DirectionalLight();
			light.setIntensity(intensity);
			lightNode.setLight(light);
			MatrixBuilder.euclidean().rotateFromTo(new double[] { 0, 0, 1 }, positions[i]).assignTo(lightNode);
			lights.addChild(lightNode);
		}
	}

	@Override
	public void setupJRViewer(JRViewer v) {
		if (doTessellatedContent) tessellatedContent.setupJRViewer(v);
		super.setupJRViewer(v);
	}

	@Override
	public List<Plugin> getPluginsToRegister()	{
		FogPlugin fp = new FogPlugin();
		if (!doTessellatedContent) {
			fp.setDensity(.00);
			pluginsToLoad.add(fp);
			pluginsToLoad.add(new Scene());
			return super.getPluginsToRegister();
		}
		pluginsToLoad.add(new Shell());
//		pluginsToLoad.add(contentPlugin);
//		pluginsToLoad.add(new ContentTools());
		pluginsToLoad.add(new ContentLoader());
		pluginsToLoad.add(new ViewPreferences());
		animationPlugin = new AnimationPlugin();
		pluginsToLoad.add(animationPlugin);
		pluginsToLoad.add(new ViewerKeyListenerPlugin());
		pluginsToLoad.add(shrinkPanelPlugin);
		pluginsToLoad.add(new TermesSpherePlugin());
		pluginsToLoad.add(tessellatedContent);
		pluginsToLoad.add(fp);
		return pluginsToLoad;
	}


	@Override
	public Component getInspector() {
		inspector.add(quartzGeom.getInspector());
		inspector.add(quartzGroup.getInspector());
		return inspector;
	}

	protected transient boolean animate2 = false, animate33 = false, animate36 = false;

	@Override
	public void display() {
		useContent = !doTessellatedContent;
		super.display();
		setLightIntensity(.25);
		setupLights();
		scene.getAvatarComponent().addChildren(lights);

		viewer = jrviewer.getViewer();
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR,Color.black); // new Color(102,0,51)); 
		
		// set near and far clipping plane
		Camera cam = CameraUtility.getCamera(viewer);
		// these are settings for lying aroumd inside.
		cam.setNear(.1);
		cam.setFar(20.0);
		cam.setFocus(4.0);
		cam.setEyeSeparation(.05);
		
		// activate scene graph animation selectively
		animationPlugin.setAnimateCamera(true);
		animationPlugin.setAnimateSceneGraph(true);
		animationPlugin.getAnimationPanel().getRecordPrefs().setCurrentDirectoryPath("/Volumes/SamsungSSD1T/gunn_local/Movies/quartz/");
		animationPlugin.getAnimationPanel().setResourceDir("src/discretegroup/quartz/");
//		if (doTessellatedContent) {
//			getContent();
//			tessellatedContent.setMasterConstraint(new DiscreteGroupSimpleConstraint(4,4,100));
//			tessellatedContent.setLightIntensity(.25);
//			tessellatedContent.setFollowsCamera(false);
//			tessellatedContent.setClipToCamera(true);
//			tessellatedContent.setGroup(quartzGroup.getSpaceGroup(true), true);
//			tessellatedContent.setContent(sixRep.getRepresentationRoot());
//			tessellatedContent.getTheRepn().update();
//		} else {
			FlyTool flytool;
			SceneGraphPath avatarPath;
			avatarPath = scene.getAvatarPath();
			avatarT = avatarPath.getLastComponent().getTransformation();
			flytool = new FlyTool();
			flytool.setGain(.5);
			avatarPath.getLastComponent().addTool(flytool);
//		}
		((Component) viewer.getViewingComponent()).addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e)	{ 
				switch(e.getKeyCode())	{
					
				case KeyEvent.VK_1:
					quartzGroup.setSingle(3);
					break;
				case KeyEvent.VK_2:
					showAxes = !showAxes;
					axis3sgc.setVisible(showAxes);
					axis6sgc.setVisible(showAxes);
					break;
				case KeyEvent.VK_3:
					showRhomb = !showRhomb;
					trianglesgc.setVisible(showRhomb);
					break;
					
				case KeyEvent.VK_4:
					showTetra = !showTetra;
					tetrasgc.setVisible(showTetra);
					break;

				case KeyEvent.VK_5:
					showBAS = !showBAS;
					bassgc.setVisible(showBAS);
					break;

				case KeyEvent.VK_6:
					updateCamera();
					break;

				case KeyEvent.VK_7:
					centerCamera();
					printBBox();
					break;
					
				case KeyEvent.VK_8:
					doHalfTetra = !doHalfTetra;
					setDoHalfTetra(doHalfTetra);
					break;

				}
			}
		});
	
	}

	protected void printBBox() {
		Rectangle3D bbox = BoundingBoxUtility.calculateBoundingBox(world);
		System.err.println("BBox = "+bbox.toString());
	}

	protected void centerCamera() {
		avatarT.setMatrix(Rn.identityMatrix(4));
	}

	private void updateCamera() {
		Camera cam = CameraUtility.getCamera(viewer);
		cam.setNear(.1);
		cam.setFar(20.0);
	}

	public static void main(String[] args) {
		new QuartzCrystal().display();

	}

}
