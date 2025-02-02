package discreteGroup.demo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;

import charlesgunn.anim.core.KeyFrameAnimatedBean;
import charlesgunn.anim.core.KeyFrameAnimatedTransformation;
import charlesgunn.anim.core.TimeDescriptor;
import charlesgunn.anim.plugin.AnimationPlugin;
import charlesgunn.anim.util.AnimationUtility.InterpolationTypes;
import charlesgunn.jreality.geometry.ClipBox;
import charlesgunn.jreality.newtools.FlyTool;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupConstraint;
import de.jtem.discretegroup.core.DiscreteGroupConstraintUtility;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;

public class JitterbugOctaTetra extends Assignment {

	transient IndexedFaceSetFactory triangleFactory = new IndexedFaceSetFactory(),
			gapFactory = new IndexedFaceSetFactory(),
			gapTriFactory = new IndexedFaceSetFactory();
	transient private DiscreteGroup fourGroup = new DiscreteGroup(), translationGroup, pointGroup;
	transient private DiscreteGroupSceneGraphRepresentation tlateRepn, pointRepn,
		fourGroupRepn = new DiscreteGroupSceneGraphRepresentation(fourGroup);
	transient private DiscreteGroupSimpleConstraint 
		simpleConstraint = new DiscreteGroupSimpleConstraint(3,0,25);

	
	transient private ClipBox clipbox;
	transient private SceneGraphComponent collectSGC, octaSGC, triLinearSGC, animTriSGC, tri1GoodSGC, tri3GoodSGC;
	transient TextSlider.Double clipSlider;
	transient TextSlider.Double timeSlider;

	double time = 0.0,
			clipSize = 3;

