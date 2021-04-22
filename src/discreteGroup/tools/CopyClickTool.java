package discreteGroup.tools;

import java.awt.Color;
import java.awt.Component;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

import charlesgunn.jreality.tools.ToolManager;
import charlesgunn.jreality.tools.UserTool;
import charlesgunn.math.Biquaternion;
import charlesgunn.math.IsometryAxis;
import charlesgunn.math.Biquaternion.Metric;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.AnimatorTask;
import de.jreality.tools.AnimatorTool;
import de.jreality.tools.Timer;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.util.WingedEdge;

public class CopyClickTool extends AbstractTool {
	SceneGraphComponent  
		highlightFace = SceneGraphUtility.createFullSceneGraphComponent("highlight face"), 
		dirichletDomain = new SceneGraphComponent("copy click tool: dd");
	DiscreteGroup dg;
	WingedEdge dirDom;
	private Vector<DiscreteGroupElement> els;
	DiscreteGroupSceneGraphRepresentation theRepn;
	SceneGraphPath path;
	double[] rootToRep;
	Viewer viewer;
	int oldIndex = -1;
	Timer animator;
	boolean activated = false;
	SoundEffects soundEffects = new SoundEffects();
	MidiSoundEffects mse = new MidiSoundEffects();
	double totalAnimation = 2.0;
	
	public CopyClickTool(DiscreteGroup dg, DiscreteGroupSceneGraphRepresentation r)	{
		super(	InputSlot.LEFT_BUTTON,
				InputSlot.MIDDLE_BUTTON
			);
		this.dg = dg;
		theRepn = r;
		DirichletDomain dirdomdom = new DirichletDomain(dg);
		dirdomdom.setDirichletDomainOrbit(600);
		dirdomdom.update();
		dirDom = (WingedEdge) dirdomdom.getDirichletDomain(); //DiscreteGroupUtility.calculateDirichletDomain(null, dg);	
		DiscreteGroupUtility.addWordLabels(dirDom);
		els = new Vector<DiscreteGroupElement>();
		els.add(new DiscreteGroupElement(Pn.EUCLIDEAN, Rn.identityMatrix(4), ""));
		Appearance ap = highlightFace.getAppearance();
		ap.setAttribute("polygonShader.diffuseColor", Color.white);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, true);
		ap.setAttribute(CommonAttributes.FACE_DRAW, true);
		theRepn.getRepresentationRoot().addChild(highlightFace);
		highlightFace.setVisible(false);
		highlightFace.setPickable(false);
		ap = new Appearance();
		ap.setAttribute("polygonShader.transparency", 1.0);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.FACE_DRAW, true);
		ap.setAttribute(CommonAttributes.RMAN_PROXY_COMMAND,"");
		ap.setAttribute(CommonAttributes.TEXT_SCALE, .001);
		ap.setAttribute(CommonAttributes.TEXT_OFFSET, new double[] { 0, 0, 0 });
		ap.setAttribute(CommonAttributes.TEXT_ALIGNMENT, SwingConstants.CENTER);
		dirichletDomain.setGeometry(dirDom);
		dirichletDomain.setAppearance(ap);
		dirichletDomain.setVisible(true);
		dirichletDomain.setPickable(true);
		MatrixBuilder.euclidean().scale(1.01).assignTo(dirichletDomain);
		mse.setDoSound(true);
