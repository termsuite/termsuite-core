package fr.univnantes.termsuite.utils;

import java.io.StringWriter;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.model.Term;

public class TermHistory {

	private static final String ERR_TERM_NOT_WATCHED = "Term key <%s> is not watched.";
	private static final Pattern LEMMA_PATTERN = Pattern.compile("[^:]+\\s*:\\s*(.+)");
	private Set<String> watchedGroupingKeys = Sets.newHashSet();
	private Set<String> watchedLemmas = Sets.newHashSet();

	private Multimap<String, String> groupingKeysByLemma = HashMultimap.create();

	private LinkedListMultimap<String, PipelineEvent> eventsByGroupingKeys = LinkedListMultimap.create();
	

	public void saveEvent(Term term, Class<?> source, String msg) {
		saveEvent(term.getGroupingKey(), source, msg);
	}
	
	public void saveEvent(String termKey, Class<?> source, String msg) {
		Preconditions.checkArgument(isGKeyWatched(termKey), ERR_TERM_NOT_WATCHED);
		eventsByGroupingKeys.put(termKey, PipelineEvent.create(termKey, source, msg));
	}

	public boolean isWatched(Term term) {
		return isGKeyWatched(term.getGroupingKey());
	}
	
	public boolean isGKeyWatched(String termKey) {
		return watchedGroupingKeys.contains(termKey);
	}
	
	public List<PipelineEvent> getEventsByGKey(String termKey) {
		Preconditions.checkArgument(isGKeyWatched(termKey), ERR_TERM_NOT_WATCHED);
		return eventsByGroupingKeys.get(termKey);
	}

	public void addWatchedTerms(Iterable<String> termKeys) {
		for(String key:termKeys) {
			Matcher matcher = LEMMA_PATTERN.matcher(key);
			if(matcher.find()) {
				watchedGroupingKeys.add(key);
				String lemma = matcher.group(1);
				watchedLemmas.add(lemma);
				groupingKeysByLemma.put(lemma, key);
			} else {
				throw new IllegalArgumentException("Invalid watch grouping key \""+key+"\"");
			}
		}
	}
	
	public void addWatchedGroupingKeys(String... termKeys) {
		addWatchedTerms(Lists.newArrayList(termKeys));
	}
	
	private static final String LINE_FORMAT="[%s] <%s> %-25s > %s%n";
	public String toString(String termKey) {
		
		Preconditions.checkArgument(isGKeyWatched(termKey), ERR_TERM_NOT_WATCHED);
		StringWriter writer = new StringWriter();
		writer.write(String.format("*** History of term <%s> *** %n", termKey));
		if(eventsByGroupingKeys.get(termKey).isEmpty())
			writer.write("(no event)");
		else
			for(PipelineEvent event:eventsByGroupingKeys.get(termKey))
				writer.write(String.format(LINE_FORMAT, 
					event.getDate().toString(), 
					event.getTermKey(),
					event.getSource().getSimpleName(),
					event.getMessage()));
		return writer.toString();
	}
	
	@Override
	public String toString() {
		return watchedGroupingKeys.stream().map(t -> toString(t)).collect(Collectors.joining("\n"));
	}

	public static TermHistory create(String... watchedTermKeys) {
		TermHistory history = new TermHistory();
		history.addWatchedGroupingKeys(watchedTermKeys);
		return history;
	}

	public boolean isLemmaWatched(String lemma) {
		return watchedLemmas.contains(lemma);
	}

	public void saveEventByLemma(String lemma, Class<?> src, String msg) {
		for(String gKey:groupingKeysByLemma.get(lemma)) 
			saveEvent(gKey, src, msg);
	}
}