	@Override
	public SceneGraphComponent getContent() {
		triLinearSGC = SceneGraphUtility.createFullSceneGraphComponent("triSGC");
		tri1GoodSGC = SceneGraphUtility.createFullSceneGraphComponent("gapSGC");
		SceneGraphUtility.createFullSceneGraphComponent("world");
		collectSGC = SceneGraphUtility.createFullSceneGraphComponent("collect");
		tri3GoodSGC = SceneGraphUtility.createFullSceneGraphComponent("gapSGC");
		SceneGraphUtility.createFullSceneGraphComponent("tri2");
		animTriSGC = SceneGraphUtility.createFullSceneGraphComponent("tri3");
		//		fundDomSGC.addTool(new RotateTool());
		// attach it to the scene graph representation
		triLinearSGC.setVisible(false);
		tri1GoodSGC.setVisible(true);
		tri3GoodSGC.setVisible(false);
		triangleFactory.setVertexCount(3);
		triangleFactory.setFaceCount(1);
		triangleFactory.setFaceIndices(new int[][]{{0,1,2}});
//		triangleFactory.setEdgeCount(1);
//		triangleFactory.setEdgeIndices(new int[][]{{0,1}});
		triangleFactory.setGenerateEdgesFromFaces(true);
		triangleFactory.setGenerateFaceNormals(true);

		gapFactory.setVertexCount(3);
		gapFactory.setFaceCount(1);
		gapFactory.setFaceIndices(new int[][]{{0,1,2}});
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
		setValueAtTime(time);
		triLinearSGC.setGeometry(triangleFactory.getIndexedFaceSet());
		tri1GoodSGC.setGeometry(gapFactory.getIndexedFaceSet());
		tri3GoodSGC.setGeometry(gapTriFactory.getIndexedFaceSet());
		
		Appearance ap = collectSGC.getAppearance();
	    ap.setAttribute("lineShader.polygonShader.diffuseColor", 
					Color.blue);
		ap.setAttribute("pointShader.polygonShader.diffuseColor", 
				new Color(1f, 1f, 0f));
		ap.setAttribute(CommonAttributes.TUBE_RADIUS, .02);
		ap.setAttribute(CommonAttributes.AMBIENT_COEFFICIENT, .1);
		ap.setAttribute(CommonAttributes.AMBIENT_COLOR, Color.white);
//		ap = triSGC.getAppearance();
//	    ap.setAttribute("polygonShader.diffuseColor", Color.yellow);
//		ap = gapSGC.getAppearance();
//	    ap.setAttribute("polygonShader.diffuseColor", new Color(0,124,255));
//		ap = gapTriSGC.getAppearance();
//	    ap.setAttribute("polygonShader.diffuseColor", Color.red);
	    
	    pointGroup = new DiscreteGroup();
	    
		DiscreteGroupElement[] gens = new DiscreteGroupElement[4];
		double[][] axes = { {1,0,0}, {0,1,0}, {0,0,1}};
		String[] names = {"x","y","z","m"};
		for (int i = 0; i<3; ++i)	{
			gens[i] = new DiscreteGroupElement( 
					Pn.EUCLIDEAN, MatrixBuilder.euclidean().rotate(Math.PI,axes[i]).getArray(),names[i]);
		}
		gens[3] = new DiscreteGroupElement( 
				Pn.EUCLIDEAN, MatrixBuilder.euclidean().reflect(new double[] {1,0,0,0}).getArray(),names[3]);
		pointGroup.setGenerators(gens);	
		pointGroup.setFinite(true);
		DiscreteGroupConstraint diconst = DiscreteGroupConstraintUtility.directIsometryConstraint(true);
		diconst.setMaxNumberElements(8);
		pointGroup.setConstraint(diconst);
		pointGroup.update();
		pointRepn = new DiscreteGroupSceneGraphRepresentation(pointGroup);
		
		octaSGC = SceneGraphUtility.createFullSceneGraphComponent("octahed");
		octaSGC.setGeometry(Primitives.coloredCube());
		octaSGC.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
		octaSGC.setVisible(false);
//		animTriSGC.addChildren(triSGC, gapSGC);
		MatrixBuilder.euclidean().rotateZ(Math.PI/4).scale(1/Math.sqrt(2.0)).assignTo(collectSGC);
		collectSGC.addChildren(triLinearSGC, tri1GoodSGC, tri3GoodSGC); 

		pointRepn.setWorldNode(collectSGC); //dgsgr.getSceneGraphRepn());
		pointRepn.update();

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
		tlateRepn.setWorldNode(pointRepn.getRepresentationRoot()); //dgsgr.getSceneGraphRepn());
		tlateRepn.setClipToCamera(false);
		tlateRepn.setFollowsCamera(false);
		tlateRepn.update();
		clipbox = new ClipBox();
		clipbox.setDim(new double[]{clipSize, clipSize, clipSize});
		simpleConstraint.setMaxDistance(clipSize+1);
		tlateRepn.getRepresentationRoot().addChild(clipbox.getBox());
		
		fourGroup.setFinite(true);

		DiscreteGroupElement[] els = new DiscreteGroupElement[4];
		double[][] tlates = {{0,0,0},{1,1,0},{1,0,1},{0,1,1}};
		Color[] clrs = {Color.yellow, Color.green, Color.red, Color.magenta};
		Appearance[] aplist = new Appearance[4];
		for (int i = 0; i<4; ++i)	{
			els[i] = new DiscreteGroupElement(
					Pn.EUCLIDEAN, MatrixBuilder.euclidean().translate(tlates[i]).getArray(), "x");
			els[i].setColorIndex(i);
			aplist[i] = new Appearance();
			aplist[i].setAttribute("polygonShader.diffuseColor", clrs[i]);
		}
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
		
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor", Color.black);
		AnimationPlugin ap = animationPlugin;
		ap.setAnimateCamera(false);
		ap.setAnimateSceneGraph(false);
		KeyFrameAnimatedBean<JitterbugOctaTetra> me = new KeyFrameAnimatedBean<JitterbugOctaTetra>(this);
		ap.getAnimated().add(me);
		
		// add the camera node by hand to animation system
//		Transformation tt = new Transformation();
//		KeyFrameAnimatedTransformation T = new KeyFrameAnimatedTransformation(tt, Pn.EUCLIDEAN);	
//		T.setInterpolationType(InterpolationTypes.CUBIC_HERMITE);
//		T.setName(animTriSGC.getName()+" Tform");
//		MatrixBuilder.euclidean().assignTo(tt);
//		T.addKeyFrame(new TimeDescriptor(0.0));
//		Matrix m = new Matrix(new double[]{0.833333, -0.5, 0.235702, -0.333333, 0.5, 0.5, -0.707107, 0, 
//			0.235702, 0.707107, 0.666667, -0.235702, 0, 0, 0, 1.});
//		m.assignTo(tt);;		
//		T.addKeyFrame(new TimeDescriptor(1.0));
//		ap.getAnimated().add(T);	
//		T.setValueAtTime(0);
		//animTriSGC.setTransformation(tt);

//		animTriSGC.getTransformation().addTransformationListener(new TransformationListener() {
//			
//			@Override
//			public void transformationMatrixChanged(TransformationEvent ev) {
//				System.err.println(animTriSGC.getName()+" changed tform");
//				
//			}
//		});

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
		double[][] coords = {{1-t, 1, k*t,1}, {0, -t, k*(2-t),1}, { 1, t-1, -k*t,1}};
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
		gapFactory.setVertexCoordinates(coordsWH);
		gapFactory.update();
		gapTriFactory.setVertexCoordinates(coordsWHWM);
		gapTriFactory.update();
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
		container.add(showFourGroup);

		JComboBox poop = new JComboBox(new String[] {"line","1 good","3 good"}); //PaintType.values()); //
		poop.setSelectedIndex(1);
		poop.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int which = ((JComboBox)e.getSource()).getSelectedIndex();
				System.err.println("selected "+which);
				setVariant(which);
			}
			
		});
		poop.setPreferredSize(new Dimension(40,20));
		container.add(poop);

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
						tlateRepn.setClipToCamera(!tlateRepn.isClipToCamera());
						break;

					case KeyEvent.VK_5:
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
			collectSGC.getChildComponent(i).setVisible(i==which);
		}
	}

	protected void flipVariant(int which) {
			collectSGC.getChildComponent(which).setVisible(
					!collectSGC.getChildComponent(which).isVisible());
	}

	public static void main(String[] args) {
		new JitterbugOctaTetra().display();
	}
}