//		dirichletDomain.addTool(this);
//		theRepn.getWorldNode().addChild(dirichletDomain);
	}
	@Override
	public void activate(ToolContext tc) {
		super.activate(tc);
		System.err.println("copy click: In activate");
		viewer = tc.getViewer();
		path = SceneGraphUtility.getPathsBetween(viewer.getSceneRoot(), theRepn.getRepresentationRoot()).get(0);
		oldIndex = -1;
		addCurrentSlot(InputSlot.POINTER_TRANSFORMATION);
		addCurrentSlot(InputSlot.SYSTEM_TIME);
		activated = true;
	}
	
	@Override
	public void perform(ToolContext tc) {
//		System.err.println("In perform");
		if (!activated) return;
		super.perform(tc);
		if (animator != null && animator.isRunning()) return;
		PickResult currentPick = tc.getCurrentPick();
		if (currentPick == null ||
				currentPick.getPickPath() == null || 
				currentPick.getPickType() != PickResult.PICK_TYPE_FACE) {
			highlightFace.setVisible(false);
			return;
		}
		SceneGraphNode element = currentPick.getPickPath().getLastElement();
		System.err.println("copy click: pick is "+element.getName());
		if (element != dirDom) {
			highlightFace.setVisible(false);
			return;
		}
		highlightFace.setVisible(true);
//		System.err.println("Setting highlight face visible "+currentPick.getPickPath());
		int whichFace = currentPick.getIndex();
//		if (whichFace == oldIndex) return;
		oldIndex = whichFace;
		IndexedFaceSet face = IndexedFaceSetUtility.constructPolygon(dirDom.getFaceWithIndex(whichFace));
		highlightFace.setGeometry(face);
		rootToRep = Rn.inverse(null, path.getMatrix(null));
		highlightFace.getTransformation().setMatrix(
				Rn.times(null, rootToRep,currentPick.getPickPath().getMatrix(null))); //);
		viewer.renderAsync();
//		System.err.println("Face"+whichFace);
	}


	@Override
	public void deactivate(ToolContext tc) {
		super.deactivate(tc);
		activated = false;
		System.err.println("copy click: In deactivate");
		removeCurrentSlot(InputSlot.POINTER_TRANSFORMATION);
		removeCurrentSlot(InputSlot.SYSTEM_TIME);
		if (animator != null && animator.isRunning()) return;
//			System.err.println(tc.getCurrentPick().toString());
		PickResult currentPick = tc.getCurrentPick();
		if (currentPick == null || currentPick.getPickPath() == null || currentPick.getPickType() != PickResult.PICK_TYPE_FACE) {
			Logger.getLogger("discreteGroup.tools").info("face not picked");
			highlightFace.setVisible(false);
			return;
		}
		SceneGraphNode element = currentPick.getPickPath().getLastElement();
		if (element != dirDom) {
			System.err.println("dir dom not picked, picked was "+element.getName());
			highlightFace.setVisible(false);
			return;
		}
		SceneGraphPath sgp = currentPick.getPickPath();
//		System.err.println("Path is "+sgp.toString());
		while (sgp.getLength() > 0) {
			if (sgp.getLastComponent().getTransformation() != null &&
				(sgp.getLastComponent().getName().startsWith("dge")))  break;
			sgp.pop();					
		}
		if (sgp.getLength() == 0) {
//			System.err.println("No DGE on path");
			highlightFace.setVisible(false);
			return;
		}
		if (tc.getSource() == InputSlot.LEFT_BUTTON) { //button == 1)	{		// add a copy on this face
			int whichFace = currentPick.getIndex();
			if (dirDom.faceList.get(whichFace) == null) 
				throw new NullPointerException("No such face index "+whichFace);
			final DiscreteGroupElement el = (DiscreteGroupElement) dirDom.faceList.get(whichFace).source;
			System.err.println("Face, generator: "+whichFace+" "+el.getWord());
//			final DiscreteGroupElement oldel = ((DiscreteGroupElement) sgp.getLastComponent().getTransformation());
			Transformation tform = sgp.getLastComponent().getTransformation();
			final DiscreteGroupElement oldel = new DiscreteGroupElement(dg.getMetric(), tform.getMatrix(), tform.getName());
//			System.err.println("old DGE matrix is "+Rn.matrixToJavaString(oldel.getMatrix()));
			System.err.println("new DGE matrix is "+Rn.matrixToJavaString(el.getArray()));
			
		    String newword = oldel.getWord() + el.getWord();
		    // initialize with the old matrix; the animation will concatenate the new element on
			final DiscreteGroupElement newdge = new DiscreteGroupElement(dg.getMetric(), oldel.getArray(), newword);
			final SceneGraphComponent sgc = theRepn.addElement(newdge);
			final Transformation thisTform = sgc.getTransformation();
			final boolean isWNeg = el.getArray()[15]<0;
			final boolean flipped = Rn.determinant(el.getArray()) < 0.0;
			System.err.println("flipped = "+flipped);
			Biquaternion biq = null, rebiq[] = null;
			
			if (flipped) rebiq = Biquaternion.biquaternionsFromIndirectIsometry(null, 
					el.getArray(), Metric.metricForCurvature(el.getMetric()));
			else biq = Biquaternion.biquaternionFromDirectIsometry(null, 
					el.getArray(), Metric.metricForCurvature(el.getMetric()));
			final IsometryAxis isom = new IsometryAxis(flipped ?rebiq[1] : biq);
			double[] trefl = null;
			if (flipped) {
				trefl = Biquaternion.matrixFromBiquaternion(null, rebiq[0]);
				if (trefl[15] > 0) {
					Rn.times(trefl, -1, trefl);	
					// This is equivalent to multiplying on the right by the diagonal matrix (1,1,1,-1)
					for (int i = 0; i<4; ++i) trefl[12+i] = -trefl[12+i];					
				}
			}
			
			final double[] refl = trefl;
			AnimatorTask task = new AnimatorTask() {
				double t = 0.0;
				int count = 0;
				public boolean run(double time, double dt) {
					double ddt = 0.001*dt/totalAnimation;;
					t+=ddt;
					count++;
					Biquaternion bq = isom.exp(t);
					double[] mat = Biquaternion.matrixFromBiquaternion(null, bq);
					if (isWNeg ^ (mat[15]<0))	{
						Rn.times(mat, -1, mat);						
					}
					if (flipped) {
						double[] interpFlip = Rn.linearCombination(null, 1-t, Rn.identityMatrix(4), t, refl);
						Rn.times(mat, interpFlip, mat);
					}
					thisTform.setMatrix(Rn.times(null, oldel.getArray(), mat)); //newFM.getArray()));
					viewer.renderAsync();
					if (t < .8 && (count %12 == 0)) mse.playMoving(t);
					if (t >= 1.0) {
						thisTform.setMatrix(Rn.times(null, oldel.getArray(), el.getArray()));
						System.err.println("Setting new matrix to its 'real' value");
						mse.playEnd();
						sgc.setPickable(true);
						return false;
					}
					return true;
				}
			};
			mse.initMoving();
			sgc.setPickable(false);
			AnimatorTool.getInstance(tc).schedule(sgc, task);
			
//			final double[] refl = trefl;
//			animator = new Timer(20, new ActionListener() {
//				int count = 0;
//				public void actionPerformed(ActionEvent e) {
//					if (count > frames) return;
//					double t = ((double) count)/frames;
//					//kfat.setValueAtTime(t);
//					Biquaternion bq = isom.exp(t);
//					double[] mat = Biquaternion.matrixFromBiquaternion(null, bq);
//					if (isWNeg ^ (mat[15]<0))	{
//						Rn.times(mat, -1, mat);						
//					}
//					if (flipped) {
//						double[] interpFlip = Rn.linearCombination(null, 1-t, Rn.identityMatrix(4), t, refl);
//						Rn.times(mat, interpFlip, mat);
//					}
//					thisTform.setMatrix(Rn.times(null, oldel.getArray(), mat)); //newFM.getArray()));
//					viewer.renderAsync();
//					if (t < .8 && (count %3 == 0)) mse.playMoving(t);
//					count++;
//					if (count > frames) {
//						thisTform.setMatrix(Rn.times(null, oldel.getArray(), el.getArray()));
//						System.err.println("Setting new matrix to its 'real' value");
//						animator.stop();
//						mse.playEnd();
//					}
//				}
//				
//			});
////			soundEffects.play();
//			mse.initMoving();
//			animator.attach(ToolSystem.getToolSystemForViewer(viewer));
//			animator.start();
			
		} else if (tc.getSource() == InputSlot.MIDDLE_BUTTON)	{		// remove this copy
			theRepn.removeElement(sgp);
			System.err.println("removing copy from dgsgr");
		}
		highlightFace.setVisible(false);
		if (viewer != null) viewer.renderAsync();
	}
	private static double[] invert = P3.makeStretchMatrix(null, -1, 1, 1);
	
