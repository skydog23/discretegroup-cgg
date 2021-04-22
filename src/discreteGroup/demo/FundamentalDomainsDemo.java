/*
 * Created on Sep 1, 2005
 *
 */
package discreteGroup.demo;

import javax.swing.JMenuBar;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.groups.WallpaperGroup;

/**
 * @author gunn
 *
 */
public class FundamentalDomainsDemo extends LoadableScene {

	@Override
	public SceneGraphComponent makeWorld()	{
		SceneGraphComponent theWorld = SceneGraphUtility.createFullSceneGraphComponent();
		theWorld.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		theWorld.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,new java.awt.Color(225,225,225));
		theWorld.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,new java.awt.Color(100,100,100));
		theWorld.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_STIPPLE,false);
		theWorld.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_STIPPLE_PATTERN, 0x5555);
		for (int i = 0; i<WallpaperGroup.names.length; ++i)	{
			WallpaperGroup wg = WallpaperGroup.instanceOfGroup(i);
			SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent();
			MatrixBuilder.euclidean().translate(2.0*(i%6), -2.0 * (i/6), 0).assignTo(sgc);
			theWorld.addChild(sgc);
			
			SceneGraphComponent sgc2 = SceneGraphUtility.createFullSceneGraphComponent();
			sgc2.setGeometry(wg.getDefaultFundamentalRegion());
			//if (i < 7) {
			if (wg.getGeneratorRepresentations() != null) sgc2.addChild(wg.getGeneratorRepresentations());
			//}
			Rectangle3D bbox = BoundingBoxUtility.calculateBoundingBox(sgc2);
			double stretch = Math.max(bbox.getExtent()[0], bbox.getExtent()[1]);
			if (stretch != 0) stretch = 1.0/stretch;
			if (i == 2 || i == 10 || i == 12) stretch *= 1.3;
//			sgc2.getTransformation().setStretch(stretch);
			double[] ctrans = Rn.times(null, -1.0, bbox.getCenter());
//			sgc2.getTransformation().setTranslation(ctrans);
			MatrixBuilder.euclidean().translate(ctrans).scale(stretch).assignTo(sgc2);
			sgc.addChild(sgc2);
		}
		return theWorld;
	}
	
	@Override
	public boolean isEncompass() {
		return true;
	}

	public boolean addBackPlane()	{return false; }

	@Override
	public void customize(JMenuBar menuBar, Viewer viewer) {
		CameraUtility.getCamera(viewer).setPerspective(false);
		CameraUtility.getCamera(viewer).setFocus(6.0d);
		MatrixBuilder.euclidean().translate(0,0,10).assignTo(CameraUtility.getCameraNode(viewer));
	}
	
}
