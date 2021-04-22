package discreteGroup.wallpaper2;

import static de.jreality.shader.CommonAttributes.BACKGROUND_COLOR;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LIGHTING_ENABLED;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.util.SystemProperties.JREALITY_DATA;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;

import charlesgunn.anim.plugin.AnimationPlugin;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.content.ContentAppearance;
import de.jreality.plugin.content.ContentLoader;
import de.jreality.plugin.content.ContentTools;
import de.jreality.plugin.content.DirectContent;
import de.jreality.plugin.menu.BackgroundColor;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.event.CameraEvent;
import de.jreality.scene.event.CameraListener;
import de.jreality.scene.pick.Graphics3D;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.viewerapp.SelectionManagerImpl;
import de.jreality.ui.viewerapp.ViewerSwitch;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.Secure;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupColorPicker;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.core.DiscreteGroupViewportConstraint;
import de.jtem.discretegroup.groups.WallpaperGroup;
import de.jtem.discretegroup.plugin.TessellatedContent;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import discreteGroup.io.ImportExport;
import discreteGroup.wallpaper2.WallpaperPlugin;

/**
 * Wallpaper is based on the 17 Euclidean wallpaper groups.  It provides a simple framework for
 * experimenting with these groups.  
 * 
 * TODO
 * [29.08.08]
 * 	add tool tips to GUI
 *  add "duplicator" plugin (begins with single tile, allows user to grow it 
 *      tile by tile)
 *  implement general cyclic and dihedral groups
 *  make ShadedSphereImage into a factory (to avoid over-using static variables)
 *  is it possible to display the PaintSource inspector in full screen mode? (like iPhoto)
 *  icons for the groups instead of names (make a separate tab to select group)
 *  optimize texture copies to copy only changed regions (compare original code sample)
 *  make brush in PaintSource a plugin too
 *  AffinePlugin:  add a flag to control whether the generators are actually 
 *      changed, or only the root transformation of the scene graph repn.
 * 4*2 has fund. domain that is too small.
 * paint: be able to select paint color from the monitor; and store brush states (undo/redo)
 * [07.02.12]
 * modularize everything better
 * take advantage of plugin system features to save/restore state automatically
 * 	save off current painting state!
 * allow several brushes at once for automatic mode                   
 * @author Charles Gunn
 *
 */
