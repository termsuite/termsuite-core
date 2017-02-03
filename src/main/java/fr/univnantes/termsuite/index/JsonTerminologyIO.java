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
package fr.univnantes.termsuite.index;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

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

import fr.univnantes.termsuite.engines.gatherer.PropertyValue;
import fr.univnantes.termsuite.export.json.JsonOptions;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.model.Component;
import fr.univnantes.termsuite.model.CompoundType;
import fr.univnantes.termsuite.model.ContextVector;
import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.Property;
import fr.univnantes.termsuite.model.PropertyHolder;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermBuilder;
import fr.univnantes.termsuite.model.TermOccurrence;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.model.WordBuilder;

public class JsonTerminologyIO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JsonTerminologyIO.class);
	
	/*
	 * Error messages for parsing
	 */
	private static final String MSG_EXPECT_PROP_FOR_VAR = "Expecting %s property for term variation";
	private static final String MSG_EXPECT_PROP_FOR_OCCURRENCE = "Expecting %s property for occurrence";
	private static final String MSG_EXPECT_PROP_FOR_TERM_WORD = "Expecting %s property for term word";
	private static final String MSG_NO_GROUPING_KEY_SET = "No GROUPING_KEY set for term";
	private static final String MSG_NO_FILE_SOURCE_WITH_ID = "No file source with id: %s";
	private static final String MSG_NO_GKEY_FOR_TERM = "No grouping found for current term.";
	private static final String MSG_WORD_NOT_FOUND = "No such word: %s";
	private static final String MSG_NO_SUCH_TERM_IN_TERMINO = "No such term in terminology: %s";


	/*
	 * Occurrence storing options
	 */
	private static final String OCCURRENCE_STORAGE_EMBEDDED = "embedded";
	private static final String OCCURRENCE_STORAGE_DISK = "disk";
	
	
	/*
	 * Json properties
	 */
	
	/*
	 * Term data model
	 */
	private static final String TERM_WORDS = "words";
	private static final String TERM_OCCURRENCES = "occurrences";
	private static final String TERM_CONTEXT = "context";
	
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
	private static final String TERM_ID = "tid";
	private static final String END = "end";
	private static final String TERMS = "terms";
	private static final String SYN = "syn";
	private static final String RELATION_TYPE = "type";
	private static final String PROPERTIES = "props";
	private static final String IS_SWT = "swt";
	
	private static final String FROM = "from";
	
	private static final String TO = "to";

	private static final String TEXT = "text";
	private static final String INPUT_SOURCES = "input_sources";
	private static final String FILE = "file";
	private static final String CO_OCCURRENCES = "cooccs";
	private static final String NB_COCCS = "cnt";
	private static final String ASSOC_RATE = "assoc_rate";
	private static final String CO_TERM = "co_term";
	private static final String TOTAL_COOCCURRENCES = "total_cooccs";
	private static final String OCCURRENCE_STORAGE = "occurrence_storage";
	private static final String OCCURRENCE_PERSITENT_STORE_PATH = "persistent_store_path";

	private static final String NB_WORD_ANNOTATIONS = "wordsNum";
	private static final String NB_SPOTTED_TERMS = "spottedTermsNum";

	
	
	/**
	 * Loads the json-serialized term index into the param {@link Terminology} object.
	 * 
	 * @param reader
	 * @param options
	 * 			The deserialization {@link IOOptions}.
	 * @return
	 * @throws JsonParseException
	 * @throws IOException
	 */
	public static IndexedCorpus load(Reader reader, JsonOptions options) throws JsonParseException, IOException {
		Terminology termino = null;
		OccurrenceStore occurrenceStore = null;
		IndexedCorpus indexedCorpus = null;
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
		String relationType;

		Map<Integer, String> inputSources = Maps.newTreeMap();
		
		
		Map<String, List<TempVecEntry>> contextVectors = Maps.newHashMap();
		
		
		// useful var for debug
		JsonToken tok;

		Lang lang = null;
		
		while ((tok = jp.nextToken()) != JsonToken.END_OBJECT) {
			 
			fieldname = jp.getCurrentName();
			if (METADATA.equals(fieldname)) {
				jp.nextToken();
				String terminoName = null;
				String corpusID = null;
				String occurrenceStorage = null;
				String persitentStorePath = null;

				while ((tok = jp.nextToken()) != JsonToken.END_OBJECT) {
					fieldname = jp.getCurrentName();
					if (LANG.equals(fieldname)) {
						lang = Lang.forName(jp.nextTextValue());
					} else if (NAME.equals(fieldname)) {
						terminoName = jp.nextTextValue();
					} else if (NB_WORD_ANNOTATIONS.equals(fieldname)) {
						nbWordAnnos = jp.nextIntValue(-1);
					} else if (NB_SPOTTED_TERMS.equals(fieldname)) {
						nbSpottedTerms = jp.nextIntValue(-1);
					} else if (CORPUS_ID.equals(fieldname)) {
						corpusID = jp.nextTextValue();
					} else if (OCCURRENCE_STORAGE.equals(fieldname)) {
						occurrenceStorage = jp.nextTextValue();
					} else if (OCCURRENCE_PERSITENT_STORE_PATH.equals(fieldname)) {
						persitentStorePath = jp.nextTextValue();
					}
				}
				Preconditions.checkState(lang != null, "The property meta.lang must be defined");
				Preconditions.checkState(terminoName != null, "The property meta.name must be defined");
				
				if(occurrenceStorage != null && occurrenceStorage.equals(OCCURRENCE_STORAGE_DISK)) {
					Preconditions.checkNotNull(persitentStorePath, "Missing attribute " + OCCURRENCE_PERSITENT_STORE_PATH);
					Preconditions.checkNotNull(lang, "Missing metadata attribute " + LANG);
					occurrenceStore = TermSuiteFactory.createPersitentOccurrenceStore(persitentStorePath, lang);
				} else {
					Preconditions.checkNotNull(lang, "Missing metadata attribute " + LANG);
					occurrenceStore = TermSuiteFactory.createMemoryOccurrenceStore(lang);
				}
				termino = TermSuiteFactory.createTerminology(lang, terminoName);
				if(corpusID != null)
					termino.setCorpusId(corpusID);
				if(nbWordAnnos != -1)
					termino.setNbWordAnnotations(new AtomicLong(nbWordAnnos));
				if(nbSpottedTerms != -1)
					termino.setNbSpottedTerms(new AtomicLong(nbSpottedTerms));
				
				indexedCorpus = new IndexedCorpus(termino, occurrenceStore);
				if(options.isMetadataOnly()) 
					return indexedCorpus;

			} else if (TERM_WORDS.equals(fieldname)) {
				jp.nextToken();
				while ((tok = jp.nextToken()) != JsonToken.END_ARRAY) {
					WordBuilder wordBuilder = WordBuilder.start(termino);
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
					Word word = wordBuilder.create();
					termino.getWords().put(word.getLemma(), word);
				}
			} else if (TERMS.equals(fieldname)) {
				jp.nextToken();
				while ((tok = jp.nextToken()) != JsonToken.END_ARRAY) { 
					TermBuilder builder = TermBuilder.start(termino);
					List<TempVecEntry> currentContextVector = Lists.newArrayList();
					Map<TermProperty, Comparable<?>> properties = null;
					String currentGroupingKey = null;
					while ((tok = jp.nextToken()) != JsonToken.END_OBJECT) {
						fieldname = jp.getCurrentName();
						if (PROPERTIES.equals(fieldname)) {
							properties = readProperties(TermProperty.class, jp);
							Preconditions.checkState(properties.containsKey(TermProperty.GROUPING_KEY), MSG_NO_GROUPING_KEY_SET);
							currentGroupingKey = (String)properties.get(TermProperty.GROUPING_KEY);
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
								Preconditions.checkState(wordLemma != null, MSG_EXPECT_PROP_FOR_TERM_WORD, LEMMA);
								Preconditions.checkState(termino.getWords().containsKey(wordLemma), MSG_WORD_NOT_FOUND, wordLemma);
								Preconditions.checkState(syntacticLabel != null, MSG_EXPECT_PROP_FOR_TERM_WORD, SYN);
								builder.addWord(termino.getWords().get(wordLemma), syntacticLabel, isSWT);
							}
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
						} else
							throw new IllegalStateException("Unexpected field name for term: " + fieldname);
						//end if fieldname
							 
					} // end term object
					Preconditions.checkState(currentGroupingKey != null, MSG_NO_GKEY_FOR_TERM);
					Term t = builder.create();
					t.setProperties(properties);
					termino.getTerms().put(t.getGroupingKey(), t);

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
			} else if (TERM_RELATIONS.equals(fieldname)) {
				jp.nextToken();
				while ((tok = jp.nextToken()) != JsonToken.END_ARRAY) {
					base = null;
					variant = null;
					relationType = null;
					Map<RelationProperty,Comparable<?>> properties = new HashMap<>();
					while ((tok = jp.nextToken()) != JsonToken.END_OBJECT) {
						fieldname = jp.getCurrentName();
						if (FROM.equals(fieldname)) 
							base = jp.nextTextValue();
						else if (TO.equals(fieldname)) 
							variant = jp.nextTextValue();
						else if (RELATION_TYPE.equals(fieldname)) 
							relationType = jp.nextTextValue();
						else if (PROPERTIES.equals(fieldname)) 
							properties = readProperties(RelationProperty.class, jp);
					}
					
					Preconditions.checkNotNull(base, MSG_EXPECT_PROP_FOR_VAR, FROM);
					Preconditions.checkNotNull(variant, MSG_EXPECT_PROP_FOR_VAR, TO);
					b = termino.getTerms().get(base);
					v = termino.getTerms().get(variant);
					if(b != null && v != null) {
						
						RelationType vType = RelationType.fromShortName(relationType);
						
						Relation tv = new Relation(
								vType, 
								b, 
								v);
						tv.setProperties(properties);
						termino.getRelations().add(tv);
					} else {
						if(b==null)
							LOGGER.warn("Could not build variant because term \"{}\" was not found.", base);
						if(v==null)
							LOGGER.warn("Could not build variant because term \"{}\" was not found.", variant);
					}
						
//					Preconditions.checkNotNull(b, MSG_TERM_DOES_NOT_EXIST, base);
//					Preconditions.checkNotNull(v, MSG_TERM_DOES_NOT_EXIST, variant);
					
				} // end syntactic variations array
			} else if (TERM_OCCURRENCES.equals(fieldname)) {
				tok = jp.nextToken();
				if(tok == JsonToken.START_ARRAY) {
					String tid;
					while ((tok = jp.nextToken()) != JsonToken.END_ARRAY) {
						tid = null;
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
							else if (TERM_ID.equals(fieldname)) 
								tid = jp.nextTextValue();
							else if (FILE.equals(fieldname)) {
								fileSource = jp.nextIntValue(-1);
							}
						}
						
						Preconditions.checkArgument(begin != -1, MSG_EXPECT_PROP_FOR_OCCURRENCE, BEGIN);
						Preconditions.checkArgument(end != -1, MSG_EXPECT_PROP_FOR_OCCURRENCE, END);
						Preconditions.checkArgument(fileSource != -1, MSG_EXPECT_PROP_FOR_OCCURRENCE, FILE);
						String documentUrl = inputSources.get(fileSource);
						Preconditions.checkNotNull(documentUrl, MSG_NO_FILE_SOURCE_WITH_ID, fileSource);
						Preconditions.checkNotNull(text, MSG_EXPECT_PROP_FOR_OCCURRENCE, TEXT);
						Term term = termino.getTerms().get(tid);
						Preconditions.checkNotNull(term, MSG_NO_SUCH_TERM_IN_TERMINO, tid);
						indexedCorpus.getOccurrenceStore().addOccurrence(term, documentUrl, begin, end, text);
					} 
				}
				// end occurrences

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
				term = termino.getTerms().get(groupingKey);
				contextVector = new ContextVector(term);
				for(TempVecEntry tempVecEntry:currentTempVecList) {
					coTerm = termino.getTerms().get(tempVecEntry.getTermGroupingKey());
					contextVector.addEntry(coTerm, tempVecEntry.getNbCooccs(), tempVecEntry.getAssocRate());
				}
				if(!contextVector.getEntries().isEmpty())
					term.setContext(contextVector);
			}
		}

		return indexedCorpus;
	}

	@SuppressWarnings("unchecked")
	private static <T extends Enum<T> & Property<?>> Map<T,Comparable<?>> readProperties(Class<T> pCls, JsonParser jp) throws IOException {
		Map<T,Comparable<?>> properties = new HashMap<>();
		T property;
		String fieldname;

		Preconditions.checkArgument(jp.nextToken() == JsonToken.START_OBJECT);
		while (jp.nextToken() != JsonToken.END_OBJECT) {
			fieldname = jp.getCurrentName();
			jp.nextToken();
			if(pCls.equals(RelationProperty.class))
				property = (T) RelationProperty.fromJsonString(fieldname);
			else if(pCls.equals(TermProperty.class))
				property = (T) TermProperty.fromJsonString(fieldname);
			else 
				throw new UnsupportedOperationException("Unsupported property class: " + pCls);
			properties.put(property, readPropertyValue(jp, property));
		}
		return properties;
	}

	public static <T extends Enum<T> & Property<?>> Comparable<?> readPropertyValue(JsonParser jp, 
			T property) throws IOException {
		if(property.getRange().equals(Double.class)) {
			checkToken(property, jp.currentToken(), JsonToken.VALUE_NUMBER_FLOAT);
			return jp.getDoubleValue();
		} else if(property.getRange().equals(Float.class)) {
			checkToken(property, jp.currentToken(), JsonToken.VALUE_NUMBER_FLOAT);
			return (float)jp.getDoubleValue();
		} else if(property.getRange().equals(Integer.class)) {
			checkToken(property, jp.currentToken(), JsonToken.VALUE_NUMBER_INT);
			return jp.getIntValue();
		} else if(property.getRange().equals(Long.class)) {
			checkToken(property, jp.currentToken(), JsonToken.VALUE_NUMBER_INT);
			return jp.getLongValue();
		} else if(property.getRange().equals(Long.class)) {
			checkToken(property, jp.currentToken(), JsonToken.VALUE_FALSE, JsonToken.VALUE_TRUE);
			return jp.getBooleanValue();
		} else if(property.getRange().equals(String.class)) {
			checkToken(property, jp.currentToken(), JsonToken.VALUE_STRING);
			return jp.getValueAsString();
		} else if(property.getRange().isEnum()) {
			checkToken(property, jp.currentToken(), JsonToken.VALUE_STRING);
			PropertyValue theValue;
			String jsonString = jp.getValueAsString();
			theValue = loadEnumConstant(property, jsonString);
			return (Comparable<?>) theValue;
		} else {
			throw new UnsupportedOperationException(String.format(
					"Unsupported property range <%s> in property %s",property.getRange(), property));
		}
	}

	public static <T extends Enum<T> & Property<?>> PropertyValue loadEnumConstant(T property, String jsonString) {
		for(Object value:property.getRange().getEnumConstants()) {
			if(((PropertyValue)value).getSerializedString().equals(jsonString))
				return (PropertyValue)value;
		}
		throw new IllegalStateException(String.format("Unkown value <%s> for enun class %s", jsonString, property.getRange()));
	}

	public static <T extends Enum<T> & Property<?>> void checkToken(T property, JsonToken valueToken, JsonToken... expectedToken) {
		if(!Arrays.asList(expectedToken).contains(valueToken))
			throw new IllegalStateException(String.format(
				"Expected %s token for property %s (range: %s). Got: <%s>", 
				expectedToken,
				property, 
				property.getRange(),
				valueToken));
	}

	public static void save(Writer writer, IndexedCorpus indexCorpus, JsonOptions options) throws IOException {
		JsonFactory jsonFactory = new JsonFactory(); // or, for data binding, org.codehaus.jackson.mapper.MappingJsonFactory 
//		jsonFactory.configure(f, state)
		JsonGenerator jg = jsonFactory.createGenerator(writer); // or Stream, Reader
		jg.useDefaultPrettyPrinter();
		Terminology terminology = indexCorpus.getTerminology();
		jg.writeStartObject();

		jg.writeFieldName(METADATA);
		jg.writeStartObject();
		jg.writeFieldName(NAME);
		jg.writeString(terminology.getName());
		jg.writeFieldName(LANG);
		jg.writeString(terminology.getLang().getCode());
		if(terminology.getCorpusId() != null) {
			jg.writeFieldName(CORPUS_ID);
			jg.writeString(terminology.getCorpusId());
		}
		
		jg.writeFieldName(OCCURRENCE_STORAGE);
		if(options.isMongoDBOccStore()) {
			jg.writeString(OCCURRENCE_STORAGE_DISK);
			jg.writeFieldName(OCCURRENCE_PERSITENT_STORE_PATH);
			jg.writeString(options.getMongoDBOccStore());
		} else if(options.isEmbeddedOccurrences())
			jg.writeString(OCCURRENCE_STORAGE_EMBEDDED);
		else
			throw new IllegalStateException("Unknown storage mode");

		jg.writeFieldName(NB_WORD_ANNOTATIONS);
		jg.writeNumber(terminology.getNbWordAnnotations().longValue());
		jg.writeFieldName(NB_SPOTTED_TERMS);
		jg.writeNumber(terminology.getNbSpottedTerms().longValue());

		jg.writeEndObject();
		
		jg.writeFieldName(INPUT_SOURCES);
		int idCnt = 0;
		Map<String, Integer> inputSources = Maps.newTreeMap();
		for(Document d:indexCorpus.getOccurrenceStore().getDocuments())
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
		for(Word w:terminology.getWords().values()) {
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
		for(Term t:terminology.getTerms().values()) {
			jg.writeStartObject();
			jg.writeFieldName(PROPERTIES);
			jg.writeStartObject();
			writeProperties(jg, t);
			jg.writeEndObject();
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
		for(Relation relation:terminology.getRelations()) {
			jg.writeStartObject();
			jg.writeFieldName(FROM);
			jg.writeString(relation.getFrom().getGroupingKey());
			jg.writeFieldName(TO);
			jg.writeString(relation.getTo().getGroupingKey());
			jg.writeFieldName(RELATION_TYPE);
			jg.writeString(relation.getType().getShortName());
			jg.writeFieldName(PROPERTIES);
			jg.writeStartObject();
			writeProperties(jg, relation);
			jg.writeEndObject();
			jg.writeEndObject();
		}
		jg.writeEndArray();
		
		
		
		jg.writeFieldName(TERM_OCCURRENCES);
		jg.writeStartArray();
		if(options.withOccurrences() && options.isEmbeddedOccurrences()) {
			for(Term t:indexCorpus.getTerminology().getTerms().values()) {
				for(TermOccurrence o:indexCorpus.getOccurrenceStore().getOccurrences(t)) {
					jg.writeStartObject();
					jg.writeFieldName(TERM_ID);
					jg.writeString(t.getGroupingKey());
					
					jg.writeFieldName(BEGIN);
					jg.writeNumber(o.getBegin());
					
					jg.writeFieldName(END);
					jg.writeNumber(o.getEnd());
					
					jg.writeFieldName(FILE);
					jg.writeNumber(inputSources.get(o.getSourceDocument().getUrl()));
					
					jg.writeFieldName(TEXT);
					jg.writeString(o.getCoveredText());

					jg.writeEndObject();
				}
				
			}
		}
		jg.writeEndArray();

		jg.writeEndObject();
		jg.close();
	}

	public static <P extends Enum<P> & Property<?>> void writeProperties(JsonGenerator jg, PropertyHolder<P> t) throws IOException {
		for(P p:t.getProperties().keySet()) {
			jg.writeFieldName(p.getJsonField());
			writePropertyValue(jg, p, t.get(p));
		}
	}
	
	private static <P extends Enum<P> & Property<?>> void writePropertyValue(JsonGenerator jg, P p, Comparable<?> value) throws IOException {
		if(p.getRange().equals(Double.class)) 
			jg.writeNumber((Double)value);
		else if(p.getRange().equals(Integer.class)) 
			jg.writeNumber((Integer)value);
		else if(p.getRange().equals(Float.class)) 
			jg.writeNumber((Float)value);
		else if(p.getRange().equals(String.class)) 
			jg.writeString((String)value);
		else if(p.getRange().equals(Boolean.class)) 
			jg.writeBoolean((Boolean)value);
		else if(PropertyValue.class.isAssignableFrom(p.getRange())) {
			jg.writeString(((PropertyValue)value).getSerializedString());
		} else 
			throw new UnsupportedOperationException(String.format("Cannot serialize property %s. Unsupported range: %s", p, p.getRange()));
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
