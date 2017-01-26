
/*******************************************************************************
 * Copyright 2015-2016 - CNRS (Centre National de Recherche Scientifique)
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License")), you may not use this file except in compliance
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

import fr.univnantes.termsuite.index.providers.AllComponentPairsProvider;
import fr.univnantes.termsuite.index.providers.DerivationLemmasProvider;
import fr.univnantes.termsuite.index.providers.LowercaseTermLemmaProvider;
import fr.univnantes.termsuite.index.providers.PrefixationLemmasProvider;
import fr.univnantes.termsuite.index.providers.SwtGroupingKeysProvider;
import fr.univnantes.termsuite.index.providers.SwtLemmasProvider;
import fr.univnantes.termsuite.index.providers.WordLemmaStemProvider;
import fr.univnantes.termsuite.index.providers.WordLemmasProvider;
import fr.univnantes.termsuite.index.providers._1FirstLetterValueProvider;
import fr.univnantes.termsuite.index.providers._2FirstLetterValueProvider;
import fr.univnantes.termsuite.index.providers._3FirstLetterValueProvider;
import fr.univnantes.termsuite.index.providers._4FirstLetterValueProvider;

public enum TermIndexType {
	LEMMA_LOWER_CASE(
			LowercaseTermLemmaProvider.class),
	WORD_LEMMAS(
			WordLemmasProvider.class),
	ALLCOMP_PAIRS(
			AllComponentPairsProvider.class),
	WORD_COUPLE_LEMMA_STEM(
			WordLemmaStemProvider.class),
	SWT_GROUPING_KEYS(
			SwtGroupingKeysProvider.class),
	SWT_LEMMAS(
			SwtLemmasProvider.class),
	SWT_LEMMAS_SWT_TERMS_ONLY(
			SwtLemmasSwtTermsOnlyProvider.class),
	SWT_GROUPING_KEYS_SWT_ONLY(
			SwtGroupingKeysSwtTermsOnlyProvider.class), 
	PREFIXATION_LEMMAS(
			PrefixationLemmasProvider.class),
	DERIVATION_LEMMAS(
			DerivationLemmasProvider.class),
	FIRST_LETTERS_1(
			_1FirstLetterValueProvider.class),
	FIRST_LETTERS_2(
			_2FirstLetterValueProvider.class),
	FIRST_LETTERS_3(
			_3FirstLetterValueProvider.class),
	FIRST_LETTERS_4(
			_4FirstLetterValueProvider.class), 
	;
	
	private Class<? extends TermIndexValueProvider> providerClass;
	
	private TermIndexType(Class<? extends TermIndexValueProvider> providerClass) {
		this.providerClass = providerClass;
	}
	
	public Class<? extends TermIndexValueProvider> getProviderClass() {
		return providerClass;
	}
}
