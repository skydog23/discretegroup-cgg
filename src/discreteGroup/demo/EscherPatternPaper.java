/*
 * Created on Sep 14, 2014
 *
 */
package discreteGroup.demo;

import java.awt.Color;
import java.util.List;

import charlesgunn.anim.plugin.AnimationPlugin;
import charlesgunn.jreality.viewer.Assignment;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.Shell;
import de.jreality.plugin.basic.ViewPreferences;
import de.jreality.plugin.content.ContentLoader;
import de.jreality.plugin.content.ContentTools;
import de.jreality.plugin.experimental.ViewerKeyListenerPlugin;
import de.jreality.plugin.menu.BackgroundColor;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.groups.WallpaperGroup;
import de.jtem.discretegroup.plugin.TessellatedContent;
import de.jtem.jrworkspace.plugin.Plugin;

public class EscherPatternPaper extends Assignment {

	DiscreteGroup dg = WallpaperGroup.instanceOfGroup("O");
	{
		Matrix stretch = MatrixBuilder.euclidean().scale(17.0/14,1,1).getMatrix();
		DiscreteGroupUtility.conjugate(dg, stretch);
	}
	TessellatedContent tessellatedContent = new TessellatedContent();
	@Override
	public void setupJRViewer(JRViewer v) {
		// TODO Auto-generated method stub
		tessellatedContent.setupJRViewer(v);
		super.setupJRViewer(v);

	}

	@Override
	public List<Plugin> getPluginsToRegister() {
		// TODO Auto-generated method stub
		pluginsToLoad.add(new Shell());
//		pluginsToLoad.add(contentPlugin);
//		pluginsToLoad.add(new ContentTools());
//		pluginsToLoad.add(new ContentLoader());
		pluginsToLoad.add(new ViewPreferences());
		animationPlugin = new AnimationPlugin();
		pluginsToLoad.add(animationPlugin);
		pluginsToLoad.add(new ViewerKeyListenerPlugin());
		pluginsToLoad.add(shrinkPanelPlugin);
		pluginsToLoad.add(tessellatedContent);
		return pluginsToLoad;
	}

	@Override
	public SceneGraphComponent getContent() {
		dg.setConstraint(new DiscreteGroupSimpleConstraint(100));
		dg.update();
//		DiscreteGroupSceneGraphRepresentation dgsgr = new DiscreteGroupSceneGraphRepresentation(dg);
		SceneGraphComponent fundDomSGC = SceneGraphUtility.createFullSceneGraphComponent("fundDom2SGC");
//		dg.setCenterPoint(new double[]{.5,.5,0,1});
//		DiscreteGroupElement[] elementList = dg.getElementList();
//		System.err.println("# ="+elementList.length);
//		dgsgr.setElementList(elementList);
		DirichletDomain dd = new DirichletDomain(dg);
		dd.update();
		fundDomSGC.setGeometry(dd.getDirichletDomain());//Primitives.regularPolygon(4,.5));
		
		//		dgsgr.setWorldNode(fundDomSGC);
//		dgsgr.update();
//		SceneGraphComponent sgc = dgsgr.getRepresentationRoot();
		return fundDomSGC;
	}

	@Override
	public void display() {
		// TODO Auto-generated method stub
		setupJRViewer(jrviewer);
		jrviewer.startup();
		tessellatedContent.setFollowsCamera(false);
		tessellatedContent.setClipToCamera(true);
		tessellatedContent.setGroup(dg, false);
		tessellatedContent.setContent(getContent());
		tessellatedContent.getTheRepn().update();
		Appearance ap = tessellatedContent.getTheRepn().getRepresentationRoot().getAppearance();
		ap.setAttribute("polygonShader.diffuseColor", Color.white);
		ap.setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute("lineShader.diffuseColor", Color.black);
		ap.setAttribute("lightingEnabled", false);
		ap.setAttribute(CommonAttributes.FACE_DRAW, true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		MatrixBuilder.euclidean().translate(0,0,6).
			assignTo(CameraUtility.getCameraNode(jrviewer.getViewer()));
//		MatrixBuilder.euclidean().translate(0,0,-7).
//			scale(17/14.0,1,1).
//			assignTo(tessellatedContent.getTheRepn().getRepresentationRoot());

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new EscherPatternPaper().display();
	}

}
