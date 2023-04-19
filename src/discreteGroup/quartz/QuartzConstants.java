/*
 * Created on 19 Apr 2023
 *
 */
package discreteGroup.quartz;

import java.awt.Color;

public class QuartzConstants {

	static Color hfclrs[] = {Color.yellow, Color.yellow, Color.cyan, Color. cyan}; 
	static Color heclrs[] = {Color.cyan, Color.magenta,  Color.yellow, Color.magenta, Color.cyan}; 
	static final double sq3 = 1/Math.sqrt(3.0);
	static double  
			yAxisPts[][] = {{0,0,0,1}, {0,1,0,1}},
			triTrans[][] = {{1, sq3,0}, {1, -sq3, 0}, {0,0,1}},
			axis3Pts[][] = {{1.0/3.0,0,0,1}, {1.0/3.0,0,1,1}},
			hex3Pts[][] = {{0,sq3,0,1}, {0,0,1,1}};
	
	static double stickRad = .024, oxygenRad = .06, siliconRad = .11, basScale = 4;
	static  Color oxygenColor = new Color(255,70,70), siliconColor = new Color(255,255,255);


}
