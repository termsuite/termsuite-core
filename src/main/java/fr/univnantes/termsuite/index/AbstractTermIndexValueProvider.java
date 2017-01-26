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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.utils.TermUtils;

public abstract class AbstractTermIndexValueProvider implements TermIndexValueProvider {
	private static final String PAIR_FORMAT = "%s+%s";

	protected static Collection<String> toRelationPairs(Terminology termino, Term term, RelationType relType) {
		Set<TermRelation> prefixations = new HashSet<>();
		for(TermWord tw:term.getWords()) {
			Term t =termino.getTerms().get(TermUtils.toGroupingKey(tw));
			if(t!=null) {
				prefixations.addAll(termino.getInboundRelations(t, relType));
				prefixations.addAll(termino.getOutboundRelations(t, relType));
			}
		}
		return prefixations.stream()
				.map(rel -> String.format(PAIR_FORMAT, rel.getFrom().getLemma(), rel.getTo().getLemma()))
				.collect(Collectors.toSet());
	}
}
