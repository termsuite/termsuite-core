package eu.project.ttc.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.project.ttc.utils.TermSuiteConstants;

public class PrefixTree implements SharedResourceObject {
	private static final Logger LOGGER = LoggerFactory.getLogger(PrefixTree.class);

	public static final String PREFIX_TREE = "PrefixTree";

	private Node rootNode = new Node(Node.ROOT_CHAR);
	
	private static class Node {
		public static final char ROOT_CHAR = '^';
		private boolean validPrefix;
		private char character;
		private Node parent;
		private Map<Character, Node> children = Maps.newHashMap();
		
		public Node(char character) {
			super();
			this.character = character;
		}
		
		public void indexString(Queue<Character> charSequence) {
			indexString(charSequence, 0);
		}
		private void indexString(Queue<Character> charSequence, int depth) {
			if(charSequence.isEmpty()) {
				Preconditions.checkArgument(!(isRoot() && depth == 0), "Empty string is not a valid prefix");
				this.validPrefix = true;
			} else {
				Character c = charSequence.poll();
				Node childNode;
				if(children.containsKey(c))
					childNode = children.get(c);
				else {
					childNode = new Node(c);
					childNode.parent = this;
					children.put(c, childNode);
				}

				childNode.indexString(charSequence, depth + 1);
			}
		}

		public String getPrefix(Queue<Character> charSequence) {
			return getPrefix(charSequence, null, 0);
		}
		
		public String getPrefix(Queue<Character> charSequence, String lastPrefixFound, int depth) {
			if(validPrefix)
				lastPrefixFound = toPrefixString();
			if(charSequence.isEmpty())
				return lastPrefixFound;
			else {
				Character c = charSequence.poll();
				if(children.containsKey(c)) {
					Node child = children.get(c);
					return child.getPrefix(charSequence, lastPrefixFound, depth + 1);
				} else 
					return lastPrefixFound;
			}
			
		}

		private String toPrefixString() {
			return toPrefixString(new StringBuffer());
		}
		private String toPrefixString(StringBuffer buffer) {
			if(isRoot()) {
				return buffer.reverse().toString();
			} else {
				buffer.append(this.character);
				return parent.toPrefixString(buffer);
			}
		}

		private boolean isRoot() {
			return this.character == ROOT_CHAR;
		}
	}
	
	@Override
	public void load(DataResource data) throws ResourceInitializationException {
		InputStream inputStream = null;
		try {
			inputStream = data.getInputStream();
			Scanner scanner = null;
			try {
				String line;
				scanner = new Scanner(inputStream, "UTF-8");
				scanner.useDelimiter(TermSuiteConstants.LINE_BREAK);
				while (scanner.hasNext()) {
					line = scanner.next().split(TermSuiteConstants.DIESE)[0].trim();
					if(line.startsWith("#"))
						continue;
					else if(line.isEmpty())
						continue;
					else if(line.endsWith("-")) {
						if(line.length() == 1)
							continue;
						else
							line = line.substring(0, line.length() - 1);
					}
						
					rootNode.indexString(toCharQueue(line));
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new ResourceInitializationException(e);
			} finally {
				IOUtils.closeQuietly(scanner);
			}
		} catch (IOException e) {
			LOGGER.error("Could not load file {}",data.getUrl());
			throw new ResourceInitializationException(e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}

	}
	
	public String getPrefix(String word) {
		return rootNode.getPrefix(toCharQueue(word));
	}
	
	
	private Queue<Character> toCharQueue(String line) {
		LinkedList<Character> characters = Lists.newLinkedList();
		for (int i = 0; i < line.length(); i++)
			characters.add(line.charAt(i));
		return characters;
	}
	
}
