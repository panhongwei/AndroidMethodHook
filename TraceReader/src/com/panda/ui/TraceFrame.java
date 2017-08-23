package com.panda.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TooManyListenersException;

import javax.swing.BorderFactory;
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
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.panda.trace.BytesHelper;
import com.panda.trace.MethodLog;
import com.panda.trace.Threads;
import com.panda.trace.Trace;
import com.panda.trace.TraceThread;

public class TraceFrame extends JFrame{

	/**
	 * panda
	 */
	private JMenuBar jmb;
	private JMenu menu1, menu2, menu3, menu4, menu5;
	private JMenuItem it1,it2;
	private DropTarget dropTarget;
	private JPanel jp0,jp1;
	private JSplitPane jspMain;
	JTextField searchField; 
	private static final long serialVersionUID = 1L;
	Threads traceThreads;
//	Trace trace;
	JList threadList;
	ThreadJListModel model;
	JTree methodsTree,mTree;
	MethodNode root,mRoot;
	ListTreeModel treeModel,mModel;
	JTable mtable;
	JTabbedPane tabbedPane;
	JComboBox jcb;
	static String mtReg="";
	static int times;
	public TraceFrame() throws TooManyListenersException{
		this.setTitle("Trace文件分析工具 by panda！！！");
		Toolkit tk = this.getToolkit();
		Dimension dm = tk.getScreenSize();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		setLocation((int) (dm.getWidth() - 800) / 2,  
                (int) (dm.getHeight() - 500) / 2);
		this.setSize(800, 500);
		initJMenu();
		initArea();
		//实现拖拽打开
		dropTarget = new DropTarget();
		dropTarget.setComponent(this);
		dropTarget.addDropTargetListener(new DropTargetAdapter(){
			@Override
			public void drop(DropTargetDropEvent dtde) {
				// TODO Auto-generated method stub
				try
    	        {
    		        Transferable tf=dtde.getTransferable();
    		        if(tf.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
    		        {
    			        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
    			        List lt=(List)tf.getTransferData(DataFlavor.javaFileListFlavor);
    			        Iterator itor=lt.iterator();
    			        while(itor.hasNext())
    			        {
    				        File fl=(File)itor.next();
    				        if(fl.getName().endsWith(".trace")){
    							try {
    								TraceThread.topMethod.getChild().clear();
    								byte[] bytes=BytesHelper.toByteArray(fl.getPath());
    								Trace trace=new Trace(bytes);
    								traceThreads=new Threads(trace);
    								//threadList.
    								threadList.updateUI();
    								
    							} catch (Exception e1) {
    								// TODO Auto-generated catch block
    								e1.printStackTrace();
    								JOptionPane.showMessageDialog(null, "解析文件出错！", "提示", JOptionPane.OK_OPTION); 
    							}
    						}else{
    							JOptionPane.showMessageDialog(null, "选中非trace文件！", "提示", JOptionPane.OK_OPTION); 
    						}
//    				        System.out.println(f.getAbsolutePath());
    			        }
    			        dtde.dropComplete(true);
    		         }
    		         else
    		         {
    			        dtde.rejectDrop();
    		         }
    	         }
    	         catch(Exception e)
    	         {
    		         e.printStackTrace();
    	         }
				
			}});
    	this.setVisible(true);
	}
	public void initJMenu(){
		jmb=new JMenuBar();
		menu1 = new JMenu("文   件(F)");  
        menu1.setMnemonic('f'); //助记符  
        it1=new JMenuItem("打开");
        it2=new JMenuItem("新建窗口");
        menu1.add(it1);
        menu1.add(it2);
        it1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub  
				File fl=chooseFile();
				if(fl==null){
					return;
				}
				if(fl.getName().endsWith(".trace")){
					try {
						TraceThread.topMethod.getChild().clear();
						byte[] bytes=BytesHelper.toByteArray(fl.getPath());
						Trace trace=new Trace(bytes);
						traceThreads=new Threads(trace);
						//threadList.
						threadList.updateUI();
						System.out.println(TraceThread.topMethod.getChild().size());
						
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						JOptionPane.showMessageDialog(null, "解析文件出错！", "提示", JOptionPane.OK_OPTION); 
					}
				}else{
					JOptionPane.showMessageDialog(null, "选中非trace文件！", "提示", JOptionPane.OK_OPTION); 
				}
			}
		});
        it2.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {
					//new TraceFrame();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
        	
        });
       // it1.
//        menu1.
        menu2 = new JMenu("编辑");  
        menu2.setMnemonic('E');  
        menu3 = new JMenu("格式");  
        menu4 = new JMenu("查看");  
        menu5 = new JMenu("帮助"); 
