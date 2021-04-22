/*
 * Created on Feb 12, 2006
 *
 */
package discreteGroup.methaneHydrate;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.SelectionComponent;
import charlesgunn.jreality.tools.ToolManager;
import charlesgunn.jreality.tools.UserTool;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.renderman.shader.SLShader;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.Graphics3D;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.toolsystem.ToolSystem;
import de.jreality.util.CameraUtility;
import de.jreality.util.DefaultMatrixSupport;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupConstraint;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupTranslationConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.core.DiscreteGroupViewportConstraint;
import discreteGroup.tools.CopyClickTool;


public class MethaneHydrate extends LoadableScene {
	SceneGraphComponent theWorld, sgc, quadkit;
	SelectionComponent theSelection;
	DiscreteGroupSceneGraphRepresentation theBigRepn, thePointRepn, theTranslationRepn;

	private SceneGraphComponent collectorSGC;
	private SceneGraphComponent rodsSGC;
	private SceneGraphComponent voronoicells;
	private SceneGraphComponent latticeCell;
	private double rodRadius = .5, time = 0.0; 
	boolean copyCat = true, 
		componentDisplayLists = true,
		clipToCamera = true, 
		followCamera = false,
		hiresRman = false,
		equalVolumes = true,
		planarFaces = true,
		regularPD = true,
		cube = false,
		x14faces = false,
		methaneVisible = false,
		fullCluster = false;
	SceneGraphPath pathToRepn = new SceneGraphPath();
	private DiscreteGroup bigGroup = null;
	private DiscreteGroup littleGroup = null;
	int maxElements, rmanMaxElements = 1;
	private IndexedFaceSetFactory voronoiFactory;
	Viewer viewer;
	private double methaneScale = .33;
	private Color backgroundColor;
	private SceneGraphComponent methaneSGC;
	private DiscreteGroupSceneGraphRepresentation dodecRepn;
	private SceneGraphComponent dodecCluster;
	private DodecCluster dodecClust;
	private boolean dodecClusterVisible = true;
	static double phi = .5 * (-1 + Math.sqrt(5));
	double y = .1452, a = .1882, z = -(a+2*a*y+y*y)/(-1+a);
	public SceneGraphComponent makeWorld() {
		theWorld =  SceneGraphUtility.createFullSceneGraphComponent("theWorld");
		theWorld.getAppearance().setAttribute(CommonAttributes.BACKEND_RETAIN_GEOMETRY, true);
		theWorld.getAppearance().setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, true);
		theWorld.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
//		theWorld.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
		theWorld.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS,.03);
		theWorld.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS,.2);
		theWorld.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, .5);

		littleGroup = MethaneHydrateUtility.getQuotientGroup();
		bigGroup = MethaneHydrateUtility.getTranslationGroup();
		DiscreteGroupConstraint dgc = null;
		if (followCamera)	{
			dgc = new DiscreteGroupSimpleConstraint(10, 6);
			dgc.setMaxNumberElements(200);
		}
		else {
			dgc = new DiscreteGroupTranslationConstraint(1,1,1,"abc");
			dgc.setMaxNumberElements(1);
		}
		bigGroup.setConstraint(dgc);
	
		// geometry for the voronoi cells
		{
		voronoiFactory = MethaneHydrateUtility.getVoronoiGeometry(y, z, a);
		IndexedFaceSet vor = voronoiFactory.getIndexedFaceSet();
		vor.setName("basic shape");
		voronoicells = new SceneGraphComponent("VoronoiSGC");
		voronoicells.setGeometry(vor);
		Appearance ap = new Appearance();
		voronoicells.setAppearance(ap);
		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(ap, true);
		DefaultPolygonShader dxs = (DefaultPolygonShader) dgs.getPolygonShader();
		dxs.setSmoothShading(false);
		dxs.setDiffuseColor(new Color(255,255,0));
		DefaultLineShader dls = (DefaultLineShader) dgs.getLineShader();
		dls.setTubeDraw(true);
		DefaultPolygonShader dps = (DefaultPolygonShader) ((DefaultPointShader) dgs.getPointShader()).getPolygonShader();
		dps.setDiffuseColor(new Color(100, 255, 10));
		((DefaultPointShader) dgs.getPointShader()).setDiffuseColor(dps.getDiffuseColor());
		}		
		{
		// geometry for the rods
		IndexedFaceSet rodifs = Primitives.cylinder(8, 1, -1, 1, Math.PI/2);
		rodifs.setName("rods");
		rodsSGC = new SceneGraphComponent("rodsSGC");
		Appearance ap = new Appearance();
		ap.setAttribute("polygonShader"+"."+CommonAttributes.DIFFUSE_COLOR, new Color(250, 250, 200));
		ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.FACE_DRAW, true);
		rodsSGC.setAppearance(ap);
		rodsSGC.setGeometry(rodifs);
		rodsSGC.setVisible(false);
		MatrixBuilder.euclidean().translate(2,0,.5).scale(rodRadius, rodRadius, .5).assignTo(rodsSGC);
		}

		thePointRepn = new  DiscreteGroupSceneGraphRepresentation(littleGroup, copyCat, "Point" );
		thePointRepn.getDropBox().setComponentDisplayLists(componentDisplayLists);
		// add geometry for  the lattice
		// unfortunately this gets drawn about a million times too often
		{
		latticeCell = new SceneGraphComponent("cubeSGC");
		latticeCell.setGeometry(Primitives.coloredCube());
		MatrixBuilder.euclidean().translate(1,1,1).assignTo(latticeCell);
		latticeCell.getGeometry().setName("cube");
		Appearance ap = new Appearance();
		latticeCell.setAppearance(ap);
		latticeCell.setVisible(false);
		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(ap, true);
		dgs.setShowFaces(false);
		DefaultLineShader dls = (DefaultLineShader) dgs.getLineShader();
		dls.setTubeDraw(false);
		dgs.setShowLines(true);
		dgs.setShowPoints(false);
		}
		
		collectorSGC  = new SceneGraphComponent("collector");
		collectorSGC.addChild(voronoicells);
		collectorSGC.addChild(latticeCell);
		collectorSGC.addChild(rodsSGC);
		collectorSGC.setAppearance(new Appearance());
		collectorSGC.getAppearance().setAttribute("vertexShadername","simple");
		collectorSGC.getAppearance().setAttribute("singlePeer",true);

		thePointRepn.setWorldNode(collectorSGC);
		thePointRepn.setElementList(littleGroup.getElementList());
		thePointRepn.update();
		SceneGraphComponent representationRoot = thePointRepn.getRepresentationRoot();
		representationRoot.getAppearance().setAttribute(CommonAttributes.RMAN_ARCHIVE_CURRENT_NODE, true);
		Appearance ap = representationRoot.getAppearance();
		ap.setAttribute("discreteGroup.clipToCamera", false);
		ap.setAttribute("discreteGroup.topCat", false);			

		dodecClust = new DodecCluster(this);
		dodecClust.setDodecClusterElements(fullCluster);