public class Wallpaper extends Plugin {
	private static final long serialVersionUID = 1L;
	protected transient SceneGraphComponent  
		theWorld,						// this is the root of our world, including tessellation
		backgroundTile, 				// contains the scene to be tessellated
		generatorRepresentation, 		// this shows the generators of the wallpaper groups
		theFullMonty,
		currentPluginTile,
		pluginTiles[],
		elSGC,
		singleFundamentalTile;							// SGC containing an L-shaped polygon
	protected boolean showGenerators = false, 		// should the generator representations be shown?
		showEl = false,					// should the L-shape be shown?
		pickable = true,
		copycat = false;
	transient protected boolean 			// should the L-shape be shown?
			running = false;
	protected transient DiscreteGroupSceneGraphRepresentation groupSceneGraph = null;	// represents wallpaper group in a scene graph
	protected int maxElements =  10000;				// maximum number of group elements to compute
	protected transient WallpaperGroup theGroup ;					// the particular wallpaper group
	protected transient double globalTime = 0.0;			// time for the animation
	protected transient Viewer viewer;						// for calling viewer.renderAsync() when we make changes
	protected transient Graphics3D dgContext;				// object for keeping track of transformations in scene graph
	protected transient SceneGraphPath pathToWallpaperGroupCOB;	// the path to change of basis node in the scene graph repn of wallpaper group
	protected transient DiscreteGroupViewportConstraint viewportConstraint;
	protected transient Timer timer = null, camTimer = null;
	transient long time;
	transient IndexedFaceSet defaultFundamentalRegion;
	WallpaperPlugin currentPlugin, plugins[];
	transient String[] pluginNames;
	transient private JPanel inspector;
	transient private JPanel pluginPanel = new JPanel();
	transient private TitledBorder pluginBorder;
	transient int currentIndex = 0;
//	transient private JPopupMenu contextMenu = new JPopupMenu();
	transient private JMenu contextMenu = new JMenu("Wallpaper");
	transient JCheckBoxMenuItem showGenCB, showLCB;
	transient JMenuItem runMI, resetMI;
	transient boolean cameraHasChanged =false;
	 String groupName;
		transient protected JRViewer jrviewer = new JRViewer();
		transient protected ShrinkPanelPlugin shrinkPanelPlugin = new ShrinkPanelPlugin(){

			@Override
			public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
				return View.class;
			}
			
		};
	
	public SceneGraphComponent getContent()	{
		SceneGraphNode.setThreadSafe(false);
		theWorld = SceneGraphUtility.createFullSceneGraphComponent("theWorld");
		theWorld.getAppearance().setAttribute(
				LINE_SHADER+"."+DIFFUSE_COLOR, java.awt.Color.WHITE);
		theWorld.getAppearance().setAttribute(
				POLYGON_SHADER+"."+DIFFUSE_COLOR, java.awt.Color.WHITE);
		theWorld.getAppearance().setAttribute(LIGHTING_ENABLED, false);
		
		elSGC = DiscreteGroupUtility.getElKit();
		MatrixBuilder.euclidean(elSGC.getTransformation()).scale(2).assignTo(elSGC);
		elSGC.getAppearance().setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, new Color(200, 0, 200));
		elSGC.setVisible(showEl);
		theFullMonty = SceneGraphUtility.createFullSceneGraphComponent("theFullMonty");
		backgroundTile = SceneGraphUtility.createFullSceneGraphComponent("backgroundTile");
		Appearance ap = theFullMonty.getAppearance(); 
		ap.setAttribute(EDGE_DRAW, false);
		ap.setAttribute(FACE_DRAW, true);
		backgroundTile.addChild(elSGC);
		theFullMonty.addChild(backgroundTile);
		running = false;
		timer = new Timer(20, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				currentPlugin.update();
			}
			
		});
		// we check very occasionally for change in camera, so as
		// not to get bogged down in recalculating small changes
		camTimer = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (cameraHasChanged)	{
					updateGroupElements();
					cameraHasChanged = false;
				}
			}
		});
		camTimer.start();
		theWorld.setPickable(pickable);
		return theWorld;
	}
	
	public void replaceGroup(String name)	{
		timer.stop();
		theGroup = WallpaperGroup.instanceOfGroup(name);
		replaceGroup(theGroup);
	}
	public void replaceGroup(DiscreteGroup theGroup)	{
		//DiscreteGroup dg = WallpaperGroup.instanceOfGroup("*236");
		timer.stop();
//		discreteGroup.io.ImportExport io = new discreteGroup.io.ImportExport();
//		File f= new File("/tmp/testArchive.xml");
//		io.write(theGroup, f);
//		theGroup = (WallpaperGroup) ImportExport.readDiscreteGroup(f);


		SceneGraphUtility.removeChildren(theWorld);
		if (groupSceneGraph != null) groupSceneGraph.dispose();
		SceneGraphPath sgp = new SceneGraphPath(viewer.getSceneRoot());
		SelectionManagerImpl.selectionManagerForViewer(viewer).setSelectionPath(sgp);
		// get a new scene graph representation for the group
		groupSceneGraph = new DiscreteGroupSceneGraphRepresentation(theGroup, true);
		groupSceneGraph.setAppList(DiscreteGroupColorPicker.appearanceList);
		// get a scene graph representing the generators of the group and add it to the root of the representation
		defaultFundamentalRegion = (IndexedFaceSet) theGroup.getDefaultFundamentalRegion();
		SceneGraphComponent fundTileCOB = SceneGraphUtility.createFullSceneGraphComponent("single tile COB");
		theFundamentalTile = SceneGraphUtility.createFullSceneGraphComponent("single tile");
		theFundamentalTile.setGeometry(defaultFundamentalRegion);
		theFundamentalTile.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.blue);
		theFundamentalTile.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, .35);
		theFundamentalTile.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		theFundamentalTile.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		generatorRepresentation = theGroup.getGeneratorRepresentations();
		if (generatorRepresentation != null) 	{
			generatorRepresentation.getAppearance().setAttribute(FACE_DRAW, true);
			generatorRepresentation.getAppearance().setAttribute(
					LINE_SHADER+"."+DIFFUSE_COLOR, java.awt.Color.BLACK);			
			theFundamentalTile.addChild(generatorRepresentation);
			generatorRepresentation.setPickable(false);
			generatorRepresentation.setVisible(showGenerators);
		}
		MatrixBuilder.euclidean().translate(0, 0, .01).assignTo(theFundamentalTile);
		fundTileCOB.setTransformation(
				new Transformation(theGroup.getChangeOfBasis().getArray()));
		fundTileCOB.addChild(theFundamentalTile);
		// Attach the geometry to be tessellated to the representation
		groupSceneGraph.setWorldNode(theFullMonty);
		// update and add the resulting scene graph to the world node
