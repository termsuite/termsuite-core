
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

package fr.univnantes.termsuite.uima.resources.preproc;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import fr.univnantes.julestar.uima.resources.MapResource;
import fr.univnantes.termsuite.uima.engines.termino.morpho.Segmentation;
import fr.univnantes.termsuite.uima.resources.io.SegmentationParser;

public class ManualSegmentationResource extends MapResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(ManualSegmentationResource.class);

	private Map<String, Segmentation> segmentations = Maps.newHashMap();
	
	private SegmentationParser parser = new SegmentationParser();
	
	@Override
	protected void doKeyValue(int lineNum, String line, String key, String value) {
		if(segmentations.containsKey(key))
			LOGGER.warn("Ignoring duplicate word lemma {} in {} resource", key, this.getClass().getSimpleName());
		else
			segmentations.put(key, parser.parse(key));
	}
	
	public Segmentation getSegmentation(String lemma) {
		return segmentations.get(lemma);
	}

}