//		dodecRepn = dodecClust.getDodecCluster();
//		dodecCluster = dodecRepn.getRepresentationRoot();
		updateGeometryParameters();
//		dodecCluster.setVisible(dodecClusterVisible);
		dodecCluster = dodecClust.getTranslationCell();
		theWorld.addChild(dodecCluster);
		
		theBigRepn = new  DiscreteGroupSceneGraphRepresentation(bigGroup, copyCat, "Translation" );
//		theBigRepn.getDropBox().componentDisplayLists = componentDisplayLists;
		SceneGraphComponent sgc2 = new SceneGraphComponent();
//		SceneGraphComponent flat = GeometryUtility.flatten(representationRoot);
//		SceneGraphComponent merge = IndexedFaceSetUtility.mergeIndexedFaceSets(flat);
		sgc2.addChild(representationRoot);
		methaneSGC = new SceneGraphComponent("Methane collector");
		methaneSGC.setVisible(methaneVisible);
		ap = new Appearance();
		ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
		ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,
				MethaneHydrateUtility.methaneBondColor);
		methaneSGC.setAppearance(ap);
		double[][] tlates = {{2,0,1},{2,0,-1},{1,2,0},{-1,2,0},{0,1,2},{0,-1,2}, {0,0,0}, {2,2,2}};
		for (int i = 0; i<tlates.length; ++i)	{
			SceneGraphComponent methane = MethaneHydrateUtility.getMethaneMolecule(methaneScale, MethaneHydrateUtility.carbonColors[3]); //i/2]);
			MatrixBuilder.euclidean().translate(tlates[i]).assignTo(methane);
			methaneSGC.addChild(methane);
		}
		sgc2.addChild(methaneSGC);
		theBigRepn.setWorldNode(sgc2);
		theBigRepn.setElementList(bigGroup.getElementList());
		theBigRepn.update();
		DefaultMatrixSupport.getSharedInstance().storeDefaultMatrices(theWorld);
		return theWorld;
	}

	// we may need these special values of the parameters to interpolate between
	double[] planarFacesEV = {0.26, 0.37, 0.16};
	double[] regularDodecEV = {0.3135199920671645, 0.49828000793283533, 0.1882};
	private void updateGeometryParameters() {
		if (regularPD)	{
			if (equalVolumes) a = .1882;
			y = (1-a) * (1+phi) - 1;
			z = 1 - (1-a)*phi;
		} else if (cube)	{
			if (equalVolumes) a = 0.0;
			y = -a+.00001;		// work around bug in normal calculations
			z = -(a+2*a*y+y*y)/(-1+a);
		} else if (!planarFaces)	{	// the general case
			z = -(a+2*a*y+y*y)/(-1+a);			
		} else {
//			z = (1-y)/2.0;
//			a = (1-2*y)/3.0;
			if (equalVolumes) a = .16;
			z = (1+3*a)/4;
			y = (1-3*a)/2;
		}
		System.err.println("y,z,a:"+y+" "+z+" "+a);
		MethaneHydrateUtility.getVoronoiGeometry(voronoiFactory, y, z, a);
		dodecClust.updateVerts(y,z,a);
		dodecClust.animateDodec(time);
		if (viewer != null) viewer.renderAsync();
	}

	public boolean isEncompass() {
		return true; 
	}

	int clusterSize = 0;
	@Override
	public void customize(JMenuBar menuBar, final Viewer v) {
		viewer = v;
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKEND_RETAIN_GEOMETRY, true);
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.IGNORE_ALPHA0, !x14faces);
		backgroundColor = new Color(100, 100, 120);
		//		viewer.getSceneRoot().getAppearance().setAttribute("fogColor", new Color(100,100, 120));
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor", backgroundColor);
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLORS_STRETCH_X, 1.1);
//		viewer.getSceneRoot().addTool(new PickShowTool());
		SLShader myfog = new SLShader("myfog");
		myfog.addParameter("signore", new Double(2.0));
		myfog.addParameter("distance", new Double(7.0));
		myfog.addParameter("background", new float[]{.4f, .4f, .5f});
