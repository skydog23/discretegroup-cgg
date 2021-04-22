/*
 * Author	gunn
 * Created on Feb 8, 2006
 *
 */
package discreteGroup.puncturedTorus;

import static de.jreality.shader.CommonAttributes.BACK_FACE_CULLING_ENABLED;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.SPECULAR_COEFFICIENT;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY_ENABLED;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Hashtable;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import charlesgunn.jreality.viewer.GlobalProperties;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.math.Complex;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.SphereUtility;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.event.GeometryEvent;
import de.jreality.scene.event.GeometryListener;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.java2d.SceneComponent;
import de.jtem.java2d.Viewer2D;


public class PuncturedTorusDemo extends LoadableScene {
	SceneGraphComponent theWorld, 
		myroot, 
		hypersphere, 
		tracesSGC, 
		fixedPoints,
		pleatedSurface,
		limitSet;
	SceneComponent dustSC;
	boolean showSphere = true,
		showParabolics = false, 	
		showLabelsOnParabolics = true,
		parabolicCommutators = true,
		showDust = false,
		minDrawMaxDepth = false,
		drawFlat = false,
		showPleatedSurface=false;
	private IndexedLineSet theLimitSet;
	
	// parameters for displaying the boundary curve of Teichmuller space
	int minRes = 3, maxRes = 8, cuspRes = 8, numSteps = 50;
	double maxTrace = 2.25;
	private TextSlider TaReSlider, TaImSlider, TbReSlider, TbImSlider, TabReSlider, TabImSlider;
	Box psInspectorPanel = null;
	private JComponent taInspector, tbInspector, tabInspector;
	private JTabbedPane graphs;
	private String[] generatorTypes = {"Maskit slice", "Parabolic commutators", "Unconstrained"};
	private int generatorType = PuncturedTorusGroupFactory.TYPE_PARABOLIC_COMMUTATOR;
;

	final PuncturedTorusGroupFactory ptgf = new PuncturedTorusGroupFactory();
	private PuncturedTorusGroupFactory epsPtgf;
	Complex initTa = new Complex(2,0); //1.90211303259032,0);
	Complex initTb = new Complex(2,0); //1.7320508075688772, 0) ; //1.90211303259032,0);
	double epsilon = .01;
	int depth = 200; 
	Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE};
	public SceneGraphComponent makeWorld() {
		theWorld = SceneGraphUtility.createFullSceneGraphComponent("hyperbolic world");
//		theWorld.getAppearance().setAttribute("metric",Pn.HYPERBOLIC);
		theWorld.getAppearance().setAttribute(LINE_SHADER+"."+TUBES_DRAW,false);
		myroot = SceneGraphUtility.createFullSceneGraphComponent("myWorld");
		
		myroot.addChild(theWorld);

		hypersphere = Primitives.sphere(.995, 0,0,0);
		hypersphere.setGeometry(SphereUtility.tessellatedIcosahedronSphere(5));
		hypersphere.getAppearance().setAttribute(BACK_FACE_CULLING_ENABLED,  true);
		hypersphere.getAppearance().setAttribute(TRANSPARENCY_ENABLED, true);
		hypersphere.getAppearance().setAttribute(TRANSPARENCY, .5);
		hypersphere.getAppearance().setAttribute(SPECULAR_COEFFICIENT, 0.0);
		hypersphere.getAppearance().setAttribute(EDGE_DRAW, false);
		hypersphere.getAppearance().setAttribute(VERTEX_DRAW, false);
		hypersphere.getAppearance().setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.WHITE);
		myroot.addChild(hypersphere);

		ptgf.setTa(initTa); //1.91,.05));
		ptgf.setTb(initTb); //1.91,.05));
