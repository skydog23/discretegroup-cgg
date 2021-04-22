/*
 * Created on Jan 1, 2008
 *
 */
package discreteGroup.demo;

import static de.jreality.shader.CommonAttributes.BACKGROUND_COLOR;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.GeometryCollector;
import charlesgunn.jreality.SelectionComponent;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.texture.RopeTextureFactory;
import charlesgunn.jreality.texture.SimpleTextureFactory;
import charlesgunn.jreality.viewer.GlobalProperties;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.BezierPatchMesh;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.geometry.QuadMeshUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.content.ContentTools;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.Texture2D;
import de.jreality.swing.jrwindows.JRWindow;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.jrworkspace.plugin.simplecontroller.SimpleController.PropertiesMode;

public class SchatzCubeLS extends LoadableScene {

	private SceneGraphComponent world,
		superworld,
		ssw,
		otherStuffSGC, 
		oloidSGC, 
		oloid2SGC,
		linesSGC,
		diagonalSGC,
		schatzGeomSGC,
		tetraGeomSGC, 
		stickGeomSGC,
		regulusGeomSGC,
		hexagonGeomSGC,
		zaxisSGC,
		deltoidSGC,
		genCurveSGC;
	SelectionComponent 		geomSGC = new SelectionComponent();
	IndexedFaceSetFactory deltoidFactory = new IndexedFaceSetFactory();
	IndexedLineSetFactory diagonalFactory = new IndexedLineSetFactory();
	GeometryCollector axisHistorySGC;
	PointRangeFactory axis1Factory;
	enum Geomtype {SCHATZ, TETRA, STICK, REGULUS, HEXAGON};
	SceneGraphPath w2FixedEdge, w2M1, w2M2;
	boolean running = false,
		showXYZ = true,
		showHinges = true,
		showOloid = false,
		showOloid2 = false,
		symmetricParameter = true,
		showDeltoid = false,
		showDiagonal = false,
		showAxisHistory = false;
	double angle = .61548, dt = .01;
	int numberSegments = 2;
	private Timer timer;
	double[][][] cverts = new double[630][2][];
	double phi = Math.sqrt(3.0)/3.0; //(Math.sqrt(5)-1)/2.0;
	// the vertices for one tetrahedron of the Schatz cube
	double[][] schatzverts = {
			{0,0,0,1}, 
			{0,0,2*phi,1},
			{0,2,0,1}, 
			{-2*phi,2,0,1}};
	double[][] verts = {
			{0,0,-2*phi,1}, 
			{0,0,2*phi,1},
			{2*phi,2,0,1}, 
			{-2*phi,2,0,1},
			{0,2,0,1},
			{0,0,0,1}};
	double[] m1 = new double[16], m2 = new double[16], m3 = new double[16];
	boolean fixedPoints = false;
	@Override
	public SceneGraphComponent makeWorld() {
		linesSGC = SceneGraphUtility.createFullSceneGraphComponent("lines");
		superworld = SceneGraphUtility.createFullSceneGraphComponent("container");
		oloidSGC = SceneGraphUtility.createFullSceneGraphComponent("oloid");
		oloid2SGC = SceneGraphUtility.createFullSceneGraphComponent("oloid2");
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		world.getAppearance().setAttribute(SMOOTH_SHADING, false);
		geomSGC.setName("geometry");
		Appearance ap = new Appearance();
		geomSGC.setAppearance(ap);
		ap.setAttribute("lineShader.polygonShader.diffuseColor", Color.white);
		ap.setAttribute("pointShader.polygonShader.diffuseColor", Color.white);
		ap.setAttribute(VERTEX_DRAW, true);
		// set up lines
		PointRangeFactory prf = new PointRangeFactory();
		axis1Factory = prf;
		prf.setElement0(verts[0]);
		prf.setElement1(verts[1]);
		prf.setFiniteSphere(true);
		prf.setSphereRadius(5.0);
		prf.setNumberOfSamples(numberSegments);
		prf.update();
		SceneGraphComponent line1 = new SceneGraphComponent("line1");
		line1.setAppearance(new Appearance());
		line1.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", new Color(255,10,10));
		line1.getAppearance().setAttribute("lineShader.diffuseColor", new Color(255,10,10));
		line1.setGeometry(prf.getLine());
		
		prf = new PointRangeFactory();
		prf.setElement0(verts[2]);
		prf.setElement1(verts[3]);
		prf.setFiniteSphere(true);
		prf.setSphereRadius(5.0);
		prf.setNumberOfSamples(6);
		prf.update();
		SceneGraphComponent line2 = new SceneGraphComponent("line2");
		line2.setAppearance(new Appearance());
		line2.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", new Color(10,10,255));
		line2.getAppearance().setAttribute("lineShader.diffuseColor", new Color(10,10,255));
		line2.setGeometry(prf.getLine());
		linesSGC.addChildren(line1, line2);
		
		DiscreteGroup dg = new DiscreteGroup();
		dg.setMetric(Pn.EUCLIDEAN);
		dg.setDimension(3);
		gens = new DiscreteGroupElement[6];
		for (int i = 0; i<6; ++i)	{
			gens[i] = new DiscreteGroupElement();			
		}
		MatrixBuilder.euclidean().reflect(new double[]{1,0,0,0}).assignTo(gens[5].getArray());
		gens[5].setWord("a");
		MatrixBuilder.euclidean().rotate(Math.PI*2.0/3.0, new double[]{0,0,1}).assignTo(gens[2].getArray());
		gens[2].setWord("b");
		MatrixBuilder.euclidean().rotate(Math.PI*2.0/3.0, new double[]{0,0,1}).reflect(new double[]{1,0,0,0}).assignTo(gens[1].getArray());
		gens[1].setWord("ba");
		MatrixBuilder.euclidean().rotate(Math.PI*4.0/3.0, new double[]{0,0,1}).assignTo(gens[4].getArray());
		gens[4].setWord("bb");
		MatrixBuilder.euclidean().rotate(Math.PI*4.0/3.0, new double[]{0,0,1}).reflect(new double[]{1,0,0,0}).assignTo(gens[3].getArray());
		gens[3].setWord("bba");
		dg.setElementList(gens);
		DiscreteGroupElement[] list = DiscreteGroupUtility.generateElements(dg, null);
		for (int i = 0; i<list.length; ++i)	{
			SceneGraphComponent sgc = new SceneGraphComponent("link"+i);
			sgc.addChild(geomSGC);
			world.addChild(sgc);
			sgc.setTransformation(new Transformation(list[i].getArray()));
		}
		for (int i = 0; i<3; ++i)	{
			SceneGraphComponent sgc = new SceneGraphComponent("hingepair"+i);
			sgc.addChild(linesSGC);
			world.addChild(sgc);
			sgc.setTransformation(new Transformation(list[2*i].getArray()));
			
		}
		timer = new Timer(10,new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setA(angle);
				angle += dt;
				w2FixedEdge.getMatrix(m1);
				double[] im1 = Rn.inverse(null, m1);
				if (fixedPoints) superworld.getTransformation().setMatrix(im1);
			}
			
		});
		
		w2FixedEdge = new SceneGraphPath();
		w2FixedEdge.push(world);
		w2FixedEdge.push(world.getChildComponent(1));
		w2FixedEdge.push(geomSGC);
		w2M1 = new SceneGraphPath();
