/*******************************************************************************
 * Copyright 2015 - CNRS (Centre National de Recherche Scientifique)
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *******************************************************************************/
package eu.project.ttc.engines.exporter;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

import eu.project.ttc.engines.AbstractTermIndexExporter;
import eu.project.ttc.models.CompoundType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermOccurrence;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.utils.TermSuiteUtils;

/**
 * Exports a {@link TermIndex} in TSV format
 * 
 * @author Damien Cram
 *
 */
public class TBXExporter extends AbstractTermIndexExporter {
	private static final Logger LOGGER = LoggerFactory.getLogger(TBXExporter.class);

	/** Prints float out numbers */
	private static final NumberFormat NUMBER_FORMATTER = NumberFormat.getNumberInstance(Locale.US);

	/** Prefix used in langset ids */
	private static final String LANGSET_ID_PREFIX = "langset-";

	/** Prefix used in langset ids */
	private static final String TERMENTRY_ID_PREFIX = "entry-";

	/** Prefix used in langset ids */
	private static final String TIG_ID_PREFIX = "term-";
	
	/*
	 * Configuration parameters
	 */
	public static final String LANGUAGE = "Language";
	@ConfigurationParameter(name=LANGUAGE, mandatory = true)
	private String lang;
	
	/* The tbx document */
	private Document document;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		try {
			prepareTBXDocument();
		} catch (ParserConfigurationException e) {
			throw new ResourceInitializationException(e);
		}
		NUMBER_FORMATTER.setMaximumFractionDigits(4);
		NUMBER_FORMATTER.setMinimumFractionDigits(4);
		NUMBER_FORMATTER.setRoundingMode(RoundingMode.UP);
		NUMBER_FORMATTER.setGroupingUsed(false);
	}
	
	@Override
	protected void processAcceptedTerms(TreeSet<Term> acceptedTerms)
			throws AnalysisEngineProcessException {
		try {
			for(Term t: acceptedTerms) {
	            addTermEntry(t, false);
				for(TermVariation v:t.getVariations())
	                addTermEntry(v.getVariant(), true);
			}
			exportTBXDocument();
		} catch (TransformerException | IOException e) {
			LOGGER.error("An error occurred when exporting term index to file {}", this.toFilePath);
			throw new AnalysisEngineProcessException(e);
		}
	}
	
	  /**
     * Prepare the TBX document that will contain the terms.
     */
	private void prepareTBXDocument() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		this.document = builder.newDocument();

		Element martif = document.createElement("martif");
		martif.setAttribute("type", "TBX");
		document.appendChild(martif);

		Element header = document.createElement("martifHeader");
		martif.appendChild(header);

		Element fileDesc = document.createElement("fileDesc");
		header.appendChild(fileDesc);

		Element encodingDesc = document.createElement("encodingDesc");
		header.appendChild(encodingDesc);

		Element encodingP = document.createElement("p");
		encodingP.setAttribute("type", "XCSURI");
		encodingP.setTextContent("http://ttc-project.googlecode.com/files/ttctbx.xcs");
		encodingDesc.appendChild(encodingP);

		Element sourceDesc = document.createElement("sourceDesc");
		Element p = document.createElement("p");
