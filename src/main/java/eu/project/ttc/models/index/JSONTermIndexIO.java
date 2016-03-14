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
package eu.project.ttc.models.index;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.models.Component;
import eu.project.ttc.models.CompoundType;
import eu.project.ttc.models.ContextVector;
import eu.project.ttc.models.Document;
import eu.project.ttc.models.OccurrenceStore;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermBuilder;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermOccurrence;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.TermWord;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.models.Word;
import eu.project.ttc.models.WordBuilder;
import eu.project.ttc.models.occstore.MemoryOccurrenceStore;

public class JSONTermIndexIO {
	/*
	 * Error messages for parsing
	 */
	private static final String MSG_EXPECT_PROP_FOR_VAR = "Expecting %s property for term variation";
	private static final String MSG_TERM_DOES_NOT_EXIST = "Error in term variation. Term %s is not present in the dump file. Make sure that terms are defined prior to variants.";
	private static final String MSG_EXPECT_PROP_FOR_OCCURRENCE = "Expecting %s property for occurrence";
	private static final String MSG_EXPECT_PROP_FOR_TERM_WORD = "Expecting %s property for term word";

	/*
	 * Occurrence storing options
	 */
	private static final String OCCURRENCE_STORAGE_EMBEDDED = "embedded";
	private static final String OCCURRENCE_STORAGE_FILESTORE = "store";
	
	/*
	 * Json properties
	 */
	private static final String WORDS = "words";
	private static final String METADATA = "metadata";
	private static final String LANG = "lang";
	private static final String NAME = "name";
	private static final String CORPUS_ID = "corpus-id";
	private static final String LEMMA = "lemma";
	private static final String STEM = "stem";
	private static final String COMPOUND_TYPE = "compound_type";
	private static final String COMPONENTS = "components";
	private static final String BEGIN = "begin";
	private static final String END = "end";
	private static final String TERMS = "terms";
	private static final String ID = "id";
	private static final String GROUPING_KEY = "key";
	private static final String SYN = "syn";
	private static final String FREQUENCY = "freq";
	private static final String SPOTTING_RULE = "rule";
	private static final String TERM_VARIATIONS = "variations";
	private static final String VARIANT_TYPE = "type";
	private static final String INFO = "info";
	private static final String BASE = "base";
	private static final String VARIANT = "variant";
//	private static final String RULE = "rule";
	private static final String FILE = "file";
	private static final String OCCURRENCES = "occurrences";
	private static final String TEXT = "text";
	private static final String INPUT_SOURCES = "input_sources";
	private static final String CONTEXT = "context";
	private static final String CO_OCCURRENCES = "cooccs";
	private static final String NB_COCCS = "cnt";
	private static final String ASSOC_RATE = "assoc_rate";
	private static final String CO_TERM = "co_term";
	private static final String TOTAL_COOCCURRENCES = "total_cooccs";
	private static final String OCCURRENCE_STORAGE = "occurrence_storage";
	private static final String OCCURRENCE_STORE_URL = "occurrence_store_path";
	
	private static final String FREQ_NORM = "f_norm";
	private static final String GENERAL_FREQ_NORM = "gf_norm";
	private static final String NB_WORD_ANNOTATIONS = "wordsNum";
	private static final String NB_SPOTTED_TERMS = "spottedTermsNum";
	
//	private static String WR_FIELD = TermProperty.WR.getShortName();
//	private static String WR_LOG_FIELD = TermProperty.WR_LOG.getShortName();
//	private static String WR_LOG_ZSCORE_FIELD = TermProperty.WR_LOG_Z_SCORE.getShortName();

