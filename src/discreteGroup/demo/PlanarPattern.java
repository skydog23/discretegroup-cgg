package discreteGroup.demo;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;

import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.Graphics3D;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupColorPicker;
import de.jtem.discretegroup.core.DiscreteGroupConstraint;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.groups.WallpaperGroup;

public class PlanarPattern extends Assignment {
	DiscreteGroup g236;
	int xdim = 3, ydim = 5;
	SceneGraphComponent theWorld, tessellation, dynamicGeometry, dragToolSGC;
	DiscreteGroupSceneGraphRepresentation theTessSGR, theDynSGR;
	Color[] colors = { Color.red, Color.green, Color.blue, Color.orange, Color.yellow, Color.white};
	Viewer viewer;
	double[] m2, m3, m6;
	double[] org = {.643, .742, 0.0}, centerpoint;
	@Override
	public SceneGraphComponent getContent() {
		g236 = WallpaperGroup.instanceOfGroup("236");
		g236.setColorPicker(new DiscreteGroupColorPicker.RotationColorPicker(6));

		DiscreteGroupElement[] gens=g236.getGenerators();	
		m2 = gens[0].getArray();
		m3 = gens[1].getArray();
		m6 = gens[2].getArray();
		m6 = Rn.inverse(null, m6);

		theWorld = SceneGraphUtility.createFullSceneGraphComponent("world");
		theWorld.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
		SceneGraphComponent reps = g236.getGeneratorRepresentations();
		MatrixBuilder.euclidean().translate(0,0,.01).assignTo(reps);
		theWorld.addChild(reps);
		
		tessellation = new SceneGraphComponent("tess");
		tessellation.setAppearance(new Appearance());
		tessellation.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
		dynamicGeometry = new SceneGraphComponent("dg");
		// we have two scene graph representations for the group since we want to be
		// able to "flatten" the dynamic geometry separately
		Appearance[] aps = new Appearance[6];
		for (int i = 0; i<6; ++i)	{
			aps[i] = new Appearance();
			aps[i].setAttribute("polygonShader.diffuseColor", colors[i]);
		}
		theTessSGR = new DiscreteGroupSceneGraphRepresentation(g236);
		tessellation.setGeometry(g236.getDefaultFundamentalRegion());
		theTessSGR.setWorldNode(tessellation);
		theTessSGR.setAppList(aps);
		theWorld.addChild(theTessSGR.getRepresentationRoot());
		
		theDynSGR = new DiscreteGroupSceneGraphRepresentation(g236);
		theDynSGR.setWorldNode(dynamicGeometry);
		theWorld.addChild(theDynSGR.getRepresentationRoot());
		theDynSGR.getRepresentationRoot().getAppearance().setAttribute(CommonAttributes.PICKABLE, false);
		
		updateConstraint();
		updateDynamicGeometry(org);
		return theWorld;
	}

	protected double getYDim()	{
		return ydim;
	}
	protected void updateConstraint() {
		DiscreteGroupConstraint dgc =  new DiscreteGroupConstraint()  {
			int maxels;
		    public boolean acceptElement(DiscreteGroupElement dge)	{
			double[] tmp = new double[4];
			double[] mat = dge.getArray();
			tmp[0] = mat[3];  tmp[1] = mat[7];  tmp[2] = mat[11];  tmp[3] = mat[15];
			de.jreality.math.Pn.dehomogenize(tmp, tmp);
			if (tmp[0] < -xdim || tmp[0] > xdim) return false;
			if (tmp[1] < -ydim || tmp[1] > ydim) return false;
			return true;
		    }
		    public int getMaxNumberElements() { return 1000; }
			public void setMaxNumberElements(int i) {
				maxels = i;
			}
			public void update() {
				// TODO Auto-generated method stub
				
			}
		};
		g236.setConstraint(dgc);
		System.err.println("Resetting constraint");
		g236.update();
		theTessSGR.setElementList(g236.getElementList());
		theTessSGR.update();
		theDynSGR.setElementList(g236.getElementList());
		theDynSGR.update();
		updateDynamicGeometry(centerpoint);
	}
	IndexedLineSetFactory dynFac;
	protected void updateDynamicGeometry(double[] thePoint)	{
		centerpoint = (thePoint == null) ? org : thePoint;
		if (dynFac == null)	{
			dynFac = new IndexedLineSetFactory();
			int[][] indices = {{0,1},{0,2},{2,3},{3,1}};
			double[][] ecolors = {{.2,1,.2,1},{1,.2,.2,1},{1,1,.2,1},{1,.2,.2,1}};
			dynFac.setVertexCount(4);
			dynFac.setEdgeCount(4);
			dynFac.setEdgeIndices(indices);
			dynFac.setEdgeColors(ecolors);
		}
		double[][] points = new double[4][3];
		if (centerpoint.length == 4) de.jreality.math.Pn.dehomogenize(centerpoint, centerpoint);
		points[0][0] = centerpoint[0];
		points[0][1] = centerpoint[1];
		points[0][2] = 0.0;
		Rn.matrixTimesVector(points[1], m2, points[0]);
		Rn.matrixTimesVector(points[2], m6, points[0]);
		Rn.matrixTimesVector(points[3], m2, points[2]);
		dynFac.setVertexCoordinates(points);
		dynFac.update();
		if (dynamicGeometry.getGeometry() != dynFac.getIndexedLineSet())
			dynamicGeometry.setGeometry(dynFac.getIndexedLineSet());
	}