//		p.setTextContent(workingDir.getAbsolutePath());
		sourceDesc.appendChild(p);
		fileDesc.appendChild(sourceDesc);

		Element text = document.createElement("text");
		martif.appendChild(text);

        Element body = document.createElement("body");
		text.appendChild(body);
    }


	
    /**
     * Export the TBX document to a file specified in parameter.
     *
     * @throws TransformerException
     */
    private void exportTBXDocument() throws TransformerException {
        // Prepare the transformer to persist the file
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
				"http://ttc-project.googlecode.com/files/tbxcore.dtd");
		transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		try {
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		} catch (IllegalArgumentException e) {
			throw new TransformerException(e);
		} // Ignore

        // Actually persist the file
		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(this.toFile);
		transformer.transform(source, result);
	}
    
    private LoadingCache<Term, Set<TermOccurrence>> allOccurrencesCaches = CacheBuilder.newBuilder()
			.maximumSize(100)
			.build(
	           new CacheLoader<Term, Set<TermOccurrence>>() {
	        	   @Override
	        	public Set<TermOccurrence> load(Term term) throws Exception {
	        		return term.getAllOccurrences();
	        	}
           });

    private LoadingCache<Term, LinkedHashMap<String, Integer>> termForms = CacheBuilder.newBuilder()
			.maximumSize(100)
			.build(
	           new CacheLoader<Term, LinkedHashMap<String, Integer>>() {
	        	   @Override
	        	public LinkedHashMap<String, Integer> load(Term term) throws Exception {
	        		   Set<TermOccurrence> allOccurrences = allOccurrencesCaches.getUnchecked(term);
	        		   List<String> forms = Lists.newArrayListWithCapacity(allOccurrences.size());
	        		   for(TermOccurrence o:allOccurrences)
	        			   forms.add(TermSuiteUtils.trimInside(o.getCoveredText()));
	        		   return TermSuiteUtils.getCounters(forms);
	        	}
           });

    /**
     * Add a term to the TBX document.
     *
     * @param doc
     * @param langsetId
     * @param term
     * @param isVariant
     * @throws IOException
     */
	private void addTermEntry(Term term, boolean isVariant)
			throws IOException {
		String langsetId = LANGSET_ID_PREFIX + term.getId();
        Node body = document.getElementsByTagName("body").item(0);

		Element termEntry = document.createElement("termEntry");
		termEntry.setAttribute("xml:id",
				TERMENTRY_ID_PREFIX + term.getId());
		body.appendChild(termEntry);
		Element langSet = document.createElement("langSet");
		langSet.setAttribute("xml:id", langsetId);
		langSet.setAttribute("xml:lang", this.lang);
		termEntry.appendChild(langSet);

		for (TermVariation variation : term.getBases()) 
			this.addTermBase(langSet, variation.getBase().getGroupingKey(), null);

		for (TermVariation variation : term.getVariations()) {
			this.addTermVariant(langSet, String.format("langset-%d", variation.getVariant().getId()),
					variation.getVariant().getGroupingKey());
		}
		Set<TermOccurrence> allOccurrences = allOccurrencesCaches.getUnchecked(term);
		this.addDescrip(langSet, langSet, "nbOccurrences", allOccurrences.size());

		Element tig = document.createElement("tig");
		tig.setAttribute("xml:id", TIG_ID_PREFIX + term.getId());
		langSet.appendChild(tig);
		Element termElmt = document.createElement("term");
		termElmt.setTextContent(term.getGroupingKey());
		tig.appendChild(termElmt);

		LinkedHashMap<String, Integer> formCounters = termForms.getUnchecked(term);
		addNote(langSet, tig, "termPilot", formCounters.entrySet().iterator().next().getKey());

		this.addNote(langSet, tig, "termType", isVariant ? "variant" : "termEntry");
		this.addNote(
				langSet,
				tig,
				"partOfSpeech",
				term.isMultiWord() ? "noun" : term.firstWord().getSyntacticLabel());
		this.addNote(langSet, tig, "termPattern", term.firstWord().getSyntacticLabel());
		this.addNote(langSet, tig, "termComplexity",
				this.getComplexity(term));
		this.addDescrip(langSet, tig, "termSpecificity",
				NUMBER_FORMATTER.format(term.getWR()));
		this.addDescrip(langSet, tig, "nbOccurrences",
				term.getFrequency());
		this.addDescrip(langSet, tig, "relativeFrequency",
				NUMBER_FORMATTER.format(term.getFrequency()));
		addDescrip(langSet, tig, "formList",
					buildFormListJSON(term, formCounters.size()));
		 this.addDescrip(langSet, tig, "domainSpecificity",
				 term.getWR());
	}
	
	private void addDescrip(Element lang, Element element,
			String type, Object value) {
		Element descrip = document.createElement("descrip");
		element.appendChild(descrip);
		descrip.setAttribute("type", type);
		descrip.setTextContent(value.toString());
	}

	private void addTermBase(Element lang, String target, Object value) {
		Element descrip = document.createElement("descrip");
		lang.appendChild(descrip);
		descrip.setAttribute("type", "termBase");
		descrip.setAttribute("target", "#"+target);
		if (value != null) {
			descrip.setTextContent(value.toString());
		}
	}

	private void addTermVariant(Element lang, String target,
			Object value) {
		Element descrip = document.createElement("descrip");
		lang.appendChild(descrip);
		descrip.setAttribute("type", "termVariant");
		descrip.setAttribute("target", "#"+target);
		if (value != null) {
			descrip.setTextContent(value.toString());
		}
	}

	private void addNote(Element lang, Element element,
			String type, Object value) {
		Element termNote = document.createElement("termNote");
		element.appendChild(termNote);
		termNote.setAttribute("type", type);
		termNote.setTextContent(value.toString());
	}

	private String buildFormListJSON(Term term, int size) {
		StringBuilder sb = new StringBuilder("[");
		LinkedHashMap<String, Integer> formCounts = termForms.getUnchecked(term);

		int i = 0;
		for (String form:formCounts.keySet()) {
			if (i > 0)
				sb.append(", ");
			sb.append("{term=\"").append(form);
			sb.append("\", count=").append(formCounts.get(form)).append("}");
			i++;
		}
		sb.append("]");
		return sb.toString();
	}

	private String getComplexity(Term term) {
		if (term.isSingleWord()) {
			if(term.isCompound()) {
				if(term.firstWord().getWord().getCompoundType() == CompoundType.NEOCLASSICAL)
					return "neoclassical-compound";
				else
					return "compound";
			} else
				return "single-word";
		}
			return "multi-word";
	}
}
