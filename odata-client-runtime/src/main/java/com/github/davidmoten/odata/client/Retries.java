package com.github.davidmoten.odata.client;

import com.github.davidmoten.guavamini.Preconditions;

import java.util.Collections;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Retries {

    private final long maxRetries;
    private final Iterable<Long> retryIntervalsMs;
    private final Supplier<? extends Function<? super Throwable, Boolean>> keepGoingIf;

    Retries(long maxRetries, Iterable<Long> retryIntervalMs, Supplier<? extends Function<? super Throwable, Boolean>> keepGoingIf){
        Preconditions.checkArgument(maxRetries >=0);
        Preconditions.checkNotNull(retryIntervalMs);
        Preconditions.checkNotNull(keepGoingIf);
        this.maxRetries = maxRetries;
        this.retryIntervalsMs = retryIntervalMs;
        this.keepGoingIf = keepGoingIf;
    }

    public long maxRetries() {
        return maxRetries;
    }

    public Iterable<Long> retryIntervalsMs() {
        return retryIntervalsMs;
    }

    public static final class Builder {

        private long maxRetries = 0;
        private Iterable<Long> retryIntervalsMs = Collections.emptyList();
        private Supplier<? extends Function<? super Throwable, Boolean>> keepGoingIf = () -> (t -> !(t instanceof Error));

        Builder() {
        }

        public Builder maxRetries(long maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder retryIntervalsMs(Iterable<Long> retryIntervalMs) {
            this.retryIntervalsMs = retryIntervalsMs;
            return this;
        }

        public Builder keepGoingIf(Function<? super Throwable, Boolean> keepGoingIf) {
            this.keepGoingIf = () -> keepGoingIf;
            return this;
        }

        public Builder keepGoingIf(Supplier<? extends Function<? super Throwable, Boolean>> keepGoingIf) {
            this.keepGoingIf = keepGoingIf;
            return this;
        }

        public Builder retryIntervals(Iterable<Long> retryIntervals, TimeUnit unit) {
            return retryIntervalsMs(new Iterable<Long>() {

                @Override
                public Iterator<Long> iterator() {
                    Iterator<Long> it = retryIntervals.iterator();
                    return new Iterator<Long>() {

                        @Override
                        public boolean hasNext() {
                            return it.hasNext();
                        }

                        @Override
                        public Long next() {
                            return unit.toMillis(it.next());
                        }
                    };
                }
            });
        }

        public Builder cappedExponentialRetry(long initial, double factor, long cap, TimeUnit unit) {
            Preconditions.checkArgument(initial >= 0);
            Preconditions.checkArgument(factor >= 0);
            Preconditions.checkArgument(cap >=0);
            Preconditions.checkNotNull(unit);
            return retryIntervalsMs(new Iterable<Long>() {
                @Override
                public Iterator<Long> iterator() {
                    return new Iterator<Long>() {

                        long v = initial;

                        @Override
                        public boolean hasNext() {
                            return true;
                        }

                        @Override
                        public Long next() {
                            long w = v;
                            v = Math.round(Math.min(v * factor, cap));
                            return unit.toMillis(w);
                        }
                    };
                }
            });
        }

        public Retries build() {
            return new Retries(maxRetries, retryIntervalsMs, keepGoingIf);
        }
    }
}
