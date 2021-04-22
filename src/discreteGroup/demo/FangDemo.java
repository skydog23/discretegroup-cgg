/*
 * Created on Feb 12, 2006
 *
 */
package discreteGroup.demo;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import charlesgunn.jreality.SelectionComponent;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.groups.ArchimedeanSolids;


public class FangDemo extends LoadableScene {
	SceneGraphComponent theWorld, sgc, quadkit;
	SelectionComponent theSelection;
	DiscreteGroupSceneGraphRepresentation theMainRepn;
	double phi = .5 * (-1 + Math.sqrt(5));
	double[][] verts = {
			{0,0,0,1},
			{1,0, 0,1},
			{1, 1-phi, 0,1},
			{1,0,phi,1}
	};
	int[][] indices = {{3,2,1}};
	private SceneGraphComponent honeycombCell;
	private SceneGraphComponent triacontahedronSGC; 
	
	public SceneGraphComponent makeWorld() {
		theWorld =  SceneGraphUtility.createFullSceneGraphComponent("theWorld");
		theSelection = new SelectionComponent();
		theSelection.setName("Selection component");
		theWorld.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
		theWorld.getAppearance().setAttribute(CommonAttributes.BACKEND_RETAIN_GEOMETRY, true);
		theWorld.getAppearance().setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, true);
		theWorld.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		theWorld.getAppearance().setAttribute(CommonAttributes.USE_OLD_TRANSPARENCY, true); 
		theWorld.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, 
CommonAttributes.DIFFUSE_COLOR_DEFAULT);
		theWorld.addChild(theSelection);

		DiscreteGroupElement[] gens = new DiscreteGroupElement[3];
		
//		for (int i = 0; i < 3; ++i)	{
//			double[] plane = P3.planeFromPoints(null, verts[0], verts[i+1], verts[1+((i+2)%3)]);
//			double[] mat = P3.makeReflectionMatrix(null, plane, Pn.EUCLIDEAN);
//			gens[i] = new DiscreteGroupElement(Pn.EUCLIDEAN, mat);
//			gens[i].setWord(DiscreteGroupUtility.genNames[i]);
//		}
		DiscreteGroup tg = new DiscreteGroup();
//		tg.setGenerators(gens);
//		tg.setMetric(Pn.EUCLIDEAN);
//		DiscreteGroupSimpleConstraint triv = new DiscreteGroupSimpleConstraint(200);
//		tg.setElementList(DiscreteGroupUtility.generateElements(tg, triv));
//		tg.setColorPicker(null);
//		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
//		ifsf.setFaceCount(1);
//		ifsf.setVertexCount(4);
//		ifsf.setVertexCoordinates(verts);
//		ifsf.setFaceIndices(indices);
//		ifsf.setGenerateEdgesFromFaces(true);
//		ifsf.setGenerateFaceNormals(true);
//		ifsf.update();
//		tg.setDefaultFundamentalDomain(ifsf.getIndexedFaceSet());
//		
//		//viewer.getSceneRoot().addChild(myroot);
//		sgc = SceneGraphUtility.createFullSceneGraphComponent("ScaledFang");
//		sgc.setGeometry(tg.getDefaultFundamentalRegion());
//		MatrixBuilder.euclidean().translate(.4,.1*phi*phi,.1*phi).scale(.5).assignTo(sgc);
//		sgc.setAppearance(null);
//		honeycombCell = new SceneGraphComponent();
//		honeycombCell.addChild(sgc);
//		theMainRepn = new  DiscreteGroupSceneGraphRepresentation(tg, true, "Fang");
//
//		theMainRepn.setWorldNode(honeycombCell);
//		theMainRepn.setElementList(tg.getElementList());
//		theMainRepn.update();
//		SceneGraphComponent oneTriacontahedron = theMainRepn.getRepresentationRoot();
//		MatrixBuilder.euclidean().scale(1.001).assignTo(oneTriacontahedron.getTransformation());
//		oneTriacontahedron.setAppearance(new Appearance());
//		oneTriacontahedron.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, 0.0);
////		oneTriacontahedron.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);
//		theSelection.addChild(oneTriacontahedron);
		
		// now we create something different and add it to theSelection
		double[][] translations = {
				{2,0,0,1},
				{1, phi,1+phi,1},
				{-1,phi,1+phi,1},
				{phi,1+phi, -1,1}, 
				{-phi,1+phi, -1,1},
				{0,2,0,1}}; //,
