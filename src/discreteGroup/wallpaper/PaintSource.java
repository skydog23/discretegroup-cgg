package discreteGroup.wallpaper;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.jreality.texture.SimpleTextureFactory;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.Rectangle3D;
import de.jtem.discretegroup.groups.WallpaperGroup;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;

public class PaintSource implements Serializable {
	private static final long serialVersionUID = 1L;
	int width = 256, height = 256;
	BufferedImage masterBufferedImage, brush, overlay;
	WallpaperGroup theGroup;
	double[][] polygon = {{0,0,1},{1,0,1},{1,1,1},{0,1,1}};
	double[] center = {0,0,1}, speed = {Math.random(), Math.random(), 0};
	Dimension size = new Dimension(width, height);
	int mousex = 0,mousey=0, prevMousex, prevMousey, oldMousex, oldMousey;
	double[] mouse = new double[2], nmouse = new double[2];
	double  dt = .01; 
	long time;
	Color currentColor = new Color(150,0,150,255), currentRGBColor;
	double alpha = .5,
		radius = .045,
		whiteBorder = 0.0,
		minimumLength2 = 0;
	boolean automate = false, 
		reset = false, 
		shadedSphere = true, 
		mouseMoved = false;
	BilliardTable billiardTable;
	AbstractPositionProvider positionProvider;
	ColorProvider colorFactory;
	AffineTransform flipY = new AffineTransform(), 
		group2Canvas = new AffineTransform(), 
		canvas2Group;
	SimpleTextureFactory brushFactory;
	enum PaintType  {BRUSH, PENCIL, POLYLINE, SNAKE};
	PaintType paintType = PaintType.BRUSH;
	SnakeGuy snakeguy = new SnakeGuy();
	Color backgroundColor  = new Color(1f,1f,1f,1f);
	LinkedList<BufferedImage> history = new LinkedList<BufferedImage>();
	double[] outline, outlineC;
	int currentState = 0;
	transient ShrinkPanel panel = null;
	transient AffineTransform[] groupGens;
	transient private JCheckBox autoCheckBox;
	transient JPanel jpanel = new JPanel();
	transient JComponent jc;
	transient Graphics biGraphics;
	transient PaintPlugin paintPlugin;
	 transient List<ChangeListener> listeners = new ArrayList<ChangeListener>();
		protected BufferedImage textureImage;
		transient Appearance paintedAp;
		transient protected Texture2D tex2d;  	
	
	public PaintSource(PaintPlugin pp)	{
		paintPlugin = pp;
//		setMasterBufferedImage(pp.getTextureImage());
		int imageSize = 256;
		textureImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d =  textureImage.createGraphics();
		g2d.setColor(new Color(1f,1f,1f,1f));
		g2d.fillRect(0, 0, textureImage.getWidth(), textureImage.getHeight());
		ImageData id = initTexture();
  		textureImage = (BufferedImage) id.getImage();
  		setMasterBufferedImage(textureImage);
  		
		positionProvider = new FeedbackPositionProvider(); //
		positionProvider = new CirclingPositionProvider(); //
		positionProvider = new CurlyQueuePositionProvider();
		initComponents();
	}
	
