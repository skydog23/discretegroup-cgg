package discreteGroup.wallpaper;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.LINE_WIDTH;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.SPHERES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.awt.Graphics2D;

import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DirichletDomain;

public class DirichletDomainPlugin extends AbstractWallpaperPlugin {

	transient SceneGraphComponent dirichletDomainSGC, pointSGC;
	transient protected IndexedFaceSet dirichletDomain;
	transient protected PointSet centerPoint;
	DirichletDomain dirdom;
	
	transient Tool dirdomTool = new AbstractTool( InputSlot.getDevice("PrimaryAction"))	{

		{
			  addCurrentSlot( InputSlot.getDevice("PointerTransformation"));
		  }
		@Override
		public void activate(ToolContext tc) {
			dirichletDomainSGC.setGeometry(dirichletDomain);
			dirichletDomainSGC.setVisible(true);
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
			wallpaper.theGroup.setCenterPoint(currentPick.getObjectCoordinates());
			//dirichletDomain = DiscreteGroupUtility.calculateDirichletDomain(dirichletDomain, wallpaper.theGroup);
			dirdom.update();
			dirichletDomainSGC.setGeometry(dirdom.getDirichletDomain());		
			pointSGC.setGeometry(Primitives.point(wallpaper.theGroup.getCenterPoint()));
//			wallpaper.viewer.renderAsync();
		}
		
	};
	public DirichletDomainPlugin(WallpaperPluggedIn wp) {
		super(wp);
		dirichletDomainSGC = SceneGraphUtility.createFullSceneGraphComponent("dirdom");
		Appearance ap = dirichletDomainSGC.getAppearance();
		ap.setAttribute(EDGE_DRAW, true);
		ap.setAttribute(FACE_DRAW, true);
		ap.setAttribute(LINE_SHADER+"."+TUBES_DRAW, false);
		ap.setAttribute(LINE_SHADER+"."+DIFFUSE_COLOR, new Color(255,100,100));
		ap.setAttribute(LINE_SHADER+"."+LINE_WIDTH, 1.5);
		MatrixBuilder.euclidean().translate(0,0,.005).assignTo(dirichletDomainSGC);
		dirichletDomainSGC.setPickable(false);
		pointSGC = SceneGraphUtility.createFullSceneGraphComponent("centerPoint");
		ap = pointSGC.getAppearance();
		ap.setAttribute(VERTEX_DRAW, true);
		ap.setAttribute(EDGE_DRAW, false);
		ap.setAttribute(FACE_DRAW, false);
		ap.setAttribute(POINT_SHADER+"."+SPHERES_DRAW, true);
		ap.setAttribute(POINT_SHADER+"."+POLYGON_SHADER+"."+DIFFUSE_COLOR, new Color(255,255,200));
		ap.setAttribute(POINT_SHADER+"."+POINT_RADIUS, .03);
		dirichletDomainSGC.addChild(pointSGC);
		singleTile.addChild(dirichletDomainSGC);
		singleTile.addTool(dirdomTool);
	}

	@Override
	public Tool getTool() {
		return dirdomTool;
	}

	@Override
	public void replaceGroup() {
		super.replaceGroup();
		dirdom = new DirichletDomain(wallpaper.theGroup);
		dirdom.update();
		dirichletDomain = dirdom.getDirichletDomain();
		dirichletDomainSGC.setGeometry(dirichletDomain);
	}

	@Override
	public void paint(Graphics2D g, double[] currentPoint) {
		super.paint(g, currentPoint);
	}

	@Override
	public String getName() {
		return "dirichlet domain";
	}

	
}