//	@Override
//	public ImageIcon getIcon(int size) {
//		return ToolManager.createImageIcon("copyClickTool-24.png", discreteGroup.tools.CopyClickTool.class);
//	}
//
//	@Override
//	public String getName() {
//		return "click to copy";
//	}
//
//	@Override
//	public void registerHelp(HelpOverlay overlay) {
//	}
//	
	
	public  UserTool wrapCCT() {
		UserTool ut = new UserTool()	{

			@Override
			public void activate(ToolContext tc) {
				CopyClickTool.this.activate(tc);
				addCurrentSlot(InputSlot.POINTER_TRANSFORMATION);
				addCurrentSlot(InputSlot.SYSTEM_TIME);
			}

			@Override
			public void deactivate(ToolContext tc) {
				CopyClickTool.this.deactivate(tc);
				removeCurrentSlot(InputSlot.POINTER_TRANSFORMATION);
				removeCurrentSlot(InputSlot.SYSTEM_TIME);
			}

			@Override
			public void perform(ToolContext tc) {
				CopyClickTool.this.perform(tc);
			}
			
			@Override
			public ImageIcon getIcon(int size) {
				return ToolManager.createImageIcon("copyClickTool-24.png", discreteGroup.tools.CopyClickTool.class);
			}
		//
			@Override
			public String getName() {
				return "click to copy";
			}
			
		};
		return ut;
	}

	public Component getInspector()	{
		return mse.getInspector();
	}
	public SceneGraphComponent getSceneGraphComponent() {
		return dirichletDomain;
	}
//	@Override
	public void attachToViewer(Viewer v) {
//		super.attachToViewer(v);
		dirichletDomain.setVisible(true);
		theRepn.getWorldNode().setPickable( false);
		theRepn.getWorldNode().getAppearance().setAttribute("polygonShader."+CommonAttributes.PICKABLE, false);
		theRepn.getWorldNode().getAppearance().setAttribute("lineShader."+CommonAttributes.PICKABLE, false);
		theRepn.getWorldNode().getAppearance().setAttribute("pointShader."+CommonAttributes.PICKABLE, false);
		if (viewer != null) viewer.renderAsync();
	}
//	@Override
	public void detachFromViewer() {
//		super.detachFromViewer();
		dirichletDomain.setVisible(false);
		theRepn.getWorldNode().setPickable(true);
		theRepn.getWorldNode().getAppearance().setAttribute("polygonShader."+CommonAttributes.PICKABLE, true);
		theRepn.getWorldNode().getAppearance().setAttribute("lineShader."+CommonAttributes.PICKABLE, true);
		theRepn.getWorldNode().getAppearance().setAttribute("pointShader."+CommonAttributes.PICKABLE, true);
		if (viewer != null) viewer.renderAsync();
	}
}
