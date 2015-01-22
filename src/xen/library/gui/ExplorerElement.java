package xen.library.gui;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ExplorerElement {
	
	String name;
	URL imagePath;
	ImageIcon image;
	
	public ExplorerElement(String name, URL imagePath) {
		this.name = name;
		this.imagePath = imagePath;
	}
	
	public String getName(){
		return name;
	}
	
	public ImageIcon getImage() {
	    if (image == null) {
	      try {
			image = new ImageIcon(ImageIO.read(imagePath).getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		} catch (IOException e) {
			e.printStackTrace();
		}
	    }
	    return image;
	}
	
	public String toString() {
	    return name;
	}

}
