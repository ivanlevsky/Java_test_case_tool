package com.casetool.tree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.tree.DefaultMutableTreeNode;

import com.casetool.utils.DatasetsUtils;




public class DynamicTreeDemo extends JPanel {
    
    private DynamicTree treePanel;
   
//    private static JTextArea area;
    private static JTextPane area;
//    private static String areaText = "";
    public static final String ToolInstructionText ="Ctrl + s  保存" + System.lineSeparator() + 
    										"Ctrl + e  展开节点" + System.lineSeparator() + 
    										"Ctrl + d 或 DELETE 删除节点" + System.lineSeparator() + 
    										"Ctrl + n  新增节点(不支持测试点新增)" + System.lineSeparator() + 
    										"Ctrl + c  复制节点" + System.lineSeparator() + 
    										"Ctrl + x  剪切节点" + System.lineSeparator() + 
    										"Ctrl + v  粘贴节点" + System.lineSeparator() 
    		;

    public static HashMap<String, String> toolSettings = new HashMap<String, String>();
    private static String selectionConfirmed = "";
    
    private static final String titleIcon= System.getProperty("user.dir") + "/assets/title_icon.png";
    private static File saveFolder = new File("C:\\Users\\");
    private static final String defaultSaveName = System.getProperty("user.dir") + "/excel.xlsx"; 
    
    public static String inputTypeVal1 = "";
    public static List<String> inputTypeVal2 = new ArrayList<String>();
    public static List<String> inputTypeVal3 = new ArrayList<String>();
    public static List<String> inputTypeVal4 = new ArrayList<String>();
    public static List<String> inputTypeVal5 = new ArrayList<String>();
    
    public static Boolean autoSetDefaultParam = true;
    
    
    public DynamicTreeDemo() {
        super(new BorderLayout());
        
        //Create the components.
        treePanel = new DynamicTree();
        populateTree(treePanel);
        treePanel.setPreferredSize(new Dimension(200, 100));

        //Lay everything out.
//        setSize(400, 300);
	    
        //右侧上面添加案例数据面板
	    JPanel testPointPanel = new JPanel();
	    testPointPanel.setPreferredSize(new Dimension(600, 400));
	    testPointPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),"测试点参数设置"));
//	    add(addDataPanel, BorderLayout.CENTER);
	    addDataTypePanel(testPointPanel);
	    
	    //日志面板
	    area = new JTextPane();
	    area.setAutoscrolls(true);
	    
		area.setSelectedTextColor(Color.WHITE);
    	area.setSelectionColor(Color.BLACK);
	    area.setEditable(false);
	    JScrollPane logPanel = new JScrollPane(area);
	    logPanel.setPreferredSize(new Dimension(600, 200));
	    logPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),"日志输出"));
	    logPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	    //右侧面板上下分割
	    JSplitPane jSplitPaneEast = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
	    		testPointPanel, logPanel);
	    //整体窗口面板左右分割
	    JSplitPane jSplitPaneCenter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                treePanel, jSplitPaneEast);
