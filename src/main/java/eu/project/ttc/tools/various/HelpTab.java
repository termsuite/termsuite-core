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

import java.io.IOException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

public class HelpTab {

	private URL url;
	
	private void setUrl(String name, String locale) throws IOException {
		String path = "eu/project/ttc/" + locale.toLowerCase() + "/documents/" + name + ".rtf";
		this.url = this.getClass().getClassLoader().getResource(path);
	}
	
	private URL getUrl() {
		return this.url;
	}
	
	private JEditorPane text;
	
	private void setText() throws IOException {
		this.text = new JEditorPane();
		this.text.setPage(this.getUrl());
		this.text.setEditable(false);
	}
	
	private JEditorPane getText() {
		return this.text;
	}
	
	private JScrollPane component;
	
	private void setComponent() {
		this.component = new JScrollPane(this.getText());
	}
	
	public JScrollPane getComponent() {
		return this.component;
	}
	
	public HelpTab(String name, String locale) throws IOException {
		this.setUrl(name, locale);
		this.setText();
		this.setComponent();
	}
	
}
