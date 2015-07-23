package org.ttc.project.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import eu.project.ttc.engines.compost.Segmentation;

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
