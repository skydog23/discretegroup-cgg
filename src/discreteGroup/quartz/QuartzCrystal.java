/*
 * Created on 10 Apr 2023
 *
 */
package discreteGroup.quartz;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.anim.plugin.AnimationPlugin;
import charlesgunn.jreality.newtools.FlyTool;
import charlesgunn.jreality.plugin.TermesSpherePlugin;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.Shell;
import de.jreality.plugin.basic.ViewPreferences;
import de.jreality.plugin.content.ContentLoader;
import de.jreality.plugin.content.ContentTools;
import de.jreality.plugin.experimental.ViewerKeyListenerPlugin;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupColorPicker;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupTranslationConstraint;
import de.jtem.discretegroup.plugin.FogPlugin;
import de.jtem.discretegroup.plugin.TessellatedContent;
import de.jtem.jrworkspace.plugin.Plugin;

public class QuartzCrystal extends Assignment {

	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world"),
			ddsgc = SceneGraphUtility.createFullSceneGraphComponent("dd"),
			axissgc = SceneGraphUtility.createFullSceneGraphComponent("axes"),
			order3chan = SceneGraphUtility.createFullSceneGraphComponent("order 3 chan"),
			selectsgc = SceneGraphUtility.createFullSceneGraphComponent("selector"),
					tetrasgc = SceneGraphUtility.createFullSceneGraphComponent("tetrasgc"),
					bassgc = SceneGraphUtility.createFullSceneGraphComponent("bassgc"),
			tetra3sgc = SceneGraphUtility.createFullSceneGraphComponent("tetra3sgc"),
			sixcellsgc = SceneGraphUtility.createFullSceneGraphComponent("all"),
			rhombsgc = SceneGraphUtility.createFullSceneGraphComponent("rhombsgc");
	boolean single = false,
			showAxes = true,
			showRhomb = true,
			showTetra = true,
			showBAS = false;
	QuartzGeometry quartzGeom = new QuartzGeometry(this);
	QuartzGroup quartzGroup = new QuartzGroup(this, quartzGeom);
	DiscreteGroupSceneGraphRepresentation spaceRep, sixRep;
	
	boolean doTessellatedContent = false;
	TessellatedContent tessellatedContent = new TessellatedContent();

	@Override
	public SceneGraphComponent getContent() {
//		tetrasgc.setGeometry(quartzGeom.getHalfTetrahedron());
		tetrasgc.setGeometry(quartzGeom.getTetrahedron());
		bassgc.addChild(quartzGeom.getBallAndStick());
		selectsgc.addChildren(tetrasgc, bassgc);
		tetrasgc.setVisible(showTetra);
		bassgc.setVisible(showBAS);
		order3chan.addChildren(axissgc, tetra3sgc);

		initRhomb();
		
		updateAxis();
		updateTetras();
		
		sixRep = quartzGroup.getSixcellRep();
		sixcellsgc.addChildren(rhombsgc, sixRep.getRepresentationRoot());
		sixRep.setWorldNode(order3chan);
		sixRep.update();
		
		spaceRep = quartzGroup.getSpaceRep();
		spaceRep.setWorldNode(sixcellsgc);
		spaceRep.update();
		world.addChild(spaceRep.getRepresentationRoot());
		
		Appearance ap = world.getAppearance();
		ap.setAttribute("polygonShader.diffuseColor", Color.white);
		ap.setAttribute("lineShader.diffuseColor", Color.white);
		
		return world;
	}


	private void initRhomb() {
		rhombsgc.setGeometry(quartzGeom.getRhomb());
		Appearance ap = rhombsgc.getAppearance();
		ap.setAttribute(CommonAttributes.FACE_DRAW, false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute("pointShader.diffuseColor", Color.white);
		//ap.setAttribute(CommonAttributes.TUBE_RADIUS, .01);
	}


	private SceneGraphComponent updateAxis() {
		axissgc.setGeometry(quartzGeom.getAxis());
		Appearance ap = axissgc.getAppearance();
		ap.setAttribute("lineShader.tubeRadius", .01);
		ap.setAttribute("lineShader.diffuseColor", Color.white);
		return axissgc;
	}
	
	public SceneGraphComponent updateTetras() {
		tetra3sgc.removeAllChildren();
		quartzGeom.updateTetras();
		quartzGeom.getTetraM().assignTo(selectsgc);
		Matrix[] s3 = quartzGeom.getScrew3();
		for (int i = 0; i<3; ++i)	{
			SceneGraphComponent child = SceneGraphUtility.createFullSceneGraphComponent("child"+i);
//			Appearance ap = child.getAppearance();
//			ap.setAttribute("polygonShader.diffuseColor", clrs[i]);
			s3[i].assignTo(child);
			child.addChild(selectsgc);
			tetra3sgc.addChild(child);
		}
		return tetra3sgc;
	}

	public void updateC(double c) {
		quartzGroup.updateC(c);
		updateAxis();

	}
	
	@Override
	public void setupJRViewer(JRViewer v) {
		if (doTessellatedContent) tessellatedContent.setupJRViewer(v);
		super.setupJRViewer(v);
	}

	@Override
	public List<Plugin> getPluginsToRegister()	{
		if (!doTessellatedContent) {
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
		pluginsToLoad.add(new FogPlugin());
		return pluginsToLoad;
	}


	@Override
	public Component getInspector() {
		
		inspector.add(quartzGeom.getInspector());
		inspector.add(quartzGroup.getInspector());
		return inspector;
	}

	@Override
	public void display() {
		useContent = !doTessellatedContent;
		super.display();
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
			de.jreality.plugin.basic.Scene scene = 
					jrviewer.getPlugin(de.jreality.plugin.basic.Scene.class);
			FlyTool flytool;
			SceneGraphPath avatarPath;
			avatarPath = scene.getAvatarPath();
			Transformation avatarT = avatarPath.getLastComponent().getTransformation();
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
					rhombsgc.setVisible(showRhomb);
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

				}
			}
		});
	
	}

	public static void main(String[] args) {
		new QuartzCrystal().display();

	}

}
