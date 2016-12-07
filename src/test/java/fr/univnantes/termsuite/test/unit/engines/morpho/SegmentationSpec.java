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
package fr.univnantes.termsuite.test.unit.engines.morpho;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import fr.univnantes.termsuite.engines.morpho.Segmentation;

public class SegmentationSpec {
	private static String toStringList(List<Segmentation> segmentations) {
		List<String> segmentationStr = Lists.newArrayList();
		for(Segmentation s:segmentations) 
			segmentationStr.add(s.toString());
		return Joiner.on(' ').join(segmentationStr);
		
	}
	
	@Test
	public void testFindSegmentations() {
		Assert.assertEquals("homme+grenouille", toStringList(Segmentation.getSegmentations("homme-grenouille", 2, 3)));
		
		String expected = "homme+grenouille homme+gre+nouille homme+gren+ouille homme+greno+uille homme+grenou+ille homme+grenoui+lle";
		Assert.assertEquals(expected, toStringList(Segmentation.getSegmentations("homme-grenouille", 3, 3)));
		
		Assert.assertEquals("homme+greno-uille", toStringList(Segmentation.getSegmentations("homme-greno-uille", 2, 3)));
		Assert.assertEquals("homme+greno-uille homme+greno+uille", toStringList(Segmentation.getSegmentations("homme-greno-uille", 3, 3)));
		Assert.assertEquals("homme+greno-uille homme+greno+uille", toStringList(Segmentation.getSegmentations("homme-greno-uille", 4, 3)));
		

		expected = "abc+defghi abc+def+ghi abcd+efghi abcde+fghi abcdef+ghi";
		Assert.assertEquals(expected, toStringList(Segmentation.getSegmentations("abcdefghi", 3, 3)));
		
		expected = "abc+defghijkl abc+def+ghijkl abc+def+ghi+jkl abc+defg+hijkl abc+defgh+ijkl abc+defghi+jkl abcd+efghijkl abcd+efg+hijkl abcd+efgh+ijkl abcd+efghi+jkl abcde+fghijkl abcde+fgh+ijkl abcde+fghi+jkl abcdef+ghijkl abcdef+ghi+jkl abcdefg+hijkl abcdefgh+ijkl abcdefghi+jkl";
		Assert.assertEquals(expected, toStringList(Segmentation.getSegmentations("abcdefghijkl", 4, 3)));
	}
	
}
