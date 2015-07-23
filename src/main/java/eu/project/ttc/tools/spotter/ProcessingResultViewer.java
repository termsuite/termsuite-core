/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright 2, 2015nership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package eu.project.ttc.tools.spotter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import eu.project.ttc.types.TermOccAnnotation;
import eu.project.ttc.types.WordAnnotation;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ProcessingResultViewer {
	
    private final JFileChooser fileChooser = new JFileChooser();
    
    private final TypeSystemDescription typeSystemDesc = TypeSystemDescriptionFactory.createTypeSystemDescription("eu.project.ttc.types.TermSuiteTypeSystem");
    
    private static final FilenameFilter XMI_FILTER = new FilenameFilter() {
        
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".xmi");
        }
    };
    
	private static final float BRIGHT = 0.95f;
	
	private static final Color[] COLORS = new Color[] {
	      // low saturation colors are best, so put them first
	      Color.getHSBColor(80f / 360, 0.25f, BRIGHT), // celery green?
	      Color.getHSBColor(330f / 360, 0.25f, BRIGHT), // light coral?
	      Color.getHSBColor(160f / 360, 0.25f, BRIGHT), // aqua?
	      Color.getHSBColor(250f / 360, 0.25f, BRIGHT), // light violet?
	      Color.getHSBColor(210f / 360, 0.25f, BRIGHT), // baby blue?
	      Color.getHSBColor(120f / 360, 0.25f, BRIGHT), // mint green?
	      Color.getHSBColor(290f / 360, 0.25f, BRIGHT), // lavender?
	      Color.getHSBColor(30f / 360, 0.25f, BRIGHT), // tangerine?
	      Color.getHSBColor(0f / 360, 0.25f, BRIGHT), // pink?
	      Color.getHSBColor(55f / 360, 0.25f, BRIGHT), // butter yellow?
	      // higher saturation colors
	      Color.getHSBColor(55f / 360, 0.5f, BRIGHT), 
	      Color.getHSBColor(0f / 360, 0.5f, BRIGHT),
	      Color.getHSBColor(210f / 360, 0.5f, BRIGHT), 
	      Color.getHSBColor(120f / 360, 0.5f, BRIGHT),
	      Color.getHSBColor(290f / 360, 0.5f, BRIGHT),
	      Color.getHSBColor(30f / 360, 0.5f, BRIGHT),
	      Color.getHSBColor(80f / 360, 0.5f, BRIGHT),
	      Color.getHSBColor(330f / 360, 0.5f, BRIGHT),
	      Color.getHSBColor(160f / 360, 0.5f, BRIGHT),
	      Color.getHSBColor(250f / 360, 0.5f, BRIGHT),
	      // even higher saturation colors
	      Color.getHSBColor(55f / 360, 0.75f, BRIGHT), 
	      Color.getHSBColor(0f / 360, 0.75f, BRIGHT),
	      Color.getHSBColor(210f / 360, 0.75f, BRIGHT), 
	      Color.getHSBColor(120f / 360, 0.75f, BRIGHT),
	      Color.getHSBColor(290f / 360, 0.75f, BRIGHT), 
	      Color.getHSBColor(30f / 360, 0.75f, BRIGHT),
	      Color.getHSBColor(80f / 360, 0.75f, BRIGHT), 
	      Color.getHSBColor(330f / 360, 0.75f, BRIGHT),
	      Color.getHSBColor(160f / 360, 0.75f, BRIGHT), 
	      Color.getHSBColor(250f / 360, 0.75f, BRIGHT) 
	};
	
	private Map<String,Color> typeColors;
	
	private void setTypeColors() {
		this.typeColors = new HashMap<String, Color>();
	}
	
	private Map<String,Color> getTypeColors() {
		return this.typeColors;
	}
	
	private TypeSystem typeSystem;
	
	private void setTypeSystem() {
		this.typeSystem = new TypeSystemImpl();
	}
	
	private TypeSystem getTypeSystem() {
		return this.typeSystem;
	}
	
	private Map<String,JCheckBox> typeSelection;
	
	private void setTypeSelection() {
		this.typeSelection = new HashMap<String,JCheckBox>();
	}
	
	private Map<String,JCheckBox> getTypeSelection() {
		return this.typeSelection;
	}
	
	
    private DefaultListModel typeModel;
	
	private void setTypeModel() {
		this.typeModel = new DefaultListModel();
	}
	
	public DefaultListModel getTypeModel() {
		return this.typeModel;
	}
		
	private JList typeList;
	
	
    private void setTypeList() {
		this.typeList = new JList();
		this.typeList.setModel(this.getTypeModel());
		this.typeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.typeList.setMinimumSize(new Dimension(225,500));
		this.typeList.setCellRenderer(new TypeRenderer());
	}
	
	public JList getTypeList() {
		return this.typeList;
	}
	
	private JScrollPane typeScroll;
	
	private void setTypeScroll() {
		this.typeScroll = new JScrollPane();
		this.typeScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.typeScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.typeScroll.getViewport().add(this.getTypeList());
	}
	
	private JScrollPane getTypeScroll() {
		return this.typeScroll;
	}
	
	private DefaultMutableTreeNode indexRoot;
	
	private void setIndexRoot() {
		this.indexRoot = new DefaultMutableTreeNode();
	}
	
	private DefaultMutableTreeNode getIndexRoot() {
		return this.indexRoot;
	}
	
	private DefaultTreeModel indexModel;
	
	private void setIndexModel() {
		this.indexModel = new DefaultTreeModel(this.getIndexRoot());
	}
	
	public DefaultTreeModel getIndexModel() {
		return this.indexModel;
	}
		
	private JTree indexTree;
	
	private void setIndexTree() {
		this.indexTree = new JTree(this.getIndexModel());
		this.indexTree.setMinimumSize(new Dimension(225,500));
		this.indexTree.setRootVisible(false);
		this.indexTree.setCellRenderer(new AnnotationRenderer());
		this.indexTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	}
	
	public JTree getIndexTree() {
		return this.indexTree;
	}
	
	private JScrollPane indexScroll;
	
	private void setIndexScroll() {
		this.indexScroll = new JScrollPane();
		this.indexScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.indexScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.indexScroll.getViewport().add(this.getIndexTree());
	}
	
	private JScrollPane getIndexScroll() {
		return this.indexScroll;
	}
	
	private DefaultListModel resultModel;
	
	private void setResultModel() {
		this.resultModel = new DefaultListModel();
	}
	
	public DefaultListModel getResultModel() {
		return this.resultModel;
	}
		
	private JList resultList;
	
	private void setResultList() {
		this.resultList = new JList();
		this.resultList.setModel(this.getResultModel());
		this.resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.resultList.setMinimumSize(new Dimension(225,500));
	}
	
	public JList getResultList() {
		return this.resultList;
	}
	
	private JScrollPane resultScroll;
	
	private void setResultScroll() {
		this.resultScroll = new JScrollPane();
		this.resultScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.resultScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.resultScroll.getViewport().add(this.getResultList());
	}
	
	private JScrollPane getResultScroll() {
		return this.resultScroll;
	}
	
	private JTabbedPane tabs;
	
	@SuppressWarnings("serial")
    private void setTabs() {
	    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		this.tabs = new JTabbedPane();
		this.tabs.addTab("Results", this.getResultScroll());
		this.tabs.addTab(" Types ", this.getTypeScroll());
		this.tabs.addTab(" Index ", this.getIndexScroll());
        JPopupMenu menu = new JPopupMenu("Load");
        menu.add(new AbstractAction("Load results from directory...") {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(tabs)) {
                    resultModel.clear();
                    File dir = fileChooser.getSelectedFile();
                    File[] listed = dir.listFiles(XMI_FILTER);
                    for (File xmi : listed) {
                        try {
                            ProcessingResult res = new ExtendedProcessingResult(xmi, typeSystemDesc);
                            resultModel.addElement(res);
                        } catch (Exception e2) {
                            // TODO: handle exception
                            e2.printStackTrace();
                        }
                    }
                }
            }
        });
        tabs.setComponentPopupMenu(menu);
        tabs.setInheritsPopupMenu(true);
	}
	
	JTabbedPane getTabs() {
		return this.tabs;
	}
	
	private AnnotationViewer viewer;
	
	public AnnotationViewer getViewer() {
		return this.viewer;
	}
	
	public ProcessingResultViewer(AnnotationViewer viewer) {
        //super(JSplitPane.HORIZONTAL_SPLIT);
        //setBorder(BorderFactory.createEmptyBorder());

		this.setTypeSystem();
		this.setTypeColors();
		this.setTypeSelection();
		this.setTypeModel();
		this.setTypeList();
		this.setTypeScroll();
		this.setIndexRoot();
		this.setIndexModel();
		this.setIndexTree();
		this.setIndexScroll();
		this.setResultModel();
		this.setResultList();
		this.setResultScroll();
		this.setTabs();
		//this.setViewer();
        this.viewer = viewer;

        // Add components together
        //setResizeWeight(0.3);
        //setLeftComponent(this.getTabs());
        //setRightComponent(this.getViewer().getComponent());

		this.doEnable(false);
	}
	
	private CAS cas;
	
	private void setCas(CAS cas) {
		this.cas = cas;
	}
	
	private CAS getCas() {
		return this.cas;
	}

	public void doUpdate(ProcessingResult result) throws Exception {
		this.setCas(result.getCas());
		this.doUpdate();
	}
	
	/*
	public void doUpdate(CAS cas) throws Exception {
		this.setCas(cas);
		this.doUpdate();
	}
	*/
	
	private void doClear() throws Exception {
		this.getTypeColors().clear();
		this.getTypeSelection().clear();
		this.getTypeModel().clear();
		this.getViewer().doClear();
	}
	
	public void doUpdate() throws Exception {
		this.doClear();
		if (this.getCas() != null) {
			this.getViewer().doUpdate(this.getCas().getDocumentText());
			this.getViewer().doGo(0);
			
			// list of type to display (if not empty)
			ArrayList<String> type_list = new ArrayList<String>();
			type_list.add(TermOccAnnotation.class.getName());
			type_list.add(WordAnnotation.class.getName());
			
			for(String type_name : type_list){
				Type type = this.getCas().getTypeSystem().getType(type_name);
				if (this.getCas().getAnnotationIndex(type).size() > 0) {
					
					JCheckBox box = new JCheckBox(type.getShortName());
					this.getTypeSelection().put(type.getName(),box);
					Color color = COLORS[this.getTypeColors().size()];
					this.getTypeColors().put(type.getName(),color);
					this.getTypeModel().addElement(type);
				}
			}
		}
	}
	
	public void doUpdate(AnnotationFS annotation) throws Exception {
		int begin = annotation.getBegin();
		int end = annotation.getEnd();
		this.getViewer().doShow(begin, end);
		this.getViewer().doGo(begin);
	}
	
	public void doUpdate(Type type) throws Exception {
		JCheckBox box = this.getTypeSelection().get(type.getName());
		box.setSelected(!box.isSelected());
		this.display();
	}

	public void display() throws Exception {
		this.getViewer().doClear();
		this.getIndexRoot().removeAllChildren();
		for (String typeName : this.getTypeSelection().keySet()) {
			JCheckBox box = this.getTypeSelection().get(typeName);
			if (box.isSelected()) {
				Type type = this.getCas().getTypeSystem().getType(typeName);
				AnnotationIndex<AnnotationFS> index = this.getCas().getAnnotationIndex(type);
				FSIterator<AnnotationFS> iterator = index.iterator();
				while (iterator.hasNext()) {
					AnnotationFS annotation = iterator.next();
					this.getIndexRoot().add(this.getNode(annotation));
					int begin = annotation.getBegin();
					int end = annotation.getEnd();
					Color color = this.getTypeColors().get(type.getName());
					this.getViewer().doSelect(begin,end,color);
				}
			}
		}
		this.getIndexModel().reload();
		this.getViewer().doGo(0);
	}
	
	private DefaultMutableTreeNode getNode(AnnotationFS annotation) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode();
		node.setUserObject(annotation);
		for (Feature feature : annotation.getType().getFeatures()) {
			String name = feature.getShortName();
			if (!name.equals("sofa")) {
				try {
					Type range = feature.getRange();
					if (range.isPrimitive()) {
						String value = annotation.getFeatureValueAsString(feature);
						if (value != null) {
							DefaultMutableTreeNode note = new DefaultMutableTreeNode();
							note.setUserObject(name + ": " + value);
							node.add(note);
						}
					}
				} catch (CASRuntimeException e) {
					// e.printStackTrace();
				}
			}
		}
		return node;
	}
	
	public void enableListeners(ProcessingResultListener listener) {
		this.getResultModel().addListDataListener(listener);
		this.getResultList().addListSelectionListener(listener);
		this.getTypeList().addListSelectionListener(listener);
		this.getTabs().addChangeListener(listener);
		this.getIndexTree().addTreeSelectionListener(listener);
	}
	
	public void doEnable(boolean enabled) {
		this.getIndexTree().setEnabled(enabled);
		this.getIndexScroll().setEnabled(enabled);
		this.getTypeList().setEnabled(enabled);
		this.getTypeScroll().setEnabled(enabled);
		this.getResultList().setEnabled(enabled);
		this.getResultScroll().setEnabled(enabled);
		this.getViewer().doEnable(enabled);
		//setEnabled(enabled);
	}
	
	private class TypeRenderer implements ListCellRenderer {
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value,	int index, boolean isSelected, boolean hasCellFocus) {
			if (value instanceof Type) {
				Type type = (Type) value;
				JCheckBox box = getTypeSelection().get(type.getName());
				return box;
			} else {
				assert false;
				return null;
			}
		}
		
	}
	
	private class AnnotationRenderer implements TreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object object, boolean arg2, boolean arg3, boolean arg4, int arg5, boolean arg6) {
			JLabel label = new JLabel();
			label.setBorder(BorderFactory.createEmptyBorder(2, 1, 2, 1));
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) object;
			Object value = node.getUserObject();
			if (value == null) {
				label.setText("");
			} else if (value instanceof AnnotationFS) {
				AnnotationFS annotation = (AnnotationFS) value;
				label.setText(annotation.getCoveredText());
			} else {
				label.setText(value.toString());
			}
			return label;
		}
		
	}
	
}