//		groupSceneGraph.update();
		SceneGraphComponent sgn =  groupSceneGraph.getRepresentationRoot();
		sgn.getAppearance().setAttribute("singlePeer", true);
//		theWorld.getAppearance().setAttribute("singlePeer", true);
		theWorld.addChild(sgn);
		theWorld.addChild(fundTileCOB);
		// set up a special constraint based on the viewport to prune the set of group elements so that
		// only those elements are generated which are visible
		List l = SceneGraphUtility.getPathsBetween(viewer.getSceneRoot(), sgn);
		pathToWallpaperGroupCOB = (SceneGraphPath) l.get(0);
		if (pathToWallpaperGroupCOB != null) 
			pathToWallpaperGroupCOB.push(groupSceneGraph.getChangeOfBasisNode());
		dgContext = new Graphics3D(viewer.getCameraPath(), pathToWallpaperGroupCOB, 
				CameraUtility.getAspectRatio(viewer));
		viewportConstraint = new DiscreteGroupViewportConstraint( 0d, 0, -1.0, -1, dgContext);
		viewportConstraint.setMaxNumberElements(maxElements);
		viewportConstraint.setFudge(1.3);
		groupSceneGraph.setElementList(DiscreteGroupUtility.generateElements(theGroup,viewportConstraint));
		System.err.println("Group has "+groupSceneGraph.getElementList().length+" elements");
		groupSceneGraph.update();
		if (currentPlugin != null) currentPlugin.replaceGroup();
		if (running) timer.start();