	/**
	 * Loads the json-serialized term index into the param {@link TermIndex} object.
	 * 
	 * @param reader
	 * @return
	 * @throws JsonParseException
	 * @throws IOException
	 */
	public static TermIndex load(Reader reader, boolean withContext) throws JsonParseException, IOException {
		TermIndex termIndex = null;
		JsonFactory jsonFactory = new JsonFactory(); 
		JsonParser jp = jsonFactory.createParser(reader); // or Stream, Reader
		jp.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
		jp.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
		String fieldname;
		String compLemma = null;
		int fileSource = -1;
		String wordLemma = null;
		String syntacticLabel = null;
		int begin = -1;
		int end = -1;
		int nbWordAnnos = -1;
		int nbSpottedTerms = -1;
		Term b;
		Term v;
		String text;
		String base;
		String variant;
//		String rule;
		String infoToken;
		String variantType;
		Map<Integer, String> inputSources = Maps.newTreeMap();
		
		
		Map<Integer, List<TempVecEntry>> contextVectors = Maps.newHashMap();
		
		
		
		// useful var for debug
		@SuppressWarnings("unused")
		JsonToken tok;
		
		while ((tok = jp.nextToken()) != JsonToken.END_OBJECT) {
			 
			fieldname = jp.getCurrentName();
			if (METADATA.equals(fieldname)) {
				jp.nextToken();
				String termIndexName = null;
				Lang lang = null;
				String corpusID = null;
				String occurrenceStorage = null;
				String occurrenceStorePath = null;

				while ((tok = jp.nextToken()) != JsonToken.END_OBJECT) {
					fieldname = jp.getCurrentName();
					if (LANG.equals(fieldname)) {
						lang = Lang.forName(jp.nextTextValue());
					} else if (NAME.equals(fieldname)) {
						termIndexName = jp.nextTextValue();
					} else if (NB_WORD_ANNOTATIONS.equals(fieldname)) {
						nbWordAnnos = jp.nextIntValue(-1);
					} else if (NB_SPOTTED_TERMS.equals(fieldname)) {
						nbSpottedTerms = jp.nextIntValue(-1);
					} else if (CORPUS_ID.equals(fieldname)) {
						corpusID = jp.nextTextValue();
					} else if (OCCURRENCE_STORAGE.equals(fieldname)) {
						occurrenceStorage = jp.nextTextValue();
					} else if (OCCURRENCE_STORE_URL.equals(fieldname)) {
						occurrenceStorePath = jp.nextTextValue();
					}
				}
				Preconditions.checkState(lang != null, "The property meta.lang must be defined");
				Preconditions.checkState(termIndexName != null, "The property meta.name must be defined");
				
				OccurrenceStore occurrenceStore = new MemoryOccurrenceStore();
				if(occurrenceStorage!= null && occurrenceStorage.equals(OCCURRENCE_STORAGE_FILESTORE)){
					Preconditions.checkNotNull(occurrenceStorePath, "Field %s missing", OCCURRENCE_STORE_URL);
					URL storeURL = new URL(occurrenceStorePath);
					throw new IllegalStateException("Not yet implemented");
				}
				
				termIndex = new MemoryTermIndex(termIndexName, lang, occurrenceStore);
				if(corpusID != null)
					termIndex.setCorpusId(corpusID);
				if(nbWordAnnos != -1)
					termIndex.setWordAnnotationsNum(nbWordAnnos);
				if(nbSpottedTerms != -1)
					termIndex.setSpottedTermsNum(nbSpottedTerms);

			} else if (WORDS.equals(fieldname)) {
				jp.nextToken();
				while ((tok = jp.nextToken()) != JsonToken.END_ARRAY) {
					WordBuilder wordBuilder = WordBuilder.start();
					while ((tok = jp.nextToken()) != JsonToken.END_OBJECT) {
						fieldname = jp.getCurrentName();
						if (LEMMA.equals(fieldname)) 
							wordBuilder.setLemma(jp.nextTextValue());
						else if (COMPOUND_TYPE.equals(fieldname)) 
							wordBuilder.setCompoundType(CompoundType.fromName(jp.nextTextValue()));
						else if (STEM.equals(fieldname)) 
							wordBuilder.setStem(jp.nextTextValue());
						else if (COMPONENTS.equals(fieldname)) {
							while ((tok = jp.nextToken()) != JsonToken.END_ARRAY) {
								while ((tok = jp.nextToken()) != JsonToken.END_OBJECT) {
									fieldname = jp.getCurrentName();
									if (LEMMA.equals(fieldname)) 
										compLemma = jp.nextTextValue();
									else if (BEGIN.equals(fieldname)) 
										begin = jp.nextIntValue(-2);
									else if (END.equals(fieldname)) 
										end = jp.nextIntValue(-2);
								}
								wordBuilder.addComponent(begin, end, compLemma);
							}
						}
					}
					termIndex.addWord(wordBuilder.create());
				}
			} else if (TERMS.equals(fieldname)) {
				jp.nextToken();
				while ((tok = jp.nextToken()) != JsonToken.END_ARRAY) { 
					TermBuilder builder = TermBuilder.start(termIndex);
					List<TempVecEntry> currentContextVector = Lists.newArrayList();
					int currentTermId = -1;
					while ((tok = jp.nextToken()) != JsonToken.END_OBJECT) {
						fieldname = jp.getCurrentName();
						if (GROUPING_KEY.equals(fieldname)) 
							builder.setGroupingKey(jp.nextTextValue());
						else if (SPOTTING_RULE.equals(fieldname)) 
							builder.setSpottingRule(jp.nextTextValue());
						else if (ID.equals(fieldname))  {
							currentTermId = jp.nextIntValue(-2);
							builder.setId(currentTermId);
						} else if (FREQUENCY.equals(fieldname)) {
							builder.setFrequency(jp.nextIntValue(-1));
						} else {
							if (FREQ_NORM.equals(fieldname)) {
								jp.nextToken();
								builder.setFrequencyNorm((double)jp.getFloatValue());
							} else if (GENERAL_FREQ_NORM.equals(fieldname))  {
								jp.nextToken();
								builder.setGeneralFrequencyNorm((double)jp.getFloatValue());
							} else if (WORDS.equals(fieldname)) {
								while ((tok = jp.nextToken()) != JsonToken.END_ARRAY) {
									wordLemma = null;
									syntacticLabel = null;
									while ((tok = jp.nextToken()) != JsonToken.END_OBJECT) {
										fieldname = jp.getCurrentName();
										if (LEMMA.equals(fieldname)) 
											wordLemma = jp.nextTextValue();
										else if (SYN.equals(fieldname)) 
											syntacticLabel = jp.nextTextValue();
									}
									Preconditions.checkArgument(wordLemma != null, MSG_EXPECT_PROP_FOR_TERM_WORD, LEMMA);
									Preconditions.checkArgument(syntacticLabel != null, MSG_EXPECT_PROP_FOR_TERM_WORD, SYN);
									builder.addWord(termIndex.getWord(wordLemma), syntacticLabel);
								}// end words
								
							} else if (OCCURRENCES.equals(fieldname)) {
								while ((tok = jp.nextToken()) != JsonToken.END_ARRAY) {
									begin = -1;
									end = -1;
									fileSource = -1;
									text = null;
									while ((tok = jp.nextToken()) != JsonToken.END_OBJECT) {
										fieldname = jp.getCurrentName();
										if (BEGIN.equals(fieldname)) 
											begin = jp.nextIntValue(-1);
										else if (TEXT.equals(fieldname)) 
											text = jp.nextTextValue();
										else if (END.equals(fieldname)) 
											end = jp.nextIntValue(-1);
										else if (FILE.equals(fieldname)) {
											fileSource = jp.nextIntValue(-1);
										}
									}
									Preconditions.checkArgument(begin != -1, MSG_EXPECT_PROP_FOR_OCCURRENCE, BEGIN);
									Preconditions.checkArgument(end != -1, MSG_EXPECT_PROP_FOR_OCCURRENCE, END);
									Preconditions.checkArgument(fileSource != -1, MSG_EXPECT_PROP_FOR_OCCURRENCE, FILE);
									Preconditions.checkNotNull(inputSources.get(fileSource), "No file source with id: %s", fileSource);
									Preconditions.checkNotNull(text, MSG_EXPECT_PROP_FOR_OCCURRENCE, TEXT);
									builder.addOccurrence(begin, end, termIndex.getDocument(inputSources.get(fileSource)), text);
								} 
							// end occurrences
							} else if (CONTEXT.equals(fieldname)) {
								@SuppressWarnings("unused")
								int totalCooccs = 0;
								while ((tok = jp.nextToken()) != JsonToken.END_OBJECT) {
									fieldname = jp.getCurrentName();
									if (TOTAL_COOCCURRENCES.equals(fieldname)) 
										/*
										 * value never used since the total will 
										 * be reincremented in the contextVector
										 */
										totalCooccs = jp.nextIntValue(-1);
									else if (CO_OCCURRENCES.equals(fieldname)) {
										jp.nextToken();
										while ((tok = jp.nextToken()) != JsonToken.END_ARRAY) {
											TempVecEntry entry = new TempVecEntry();
											while ((tok = jp.nextToken()) != JsonToken.END_OBJECT) {
												fieldname = jp.getCurrentName();
												if (NB_COCCS.equals(fieldname)) 
													entry.setNbCooccs(jp.nextIntValue(-1));
												else if (ASSOC_RATE.equals(fieldname)) {
													jp.nextToken();
													entry.setAssocRate(jp.getFloatValue());
												} else if (CO_TERM.equals(fieldname)) 
													entry.setTermGroupingKey(jp.nextTextValue());
												else if (FILE.equals(fieldname)) {
													fileSource = jp.nextIntValue(-1);
												}
											}
											currentContextVector.add(entry);
										}
									}
								}
							}
						} //end if fieldname
							 

					} // end term object
					builder.createAndAddToIndex();
					if(withContext)
						contextVectors.put(currentTermId, currentContextVector);

				}// end array of terms
				
			} else if (INPUT_SOURCES.equals(fieldname)) {
				jp.nextToken();
				while ((tok = jp.nextToken()) != JsonToken.END_OBJECT) {
					String id = jp.getCurrentName();
					try {
						inputSources.put(Integer.parseInt(id),jp.nextTextValue());
					} catch(NumberFormatException e) {
						IOUtils.closeQuietly(jp);
						throw new IllegalArgumentException("Bad format for input source key: " + id);
					} 
				}
			} else if (TERM_VARIATIONS.equals(fieldname)) {
				jp.nextToken();
				while ((tok = jp.nextToken()) != JsonToken.END_ARRAY) {
					base = null;
					variant = null;
					infoToken = null;
					variantType = null;
					while ((tok = jp.nextToken()) != JsonToken.END_OBJECT) {
						fieldname = jp.getCurrentName();
						if (BASE.equals(fieldname)) 
							base = jp.nextTextValue();
						else if (VARIANT.equals(fieldname)) 
							variant = jp.nextTextValue();
						else if (VARIANT_TYPE.equals(fieldname)) 
							variantType = jp.nextTextValue();
						else if (INFO.equals(fieldname)) 
							infoToken = jp.nextTextValue();
					} // end syntactic variant object
					Preconditions.checkNotNull(base, MSG_EXPECT_PROP_FOR_VAR, BASE);
					Preconditions.checkNotNull(variant, MSG_EXPECT_PROP_FOR_VAR, VARIANT);
					Preconditions.checkNotNull(infoToken, MSG_EXPECT_PROP_FOR_VAR, INFO);
					b = termIndex.getTermByGroupingKey(base);
					Preconditions.checkNotNull(b, MSG_TERM_DOES_NOT_EXIST, base);
					v = termIndex.getTermByGroupingKey(variant);
					Preconditions.checkNotNull(v, MSG_TERM_DOES_NOT_EXIST, variant);
					
					VariationType vType = VariationType.fromShortName(variantType);
					b.addTermVariation(
							v, 
							vType, 
							vType == VariationType.GRAPHICAL ? Double.parseDouble(infoToken) : infoToken);
				} // end syntactic variations array
			}
		}
		jp.close();
		
		if(withContext) {
			/*
			 *  map term ids with terms in context vectors and
			 *  set context vectors
			 */
			List<TempVecEntry> currentTempVecList;
			Term term = null;
			Term coTerm = null;
			ContextVector contextVector;
			for(int termId:contextVectors.keySet()) {
				currentTempVecList = contextVectors.get(termId);
				term = termIndex.getTermById(termId);
				contextVector = new ContextVector(term);
				for(TempVecEntry tempVecEntry:currentTempVecList) {
					coTerm = termIndex.getTermByGroupingKey(tempVecEntry.getTermGroupingKey());
					contextVector.addEntry(coTerm, tempVecEntry.getNbCooccs(), tempVecEntry.getAssocRate());
				}
				if(!contextVector.getEntries().isEmpty())
					term.setContextVector(contextVector);
			}
		}

		return termIndex;
	}

