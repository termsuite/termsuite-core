package eu.project.ttc.resources;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.project.ttc.models.TermWord;
import eu.project.ttc.utils.TermSuiteConstants;
import fr.univnantes.julestar.uima.resources.TabResource;

public class SuffixDerivationList extends TabResource {
	
	public static class SuffixDerivationEntry {
		private String suffix;
		private String label;
		public SuffixDerivationEntry(String suffix, String label) {
			super();
			this.suffix = suffix;
			this.label = label;
		}
		
		public String getSuffix() {
			return suffix;
		}
		public String getLabel() {
			return label;
		}
		@Override
		public int hashCode() {
			return Objects.hashCode(suffix, label);
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SuffixDerivationEntry) {
				SuffixDerivationEntry other = (SuffixDerivationEntry)obj;
				return Objects.equal(suffix, other.suffix)
							&& Objects.equal(label, other.label);
			} else
				return false;
		}

	}
	
	public SuffixDerivationList() {
		super(TermSuiteConstants.TAB);
	}

	public static final String SUFFIX_DERIVATIONS = "SuffixDerivations";
	public static final String ERR_SHOULD_START_WITH_HYPHEN = "Suffix should start with hyphen. Got: \"%s\" at line %s ";
	private static final String HYPHEN = "-";
	
	private Map<SuffixDerivationEntry, Set<SuffixDerivation>> derivations = Maps.newHashMap();
	private static final String ERR_REQUIRES_THREE_COLUMNS = "Row must have 3 columns at line %s. Got %s columns (line: \"%s\").";
	private static final String ERR_PATTERN_MUST_BE_SIZE_2 = "Derivation pattern must be of size 2 at line %s. Got size %s (line: \"%s\")";

	@Override
	protected void doRow(int lineNum, String line, String[] values) {
		Preconditions.checkArgument(values.length == 3, ERR_REQUIRES_THREE_COLUMNS,
				lineNum,
				values.length,
				line);
		List<String> pattern = Splitter.on(TermSuiteConstants.WHITESPACE).splitToList(values[0]);
		Preconditions.checkArgument(pattern.size() == 2, ERR_PATTERN_MUST_BE_SIZE_2,
				lineNum,
				pattern.size(),
				line);
		String fromLabel = pattern.get(0).trim();
		String toLabel = pattern.get(1).trim();
		
		
		String fromSuffix = values[1];
		String toSuffix = values[2];
		
		Preconditions.checkArgument(fromSuffix.startsWith(HYPHEN), ERR_SHOULD_START_WITH_HYPHEN, fromSuffix, lineNum);
		Preconditions.checkArgument(toSuffix.startsWith(HYPHEN), ERR_SHOULD_START_WITH_HYPHEN, toSuffix, lineNum);
		String actualFromSuffix = fromSuffix.substring(1);
		String actualToSuffix = toSuffix.substring(1);
		SuffixDerivation suffixDerivation = new SuffixDerivation(fromLabel, toLabel, actualFromSuffix, actualToSuffix);
		SuffixDerivationEntry suffixDerivationEntry = new SuffixDerivationEntry(actualFromSuffix, fromLabel);
		if(derivations.containsKey(suffixDerivationEntry)) {
			derivations.get(suffixDerivationEntry).add(suffixDerivation);
		} else {
			Set<SuffixDerivation> set = Sets.newHashSet();
			set.add(suffixDerivation);
			derivations.put(suffixDerivationEntry, set);
		}
	}
	
	
	public List<SuffixDerivation> getSuffixDerivations(String plainWordLemma, String label) {
		return getSuffixDerivations(TermWord.create(plainWordLemma, label));
	}
	
	public List<SuffixDerivation> getSuffixDerivations(TermWord termWord) {
		List<SuffixDerivation> list = Lists.newArrayListWithExpectedSize(2);
		String suffix;
		Collection<SuffixDerivation> suffixDerivations;
		for(int i=0; i<termWord.getWord().getLemma().length(); i++) {
			suffix = termWord.getWord().getLemma().substring(i);
			SuffixDerivationEntry entry = new SuffixDerivationEntry(suffix, termWord.getSyntacticLabel());
			suffixDerivations = derivations.get(entry);
			if(suffixDerivations != null)
				list.addAll(suffixDerivations);
		}
		return list;
	}
	
	
	public Map<SuffixDerivationEntry, Set<SuffixDerivation>> getDerivations() {
		return derivations;
	}
}
