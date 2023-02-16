package com.leskor.sanitizer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.leskor.sanitizer.entities.SanitizedPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DuplicationFilter {
    private final ConcurrentMap<String, Buffer> topicBuffers = new ConcurrentHashMap<>();

    // returns false if post title is a duplicate, true if unique
    public boolean filter(SanitizedPost post) {
        Buffer buffer = topicBuffers.computeIfAbsent(post.topic(), t -> new Buffer());
        synchronized (buffer) {
            if (buffer.isDuplicate(post.title())) {
                return false;
            }
            buffer.add(post.title());
            return true;
        }
    }

    static class Buffer {
        private static final Logger logger = LoggerFactory.getLogger("Application");

        private static final int BUFFER_SIZE = 42;
        private static final int MATCHES_REQUIRED = 3;
        private static final int MIN_WORD_SIZE = 4;

        private final List<List<String>> titles = new LinkedList<>();

        void add(String title) {
            List<String> titleWords = titleToList(title);
            if (titles.size() >= BUFFER_SIZE) {
                titles.remove(0);
            }
            titles.add(titleWords);
        }

        boolean isDuplicate(String title) {
            List<String> titleWords = titleToList(title);

            for (List<String> t : titles) {
                int matches = 0;
                for (String w1 : t) {
                    for (String w2 : titleWords) {
                        if (w1.equals(w2)) {
                            matches++;
                            break;
                        }
                    }
                }
                if (matches >= MATCHES_REQUIRED) {
                    logger.debug("Duplicate found. Original: '{}' Current: '{}'",
                            String.join(" ", t),
                            title);
                    return true;
                }
            }

            return false;
        }

        private List<String> titleToList(String title) {
            return Arrays.stream(title.split(" "))
                    .filter(Objects::nonNull)
                    .map(w -> w.replaceAll("\\.", "")
                            .replaceAll(",", "")
                            .replaceAll(":", "")
                            .toLowerCase())
                    .filter(w -> w.length() >= MIN_WORD_SIZE)
                    .distinct()
                    .toList();
        }
    }
}