//		viewer.renderAsync();
	}

	private void updateGroupElements() {
		timer.stop();
		double aspectRatio = CameraUtility.getAspectRatio(viewer);
		dgContext.setAspectRatio(aspectRatio);
		System.err.println("Aspect ratio = "+aspectRatio);
		viewportConstraint.setCenterPoint(theGroup.getCenterPoint());
		viewportConstraint.update();
		groupSceneGraph.setElementList(DiscreteGroupUtility.generateElements(theGroup,viewportConstraint));
		groupSceneGraph.update();
		System.err.println("Setting dg el list");
		if (running) timer.start();
	}
	
	private void setCurrentPlugin(final WallpaperPlugin plugin) {
		Scene.executeWriter(backgroundTile, new Runnable() {

			public void run() {
				if (currentPlugin != null) {
					currentPlugin.deactivate();
					pluginPanel.setBorder(null);
					if (currentPlugin.getInspectorPanel() != null) pluginPanel.remove(currentPlugin.getInspectorPanel());
//					if (currentPlugin.getTool() != null) backgroundTile.removeTool(currentPlugin.getTool());
					if (currentPlugin.getSceneGraphComponent() != null) 
						currentPlugin.getSceneGraphComponent().setVisible(false);
				}
				currentPlugin = plugin;
				System.err.println("resetting current plugin");
				currentPlugin.replaceGroup();
//				if (currentPlugin.getTool() != null) backgroundTile.addTool(currentPlugin.getTool());
				if (currentPlugin.getSceneGraphComponent() != null) 
					currentPlugin.getSceneGraphComponent().setVisible(true);
				if (currentPlugin.getInspectorPanel() != null)  {
//					pluginBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
//													currentPlugin.getName());
//					pluginPanel.setBorder(pluginBorder);
					pluginPanel.setLayout(new GridLayout());
					pluginPanel.add(currentPlugin.getInspectorPanel());
				}
				currentPlugin.activate();
				inspector.validate();
			}
		});
				System.err.println("finished setting current plugin");
	}

	public void init() {
		viewer = psl.getViewer();
		// continue with tasks which depend on knowing the viewer
		viewer.getSceneRoot().getAppearance().setAttribute(BACKGROUND_COLOR, Color.black);
		double distanceToScreen = 6.0;		// distance to screen (scales tessellation)
		MatrixBuilder.euclidean().translate(0,0,distanceToScreen).assignTo(CameraUtility.getCameraNode(viewer));
		CameraUtility.getCamera(viewer).setFocus(distanceToScreen);
		CameraUtility.getCamera(viewer).addCameraListener(new CameraListener() {

			public void cameraChanged(CameraEvent ev) {
				cameraHasChanged = true;
			}
			
		});
		((Component) viewer.getViewingComponent()).addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				cameraHasChanged = true;
			}
			
		});
		ViewMenuBar viewerMenu = psl.getController().getPlugin(ViewMenuBar.class);

		JMenuItem jmi = new JMenuItem("Save wallpaper as ...");
		jmi.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent actionevent) {
				saveWallpaper();
			}
		});
		viewerMenu.addMenuItem(
				getClass(),
				10.0,
				jmi,
				"File"
		);

		jmi = new JMenuItem("Load wallpaper ...");
		jmi.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent actionevent) {
				loadWallpaper();
			}
		});
		viewerMenu.addMenuItem(
				getClass(),
				10.0,
				jmi,
				"File"
		);


		groupName = "*236";
		replaceGroup(groupName);
		plugins = new WallpaperPlugin[5];
		plugins[0] = new PaintPlugin(this);
		plugins[1] = new TexturePlugin(this);
		plugins[2] = new DirichletDomainPlugin(this);
		plugins[3] = new AffinePlugin(this);
		plugins[4] = new WallpaperPlugin(this) {

			@Override
			public String getName() {
				return "none";
			}
			
		};
		pluginNames = new String[plugins.length];
		pluginTiles = new SceneGraphComponent[plugins.length];
		for (int i = 0; i<plugins.length; ++i) {
			pluginNames[i] = plugins[i].getName();
			pluginTiles[i] = plugins[i].getSceneGraphComponent();
			pluginTiles[i].setVisible(false);
			theFullMonty.addChild(pluginTiles[i]);
		}
		getInspector();
		setCurrentPlugin(plugins[currentIndex]);
		
		
		// list all the groups in the menu
		JMenu groupM = new JMenu("Group");
		ButtonGroup bg = new ButtonGroup();
		final String[] gnames = WallpaperGroup.names;
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
//		theMenuBar.add(testM);
		contextMenu.add(groupM);
		contextMenu.addSeparator();
		
		bg = new ButtonGroup();
		JMenu pluginM = new JMenu("Plugin");
		for (int i = 0; i<pluginNames.length; ++i)	{
			final int j = i;
			JMenuItem jm = pluginM.add(new JRadioButtonMenuItem(pluginNames[i]));
			jm.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)	{
					setCurrentPlugin(plugins[j]);
				}
			});
			bg.add(jm);
		}
		contextMenu.add(pluginM);
		contextMenu.addSeparator();
		
		Action action = new AbstractAction("Show generators") {

			public void actionPerformed(ActionEvent e) {
				showGenerators = !showGenerators;
				showGenCB.setSelected(showGenerators);
				theFundamentalTile.setVisible(showGenerators);
				viewer.renderAsync();
			}
			
		};
		// construct three menu buttons for special actions
		showGenCB = new JCheckBoxMenuItem(action);
		showGenCB.setSelected(showGenerators);
		showGenCB.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0));
		showGenCB.setAction(action);
		
		action.putValue(Action.SHORT_DESCRIPTION, "Display group generator icons");
		KeyStroke acc = KeyStroke.getKeyStroke(KeyEvent.VK_1, 0);
		action.putValue(Action.ACCELERATOR_KEY, acc);
		contextMenu.getActionMap().put(acc, action);
		
		contextMenu.add(showGenCB);
		
		action = new AbstractAction("Show L") {

			public void actionPerformed(ActionEvent e) {
				showEl = !showEl;
				showLCB.setSelected(showEl);
				elSGC.setVisible(showEl);
				viewer.renderAsync();
			}
			
		};
		// construct three menu buttons for special actions
		showLCB = new JCheckBoxMenuItem(action);
		showLCB.setSelected(showGenerators);
		acc = KeyStroke.getKeyStroke(KeyEvent.VK_2, 0);
		showLCB.setAccelerator(acc);
		showLCB.setAction(action);
		
		action.putValue(Action.SHORT_DESCRIPTION, "Display the letter L");
		action.putValue(Action.ACCELERATOR_KEY, acc);
		contextMenu.getActionMap().put(acc, action);
		contextMenu.add(showLCB);
		
		action = new AbstractAction("Run") {

			public void actionPerformed(ActionEvent e) {
				running = !running;
				if (running) {
					timer.start();
					theWorld.setPickable(false);
				}
				else {
					timer.stop();
					theWorld.setPickable(pickable);
				}
				runMI.setText(running ? "pause" : "run");
			}
			
		};
		runMI = new JMenuItem(action);
		runMI.setSelected(showGenerators);
		acc = KeyStroke.getKeyStroke(KeyEvent.VK_3, 0);
		runMI.setAccelerator(acc);
		runMI.setAction(action);
		
		action.putValue(Action.SHORT_DESCRIPTION, "Toggle animate");
		action.putValue(Action.ACCELERATOR_KEY, acc);
		contextMenu.getActionMap().put(acc, action);
		contextMenu.add(runMI);
		
		action = new AbstractAction("Reset") {

			public void actionPerformed(ActionEvent e) {
				currentPlugin.reset();
				viewer.renderAsync();
			}
			
		};
		resetMI = new JMenuItem(action);
		acc = KeyStroke.getKeyStroke(KeyEvent.VK_4, 0);
		resetMI.setAccelerator(acc);
		resetMI.setAction(action);
		
		action.putValue(Action.SHORT_DESCRIPTION, "Show generator representations");
		action.putValue(Action.ACCELERATOR_KEY, acc);
		contextMenu.getActionMap().put(acc, action);
		contextMenu.add(resetMI);
