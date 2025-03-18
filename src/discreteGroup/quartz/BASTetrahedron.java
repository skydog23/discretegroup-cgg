/*
 * Created on 10 May 2023
 *
 */
package discreteGroup.quartz;

import static discreteGroup.quartz.QuartzConstants.basScale;
import static discreteGroup.quartz.QuartzConstants.chan31Color;
import static discreteGroup.quartz.QuartzConstants.chan32Color;
import static discreteGroup.quartz.QuartzConstants.nullRad;
import static discreteGroup.quartz.QuartzConstants.oxygenColor;
import static discreteGroup.quartz.QuartzConstants.oxygenRad;
import static discreteGroup.quartz.QuartzConstants.siliconColor;
import static discreteGroup.quartz.QuartzConstants.siliconRad;
import static discreteGroup.quartz.QuartzConstants.stickRad;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import charlesgunn.util.TextSlider;
import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.SceneGraphUtility;

public abstract class BASTetrahedron {

	boolean showLabels = false;
	protected  Color[] pointClr = { siliconColor, oxygenColor, oxygenColor, oxygenColor, oxygenColor },
			edgeClr = { chan31Color, chan31Color, chan32Color, chan32Color };
	protected  String[] vertexLabels = { "Si", "O", "O", "O", "O" };
	protected  double[] pointRadii = { siliconRad, oxygenRad, oxygenRad, nullRad, nullRad };

	protected  double[][] baspts = { { 0, 0, 0 }, { 1, 1, 1 }, { 1, -1, -1 }, { -1, 1, -1 }, { -1, -1, 1 } };
	protected  int[][] basIndices = { { 0, 1 }, { 0, 2 }, { 0, 3 }, { 0, 4 } };
	static BallAndStickFactory basf = null;
	IndexedLineSetFactory SiO4ilsf = new IndexedLineSetFactory();
	SceneGraphComponent SiO4sgc = SceneGraphUtility.createFullSceneGraphComponent();

	public SceneGraphComponent getBallAndStick()	{
		return getBallAndStick(showLabels, basScale, pointClr, edgeClr, vertexLabels, pointRadii);
	}

	public  SceneGraphComponent getBallAndStick(boolean sl, double scale, Color[] pColor, Color[] eColor, String[] vLabels, double[] pRadii)	{
		SiO4ilsf.setVertexCount(5);
		SiO4ilsf.setEdgeCount(4);
		SiO4ilsf.setVertexCoordinates(baspts);
		SiO4ilsf.setVertexLabels(vLabels);
		SiO4ilsf.setEdgeIndices(basIndices);
		SiO4ilsf.setEdgeColors(eColor);
		SiO4ilsf.setVertexColors(pColor);
		SiO4ilsf.setVertexAttribute(Attribute.RELATIVE_RADII, Rn.times(null, scale, pRadii));
		SiO4ilsf.setVertexAttribute(Attribute.POINT_SIZE, Rn.times(null, scale, pRadii));
		SiO4ilsf.update();
		SceneGraphComponent ret = null;

		Appearance ap = SiO4sgc.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.SHOW_LABELS, sl);
		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(ap, false);
		DefaultTextShader pts = (DefaultTextShader) ((DefaultPointShader) dgs.getPointShader()).getTextShader();
		pts.setDiffuseColor(new Color(153, 255, 153));
		pts.setScale(.0025);
		pts.setOffset(new double[] { .0, .04, .2 });
		pts.setAlignment(SwingConstants.NORTH_EAST);
		Font f = new Font("Arial Bold", Font.ITALIC, 48);
		pts.setFont(f);
		updateBallAndStick(scale, pRadii, stickRad);
		SiO4sgc.setGeometry(SiO4ilsf.getIndexedLineSet());
		ret = SiO4sgc;
		return ret;
	}
	
	private  void updateBallAndStick() {
		updateBallAndStick(basScale, pointRadii, stickRad);
	}

	private  void updateBallAndStick(double scale, double[] pRadii, double eRad) {
		SiO4ilsf.setVertexAttribute(Attribute.POINT_SIZE, Rn.times(null, scale, pRadii));
		SiO4ilsf.setVertexAttribute(Attribute.RELATIVE_RADII, Rn.times(null, scale, pRadii));
		SiO4ilsf.update();
		Appearance ap = SiO4sgc.getAppearance();
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, scale*eRad);
		ap.setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, 1.0);
	}
	Box inspector= null;
	public Component getInspector() {
		if (inspector == null) {
			inspector = Box.createVerticalBox();	
		} else return inspector;
		Box hbox = Box.createHorizontalBox();
		inspector.add(hbox);
		final TextSlider<Double> bsSlider = new TextSlider.Double("atom scale",  SwingConstants.HORIZONTAL, 0, 4.0, basScale);
		bsSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				basScale = bsSlider.getValue().doubleValue();
				updateBallAndStick();				
			}
		});
		hbox.add(bsSlider);
		
		final JCheckBox lcb = new JCheckBox("Show labels");
		lcb.setSelected(showLabels);
		lcb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showLabels = lcb.isSelected();
				SiO4sgc.getAppearance().setAttribute(CommonAttributes.SHOW_LABELS, showLabels);
			}
		});
		hbox.add(lcb);

		
		return inspector;
	}

}
