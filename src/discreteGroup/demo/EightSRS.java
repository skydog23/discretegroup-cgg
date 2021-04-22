/*
 * Created on Jun 8, 2012
 *
 */
package discreteGroup.demo;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.FOG_COLOR;
import static de.jreality.shader.CommonAttributes.FOG_DENSITY;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.METRIC;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.TEXT_SCALE;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.ImplodePolygonShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.tutorial.util.FlyTool;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupColorPicker;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.util.WingedEdge;

public class EightSRS extends Assignment {

	boolean tetraGen = false,
			coloredFace = true,
			showFD = true,
			drawHalf = false,
			drawVerts = false,
			doFog = false,
			testExtraGen = false;
	double scale = 1.0,
			implodeFactor = .15;
	Timer rotate = null;
	
	SceneGraphComponent 
	world,
		markedCube,
		animTform,
			cube,
			truncOct,
			// DGSGR contains
			willie1,
			willie2;
	SceneGraphComponent child10 = new SceneGraphComponent("child10"),
			child11 = new SceneGraphComponent("child11");
	Geometry triangleFace, dirichletDom;
	protected Appearance rootAp;
	protected Appearance[] appList;
	int count = 8, maxCount = 100;
	private DiscreteGroupSceneGraphRepresentation oneTetra1,
		oneTetra2,
		dgsgrTlate;

	public SceneGraphComponent getContent()	{
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		animTform = SceneGraphUtility.createFullSceneGraphComponent("anim tform");
		willie1 = SceneGraphUtility.createFullSceneGraphComponent("willie1");
		willie2 = SceneGraphUtility.createFullSceneGraphComponent("willie2");
		willie2.setAppearance(willie1.getAppearance());
		cube = SceneGraphUtility.createFullSceneGraphComponent("cube");
		markedCube = SceneGraphUtility.createFullSceneGraphComponent("marked cube");
		truncOct = SceneGraphUtility.createFullSceneGraphComponent("truncOct");

		updateAppearance();

		IndexedFaceSet cube4 = Primitives.cube4(false);
		markedCube.setGeometry(cube4);
		// have to make sure the labels match the vertices in cube4
		String[] labels = {"+++","++-","+-+","+--","-++","-+-","--+","---"};
		cube4.setVertexAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labels));
		Appearance ap = markedCube.getAppearance();
		ap.setAttribute(FACE_DRAW, false);
		ap.setAttribute(VERTEX_DRAW, true);
		ap.setAttribute(TUBES_DRAW, false);
		ap.setAttribute(LINE_SHADER+"."+DIFFUSE_COLOR, new Color(50,50,50));
		ap.setAttribute(LINE_SHADER+"."+CommonAttributes.LINE_WIDTH, 4.0);
	    DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(ap, false);
	    DefaultTextShader pts = (DefaultTextShader) ((DefaultPointShader)dgs.getPointShader()).getTextShader();
	    pts.setDiffuseColor(Color.black);
	    pts.setScale(.003);;
	    pts.setAlignment(SwingConstants.CENTER);
	    world.addChild(markedCube);	    
	    
		cube.setGeometry(cube4);
		ap = cube.getAppearance();
		ap.setAttribute(FACE_DRAW, false);
		ap.setAttribute(TUBES_DRAW, false);
		ap.setAttribute(LINE_SHADER+"."+DIFFUSE_COLOR, new Color(255,200,50));
		ap.setAttribute(LINE_SHADER+"."+CommonAttributes.LINE_WIDTH, 2.0);
		
		WingedEdge truncOctWE = getTruncatedOctahedron();
		truncOct.setGeometry(truncOctWE); //ifsfTruncOct.getGeometry());

		appList = getAppList();

		oneTetra1 = constructHalf(0, willie1);
		oneTetra2 = constructHalf(1, willie2);
		constructTlate();
		updateCount();
		ap = world.getAppearance();
		dgs = (DefaultGeometryShader) 
	   			ShaderUtility.createDefaultGeometryShader(ap, true);
		ImplodePolygonShader dps = (ImplodePolygonShader) dgs.createPolygonShader("implode");

//		ap.setAttribute("lineShader.polygonShader.ambientCoefficient", .1);
		world.addChild(dgsgrTlate.getRepresentationRoot());