//		jmi = new JMenuItem("Deactivate tool");
//		jmi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, 0));
//		testM.add(jmi);
//		jmi.addActionListener( new ActionListener() {
//			public void actionPerformed(ActionEvent e)	{
//				boolean hasTool = currentPlugin.getSceneGraphComponent().getTools().size() != 0;
//				if (hasTool) currentPlugin.getSceneGraphComponent().removeTool(currentPlugin.getTool());
//				else  currentPlugin.getSceneGraphComponent().addTool(currentPlugin.getTool());
//				System.err.println("Tool is active: "+!hasTool);
//				viewer.renderAsync();
//			}
//		});
		contextMenu.addSeparator();
//		theMenuBar.add(contextMenu);
		ViewMenuBar mb = psl.getController().getPlugin(ViewMenuBar.class);
		mb.addMenuItem(getClass(), 200, contextMenu);
//		addContextMenu(((Component)viewer.getViewingComponent()), contextMenu);
	}

	static File lastDir = new File(Secure.getProperty("wallpaperFiles"));

	protected void saveWallpaper() {
		FileSystemView view = FileSystemView.getFileSystemView();
		JFileChooser chooser = new JFileChooser(
				!lastDir.exists() ? view.getHomeDirectory() : lastDir, view);
		if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
			return;
		File files = chooser.getSelectedFile();
		lastDir = chooser.getCurrentDirectory();
		System.err.println("File chosen: " + files.getName());
		discreteGroup.wallpaper.ImportExport io = new discreteGroup.wallpaper.ImportExport();
		io.write(this, files);
	}

	protected void loadWallpaper()	{
		FileSystemView view = FileSystemView.getFileSystemView();
		JFileChooser chooser = new JFileChooser(
				!lastDir.exists() ? view.getHomeDirectory() : lastDir, view);
		if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
			return;
		File files = chooser.getSelectedFile();
		lastDir = chooser.getCurrentDirectory();
		System.err.println("File chosen: " + files.getName());
		discreteGroup.wallpaper.ImportExport io = new discreteGroup.wallpaper.ImportExport();
		Wallpaper wpi = io.read(files);		
		// test to see if pattern is there
		replaceGroup(wpi.theGroup);
		wpi.currentPlugin.wallpaper = wpi;
		wpi.currentPlugin.replaceGroup();
		setCurrentPlugin(wpi.currentPlugin);
	}
	//	protected void addContextMenu(Component c, final JPopupMenu contextMenu) {
