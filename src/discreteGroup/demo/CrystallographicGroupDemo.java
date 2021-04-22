/*
 * Created on Mar 16, 2004
 *
 */
package discreteGroup.demo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import charlesgunn.jreality.CameraUtilityOverflow;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.Graphics3D;
import de.jreality.ui.viewerapp.SelectionManagerImpl;
import de.jreality.util.CameraUtility;
import de.jreality.util.DefaultMatrixSupport;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupCameraFollower;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.core.DiscreteGroupViewportConstraint;
import de.jtem.discretegroup.groups.CrystallographicGroup;
import de.jtem.discretegroup.groups.WallpaperGroup;
import de.jtem.discretegroup.util.WingedEdge;

/**
 * @author gunn
 *
 */
public class CrystallographicGroupDemo extends LoadableScene {

	//CrystallographicGroup tg ;
	WingedEdge standardDD;
	DiscreteGroup tg;
	static SceneGraphComponent elkit, cubekit;
	SceneGraphComponent theWorld;
	SceneGraphComponent cpkit;
	Geometry vs;
	SceneGraphPath dgPath = null;
	DiscreteGroupCameraFollower trapper;
	static double[][] el = {{0,0,0},{1,0,0},{1,.2,0},{.2,.2,0},{.2,1.618,0},{0,1.618,0}};
	Viewer viewer;
	static {
		elkit = DiscreteGroupUtility.getElKit();
		cubekit = new SceneGraphComponent();
		cubekit.setName("Cube kit");
		cubekit.setTransformation(new Transformation());
		MatrixBuilder.euclidean().translate(.15,.15,.15).scale(.15).assignTo(cubekit);
//		cubekit.getTransformation().setStretch(.15);
//		cubekit.getTransformation().setTranslation(.15,.15,.5);
		IndexedFaceSet foo = Primitives.cube();
		cubekit.setGeometry(foo);
	}
	/**
	 * 
	 */
	public CrystallographicGroupDemo() {
		super();
		cpkit = new SceneGraphComponent();
		//DataGrid points = new DataGrid(cpd, false);
		vs = new PointSet(1);
		// TODO set the point in vs
		cpkit.setGeometry(vs);
		cpkit.setName("Center point");
	}
	boolean showDirichletDomain = true,
		showEl = false,
		showCube = false,
		automate = false,
		showCenterPoint = false;
	
	@Override
	public void customize(JMenuBar theMenuBar, Viewer viewer) {
		this.viewer=viewer;
		replaceGroup("O");
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
		JMenuItem jm ;
//		JMenuItem jm = testM.add(new JRadioButtonMenuItem("WeissmanDG"));
//		jm.addActionListener( new ActionListener() {
//			public void actionPerformed(ActionEvent e)	{
//				replaceWeissman();
//				viewer.getViewingComponent().requestFocus();
//			}
//
//		});
//		bg.add(jm);
		theMenuBar.add(testM);
		testM = new JMenu("Geometry");
		bg = new ButtonGroup();
		jm = testM.add(new JRadioButtonMenuItem("Standard"));
		jm.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showDirichletDomain = false;
				updateGeometry();
			}
		});
		bg.add(jm);
		bg.setSelected(jm.getModel(),true);
		jm = testM.add(new JRadioButtonMenuItem("Dirichlet"));
		jm.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showDirichletDomain = true;
				updateGeometry();
			}
		});
		bg.add(jm);
		
		final JCheckBoxMenuItem jcm = new JCheckBoxMenuItem("Show L");
		jcm.setSelected(showEl);
		testM.add(jcm);
		jcm.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showEl = jcm.isSelected();
				updateGeometry();
			}
		});
		final JCheckBoxMenuItem jcu = new JCheckBoxMenuItem("Show Cube");
		jcu.setSelected(showCube);
		testM.add(jcu);
		jcu.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showCube = jcu.isSelected();
				System.out.println("ShowCube is "+showCube);
				updateGeometry();
			}
		});
		final JCheckBoxMenuItem jcc = new JCheckBoxMenuItem("Show Center Point");
		jcc.setSelected(showCenterPoint);
		testM.add(jcc);
		jcc.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showCenterPoint = jcc.isSelected();
				updateGeometry();
			}
		});
		final JCheckBoxMenuItem jca = new JCheckBoxMenuItem("Automate");
		jca.setSelected(automate);
		testM.add(jca);
		jca.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				automate = jca.isSelected();
				updateGeometry();
			}
		});
		theMenuBar.add(testM);
	}
		
