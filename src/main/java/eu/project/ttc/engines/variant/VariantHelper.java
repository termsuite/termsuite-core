
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

package eu.project.ttc.engines.variant;

import java.util.Iterator;

import com.google.common.base.Objects;

import eu.project.ttc.models.GroovyWord;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermRelation;
import eu.project.ttc.models.RelationType;
import eu.project.ttc.utils.TermUtils;

public class VariantHelper {

	private TermIndex termIndex;
	
	public VariantHelper() {
		super();
	}
	
	void setTermIndex(TermIndex termIndex) {
		this.termIndex = termIndex;
	}

	
	public boolean derivesInto(String derivationPattern, GroovyWord s, GroovyWord t) {
		Term sourceTerm = toTerm(s);
		if(sourceTerm == null)
			return false;
		Term targetTerm = toTerm(t);
		if(targetTerm == null)
			return false;
		
		TermRelation tv;
		for(Iterator<TermRelation> it = termIndex.getOutboundRelations(sourceTerm, RelationType.DERIVES_INTO).iterator()
				; it.hasNext() 
				; ) {
			tv = it.next();
			if(tv.getTo().equals(targetTerm)) {
				if(Objects.equal(tv.getInfo(), derivationPattern))
					return true;
			}
		}
		
		return false;
	}
	
	public boolean isPrefixOf(GroovyWord s, GroovyWord t) {
		Term sourceTerm = toTerm(s);
		if(sourceTerm == null)
			return false;
		Term targetTerm = toTerm(t);
		if(targetTerm == null)
			return false;
		
		TermRelation tv;
		for(Iterator<TermRelation> it = termIndex.getOutboundRelations(sourceTerm, RelationType.IS_PREFIX_OF).iterator()
				; it.hasNext() 
				; ) {
			tv = it.next();
			if(tv.getTo().equals(targetTerm)) {
				return true;
			}
		}
		
		return false;
	}

	private Term toTerm(GroovyWord s) {
		String sourceGroupingKey = TermUtils.toGroupingKey(s.getTermWord());
		Term sourceTerm = this.termIndex.getTermByGroupingKey(sourceGroupingKey);
		return sourceTerm;
	}
}
