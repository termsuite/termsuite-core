package fr.univnantes.termsuite.test.asserts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.assertj.core.api.AbstractAssert;
import org.hamcrest.Matcher;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;

public class StringAssert extends AbstractAssert<StringAssert, String> {

	public StringAssert(String actual) {
		super(actual, StringAssert.class);
	}
	
	private String getLine(int lineNum) {
		BufferedReader reader = new BufferedReader(new StringReader(this.actual));
		int i = 0;
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				i++;
				if(lineNum == i)
					return line;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		throw new RuntimeException(String.format("String has only %d lines. Requested line: %d", i, lineNum));		
	}
	
	public StringAssert lineEquals(int lineNum, String expectedLine) {
		if(!getLine(lineNum).equals(expectedLine))
			failWithMessage("Expected text at line %s was <%s>, but actual is <%s>", lineNum, expectedLine, getLine(lineNum));
		return this;
	}
	
	public StringAssert tsvLineEquals(int lineNum, Object... expectedValues) {
		String[] actualValues = tsvLine(getLine(lineNum));
		
		if(actualValues.length != expectedValues.length) {
			failWithMessage("Expected line %s to have <%s> values, but actual line has <%s> values: <%s>", 
					lineNum,
					expectedValues.length,
					actualValues.length, 
					Joiner.on(",").join(actualValues)
				);
			return this;
		}
		
		for(int i = 0; i< actualValues.length; i++) {
			if(!Objects.equal(actualValues[i], expectedValues[i].toString())) {
				failWithMessage("Expected value at line %s and column %s was <%s>, but actual value is <%s>. Line: %s", 
						lineNum, 
						i+1,
						expectedValues[i], 
						actualValues[i],
						getLine(lineNum));
				return this;
			}
		}
		return this;
	}

	private String[] tsvLine(String line) {
		List<String> l = Splitter.on("\t").splitToList(line);
		return l.toArray(new String[l.size()]);
	}

	public StringAssert hasLineCount(int expected) {
		if(getLineCount(actual) != expected)
			failWithMessage("Expected string to have <%s> lines, but actual number of lines is <%s>", expected, getLineCount(actual));
		return this;
	}
	
	public int getLineCount(String string) {
		BufferedReader reader = new BufferedReader(new StringReader(this.actual));
		int i = 0;
		try {
			while (reader.readLine() != null)
				i++;
			return i;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public StringAssert atLine(int i, Matcher<String> matcher) {
		if(!matcher.matches(getLine(i)))
			failWithMessage("Expected to match <%s> at line <%s>, but actual string is <%s>", matcher.toString(), i, getLine(i));

		return this;
	}
}
