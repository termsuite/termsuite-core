/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright 2, 2014nership.  The ASF licenses this file
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

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Fabien Poulard <fpoulard@dictanova.com>
 * @date 10/07/13
 */
@SuppressWarnings({"serial", "unchecked"})
public class ResultsPanel extends JPanel {

    private JPanel viewerGUI;
    private ProcessingResultListener listener;
    private ProcessingResultViewer viewer;
    private JPanel toolbarGUI;
    private AnnotationViewer annotationViewer;

    public ResultsPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        // Prepare components
        createToolbar();
        createViewer();

        // Assemble the GUI
        add(toolbarGUI, BorderLayout.NORTH);
        add(Box.createRigidArea(new Dimension(0,5)));
        add(viewerGUI, BorderLayout.SOUTH);
    }

    private void createViewer() {
        annotationViewer = new AnnotationViewer();

        this.viewer = new ProcessingResultViewer(annotationViewer);
        this.listener = new ProcessingResultListener();
        listener.setViewer(viewer);
        viewer.enableListeners(listener);

//        viewerGUI = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
//        viewerGUI.setBorder(BorderFactory.createLineBorder(Color.BLUE));
//        viewerGUI.setResizeWeight(0.3);
//        viewerGUI.setTopComponent(viewer.getTabs());
//        viewerGUI.setBottomComponent(viewer.getViewer().getComponent());
//        viewerGUI.setPreferredSize(getPreferredSize());
        viewerGUI = new JPanel();
        viewerGUI.setLayout(new BoxLayout(viewerGUI, BoxLayout.LINE_AXIS));
        viewerGUI.add(viewer.getTabs());
        viewerGUI.add(Box.createHorizontalGlue());
        viewerGUI.add(viewer.getViewer().getComponent());
    }

    private void createToolbar() {
        toolbarGUI = new JPanel();
        toolbarGUI.setLayout(new BoxLayout(toolbarGUI, BoxLayout.LINE_AXIS));
        toolbarGUI.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lbl = new JLabel("<html>You can visualise the results of the processing below.<br/>\n" +
                "You can also load old processed files: Locate the mouse above the Results panel; Press the right mouse button, a load menu will appear. Choose a directory that contains xmi files.</html>");
        toolbarGUI.add(lbl);
    }

    public void addResult(ProcessingResult result) {
        viewer.getResultModel().addElement(result);
    }

    public DefaultListModel getResultModel() {
        return viewer.getResultModel();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        viewer.doEnable(enabled);
    }
}
