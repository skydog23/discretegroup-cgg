/*
 * Created on Jan 17, 2011
 *
 */
package discreteGroup.demo;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.tools.MouseTool;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.ThickenedSurfaceFactory;
import de.jreality.jogl.plugin.HelpOverlay;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.groups.TriangleGroup;
import discreteGroup.util.ArchimedeanSolidsUtility;

public class FanSolid extends Assignment {
	String type = "101";
	String group = "*235";
	SceneGraphComponent[] three = new SceneGraphComponent[3];
	String[] names = {"s0","s1", "s2"};
	Disk[] thickDisks = new Disk[3], sixDisks = new Disk[6], currentlyInspected;
	static Color[] colors = {
		new Color(255,255,0),	
		new Color(255,0,0),		
		new Color(0, 80, 255),
		new Color(80, 255, 80),
		new Color(255,80,255)};
	Color[] houseColors = {
			new Color(255,0,0),
			new Color(0,80,255)};
	int[] sides = {4, 6, 10};
	double[][] holeprofile = {{0,0}, {0,1}, {1,1}, {1,0}};
	double saturated = 0.0;
	boolean makeHoles = false,
		linearHoles = true,
		fiveCubes = true,		// specialize to case of 15 elements arranged as 5 cubes
		doHouse = false,
		fiveSideHouse = true;
	int[][] houseGroups = {{0,1,5},{2,3,4}};
	int[] houseInds = {0,0,1,1,1,0};
	int[] houseSides = {5,5, 10};
	double[] houseRadii = {1.0, 1.0, .852};
	double[] housePhases1 = {0,.5};
	double[] housePhases = {.5,0,.5,0,.5,0};
	SceneGraphComponent world, children[] = new SceneGraphComponent[3];
	SceneGraphComponent fixer = null;
	@Override
	public SceneGraphComponent getContent() {
		if (world != null) return world;
		IndexedFaceSet disk = null;
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		world.getAppearance().setAttribute("polygonShader.diffuseColor", java.awt.Color.white);
		world.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW,false);
		TriangleGroup tg = TriangleGroup.instanceOfGroup(group);
		double[][] tri = tg.getTriangle(); // ReflectionPlanes();
		for (int i = 0; i < 3; ++i)	{
			thickDisks[i] = new Disk();
//			thickDisks[i].setColor(colors[i]);
			thickDisks[i].setNumSides(sides[i]);
			thickDisks[i].update();
			three[i] = thickDisks[i].getThickDiskSGC();
			children[i] = ArchimedeanSolidsUtility.polarOrbit(group, type, tri[i], three[i]);
			children[i].getAppearance().setAttribute("polygonShader.diffuseColor", colors[i]);
			if (!doHouse) {
				world.addChild(children[i]);
				children[i].setVisible(!fiveCubes || i == 0);
			}
			if (fiveCubes && i == 0) {
				setupFiveCubes();
			}
			else if (i == 2) fixer = children[i];
		}
		// TODO: reduce to two groups of disks, one for 0,1,5 with 5 sides  (0 has phase .5)
		//    the second for 2,3,4   there are also phase changes here too
		if (doHouse) {
			world.addChild(fixer);
			fixer.setName("fixer");
			for (int i = 0; i < 6; ++i)	{
				sixDisks[i] = new Disk();
				sixDisks[i].setColor(houseColors[houseInds[i]]);
				sixDisks[i].setNumSides(houseSides[houseInds[i]]);
				sixDisks[i].setRadius(houseRadii[houseInds[i]]);
				sixDisks[i].setPhase(housePhases[i]);
				sixDisks[i].update();
				SceneGraphUtility.removeChildren(fixer.getChildComponent(i).getChildComponent(0));
				fixer.getChildComponent(i).getChildComponent(0).addChild(sixDisks[i].getThickDiskSGC());
				fixer.addTool(specialTool);
			}			
		}
//		fixer.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);

