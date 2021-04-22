package discreteGroup.wallpaper;

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
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
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

import charlesgunn.jreality.texture.SimpleTextureFactory;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jreality.util.Rectangle3D;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.groups.WallpaperGroup;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;

public class PaintSourceFromHome {
	JPanel jpanel = new JPanel();
	JComponent jc;
	BufferedImage masterBufferedImage, brush, overlay;
	Graphics biGraphics;
	WallpaperGroup theGroup;
	double[][] polygon = {{0,0,1},{1,0,1},{1,1,1},{0,1,1}};
	double[] center = {0,0,1}, speed = {Math.random(), Math.random(), 0};
	int width = 256, height = 256;
	Dimension size = new Dimension(width, height);
	int mousex = 0,mousey=0, prevMousex, prevMousey, oldMousex, oldMousey;
	double  dt = .01; 
	long time;
	Color currentColor = new Color(150,0,150,255), currentRealColor;
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
	private ColorProvider colorFactory;
	AffineTransform flipY = new AffineTransform(), 
		group2Canvas = new AffineTransform(), 
		canvas2Group;
	private SimpleTextureFactory brushFactory;
	enum PaintType  {BRUSH, PENCIL, POLYLINE, SNAKE};
	PaintType paintType = PaintType.BRUSH;
	SnakeGuy snakeguy = new SnakeGuy();
	private Color backgroundColor  = new Color(1f,1f,1f,1f);
	LinkedList<BufferedImage> history = new LinkedList<BufferedImage>();
	
	public PaintSourceFromHome(BufferedImage im)	{
		if (im == null) {
			masterBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		}
		else {
			masterBufferedImage = im;
		}
		overlay = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		pushState();
		biGraphics = masterBufferedImage.createGraphics();
		positionProvider = new CurlyQueuePositionProvider(); //
		initComponents();
	}
	
	public void setPositionFactory(PositionProvider pf)	{
		positionProvider =  (AbstractPositionProvider) pf;
	}
	
	public PositionProvider getPositionFactory() {
		return positionProvider;
	}
	
	public void setColorFactory(ColorProvider cf)	{
		colorFactory = cf;
	}
	
	public ColorProvider getColorFactory()	{
		return colorFactory;
	}
	
