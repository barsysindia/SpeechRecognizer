package com.example.pratyush.speechrecognizer;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public class SpeechResultComparator implements Comparator<SpeechResult> {


    @Override
    public int compare(SpeechResult o1, SpeechResult o2) {
        return o1.getDistance()-o2.getDistance();
    }

    @Override
    public Comparator<SpeechResult> reversed() {
        return null;
    }

    @Override
    public Comparator<SpeechResult> thenComparing(Comparator<? super SpeechResult> other) {
        return null;
    }

    @Override
    public <U> Comparator<SpeechResult> thenComparing(Function<? super SpeechResult, ? extends U> keyExtractor, Comparator<? super U> keyComparator) {
        return null;
    }

    @Override
    public <U extends Comparable<? super U>> Comparator<SpeechResult> thenComparing(Function<? super SpeechResult, ? extends U> keyExtractor) {
        return null;
    }

    @Override
    public Comparator<SpeechResult> thenComparingInt(ToIntFunction<? super SpeechResult> keyExtractor) {
        return null;
    }

    @Override
    public Comparator<SpeechResult> thenComparingLong(ToLongFunction<? super SpeechResult> keyExtractor) {
        return null;
    }

    @Override
    public Comparator<SpeechResult> thenComparingDouble(ToDoubleFunction<? super SpeechResult> keyExtractor) {
        return null;
    }
}