//		FiniteStateAutomaton fsa = new FiniteStateAutomaton("ab10-6.wa");
//		ptgf.setWordAcceptor(fsa);
		limitSet = new SceneGraphComponent();
		Appearance ap = new Appearance();
		limitSet.setAppearance(ap);
		ap.setAttribute(VERTEX_DRAW, false);
		ptgf.setEpsilon(epsilon);
		ptgf.setDepth(depth);
		ptgf.update();
		theLimitSet = ptgf.getLimitSet();
		limitSet.setGeometry(theLimitSet);
		theWorld.addChild(limitSet);
		fixedPoints = new SceneGraphComponent("fixedPoints");
		fixedPoints.setVisible(showParabolics);
		ap = new Appearance();
		fixedPoints.setAppearance(ap);
		ap.setAttribute(VERTEX_DRAW, true);
		ap.setAttribute("pointShader.textShader.scale", .003);
		Font f = new Font("Helvetica",Font.PLAIN,36);
		ap.setAttribute("pointShader.textShader.font", f);
		de.jreality.scene.PointSet[] fixedPointSets = ptgf.getFixedPointSets();
		double[][] offsets = {{.04,0,.04},{0,.04,.04},{-.06,0,.04},{0,-.06,.04}};
		for (int i = 0; i<4; ++i)	{
			SceneGraphComponent sgc = new SceneGraphComponent();
			sgc.setName("fixedPointSet"+i);
			ap = new Appearance();
			sgc.setAppearance(ap);
			ap.setAttribute("pointShader.polygonShader.diffuseColor", colors[i]);
			ap.setAttribute("pointShader.textShader.diffuseColor", colors[i]);
			ap.setAttribute("pointShader.textShader.offset", offsets[i]);
//			ap.setAttribute("pointShader.textShader.scale", .2);
			sgc.setGeometry(fixedPointSets[i]);
			fixedPoints.addChild(sgc);
		}
		theWorld.addChild(fixedPoints);
		pleatedSurface = ptgf.getPleatedSurface();
		pleatedSurface.setVisible(showPleatedSurface);

		theWorld.addChild(pleatedSurface);
		MatrixBuilder.euclidean().rotateY(Math.PI).assignTo(theWorld);
		return myroot;
	}

	public boolean isEncompass() {
		return true; 
	}

	public boolean addBackPlane()	{
		return true;
	}
	public void customize(JMenuBar menuBar, final Viewer viewer) {

//		menuBar.removeAll();
		JMenu fileM = menuBar.getMenu(0); //new JMenu("File");
	    JMenuItem jcc = new JMenuItem("Save PS ...");
	    fileM.add(jcc);
	    jcc.addActionListener( new ActionListener() {
	        public void actionPerformed(ActionEvent e)  {
	            savePS();
	            viewer.render();
	        }
	    });
//		menuBar.add(fileM);
		
//		JMenu fileM = new JMenu("Help");
//		JMenuItem explanation = new JMenuItem("Help... ");
//		fileM.add(explanation);
//		
//		HelpSet hs;
//		ClassLoader cl = HelpSet.class.getClassLoader();
//		try {
//		  URL hsURL = HelpSet.findHelpSet(cl, getHelpSet());
//		  hs = new HelpSet(null, hsURL);
//		  // Create a HelpBroker object:
//		  HelpBroker hb = hs.createHelpBroker();
//		  explanation.addActionListener(
//		    new CSH.DisplayHelpFromSource( hb )
//		  );
//		} catch (Exception ee) {
//			ee.printStackTrace();
//		}
//		menuBar.add(fileM);			

//		theWorld.getAppearance().setAttribute("metric",Pn.HYPERBOLIC);
		theViewer = viewer;
		theLimitSet.addGeometryListener( new GeometryListener() {
			public void geometryChanged(GeometryEvent ev) {
				System.err.println("Geometry changed, rendering");
				theViewer.renderAsync();
			}
			
		});
		
		CameraUtility.getCamera(viewer).setPerspective(false);
		
	}

	double epslinewidth = .001;
	double epseps = .01;
	int epsdepth = 200, epsmaxpoints;
	private Viewer theViewer;
	
	public boolean hasInspector() {return true; }

	protected void updateType(int which) {
		ptgf.setGeneratorFamily(which );
		if (which == PuncturedTorusGroupFactory.TYPE_MASKIT)	{
			tbInspector.setVisible(false);
//			tabInspector.setVisible(false);
		} else if (which == PuncturedTorusGroupFactory.TYPE_PARABOLIC_COMMUTATOR)	{
			tbInspector.setVisible(true);
//			tabInspector.setVisible(false);
		} else if (which == PuncturedTorusGroupFactory.TYPE_UNCONSTRAINED)	{
			tbInspector.setVisible(true);
//			tabInspector.setVisible(true);
		}
		generatorType = which;
		ptgf.update();
	}

	Hashtable ht = new Hashtable();
	public Component getInspector(final Viewer viewer) {
		//JComboBox cb = new JComboBox();
		JTabbedPane tp = new JTabbedPane();
//		tp.setPreferredSize(new Dimension(400, 600));
		JPanel enumeration = new JPanel();
		JPanel generators = new JPanel();
		JPanel drawing = new JPanel();
		tp.addTab("Generators", generators);
		generators.setToolTipText("Specify generators for the group");
		tp.addTab("Group Enumeration", enumeration);
		enumeration.setToolTipText("Set parameters controlling when group elements are generated");
		tp.addTab("Drawing", drawing);
		drawing.setToolTipText("Set drawing options");
		
		// Group generation parameters panel
		Box container1 = Box.createVerticalBox();
//		Box box23 = Box.createHorizontalBox();
//		container1.add(box23);
		generators.add(container1);
//		JComboBox jcb = new JComboBox(generatorTypes);
//		box23.add(Box.createHorizontalGlue());
//		box23.add(jcb);
//		jcb.setSelectedIndex(1);
//		box23.add(Box.createHorizontalGlue());
//		jcb.addActionListener( new ActionListener() {
//
//			    public void actionPerformed(ActionEvent e) {
//			        JComboBox cb = (JComboBox)e.getSource();
//			        String type = (String)cb.getSelectedItem();
//			        int which = 0;
//					if (type == generatorTypes[0])	{		// maskit slice
//						which = PuncturedTorusGroupFactory.TYPE_MASKIT;
//					} else if (type == generatorTypes[1])	{
//						which = PuncturedTorusGroupFactory.TYPE_PARABOLIC_COMMUTATOR;
//					} else if (type == generatorTypes[2])	{
//						which = PuncturedTorusGroupFactory.TYPE_UNCONSTRAINED;
//					}
//			        updateType(which);
//			    }			
//		});
		
		graphs = new JTabbedPane();
		SceneComponent sc;
		taInspector = new JPanel();
		tbInspector = new JPanel();
//		tabInspector = new JPanel();
		setupTaInspector();
		setupTbInspector();
//		setupTabInspector();
//		tbGraph.getRoot().setVisible(false);
		graphs.addTab("Trace a",taInspector);
		graphs.addTab("Trace b", tbInspector);
//		graphs.addTab("Trace ab", tabInspector);
//		ht.put("a", taInspector);
//		ht.put("b", tbInspector);
//		ht.put("ab",tabInspector);

		container1.add(graphs);
//		updateType(generatorType);
		
		Box hbox = Box.createHorizontalBox();
		TitledBorder title = BorderFactory.createTitledBorder(
				BorderFactory.createRaisedBevelBorder(), "Calculation state");
		hbox.setBorder(title);
		container1.add(hbox);
		JButton reset = new JButton("Reset");
		reset.addActionListener(new ActionListener() 	{
			public void actionPerformed(ActionEvent arg0) {
		    	System.err.println("Event is "+arg0.toString());
	              ptgf.setTa(new Complex(2,0));
	              ptgf.setTb(new Complex(2,0));
	              ptgf.update();
			}	
		});
		hbox.add(reset);
		final JButton interrupt = new JButton("Interrupt");
		interrupt.setBackground(Color.RED);
		interrupt.addActionListener(new ActionListener() 	{
			public void actionPerformed(ActionEvent arg0) {
		    	System.err.println("Event is "+arg0.toString());
	              ptgf.interrupt();
			}	
		});

		Timer checkCalc = new Timer(20, new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if (ptgf.isCalculating()) {
					interrupt.setText("Interrupt");
					interrupt.setBackground(Color.RED);
				}
				else {
					interrupt.setText("Finished");
					interrupt.setBackground(Color.GRAY);
				}
			}
			
		});
		checkCalc.start();
		hbox.add(interrupt);
		hbox.add(Box.createHorizontalGlue());
		
		
		// Enumeration parameters panel
		Box container = Box.createVerticalBox();
		enumeration.add(container);
