package com.valeamoris.solana4j.assertion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.locks.LockSupport;

public final class Waiter
{
    private static final Duration DEFAULT_INITIAL_DELAY = Duration.ZERO;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration DEFAULT_POLLING_INTERVAL = Duration.ofSeconds(3);
    private static final Waiter DEFAULT_WAITER = new Waiter(DEFAULT_INITIAL_DELAY, DEFAULT_TIMEOUT, DEFAULT_POLLING_INTERVAL);
    private static final Logger log = LoggerFactory.getLogger(Waiter.class);

    private final Duration initialDelay;
    private final Duration timeout;
    private final Duration pollingInterval;

    private Waiter(final Duration initialDelay, final Duration timeout, final Duration pollingInterval)
    {
        this.initialDelay = initialDelay;
        this.timeout = timeout;
        this.pollingInterval = pollingInterval;
    }

    public Waiter withInitialDelay(final Duration initialDelay)
    {
        return new Waiter(initialDelay, timeout, pollingInterval);
    }

    public Waiter withTimeout(final Duration timeout)
    {
        return new Waiter(initialDelay, timeout, pollingInterval);
    }

    public Waiter withPollingInterval(final Duration pollingInterval)
    {
        return new Waiter(initialDelay, timeout, pollingInterval);
    }

    public <T> T waitForCondition(final Condition<T> condition) throws AssertionError
    {
        final long endTimeMillis = System.currentTimeMillis() + timeout.toMillis();

        LockSupport.parkNanos(initialDelay.toNanos());
        while (System.currentTimeMillis() < endTimeMillis)
        {
            try
            {
                condition.check();
                return condition.getActual();
            }
            catch (final Throwable throwable)
            {
                LockSupport.parkNanos(pollingInterval.toNanos());
            }
        }

        throw new AssertionError("Waiting for condition that never happened. " + ZonedDateTime.now());
    }

    public static Waiter waiter()
    {
        return DEFAULT_WAITER;
    }

    public static <T> T waitForConditionMet(final Condition<T> condition)
    {
        return waiter().waitForCondition(condition);
    }

    public static <T> void waitForConditionNotMet(final Condition<T> condition)
    {
        try
        {
            waiter().withTimeout(Duration.of(15, ChronoUnit.SECONDS)).waitForCondition(condition);
            throw new AssertionError("We expected the condition to never happen, but it did!");
        }
        catch (final AssertionError e)
        {
            // we want this assertion error as it tells us our condition was not met
        }
    }
}
