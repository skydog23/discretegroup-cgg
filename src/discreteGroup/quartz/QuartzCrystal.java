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
 *
 */
package discreteGroup.quartz;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import charlesgunn.anim.plugin.AnimationPlugin;
import charlesgunn.jreality.newtools.FlyTool;
import charlesgunn.jreality.plugin.TermesSpherePlugin;
import charlesgunn.jreality.viewer.Assignment;
import de.jreality.geometry.SliceBoxFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.Shell;
import de.jreality.plugin.basic.ViewPreferences;
import de.jreality.plugin.content.ContentLoader;
import de.jreality.plugin.experimental.ViewerKeyListenerPlugin;
import de.jreality.scene.Appearance;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.ClickWheelCameraZoomTool;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.plugin.FogPlugin;
import de.jtem.discretegroup.plugin.TessellatedContent;
import de.jtem.jrworkspace.plugin.Plugin;

public class QuartzCrystal extends Assignment {

	SceneGraphComponent 
		world = SceneGraphUtility.createFullSceneGraphComponent("world"),
			ddsgc = SceneGraphUtility.createFullSceneGraphComponent("dd"),
			axissgc = SceneGraphUtility.createFullSceneGraphComponent("axes"),
			order3chan = SceneGraphUtility.createFullSceneGraphComponent("order 3 chan"),
			tetraGeomSGC = SceneGraphUtility.createFullSceneGraphComponent("selector"),
				tetrasgc = SceneGraphUtility.createFullSceneGraphComponent("tetrasgc"),
				bassgc = SceneGraphUtility.createFullSceneGraphComponent("bassgc"),
			tetra3sgc = SceneGraphUtility.createFullSceneGraphComponent("tetra3sgc"),
			sixcellsgc = SceneGraphUtility.createFullSceneGraphComponent("all"),
			trianglesgc = SceneGraphUtility.createFullSceneGraphComponent("rhombsgc");
	boolean single = false,
			showAxes = true,
			showRhomb = true,
			showTetra = true,
			showBAS = false,
			doSliceBox = false,
			doTessellatedContent = false,
			doHalfTetra = true;
	public QuartzGeometry quartzGeom = new QuartzGeometry(this);
	public QuartzGroup quartzGroup = new QuartzGroup(this);
	DiscreteGroupSceneGraphRepresentation spaceRep, sixRep;
	
	SliceBoxFactory sbf;	
	TessellatedContent tessellatedContent = new TessellatedContent();
	
