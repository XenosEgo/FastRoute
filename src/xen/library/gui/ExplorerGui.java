package xen.library.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import xen.library.files.Archive;

public class ExplorerGui extends JList<ExplorerElement>{

	private static final long serialVersionUID = -3129713011075400086L;
	
	private String path;
	
	public ExplorerGui(ExplorerElement[] list){
		super(list);
		setCellRenderer(new ExplorerRenderer());
	}
	
	public ExplorerGui(){
		setCellRenderer(new ExplorerRenderer());
	}
	
	public void setCurrentPath(String path){
		this.path = path;
	}
	
	public String getCurrentPath(){
		return path;
	}

	public static ExplorerElement[] genList(File file) throws IOException {
		if(file.isDirectory()){
			ExplorerElement[] output = new ExplorerElement[file.list().length];
			int i = 0;
			for(File f : file.listFiles()){
				if(f.isDirectory()){
					output[i] = new ExplorerFolder(f.getName());
					i++;
				}
			}
			for(File f : file.listFiles()){
				if(f.isFile()){
					output[i] = new ExplorerFile(f.getName());
					i++;
				}
			}
			return output;
		}
		return new ExplorerElement[0];
	}

	public void upFolder(JTextField currentPath) {
		if(Archive.getInstance().inArchive()){
			
		} else {
			if(new File(currentPath.getText()).getParentFile() != null){
				String path = new File(currentPath.getText()).getParentFile().getAbsolutePath();
				try{
					setListData(ExplorerGui.genList(new File(path)));
					setCurrentPath(path);
					currentPath.setText(path);
				} catch (IOException e1){
					JOptionPane.showMessageDialog(currentPath,"Error: " + path + " " + e1.getLocalizedMessage(),"Error!",JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	public void currentPath(JTextField currentPath) {
		currentPath.getParent().requestFocus();
		if(new File(currentPath.getText()).exists()){
			if(new File(currentPath.getText()).isDirectory()){
				String path = new File(currentPath.getText()).getAbsolutePath();
				try{
					setListData(ExplorerGui.genList(new File(path)));
					setCurrentPath(path);
					currentPath.setText(path);
				} catch (IOException e1){
					JOptionPane.showMessageDialog(currentPath,"Error: " + path + " " + e1.getLocalizedMessage(),"Error!",JOptionPane.ERROR_MESSAGE);
				}
			} else if(currentPath.getText().endsWith(".sprk")){
				
			} else {
				try {
					Desktop.getDesktop().open(new File(currentPath.getText()));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} else {
			JOptionPane.showMessageDialog(currentPath,"Error: " + currentPath.getText() + " Does not exist!","Error!",JOptionPane.ERROR_MESSAGE);
			currentPath.setText(getCurrentPath());
		}
	}

	public void mousePressed(JTextField currentPath) {
		if(Archive.getInstance().inArchive()){
			
		} else {
			String path = new File(currentPath.getText() + File.separatorChar + getSelectedValue().getName()).getAbsolutePath();
			if(new File(path).isDirectory()){
				try{
					setListData(ExplorerGui.genList(new File(path)));
					setCurrentPath(path);
					currentPath.setText(path);
				} catch (IOException e1){
					JOptionPane.showMessageDialog(currentPath,"Error: " + path + " " + e1.getLocalizedMessage(),"Error!",JOptionPane.ERROR_MESSAGE);
				}
			} else if(path.endsWith(".sprk")){
			
			} else {
				try {
					Desktop.getDesktop().open(new File(path));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

}
