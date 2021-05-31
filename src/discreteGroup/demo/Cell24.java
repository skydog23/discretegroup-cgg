/*
 * Created on Jun 8, 2012
 *
 */
package discreteGroup.demo;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LIGHTING_ENABLED;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.METRIC;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import charlesgunn.jreality.geometry.projective.LineUtility;
import charlesgunn.jreality.newtools.FlyTool;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.p5.PlueckerLineGeometry;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.tool.Tool;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.ImplodePolygonShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.util.WingedEdge;
import discreteGroup.tools.CopyClickTool;

public class Cell24 extends Assignment {

	DiscreteGroup my24cell, octaGroup;
	private DirichletDomain dirdom;
	boolean tetraGen = false,
			coloredFace = false,
			showCube = true,
			showFD = true,
			showTetras = true,
			copyClick = false,
			fullGroup = !copyClick,
			rememberCC = true,
			isBackBanana = false,
			showPoint = false;
	private static Color[] colors = new Color[]{
			new Color(50, 140,255),
			new Color(255, 100, 100),
			new Color(50,200,50)
	};
	double scale = 1.0,
			implodeFactor = .15;
	Timer rotate = null;
	CopyClickTool cct;
	DiscreteGroupElement origDGE[], ccList[], identityList[];
	
	SceneGraphComponent 
	world,
		animTform,
			tetras,
				oneTetra,
			cube,
			// DGSGR contains
			willie;
	Geometry triangleFace, dirichletDom;
	IndexedFaceSetFactory ifsf3color = new IndexedFaceSetFactory();
	IndexedFaceSetFactory ifsfSimple = new IndexedFaceSetFactory();
	DiscreteGroupSceneGraphRepresentation dgsgr, dgsgr24;
	private Appearance rootAp;
	
	public SceneGraphComponent getContent()	{
		 world = SceneGraphUtility.createFullSceneGraphComponent("world");
		 animTform = SceneGraphUtility.createFullSceneGraphComponent("anim tform");
		 willie = SceneGraphUtility.createFullSceneGraphComponent("willie");
		 cube = SceneGraphUtility.createFullSceneGraphComponent("cube");

		octaGroup = setupOctahedron();
		dgsgr = new DiscreteGroupSceneGraphRepresentation(octaGroup);
		dgsgr.setWorldNode(willie);
		dgsgr.update();
		
		my24cell = get24CellGroup();
	
		dgsgr24 = new DiscreteGroupSceneGraphRepresentation(my24cell);
		MatrixBuilder.elliptic().
			rotateZ(Math.PI/4).
			translate( new double[] {0,0,0,1}, new double[] {0,0,1,1}).
			assignTo(dgsgr24.getChangeOfBasisNode());
		dgsgr24.update();
//		dgsgr24.getChangeOfBasisNode().setTransformation(tf );
		origDGE = dgsgr24.getElementList();
		identityList = new DiscreteGroupElement[] {origDGE[0]};

		dirdom = new DirichletDomain(my24cell);
		((WingedEdge) dirdom.getDirichletDomain()).setColoredFaces(false);
		dirdom.update();
		System.err.println("dirdom " + dirdom.getDirichletDomain().getNumFaces());
		dirichletDom = dirdom.getDirichletDomain();
		// DiscreteGroupUtility.addWordLabels((WingedEdge)
		// dirdom.getDirichletDomain());

		MatrixBuilder.euclidean().scale(scale).assignTo(willie);

		updateGroup();
		
		updateDGSGR();
		
		updateGeometry();

		// create the 3 tetrahedra as wire-frame
		tetras = getThreeTetras(colors);
		
		cube.setGeometry(Primitives.cube4(false));
		cube.setPickable(false);
//		SceneGraphComponent backBanana = new SceneGraphComponent("backbanana");
//		backBanana.setGeometry(Primitives.cube4(false));
//		cube.addChild(backBanana);
//		Matrix m = new Matrix(Rn.times(null, -1, Rn.identityMatrix(4)));
//		m.assignTo(backBanana);

		Appearance ap = cube.getAppearance();
		// has to be turned off otherwise looks very dark
		ap.setAttribute(LIGHTING_ENABLED, false);
		ap.setAttribute("lineShader.diffuseColor", Color.YELLOW);
		ap.setAttribute(FACE_DRAW,false);
		ap.setAttribute(TUBES_DRAW,false);
		
		SceneGraphComponent moo = SceneGraphUtility.createFullSceneGraphComponent();
		moo.setGeometry(Primitives.point(new double[]{.5,.5,.5,1}));
		ap = moo.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.POINT_RADIUS, .03);
		ap.setAttribute("pointShader.diffuseColor", Color.red);
		moo.setVisible(showPoint);
		
