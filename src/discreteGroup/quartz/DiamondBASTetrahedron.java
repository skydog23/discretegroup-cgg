/*
 * Created on 10 May 2023
 *
 */
package discreteGroup.quartz;

import static discreteGroup.quartz.QuartzConstants.chan31Color;
import static discreteGroup.quartz.QuartzConstants.chan32Color;
import static discreteGroup.quartz.QuartzConstants.edge3Color;
import static discreteGroup.quartz.QuartzConstants.edge4Color;
import static discreteGroup.quartz.QuartzConstants.siliconColor;
import static discreteGroup.quartz.QuartzConstants.siliconRad;

import java.awt.Color;

public class DiamondBASTetrahedron extends BASTetrahedron {

	{
		showLabels = true;
		pointClr = new Color[] { siliconColor, siliconColor, siliconColor, siliconColor, siliconColor };
		edgeClr = new Color[] { chan31Color, edge3Color, chan32Color, edge4Color };
		vertexLabels = new String[] { "Si", "Si", "Si", "Si", "Si" };
		pointRadii = new double[] { siliconRad, siliconRad, siliconRad, siliconRad, siliconRad };
	}

}
