package eu.project.ttc.resources;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import fr.univnantes.julestar.uima.resources.MultimapFlatResource;

public class SuffixDerivationList extends MultimapFlatResource {
	
	public static final String SUFFIX_DERIVATIONS = "SuffixDerivations";
	public static final String ERR_SHOULD_START_WITH_HYPHEN = "Suffix should start with hyphen. Got: \"%s\" at line %s ";
	private static final String HYPHEN = "-";
	
	private Multimap<String, SuffixDerivation> derivations = HashMultimap.create();
	
	@Override
	protected void doKeyValue(int lineNum, String line, String fromSuffix, String toSuffix) {
		Preconditions.checkArgument(fromSuffix.startsWith(HYPHEN), ERR_SHOULD_START_WITH_HYPHEN, fromSuffix, lineNum);
		Preconditions.checkArgument(toSuffix.startsWith(HYPHEN), ERR_SHOULD_START_WITH_HYPHEN, toSuffix, lineNum);
		String actualFromSuffix = fromSuffix.substring(1);
		String actualToSuffix = toSuffix.substring(1);
		SuffixDerivation suffixDerivation = new SuffixDerivation(actualFromSuffix, actualToSuffix);
		if(!derivations.containsEntry(actualFromSuffix, suffixDerivation))
			derivations.put(actualFromSuffix, suffixDerivation);
	}
	
	
	public List<SuffixDerivation> getSuffixDerivations(String plainWord) {
		List<SuffixDerivation> list = Lists.newArrayListWithExpectedSize(2);
		String suffix;
		Collection<SuffixDerivation> suffixDerivations;
		for(int i=0; i<plainWord.length(); i++) {
			suffix = plainWord.substring(i);
			suffixDerivations = derivations.get(suffix);
			list.addAll(suffixDerivations);
		}
		return list;
	}
	
	public Multimap<String, SuffixDerivation> getDerivations() {
		return derivations;
	}
}
