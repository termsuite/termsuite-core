/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright 2, 2013nership.  The ASF licenses this file
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

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;

public class ProcessingResultListener implements ListSelectionListener, ListDataListener, ChangeListener, TreeSelectionListener {
	
	private ProcessingResultViewer viewer;
	
	public void setViewer(ProcessingResultViewer viewer) {
		this.viewer = viewer;
	}
	
	private ProcessingResultViewer getViewer() {
		return this.viewer;
	}
		
	@SuppressWarnings("rawtypes")
    @Override
	public void valueChanged(ListSelectionEvent event) {
		if (!event.getValueIsAdjusting()) { 
			JList list = (JList) event.getSource();
			Object object = list.getSelectedValue();
			try {
				if (object instanceof ProcessingResult) {
					final ProcessingResult result = (ProcessingResult) object;
					this.doUpdate(result);
					
				} else if (object instanceof Type) {
					final Type type = (Type) object;
					this.doUpdate(type);
				} 
			} catch (Exception e) {
				// FIXME, what now ?
			}
		}
	}

	/*
	private void doUpdate(AnnotationFS annotation) throws Exception {
		this.getViewer().doUpdate(annotation);
	}
	*/

	private void doUpdate(Type type) throws Exception {
		this.getViewer().doUpdate(type);
	}
	
	private void doUpdate(ProcessingResult result) throws Exception {
		this.getViewer().doUpdate(result);
	}

	@Override
	public void contentsChanged(ListDataEvent event) {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("rawtypes")
    @Override
	public void intervalAdded(ListDataEvent event) {
		Object object = event.getSource();
		if (object instanceof DefaultListModel) {
			DefaultListModel model = (DefaultListModel) object;
			if (model.getSize() > 0) {
				this.getViewer().doEnable(true);
			}
		}
	}

	@SuppressWarnings("rawtypes")
    @Override
	public void intervalRemoved(ListDataEvent event) {
		Object object = event.getSource();
		if (object instanceof DefaultListModel) {
			DefaultListModel model = (DefaultListModel) object;
			if (model.getSize() <= 0) {
				this.getViewer().doEnable(false);
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent event) {
		JTabbedPane tab = (JTabbedPane) event.getSource();
		int index = tab.getSelectedIndex();
		if (index == 0) {

		} else if (index == 1) {
			
		} else if (index == 3) {
			
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent event) {
		JTree tree = (JTree) event.getSource();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (node != null) {
		    Object object = node.getUserObject();
		    if (object instanceof AnnotationFS) {
		    	AnnotationFS annotation = (AnnotationFS) object;
		    	try {
		    		this.getViewer().doUpdate(annotation);
		    	} catch (Exception e) { /* ignore */ }
		    }			
		}
	}
	
}