//        jb2 = new JButton("打开"); 
        jmb.add(menu1);  
        jmb.add(menu2);  
        jmb.add(menu3);  
        jmb.add(menu4);  
        jmb.add(menu5);
        this.setJMenuBar(jmb);
	}
	public void initArea(){
		jp0=new JPanel();
		jp1=new JPanel();
		BorderLayout border=new BorderLayout();
		String str1[] = {"线程列表", "方法列表"}; 
		jcb = new JComboBox(str1);
		jp1.setLayout(border);
		jp1.add(jcb,BorderLayout.NORTH);
		jcb.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				System.out.println(jcb.getSelectedItem().toString());
				if(jcb.getSelectedItem().toString().equals("线程列表")){
					// List<Map.Entry<String,TraceThread>> list = new ArrayList<Map.Entry<String,TraceThread>>(traceThreads.getThreads().entrySet());
					 Collections.sort(traceThreads.names,new Comparator<String>() {
							@Override
							public int compare(String o1, String o2) {
								// TODO Auto-generated method stub
								return o1.compareTo(o2);
							}
				            
				      });
				     if(threadList!=null){
				    	 threadList.updateUI();
				    	// model.
				     }
				}else if(jcb.getSelectedItem().toString().equals("方法列表")){
					//List<Map.Entry<String,TraceThread>> list = new ArrayList<Map.Entry<String,TraceThread>>(traceThreads.getThreads().entrySet());
				     Collections.sort(traceThreads.names,new Comparator<String>() {
							@Override
							public int compare(String o1, String o2) {
								// TODO Auto-generated method stub
								return traceThreads.getThreads().get(o2).getMethods().size()-traceThreads.getThreads().get(o1).getMethods().size();
							}
				            
				      });
				     if(threadList!=null){
				    	 threadList.updateUI();
				    	// model.
				     }
				}
			}
		});
		
		BorderLayout border0=new BorderLayout();
		jp0.setLayout(border0);
		
		
		jspMain=new JSplitPane();
		jspMain.setDividerLocation(200);
		threadList=new JList();
		model=new ThreadJListModel(this);
		threadList.setModel(model);
		threadList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				if(e.getClickCount()==2){
					root.removeAllChildren();
					String name=traceThreads.names.get(threadList.getSelectedIndex());
					MethodLog top=TraceThread.topMethod;
					MethodNode node=new MethodNode(top.getFullName());
					treeModel.insertNodeInto(node, root, 0);
//					addNode(node,top);
					int i=0;
					for(MethodLog m:top.getChild()){
						if(m.getRecord().getThreadId()==Integer.parseInt(name)){
							MethodNode md=new MethodNode(m.getFullName()+"("+getChildNum(m)+")");
							//System.out.println(m.getFullName()+" "+top.getChild().size()+" "+i);
							treeModel.insertNodeInto(md, node, i++);
							addNode(md,m);
						}
					}
					treeModel.reload();
					
					TraceThread thread=traceThreads.getThreads().get(name);
					List<MethodLog> methods=thread.getMethods();
					mModel.setMethods(methods);
					reloadMMode(methods,"");
//					System.out.println(methods.size());
//					int n=0;
					
					//root.add(node);
					//methodsTree.updateUI();
				}
			}
		});
		JScrollPane jScrollPane = new JScrollPane();
		jScrollPane.setViewportView(threadList);
		jp1.add(jScrollPane, BorderLayout.CENTER);
		jspMain.setLeftComponent(jp1);
		
		root = new MethodNode("<Method call>"); 
		root.setAllowsChildren(true);
		//root.se
	    treeModel=new ListTreeModel(root);
		methodsTree=new JTree(treeModel);
		methodsTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				if(e.getClickCount()==2){
					System.out.println();
				}
			}
		});
		//methodsTree.setEditable(true);  
		methodsTree.setCellRenderer(new MethodRenderer());
		methodsTree.setBorder(BorderFactory.createTitledBorder("调用关系:"));
		JScrollPane jScrollPane1 = new JScrollPane();
		jScrollPane1.setViewportView(methodsTree);
		
		mRoot = new MethodNode("<Methods>"); 
		mRoot.setAllowsChildren(true);
	    mModel=new ListTreeModel(mRoot);
		mTree=new JTree(mModel);
		mTree.setCellRenderer(new MethodRenderer());
		mTree.setBorder(BorderFactory.createTitledBorder("方法列表:"));
		mTree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e)  {
				// TODO Auto-generated method stub
				if(e.getKeyCode()==KeyEvent.VK_ENTER){
					MethodNode note = (MethodNode) mTree.getLastSelectedPathComponent();
					TreePath p=mTree.getSelectionPath();
//					System.out.println(note.getChildCount());
					if(note.getText().equals("===parent===")||note.getText().equals("===child===")){
						return;
					}
					selectMMode(mModel.getMethods(),note.getText());
//					note.setSelected(true);
					mModel.reload();
					mTree.addSelectionPath(p);;
//					note.setSelected(false);
				}
			}
		});
		mTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				if(e.getClickCount()==2){
					MethodNode note = (MethodNode) mTree.getLastSelectedPathComponent();
					TreePath p=mTree.getSelectionPath();
					//System.out.println(note.getChildCount());
					if(note.getChildCount()==0){
						if(note.getText().equals("===parent===")||note.getText().equals("===child===")){
							return;
						}
						selectMMode(mModel.getMethods(),note.getText());
						mModel.reload();
						mTree.addSelectionPath(p);;
					}
				}
			}
		});
		
		
		mtable=new JTable(new MethodTableModel(this));
		
		tabbedPane=new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("树形结构", jScrollPane1);
		tabbedPane.addTab("方法列表", new JScrollPane(mTree));
		tabbedPane.addTab("方法集合", new JScrollPane(mtable));
		jspMain.setRightComponent(tabbedPane);
		
		
		JPanel jp3=new JPanel();
		jp3.setBorder(BorderFactory.createTitledBorder("查  找"));
		BorderLayout border3=new BorderLayout();
		jp3.setLayout(border3);
		searchField=new JTextField();
		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e)  {
				if(e.getKeyCode()==KeyEvent.VK_ENTER){
					//System.out.println(searchField.getText());
					String regString=searchField.getText();
					if(tabbedPane.getSelectedIndex()==0){
						if(regString=="")
							return;
						extendTreeMode(root,regString);
						return;
//						methodsTree
//						root
					}
					if(tabbedPane.getSelectedIndex()==1){
						reloadMMode(mModel.getMethods(),regString);
						mModel.reload();
						return;
					}
				}
			}
			
		});
		jp3.add(searchField,border3.CENTER);
		
		jp0.add(jspMain,border0.CENTER);
		jp0.add(jp3, border0.SOUTH);
		this.getContentPane().add(jp0);
		//jp1
	}
	private void extendTreeMode(MethodNode root,String reg){
		 if(mtReg.equals(reg)){
			 times++;
		 }else{
			 mtReg=reg;
			 times=1;
		 }
		 int n=times;
		 Enumeration<MethodNode> enums=root.preorderEnumeration();
		 while(enums.hasMoreElements()&&n>0){
			 MethodNode node=(MethodNode) enums.nextElement();
			 if(node.getText().contains(reg)){
//				 node.setNeedTab(true);
				 //System.out.println(node.getText());
				 methodsTree.addSelectionPath(new TreePath(node.getPath()));
				 n--;
			 }
		 }
	}
	private void selectMMode(List<MethodLog> methods,String reg){
		 Enumeration<MethodNode> enums=mRoot.children();
		 while(enums.hasMoreElements()){
			 MethodNode node=(MethodNode) enums.nextElement();
			 if(node.getText().contains(reg)){
				 node.setNeedTab(true);
				 //System.out.println(node.getText());
			 }else{
				 node.setNeedTab(false);
			 }
		 }
	}
	private void reloadMMode(List<MethodLog> methods,String reg){
		int i=0;
		int n=0;
		if(methods==null){
			return;
		}
		mRoot.removeAllChildren();
		for(i=0;i< methods.size();++i){
			MethodLog m=methods.get(i);
			if(!m.getPartner().equals(TraceThread.noPart)&&m.getAction()==1){
				continue;
			}
			if(!m.getFullName().contains(reg)){
				continue;
			}
			MethodNode mm=new MethodNode(n+"\t"+m.getFullName());
			mModel.insertNodeInto(mm,mRoot,n++);
			MethodNode parent=new MethodNode("===parent===");
			MethodNode child=new MethodNode("===child===");
			mModel.insertNodeInto(parent, mm, 0);
			mModel.insertNodeInto(child, mm, 1);
			for(int j=0;j<m.getChild().size();++j){
				MethodNode mj=new MethodNode(m.getChild().get(j).getFullName());
				mModel.insertNodeInto(mj, child, j);
			}
			MethodNode mp=new MethodNode(m.getParent().getFullName());
			mModel.insertNodeInto(mp, parent, 0);
		}
		mModel.reload();
	}
	public File chooseFile(){
		JFileChooser jfc=new JFileChooser();  
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY );  
        jfc.showDialog(new JLabel(), "选择");  
        File file=jfc.getSelectedFile();
        return file;
	}
	public void addNode(MethodNode node,MethodLog log){
		int i=0;
		for(MethodLog m1:log.getChild()){
			MethodNode node1=new MethodNode(m1.getFullName()+"("+getChildNum(m1)+")");
			treeModel.insertNodeInto(node1, node, i++);
			if(m1.getChild().size()==0){
				//System.out.println(m1.getFullName()+">>>>no child!");
				continue;
			}else{
				addNode(node1,m1);
			}
		}
	}
	public static int getChildNum(MethodLog log){
		int sum=log.getChild().size();
		if(sum==0){
			return sum;
		}
		for(MethodLog m1:log.getChild()){
			sum=sum+getChildNum(m1);
		}
		return sum;
	}
	
}
