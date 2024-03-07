package com.leskor.postclassifier;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StringMatcherTest {
	@Test
	void shouldReturnTrueWhenSourceContainsFourWordsFromReference() {
		assertTrue(StringMatcher.matches("hello world, this sentence contains some words",
				"hello some sentence world"));
	}

	@Test
	void shouldReturnFalseWhenSourceContainsLessThanFourWordsFromReference() {
		assertFalse(StringMatcher.matches("hello world", "this sentence does not contain above"));
	}

	@Test
	void shouldReturnTrueWhenSourceContainsFourPiecesFromReferenceCaseInsensitive() {
		assertTrue(StringMatcher.matches("HELLO WORLD, THIS SENTENCE contains SOME Words",
				"heLLO sOME sEntence WoRLD"));
	}

	@Test
	void shouldReturnFalseWhenAnyInputIsNull() {
		assertFalse(StringMatcher.matches(null, "hello world this sentence contains"));
		assertFalse(StringMatcher.matches("hello world this sentence contains", null));
	}

	@Test
	void shouldReturnFalseWhenBothInputsAreEmpty() {
		assertFalse(StringMatcher.matches("", ""));
	}
}