	private ImageData initTexture() {
//		tex2d = (Texture2D) AttributeEntityUtility
//	       .createAttributeEntity(Texture2D.class, "polygonShader.texture2d", paintedAp, true);
		paintedAp = paintPlugin.getSceneGraphComponent().getAppearance();
		paintedAp.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.white);
		paintedAp.setAttribute(FACE_DRAW, true);
		ImageData id = new ImageData(textureImage);//, flipRB);
		tex2d = TextureUtility.createTexture(paintedAp,"polygonShader", id);
		tex2d.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
		tex2d.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
 		tex2d.setAnimated(true);
 		// not a good idea to set false: no picture shows up!
 		tex2d.setMipmapMode(true);
 		tex2d.setApplyMode(Texture2D.GL_MODULATE);
 		// this is needed to match the format used by java2d
 		tex2d.setPixelFormat(Texture2D.GL_BGRA);
		return id;
	}
	public void setPositionFactory(PositionProvider pf)	{
		if (positionProvider != null) positionProvider.setWallpaperGroup(null);
		positionProvider =  (AbstractPositionProvider) pf;
		positionProvider.setWallpaperGroup(theGroup);
		positionProvider.setTile(paintPlugin.singleTile);
		billiardTable = new BilliardTable(positionProvider, polygon, mats33s);
		positionPanel.removeAll();
		positionPanel.add(positionProvider.getInspector());
	}
	
	public PositionProvider getPositionFactory() {
		return positionProvider;
	}
	
	public void setColorFactory(ColorProvider cf)	{
		colorFactory = cf;
		colorPanel.removeAll();
		colorPanel.add(colorFactory.getInspector());
	}
	
	public ColorProvider getColorFactory()	{
		return colorFactory;
	}
	
	void pushState() {
		// first remove the undone states from the history list; they are really history now!
		int n = history.size()-1;
		for (int i = n; i > currentState; --i)	{
			history.remove(i);
		}
		BufferedImage bi = new BufferedImage(masterBufferedImage.getWidth(), 
				masterBufferedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d =  bi.createGraphics();
		g2d.drawImage(masterBufferedImage, 0, 0, null);
		history.addLast(bi);
		currentState = history.size()-1;
	}

	public void undo()	{
		if (currentState <= 0) {
			System.err.println("Nothing to undo");
			return;
		}
		currentState = currentState - 1;
		setCurrentImage();
		System.err.println("in undo: "+currentState);
		repaint();
	}

	public void redo()	{
		if (currentState >= (history.size()-1))	{
			System.err.println("Nothing to redo");
			return;
		}
		currentState++;
		setCurrentImage();
		System.err.println("in redo: "+currentState);
		repaint();
	}
	
	private void setCurrentImage() {
		Graphics2D bg2 = (Graphics2D) biGraphics;
		Composite oldC = bg2.getComposite();
		bg2.setComposite(AlphaComposite.Src);
		BufferedImage bi = history.get(currentState);
		bg2.drawImage(bi, 0, 0, null);
		bg2.setComposite(oldC);
	}

	protected void repaint() {
//		biGraphics = masterBufferedImage.createGraphics();
		paintImage(biGraphics);
		jc.repaint();
		fireChange();
	}
	
	// draw into the buffered image
	public void paintImage(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		if (reset)	{
			g2.setBackground(backgroundColor);
			g2.clearRect(0, 0, masterBufferedImage.getWidth(), masterBufferedImage.getHeight());
			reset = false;
			pushState();
			System.err.println("Resetting image");
			return;
		}
		if (groupGens == null || !mouseMoved)	return;
		int w = brush.getWidth()/2;
		int h = brush.getHeight()/2;
		mouse[0] = mousex-w/2;
		mouse[1] = mousey-h/2;
		canvas2Group.transform(mouse, 0, nmouse, 0, 1);
		//			System.err.println("mouse in group coords: "+nmouse[0]+" "+nmouse[1]);
		double dist2 = (mousex-prevMousex)*(mousex-prevMousex) + (mousey-prevMousey)*(mousey-prevMousey);
		//				System.err.println("dist2 = "+dist2);
		float scale = (float) group2Canvas.getScaleX();
		AffineTransform old = g2.getTransform();
		Graphics2D overlayG = (Graphics2D) overlay.getGraphics();
		if (paintType == PaintType.POLYLINE || paintType == paintType.SNAKE) {
			overlayG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
			overlayG.drawImage(history.get(currentState), 0,0, null);
			overlayG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			overlayG.setColor(currentRGBColor);
			overlayG.setStroke(new BasicStroke((float) radius * scale /2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			//					overlayG.transform(af);
			overlayG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
			if (paintType == paintType.SNAKE) snakeguy.draw(overlayG);					
		}
		for (AffineTransform af : groupGens) {
			g2.setTransform(af);
			overlayG.setTransform(af);
			switch(paintType)	{
				case BRUSH:
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
					g2.drawImage(brush.getScaledInstance(
							w,h, BufferedImage.SCALE_SMOOTH), 
							mousex-w/2, mousey-h/2, null);						
					break;
				case PENCIL:
					if (dist2 > minimumLength2){
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
						g2.setColor(currentRGBColor);
						g2.setStroke(new BasicStroke((float) radius * scale /2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
						g2.drawLine(mousex, mousey, prevMousex, prevMousey);															
					}
					break;
				case POLYLINE:
					overlayG.drawLine(oldMousex, oldMousey, mousex, mousey);															
					break;
				case SNAKE:
					overlayG.translate(mousex, mousey);
					snakeguy.draw(overlayG);
					overlayG.translate(-mousex, -mousey);	// WOW no push/pop for transforms!
					break;
			}
			mouseMoved = false;
			g2.setTransform(old);
			if (paintType == PaintType.POLYLINE || paintType == paintType.SNAKE)	{
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
				g2.drawImage(overlay, 0, 0, null);
			}
		}
	}


	void setMouseMoved() {
		prevMousex = mousex;
		prevMousey = mousey;
		mouseMoved = true;
	}
	
	public void initComponents() {
		setColor(currentColor);
		colorFactory = new SinusoidColorProvider();
		colorFactory.setAlpha(alpha);
		colorFactory.update();
		brushFactory = new SimpleTextureFactory();
		brushFactory.setType(SimpleTextureFactory.TextureType.SPHERE);
		brushFactory.setChannels(new int[]{1,0,3,2});
		updateBrush();
		center = Rn.average(center, polygon);
		positionProvider.setPosition(center); // = new PositionFactory(center, speed);
		billiardTable = new BilliardTable(positionProvider, polygon);
		jpanel.setLayout(new BorderLayout());
//		frame.getContentPane().add(jpanel);
		
//		updateBI();
		jc = new PaintingComponent(this);
		jc.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent arg0) {
				System.err.println("key pressed");
			}

			public void keyReleased(KeyEvent arg0) {
				System.err.println("key released");				
			}

			public void keyTyped(KeyEvent arg0) {
				System.err.println("key typed");				
			}
			
		});
		jc.addMouseMotionListener(new MouseMotionAdapter() {
			
			public void mouseDragged(MouseEvent arg0) {
//				System.err.println("mouse dragged:"+arg0.getX()+":"+arg0.getY());
				setMouseMoved();
				mousex = arg0.getX();
				mousey = height- 1 -arg0.getY();
				repaint();
			}

		});

		jc.addMouseListener(new MouseAdapter() {

			public void mousePressed(MouseEvent arg0) {
				System.err.println("mouse pressed");
				oldMousex = mousex = arg0.getX();
				oldMousey = mousey = height- 1 -arg0.getY();
				System.err.println("old x, y: "+oldMousex+" "+oldMousey);
				setMouseMoved();
//				mouseMoved = false;
				repaint();
			}

			public void mouseReleased(MouseEvent arg0) {
				System.err.println("mouse released");
//				System.err.println("x, prev x = "+mousex+" "+prevMousex);
				mouseMoved = true;
				pushState();
//				repaint();
			}
			
		});
		jc.addComponentListener(new ComponentAdapter()  {

			public void componentResized(ComponentEvent e) {
				width=e.getComponent().getWidth();
				height = e.getComponent().getHeight();
				System.err.println("w:h = "+width+":"+height);
				int max = Math.max(width, height);
				dt = 2.0/max;
//				positionProvider.setGlobalSpeed(globalSpeed = 200.0/max);
				flipY.setToIdentity();
				flipY.translate(0, height/2);
				flipY.scale(1, -1);
				flipY.translate(0, -height/2);
			}

			
		});
		jc.setSize(new Dimension(width, height));
		jc.setPreferredSize(new Dimension(width, height));
		jc.setMaximumSize(new Dimension(width, height));
		jpanel.add(jc);
//		if (automate) timer.start();

	}
	
	public Component getComponent()	{
		return jc; 
	}
	
	transient Tool paintTool = new PaintTool(this, InputSlot.getDevice("PrimaryAction"));
	protected Tool getTool()	{
		return paintTool;
	}
	

	public void update() {
		billiardTable.update();
		colorFactory.update();
		currentColor = colorFactory.getRGBColor();
		updateBrush();
		group2Canvas.transform(positionProvider.getPosition(), 0, nmouse,0, 1);
		mousex = (int) nmouse[0]; mousey = (int) nmouse[1];
		mouseMoved = true;
//		System.err.println("center: "+mousex+":"+mousey);
		repaint();
	}
	
	public void addChangeListener(ChangeListener cl)	{
		listeners.add(cl);
	}
	
	protected void fireChange()	{
		ChangeEvent ce = new ChangeEvent(this);
		for (ChangeListener cl : listeners)	{
			cl.stateChanged(ce);
		}
	}
	
	static Color specColor = new Color(1f,1f,1f,1f);
	private void updateBrush() {
//		System.err.println("Current color = "+currentColor);
		colorFactory.setAlpha(alpha);
		Color bgrColor = new Color(currentColor.getBlue(), currentColor.getGreen(), currentColor.getRed(), currentColor.getAlpha());
		brushFactory.setColor(0,bgrColor);
		brushFactory.setColor(1,specColor);
		brushFactory.setSize((int) (2 * width * radius));
		brushFactory.update();
		brush = (BufferedImage) brushFactory.getImageData().getImage();
	}
	
	public Component getInspector()	{
		if (panel != null) return panel;
		panel = new  ShrinkPanel("Paint"); // Box.createVerticalBox(); // 
		panel.setMinimumSize(new Dimension(300,10));
		GridBagLayout gl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
//		ShrinkPanel hpanel = new ShrinkPanel("Hand Paint");
//		ShrinkPanel apanel = new ShrinkPanel("Automated Paint");
//		hpanel.setLayout(gl);	
//		apanel.setLayout(gl);

		Box box = Box.createVerticalBox();

//		JTabbedPane tabs = new JTabbedPane();
//		tabs.addTab("Paint", box);
		panel.add(box, gbc);
//		panel.add(apanel, gbc);
//		panel.add(hpanel, gbc);
//		panel.add(box, gbc);

		TextSlider slider = new TextSlider.Double("alpha",SwingConstants.HORIZONTAL,0.0,1.0,alpha);
		slider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				alpha = ((TextSlider) e.getSource()).getValue().doubleValue();
				Color tmp = currentColor;
				currentColor = new Color(tmp.getRed(), tmp.getGreen(), tmp.getBlue(), (int)(255*alpha));
				specColor = new Color(1f,1f,1f,(float)alpha);
				updateBrush();
			}
		});
		box.add(slider);
		TextSlider rslider = new TextSlider.Double("radius",SwingConstants.HORIZONTAL,0.0,.25,radius);
		rslider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				radius = ((TextSlider) e.getSource()).getValue().doubleValue();
				updateBrush();
			}
		});
		box.add(rslider);

		
