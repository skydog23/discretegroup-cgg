package discreteGroup.wallpaper;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import javax.swing.JComponent;

public class PaintingComponent extends JComponent implements Serializable {

	PaintSource ps;
	public PaintingComponent(PaintSource ps)	{
		this.ps = ps;
	}
		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			AffineTransform old = g2.getTransform();
			g2.transform(ps.flipY);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
//			System.err.println("in paintComponent");
			if (ps.width != ps.masterBufferedImage.getWidth() || ps.height != ps.masterBufferedImage.getHeight())
				g2.drawImage(
					ps.masterBufferedImage.getScaledInstance(
							ps.width, ps.height, BufferedImage.SCALE_FAST), 
					0, 0, null);
			else
				g2.drawImage(ps.masterBufferedImage, 0, 0, null);
			// draw outline
			int n = ps.outline.length;
			g2.setColor(Color.black);
			g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			for (int i = 0; i<n/2; ++i)	{
				g2.draw(new Line2D.Double(
						ps.outlineC[2*i], ps.outlineC[2*i+1],ps.outlineC[(2*i+2)%n], ps.outlineC[(2*i+3)%n]));
			}
			g2.setTransform(old);
		}

		@Override
		public Dimension getMinimumSize() {
			return ps.size;
		}

		@Override
		public Dimension getPreferredSize() {
			return ps.size;
		}
	}
