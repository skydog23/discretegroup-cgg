package discreteGroup.wallpaper;

import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import discreteGroup.wallpaper.PaintSource.PaintType;

public class PaintTool extends AbstractTool  {
	private SceneGraphComponent origGroupEl, currentGroupEl;
	private double[] origInvMatrix, currentToOrig ;
	 PaintSource ps;
	public PaintTool(PaintSource ps, InputSlot ... inputslots)
	{
		super(inputslots);
		this.ps = ps;
		  addCurrentSlot( InputSlot.getDevice("PointerTransformation"));
	  }
	@Override
	public void activate(ToolContext tc) {
		System.err.println("activate");
		origGroupEl = DiscreteGroupSceneGraphRepresentation.getGroupElementOnPath(tc.getRootToLocal());
		origInvMatrix = Rn.inverse(null,origGroupEl.getTransformation().getMatrix());
		currentGroupEl = origGroupEl;
		currentToOrig = Rn.identityMatrix(4);
		handleMouseMove(tc);
		ps.oldMousex = ps.mousex; 
		ps.oldMousey = ps.mousey;
		ps.setMouseMoved();
//		mouseMoved = false;
		ps.repaint();
	}

	@Override
	public void deactivate(ToolContext tc) {
		System.err.println("deactivate");
		handleMouseMove(tc);
		ps.pushState();
//		repaint();
	}
	
	@Override
	public void perform(ToolContext tc) {
		handleMouseMove(tc);
		ps.repaint();
	}

	private void handleMouseMove(ToolContext tc) {
		PickResult currentPick = tc.getCurrentPick();
		if (currentPick == null ||
				currentPick.getObjectCoordinates() == null ||
				currentPick.getObjectCoordinates().length < 1) return;
		double[] mouse = (currentPick.getObjectCoordinates());
		SceneGraphComponent newGroupEl = DiscreteGroupSceneGraphRepresentation.getGroupElementOnPath(currentPick.getPickPath());
		if (newGroupEl == null) return;
		if (ps.paintType != PaintType.BRUSH && newGroupEl != currentGroupEl)	{
			System.err.println("Moved into new group element "+newGroupEl.getName());
			currentToOrig = Rn.times(null, 
					origInvMatrix,
					newGroupEl.getTransformation().getMatrix());
			origGroupEl = currentGroupEl;
			origInvMatrix = Rn.inverse(null, origGroupEl.getTransformation().getMatrix());
			currentGroupEl = newGroupEl;
		}
		Rn.matrixTimesVector(mouse, currentToOrig, mouse);
//		System.err.println("mouse = "+Rn.toString(mouse));
		ps.group2Canvas.transform(mouse, 0, mouse, 0, 1);
		ps.setMouseMoved();
		ps.mousex = (int) mouse[0]; 
		ps.mousey = (int) mouse[1];
	}
	
};
