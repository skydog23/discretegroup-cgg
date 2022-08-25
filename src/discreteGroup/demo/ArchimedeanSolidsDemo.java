/*
 * Created on Apr 21, 2004
 *
 */
package discreteGroup.demo;


import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import charlesgunn.jreality.viewer.Assignment;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.scene.Sky;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.ActionTool;
import de.jreality.util.DefaultMatrixSupport;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.groups.ArchimedeanSolids;
import de.jtem.discretegroup.util.WingedEdge;
import de.jtem.jrworkspace.plugin.Plugin;


/**
 * @author gunn
 *
 */
public class ArchimedeanSolidsDemo  extends Assignment {
	public boolean showSolids = true;
	public boolean showPolars = true;
	public boolean showPlatonics = false;
	boolean showLabels = false;
	public boolean transpPolars = true;
	boolean showAll = true;
	double[][] archPositions, platoPositions; // = {{0,0,0},{1,1,1},{1,-1,-1},{-1,1,-1},{-1,-1,1}};
	String[] allArchieNames = ArchimedeanSolids.getArchimedeanNames();
	WingedEdge[] archList, archPList;
	SceneGraphComponent[] archCList, archPCList, pairsList;
	SceneGraphComponent allArchKit, allPlatoKit, onekit;
	SceneGraphComponent theWorld;
	WingedEdge archie, archieP;
	SceneGraphComponent archkit, archkitP;
	private SceneGraphComponent archLabelsSGC, platoLabelsSGC;
	Appearance ap = new Appearance();
	public Appearance pap = new Appearance();
	Hashtable<String, Integer> nameTable;
	private ActionTool actionTool=new ActionTool("PanelActivation");

	public ArchimedeanSolidsDemo() {
		super();
		archkit = SceneGraphUtility.createFullSceneGraphComponent("archimedean solid");
		archkitP = SceneGraphUtility.createFullSceneGraphComponent("polar archimedean solid");
		allArchKit = SceneGraphUtility.createFullSceneGraphComponent("archimedean solid group");
		allPlatoKit = SceneGraphUtility.createFullSceneGraphComponent("platonic solid group");
//		MatrixBuilder.euclidean().scale(.8).assignTo(allPlatoKit);
		onekit = SceneGraphUtility.createFullSceneGraphComponent("archimedean solid pair");
		theWorld = SceneGraphUtility.createFullSceneGraphComponent("world");
		MatrixBuilder.euclidean().translate(0,0,-2.6).assignTo(theWorld);
		Appearance ap = theWorld.getAppearance();
		ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute("lineShader.polygonShader.diffuseColor", new Color(255, 255, 200));
		ap.setAttribute("pointShader.polygonShader.diffuseColor", new Color(255, 255,0));
		ap.setAttribute("lineShader.tubeRadius", .02);
		ap.setAttribute("pointShader.pointRadius", .03);
		ap.setAttribute("pointShader.spheresDraw", true);
//		ap.setAttribute("polygonShader.reflectionMap:blendColor",new Color(1f, 1f, 1f, .5f));
//		ap.setAttribute(CommonAttributes.RADII_WORLD_COORDINATES, true);
		theWorld.addChildren(onekit,allArchKit,allPlatoKit);
//		theWorld.addTool(new PickShowTool());
//		theWorld.getAppearance().setAttribute(CommonAttributes.RMAN_RETAIN_GEOMETRY, true);
		// set up positions for group display, on vertices of cuboctahedron
		WingedEdge co = ArchimedeanSolids.archimedeanSolid("3.4.3.4");		
		archPositions = co.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		co = ArchimedeanSolids.archimedeanSolid("3.3.3");		
		platoPositions = co.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		
		//double[] stretch = Pn.makeStretchMatrix(null, 4.0);
		//Rn.matrixTimesVector(positions, stretch, positions);

		nameTable = new Hashtable<String, Integer>();
		int n = allArchieNames.length;
		archList = new WingedEdge[n];
		archPList = new WingedEdge[n];
		archCList = new SceneGraphComponent[n];
		archPCList = new SceneGraphComponent[n];
		pairsList = new SceneGraphComponent[18];
		String[] archLabels = new String[13], platoLabels = new String[5];
		pap.setAttribute(CommonAttributes.TRANSPARENCY, .7);
		pap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
//		pap.setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, false);
		pap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, 
			new Color(255, 100,0));
		pap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, 
				new Color(255, 200, 100));
		pap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, .015);
			pap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, .01);
			double foo = .3, bar = .7, boo = .25;
		for (int i = 0; i<allArchieNames.length; ++i)	{
			nameTable.put(allArchieNames[i], new Integer(i));
			archList[i] = ArchimedeanSolids.archimedeanSolid(allArchieNames[i]);
			archCList[i] = SceneGraphUtility.createFullSceneGraphComponent(allArchieNames[i]);
			archCList[i].setAppearance(ap);
			//double[] pos = {(i-3/2.0)*3.0, (j-3/2.0) * 3.0,0};
			archCList[i].setGeometry(archList[i]);
			archPList[i]= archList[i].polarize();
			double[][] polarColorMap = {{ foo + bar * Math.random(),foo + bar *Math.random(), foo + bar *Math.random(), 1.0}};
			archPList[i].setColormap(polarColorMap);
			archPCList[i] = SceneGraphUtility.createFullSceneGraphComponent(allArchieNames[i]+" polar");
			archPCList[i].setGeometry(archPList[i]);	
			archPCList[i].setAppearance(pap);
			pairsList[i] = SceneGraphUtility.createFullSceneGraphComponent(allArchieNames[i]+" pair");
			pairsList[i].addChild(archCList[i]);
			pairsList[i].addChild(archPCList[i]);
			if (i == 0) {
				MatrixBuilder.euclidean().scale(.45).assignTo(pairsList[i]);									
			}
			else if ( i<5) {
				MatrixBuilder.euclidean().translate(platoPositions[i-1]).scale(.45).assignTo(pairsList[i]);					
			} else if (i==5)
				MatrixBuilder.euclidean().scale(boo).assignTo(pairsList[i]);					
			else if (i>5) {
				MatrixBuilder.euclidean().translate(archPositions[i-6]).scale(boo).assignTo(pairsList[i]);
			}
			DefaultMatrixSupport.getSharedInstance().storeAsDefault(pairsList[i].getTransformation());
			int k = 4;
			if (i<5)  {
				allPlatoKit.addChild(pairsList[i]);
				platoLabels[(i+k)%5] = allArchieNames[i];
			} else  {
				allArchKit.addChild(pairsList[i]);
				archLabels[i-5] = allArchieNames[i];
			}
		}
		archLabelsSGC = SceneGraphUtility.createFullSceneGraphComponent("arch names");
		Appearance a = archLabelsSGC.getAppearance();
		a.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		a.setAttribute(CommonAttributes.TEXT_SCALE, .002);
		a.setAttribute(CommonAttributes.TEXT_OFFSET, new double[]{.35,0,.05});
		a.setAttribute(CommonAttributes.TEXT_ALIGNMENT, SwingConstants.EAST);
		a.setAttribute("pointShader.textShader.diffuseColor", Color.white);
