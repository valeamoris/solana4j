package com.valeamoris.solana4j.assertion;

import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

class IsEqualToCondition<T> extends Condition<T>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(IsEqualToCondition.class);

    private final T expected;
    private final Supplier<T> actualSupplier;

    IsEqualToCondition(final T expected, final Supplier<T> actualSupplier)
    {
        this.expected = expected;
        this.actualSupplier = actualSupplier;
    }

    @Override
    public void check()
    {
        try
        {
            final T actual = actualSupplier.get();
            LOGGER.info("Expected value {} and actual value {}.", expected, actual);
            Assertions.assertThat(expected).isEqualTo(actual);
        }
        catch (final Exception e)
        {
            LOGGER.error("Something went wrong trying to get the actual value.", e.getCause());
            throw e;
        }
    }

    @Override
    public T getActual()
    {
        return actualSupplier.get();
    }
}