//		container.setPreferredSize(new Dimension(400, 600));
		container1 = Box.createVerticalBox();
		container.add(container1);
		title = BorderFactory.createTitledBorder(
				BorderFactory.createRaisedBevelBorder(), "Enumeration parameters");
		container1.setBorder(title);
		Box hbox3 = Box.createHorizontalBox();
		container1.add(hbox3);
		final TextSlider epsSlider = new TextSlider.Double("epsilon",  SwingConstants.HORIZONTAL, 0.0, 0.1, epsilon);
		epsSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent arg0) {
		    	System.err.println("Event is "+arg0.toString());
	                ptgf.setEpsilon(epsSlider.getValue().doubleValue());
	                ptgf.update();
			}	       	
	       });
//		epsSlider.setVisible(false);
		hbox3.add(epsSlider);
		hbox3.add(Box.createHorizontalGlue());

		hbox3 = Box.createHorizontalBox();
		container1.add(hbox3);
		final TextSlider depthSlider = new TextSlider.Integer("depth",  SwingConstants.HORIZONTAL, 0, 500, depth);
		depthSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent arg0) {
		    	System.err.println("Event is "+arg0.toString());
	                ptgf.setDepth(depthSlider.getValue().intValue());
	                ptgf.update();
			}	       	
	       });