	public static void save(Writer writer, TermIndex termIndex, boolean withOccurrences, boolean withContexts) throws IOException {
		JsonFactory jsonFactory = new JsonFactory(); // or, for data binding, org.codehaus.jackson.mapper.MappingJsonFactory 
//		jsonFactory.configure(f, state)
		JsonGenerator jg = jsonFactory.createGenerator(writer); // or Stream, Reader
		jg.useDefaultPrettyPrinter();
		
		jg.writeStartObject();

		jg.writeFieldName(METADATA);
		jg.writeStartObject();
		jg.writeFieldName(NAME);
		jg.writeString(termIndex.getName());
		jg.writeFieldName(LANG);
		jg.writeString(termIndex.getLang().getCode());
		if(termIndex.getCorpusId() != null) {
			jg.writeFieldName(CORPUS_ID);
			jg.writeString(termIndex.getCorpusId());
		}
		
		jg.writeFieldName(NB_WORD_ANNOTATIONS);
		jg.writeNumber(termIndex.getWordAnnotationsNum());
		jg.writeFieldName(NB_SPOTTED_TERMS);
		jg.writeNumber(termIndex.getSpottedTermsNum());

		jg.writeEndObject();
		
		jg.writeFieldName(INPUT_SOURCES);
		int idCnt = 0;
		Map<String, Integer> inputSources = Maps.newTreeMap();
		for(Document d:termIndex.getDocuments())
			if(!inputSources.containsKey(d.getUrl()))
				inputSources.put(d.getUrl(), ++idCnt);
		jg.writeStartObject();
		for(String uri:inputSources.keySet()) {
			jg.writeFieldName(inputSources.get(uri).toString());
			jg.writeString(uri);
		}
		jg.writeEndObject();
		
		jg.writeFieldName(WORDS);
		jg.writeStartArray();
		for(Word w:termIndex.getWords()) {
			jg.writeStartObject();
			jg.writeFieldName(LEMMA);
			jg.writeString(w.getLemma());
			jg.writeFieldName(STEM);
			jg.writeString(w.getStem());
			if(w.isCompound()) {
				jg.writeFieldName(COMPOUND_TYPE);
				jg.writeString(w.getCompoundType().name());
				jg.writeFieldName(COMPONENTS);
				jg.writeStartArray();
				for(Component c:w.getComponents()) {
					jg.writeStartObject();
					jg.writeFieldName(LEMMA);
					jg.writeString(c.getLemma());
					jg.writeFieldName(BEGIN);
					jg.writeNumber(c.getBegin());
					jg.writeFieldName(END);
					jg.writeNumber(c.getEnd());
					jg.writeEndObject();
				}
				jg.writeEndArray();
			}
			
			jg.writeEndObject();
		}
		jg.writeEndArray();
		
		Set<TermVariation> termVariations = Sets.newHashSet();
		
		jg.writeFieldName(TERMS);
		jg.writeStartArray();
		for(Term t:termIndex.getTerms()) {
			termVariations.addAll(t.getVariations());
			
			jg.writeStartObject();
			jg.writeFieldName(ID);
			jg.writeNumber(t.getId());
			jg.writeFieldName(GROUPING_KEY);
			jg.writeString(t.getGroupingKey());
			jg.writeFieldName(WORDS);
			jg.writeStartArray();
			for(TermWord tw:t.getWords()) {
				jg.writeStartObject();
				jg.writeFieldName(SYN);
				jg.writeString(tw.getSyntacticLabel());
				jg.writeFieldName(LEMMA);
				jg.writeString(tw.getWord().getLemma());
				jg.writeEndObject();
			}
			jg.writeEndArray();
			
			jg.writeFieldName(FREQUENCY);
			jg.writeNumber(t.getFrequency());
			jg.writeFieldName(FREQ_NORM);
			jg.writeNumber(t.getFrequencyNorm());
			jg.writeFieldName(GENERAL_FREQ_NORM);
			jg.writeNumber(t.getGeneralFrequencyNorm());
			jg.writeFieldName(SPOTTING_RULE);
			jg.writeString(t.getSpottingRule());
			
			if(withOccurrences) {
				jg.writeFieldName(OCCURRENCES);
				jg.writeStartArray();
				for(TermOccurrence termOcc:t.getOccurrences()) {
					jg.writeStartObject();
					jg.writeFieldName(BEGIN);
					jg.writeNumber(termOcc.getBegin());
					jg.writeFieldName(END);
					jg.writeNumber(termOcc.getEnd());
					jg.writeFieldName(TEXT);
					jg.writeString(termOcc.getCoveredText());
					jg.writeFieldName(FILE);
					jg.writeNumber(inputSources.get(termOcc.getSourceDocument().getUrl()));
					jg.writeEndObject();
				}
				jg.writeEndArray();
			}
			
			if(withContexts && t.isContextVectorComputed()) {
				jg.writeFieldName(CONTEXT);
				jg.writeStartObject();
				
				jg.writeFieldName(TOTAL_COOCCURRENCES);
				jg.writeNumber(t.getContextVector().getTotalCoccurrences());
				jg.writeFieldName(CO_OCCURRENCES);
				jg.writeStartArray();
				if(t.isContextVectorComputed()) {
					for(ContextVector.Entry contextEntry:t.getContextVector().getEntries()) {
						jg.writeStartObject();
						jg.writeFieldName(CO_TERM);
						jg.writeString(contextEntry.getCoTerm().getGroupingKey());
						jg.writeFieldName(NB_COCCS);
						jg.writeNumber(contextEntry.getNbCooccs());
						jg.writeFieldName(ASSOC_RATE);
						jg.writeNumber(contextEntry.getAssocRate());
						jg.writeEndObject();
					}
				}
				jg.writeEndArray();
				jg.writeEndObject();
			}
			
			jg.writeEndObject();
		}
		jg.writeEndArray();
		
		/* Variants */
		jg.writeFieldName(TERM_VARIATIONS);
		jg.writeStartArray();
		for(TermVariation v:termVariations) {
			jg.writeStartObject();
			jg.writeFieldName(BASE);
			jg.writeString(v.getBase().getGroupingKey());
			jg.writeFieldName(VARIANT);
			jg.writeString(v.getVariant().getGroupingKey());
			jg.writeFieldName(VARIANT_TYPE);
			jg.writeString(v.getVariationType().getShortName());
			jg.writeFieldName(INFO);
			jg.writeString(v.getInfo().toString());
			jg.writeEndObject();
		}
		jg.writeEndArray();
		
		jg.writeEndObject();
		jg.close();
	}
	
	private static class TempVecEntry {
		String termGroupingKey;
		double assocRate;
		int nbCooccs;
		public String getTermGroupingKey() {
			return termGroupingKey;
		}
		public void setTermGroupingKey(String termGroupingKey) {
			this.termGroupingKey = termGroupingKey;
		}
		public double getAssocRate() {
			return assocRate;
		}
		public void setAssocRate(double assocRate) {
			this.assocRate = assocRate;
		}
		public int getNbCooccs() {
			return nbCooccs;
		}
		public void setNbCooccs(int nbCooccs) {
			this.nbCooccs = nbCooccs;
		}
	}
}
