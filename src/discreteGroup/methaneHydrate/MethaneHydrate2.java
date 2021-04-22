package discreteGroup.methaneHydrate;

import java.awt.Color;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JMenuBar;

import charlesgunn.jreality.tools.MouseTool;
import charlesgunn.jreality.tools.ToolManager;
import charlesgunn.jreality.tools.UserTool;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.jogl.plugin.HelpOverlay;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.util.WingedEdge;
import discreteGroup.tools.CopyClickTool;

public class MethaneHydrate2 extends LoadableScene {
	WingedEdge dirDom = null;
	private DiscreteGroupSceneGraphRepresentation theRepn;
	private Vector<DiscreteGroupElement> els;
	private DiscreteGroup fullGroup;
	private DiscreteGroup dirdomGroup;
	private double[][] facecolors;
	double rodRadius = .5;
	SceneGraphComponent world, highlightFace = new SceneGraphComponent();
	@Override
	public SceneGraphComponent makeWorld() {
		MethaneHydrateUtility mhg = new MethaneHydrateUtility();
		fullGroup = mhg.getFullGroup2();
//		fullGroup.setMaxNumberElements(1);
		fullGroup.update();
		els = new Vector<DiscreteGroupElement>();
		els.add(new DiscreteGroupElement(Pn.EUCLIDEAN, Rn.identityMatrix(4), ""));
//		double[][] base = {{1,1,-1},{1,-1,-1},{-1,-1,-1},{-1,1,-1}};
//		double[] tip = {0,0,0};
//		IndexedFaceSet pyr = Primitives.pyramid(base, tip);
		dirdomGroup = mhg.getFullGroup2();
//		dirdomGroup.setMaxNumberElements(100);
		dirdomGroup.setCenterPoint(new double[]{1,1,.5});
		dirdomGroup.setElementList(DiscreteGroupUtility.generateElements(dirdomGroup, null));
//		dirDom = (WingedEdge) DiscreteGroupUtility.calculateDirichletDomain(dirdomGroup);
//		double[] opaque = {1,1,0,1};
//		double[] transp = {1,1,0,0};
//		facecolors = new double[dirDom.getNumFaces()][];
//		for (int i = 0; i<dirDom.getNumFaces(); ++i) facecolors[i] = transp;
//		dirDom.setFaceAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(4).createReadOnly(facecolors));
//		dirichletDomain.setGeometry(dirDom);
//		dirichletDomain.addTool(getSpecialTool());
//		Appearance ap = new Appearance();
//		ap.setAttribute("polygonShader.transparency", 1.0);
//		dirichletDomain.setAppearance(ap);
//		double[][] vs = dirDom.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
//		double[] c = Rn.average(null, vs);
//		MatrixBuilder.euclidean().translate(c).scale(.5).translate(-c[0],-c[1], -c[2]).assignTo(dirichletDomain);
//		dirichletDomain.setVisible(true);
		theRepn = new  DiscreteGroupSceneGraphRepresentation(fullGroup, false, "Point" );
//		dirichletDomain.addTool(new CopyClickTool(dirdomGroup,theRepn));
		
		IndexedFaceSet vor = MethaneHydrateUtility.getVoronoiGeometry(0.26, 0.37, 0.16).getIndexedFaceSet();
		SceneGraphComponent voronoiSGC = new SceneGraphComponent("voronoi");
		voronoiSGC.setGeometry(vor);
		Appearance ap = new Appearance();
		voronoiSGC.setAppearance(ap);
		ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
		SceneGraphComponent collector = new SceneGraphComponent("Collector");
//		collector.addChild(dirichletDomain);
		collector.addChild(voronoiSGC);
		IndexedFaceSet rodifs = Primitives.cylinder(8, 1, -1, 1, Math.PI/2);
		rodifs.setName("rods");
		SceneGraphComponent rodsSGC = new SceneGraphComponent("rodsSGC");
		//Appearance ap = new Appearance();
		ap.setAttribute("polygonShader"+"."+CommonAttributes.DIFFUSE_COLOR, new Color(250, 250, 200));
		ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
//		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		rodsSGC.setAppearance(ap);
		rodsSGC.setGeometry(rodifs);
		rodsSGC.setVisible(false);
		MatrixBuilder.euclidean().translate(2,0,.5).scale(rodRadius, rodRadius, .5).assignTo(rodsSGC);
		
		theRepn.setWorldNode(collector);
		collector.addChild(rodsSGC);
		//theRepn.setElementList(fullGroup.getElementList());
		updateElList();
		
		world = new SceneGraphComponent();
		world.addChild(theRepn.getRepresentationRoot());
		world.addChild(highlightFace);
		highlightFace.setTransformation(new Transformation());
		return world;
	}

