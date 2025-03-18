package discreteGroup.demo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;

import charlesgunn.anim.core.KeyFrameAnimatedBean;
import charlesgunn.anim.plugin.AnimationPlugin;
import charlesgunn.jreality.geometry.ClipBox;
import charlesgunn.jreality.newtools.FlyTool;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.Biquaternion;
import charlesgunn.math.BiquaternionUtility;
import charlesgunn.math.Biquaternion.Metric;
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
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.SimpleURLPolicy;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupConstraint;
import de.jtem.discretegroup.core.DiscreteGroupConstraintUtility;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;

public class JitterbugOctaTetra extends Assignment {

	transient IndexedFaceSetFactory triangleFactory = new IndexedFaceSetFactory(),
			gapFactory = new IndexedFaceSetFactory(),
			gapTriFactory = new IndexedFaceSetFactory(),
			octaFactory = new IndexedFaceSetFactory();
	transient private DiscreteGroup fourGroup = new DiscreteGroup(), translationGroup, pointGroup;
	transient private DiscreteGroupSceneGraphRepresentation tlateRepn, pointRepn,
		fourGroupRepn = new DiscreteGroupSceneGraphRepresentation(fourGroup);
	transient private DiscreteGroupSimpleConstraint 
		simpleConstraint = new DiscreteGroupSimpleConstraint(3,0,25);
	DiscreteGroupConstraint diconst = DiscreteGroupConstraintUtility.directIsometryConstraint(false);

	
	transient private ClipBox clipbox;
	transient private SceneGraphComponent interpVariantsSGC, collect2SGC, octaSGC, triLinearSGC, animTriSGC, tri1GoodSGC, tri3GoodSGC;
	transient TextSlider.Double clipSlider;
	transient TextSlider.Double timeSlider;

	double time = 0.0,
			clipSize = 3;
	transient protected boolean oneSplitOnly = false;

