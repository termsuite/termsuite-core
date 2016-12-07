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
package fr.univnantes.termsuite.model.termino;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import fr.univnantes.termsuite.api.JsonOptions;
import fr.univnantes.termsuite.model.Component;
import fr.univnantes.termsuite.model.CompoundType;
import fr.univnantes.termsuite.model.ContextVector;
import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermBuilder;
import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.model.TermOccurrence;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.model.WordBuilder;
import fr.univnantes.termsuite.model.occurrences.MemoryOccurrenceStore;
import fr.univnantes.termsuite.model.occurrences.MongoDBOccurrenceStore;

public class JsonTermIndexIO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JsonTermIndexIO.class);
	
	/*
	 * Error messages for parsing
	 */
	private static final String MSG_EXPECT_PROP_FOR_VAR = "Expecting %s property for term variation";
	private static final String MSG_EXPECT_PROP_FOR_OCCURRENCE = "Expecting %s property for occurrence";
	private static final String MSG_EXPECT_PROP_FOR_TERM_WORD = "Expecting %s property for term word";

	/*
	 * Occurrence storing options
	 */
	private static final String OCCURRENCE_STORAGE_EMBEDDED = "embedded";
	private static final String OCCURRENCE_STORAGE_MONGODB = "MongoDB";
	
	/*
	 * Json properties
	 */
	
	/*
	 * Term data model
	 */
	private static final String TERM_WORDS = "words";
	private static final String TERM_OCCURRENCES = "occurrences";
	private static final String TERM_CONTEXT = "context";
	
	/*
	 * Term properties
	 */
	private static final String TERM_GROUPING_KEY = "key";
	private static final String TERM_PILOT = "pilot";
	private static final String TERM_DOCUMENT_FREQUENCY = "dfreq";
	private static final String TERM_FREQUENCY = "freq";
	private static final String TERM_SPOTTING_RULE = "rule";
	private static final String TERM_RANK = "rank";
	private static final String TERM_SPECIFICITY = "spec";
	private static final String TERM_FREQ_NORM = "f_norm";
	private static final String TERM_GENERAL_FREQ_NORM = "gf_norm";

	
	@Deprecated
	private static final String TERM_VARIATIONS = "variations";
	private static final String TERM_RELATIONS = "relations";
	private static final String METADATA = "metadata";
	private static final String LANG = "lang";
	private static final String NAME = "name";
	private static final String CORPUS_ID = "corpus-id";
	private static final String LEMMA = "lemma";
	private static final String SUBSTRING = "substring";
	private static final String STEM = "stem";
	private static final String COMPOUND_TYPE = "compound_type";
	private static final String COMPOUND_NEOCLASSICAL_AFFIX = "neoAffix";
	private static final String COMPONENTS = "components";
	private static final String BEGIN = "begin";
	private static final String END = "end";
	private static final String TERMS = "terms";
	private static final String ID = "id";
	private static final String SYN = "syn";
	private static final String RELATION_TYPE = "type";
	private static final String IS_SWT = "swt";
	
	@Deprecated
	private static final String BASE = "base";
	private static final String FROM = "from";
	
	@Deprecated
	private static final String VARIANT = "variant";
	private static final String TO = "to";

	private static final String VARIANT_SCORE = "vscore";

	private static final String TEXT = "text";
	private static final String INPUT_SOURCES = "input_sources";
	private static final String FILE = "file";
	private static final String CO_OCCURRENCES = "cooccs";
	private static final String NB_COCCS = "cnt";
	private static final String ASSOC_RATE = "assoc_rate";
	private static final String CO_TERM = "co_term";
	private static final String TOTAL_COOCCURRENCES = "total_cooccs";
	private static final String OCCURRENCE_STORAGE = "occurrence_storage";
	private static final String OCCURRENCE_MONGODB_STORE_URI = "occurrence_store_mongodb_uri";

	private static final String NB_WORD_ANNOTATIONS = "wordsNum";
	private static final String NB_SPOTTED_TERMS = "spottedTermsNum";
	
	
	/**
	 * Loads the json-serialized term index into the param {@link TermIndex} object.
	 * 
	 * @param reader
	 * @param options
	 * 			The deserialization {@link IOOptions}.
	 * @return
	 * @throws JsonParseException
	 * @throws IOException
	 */
	public static TermIndex load(Reader reader, JsonOptions options) throws JsonParseException, IOException {
		TermIndex termIndex = null;
		JsonFactory jsonFactory = new JsonFactory(); 
		JsonParser jp = jsonFactory.createParser(reader); // or Stream, Reader
		jp.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
		jp.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
		String fieldname;
		String compLemma = null;
		String substring = null;
		int fileSource = -1;
		String wordLemma = null;
		boolean isSWT;
		String syntacticLabel = null;
		boolean neoclassicalAffix = false;
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
		Object infoToken;
		String variantType;
		Double variantScore;

		Map<Integer, String> inputSources = Maps.newTreeMap();
		
		
		Map<String, List<TempVecEntry>> contextVectors = Maps.newHashMap();
		
		
		OccurrenceStore occurrenceStore = null;
		
		// useful var for debug
		JsonToken tok;

		while ((tok = jp.nextToken()) != JsonToken.END_OBJECT) {
			 
			fieldname = jp.getCurrentName();
			if (METADATA.equals(fieldname)) {
				jp.nextToken();
				String termIndexName = null;
				Lang lang = null;
				String corpusID = null;
				String occurrenceStorage = null;
				String occurrenceStoreURI = null;

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
					} else if (OCCURRENCE_MONGODB_STORE_URI.equals(fieldname)) {
						occurrenceStoreURI = jp.nextTextValue();
					}
				}
				Preconditions.checkState(lang != null, "The property meta.lang must be defined");
				Preconditions.checkState(termIndexName != null, "The property meta.name must be defined");
				
				if(occurrenceStorage != null && occurrenceStorage.equals(OCCURRENCE_STORAGE_MONGODB)) {
					Preconditions.checkNotNull(occurrenceStoreURI, "Missing attribute " + OCCURRENCE_MONGODB_STORE_URI);
					occurrenceStore = new MongoDBOccurrenceStore(occurrenceStoreURI, OccurrenceStore.State.INDEXED);
				} else
					occurrenceStore = new MemoryOccurrenceStore();
				
				termIndex = new MemoryTermIndex(termIndexName, lang, occurrenceStore);
				if(corpusID != null)
					termIndex.setCorpusId(corpusID);
				if(nbWordAnnos != -1)
					termIndex.setWordAnnotationsNum(nbWordAnnos);
				if(nbSpottedTerms != -1)
					termIndex.setSpottedTermsNum(nbSpottedTerms);
				
				if(options.isMetadataOnly())
					return termIndex;

			} else if (TERM_WORDS.equals(fieldname)) {
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
									else if (SUBSTRING.equals(fieldname)) 
										substring = jp.nextTextValue();
									else if (BEGIN.equals(fieldname)) 
										begin = jp.nextIntValue(-2);
									else if (COMPOUND_NEOCLASSICAL_AFFIX.equals(fieldname)) 
										neoclassicalAffix = jp.nextBooleanValue();
									else if (END.equals(fieldname)) 
										end = jp.nextIntValue(-2);
								}
								wordBuilder.addComponent(begin, end, substring, compLemma, neoclassicalAffix);
							}
						}
					}
					try {
						termIndex.addWord(wordBuilder.create());
					} catch(Exception e) {
						LOGGER.error("Could not add word "+wordBuilder.getLemma()+" to term index",e);
						LOGGER.warn("Error ignored, trying ton continue the loading of TermIndex");
					}
				}
			} else if (TERMS.equals(fieldname)) {
				jp.nextToken();
				while ((tok = jp.nextToken()) != JsonToken.END_ARRAY) { 
					TermBuilder builder = TermBuilder.start(termIndex);
					List<TempVecEntry> currentContextVector = Lists.newArrayList();
					String currentGroupingKey = null;
					while ((tok = jp.nextToken()) != JsonToken.END_OBJECT) {
						fieldname = jp.getCurrentName();
						if (TERM_GROUPING_KEY.equals(fieldname)) {
							currentGroupingKey = jp.nextTextValue();
							builder.setGroupingKey(currentGroupingKey);
						} else if (TERM_PILOT.equals(fieldname)) 
							builder.setPilot(jp.nextTextValue());
						else if (TERM_DOCUMENT_FREQUENCY.equals(fieldname)) 
							builder.setDocumentFrequency(jp.nextIntValue(-1));
						else if (TERM_SPOTTING_RULE.equals(fieldname)) 
							builder.setSpottingRule(jp.nextTextValue());
						else if (ID.equals(fieldname))  {
							// term ids are deprecated
						} else if (TERM_RANK.equals(fieldname)) {
							builder.setRank(jp.nextIntValue(-1));
						} else if (TERM_FREQUENCY.equals(fieldname)) {
							builder.setFrequency(jp.nextIntValue(-1));
						} else {
							if (TERM_FREQ_NORM.equals(fieldname)) {
								jp.nextToken();
								builder.setFrequencyNorm((double)jp.getFloatValue());
							} else if (TERM_SPECIFICITY.equals(fieldname))  {
								jp.nextToken();
								builder.setSpecificity((double)jp.getDoubleValue());
							} else if (TERM_GENERAL_FREQ_NORM.equals(fieldname))  {
								jp.nextToken();
								builder.setGeneralFrequencyNorm((double)jp.getFloatValue());
							} else if (TERM_WORDS.equals(fieldname)) {
								while ((tok = jp.nextToken()) != JsonToken.END_ARRAY) {
									wordLemma = null;
									syntacticLabel = null;
									isSWT = false;
									while ((tok = jp.nextToken()) != JsonToken.END_OBJECT) {
										fieldname = jp.getCurrentName();
										if (LEMMA.equals(fieldname)) 
											wordLemma = jp.nextTextValue();
										else if (IS_SWT.equals(fieldname)) 
											isSWT = jp.nextBooleanValue();
										else if (SYN.equals(fieldname)) 
											syntacticLabel = jp.nextTextValue();
									}
									Preconditions.checkArgument(wordLemma != null, MSG_EXPECT_PROP_FOR_TERM_WORD, LEMMA);
									Preconditions.checkArgument(syntacticLabel != null, MSG_EXPECT_PROP_FOR_TERM_WORD, SYN);
									builder.addWord(termIndex.getWord(wordLemma), syntacticLabel, isSWT);
								}// end words
								
							} else if (TERM_OCCURRENCES.equals(fieldname)) {
								tok = jp.nextToken();
								if(tok == JsonToken.START_ARRAY) {
									
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
										if(occurrenceStore.getStoreType() == OccurrenceStore.Type.MEMORY)
											builder.addOccurrence(begin, end, inputSources.get(fileSource), text);
									} 
								}
							// end occurrences
							} else if (TERM_CONTEXT.equals(fieldname)) {
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
					try {
						builder.createAndAddToIndex();
					} catch(Exception e) {
						LOGGER.error("Could not add term "+currentGroupingKey+" to term index",e);
						LOGGER.warn("Error ignored, trying ton continue the loading of TermIndex");
					}

					if(options.isWithContexts())
						contextVectors.put(currentGroupingKey, currentContextVector);

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
			} else if (TERM_VARIATIONS.equals(fieldname) || TERM_RELATIONS.equals(fieldname)) {
				jp.nextToken();
				while ((tok = jp.nextToken()) != JsonToken.END_ARRAY) {
					base = null;
					variant = null;
					variantType = null;
					variantScore = null;
					Map<RelationProperty, Comparable<?>> properties = Maps.newHashMap();
					while ((tok = jp.nextToken()) != JsonToken.END_OBJECT) {
						fieldname = jp.getCurrentName();
						if (BASE.equals(fieldname) || FROM.equals(fieldname)) 
							base = jp.nextTextValue();
						else if (VARIANT.equals(fieldname) || TO.equals(fieldname)) 
							variant = jp.nextTextValue();
						else if (RELATION_TYPE.equals(fieldname)) 
							variantType = jp.nextTextValue();
						else if (VARIANT_SCORE.equals(fieldname)) {
							jp.nextToken();
							variantScore = jp.getDoubleValue();
						} else if(RelationProperty.fromJsonField(fieldname) != null){
							RelationProperty p = RelationProperty.fromJsonField(fieldname);
							JsonToken token = jp.nextToken();
							switch(token) {
							case VALUE_NUMBER_FLOAT:
								properties.put(p, jp.getDoubleValue());
								break;
							case VALUE_NUMBER_INT:
								properties.put(p, jp.getIntValue());
								break;
							case VALUE_STRING:
								properties.put(p, jp.getValueAsString());
								break;
							case VALUE_TRUE:
							case VALUE_FALSE:
								properties.put(p, jp.getValueAsBoolean());
								break;
							default:
								LOGGER.info("Unsupported property range for json token " + token);
							}
							
						}
					} // end syntactic variant object
					Preconditions.checkNotNull(base, MSG_EXPECT_PROP_FOR_VAR, FROM);
					Preconditions.checkNotNull(variant, MSG_EXPECT_PROP_FOR_VAR, TO);
					b = termIndex.getTermByGroupingKey(base);
					v = termIndex.getTermByGroupingKey(variant);
					if(b != null && v != null) {
						
						RelationType vType = RelationType.fromShortName(variantType);
						
						TermRelation tv = new TermRelation(
								vType, 
								b, 
								v);
						
						for(Map.Entry<RelationProperty, Comparable<?>> e:properties.entrySet())
							tv.setProperty(e.getKey(), e.getValue());
						
						if(variantScore != null)
							tv.setProperty(RelationProperty.VARIANT_SCORE, variantScore);
						termIndex.addRelation(tv);
					} else {
						if(b==null)
							LOGGER.warn("Could not build variant because term \"{}\" was not found.", base);
						if(v==null)
							LOGGER.warn("Could not build variant because term \"{}\" was not found.", variant);
					}
						
//					Preconditions.checkNotNull(b, MSG_TERM_DOES_NOT_EXIST, base);
//					Preconditions.checkNotNull(v, MSG_TERM_DOES_NOT_EXIST, variant);
					
				} // end syntactic variations array
			}
		}
		jp.close();
		
		if(options.isWithContexts()) {
			/*
			 *  map term ids with terms in context vectors and
			 *  set context vectors
			 */
			List<TempVecEntry> currentTempVecList;
			Term term = null;
			Term coTerm = null;
			ContextVector contextVector;
			for(String groupingKey:contextVectors.keySet()) {
				currentTempVecList = contextVectors.get(groupingKey);
				term = termIndex.getTermByGroupingKey(groupingKey);
				contextVector = new ContextVector(term);
				for(TempVecEntry tempVecEntry:currentTempVecList) {
					coTerm = termIndex.getTermByGroupingKey(tempVecEntry.getTermGroupingKey());
					contextVector.addEntry(coTerm, tempVecEntry.getNbCooccs(), tempVecEntry.getAssocRate());
				}
				if(!contextVector.getEntries().isEmpty())
					term.setContext(contextVector);
			}
		}

		return termIndex;
	}

	public static void save(Writer writer, TermIndex termIndex, JsonOptions options) throws IOException {
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
		
		jg.writeFieldName(OCCURRENCE_STORAGE);
		if(options.isMongoDBOccStore()) {
			jg.writeString(OCCURRENCE_STORAGE_MONGODB);
			jg.writeFieldName(OCCURRENCE_MONGODB_STORE_URI);
			jg.writeString(options.getMongoDBOccStore());
		} else if(options.isEmbeddedOccurrences())
			jg.writeString(OCCURRENCE_STORAGE_EMBEDDED);
		else
			throw new IllegalStateException("Unknown storage mode");

		jg.writeFieldName(NB_WORD_ANNOTATIONS);
		jg.writeNumber(termIndex.getWordAnnotationsNum());
		jg.writeFieldName(NB_SPOTTED_TERMS);
		jg.writeNumber(termIndex.getSpottedTermsNum());

		jg.writeEndObject();
		
		jg.writeFieldName(INPUT_SOURCES);
		int idCnt = 0;
		Map<String, Integer> inputSources = Maps.newTreeMap();
		for(Document d:termIndex.getOccurrenceStore().getDocuments())
			if(!inputSources.containsKey(d.getUrl()))
				inputSources.put(d.getUrl(), ++idCnt);
		jg.writeStartObject();
		for(String uri:inputSources.keySet()) {
			jg.writeFieldName(inputSources.get(uri).toString());
			jg.writeString(uri);
		}
		jg.writeEndObject();
		
		jg.writeFieldName(TERM_WORDS);
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
					jg.writeFieldName(COMPOUND_NEOCLASSICAL_AFFIX);
					jg.writeBoolean(c.isNeoclassicalAffix());
					jg.writeEndObject();
				}
				jg.writeEndArray();
			}
			
			jg.writeEndObject();
		}
		jg.writeEndArray();
		
		
		jg.writeFieldName(TERMS);
		jg.writeStartArray();
		for(Term t:termIndex.getTerms()) {
			jg.writeStartObject();
			
			Preconditions.checkState(t.isPropertySet(TermProperty.GROUPING_KEY));
			jg.writeFieldName(TERM_GROUPING_KEY);
			jg.writeString(t.getGroupingKey());
			
			for(TermProperty p:t.getProperties().keySet()) {
				if(p == TermProperty.GROUPING_KEY)
					continue;
				jg.writeFieldName(p.getJsonField());
				jg.writeObject(t.getPropertyValue(p));
			}
			jg.writeFieldName(TERM_WORDS);
			jg.writeStartArray();
			for(TermWord tw:t.getWords()) {
				jg.writeStartObject();
				jg.writeFieldName(SYN);
				jg.writeString(tw.getSyntacticLabel());
				jg.writeFieldName(IS_SWT);
				jg.writeBoolean(tw.isSwt());
				jg.writeFieldName(LEMMA);
				jg.writeString(tw.getWord().getLemma());
				jg.writeEndObject();
			}
			jg.writeEndArray();
			
			if(options.withOccurrences() && options.isEmbeddedOccurrences()) {
				jg.writeFieldName(TERM_OCCURRENCES);
				jg.writeStartArray();
				for(TermOccurrence termOcc:termIndex.getOccurrenceStore().getOccurrences(t)) {
					jg.writeStartObject();
					jg.writeFieldName(BEGIN);
					jg.writeNumber(termOcc.getBegin());
					jg.writeFieldName(END);
					jg.writeNumber(termOcc.getEnd());
					jg.writeFieldName(TEXT);
					jg.writeString(termOcc.getForm().getText());
					jg.writeFieldName(FILE);
					jg.writeNumber(inputSources.get(termOcc.getSourceDocument().getUrl()));
					jg.writeEndObject();
				}
				jg.writeEndArray();
			}
			
			if(options.isWithContexts() && t.getContext() != null) {
				jg.writeFieldName(TERM_CONTEXT);
				jg.writeStartObject();
				
				jg.writeFieldName(TOTAL_COOCCURRENCES);
				jg.writeNumber(t.getContext().getTotalCoccurrences());
				jg.writeFieldName(CO_OCCURRENCES);
				jg.writeStartArray();
				if(t.getContext() != null) {
					for(ContextVector.Entry contextEntry:t.getContext().getEntries()) {
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
		jg.writeFieldName(TERM_RELATIONS);
		jg.writeStartArray();
		for(TermRelation relation:termIndex.getRelations().collect(Collectors.toList())) {
			jg.writeStartObject();
			jg.writeFieldName(FROM);
			jg.writeString(relation.getFrom().getGroupingKey());
			jg.writeFieldName(TO);
			jg.writeString(relation.getTo().getGroupingKey());
			jg.writeFieldName(RELATION_TYPE);
			jg.writeString(relation.getType().getShortName());
			for(RelationProperty p:relation.getProperties().keySet()) {
				jg.writeFieldName(p.getJsonField());
				jg.writeObject(relation.getPropertyValue(p));
			}
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