//		w2M1.push(superworld);
		w2M1.push(world);
		w2M1.push(world.getChildComponent(0));
		w2M1.push(geomSGC);
		w2M2 = new SceneGraphPath();
//		w2M2.push(superworld);
		w2M2.push(world);
		w2M2.push(world.getChildComponent(2));
		w2M2.push(geomSGC);
		superworld.addChild(world);
		ssw = new SceneGraphComponent();
		ssw.addChild(superworld);
		otherStuffSGC = SceneGraphUtility.createFullSceneGraphComponent("accessory");
		otherStuffSGC.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", Color.white);
		otherStuffSGC.getAppearance().setAttribute("lineShader.diffuseColor", Color.white);
//		otherStuffSGC.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
//		SurfaceElement.setDiskRadius(5);
//		SurfaceElement se = new SurfaceElement(new double[]{0,0,0,1}, new double[]{0,0,1,0});
//		SceneGraphComponent sgc = se.getRepresentation();
//		sgc.setName("surfaceElement");
//		sgc.getAppearance().setAttribute(TRANSPARENCY_ENABLED, true);
//		sgc.getAppearance().setAttribute(TRANSPARENCY, .8);
//		otherStuffSGC.addChild(sgc);
		prf = new PointRangeFactory();
		prf.setElement0(new double[]{0,0,0,1});
		prf.setElement1(new double[]{0,0,1,1});
		prf.setFiniteSphere(true);
		prf.setSphereRadius(5.0);
		prf.setNumberOfSamples(6);
		prf.update();
		zaxisSGC = SceneGraphUtility.createFullSceneGraphComponent("zaxis");
		zaxisSGC.setGeometry(prf.getLine());
		
		deltoidSGC = SceneGraphUtility.createFullSceneGraphComponent("deltoid");
		double[][] deltoidVerts = new double[8][];
		for (int i = 0; i<6; ++i)	{
			deltoidVerts[i] = Rn.matrixTimesVector(null, gens[i].getArray(), schatzverts[0]);
		}
		deltoidVerts[6] = deltoidVerts[7] = P3.originP3;
		int[][] deltoidInd = {{6,0,1,2},{6,2,3,4},{6,4,5,0},{7,1,2,3},{7,3,4,5},{7,5,0,1}};
		
		deltoidFactory.setVertexCount(deltoidVerts.length);
		deltoidFactory.setVertexCoordinates(deltoidVerts);
		deltoidFactory.setFaceCount(deltoidInd.length);
		deltoidFactory.setFaceIndices(deltoidInd);
		deltoidFactory.setFaceColors(new Color[]{colors[0], colors[0], colors[0], colors[1], colors[1], colors[1]});
		deltoidFactory.setGenerateEdgesFromFaces(true);
		deltoidFactory.setGenerateFaceNormals(true);
		deltoidFactory.setGenerateVertexNormals(true);
		deltoidFactory.update();
		deltoidSGC.setGeometry(deltoidFactory.getIndexedFaceSet());
		deltoidSGC.setVisible(showDeltoid);
		deltoidFactory.getIndexedFaceSet().setGeometryAttributes(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);

		diagonalSGC = SceneGraphUtility.createFullSceneGraphComponent("diagonal");
		diagonalSGC.getAppearance().setAttribute(LINE_SHADER+"."+
				POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.cyan);
		diagonalFactory = 
				IndexedLineSetUtility.createCurveFactoryFromPoints(
						new double[][]{schatzverts[0],schatzverts[2]}, false);
		diagonalSGC.setGeometry(diagonalFactory.getGeometry());
		diagonalSGC.setVisible(showOloid || showOloid2);

		axisHistorySGC = new GeometryCollector(100);
		axisHistorySGC.setAppearance(new Appearance());
		axisHistorySGC.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
		axisHistorySGC.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
		axisHistorySGC.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
		axisHistorySGC.setVisible(showAxisHistory);
		
		setupGeometry();
		setA(angle);

		otherStuffSGC.addChildren(zaxisSGC, deltoidSGC, diagonalSGC, axisHistorySGC);
		linesSGC.setAppearance(otherStuffSGC.getAppearance());
		world.addChild(otherStuffSGC);
		
		generateOloid();
		return ssw;
	}
	Color[] colors = {Color.red, Color.green, Color.blue, Color.yellow};
	private void setupGeometry() {
		schatzGeomSGC = SceneGraphUtility.createFullSceneGraphComponent("schatzCubeGeom");
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		int[][] indices  = {{0,1,2},{0,2,3},{0,3,1},{1,2,3}};
		ifsf.setVertexCount(schatzverts.length);
		ifsf.setVertexCoordinates(schatzverts);
		ifsf.setFaceCount(indices.length);
		ifsf.setFaceIndices(indices);
		ifsf.setFaceColors(colors);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setGenerateVertexNormals(true);
		ifsf.update();
		schatzGeomSGC.setGeometry(ifsf.getIndexedFaceSet());
		
		tetraGeomSGC = SceneGraphUtility.createFullSceneGraphComponent("tetraGeom");
		ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(verts.length);
		ifsf.setVertexCoordinates(verts);
		ifsf.setFaceCount(indices.length);
		ifsf.setFaceIndices(indices);
		ifsf.setFaceColors(colors);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setGenerateVertexNormals(true);
		ifsf.update();
		tetraGeomSGC.setGeometry(ifsf.getIndexedFaceSet());
		
		stickGeomSGC = SceneGraphUtility.createFullSceneGraphComponent("stickGeom");
		stickGeomSGC.getAppearance().setAttribute(LINE_SHADER+"."+
				POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.yellow);
		stickGeomSGC.setGeometry(
				IndexedLineSetUtility.createCurveFromPoints(
						new double[][]{schatzverts[0],schatzverts[2]}, false));
		
		regulusGeomSGC = SceneGraphUtility.createFullSceneGraphComponent("regulusGeom");
		Appearance ap = regulusGeomSGC.getAppearance();
		ap.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.white);
		ap.setAttribute(VERTEX_DRAW, false);
		ap.setAttribute(EDGE_DRAW, false);
		ap.setAttribute(LINE_SHADER+"."+TUBES_DRAW, false);
		RopeTextureFactory rtf = new RopeTextureFactory(ap);
		rtf.update();
		Texture2D tex2d = rtf.getTexture2D();
		Matrix foo = new Matrix();
		MatrixBuilder.euclidean().scale(8,10,1).assignTo(foo);
		tex2d.setTextureMatrix(foo);
		BezierPatchMesh thing = new BezierPatchMesh(1, 1, new double[][][]{{verts[0],verts[1]},{verts[2],verts[3]}});
		for (int i = 0; i<6; ++i) thing.refine();
		IndexedFaceSet ifs = BezierPatchMesh.representBezierPatchMeshAsQuadMesh(thing);
		QuadMeshUtility.generateAndSetEdgesFromQuadMesh(ifs);
		regulusGeomSGC.setGeometry(ifs);

		hexagonGeomSGC = SceneGraphUtility.createFullSceneGraphComponent("hexagonGeom");
		int[][] hexInd = {{0,1,4},{2,3,5}};
		ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(verts.length);
		ifsf.setVertexCoordinates(verts);
		ifsf.setFaceCount(hexInd.length);
		ifsf.setFaceIndices(hexInd);
		ifsf.setFaceColors(new Color[]{colors[0], colors[1]});
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setGenerateVertexNormals(true);
		ifsf.update();
		hexagonGeomSGC.setGeometry(ifsf.getIndexedFaceSet());
		
		
		geomSGC.addChildren(stickGeomSGC, 
				schatzGeomSGC, 
				tetraGeomSGC, 
				hexagonGeomSGC, 
				regulusGeomSGC );
		geomSGC.setSelectedChild(1);

		
	}
	private void setA(double unsymmetricA)	{
		double a = unsymmetricA;
		if (symmetricParameter) a = a-.5*Math.asin(Math.sin(2*a)/3.0);
		double s = Math.sin(a),
			c = Math.cos(a),
			k = Math.sqrt(1+3*s*s), 
			k1 = Math.sqrt(3.0);
			
		double[] m = {
				k1*s, 1, 0, 0,
				-s, k1* s * s, k * c, -2*k1*k*k/3,
				c, -k1*c*s, k * s, k1*c*s,
				0,0,0,k};
		Rn.times(m, 1.0/k, m);
		Matrix mm = new Matrix(m);
		mm.assignTo(geomSGC);
		mm.assignTo(linesSGC);
		// update the deltoid geometry
		double[][] deltoidVerts = new double[8][];
		double[] vert0 = Rn.matrixTimesVector(null, m, schatzverts[0]);
		double[] vert1 = Rn.matrixTimesVector(null, m, schatzverts[2]);
		for (int i = 0; i<3; ++i)	{
			deltoidVerts[2*i] = Rn.matrixTimesVector(null, gens[2*i].getArray(), vert0);
			deltoidVerts[2*i+1] = Rn.matrixTimesVector(null, gens[2*i].getArray(), vert1);
		}
		double[][] dv = deltoidVerts;
		double[] plane1 = P3.planeFromPoints(null, dv[0], dv[1], dv[2]);
		deltoidVerts[6] = P3.lineIntersectPlane(null, P3.originP3, new double[]{0,0,1,0}, plane1);
		plane1 = P3.planeFromPoints(null, dv[5], dv[0], dv[1]);
		deltoidVerts[7] = P3.lineIntersectPlane(null, P3.originP3, new double[]{0,0,1,0}, plane1);
		deltoidFactory.setVertexCoordinates(deltoidVerts);
		deltoidFactory.update();
		
		double[][] diagverts = new double[2][];
		diagverts[0] = deltoidVerts[0];
		diagverts[1] = Rn.matrixTimesVector(null, gens[2].getArray(), deltoidVerts[1]);
		diagonalFactory.setVertexAttribute(Attribute.COORDINATES, diagverts);
		diagonalFactory.update();
		
		double[][] peep = axis1Factory.getLine().getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		peep = Rn.matrixTimesVector(null, m, peep);
		IndexedLineSet ils = IndexedLineSetUtility.createCurveFromPoints(peep, false);
		if (count % 10 == 0) axisHistorySGC.addGeometry(ils);
		count++;
	}
	int count = 0;
	@Override
	public boolean isEncompass() {
		return true;
	}
	public boolean hasInspector() {return true; }
	public Component getInspector(final Viewer viewer) {

		
		Box container = Box.createVerticalBox();
		final TextSlider aSlider = new TextSlider.Double("angle",  SwingConstants.HORIZONTAL, 0.0, Math.PI*2, 0);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				setA(aSlider.getValue().doubleValue());
				//viewer.renderAsync();
			}
		});
		container.add(aSlider);
		container.add(Box.createVerticalGlue());
		runningCB = new JCheckBox("stop/start animation");
		runningCB.setSelected(running);
		runningCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				running = !running;
				if (running) timer.start();
				else timer.stop();
			}	
		});
		container.add(runningCB);
		
		fixCoordCB = new JCheckBox("fix link");
		fixCoordCB.setSelected(fixedPoints);
		fixCoordCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fixedPoints = fixCoordCB.isSelected();
				if (fixedPoints) superworld.getTransformation().setMatrix(w2FixedEdge.getInverseMatrix(null));
				else superworld.getTransformation().setMatrix(Rn.identityMatrix(4));
			}	
		});
		container.add(fixCoordCB);

		symmetricCB = new JCheckBox("use symmetric parametrization");
		symmetricCB.setSelected(symmetricParameter);
		symmetricCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				symmetricParameter = symmetricCB.isSelected();
			}	
		});
		container.add(symmetricCB);

		Box vbox = Box.createVerticalBox();
		TitledBorder title = BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Visibility");
		vbox.setBorder(title);
		showXYZCB = new JCheckBox("show coordinate system");
		showXYZCB.setSelected(showXYZ);
		showXYZCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showXYZ = showXYZCB.isSelected();
				zaxisSGC.setVisible(showXYZ);
			}	
		});
		vbox.add(showXYZCB);
		
		showHingesCB = new JCheckBox("show hinge lines");
		showHingesCB.setSelected(showHinges);
		showHingesCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showHinges = showHingesCB.isSelected();
				linesSGC.setVisible(showHinges);
			}	
		});
		vbox.add(showHingesCB);
		 
		// the following is a nice idea but the accelerator key seems to
		// have no effect since I don't attach the action to a menu item, 
		// but to a check box.  And, there's also the question of keyboard focus.
		AbstractAction showOloidAction = new AbstractAction("show oloid") {
			
			public void actionPerformed(ActionEvent e) {
				showOloid = showOloidCB.isSelected();
				oloidSGC.setVisible(showOloid);
				diagonalSGC.setVisible(showOloid || showOloid2);
			}
			
		};
		showOloidAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
		        KeyEvent.VK_4, 0));
		showOloidCB = new JCheckBox(showOloidAction);
		showOloidCB.setSelected(showOloid);
		vbox.add(showOloidCB);
		
		showOloid2CB = new JCheckBox("show oloid #2");
		showOloid2CB.setSelected(showOloid2);
		showOloid2CB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showOloid2 = showOloid2CB.isSelected();
				oloid2SGC.setVisible(showOloid2);
				diagonalSGC.setVisible(showOloid || showOloid2);
		}	
		});
		vbox.add(showOloid2CB);
		
		showDeltoidCB = new JCheckBox("show deltoid");
		showDeltoidCB.setSelected(showDeltoid);
		showDeltoidCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showDeltoid = showDeltoidCB.isSelected();
				deltoidSGC.setVisible(showDeltoid);
			}	
		});
		vbox.add(showDeltoidCB);
		container.add(vbox);
		
		vbox = Box.createVerticalBox();
		title = BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Geometry selection");
		vbox.setBorder(title);
		String[] geomNames = { "Stick", "Schatz cube","Maximal tetrahedron", "Hexagon","Regulus"};
		for (int i = 0; i<geomNames.length; ++i)	{
			geomB[i] = new JRadioButton(geomNames[i]);
			final int j = i;
			geomB[i].addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					geomSGC.setSelectedChild(j);
				}
				
			});
			bg.add(geomB[i]);
			vbox.add(geomB[i]);
		}
		geomB[1].setSelected(true);
		container.add(vbox);
		
		JPanel panel = new JPanel();
		panel. setName("Parameters");
		panel.add(container);
		return panel;
	}
	JCheckBox runningCB, 
		showXYZCB, 
		showHingesCB, 
		showOloidCB, 
		showOloid2CB, 
		fixCoordCB,
		symmetricCB,
		showDeltoidCB;
	JRadioButton[] geomB = new JRadioButton[5];
	ButtonGroup bg = new ButtonGroup();
	
	@Override
	public void customize(JMenuBar menuBar, PluginSceneLoader psl) {
		JRViewer v = psl.getJRViewer();
		v.getController().setPropertiesMode(PropertiesMode.StaticPropertiesFile);
		v.getController().setStaticPropertiesFile(new File("src/discreteGroup/demo/schatzCubeVR.xml"));
//		psl.getJRViewer().setPropertiesFile("schatzCubeVR.xml");
		Viewer viewer = psl.getViewer();
		viewer.getSceneRoot().getAppearance().setAttribute(BACKGROUND_COLOR, new Color(0,0,60));
		setBGC(viewer);
		attachKeyListener(viewer);

	}
	private void setBGC(Viewer viewer) {
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
	}
	private void attachKeyListener(Viewer viewer) {
		((Component) viewer.getViewingComponent()).addKeyListener( new KeyAdapter()	{
			int which = 0;
			boolean drag = false;
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode())	{
				
				case KeyEvent.VK_H:
					System.err.println(" 1: toggle animate");
					break;
				case KeyEvent.VK_1:
					running = !running;
					if (running) timer.start();
					else timer.stop();
					break;
				case KeyEvent.VK_2:
					otherStuffSGC.setVisible(!otherStuffSGC.isVisible());
					break;
				case KeyEvent.VK_3:
					linesSGC.setVisible(!linesSGC.isVisible());
					break;
				case KeyEvent.VK_4:
					oloidSGC.setVisible(!oloidSGC.isVisible());
					break;
				case KeyEvent.VK_5:
					oloid2SGC.setVisible(!oloid2SGC.isVisible());
					break;
				case KeyEvent.VK_6:
					fixedPoints = !fixedPoints;
					if (fixedPoints) superworld.getTransformation().setMatrix(w2FixedEdge.getInverseMatrix(null));
					else superworld.getTransformation().setMatrix(Rn.identityMatrix(4));
					break;
				case KeyEvent.VK_7:
					int selected = geomSGC.getSelectedChild();
					selected = (selected+1)%5;
					geomSGC.setSelectedChild(selected);
					geomB[selected].setSelected(true);
					break;
				}
			}	
			
		});
	}

	static JRWindow win;
	static JTabbedPane tabs = new JTabbedPane();

	boolean doVR = false;
	static SchatzCubeLS schatzcube = new SchatzCubeLS();
	private DiscreteGroupElement[] gens;
