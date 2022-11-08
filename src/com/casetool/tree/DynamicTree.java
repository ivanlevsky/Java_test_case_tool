package com.casetool.tree;


import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

@SuppressWarnings("serial")
public class DynamicTree extends JPanel {
    private final static String splitText = "S_P_L_I_T";
	private final static String lineSplit = "L_S_P_L_I_T";
	
    protected DefaultMutableTreeNode rootNode;
    protected DefaultTreeModel treeModel;
    protected static JTree tree;
    private Toolkit toolkit = Toolkit.getDefaultToolkit();
    private DefaultMutableTreeNode tempCopyTreeNode;
    private DefaultMutableTreeNode tempPasteTreeNode;
    private Boolean pressedCtrlC;
    private Boolean pressedCtrlX;
    //测试点树节点层级
    private static final int testTreeNodeLevel = 4;
    
    private static final String JtreeSaveFile = System.getProperty("user.dir")+"/tree1.sav";
    private static HashMap<String, Object> JtreeSaveObjects = new HashMap<String, Object>();
    private static LinkedHashMap<TreePath, Boolean> treeExpandStatus = new LinkedHashMap<TreePath, Boolean>();
    private static String lastSelectTreeNode = "";//保存关闭前选择树项目状态，待完成
    
    
    public static Boolean nodeEditSaved = false;//改动标识, 存在改动时为true
    public static String exportDataString;
    public DynamicTree() {
        super(new GridLayout(1,0));
        
        rootNode = new DefaultMutableTreeNode("项目");
        treeModel = new DefaultTreeModel(rootNode);
        
        tree = loadTheTreeFromFile();
        treeModel.addTreeModelListener(new MyTreeModelListener());
        
//        tree = new JTree(treeModel);
        tree.setEditable(true);
        tree.setDragEnabled(true);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.setShowsRootHandles(true);
//        tree.setToggleClickCount(1); //设置展开节点之前的鼠标单击数为1
        tree.setDragEnabled(true);
        tree.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        
//      tree.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
//		
        //树节点编辑, 支持F2、三次点击鼠标左键、两次点击鼠标左键(两次间有间隔)编辑, 编辑完回车或鼠标左右键点击编辑框外任意位置保存
        tree.getCellEditor().addCellEditorListener(new CellEditorListener() {
			@Override
			public void editingStopped(ChangeEvent e) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void editingCanceled(ChangeEvent e) {
				if(tree.getSelectionPath() != null) {
					// TODO Auto-generated method stub
					TreeCellEditor cell = (TreeCellEditor) e.getSource(); 
					String data = cell.getCellEditorValue().toString();
					DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
	                        (tree.getSelectionPath().getLastPathComponent());
					currentNode.setUserObject(data);
					if(data.equals("")) {
						currentNode.setUserObject("新增节点");
					}
					cell.stopCellEditing();
					nodeEditSaved = true;
				}
			}
		});

        
        final TreePopup treePopup = new TreePopup(tree);
        final AddDataTreePopup addDataTreePopup = new AddDataTreePopup(tree);
        tree.addMouseListener(new MouseAdapter() {
        	//鼠标右键弹出框, 不使用mousePressed, 待mouseReleased触发条件
            public void mouseReleased(MouseEvent e) {
	               if(SwingUtilities.isRightMouseButton(e)){
	            	  //使用stopCellEditing, 右键选中树节点时结束其他节点编辑状态, 不要调用stopCellEditing方法 该方法覆写会保存数据到右键新选中的其他节点
	 	        	  JTree cellTree = (JTree) e.getSource();
	 	        	  cellTree.getCellEditor().stopCellEditing();
	 	        	  
	 		          int selRow = tree.getRowForLocation(e.getX(), e.getY());
	 		          TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
	                  tree.setSelectionPath(selPath); 
	                  //鼠标点击位置为树节点坐标时触发，其他空白区域不显示弹出框
	                  if (selRow>-1){
	                	  tree.setSelectionRow(selRow); 
	                	  int treeNodeLevel = ((DefaultMutableTreeNode)selPath.getLastPathComponent()).getLevel();
	                	  if(e.isPopupTrigger()) {
	                		  if(treeNodeLevel == testTreeNodeLevel) {
	                			  addDataTreePopup.show(e.getComponent(), e.getX(), e.getY());
		                	  }else if(treeNodeLevel < testTreeNodeLevel) {
		                		  treePopup.show(e.getComponent(), e.getX(), e.getY());
		                	  }
	    	               }
	                  }
	 		      }
            }
            //鼠标拖拽树节点功能, 未完成mouseMoved、mouseDragged
         });

        
        tree.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				//delete键 或 ctrl + d 删除节点
				if(e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_D) {
					TreePath currentSelection = tree.getSelectionPath();
					//树节点存在编辑状态时不可删除编辑节点, 否则报错
			        if (currentSelection != null && !tree.isEditing()) {
			        	removeCurrentNode();
			        }
				}
				
				// contrl 组合键事件, 复制粘贴等...
				if(e.isControlDown()) {
					//ctrl + s 保存节点
					if(e.getKeyCode() == KeyEvent.VK_S) {
						saveTheTree();
					}
					
					//ctrl + n 新建节点
					if(e.getKeyCode() == KeyEvent.VK_N) {
						int treeNodeLevel = ((DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent()).getLevel();
						//测试点类型不支持快捷键新增, 需在右边面板设置
						if(treeNodeLevel < testTreeNodeLevel) {
							addObject("新增节点");
						}
					}
					
					//ctrl + e 展开节点
					if(e.getKeyCode() == KeyEvent.VK_E) {
						TreePath currentSelection = tree.getSelectionPath();
				        if (currentSelection != null) {
				        	expandOrCollapseAllTreeNode();
				        }
					}
					
					//ctrl + c 复制树节点
					if(e.getKeyCode() == KeyEvent.VK_C) {
						TreePath currentSelection = tree.getSelectionPath();
				        if (currentSelection != null) {
				        	tempCopyTreeNode = (DefaultMutableTreeNode)
				                         (currentSelection.getLastPathComponent());
				        	pressedCtrlC = true;
				        	tempPasteTreeNode = null;
				        }
					}
					
					//ctrl + x 剪切树节点
					if(e.getKeyCode() == KeyEvent.VK_X) {
						TreePath currentSelection = tree.getSelectionPath();
				        if (currentSelection != null) {
				        	tempPasteTreeNode = (DefaultMutableTreeNode)
				                         (currentSelection.getLastPathComponent());
				        	pressedCtrlX = true;
				        	tempCopyTreeNode = null;
				        }
					}
					
					//ctrl + v 粘贴复制的树节点及子节点
					if(e.getKeyCode() == KeyEvent.VK_V) {
				        if (tree.getSelectionPath() != null) {
				        	DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
				        	if(targetNode.getLevel() <= testTreeNodeLevel) {
				        		if(tempCopyTreeNode != null && pressedCtrlC) {
				        			if(targetNode.getLevel() == tempCopyTreeNode.getLevel() - 1) {
				        				copyNodeWithChildren(targetNode, tempCopyTreeNode);
				        				nodeEditSaved = true;
				        				DynamicTreeDemo.printLogInJTextArea("复制成功!!!", null);
				        			}else {
				        				DynamicTreeDemo.printLogInJTextArea("复制节点需与目标节点同一级别!!!", Color.RED);
				        			}
					        	}else if(tempPasteTreeNode != null && pressedCtrlX) {
					        		if(targetNode.getLevel() == tempPasteTreeNode.getLevel() - 1) {
					        			treeModel.removeNodeFromParent(tempPasteTreeNode);
						        		copyNodeWithChildren(targetNode, tempPasteTreeNode);
						        		nodeEditSaved = true;
						        		DynamicTreeDemo.printLogInJTextArea("剪切成功!!!", null);
				        			}else {
				        				DynamicTreeDemo.printLogInJTextArea("复制节点需与目标节点同一级别!!!", Color.RED);
				        			}
					        	}
				        	}
				        	else {
				        		
				        	}
				        	//复制完节点清除复制缓存
			        		tempCopyTreeNode = null;
			        		tempPasteTreeNode = null;
			        		pressedCtrlC = false;
			        		pressedCtrlX = false;
				        	
				        }
					}
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
			}
		});
        
