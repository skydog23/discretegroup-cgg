/*
 * Created on Apr 7, 2011
 *
 */
package discreteGroup.demo;

import java.awt.Color;

import javax.swing.JMenuBar;

import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import discreteGroup.demo.FanSolid.Disk;

public class PolarPlaneDemo2 extends LoadableScene {

	@Override
	public SceneGraphComponent makeWorld() {
		final SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		Disk theDisk = new FanSolid().new Disk();
		theDisk.setHoleSize(0.0);
		theDisk.setColor( Color.cyan);
		theDisk.setSaturated(1.0);
		theDisk.update();
		final SceneGraphComponent thickDiskSGC = theDisk.getThickDiskSGC();
		world.addChild(thickDiskSGC);
		thickDiskSGC.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
		thickDiskSGC.getAppearance().setAttribute("lineShader.diffuseColor", Color.black);
		thickDiskSGC.getAppearance().setAttribute("lineShader.tubeRadius",.005);
		
		IndexedLineSet vec = IndexedLineSetUtility.createCurveFromPoints(new double[][]{{0,0,0,1},{0,0,.9,1}}, false);
		
		BallAndStickFactory basf = new BallAndStickFactory(vec);
		basf.setArrowColor(Color.red);
		basf.setArrowPosition(1.0);
		basf.setShowBalls(false);
		basf.setShowArrows(true);
		basf.setArrowSlope(1.8);
		basf.setStickColor(Color.yellow);
		basf.setStickRadius(.05);
		basf.update();
		SceneGraphComponent vecSGC = SceneGraphUtility.createFullSceneGraphComponent("basf");
		vecSGC.addChild(basf.getSceneGraphComponent());
		world.addChild(vecSGC);
		vecSGC = SceneGraphUtility.createFullSceneGraphComponent("basf");
		vecSGC.addChild(basf.getSceneGraphComponent());
		MatrixBuilder.euclidean().scale(-1,-1,-1).assignTo(vecSGC);
		world.addChild(vecSGC);
		SceneGraphComponent sphereSGC = SceneGraphUtility.createFullSceneGraphComponent("invsphere");
		sphereSGC.setGeometry(SphereUtility.tessellatedIcosahedronSphere(3));
		Appearance ap = sphereSGC.getAppearance();
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.TRANSPARENCY, .9);
		ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		makeCurve();
		world.addChild(sphereSGC);
		ap = world.getAppearance();
		ap.setAttribute(CommonAttributes.AMBIENT_COEFFICIENT, .2);
//		world.addTool(new RotateShapeTool());
//		world.getTransformation().addTransformationListener(new TransformationListener() {
//			
//			public void transformationMatrixChanged(TransformationEvent ev) {
//				Matrix m = new Matrix(world.getTransformation().getMatrix());
//				m = m.getInverse();
//				SceneGraphComponent child = new SceneGraphComponent("child");
//				child.setTransformation(new Transformation(m.getArray()));
//				child.addChild(thickDiskSGC);
//				world.addChild(child);
//			}
//		});
		return world;
	}

	private void makeCurve() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isEncompass() {
		return true;
	}

	@Override
	public void customize(JMenuBar menuBar, PluginSceneLoader psl) {
		psl.getViewer().getSceneRoot().getAppearance().setAttribute("backgroundColor", Color.white);
	}

}
