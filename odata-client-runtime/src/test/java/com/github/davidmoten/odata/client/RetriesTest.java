package com.github.davidmoten.odata.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;

public class RetriesTest {

    @Test
    public void testMaxRetriesHonoured0() {
        checkMaxRetriesHonoured(0);
    }

    @Test
    public void testMaxRetriesHonoured1() {
        checkMaxRetriesHonoured(1);
    }

    @Test
    public void testMaxRetriesHonoured5() {
        checkMaxRetriesHonoured(5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxRetriesHonouredNegative() {
        checkMaxRetriesHonoured(-1);
    }

    @Test
    public void testRunOutOfIntervals() {
        AtomicInteger attempts = new AtomicInteger();
        Retries r = Retries.builder().retryIntervalsMs(Arrays.asList(0L, 0L)).maxRetries(2000).build();
        try {
            r.performWithRetries(() -> {
                attempts.incrementAndGet();
                throw new RuntimeException("boo");
            });
            Assert.fail();
        } catch (RuntimeException e) {
            assertEquals(3, attempts.get());
            assertTrue(e instanceof RetryException);
            assertEquals("stopping retries because no more intervals specified", e.getMessage());
        }
    }

    @Test
    public void testKeepGoingIf() {
        AtomicInteger attempts = new AtomicInteger();
        Retries r = Retries.builder().keepGoingIf(t -> false).build();
        try {
            r.performWithRetries(() -> {
                attempts.incrementAndGet();
                throw new RuntimeException("boo");
            });
            Assert.fail();
        } catch (RuntimeException e) {
            assertEquals(1, attempts.get());
        }
    }

    @Test
    public void testCalledIfNoError() {
        AtomicInteger attempts = new AtomicInteger();
        Retries r = Retries.builder().maxRetries(0).build();
        r.performWithRetries(() -> attempts.incrementAndGet());
        assertEquals(1, attempts.get());
    }

    @Test
    public void testCappedExponentialRetries() {
        List<Long> list = new ArrayList<>();
        Retries r = Retries.builder().cappedExponentialRetry(10, 2.0, 100, TimeUnit.MILLISECONDS).maxRetries(8).build();
        AtomicInteger attempts = new AtomicInteger();
        AtomicLong lastTime = new AtomicLong(System.currentTimeMillis());
        try {
            r.performWithRetries(() -> {
                attempts.incrementAndGet();
                long t = System.currentTimeMillis();
                list.add(t - lastTime.get());
                lastTime.set(t);
                throw new RuntimeException("boo");
            });
            Assert.fail();
        } catch (RuntimeException e) {
            assertEquals(9, attempts.get());
            System.out.println(list);
//            // if this testing proves too flaky we might chuck it
//            assertEquals(1, list.get(0), 5.0);
//            assertEquals(10, list.get(1), 5.0);
//            assertEquals(20, list.get(2), 8.0);
//            assertEquals(40, list.get(3), 15.0);
//            assertEquals(80, list.get(4), 15.0);
//            assertEquals(100, list.get(5), 30.0);
//            assertEquals(100, list.get(6), 30.0);
//            assertEquals(100, list.get(7), 30.0);
//            assertEquals(100, list.get(8), 30.0);
        }
    }

    private void checkMaxRetriesHonoured(int maxRetries) {
        AtomicInteger attempts = new AtomicInteger();
        Retries r = Retries.builder().maxRetries(maxRetries).build();
        try {
            r.performWithRetries(() -> {
                attempts.incrementAndGet();
                throw new RuntimeException("boo");
            });
            Assert.fail();
        } catch (RuntimeException e) {
            assertEquals(maxRetries + 1, attempts.get());
        }
    }

}