//	private void replaceWeissman() {
//		tg = new CrystallographicGroup();
//		DiscreteGroupElement[] gens = GroupGeneratorFactory.generateQuaterGroup(1,0);
//		tg.setDirichletDomainOrbit(150);
//		double[] cp = {0.001, 0.001, 0.001,1.0};
//		tg.setCenterPoint(cp);
//		tg.setGenerators(gens);
//		sharedCode();
//	}
	
	DiscreteGroupSceneGraphRepresentation sgr = null;
	public void replaceGroup(String name)	{
		tg = CrystallographicGroup.instanceOfGroup(name);
		DirichletDomain dirdom = new DirichletDomain(tg);
		dirdom.update();
		standardDD = (WingedEdge) dirdom.getDirichletDomain(); //DiscreteGroupUtility.calculateDirichletDomain(null, tg);
		tg.setCenterPoint(P3.originP3);
		if (sgn != null) theWorld.removeChild(sgn);
		if (sgr != null)	{
			sgr.dispose();
		}
		sgr = new DiscreteGroupSceneGraphRepresentation(tg);
		sgr.update();
//		sgn =  sgr.getRepresentationRoot();
//		theWorld.addChild(sgn);
		if (!viewer.getSceneRoot().isDirectAncestor(theWorld)) viewer.getSceneRoot().addChild(theWorld);
//		List l = SceneGraphUtility.getPathsBetween(viewer.getSceneRoot(), sgn);
		dgPath = new SceneGraphPath(viewer.getSceneRoot(), theWorld);
		Graphics3D gc = new Graphics3D(viewer, dgPath);
		DiscreteGroupViewportConstraint vc = new DiscreteGroupViewportConstraint(2d, 2,8.0, 8, gc);
		tg.setConstraint(vc);
//		sgr.setContext(gc);
		sgr.attachToViewer(viewer, dgPath, !tg.isFinite(), 200, true, 200);
//		if (!tg.isFinite()) sgr.followCamera( standardDD, 200, true);
		DiscreteGroupUtility.generateElements(tg, null);
//		sgr.clipToCamera(vc, 200, true);
		List l = SceneGraphUtility.getPathsBetween(viewer.getSceneRoot(), theWorld);
		SceneGraphPath ds = (SceneGraphPath) l.get(0);
		SelectionManagerImpl.selectionManagerForViewer(viewer).setDefaultSelectionPath(ds);
		SelectionManagerImpl.selectionManagerForViewer(viewer).setSelectionPath(ds);
		SceneGraphUtility.setMetric(CameraUtility.getCameraNode(viewer),tg.getMetric());
		DefaultMatrixSupport.getSharedInstance().restoreDefault(CameraUtility.getCameraNode(viewer).getTransformation(), true);
//		CameraUtility.getCamera(viewer).setMetric(tg.getMetric());
//		CameraUtility.getCamera(viewer).reset();
		CameraUtilityOverflow.reset(CameraUtility.getCamera(viewer), getMetric());

		//tg.update();
		updateGeometry();
	}
	SceneGraphComponent sgn, scaledDD;
	Vector geom;

	public void updateGeometry()	{
		geom.clear();
		if (showEl) geom.add(elkit);
		if (showCube) geom.add(cubekit);
		if (scaledDD == null)	{
			scaledDD = new SceneGraphComponent();
			scaledDD.setTransformation(new Transformation(P3.makeStretchMatrix(null,.3)));
//			scaledDD.getTransformation().setStretch(.3);
		}
//		scaledDD.getTransformation().setCenter(tg.getCenterPoint());
		DirichletDomain dirdom = new DirichletDomain(tg);
		dirdom.update();
		scaledDD.setGeometry( dirdom.getDirichletDomain());
		//DiscreteGroupUtility.calculateDirichletDomain(null, tg));
		if (showDirichletDomain) geom.add(scaledDD);
		//if (showCenterPoint)	{
			//vs.getVertices().setVectorAt(0, tg.getCenterPoint());
			//vs.broadcastChange();
			//geom.add(cpkit);
		//}
		if (sgn != null && theWorld.isDirectAncestor(sgn)) theWorld.removeChild(sgn);
		sgr.setWorldNode( DiscreteGroupUtility.collectGeometry(geom, null));
		sgr.update();
		sgn = sgr.getRepresentationRoot();
		theWorld.addChild( sgn);
	}

	
	public SceneGraphComponent makeWorld(){
		theWorld = new SceneGraphComponent();
		theWorld.setTransformation(new Transformation());
		geom = new Vector();
		return theWorld;
	}
	int oldCount = 0;
	double angle;
	
	public boolean isEncompass() {return false;}
	
	public void update()	{
		//if (true) return true;
		if (automate && showDirichletDomain)	{
			angle += Math.PI/93.0;
			double[] cp = { .125*Math.sin( angle),0d,.25*Math.cos(angle),1d};
			tg.setCenterPoint(cp);
			updateGeometry();
			viewer.render();
			//System.err.println("Updating");
		}
	}
	


}