//	public void doViewerThing() {
//			JRViewer v = new JRViewer();
//			v.getController().setPropertiesMode(PropertiesMode.StaticPropertiesFile);
//			v.getController().setStaticPropertiesFile(new File("src/discreteGroup/demo/schatzCubeVR.xml"));
//			v.addBasicUI();
//			if (doVR) {
//				v.addVRSupport();
//				v.addContentSupport(ContentType.TerrainAligned);
//			} else {
//				v.addContentSupport(ContentType.Raw);
//			}
//			v.setContent(schatzcube.makeWorld());
//			v.registerPlugin(new ContentTools());
//			JTabbedPane tb = new JTabbedPane();
//			schatzcube.insertTabs(tb);
//			v.registerPlugin(JRViewer.createSceneShrinkPanel(tb, "SchatzCube"));
////			v.setPropertiesFile("schatzCubeVR.xml");
//			v.startup();
//			Viewer viewer = v.getPlugin(View.class).getViewer().getCurrentViewer();
//			attachKeyListener(viewer);
//			setBGC(viewer);
//
//	}

	private void insertTabs(JTabbedPane tabs2) {
		Component insp = getInspector(null);
		tabs2.addTab("Parameters", insp);
		tabs2.addTab("ReadMe", getReadMePanel());
	}
	public static void main(String[] args) {
		Secure.setProperty(SystemProperties.VIEWER,GlobalProperties.DEFAULT_VIEWER); 
		Secure.setProperty("doOwnTools", "false"); 
//		schatzcube.doViewerThing();
	}
	
	public Component getReadMePanel()	{
		JPanel mypanel = new JPanel();
		mypanel.setName("ReadMe");
		JTextArea textarea = new JTextArea(10,30);
		textarea.setEditable(false);
		textarea.append("Enjoy the fascinating motion\n" +
				"of the 6-linkage ring, the Schatz cube\n" +
				"(named for its discoverer Paul Schatz.\n" +
				"\nThe following keys are active:\n" +
				"    1: toggle animated motion\n" +
				"    2: toggle xyz display\n" +
				"    3: toggle display of hinge lines\n" +
				"    4: toggle display of first oloid\n" +
				"    5: toggle display of second oloid\n" +
				"    6: fix the first oloid\n" +
				"    7: cycle the geometry\n\n" +
				"'h' shows a help overlay for viewer\n" +
				"'e' encompasses the scene\n" +
				"\nAuthor: Charles Gunn\n"+
				"    gunn at math.tu-berlin.de\n");
		mypanel.add(textarea);
		return mypanel;
	}

	private void generateOloid()	{
		for (int count = 0; count < cverts.length; ++count)	{
			setA(angle);
			angle += dt;
			w2FixedEdge.getMatrix(m1);
			double[] im1 = Rn.inverse(null, m1);
			w2M1.getMatrix(m2);
			m2 = Rn.times(null, im1, m2);
			double[][]  tverts1 = Rn.matrixTimesVector(null, m2, schatzverts);
			w2M2.getMatrix(m3);
			m3 = Rn.times(null, im1, m3);
			double[][] tverts3 = Rn.matrixTimesVector(null, m3, schatzverts);
			cverts[count][0] = tverts1[0];
			cverts[count][1] = tverts3[2];			
		}
		System.err.println("Oloid verts = \n"+Rn.toString(cverts));
		QuadMeshFactory qmf = new QuadMeshFactory();
		qmf.setClosedInUDirection(false);
		qmf.setClosedInVDirection(false);
		qmf.setULineCount(2);
		qmf.setVLineCount(cverts.length);
		qmf.setVertexCoordinates(cverts);
		qmf.setGenerateFaceNormals(true);
		qmf.setGenerateVertexNormals(true);
		qmf.setGenerateTextureCoordinates(true);
		qmf.update();
		SceneGraphComponent suboloid = SceneGraphUtility
				.createFullSceneGraphComponent("suboloid");
		Appearance ap1 = suboloid.getAppearance();
		Texture2D tex2d = null;
		tex2d = (Texture2D) AttributeEntityUtility.createAttributeEntity(
				Texture2D.class, "polygonShader.texture2d", ap1, true);
		SimpleTextureFactory stf = new SimpleTextureFactory();
		stf.setType(SimpleTextureFactory.TextureType.GRAPH_PAPER);
		stf.setColor(0, new Color(255, 255, 255, 0));
		stf.update();
		tex2d.setImage(stf.getImageData());
		Matrix m = new Matrix();
		MatrixBuilder.euclidean().scale(5, 25, 1).assignTo(m);
		tex2d.setTextureMatrix(m);
		ap1.setAttribute("polygonShader.diffuseColor", Color.white);
		ap1.setAttribute(VERTEX_DRAW, false);
		suboloid.setGeometry(qmf.getQuadMesh());
		suboloid.setTransformation(geomSGC.getTransformation());
		oloidSGC.addChild(suboloid);
		oloidSGC.setTransformation(world.getChildComponent(1)
				.getTransformation());
		oloidSGC.setVisible(showOloid);
		world.addChild(oloidSGC);
		oloid2SGC.addChild(suboloid);
		oloid2SGC.setTransformation(world.getChildComponent(4)
				.getTransformation());
		oloid2SGC.setVisible(showOloid2);
		world.addChild(oloid2SGC);
		genCurveSGC = SceneGraphUtility.createFullSceneGraphComponent("generatingCurve");
//		suboloid.addChild(genCurveSGC);
		genCurveSGC.setGeometry(getGeneratingCurve(40, true));
		// The full generating curve is created by the symmetry group 2*2 of order 8 
		// generated by an order-2 rotation around a line angled at
		// 45 degrees to the x-axis, and a reflection in the xz plane
		DiscreteGroup symm = new DiscreteGroup();
		DiscreteGroupElement gens[] = new DiscreteGroupElement[2];
		double[] refl = P3.makeReflectionMatrix(null, new double[]{0,1,0,0}, Pn.EUCLIDEAN);
		gens[0] = new DiscreteGroupElement(Pn.EUCLIDEAN, refl, "a");
		double[] rot = P3.makeRotationMatrix(null, new double[]{-.5,0,0,1}, new double[]{-.5,.5,.5,1}, Math.PI, Pn.EUCLIDEAN);
		gens[1] = new DiscreteGroupElement(Pn.EUCLIDEAN, rot, "b");
		symm.setGenerators(gens);
		symm.setFinite(true);
		symm.update();
		DiscreteGroupSceneGraphRepresentation dgsgr = new DiscreteGroupSceneGraphRepresentation(symm);
		dgsgr.setWorldNode(genCurveSGC);
		dgsgr.update();
		// following matrix is needed to convert from mathematica notebook coordinate system
		// to the coordinate system used here
		double[] coa = {
				0,0,-2,0,
				2,0,0,2,
				0,2,0,0,
				0,0,0,1
		};
		
		new Matrix(coa).assignTo(dgsgr.getRepresentationRoot());
		suboloid.addChild(dgsgr.getRepresentationRoot());
	}
	
	private Geometry getGeneratingCurve(int n, boolean qm) {
		double[][][] pts = new double[2][n][];
		for (int i = 0; i<n; ++i)	{
			double t = AnimationUtility.linearInterpolation(i, 0, n-1,0.0	, Math.PI/2.0);
			// following code came from Mathematica and gives homogeneous coordinates 
			// for one eighth of the generating curve
			pts[0][i] = new double[]{Math.cos(t), Math.sin(t), 0, 1};

//			pts[0][i] = new double[]{-1, 0, Math.sqrt(1 + 2*Math.cos(t)), 1+Math.cos(t)};

			pts[1][i] = new double[]{
					   (-1)*(-4 - 9*Math.cos(t) + Math.cos(3*t))/(4.*Math.sqrt(1 + 2*Math.cos(t))),
					   Math.pow(Math.sin(t),3)/Math.sqrt(1 + 2*Math.cos(t)),
					   (-1 - 2*Math.cos(t)),
					   (-6)*Math.pow(Math.cos(t/2.),2)*Math.cos(t)/Math.sqrt(1 + 2*Math.cos(t))};
		}
		Rn.times(pts[1], -1, pts[1]);
//		Pn.dehomogenize(pts, pts);
//		System.err.println("curve = \n"+Rn.toString(pts));
		if (!qm) return IndexedLineSetUtility.createCurveFromPoints(pts[1], false);
		QuadMeshFactory qmf = new QuadMeshFactory();
		qmf.setULineCount(n);
		qmf.setVLineCount(2);
		qmf.setVertexCoordinates(pts);
		qmf.setGenerateEdgesFromFaces(true);
		qmf.setClosedInUDirection(false);
		qmf.setClosedInVDirection(false);
		qmf.setGenerateFaceNormals(true);
		qmf.update();
		return qmf.getIndexedFaceSet();
	}

}
