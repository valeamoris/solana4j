package com.valeamoris.solana4j.client.jsonrpc;

import com.valeamoris.solana4j.client.api.*;
import com.valeamoris.solana4j.domain.Sol;
import jdk.nashorn.internal.ir.Block;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

// https://solana.com/docs/rpc/http/gettransaction
final class GetBlockContractTest extends SolanaClientIntegrationTestBase {
    @Test
    void shouldGetBlockDefaultOptionalParams() throws SolanaJsonRpcClientException {
        final String transactionSignature = SOLANA_API.requestAirdrop(PAYER, Sol.lamports(BigDecimal.ONE)).getResponse();

        System.out.println("transactionSignature: " + transactionSignature);
        final TransactionResponse response = waitForTransactionSuccess(transactionSignature);
        assertThat(response.getSlot()).isGreaterThan(0);
        assertThat(response.getBlockTime()).isGreaterThan(0);
        assertThat(response.getVersion()).isEqualTo("legacy");

        final long slot = response.getSlot();
        final BlockResponse blockResponse = SOLANA_API.getBlock(slot).getResponse();
        System.out.println(blockResponse);

        assertThat(blockResponse.getBlockHeight()).isGreaterThan(0);
        assertThat(blockResponse.getBlockTime()).isGreaterThan(0);
        assertThat(blockResponse.getBlockhash()).isNotEmpty();

        final List<BlockResponse.Transaction> transactions = blockResponse.getTransactions();
        assertThat(transactions.size()).isGreaterThan(0);

        final BlockResponse.Transaction transaction = transactions.get(0);

        final List<String> encodedTransactionData = transaction.getTransactionData().getEncodedTransactionData();
        assertThat(encodedTransactionData.get(0)).isNotEmpty();
        assertThat(encodedTransactionData.get(1)).isEqualTo("base64");

        final BlockResponse.TransactionMetadata metadata = transaction.getTransactionMetadata();
        assertThat(metadata.getErr()).isNull();
        assertThat(metadata.getFee()).isGreaterThan(0);
        assertThat(metadata.getInnerInstructions()).isEmpty();
        assertThat(metadata.getLogMessages()).hasSize(2);
        assertThat(metadata.getPreBalances()).hasSize(3);
        assertThat(metadata.getPostBalances()).hasSize(3);
        assertThat(metadata.getPreTokenBalances()).isEmpty();
        assertThat(metadata.getPostTokenBalances()).isEmpty();
        assertThat(metadata.getRewards()).isEmpty();
        assertThat(metadata.getComputeUnitsConsumed()).isGreaterThan(0);
        assertThat(metadata.getLoadedAddresses().getReadonly()).isEmpty();
        assertThat(metadata.getLoadedAddresses().getWritable()).isEmpty();
        assertThat(metadata.getStatus().getKey()).isEqualTo("Ok");
    }


}
