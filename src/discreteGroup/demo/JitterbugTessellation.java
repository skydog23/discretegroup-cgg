package discreteGroup.demo;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.anim.core.KeyFrameAnimatedBean;
import charlesgunn.anim.plugin.AnimationPlugin;
import charlesgunn.jreality.geometry.ClipBox;
import charlesgunn.jreality.newtools.FlyTool;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.groups.TriangleGroup;

public class JitterbugTessellation extends Assignment {

	transient IndexedFaceSetFactory triangleFactory = new IndexedFaceSetFactory(),
			gapFactory = new IndexedFaceSetFactory(),
			gapTriFactory = new IndexedFaceSetFactory();
	transient private DiscreteGroupSceneGraphRepresentation tlateRepn;
	transient private DiscreteGroupSimpleConstraint simpleConstraint = 
			new DiscreteGroupSimpleConstraint(10.0, 1, 20);

	transient private DiscreteGroup translationGroup;
	transient private ClipBox clipbox;
	transient private SceneGraphComponent worldSGC, triSGC, gapSGC, gapTriSGC;
	transient TextSlider.Double clipSlider;
	transient TextSlider.Double timeSlider;

	boolean showGaps = false, showTris = true, showGapTri= false;
	double time = 0.0,
			clipSize = 3;

	@Override
	public SceneGraphComponent getContent() {
		DiscreteGroup pointGroupS222 = TriangleGroup.instanceOfGroup("3*2");
		DiscreteGroupSceneGraphRepresentation dgsgr = new DiscreteGroupSceneGraphRepresentation(pointGroupS222);
		triSGC = SceneGraphUtility.createFullSceneGraphComponent("triSGC");
		gapSGC = SceneGraphUtility.createFullSceneGraphComponent("gapSGC");
		worldSGC = SceneGraphUtility.createFullSceneGraphComponent("world");
		gapTriSGC = SceneGraphUtility.createFullSceneGraphComponent("gapSGC");
		//		fundDomSGC.addTool(new RotateTool());
		// attach it to the scene graph representation
		gapSGC.setVisible(showGaps);
		gapTriSGC.setVisible(showGapTri);
		triSGC.setVisible(showTris);
		SceneGraphComponent rootNode = new SceneGraphComponent("root");
		rootNode.setAppearance(new Appearance());
		rootNode.addChildren(triSGC, gapSGC, gapTriSGC);
		dgsgr.setWorldNode(rootNode);
		// this will generate a jReality scene graph
		dgsgr.update();
		triangleFactory.setVertexCount(3);
		double t  = 0;
		triangleFactory.setFaceCount(1);
		triangleFactory.setFaceIndices(new int[][]{{0,1,2}});
		triangleFactory.setEdgeCount(1);
		triangleFactory.setEdgeIndices(new int[][]{{0,1}});
		triangleFactory.setGenerateEdgesFromFaces(false);
		triangleFactory.setGenerateFaceNormals(true);

		gapFactory.setVertexCount(3);
		gapFactory.setFaceCount(1);
		gapFactory.setFaceIndices(new int[][]{{0,1,2}});
		gapFactory.setEdgeCount(1);
		gapFactory.setEdgeIndices(new int[][]{{1,2}});
		gapFactory.setGenerateEdgesFromFaces(false);
		gapFactory.setGenerateFaceNormals(true);

		gapTriFactory.setVertexCount(3);
		gapTriFactory.setFaceCount(1);
		gapTriFactory.setFaceIndices(new int[][]{{0,1,2}});
		gapTriFactory.setEdgeCount(2);
		gapTriFactory.setEdgeIndices(new int[][]{{0,1},{2,0}});
		gapTriFactory.setGenerateEdgesFromFaces(false);
		gapTriFactory.setGenerateFaceNormals(true);
		update(time);
		triSGC.setGeometry(triangleFactory.getIndexedFaceSet());
		gapSGC.setGeometry(gapFactory.getIndexedFaceSet());
		gapTriSGC.setGeometry(gapTriFactory.getIndexedFaceSet());
		
		Appearance ap = rootNode.getAppearance();
	    ap.setAttribute("lineShader.polygonShader.diffuseColor", 
					new Color(.3f, 1f, 0f));
		ap.setAttribute("pointShader.polygonShader.diffuseColor", 
				new Color(1f, 1f, 0f));
		ap.setAttribute(CommonAttributes.TUBE_RADIUS, .008);
		ap.setAttribute(CommonAttributes.AMBIENT_COEFFICIENT, .2);
		ap.setAttribute(CommonAttributes.AMBIENT_COLOR, Color.white);
		ap = triSGC.getAppearance();
	    ap.setAttribute("polygonShader.diffuseColor", Color.yellow);
		ap = gapSGC.getAppearance();
	    ap.setAttribute("polygonShader.diffuseColor", new Color(0,124,255));
		ap = gapTriSGC.getAppearance();
	    ap.setAttribute("polygonShader.diffuseColor", Color.red);
		translationGroup = new DiscreteGroup();
		translationGroup.setMetric(Pn.EUCLIDEAN);	// only indirectly used, when creating various sorts of geometry associated to the group
		translationGroup.setDimension(3);			// ditto
		translationGroup.setFinite(false);			// this is a 'hint' that can help optimize the group element generation
		// create the generators: in this case reflections in the three coordinate axes.
		boolean smallNbhd = false;
		if (smallNbhd)	{
			DiscreteGroupElement[] gens = new DiscreteGroupElement[8];
			double[] dir1 = {1,1,1},
				dir2 = {1,-1,-1},
				dir3 = {-1,-1,1},
				dir4 = {-1,1,-1};
			gens[0] = new DiscreteGroupElement( Pn.EUCLIDEAN, MatrixBuilder.euclidean().translate(dir1).getArray(), "x");
			gens[1] = new DiscreteGroupElement( Pn.EUCLIDEAN, MatrixBuilder.euclidean().translate(dir2).getArray(), "y");
			gens[2] = new DiscreteGroupElement( Pn.EUCLIDEAN, MatrixBuilder.euclidean().translate(dir3).getArray(), "z");
			gens[3] = new DiscreteGroupElement( Pn.EUCLIDEAN, MatrixBuilder.euclidean().translate(dir4).getArray(), "w");
			for (int i = 0; i<4; ++i) gens[i+4] = gens[i].getInverse();
			translationGroup.setGenerators(gens);
			simpleConstraint.setManhattan(true);
			translationGroup.setConstraint(simpleConstraint);
			
		} else {
			DiscreteGroupElement[] gens = new DiscreteGroupElement[6];
			double[] xplane = {2,0,0,1},
				yplane = {0,2,0,1},
				zplane = {0,0,2,1};
			gens[0] = new DiscreteGroupElement( Pn.EUCLIDEAN, MatrixBuilder.euclidean().translate(xplane).getArray(), "x");
			gens[1] = new DiscreteGroupElement( Pn.EUCLIDEAN, MatrixBuilder.euclidean().translate(yplane).getArray(), "y");
			gens[2] = new DiscreteGroupElement( Pn.EUCLIDEAN, MatrixBuilder.euclidean().translate(zplane).getArray(), "z");
			for (int i = 0; i<3; ++i) gens[i+3] = gens[i].getInverse();
			translationGroup.setGenerators(gens);			
			simpleConstraint = new DiscreteGroupSimpleConstraint(10, 10, 100);
			simpleConstraint.setManhattan(true);
			translationGroup.setConstraint(simpleConstraint);
		}
		translationGroup.update();
		tlateRepn = new DiscreteGroupSceneGraphRepresentation(translationGroup);
		tlateRepn.setWorldNode(dgsgr.getSceneGraphRepn());
		tlateRepn.setClipToCamera(false);
		tlateRepn.setFollowsCamera(false);
		tlateRepn.update();
		simpleConstraint.addListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				System.err.println("jitterbug update.");
				translationGroup.update();
				tlateRepn.setElementList(translationGroup.getElementList());
				tlateRepn.update();
			}
		});
		simpleConstraint.setManhattan(true);
		clipbox = new ClipBox();
		clipbox.setDim(new double[]{clipSize, clipSize, clipSize});
