package com.valeamoris.solana4j.client.jsonrpc;

import com.valeamoris.solana4j.client.api.Blockhash;
import com.valeamoris.solana4j.client.api.SolanaClientOptionalParams;
import com.valeamoris.solana4j.client.api.SolanaClientResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// https://solana.com/docs/rpc/http/getlatestblockhash
final class GetLatestBlockhashContractTest extends SolanaClientIntegrationTestBase {
    @Test
    void shouldGetLatestBlockhash() throws SolanaJsonRpcClientException {
        final Blockhash latestBlockhash = SOLANA_API.getLatestBlockhash().getResponse();
        assertThat(latestBlockhash.getBlockhashBase58()).isNotBlank();
        assertThat(latestBlockhash.getLastValidBlockHeight()).isGreaterThan(0);
    }

    @Test
    void shouldReturnErrorForMinContextSlotNotReached() throws SolanaJsonRpcClientException {
        final SolanaClientOptionalParams optionalParams = new SolanaJsonRpcClientOptionalParams();
        optionalParams.addParam("minContextSlot", 10000000000L);

        final SolanaClientResponse<Blockhash> response = SOLANA_API.getLatestBlockhash(optionalParams);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError().getErrorCode()).isEqualTo(-32016L);
        assertThat(response.getError().getErrorMessage()).isEqualTo("Minimum context slot has not been reached");
    }
}