		currentlyInspected = doHouse ? sixDisks : thickDisks;
		updateColors();
		return world;
	}

	private int cubeIndices[][] = new int[5][3];
	private void setupFiveCubes() {
		// find 5 cosets
		ArrayList<List<Integer>> cosets = new ArrayList<List<Integer>>();
		Matrix[] matrices = new Matrix[15];
		for (int j = 0; j<15; ++j)	{
			matrices[j] = new Matrix(children[0].getChildComponent(j).getChildComponent(0).getTransformation());
		}
		for (int j = 0; j<15; ++j)	{
			boolean found = false;
			Matrix thisM = matrices[j];
			for (List<Integer> oneCoset : cosets) {
				int k = oneCoset.get(0);
				double xx = Rn.innerProduct(thisM.getColumn(0), matrices[k].getColumn(0));
				double yy = Rn.innerProduct(thisM.getColumn(1), matrices[k].getColumn(1));
				double zz = Rn.innerProduct(thisM.getColumn(2), matrices[k].getColumn(2));
				double diff = Math.abs(xx)+Math.abs(yy)+Math.abs(zz);
				if (diff < 10E-6) {
					found = true;
					oneCoset.add(j);
					break;
				}
			}
			if (!found) {
				ArrayList<Integer> newlist = new ArrayList<Integer>();
				newlist.add(j);
				cosets.add(newlist);
			}
		}
		System.err.println(cosets.size()+" cosets");
		for (int k  = 0; k<cosets.size(); ++k) {
			System.err.println(k+" "+cosets.get(k).size());
			for (int m = 0; m<3; ++m)	{
				cubeIndices[k][m] = cosets.get(k).get(m);
			}
		}
	}

	protected void updateColors() {
		for (int i = 0; i<3; ++i) {
			if (i>0 || !fiveCubes) {
				Color tmp = AnimationUtility.linearInterpolation( Color.white, colors[i], saturated);
				children[i].getAppearance().setAttribute("polygonShader.diffuseColor", tmp);
			}
			else {
				for (int j = 0; j<5; ++j)	{
					Color tmp = AnimationUtility.linearInterpolation( Color.white, colors[j], saturated);
					for (int m = 0; m<3; ++m)	{
						Appearance ap = children[0].getChildComponent(cubeIndices[j][m]).getChildComponent(0).getAppearance();
						ap.setAttribute("polygonShader.diffuseColor", tmp);
					}
				}
			}
		}
	}

	@Override
	public void display() {
		super.display();
		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute("backgroundColor", Color.black);
		if (fixer != null) pathToFixer = SceneGraphUtility.getPathsBetween(jrviewer.getViewer().getSceneRoot(), fixer).get(0);
		((Component) jrviewer.getViewer().getViewingComponent()).addKeyListener(new KeyAdapter() {
		
			@Override
			public void keyPressed(KeyEvent e) {
					switch(e.getKeyCode())	{
				
				case KeyEvent.VK_H:
					break;
	
				case KeyEvent.VK_1:
					toggleVisibility(0);
					break;
				case KeyEvent.VK_2:
					toggleVisibility(1);
					break;
				case KeyEvent.VK_3:
					toggleVisibility(2);
					break;
				}
			}

		});
	}
	
	SceneGraphPath pathToFixer = null;
	ArrayList<double[]> verts = new ArrayList<double[]>();
	Tool specialTool = new MouseTool()	{
		
		public void perform(ToolContext tc) {
//			System.err.println("In perform");
			super.perform(tc);
		}


		@Override
		public void activate(ToolContext tc) {
			super.activate(tc);
//			verts.clear();
		}


		@Override
		public void deactivate(ToolContext tc) {
			super.deactivate(tc);
//			System.err.println(tc.getCurrentPick().toString());
			if (button == 1 || button == 2)	{
				if (tc.getCurrentPick() == null) return;
//				if (tc.getCurrentPick().getPickType() != PickResult.PICK_TYPE_POINT) return;
				int whichVert = tc.getCurrentPick().getIndex();
				SceneGraphPath sgp = tc.getCurrentPick().getPickPath();
				System.err.println("pick path = "+sgp.toString());
				double[] objC = tc.getCurrentPick().getWorldCoordinates();
				double[] fixedC = convertCoordinates(objC, pathToFixer, sgp);
				verts.add(fixedC);
				System.err.println("vertex"+whichVert);
			} 
			if (button == 2)	{
				System.err.println("button2");
				if (verts.size() >= 3) {
					double[][] vertsA = verts.toArray(new double[verts.size()][]);
					IndexedFaceSet face = IndexedFaceSetUtility.constructPolygon(vertsA);
					SceneGraphComponent sgc = new SceneGraphComponent();
					sgc.setGeometry(face);
					fixer.addChild(sgc);	
					System.err.println("adding face");
					verts.clear();
				}
			}
		}
		
		@Override
		public ImageIcon getIcon(int size) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}	


		@Override
		public void registerHelp(HelpOverlay overlay) {
			// TODO Auto-generated method stub
			
		}
		
	};

	private void toggleVisibility(int i) {
		three[i].setVisible(!three[i].isVisible());
		thickDisks[i].visBox.setSelected(three[i].isVisible());
	}
	
	protected double[] convertCoordinates(double[] objC,
			SceneGraphPath pathToFixer2, SceneGraphPath sgp) {
		double[] w2F = pathToFixer2.getInverseMatrix(null);
		double[] p2W = sgp.getMatrix(null);
		double[] p2F = Rn.times(null, w2F, p2W);
		return Rn.matrixTimesVector(null, w2F, objC);
	}

	public class Disk {
		protected int numSides = 40;
		protected double thickness = .025,
			phase = 0.0,
			radius = 1.0,
			holeSize = .5;
		protected Color color = Color.white;
		protected IndexedFaceSet disk = Primitives.regularPolygon(numSides),
			thickDisk;
		protected ThickenedSurfaceFactory thickDiskFactory = new ThickenedSurfaceFactory(disk);
		protected SceneGraphComponent thickDiskSGC = SceneGraphUtility.createFullSceneGraphComponent("thickDisk");
		double saturated = 0.0;
		private TextSlider thickSlider;
		private TextSlider radiusSlider;
		private TextSlider phaseSlider;
		private TextSlider numSlider;
		private Box panel;
		protected JCheckBox visBox;
		
		public Disk() {
			getInspector();
			thickDiskFactory.setLinearHole(linearHoles);
			thickDiskFactory.setMakeHoles(makeHoles);
			thickDiskFactory.setProfileCurve(holeprofile);
			update();
			thickDisk = thickDiskFactory.getThickenedSurface();
			thickDiskSGC.setGeometry(thickDisk);
		}
		protected void update() {
			updateGeometry();
			updateTransformation();
//			updateColors();
		}
		private void updateTransformation() {
			MatrixBuilder.euclidean().scale(radius).assignTo(thickDiskSGC);
		}
		private void updateGeometry() {
			disk = Primitives.regularAnnulus(numSides, phase, holeSize);
			thickDiskFactory.setSurface(disk);
			thickDiskFactory.setThickness(thickness);
			thickDiskFactory.update();
		}
		protected void updateColors() {
			Color tmp = AnimationUtility.linearInterpolation( Color.white,color, saturated);
			thickDiskSGC.getAppearance().setAttribute("polygonShader.diffuseColor", tmp); 
	}
		public int getNumSides() {
			return numSides;
		}
		public void setNumSides(int numSides) {
			this.numSides = numSides;
			numSlider.setValue(numSides);
		}
		public double getThickness() {
			return thickness;
		}
		public void setThickness(double thickness) {
			this.thickness = thickness;
			thickSlider.setValue(thickness);
		}
		public double getRadius() {
			return radius;
		}
		public void setRadius(double radius) {
			this.radius = radius;
			radiusSlider.setValue(radius);
		}
		public double getPhase() {
			return phase;
		}
		public void setPhase(double phase) {
			this.phase = phase;
			phaseSlider.setValue(phase);
		}
		public double getHoleSize() {
			return holeSize;
		}
		public void setHoleSize(double holeSize) {
			this.holeSize = holeSize;
		}
		public double getSaturated() {
			return saturated;
		}
		public void setSaturated(double saturated) {
			this.saturated = saturated;
		}
		public Color getColor() {
			return color;
		}
		public void setColor(Color color) {
			this.color = color;
		}
		public IndexedFaceSet getThickDisk() {
			return thickDisk;
		}
		public void setThickDisk(IndexedFaceSet thickDisk) {
			this.thickDisk = thickDisk;
		}
		public SceneGraphComponent getThickDiskSGC() {
			return thickDiskSGC;
		}
		public void setThickDiskSGC(SceneGraphComponent thickDiskSGC) {
			this.thickDiskSGC = thickDiskSGC;
		}
		public Box getInspector() {
			if (panel != null) return panel;
			panel = Box.createVerticalBox();
			visBox = new JCheckBox("Visible");
			visBox.setSelected(thickDiskSGC.isVisible());
			visBox.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					thickDiskSGC.setVisible(visBox.isSelected());
				}
			});
			panel.add(visBox);
			thickSlider = new TextSlider.Double("thickness",SwingConstants.HORIZONTAL,0.0,.10, thickness);
			thickSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					thickness = thickSlider.getValue().doubleValue();
					updateGeometry();
				}
			});
			panel.add(thickSlider);
			
			radiusSlider = new TextSlider.Double("radius",SwingConstants.HORIZONTAL,0.0, 2.0, radius);
			radiusSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					radius = radiusSlider.getValue().doubleValue();
					updateTransformation();
				}
			});
			panel.add(radiusSlider);
			
			phaseSlider = new TextSlider.Double("phase",SwingConstants.HORIZONTAL,-1.0,1.0, phase);
			phaseSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					phase = phaseSlider.getValue().doubleValue();
					updateGeometry();
				}
			});
			panel.add(phaseSlider);
			
			numSlider = new TextSlider.Integer("num sides",SwingConstants.HORIZONTAL,0, 100, 6);
			numSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					numSides = numSlider.getValue().intValue();
					updateGeometry();
				}
			});
			panel.add(numSlider);

			return panel;
		}
	}

	@Override
	public Component getInspector() {
		getContent();
		Box panel = Box.createVerticalBox();
		panel.setName("Parameters");
		final TextSlider minTSlider = new TextSlider.Double("override thickness",SwingConstants.HORIZONTAL,0.0,.10, .025);
		minTSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				double thickness = minTSlider.getValue().doubleValue();
				for (Disk d : currentlyInspected)	{
					d.setThickness(thickness);
					d.update();
				}
			}
		});
		panel.add(minTSlider);
		final TextSlider holeSlider = new TextSlider.Double("hole radius",SwingConstants.HORIZONTAL,0.0,1, .5);
		holeSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				double holeSize = holeSlider.getValue().doubleValue();
				for (Disk d : currentlyInspected)	{
					d.setHoleSize(holeSize);
					d.update();
				}
			}
		});
		panel.add(holeSlider);

		final TextSlider saturationSlider = new TextSlider.Double("saturation",SwingConstants.HORIZONTAL,0.0,1,saturated);
		saturationSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				saturated = ((TextSlider) e.getSource()).getValue().doubleValue();
				updateColors();
			}

		});
		panel.add(saturationSlider);
		if (doHouse)	{
			final JCheckBox fiveCB = new JCheckBox("pentagon");
			fiveCB.setSelected(fiveSideHouse);
			fiveCB.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					fiveSideHouse = fiveCB.isSelected();
					updateHouseConfiguration();
				}
			});
			panel.add(fiveCB);			
		}
		JTabbedPane tabs = new JTabbedPane();
		panel.add(tabs);
		if (!doHouse)	{
			for (int i = 0; i<3; ++i)	{
				final int j= i;
				Box insp = thickDisks[i].getInspector();
				tabs.add("Vertex"+i, insp);
			}			
		} else {
			for (int i = 0; i<6; ++i)	{
				final int j= i;
				Box insp = sixDisks[i].getInspector();
				tabs.add("Plane"+i, insp);
			}						
		}
		return panel;
	}

	protected void updateHouseConfiguration() {
		for (int i = 0; i<6; ++i)	{
			Disk d = sixDisks[i];
			if (houseInds[i] == 1) {
				d.setNumSides(houseSides[ fiveSideHouse ? 1 : 2]);
				d.setRadius(houseRadii[ fiveSideHouse ? 1 : 2]);
				d.update();
			}
		}
		// one little detail ...
		sixDisks[3].setPhase(fiveSideHouse ? 0.5 : 0.0);
	}
	
	public static void main(String[] args) {
		new FanSolid().display();
	}
