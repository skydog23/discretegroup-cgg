/*
 * Created on Apr 21, 2004
 *
 */
package discreteGroup.demo;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingConstants;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.TextureUtility;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.ui.viewerapp.SelectionManagerImpl;
import de.jreality.util.DefaultMatrixSupport;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.groups.ArchimedeanSolids;
import de.jtem.discretegroup.util.WingedEdge;
import discreteGroup.ResourceClass;

/**
 * @author gunn
 * 
 */
public class XmasArchimedeanSolids extends LoadableScene {
	boolean showSolids = true, showPolars = true, showLabels = false,
			transpPolars = false;
	double[][] positions;
	String[] showAllNames = ArchimedeanSolids.getArchimedeanNames();
	WingedEdge[] archList, archPList;
	SceneGraphComponent[] archCList, archPCList, pairsList;
	SceneGraphComponent allkit, onekit;
	SceneGraphComponent theWorld, holder;
	WingedEdge archie, archieP;
	SceneGraphComponent archkit, archkitP;
	private SceneGraphComponent labelsSGC;
	Appearance ap = new Appearance(), pap = new Appearance();
	Hashtable<String, Integer> nameTable;
	boolean ct = true, bkgdImage = true;

	/**
	 * 
	 */
	public XmasArchimedeanSolids() {
		super();
		archkit = SceneGraphUtility
				.createFullSceneGraphComponent("archimedean solid");
		archkitP = SceneGraphUtility
				.createFullSceneGraphComponent("polar archimedean solid");
		allkit = SceneGraphUtility
				.createFullSceneGraphComponent("archimedean solid group");
		onekit = SceneGraphUtility
				.createFullSceneGraphComponent("archimedean solid pair");
		theWorld = SceneGraphUtility
				.createFullSceneGraphComponent("archimedean solid world");
		MatrixBuilder.euclidean().translate(0, 0, -4).assignTo(theWorld);
		Appearance ap = theWorld.getAppearance();
		ap.setAttribute(CommonAttributes.LINE_SHADER + "."
				+ CommonAttributes.TUBES_DRAW, true);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute("lineShader.polygonShader.diffuseColor", new Color(255,255, 200));
		ap.setAttribute("pointShader.polygonShader.diffuseColor", new Color(255, 255, 0));
		ap.setAttribute("lineShader.tubeRadius", .03);
		ap.setAttribute("pointShader.pointRadius", .05);
		ap.setAttribute("pointShader.spheresDraw", true);
//		try {
//			CubeMap rm = TextureUtility.createReflectionMap(
//			          ap,
//			          "polygonShader",
//			          "/homes/geometer/gunn/Pictures/textures/christmasTree/cm_", //textures/jms/jms_", //desertstorm/desertstorm_",
//			          new String[]{"rt","lf","up", "dn","bk","ft"},
//			          ".png");
//			rm.setBlendColor(new Color(1f,1f,1f, 0.3f));
//			CubeMap rm2 = TextureUtility.createReflectionMap(ap, "lineShader.polygonShader", 
//					TextureUtility.getCubeMapImages(rm));
//			rm2.setBlendColor(new Color(1f,1f,1f, 0.3f));
//			CubeMap rm3 = TextureUtility.createReflectionMap(ap, "pointsShader.polygonShader", 
//					TextureUtility.getCubeMapImages(rm));
//			rm3.setBlendColor(new Color(1f,1f,1f, 0.3f));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		theWorld.addChild(onekit);
		theWorld.addChild(allkit);
		// theWorld.getAppearance().setAttribute(CommonAttributes.RMAN_RETAIN_GEOMETRY,
		// true);
		// set up positions for group display, on vertices of cuboctahedron
		WingedEdge co = ArchimedeanSolids.archimedeanSolid("3.4.3.4");
		positions = co.getVertexAttributes(Attribute.COORDINATES)
				.toDoubleArrayArray(null);
		// double[] stretch = Pn.makeStretchMatrix(null, 4.0);
		// Rn.matrixTimesVector(positions, stretch, positions);

		nameTable = new Hashtable<String, Integer>();
		int n = showAllNames.length;
		archList = new WingedEdge[n];
		archPList = new WingedEdge[n];
		archCList = new SceneGraphComponent[n];
		archPCList = new SceneGraphComponent[n];
		pairsList = new SceneGraphComponent[18];
		String[] labels = new String[12];
		pap.setAttribute(CommonAttributes.TRANSPARENCY, .8);
		pap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		if (ct)
			pap.setAttribute(CommonAttributes.FACE_DRAW, false);
		// pap.setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, false);
		pap.setAttribute(CommonAttributes.POINT_SHADER + "."
				+ CommonAttributes.POLYGON_SHADER + "."
				+ CommonAttributes.DIFFUSE_COLOR, new Color(255, 100, 0));
		pap.setAttribute(CommonAttributes.LINE_SHADER + "."
				+ CommonAttributes.POLYGON_SHADER + "."
				+ CommonAttributes.DIFFUSE_COLOR, new Color(255, 200, 100));
		pap.setAttribute(CommonAttributes.POINT_SHADER + "."
				+ CommonAttributes.POINT_RADIUS, ct ? .04 : 015);
		pap.setAttribute(CommonAttributes.LINE_SHADER + "."
				+ CommonAttributes.TUBE_RADIUS, ct ? .027 : .01);
		for (int i = 0; i < showAllNames.length; ++i) {
			double foo = .1, bar = .4;
			nameTable.put(showAllNames[i], new Integer(i));
			archList[i] = ArchimedeanSolids
					.archimedeanSolid(showAllNames[i]);
			archCList[i] = SceneGraphUtility
					.createFullSceneGraphComponent(showAllNames[i]);
			archCList[i].setAppearance(ap);
			// double[] pos = {(i-3/2.0)*3.0, (j-3/2.0) * 3.0,0};
			archCList[i].setGeometry(archList[i]);
			archPList[i] = archList[i].polarize();
			double[][] polarColorMap = { { .2 + foo + bar * Math.random(),
					foo + bar * Math.random(), foo + bar * Math.random(), 1.0 } };
			archPList[i].setColormap(polarColorMap);
			archPCList[i] = SceneGraphUtility
					.createFullSceneGraphComponent(showAllNames[i] + " polar");
			archPCList[i].setGeometry(archPList[i]);
			archPCList[i].setAppearance(pap);
			// if (i >= 5) {
			pairsList[i] = SceneGraphUtility
					.createFullSceneGraphComponent(showAllNames[i] + " pair");
			pairsList[i].addChild(archCList[i]);
			pairsList[i].addChild(archPCList[i]);
			if (!ct) {
				if (i == 5)
					MatrixBuilder.euclidean().scale(.25).assignTo(pairsList[i]);
				if (i > 5) {
					MatrixBuilder.euclidean().translate(positions[i - 6])
							.scale(.25).assignTo(pairsList[i]);
				}
			} else {
				MatrixBuilder.euclidean().translate(-1 + i + .1, -1, 0)
						.scale(1).assignTo(pairsList[i]);
			}
			// pairsList[i-5].getTransformation().setStretch(.25);
			// if (i>5) {
			// pairsList[i-5].getTransformation().setTranslation(positions[i-6]);
			// }
			DefaultMatrixSupport.getSharedInstance().storeAsDefault(
					pairsList[i].getTransformation());
			// pairsList[i-5].getTransformation().setIsEditable(false);
			if (ct || i>=5) allkit.addChild(pairsList[i]);
			if (i > 5)
				labels[i - 6] = showAllNames[i];
			// }
			labelsSGC = SceneGraphUtility
					.createFullSceneGraphComponent("names");
			Appearance a = labelsSGC.getAppearance();
			a.setAttribute(CommonAttributes.VERTEX_DRAW, true);
			a.setAttribute(CommonAttributes.FACE_DRAW, false);
			a.setAttribute(CommonAttributes.EDGE_DRAW, false);
			a.setAttribute(CommonAttributes.TEXT_SCALE, .002);
			a.setAttribute(CommonAttributes.TEXT_OFFSET, new double[] { .3, 0, 0 });
			a.setAttribute(CommonAttributes.TEXT_ALIGNMENT, SwingConstants.EAST);
			co.setVertexAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY
					.createReadOnly(labels));
			labelsSGC.setGeometry(co);
			labelsSGC.setVisible(showLabels);
			if (!ct)
				allkit.addChild(labelsSGC);
		}
		if (ct) {
			SceneGraphComponent stars = new StarSolids().makeWorld();
			for (int i = 0; i < stars.getChildComponentCount(); ++i) {
				SceneGraphComponent child = new SceneGraphComponent();
				child.addChild(stars.getChildComponent(i));
				MatrixBuilder.euclidean().scale(.8).assignTo(stars.getChildComponent(i));
//				MatrixBuilder.euclidean().translate(-1 + (i) * .1, -1.5, 0)
//						.scale(.5).assignTo(stars.getChildComponent(i));
				child.setTransformation(new Transformation());
				allkit.addChild(child);
			}
			// // make a second set
			// SceneGraphComponent copy = new SceneGraphComponent();
			// MatrixBuilder.euclidean().translate(0,-.1,0).assignTo(copy);
			// copy.addChild(allkit);
			// theWorld.addChild(copy);
		}
//		FirTreeDemo ftd = new FirTreeDemo();
//		holder = new SceneGraphComponent("tree and ornaments");
//		holder.addChildren(theWorld,ftd.makeWorld());
		//theWorld.addChild(ftd.makeWorld());
	}

	public SceneGraphComponent makeWorld() {
		showAll();
		return theWorld;
	}

	public SceneGraphComponent getShapes()	{
		makeWorld();
		return allkit;
	}
	
	Viewer viewer;

	@Override
	public void customize(JMenuBar theMenuBar, Viewer v) {
		viewer = v;
		URL is = ResourceClass.class.getResource("resources/textures/xmasTree-01.png");
		ImageData id = null;
		try {
			id = ImageData.load(new Input(is));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!bkgdImage) {
			final Color URBackground = new Color(.8f, .85f, .68f);
			final Color ULBackground = new Color(1f, .98f, .8f);
			final Color LLBackground = new Color(.1f, .1f, .25f); 
			final Color LRBackground = new Color(0.05f, .15f, .35f); 
			Color[] backgroundArray = new Color[4];
			backgroundArray[0] = URBackground;
			backgroundArray[1] = ULBackground;// bg[1];
			backgroundArray[2] = LLBackground;
			backgroundArray[3] = LRBackground; // bg[2];
			viewer.getSceneRoot().getAppearance().setAttribute(
					"backgroundColors", backgroundArray);
		}
		else {
			TextureUtility.setBackgroundTexture(viewer.getSceneRoot().getAppearance(), id);
		}

		// TextureUtility.createSkyBox(viewer.getSceneRoot().getAppearance(),
		// TextureUtility.getCubeMapImages(rm));
		List<SceneGraphPath> copies = SceneGraphUtility.getPathsBetween(
				viewer.getSceneRoot(), allkit);
		SelectionManager sm =SelectionManagerImpl
				.selectionManagerForViewer(viewer);
		for (int j = 0; j < copies.size(); ++j) {
			SceneGraphPath toP = copies.get(j);
			for (int i = 0; i < allkit.getChildComponentCount(); ++i) {
				toP.push(allkit.getChildComponent(i));
				sm.addSelection(toP);
				System.err.println("Adding path " + toP);
				toP = new SceneGraphPath(toP);
				toP.pop();
			}
		}

		JMenu testM = new JMenu("Solid");
		ButtonGroup bg = new ButtonGroup();
		final String[] gnames = ArchimedeanSolids.getArchimedeanNames();
		for (int i = 0; i < gnames.length; ++i) {
			final int j = i;
			JMenuItem jm = testM.add(new JRadioButtonMenuItem(gnames[i]));
			jm.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					replaceSolid(gnames[j]);
				}
			});
			bg.add(jm);
		}
		JMenuItem jm = testM.add(new JRadioButtonMenuItem("all"));
		jm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAll();
			}
		});
		theMenuBar.add(testM, 0);

		testM = new JMenu("Flags");
		final JCheckBoxMenuItem jcm = new JCheckBoxMenuItem("Show Solids");
		testM.add(jcm);
		jcm.setSelected(showSolids);
		jcm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DiscreteGroupUtility.logger.log(Level.FINE, "In callback");
				showSolids = jcm.isSelected();
				setVisibility();
			}
		});
		final JCheckBoxMenuItem jcn = new JCheckBoxMenuItem("Show Polars");
		testM.add(jcn);
		jcn.setSelected(showPolars);
		jcn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DiscreteGroupUtility.logger.log(Level.FINE, "In callback");
				showPolars = jcn.isSelected();
				setVisibility();
			}
		});
		final JCheckBoxMenuItem jpp = new JCheckBoxMenuItem(
				"Toggle transparent polars");
		testM.add(jpp);
		jpp.setSelected(transpPolars);
		jpp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				transpPolars = jpp.isSelected();
				pap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED,
						transpPolars);
				viewer.render();
			}
		});
		final JCheckBoxMenuItem jpn = new JCheckBoxMenuItem("Show Labels");
		testM.add(jpn);
		jpn.setSelected(showLabels);
		jpn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showLabels = jpn.isSelected();
				labelsSGC.setVisible(showLabels);
				viewer.render();
			}
		});
		theMenuBar.add(testM, 1);
		// CameraUtility.getCameraNode(viewer).getTransformation().setTranslation(0,0,10.0);
		viewer.render();
	}

	public void setVisibility() {
		for (int i = 0; i < archList.length; ++i) {
			archCList[i].setVisible(showSolids);
			archPCList[i].setVisible(showPolars);
		}
		if (viewer != null)
			viewer.render();
	}

	double foo = .1, bar = .4;

	public void replaceSolid(String name) {
		Integer which = (Integer) nameTable.get(name);
		// if it's in the prepared list, use it
		if (which != null) {
			int wh = which.intValue();
			SceneGraphUtility.removeChildren(onekit);
			onekit.addChild(archCList[wh]);
			onekit.addChild(archPCList[wh]);
		} else {
			// signal error
			return;
		}
		allkit.setVisible(false);
		onekit.setVisible(true);
		if (viewer != null)
			viewer.render();
	}

	public void showAll() {
		allkit.setVisible(true);
		onekit.setVisible(false);
		// if (viewer != null) viewer.render();
	}

	public boolean addBackPlane() {
		return true;
	}

	public boolean isEncompass() {
		return true;
	}
}