	@Override
	public SceneGraphComponent getContent() {
		triLinearSGC = SceneGraphUtility.createFullSceneGraphComponent("triSGC");
		tri1GoodSGC = SceneGraphUtility.createFullSceneGraphComponent("gapSGC");
		SceneGraphUtility.createFullSceneGraphComponent("world");
		interpVariantsSGC = SceneGraphUtility.createFullSceneGraphComponent("collect1");
		collect2SGC = SceneGraphUtility.createFullSceneGraphComponent("collect2");
		tri3GoodSGC = SceneGraphUtility.createFullSceneGraphComponent("gapSGC");
		SceneGraphUtility.createFullSceneGraphComponent("tri2");
		animTriSGC = SceneGraphUtility.createFullSceneGraphComponent("tri3");
		//		fundDomSGC.addTool(new RotateTool());
		// attach it to the scene graph representation
		triangleFactory.setVertexCount(3);
		triangleFactory.setFaceCount(1);
		triangleFactory.setFaceIndices(new int[][]{{0,1,2}});
//		triangleFactory.setVertexLabels(new String[] {"A","B","C"});
//		triangleFactory.setEdgeCount(1);
//		triangleFactory.setEdgeIndices(new int[][]{{0,1}});
		triangleFactory.setGenerateEdgesFromFaces(true);
		triangleFactory.setGenerateFaceNormals(true);

		gapFactory.setVertexCount(3);
		gapFactory.setFaceCount(1);
		gapFactory.setFaceIndices(new int[][]{{0,1,2}});
		gapFactory.setFaceColors(new Color[] {Color.white});
//		gapFactory.setEdgeCount(1);
//		gapFactory.setEdgeIndices(new int[][]{{1,2}});
		gapFactory.setGenerateEdgesFromFaces(true);
		gapFactory.setGenerateFaceNormals(true);

		gapTriFactory.setVertexCount(3);
		gapTriFactory.setFaceCount(1);
		gapTriFactory.setFaceIndices(new int[][]{{0,1,2}});
//		gapTriFactory.setEdgeCount(2);
//		gapTriFactory.setEdgeIndices(new int[][]{{0,1},{2,0}});
		gapTriFactory.setGenerateEdgesFromFaces(true);
		gapTriFactory.setGenerateFaceNormals(true);
		
		octaFactory.setVertexCount(8);
		octaFactory.setFaceCount(8);
		octaFactory.setFaceIndices(new int[][]{{0,1,2}});
//		octaFactory.setEdgeCount(1);
//		octaFactory.setEdgeIndices(new int[][]{{0,1}});
		octaFactory.setGenerateEdgesFromFaces(true);
		octaFactory.setGenerateFaceNormals(true);


		setValueAtTime(time);
		triLinearSGC.setGeometry(triangleFactory.getIndexedFaceSet());
		tri1GoodSGC.setGeometry(gapFactory.getIndexedFaceSet());
		tri3GoodSGC.setGeometry(gapTriFactory.getIndexedFaceSet());
		
		Appearance ap = collect2SGC.getAppearance();
	    ap.setAttribute("lineShader.diffuseColor", Color.blue);
		ap.setAttribute("pointShader.polygonShader.diffuseColor", 
				new Color(1f, 1f, 0f));
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .02);
//		ap.setAttribute(CommonAttributes.SHOW_LABELS, true);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(ap, false);
		DefaultTextShader pts = (DefaultTextShader) ((DefaultPointShader) dgs.getPointShader()).getTextShader();
		pts.setDiffuseColor(new Color(153, 255, 153));
		pts.setScale(.0025);
		pts.setOffset(new double[] { .0, .04, .2 });
		pts.setAlignment(SwingConstants.NORTH_EAST);
		Font f = new Font("Arial Bold", Font.ITALIC, 48);
		pts.setFont(f);

//		ap = triSGC.getAppearance();
//	    ap.setAttribute("polygonShader.diffuseColor", Color.yellow);
//		ap = gapSGC.getAppearance();
//	    ap.setAttribute("polygonShader.diffuseColor", new Color(0,124,255));
//		ap = gapTriSGC.getAppearance();
//	    ap.setAttribute("polygonShader.diffuseColor", Color.red);
	    
	    pointGroup = new DiscreteGroup();
	    DiscreteGroupElement[] gens = null;
	    int numEls = 0;
		double[][] axes = { {1,0,0}, {0,1,0}, {0,0,1}, {1,-1,0}};
	    if (oneSplitOnly)   {
			gens = new DiscreteGroupElement[2];
			String[] names = {"m","n","r"};
			double[][] planes = {{1,0,0,0},{0,1,0,0}};
//			for (int i = 0; i<2; ++i)	{
				gens[0] = new DiscreteGroupElement( 
						Pn.EUCLIDEAN, MatrixBuilder.euclidean().rotateZ(Math.PI).getArray(),names[2]);
//			}
			gens[1] = new DiscreteGroupElement( 
					Pn.EUCLIDEAN, MatrixBuilder.euclidean().rotate(Math.PI,axes[3]).reflect(planes[0]).getArray(),names[0]);
	    	numEls = 8;
	    } else {
			gens = new DiscreteGroupElement[4];
			String[] names = {"x","y","z","m"};
			for (int i = 0; i<3; ++i)	{
				gens[i] = new DiscreteGroupElement( 
						Pn.EUCLIDEAN, MatrixBuilder.euclidean().rotate(Math.PI,axes[i]).getArray(),names[i]);
			}
			gens[3] = new DiscreteGroupElement( 
					Pn.EUCLIDEAN, MatrixBuilder.euclidean().reflect(new double[] {1,0,0,0}).getArray(),names[3]);
			numEls = 8;
	    }
		diconst.setMaxNumberElements(numEls);	    	
		pointGroup.setGenerators(gens);	
		pointGroup.setFinite(true);
		pointGroup.update();
		pointRepn = new DiscreteGroupSceneGraphRepresentation(pointGroup);
		