//	int currentPlane = 0, currentPhase = 0;
//	boolean fiveSided = true;
//	private JComponent sixPlaneInspector()  {
//		Box hbox = Box.createVerticalBox();
//		TitledBorder title = BorderFactory.createTitledBorder(
//				BorderFactory.createEtchedBorder(), "customize planes");
//		final TextSlider whichSlider = new TextSlider.Integer("which plane",SwingConstants.HORIZONTAL,0, 5, currentPlane);
//		whichSlider.addActionListener(new ActionListener()	{
//			public void actionPerformed(ActionEvent e)	{
//				currentPlane = whichSlider.getValue().intValue();
//				updatePlanes();
//			}
//		});
//		hbox.add(whichSlider);
//		final TextSlider phaseSlider = new TextSlider.Integer("which phase",SwingConstants.HORIZONTAL,0, 3, currentPhase);
//		phaseSlider.addActionListener(new ActionListener()	{
//			public void actionPerformed(ActionEvent e)	{
//				currentPhase = phaseSlider.getValue().intValue();
//				updatePlanes();
//			}
//		});
//		hbox.add(phaseSlider);
//		
//		final JCheckBox fiveCB = new JCheckBox("pentagon");
//		fiveCB.setSelected(fiveSided);
//		fiveCB.addActionListener(new ActionListener()	{
//			public void actionPerformed(ActionEvent e)	{
//				fiveSided = fiveCB.isSelected();
//				updatePlanes();
//			}
//		});
//		hbox.add(fiveCB);
//		return hbox;
//	}
//
//	private void updatePlanes() {
//		SceneGraphComponent currentSGC = sixDisks[currentPlane].getThickDiskSGC();
////		MatrixBuilder.euclidean().rotateZ(currentPhase * Math.PI/4.0).assignTo(currentSGC);
//		sixDisks[currentPlane].setPhase(currentPhase*.25 + thickDisks[2].getPhase());
//		sixDisks[currentPlane].setNumSides(fiveSided ? 5 : 10);
//		sixDisks[currentPlane].update();
//	}
}
