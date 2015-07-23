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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;

import eu.project.ttc.tools.commons.LanguageItem;
import eu.project.ttc.tools.commons.TTCDirectoryChooser;

/**
 * This JPanel exposes the configuration part of the Spotter tool.
 *
 * @author Fabien Poulard <fpoulard@dictanova.com>
 */
public class ConfigPanel extends JPanel {

    private static final long serialVersionUID = -726785057846274354L;
    
    private static final String LBL_INPUT = "Input Directory";
    private static final String LBL_LANGUAGE = "Language";
    private static final String LBL_OUTPUT = "Output Directory";
    private static final String LBL_TTG = "TreeTagger Directory";
    private final static String LBL_TSV = "Export in TSV format";


    // Main title
    private JEditorPane epHelp;

    // Language parameter
    private JLabel lblLanguage;
    private JComboBox<LanguageItem> cbLanguage;
    private JEditorPane epLanguage;

    // Input directory parameter
    private JLabel lblInDirectory;
    private JEditorPane epInDirectory;
    private TTCDirectoryChooser fcInDirectory;

    // Output directory parameter
    private JLabel lblOutDirectory;
    private JEditorPane epOutDirectory;
    private TTCDirectoryChooser fcOutDirectory;

    // TreeTagger directory parameter
    private JLabel lblTtgDirectory;
    private JEditorPane epTtgDirectory;
    private TTCDirectoryChooser fcTtgDirectory;

    // TSV export parameter
    private JLabel lblTSV;
    private JCheckBox cbTSV;
    private JEditorPane epTSV;
    
    private GroupLayout cfgLayout;

    public ConfigPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Prepare components
        createHelpTitle();
        int pWidth = (int) getPreferredSize().getWidth() / 2;
        createLanguageForm(pWidth);
        createInputDirectoryForm(pWidth);
        createOutputDirectoryForm(pWidth);
        createEnableTsvForm(pWidth);
        createTtgDirectoryForm(pWidth);

