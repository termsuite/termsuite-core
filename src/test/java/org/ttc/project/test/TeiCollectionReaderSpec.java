package org.ttc.project.test;

import org.junit.Test;
import org.ttc.project.TestUtil;

import junit.framework.TestCase;

public class TeiCollectionReaderSpec extends TestCase {
	private static final String xml1 = "org/project/ttc/test/tei/file1.xml";
	private static final String xml2 = "org/project/ttc/test/tei/file2.xml";
	private static final String txt1 = "org/project/ttc/test/tei/file1.txt";
	private static final String txt2 = "org/project/ttc/test/tei/file2.txt";
	
	
	@Test
	public void testParseFile() throws Exception {
		fail("TEI broken.");
		String teiStr2 = TestUtil.getTeiTxt(xml2);
		String txtStr2 = TestUtil.readFile(txt2);
		assertTrue(teiStr2.indexOf("purée1") != -1);
		assertTrue(teiStr2.indexOf("purée2") != -1);
		assertTrue(teiStr2.indexOf("purée3") != -1);
		assertTrue(teiStr2.indexOf("purée4") != -1);
		assertTrue(teiStr2.indexOf("purée1") == teiStr2.indexOf("purée1"));
		assertTrue(teiStr2.indexOf("purée2") == teiStr2.indexOf("purée2"));
		assertTrue(teiStr2.indexOf("purée3") == teiStr2.indexOf("purée3"));
		assertTrue(teiStr2.indexOf("purée4") == teiStr2.indexOf("purée4"));
		assertEquals(txtStr2, teiStr2);
		String teiStr1 = TestUtil.getTeiTxt(xml1);
		String txtStr1 = TestUtil.readFile(txt1);
		assertEquals(txtStr1, teiStr1);
		assertTrue(txtStr1.indexOf("sont récoltés le même jour") == txtStr1.indexOf("sont récoltés le même jour"));
		
	}
	
	
}