//		simpleConstraint.setMaxDistance(clipSize+1);
		tlateRepn.getRepresentationRoot().addChild(clipbox.getBox());
		worldSGC.addChild(tlateRepn.getRepresentationRoot());
		return worldSGC;
	}

	@Override
	public void display() {
		super.display();
		viewer = jrviewer.getViewer();
		FlyTool flyTool = new FlyTool();
//		flyTool.addChangeListener(new ActionListener() {
//
//			public void actionPerformed(ActionEvent e) {
//				System.err.println("flying");
//				viewer.renderAsync();
//			}
//			
//		});

		flyTool.setGain(1);
		scene.getAvatarComponent().addTool(flyTool);
//		CameraUtility.getCameraNode(viewer).addTool(flyTool);
		System.err.println("camera path = "+viewer.getCameraPath().toString());
		
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor", Color.black);
		AnimationPlugin ap = animationPlugin;
		ap.setAnimateCamera(true);
		ap.setAnimateSceneGraph(true);
		KeyFrameAnimatedBean me = new KeyFrameAnimatedBean(this);
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


	@Override
	public Component getInspector() {
		Box container = (Box) super.getInspector();
		timeSlider = new TextSlider.Double("jitterbug",
				SwingConstants.HORIZONTAL, 0.0, 1, time);
		timeSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				time = timeSlider.getValue();
				update(time);
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
						showGaps = !showGaps;
						setShowGaps(showGaps);
						break;

					case KeyEvent.VK_2:
						showGapTri = !showGapTri;
						setShowGapTri(showGapTri);
						break;		


					case KeyEvent.VK_3:
						showTris = !showTris;
						setShowTris(showTris);
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
	void update(double t)	{
		double[][] tv = {{t,1,0},{1,0,-t},{(t+1.0)/3.0, (1+t)/3.0, (-1-t)/3.0},{1,0,0},{1,1,0}};  //{0,t,-1},
		triangleFactory.setVertexCoordinates(new double[][]{tv[0], tv[1], tv[2]});
		triangleFactory.update();
		gapFactory.setVertexCoordinates(new double[][]{tv[0], tv[1], tv[3]});
		gapFactory.update();
		gapTriFactory.setVertexCoordinates(new double[][]{tv[0], tv[1], tv[4]});
		gapTriFactory.update();
		
	}

	public boolean isShowGaps() {
		return showGaps;
	}

	public void setShowGaps(boolean showGaps) {
		this.showGaps = showGaps;
		gapSGC.setVisible(showGaps);
	}

	public boolean isShowTris() {
		return showTris;
	}

	public void setShowTris(boolean showTris) {
		this.showTris = showTris;
		triSGC.setVisible(showTris);
	}

	public boolean isShowGapTri() {
		return showGapTri;
	}

	public void setShowGapTri(boolean showGapTri) {
		this.showGapTri = showGapTri;
		gapTriSGC.setVisible(showGapTri);
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
		timeSlider.setValue(time);
		update(time);
	}

	public double getClipSize() {
		return clipSize;
	}

	public void setClipSize(double clipSize) {
		this.clipSize = clipSize;
		clipSlider.setValue(clipSize);
		clipbox.setDim(new double[]{clipSize, clipSize, clipSize});
	}
	public static void main(String[] args) {
		new JitterbugTessellation().display();
	}
}
