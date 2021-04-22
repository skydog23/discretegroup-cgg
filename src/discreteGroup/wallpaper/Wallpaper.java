package discreteGroup.wallpaper;

import static de.jreality.shader.CommonAttributes.BACKGROUND_COLOR;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LIGHTING_ENABLED;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.LINE_WIDTH;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.SPHERES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.iharder.dnd.FileDrop;
import charlesgunn.jreality.newtools.TexturePlacementTool;
import charlesgunn.jreality.texture.SimpleTextureFactory;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.event.CameraEvent;
import de.jreality.scene.event.CameraListener;
import de.jreality.scene.pick.Graphics3D;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.ui.viewerapp.SelectionManagerImpl;
import de.jreality.util.CameraUtility;
import de.jreality.util.DefaultMatrixSupport;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.Secure;
import de.jtem.discretegroup.core.DirichletDomain;
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
 *  investigate mysterious slowness (it's not memory!)
 *  single key control to toggle display of single tile (for pedagogical reasons)
  *  implement general cyclic and dihedral groups
 *  consider toggle on color factory to maintain a minimum saturation level (avoid black)
 *  make ShadedSphereImage into a factory (to avoid over-using static variables)
 *  is it possible to display the PaintSource inspector in full screen mode? (like iPhoto)
 *  icons for the groups instead of names (make a separate tab to select group)
 *  rescue discreteGroup.demo.WallpaperDemo and adapt the tools there
 *  	(affine COC is in Assignment06 in mathvis06)
 *  convert from single brush to list of brushes
 *  optimize texture copies to copy only changed regions (compare original code sample)
 *  more brushes: pencil brush, and polyline brush
 *  
 * @author Charles Gunn
 *
 */
public class Wallpaper extends LoadableScene implements ChangeListener {
	protected
	SceneGraphComponent  theFullMonty, // contains the scene to be tessellated
	    dirichletDomainSGC,
	    pointSGC,
		generatorRepresentation, 		// this shows the generators of the wallpaper groups
		loadedGeometry, 				// geometry is loaded from files  into this SGC
		theWorld, 						// this is the root of our world, including tessellation
		elSGC;							// SGC containing an L-shaped polygon
	protected boolean showGenerators = true, 		// should the generator representations be shown?
		showEl = false,					// should the L-shape be shown?
		running = false,
		showDirdom = true;
	protected DiscreteGroupSceneGraphRepresentation groupSceneGraph = null;	// represents wallpaper group in a scene graph
	protected int maxElements = 1000;				// maximum number of group elements to compute
	protected WallpaperGroup theGroup ;					// the particular wallpaper group
	protected double globalTime = 0.0;			// time for the animation
	protected double distanceToScreen = 6.0;		// distance to screen (scales tessellation)
	protected Viewer viewer;						// for calling viewer.renderAsync() when we make changes
	protected Graphics3D dgContext;				// object for keeping track of transformations in scene graph
	protected SceneGraphPath pathToWallpaperGroupCOB,	// the path to change of basis node in the scene graph repn of wallpaper group
		pathToFullMonty;
	protected IndexedFaceSet dirichletDomain;
	protected PointSet centerPoint;
	protected DiscreteGroupViewportConstraint viewportConstraint;
	protected Timer timer = null;
	protected Appearance paintedAp, imageAp;
	protected Texture2D paintedTex, imageTex;
	private Image thumbnailImage = null;
	private int thumbnailSize = 100;
	boolean painting = true;
	// where to find the OFF files for our tessellation
	private PaintSource paintSource;
	PositionProvider pfac;
	ColorProvider colorFactory;
	private Matrix texMatrix;
	long time;
	// look to see if the use has set with property from command line: -DwallpaperFiles=...
	protected static String wallpaperFiles = "./resources/";
	static {
		String foo = Secure.getProperty("wallpaperFiles");
		if (foo != null) wallpaperFiles = foo;
	}
	
	Tool dirdomTool = new AbstractTool( InputSlot.getDevice("PrimaryAction"))	{

		{
			  addCurrentSlot( InputSlot.getDevice("PointerTransformation"));
		  }
		@Override
		public void activate(ToolContext tc) {
			dirichletDomainSGC.setGeometry(dirichletDomain);
			dirichletDomainSGC.setVisible(true);
		}

		@Override
		public void deactivate(ToolContext tc) {
		}

		@Override
		public void perform(ToolContext tc) {
			PickResult currentPick = tc.getCurrentPick();
			if (currentPick == null ||
					currentPick.getObjectCoordinates() == null ||
					currentPick.getObjectCoordinates().length < 1) return;
			theGroup.setCenterPoint(currentPick.getObjectCoordinates());
			DirichletDomain dirdom = new DirichletDomain(theGroup);
			dirdom.update();
			dirichletDomain = dirdom.getDirichletDomain(); //DiscreteGroupUtility.calculateDirichletDomain(dirichletDomain, theGroup);
			dirichletDomainSGC.setGeometry(dirichletDomain);		
			pointSGC.setGeometry(Primitives.point(theGroup.getCenterPoint()));
			viewer.renderAsync();
		}
		
	};
	
	@Override
	public SceneGraphComponent makeWorld()	{
		SceneGraphNode.setThreadSafe(false);
		theWorld = SceneGraphUtility.createFullSceneGraphComponent("theWorld");
		theWorld.getAppearance().setAttribute(
				LINE_SHADER+"."+DIFFUSE_COLOR, java.awt.Color.WHITE);
		theWorld.getAppearance().setAttribute(LIGHTING_ENABLED, false);
		theFullMonty = SceneGraphUtility.createFullSceneGraphComponent("theFullMonty");
		paintedAp = theFullMonty.getAppearance();
		paintedAp.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.white);
		paintedAp.setAttribute(EDGE_DRAW, false);
		paintedAp.setAttribute(FACE_DRAW, true);
		Texture2D tex2d = (Texture2D) AttributeEntityUtility
	       .createAttributeEntity(Texture2D.class, "polygonShader.texture2d", paintedAp, true);
		int imageSize = 256;
		BufferedImage bi = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d =  bi.createGraphics();
		g2d.setColor(new Color(1f,1f,1f,1f));
		g2d.fillRect(0, 0, imageSize, imageSize);
		ImageData id = new ImageData(bi);//, flipRB);
		setTextureImage(tex2d, id);
		tex2d.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
		tex2d.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
 		tex2d.setAnimated(true);
 		tex2d.setPixelFormat(Texture2D.GL_BGRA);
 		paintedTex = tex2d;
 		
 		imageAp = new Appearance();
		imageAp.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.white);
		imageAp.setAttribute(EDGE_DRAW, false);
		imageAp.setAttribute(FACE_DRAW, true);
		tex2d = (Texture2D) AttributeEntityUtility
	       .createAttributeEntity(Texture2D.class, "polygonShader.texture2d", imageAp, true);
		
  		try {
  			ImageData id2 = ImageData.load(Input.getInput("http://www.math.tu-berlin.de/~gunn/Pictures/christmasTree.jpg"));
   			SimpleTextureFactory stf = new SimpleTextureFactory();
  			stf.update();
//  			id2 = stf.getImageData();
 			setTextureImage(tex2d, id2);
 		} catch (IOException e) {
  			e.printStackTrace();
  		}
// 		tex2d.setMipmapMode(false);
		tex2d.setRepeatS(Texture2D.GL_REPEAT);
		tex2d.setRepeatT(Texture2D.GL_REPEAT);
		imageTex = tex2d;
		texMatrix = new Matrix();
		MatrixBuilder.euclidean().rotateZ(Math.PI).assignTo(texMatrix);
		imageTex.setTextureMatrix(texMatrix);
		texturePlacementTool = new TexturePlacementTool(imageTex);
//		theFullMonty.addTool(texturePlacementTool);
		theFullMonty.setAppearance(painting ? paintedAp : imageAp);
//		theFullMonty.addTool(dirdomTool);
		dirichletDomainSGC = SceneGraphUtility.createFullSceneGraphComponent("dirdom");
		Appearance ap = dirichletDomainSGC.getAppearance();
		ap.setAttribute(EDGE_DRAW, true);
		ap.setAttribute(FACE_DRAW, false);
		ap.setAttribute(LINE_SHADER+"."+TUBES_DRAW, false);
		ap.setAttribute(LINE_SHADER+"."+DIFFUSE_COLOR, Color.red);
		ap.setAttribute(LINE_SHADER+"."+LINE_WIDTH, 1.5);
		MatrixBuilder.euclidean().translate(0,0,.01).assignTo(dirichletDomainSGC);
		pointSGC = SceneGraphUtility.createFullSceneGraphComponent("centerPoint");
		ap = pointSGC.getAppearance();
		ap.setAttribute(VERTEX_DRAW, true);
		ap.setAttribute(EDGE_DRAW, false);
		ap.setAttribute(FACE_DRAW, false);
		ap.setAttribute(POINT_SHADER+"."+SPHERES_DRAW, true);
		ap.setAttribute(POINT_SHADER+"."+DIFFUSE_COLOR, new Color(255,255,200));
		ap.setAttribute(POINT_SHADER+"."+POINT_RADIUS, .03);
		dirichletDomainSGC.addChild(pointSGC);
//		theFullMonty.addChild(dirichletDomainSGC);
// 		tex2d.setMipmapMode(false);
// 		tex2d.setMagFilter(Texture2D.GL_NEAREST);
// 		tex2d.setMinFilter(Texture2D.GL_NEAREST);
 		// following doesn't work if I use the original buffered image bi
		paintSource = new PaintSource((BufferedImage) id.getImage());
//		System.err.println("BI "+paintSource.getBufferedImage().toString());
		paintSource.addChangeListener(this);
		theFullMonty.addTool(paintSource.getTool());
		
		colorFactory = paintSource.getColorFactory();
		elSGC = DiscreteGroupUtility.getElKit();
		MatrixBuilder.euclidean(elSGC.getTransformation()).scale(2).assignTo(elSGC);
		elSGC.getAppearance().setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, new Color(200, 0, 200));
		theFullMonty.addChild(elSGC);
		elSGC.setVisible(showEl);
		running = false;
		timer = new Timer(20, new ActionListener() {
			double rot = 0.0, x = 0, y = 0;
			public void actionPerformed(ActionEvent arg0) {
				long newtime = System.currentTimeMillis();
				int diff = (int) (newtime - time);
				// frac will be one when we are doing 30 fps
				double frac = 30*diff/1000.0;
//				System.err.println(diff+" ms");
				if (time == 0) { 				time = newtime; return; }
				time = newtime;
				if (painting) paintSource.update();
				else {					double[] parms = new double[3];
					pfac = paintSource.getPositionFactory();
					colorFactory = paintSource.getColorFactory();
					pfac.update();
					colorFactory.update();
					Color color = colorFactory.getColor();
					rot += frac*(color.getRed() + color.getGreen() -255)/10000f;
					x += frac*color.getGreen()/100000f;
					y +=  frac*color.getBlue()/100000f;
//					x = x%1.0;
//					y = y %1.0;
//					System.err.println("rot, x, y="+rot+":"+x+":"+y);
					double[] icenter = Rn.times(null, -1, paintSource.center);		
					MatrixBuilder.euclidean().translate(x,y,0).translate(paintSource.center).rotateZ(rot).translate(icenter).assignTo(texMatrix.getArray());
					viewer.renderAsync();
				}
			}
			
		});
		return theWorld;
	}
	private void setTextureImage(Texture2D tex2d, ImageData id2) {
		tex2d.setImage(id2);
		thumbnailImage = getScaledImage(id2.getImage(), thumbnailSize, (thumbnailSize*id2.getHeight())/id2.getWidth());
	}
	
	public void stateChanged(ChangeEvent e) {
		if (viewer == null) return;
		viewer.renderAsync();
	}

	@Override
	public void customize(JMenuBar theMenuBar, final Viewer v)	{
		viewer = v;
		texturePlacementTool.setViewer(viewer);
		// continue with tasks which depend on knowing the viewer
		viewer.getSceneRoot().getAppearance().setAttribute(BACKGROUND_COLOR, Color.black);
		MatrixBuilder.euclidean().translate(0,0,distanceToScreen).assignTo(CameraUtility.getCameraNode(viewer));
		CameraUtility.getCamera(viewer).setFocus(distanceToScreen);
		CameraUtility.getCamera(viewer).addCameraListener(new CameraListener() {

			public void cameraChanged(CameraEvent ev) {
				updateGroupElements();
			}
			
		});
		((Component) viewer.getViewingComponent()).addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				updateGroupElements();
			}
			
		});
		replaceGroup("*X");
		// list all the groups in the menu
		JMenu testM = new JMenu("Group");
		ButtonGroup bg = new ButtonGroup();
		final String[] gnames = WallpaperGroup.names;
		for (int i = 0; i<gnames.length; ++i)	{
			final int j = i;
			JMenuItem jm = testM.add(new JRadioButtonMenuItem(gnames[i]));
			jm.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)	{
					replaceGroup(gnames[j]);
				}
			});
			bg.add(jm);
		}
		theMenuBar.add(testM);
		// construct three menu buttons for special actions
		testM = new JMenu("Actions");
		JCheckBoxMenuItem jcm = new JCheckBoxMenuItem("Show Generators");
		jcm.setSelected(showGenerators);
		jcm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0));
		testM.add(jcm);
		jcm.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showGenerators = ((JCheckBoxMenuItem) e.getSource()).isSelected();
				theFundamentalTile.setVisible(showGenerators);
				viewer.renderAsync();
			}

		});
		jcm = new JCheckBoxMenuItem("Show L");
		jcm.setSelected(showEl);
		jcm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0));
		testM.add(jcm);
		jcm.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showEl = ((JCheckBoxMenuItem) e.getSource()).isSelected();
				System.out.println("ShowEl is "+showEl);
				elSGC.setVisible(showEl);
				viewer.renderAsync();
			}
		});
		jcm = new JCheckBoxMenuItem("Toggle run");
		jcm.setSelected(running);
		jcm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0));
		testM.add(jcm);
		jcm.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				running = ((JCheckBoxMenuItem) e.getSource()).isSelected();
				if (running) timer.start();
				else timer.stop();