		octaSGC = SceneGraphUtility.createFullSceneGraphComponent("octahed");
		ap = octaSGC.getAppearance();
	    ap.setAttribute("lineShader.diffuseColor", new Color(250,250,150));
		ap.setAttribute(CommonAttributes.TUBE_RADIUS, .01);
		
		octaSGC.setGeometry(Primitives.octahedron());
		octaSGC.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
		octaSGC.setVisible(true);
//		animTriSGC.addChildren(triSGC, gapSGC);
		MatrixBuilder.euclidean().rotateZ(Math.PI/4).scale(1/Math.sqrt(2.0)).assignTo(interpVariantsSGC);
		interpVariantsSGC.addChildren(triLinearSGC, tri1GoodSGC, tri3GoodSGC); 
		setVariant(0);
		collect2SGC.addChildren(interpVariantsSGC, octaSGC);
		pointRepn.setWorldNode(collect2SGC); //dgsgr.getSceneGraphRepn());
		pointRepn.update();
		pointGroupVis(false);

		translationGroup = new DiscreteGroup();
		translationGroup.setMetric(Pn.EUCLIDEAN);	// only indirectly used, when creating various sorts of geometry associated to the group
		translationGroup.setDimension(3);			// ditto
		translationGroup.setFinite(false);			// this is a 'hint' that can help optimize the group element generation

		gens = new DiscreteGroupElement[6];
		double[] xplane = {2,0,0,1},
			yplane = {0,2,0,1},
			zplane = {0,0,2,1};
		gens[0] = new DiscreteGroupElement( Pn.EUCLIDEAN, MatrixBuilder.euclidean().translate(xplane).getArray(), "x");
		gens[1] = new DiscreteGroupElement( Pn.EUCLIDEAN, MatrixBuilder.euclidean().translate(yplane).getArray(), "y");
		gens[2] = new DiscreteGroupElement( Pn.EUCLIDEAN, MatrixBuilder.euclidean().translate(zplane).getArray(), "z");
		for (int i = 0; i<3; ++i) gens[i+3] = gens[i].getInverse();
		translationGroup.setGenerators(gens);			
		simpleConstraint.setManhattan(true);
		translationGroup.setConstraint(simpleConstraint);

