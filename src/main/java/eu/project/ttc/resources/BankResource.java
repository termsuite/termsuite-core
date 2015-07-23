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
package eu.project.ttc.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;

import eu.project.ttc.models.Tree;

public class BankResource implements Bank {
	
	private class CharacterTree implements Tree<Character> {

		private Map<Character,CharacterTree> childs;
		
		private void setChilds() {
			this.childs = new HashMap<Character,CharacterTree>();
		}
		
		public Map<Character,CharacterTree> getChilds() {
			return this.childs;
		}
		
		@Override
		public CharacterTree getSubTree(Character c) {
			return this.getChilds().get(c);
		}

		private boolean leaf;
		
		public void leaf(boolean enabled) {
			this.leaf = enabled;
		}
		
		@Override
		public boolean isLeaf() {
			return this.leaf;
		}
		
		public CharacterTree() {
			this.setChilds();
			this.leaf(false);
		}
		
		public String toString() {
			return this.toString(0);
		}

		private String toString(int level) {
			String string = "";
			for (Character c : this.getChilds().keySet()) {
				for (int index = 0; index < level; index++) {
					string += "  ";
				}
				string += "'" + c.toString() + "'" + "\n";
				string += this.getChilds().get(c).toString(level + 1);
			}
			if (this.isLeaf()) {
				for (int index = 0; index < level; index++) {
					string += "  ";
				}
				string += " *\n";
			}
			return string + "\n";
		}

		
	}
	
	public BankResource() {
		this.setPrefixTree();
		this.setSuffixTree();
	}
	
	private CharacterTree prefixTree;
	
	private void setPrefixTree() {
		this.prefixTree = new CharacterTree();
	}
	
	@Override
	public CharacterTree getPrefixTree() {
		return this.prefixTree;
	}
	
	private CharacterTree suffixTree;
	
	private void setSuffixTree() {
		this.suffixTree = new CharacterTree();
	}
	
	@Override
	public CharacterTree getSuffixTree() {
		return this.suffixTree;
	}
	
	private void addRoot(CharacterTree tree,String word) {
		CharacterTree current = tree;
		int length = word.length() - 1;
		for (int index = 0; index <= length; index++) {
			char c = word.charAt(index);
			CharacterTree child = current.getSubTree(c);
			if (child == null) {
				CharacterTree t = new CharacterTree();
				current.getChilds().put(c,t);
				current = t;
			} else {
				current = child;
			}
			if (index == length) {
				current.leaf(true);
			}
		}
	}	

	@Override
	public void load(DataResource aData) throws ResourceInitializationException {
		try {
			this.doLoad(aData.getInputStream());
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	private void doLoad(InputStream inputStream) throws IOException {
		Scanner scanner = new Scanner(inputStream);
		scanner.useDelimiter(System.getProperty("line.separator"));
		while (scanner.hasNext()) {
			String line = scanner.next();
			if (line.startsWith("#")) {
				continue;
			} else {
				this.doParse(line);
			}
		}
		scanner.close();
	}

    private void doParse(String line) throws IOException {
    	int length = line.length();
    	if (length >= 2) {
    		int first = line.indexOf("-");
    		int last = line.lastIndexOf("-");
    		if (first == -1 && last == -1) {
    			return;
    		} else if (first == last) {
    			if (first == 0) {
    				String suffix = line.substring(1);
    				String reverse = new StringBuffer(suffix).reverse().toString();
    				this.addRoot(this.getSuffixTree(),reverse);
    			} else if (last == length - 1) {
    				String prefix = line.substring(0,last);
    				this.addRoot(this.getPrefixTree(),prefix);
    			}
    		} else {
    			String affix = line.substring(1, line.length() - 1);
				String reverse = new StringBuffer(affix).reverse().toString();
				this.addRoot(this.getPrefixTree(),affix);
				this.addRoot(this.getSuffixTree(),reverse);
    		}
    	} else {
    		return;
    	}
    }


}