//				{-1,(1+phi*phi),-(3*phi*phi),1}};
		gens = new DiscreteGroupElement[2*translations.length];
		for (int i = 0; i<translations.length; ++i)	{
			gens[i] = new DiscreteGroupElement();
			MatrixBuilder.euclidean().translate(translations[i]).assignTo(gens[i].getArray());
			gens[i].setWord(DiscreteGroupUtility.genNames[i]);
			gens[i+translations.length] = (DiscreteGroupElement) gens[i].getInverse();
		}
		tg = new DiscreteGroup();
		tg.setDimension(3);
		tg.setGenerators(gens);
		tg.setMetric(Pn.EUCLIDEAN);
		tg.setConstraint(new DiscreteGroupSimpleConstraint(30));
		DirichletDomain dirdom = new DirichletDomain(tg);
		dirdom.update();
		Geometry shape = dirdom.getDirichletDomain(); //DiscreteGroupUtility.calculateDirichletDomain(null, tg);
		sgc = SceneGraphUtility.createFullSceneGraphComponent();
		MatrixBuilder.euclidean().scale(.995).assignTo(sgc);
		Appearance ap = sgc.getAppearance();
//		ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		ap.setAttribute("polygonShader."+CommonAttributes.TRANSPARENCY, 0.5);
//		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, 0.01);

		honeycombCell = new SceneGraphComponent("honeycomb cell");
		ap = new Appearance("honeycombAp");
		honeycombCell.setAppearance(ap);
		honeycombCell.setGeometry(shape);
		ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(150,150,150));
		ap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(150,150,150));
//		ap.setAttribute(CommonAttributes.FACE_DRAW, false);

		triacontahedronSGC = SceneGraphUtility.createFullSceneGraphComponent("triacontahedron");
		triacontahedronSGC.setName("triacont");
		triacontahedronSGC.setGeometry(ArchimedeanSolids.archimedeanSolid("3.5.3.5").polarize());
		ap = triacontahedronSGC.getAppearance();
		ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(200,150,100));
		MatrixBuilder.euclidean().scale(1.12).rotateY(Math.PI/2).assignTo(triacontahedronSGC);
		sgc.addChild(honeycombCell);
		sgc.addChild(triacontahedronSGC);

		// I have no idea what is going on here
		DiscreteGroupElement[] newgens = new DiscreteGroupElement[8];
	    for (int i = 0; i<4; ++i) 
	    	{newgens[i] = gens[i];  newgens[i+4] = gens[i+translations.length]; }
		MatrixBuilder.euclidean().translate(new double[]{phi,1+phi,-1,1}).assignTo(newgens[3].getArray());
		newgens[7] = (DiscreteGroupElement) tg.getGenerators()[3].getInverse();
		tg.setGenerators(newgens);
		tg.update();

		theMainRepn = new  DiscreteGroupSceneGraphRepresentation(tg, true, "Triacon tessellation");
		theMainRepn.setWorldNode(sgc);
		DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(4.0, 4, 25);
		theMainRepn.setElementList(DiscreteGroupUtility.generateElements(tg, dgsc));
		theMainRepn.update();
		theMainRepn.getRepresentationRoot().setAppearance(new Appearance());
		theMainRepn.getRepresentationRoot().getAppearance().setAttribute("polygonShader.vertexShadername","simple");

		//theWorld.addChild(sgc);
		theSelection.addChild(theMainRepn.getRepresentationRoot());
		theSelection.setSelectedChild(1);
		return theWorld;
	}

	public boolean isEncompass() {
		return true; 
	}

	@Override
	public void customize(JMenuBar menuBar, Viewer viewer) {
		JMenu testM = new JMenu("Actions");
//		JMenuItem jca = new JMenuItem("Cycle selection");
//		testM.add(jca);
//		jca.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0));
//		jca.addActionListener( new ActionListener() {
//			public void actionPerformed(ActionEvent e)	{
//				theSelection.setSelectedChild( (theSelection.getSelectedChild()+1) % 2);
//			}
//		});
		JMenuItem jca = new JMenuItem("Toggle honeycomb visible");
		testM.add(jca);
		jca.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0));
		jca.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				honeycombCell.setVisible(!honeycombCell.isVisible());
			}
		});
		 jca = new JMenuItem("Toggle triacontahedron visible");
			testM.add(jca);
			jca.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0));
			jca.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)	{
					triacontahedronSGC.setVisible(!triacontahedronSGC.isVisible());
				}
			});
		menuBar.add(testM);
		final Color URBackground = new Color(.8f, .85f, .68f); //new Color(215, 215, 190);
		final Color ULBackground  = new Color(1f, .98f, .8f); //new Color(255, 255, 200);  // bg[1];
		final Color LLBackground  = new Color(.1f, .1f, .25f); //new Color(20,20,60);
		final Color LRBackground  = new Color(0.05f, .15f, .35f); //new Color(25, 25, 100);  //bg[2];
		Color[] bg = new Color[4];
		bg[0] = URBackground;
		bg[1] = ULBackground;// bg[1];
		bg[2] = LLBackground;
		bg[3] = LRBackground;  //bg[2];
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColors", bg);
	}
}