//		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.RMAN_VOLUME_ATMOSPHERE_SHADER, myfog);
		MatrixBuilder.euclidean().translate(-2, -2, 0).assignTo(CameraUtility.getCameraNode(viewer));
		Graphics3D gc = new Graphics3D(viewer);
		DiscreteGroupViewportConstraint vc = new DiscreteGroupViewportConstraint(  6d, 4, -1, -1, gc);
		theBigRepn.setViewportConstraint(vc);
		pathToRepn = new SceneGraphPath();
		pathToRepn.push(viewer.getSceneRoot());
		pathToRepn.push(theWorld);
		theBigRepn.attachToViewer(viewer, pathToRepn, followCamera, 200, clipToCamera, 200);
		Appearance ap = theBigRepn.getRepresentationRoot().getAppearance();
		if (copyCat)	{
			ap.setAttribute("discreteGroup.clipToCamera", clipToCamera);			
			ap.setAttribute("discreteGroup.topCat", true);			
		}
		if (hiresRman)	{
			DiscreteGroupSceneGraphRepresentation fancy = new  DiscreteGroupSceneGraphRepresentation(bigGroup, copyCat, "hiresRman");
			fancy.setWorldNode(theBigRepn.getWorldNode());
			DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(16.0, -1);
			dgsc.setMaxNumberElements(rmanMaxElements);
			fancy.setElementList(DiscreteGroupUtility.generateElements(bigGroup,dgsc));
			fancy.update();
			ap.setAttribute(CommonAttributes.RMAN_PROXY_COMMAND, fancy.getRepresentationRoot());
		}
		bigGroup2 = MethaneHydrateUtility.getTranslationGroup();
		bigGroup2.setConstraint(new DiscreteGroupSimpleConstraint(200)); //setMaxNumberElements(200);