	@Override
	public void customize(JMenuBar menuBar, Viewer viewer) {
		CopyClickTool origcct = new CopyClickTool(dirdomGroup,theRepn);
		UserTool cct = origcct.wrapCCT();
//		CopyClickTool cct = new CopyClickTool(dirdomGroup,theRepn);
		ToolManager.toolManagerForViewer(viewer).addUserTool(cct, null, "click to copy");
		SceneGraphComponent dirichletDomain = origcct.getSceneGraphComponent();
		dirichletDomain.addTool(cct);
		theRepn.getFundamentalRegion().addChild(dirichletDomain);
	}
	private void updateElList()	{
		DiscreteGroupElement[] dgel = new DiscreteGroupElement[els.size()];
		els.toArray(dgel);
		theRepn.setElementList(dgel);
		theRepn.update();
	}
	Tool getSpecialTool()	{
		Tool specialTool = new MouseTool()	{
			
			public void perform(ToolContext tc) {
				System.err.println("In perform");
				super.perform(tc);
				PickResult currentPick = tc.getCurrentPick();
				if (currentPick == null) {
					highlightFace.setVisible(false);
					return;
				}
				// deactivate until it works -- currently the matrix is wrong
				highlightFace.setVisible(true);
				highlightFace.getTransformation().setMatrix(
//						Rn.times(null, Rn.inverse(null, world.getTransformation().getMatrix(null)),
								currentPick.getPickPath().getMatrix(null,2)); //);
				if (currentPick.getPickType() != PickResult.PICK_TYPE_FACE) return;
				int whichFace = currentPick.getIndex();
				IndexedFaceSet face = IndexedFaceSetUtility.constructPolygon(dirDom.getFaceWithIndex(whichFace));
				highlightFace.setGeometry(face);
				viewer.render();
				System.err.println("Face"+whichFace);
			}


			@Override
			public void deactivate(ToolContext tc) {
				super.deactivate(tc);
//				System.err.println(tc.getCurrentPick().toString());
				PickResult currentPick = tc.getCurrentPick();
				if (currentPick == null) return;
				SceneGraphPath sgp = tc.getRootToLocal();
//				while ( sgp.getLastComponent().getTransformation() == null || 
//						!(sgp.getLastComponent().getTransformation() instanceof DiscreteGroupElement) ){
//					if (sgp.getLength() == 0) 
//						break;
//					sgp.pop();					
//				}
				if (sgp.getLength() == 0) return;
				if (button == 1)	{		// add a copy on this face
					int whichFace = currentPick.getIndex();
					int whichEl = dirDom.faceList.get(whichFace).index;
					System.err.println("Face, generator: "+whichFace+" "+whichEl);
//					DiscreteGroupElement oldel = ((DiscreteGroupElement) sgp.getLastComponent().getTransformation());
//					double[] newmat = Rn.times(null, oldel.getMatrix(), dirdomGroup.getElementList()[whichEl].getMatrix());
//					String newword = oldel.getWord() + dirdomGroup.getElementList()[whichEl].getWord();
////					System.err.println("DGE matrix is "+Rn.matrixToString(oldel.getMatrix()));
//					
//					DiscreteGroupElement newone = new DiscreteGroupElement(Pn.EUCLIDEAN, newmat, newword);
//					els.add(newone);
					
				} else if (button == 2)	{		// remove this copy
//					DiscreteGroupElement oldel = ((DiscreteGroupElement) sgp.getLastComponent().getTransformation());
//					els.remove(oldel);

				}
				updateElList();
				highlightFace.setVisible(false);
				viewer.renderAsync();
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
		return specialTool;
	}

	@Override
	public boolean isEncompass() {
		return true;
	}


}