//				paintSource.setAutomate(running);
				viewer.renderAsync();
			}
		});
		JMenuItem jmi = new JMenuItem("Reset");
		jmi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, 0));
		testM.add(jmi);
		jmi.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				paintSource.reset();
				viewer.renderAsync();
			}
		});
		jmi = new JMenuItem("Toggle textures");
		jmi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, 0));
		testM.add(jmi);
		jmi.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				painting = !painting;
				theFullMonty.setAppearance(
						painting ? paintedAp : imageAp);
				viewer.renderAsync();
			}
		});
//		final JMenuItem jcy = new JMenuItem("Load OFF file ...");
//		jcy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, 0));
//		testM.add(jcy);
//		jcy.addActionListener( new ActionListener() {
//			public void actionPerformed(ActionEvent e)	{
//				loadFile();
//				viewer.renderAsync();
//			}
//
//		});
		theMenuBar.add(testM);
	}

	JScrollPane sp = null;
	private SceneGraphComponent theFundamentalTile;
	private TexturePlacementTool texturePlacementTool;
	private JButton filedropButton;
	public Component getInspector() {	
		return paintSource.getInspector();
	}
	
	@Override
	public boolean hasHelpset() {
		return false;
	}

	@Override
	public String getHelpSet() {
		return null; //"Assignment05Help.html";
	}
	/* 
	 * Beyond here are methods which are not part of the LoadableSceneInterface
	 */
	/**
	 * Generate a new group and update the scene graph accordingly
	 * @param	name	The name of the group in Conway notation.
	 */
	public void replaceGroup(String name)	{
		timer.stop();
		theGroup = WallpaperGroup.instanceOfGroup(name);
//		if (groupSceneGraph != null)	{
//			if (theWorld.isDirectAncestor(groupSceneGraph.getRepresentationRoot()))
				//theWorld.removeChild(groupSceneGraph.getRepresentationRoot());
				SceneGraphUtility.removeChildren(theWorld);
				if (groupSceneGraph != null) groupSceneGraph.dispose();
//		}
		// reset selection path to be empty
		SceneGraphPath sgp = new SceneGraphPath(viewer.getSceneRoot());
		SelectionManagerImpl.selectionManagerForViewer(viewer).setSelectionPath(sgp);
		// get a new scene graph representation for the group
		groupSceneGraph = new DiscreteGroupSceneGraphRepresentation(theGroup);
		// get a scene graph representing the generators of the group and add it to the root of the representation
		generatorRepresentation = theGroup.getGeneratorRepresentations();
		generatorRepresentation.getAppearance().setAttribute(FACE_DRAW, true);
		generatorRepresentation.getAppearance().setAttribute(
				LINE_SHADER+"."+DIFFUSE_COLOR, java.awt.Color.BLACK);
		IndexedFaceSet defaultFundamentalRegion = (IndexedFaceSet) theGroup.getDefaultFundamentalRegion();
		theFundamentalTile = SceneGraphUtility.createFullSceneGraphComponent("single tile");
		theFundamentalTile.setGeometry(defaultFundamentalRegion);
		theFundamentalTile.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.blue);
		theFundamentalTile.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, .35);
		theFundamentalTile.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		theFundamentalTile.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		theFundamentalTile.addChild(generatorRepresentation);
		theFundamentalTile.setVisible(showGenerators);
		MatrixBuilder.euclidean().translate(0, 0, .01).assignTo(generatorRepresentation);
		DirichletDomain dirdom = new DirichletDomain(theGroup);
		dirdom.update();
		dirichletDomain = dirdom.getDirichletDomain(); //DiscreteGroupUtility.calculateDirichletDomain(dirichletDomain, theGroup);
		dirichletDomainSGC.setGeometry(dirichletDomain);
		theFullMonty.setGeometry(defaultFundamentalRegion);
		// Attach the geometry to be tessellated to the representation
		groupSceneGraph.setWorldNode(theFullMonty);
		// update and add the resulting scene graph to the world node
		groupSceneGraph.update();
		SceneGraphComponent sgn =  groupSceneGraph.getRepresentationRoot();