//		depthSlider.setVisible(false);
		hbox3.add(depthSlider);
		hbox3.add(Box.createHorizontalGlue());

		final TextSlider maxNoSlider = new TextSlider.Integer("maximum # points",  SwingConstants.HORIZONTAL, 0, 3000000, ptgf.getMaxNumberPoints());
		maxNoSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent arg0) {
		    	System.err.println("Event is "+arg0.toString());
	                ptgf.setMaxNumberPoints(maxNoSlider.getValue().intValue());
	                ptgf.update();
			}	       	
	       });
//		maxNoSlider.setVisible(false);
		container1.add(maxNoSlider);
		
		hbox = Box.createHorizontalBox();
		
		final JCheckBox nolimit = new JCheckBox("Draw all segments", false);
		hbox.add(nolimit);
		hbox.add(Box.createHorizontalGlue());
		container1.add(hbox);
		nolimit.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
		           if (nolimit.isSelected()) {
		        	   ptgf.setMaxNumberPoints(-1);
		        	   ptgf.update();
		        	   maxNoSlider.setEnabled(false);
		           } else maxNoSlider.setEnabled(true);
			}
		});

		
		container1 = Box.createVerticalBox();
		container.add(container1);
		title = BorderFactory.createTitledBorder(
				BorderFactory.createRaisedBevelBorder(), "Almost-parabolic element search");
		container1.setBorder(title);
		hbox3 = Box.createHorizontalBox();
		container1.add(hbox3);
		final TextSlider epsPSlider = new TextSlider.Double("trace epsilon",  SwingConstants.HORIZONTAL, 0.0, 1.0, ptgf.getEpsilonForAlmostParabolic());
		epsPSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent arg0) {
		    	System.err.println("Event is "+arg0.toString());
	                ptgf.setEpsilonForAlmostParabolic(epsPSlider.getValue().doubleValue());
	                ptgf.update();
			}	       	
	       });
//		epsPSlider.setVisible(false);
		hbox3.add(epsPSlider);
		hbox3.add(Box.createHorizontalGlue());

		final TextSlider depthPSlider = new TextSlider.Integer("Farey depth",  SwingConstants.HORIZONTAL, 2, 30, ptgf.getDepthForAlmostParabolic());
		depthPSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent arg0) {
		    	System.err.println("Event is "+arg0.toString());
	                ptgf.setDepthForAlmostParabolic(depthPSlider.getValue().intValue());
	                ptgf.update();
			}	       	
	       });
		depthPSlider.setVisible(false);
		hbox3 = Box.createHorizontalBox();
		container1.add(hbox3);
		hbox3.add(depthPSlider);
		hbox3.add(Box.createHorizontalGlue());
		
		container.add(Box.createVerticalGlue());
		
		// drawing parameters panel
		container = Box.createVerticalBox();
		drawing.add(container);
		container1 = Box.createVerticalBox();
		container.add(container1);
		title = BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), "Drawing toggles");
		container1.setBorder(title);
		hbox3 = Box.createHorizontalBox();
		container1.add(hbox3);
		final JCheckBox flatcb = new JCheckBox("Draw flat", drawFlat);
		hbox3.add(flatcb);
		flatcb.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				drawFlat = flatcb.isSelected();
				ptgf.setDrawFlat(drawFlat);
				viewer.render();
			}
		});
		hbox3.add(Box.createHorizontalGlue());
	
		hbox3 = Box.createHorizontalBox();
		container1.add(hbox3);
		final JCheckBox cp = new JCheckBox("Show Riemann sphere",showSphere);
		hbox3.add(cp);
		cp.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showSphere = cp.isSelected();
				hypersphere.setVisible(showSphere);
				viewer.render();
			}
		});
		hbox3.add(Box.createHorizontalGlue());
	
		hbox3 = Box.createHorizontalBox();
		container1.add(hbox3);
		final JCheckBox cp2 = new JCheckBox("Show parabolic fixed points",showParabolics);
		hbox3.add(cp2);
		cp2.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showParabolics = cp2.isSelected();
				fixedPoints.setVisible(showParabolics);
				viewer.render();
			}
		});
		
		final JCheckBox cp3 = new JCheckBox("with labels",showLabelsOnParabolics);
		hbox3.add(cp3);
		cp2.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showLabelsOnParabolics = cp3.isSelected();
				fixedPoints.getAppearance().setAttribute("pointShader.textShader.showLabels", new Boolean(showLabelsOnParabolics));
				viewer.render();
			}
		});
		hbox3.add(Box.createHorizontalGlue());
		
		hbox3 = Box.createHorizontalBox();
		container1.add(hbox3);
		final JCheckBox cp4 = new JCheckBox("Minimal draw at maximum depth",minDrawMaxDepth);
		hbox3.add(cp4);
		cp4.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				minDrawMaxDepth = cp4.isSelected();
				ptgf.setMinimalDrawAtMaximalDepth(minDrawMaxDepth);
				ptgf.update();
				viewer.render();
			}
		});
		hbox3.add(Box.createHorizontalGlue());
		