//		bigGroup2.setDirichletDomainOrbit(100);
		bigGroup2.setCenterPoint(new double[]{0,0,0});
		origcct = new CopyClickTool(bigGroup2,theBigRepn);
		UserTool cct = origcct.wrapCCT();
		ToolManager.toolManagerForViewer(viewer).addUserTool(cct, null, "click to copy");
		SceneGraphComponent toolDD = origcct.getSceneGraphComponent();
		//toolDD.addTool(cct);
		theBigRepn.getFundamentalRegion().addChild(toolDD);
//		theBigRepn.getRepresentationRoot().addTool(cct);
		
		SceneGraphPath sgp = new SceneGraphPath(viewer.getSceneRoot(), theBigRepn.getRepresentationRoot());
		ToolSystem.getToolSystemForViewer(viewer).setEmptyPickPath(sgp);

//		viewer.getSceneRoot().addTool(new PickShowTool());
		if (!followCamera) CameraUtility.encompass(viewer);
		CameraUtility.getCamera(viewer).setNear(.05);
		JMenu testM = new JMenu("Actions");

		JRadioButtonMenuItem jca = new JRadioButtonMenuItem("Toggle cages visible");
		testM.add(jca);
		jca.setSelected(voronoicells.isVisible());
		jca.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0));
		jca.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				voronoicells.setVisible( ((JRadioButtonMenuItem) e.getSource()).isSelected());
				viewer.renderAsync();
			}
		});

		JRadioButtonMenuItem jcb = new JRadioButtonMenuItem("Toggle methane molecules visible");
		testM.add(jcb);
		jcb.setSelected(methaneVisible);
		jcb.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0));
		jcb.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				methaneVisible = ((JRadioButtonMenuItem) e.getSource()).isSelected();
				methaneSGC.setVisible(methaneVisible);
				viewer.renderAsync();
			}
		});

		jca = new JRadioButtonMenuItem("Toggle lattice visible");
		testM.add(jca);
		jcb.setSelected(latticeCell.isVisible());
		jca.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, 0));
		jca.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				latticeCell.setVisible(((JRadioButtonMenuItem) e.getSource()).isSelected());
				viewer.renderAsync();
			}
		});
		jca = new JRadioButtonMenuItem("Toggle rod visible");
		testM.add(jca);
		jca.setSelected(rodsSGC.isVisible());
		jca.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, 0));
		jca.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				rodsSGC.setVisible(((JRadioButtonMenuItem) e.getSource()).isSelected());
				System.err.println("Toggling rods visible");
				viewer.renderAsync();
			}
		});

		jcb = new JRadioButtonMenuItem("Toggle 14-side pentagons");
		testM.add(jcb);
		jcb.setSelected(x14faces);
		jcb.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_6, 0));
		jcb.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
//				x14.setVisible(!rodsSGC.isVisible());
				x14faces = ((JRadioButtonMenuItem) e.getSource()).isSelected();
				viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.IGNORE_ALPHA0, !x14faces);
				System.err.println("Toggling 14-faces"+x14faces);
				viewer.renderAsync();
			}
		});

		jcb = new JRadioButtonMenuItem("Toggle dodecahedron cluster");
		testM.add(jcb);
		jcb.setSelected(dodecCluster.isVisible());
		jcb.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7, 0));
		jcb.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
