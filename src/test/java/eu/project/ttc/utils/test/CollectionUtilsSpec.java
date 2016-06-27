package eu.project.ttc.utils.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import eu.project.ttc.utils.CollectionUtils;

public class CollectionUtilsSpec {

	@Test
	public void testCombineAndProduct() {
		List<? extends Set<String>> sets = ImmutableList.of(
				ImmutableSet.of("a", "b", "c"),
				ImmutableSet.of("d"),
				ImmutableSet.of("e", "f")
			);
		
		assertThat(CollectionUtils.combineAndProduct(sets))
			.hasSize(11)
			.extracting("element1", "element2")
			.contains(
					tuple("a", "d"),
					tuple("b", "d"),
					tuple("c", "d"),
					tuple("a", "e"),
					tuple("b", "e"),
					tuple("c", "e"),
					tuple("a", "f"),
					tuple("b", "f"),
					tuple("c", "f"),
					tuple("d", "e"),
					tuple("d", "f")
				);
	}
}
