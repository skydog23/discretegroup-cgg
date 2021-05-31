/*
 * Created on Apr 5, 2007
 *
 */
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
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.FrameFieldType;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupUtility;

public class SixHundredCell extends Assignment {

	private SceneGraphComponent cube;
	double scale = .9;
	private SceneGraphComponent bothcubes, theSGRepn;
	@Override
	public SceneGraphComponent getContent() {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		SceneGraphComponent cubefaces = SceneGraphUtility.createFullSceneGraphComponent("cubefaces");
		cube = SceneGraphUtility.createFullSceneGraphComponent("cube");
		IndexedFaceSet cubex = Primitives.cube4(true);
		
		cube.setGeometry(IndexedLineSetUtility.refine(cubex,6));
		cubefaces.setGeometry(cubex);
		cubefaces.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		cube.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
		bothcubes = new SceneGraphComponent("bothcubes");
		bothcubes.addChild(cube);
		bothcubes.addChild(cubefaces);
		MatrixBuilder.euclidean().scale(scale).assignTo(bothcubes);
		
		DiscreteGroup dg = new DiscreteGroup();
		dg.setDimension(3);
		DiscreteGroupElement[] gens = new DiscreteGroupElement[8];
		double[][] from = {{-1,0,0,1},{0,-1,0,1},{0,0,-1,1}};
		double[][] to = {{1,0,0,1},{0,1,0,1},{0,0,1,1}};
		gens[0] = new DiscreteGroupElement();
		gens[4] = new DiscreteGroupElement(Pn.ELLIPTIC, Rn.diagonalMatrix(null, new double[]{-1,-1,-1,-1}));
		for (int i = 0; i<3; ++i)	{
			gens[i+1] =  new DiscreteGroupElement( Pn.ELLIPTIC, P3.makeTranslationMatrix(null, from[i], to[i], Pn.ELLIPTIC));
			gens[i+1].setWord(DiscreteGroupUtility.genNames[i]);
			gens[i+5] = (DiscreteGroupElement) gens[i+1].getInverse();
		}
		Color[] colorlist = {
				Color.white,
				Color.red,
				Color.blue,
				Color.green,
				Color.black,
				Color.cyan,
				Color.yellow,
				new Color(255,0,255),
		};
		Appearance[] aplist = new Appearance[8];
		for (int i = 0; i<8; ++i)	{
			gens[i].setColorIndex(i);
			aplist[i] = new Appearance();
			aplist[i].setAttribute("lineShader.polygonShader.diffuseColor", colorlist[i]);
			aplist[i].setAttribute("pointShader.polygonShader.diffuseColor", colorlist[(i+4)%8]);
			aplist[i].setAttribute("polygonShader.diffuseColor", colorlist[i]);
		}
		
		dg.setElementList(gens);
		dg.setFinite(true);
		dg.setMetric(Pn.ELLIPTIC);
		DiscreteGroupSceneGraphRepresentation dgr = new DiscreteGroupSceneGraphRepresentation(dg, false);
		dgr.setAppList(aplist);
		dgr.setElementList(dg.getElementList());
		dgr.setWorldNode(bothcubes);
		dgr.update();
		theSGRepn = dgr.getSceneGraphRepn();
		for (int i = 1; i<8; ++i)	{
			theSGRepn.getChildComponent(i).setVisible(false);
		}
		world.addChild(dgr.getRepresentationRoot());
		world.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		world.getAppearance().setAttribute("polygonShader."+CommonAttributes.TRANSPARENCY, .85);
		world.getAppearance().setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, .02);
		world.getAppearance().setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .02);
		world.getAppearance().setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, true);
		world.getAppearance().setAttribute("lineShader."+CommonAttributes.TUBE_STYLE, FrameFieldType.FRENET);
		world.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, true);
		world.getAppearance().setAttribute("useGLSL", true);
		MatrixBuilder.elliptic().translate(new double[]{0,0,-1,0}).assignTo(world);
		return world;
	}

	@Override
	public Component getInspector() {	
		Box inspectionPanel =  inspector;
		Box animateBox =  Box.createHorizontalBox();
		String[] labels = {"1","-x","+x","-y","+y","-z","+z","-1"};
		final int[] offsets = {0,5,1,6,2,7,3,4};
		for (int i = 0; i<8; ++i)	{
			JCheckBox animate = new JCheckBox(labels[i]);
			animate.setSelected(theSGRepn.getChildComponent(offsets[i]).isVisible());
			final int j = i;
			animate.addActionListener( new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					boolean b = ((JCheckBox) e.getSource()).isSelected();
					theSGRepn.getChildComponent(offsets[j]).setVisible(b);
					viewer.renderAsync();
				}
			});
			animateBox.add(animate);
			
		}
		inspectionPanel.add(animateBox);
		final TextSlider timeSlider = new TextSlider.Double("scale",SwingConstants.HORIZONTAL,0.0,1.0,scale);
		timeSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				scale = timeSlider.getValue().doubleValue();
				MatrixBuilder.euclidean().scale(scale).assignTo(bothcubes);
				viewer.renderAsync();
			}
		});
		inspectionPanel.add(timeSlider);
		return inspectionPanel;
	}


	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		CameraUtility.getCamera(viewer).setFieldOfView(110.0);
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.METRIC, Pn.ELLIPTIC);
	}
	
	public static void main(String[] args) {
		new SixHundredCell().display();
	}
	
}
//double[][] verts = new double[16][4];
//double[][] colors = new double[16][4];
//
//for (int w = 0; w <= 1; w+=1)	{
//	for (int z = 0; z <= 1; z+=1)	{
//		for (int y = 0; y <= 1; y += 1)	{
//			for (int x = 0; x <= 1; x+=1)	{
//				verts[w*8+z*4+y*2+x][0] = 2*x-1;
//				verts[w*8+z*4+y*2+x][1] = 2*y-1;
//				verts[w*8+z*4+y*2+x][2] = 2*z-1;
//				verts[w*8+z*4+y*2+x][3] = 2*w-1;
//				colors[w*8+z*4+y*2+x][0] = x;
//				colors[w*8+z*4+y*2+x][1] = y;
//				colors[w*8+z*4+y*2+x][2] = z;
//				colors[w*8+z*4+y*2+x][3] = 1;
//			}
//		}
//	}
//}
//int[][] edges = null;
//ArrayList<int[]> edgelist = new ArrayList<int[]>();
//for (int i = 0, count = 0; i<16; ++i)	{
//	for (int j = i+1; j<16; ++j)	{
//		double d = Rn.euclideanDistanceSquared(verts[i], verts[j]);
//		if (d == 4) {
//			edgelist.add(new int[]{i,j});
//			System.err.println("found edge"+i+j);
////			edges[count][0] = i;
////			edges[count][1] = j;
////			count++;
//		}
//	}
//}
//edges = edgelist.toArray(new int[edgelist.size()][2]);
//int[][] basecube = {
//		{0,1,3,2},
//		{6,7,5,4}, //{4,5,6,7},
//		{0,2,6,4},
//		{5,7,3,1}, //{1,3,7,5},
//		{0,4,5,1},
//		{3,7,6,2}  //{2,6,7,3}
//};
//int[][] indices = new int[12][4]; //48][4];
//for (int i = 0; i<6; ++i)	{
//	for (int j = 0; j<4; ++j) {	
//		indices[i][j] = basecube[i][j];
//		indices[i+6][j] = basecube[i][j] + 8;
//	}
//}
//IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
//ifsf.setVertexCount(16);
//ifsf.setVertexCoordinates(verts);
//ifsf.setVertexColors(colors);
//ifsf.setLineCount(edges.length);
//ifsf.setEdgeIndices(edges);
//ifsf.setFaceCount(12);
//ifsf.setFaceIndices(indices);
//ifsf.setGenerateEdgesFromFaces(false);
//ifsf.update();
////world.setGeometry(ifsf.getIndexedFaceSet());