//				x14.setVisible(!rodsSGC.isVisible());
				dodecCluster.setVisible(!dodecCluster.isVisible());
				viewer.renderAsync();
			}
		});

		jca = new JRadioButtonMenuItem("Toggle follow camera");
		testM.add(jca);
		jca.setSelected(theBigRepn.isFollowsCamera());
		jca.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_8, 0));
		jca.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				theBigRepn.setFollowsCamera(((JRadioButtonMenuItem) e.getSource()).isSelected());
				if (!theBigRepn.isFollowsCamera()) {
					theBigRepn.setClipToCamera(false);
					theBigRepn.getRepresentationRoot().getAppearance().setAttribute("discreteGroup.clipToCamera", theBigRepn.isClipToCamera());
					CameraUtility.encompass(viewer);
				}
				viewer.renderAsync();
			}
		});

		jca = new JRadioButtonMenuItem("Toggle clip to camera");
		testM.add(jca);
		jca.setSelected(theBigRepn.isClipToCamera());
		jca.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_9, 0));
		jca.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
		//		if (copyCat) return;
				theBigRepn.setClipToCamera( ((JRadioButtonMenuItem) e.getSource()).isSelected());
				theBigRepn.getRepresentationRoot().getAppearance().setAttribute("discreteGroup.clipToCamera", theBigRepn.isClipToCamera());
				viewer.renderAsync();
			}
		});

		menuBar.add(testM);
	}
	
	int timecount = 0;
	Timer timer = null;
	private SceneGraphComponent tessX, tessY, tessZ;
	private SceneGraphComponent[] tess = new SceneGraphComponent[3];
	private int which = 0;
	private boolean direction = true;
	double[][] tlates = {{4,0,0},{0,4,0},{0,0,4}};
	FactoredMatrix tm1 = new FactoredMatrix(Rn.identityMatrix(4)), tm2, tm3= new FactoredMatrix();
	private DiscreteGroup bigGroup2;
	private CopyClickTool origcct;
	private void performAnimation(boolean forward, int axis)	{
		direction = forward;
		which = axis;
		tm2 = new FactoredMatrix(MatrixBuilder.euclidean().translate(tlates[which]).getArray());
		if (tessX == null)	{
			tess[0] = tessX = new SceneGraphComponent("tess0");
			SceneGraphComponent sgc = new SceneGraphComponent("tess00");
			tessX.addChild(sgc);
			sgc.setAppearance(new Appearance());
//			sgc.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
			sgc.addChild(dodecCluster);
			tess[1] = tessY = new SceneGraphComponent("tess1");
			sgc = new SceneGraphComponent("tess10");
			sgc.setAppearance(new Appearance());
//			sgc.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
			tessY.addChild(sgc);		
			sgc.addChild(tessX);
			tess[2] = tessZ = new SceneGraphComponent("tess2");
			sgc = new SceneGraphComponent("tess20");
			sgc.setAppearance(new Appearance());
//			sgc.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
			tessZ.addChild(sgc);		
			sgc.addChild(tessY);
			theWorld.addChild(tessZ);	
//			for (int i = 0; i<3; ++i) tess[i].setTransformation(new Transformation());
		}
		final double animationLength = 3;
		timecount = 0;
		final int alreadyAdded = tess[which].getChildComponentCount() - 1;
		final SceneGraphComponent newSGC = new SceneGraphComponent("tess"+which+(alreadyAdded+1));
		newSGC.setAppearance(new Appearance());
		newSGC.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		newSGC.addChild(which == 0 ? dodecCluster : tess[which-1]);
		System.err.println("Adding copy which = "+which);
		tess[which].addChild(newSGC);
		MatrixBuilder.euclidean().translate(Rn.times(null, alreadyAdded, tlates[which])).assignTo(tm1);
		MatrixBuilder.euclidean().translate(Rn.times(null, alreadyAdded+1, tlates[which])).assignTo(tm2);
		final int delay = 20;
//		if (timer == null) 
			timer = new Timer(delay, new ActionListener()	{
				public void actionPerformed(ActionEvent e) {
					double t = timecount * delay /(animationLength * 1000.0);
					//if ((count+2) > tess[which].getChildComponentCount()) {
					if (t > 1) {
						System.err.println("Setting tform for "+newSGC.getName());
						tm2.assignTo(newSGC);
						stopAnimation(which);
					} else {
						AnimationUtility.linearInterpolation(tm3, tm1, tm2, t);
						tm3.assignTo(newSGC);
						timecount++;						
					}
					viewer.renderAsync();
				}
				
			});
		timer.start();
	}
	private void stopAnimation(int axis)	{
		which = axis;
		for (int i = 0; i<tess[which].getChildComponentCount(); ++i)	{
			SceneGraphComponent sgc = tess[which].getChildComponent(i);
			if (sgc.getAppearance() != null)
				sgc.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, Appearance.INHERITED);
		}
		timer.stop();
	}
	public boolean hasInspector() {return true; }
	public Component getInspector(final Viewer viewer) {	
		Box inspectionPanel =  Box.createVerticalBox();
		final TextSlider holeFactorSlider = new TextSlider.Double("a",SwingConstants.HORIZONTAL,0.0,1.0,a);
		holeFactorSlider.setEnabled(!planarFaces);
		final TextSlider squashFactorSlider = new TextSlider.Double("y",SwingConstants.HORIZONTAL,-1.0,1.0,y);
		Box animateBox =  Box.createHorizontalBox();
		JButton animate = new JButton("+x");
		animate.addActionListener( new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				performAnimation(true, 0);
			}
		});
		animateBox.add(animate);
		animate = new JButton("+y");
		animate.addActionListener( new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				performAnimation(true, 1);
			}
		});
		animateBox.add(animate);
		 animate = new JButton("+z");
		animate.addActionListener( new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				performAnimation(true, 2);
			}
		});
		animateBox.add(animate);
		inspectionPanel.add(animateBox);
		JCheckBox translationCell = new JCheckBox("translation cell", !fullCluster);
		translationCell.addActionListener( new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				fullCluster = !((JCheckBox) e.getSource()).isSelected();
				//tsf.setMakeHoles(makeHoles);
				dodecClust.setDodecClusterElements(fullCluster);
				viewer.renderAsync();
			}
		});
		inspectionPanel.add(translationCell);
		JCheckBox equalVolumeBox = new JCheckBox("equal volume cages", equalVolumes);
		equalVolumeBox.addActionListener( new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				equalVolumes = ((JCheckBox) e.getSource()).isSelected();
				//tsf.setMakeHoles(makeHoles);
				updateGeometryParameters();
			}
		});
		inspectionPanel.add(equalVolumeBox);
		JCheckBox thickButton = new JCheckBox("planar faces on tetradekahedron", planarFaces);
		thickButton.addActionListener( new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				planarFaces = ((JCheckBox) e.getSource()).isSelected();
				//tsf.setMakeHoles(makeHoles);
				updateGeometryParameters();
			}
		});
		inspectionPanel.add(thickButton);
		final JCheckBox regularPDButton = new JCheckBox("regular pentagon dodecahedron", regularPD);
		final JCheckBox cubeButton = new JCheckBox("cube", cube);
		regularPDButton.addActionListener( new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				regularPD = ((JCheckBox) e.getSource()).isSelected();
				if (regularPD) {
					cube = false;
					cubeButton.setSelected(false);
				}
				//tsf.setMakeHoles(makeHoles);
				updateGeometryParameters();
			}
		});
		inspectionPanel.add(regularPDButton);
		cubeButton.addActionListener( new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				cube = ((JCheckBox) e.getSource()).isSelected();
				//tsf.setMakeHoles(makeHoles);
				if (cube) {
					regularPD = false;
					regularPDButton.setSelected(false);
				}
				updateGeometryParameters();
			}
		});
		inspectionPanel.add(cubeButton);
		holeFactorSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				a = holeFactorSlider.getValue().doubleValue();
				System.err.println("a "+a);
				updateGeometryParameters();
			}
		});
		inspectionPanel.add(holeFactorSlider);
		squashFactorSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				y = squashFactorSlider.getValue().doubleValue();
				System.err.println("y: "+y);
				updateGeometryParameters();
			}
		});
		inspectionPanel.add(squashFactorSlider);
		final TextSlider rodRadiusSlider = new TextSlider.Double("rod radius",SwingConstants.HORIZONTAL,0.0,1.0,rodRadius);
		rodRadiusSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				rodRadius = rodRadiusSlider.getValue().doubleValue();
				System.err.println("rodRadius: "+rodRadius);
				MatrixBuilder.euclidean().translate(2,0,.5).scale(rodRadius, rodRadius, .5).assignTo(rodsSGC);
				viewer.renderAsync();
			}
		});
		inspectionPanel.add(rodRadiusSlider);
		final TextSlider timeSlider = new TextSlider.Double("time",SwingConstants.HORIZONTAL,0.0,1.0,time);
		timeSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				time = timeSlider.getValue().doubleValue();
				dodecClust.animateDodec(time);
				viewer.renderAsync();
			}
		});
		inspectionPanel.add(timeSlider);
		return inspectionPanel;
	}
	

	public String getHelpSet() {
		return "MethaneHydrateHelp/helpset.hs";
	}
	public boolean hasHelpset() {
		return true;
	}

}

