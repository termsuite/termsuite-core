
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

package eu.project.ttc.resources;

import java.util.List;

import com.google.common.collect.Lists;

import eu.project.ttc.tools.PipelineListener;

public class TermSuitePipelineObserver {
	
	private List<PipelineListener> listeners = Lists.newArrayList();
	

	private int globalProcessWeight = 1;
	private int globalCCWeight = 1;
	private int sum = globalCCWeight + globalProcessWeight;

	public TermSuitePipelineObserver(int globalProcessWeight, int globalCCWeight) {
		super();
		this.globalProcessWeight = globalProcessWeight;
		this.globalCCWeight = globalCCWeight;
		this.sum = globalProcessWeight + globalCCWeight;
	}

	public boolean registerListener(PipelineListener e) {
		return listeners.add(e);
	}

	public boolean removeListener(PipelineListener l) {
		return listeners.remove(l);
	}

	public void status(double processProgress, double ccProgress, String statusline) {
		
		for(PipelineListener l:listeners) {
			double absProgress = (processProgress * globalProcessWeight) + (ccProgress * globalCCWeight);
			l.statusUpdated(absProgress/sum, statusline);
		}
	}
}
