package discreteGroup.wallpaper;

import static de.jreality.shader.CommonAttributes.BACKGROUND_COLOR;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LIGHTING_ENABLED;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
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

import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.event.CameraEvent;
import de.jreality.scene.event.CameraListener;
import de.jreality.scene.pick.Graphics3D;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
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
public class WallpaperPluggedIn extends LoadableScene  {
	private static final long serialVersionUID = 1L;
	protected transient SceneGraphComponent  
		theWorld,						// this is the root of our world, including tessellation
		backgroundTile, 				// contains the scene to be tessellated
		generatorRepresentation, 		// this shows the generators of the wallpaper groups
		theFullMonty,
		currentPluginTile,
		pluginTiles[],
		elSGC,
		theFundamentalTile;							// SGC containing an L-shaped polygon
	protected boolean showGenerators = false, 		// should the generator representations be shown?
		showEl = false,					// should the L-shape be shown?
		running = false,
		pickable = true;
	protected transient DiscreteGroupSceneGraphRepresentation groupSceneGraph = null;	// represents wallpaper group in a scene graph
	protected int maxElements =  10000;				// maximum number of group elements to compute
	protected WallpaperGroup theGroup ;					// the particular wallpaper group
	protected transient double globalTime = 0.0;			// time for the animation
	protected transient Viewer viewer;						// for calling viewer.renderAsync() when we make changes
	protected transient Graphics3D dgContext;				// object for keeping track of transformations in scene graph
	protected transient SceneGraphPath pathToWallpaperGroupCOB;	// the path to change of basis node in the scene graph repn of wallpaper group
	protected transient DiscreteGroupViewportConstraint viewportConstraint;
	protected transient Timer timer = null, camTimer = null;
	transient long time;
	transient IndexedFaceSet defaultFundamentalRegion;
	AbstractWallpaperPlugin currentPlugin, plugins[];
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
	
	public IndexedFaceSet getDefaultFundamentalRegion() {
		return defaultFundamentalRegion;
	}

	public void setDefaultFundamentalRegion(IndexedFaceSet defaultFundamentalRegion) {
		this.defaultFundamentalRegion = defaultFundamentalRegion;
	}
	
@Override
	public SceneGraphComponent makeWorld()	{
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

		System.err.println("Replacing group: "+theGroup.getName());
		SceneGraphUtility.removeChildren(theWorld);
		if (groupSceneGraph != null) groupSceneGraph.dispose();
		SceneGraphPath sgp = new SceneGraphPath(viewer.getSceneRoot());
		SelectionManagerImpl.selectionManagerForViewer(viewer).setSelectionPath(sgp);
		// get a new scene graph representation for the group
		groupSceneGraph = new DiscreteGroupSceneGraphRepresentation(theGroup, false);  //true);
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
		}
		theFundamentalTile.setVisible(showGenerators);
		MatrixBuilder.euclidean().translate(0, 0, .01).assignTo(theFundamentalTile);
		fundTileCOB.setTransformation(
				new Transformation(theGroup.getChangeOfBasis().getArray()));
		fundTileCOB.addChild(theFundamentalTile);
//		backgroundTile.setGeometry(defaultFundamentalRegion);
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
	
	private void setCurrentPlugin(final AbstractWallpaperPlugin plugin) {
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

	@Override
	public void customize(JMenuBar theMenuBar, final PluginSceneLoader psl)	{
		viewer = psl.getViewer();
		// continue with tasks which depend on knowing the viewer
		viewer.getSceneRoot().getAppearance().setAttribute(BACKGROUND_COLOR, Color.black);
		double distanceToScreen = 6.0;		// distance to screen (scales tessellation)
		MatrixBuilder.euclidean().translate(0,0,distanceToScreen).assignTo(CameraUtility.getCameraNode(viewer));
		CameraUtility.getCamera(viewer).setFocus(distanceToScreen);
		CameraUtility.getCamera(viewer).addCameraListener(new CameraListener() {

			public void cameraChanged(CameraEvent ev) {
				//updateGroupElements();
				cameraHasChanged = true;
			}
			
		});
		((Component) viewer.getViewingComponent()).addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				//updateGroupElements();
				cameraHasChanged = true;
			}
			
		});

		groupName = "*236";
		replaceGroup(groupName);
//		discreteGroup.wallpaper.ImportExport io = new discreteGroup.wallpaper.ImportExport();
//		io.write(this, new File("/tmp/testWPArchive.xml"));
		plugins = new AbstractWallpaperPlugin[5];
		plugins[0] = new PaintPlugin(this);
		plugins[1] = new TexturePlugin(this);
		plugins[2] = new DirichletDomainPlugin(this);
		plugins[3] = new AffinePlugin(this);
		plugins[4] = new AbstractWallpaperPlugin(this) {

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
		
		
		JMenuItem jmi = new JMenuItem("Save ...");
		jmi.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent actionevent) {
				saveWallpaper();
			}
		});
		contextMenu.add(jmi);

		jmi = new JMenuItem("Load ...");
		jmi.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent actionevent) {
				loadWallpaper();
			}
		});
		contextMenu.add(jmi);
		contextMenu.addSeparator();

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
		ViewMenuBar mb = psl.getController().getPlugin(ViewMenuBar.class);
		mb.addMenuItem(getClass(), 200, contextMenu);
