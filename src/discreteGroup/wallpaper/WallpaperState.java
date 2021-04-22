/*
 * Created on Jan 3, 2013
 *
 */
package discreteGroup.wallpaper;

import java.awt.image.BufferedImage;
import java.io.Serializable;

public class WallpaperState implements Serializable {

	protected String groupName;
	BufferedImage bufferedImage;
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public BufferedImage getBufferedImage() {
		return bufferedImage;
	}
	public void setBufferedImage(BufferedImage bufferedImage) {
		this.bufferedImage = bufferedImage;
	}
}
