package gui;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JProgressBar;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;

import xen.Main;
import xen.library.gui.ExplorerGui;

import java.awt.Font;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.JScrollPane;

import network.MultiCastThread;
import network.PingThread;

import javax.swing.JPopupMenu;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

public class Form extends JFrame{
	private static final long serialVersionUID = -3526248974305872670L;
	
	private static Form instance = null;
		
	public static Form getInstance(){
		if(instance == null){
			instance = new Form();
		}
		return instance;
	}
	
	private Listener l = new Listener();
	
	private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	private JTextField from;
	private JTextField to;
	private JLabel detailLabel;
	private JLabel progressLabel;
	private JProgressBar progressBar;
	private JButton goButton;
	private JButton refresh;
	private JTextField name;
	private JTextField lanFrom;
	private JList<String> usersOnline;
	private JLabel lblDetails_1;
	private JLabel detailLan;
	private JButton fromButton;
	private JButton toButton;
	private JButton lanFromButton;
	private JMenuItem addIp;
	private JTextField currentPath;
	private ExplorerGui explorer;
	private JButton upFolder;
	
	private Form(){
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("FastRoute");
		getContentPane().setLayout(null);
		setBounds(0, 0, 438, 291);
		setResizable(false);
		setLocationRelativeTo(null);
		
		try {
			setIconImage(ImageIO.read(this.getClass().getResource("/ressource/icon.png")));
		} catch (IOException e) {
			e.printStackTrace();
			
		}
		
		JPanel Archiver = new JPanel();
		//tabbedPane.addTab("Archiver", null, Archiver, null);
		Archiver.setLayout(null);
		
		currentPath = new JTextField(System.getProperty("user.home"));
		currentPath.setBounds(38, 11, 381, 20);
		currentPath.addActionListener(l);
		Archiver.add(currentPath);
		currentPath.setColumns(10);
		
		
		try{
			explorer = new ExplorerGui(ExplorerGui.genList(new File(currentPath.getText())));
			explorer.setCurrentPath(currentPath.getText());
		} catch (IOException e){
			explorer = new ExplorerGui();
		}
		explorer.addMouseListener(l);
		JScrollPane explorerPane = new JScrollPane(explorer);
		explorerPane.setBounds(10, 42, 409, 158);
		Archiver.add(explorerPane);
		
		upFolder = new JButton();
		upFolder.addActionListener(l);
		upFolder.setBounds(10, 10, 24, 23);
		try {
			upFolder.setIcon(new ImageIcon(ImageIO.read(Form.class.getResource("/ressource/Up-folder.png")).getScaledInstance(upFolder.getWidth()*2/3, upFolder.getHeight()*2/3, Image.SCALE_SMOOTH)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Archiver.add(upFolder);
		
		tabbedPane.setBounds(0, 0, 434, 239);
		getContentPane().add(tabbedPane);
		
		JPanel LAN = new JPanel();
		tabbedPane.addTab("LAN", null, LAN, null);
		LAN.setLayout(null);
		
		JLabel lblUsers = new JLabel("Users:");
		lblUsers.setBounds(10, 11, 46, 14);
		LAN.add(lblUsers);
		
		refresh = new JButton("Refresh");
		refresh.setBounds(52, 7, 84, 23);
		refresh.addActionListener(l);
		LAN.add(refresh);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 36, 126, 164);
		LAN.add(scrollPane);
		
		usersOnline = new JList<String>();
		usersOnline.setFont(new Font("Tahoma", Font.PLAIN, 9));
		scrollPane.setViewportView(usersOnline);
		usersOnline.setModel(new AbstractListModel<String>() {
			private static final long serialVersionUID = 5727317642350297462L;
			String[] values = new String[0];
			public int getSize() {
				return values.length;
			}
			public String getElementAt(int index) {
				return values[index];
			}
		});
		
		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(usersOnline, popupMenu);
		
		addIp = new JMenuItem("add direct IP");
		addIp.addActionListener(l);
		popupMenu.add(addIp);
		
		name = new JTextField();
		name.setBounds(202, 8, 217, 20);
		LAN.add(name);
		name.setColumns(10);
		
		JLabel lblName = new JLabel("Name:");
		lblName.setBounds(146, 11, 46, 14);
		LAN.add(lblName);
		
		lanFrom = new JTextField();
		lanFrom.setBounds(146, 85, 239, 20);
		LAN.add(lanFrom);
		lanFrom.setColumns(10);
		lanFrom.setDropTarget(new DropTarget() {
			private static final long serialVersionUID = -3715935897379683479L;
			@SuppressWarnings("unchecked")
			public synchronized void drop(DropTargetDropEvent evt) {
	            try {
	                evt.acceptDrop(DnDConstants.ACTION_COPY);
	                List<File> droppedFiles = (List<File>) evt
	                        .getTransferable().getTransferData(
	                                DataFlavor.javaFileListFlavor);
	                lanFrom.setText(droppedFiles.get(0).getAbsolutePath());
	            } catch (Exception ex) {
	                ex.printStackTrace();
	            }
	        }
		});
		
		JLabel lblFrom_1 = new JLabel("From:");
		lblFrom_1.setBounds(146, 60, 46, 14);
		LAN.add(lblFrom_1);
		
		lblDetails_1 = new JLabel("Details:");
		lblDetails_1.setBounds(146, 116, 46, 14);
		LAN.add(lblDetails_1);
		
		detailLan = new JLabel("put details here");
		detailLan.setFont(new Font("Tahoma", Font.BOLD, 12));
		detailLan.setBounds(146, 141, 273, 58);
		LAN.add(detailLan);
		
		lanFromButton = new JButton("...");
		lanFromButton.setBounds(395, 85, 24, 20);
		lanFromButton.addActionListener(l);
		LAN.add(lanFromButton);
		
		JPanel Local = new JPanel();
		tabbedPane.addTab("Local", null, Local, null);
		Local.setLayout(null);
		
		from = new JTextField();
		from.setBounds(10, 30, 375, 20);
		Local.add(from);
		from.setColumns(10);
		from.setDropTarget(new DropTarget() {
			private static final long serialVersionUID = -3715935897379683479L;
			@SuppressWarnings("unchecked")
			public synchronized void drop(DropTargetDropEvent evt) {
	            try {
	                evt.acceptDrop(DnDConstants.ACTION_COPY);
	                List<File> droppedFiles = (List<File>) evt
	                        .getTransferable().getTransferData(
	                                DataFlavor.javaFileListFlavor);
	                from.setText(droppedFiles.get(0).getAbsolutePath());
	            } catch (Exception ex) {
	                ex.printStackTrace();
	            }
	        }
		});
		
		to = new JTextField();
		to.setBounds(10, 86, 375, 20);
		Local.add(to);
		to.setColumns(10);
		to.setDropTarget(new DropTarget() {
			private static final long serialVersionUID = 9189257289792530354L;
			@SuppressWarnings("unchecked")
			public synchronized void drop(DropTargetDropEvent evt) {
	            try {
	                evt.acceptDrop(DnDConstants.ACTION_COPY);
	                List<File> droppedFiles = (List<File>) evt
	                        .getTransferable().getTransferData(
	                                DataFlavor.javaFileListFlavor);
	                if(droppedFiles.get(0).isFile()){
	                	to.setText(droppedFiles.get(0).getParentFile().getAbsolutePath() + File.separator);
	                } else {
	                	to.setText(droppedFiles.get(0).getAbsolutePath() + File.separator);
	                }
	            } catch (Exception ex) {
	                ex.printStackTrace();
	            }
	        }
		});
		
		JLabel lblFrom = new JLabel("From:");
		lblFrom.setBounds(10, 11, 46, 14);
		Local.add(lblFrom);
		
		JLabel lblTo = new JLabel("To:");
		lblTo.setBounds(10, 61, 46, 14);
		Local.add(lblTo);
		
		JLabel lblDetails = new JLabel("Details:");
		lblDetails.setBounds(10, 117, 46, 14);
		Local.add(lblDetails);
		
		detailLabel = new JLabel("put details here");
		detailLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		detailLabel.setBounds(10, 142, 409, 58);
		Local.add(detailLabel);
		
		fromButton = new JButton("...");
		fromButton.setBounds(395, 30, 24, 20);
		fromButton.addActionListener(l);
		Local.add(fromButton);
		
		toButton = new JButton("...");
		toButton.setBounds(395, 86, 24, 20);
		toButton.addActionListener(l);
		Local.add(toButton);
		
		JPanel drive = new JPanel();
		//tabbedPane.addTab("Drive backup", null, drive, null);
		drive.setLayout(null);
		
		JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"hi", "test", "tes2", "   "}));
		comboBox.setBounds(10, 11, 165, 20);
		drive.add(comboBox);
		
