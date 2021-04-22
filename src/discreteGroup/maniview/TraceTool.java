package discreteGroup.maniview;

import charlesgunn.jreality.geometry.OneArmedTinManFactory;
import charlesgunn.jreality.geometry.SnakeFactory;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Transformation;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;

public class TraceTool extends AbstractTool {
	static InputSlot traceActivate = InputSlot.getDevice("RotateActivation");
	static InputSlot resetActivate = InputSlot.getDevice("DragActivation");
	static InputSlot timer = InputSlot.getDevice("SystemTime");
	SnakeFactory sf;
	OneArmedTinManFactory oatmf;
	Appearance ap;
	Transformation c2w;
	double[] c2wm = Rn.identityMatrix(4);
	public TraceTool(SnakeFactory sf, OneArmedTinManFactory oatmf, Appearance ap, Transformation c2w) {
		super(traceActivate, resetActivate);
		this.sf = sf;
		this.oatmf = oatmf;
		this.ap = ap;
		this.c2w = c2w;
	}
	@Override
	public void activate(ToolContext tc) {
		if (tc.getSource() == traceActivate)	{
			addCurrentSlot(timer);
			ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
			System.err.println("Activating trace tool");			
		} else if (tc.getSource() == resetActivate)	{
			sf.reset();
		}
	}
	
	@Override
	public void perform(ToolContext tc)	{
		if (c2w != null) c2w.getMatrix(c2wm);
		sf.addPoint(Rn.matrixTimesVector(null, c2wm, oatmf.getStickTipWorldPosition()));
		sf.update();				
	}
	
	@Override
	public void deactivate(ToolContext tc)	{
		removeCurrentSlot(timer);
		ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
	}
	
}
