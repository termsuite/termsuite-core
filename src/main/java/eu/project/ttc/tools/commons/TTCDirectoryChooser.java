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
package eu.project.ttc.tools.commons;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * This class implements an easy way to choose a directory from
 * the file system, or simply write its path.
 *
 * @author Fabien Poulard <fpoulard@dictanova.com>
 * @date 15/08/13
 */
public class TTCDirectoryChooser extends JPanel {

    private static final long serialVersionUID = -3577299442081494196L;

    private static final JFileChooser jfc;
    static {
        jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }

    private final JTextField tfPath;
    private final JButton btBrowse;
    private final String jfcTitle;
    private final Color defaultBgColor;

    public TTCDirectoryChooser(String title) {
        super();
        setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

        jfcTitle = title;

        // Field to display path
        tfPath = new JTextField(25);
        tfPath.setEnabled(false);
        defaultBgColor = tfPath.getBackground();
        tfPath.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                if ( new File(tfPath.getText()).exists() ) {
                    firePropertyChange("path", null, tfPath.getText());
                    tfPath.setBackground(defaultBgColor);
                    tfPath.setOpaque(false);
                } else {
                    // FIXME does not work!!!
                    tfPath.setBackground(Color.RED);
                    tfPath.setOpaque(true);
                }
            }

            public void removeUpdate(DocumentEvent e) {
                if ( new File(tfPath.getText()).exists() ) {
                    firePropertyChange("path", null, tfPath.getText());
                    tfPath.setBackground(defaultBgColor);
                    tfPath.setOpaque(false);
                } else {
                    tfPath.setBackground(Color.RED);
                    tfPath.setOpaque(true);
                }
            }

            public void changedUpdate(DocumentEvent e) {
                if ( new File(tfPath.getText()).exists() ) {
                    firePropertyChange("path", null, tfPath.getText());
                    tfPath.setBackground(defaultBgColor);
                    tfPath.setOpaque(false);
                } else {
                    tfPath.setBackground(Color.RED);
                    tfPath.setOpaque(true);
                }
            }
        });

        //tfPath.setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
        // Button to browse
        btBrowse = new JButton();
        btBrowse.setBorder(BorderFactory.createEmptyBorder());
        btBrowse.setText("Browse");
        btBrowse.setHorizontalTextPosition(SwingConstants.CENTER);
        btBrowse.setHorizontalAlignment(SwingConstants.CENTER);
//            btBrowse.setActionCommand("browse");
        btBrowse.setPreferredSize( new Dimension(96,32) );
        //btBrowse.setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));

        add(tfPath);
        add(btBrowse);


        setPreferredSize(getPreferredSize());
//            new Dimension(
//                    Math.max(tfPath.getHeight(), btBrowse.getHeight()),
//                    (int) 1.2*(tfPath.getgetWidth() + btBrowse.getWidth()) ));

        // Bind action on button to choose file
        btBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Set to current path if any
                if ( tfPath.getText() != null ) {
                    File current = new File(tfPath.getText());
                    if (current.exists()) {
                        jfc.setSelectedFile( current );
                    } else {
                        jfc.setCurrentDirectory( new File(System.getProperty("user.home")) );
                    }
                }
                // Operate browsing
//                    if (e.getActionCommand().equals("browse")) {
                jfc.setDialogTitle(jfcTitle);
                if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    // Some new path have been selected
                    String prev = tfPath.getText();
                    tfPath.setText( jfc.getSelectedFile().getAbsolutePath() );
                    firePropertyChange("path", prev, tfPath.getText());
                }
//                    }
            }
        });
    }

    /**
     * Programaticaly change the path.
     * @param path  new path to be set
     */
    public void setPath(String path) {
        tfPath.setText(path); // fire removeUpdate then changedUpdate
    }

    public String getPath() {
        return tfPath.getText();
    }
}