//		Box vbox = Box.createVerticalBox();
//		box.add(vbox);
//		panel.add(box, gbc);
		Box hbox =  Box.createHorizontalBox();
		box.add(hbox);
		Object[] types3 = {PaintType.BRUSH, PaintType.PENCIL, PaintType.POLYLINE};
		JComboBox paintTypes = new JComboBox(types3); //PaintType.values()); //
		paintTypes.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
//				System.err.println("selected "+((JComboBox)e.getSource()).getSelectedItem());
				paintType = (PaintType) ((JComboBox)e.getSource()).getSelectedItem();
			}
			
		});
		paintTypes.setPreferredSize(new Dimension(40,20));
		hbox.add(paintTypes);
		
		JCheckBox shaded = new JCheckBox("Shaded");
		shaded.setSelected(shadedSphere);
		shaded.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				shadedSphere = ((JCheckBox) arg0.getSource()).isSelected();
				brushFactory.setType(shadedSphere ?
						SimpleTextureFactory.TextureType.SPHERE :
						SimpleTextureFactory.TextureType.DISK);
				updateBrush();
			}
			
		});
		hbox.add(shaded);
		
		List<AbstractPositionProvider> positionProviders = AbstractPositionProvider.getSubclasses();
		final AbstractPositionProvider[] array = positionProviders.toArray(new AbstractPositionProvider[positionProviders.size()]);
		String[] names = new String[positionProviders.size()];
		for (int i = 0; i<names.length; ++i)	{
			names[i] = array[i].getClass().getSimpleName();
			int index = names[i].indexOf("PositionProvider");
			if (index > 0) names[i] = names[i].substring(0, index);
		}
		JComboBox poop = new JComboBox(names); //PaintType.values()); //
		poop.setSelectedIndex(1);
		poop.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int which = ((JComboBox)e.getSource()).getSelectedIndex();
				System.err.println("selected "+which);
				setPositionFactory(array[which]);
			}
			
		});
		poop.setPreferredSize(new Dimension(40,20));
		hbox.add(poop);

		box.add(Box.createVerticalStrut(5));
		hbox =  Box.createHorizontalBox();
		box.add(hbox);
		JButton resetb = new JButton("Undo");
		resetb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				undo();
			}
			
		});
		hbox.add(resetb);
		
		JButton redob = new JButton("Redo");
		redob.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				redo();
			}
			
		});
		hbox.add(redob);
		
		box.add(Box.createVerticalStrut(5));

		hbox = Box.createHorizontalBox();
		hbox.add(Box.createHorizontalGlue());
		hbox.add(jc);
		hbox.add(Box.createHorizontalGlue());
		box.add(hbox);
		final JColorChooser jcc = new JColorChooser(currentColor);
		box.add(jcc);
		jcc.setPreviewPanel(null);
		jcc.getSelectionModel().addChangeListener( new ChangeListener() {

			public void stateChanged(ChangeEvent arg0) {
				setColor( jcc.getColor());
				updateBrush();
			}

		});
		box = Box.createVerticalBox();
		Insets insets = new Insets(1,0,1,0);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = insets;
		c.weighty = 0.0;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		panel.add(box, c);