	@Override
	public SceneGraphComponent getContent() {
		quartzGroup.init();
		
//		tetrasgc.setGeometry(quartzGeom.getHalfTetrahedron());
		tetrasgc.setGeometry(doHalfTetra ? quartzGeom.getHalfTetrahedron() : quartzGeom.getTetrahedron());
		tetrasgc.setVisible(showTetra);
		bassgc.addChild(quartzGeom.getBallAndStick());
		bassgc.setVisible(showBAS);
		tetraGeomSGC.addChildren(tetrasgc, bassgc);
		for (int i = 0; i<3; ++i)	{
			SceneGraphComponent child = SceneGraphUtility.createFullSceneGraphComponent("child"+i);
			child.addChild(tetraGeomSGC);
			tetra3sgc.addChild(child);
		}
		Appearance ap = axissgc.getAppearance();
		ap.setAttribute("lineShader.tubeRadius", .01);
		ap.setAttribute("lineShader.diffuseColor", Color.white);

		order3chan.addChildren(trianglesgc, axissgc, tetra3sgc);

		trianglesgc.setGeometry(quartzGeom.getTriangle());
		ap = trianglesgc.getAppearance();
		ap.setAttribute(CommonAttributes.FACE_DRAW, false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute("pointShader.diffuseColor", Color.white);
		ap.setAttribute("pointShader.pointRadius", .01);
		ap.setAttribute("lineShader.diffuseColor", Color.white);
		ap.setAttribute("lineShader.tubeRadius", .01);
		
		updateAxis();
		updateTetras();
		
		sixRep = quartzGroup.getSixcellRep();
		sixcellsgc.addChildren(sixRep.getRepresentationRoot());
		sixRep.setWorldNode(order3chan);
		sixRep.update();
		
		spaceRep = quartzGroup.getSpaceRep();
		spaceRep.setWorldNode(sixcellsgc);
		spaceRep.update();
		world.addChild(spaceRep.getRepresentationRoot());
		
		ap = world.getAppearance();
		ap.setAttribute("polygonShader.diffuseColor", Color.white);
		ap.setAttribute("lineShader.diffuseColor", Color.white);
		
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

	private SceneGraphComponent updateAxis() {
		axissgc.setGeometry(quartzGeom.getAxis());
		return axissgc;
	}
	
	public SceneGraphComponent updateTetras() {
		quartzGeom.updateTetras();
		quartzGeom.getTetraM().assignTo(tetraGeomSGC);
		Matrix[] s3 = quartzGroup.getTriChannelM();
		for (int i = 0; i<3; ++i)	{
			s3[i].assignTo(tetra3sgc.getChildComponent(i));
		}
		return tetra3sgc;
	}
	

	public void updateC(double c) {
		quartzGroup.updateC(c);
		updateAxis();

	}
	
	SceneGraphComponent lights = new SceneGraphComponent("lights");
	
	double[][] positions = { {-1,-1,-1}, {-.1, 1, .2},{1, .3, -.1}, {.2, -.1, 1}}; //, 
//	{1,1,1}, {1,-1,-1},{-1,1,-1}, {-1,-1,1}}; //{ {-1,-1,-1}, {-.3, 1, .2},{1, .3, -.4}, {.2, -.4, 1}};
	double intensity = .5;

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
			fp.setDensity(.03);
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

	boolean animate = false;

	@Override
	public void display() {
		useContent = !doTessellatedContent;
		super.display();
		setLightIntensity(.25);
		setupLights();
		scene.getAvatarComponent().addChildren(lights);

		viewer = jrviewer.getViewer();
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(102,0,51)); //new Color(255,204,204));
		if (doTessellatedContent) {
			getContent();
			tessellatedContent.setMasterConstraint(new DiscreteGroupSimpleConstraint(4,4,100));
			tessellatedContent.setLightIntensity(.25);
			tessellatedContent.setFollowsCamera(false);
			tessellatedContent.setClipToCamera(true);
			tessellatedContent.setGroup(quartzGroup.getSpaceGroup(true), true);
			tessellatedContent.setContent(sixRep.getRepresentationRoot());
			tessellatedContent.getTheRepn().update();
		} else {
//			de.jreality.plugin.basic.Scene scene = 
//					jrviewer.getPlugin(de.jreality.plugin.basic.Scene.class);
			FlyTool flytool;
			SceneGraphPath avatarPath;
			avatarPath = scene.getAvatarPath();
			flytool = new FlyTool();
			flytool.setGain(.5);
			avatarPath.getLastComponent().addTool(flytool);
		}
		((Component) viewer.getViewingComponent()).addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e)	{ 
				switch(e.getKeyCode())	{
					
				case KeyEvent.VK_1:
					single = !single;
					quartzGroup.setSingle(single);
					break;
				case KeyEvent.VK_2:
					showAxes = !showAxes;
					axissgc.setVisible(showAxes);
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
					quartzGroup.toggleHalfTurn();
					break;

				case KeyEvent.VK_7:
					animate = !animate;
					quartzGroup.runAI(animate);
					break;
					
				case KeyEvent.VK_8:
					doHalfTetra = !doHalfTetra;
					tetrasgc.setGeometry(doHalfTetra ? quartzGeom.getHalfTetrahedron() : quartzGeom.getTetrahedron());
					break;

				}
			}
		});
	
	}

	public static void main(String[] args) {
		new QuartzCrystal().display();

	}

}