//		co.setVertexAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labels));
		double[][] allPoints = new double[archPositions.length+1][];
		for (int i = 0; i<archPositions.length; ++i) allPoints[i+1] = archPositions[i];
		allPoints[0] = new double[]{0,0,0,1};
		PointSetFactory psf = new PointSetFactory();
		psf.setVertexCount(13);
		psf.setVertexCoordinates(allPoints);
		psf.setVertexLabels(archLabels);
		psf.update();
		archLabelsSGC.setGeometry(psf.getGeometry());
		archLabelsSGC.setVisible(showLabels);
		allArchKit.addChild(archLabelsSGC);

		platoLabelsSGC = SceneGraphUtility.createFullSceneGraphComponent("plato names");
		a = platoLabelsSGC.getAppearance();
		a.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		a.setAttribute(CommonAttributes.TEXT_SCALE, .003);
		a.setAttribute(CommonAttributes.TEXT_OFFSET, new double[]{.4,0,.1});
		a.setAttribute(CommonAttributes.TEXT_ALIGNMENT, SwingConstants.EAST);
		a.setAttribute("pointShader.textShader.diffuseColor", Color.white);
		psf = new PointSetFactory();
		psf.setVertexCount(5);
		psf.setVertexCoordinates(platoPositions);
		psf.setVertexLabels(platoLabels);
		System.err.println("labels = "+platoLabels.toString());
		psf.update();
		platoLabelsSGC.setGeometry(psf.getGeometry());
		platoLabelsSGC.setVisible(showLabels);
		allPlatoKit.addChild(platoLabelsSGC);
	
		theWorld.addTool(actionTool);
		actionTool.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				ToolContext tc = (ToolContext) e.getSource();
				if (tc.getCurrentPick() == null || tc.getCurrentPick().getPickPath() == null) return;
				if (showAll) {
					SceneGraphComponent end = tc.getCurrentPick().getPickPath().getLastComponent();
					String name = end.getName().split(" ")[0];
					replaceSolid(name);
					System.err.println("got double click. name is "+name);
				} else {
					showAll();
				}
			}
		});
	}
	
	
	public SceneGraphComponent getPlatonic()	{
		double[][] verts = Primitives.tetrahedron().getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		SceneGraphComponent plato = new SceneGraphComponent("plato");
		plato.setAppearance(ap);
		double scale = .4, sqs = Math.sqrt(.4);
		for (int i = 0; i<5; ++i)	{
			SceneGraphComponent child = new SceneGraphComponent();
			plato.addChild(child);
			if (i>0) MatrixBuilder.euclidean().scale(sqs).translate(verts[i-1]).scale(sqs).assignTo(child);
			else MatrixBuilder.euclidean().scale(scale).rotateX(.2).assignTo(child);
			child.setGeometry(archList[i]);
			child.setAppearance(ap);
		}
		return plato;
	}
	
	
	@Override
	public List<Plugin> getPluginsToRegister() {
		// TODO Auto-generated method stub
		super.getPluginsToRegister();
		Sky sky = new Sky();
		sky.setEnvironment("Snow");
		sky.setShowSky(true);
		pluginsToLoad.add(sky);
		return pluginsToLoad;
//		psl.getVRPanel().setShowPanel(true);
//		psl.getJRViewer().registerPlugin(sky);
//		try {
//			sky.install(psl.getController());
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

	}


	public SceneGraphComponent getContent()	{
		//replaceSolid("3.3.3");
		showAll();
		setVisibility();
		return theWorld;
	}

	@Override
	public void display()	{
		super.display();
		viewer = jrviewer.getViewer();
		viewer.getSceneRoot().getAppearance().setAttribute(
				"polygonShader.reflectionMap:blendColor",
				new Color(1f, 1f, 1f, (float) .3));
				
		final Color URBackground = new Color(.8f, .85f, .68f); //new Color(215, 215, 190);
		final Color ULBackground  = new Color(1f, .98f, .8f); //new Color(255, 255, 200);  // bg[1];
		final Color LLBackground  = new Color(.1f, .1f, .25f); //new Color(20,20,60);
		final Color LRBackground  = new Color(0.05f, .15f, .35f); //new Color(25, 25, 100);  //bg[2];
		Color[] backgroundArray = new Color[4];
		backgroundArray[0] = URBackground;
		backgroundArray[1] = ULBackground;// bg[1];
		backgroundArray[2] = LLBackground;
		backgroundArray[3] = LRBackground;  //bg[2];
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColors", backgroundArray);
		//theMenuBar = super.createMenuBar();
		((Component) viewer.getViewingComponent()).addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
					switch(e.getKeyCode())	{
				
				case KeyEvent.VK_H:
					System.err.println("1: toggle platonic/archimedean");
					System.err.println("	2: toggle solids");
					System.out.println("	3: toggle duals");
					System.out.println("	4: toggle transparent duals");
					System.out.println("	5: toggle labels");
					break;
	
				case KeyEvent.VK_1:
					showPlatonics = !showPlatonics;
					showAll();
					//CameraUtility.encompass(viewer);
					break;
				case KeyEvent.VK_2:
					showSolids = !showSolids;
					setVisibility();
					break;
				case KeyEvent.VK_3:
					showPolars = !showPolars;
					setVisibility();
					break;
				case KeyEvent.VK_4:
					transpPolars = !transpPolars;
					pap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, transpPolars);
					viewer.renderAsync();
					break;
				case KeyEvent.VK_5:
					showLabels = !showLabels;
					archLabelsSGC.setVisible(showLabels);
					platoLabelsSGC.setVisible(showLabels);
					viewer.renderAsync();
					break;
				}
			}
			
		});

	}
		
	public void setVisibility()	{
		for (int i = 0; i<archList.length; ++i)	{
			archCList[i].setVisible(showSolids);
			archPCList[i].setVisible(showPolars);
		}
		if (viewer != null) viewer.render();
	}
	
	double foo = .1, bar = .4;
	public void replaceSolid(String name)	{
		Integer which = (Integer) nameTable.get(name);
		if (which != null) replaceSolid(which.intValue());
	}
	public void replaceSolid(int wh) {
		showAll = false;
		SceneGraphUtility.removeChildren(onekit);
		onekit.addChild(archCList[wh]);
		onekit.addChild(archPCList[wh]);
		allArchKit.setVisible(false);
		allPlatoKit.setVisible(false);
		onekit.setVisible(true);
		if (viewer != null) viewer.renderAsync();
	}
	

	public void showAll()	{
		showAll = true;
		allArchKit.setVisible(!showPlatonics);
		allPlatoKit.setVisible(showPlatonics);
		onekit.setVisible(false);
		//if (viewer != null) viewer.render();
	}

	public boolean addBackPlane() {
		return true;
	}
	public boolean isEncompass() {
		return false;
	}
	
	@Override
	public Component getInspector()	{
//		JPanel mypanel = new JPanel();
		Box vbox = inspector;
		Box hbox = Box.createVerticalBox();
		JMenuBar menubar = new JMenuBar();
		hbox.add(Box.createHorizontalGlue());
		hbox.add(menubar);
		hbox.add(Box.createHorizontalGlue());
//		vbox.add(hbox);
		//hbox.add(Box.createHorizontalGlue());
		JMenu testM = new JMenu("Solid");
		ButtonGroup bg = new ButtonGroup();
		final String[] gnames = ArchimedeanSolids.getArchimedeanNames();
		for (int i = 0; i<gnames.length; ++i)	{
			final int j = i;
			JMenuItem jm = testM.add(new JRadioButtonMenuItem(gnames[i]));
			jm.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)	{
					replaceSolid(gnames[j]);
				}
			});
			bg.add(jm);
		}
		JMenuItem jm = testM.add(new JRadioButtonMenuItem("all"));
		jm.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showAll();
			}
		});
		menubar.add(testM);

		testM = new JMenu("Flags");
		final JCheckBoxMenuItem jcm = new JCheckBoxMenuItem("Show Solids");
		testM.add(jcm);
		jcm.setSelected(showSolids);
		jcm.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				DiscreteGroupUtility.logger.log(Level.FINE,"In callback");
				showSolids = jcm.isSelected();
				setVisibility();
			}
		});
		final JCheckBoxMenuItem jcn = new JCheckBoxMenuItem("Show Polars");
		testM.add(jcn);
		jcn.setSelected(showPolars);
		jcn.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				DiscreteGroupUtility.logger.log(Level.FINE,"In callback");
				showPolars = jcn.isSelected();
				setVisibility();
			}
		});
		final JCheckBoxMenuItem jpp = new JCheckBoxMenuItem("Toggle transparent polars");
		testM.add(jpp);
		jpp.setSelected(transpPolars);
		jpp.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				transpPolars = jpp.isSelected();
				pap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, transpPolars);
				viewer.renderAsync();
			}
		});
		final JCheckBoxMenuItem jpn = new JCheckBoxMenuItem("Show Labels");
		testM.add(jpn);
		jpn.setSelected(showLabels);
		jpn.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showLabels = jpn.isSelected();
				archLabelsSGC.setVisible(showLabels);
				viewer.renderAsync();
			}
		});
		menubar.add(testM);

		//mypanel.setName("ReadMe");
		JTextArea textarea = new JTextArea(8,30);
		textarea.setEditable(false);
//		textarea.append("For documentation, click on the ? on \n" +
//		"the right end of the title bar above. \n");

		textarea.append("Explore the world of the\n" +
				"5 Platonic and 13 Archimedean solids, \n" +
				"and their duals. \n" +
				"The beginning view shows the 13 Archimedean\n"+
				"solids arranged on the 12 vertices and center\n"+
				" of the cuboctahedron\n"+
				"\nDouble click on a solid to focus on it\n" +
				"or to return to display the full collection. \n" +
				"One can also display the five Platonic solids\n" +
				"with their duals\n"+
				"Keyboard controls include:\n"+
				"    1: toggle platonic/archimedean\n" +
				"    2: toggle display of solids\n" +
				"    3: toggle display of duals\n" +
				"    4: toggle transparent duals\n" +
				"    5: toggle labels (only for groups)\n" +
				"\n'h' shows a help overlay for viewer\n" +
				"'e' encompasses the scene\n" +
				"'Meta-f toggles full-screen\n\n"+
				"\nAuthor: Charles Gunn\n"+
				"    gunn at math.tu-berlin.de\n");
		vbox.add(textarea);
		return vbox;
	}

	public static void main(String[] args) {
		new ArchimedeanSolidsDemo().display();
	}
}
