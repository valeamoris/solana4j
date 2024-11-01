package com.lmax.solana4j.client.jsonrpc;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GetMinimumBalanceForRentExemptionContractTest extends SolanaClientIntegrationTestBase
{
    @Test
    void shouldGetMinimumBalanceForRentExemption() throws SolanaJsonRpcClientException
    {
//        {
//            "jsonrpc" : "2.0",
//                "result" : 7850880,
//                "id" : 4
//        }

        assertThat(api.getMinimumBalanceForRentExemption(1000).getResponse()).isGreaterThan(0L);
    }

    @Test
    @Disabled
    void whatHappensWithNegativeOrZeroSpace()
    {

    }
}
