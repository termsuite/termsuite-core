package eu.project.ttc.history;

import java.io.StringWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.project.ttc.models.Term;

public class TermHistory {

	private static final String ERR_TERM_NOT_WATCHED = "Term key <%s> is not watched.";
	
	private Set<String> watchedTerms = Sets.newHashSet();

	private LinkedListMultimap<String, PipelineEvent> events = LinkedListMultimap.create();
	
	public void saveEvent(String termKey, Class<?> source, String msg) {
		Preconditions.checkArgument(isWatched(termKey), ERR_TERM_NOT_WATCHED);
		events.put(termKey, PipelineEvent.create(termKey, source, msg));
	}

	public boolean isWatched(Term term) {
		return isWatched(term.getGroupingKey());
	}
	
	public boolean isWatched(String termKey) {
		return watchedTerms.contains(termKey);
	}
	
	public List<PipelineEvent> getEvents(String termKey) {
		Preconditions.checkArgument(isWatched(termKey), ERR_TERM_NOT_WATCHED);
		return events.get(termKey);
	}

	public void addWatchedTerms(Iterable<String> termKeys) {
		for(String key:termKeys)
			watchedTerms.add(key);
	}
	public void addWatchedTerms(String[] termKeys) {
		addWatchedTerms(Lists.newArrayList(termKeys));
	}
	
	private static final String LINE_FORMAT="[%s] <%s> %-25s > %s%n";
	public String toString(String termKey) {
		
		Preconditions.checkArgument(isWatched(termKey), ERR_TERM_NOT_WATCHED);
		StringWriter writer = new StringWriter();
		writer.write(String.format("*** History of term <%s> *** %n", termKey));
		if(events.get(termKey).isEmpty())
			writer.write("(no event)");
		else
			for(PipelineEvent event:events.get(termKey))
				writer.write(String.format(LINE_FORMAT, 
					event.getDate().toString(), 
					event.getTermKey(),
					event.getSource().getSimpleName(),
					event.getMessage()));
		return writer.toString();
	}
	
	@Override
	public String toString() {
		return watchedTerms.stream().map(t -> toString(t)).collect(Collectors.joining("\n"));
	}

	public static TermHistory create(String... watchedTermKeys) {
		TermHistory history = new TermHistory();
		history.watchedTerms = Sets.newHashSet(watchedTermKeys);
		return history;
	}
}