		animTform.addChildren(dgsgr24.getRepresentationRoot(), tetras, cube, moo);
		world.addChild(animTform);
		MatrixBuilder.elliptic().
			translate(new double[]{0,0,-2}).
			rotateY(Math.PI).assignTo(world);
		return world;
	}

	private DiscreteGroup setupOctahedron() {
		DiscreteGroup myocta = new DiscreteGroup();
		cube.setVisible(showCube);
		cube.setPickable(false);
		willie.setVisible(showFD);
		updateAppearance(willie);

		myocta.setName("octahedron");
		myocta.setFinite(true);
		myocta.setDimension(3);
		myocta.setMetric(Pn.ELLIPTIC);
		myocta.setCenterPoint(new double[] { 1 / 6.0, 1 / 6.0, 1 / 6.0, 1 });
		// just display the identity and the generators
		// myocta.setConstraint(new DiscreteGroupSimpleConstraint(13));
		double[][] planes = { { 1, 0, 0, 0 }, { 0, 1, 0, 0 }, { 0, 0, 1, 0 } };

		String[] names = { "x", "y", "z" };
		double[][] gens = new double[3][];
		DiscreteGroupElement[] dge = new DiscreteGroupElement[3];
		for (int i = 0; i < 3; ++i) {
			gens[i] = P3.makeReflectionMatrix(null, planes[i], Pn.ELLIPTIC);
			dge[i] = new DiscreteGroupElement(Pn.ELLIPTIC, gens[i], names[i]);
		}
		myocta.setFinite(true);
		myocta.setGenerators(dge);
		myocta.update();
		System.err.println("myocta has "+myocta.getElementList().length+" elements.");

		ifsf3color.setVertexCount(7);
		ifsf3color.setVertexCoordinates(new double[][] { 
			{ 1, 0, 0, 1 }, 
			{ .5, .5, 0, 1 }, 
			{ 0, 1, 0, 1 },
			{ 0, .5, .5, 1 }, 
			{ 0, 0, 1, 1 }, 
			{ .5, 0, 0.5, 1 }, 
			{ 1 / 3.0, 1 / 3.0, 1 / 3.0, 1.0 } 
			});
		ifsf3color.setFaceCount(3);	
		ifsf3color.setFaceIndices(new int[][] { 
			{ 0, 1, 6, 5 }, 
			{ 2, 3, 6, 1 }, 
			{ 4, 5, 6, 3 } });
		ifsf3color.setFaceColors(colors);
		ifsf3color.setMetric(Pn.ELLIPTIC);
		ifsf3color.setGenerateFaceNormals(true);
		ifsf3color.setGenerateEdgesFromFaces(true);
		ifsf3color.update();

		ifsfSimple.setVertexCount(3);
		ifsfSimple.setVertexCoordinates(new double[][] { 
			{ 1, 0, 0, 1 }, { 0, 1, 0, 1 }, { 0, 0, 1, 1 } });
		ifsfSimple.setFaceCount(1);
		ifsfSimple.setFaceIndices(new int[][] { { 0, 1, 2 } });
		ifsfSimple.setGenerateEdgesFromFaces(true);
		ifsfSimple.update();
		return myocta;
	}

	public static SceneGraphComponent getThreeTetras(Color[] cc) {
		SceneGraphComponent tetras = SceneGraphUtility.createFullSceneGraphComponent("tetras");
		SceneGraphComponent oneTetra = SceneGraphUtility.createFullSceneGraphComponent("one tetra");
		Appearance ap = tetras.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(CommonAttributes.LINE_WIDTH, 3.0);
		double[][] tetraPoints = new double[][]{
			{1,0,0,0},
			{0,1,0,0},
			{0,0,1,0},
			{0,0,0,1}
		};
		double[][] matrices = {
				Rn.transpose(null, new double[]{1,1,1,1,
				 -1,-1,1,1,
				 -1,1,-1,1,
				 1,-1,-1,1
				}),
				Rn.transpose(null, new double[]{-1,-1,-1,1,
				1,1,-1,1,
				1,-1,1,1,
				-1,1,1,1
				}),
				Rn.identityMatrix(4)
		};
		int[][] indices = {{0,1},{0,2},{0,3},{1,2},{3,1},{2,3}};
		for (int i = 0; i<6; ++i)	{
			double[] line = PlueckerLineGeometry.lineFromPoints(null, 
					tetraPoints[indices[i][0]],  tetraPoints[indices[i][1]]);
			oneTetra.addChild(LineUtility.sceneGraphForLine(null, line, tetraPoints[indices[i][0]], 1.0, false));
		}
		for (int i = 0; i<3; ++i)	{
			SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("child"+i);
			sgc.setTransformation(new Transformation(matrices[i]));
			ap = sgc.getAppearance();
			ap.setAttribute("lineShader.diffuseColor",cc == null ? colors[i] : cc[i]);
			sgc.addChild(oneTetra);
			tetras.addChild(sgc);
		}
		return tetras;
	}

	public static DiscreteGroup get24CellGroup() {
		double[][] gens;
		DiscreteGroupElement[] dge;
		DiscreteGroup my24cell = new DiscreteGroup(); //24cell.gens");		
		my24cell.setName("24 cell");
		my24cell.setDimension(3);
		my24cell.setMetric(Pn.ELLIPTIC);
		my24cell.setProjective(false);  // we identify two matrices X and Y if X = -Y.
		my24cell.setFinite(true);
	
		String[] names2 = {"x","y","z","w"};
		// the four pairs of opposite planes of the octahedron should be 90 degrees apart
		// k = 1/3 produces this result. 
		double k = 1/3.0;
		double[][] points = {
				{k, k, k, 1},{-k,-k,-k,1},
				{k, -k, -k, 1},{-k,k,k,1},
				{-k, k, -k, 1},{k,-k,k,1},
				{-k, -k, k, 1},{k,k,-k,1}
				};
		gens = new double[4][];
		dge = new DiscreteGroupElement[4];
		for (int i = 0; i<4; ++i)	{
			gens[i] = P3.makeScrewMotionMatrix(null, points[2*i], points[2*i+1], Math.PI/3.0, Pn.ELLIPTIC);
			dge[i] = new DiscreteGroupElement(Pn.ELLIPTIC, gens[i], names2[i]);
		}
		my24cell.setGenerators(dge);
		my24cell.update();
		System.err.println("24 cell has order "+my24cell.getElementList().length);
		return my24cell;
	}

	private void updateGeometry() {
		willie.setGeometry(tetraGen ? 
				(coloredFace ? ifsf3color.getGeometry() : ifsfSimple.getGeometry()) 
				: dirichletDom);
		
		if (cct == null)	{
			cct = new CopyClickTool(my24cell, dgsgr24);
			updateTool();
		}
		updateAppearance(willie);
	}

	private void updateTool() {
		List<Tool> listo = cct.getSceneGraphComponent().getTools();
		boolean alreadyLoaded = listo.contains(cct);
//		System.err.println("list = "+listo.toString());
		if (copyClick && !alreadyLoaded ) {
			cct.getSceneGraphComponent().addTool(cct);
			dgsgr24.getFundamentalRegion().addChild(cct.getSceneGraphComponent());
		}
		else if (alreadyLoaded) {
			cct.getSceneGraphComponent().removeTool(cct);
			dgsgr24.getFundamentalRegion().removeChild(cct.getSceneGraphComponent());
		}
	}

	private void updateDGSGR() {
		if (tetraGen)	{
			dgsgr24.setWorldNode(dgsgr.getRepresentationRoot());
		} else {
			dgsgr24.setWorldNode(willie);
		}
		dgsgr24.update();
	}
	
	private void updateGroup()	{
		if (fullGroup)	{
			dgsgr24.setElementList(origDGE);			
		} else if (rememberCC && ccList != null){
			dgsgr24.setElementList(ccList);
			System.err.println("Restoring cclist: order is "+ccList.length);
		} else {
			dgsgr24.setElementList(identityList);			
		}
		dgsgr24.update();

	}
	private void updateAppearance(SceneGraphComponent willie) {
		Appearance ap = willie.getAppearance();
//		ap.setAttribute(LINE_SHADER+"."+POLYGON_SHADER+"."+DIFFUSE_COLOR, new Color(255,200,200));
			DefaultGeometryShader dgs = (DefaultGeometryShader) 
		   			ShaderUtility.createDefaultGeometryShader(ap, true);
			ImplodePolygonShader dps = (ImplodePolygonShader) dgs.createPolygonShader("implode");
			ap.setAttribute("polygonShader.implodeFactor", implodeFactor);
		ap.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, new Color(255,200,200));
		ap.setAttribute(LINE_SHADER+"."+DIFFUSE_COLOR, new Color(255,200,200));
		ap.setAttribute(POINT_SHADER+"."+DIFFUSE_COLOR, new Color(140,200,255));
		ap.setAttribute(METRIC, Pn.ELLIPTIC);
		ap.setAttribute("useGLSL", true); //!coloredFace);
		ap.setAttribute("oneGLSL", true);  // don't ask!
