package discreteGroup.maniview;

import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.RMAN_PROXY_COMMAND;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY_ENABLED;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.USE_GLSL;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.util.List;
import java.util.Vector;

import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.jreality.geometry.projective.CircleFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.util.WingedEdge;

public class SkewerTool extends AbstractTool {
	static InputSlot mouseOver = InputSlot.POINTER_HIT;
	static InputSlot facePickActivate = InputSlot.LEFT_BUTTON;
	static InputSlot edgePickActivate = InputSlot.MIDDLE_BUTTON;
	static InputSlot timer = InputSlot.SYSTEM_TIME;

	DiscreteGroup dg;
	WingedEdge dirDom;
	DiscreteGroupSceneGraphRepresentation theRepn;
	SceneGraphComponent dirichletDomain = new SceneGraphComponent(),
		planesSGC;
	int metric,
		numPlanes = 8;
	boolean leftMouseDown, middleMouseDown;
	PickResult anchorPoint;
	DragInFacePlaneTool difpt = new DragInFacePlaneTool();
	Viewer viewer;
	public SkewerTool(DiscreteGroup dg, DiscreteGroupSceneGraphRepresentation r, Viewer v)	{
//		super(facePickActivate, edgePickActivate);
		viewer = v;
		addCurrentSlot(facePickActivate);
		addCurrentSlot(edgePickActivate);
		addCurrentSlot(InputSlot.POINTER_TRANSFORMATION);
		this.dg = dg;
		metric = dg.getMetric();
		theRepn = r;
		DirichletDomain dirdom = new DirichletDomain(dg);
		dirdom.update();
		dirDom = (WingedEdge)dirdom.getDirichletDomain(); //DiscreteGroupUtility.calculateDirichletDomain(null, dg);		
//	    AABBTree aabb = AABBTree.construct(dirDom, 2);
//	    dirDom.setGeometryAttributes(PickUtility.AABB_TREE, aabb);
		Appearance ap = new Appearance();
		ap.setAttribute("polygonShader.transparency", 1.0);
		ap.setAttribute(TRANSPARENCY_ENABLED, true);
		ap.setAttribute(VERTEX_DRAW, false);
		ap.setAttribute(EDGE_DRAW, false);
		ap.setAttribute(TUBES_DRAW, false);
		ap.setAttribute(FACE_DRAW, true);
		ap.setAttribute(RMAN_PROXY_COMMAND,"");
		dirichletDomain.setGeometry(dirDom);
		dirichletDomain.setAppearance(ap);
		dirichletDomain.setVisible(true);
		dirichletDomain.setPickable(true);
		// important: this separates the identified faces, thereby separating 
		// the pick points and saves us headaches in sorting double hits
		MatrixBuilder.euclidean().scale(.998).assignTo(dirichletDomain);
		planesSGC = SceneGraphUtility.createFullSceneGraphComponent("skewerTool planes");
		ap = planesSGC.getAppearance();
		ap.setAttribute(TRANSPARENCY_ENABLED, true);
		ap.setAttribute("polygonShader.transparency", .8);
		ap.setAttribute(VERTEX_DRAW, false);
		ap.setAttribute(EDGE_DRAW, false);
		ap.setAttribute(FACE_DRAW, true);
		//ap.setAttribute(CommonAttributes.ANY_DISPLAY_LISTS, false);
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		ap.setAttribute(USE_GLSL, false);
		for (int i= 0; i<numPlanes; ++i)	{
			SceneGraphComponent sgc = new SceneGraphComponent("skewerTool plane"+i);
			planesSGC.addChild(sgc);
			if (metric == Pn.HYPERBOLIC)	{
				SceneGraphComponent plane = new SceneGraphComponent("skewerTool plane"+i);
				plane.setGeometry(GeometryUtilityOverflow.getDisk(60, 2, dg.getMetric())); //Primitives.regularPolygon(100));
				sgc.addChild(plane);				
			}
			sgc.setVisible(false);
		}
		theRepn.getRepresentationRoot().addChild(planesSGC);
//		planesSGC.setVisible(false);
		planesSGC.setPickable(false);
		
	}
	
	public SceneGraphComponent getFundamentalDomain() {
		return dirichletDomain;
	}
	
	public SceneGraphComponent getPlanes()	{
		return planesSGC;
	}
	