//		MatrixBuilder.euclidean().
//			translate(new double[]{0,0,-5}).assignTo(world);
		return world;
	}


	private void constructTlate() {
		// TODO Auto-generated method stub
		DiscreteGroup tlate3d = new DiscreteGroup();
		tlate3d.setName("tlate");
		tlate3d.setFinite(false);
		tlate3d.setDimension(3);
		tlate3d.setMetric(Pn.EUCLIDEAN);
		double[][] tpoints = new double[][] {{4,0,0,1},{0,4,0,1},{0,0,4,1}};
		
		String[] names = {"x", "y","z" };
		DiscreteGroupElement[] dge = new DiscreteGroupElement[6];
		for (int i = 0; i<names.length; ++i)	{
			double[] mat = MatrixBuilder.euclidean().translate(tpoints[i]).getArray();  			
			dge[2*i] = new DiscreteGroupElement(Pn.EUCLIDEAN, mat, names[i]);
			dge[2*i+1] = dge[2*i].getInverse();
		}
		tlate3d.setGenerators(dge);
		tlate3d.setConstraint(
				new DiscreteGroupSimpleConstraint(15.0, -1, testExtraGen ? 1 : maxCount));
		tlate3d.update();
		
//		MatrixBuilder.euclidean().rotateX(Math.PI/2).assignTo(inverse);
		if (testExtraGen) {
			animTform.addChildren(oneTetra1.getRepresentationRoot()); 
		} else {
		animTform.addChildren(oneTetra1.getRepresentationRoot(), 
				oneTetra2.getRepresentationRoot());
		}
		oneTetra1.getWorldNode().addChildren(cube, truncOct);
		dgsgrTlate = new DiscreteGroupSceneGraphRepresentation(tlate3d);
		dgsgrTlate.setWorldNode(animTform);
		dgsgrTlate.update();
	}


	private DiscreteGroupSceneGraphRepresentation constructHalf(
			int which, SceneGraphComponent willie) {
		final double[] rot = P3.makeRotationMatrixY(null,  Math.PI/2);

		if (testExtraGen && which == 1) return null;
		
		DiscreteGroupColorPicker cp1 = new DiscreteGroupColorPicker() {
			double[] diag = {.5, .5, .5, 1};
			@Override
			public int calculateColorIndexForElement(DiscreteGroupElement dge) {
				double[] image = Rn.matrixTimesVector(null, 
						dge.getArray(), diag);
				for (int i = 0; i<3; ++i) if (image[i] < -1.0) image[i]+=2.0;
				for (int i = 0; i<3; ++i) if (image[i] > 1.0) image[i] -=2.0;
				System.err.println("image = "+Rn.toString(image));
				boolean px = image[0] > 0,
						py = image[1] > 0,
						pz = image[2] > 0;
				int foo = (px ? 0 : 4) + (py ? 0 : 2) + (pz ? 0 : 1);
				return foo;
			}
		};
		
		DiscreteGroupColorPicker cp2 = new DiscreteGroupColorPicker() {
			double[] diag = {.5, .5, -.5, 1};
			@Override
			public int calculateColorIndexForElement(DiscreteGroupElement dge) {
				double[] image = Rn.matrixTimesVector(null, 
						dge.getArray(), diag);
				System.err.println("image = "+Rn.toString(image));
				for (int i = 0; i<3; ++i) if (image[i] < -1.0) image[i]+=2.0;
				for (int i = 0; i<3; ++i) if (image[i] > 1.0) image[i] -=2.0;
				boolean px = image[0] < 0,
						py = image[1] < 0,
						pz = image[2] < 0;
				int ret = 7-((px ? 0 : 4) + (py ? 0 : 2) + (pz ? 0 : 1));
				return ret;
			}
		};
		// each half has the direct symmetries of a tetrahedron
		DiscreteGroup my23 = new DiscreteGroup();
		my23.setName("23");
		my23.setFinite(true);
		my23.setDimension(3);
		my23.setMetric(Pn.EUCLIDEAN);
		my23.setColorPicker(which == 0 ?  cp1 : cp2);
		
//		double[][] points = { { 0, 0, 0, 1 }, { 0, 1, 0, 0 }, { 1, 1, 1, 1 }, { 1, 1, 0, 1 } };
//		String[] names = { "4", "3", "2", };
//		double[] angles = { Math.PI / 2, 2 * Math.PI / 3, Math.PI };
		double[][] points = { { 0, 0, 0, 1 }, { 0, 1, 0, 0 }, { 1, 1, 1, 1 }};
		String[] names = testExtraGen ? new String[]{ "2", "3", "s" } : new String[]{"2", "3"};
		double[] angles = { Math.PI, 2 * Math.PI / 3, 4*Math.PI/3};
		double[][] gens = new double[names.length][];
		DiscreteGroupElement[] dge = new DiscreteGroupElement[names.length];
		for (int i = 0; i < names.length; ++i) {
			if (i<2) gens[i] = P3.makeRotationMatrix(null, points[0], points[i + 1], angles[i], Pn.EUCLIDEAN);
			else if (i == 2) {
				gens[i] = P3.makeScrewMotionMatrix(null, points[2], points[0], angles[2], Pn.EUCLIDEAN);
			}
			dge[i] = new DiscreteGroupElement(Pn.EUCLIDEAN, gens[i], names[i]);
		}
		my23.setCenterPoint(new double[] { 1 / 2.0, 3 / 4.0, 1 / 4.0, 1 });
		my23.setFinite(!testExtraGen);
		if (testExtraGen) 
			my23.setConstraint(new DiscreteGroupSimpleConstraint(24));
		my23.setGenerators(dge);
		my23.update();
		System.err.println("found # " + my23.getElementList().length + " elements");

		IndexedLineSetFactory ifsfSimple = new IndexedLineSetFactory();
		ifsfSimple.setVertexCount(2);
		double[][] verts = new double[][] { { 1,.5,0, 1 }, { .5, .5, .5, 1 } };
		if (which == 1) 
			verts = Rn.matrixTimesVector(null, rot, verts);
		System.err.println("Verts =\n"+Rn.toString(verts));
		ifsfSimple.setVertexCoordinates(verts);
		ifsfSimple.setEdgeCount(1);
		
		ifsfSimple.setEdgeIndices(new int[][] { { 0, 1 } });
		ifsfSimple.update();

		DiscreteGroupSceneGraphRepresentation dgsgr23
			= new DiscreteGroupSceneGraphRepresentation(my23);
		dgsgr23.setAppList(appList);
		dgsgr23.update();
		
		// rotate the second copy by 90 degrees so it fits onto the "second" tetrahedron in the cube
//		if (which == 0)
//			MatrixBuilder.euclidean().rotateY(Math.PI/2).assignTo(dgsgr23.getChangeOfBasisNode());

		DiscreteGroup eightclusterdg;
		DirichletDomain dirdom = new DirichletDomain(my23);
		((WingedEdge) dirdom.getDirichletDomain()).setColoredFaces(false);
		dirdom.update();
		
//		IndexedFaceSetFactory ifsfTruncOct = new IndexedFaceSetFactory();
//		ifsfTruncOct.setVertexCount(5);
//		double[][] pts = new double[][] { 
//			{ 0,1,0,1 }, {.5,1,0,1}, {0,1,.5,1 }, {.5, .5, .5, 1 }, {1,.5,0,1}};
//			if (which == 1) 
//				pts = Rn.matrixTimesVector(null, rot, pts);
//		ifsfTruncOct.setVertexCoordinates(pts);
//		ifsfTruncOct.setFaceCount(2);
//		ifsfTruncOct.setFaceIndices(new int[][]{{0,1,2},{1,2,3,4}});
//		ifsfTruncOct.update();
//
		SceneGraphComponent geom = SceneGraphUtility.createFullSceneGraphComponent("geom");
		SceneGraphComponent child1 = which == 1 ? child11 : child10;
		child1.setGeometry(ifsfSimple.getGeometry());
		
		geom.addChildren(child1);
//		geom.addChildren(child2);
		willie.addChild(geom);
		System.err.println("dirdom " + dirdom.getDirichletDomain().getNumFaces());
		dirichletDom = dirdom.getDirichletDomain();
//		willie.setGeometry(coloredFace ? ifsfSimple.getGeometry() : dirichletDom);

		dgsgr23.setWorldNode(willie);
		MatrixBuilder.euclidean().scale(scale).assignTo(willie);
		dgsgr23.update();			

		eightclusterdg = new DiscreteGroup();
		eightclusterdg.setName("b2");
		eightclusterdg.setFinite(true);
		eightclusterdg.setDimension(3);
		eightclusterdg.setMetric(Pn.EUCLIDEAN);
		points = new double[][] {{2,0,0,1},{0,2,0,1},{0,0,2,1}};
		double[][] ap = {
				{1,0,0,1},{0,0,1,0},
				{0,1,0,1},{1,0,0,0},
				{0,0,1,1},{0,1,0,0},
				{-1,0,0,1},{0,0,1,0},
				{0,-1,0,1},{1,0,0,0},
				{0,0,-1,1},{0,1,0,0},			
		};
		
		String[] names2 = {"x", "y","z","X","Y","Z" };
		dge = new DiscreteGroupElement[6];
		for (int i = 0; i<names2.length; ++i)	{
			double[] mat = MatrixBuilder.euclidean().
					rotate(ap[2*i], ap[2*((i-1+which+6)%6)+1], Math.PI).getArray();  			
			dge[i] = new DiscreteGroupElement(Pn.EUCLIDEAN, mat, names2[i]);
		}
		eightclusterdg.setGenerators(dge);
		eightclusterdg.setConstraint(new DiscreteGroupSimpleConstraint(count));
//		eightclusterdg.update();
		
		names2 = new String[]{"","x","y","z","xy","yz","zx","Zxy"};
		String[] names3 = {"","x","y","z","yx","zy","xz","Zyx"};
		String[] nams = which==0 ? names3 : names2;
		DiscreteGroupElement[] dge2 = new DiscreteGroupElement[nams.length];
		for (int i = 0; i<nams.length; ++i)	{
			dge2[i] = DiscreteGroupUtility.elementFromWord(eightclusterdg, nams[i]);
//			System.err.println(dge2[i].getWord()+" Matrix = \n"+Rn.matrixToString(dge2[i].getArray(), "%8.4f"));
		}
		eightclusterdg.setElementList(dge2);
		eightclusterdg.update();
		
		System.err.println("tlate: found # "+eightclusterdg.getElementList().length+" elements");
		DiscreteGroupSceneGraphRepresentation popo = new DiscreteGroupSceneGraphRepresentation(eightclusterdg);
		popo.setWorldNode(dgsgr23.getSceneGraphRepn());
		popo.update();
		return popo;
	}

	private WingedEdge getTruncatedOctahedron() {
		double k = -1, m = -1.5;
		double[][] planes = {
				{1,0,0,k},
				{0,1,0,k},
				{0,0,1,k},
				{-1,0,0,k},
				{0,-1,0,k},
				{0,0,-1,k},
				{1,1,1,m},
				{1,1,-1,m},
				{1,-1,1,m},
				{1,-1,-1,m},
				{-1,1,1,m},
				{-1,1,-1,m},
				{-1,-1,1,m},
				{-1,-1,-1,m}
		};
		WingedEdge TO = new WingedEdge();
		TO.setColoredFaces(true);
		TO.cutWithPlane(planes);
		TO.update();
		return TO;
	}


	private void updateCount()	{
		int n = dgsgrTlate.getSceneGraphRepn().getChildComponentCount();
		for (int i = 0; i<n; ++i)	{
			dgsgrTlate.getSceneGraphRepn().getChildComponent(i).setVisible(i<count);
		}
	}
	private void updateAppearance() {
		Appearance ap = willie1.getAppearance();
		ap.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, new Color(255,200,200));
		ap.setAttribute(LINE_SHADER+"."+DIFFUSE_COLOR, new Color(255,200,50));
		ap.setAttribute(POINT_SHADER+"."+DIFFUSE_COLOR, new Color(140,200,255));
		ap.setAttribute(METRIC, Pn.EUCLIDEAN);
		ap.setAttribute(TUBES_DRAW, true);
		
		ap.setAttribute(LINE_SHADER+"."+TUBE_RADIUS,.05);
		ap.setAttribute(POINT_SHADER+"."+POINT_RADIUS,.07);
		ap.setAttribute(TEXT_SCALE, .003);
		
		ap = world.getAppearance();
		ap.setAttribute(LINE_SHADER+"."+DIFFUSE_COLOR, new Color(200,200,200));
		
	}
	
	Camera cam;
	SceneGraphComponent cameraNode, avatarNode;
	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		rootAp = jrviewer.getViewer().getSceneRoot().getAppearance();
	    rootAp.setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(250,150,150));
	    rootAp.setAttribute(FOG_DENSITY, .2);
	    rootAp.setAttribute(CommonAttributes.FOG_ENABLED, doFog);
		SceneGraphUtility.removeLights(viewer);
		viewer.getSceneRoot().addChild(makeLights(.4));
		
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.METRIC, Pn.EUCLIDEAN);
		cam = CameraUtility.getCamera(viewer);
