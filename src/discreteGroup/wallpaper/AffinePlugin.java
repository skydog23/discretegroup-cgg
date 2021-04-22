package discreteGroup.wallpaper;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.awt.Color;

import charlesgunn.jreality.tools.UserTool;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jtem.discretegroup.groups.WallpaperGroup;

public class AffinePlugin extends AbstractWallpaperPlugin {

	/**
	 * Add an instance of {@link UserTool} which applies allowable affine transformations to the 
	 * wallpaper pattern.  Note that the only data which the tool uses from its superclass is
	 * the double array <i>diffNDC</i> which gives the difference vector of the current cursor 
	 * position with the original (at time when mouse button was pressed), 
	 * in Normalized Device Coordinates (NDC). From this displacement and from which button
	 * was depressed, it constructs an allowable affine transformation and applies it to the
	 * scene graph representation of the wallpaper group. In particular, it applies it to the node
	 * named the <i>change of basis</i> node of the scene graph representation.
	 * <p>
	 * Notice that the code has to check that the button is either 1 or 2 (left or middle) since
	 * the superclass {@link UserTool}  registers for other input slot events (such as scroll wheel) which
	 * this tool doesn't want to respond to.
	 * <p>
	 * Notice that some wallpaper groups
	 * allow more affine transformations than others; all allow rotate and scale but the amount
	 * of freedom for skew transformations can be 0, 1, or 2 degrees of freedom.
	 * <p>
	 * See also {@link WallpaperGroup#getChangeOfBasisParameters()}.
	 *
	 */
//	public void addStretcher()	{
//		
//		UserTool stretcher = new UserTool() {
//			double[] changeOfBasisParameters;
//			boolean stretching = false;
//			public void activate(ToolContext tc) {
//				super.activate(tc);
//				if (button == 0) {stretching = false; return; }
//				stretching = true;
//				groupSceneGraph.getChangeOfBasisNode().setTransformation(theGroup.getChangeOfBasis());
//				changeOfBasisParameters=theGroup.getChangeOfBasisParameters();
//			}
//
//			public void perform(ToolContext tc){
//				super.perform(tc);
//				if (!stretching) return;
//				switch(button)	{
//					case 1:
//						theGroup.setChangeOfBasis(changeOfBasisParameters[0], changeOfBasisParameters[1] + diffNDC[1], changeOfBasisParameters[2], changeOfBasisParameters[3]);
//						break;
//					case 2:
//						if (!shift) theGroup.setChangeOfBasis(changeOfBasisParameters[0], changeOfBasisParameters[1], changeOfBasisParameters[2]+diffNDC[0], changeOfBasisParameters[3]+diffNDC[1]);
//						else theGroup.setChangeOfBasis(changeOfBasisParameters[0] + diffNDC[1], changeOfBasisParameters[1], changeOfBasisParameters[2], changeOfBasisParameters[3]);
//						break;
//				}
//				viewer.renderAsync();
//			}
//
//			public void deactivate(ToolContext tc){
//				super.deactivate(tc);
//				if (!stretching) return;
//				viewportConstraint.update();
//				groupSceneGraph.setElementList(DiscreteGroup.generateElements(theGroup,viewportConstraint));
//				groupSceneGraph.update();
//				viewer.renderAsync();
//			}
//	
//			public void registerHelp(HelpOverlay overlay) {
//				overlay.registerInfoString("Wallpaper group affine deformation tool", "");
//				overlay.registerInfoString("Mouse button1 dragged", 
//				"y-movement controls scaling of pattern");
//				overlay.registerInfoString("Mouse button2 dragged", 
//				"drag second basis vector to follow mouse (constrained movement)");
//				overlay.registerInfoString("Shift-Mouse button2 dragged", 
//				"rotate pattern around axis perpendicular to screen through (0,0)");
//			}
//			// note this class doesn't provide its own getIcon() method, so it receives the
//			// default icon, which is currently a pentagon dodecahedron
//
//		};
//		ToolManager.toolManagerForViewer(viewer).addUserTool(stretcher, null, "Affine change of basis");
//	}	

	WallpaperGroup group;
	double[] changeOfBasisParameters;
	
	public AffinePlugin(WallpaperPluggedIn wp) {
		super(wp);
		group = wallpaper.theGroup;
		singleTile.addTool(affineTool);
		Appearance ap = singleTile.getAppearance();
		ap.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.yellow);
		ap.setAttribute(EDGE_DRAW, true);
		ap.setAttribute(FACE_DRAW, true);
	}

	private static InputSlot[] slots = {InputSlot.LEFT_BUTTON, 
		InputSlot.SHIFT_LEFT_BUTTON,
		InputSlot.MIDDLE_BUTTON,
		InputSlot.SHIFT_MIDDLE_BUTTON};
	
	transient Tool affineTool = new AbstractTool( slots)	{
		int which = 0;
		double[] orig;

		{
			addCurrentSlot( InputSlot.getDevice("PointerTransformation"));
		}
		@Override
		public void activate(ToolContext tc) {
			group.getChangeOfBasis().assignTo(wallpaper.groupSceneGraph.getChangeOfBasisNode());					
			changeOfBasisParameters=wallpaper.theGroup.getChangeOfBasisParameters();
			for (int i = 0; i<slots.length; ++i) 
				if (tc.getSource().equals(slots[i])) { which = i; break; }
			PickResult currentPick = tc.getCurrentPick();
			orig = currentPick.getWorldCoordinates();
			if (which == 3) 
				MatrixBuilder.euclidean().assignTo(wallpaper.groupSceneGraph.getChangeOfBasisNode());
		}

		@Override
		public void deactivate(ToolContext tc) {
		}

		@Override
		public void perform(ToolContext tc) {
			PickResult currentPick = tc.getCurrentPick();
			if (currentPick == null ||
					currentPick.getObjectCoordinates() == null ||
					currentPick.getObjectCoordinates().length < 1) return;
			double[] displacement = currentPick.getWorldCoordinates();
			Rn.subtract(displacement, displacement, orig);
			switch(which)	{
			case 0:
				group.setChangeOfBasis(changeOfBasisParameters[0], changeOfBasisParameters[1], changeOfBasisParameters[2]+displacement[0], changeOfBasisParameters[3]+displacement[1]);
				break;
			case 1:
				group.setChangeOfBasis(changeOfBasisParameters[0], changeOfBasisParameters[1] + displacement[1], changeOfBasisParameters[2], changeOfBasisParameters[3]);
				break;
			case 2:
				group.setChangeOfBasis(changeOfBasisParameters[0] + displacement[1], changeOfBasisParameters[1], changeOfBasisParameters[2], changeOfBasisParameters[3]);
				break;
		}
		group.getChangeOfBasis().assignTo(wallpaper.groupSceneGraph.getChangeOfBasisNode());					
		
		viewer.renderAsync();
		}
		
	};

	@Override
	public void replaceGroup()	{
		super.replaceGroup();
		group = wallpaper.theGroup;
	}
	@Override
	public String getName() {
		return "affine";
	}

}
