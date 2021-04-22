package discreteGroup.maniview;


import java.awt.Color;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import charlesgunn.anim.plugin.AnimationPlugin;
import charlesgunn.jreality.geometry.FrontWindow;
import charlesgunn.jreality.plugin.TermesSpherePlugin;
import de.jreality.io.JrScene;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.Inspector;
import de.jreality.plugin.basic.Shell;
import de.jreality.plugin.basic.ToolSystemPlugin;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.basic.ViewToolBar;
import de.jreality.plugin.menu.ExportMenu;
import de.jreality.renderman.shader.SLShader;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.RenderTrigger;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;


public class ManiviewVR {
	
	{
		Locale.setDefault(Locale.US);
	}
	
//    static ViewerVR vr;
	static SceneGraphComponent sgc = new SceneGraphComponent("minimalSurface");
	
	private ViewerApp va = null;
	private Viewer viewer ;
	boolean testSphericalLens = false;

	//protected static boolean doPlugins = true;

	private JRViewer jrv;

	protected AnimationPlugin animationPlugin;
	
	public ManiviewVR() {
		Maniview tgf = new Maniview(this);
		JrScene scene = createDefaultScene(tgf);
		jrv = new JRViewer(scene);
		jrv.registerPlugin(new TermesSpherePlugin());
		animationPlugin = new AnimationPlugin();
		jrv.registerPlugin(animationPlugin);
		jrv.registerPlugin(new Inspector());
		jrv.registerPlugin(new Shell());
		
		jrv.registerPlugin(new ViewMenuBar());
		jrv.registerPlugin(new ViewToolBar());
		
		jrv.registerPlugin(new ExportMenu());
		tgf.setupTabs( jrv);
		jrv.startup();	
		
		viewer = jrv.getPlugin(View.class).getViewer().getCurrentViewer();
//			jrv.getPlugin(SceneShrinkPanel.class).setShowPanel(true);
		tgf.toolSystem = jrv.getPlugin(ToolSystemPlugin.class).getToolSystem();
		if (testSphericalLens)	{
			FrontWindow fw = new FrontWindow(viewer);
			SceneGraphComponent win = fw.getWindow();
			SLShader sls = new SLShader("spherical");
			win.getAppearance().setAttribute(CommonAttributes.RMAN_SURFACE_SHADER, sls);			
		}

		if (!SystemProperties.isPortal) {
			RenderTrigger rt = new RenderTrigger();
			rt.addSceneGraphComponent( scene.getPath("avatarPath").getLastComponent()); //scene.getSceneRoot()); //winMan.getSceneGraphRepresentation()); //
//			rt.addSceneGraphComponent(viewer.getCameraPath().getLastComponent()); // scene.getSceneRoot()); //winMan.getSceneGraphRepresentation());
			rt.addViewer(viewer);			
		} 

		tgf.makeWorld(viewer);
		tgf.cleanupTrigger(jrv);
		tgf.replaceGroup(Maniview.noneuclideanNames[2]); //Platycosm.names[0]); //
		SceneGraphUtility.setMetric(viewer.getSceneRoot(), tgf.theGroup.getMetric());
		animationPlugin.resetSceneGraph();
	}


	private JrScene createDefaultScene(Maniview tgf) {
		JrScene scene;
		final SceneGraphComponent sceneroot = SceneGraphUtility.createFullSceneGraphComponent("root");
		Appearance ap = sceneroot.getAppearance();
		ap.setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(20,20,40));
		ap.setAttribute(CommonAttributes.BACKGROUND_COLORS, Appearance.INHERITED);
		SceneGraphPath cp = tgf.createDefaultCameraPath(sceneroot);
		SceneGraphPath avatarPath = cp.popNew(); avatarPath.pop();
		List<SceneGraphPath> paths = SceneGraphUtility.getPathsToNamedNodes(sceneroot, "avatar");
		if (paths != Collections.EMPTY_LIST) avatarPath = paths.get(0);			
		SceneGraphPath emptyPath = new SceneGraphPath(sceneroot);
		scene = new JrScene(sceneroot);
		scene.addPath("cameraPath", cp);
		scene.addPath("avatarPath", avatarPath);
		scene.addPath("emptyPickPath", emptyPath);
		return scene;
	}
	

	public static void main(String[] args) {
		try {
			remoteMain(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static ViewerApp remoteMain(String[] args) throws IOException {
//		Secure.setProperty(SystemProperties.AUTO_RENDER, "false");
		Secure.setProperty(SystemProperties.JOGL_COPY_CAT, "true");
		ManiviewVR nbv = new ManiviewVR();
		
		return nbv.va;
	}
}
