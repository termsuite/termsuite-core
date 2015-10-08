package eu.project.ttc.models;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

public class VariationPath implements Iterable<TermVariation> {
	private static final String MSG_NOT_A_PATH = "Not a path";

	private Deque<TermVariation> variations;
	
	public VariationPath(TermVariation tv1, TermVariation... variations) {
		super();
		this.variations = Lists.newLinkedList();
		this.variations.add(tv1);
		for(TermVariation tv:variations) 
			addTermVariation(tv);
	}

	public VariationPath(List<TermVariation> path) {
		super();
		Preconditions.checkArgument(!path.isEmpty());
		this.variations = Lists.newLinkedList(path);
	}

	public void addTermVariation(TermVariation tv) {
		Preconditions.checkArgument(this.variations.getLast().getVariant().equals(tv.getBase()), MSG_NOT_A_PATH);
		this.variations.add(tv);
	}

	public Term getBase() {
		return this.variations.getFirst().getBase();
	}
	
	public Term getVariant() {
		return this.variations.getLast().getVariant();
	}
	
	public int size() {
		return variations.size();
	}

	@Override
	public Iterator<TermVariation> iterator() {
		return Iterators.unmodifiableIterator(variations.iterator());
	}

	public TermVariation[] toArray() {
		return variations.toArray(new TermVariation[this.variations.size()]);
	}

	public boolean isCycle() {
		return this.getBase().equals(this.getVariant());
	}
}
