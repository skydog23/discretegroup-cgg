package discreteGroup.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import charlesgunn.jreality.geometry.BilliardsTable;
import charlesgunn.jreality.texture.SimpleTextureFactory;
import de.jreality.math.Rn;

public class TestJava2D {

	JPanel jpanel = new JPanel();
	JFrame frame = new JFrame();
	JComponent jc;
	public static void main(String[] args) {
		JFrame f = new JFrame("Weather Wizard");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		TestJava2D ap = new TestJava2D();
		ap.initComponents();
		
	}

	double[] baryCoords = {1/3.0, 1/3.0, 1/3.0};
	boolean drawDisk = false;
	double radius = 1; //.05;
	int mousex = 0,mousey=0;
	BufferedImage bi, brush;
	double[][] polygon = {{0,0,1},{1,0,1},{1,1,1},{0,1,1}};
	double[] center = {0,0,1}, speed = {.05*Math.random(), .05*Math.random(), 0};
	int width = 200, height = 200;
	Dimension size = new Dimension(width, height);
	Graphics big;
	double t1, t2 = 0, dt = .01;
	long time;
	public void initComponents() {
		final SimpleTextureFactory stf = new SimpleTextureFactory();
		stf.setType(SimpleTextureFactory.TextureType.DISK);
		stf.setSize(16);
		stf.setChannels(new int[]{3,0,1,2});
		stf.setColor(0, new Color(0,0,150,255));
		stf.update();
		brush = (BufferedImage) stf.getImageData().getImage();
		center = Rn.average(center, polygon);
		final BilliardsTable bt = new BilliardsTable(center, speed, polygon);
		Timer timer = new Timer(5, new ActionListener() {
			int c0 = 0;
			int c1 = 255;
			public void actionPerformed(ActionEvent arg0) {
				bt.update();
				stf.setColor(0, new Color(c0,0,c1,150));
				t1 += dt;
				t2 += 1.618033 * dt;
				c0 = (int) (128 + 127 * Math.cos(t1));
				c1 = (int) (128 + 127 * Math.sin(t2));
				stf.update();
				brush = (BufferedImage) stf.getImageData().getImage();
				//Rn.add(center, center, speed);
				//DiscreteGroupUtility.reflectIntoInside(null, center, polygon, Pn.EUCLIDEAN);
				mousex = (int) (width *(.25+.5* center[0]));
				mousey = (int) (height *(.25+.5* center[1]));
				drawDisk = true;
//				System.err.println("center: "+mousex+":"+mousey);
				long newtime = System.currentTimeMillis();
				int diff = (int) (newtime - time);
//				System.err.println(diff+" ms");
				time = newtime;
				jc.repaint();
			}
			
		});
		jpanel.setLayout(new BorderLayout());
		frame.getContentPane().add(jpanel);
		
		updateBI();
		jc = new JComponent() {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				if (g != big) {
					paintComponent(big);
					g2.drawImage(bi, 0, 0, null);
					return;
				}
				AffineTransform at = g2.getTransform();
//				g2.translate(50, 50);
//				g2.transform(AffineTransform.getScaleInstance(100, 100));
				g2.setColor(Color.yellow);
//				g2.fillRect(0, 0, 1, 1);
				if (drawDisk)	{
//					at.setToIdentity();
//					at.translate(50+100*center[0], 50+100*center[1]);
//					g2.setTransform(at);
					g2.drawImage(brush, mousex, mousey, null);
					//g2.draw( new Rectangle2D.Double(0,0, radius, radius));
					drawDisk = false;
				}
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
		jc.addMouseMotionListener(new MouseMotionListener() {

			public void mouseDragged(MouseEvent arg0) {
				System.err.println("mouse dragged:"+arg0.getX()+":"+arg0.getY());
				mousex = arg0.getX();
				mousey = arg0.getY();
				drawDisk = true;
				jc.repaint();
			}

			public void mouseMoved(MouseEvent arg0) {
				//System.err.println("mouse moved");				
			}
			
		});
		jc.addComponentListener(new ComponentListener()  {

			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void componentResized(ComponentEvent e) {
				width=e.getComponent().getWidth();
				height = e.getComponent().getHeight();
				int max = Math.max(width, height);
				dt = 2.0/max;
				bt.setGlobalSpeed(200.0/max);
				updateBI();
			}

			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		jpanel.add(jc);
		frame.pack();
		frame.setVisible(true);
		timer.start();

	}
	private void updateBI() {
		bi = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		big = bi.createGraphics();
	}

}
