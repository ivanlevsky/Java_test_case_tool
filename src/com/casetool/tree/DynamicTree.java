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
    //���Ե����ڵ�㼶
    private static final int testTreeNodeLevel = 4;
    
    private static final String JtreeSaveFile = System.getProperty("user.dir")+"/tree1.sav";
    private static HashMap<String, Object> JtreeSaveObjects = new HashMap<String, Object>();
    private static LinkedHashMap<TreePath, Boolean> treeExpandStatus = new LinkedHashMap<TreePath, Boolean>();
    private static String lastSelectTreeNode = "";//����ر�ǰѡ������Ŀ״̬�������
    
    
    public static Boolean nodeEditSaved = false;//�Ķ���ʶ, ���ڸĶ�ʱΪtrue
    public static String exportDataString;
    public DynamicTree() {
        super(new GridLayout(1,0));
        
        rootNode = new DefaultMutableTreeNode("��Ŀ");
        treeModel = new DefaultTreeModel(rootNode);
        
        tree = loadTheTreeFromFile();
        treeModel.addTreeModelListener(new MyTreeModelListener());
        
//        tree = new JTree(treeModel);
        tree.setEditable(true);
        tree.setDragEnabled(true);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.setShowsRootHandles(true);
//        tree.setToggleClickCount(1); //����չ���ڵ�֮ǰ����굥����Ϊ1
        tree.setDragEnabled(true);
        tree.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        
//      tree.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
//		
        //���ڵ�༭, ֧��F2�����ε�������������ε��������(���μ��м��)�༭, �༭��س���������Ҽ�����༭��������λ�ñ���
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
						currentNode.setUserObject("�����ڵ�");
					}
					cell.stopCellEditing();
					nodeEditSaved = true;
				}
			}
		});

        
        final TreePopup treePopup = new TreePopup(tree);
        final AddDataTreePopup addDataTreePopup = new AddDataTreePopup(tree);
        tree.addMouseListener(new MouseAdapter() {
        	//����Ҽ�������, ��ʹ��mousePressed, ��mouseReleased��������
            public void mouseReleased(MouseEvent e) {
	               if(SwingUtilities.isRightMouseButton(e)){
	            	  //ʹ��stopCellEditing, �Ҽ�ѡ�����ڵ�ʱ���������ڵ�༭״̬, ��Ҫ����stopCellEditing���� �÷�����д�ᱣ�����ݵ��Ҽ���ѡ�е������ڵ�
	 	        	  JTree cellTree = (JTree) e.getSource();
	 	        	  cellTree.getCellEditor().stopCellEditing();
	 	        	  
	 		          int selRow = tree.getRowForLocation(e.getX(), e.getY());
	 		          TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
	                  tree.setSelectionPath(selPath); 
	                  //�����λ��Ϊ���ڵ�����ʱ�����������հ�������ʾ������
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
            //�����ק���ڵ㹦��, δ���mouseMoved��mouseDragged
         });

        
        tree.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				//delete�� �� ctrl + d ɾ���ڵ�
				if(e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_D) {
					TreePath currentSelection = tree.getSelectionPath();
					//���ڵ���ڱ༭״̬ʱ����ɾ���༭�ڵ�, ���򱨴�
			        if (currentSelection != null && !tree.isEditing()) {
			        	removeCurrentNode();
			        }
				}
				
				// contrl ��ϼ��¼�, ����ճ����...
				if(e.isControlDown()) {
					//ctrl + s ����ڵ�
					if(e.getKeyCode() == KeyEvent.VK_S) {
						saveTheTree();
					}
					
					//ctrl + n �½��ڵ�
					if(e.getKeyCode() == KeyEvent.VK_N) {
						int treeNodeLevel = ((DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent()).getLevel();
						//���Ե����Ͳ�֧�ֿ�ݼ�����, �����ұ��������
						if(treeNodeLevel < testTreeNodeLevel) {
							addObject("�����ڵ�");
						}
					}
					
					//ctrl + e չ���ڵ�
					if(e.getKeyCode() == KeyEvent.VK_E) {
						TreePath currentSelection = tree.getSelectionPath();
				        if (currentSelection != null) {
				        	expandOrCollapseAllTreeNode();
				        }
					}
					
					//ctrl + c �������ڵ�
					if(e.getKeyCode() == KeyEvent.VK_C) {
						TreePath currentSelection = tree.getSelectionPath();
				        if (currentSelection != null) {
				        	tempCopyTreeNode = (DefaultMutableTreeNode)
				                         (currentSelection.getLastPathComponent());
				        	pressedCtrlC = true;
				        	tempPasteTreeNode = null;
				        }
					}
					
					//ctrl + x �������ڵ�
					if(e.getKeyCode() == KeyEvent.VK_X) {
						TreePath currentSelection = tree.getSelectionPath();
				        if (currentSelection != null) {
				        	tempPasteTreeNode = (DefaultMutableTreeNode)
				                         (currentSelection.getLastPathComponent());
				        	pressedCtrlX = true;
				        	tempCopyTreeNode = null;
				        }
					}
					
					//ctrl + v ճ�����Ƶ����ڵ㼰�ӽڵ�
					if(e.getKeyCode() == KeyEvent.VK_V) {
				        if (tree.getSelectionPath() != null) {
				        	DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
				        	if(targetNode.getLevel() <= testTreeNodeLevel) {
				        		if(tempCopyTreeNode != null && pressedCtrlC) {
				        			if(targetNode.getLevel() == tempCopyTreeNode.getLevel() - 1) {
				        				copyNodeWithChildren(targetNode, tempCopyTreeNode);
				        				nodeEditSaved = true;
				        				DynamicTreeDemo.printLogInJTextArea("���Ƴɹ�!!!", null);
				        			}else {
				        				DynamicTreeDemo.printLogInJTextArea("���ƽڵ�����Ŀ��ڵ�ͬһ����!!!", Color.RED);
				        			}
					        	}else if(tempPasteTreeNode != null && pressedCtrlX) {
					        		if(targetNode.getLevel() == tempPasteTreeNode.getLevel() - 1) {
					        			treeModel.removeNodeFromParent(tempPasteTreeNode);
						        		copyNodeWithChildren(targetNode, tempPasteTreeNode);
						        		nodeEditSaved = true;
						        		DynamicTreeDemo.printLogInJTextArea("���гɹ�!!!", null);
				        			}else {
				        				DynamicTreeDemo.printLogInJTextArea("���ƽڵ�����Ŀ��ڵ�ͬһ����!!!", Color.RED);
				        			}
					        	}
				        	}
				        	else {
				        		
				        	}
				        	//������ڵ�������ƻ���
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
    				addUserObjects.add(currentNode.getUserObject() + "�ֶγ���" + itv2 +DynamicTreeDemo.inputTypeVal1 + "λ");
    			}
             }
        	 if(DynamicTreeDemo.inputTypeVal3.size() > 0) {
        		 if(DynamicTreeDemo.inputTypeVal3.get(0).equals("����") || DynamicTreeDemo.inputTypeVal3.get(0).equals("�Ǳ���")) {
        			 addUserObjects.add(currentNode.getUserObject() + "�ֶ�Ϊ��");
        			 addUserObjects.add(currentNode.getUserObject() + "�ֶβ�Ϊ��");
        		 }
             }
        	 if(DynamicTreeDemo.inputTypeVal4.size() > 0) {
        		String multi = "";
            	for (String itv4 : DynamicTreeDemo.inputTypeVal4) {
    				addUserObjects.add(currentNode.getUserObject() + "�ֶ���������Ϊ" + itv4  + "�ַ�");
    				multi += itv4 + "��";
    			}
            	multi = multi.substring(0, multi.length() - 1);
            	addUserObjects.add(currentNode.getUserObject() + "�ֶ��������ݰ���" + multi  + "�ַ�");
             }
        	 if(DynamicTreeDemo.inputTypeVal5.size() > 0) {
        		String multi = "";
             	for (String itv5 : DynamicTreeDemo.inputTypeVal5) {
     				addUserObjects.add(currentNode.getUserObject() + "�ֶ���������Ϊ" + itv5  + "�ַ�");
     				multi += itv5 + "��";
     			}
             	multi = multi.substring(0, multi.length() - 1);
             	addUserObjects.add(currentNode.getUserObject() + "�ֶ��������ݰ���" + multi  + "�ַ�");
             }
        	 for (String auo : addUserObjects) {
        		 currentNode.add(new DefaultMutableTreeNode(auo));
        	 }
        	 expandAllTreeNode(currentNode);
        	 tree.updateUI();
        	 DynamicTreeDemo.printLogInJTextArea("�����ڵ�ɹ�", Color.CYAN);
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
    		exportDataString = "������Ŀ" + splitText + 
    							"ģ��" + splitText +
    							"��ģ��" + splitText +
    							"���" + splitText +
    							"����" + splitText +
    							"��������" + splitText +
    							"ǰ������" + splitText +
    							"���Բ���" + splitText +
    							"Ԥ�ڽ��" + splitText +
    							"ʵ�ʽ��" + splitText +
    							"��ע" + splitText +
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
										level1 + ", " + level1ChildNode.getUserObject() + "-" + level2ChildNode.getUserObject() + "ҳ��, "
												+level3ChildNode.getChildAt(l) + ", �����ѯ��ť" + splitText + 
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
 		   DynamicTreeDemo.printLogInJTextArea("����ɹ�", Color.CYAN);
 		   nodeEditSaved = false;
 	   }
 	   catch(IOException e){
 		   DynamicTreeDemo.printLogInJTextArea(e.getMessage(), null);
// 		   e.printStackTrace();
 	   }
   }
    
    //��ȡ������������ڵ�����,���������ļ�,ֱ���½�������
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
	              DynamicTreeDemo.printLogInJTextArea("�༭���", null);
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
		      JMenuItem delete = new JMenuItem("ɾ���ڵ�");
		      JMenuItem add = new JMenuItem("�����ڵ�");
		      JMenuItem edit = new JMenuItem("�༭�ڵ�");
		      //�����ӽڵ�ȫ��չ��
		      JMenuItem expand = new JMenuItem("չ�����۵�����ڵ�");
		     
		      JMenuItem debug = new JMenuItem("debug�˵�");
		      
		      delete.addActionListener(new ActionListener() {
		         public void actionPerformed(ActionEvent ae) {
		        	 removeCurrentNode();
		         }
		      });
		      add.addActionListener(new ActionListener() {
		         public void actionPerformed(ActionEvent ae) {
		        	 addObject("�����ڵ�");
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
//    		  JMenuItem delete = new JMenuItem("ɾ��ֵ����");
//		      JMenuItem add = new JMenuItem("����ֵ����");
//		      JMenuItem edit = new JMenuItem("�༭ֵ����");
//		      JMenuItem expand = new JMenuItem("չ������ڵ�");
		      JMenuItem debug = new JMenuItem("debug�˵�");
//		      delete.addActionListener(new ActionListener() {
//		         public void actionPerformed(ActionEvent ae) {
//		        	 removeCurrentNode();
//		         }
//		      });
//		      add.addActionListener(new ActionListener() {
//		         public void actionPerformed(ActionEvent ae) {
//		        	 addObject("�����ڵ�");
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
          //���ò��Ե���ɫ�����
        } 
    }
}
