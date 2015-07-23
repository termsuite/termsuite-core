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
package eu.project.ttc.tools.various;

import java.awt.Frame;

import javax.swing.JOptionPane;

import eu.project.ttc.tools.commons.TermSuiteVersion;



public class About {

	private Frame frame;
	
	public void setFrame(Frame frame) {
		this.frame = frame;
	}
	
	private Frame getFrame() {
		return this.frame;
	}
	
	private final String getTitle() {
		return TermSuiteVersion.TITLE;
	}
	
	private final String getVersion() {
        return "(version " + TermSuiteVersion.VERSION + ")";
	}
	
	private final String getLicense() {
        return TermSuiteVersion.LICENSE;
	}
	
	private final String getSummary() {
        return TermSuiteVersion.SUMMARY;
	}
		
	private String getMessage() {
		return getTitle() + " " + getVersion() + "\n" + getSummary() + "\n" + getLicense();
	}
	
	public void show() { 
		JOptionPane.showMessageDialog(this.getFrame(),
                this.getMessage(),
                "About",
                JOptionPane.INFORMATION_MESSAGE,
                null);
	}
	
}
