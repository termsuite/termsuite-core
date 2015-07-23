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
package eu.project.ttc.tools.various;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

@SuppressWarnings("serial")
public class MainToolBar extends JPanel {

	private final Dimension dimension = new Dimension(66, 33);
	private final Insets insets = new Insets(0, 0, 0, 0);
	private final Color color = new Color(0, 0, 1, 1);

	private JButton quit;

	private void setQuit() {
		this.quit = new JButton("Quit");
		this.quit.setActionCommand("quit");
		this.quit.setEnabled(true);
		this.quit.setPreferredSize(dimension);
		this.quit.setMargin(insets);
		this.quit.setBackground(color);
		this.quit.setBorderPainted(false);
	}

	public JButton getQuit() {
		return this.quit;
	}

	private JButton about;

	private void setAbout() {
        this.setAboutWindow();

		this.about = new JButton("About");
		this.about.setActionCommand("about");
		this.about.setEnabled(true);
		this.about.setPreferredSize(dimension);
		this.about.setMargin(insets);
		this.about.setBackground(color);
		this.about.setBorderPainted(false);
        this.about.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ( "about".equals(e.getActionCommand()) ) {
                    getAboutWindow().show();
                }
            }
        });
	}

	public JButton getAbout() {
		return this.about;
	}

	private JButton save;

	private void setSave() {
		this.save = new JButton("Save");
		this.save.setActionCommand("save");
		this.save.setEnabled(true);
		this.save.setPreferredSize(dimension);
		this.save.setMargin(insets);
		this.save.setBackground(color);
		this.save.setBorderPainted(false);
	}

	public JButton getSave() {
		return this.save;
	}

	private JButton run;

	private void setRun() {
		this.run = new JButton("Run");
		this.run.setActionCommand("run");
		this.run.setEnabled(true);
		this.run.setPreferredSize(dimension);
		this.run.setMargin(insets);
		this.run.setBackground(color);
		this.run.setBorderPainted(false);
	}

	public JButton getRun() {
		return this.run;
	}

	private JButton stop;

	private void setStop() {
		this.stop = new JButton("Stop");
		this.stop.setActionCommand("stop");
		this.stop.setEnabled(false);
		this.stop.setPreferredSize(dimension);
		this.stop.setMargin(insets);
		this.stop.setBackground(color);
		this.stop.setBorderPainted(false);
	}

	public JButton getStop() {
		return this.stop;
	}

	private JProgressBar progressBar;

	private void setProgressBar() {

		JProgressBar tmpProgress = new JProgressBar();

		switchToLnF(UIManager.getCrossPlatformLookAndFeelClassName());

		UIManager.put("ProgressBar.selectionForeground", ColorUIResource.BLACK);
		UIManager.put("ProgressBar.selectionBackground", ColorUIResource.BLACK);
		progressBar = new JProgressBar();
		// progressBar.setForeground(new Color(46,139,87));
		progressBar.setForeground(new Color(163, 204, 184));
		progressBar.setPreferredSize(new Dimension(333, 33));
		progressBar.setMaximum(0);
		progressBar.setMaximum(100);
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(false);
		progressBar.setValue(0);
		progressBar.setBorder(tmpProgress.getBorder());

		switchToLnF(UIManager.getSystemLookAndFeelClassName());
	}

	private void switchToLnF(String lookAndFeelClassName) {
		try {
			UIManager.setLookAndFeel(lookAndFeelClassName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

    private GridBagConstraints constraints;

	private void populatePanel() {
		constraints.insets = new Insets(0, 10, 0, 3);
		constraints.weightx = 0.0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		add(this.getAbout(), constraints);
		constraints.gridx = 1;
		constraints.insets = new Insets(0, 3, 0, 3);
		constraints.weightx = 0.0;
		add(this.getRun(), constraints);
		constraints.gridx = 2;
		constraints.insets = new Insets(0, 3, 0, 3);
		constraints.weightx = 0.0;
		add(this.getStop(), constraints);
		constraints.insets = new Insets(0, 3, 0, 10);
		constraints.weightx = 1.0;
		constraints.gridx = 3;
		constraints.gridwidth = 5;
		constraints.fill = GridBagConstraints.BOTH;
		add(this.getProgressBar(), constraints);
		constraints.gridx = 8;
		constraints.insets = new Insets(0, 3, 0, 3);
		constraints.weightx = 0.0;
		constraints.gridwidth = 1;
		add(this.getSave(), constraints);
		constraints.gridx = 9;
		constraints.insets = new Insets(0, 3, 0, 10);
		constraints.weightx = 0.0;
		constraints.gridwidth = 1;
		add(this.getQuit(), constraints);
	}
	
	public void enableListeners(ActionListener listener) {
		this.getQuit().addActionListener(listener);
		this.getAbout().addActionListener(listener);
		this.getSave().addActionListener(listener);
		this.getStop().addActionListener(listener);
		this.getRun().addActionListener(listener);
	}

    /**
     * Adds an <code>ActionListener</code> to the each component able
     * to fire an action.
     */
    public void addActionListener(ActionListener l) {
        getRun().addActionListener(l);
        getQuit().addActionListener(l);
        getSave().addActionListener(l);
        getAbout().addActionListener(l);
    }

	public MainToolBar() {
        super(new GridBagLayout());
        constraints = new GridBagConstraints();

		this.setQuit();
		this.setAbout();
		this.setSave();
		this.setRun();
		this.setStop();
		this.setProgressBar();
		this.populatePanel();
	}

    /**
     * Indicates if the toolbar should be in run mode or not.
     * When in run mode:
     * <ul>
     *     <li>the progress bar progresses,</li>
     *     <li>the Run button should not be enabled,</li>
     *     <li>the Stop button should be enabled.</li>
     * </ul>
     *
     * @param isRun
     *      flag indicating if the run mode should be activated
     *      (when true) or deactivated (false)
     */
    public void setRunMode(boolean isRun) {
        getRun().setEnabled(!isRun);
        getStop().setEnabled(isRun);

        // Disable all stop listeners as it does not make sense when
        // not in run mode
        if ( ! isRun ) {
            for(ActionListener l: getStop().getActionListeners()) {
                getStop().removeActionListener(l);
            }
        }
    }

    public void setProgress(int progress, String s) {
        getProgressBar().setValue(progress);
        getProgressBar().setString(s);
    }

    /************************************************************************************************** ABOUT WINDOW */

    private About aboutWindow;

    private void setAboutWindow() {
        this.aboutWindow = new About();
    }

    public About getAboutWindow() {
        return this.aboutWindow;
    }
}