//		cam.setFar(-.1);
//		cam.setFieldOfView(100.0);
		cameraNode = CameraUtility.getCameraNode(viewer);
	    FlyTool flytool = new FlyTool();
	    flytool.setGain(1.0);
		cameraNode.addTool(flytool);
		List<SceneGraphPath> list = SceneGraphUtility.getPathsToNamedNodes(jrviewer.getViewer().getSceneRoot(), "avatar");
		if (list.size()>0) {
			SceneGraphPath sgp = list.get(0);
			avatarNode = sgp.getLastComponent();
		}

		((Component) viewer.getViewingComponent()).addKeyListener(getKeyAdapter());
		rotate = new Timer(20, new ActionListener()	{
			double dangle = .005;
			Matrix animM = null;
			public void actionPerformed(ActionEvent e) {
				animTform.getTransformation().multiplyOnRight(animM.getArray());
				viewer.renderAsync();
			}
			
		});
	}

	JCheckBox fullGroupB;
	int counter = 0;
	@Override
	public Component getInspector() {
		Box hbox = Box.createHorizontalBox();
		inspector.add(hbox);
		JCheckBox cB = new JCheckBox("draw cube");
		cB.setSelected(true);
		cB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean foo = ((JCheckBox)arg0.getSource()).isSelected();
				cube.setVisible(foo);
			}
		});
		hbox.add(cB);
		JCheckBox dTOB = new JCheckBox("draw trunc oct");
		dTOB.setSelected(true);
		dTOB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean foo = ((JCheckBox)arg0.getSource()).isSelected();
				truncOct.setVisible(foo);
			}
		});
		hbox.add(dTOB);
