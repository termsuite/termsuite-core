/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright 2, 2013nership.  The ASF licenses this file
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
package eu.project.ttc.tools.commons;

import java.util.Locale;

/**
 * This class represents a language item. It is an abstraction between
 * language code as used by the system vs. language representation as
 * understood by humans.
 *
 * @author Fabien Poulard <fpoulard@dictanova.com>
 * @date 15/08/13
 */
public class LanguageItem {

    private String code;

    public LanguageItem(String code) {
        this.code = code;
    }

    public String getValue() {
        return code;
    }

    @Override
    public String toString() {
        return new Locale(code).getDisplayLanguage(Locale.ENGLISH);
    }

}