		translationGroup.update();
		simpleConstraint.addListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				System.err.println("jitterbug update.");
				translationGroup.update();
				tlateRepn.setElementList(translationGroup.getElementList());
				tlateRepn.update();
			}
		});
		
		tlateRepn = new DiscreteGroupSceneGraphRepresentation(translationGroup);
		SceneGraphComponent collect3SGC = SceneGraphUtility.createFullSceneGraphComponent(),
				child = SceneGraphUtility.createFullSceneGraphComponent();
		child.addChildren(pointRepn.getRepresentationRoot());
		new Matrix(magicMatrix()).assignTo(child);
		collect3SGC.addChildren(child, pointRepn.getRepresentationRoot());
		tlateRepn.setWorldNode(pointRepn.getSceneGraphRepn());
		tlateRepn.setClipToCamera(false);
		tlateRepn.setFollowsCamera(false);
		tlateRepn.update();
		clipbox = new ClipBox();
		clipbox.setDim(new double[]{clipSize, clipSize, clipSize});
		simpleConstraint.setMaxDistance(clipSize+1);
		tlateRepn.getRepresentationRoot().addChild(clipbox.getBox());
		

		boolean doRotate4 = false;
		DiscreteGroupElement[] els = new DiscreteGroupElement[4];
		double[][] tlates = {{1,1,0},{1,0,1},{0,1,1}};
		double[][] points = {{1,0,0,1},{0,1,0,1},{0,0,1,1}};
		String[] nms = {"i","x","y","z"};
		Color[] clrs = {Color.yellow, Color.green, Color.red, Color.magenta};
		Appearance[] aplist = new Appearance[4];
		double[][] mats = new double[4][];
		mats[0] = Rn.identityMatrix(4);
		for (int i = 0; i<3; ++i)	{
			mats[i+1] = doRotate4 ? 
					P3.makeRotationMatrix(null, points[(i+1)%3], points[(i+2)%3], Math.PI, Pn.EUCLIDEAN) :
					MatrixBuilder.euclidean().translate(tlates[i]).getArray();	
		}			
		for (int i = 0; i<4; ++i)	{
			els[i] = new DiscreteGroupElement(Pn.EUCLIDEAN, mats[i], nms[i]);
			els[i].setColorIndex(i);
			aplist[i] = new Appearance();
			aplist[i].setAttribute("polygonShader.diffuseColor", clrs[i]);
		}
		fourGroup.setFinite(true);
		fourGroup.setElementList(els, true);
		fourGroup.update();
		fourGroupRepn.setElementList(els);
		fourGroupRepn.setAppList(aplist);
		fourGroupRepn.setWorldNode(tlateRepn.getRepresentationRoot());
		fourGroupRepn.update();
		return(fourGroupRepn.getRepresentationRoot());
	}

	@Override
	public void display() {
		hlIntensity = .3;
		setAddCameraLight(true);
		
		super.display();
		viewer = jrviewer.getViewer();
		FlyTool flyTool = new FlyTool();

		flyTool.setGain(1);
		scene.getAvatarComponent().addTool(flyTool);
		
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor", new Color(0,0,0,0));
		AnimationPlugin ap = animationPlugin;
		ap.setAnimateCamera(true);
		ap.setAnimateSceneGraph(true);
		KeyFrameAnimatedBean<JitterbugOctaTetra> me = new KeyFrameAnimatedBean<JitterbugOctaTetra>(this);
		ap.getAnimated().add(me);
		
//		SceneGraphPath pathToWorld = SceneGraphUtility.getPathsBetween(
//				viewer.getSceneRoot(), world ).get(0);
//		Graphics3D g3d = new Graphics3D(viewer);
//		DiscreteGroupViewportConstraint dgvc = new DiscreteGroupViewportConstraint(2.0, 2, 4.0, 4, g3d);
//		tlateRepn.setViewportConstraint(dgvc);
//		tlateRepn.attachToViewer(viewer, pathToWorld); 	
//		tlateRepn.update();
		addKeyListener(viewer);
	}

	static double k = Math.sqrt(2.0)/2;
	private double h(double t) { return Math.sqrt(.5*t*(2-t));}
	
	@Override
	public void setValueAtTime(double t) {
		// TODO Auto-generated method stub
		super.setValueAtTime(t);
		// Three different ways to calculate the coordinates of the animated triangle
		// The first interpolates linearly, and ends up being an equilateral triangle that gets smaller then bigger
		// the second attempts keeps the length of at one edges constant
		// The third uses Mathematica code to adjust the third vertex so that the triangle remains the same size and equilateral
		// Unfortunately only the first avoids distracting self-intersections of neighboring triangles
		//{-t, -2*t, k*(1.5*t-1),1}
		double[][] coords = {{1-t, 1, k*t,1}, {0, -t, k*(2-t),1}, { 1, t-1, -k*t,1}};
		double[][] coordsGap = {{1-t, 1, k*t,1}, {0, -t, k*(2-t),1}, { -1, -(t-1), -k*t,1}};
//		double[][][] pairs = {{{0,0,k},{-1,-1,0},{1,-1,0}},{{1,0,k/2},{0,-1,-k/2},{2,0
//		double[][] coordsGap = {{1-t, 1, k*t,1}, {2* t, t, k*(3*t-2),1}, { 1, (t-1), -k*t,1}};
		double[][] coordsWH = {{1-t, 1, h(t),1}, {0, -t, k+h(1-t),1}, { 1, t-1, -h(t),1}};
		double  sc = 1.0/(-8+2*Math.pow(t,2)),
				y = sc*((2*(4*Math.pow(t,2) - 4*Math.pow(t,3) + Math.pow(t,4) - 
		          2*Math.sqrt(2)*Math.sqrt(-((-2 + t)*t))*Math.sqrt(-(Math.pow(-2 + t,3)*(1 + t)))))/(-2 + t)),
				z =  sc*(2*Math.sqrt(2)*t*Math.sqrt(-((-2 + t)*t)) 
					- Math.sqrt(2)*Math.pow(t,2)*Math.sqrt(-((-2 + t)*t))
			        - 4*Math.sqrt(-(Math.pow(-2 + t,3)*(1 + t))));
			      
		double[][] coordsWHWM = {coordsWH[0], {0, y, z, 1}, coordsWH[2]};
		double[] ds = new double[3], dswh = new double [3], dswhwm = new double[3];
		for (int i = 0; i<3; ++i)	{
			int j = (i+1)%3, m = (i+2)%3;
			ds[i] = Pn.distanceBetween(coords[j], coords[m], Pn.EUCLIDEAN);
			dswh[i] = Pn.distanceBetween(coordsWH[j], coordsWH[m], Pn.EUCLIDEAN);	
			dswhwm[i] = Pn.distanceBetween(coordsWHWM[j], coordsWHWM[m], Pn.EUCLIDEAN);	
		}
//		System.err.println("dists = "+Rn.toString(ds));
//		System.err.println("distsWH = "+Rn.toString(dswh));
//		System.err.println("distsWHWM = "+Rn.toString(dswhwm));
		triangleFactory.setVertexCoordinates(coords);
		triangleFactory.update();
		gapFactory.setVertexCoordinates(coordsGap); //coordsWH);
		gapFactory.update();
		gapTriFactory.setVertexCoordinates(coordsWHWM);
		gapTriFactory.update();
	}

	private double[] magicMatrix()	{
		double[] p1 = {0,-1,k,1}, p2 = {1,0,-k,1};
		double[] ln = PlueckerLineGeometry.lineFromPoints(null, p1, p2);
		Biquaternion biq = new Biquaternion(ln, Metric.EUCLIDEAN);
		Biquaternion rot = Biquaternion.exp(null, biq, new Biquaternion(1, 0, Metric.EUCLIDEAN), Math.PI/2);
		double[] m = Biquaternion.matrixFromBiquaternion(null, rot);
		System.err.println("Line = "+Rn.toString(ln));
		System.err.println("Biq = "+biq.toString());
			System.err.println("Matrix = "+Rn.matrixToJavaString(m));
		return m;
	}
	@Override
	public Component getInspector() {
		Box container = (Box) super.getInspector();
		Box showFourGroup =  Box.createHorizontalBox();
		String[] labels = {"---","-++","+-+","++-"};
		for (int i = 0; i<4; ++i)	{
			JCheckBox animate = new JCheckBox(labels[i]);
			animate.setSelected(true);
			final int j = i;
			animate.addActionListener( new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					boolean b = ((JCheckBox) e.getSource()).isSelected();
					fourGroupRepn.getSceneGraphRepn().getChildComponent(j).setVisible(b);
					viewer.renderAsync();
				}
			});
			showFourGroup.add(animate);
		}

		JComboBox poop = new JComboBox(new String[] {"line","1 good","3 good"}); //PaintType.values()); //
		poop.setSelectedIndex(0);
		poop.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int which = ((JComboBox)e.getSource()).getSelectedIndex();
				System.err.println("selected "+which);
				setVariant(which);
			}
			
		});
		poop.setPreferredSize(new Dimension(40,20));
		showFourGroup.add(poop);
		
		JCheckBox all8 = new JCheckBox("mirror images");
		all8.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean b = ((JCheckBox) e.getSource()).isSelected();
				pointGroupVis(b);
			}
			
		});

		showFourGroup.add(all8);
		container.add(showFourGroup);


		timeSlider = new TextSlider.Double("jitterbug",
				SwingConstants.HORIZONTAL, 0.0, 1, time);
		timeSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				time = timeSlider.getValue();
				setValueAtTime(time);
			}
		});
		container.add(timeSlider);
		clipSlider = new TextSlider.Double("clip size",
				SwingConstants.HORIZONTAL, 0.0, 4, clipSize);
		clipSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clipSize = clipSlider.getValue();
				clipbox.setDim(new double[]{clipSize, clipSize, clipSize});
