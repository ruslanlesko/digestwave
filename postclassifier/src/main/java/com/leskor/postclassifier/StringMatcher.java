package com.leskor.postclassifier;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class StringMatcher {
	private StringMatcher() {
	}

	// Returns true if the source string contains at least 3 words (with more than 3
	// letters) from
	// a reference string, case-insensitive.
	public static boolean matches(String source, String reference) {
		if (Objects.isNull(source) || Objects.isNull(reference)) {
			return false;
		}

		List<String> referenceWords = Arrays.stream(reference.split(" "))
				.filter(word -> word.length() > 3)
				.map(String::toLowerCase)
				.map(StringMatcher::removePunctuation)
				.toList();

		List<String> sourceWords = Arrays.stream(source.split(" "))
				.filter(word -> word.length() > 3)
				.map(String::toLowerCase)
				.map(StringMatcher::removePunctuation).toList();

		int matchCount = 0;
		for (String word : sourceWords) {
			if (referenceWords.contains(word)) {
				matchCount++;
				if (matchCount == 4) {
					return true;
				}
			}
		}

		return false;
	}

	private static String removePunctuation(String str) {
		if (str.length() < 2) {
			return str;
		}
		if (str.endsWith(".") || str.endsWith(",") || str.endsWith(":") || str.endsWith("!") || str.endsWith("?")) {
			return str.substring(0, str.length() - 1);
		}
		return str;
	}
}