	int currentState = 0;
	private void pushState() {
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
	
	transient double[] mouse = new double[2], nmouse = new double[2];
	// draw into the buffered image
	public void paintImage(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		int w = brush.getWidth()/2;
		int h = brush.getHeight()/2;
		if (mouseMoved) {
			mouse[0] = mousex-w/2;
			mouse[1] = mousey-h/2;
			canvas2Group.transform(mouse, 0, nmouse, 0, 1);
//			System.err.println("mouse in group coords: "+nmouse[0]+" "+nmouse[1]);
			if (groupGens != null)	{
//				System.err.println("x, prev x = "+mousex+" "+prevMousex);
				double dist2 = (mousex-prevMousex)*(mousex-prevMousex) + (mousey-prevMousey)*(mousey-prevMousey);
//				System.err.println("dist2 = "+dist2);
				float scale = (float) group2Canvas.getScaleX();
				AffineTransform old = g2.getTransform();
				Graphics2D overlayG  = null;
				if (paintType == PaintType.POLYLINE || paintType == paintType.SNAKE) {
					overlayG = (Graphics2D) overlay.getGraphics();
					overlayG.drawImage(history.get(currentState), 0,0, null);
					overlayG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					overlayG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
					overlayG.setColor(currentRealColor);
					overlayG.setStroke(new BasicStroke((float) radius * scale /2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					if (paintType == paintType.SNAKE) snakeguy.draw(overlayG);					
				}

				for (AffineTransform af : groupGens) {
					g2.transform(af);
					switch(paintType)	{
						case BRUSH:
							g2.drawImage(brush.getScaledInstance(
								w,h, BufferedImage.SCALE_SMOOTH), 
								mousex-w/2, mousey-h/2, null);						
							break;
						case PENCIL:
							if (dist2 > minimumLength2){
								g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
								g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
								g2.setColor(currentRealColor);
								g2.setStroke(new BasicStroke((float) radius * scale /2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
								g2.drawLine(mousex, mousey, prevMousex, prevMousey);															
								mouseMoved = false;
							}
							break;
						case POLYLINE:
//							overlayG.transform(af);
							overlayG.drawLine(mousex, mousey, oldMousex, oldMousey);															
							mouseMoved = false;
							break;
						case SNAKE:
							overlayG.translate(mousex, mousey);
							snakeguy.draw(overlayG);
							overlayG.translate(-mousex, -mousey);	// WOW no push/pop for transforms!
							break;
					}
					if (paintType == PaintType.POLYLINE || paintType == paintType.SNAKE) {
						g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
						g2.drawImage(overlay, 0, 0, null);						
					}
					g2.setTransform(old);
				}
			}
		}
		if (reset)	{
			g2.setBackground(backgroundColor);
			g2.clearRect(0, 0, masterBufferedImage.getWidth(), masterBufferedImage.getHeight());

			reset = false;
			System.err.println("Resetting image");
		}
	}


	private void setMouseMoved() {
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
		jc = new JComponent() {
			@Override
			public void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				AffineTransform old = g2.getTransform();
				g2.transform(flipY);
				if (width != masterBufferedImage.getWidth() || height != masterBufferedImage.getHeight())
					g2.drawImage(
						masterBufferedImage.getScaledInstance(
								width, height, BufferedImage.SCALE_FAST), 
						0, 0, null);
				else
					g2.drawImage(masterBufferedImage, 0, 0, null);
				// draw outline
				int n = outline.length;
				g2.setColor(Color.black);
				g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				for (int i = 0; i<n/2; ++i)	{
					g2.draw(new Line2D.Double(
						outlineC[2*i], outlineC[2*i+1],outlineC[(2*i+2)%n], outlineC[(2*i+3)%n]));
				}
				g2.setTransform(old);
			}

			@Override
			public Dimension getMinimumSize() {
				return size;
			}

			@Override
			public Dimension getPreferredSize() {
				return size;
			}


		};
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
				repaint();
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
	
	Tool paintTool = new AbstractTool( InputSlot.getDevice("PrimaryAction"))	{
		private SceneGraphComponent origGroupEl, currentGroupEl;
		private double[] origInvMatrix, currentToOrig ;

		{
			  addCurrentSlot( InputSlot.getDevice("PointerTransformation"));
		  }
		@Override
		public void activate(ToolContext tc) {
			System.err.println("activate");
			origGroupEl = DiscreteGroupSceneGraphRepresentation.getGroupElementOnPath(tc.getRootToLocal());
			origInvMatrix = Rn.inverse(null,origGroupEl.getTransformation().getMatrix());
			currentGroupEl = origGroupEl;
			currentToOrig = Rn.identityMatrix(4);
			handleMouseMove(tc);
			oldMousex = mousex; 
			oldMousey = mousey;
			setMouseMoved();
//			mouseMoved = false;
			repaint();
		}

		@Override
		public void deactivate(ToolContext tc) {
			System.err.println("deactivate");
			handleMouseMove(tc);
			pushState();
			repaint();
		}
		
		@Override
		public void perform(ToolContext tc) {
			handleMouseMove(tc);
			repaint();
		}

		private void handleMouseMove(ToolContext tc) {
			PickResult currentPick = tc.getCurrentPick();
			if (currentPick == null ||
					currentPick.getObjectCoordinates() == null ||
					currentPick.getObjectCoordinates().length < 1) return;
			double[] mouse = (currentPick.getObjectCoordinates());
			SceneGraphComponent newGroupEl = DiscreteGroupSceneGraphRepresentation.getGroupElementOnPath(currentPick.getPickPath());
			if (newGroupEl == null) return;
			if (paintType != PaintType.BRUSH && newGroupEl != currentGroupEl)	{
				System.err.println("Moved into new group element "+newGroupEl.getName());
				currentToOrig = Rn.times(null, 
						origInvMatrix,
						newGroupEl.getTransformation().getMatrix());
				origGroupEl = currentGroupEl;
				origInvMatrix = Rn.inverse(null, origGroupEl.getTransformation().getMatrix());
				currentGroupEl = newGroupEl;
			}
			Rn.matrixTimesVector(mouse, currentToOrig, mouse);
			System.err.println("mouse = "+Rn.toString(mouse));
			group2Canvas.transform(mouse, 0, mouse, 0, 1);
			setMouseMoved();
			mousex = (int) mouse[0]; 
			mousey = (int) mouse[1];
		}
		
	};
	
	protected Tool getTool()	{
		return paintTool;
	}
	

	public void update() {
		billiardTable.update();
		colorFactory.update();
		currentColor = colorFactory.getColor();
		updateBrush();
		//DiscreteGroupUtility.reflectIntoInside(null, center, polygon, Pn.EUCLIDEAN);
//		mousex = (int) (width *(whiteBorder+(1-2*whiteBorder)* center[0]));
//		mousey = (int) (height *(whiteBorder+(1-2*whiteBorder)* center[1]));
		group2Canvas.transform(positionProvider.getPosition(), 0, nmouse,0, 1);
		mousex = (int) nmouse[0]; mousey = (int) nmouse[1];
		mouseMoved = true;
//		System.err.println("center: "+mousex+":"+mousey);
		repaint();
	}

	public BufferedImage getBufferedImage() {
		return masterBufferedImage;
	}

	List<ChangeListener> listeners = new ArrayList<ChangeListener>();
	
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
		brushFactory.setColor(0,currentColor);
		brushFactory.setColor(1,specColor);
		brushFactory.setSize((int) (2 * width * radius));
		brushFactory.update();
		brush = (BufferedImage) brushFactory.getImageData().getImage();
	}
	
	ShrinkPanel panel = null;
	public Component getInspector()	{
		if (panel != null) return panel;
		panel = new ShrinkPanel("Paint");
		GridBagLayout gl = new GridBagLayout();
		panel.setLayout(gl);		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;

		Box box = Box.createVerticalBox();
		panel.add(box, gbc);

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
		panel.add(positionProvider.getInspector(), gbc);
		panel.add(colorFactory.getInspector(),gbc);

//		autoCheckBox = new JCheckBox("Automate");
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
		
		box = Box.createVerticalBox();
		panel.add(box, gbc);
		Box hbox =  Box.createHorizontalBox();
		box.add(hbox);
		
		JComboBox paintTypes = new JComboBox(PaintType.values());
		paintTypes.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				System.err.println("selected "+((JComboBox)e.getSource()).getSelectedItem());
				paintType = (PaintType) ((JComboBox)e.getSource()).getSelectedItem();
			}
			
		});
		paintTypes.setPreferredSize(new Dimension(60,20));
		hbox.add(paintTypes);
		
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
		
		JCheckBox shaded = new JCheckBox("Shaded sphere");
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
		return panel;
	}

	private void setColor(Color cc)	{
		currentRealColor = cc;
		currentColor = new Color(currentRealColor.getBlue(), currentRealColor.getGreen(), currentRealColor.getRed(), (int)(255*alpha));
		System.err.println("current color = "+currentColor);
	}
	public double[][] getPolygon() {
		return polygon;
	}
	AffineTransform[] groupGens;
	private JCheckBox autoCheckBox;
	double[] outline, outlineC;

	public void setPolygon(IndexedFaceSet ifs)	{ //double[][] polygon) {
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
		double[][] mats33 = new double[edgeids.length+1][];
		groupGens = new AffineTransform[edgeids.length+1];
		groupGens[0] = new AffineTransform(1, 0,  0, 1, 0, 0);
		for (int i = 1; i<=edgeids.length; ++i)	{
			double[] m = mats33[i] = convert44To33(edgeids[i-1]);
			groupGens[i] = new AffineTransform(m[0], m[3], m[1], m[4], m[2], m[5]);
			System.err.println("group gen = "+groupGens[i].toString());
			groupGens[i].concatenate(canvas2Group);
			groupGens[i].preConcatenate(group2Canvas);
			System.err.println("group gen = "+groupGens[i].toString());
		}
		for (int i = 0; i<polygon.length; ++i)	polygon[i][2] = 1.0;
		center = Rn.average(null, polygon);
		positionProvider.setPosition(center); 
		billiardTable = new BilliardTable(positionProvider, polygon, mats33);
		System.err.println("New polygon = "+Rn.toString(polygon));
	}

	private static double[] convert44To33(double[] d) {
		double[] d33 = new double[9];
		d33[0] = d[0];
		d33[1] = d[1];
		d33[2] = d[3];
		d33[3] = d[4];
		d33[4] = d[5];
		d33[5] = d[7];
		d33[6] = d[12];
		d33[7] = d[13];
		d33[8] = d[15];
		return d33;
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

		PaintSourceFromHome ap = new PaintSourceFromHome(null);
	}

	public void setWallpaperGroup(WallpaperGroup theGroup) {
		this.theGroup = theGroup;
		positionProvider.setWallpaperGroup(theGroup);
	}
	

}