//				simpleConstraint.setMaxDistance(clipSize+1);
			}
		});
		container.add(clipSlider);
		container.add(simpleConstraint.getInspector());
		return container;

	}

	private void addKeyListener(Viewer viewer) {
		Component comp = ((Component) viewer.getViewingComponent());
		comp.addKeyListener(new KeyAdapter() {
 				public void keyPressed(KeyEvent e)	{ 
					switch(e.getKeyCode())	{
						
					case KeyEvent.VK_H:
						break;
		
					case KeyEvent.VK_1:
						flipVariant(0);
						break;

					case KeyEvent.VK_2:
						flipVariant(1);
						break;		


					case KeyEvent.VK_3:
						flipVariant(2);
						break;		

					case KeyEvent.VK_4:
						octaSGC.setVisible(!octaSGC.isVisible());
						break;		

						
					case KeyEvent.VK_9:
						tlateRepn.setClipToCamera(!tlateRepn.isClipToCamera());
						break;

					case KeyEvent.VK_0:
						tlateRepn.setFollowsCamera(!tlateRepn.isFollowsCamera());
						break;		
}
		
				}
			});
	}
	
//	void update(double t)	{
//		double[][] tv = {{t,1,0},{1,0,-t},{(t+1.0)/3.0, (1+t)/3.0, (-1-t)/3.0},{1,0,0},{1,1,0}};  //{0,t,-1},
//		double[][] tv = {{1,1,0},{0,0,Math.sqrt(2.0)},{1,-1,0}}; // (1+t)/3.0, (-1-t)/3.0},{1,0,0},{1,1,0}};  //{0,t,-1},
//		gapFactory.setVertexCoordinates(new double[][]{tv[0], tv[1], tv[3]});
//		gapFactory.update();
//		gapTriFactory.setVertexCoordinates(new double[][]{tv[0], tv[1], tv[4]});
//		gapTriFactory.update();
		
