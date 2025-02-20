package com.valeamoris.solana4j.client.jsonrpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.valeamoris.solana4j.client.api.SolanaRpcResponse;

final class BalanceDTO implements SolanaRpcResponse<Long>
{
    private final ContextDTO context;
    private final Long value;

    @JsonCreator
    BalanceDTO(
            final @JsonProperty("context") ContextDTO context,
            final @JsonProperty("value") Long value)
    {
        this.context = context;
        this.value = value;
    }

    @Override
    public Context getContext()
    {
        return context;
    }

    @Override
    public Long getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return "BalanceDTO{" +
               "context=" + context +
               ", value=" + value +
               '}';
    }
}
