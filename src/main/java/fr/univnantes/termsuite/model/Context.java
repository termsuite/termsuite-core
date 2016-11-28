
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

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright 2, 2015nership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package fr.univnantes.termsuite.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableDouble;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class Context {

    protected ContextComparator comparator = new ContextComparator();

    private final HashMap<String, MutableDouble> occurrences = new HashMap<String, MutableDouble>();

    public static int MAX_MODE = 0;

    public static int MIN_MODE = 1;

    public static int ADD_MODE = 2;

    public static int DEL_MODE = 3;

    public void setCoOccurrences(String term, double coOccurrences, int mode) {
        if (!occurrences.containsKey(term))
            occurrences.put(term, new MutableDouble(0.0));

        MutableDouble coOcc = occurrences.get(term);

        if (mode == DEL_MODE) {
            coOcc.setValue(coOccurrences);
        } else if (mode == ADD_MODE) {
            coOcc.add(coOccurrences);
        } else if (mode == MAX_MODE && coOccurrences > coOcc.doubleValue()) {
            coOcc.setValue(coOccurrences);
        } else if (mode == MIN_MODE && coOccurrences < coOcc.doubleValue()) {
            coOcc.setValue(coOccurrences);
        }
    }

    public Set<String> getCoocurringTerms() {
        return occurrences.keySet();
    }

    protected Map<String, MutableDouble> sort() {
        Map<String, MutableDouble> occurrences = new TreeMap<String, MutableDouble>(comparator);
        occurrences.putAll(this.occurrences);
        return occurrences;
    }

    public Map<String, Double> getOccurrenceVector() {
        return Maps.transformValues(occurrences, DoubleMutator);
    }

    public void fromString(String text) {
        if (StringUtils.isEmpty(text))
            return;

        String[] pairs = text.split("\n");
        for (String pair : pairs) {
            String[] keyValue = pair.split("\t");
            String key = keyValue[0];
            String value = keyValue[1];
            this.setCoOccurrences(key, Double.valueOf(value), Context.ADD_MODE);
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        Map<String, MutableDouble> occurrences = this.sort();
        int size = occurrences.size();
        for (String key : occurrences.keySet()) {
            index++;
            MutableDouble occ = occurrences.get(key);
            if (occ == null) {
                continue;
            } else {
                builder.append(key);
                builder.append('\t');
                double value = occ.doubleValue();
                builder.append(value);
            }
            if (index < size) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    public double getOccurrences(String coTerm) {
        MutableDouble occ = occurrences.get(coTerm);
        return occ == null ? 0.0 : occ.doubleValue();
    }

    public void removeCoterm(String term) {
        occurrences.remove(term);
    }

    protected class ContextComparator implements Comparator<String> {

        @Override
        public int compare(String sourceKey, String targetKey) {
            MutableDouble sourceValue = Context.this.occurrences.get(sourceKey);
            MutableDouble targetValue = Context.this.occurrences.get(targetKey);
            double source = sourceValue == null ? 0.0 : sourceValue.doubleValue();
            double target = targetValue == null ? 0.0 : targetValue.doubleValue();
            if (source < target) {
                return 1;
            } else if (source > target) {
                return -1;
            } else {
                return sourceKey.compareTo(targetKey);
            }
        }
    }

    protected static final Function<MutableDouble, Double> DoubleMutator = new Function<MutableDouble, Double>() {
        @Override
        public Double apply(MutableDouble input) {
            return new Double(input.doubleValue());
        }
    };
}
