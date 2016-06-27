package eu.project.ttc.utils.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import eu.project.ttc.utils.CollectionUtils;

public class CollectionUtilsSpec {

	@SuppressWarnings("unchecked")
	@Test
	public void testCombineAndProduct() {
		List<? extends Set<String>> sets = ImmutableList.of(
				ImmutableSet.of("a", "b", "c"),
				ImmutableSet.of("d"),
				ImmutableSet.of("e", "f")
			);
		
		assertThat(CollectionUtils.combineAndProduct(sets))
			.hasSize(11)
			.contains(
					ImmutableList.of("a", "d"),
					ImmutableList.of("b", "d"),
					ImmutableList.of("c", "d"),
					ImmutableList.of("a", "e"),
					ImmutableList.of("b", "e"),
					ImmutableList.of("c", "e"),
					ImmutableList.of("a", "f"),
					ImmutableList.of("b", "f"),
					ImmutableList.of("c", "f"),
					ImmutableList.of("d", "e"),
					ImmutableList.of("d", "f")
				);
	}
}
