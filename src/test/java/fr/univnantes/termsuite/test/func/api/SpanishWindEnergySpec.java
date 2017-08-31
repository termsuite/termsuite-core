
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

package fr.univnantes.termsuite.test.func.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;
import org.junit.Test;

import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Term;

public class SpanishWindEnergySpec extends WindEnergySpec {
	
	@Override
	protected Lang getLang() {
		return Lang.ES;
	}
	
	@Override
	protected List<String> getSyntacticMatchingRules() {
		return Lists.newArrayList(
				"S-I-NN-PA",
				"S-Ed-NA-A",
				"S-Ed-NA-AA",
				"S-Ed-NA-PN",
				"S-Ed-NA-PAN",
				"S-Ed-NA-PNA",
				"S-Ed-NA-RA",
				"S-Ed-NA-CA",
				"S-Eg-NA-NP",
				"S-I-NA-A",
				"S-I-NA-PN",
				"S-I-NA-R",
				"S-I-NA-AC",
				"S-I-NA-V",
				"S-I-NA-VAC",
				"S-IEd-NA-V-CN",
				"S-I-AN-CA",
				"S-Eg-AN-AC",
				"S-I1-NPN-A",
				"S-I1-NPN-PN",
				"S-I2-NPN-A",
				"S-I1-NPN-PNC",
				"S-I1-NPN-CN",
				"S-I2-NPN-NC",
				"S-Ed-NPN-PN",
				"S-Ed-NPN-A",
				"S-Ed-NPN-CPN",
				"S-Ed-NPN-CN",
				"S-Ed-NPN-CPNA",
				"S-Eg-NPN-NP",
				" S-Eg-NPN-NC",
				"S-R2I-NPN-P",
				"S-R2D-NPN",
				"S-I3-NPNA-R",
				"S-I1-NPNA-PNC",
				"S-I1-NPNA-CN",
				"S-Eg-NPNA-NC",
				"S-R2-NPNA",
				"S-P-NPNA-A",
				"S-P-NPNPN-PN"
				);
	}
	
	@Override
	protected List<String> getSyntacticNotMatchingRules() {
		return Lists.newArrayList(
				"S-Ed-N",
				"S-I5-NPNPNA-R",
				"M-S-NN",
				"M-P-NEN-E",
				"M-A",
				"M-R2-NA",
				"M-R2-NAA"
				);
	}
	
	@Override
	protected List<String> getRulesNotTested() {
		return Lists.newArrayList(
				);
	}
	
	@Test
	public void testNoZeroGFnorm() {
		/*
		 * Check on bug fix.
		 * 
		 * There should never be any general frequency equal to 0
		 */
		
		List<Term> terms = termino.getTerms().values().stream()
				.filter(t-> t.getGeneralFrequencyNorm() == 0)
				.collect(Collectors.toList());

		assertThat(terms).isEmpty();
	}
}
