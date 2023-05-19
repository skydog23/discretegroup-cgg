/*
 * Created on 10 May 2023
 *
 */
package discreteGroup.quartz;

import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.basic.Scene;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphComponent;

public class LightUtility {

	protected transient SceneGraphComponent lights = new SceneGraphComponent("lights");
	
	protected transient double[][] positions = { {-1,-1,-1}, {-.1, 1, .2},{1, .3, -.1}, {.2, -.1, 1}}; //, 
//	{1,1,1}, {1,-1,-1},{-1,1,-1}, {-1,-1,1}}; //{ {-1,-1,-1}, {-.3, 1, .2},{1, .3, -.4}, {.2, -.4, 1}};
	protected transient double intensity = .5;
    protected Light[] lightL = new Light[positions.length]
    		;
	public void setLightIntensity(double i) {
		intensity = i;
		setupLights();
	}

	public void setupLights()	{

		if (lights == null)
			lights = new SceneGraphComponent("Euclidean Lights");
		for (int i = 0; i < positions.length; ++i) {
			SceneGraphComponent lightNode = new SceneGraphComponent("light" + i);
			DirectionalLight light = new DirectionalLight();
			lightL[i] = light;
			light.setIntensity(intensity);
			lightNode.setLight(light);
			MatrixBuilder.euclidean().rotateFromTo(new double[] { 0, 0, 1 }, positions[i]).assignTo(lightNode);
			lights.addChild(lightNode);
		}
	}

	public void updateLights(double intensity, Scene scene) {
		if (lightL == null) return;
		for (int i = 0; i<lightL.length; ++i)
			lightL[i].setIntensity(intensity);
		scene.getAvatarComponent().removeChild(lights);
		scene.getAvatarComponent().addChildren(lights);

	}
	
	public SceneGraphComponent getLights() {
		return lights;
	}

}
