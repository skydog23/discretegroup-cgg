package discreteGroup.demo;

import java.awt.Color;

import charlesgunn.jreality.AbstractDeformation;
import charlesgunn.jreality.tools.AbstractShapeTool;
import charlesgunn.jreality.viewer.Assignment;
import de.jreality.geometry.GeometryMergeFactory;
import de.jreality.geometry.RemoveDuplicateInfo;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.PickShowTool;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;
import de.jreality.util.CameraUtility;
import de.jreality.util.CopyVisitor;
import de.jreality.util.PickUtility;
import de.jreality.util.SceneGraphUtility;

public class TensegrityStructure extends Assignment {
	
	PlanarPattern pp;
	SceneGraphComponent theWorld;
	GeometryMergeFactory gmf = new GeometryMergeFactory();
	boolean loaded = false;
	Viewer viewer;
	double arc = Math.PI*2;
	private SceneGraphComponent deformedGeometry;
	private IndexedFaceSet flat;
	@Override
	public SceneGraphComponent getContent() {
		AbstractShapeTool.setDeactivatePicking(false);
		theWorld = SceneGraphUtility.createFullSceneGraphComponent("world");
		theWorld.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
		theWorld.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW,true);
		theWorld.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW,true);
		theWorld.getAppearance().setAttribute(CommonAttributes.POINT_RADIUS,0.05);
		deformedGeometry = new SceneGraphComponent("new");
		theWorld.addChild(deformedGeometry);
		pp = new PlanarPattern() {

			@Override
			protected void updateDynamicGeometry(double[] thePoint) {
				super.updateDynamicGeometry(thePoint);
				if (loaded) updateWorld();
			}
			
		};
		pp.getContent();
		DragEventTool t = new DragEventTool();
		double[] highlightColor = {1,0,0,1};		// highlight point in red
		t.addPointDragListener(new PointDragListener() {
			double[][] points;
			PointSet pointSet;

			public void pointDragStart(PointDragEvent e) {
				pointSet = e.getPointSet();
				points=pointSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
				double[] position = points[e.getIndex()];
				System.out.println("drag start of vertex no "+e.getIndex()+" "+Rn.toString(position));				
			}

			public void pointDragged(PointDragEvent e) {
				pointSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(points);
		        points[e.getIndex()]=e.getPosition();  
		        pointSet.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(points));			
			}

			public void pointDragEnd(PointDragEvent e) {
			}
			
		});
		
		deformedGeometry.addTool(t);
		updateWorld();
		return theWorld;
	}
	public void updateWorld()	{
		gmf = new GeometryMergeFactory();
		flat = gmf.mergeGeometrySets(pp.getTheDynSGR().getRepresentationRoot());
		int count = flat.getNumPoints();
//		flat = (IndexedFaceSet) RemoveDuplicateInfo.removeDuplicateVertices(flat, 10E-3);
//		System.err.println("Before, after: "+count+" "+flat.getNumPoints());
		updateCylinder();
//		System.err.println("Updating");
	}
	
	protected void updateCylinder()	{
		final double scale = .5*arc/(Math.sqrt(.75)*pp.getYDim());
		final double ydim = pp.getYDim();
		final double r = 2*ydim/arc;
		AbstractDeformation rollIt = new AbstractDeformation()	{
		     public double[] valueAt(double[] in, double[] out)	{
		        double angle = scale * in[1];		// should be between +/- arc/2
		        double c = r * Math.cos(angle);
		        double s = r * Math.sin(angle);
		        if (out == null) out = new double[in.length];
		        out[0] = in[0];
		        out[1] = c; // * in[0];
		        out[2] = s; // * in[0];
		        if (out.length > 3 && in.length > 3) out[3] = in[3];
		        return out;
		    }
		};
//		System.out.println("Rolling!");
		CopyVisitor cv = new CopyVisitor();
		cv.visit(flat);
		IndexedFaceSet copy = (IndexedFaceSet) cv.getCopy();
		double[][] vc = new double[copy.getNumPoints()][3];
		Pn.dehomogenize(vc, copy.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null));
		copy.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(vc));
		deformedGeometry.setGeometry(copy);
		AbstractDeformation.deform(deformedGeometry, rollIt);
		int count = copy.getNumPoints();
		copy = (IndexedFaceSet) RemoveDuplicateInfo.removeDuplicateVertices(copy, 10E-6);
//		System.err.println("Before, after: "+count+" "+copy.getNumPoints());
		deformedGeometry.setGeometry(copy);
		PickUtility.setPickable(deformedGeometry, true, true, false);
		if (viewer != null) viewer.renderAsync();
	}
	
	@Override
	public void display() {
		super.display();
		viewer = jrviewer.getViewer();
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.lightGray);
		CameraUtility.encompass(viewer);
		viewer.getSceneRoot().addTool(new PickShowTool());
	}

//	public Component getMyInspector() {
//		Box box = Box.createVerticalBox();
//		JPanel panel = new JPanel(new BorderLayout());
//		box.setName("tensegrity structure");
////		InteractiveViewerPanel ivp = new InteractiveViewerPanel();
////		ivp.loadScene(pp);
//		ViewerAppLoader val = TestViewerApp.makeViewerAppLoader();
//		val.loadScene(pp);
//		loaded = true;
//		updateWorld();
//			((Component) val.viewerAp.getViewingComponent()).setPreferredSize(new Dimension(20,20));
//		panel.add((Component) val.viewerAp.getContent()); 
//		box.add(panel);
//		TextSlider slider = new TextSlider.Double("arc",SwingConstants.HORIZONTAL,0.0, 2*Math.PI, arc);
//		slider.addActionListener(new ActionListener()	{
//			public void actionPerformed(ActionEvent e)	{
//				arc = ((TextSlider) e.getSource()).getValue().doubleValue();
//				updateCylinder();
//				viewer.renderAsync();
//				
//			}
//		});
//		box.add(slider);
//		box.add(pp.getInspector(null));
//		return box;
//	}

//	@Override
//	public boolean hasInspector() {
//		return false;
//	}
//
//	@Override
//	public boolean isEncompass() {
//		return true;
//	}

//	@Override
//	public boolean isPerspective() {
//		return true;
//	}
	
	static TensegrityStructure ts;
	public static void main(String[] argc)	{
		ts = new TensegrityStructure();
		ts.display();
//		ViewerAppLoader val = TestViewerApp.makeViewerAppLoader();
//		val.loadScene(ts);
//		ViewerApp va = val.viewerAp;
//		va.setExternalNavigator(false);
//		va.setAttachNavigator(true);
//		va.setAttachBeanShell(false);
//		va.setExternalBeanShell(false);
//		Component inspector = ts.getMyInspector();
//		va.addAccessory(inspector, "PlanarPattern", false);
//		va.setFirstAccessory(inspector);
//		va.getNavigator().setPreferredSize(new Dimension(200,20));
//		va.update();
//		va.display();
//		CameraUtility.encompass(ts.viewer);
	}
}