//		ap.setAttribute(EDGE_DRAW, !coloredFace);
//		ap.setAttribute(VERTEX_DRAW, !coloredFace);
		ap.setAttribute(FACE_DRAW, copyClick || tetraGen);
		ap.setAttribute(LINE_SHADER+"."+TUBE_RADIUS,.01);
		ap.setAttribute(POINT_SHADER+"."+POINT_RADIUS,.015);
//		ap.setAttribute(CommonAttributes.OFFSET, new double[]{0,0,0});
//		ap.setAttribute(CommonAttributes.ALIGNMENT, SwingConstants.CENTER);
		ap.setAttribute(CommonAttributes.TEXT_SCALE, .003);
	}
	
	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		rootAp = jrviewer.getViewer().getSceneRoot().getAppearance();
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.METRIC, Pn.ELLIPTIC);
		Camera cam = CameraUtility.getCamera(viewer);
		cam.setFar(-.1);
		cam.setFieldOfView(100.0);
		SceneGraphComponent cameraNode = CameraUtility.getCameraNode(viewer);
	    FlyTool flytool = new FlyTool();
	    flytool.setGain(.15);
		cameraNode.addTool(flytool);
		((Component) viewer.getViewingComponent()).addKeyListener(getKeyAdapter());
		rotate = new Timer(20, new ActionListener()	{
			double dangle = .005;
			Matrix animM = new Matrix(P3.makeTranslationMatrix(null, 
					new double[]{dangle, dangle, dangle,1}, 
					new double[]{0,0,0,1}, 
					Pn.ELLIPTIC));
			public void actionPerformed(ActionEvent e) {
				animTform.getTransformation().multiplyOnRight(animM.getArray());
				viewer.renderAsync();
			}
			
		});
	}

	JCheckBox fullGroupB;
	@Override
	public Component getInspector() {
		Box hbox = Box.createHorizontalBox();
		inspector.add(hbox);
		JCheckBox bbB = new JCheckBox("Back banana");
		bbB.setSelected(isBackBanana);
		bbB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				isBackBanana = ((JCheckBox)arg0.getSource()).isSelected();
				rootAp.
					setAttribute(CommonAttributes.RENDER_S3, isBackBanana);
			}
		});
		hbox.add(bbB);
		JCheckBox copyClickB = new JCheckBox("Copy click tool");
		copyClickB.setSelected(copyClick);
		copyClickB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				copyClick = ((JCheckBox)arg0.getSource()).isSelected();
				if (copyClick) {
					fullGroup = false;
					fullGroupB.setSelected(fullGroup);
					updateGroup();
				} else {  // remember the current state of the list
					ccList = dgsgr24.getElementListFromSGC();
					System.err.println(" setting ccList: order is "+ccList.length);
				}
				updateTool();
			}
		});
		hbox.add(copyClickB);
		fullGroupB = new JCheckBox("Full group");
		fullGroupB.setSelected(fullGroup);
		fullGroupB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				fullGroup = ((JCheckBox)arg0.getSource()).isSelected();
				updateGroup();
			}
		});
		hbox.add(fullGroupB);
		JCheckBox identityB= new JCheckBox("Trivial group");
		identityB.setSelected(!rememberCC);
		identityB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				rememberCC = !((JCheckBox)arg0.getSource()).isSelected();
				updateGroup();
			}
		});
		hbox.add(identityB);
		hbox = Box.createHorizontalBox();
		inspector.add(hbox);
		JCheckBox colorB = new JCheckBox("Show cube");
		colorB.setSelected(showCube);
		colorB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showCube = ((JCheckBox)arg0.getSource()).isSelected();
				cube.setVisible(showCube);
			}
		});
		hbox.add(colorB);
		
		JCheckBox showTetrasB = new JCheckBox("Show tetras");
		showTetrasB.setSelected(showTetras);
		showTetrasB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showTetras = ((JCheckBox)arg0.getSource()).isSelected();
				tetras.setVisible(showTetras);
			}
		});
		hbox.add(showTetrasB);
		
		JCheckBox showFDB = new JCheckBox("Show FD");
		showFDB.setSelected(showFD);
		showFDB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showFD = ((JCheckBox)arg0.getSource()).isSelected();
				willie.setVisible(showFD);
			}
		});
		hbox.add(showFDB);
		
		JCheckBox tetraGenB = new JCheckBox("Tetra gen");
		tetraGenB.setSelected(tetraGen);
		tetraGenB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tetraGen = ((JCheckBox)arg0.getSource()).isSelected();
				updateDGSGR();
				updateGeometry();
			}
		});
		hbox.add(tetraGenB);
		
		JCheckBox triColorB = new JCheckBox("Three color triangle");
		triColorB.setSelected(coloredFace);
		triColorB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				coloredFace = ((JCheckBox)arg0.getSource()).isSelected();
				updateGeometry();
			}
		});
		hbox.add(triColorB);
		
		final TextSlider bSlider = new TextSlider.Double("scale",  SwingConstants.HORIZONTAL,
				0, 1, scale);
		bSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				scale = bSlider.getValue().doubleValue();
				MatrixBuilder.euclidean().scale(scale).assignTo(willie);			
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
				updateAppearance(willie);
			}
		});
		inspector.add(aSlider);
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

	public static void main(String[] argv)	{
		Cell24 harry = new Cell24();
		harry.display();
	}
	
	private double[] getFaceCenter(int k, int[][] indices, double[][] verts)	{
		double[][] vs = new double[indices[k].length][];
		for (int i = 0; i<vs.length; ++i)	{
			vs[i] = verts[indices[k][i]];
		}
		return Rn.average(null, vs);
	}
}