	double[] anchorPoint = null;
	double[] currentPoint = new double[4];
	double[] NDCToObject = null;
	Graphics3D gc = null;
	AbstractTool dragger = new AbstractTool(InputSlot.getDevice("PrimaryAction")) {
			{
				addCurrentSlot(InputSlot.getDevice("TrackballTransformation"));
			}
			public void activate(ToolContext tc) {
				System.err.println("Start tracking "+tc.getRootToLocal().toString());
				dragToolSGC.setVisible(true);
			}

			public void perform(ToolContext tc) {
				PickResult currentPick = tc.getCurrentPick();
				if (currentPick == null || currentPick.getPickPath() == null) return;
				double[] currentObject = currentPick.getObjectCoordinates();
				System.err.println("Perform");
				Pn.dehomogenize(currentObject, currentObject);
				updateDynamicGeometry(currentObject);
				MatrixBuilder.euclidean().translate(currentObject).assignTo(dragToolSGC);
				viewer.renderAsync();
			}

			public void deactivate(ToolContext tc) {
				dragToolSGC.setVisible(false);
				viewer.renderAsync();
			}

	};
	
	@Override
	public void display() {
		super.display();
		viewer = jrviewer.getViewer();
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.gray);
		CameraUtility.getCamera(viewer).setFocus(2.0);
		MatrixBuilder.euclidean().translate(0d,0d,2d).assignTo(CameraUtility.getCameraNode(viewer));
		//viewer.getSceneRoot().addTool(new ClickWheelCameraZoomTool());
		dragToolSGC = SceneGraphUtility.createFullSceneGraphComponent("drag tool");
		dragToolSGC.setOwner(this);
		dragToolSGC.getAppearance().setAttribute(CommonAttributes.PICKABLE, false);
		dragToolSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		dragToolSGC.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW, true);
		dragToolSGC.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, true);
		dragToolSGC.getAppearance().setAttribute("pointShader.pointRadius", .04);
		dragToolSGC.getAppearance().setAttribute("pointShader.polygonShader.diffuseColor",Color.red);
		PointSet ps = Primitives.point(new double[]{0,0,0});
		MatrixBuilder.euclidean().translate(org).assignTo(dragToolSGC);
		dragToolSGC.setGeometry(ps);
//		ToolManager.toolManagerForViewer(viewer).addUserTool(dragger, null, "Drag vertex");
//		ToolManager.toolManagerForViewer(viewer).activateTool(ToolManager.SELECTION_TOOL);
//		ToolManager.toolManagerForViewer(v).setActive(false);
		tessellation.addChild(dragToolSGC);
		tessellation.addTool(dragger);
//		viewer.getSceneRoot().addTool(new PickShowTool());
	}

	@Override
	public Component getInspector() {
		Viewer v = jrviewer.getViewer();
		Box inspectionPanel =  inspector; //Box.createVerticalBox();
		TextSlider slider = new TextSlider.Integer("xdim",SwingConstants.HORIZONTAL,1, 6, xdim);
		slider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				xdim = ((TextSlider) e.getSource()).getValue().intValue();
				updateConstraint();
				viewer.renderAsync();
				
			}
		});
		inspectionPanel.add(slider);
		slider = new TextSlider.Integer("yxdim",SwingConstants.HORIZONTAL,1, 6, ydim);
		slider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				ydim = ((TextSlider) e.getSource()).getValue().intValue();
				updateConstraint();
				viewer.renderAsync();
			}
		});
		inspectionPanel.add(slider);
		JCheckBox anim = new JCheckBox("show tessellation");
		anim.setSelected(tessellation.isVisible());
		anim.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tessellation.setVisible( ((JCheckBox) e.getSource()).isSelected());
			}
		});
//		inspectionPanel.add(anim);
		return inspectionPanel;
	}

	public DiscreteGroupSceneGraphRepresentation getTheDynSGR() {
		return theDynSGR;
	}

	public static void main(String[] args) {
		new PlanarPattern().display();
	}
}
