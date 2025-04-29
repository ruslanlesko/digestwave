package com.leskor.digestwave.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Sentiment {
    POSITIVE,
    NEGATIVE,
    NEUTRAL;

    @JsonCreator
    public static Sentiment fromValue(String value) {
        return Sentiment.valueOf(value.toUpperCase());
    }
}