//		Box jpanel= new JPanel();
//		panel = new ShrinkPanel("Automated Paint");
//		tabs.add("Autopaint", box);
//		box.add(jpanel);
//		jpanel.setLayout(new BoxLayout(jpanel, BoxLayout.Y_AXIS));
//		jpanel.setLayout(new GridBagLayout());		
//		hbox = Box.createHorizontalBox();
//		box.add(hbox);
//		autoCheckBox = new JCheckBox("Automate");
//		hbox.add(autoCheckBox);
//		autoCheckBox.setSelected(automate);
//		autoCheckBox.addActionListener(new ActionListener() {
//
//			public void actionPerformed(ActionEvent arg0) {
//				automate = ((JCheckBox) arg0.getSource()).isSelected();
//				setAutomate(automate);
//			}
//			
//		});
//		hbox.add(autoCheckBox);
		positionPanel.add(positionProvider.getInspector());
		box.add(positionPanel, c);
		colorPanel.add(colorFactory.getInspector());
		box.add(colorPanel, c);
		box.add(Box.createVerticalGlue());
//		box.add(Box.createVerticalGlue());
			
		return panel;
	}

	transient Box positionPanel = Box.createVerticalBox();
	transient Box colorPanel = Box.createVerticalBox();
	transient private double[][] mats33s;
	private void setColor(Color cc)	{
		// TODO check here whether its BGR or RGB; this assumes RGB
		currentRGBColor = new Color(cc.getRed(), cc.getGreen(), cc.getBlue(), 255);
		currentColor = new Color(currentRGBColor.getRed(), currentRGBColor.getGreen(), currentRGBColor.getBlue(), (int)(255*alpha));
//		System.err.println("current color = "+currentColor);
	}
	public double[][] getPolygon() {
		return polygon;
	}
	public void setPolygon(IndexedFaceSet ifs)	{ //double[][] polygon) {
		// figure out how to place the fundamental polygon inside the buffered image
		// so that it is
		//   1) as big as possible while being fully contained in the BI,
		//	 2) centered horizontally, and 
		//   3) sits on the bottom edge, vertically.
		Rectangle3D bnds = BoundingBoxUtility.calculateBoundingBox(ifs);
		System.err.println("Bnds = "+bnds.toString());
		double w = bnds.getExtent()[0];
		double h = bnds.getExtent()[1];
		double cx = bnds.getCenter()[0];
		double cy = bnds.getCenter()[1];
		double s = Math.min(masterBufferedImage.getWidth()/w, masterBufferedImage.getHeight()/h);
		System.err.println("Scale = "+s);
		group2Canvas.setToIdentity();
		group2Canvas.translate((masterBufferedImage.getWidth()/2.0-s*cx),-s*bnds.getMinY());
		group2Canvas.scale(s, s);
		System.err.println("g2c = "+group2Canvas.toString());
		try {
			canvas2Group = group2Canvas.createInverse();
		} catch (NoninvertibleTransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		polygon = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		outline = new double[2*polygon.length];
		outlineC = new double[2*polygon.length];
		for (int i = 0; i<polygon.length; ++i) {
			outline[2*i] = polygon[i][0];
			outline[2*i+1] = polygon[i][1];
		}
		group2Canvas.transform(outline, 0, outlineC, 0, polygon.length);
		// calculate how to draw the polygon on the canvas
		double[][] edgeids = ifs.getVertexAttributes(Attribute.attributeForName("edgeIds")).toDoubleArrayArray(null);
		mats33s = new double[edgeids.length+1][];
		groupGens = new AffineTransform[edgeids.length+1];
		groupGens[0] = new AffineTransform(1, 0,  0, 1, 0, 0);
		// we here create a list of group generators determined by the edge identifications
		// of the fundamental polygon (in the canvas coordinate system)
		for (int i = 1; i<=edgeids.length; ++i)	{
			double[] m = mats33s[i] = GeometryUtilityOverflow.convert44To33(edgeids[i-1]);
			groupGens[i] = new AffineTransform(m[0], m[3], m[1], m[4], m[2], m[5]);
			System.err.println("group gen = "+groupGens[i].toString());
			groupGens[i].concatenate(canvas2Group);
			groupGens[i].preConcatenate(group2Canvas);
			System.err.println("canvas coords group gen = "+groupGens[i].toString());
		}
		for (int i = 0; i<polygon.length; ++i)	polygon[i][2] = 1.0;
		center = Rn.average(null, polygon);
		positionProvider.setPosition(center); 
		billiardTable = new BilliardTable(positionProvider, polygon, mats33s);
		System.err.println("New polygon = "+Rn.toString(polygon));
	}

	public void setAutomate(boolean running) {
		automate = running;
		autoCheckBox.setSelected(automate);
	}
	
	public void reset()	{
		reset = true;
		repaint();
	}
	
	public PaintType getPaintType() {
		return paintType;
	}

	public void setPaintType(PaintType paintType) {
		this.paintType = paintType;
	}



	public static void main(String[] args) {
		JFrame f = new JFrame("Paint source");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		PaintSource ap = new PaintSource(null);
	}

	public void setWallpaperGroup(WallpaperGroup theGroup) {
		this.theGroup = theGroup;
		positionProvider.setWallpaperGroup(theGroup);
	}
	
//	private Object readResolve()	{
//		overlay = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//		pushState();
//		biGraphics = masterBufferedImage.createGraphics();
//		initComponents();
//
//		return this;
//	}

	public BufferedImage getMasterBufferedImage() {
		return masterBufferedImage;
	}

	public void setMasterBufferedImage(BufferedImage bi) {
		textureImage = bi;
		ImageData id = initTexture();
		masterBufferedImage = (BufferedImage) id.getImage();
		if (masterBufferedImage == null) {
			this.masterBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		}
		pushState();
		overlay = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		biGraphics = masterBufferedImage.createGraphics();

	}

}
