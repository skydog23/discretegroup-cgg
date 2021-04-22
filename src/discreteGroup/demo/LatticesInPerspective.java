/*
 * Created on Feb 26, 2008
 *
 */
package discreteGroup.demo;

import static de.jreality.shader.CommonAttributes.BACKGROUND_COLOR;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FOG_ENABLED;
import static de.jreality.shader.CommonAttributes.LIGHTING_ENABLED;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.Graphics3D;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.ResourceClass;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.FiniteStateAutomaton;
import de.jtem.discretegroup.groups.CrystallographicGroup;

public class LatticesInPerspective extends LoadableScene {

	SceneGraphComponent theWorld = SceneGraphUtility.createFullSceneGraphComponent();
	double radius = .2;
	private SceneGraphComponent circleSGC;
	@Override
	public SceneGraphComponent makeWorld() {
		
		return theWorld;
	}
	@Override
	public void customize(JMenuBar menuBar, Viewer viewer) {
		
		DiscreteGroup tg = CrystallographicGroup.instanceOfGroup("O");
		DiscreteGroupSceneGraphRepresentation theMainRepn = 
			new  DiscreteGroupSceneGraphRepresentation(tg, true);
		Graphics3D gc = new Graphics3D(viewer);
		FiniteStateAutomaton fsa = FiniteStateAutomaton.fsaForName("3DP.wa", ResourceClass.class);
		tg.setFsa(fsa);
		//tg.setFsa(null);
//		DiscreteGroupViewportConstraint viewportConstraint = 
//			new DiscreteGroupViewportConstraint( 3d, 3, 8.0,10, gc);
//		viewportConstraint.setMaxNumberElements(3000);
//		tg.setConstraint(viewportConstraint);
//		SceneGraphPath sgp = new SceneGraphPath(viewer.getSceneRoot(), theWorld);
//		theMainRepn.attachToViewer(viewer, sgp, false, 200, true, 200);
		tg.setConstraint(new DiscreteGroupSimpleConstraint(5000));
		IndexedFaceSet circle = Primitives.regularPolygon(32);
		circleSGC = new SceneGraphComponent("circle");
		MatrixBuilder.euclidean().scale(.2).assignTo(circleSGC);
		circleSGC.setGeometry(circle);
		theMainRepn.setWorldNode(circleSGC);
		theMainRepn.update();
		theWorld.addChild(theMainRepn.getRepresentationRoot());
		Appearance pa = theWorld.getAppearance();
		pa.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.white);
		pa.setAttribute(LIGHTING_ENABLED, false);
		pa.setAttribute(EDGE_DRAW, false);
		CameraUtility.getCamera(viewer).setFieldOfView(130);
		viewer.getSceneRoot().getAppearance().setAttribute(FOG_ENABLED, true);
		viewer.getSceneRoot().getAppearance().setAttribute(BACKGROUND_COLOR, Color.BLACK);
	}
	@Override
	public Component getInspector(final Viewer v) {
	
			Box container = Box.createVerticalBox();
			final TextSlider aSlider = new TextSlider.Double("radius",  SwingConstants.HORIZONTAL, 0.0, .5, radius);
			aSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					radius = aSlider.getValue().doubleValue();
					MatrixBuilder.euclidean().scale(radius).assignTo(circleSGC);
					v.renderAsync();
				}
			});
			container.add(aSlider);
			JPanel panel = new JPanel();
			panel. setName("Parameters");
			panel.add(container);
			return panel;

	}
	@Override
	public boolean hasInspector() {
		return true;
	}

}