////		contextMenu.setLightWeightPopupEnabled(false);
//		
//		c.addMouseListener(new MouseAdapter() {
//
//			public void mousePressed( MouseEvent e ) {
//				handlePopup( e );
//			}
//			public void mouseReleased( MouseEvent e ) {
//				handlePopup( e );
//			}
//			private void handlePopup( MouseEvent e ) {
//				if ( e.isPopupTrigger() ) {
//					contextMenu.show( e.getComponent(), e.getX(), e.getY() );
//				}
//			}
//		});
//		
//		c.addKeyListener(new KeyAdapter(){
//			@Override
//			public void keyReleased(KeyEvent e) {
//				if(e.getKeyCode()<=18) return;  //modifier released
//				System.err.println("Got key code "+e.getKeyCode());
//				Action action = contextMenu.getActionMap().get(KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers()));
//				if (action!=null) {
//					action.actionPerformed(null);
////					updateContextMenu();
//				}
//			}
//		});
//	}
	

	@Override
	public PluginInfo getPluginInfo() {
		return super.getPluginInfo();
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		tessellatedContent = con.getPlugin(TessellatedContent.class);
		tessellatedContent.setClipToCamera(false);
		tessellatedContent.setDoDirichletDomain(false);
		tessellatedContent.setFollowsCamera(false);
		tessellatedContent.setDoDirichletDomain(false);
		tessellatedContent.setGroup(dg, copycat);
		DirectContent dc = c.getPlugin(DirectContent.class);
		dc.setContent(getContent());
	}

	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		c.getProperty(getClass(), "groupName", "*236");
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "groupName", theGroup.getName());
	}

	public static void main(String[] args) {
		v.addBasicUI();
		v.registerPlugin(new DirectContent());
		v.registerPlugin(new ContentTools());
		v.registerPlugin(new ContentLoader());
		v.registerPlugin(new BackgroundColor());
		v.registerPlugin(new AnimationPlugin());
		v.registerPlugin(shrinkPanelPlugin);
		v.registerPlugin(TessellatedContent.class);
		v.registerPlugin(ContentAppearance.class);
		v.startup();
	}
}