        JScrollPane scrollPane = new JScrollPane(tree);
        add(scrollPane);
    }
    
    public static void addInputTypeCases() {
    	 ArrayList<String> addUserObjects = new ArrayList<String>();
    	 TreePath currentSelection = tree.getSelectionPath();
         if (currentSelection != null) {
        	 DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
                     (currentSelection.getLastPathComponent());
        	 if(DynamicTreeDemo.inputTypeVal2.size() > 0) {
            	 for (String itv2 : DynamicTreeDemo.inputTypeVal2) {
    				addUserObjects.add(currentNode.getUserObject() + "字段长度" + itv2 +DynamicTreeDemo.inputTypeVal1 + "位");
    			}
             }
        	 if(DynamicTreeDemo.inputTypeVal3.size() > 0) {
        		 if(DynamicTreeDemo.inputTypeVal3.get(0).equals("必填") || DynamicTreeDemo.inputTypeVal3.get(0).equals("非必填")) {
        			 addUserObjects.add(currentNode.getUserObject() + "字段为空");
        			 addUserObjects.add(currentNode.getUserObject() + "字段不为空");
        		 }
             }
        	 if(DynamicTreeDemo.inputTypeVal4.size() > 0) {
        		String multi = "";
            	for (String itv4 : DynamicTreeDemo.inputTypeVal4) {
    				addUserObjects.add(currentNode.getUserObject() + "字段输入内容为" + itv4  + "字符");
    				multi += itv4 + "、";
    			}
            	multi = multi.substring(0, multi.length() - 1);
            	addUserObjects.add(currentNode.getUserObject() + "字段输入内容包含" + multi  + "字符");
             }
        	 if(DynamicTreeDemo.inputTypeVal5.size() > 0) {
        		String multi = "";
             	for (String itv5 : DynamicTreeDemo.inputTypeVal5) {
     				addUserObjects.add(currentNode.getUserObject() + "字段输入内容为" + itv5  + "字符");
     				multi += itv5 + "、";
     			}
             	multi = multi.substring(0, multi.length() - 1);
             	addUserObjects.add(currentNode.getUserObject() + "字段输入内容包含" + multi  + "字符");
             }
        	 for (String auo : addUserObjects) {
        		 currentNode.add(new DefaultMutableTreeNode(auo));
        	 }
        	 expandAllTreeNode(currentNode);
        	 tree.updateUI();
        	 DynamicTreeDemo.printLogInJTextArea("新增节点成功", Color.CYAN);
         } 
        
    }
    

    /** Remove all nodes except the root node. */
    public void clear() {
        rootNode.removeAllChildren();
        treeModel.reload();
    }

    /** Remove the currently selected node. */
    public void removeCurrentNode() {
        TreePath currentSelection = tree.getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
                         (currentSelection.getLastPathComponent());
            MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
            if (parent != null) {
                treeModel.removeNodeFromParent(currentNode);
                nodeEditSaved = true;
                return;
            }
        } 

        // Either there was no selection, or the root was selected.
        toolkit.beep();
    }
    
    /** edit node*/
    public void editCurrentNode() {
        TreePath currentSelection = tree.getSelectionPath();
        if (currentSelection != null) {
        	tree.startEditingAtPath(currentSelection);
        } 
    }

    /** Add child to the currently selected node. */
    public DefaultMutableTreeNode addObject(Object child) {
        DefaultMutableTreeNode parentNode = null;
        TreePath parentPath = tree.getSelectionPath();

        if (parentPath == null) {
            parentNode = rootNode;
        } else {
            parentNode = (DefaultMutableTreeNode)
                         (parentPath.getLastPathComponent());
        }
        nodeEditSaved = true;
        return addObject(parentNode, child, true);
    }
    
    public void copyNodeWithChildren(DefaultMutableTreeNode targetNode, DefaultMutableTreeNode copyNode) {
        DefaultMutableTreeNode childNode = 
                new DefaultMutableTreeNode(copyNode.getUserObject(), true);
    	treeModel.insertNodeInto(childNode, targetNode, targetNode.getChildCount());
        if(copyNode.getChildCount() > 0) {
        	for (int i = 0; i < copyNode.getChildCount(); i++) {
        		copyNodeWithChildren(childNode, (DefaultMutableTreeNode) copyNode.getChildAt(i));
    		}
        }else {
        	
        }
    }
    
    public static void expandAllTreeNode(DefaultMutableTreeNode treeNode) {
        TreePath path = new TreePath(treeNode.getPath());
    	if(tree.isCollapsed(path)) {
    		tree.expandPath(path);
    	}
    	if(treeNode.getChildCount() > 0) {
        	for (int i = 0; i < treeNode.getChildCount(); i++) {
        		expandAllTreeNode((DefaultMutableTreeNode) treeNode.getChildAt(i));
    		}
        }
    }
    
    public void collapseAllTreeNode(DefaultMutableTreeNode treeNode) {
        TreePath path = new TreePath(treeNode.getPath());
    	if(tree.isExpanded(path)) {
    		tree.collapsePath(path);
    	}
    	if(treeNode.getChildCount() > 0) {
        	for (int i = 0; i < treeNode.getChildCount(); i++) {
        		collapseAllTreeNode((DefaultMutableTreeNode) treeNode.getChildAt(i));
    		}
        }
    }
    
    public void expandOrCollapseAllTreeNode() {
    	TreePath currentSelection = tree.getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
                         (currentSelection.getLastPathComponent());
            TreePath path = new TreePath(currentNode.getPath());
            if(tree.isCollapsed(path)) {
            	expandAllTreeNode(currentNode);
            }else if(tree.isExpanded(path)) {
            	collapseAllTreeNode(currentNode);
            }
            nodeEditSaved = true;
        } 
    }
    
    public void getAllTreeExpandStatus(DefaultMutableTreeNode treeNode) {
    	TreePath path = new TreePath(treeNode.getPath());
		treeExpandStatus.put(path, tree.isExpanded(path));
    	if(treeNode.getChildCount() > 0) {
        	for (int i = 0; i < treeNode.getChildCount(); i++) {
        		getAllTreeExpandStatus((DefaultMutableTreeNode) treeNode.getChildAt(i));
    		}
        }
    }
    
    public void loadAllTreeExpandStatus() {
    	for (TreePath dt : treeExpandStatus.keySet()) {
			if(treeExpandStatus.get(dt)) {
				tree.expandPath(dt);
			}
		}
    }
    
    public static Boolean isTreeSelected(int level) {
    	boolean select = false;
    	if(!tree.isSelectionEmpty() && 
    			((DefaultMutableTreeNode)(tree.getSelectionPath().getLastPathComponent())).getLevel() == level) {
    		select = true;
    	}
    	return select;
    }
    
    
    public static void getExportTreeData() {
    	DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) ( tree.getSelectionPath().getLastPathComponent());
    	if(!tree.isSelectionEmpty() && currentNode.getLevel() == 1) {
    		exportDataString = "测试项目" + splitText + 
    							"模块" + splitText +
    							"子模块" + splitText +
    							"编号" + splitText +
    							"级别" + splitText +
    							"用例标题" + splitText +
    							"前提条件" + splitText +
    							"测试步骤" + splitText +
    							"预期结果" + splitText +
    							"实际结果" + splitText +
    							"备注" + splitText +
    							lineSplit;
    		String temp = "";
    		String level1 = currentNode.getUserObject().toString();
    		for (int i = 0; i < currentNode.getChildCount(); i++) {
    			DefaultMutableTreeNode level1ChildNode = (DefaultMutableTreeNode)currentNode.getChildAt(i);
				for (int j = 0; j < level1ChildNode.getChildCount(); j++) {
					DefaultMutableTreeNode level2ChildNode = (DefaultMutableTreeNode)level1ChildNode.getChildAt(j);
					for (int k = 0; k < level2ChildNode.getChildCount(); k++) {
						DefaultMutableTreeNode level3ChildNode = (DefaultMutableTreeNode)level2ChildNode.getChildAt(k);
						if(level3ChildNode.getChildCount() > 0) {
							for (int l = 0; l < level3ChildNode.getChildCount(); l++) {
								temp = 
										level1  + splitText +
										level1ChildNode.getUserObject() + splitText + 
										level2ChildNode.getUserObject() + splitText + 
										"ROW()-1" + splitText +
										"" + splitText + 
										level3ChildNode.getChildAt(l) + splitText + 
										"" + splitText + 
										level1 + ", " + level1ChildNode.getUserObject() + "-" + level2ChildNode.getUserObject() + "页面, "
												+level3ChildNode.getChildAt(l) + ", 点击查询按钮" + splitText + 
										"" + splitText +
										"" + splitText +
										"" + splitText +										
										lineSplit;
										
								DynamicTreeDemo.printLogInJTextArea(temp, Color.GREEN);
								exportDataString += temp;
							}
						}
						
					}
//					
				}
			}
    	}
    }
    
