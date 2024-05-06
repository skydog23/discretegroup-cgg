/*
 * Created on 19 Apr 2023
 *
 */
package discreteGroup.quartz;

import java.awt.Color;

import charlesgunn.anim.util.AnimationUtility;


public class QuartzConstants {

	static Color chan1 = Color.yellow,
			chan2 = Color.cyan,
			chan3 = new Color(51,255,51);
	static Color fclrs[] = {chan1, chan2, chan1, chan2};
	static Color vclrs[] = {chan1, chan1, chan2, chan2};
	static Color eclrs[] = {Color.orange, chan2, chan3, chan2, chan1, Color.orange}; 
	static Color eclrs4[] = {chan1, chan3, chan3, chan2}; // Color.yellow, Color. green, Color.blue};
	static Color hfclrs[] = {chan1, chan1, chan2, chan2}; 
	static Color heclrs[] = {chan2, chan3, chan1,chan3, chan2};  
	static final double sq3 = 1/Math.sqrt(3.0);
	static double  
			yAxisPts[][] = {{0,0,0,1}, {0,1,0,1}},
			hexTrans[][] = {{1,sq3,0},{0, 2*sq3,0},{-1,sq3,0}},
			axis3Pts[][] = {{1.0/3.0,0,0,1}, {1.0/3.0,0,1,1}},
			hex3Pts[][] = {{0,sq3,0,1}, {0,0,1,0}};
	
	static double stickRad = .024, oxygenRad = .06, siliconRad = .11, 
			basScale = 1, 
			saturated = .5,
			saturated2 = .15;
	static  Color oxygenColor = new Color(255,70,70), 
			siliconColor = new Color(255,255,255),
			chan31Color = AnimationUtility.linearInterpolation( Color.white, chan1, saturated),
			chan32Color = AnimationUtility.linearInterpolation( Color.white, chan2, saturated),
			chan6Color = AnimationUtility.linearInterpolation( Color.white, chan3, saturated),
			edge3Color = AnimationUtility.linearInterpolation( Color.white, Color.magenta, saturated),
			edge4Color = AnimationUtility.linearInterpolation( Color.white, Color.red, saturated),
			RD1 = AnimationUtility.linearInterpolation( Color.white, chan1, saturated2),
			RD2 = AnimationUtility.linearInterpolation( Color.white, edge3Color, saturated2),
			RD3 = AnimationUtility.linearInterpolation( Color.white, chan2, saturated2),
			RD4 = AnimationUtility.linearInterpolation( Color.white, edge4Color, saturated2);
;


}
