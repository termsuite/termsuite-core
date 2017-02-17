package fr.univnantes.termsuite.utils;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.Word;

public class TermHistory {
	private static final String LINE_FORMAT="[%s] <%s> %-25s > %s%n";
	private static final String ERR_TERM_NOT_WATCHED = "Term key <%s> is not watched.";

	private Set<String> watchedStrings = Sets.newHashSet();
	private LinkedListMultimap<String, PipelineEvent> events = LinkedListMultimap.create();
	
	public void saveEvent(TermService term, Class<?> source, String msg) {
		saveEvent(term.getTerm(), source, msg);
	}
	
	public void saveEvent(Term term, Class<?> source, String msg) {
		Preconditions.checkArgument(isTermWatched(term), ERR_TERM_NOT_WATCHED);
		saveEvent(getTermString(term).get(), source, msg);
	}
	
	private Optional<String> getTermString(Term term) {
		if(term.isPropertySet(TermProperty.GROUPING_KEY) && isStringWatched(term.getGroupingKey()))
			return Optional.of(term.getGroupingKey());
		else if(term.isPropertySet(TermProperty.LEMMA) && isStringWatched(term.getLemma()))
			return Optional.of(term.getLemma());
		else if(term.isPropertySet(TermProperty.PILOT) && isStringWatched(term.getPilot()))
			return Optional.of(term.getPilot());

		return Optional.empty();
	}

	public Set<String> getWatchedTermStrings() {
		return watchedStrings;
	}
	
	public void saveEvent(String watchedString, Class<?> source, String msg) {
		Preconditions.checkArgument(isStringWatched(watchedString), ERR_TERM_NOT_WATCHED);
		events.put(watchedString, PipelineEvent.create(watchedString, source, msg));
	}

	
	public boolean isTermWatched(TermService term) {
		return isTermWatched(term.getTerm());
	}
	
	
	private Optional<String> getWordString(Word word) {
		if(watchedStrings.contains(word.getLemma()))
			return Optional.of(word.getLemma());
		else if(watchedStrings.contains(word.getLemma().toLowerCase()))
			return Optional.of(word.getLemma().toLowerCase());
		return Optional.empty();
	}

	public boolean isWordWatched(Word word) {
		return getWordString(word).isPresent();
	}
	
	public boolean isTermWatched(Term term) {
		return getTermString(term).isPresent();
	}
	
	public boolean isStringWatched(String termString) {
		return watchedStrings.contains(termString);
	}
	
	public void addWatchedTermString(Collection<String> termStrings) {
		watchedStrings.addAll(termStrings);
	}
	
	public void addWatchedTermString(String... termStrings) {
		addWatchedTermString(Lists.newArrayList(termStrings));
	}
	
	
	public String toString(String watchedString) {
		Preconditions.checkArgument(isStringWatched(watchedString), ERR_TERM_NOT_WATCHED);
		StringWriter writer = new StringWriter();
		writer.write(String.format("*** History of term <%s> *** %n", watchedString));
		if(events.get(watchedString).isEmpty())
			writer.write("(no event)");
		else
			for(PipelineEvent event:events.get(watchedString))
				writer.write(String.format(LINE_FORMAT, 
					event.getDate().toString(), 
					event.getTermString(),
					event.getSource().getSimpleName(),
					event.getMessage()));
		return writer.toString();
	}
	
	@Override
	public String toString() {
		return events.keySet().stream().map(t -> toString(t)).collect(Collectors.joining("\n"));
	}

	public static TermHistory create(String... watchedTermStrings) {
		TermHistory history = new TermHistory();
		history.addWatchedTermString(watchedTermStrings);
		return history;
	}
}