//	    jSplitPaneCenter.setDividerLocation(0.5);
	    add(jSplitPaneCenter, BorderLayout.CENTER);
    }
    
    
    
    public static JMenuBar menuInit() {
    	JMenuBar menuBar;
    	menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("文件"); 
        JMenu settingMenu = new JMenu("设置"); 
        JMenu helpMenu = new JMenu("帮助"); 
        
        menuBar.add(fileMenu); 
        menuBar.add(settingMenu); 
        menuBar.add(helpMenu);
        menuBar.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        JMenuItem exportCasesItem= new JMenuItem("导出案例");
        JMenuItem settingMenuItem = new JMenuItem("工具配置"); 
        JMenuItem instructionMenuItem = new JMenuItem("说明"); 
        JMenuItem versionMenuItem = new JMenuItem("版本"); 
        fileMenu.add(exportCasesItem);
        settingMenu.add(settingMenuItem);
        helpMenu.add(instructionMenuItem);
        helpMenu.add(new JSeparator());
        helpMenu.add(versionMenuItem);
        
        exportCasesItem.addMouseListener(new MouseAdapter() {
        	public void mouseReleased(MouseEvent e) {
//        		System.out.println(DynamicTree.isTreeSelected());
        		if(DynamicTree.isTreeSelected(1)) {
	        		JFileChooser jFileChooser = new JFileChooser(saveFolder);
	        		// Open the save dialog
//	        		jFileChooser.showSaveDialog(null);
//	        		jFileChooser.setFileView(null);
	        		DynamicTree.getExportTreeData();
//	        		System.out.print(DynamicTree.exportDataString);
	        		DatasetsUtils.writeExcel(defaultSaveName, "Sheet1", DynamicTree.exportDataString, false);
        		}else {
        			JOptionPane.showMessageDialog(menuBar.getParent(), "请选择项目节点！！", "提示", JOptionPane.INFORMATION_MESSAGE);
        		}
        	}
        });
        
  
        
        settingMenuItem.addMouseListener(new MouseAdapter() {
        	public void mouseReleased(MouseEvent e) {
        		if(SwingUtilities.isLeftMouseButton(e)){
//        			JOptionPane.showMessageDialog(menuBar.getParent(), "", "工具设置", JOptionPane.PLAIN_MESSAGE);
        			
        			String message = "工具配置选项";
        			JCheckBox checkbox1 = new JCheckBox("自动保存");
        			JCheckBox checkbox2 = new JCheckBox("自动备份");
        			JCheckBox checkbox3 = new JCheckBox("自动设置默认参数");
        			JCheckBox checkbox4 = new JCheckBox("默认最大化窗口");
        			JCheckBox checkbox5 = new JCheckBox("默认路径");
        			Object[] params = {message, checkbox1,checkbox2, checkbox3, checkbox4,checkbox5};
        			int n = JOptionPane.showConfirmDialog(menuBar.getParent(), params, "工具设置", JOptionPane.YES_NO_OPTION);
        			boolean dontShow = checkbox1.isSelected();
            	}
        	}
		});
        
        instructionMenuItem.addMouseListener(new MouseAdapter() {
        	public void mouseReleased(MouseEvent e) {
        		if(SwingUtilities.isLeftMouseButton(e)){
        			JOptionPane.showMessageDialog(menuBar.getParent(), ToolInstructionText, "快捷键说明", JOptionPane.QUESTION_MESSAGE);
            	}
        	}
		});
        
        versionMenuItem.addMouseListener(new MouseAdapter() {
        	public void mouseReleased(MouseEvent e) {
        		if(SwingUtilities.isLeftMouseButton(e)){
        			JOptionPane.showMessageDialog(menuBar.getParent(), "作者: 周之奇", "版本 v0.2", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(titleIcon));
            	}
        	}
		});
        return menuBar;
    }
    
    
    public void addDataTypePanel(JPanel panel) {
    	String dataType1 = "输入型";
    	String dataType2 = "时间型";
    	String dataType3 = "选择型";
    	String dataType4 = "debug";
    	JPanel settingPanel = new JPanel();
        settingPanel.setPreferredSize(new Dimension(580, 300));
        settingPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    	JLabel dataTypeComboBoxLabel=new JLabel("参数类型: ");
    	String[] params = {"--请选择--", dataType1, dataType3, dataType4};
        JComboBox<String> dataTypeComboBox = new JComboBox<>(params);
        dataTypeComboBox.setPreferredSize(new Dimension(200,28));
        JButton settingDataTypeButton = new JButton("设置参数");
        JButton dataTypeAddButton = new JButton("添加测试点");
        
        JLabel jLabel1 =new JLabel("字符输入条件: ");
    	JLabel jLabel2 =new JLabel("字符长度: ");
    	JLabel jLabel3 =new JLabel("字符是否必填: ");
    	JLabel jLabel4 =new JLabel("字符输入类型: ");
    	JLabel jLabel5 =new JLabel("字符自定义输入: ");
    	JLabel jLabel6 =new JLabel("输入待添加自定义字符: ");
    	JTextField jTextField1 = new JTextField(10);
    	JTextField jTextField2 = new JTextField(5);
    	JList<String> jList1 = new JList<String>(new String[] {"所有", "小于", "小于等于","等于", "大于等于", "大于", "不等于" });
    	JList<String> jList2 = new JList<String>(new String[] {"必填", "非必填" });
    	JList<String> jList3 = new JList<String>(new String[] {"所有", "中文", "英文", "数字", "全角", "特殊符号" });
    	JList<String> jList4 = new JList<String>();
    	DefaultListModel<String> dlm = new DefaultListModel<String>();
    	dlm.addElement("所有");
    	jList4.setModel(dlm);
        jList1.setFixedCellHeight(15);
        jList1.setFixedCellWidth(100);
        jList1.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jList1.setVisibleRowCount(4);
        jList1.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        jList2.setFixedCellHeight(15);
        jList2.setFixedCellWidth(100);
        jList2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList2.setVisibleRowCount(4);
        jList2.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        jList3.setFixedCellHeight(15);
        jList3.setFixedCellWidth(100);
        jList3.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jList3.setVisibleRowCount(4);
        jList3.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        jList4.setFixedCellHeight(15);
        jList4.setFixedCellWidth(100);
        jList4.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jList4.setVisibleRowCount(4);
        jList4.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        //根据自动设置默认参数, 设置默认下拉选项和输入框值
        if(autoSetDefaultParam) {
        	  jList1.setSelectedIndex(0);
              jList2.setSelectedIndex(1);
              jList3.setSelectedIndex(0);
              jList4.setSelectedIndex(0);
              jTextField1.setText("1");
        }
      
        
        
        JButton jButtonAdd = new JButton("添加");
        JButton jButtonDel = new JButton("删除");
        
        settingDataTypeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!selectionConfirmed.equals(dataTypeComboBox.getSelectedItem())){
					if(dataTypeComboBox.getSelectedItem().equals(dataType1)) {
						GroupLayout settingLayout = new GroupLayout(settingPanel);
				        settingPanel.setLayout(settingLayout);
				        settingLayout.setAutoCreateGaps(true);
				        settingLayout.setAutoCreateContainerGaps(true);
				        
				        settingLayout.setHorizontalGroup(
				        		settingLayout.createSequentialGroup()
				        			.addGroup(settingLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				        		   		   .addComponent(jLabel1)
				        		           .addComponent(jLabel4)
				        		           )
				        			.addGroup(settingLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
					        		   	   .addComponent(jList1)
					        		       .addComponent(jList3)
					        		       )
				        			.addGroup(settingLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
			        					   .addGroup(settingLayout.createSequentialGroup()
			        							.addComponent(jLabel2)
				        		   				.addComponent(jTextField1)
				        		   				.addComponent(jLabel3)
				        		   				.addComponent(jList2)
				        		   					)
			        					   .addGroup(settingLayout.createSequentialGroup()
			        							    .addGroup(settingLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
			        							    		.addGroup(settingLayout.createSequentialGroup()
			        							    				.addComponent(jLabel5)
									        		   				.addComponent(jList4)
			        							    				)
							        		   				.addGroup(settingLayout.createSequentialGroup()
							        		   						.addComponent(jLabel6)
							        		   						.addComponent(jTextField2)
									        		   				.addComponent(jButtonAdd)
									        		   				.addComponent(jButtonDel)
							        		   						)
			        							    		)
			        							   )   
				        					)
				        );
				        settingLayout.setVerticalGroup(
				        		settingLayout.createSequentialGroup()
				        			.addGroup(settingLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				        					.addComponent(jLabel1)
				        					.addComponent(jList1)
				        					.addComponent(jLabel2)
				        					.addComponent(jTextField1)
				        					.addComponent(jLabel3)
				        					.addComponent(jList2)
				        					)
				        			.addGroup(settingLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				        					.addComponent(jLabel4)
				        					.addComponent(jList3)
				        					.addGroup(settingLayout.createSequentialGroup()
					        							 .addGroup(settingLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							        			                 .addComponent(jLabel5)
							        			                 .addComponent(jList4)
					        									 )
					        			                  .addGroup(settingLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					        			                		 .addComponent(jLabel6)
							        			                 .addComponent(jTextField2)
							        			                 .addComponent(jButtonAdd)
							        			                 .addComponent(jButtonDel)
							        			                 )
				        							)
				        					)
				        );
						
						selectionConfirmed = dataType1;
					}else if(dataTypeComboBox.getSelectedItem().equals(dataType4)) {
						addDataTypePanelSubDebug(settingPanel);
						selectionConfirmed = dataType4;
					}
					settingPanel.revalidate();
					settingPanel.repaint();
				}
			}
		});
        
        jTextField1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jTextField1.setText(jTextField1.getText());
			}
		});
        jTextField2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jTextField1.setText(jTextField2.getText());
			}
		});
        jButtonAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!jTextField2.getText().trim().equals("")) {
					if(!dlm.contains(jTextField2.getText())) {
						dlm.addElement(jTextField2.getText());
						jTextField2.setText("");
					}
				}
			}
		});
        jButtonDel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<String> j4Lists = jList4.getSelectedValuesList();
				if(j4Lists.size() > 0) {
					if(j4Lists.contains("所有")) {
						dlm.removeAllElements();
						dlm.addElement("所有");
					}else {
						for (String string : j4Lists) {
							dlm.removeElement(string);
						}
					}
				}
			}
		});
        
        dataTypeAddButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(selectionConfirmed.equals(dataTypeComboBox.getSelectedItem())){
					if(dataTypeComboBox.getSelectedItem().equals(dataType1)) {
						if(jTextField1.getText().trim().equals("")) {
							JOptionPane.showMessageDialog(dataTypeComboBox.getParent(), "请输入字符长度！！", "提示", JOptionPane.INFORMATION_MESSAGE);
						}else if(jList1.getSelectedIndex() == -1) {
							JOptionPane.showMessageDialog(dataTypeComboBox.getParent(), "请选择字符输入条件！！", "提示", JOptionPane.INFORMATION_MESSAGE);
						}else if(jList2.getSelectedIndex() == -1) {
							JOptionPane.showMessageDialog(dataTypeComboBox.getParent(), "请选择字符是否必填！！", "提示", JOptionPane.INFORMATION_MESSAGE);
						}else if(!Pattern.matches("\\d+", jTextField1.getText().trim())){
							JOptionPane.showMessageDialog(dataTypeComboBox.getParent(), "字符长度为整数！！", "提示", JOptionPane.INFORMATION_MESSAGE);
						}else {
							inputTypeVal1 = jTextField1.getText();
							if(jList1.getSelectedValuesList().contains("所有")) {
								inputTypeVal2 = Arrays.asList(new String[] {"小于", "小于等于","等于", "大于等于", "大于", "不等于"});
							}else {
								inputTypeVal2 = jList1.getSelectedValuesList();
							}
							inputTypeVal3 = jList2.getSelectedValuesList();
							if(jList3.getSelectedValuesList().contains("所有")) {
								inputTypeVal4 = Arrays.asList(new String[] {"中文", "英文", "数字", "全角", "特殊符号" });
							}else {
								inputTypeVal4 = jList3.getSelectedValuesList();
							}
							if(jList4.getSelectedValuesList().contains("所有")) {
//								inputTypeVal4 = (List<String>) dlm.elements();
								List<String> j5All = new ArrayList<String>();
								for (int i = 1; i < dlm.getSize(); i++) {
									j5All.add(dlm.get(i));
								}
								inputTypeVal5 = j5All;
							}else {
								inputTypeVal5 = jList4.getSelectedValuesList();
							}
							if(DynamicTree.isTreeSelected(4)) {
								DynamicTree.addInputTypeCases();
								
							}else {
								JOptionPane.showMessageDialog(dataTypeComboBox.getParent(), "请选择左侧测试点", "提示", JOptionPane.INFORMATION_MESSAGE);
							}
						}
						
//						System.out.println(jList1.getSelectedValuesList());)
					}else if(dataTypeComboBox.getSelectedItem().equals(dataType4)) {
//						addDataTypePanelSubDebug(settingPanel);
//						selectionConfirmed = dataType3;
					}
//					settingPanel.revalidate();
//					settingPanel.repaint();
				}
			}
		});
        
        
        
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(
        		   layout.createSequentialGroup()
        		   	  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        		   		   .addGroup(layout.createSequentialGroup()
        		   				   .addComponent(dataTypeComboBoxLabel)
        		   				   .addComponent(dataTypeComboBox)
        		   				   .addComponent(settingDataTypeButton)
        		   				   .addComponent(dataTypeAddButton))
        		           .addComponent(settingPanel))
        		   	  
        		      
        );
		layout.setVerticalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		           .addComponent(dataTypeComboBoxLabel)
		           .addComponent(dataTypeComboBox)
		           .addComponent(settingDataTypeButton)
		           .addComponent(dataTypeAddButton))
		      .addComponent(settingPanel)
		);
        panel.revalidate();
    }
    

    public void addDataTypePanelSubDebug(JPanel panel) {
    	panel.removeAll();
    }
    
    public static void printLogInJTextArea(String text, Color color) {
    	SimpleDateFormat formater = new SimpleDateFormat ("yyyy-MM-dd HH:MM:ss");
    	String dateText = formater.format(new Date()) + ": ";
    	Document doc = area.getStyledDocument();
    	text += System.lineSeparator();
    	try {
    		
    		SimpleAttributeSet attrSet = new SimpleAttributeSet();
    		area.setCharacterAttributes(attrSet, true);
    		StyleConstants.setForeground(attrSet, Color.BLACK);
    		doc.insertString(doc.getLength(), dateText, attrSet);
    		
    		if(color != null) {
    			StyleConstants.setForeground(attrSet, color);
    		}
    		
			doc.insertString(doc.getLength(), text, attrSet);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
    	
    }

    //默认树结构
    public void populateTree(DynamicTree treePanel) {
        String p1Name = new String("测试项目 1");
        String p2Name = new String("测试项目 2");
        String c1Name = new String("模块 1");
        String c2Name = new String("模块 2");
        String cc1Name = new String("子模块 1");
        String cc2Name = new String("子模块 2");

        DefaultMutableTreeNode p1, p2;

        p1 = treePanel.addObject(null, p1Name);
        p2 = treePanel.addObject(null, p2Name);
        

        DefaultMutableTreeNode c1 = treePanel.addObject(p1, c1Name);
        DefaultMutableTreeNode c2 = treePanel.addObject(p1, c2Name);

        DefaultMutableTreeNode c3 = treePanel.addObject(p2, c1Name);
        DefaultMutableTreeNode c4 = treePanel.addObject(p2, c2Name);
        
        treePanel.addObject(c1, cc1Name);
        treePanel.addObject(c1, cc2Name);
        treePanel.addObject(c2, cc1Name);
        treePanel.addObject(c2, cc2Name);
        
        treePanel.addObject(c3, cc1Name);
        treePanel.addObject(c3, cc2Name);
        treePanel.addObject(c4, cc1Name);
        treePanel.addObject(c4, cc2Name);
        
        
    }


    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("案例数据生成工具");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        File iconFile = new File(titleIcon);
        if(iconFile.exists()) {
        	try {
        		frame.setIconImage(ImageIO.read(iconFile));
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        //Create and set up the content pane.
        DynamicTreeDemo newContentPane = new DynamicTreeDemo();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
        frame.setJMenuBar(menuInit());
        
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
        	public void windowClosing(WindowEvent e) {
        		if(DynamicTree.nodeEditSaved) {
	        		Object[] params = {"数据未保存，是否退出?"};
	        		int result = JOptionPane.showConfirmDialog(frame, params, "关闭窗口", JOptionPane.CANCEL_OPTION);
	        		if (result == JOptionPane.OK_OPTION) {
	                    frame.dispose();
	        		}
        		}else {
                    frame.dispose();
        		}
        	}
        });
        //Display the window.
        frame.pack();
        
        frame.setLocationRelativeTo(null);//居中显示
        frame.setVisible(true);
        
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}