package xen.library.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class ExplorerRenderer extends JLabel implements ListCellRenderer<ExplorerElement>{

	private static final long serialVersionUID = 1629283495742543041L;
	
	private static final Color HIGHLIGHT_COLOR = new Color(32, 168, 255);

	  public ExplorerRenderer() {
	    setOpaque(true);
	    setIconTextGap(12);
	  }

	@Override
	public Component getListCellRendererComponent(JList<? extends ExplorerElement> list, ExplorerElement value,
			int index, boolean isSelected, boolean cellHasFocus) {

	    setText(value.getName());
	    setIcon(value.getImage());
	    if (isSelected) {
	      setBackground(HIGHLIGHT_COLOR);
	      setForeground(Color.white);
	    } else {
	      setBackground(Color.white);
	      setForeground(Color.black);
	    }
		
		return this;
	}

}