		progressLabel = new JLabel("0% - 0 MB/s");
		progressLabel.setBounds(122, 242, 213, 14);
		getContentPane().add(progressLabel);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(2, 239, 343, 23);
		getContentPane().add(progressBar);
		
		goButton = new JButton("Go");
		goButton.setBounds(345, 238, 85, 24);
		getContentPane().add(goButton);
		goButton.addActionListener(l);
		
		
		setVisible(true);
	}
	
	public void setGoButtonEnabled(boolean enabled) {
		goButton.setEnabled(enabled);
	}

	public void setDetailLabel(String text) {
		detailLabel.setText(text);
	}
	public void setLanDetailLabel(String text) {
		detailLan.setText(text);
	}
	
	public void setProgressLabel(String text) {
		progressLabel.setText(text);
	}
	
	public void setProgressBar(int value){
		progressBar.setValue(value);
	}
	
	public void setUserName(String name){
		this.name.setText(name);
	}
	
	public String getUserName(){
		return name.getText();
	}
	
	public void setUserList(TreeMap<String,String> list){
		String[] out = new String[list.size()]; 
		int i = 0;
		for(Entry<String,String> e : list.entrySet()){
			out[i] = e.getKey() + ": " + e.getValue();
			i++;
		}
		usersOnline.setListData(out);
	}
	public void addUserList(String ip, String name){
		String[] out = new String[usersOnline.getModel().getSize() + 1]; 
		for(int i = 0; i < usersOnline.getModel().getSize(); i++){
			out[i] = usersOnline.getModel().getElementAt(i);
		}
		out[usersOnline.getModel().getSize()] = ip + ": " + name;
		usersOnline.setListData(out);
	}
	
	public void setTabIndex(int i){
		tabbedPane.setSelectedIndex(i);
	}

	public class Listener implements ActionListener, MouseListener{
		
		final JFileChooser fc = new JFileChooser();

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == goButton){
				if(tabbedPane.getSelectedIndex() == 1){
					Main.startLocal(from.getText(), to.getText());
				} else if(tabbedPane.getSelectedIndex() == 0){
					if(!usersOnline.isSelectionEmpty()){
						Main.startInternet(usersOnline.getSelectedValue().split(":")[0].trim(), lanFrom.getText());
					} else {
						JOptionPane.showMessageDialog(Form.getInstance(),"No Users selected!","Error",JOptionPane.ERROR_MESSAGE);
					}
				}
			} else if(e.getSource() == refresh){
				if(MultiCastThread.getInstance() != null){
					try {
						MultiCastThread.getInstance().update(name.getText());
					} catch (UnknownHostException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				
			} else if(e.getSource() == fromButton){
				fc.setMultiSelectionEnabled(false);
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fc.setCurrentDirectory(new File(System.getProperty("user.home")));
					
				int returnVal = fc.showOpenDialog(Form.getInstance().getFocusOwner());
					
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					from.setText(fc.getSelectedFile().getAbsolutePath());
				}
			} else if(e.getSource() == toButton){
				fc.setMultiSelectionEnabled(false);
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fc.setCurrentDirectory(new File(System.getProperty("user.home")));
				
				int returnVal = fc.showSaveDialog(Form.getInstance().getFocusOwner());
				
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					if(fc.getSelectedFile().isFile()){
	                	to.setText(fc.getSelectedFile().getParentFile().getAbsolutePath() + File.separator);
	                } else {
	                	to.setText(fc.getSelectedFile().getAbsolutePath() + File.separator);
	                }
				}
			} else if(e.getSource() == lanFromButton){
				fc.setMultiSelectionEnabled(false);
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fc.setCurrentDirectory(new File(System.getProperty("user.home")));
				
				int returnVal = fc.showOpenDialog(Form.getInstance().getFocusOwner());
					
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					lanFrom.setText(fc.getSelectedFile().getAbsolutePath());
				}
			} else if(e.getSource() == addIp){
				String ip = JOptionPane.showInputDialog(Form.getInstance(), "Enter IP adresse:");
				if(ip != null){
					try {
						InetAddress target = InetAddress.getByName(ip);
						if(target.isReachable(2000)){
							String name = PingThread.sendPing(ip, Form.getInstance().getUserName());
							if(name != null && !name.equalsIgnoreCase("")){
								addUserList("/" + target.getHostAddress(), name);
							} else {
								JOptionPane.showMessageDialog(Form.getInstance(),ip + " is not running the application or is blocking it.","Error!",JOptionPane.ERROR_MESSAGE);
							}
						} else {
							JOptionPane.showMessageDialog(Form.getInstance(),ip + " is not reachable!","Error!",JOptionPane.ERROR_MESSAGE);
						}
					} catch (UnknownHostException e1) {
						JOptionPane.showMessageDialog(Form.getInstance(),ip + " is not a valid Hostname or IP!","Error!",JOptionPane.ERROR_MESSAGE);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(Form.getInstance(),ip + " is not reachable!","Error!",JOptionPane.ERROR_MESSAGE);
					}
				}
			} else if(e.getSource() == upFolder){
				explorer.upFolder(currentPath);
			} else if(e.getSource() == currentPath){
				explorer.currentPath(currentPath);
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {	}

		@Override
		public void mouseEntered(MouseEvent e) {	}

		@Override
		public void mouseExited(MouseEvent e) {		}

		@Override
		public void mousePressed(MouseEvent e) {
			if(e.getClickCount() == 2){
				if(e.getSource() == explorer){
					explorer.mousePressed(currentPath);
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {	}
	}
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