//    public static int getTreeLevel() {
//    	int level = 0;
//    	TreePath currentSelection = tree.getSelectionPath();
//        if (currentSelection != null) {
//            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
//                         (currentSelection.getLastPathComponent());
//            level = currentNode.getLevel();
//        }
//        return level;
//    }

    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                            Object child) {
        return addObject(parent, child, false);
    }

    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                            Object child, 
                                            boolean shouldBeVisible) {
        DefaultMutableTreeNode childNode = 
                new DefaultMutableTreeNode(child);

        if (parent == null) {
            parent = rootNode;
        }
	
        //It is key to invoke this on the TreeModel, and NOT DefaultMutableTreeNode
        treeModel.insertNodeInto(childNode, parent, 
                                 parent.getChildCount());

        //Make sure the user can see the lovely new node.
        if (shouldBeVisible) {
            tree.scrollPathToVisible(new TreePath(childNode.getPath()));
        }
        return childNode;
    }
    
    public void saveTheTree() {
 	   try {
 		   ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(JtreeSaveFile));
 		   JtreeSaveObjects.put("treeModel", treeModel);
 		   getAllTreeExpandStatus((DefaultMutableTreeNode) treeModel.getRoot());
 		   JtreeSaveObjects.put("treeExpandStatus", treeExpandStatus);
 		   
 		   out.writeObject(JtreeSaveObjects);//the actual tree object
 		   out.flush();
 		   out.close();
 		   DynamicTreeDemo.printLogInJTextArea("保存成功", Color.CYAN);
 		   nodeEditSaved = false;
 	   }
 	   catch(IOException e){
 		   DynamicTreeDemo.printLogInJTextArea(e.getMessage(), null);
// 		   e.printStackTrace();
 	   }
   }
    
    //读取保存的所有树节点数据,若不存在文件,直接新建树对象
    @SuppressWarnings("unchecked")
	public JTree loadTheTreeFromFile() {
    	ObjectInputStream in = null;
	    try {
	    	File save = new File(JtreeSaveFile);
	    	if(!save.exists()) {
	    		tree = new JTree(treeModel);
	    	}else {
		        in = new ObjectInputStream(new FileInputStream(JtreeSaveFile));
		        JtreeSaveObjects = (HashMap<String, Object>) in.readObject();
		        treeModel = (DefaultTreeModel) JtreeSaveObjects.get("treeModel");
		        treeExpandStatus =(LinkedHashMap<TreePath, Boolean>) JtreeSaveObjects.get("treeExpandStatus");
		        
		        tree = new JTree(treeModel);
		        loadAllTreeExpandStatus();

	    	}
	    }
	    catch(Exception e) {
	    	e.printStackTrace();
	    }
	    return tree;
    }

    class MyTreeModelListener implements TreeModelListener {
        public void treeNodesChanged(TreeModelEvent e) {
            DefaultMutableTreeNode node;
            node = (DefaultMutableTreeNode)(e.getTreePath().getLastPathComponent());

            /*
             * If the event lists children, then the changed
             * node is the child of the node we've already
             * gotten.  Otherwise, the changed node and the
             * specified node are the same.
             */
            if(e.getChildIndices() != null) {
	              int index = e.getChildIndices()[0];
	              node = (DefaultMutableTreeNode)(node.getChildAt(index));
	              DynamicTreeDemo.printLogInJTextArea("编辑完成", null);
//	              System.out.println("The user has finished editing the node.");
//	          	  System.out.println("New value: " + node.getUserObject());
            }else {
//            	System.out.println("--The user has finished editing the node.");
//            	System.out.println("New value: " + rootNode.getUserObject());
            }
            nodeEditSaved = true;

        }
        public void treeNodesInserted(TreeModelEvent e) {
        }
        public void treeNodesRemoved(TreeModelEvent e) {
        }
        public void treeStructureChanged(TreeModelEvent e) {
        }
    }
    
	class TreePopup extends JPopupMenu {
	   public TreePopup(JTree tree) {
		      JMenuItem delete = new JMenuItem("删除节点");
		      JMenuItem add = new JMenuItem("新增节点");
		      JMenuItem edit = new JMenuItem("编辑节点");
		      //包含子节点全部展开
		      JMenuItem expand = new JMenuItem("展开或折叠子项节点");
		     
		      JMenuItem debug = new JMenuItem("debug菜单");
		      
		      delete.addActionListener(new ActionListener() {
		         public void actionPerformed(ActionEvent ae) {
		        	 removeCurrentNode();
		         }
		      });
		      add.addActionListener(new ActionListener() {
		         public void actionPerformed(ActionEvent ae) {
		        	 addObject("新增节点");
		         }
		      });
		      
		      edit.addActionListener(new ActionListener() {
		         public void actionPerformed(ActionEvent ae) {
		        	 editCurrentNode();
		         }
		      });
		      expand.addActionListener(new ActionListener() {
		         public void actionPerformed(ActionEvent ae) {
		        	 expandOrCollapseAllTreeNode();
	             }
		      });
		      
		      
		      debug.addActionListener(new ActionListener() {
		         public void actionPerformed(ActionEvent ae) {
		        	 debugNode();
		         }
	    	  });
		     
		      add(add);
		      add(new JSeparator());
		      add(edit);
		      add(new JSeparator());
		      add(expand);
		      add(new JSeparator());
		      add(delete);
		      
		      add(new JSeparator());
		      add(debug);
		      
	   }
    }
    
    @SuppressWarnings("serial")
    class AddDataTreePopup extends JPopupMenu {
    	public AddDataTreePopup(JTree tree) {
//    		  JMenuItem delete = new JMenuItem("删除值类型");
//		      JMenuItem add = new JMenuItem("新增值类型");
//		      JMenuItem edit = new JMenuItem("编辑值类型");
//		      JMenuItem expand = new JMenuItem("展开子项节点");
		      JMenuItem debug = new JMenuItem("debug菜单");
//		      delete.addActionListener(new ActionListener() {
//		         public void actionPerformed(ActionEvent ae) {
//		        	 removeCurrentNode();
//		         }
//		      });
//		      add.addActionListener(new ActionListener() {
//		         public void actionPerformed(ActionEvent ae) {
//		        	 addObject("新增节点");
//		         }
//		      });
//		      
//		      edit.addActionListener(new ActionListener() {
//		         public void actionPerformed(ActionEvent ae) {
//		        	 editCurrentNode();
//		         }
//		      });
//		      expand.addActionListener(new ActionListener() {
//		         public void actionPerformed(ActionEvent ae) {
//		         }
//		      });

	    	 
	    	  debug.addActionListener(new ActionListener() {
		         public void actionPerformed(ActionEvent ae) {
		        	 debugNode();
		         }
	    	  });
//		      add(add);
//		      add(new JSeparator());
//		      add(edit);
//		      add(new JSeparator());
//		      add(expand);
//		      add(new JSeparator());
//		      add(delete);
		      add(new JSeparator());
		      add(debug);
		      
    	}
    }
    
    
    
    public void debugNode() {
    	TreePath currentSelection = tree.getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
                         (currentSelection.getLastPathComponent());
            
            String debugMessage = String.valueOf("12222222222222222222222222222222222222i11111111111111111111111111122222222222223333333333333333333333222");
            DynamicTreeDemo.printLogInJTextArea(debugMessage, null);
          //设置测试点颜色待完成
        } 
    }
}