//		addContextMenu(((Component)viewer.getViewingComponent()), contextMenu);
	}

	static File lastDir = new File(Secure.getProperty("wallpaperFiles", Secure.getProperty("user.home")+"/Documents/Models/symmetry"));

	protected void saveWallpaper() {
		FileSystemView view = FileSystemView.getFileSystemView();
		JFileChooser chooser = new JFileChooser(
				lastDir == null || !lastDir.exists() ? view.getHomeDirectory() : lastDir, view);
		if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
			return;
		File files = chooser.getSelectedFile();
		lastDir = chooser.getCurrentDirectory();
		System.err.println("File chosen: " + files.getName());
		ImportExport io = new ImportExport();
		SceneGraphComponent sgc = new SceneGraphComponent("test");
		io.setSceneGraph(sgc);
		io.setWallpaperPluggedIn(this);
		io.write( files);
	}

	protected void loadWallpaper()	{
		FileSystemView view = FileSystemView.getFileSystemView();
		JFileChooser chooser = new JFileChooser(
				lastDir == null || !lastDir.exists() ? view.getHomeDirectory() : lastDir, view);
		if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
			return;
		File files = chooser.getSelectedFile();
		lastDir = chooser.getCurrentDirectory();
		System.err.println("File chosen: " + files.getName());
		ImportExport io = new ImportExport();
		io.read(files);	
		WallpaperPluggedIn wpi = io.getWallpaperPluggedIn();
		SceneGraphComponent sgc = io.getSceneGraph();
		System.err.println("read sgc named "+sgc.getName());
		replaceGroup(wpi.theGroup.getName());
		if (wpi.currentPlugin instanceof PaintPlugin && currentPlugin instanceof PaintPlugin) {
			currentPlugin.reset();
			PaintPlugin pp1 = (PaintPlugin) wpi.currentPlugin,
				pp2 = (PaintPlugin) currentPlugin;
			double[] permmat = Rn.permutationMatrix(null, new int[]{2,3,0,1});
			ImageData id = new ImageData(pp1.getTextureImage(), permmat);
			pp2.setTextureImage((BufferedImage) id.getImage());
		}
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
	public boolean hasInspector() {
		return true;
	}
	public Component getInspector() {
		return getInspector(null);
	}
	
	public Component getInspector(Viewer v) {	
		if (inspector == null) {
//			inspector = Box.createVerticalBox();
//			inspector.setName("Wallpaper plugin");
//			inspector.add(pluginPanel);
			inspector = pluginPanel;
			//inspector.add(Box.createVerticalGlue());
		}
		return inspector;
	}
	
	/**
	 * Create a simple documentation panel for the application.
	 */
	protected Component getReadme()	{
		JPanel panel = new JPanel();
		panel.setName("ReadMe");
		JTextArea textarea = new JTextArea(10,20);
		textarea.setEditable(false);
		textarea.append("Welcome to eucsym ('yooksim')"+
				"a program for exploring the two-dimensional\n"+
				"euclidean symmetry groups, aka\n"+
				"wallpaper groups.\n\n"+
				"This program is a reincarnation of \n"+
				"a program written in 1981-1983 at the\n"+
				"UNC-CH as my Masters project\n\n" +
				"Keyboard controls:\n"+
//				"    '1':    toggle terrain texture\n"+
				"    'h':    toggle display of help overlay for viewer\n"+
				"    'i':    toggle display of performance overlay\n\n"+
				"Use mouse click wheel to zoom in and out.\n"+
				"Shift-cntl-f  toggles fullscreen mode.\n"+
				"Click on the tab 'Scene Graph' to explore structure\n"+
				"\nAuthor: Charles Gunn\n"+
				"    gunn at math.tu-berlin.de\n");
		panel.add(textarea);
		return panel;
	}
	public static void main(String[] args) {
    	String cp = ((String)System.getProperty("java.class.path")).replace(':', '\n'); //split(":");
    	System.err.println("cp = "+cp);

//		ViewerAppLoader val = TestViewerApp.makeViewerAppLoader();
		PluginSceneLoader psl = new PluginSceneLoader();
		WallpaperPluggedIn ls = new WallpaperPluggedIn();
		psl.loadScene(ls);
//		ViewerApp va = val.viewerAp;
		JRViewer jrv = psl.getJRViewer();

		Component vc = (Component) ((ViewerSwitch) jrv.getViewer()).getCurrentViewer().getViewingComponent();
		vc.setPreferredSize(new Dimension(800,600));
		vc.setMaximumSize(new Dimension(800,600));
		vc.setSize(new Dimension(800,600));
//		va.setAttachNavigator(true);
//		va.setExternalNavigator(true);
//		va.setAttachBeanShell(false);
//		va.setExternalBeanShell(false);
//		Component insp = ls.getInspector();
//		va.addAccessory(insp);
//		insp = ls.getReadme();
//		va.addAccessory(insp);
//		va.setFirstAccessory(insp);
//		va.update();
//		JFrame frame = va.display();
//		frame.setTitle("Wallpaper Groups");

	}

}
