package discreteGroup.demo;

import java.awt.Color;

import charlesgunn.jreality.newtools.FlyTool;
import charlesgunn.jreality.newtools.RotateTool;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.groups.Spherical3DGroup;
import de.jtem.discretegroup.util.WingedEdge;

public class TetrahedraTower extends Assignment {

	private IndexedFaceSet tetrahedron;
	double[][] planes = new double[4][];
	private SceneGraphComponent pointsSGC;

	@Override
	public SceneGraphComponent getContent() {
//		tetrahedron = Primitives.coloredTetrahedron();
		tetrahedron = getTetrahedron(Math.PI*2.0/5.0);
		int[][] indices = tetrahedron.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		double[][] verts = tetrahedron.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		double[][] pts = new double[5][];
		pts[0] = new double[]{0,0,0};
		for (int i = 0; i<indices.length; ++i)	{
			planes[i] = P3.planeFromPoints(null, verts[indices[i][0]], verts[indices[i][1]], verts[indices[i][2]]);
			double[][] tmp = {verts[indices[i][0]], verts[indices[i][1]], verts[indices[i][2]]};
			pts[i+1] = Rn.average(null, tmp);
		}
		PointSetFactory psf = new PointSetFactory();
		psf.setVertexCount(5);
		psf.setVertexCoordinates(pts);
		psf.setVertexColors(new Color[]{Color.white, Color.red, Color.yellow, Color.green, Color.blue});
		psf.update();
		pointsSGC = SceneGraphUtility.createFullSceneGraphComponent("pts");
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		SceneGraphComponent universe = SceneGraphUtility.createFullSceneGraphComponent("world");
		world.setGeometry(tetrahedron);
		Appearance ap = world.getAppearance();
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute("lineShader.lineWidth", 2.0);
//		world.addChild(pointsSGC);
		pointsSGC.setGeometry(psf.getPointSet());
		pointsSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		world.getAppearance().setAttribute("metric", Pn.ELLIPTIC);
		world.getAppearance().setAttribute("useGLSL", true);
		world.getAppearance().setAttribute(CommonAttributes.SMOOTH_SHADING, false);
		
		Tool flipper = new AbstractTool(InputSlot.LEFT_BUTTON, InputSlot.SHIFT_LEFT_BUTTON) {
			boolean activated = false;
			boolean remove = false;
			@Override
			public void activate(ToolContext tc) {
				super.activate(tc);
				System.err.println("copy click: In activate");
				activated = true;
				addCurrentSlot(InputSlot.POINTER_TRANSFORMATION);
				remove = tc.getSource() == InputSlot.SHIFT_LEFT_BUTTON;
			}
			
			@Override
			public void perform(ToolContext tc) {
//				System.err.println("In perform");
				if (!activated) return;
				super.perform(tc);
			}


			@Override
			public void deactivate(ToolContext tc) {
				super.deactivate(tc);
				activated = false;
				System.err.println("copy click: In deactivate");
				removeCurrentSlot(InputSlot.POINTER_TRANSFORMATION);
				PickResult currentPick = tc.getCurrentPick(); //lastPick; //
				if (currentPick == null ||
						currentPick.getPickPath() == null || 
						currentPick.getPickType() != PickResult.PICK_TYPE_FACE) {
					return;
				}
				SceneGraphNode element = currentPick.getPickPath().getLastElement();
				if (element != tetrahedron) {
					return;
				}
				if (remove)	{
					currentPick.getPickPath().getLastComponent().setVisible(false);
					currentPick.getPickPath().getLastComponent().setPickable(false);
				}
//				System.err.println("Setting highlight face visible "+currentPick.getPickPath());
				int whichFace = currentPick.getIndex();
				System.err.println("face # = "+whichFace);
//				if (whichFace == oldIndex) return;
				SceneGraphComponent parent = currentPick.getPickPath().getLastComponent();
				SceneGraphComponent copySGC = new SceneGraphComponent("copy");
				copySGC.setGeometry(tetrahedron);
				copySGC.addChild(pointsSGC);
				parent.addChild(copySGC);
				MatrixBuilder.elliptic().reflect(planes[whichFace]).assignTo(copySGC);				
			}
		};
//		world.addTool(flipper);
		DiscreteGroup dg = Spherical3DGroup.towerOfTetrahedra(); //instanceOf("335");
		DiscreteGroupSceneGraphRepresentation dgsgr = new DiscreteGroupSceneGraphRepresentation(dg);
		dgsgr.setWorldNode(world);
		dgsgr.update();
		universe.addChild(dgsgr.getRepresentationRoot());
		RotateTool rt = new RotateTool();
		universe.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		universe.addTool(rt);
		MatrixBuilder.elliptic().translate(0, 0, -1);
		return universe;
	}
	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		FlyTool flytool = new FlyTool();
		flytool.setGain(.1);
		CameraUtility.getCameraNode(viewer).addTool(flytool);
		Camera cam = CameraUtility.getCamera( viewer);
		cam.setFar(-.1);
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(80,80,80));
	}
	/*
	 * Construct a spherical tetrahedron with dihedral angles with a given dihedral angle.
	 */
	private IndexedFaceSet getTetrahedron(double dihedralAngle) {
		WingedEdge we = new WingedEdge();
		double d = Math.cos(Math.PI-dihedralAngle);
		// calculate the necessary homogeneous coordinate so that the inner product of two
		// planes (after normalization of the planes) yields the cosine of the desired angle
		double eps = Math.sqrt((3*d+1)/(1-d));
		double[][] planes = {{1,1,1,-eps},{1,-1,-1,-eps},{-1,1,-1,-eps},{-1,-1,1,-eps}};
		double[] corner = P3.pointFromPlanes(null, planes[0], planes[1], planes[2]);
		System.err.println("corner = "+Rn.toString(corner));
		we.cutWithPlane(planes);
		return we;
	}

	public static void main(String[] args) {
		new TetrahedraTower().display();
	}
}