//		SceneGraphUtility.removeChildren(theWorld);
		theWorld.addChild(sgn);
		theWorld.addChild(theFundamentalTile);
		// set up a special constraint based on the viewport to prune the set of group elements so that
		// only those elements are generated which are visible
		List l = SceneGraphUtility.getPathsBetween(viewer.getSceneRoot(), sgn);
		pathToWallpaperGroupCOB = (SceneGraphPath) l.get(0);
		if (pathToWallpaperGroupCOB != null) pathToWallpaperGroupCOB.push(groupSceneGraph.getChangeOfBasisNode());
		dgContext = new Graphics3D(viewer.getCameraPath(), pathToWallpaperGroupCOB, 
				CameraUtility.getAspectRatio(viewer));
		viewportConstraint = new DiscreteGroupViewportConstraint( 0d, 0, -1.0, -1, dgContext);
		viewportConstraint.setMaxNumberElements(maxElements);
		viewportConstraint.setFudge(1.3);
		groupSceneGraph.setElementList(DiscreteGroupUtility.generateElements(theGroup,viewportConstraint));
		System.err.println("Group has "+groupSceneGraph.getElementList().length+" elements");
		groupSceneGraph.update();
		l = SceneGraphUtility.getPathsBetween(viewer.getSceneRoot(), theFullMonty);
		pathToFullMonty = (SceneGraphPath) l.get(0);
		paintSource.setPolygon(defaultFundamentalRegion);
		paintSource.repaint();
		if (running) timer.start();
