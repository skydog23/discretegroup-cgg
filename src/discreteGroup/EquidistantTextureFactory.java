/*
 * Created on Jul 15, 2010
 *
 */
package discreteGroup;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.jreality.texture.SimpleTextureFactory;
import charlesgunn.jreality.texture.SimpleTextureFactory.TextureType;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;

public class EquidistantTextureFactory {
	double incrementx = .1, incrementy = .5;
	int samplesX = 10, samplesY = 10;
	SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("world");

	public EquidistantTextureFactory() 	{
		init();
	}
	
	public SceneGraphComponent getSceneGraphComponent() {
		return sgc;
	}
	
	DiscreteGroup tg = new DiscreteGroup();
	DiscreteGroupElement[] gens = new DiscreteGroupElement[2];
	DiscreteGroupSceneGraphRepresentation dgsgr;
	SceneGraphComponent geomSGC = new SceneGraphComponent("quad");
	public double getIncrementx() {
		return incrementx;
	}

	public void setIncrementx(double incrementx) {
		this.incrementx = incrementx;
	}

	public double getIncrementy() {
		return incrementy;
	}

	public void setIncrementy(double incrementy) {
		this.incrementy = incrementy;
	}

	public int getSamplesX() {
		return samplesX;
	}

	public void setSamplesX(int samplesX) {
		this.samplesX = samplesX;
	}

	public int getSamplesY() {
		return samplesY;
	}

	public void setSamplesY(int samplesY) {
		this.samplesY = samplesY;
	}

	QuadMeshFactory ifsf = new QuadMeshFactory();
	private void init() {
		sgc.getAppearance().setAttribute("diffuseColor", Color.white);
		
		tg.setDimension(2);
		tg.setMetric(Pn.HYPERBOLIC);
		for (int i=0;i<gens.length;++i)  {
			gens[i] = new DiscreteGroupElement();
			gens[i].setWord(DiscreteGroupUtility.genNames[i]);
		}
		//tg.setFsa(	 FiniteStateAutomaton.fsaForName("P.wa", ResourceClass.class));
		tg.setConstraint(new DiscreteGroupSimpleConstraint(40));
		
		dgsgr = new DiscreteGroupSceneGraphRepresentation(tg, false);
		dgsgr.setWorldNode(geomSGC);
		Color transpblack = new Color(0,0,0,0),
				yellow = new Color(255, 255, 72);
		SimpleTextureFactory stf = new SimpleTextureFactory();

		stf.setType(SimpleTextureFactory.TextureType.GRAPH_PAPER); // LINE); //
//		stf.setColor(0, new Color(180, 180, 180));
//		stf.setColor(1, new Color(100,100,100));
//		stf.setColor(2, new Color(140,140,140));
//		stf.setColor(3, new Color(100,100,100));
		stf.setColor(0, new Color(100,100,100));
		stf.setColor(1, new Color(250,250,250));
		stf.setColor(2, new Color(140,140,140));
		stf.setColor(3, new Color(250,250,250));
		stf.setSize(512);
		stf.setAppearance(geomSGC.getAppearance());
		stf.update();

		Texture2D tex2d = TextureUtility.createTexture(geomSGC.getAppearance(), "polygonShader", stf.getImageData());
		Matrix foo = new Matrix();
		MatrixBuilder.euclidean().scale(1, 10, 1).assignTo(foo);
		tex2d.setTextureMatrix(foo);
		
		sgc.addChild(dgsgr.getRepresentationRoot());
		sgc.getAppearance().setAttribute(CommonAttributes.METRIC, Pn.HYPERBOLIC);
	}
	
	public void update() {
		double[] t1 = {Pn.sinh(incrementx), 0, 0, Pn.cosh(incrementx)};
		gens[0].setArray(P3.makeTranslationMatrix(null, t1, Pn.HYPERBOLIC));
		gens[1] = gens[0].getInverse();
		tg.setGenerators(gens);
		tg.setElementList(DiscreteGroupUtility.generateElements(tg, null));
		double[][][] pts = new double[samplesY][samplesX][];
		double[] ydir = {0,1,0,0};
		for (int i = 0; i<samplesX; ++i) 	{
			double dx = i/(samplesX-1.0),
				x = incrementx*dx;
			double[] hh = new double[]{Pn.sinh(x), 0, 0, Pn.cosh(x)};
			for (int j = 0; j<samplesY; ++j)	{
				double dy = j/(samplesY-1.0),
					y = 2*incrementy*(-.5+dy);
				pts[j][i] = Pn.dragTowards(null, hh, ydir, y, Pn.HYPERBOLIC);
			}	
		}
		ifsf.setULineCount(samplesX);
		ifsf.setVLineCount(samplesY);
		ifsf.setVertexCoordinates(pts);
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		geomSGC.setGeometry(ifsf.getGeometry());
		
		dgsgr.update();

	}

	public Component getInspector()		{
		Box container = Box.createVerticalBox();
		final TextSlider aSlider = new TextSlider.Double("y",  SwingConstants.HORIZONTAL, 0, 2.0, incrementy);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
                incrementy = aSlider.getValue().doubleValue();
				update();
			}
		});
		container.add(aSlider);

		return container;
	}
}