        // Layout components together
        layoutComponents();
    }

    /**
     * This HUGE method is responsible to layout the panel by grouping
     * together parameters labels, fields and descriptions.
     *
     * Yes, this is quite massive and it looks messy, but I didn't find
     * a better way to do it as there are side effects regarding the
     * group layout.
     * So please, do not intend to change things unless you're sure you
     * plainly understand the group layout!
     */
    private void layoutComponents() {
        JPanel config = new JPanel();

        // Init the group layout
        cfgLayout = new GroupLayout(config);
        config.setLayout(cfgLayout);

        // Configure the horizontal layout
        cfgLayout.setHorizontalGroup(
                cfgLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        // Language parameter
                        .addGroup(cfgLayout.createSequentialGroup()
                                .addGroup(cfgLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(lblLanguage)
                                        .addComponent(cbLanguage))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(epLanguage))
                        // Input directory parameter
                        .addGroup(cfgLayout.createSequentialGroup()
                                .addGroup(cfgLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(lblInDirectory)
                                        .addComponent(fcInDirectory))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(epInDirectory)
                        )
                        // Output directory parameter
                        .addGroup(cfgLayout.createSequentialGroup()
                                .addGroup(cfgLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(lblOutDirectory)
                                        .addComponent(fcOutDirectory))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(epOutDirectory)
                        )
                        // Enable TSV parameter
                        .addGroup(cfgLayout.createSequentialGroup()
                                .addComponent(cbTSV)
                                .addComponent(lblTSV)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(epTSV))
                        // TreeTagger directory parameter
                        .addGroup(cfgLayout.createSequentialGroup()
                                .addGroup(cfgLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(lblTtgDirectory)
                                        .addComponent(fcTtgDirectory))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(epTtgDirectory)
                        )
        );

        // Configure the vertical layout
        cfgLayout.setVerticalGroup(
                cfgLayout.createSequentialGroup()
                        // Language parameter
                        .addGroup(cfgLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(cfgLayout.createSequentialGroup()
                                        .addComponent(lblLanguage)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbLanguage,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.PREFERRED_SIZE))
                                .addComponent(epLanguage))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                // Input directory parameter
                        .addGroup(cfgLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(cfgLayout.createSequentialGroup()
                                        .addComponent(lblInDirectory)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(fcInDirectory))
                                .addComponent(epInDirectory)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                // Output directory parameter
                        .addGroup(cfgLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(cfgLayout.createSequentialGroup()
                                        .addComponent(lblOutDirectory)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(fcOutDirectory))
                                .addComponent(epOutDirectory)
                        )
                        // Enable tsv
                        .addGroup(cfgLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(cbTSV,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblTSV)
                                .addComponent(epTSV))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                // TreeTagger directory parameter
                        .addGroup(cfgLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(cfgLayout.createSequentialGroup()
                                        .addComponent(lblTtgDirectory)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(fcTtgDirectory))
                                .addComponent(epTtgDirectory)
                        )
        );


        // Set everything together
        Box box = Box.createVerticalBox();
        box.setOpaque(false);
        box.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        box.add(epHelp);
        box.add( Box.createRigidArea( new Dimension(0, 15) ) );
        box.add( new JSeparator(SwingConstants.HORIZONTAL) );
        box.add( Box.createRigidArea( new Dimension(0, 15) ) );
        box.add(config);
        add(box, BorderLayout.NORTH);
    }

    private void createHelpTitle() {
        epHelp = new JEditorPane();
        epHelp.setEditable(false);
        epHelp.setOpaque(false);
        try {
            URL resHelp = getClass().getResource("/eu/project/ttc/gui/texts/spotter/mainhelp.html");
            epHelp.setPage(resHelp);
        } catch (IOException e) {} // No help available
    }

    /**
     * Create the ParameterGroup dedicated to configure the language to be used.
     */
    public void createLanguageForm(int preferredWidth) {
        // Language label
        lblLanguage = new JLabel("<html><b>" + LBL_LANGUAGE + "</b></html>");
        lblLanguage.setPreferredSize(new Dimension(
                (int) lblLanguage.getPreferredSize().getHeight(),
                preferredWidth ));

        // Language combobox
        cbLanguage = new JComboBox<LanguageItem>();
        for(String code: new String[]{"en","fr","de","es","ru","da","lv","zh"}) {
            cbLanguage.addItem( new LanguageItem(code) );
        }
        cbLanguage.setPreferredSize(new Dimension(
                (int) cbLanguage.getPreferredSize().getHeight(),
                preferredWidth ));
        cbLanguage.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    LanguageItem item = (LanguageItem) e.getItem();;
                    firePropertyChange(SpotterBinding.PRM.LANGUAGE.getProperty(), null, item.getValue());
                }
            }
        });

        // Help pane
        epLanguage = new JEditorPane();

        epLanguage.setEditable(false);
        epLanguage.setOpaque(false);
        epLanguage.setPreferredSize(new Dimension(
                (int) epLanguage.getPreferredSize().getHeight(),
                preferredWidth));
        try {
            URL res = getClass().getResource("/eu/project/ttc/gui/texts/spotter/param.language.html");
            epLanguage.setPage(res);
        } catch (IOException e){} // No various
    }

    /**
     * Create the ParameterGroup dedicated to configure the input directory.
     */
    public void createInputDirectoryForm(int preferredWidth) {
        // Input directory label
        lblInDirectory = new JLabel("<html><b>" + LBL_INPUT + "</b></html>");
        lblInDirectory.setPreferredSize(new Dimension(
                (int) lblInDirectory.getPreferredSize().getHeight(),
                preferredWidth ));

        // Input directory field
        fcInDirectory = new TTCDirectoryChooser("Choose the input directory");
        fcInDirectory.setPreferredSize(new Dimension(
                (int) fcInDirectory.getPreferredSize().getHeight(),
                preferredWidth ));
        fcInDirectory.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ("path".equals(evt.getPropertyName()))
                    firePropertyChange(SpotterBinding.PRM.INPUT.getProperty(),
                            evt.getOldValue(), evt.getNewValue());
            }
        });

        // Help panel
        epInDirectory = new JEditorPane();
        epInDirectory.setEditable(false);
        epInDirectory.setOpaque(false);
        epInDirectory.setPreferredSize( new Dimension(
                (int) epInDirectory.getPreferredSize().getHeight(),
                preferredWidth ));
        try {
            URL res = getClass().getResource("/eu/project/ttc/gui/texts/spotter/param.inputdirectory.html");
            epInDirectory.setPage(res);
        } catch (IOException e) {} // No various
    }

    /**
     * Create the ParameterGroup dedicated to configure the output directory.
     */
    public void createOutputDirectoryForm(int preferredWidth) {
        // Output directory label
        lblOutDirectory = new JLabel("<html><b>" + LBL_OUTPUT + "</b></html>");
        lblOutDirectory.setPreferredSize(new Dimension(
                (int) lblOutDirectory.getPreferredSize().getHeight(),
                preferredWidth ));

        // Output directory field
        fcOutDirectory = new TTCDirectoryChooser("Choose the output directory");
        fcOutDirectory.setPreferredSize(new Dimension(
                (int) fcOutDirectory.getPreferredSize().getHeight(),
                preferredWidth ));
        fcOutDirectory.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ("path".equals(evt.getPropertyName()))
                    firePropertyChange(SpotterBinding.PRM.OUTPUT.getProperty(),
                            evt.getOldValue(), evt.getNewValue());
            }
        });

        // Help pane
        epOutDirectory = new JEditorPane();
        epOutDirectory.setEditable(false);
        epOutDirectory.setOpaque(false);
        epOutDirectory.setPreferredSize( new Dimension(
                (int) epOutDirectory.getPreferredSize().getHeight(),
                preferredWidth ));
        try {
            URL res = getClass().getResource("/eu/project/ttc/gui/texts/spotter/param.outputdirectory.html");
            epOutDirectory.setPage(res);
        } catch (IOException e) {} // No various
    }

    /**
     * Create the ParameterGroup dedicated to enable/disable the TSV output.
     */
    private void createEnableTsvForm(int pWidth) {
        // Label
        lblTSV = new JLabel("<html><b>" + LBL_TSV + "</b></html>");
        lblTSV.setPreferredSize(new Dimension((int) lblTSV.getPreferredSize()
                .getHeight(), pWidth));

        // Checkbox as it is a boolean
        cbTSV = new JCheckBox();
        cbTSV.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out
                        .println("Detected a TSV change, fire property change.");
                firePropertyChange(SpotterBinding.PRM.ENABLETSV.getProperty(),
                        !cbTSV.isSelected(), cbTSV.isSelected());
            }
        });

        // Editor pane to display help
        epTSV = new JEditorPane();
        epTSV.setEditable(false);
        epTSV.setOpaque(false);
        epTSV.setPreferredSize(new Dimension((int) epTSV.getPreferredSize()
                .getHeight(), pWidth));
        try {
            URL res = getClass().getResource(
                    "/eu/project/ttc/gui/texts/spotter/param.enabletsv.html");
            epTSV.setPage(res);
        } catch (IOException e) {
        } // No help
    }
    
    /**
     * Create the ParameterGroup dedicated to configure the tree tagger directory.
     */
    public void createTtgDirectoryForm(int preferredWidth) {
        // TreeTagger directory label
        lblTtgDirectory = new JLabel("<html><b>" + LBL_TTG + "</b></html>");
        lblTtgDirectory.setPreferredSize(new Dimension(
                (int) lblTtgDirectory.getPreferredSize().getHeight(),
                preferredWidth ));

        // TreeTagger directory field
        fcTtgDirectory = new TTCDirectoryChooser("Choose the treetagger home directory");
        fcTtgDirectory.setPreferredSize(new Dimension(
                (int) fcTtgDirectory.getPreferredSize().getHeight(),
                preferredWidth ));
        fcTtgDirectory.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ("path".equals(evt.getPropertyName()))
                    firePropertyChange(SpotterBinding.PRM.TTGHOME.getProperty(),
                            evt.getOldValue(), evt.getNewValue());
            }
        });

        // Help pane
        epTtgDirectory = new JEditorPane();
        epTtgDirectory.setEditable(false);
        epTtgDirectory.setOpaque(false);
        epTtgDirectory.setPreferredSize( new Dimension(
                (int) epTtgDirectory.getPreferredSize().getHeight(),
                preferredWidth ));
        try {
            URL res = getClass().getResource("/eu/project/ttc/gui/texts/spotter/param.ttgdirectory.html");
            epTtgDirectory.setPage(res);
        } catch (IOException e) {} // No various
    }

    ////////////////////////////////////////////////////////////////////// ACCESSORS

    public void setLanguage(String language) {
        for(int i=0 ; i < cbLanguage.getItemCount() ; i++) {
            LanguageItem item = (LanguageItem) cbLanguage.getItemAt(i);
            if ( item.getValue().equals(language) ) {
                if (cbLanguage.getSelectedIndex() != i) {
                    cbLanguage.setSelectedItem( item );
                }
                return;
            }
        }
        // If reach here, we have a problem
        throw new IllegalArgumentException("I cannot reflect the change to value '"
                + language + "' as I do not handle this value.");
    }

    public void setLanguageError(IllegalArgumentException e) {
        lblLanguage.setText("<html><b>" + LBL_LANGUAGE + "</b><br/><p style=\"color: red; font-size: small\">"
                + e.getMessage() + "</p></html>");
    }
    public void unsetLanguageError() {
        lblLanguage.setText("<html><b>" + LBL_LANGUAGE + "</b></html>");
    }

    public String getLanguage() {
        return ((LanguageItem) cbLanguage.getSelectedItem()).getValue();
    }

    public void setInputDirectory(String inputDirectory) {
        fcInDirectory.setPath(inputDirectory);
    }

    public void setInputDirectoryError(IllegalArgumentException e) {
        lblInDirectory.setText("<html><b>" + LBL_INPUT + "</b><br/><p style=\"color: red; font-size: small\">"
                + e.getMessage() + "</p></html>");
    }
    public void unsetInputDirectoryError() {
        lblInDirectory.setText("<html><b>" + LBL_INPUT + "</b></html>");
    }

    public String getInputDirectory() {
        return fcInDirectory.getPath();
    }

    public void setOutputDirectory(String outputDirectory) {
        fcOutDirectory.setPath(outputDirectory);
    }

    public void setOutputDirectoryError(IllegalArgumentException e) {
        lblOutDirectory.setText("<html><b>" + LBL_OUTPUT + "</b><br/><p style=\"color: red; font-size: small\">"
                + e.getMessage() + "</p></html>");
    }
    public void unsetOutputDirectoryError() {
        lblOutDirectory.setText("<html><b>" + LBL_OUTPUT + "</b></html>");
    }

    public String getOutputDirectory() {
        return fcOutDirectory.getPath();
    }

    public void setTreetaggerHome(String ttgDirectory) {
        fcTtgDirectory.setPath(ttgDirectory);
    }

    public void setTreetaggerHomeError(IllegalArgumentException e) {
        lblTtgDirectory.setText("<html><b>" + LBL_TTG + "</b><br/><p style=\"color: red; font-size: small\">"
                + e.getMessage() + "</p></html>");
    }
    public void unsetTreetaggerHomeError() {
        lblTtgDirectory.setText("<html><b>" + LBL_TTG + "</b></html>");
    }

    public String getTreetaggerHome() {
        return fcTtgDirectory.getPath();
    }

    public void setEnableTsvOutput(boolean enableTsv) {
        cbTSV.setSelected(enableTsv);        
    }

    public Boolean isEnableTsvOutput() {
        return cbTSV.isSelected();
    }

    public void setEnableTsvOutputError(Exception e) {
        lblTSV.setText("<html><b>"+LBL_TSV+"</b><br/><p style=\"color: red; font-size: small\">"
                + e.getMessage() + "</p></html>");        
    }
    
    public void unsetEnableTsvOutputError() {
        lblTSV.setText("<html><b>"+LBL_TSV+"</b></html>");
    }
}