//	}


	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
		timeSlider.setValue(time);
		setValueAtTime(time);
	}

	public double getClipSize() {
		return clipSize;
	}

	public void setClipSize(double clipSize) {
		this.clipSize = clipSize;
		clipSlider.setValue(clipSize);
		clipbox.setDim(new double[]{clipSize, clipSize, clipSize});
	}
	
	protected void setVariant(int which) {
		for (int i = 0; i<3; ++i)	{
			interpVariantsSGC.getChildComponent(i).setVisible(i==which);
		}
	}

	protected void flipVariant(int which) {
			interpVariantsSGC.getChildComponent(which).setVisible(
					!interpVariantsSGC.getChildComponent(which).isVisible());
	}

	protected void pointGroupVis(boolean b) {
		int n = pointRepn.getSceneGraphRepn().getChildComponentCount();
		for (int i = 0; i<n; ++i)	{
			if (b) {
				pointRepn.getSceneGraphRepn().getChildComponent(i).setVisible(true);
				continue;
			} // only show the direct isometries
			double[] m = pointRepn.getSceneGraphRepn().getChildComponent(i).getTransformation().getMatrix();
			pointRepn.getSceneGraphRepn().getChildComponent(i).setVisible(Rn.determinant(m)>0);
		}
	}

	public static void main(String[] args) {
		new JitterbugOctaTetra().display();
	}
}