//else {
//	DiscreteGroupElement[] gens = mhg.getFullGroup().getGenerators();
//	// generate the "point" group
//	DiscreteGroup ptgp = new DiscreteGroup();
//	DiscreteGroupElement[] ptgens = new DiscreteGroupElement[4];
//	ptgens[0] = gens[2];
//	ptgens[1] = gens[3];
//	ptgens[2] = gens[6];
//	ptgens[3] = gens[7];
//	ptgp.setGenerators(ptgens);
//	ptgp.setDimension(3);
//	ptgp.setFinite(true);
//	ptgp.setElementList(DiscreteGroup.generateElements(ptgp, null));
//	
//	// generate the "mirror" group
//	DiscreteGroup mgp = new DiscreteGroup();
//	DiscreteGroupElement[] mgens = new DiscreteGroupElement[6];
//	for (int i = 0; i < 6; ++i)	{
//		double[] mat = P3.makeReflectionMatrix(null, planes[i], Pn.EUCLIDEAN);
//		mgens[i] = new DiscreteGroupElement(Pn.EUCLIDEAN, mat);
//		mgens[i].setWord(DiscreteGroupUtility.gnames[i]);
//	}
//	mgp.setGenerators(mgens);
//	mgp.setDimension(3);
//	mgp.setMetric(Pn.EUCLIDEAN);
//	mgp.setMaxNumberElements(250);
//	DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(4, -1);
//	dgsc.setManhattan(true);
//	dgsc.setCenterPoint(new double[]{1,1,1,1});
//	final double maxdist = 5.0;
//	DiscreteGroupConstraint dgc = new DiscreteGroupConstraint()	{
//		double[] tmp = new double[4];
//		double[] centerPoint = {0,0,0,1};
//		public boolean acceptElement(DiscreteGroupElement dge) {
//			double[] mat = dge.getMatrix();
//			//tmp[0] = mat[3];  tmp[1] = mat[7];  tmp[2] = mat[11];  tmp[3] = mat[15];
//			Matrix m = new Matrix(dge.getMatrix());
//			tmp = m.getColumn(3);
//			double d = 0;
//			double[] diff = Pn.dehomogenize(null, Rn.abs(null, Rn.subtract(null, tmp, centerPoint)));
//			d = Math.max(diff[0], Math.max(diff[1], diff[2]));				
//			double det = m.getDeterminant();
//			//System.err.println("Word, dist: "+dge.getWord()+" "+d);
//			if (d >(maxdist - (det < 0 ? 0 : 0))) return false;
//			return true;
//		}
//
//		public int getMaxNumberElements() {
//			return 250;
//		}
//
//	};
//	mgp.setConstraint(dgc);
//	dgsc = new DiscreteGroupSimpleConstraint(8.0,4);
//	if (followCamera) mgp.setConstraint(dgsc);
//	mgp.setElementList(DiscreteGroup.generateElements(mgp, dgc));
//	// generate the translation group
//	DiscreteGroup tgp = new DiscreteGroup();
//	gens = new DiscreteGroupElement[7];
//	for (int i = 0; i<3; ++i)	{
//		double[] mat = Rn.times(null, mgens[2*i].getMatrix(), mgens[2*i+1].getMatrix());
//		gens[2*i] = new DiscreteGroupElement(Pn.EUCLIDEAN, mat);
//		gens[2*i].setWord(DiscreteGroupUtility.gnames[i]);
//		gens[2*i+1] = (DiscreteGroupElement) gens[0].getInverse();
//		
//	}
//	double[] mat = P3.makeRotationMatrix(null, vs[0], vs[1], 2*Math.PI/3.0,Pn.EUCLIDEAN);
//	gens[6] = new DiscreteGroupElement(Pn.EUCLIDEAN, mat);
//	gens[6].setWord(DiscreteGroupUtility.gnames[1]);
//	tgp.setGenerators(gens);
//	tgp.setDimension(3);
//	tgp.setMetric(Pn.EUCLIDEAN);
//	tgp.setMaxNumberElements(250);
//	dgsc = new DiscreteGroupSimpleConstraint(5, -1);
//	dgsc.setManhattan(true);
//	tgp.setConstraint(dgsc);
//	tgp.setElementList(DiscreteGroup.generateElements(tgp, dgsc));
//
//	bigGroup = mgp;
//	littleGroup = ptgp;
//}