//		final JCheckBox cp3 = new JCheckBox("Show isolated p/q points",showDust);
//		container1.add(cp3);
//		cp3.setSelected(showDust);
//		cp3.addActionListener( new ActionListener() {
//			public void actionPerformed(ActionEvent e)	{
//				showDust = cp3.isSelected();
//				dustSC.setVisible(showDust);
//				viewer.render();
//			}
//		});
		Box box1 = Box.createHorizontalBox();
		container1.add(box1);
		final JCheckBox sps = new JCheckBox("Show pleated surface",showPleatedSurface);
		box1.add(sps);
		sps.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showPleatedSurface = sps.isSelected();
				pleatedSurface.setVisible(drawFlat ? false : showPleatedSurface);
				viewer.render();
			}
		});
		final TextSlider mplSlider = new TextSlider.Integer("with ",  SwingConstants.HORIZONTAL,1, 1000, ptgf.getPleatedSurfaceMaxElements());
		mplSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent arg0) {
	                ptgf.setPleatedSurfaceMaxElements(mplSlider.getValue().intValue());
	                ptgf.update();
			}	       	
	       });
		mplSlider.setVisible(false);
		box1.add(mplSlider);
		JLabel l = new JLabel(" elements");
		box1.add(l);
		box1.add(Box.createHorizontalGlue());

	
		return tp;
	}

	/**
	 * 
	 */
	private void setupTbInspector() {
		tbInspector.removeAll();
		Box vbox = Box.createVerticalBox();
		tbInspector.add(vbox);

		SceneComponent sc;
		//tbGraph = PuncturedTorusUtility.getViewer2D(Viewer2D.ENCOMPASS_ON_FIRST_RESIZE);
		final Viewer2D tbGraph = PuncturedTorusUtility.maskitSliceComponent(minRes, maxRes, cuspRes, maxTrace,numSteps, ptgf.getTa());
		vbox.add(tbGraph);
		tbGraph.setPreferredSize(new Dimension(400,350));
//		final PointArray pb = new PointArray(1);
		sc = new SceneComponent();
//		sc.setStamp(new BasicStamp(1));
//		sc.setPointSet(pb);
		sc.setPointDragEnabled(true);
		sc.setPointPaint(java.awt.Color.black);
//		pb.setPoint(0, ptgf.getTb().re, ptgf.getTb().im);
		tbGraph.getRoot().addChild(sc);
//		tbGraph.setTernaryTool(new DragListener()	{
//
//			public void dragStart(DragEvent arg0) {
//			}
//
//			public void drag(DragEvent arg0) {
//	           ptgf.setTb(new Complex(arg0.getX(), arg0.getY() ));
//	   			pb.setPoint(0, arg0.getX(), arg0.getY() );
//	   			tbGraph.repaint();
//	           TbReSlider.setValue(new Double(ptgf.getTb().re));
//	           TbImSlider.setValue(new Double(ptgf.getTb().im));
//	           ptgf.update();
//			}
//
//			public void dragEnd(DragEvent arg0) {
//				ptgf.setTb(new Complex(arg0.getX(), arg0.getY()));
//	   			pb.setPoint(0, arg0.getX(), arg0.getY() );
//	   			tbGraph.repaint();
//				TbReSlider.setValue(new Double(ptgf.getTb().re));
//				TbImSlider.setValue(new Double(ptgf.getTb().im));
//				ptgf.update();
//				setupTaInspector();
//				graphs.addTab("Trace a", taInspector);
//			}
//		});
//			TbReSlider = new TextSlider.Double("Tb.re",  SwingConstants.HORIZONTAL, 0.0 ,3.0, initTb.re);
//			TbReSlider.addActionListener(new ActionListener()	{
//				public void actionPerformed(ActionEvent arg0) {
//			    	System.err.println("Event is "+arg0.toString());
//		            ptgf.setTb(new Complex(TbReSlider.getValue().doubleValue(), ptgf.getTb().im));
//	                ptgf.update();
//					
//				}	       	
//		       });
//			vbox.add(TbReSlider);
//
//			TbImSlider = new TextSlider.Double("Tb.im",  SwingConstants.HORIZONTAL, -2,2, initTb.im);
//			TbImSlider.addActionListener(new ActionListener()	{
//				public void actionPerformed(ActionEvent arg0) {
//			    	System.err.println("Event is "+arg0.toString());
//		              ptgf.setTb(new Complex(ptgf.getTb().re, TbImSlider.getValue().doubleValue()));
//		              ptgf.update();
//				}	       	
//		       });
			vbox.add(TbImSlider);
	}

	/**
	 * 
	 */
	private void setupTaInspector() {
		taInspector.removeAll();
		Box vbox = Box.createVerticalBox();
		taInspector.add(vbox);
		final Viewer2D taGraph = PuncturedTorusUtility.maskitSliceComponent(minRes, maxRes, cuspRes, maxTrace,numSteps, ptgf.getTb());
		vbox.add(taGraph);
		taGraph.setPreferredSize(new Dimension(400,350));
//		final PointArray pa = new PointArray(1);
		SceneComponent sc = new SceneComponent();
//		sc.setPointSet(pa);
//		sc.setStamp(new BasicStamp(1));
		sc.setPointDragEnabled(true);
		sc.setPointPaint(java.awt.Color.black);
//		pa.setPoint(0, ptgf.getTa().re, ptgf.getTa().im);
		taGraph.getRoot().addChild(sc);
//		taGraph.setTernaryTool(new DragListener()	{
//
//			public void dragStart(DragEvent arg0) {
//			}
//
//			public void drag(DragEvent arg0) {
//	           ptgf.setTa(new Complex(arg0.getX(), arg0.getY()));
//	   			pa.setPoint(0, arg0.getX(), arg0.getY() );
//	   			taGraph.repaint();
//	           TaReSlider.setValue(new Double(ptgf.getTa().re));
//	           TaImSlider.setValue(new Double(ptgf.getTa().im));
//	           ptgf.update();
//			}
//
//			public void dragEnd(DragEvent arg0) {
//				ptgf.setTa(new Complex(arg0.getX(), arg0.getY()));
//	   			pa.setPoint(0, arg0.getX(), arg0.getY() );
//	   			taGraph.repaint();
//				TaReSlider.setValue(new Double(ptgf.getTa().re));
//				TaImSlider.setValue(new Double(ptgf.getTa().im));
//				System.err.println("Ta is " + ptgf.getTa().toString());
//				System.err.println("Tb is " + ptgf.getTb().toString());
//				ptgf.update();
//				setupTbInspector();
//				graphs.addTab("Trace b", tbInspector);
//			}
//		});		
//		TaReSlider = new TextSlider.Double("Ta.re",  SwingConstants.HORIZONTAL, 0.0,3.0, initTa.re);
//		TaReSlider.addActionListener(new ActionListener()	{
//			public void actionPerformed(ActionEvent arg0) {
//		    	System.err.println("Event is "+arg0.toString());
//                ptgf.setTa(new Complex(TaReSlider.getValue().doubleValue(), ptgf.getTa().im));
//                ptgf.update();
//				
//			}	       	
//	       });
//		vbox.add(TaReSlider);
//
//		TaImSlider = new TextSlider.Double("Ta.im",  SwingConstants.HORIZONTAL, -2,2, initTa.im);
//		TaImSlider.addActionListener(new ActionListener()	{
//			public void actionPerformed(ActionEvent arg0) {
//		    	System.err.println("Event is "+arg0.toString());
//               ptgf.setTa(new Complex(ptgf.getTa().re, TaImSlider.getValue().doubleValue()));
//                ptgf.update();
//				
//			}	       	
//	       });
		vbox.add(TaImSlider);
	}

	public void savePS()	{
		JFileChooser fc = new JFileChooser(GlobalProperties.saveResourceDir) ;
		fc.setAccessory(getPSInspectorPanel());
		int result = fc.showSaveDialog(new JFrame());
		if (result == JFileChooser.APPROVE_OPTION)	{
			File file = fc.getSelectedFile();
			String name = file.getAbsolutePath();
			GlobalProperties.saveResourceDir = file.getAbsolutePath();
			epsPtgf = ptgf.copy();
			epsPtgf.setMultiThreaded(false);
			epsPtgf.setEpsilon(epseps);
			epsPtgf.setDepth(epsdepth);
			epsPtgf.setMaxNumberPoints(epsmaxpoints);
			epsPtgf.update();
			System.err.println("factory: "+(epsPtgf.getWordAcceptor() != null));
			System.err.println("factory: "+epsPtgf.getEpsilon()+":"+epsPtgf.getDepth());
			IndexedLineSet ils = epsPtgf.getLimitSet();
			SceneGraphPath sgp = (SceneGraphPath) SceneGraphUtility.getPathsBetween(theViewer.getSceneRoot(), limitSet).get(0);
			double[] m = sgp.getMatrix(null);
			String comment = "%% Created by PuncturedTorusDemo \n%% Trace a="+ptgf.getTa().toString()+"\n%% Trace b="+ptgf.getTb().toString();
			PuncturedTorusUtility.writePS(new Matrix(m), ils, file, epslinewidth, comment);
		} else {
			JOGLConfiguration.theLog.log(Level.WARNING,"Unable to open file");
			return;
		}
		
	}
	
	public JComponent getPSInspectorPanel()	{
		if (psInspectorPanel != null) return psInspectorPanel;
		Box container1 = Box.createVerticalBox();
//		container.add(container1);
		TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), "PostScript Parameters");
		container1.setBorder(title);
		//pair = Box.createHorizontalBox();
		epseps =ptgf.getEpsilon();
		epsdepth=ptgf.getDepth();
		epsmaxpoints = ptgf.getMaxNumberPoints();
		final TextSlider reSlider = new TextSlider.Double("epsilon ",  SwingConstants.HORIZONTAL, 0.0, 0.1, epseps);
		reSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent arg0) {
		    	System.err.println("Event is "+arg0.toString());
	                epseps=reSlider.getValue().doubleValue();
			}	       	
	       });
		container1.add(reSlider);

		final TextSlider imSlider = new TextSlider.Integer("depth ",  SwingConstants.HORIZONTAL, 0,2000, epsdepth);
		imSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent arg0) {
		    	System.err.println("Event is "+arg0.toString());
	                epsdepth=imSlider.getValue().intValue();
			}	       	
	       });
		container1.add(imSlider);
		
		final TextSlider epslwSlider = new TextSlider.Double("line width ",  SwingConstants.HORIZONTAL, 0.0, 0.1, epslinewidth);
		epslwSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent arg0) {
		    	System.err.println("Event is "+arg0.toString());
	                epslinewidth=epslwSlider.getValue().doubleValue();
			}	       	
	       });
		container1.add(epslwSlider);

		
		final JCheckBox drawall = new JCheckBox("Draw all segments", false);
		container1.add(drawall);
		drawall.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
		           if (drawall.isSelected()) epsmaxpoints = -1;
		           else epsmaxpoints = ptgf.getMaxNumberPoints();
			}
		});
		psInspectorPanel = container1;
		return container1;
	}
	public int getSplitPaneOrientation() {
		return JSplitPane.HORIZONTAL_SPLIT;
	}

	public String getHelpSet() {
		return "PuncturedTorusHelp/helpset.hs";
	}

	public boolean hasHelpset() {
		return true;
	}

}
