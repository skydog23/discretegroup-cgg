package discreteGroup.maniview;

import java.awt.Color;

import charlesgunn.jreality.geometry.OneArmedTinManFactory;
import charlesgunn.jreality.geometry.SnakeFactory;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;

public class TestTinManPortal extends LoadableScene {

	private SceneGraphComponent tm, stickTipSGC;
//	private OneArmedTinManFactory tmf;
	SnakeFactory sf;
	OneArmedTinManFactory oatmf;
	@Override
	public SceneGraphComponent makeWorld() {
		TinManTool tmt = new TinManTool();
		oatmf = tmt.getTinManFactory();
		tm = new SceneGraphComponent("avatar Repn");
		tm.setAppearance(new Appearance());
		tm.addTool(tmt);
		tmt.setActive(true);
		tm.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		tm.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", new Color(255,200,0));
		tm.getAppearance().setAttribute("pointShader.polygonShader.diffuseColor", Color.red);
		tm.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		SceneGraphComponent world = new SceneGraphComponent();
		world.addChild(tm);
		SceneGraphComponent circle = new SceneGraphComponent("circle");
		circle.setAppearance(new Appearance());
		circle.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
		circle.setGeometry(IndexedLineSetUtility.circle(10));
		MatrixBuilder.euclidean().scale(.1).assignTo(circle);
		world.addChild(circle);
		MatrixBuilder.euclidean().translate(0,1.7,-3).assignTo(world); 
		stickTipSGC = new SceneGraphComponent("stick tip");
		Appearance ap = new Appearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, false);
//		ap.setAttribute("pointShader.polygonShader.diffuseColor", new Color(0,255,255));
//		ap.setAttribute("pointShader.pointRadius", .1);
		ap.setAttribute("lineShader.polygonShader.diffuseColor", new Color(0,255,255));
		ap.setAttribute("lineShader.diffuseColor", new Color(0,255,255));
		ap.setAttribute("lineShader.tubeRadius", .02);
		stickTipSGC.setAppearance(ap);
		sf = new SnakeFactory(100, 3);
		sf.update();
//		Timer tt = new Timer(20,new ActionListener() {
//
//			public void actionPerformed(ActionEvent e) {
//				sf.addPoint(oatmf.getStickTipWorldPosition());
//				sf.update();
//			}
//			
//		});
//		tt.start();
		stickTipSGC.setGeometry(sf.getSnake());
		world.addChild(stickTipSGC);
		world.addTool(new TraceTool(sf, oatmf, stickTipSGC.getAppearance(), null));
		//update();
		return world;
	}

//	private static ViewerApp mainImpl(String[] args) {
//		Secure.setProperty(SystemProperties.VIEWER,GlobalProperties.DEFAULT_VIEWER); //de.jreality.portal.DesktopPortalViewer");
////		Secure.setProperty(SystemProperties.SYNCH_RENDER, "false");
////		Secure.setProperty("doOwnTools", "false");
//		ViewerApp va = new ViewerApp(new TestTinManPortal().makeWorld());
//		va.getCurrentViewer().getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.black);
//		List<Tool> tools = va.getCurrentViewer().getSceneRoot().getTools();
//		for (Tool t : tools ) {
//			va.getCurrentViewer().getSceneRoot().removeTool(t);
//		}
//		if (va.getCurrentViewer() instanceof InteractiveViewer)
//			((InteractiveViewer) va.getCurrentViewer()).getInfoOverlay().setVisible(true);
//		return va;
//	}
//	public static ViewerApp remoteMain(String[] args) {
//		return mainImpl(args);
//	}

//	public static void main(String[] args) {
//
//		System.err.println("args 0 is "+args[0]);
//		final ViewerApp va = mainImpl(args);
//		//viewerApp options
//		va.setAttachNavigator(true);
//		va.setExternalNavigator(false);
//		va.setAttachBeanShell(true);
//		va.setExternalBeanShell(false);
//
//		va.update();
//		va.display();
//	}
//	
	
}