//		JCheckBox dnB = new JCheckBox("draw net");
//		dnB.setSelected(true);
//		dnB.addActionListener(new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				boolean foo = ((JCheckBox)arg0.getSource()).isSelected();
//				child1.setVisible(foo);
//			}
//		});
//		hbox.add(dnB);
		
		JCheckBox bbB = new JCheckBox("draw vertices");
		bbB.setSelected(drawVerts);
		bbB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				drawVerts = ((JCheckBox)arg0.getSource()).isSelected();
				world.getAppearance().
					setAttribute(CommonAttributes.VERTEX_DRAW, drawVerts);
			}
		});
		hbox.add(bbB);
		JButton dhB = new JButton("cycle net");
		dhB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				drawHalf = ((JButton)arg0.getSource()).isSelected();
				counter++;
				counter = counter % 4;
				if (counter == 0) counter = 1;
				child10.setVisible((counter & 1) == 1);
				child11.setVisible((counter & 2) == 2);
			}
		});
		hbox.add(dhB);
		JCheckBox fogB = new JCheckBox("fog");
		fogB.setSelected(doFog);
		fogB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doFog = ((JCheckBox)arg0.getSource()).isSelected();
				jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute(CommonAttributes.FOG_ENABLED, doFog);
			}
		});
		hbox.add(fogB);
		
		JButton mB = new JButton("fly to middle");
		mB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				cameraNode.getTransformation().setMatrix(Rn.identityMatrix(4));
				if (avatarNode != null) avatarNode.getTransformation().setMatrix(Rn.identityMatrix(4));
				cam = CameraUtility.getCamera(jrviewer.getViewer());
				cam.setNear(.1);
				cam.setFieldOfView(50);
			}
		});
		hbox.add(mB);
		
		
		final TextSlider bSlider = new TextSlider.Double("scale",  SwingConstants.HORIZONTAL,
				0, 1, scale);
		bSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				scale = bSlider.getValue().doubleValue();
				MatrixBuilder.euclidean().scale(scale).assignTo(willie1);			
				MatrixBuilder.euclidean().scale(scale).assignTo(willie2);			
