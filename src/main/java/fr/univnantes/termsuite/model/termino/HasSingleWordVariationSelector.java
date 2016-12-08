
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

import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.utils.TermUtils;

/**
 * 
 * Selects a term if any of its single-word sub-terms 
 * has a variation or a base of given type.
 * 
 * @author Damien Cram
 *
 */
public class HasSingleWordVariationSelector extends AbstractTermSelector {

	private RelationType variationType;
	
	public HasSingleWordVariationSelector(RelationType variationType) {
		super(true);
		this.variationType = variationType;
	}

	public RelationType getVariationType() {
		return variationType;
	}
	
	@Override
	public boolean select(Terminology termIndex, Term term) {
		Term swt;
		for (TermWord termWord : term.getWords()) {
			swt = termIndex.getTermByGroupingKey(TermUtils.toGroupingKey(termWord));
			if (swt != null) {
				if(termIndex.getInboundRelations(swt,this.variationType).iterator().hasNext()
					|| termIndex.getOutboundRelations(swt,this.variationType).iterator().hasNext())
					return true;
			}
		}
		return false;
	}

}
