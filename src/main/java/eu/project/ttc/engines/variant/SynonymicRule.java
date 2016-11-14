package eu.project.ttc.engines.variant;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.index.TermValueProvider;
import eu.project.ttc.utils.TermSuiteConstants;

public class SynonymicRule extends VariantRule {
	private int synonymSourceWordIndex = -1;
	private Optional<TermValueProvider> equalityProvider = Optional.empty();
	private LinkedList<Integer> eqIndices = Lists.newLinkedList();
	
	private static final Pattern SYNONYM_EXPRESSION_PATTERN = Pattern.compile("synonym\\s*\\(\\s*([st]\\[\\s*\\d+\\s*\\])\\s*,\\s*([st]\\[\\s*\\d+\\s*\\])\\s*\\)");
	
	private static final Pattern EQUALITY_EXPRESSION_PATTERN = Pattern.compile("[st]\\s*\\[\\s*\\d+\\s*\\]\\s*==\\s*[st]\\s*\\[\\s*\\d+\\s*\\]");
	
	private SynonymicRule(String ruleName) {
		super(ruleName);
	}
	
	public static SynonymicRule parseSynonymicRule(VariantRule rawRule) {
		String name = rawRule.getName();
		Preconditions.checkNotNull(name);
		SynonymicRule rule = new SynonymicRule(name);
		rule.init(rawRule);
		return rule;
	}

	private void init(VariantRule rawRule) {
		Preconditions.checkArgument(rawRule.getSourcePatterns().size() == 1, "Only one source pattern allowed in a synonymic rule");
		Preconditions.checkArgument(rawRule.getTargetPatterns().size() == 1, "Only one target pattern allowed in a synonymic rule");
		Preconditions.checkArgument(rawRule.getSourcePatterns().equals(rawRule.getTargetPatterns()), "Source pattern must be equal to target pattern");
		
		if(rawRule.getExpression().contains("||"))
			throw new IllegalStateException("No disjunction allowed in synonym expression");

		Matcher matcher = SYNONYM_EXPRESSION_PATTERN.matcher(rawRule.getExpression());
		
		sourcePatterns = rawRule.sourcePatterns;
		sourcePatterns = rawRule.targetPatterns;
		helper = new VariantHelper();
		index = VariantRuleIndex.SYNONYM;
		sourceCompound = rawRule.sourceCompound;
		targetCompound = rawRule.targetCompound;
		
		if(matcher.find()) {
			String sourceExpr = matcher.group(1);
			String targetExpr = matcher.group(2);
			
			// ensure there is on s and one t as parameters
			if((sourceExpr.startsWith("s") && targetExpr.startsWith("t"))
					|| (sourceExpr.startsWith("t") && targetExpr.startsWith("s"))) {
				
				// switch s and t if required
				if(sourceExpr.startsWith("t")) {
					String aux = sourceExpr;
					sourceExpr = targetExpr;
					targetExpr = aux;
				}
				
				synonymSourceWordIndex  = Integer.parseInt(sourceExpr.replaceAll("\\D+", ""));
				int synonymTargetWordIndex = Integer.parseInt(targetExpr.replaceAll("\\D+", ""));
				Preconditions.checkState(synonymSourceWordIndex == synonymTargetWordIndex,
						"Synonymic expression parameters must have the same index. Got " 
								+ synonymSourceWordIndex + " and " + synonymTargetWordIndex);
				
				String expression = removeSubExpression(
						SYNONYM_EXPRESSION_PATTERN.toString(), 
						rawRule.getExpression());
				
				eqIndices = new LinkedList<>(extractEqualityIndices(expression));
				
				if(!eqIndices.isEmpty()) {
					equalityProvider = Optional.of(new TermValueProvider() {
						@Override
						public String getName() {
							return "SubStringProvider";
						}
						
						@Override
						public Collection<String> getClasses(TermIndex termIndex, Term term) {
							if(eqIndices.getLast() < term.getWords().size()) {
								return Lists.newArrayList(term.getWords().subList(
										eqIndices.getFirst(), 
										eqIndices.getLast() + 1).stream()
									.map(tw -> tw.getWord().getLemma())
									.collect(Collectors.joining(TermSuiteConstants.COLONS)));
							} else
								return Lists.newArrayList();
						}
					});
				}
				
				setGroovyRule(expression);
			} else
				throw new IllegalStateException("Expected extactly one source ref (s[]) and one target ref (t[]) in synonym expression. Got:  " + matcher.group());
		} else
			throw new IllegalStateException("No synonym expression found for synonymic rule " + rawRule.getName());
		
		if(matcher.find()) 
			throw new IllegalStateException("Only one synonym expression allowed. Rule <"+rawRule.getName()+"> has several ones.");
	}

	private List<Integer> extractEqualityIndices(String expression) {
		Matcher matcher = EQUALITY_EXPRESSION_PATTERN.matcher(expression);
		List<Integer> equalityIndices = Lists.newArrayList();
		
		while(matcher.find()) {
			Matcher sourceMatcher = Pattern.compile("s\\s*\\[\\s*(\\d+)\\s*\\]").matcher(matcher.group());
			if(!sourceMatcher.find())
				continue;
			Matcher targetMatcher = Pattern.compile("t\\s*\\[\\s*(\\d+)\\s*\\]").matcher(matcher.group());
			if(!targetMatcher.find())
				continue;
			
			int sourceIndex = Integer.parseInt(sourceMatcher.group(1));
			int targetIndex = Integer.parseInt(targetMatcher.group(1));
			
			if(sourceIndex != targetIndex) 
				continue;
			equalityIndices.add(sourceIndex);
		} 

		if(equalityIndices.isEmpty())
			return Lists.newArrayList();
		else {
			Collections.sort(equalityIndices);
			LinkedList<Integer> currentSequence = new LinkedList<>();
			List<List<Integer>> sequences = new LinkedList<>();
			for(Integer index:equalityIndices) {
				if(currentSequence.isEmpty())
					currentSequence.add(index);
				else {
					if(index == currentSequence.getLast() + 1)
						currentSequence.add(index);
					else {
						sequences.add(currentSequence);
						currentSequence = new LinkedList<>();
						currentSequence.add(index);
					}
				}
			}
			
			if(!currentSequence.isEmpty())
				sequences.add(currentSequence);
			
			Optional<List<Integer>> max = sequences.stream().max(new Comparator<List<Integer>>() {
				@Override
				public int compare(List<Integer> o1, List<Integer> o2) {
					return ComparisonChain.start()
							.compare(o1.size(), o2.size())
							.compare(o2.get(0), o1.get(0))
							.result()
							;
				}
			});
			if(max.isPresent()) {
				return max.get();
			} 
				else
					return Lists.newArrayList();
		}
	}

	public String removeSubExpression(String pattern, String sourceString) {
		String expression = sourceString.replaceAll(pattern, "");
		expression = expression
				.replaceAll("&&\\s*&&", "&&")
				.replaceAll("&&\\s*$", "")
				.replaceAll("^\\s*&&", "")
				;
		return expression;
	}

	public int getSynonymSourceWordIndex() {
		return synonymSourceWordIndex;
	}
	
	public String getIndexingKey(Term t) {
		return equalityProvider.get().getClasses(null, t).iterator().next();
	}

	public TermValueProvider getTermProvider() {
		return equalityProvider.get();
	}
	
	public LinkedList<Integer> getEqIndices() {
		return eqIndices;
	}
}
