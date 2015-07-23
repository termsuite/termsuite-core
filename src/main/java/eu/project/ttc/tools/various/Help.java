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

import java.awt.Component;
import java.io.IOException;
import java.util.Locale;

import javax.swing.JTabbedPane;

public class Help {

	private Locale locale;
	
	private void setLocale() {
		Locale locale = Locale.getDefault();
		if (locale == null) {
			this.locale = Locale.ENGLISH;
		} else {
			this.locale = locale;
		}
	}
	
	public Locale getLocale() {
		return this.locale;
	}
	
	private Component getHelp(String name, String locale) {
		try { 
			HelpTab help = new HelpTab(name, locale);
			return help.getComponent();
		} catch (IOException e) {
			try {
				HelpTab help = new HelpTab(name, Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH));
				return help.getComponent();	
			} catch (IOException ee) {
				ee.printStackTrace();
				return null;
			}
		}
	}
	
	private JTabbedPane component;
	
	private void setComponent() {		
		this.component = new JTabbedPane();
		this.component.setTabPlacement(JTabbedPane.RIGHT);
		String locale = this.getLocale().getDisplayLanguage(Locale.ENGLISH);
		this.component.addTab("   Term Suite   ",this.getHelp("TermSuite", locale));
		this.component.addTab("   TreeTagger   ",this.getHelp("TreeTagger", locale));
		this.component.addTab("     Termer     ",this.getHelp("Termer", locale));
		this.component.addTab(" Contextualizer ",this.getHelp("Contextualizer", locale));
		this.component.addTab("    Aligner     ",this.getHelp("Aligner", locale));
		this.component.addTab("   Converter    ",this.getHelp("Converter", locale));
	}
	
	public JTabbedPane getComponent() {
		return this.component;
	}
	
	public Help() {
		this.setLocale();
		this.setComponent();
	}

	public void selectTermSuite() {
		this.getComponent().setSelectedIndex(0);
	}
	
	public void selectTreeTagger() {
		this.getComponent().setSelectedIndex(1);
	}
	
	public void selectTermer() {
		this.getComponent().setSelectedIndex(2);
	}
	
	public void selectContextualizer() {
		this.getComponent().setSelectedIndex(3);
	}
	
	public void selectAligner() {
		this.getComponent().setSelectedIndex(4);
	}
	
	public void selectConverter() {
		this.getComponent().setSelectedIndex(5);
	}
	
}
