package org.ttc.project.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.project.ttc.utils.TermUtils;

public class TermUtilsSpec {
	@Test
	public void testCollapseText() {
		assertEquals("la vie, est belle", TermUtils.collapseText("   la\n    vie,\rest   belle "));
		assertEquals("la vie , est belle", TermUtils.collapseText(" \n  la    vie  , est   belle "));
	}
}