	public void activate()	{
		planesSGC.setVisible(true);
	}
	
	public void deactivate()	{
		leftMouseDown = middleMouseDown =false;
		planesSGC.setVisible(false);
	}
	boolean repnActive;
	@Override
	public void perform(ToolContext tc)	{
		super.perform(tc);
//		System.err.println("performing");			
		if (tc.getSource() == facePickActivate)	{
			System.err.println("Activating face pick");	
			boolean down = tc.getAxisState(facePickActivate).isPressed();
			leftMouseDown = down;
			if (leftMouseDown)	{
		    	repnActive = theRepn.isFollowsCamera();
		    	theRepn.setFollowsCamera(false);
				anchorPoint = tc.getCurrentPick();
				theRepn.getRepresentationRoot().addTool(difpt);
				removeCurrentSlot(InputSlot.POINTER_TRANSFORMATION);
		    	difpt.init();
			} else {
				theRepn.getRepresentationRoot().removeTool(difpt);
				addCurrentSlot(InputSlot.POINTER_TRANSFORMATION);
		    	difpt.removeCurrentSlot(InputSlot.POINTER_TRANSFORMATION);
				anchorPoint = null;
		    	theRepn.setFollowsCamera(repnActive);
			}
		} 
		else if (tc.getSource() == edgePickActivate)	{
			System.err.println("Activating edge pick");			
			boolean down = tc.getAxisState(edgePickActivate).isPressed();
			middleMouseDown = down;
		} 
		if (leftMouseDown) {
			return;
		}
//		System.err.println("In skewer perform");
		PickResult pick = tc.getCurrentPick();
		if (pick == null || pick.getPickPath() == null) return;
		if (pick.getPickType() == PickResult.PICK_TYPE_FACE)	{
//			System.err.println("Face, generator: "+whichFace+" "+el.getName());
			List<PickResult> pickresults = tc.getCurrentPicks();
			PickResult previous = null;
			SceneGraphComponent previousGroupEl = null;
			int count = 0;
			pickresults = prune(pickresults);
			
			for (PickResult p : pickresults)	{
				int index = p.getIndex();
				if (dirDom.faceList.get(index) == null) 
					throw new NullPointerException("No such face index "+index);
				SceneGraphComponent groupel = getGroupNodeOnPath(p.getPickPath());
				if (previous != null &&
						(groupel == previousGroupEl || 
						Pn.distanceBetween(
								p.getWorldCoordinates(),
								previous.getWorldCoordinates(), 
								metric) < 10E-4) )
					continue;
				previousGroupEl = groupel;
				DiscreteGroupElement el = (DiscreteGroupElement) dirDom.faceList.get(index).source;
				Transformation tform = groupel.getTransformation();
				final DiscreteGroupElement oldel = new DiscreteGroupElement(metric, tform.getMatrix(), tform.getName());
//				System.err.println("Picked face"+index);
//				System.err.println("last component = "+sgp.getLastComponent().getName());
//				System.err.println("plane coord = "+Rn.toString(dirDom.faceList.get(index).plane));
				IndexedFaceSet faceIFS = IndexedFaceSetUtility.extractFace(dirDom, index);
				//faceIFS.setFaceAttributes(Attribute.COLORS, null);
				SceneGraphComponent child = planesSGC.getChildComponent(count);
				child.setVisible(true);
				if (metric == Pn.HYPERBOLIC)	{
					CircleFactory cf = new CircleFactory();
					cf.setPlaneEquation(dirDom.faceList.get(index).plane);
					cf.update();
					SceneGraphComponent planer = cf.getSphereSGC();
					if (planer.getAppearance() == null) planer.setAppearance(new Appearance());
					child.getChildComponent(0).setTransformation(planer.getTransformation());
					DataList fc = faceIFS.getFaceAttributes(Attribute.COLORS);
					planer.getAppearance().setAttribute("polygonShader.transparency", .85);
					if (fc != null)	{
						double[] fca = fc.toDoubleArrayArray(null)[0];
						Color cc = new Color((float) fca[0], (float) fca[1], (float) fca[2]);
						planer.getAppearance().setAttribute("polygonShader.diffuseColor", 		cc);			
					}
				}
				child.setTransformation(new Transformation(oldel.getArray()));
				child.setGeometry(faceIFS); //planer.getGeometry());
				count++;
				if (count >= planesSGC.getChildComponentCount()) break;
				previous = p;
			}
			for (; count<numPlanes; ++count)	{
				planesSGC.getChildComponent(count).setVisible(false);
			}
		} else 
			if (pick.getPickType() == PickResult.PICK_TYPE_LINE)	{
				int index = pick.getIndex();
//				System.err.println("Picked edge"+index);
		}
    	viewer.renderAsync();
	}
	
