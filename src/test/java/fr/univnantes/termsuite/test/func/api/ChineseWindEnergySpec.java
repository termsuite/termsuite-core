
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

import static fr.univnantes.termsuite.test.func.FunctionalTests.termsByProperty;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.Test;

import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.TermProperty;

public class ChineseWindEnergySpec extends WindEnergySpec {

	@Override
	protected Lang getLang() {
		return Lang.ZH;
	}

	@Override
	protected List<String> getSyntacticMatchingRules() {
		return Lists.newArrayList(
				"rule1"
			);
	}


	@Override
	protected List<String> getSyntacticNotMatchingRules() {
		return Lists.newArrayList();
	}

	@Override
	protected List<String> getRulesNotTested() {
		return new ArrayList<String>();
	}

	@Test
	public void testTop10ByWR() {
		assertThat(termsByProperty(termino, TermProperty.SPECIFICITY, true).subList(0, 10))
			.hasSize(10)
			.extracting("groupingKey")
			.containsExactly(
					"n: ないので",
				    "nn: _ vicinity",
				    "nn: _ ansi",
				    "nn: slope _",
				    "n: 動作",
				    "n: economic",
				    "nn: gpsprom watchdog",
				    "nn: _ うものです",
				    "nn: _ ジョイントボルト",
				    "n: srppq")
			;
	}
}
