package com.github.davidmoten.odata.client;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.davidmoten.guavamini.Preconditions;

public final class Retries {

    private static final Logger log = LoggerFactory.getLogger(Retries.class);

    public static final Retries NONE = Retries.builder().maxRetries(0).keepGoingIf(t -> false).build();

    private final long maxRetries;
    private final Iterable<Long> retryIntervalsMs;
    private final Supplier<? extends Function<? super Throwable, Boolean>> keepGoingIf;

    Retries(long maxRetries, Iterable<Long> retryIntervalMs,
            Supplier<? extends Function<? super Throwable, Boolean>> keepGoingIf) {
        Preconditions.checkArgument(maxRetries >= 0);
        Preconditions.checkNotNull(retryIntervalMs);
        Preconditions.checkNotNull(keepGoingIf);
        this.maxRetries = maxRetries;
        this.retryIntervalsMs = retryIntervalMs;
        this.keepGoingIf = keepGoingIf;
    }

    public void performWithRetries(RunnableThrowing runnable) {
        int attempt = 0;
        Throwable error = null;
        Function<? super Throwable, Boolean> keepGoing = keepGoingIf().get();
        Iterator<Long> intervalsMs = retryIntervalsMs().iterator();
        while (true) {
            if (attempt > maxRetries()) {
                throw new RetryException("attempts greater than maxRetries", error);
            }
            attempt++;
            try {
                runnable.run();
                break;
            } catch (Throwable e) {
                error = e;
                log.debug(e.getMessage(), e);
                if (!keepGoing.apply(e)) {
                    throw new RetryException("exception not retryable", e);
                }
                if (!intervalsMs.hasNext()) {
                    throw new RetryException("stopping retries because no more intervals specified");
                }
                long waitMs = intervalsMs.next();
                log.debug("sleeping " + waitMs + "ms");
                sleep(waitMs);
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public long maxRetries() {
        return maxRetries;
    }

    public Iterable<Long> retryIntervalsMs() {
        return retryIntervalsMs;
    }

    public Supplier<? extends Function<? super Throwable, Boolean>> keepGoingIf() {
        return keepGoingIf;
    }
    
    private static final class ForeverZero implements Iterable<Long> {
        @Override
        public Iterator<Long> iterator() {
            return new ForeverZeroIterator(); 
        }
    }

    private static final class ForeverZeroIterator implements Iterator<Long> {

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public Long next() {
            return 0L;
        }
    }
    
    public static final class Builder {

        private static final Iterable<Long> NO_INTERVAL = new ForeverZero();
        
        private long maxRetries = 0;
        private Iterable<Long> retryIntervalsMs = NO_INTERVAL;
        private Supplier<? extends Function<? super Throwable, Boolean>> keepGoingIf = () -> (t -> !(t instanceof Error));

        Builder() {
        }

        public Builder maxRetries(long maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder retryIntervalsMs(Iterable<Long> retryIntervalsMs) {
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
            return retryIntervalsMs(createRetryIntervalMsIterable(retryIntervals, unit));
        }

        private static Iterable<Long> createRetryIntervalMsIterable(Iterable<Long> retryIntervals, TimeUnit unit) {
            return new Iterable<Long>() {

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
            };
        }

        public Builder cappedExponentialRetry(long initial, double factor, long cap, TimeUnit unit) {
            Preconditions.checkArgument(initial >= 0);
            Preconditions.checkArgument(factor >= 0);
            Preconditions.checkArgument(cap >= 0);
            Preconditions.checkNotNull(unit);
            return retryIntervalsMs(createCappedExponentialRetryIterable(initial, factor, cap, unit));
        }
        
        private static Iterable<Long> createCappedExponentialRetryIterable(long initial, double factor, long cap, TimeUnit unit) {
            return new Iterable<Long>() {
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
            };
        }

        public Retries build() {
            return new Retries(maxRetries, retryIntervalsMs, keepGoingIf);
        }
    }

    private static void sleep(long waitMs) {
        try {
            Thread.sleep(waitMs);
        } catch (InterruptedException interruptedException) {
            throw new RetryException("interrupted", interruptedException);
        }
    }
}