	private List<PickResult> prune(List<PickResult> pickresults) {
		List<PickResult> results = new Vector<PickResult>();
		SceneGraphComponent[] first4 = new SceneGraphComponent[4];
		int count = 0;
		for (PickResult p : pickresults)	{
			if (p.getPickType() != PickResult.PICK_TYPE_FACE) continue;
			SceneGraphComponent groupel = getGroupNodeOnPath(p.getPickPath());
			if (groupel == null) continue;
			if (count < 4) {
				first4[count++] = groupel;
			}
			results.add(p);
		}
		// we need to find the first element: it's the unmatched of the first 2
		if (first4[0] == first4[2] || first4[0] == first4[3]) {  // flip 1 and 2
			PickResult removed = pickresults.remove(0);
			pickresults.add(1, removed);
			System.err.println("Flipping");
		}
			
		return results;
	}

	private SceneGraphComponent getGroupNodeOnPath(SceneGraphPath sgp) {
		while (sgp.getLength() > 0) {
			if (sgp.getLastComponent().getTransformation() != null &&
				(sgp.getLastComponent().getName().startsWith("dge"))) return sgp.getLastComponent();
			sgp.pop();					
		}
		return null;
	}

	private class DragInFacePlaneTool	extends AbstractTool {
		
		SceneGraphComponent anchorGroupElementSGC;
	    Matrix anchorMatrix;
	    
		void init()	{
			addCurrentSlot(InputSlot.POINTER_TRANSFORMATION);
			anchorGroupElementSGC = null;
		}
		
	    public void perform(ToolContext tc) {
	    	System.err.println("dragging face believe me!");
	    	if (anchorPoint == null) return;
	    	PickResult currentPoint = findCompatiblePick(tc.getCurrentPicks());
	    	if (currentPoint == null) return;
//			System.err.println("cp = "+thisGroupEl.getName());
//			if (thisGroupEl != groupEl || currentPoint.getIndex() != anchorPoint.getIndex()) {
//	    		// unwanted new pick; discard
//	    		for (PickResult p : allPicks)	{
//	    			if (getGroupNodeOnPath(p.getPickPath()) == groupEl)	{
//	    				thisGroupEl = getGroupNodeOnPath(p.getPickPath());
//	    				break;
//	    			}
//	    		}
//	    		return;
//	    	}
//	    	System.err.println("cp = "+Rn.toString(currentPoint.getObjectCoordinates()));
	    	Matrix worldM = new Matrix(Rn.times(null, 
	    			theRepn.getRepresentationRoot().getTransformation().getMatrix(), 
	    			anchorMatrix.getArray()));
	    	MatrixBuilder.init(worldM, metric).
	    		translate(anchorPoint.getObjectCoordinates(), currentPoint.getObjectCoordinates()).
	    		assignTo(theRepn.getRepresentationRoot());
//	    	System.err.println("matrix = "+Rn.matrixToString(theRepn.getRepresentationRoot().getTransformation().getMatrix()));
	    	viewer.renderAsync();
	    }

		private PickResult findCompatiblePick(List<PickResult> pickresults) {
			PickResult ret = null;
			SceneGraphComponent node = null;
			if (anchorGroupElementSGC == null) {
				ret = pickresults.get(0);
				node = getGroupNodeOnPath(ret.getPickPath());
	    		anchorGroupElementSGC = node;
	    		anchorMatrix = new Matrix(anchorGroupElementSGC.getTransformation().getMatrix());
	    		return ret;
			}
			// not first time; search for compatible pick result
			for (PickResult p : pickresults)	{
				node = getGroupNodeOnPath(p.getPickPath());
				if (node == anchorGroupElementSGC && p.getPickType() == anchorPoint.getPickType() &&
						p.getIndex() == anchorPoint.getIndex()) {
					ret = p;
					break;
				}
			}
			return ret;
		}

	}

}
