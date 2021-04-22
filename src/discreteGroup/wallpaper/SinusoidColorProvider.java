/*
 * Created on Mar 3, 2008
 *
 */
package discreteGroup.wallpaper;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import charlesgunn.util.TextSlider;

public class SinusoidColorProvider extends AbstractColorProvider {
	{
		int clahpahboling = (int) (System.currentTimeMillis()%23);
		for (int i = 0; i<clahpahboling; ++i) Math.random();
	}
	double foo = Math.random();
	double c1 = 1.27, c2 = 1.0, c3 = .786, 
			c4 = 0.0,
			c5 = Math.random(),
			c6 = Math.random();
	
	public double getC1() {
		return c1;
	}

	public void setC1(double c1) {
		this.c1 = c1;
	}

	public double getC2() {
		return c2;
	}

	public void setC2(double c2) {
		this.c2 = c2;
	}

	public double getC3() {
		return c3;
	}

	public void setC3(double c3) {
		this.c3 = c3;
	}

	public double getC4() {
		return c4;
	}

	public void setC4(double c4) {
		this.c4 = c4;
	}
	double dt = .005;
	double t = 0;
	double saturated = 1.0;
	public void update()	{
		t += dt;
		double isaturated = 1.0 - saturated;
		double rraw = .5 + .5*Math.sin(c1*t+c4);
		double graw = .5 + .5*Math.sin(c2*t+c5);
		double braw = .5 + .5*Math.sin(c3*t+c6);
		double mmax = 1.0/Math.max(Math.max(rraw, graw), braw);
		mmax = 1.0;
		int r = (int) (255 * (isaturated + saturated*mmax*rraw));
		int g = (int) (255 * (isaturated + saturated*mmax*graw));
		int b = (int) (255 * (isaturated + saturated*mmax*braw));
		int a = (int) (255 * alpha);
		color = new Color(r,g,b,a);
	}
	
	public void setSpeed(double s)	{
		dt = s;
	}
	
	public double getSpeed()	{
		return dt;
	}

	public double getSaturated() {
		return saturated;
	}

	public void setSaturated(double saturated) {
		this.saturated = saturated;
	}
	transient JPanel panel;
	@Override
	public JComponent getInspector() {
		if (panel != null) return shrinkPanel;
		 panel = new JPanel();
//		c.anchor = GridBagConstraints.PAGE_START;
			TextSlider c1slider = new TextSlider.Double("red speed",SwingConstants.HORIZONTAL, -3, 3, c1);
			c1slider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					c1 = ((TextSlider) e.getSource()).getValue().doubleValue()-.01;
				}
			});
			TextSlider c2slider = new TextSlider.Double("green speed",SwingConstants.HORIZONTAL, -3, 3, c2);
			c2slider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					c2 = ((TextSlider) e.getSource()).getValue().doubleValue()-.01;
				}
			});
			TextSlider c3slider = new TextSlider.Double("blue speed",SwingConstants.HORIZONTAL, -3, 3, c3);
			c3slider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					c3 = ((TextSlider) e.getSource()).getValue().doubleValue()-.01;
				}
			});
		TextSlider cslider = new TextSlider.Double("global speed",SwingConstants.HORIZONTAL,0.0,.1,dt);
		cslider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				dt = ((TextSlider) e.getSource()).getValue().doubleValue();
			}
		});
//		box.add(cslider);
		TextSlider sslider = new TextSlider.Double("saturation",SwingConstants.HORIZONTAL,0.0,1,saturated);
		sslider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				saturated = ((TextSlider) e.getSource()).getValue().doubleValue();
			}
		});
//		box.add(sslider, c);
//		c.anchor = GridBagConstraints.PAGE_START;
//		c.fill = GridBagConstraints.NONE;
		Insets insets = new Insets(1,0,1,0);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = insets;
		c.weighty = 0.0;
		c.anchor = GridBagConstraints.WEST;
		panel.setLayout(new GridBagLayout());

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		panel.add(c1slider, c);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		panel.add(c2slider, c);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		panel.add(c3slider, c);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		panel.add(cslider, c);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		panel.add(sslider, c);
		
		super.getInspector();
		shrinkPanel.add(panel, c);
		return shrinkPanel;
	}

	@Override
	public String toString() {
		Color foo2 = getColor();
		float[] foo = foo2.getRGBComponents(null);
		return String.valueOf(foo[0])+"\t"+String.valueOf(foo[1])+"\t"+String.valueOf(foo[2]);

	}
	
}