//		viewer.renderAsync();
	}

	/**
	 * Load a geometry file into the scene graph so it is tessellated by the group.
	 * Put it into the SGC theFullMonty, which contains geometry which should be tessellated.
	 */
	protected void loadFile() {
		JFileChooser fc = new JFileChooser(wallpaperFiles);
		//System.out.println("FCI resource dir is: "+resourceDir);
		int result = fc.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION)	{
			File file = fc.getSelectedFile();
			SceneGraphComponent readSGC = null;
			try {
				readSGC = Readers.read(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (readSGC == null) return;
			IndexedFaceSetUtility.calculateFaceNormals(readSGC);
			IndexedFaceSetUtility.calculateVertexNormals(readSGC);
			DefaultMatrixSupport.getSharedInstance().storeDefaultMatrices(readSGC);
			if (loadedGeometry != null) theFullMonty.removeChild(loadedGeometry);
			loadedGeometry = readSGC;
			theFullMonty.addChild(loadedGeometry);
			wallpaperFiles = file.getAbsolutePath();
		} else {
			System.out.println("Unable to open file");
			return;
		}
	}

    Image getScaledImage(Image srcImg, int w, int h){
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();
        return resizedImg;
    }
	private Component getTextureInspector()	{
		JPanel insp = new JPanel();
		JLabel label = new JLabel("Drag and drop image file on this button");
		insp.add(label);
		
		ImageIcon icon = new ImageIcon(thumbnailImage);
		filedropButton = new JButton(icon);
//		filedropButton.setPreferredSize(new Dimension(50,50));
//		filedropButton.setBackground(Color.red);
		insp.add(filedropButton);
		FileDrop fileDrop = new FileDrop(filedropButton, new FileDrop.Listener() {

			public void filesDropped(File[] arg0) {
				if (arg0.length == 0) return;
	 			try {
					ImageData id2 = ImageData.load(Input.getInput(arg0[0]));
					setTextureImage(imageTex,id2);
					filedropButton.setIcon(new ImageIcon(thumbnailImage));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.err.println("File dropped"+arg0[0].getName());
			}
			
		});
		insp.setName("Parameters");
		return insp;
	}
	private void updateGroupElements() {
		timer.stop();
		double aspectRatio = CameraUtility.getAspectRatio(viewer);
		dgContext.setAspectRatio(aspectRatio);
		System.err.println("Aspect ratio = "+aspectRatio);
		viewportConstraint.update();
		groupSceneGraph.setElementList(DiscreteGroupUtility.generateElements(theGroup,viewportConstraint));
		groupSceneGraph.update();
		System.err.println("Setting dg el list");
		if (running) timer.start();
	}
	
}
