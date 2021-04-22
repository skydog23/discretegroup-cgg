/*
 * Created on May 4, 2010
 *
 */
package discreteGroup.imulogo;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.util.TextSlider;
import de.jreality.geometry.ParametricSurfaceFactory;

public class CreditsSurface extends ParametricSurfaceFactory {


	 double s1 = .1637; //Math.tan(Math.PI/14);	// scale output y -value (flatten the hyperbola)
	 double s2 = 2.8; //2;		// stretch v-parameter to have wider domain
	 double s3 = .9591; //1;		// scale output z-value 
	 double s4 = 3.0; //1.5;		// translate this much towards eye
	public CreditsSurface() {
		setULineCount(40);
		setVLineCount(40);
		setUMin(-1);
		setUMax(1);
		setVMin(-2);
		setVMax(2);
		setGenerateTextureCoordinates(true);
		setGenerateVertexNormals(true);
	}
	@Override
	protected void updateImpl() {
		setImmersion( new ParametricSurfaceFactory.DefaultImmersion()	{
			@Override
			public void evaluate(double u, double v) {
				x = .35*u;
				y = -s1*s2*v; //Math.sinh(s2*v);
				z = s4-s3*Math.sqrt(1+s2*s2*v*v); //Math.cosh(s2*v); //-3*v*v+1;
			}	
		});
		super.updateImpl();
	}

	public Component getInspector()	{
		Box container = Box.createVerticalBox();

		final TextSlider s1Slider = new TextSlider.Double("s1",  SwingConstants.HORIZONTAL, 0.0, 4.0,s1);
		s1Slider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				s1 = (s1Slider.getValue().doubleValue());
				update();
			}
		});
		container.add(s1Slider);

		final TextSlider s2Slider = new TextSlider.Double("s2",  SwingConstants.HORIZONTAL, 0.0, 4.0,s2);
		s2Slider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				s2 = (s2Slider.getValue().doubleValue());
				update();
			}
		});
		container.add(s2Slider);

		final TextSlider s3Slider = new TextSlider.Double("s3",  SwingConstants.HORIZONTAL, 0.0, 4.0,s3);
		s3Slider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				s3 = (s3Slider.getValue().doubleValue());
				update();
			}
		});
		container.add(s3Slider);

		final TextSlider s4Slider = new TextSlider.Double("s4",  SwingConstants.HORIZONTAL, 0.0, 4.0,s4);
		s4Slider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				s4 = (s4Slider.getValue().doubleValue());
				update();
			}
		});
		container.add(s4Slider);

		container.add(Box.createVerticalGlue());
		container.setName("Credits Surface");
		return container;

	}
	
}
