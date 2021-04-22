package discreteGroup.demo;

import java.awt.Component;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import discreteGroup.EquidistantTextureFactory;

public class EquidistantTextureDemo extends LoadableScene {

	EquidistantTextureFactory etf = new EquidistantTextureFactory();
	@Override
	public SceneGraphComponent makeWorld() {
		etf.update();
		SceneGraphComponent sgc = etf.getSceneGraphComponent();
		return sgc;
	}
	@Override
	public boolean isEncompass() {
		return true;
	}
	
	@Override
	public boolean hasInspector() {return true;}
	
	@Override
	public Component getInspector(final Viewer viewer) {
		return etf.getInspector();
	}
	
}
