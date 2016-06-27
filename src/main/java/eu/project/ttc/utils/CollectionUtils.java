package eu.project.ttc.utils;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

public class CollectionUtils {
	
	/**
	 * 
	 * Transforms a list of sets into a set of element pairs,
	 * which is the union of all cartesian products of
	 * all pairs of sets.
	 * 
	 * Example: 
	 * 
	 * A = {a,b,c}
	 * B = {d}
	 * C = {e, f}
	 * 
	 * returns AxB U AxC U BxC = {
	 * 	{a,d}, {b,d}, {c,d}, 
	 *  {a,e}, {a,f}, {b,e}, {b,f}, {c,e}, {c,f},
	 *  {d,e}, {d,f}
	 * }
	 * 
	 * 
	 * @param iterable
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Set<List<T>> combineAndProduct(List<? extends Set<? extends T>> sets) {
		Set<List<T>> results = Sets.newHashSet();
		for(int i = 0; i< sets.size(); i++) {
			for(int j = i+1; j< sets.size(); j++)
				results.addAll(
						Sets.cartesianProduct(sets.get(i), sets.get(j)));
		}
		return results;
	}

}
