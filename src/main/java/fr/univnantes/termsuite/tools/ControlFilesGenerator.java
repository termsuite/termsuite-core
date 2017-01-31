
/*******************************************************************************
 * Copyright 2015-2016 - CNRS (Centre National de Recherche Scientifique)
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

package fr.univnantes.termsuite.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.framework.Relations;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.Component;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.Word;

/**
 * 
 * A tool that generates all control files required for 
 * functional tests from a {@link Terminology}.
 * 
 * @author Damien Cram
 *
 */
public class ControlFilesGenerator {
	
	private TerminologyService termino;
	
	public ControlFilesGenerator(TerminologyService termino) {
		super();
		this.termino = termino;
	}

	/**
	 * 
	 * @param directory
	 * 			the directory where to create the files.
	 */
	public void generate(File directory) throws IOException {
		if(!directory.exists())
			directory.mkdirs();
		
		Set<String> distinctRuleNames = Sets.newHashSet();
		termino.variations().forEach( tv -> {
			if(tv.isPropertySet(RelationProperty.VARIATION_RULE))
				distinctRuleNames.add(tv.getPropertyStringValue(RelationProperty.VARIATION_RULE));
		});

		/*
		 * Write syntactic rules
		 */
		for(String ruleName:distinctRuleNames) {
			String pathname = directory.getAbsolutePath() + "/" + getSyntacticRuleFileName(ruleName);
			writeVariations(
				pathname, 
				termino.relations()
					.filter(r-> r.isPropertySet(RelationProperty.VARIATION_RULE))
					.filter(r-> r.getPropertyStringValue(RelationProperty.VARIATION_RULE).equals(ruleName)),
				RelationProperty.VARIATION_RULE
				);
		}
		
		/*
		 * Write variation types
		 */
		for(VariationType vType:VariationType.values()) {
			String pathname = directory.getAbsolutePath() + "/variations-" + vType.getShortName();
			writeVariations(
					pathname, 
					termino.variations(vType).sorted(Relations.relFreqHmean())
				);
		}
		

		/*
		 * Write prefix variations
		 */
		String prefixPath = directory.getAbsolutePath() + "/" + getPrefixFileName();
		writeVariations(
				prefixPath, 
				termino.relations(RelationType.IS_PREFIX_OF)
			);
		

		/*
		 * Write derivative variations
		 */
		String derivativePath = directory.getAbsolutePath() + "/" + getDerivatesFileName();
		writeVariations(
				derivativePath, 
				termino.relations(RelationType.DERIVES_INTO),
				RelationProperty.DERIVATION_TYPE);

		/*
		 * Write compounds
		 */
		String compoundPath = directory.getAbsolutePath() + "/" + getCompoundsFileName();
		writeCompounds(compoundPath);
		
	}

	public static String getCompoundsFileName() {
		return "compounds.tsv";
	}

	public static String getDerivatesFileName() {
		return "derivates.tsv";
	}

	public static String getPrefixFileName() {
		return "prefixes.tsv";
	}

	public static String getSyntacticRuleFileName(String ruleName) {
		return "syntactic-" + ruleNametoFileName(ruleName) + ".tsv";
	}


	public static String ruleNametoFileName(String ruleName) {
		return ruleName.replaceAll("\\|", "-or-");
	}

	private void writeCompounds(String filePath) throws IOException {
		Writer writer = new FileWriter(filePath);
		for(Term t:termino.getTerms()) {
			if(t.isSingleWord() && t.isCompound()) {
				writer.append(String.format("%s\t%s\t%s%n", 
						t.getGroupingKey(),
						t.getWords().get(0).getWord().getCompoundType(),
						toCompoundString(t)
					)
				);
			}
		}
		writer.flush();
		writer.close();
	}


	public static String toCompoundString(Term t) {
		Preconditions.checkArgument(t.isSingleWord(), "Term %s should be a single-word term", t);
		Preconditions.checkArgument(t.isCompound(), "Term %s should be compound", t);
		List<String> componentStrings = Lists.newArrayList();
		Word word = t.getWords().get(0).getWord();
		for(Component c:word.getComponents()) {
			componentStrings.add(String.format("%s:%s", 
					word.getLemma().substring(c.getBegin(), c.getEnd()),
					c.getLemma()
					));
		}
		
		return Joiner.on("|").join(componentStrings);
	}
	
	private void writeVariations(String path, Stream<TermRelation> variations, RelationProperty... additionalProperties) throws IOException {
		Writer writer = new FileWriter(path);
		for(TermRelation tv:variations.collect(Collectors.toList())) {
			writer.append(tv.getFrom().getGroupingKey());
			writer.append("\t");
			writer.append(tv.getTo().getGroupingKey());
			writer.append("\t");
			writer.append(tv.getType().toString());
			
			for(RelationProperty p:additionalProperties) {
				writer.append("\t");
				writer.append(tv.isPropertySet(p) ? Objects.toString(tv.get(p)) : "");
			}
			
			writer.append("\n");
		}
		writer.flush();
		writer.close();
	}
}