//				triangles.getAppearance().setAttribute("polygonShader.implodeFactor", implodeFactor);
				//viewer.renderAsync();
			}
		});
		inspector.add(bSlider);
		final TextSlider aSlider = new TextSlider.Double("implode",  SwingConstants.HORIZONTAL,
				-1, 1, implodeFactor);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				implodeFactor = aSlider.getValue().doubleValue();
				world.getAppearance().setAttribute("polygonShader.implodeFactor", implodeFactor);
;
			}
		});
		inspector.add(aSlider);
		final TextSlider nSlider = new TextSlider.Integer("count",  SwingConstants.HORIZONTAL,
				1,maxCount,count);
		nSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				count = nSlider.getValue().intValue();
				updateCount();
			}
		});
		inspector.add(nSlider);
		return inspector;
	}
	KeyAdapter ka = null;
	public KeyAdapter getKeyAdapter() {
		if (ka == null)	{
			ka = new KeyAdapter()	{
				boolean rotating = false, translating = false, xroting = false;
				boolean beyondInfinity = false;
				public void keyPressed(KeyEvent e)	{ 
					switch(e.getKeyCode())	{
						
					case KeyEvent.VK_H:
						System.out.println("	1: toggle rotate");
						break;
		
					case KeyEvent.VK_1:
						rotating = !rotating;
						System.err.println("rotating = "+rotating);
						if (rotating) rotate.start();
						else rotate.stop();
						break;
					}
				}
			};
		}
		return ka;
	}

	int k1 = 50, k2 = 255, k3 = 192, k4 = 92, k5 = 150;
	Appearance[] getAppList()	{
		Color[] colorlist = {
				new Color(k2, k1, k1), //Color.gray,
				new Color(k2, k5, k1), //Color.red,
				new Color(k2, k1, k2), //Color.blue,
				new Color(k2, k2, k1), // Color.green,
				new Color(k1, k2, k2), // Color.magenta,
				new Color(k1, k1, k2), // Color.cyan,
				new Color(0,  k5, k1), //Color.yellow,
				new Color(k4, k4, k4)   //Color.pink,
		};
		Appearance[] aplist = new Appearance[8];
		for (int i = 0; i<8; ++i)	{
			aplist[i] = new Appearance();
			aplist[i].setAttribute("lineShader.polygonShader.diffuseColor", colorlist[i]);
			aplist[i].setAttribute("pointShader.polygonShader.diffuseColor", colorlist[i]);
			aplist[i].setAttribute("polygonShader.diffuseColor", colorlist[i]);
		}
		return aplist;
	
	}
	
	public SceneGraphComponent makeLights(double strength) {
		SceneGraphComponent lightNode = new SceneGraphComponent();
		SceneGraphComponent lightNode1 = new SceneGraphComponent();
		SceneGraphComponent lightNode2 = new SceneGraphComponent();
		lightNode.setName("lights");
		double[][] lightlocations = {
				{1,1,1,1},
				{1,-1,-1,1},
				{-1,-1,1,1},
				{-1,1,-1,1},
		};
		double[][] mats = new double[4][];
		mats[0] = P3.makeRotationMatrix(null, new double[]{0,0,1}, new double[]{1,1,1});
		mats[1] = P3.makeRotationMatrixX(null, Math.PI);
		mats[2] = P3.makeRotationMatrixY(null, Math.PI);
		mats[3] = P3.makeRotationMatrixZ(null, Math.PI);
		
		for( int i = 0; i < lightlocations.length; ++i )	{
			if (i > 0) mats[i] = Rn.times(null, mats[i], mats[0]);
//			double[] axis = {-1.2,-1.6,-2,1};
//			if( i > 0 )
//				axis[i-1] = 1; 
//			else 
//				axis = new double[]{1.8,1.3,1,1};
			SceneGraphComponent l0 = SceneGraphUtility.createFullSceneGraphComponent("light0");
			DirectionalLight pointLight = new DirectionalLight();
			int c[] = {255, 255, 255};
			if( i < 3 )
				c[i] = 200;
			pointLight.setColor(new Color(c[0], c[1], c[2]));
			pointLight.setIntensity(strength);
			l0.getTransformation().setMatrix( P3.makeTranslationMatrix(null, lightlocations[i], Pn.EUCLIDEAN));		
			l0.setLight(pointLight);
			lightNode1.addChild(l0);
			//			dl.setFalloff(falloffs[metric+1]);
		}
		lightNode2.addChild(lightNode1);
		lightNode2.setTransformation(new Transformation(
				Rn.times(null, -1, Rn.identityMatrix(4))));
		
		lightNode.addChildren(lightNode1, lightNode2);
		return lightNode;
	}

	public static void main(String[] argv)	{
		EightSRS harry = new EightSRS();
		harry.display();
	}
	
}

