package com.valeamoris.solana4j.client.jsonrpc;

import com.valeamoris.solana4j.client.api.Commitment;
import com.valeamoris.solana4j.client.api.SignatureStatus;
import com.valeamoris.solana4j.client.api.SolanaClientOptionalParams;
import com.valeamoris.solana4j.client.api.SolanaClientResponse;
import com.valeamoris.solana4j.domain.KeyPairGenerator;
import com.valeamoris.solana4j.domain.Sol;
import com.valeamoris.solana4j.encoding.SolanaEncoding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// https://solana.com/docs/rpc/http/getsignaturestatuses
final class GetSignatureStatuesContractTest extends SolanaClientIntegrationTestBase {
    private String address;

    @BeforeEach
    void beforeEach() {
        address = SolanaEncoding.encodeBase58(KeyPairGenerator.generateKeyPair().getPublicKey());
    }


    @Test
    void shouldGetSignatureStatusesDefaultOptionalParams() throws SolanaJsonRpcClientException {
        final String transactionSignature1 = SOLANA_API.requestAirdrop(address, Sol.lamports(BigDecimal.ONE)).getResponse();
        waitForTransactionSuccess(transactionSignature1);
        final String transactionSignature2 = SOLANA_API.requestAirdrop(address, Sol.lamports(BigDecimal.ONE)).getResponse();
        waitForTransactionSuccess(transactionSignature2);
        final String transactionSignature3 = SOLANA_API.requestAirdrop(address, Sol.lamports(BigDecimal.ONE)).getResponse();
        waitForTransactionSuccess(transactionSignature3);

        List<String> transactionSignatures = new ArrayList<>();
        transactionSignatures.add(transactionSignature1);
        transactionSignatures.add(transactionSignature2);
        transactionSignatures.add(transactionSignature3);
        final List<SignatureStatus> response = SOLANA_API.getSignatureStatuses(transactionSignatures).getResponse();

        assertThat(response).hasSize(3);

        final SignatureStatus signatureStatus1 = response.get(0);
        assertThat(signatureStatus1.getErr()).isNull();
        assertThat(signatureStatus1.getSlot()).isGreaterThan(0L);
        assertThat(signatureStatus1.getConfirmationStatus()).isEqualTo(Commitment.FINALIZED);
        assertThat(signatureStatus1.getStatus().getKey()).isEqualTo("Ok");
        // number of blocks since signature confirmation, null if rooted, as well as finalized by a supermajority of the cluster
        // since we're finalized this is going to be null
        assertThat(signatureStatus1.getConfirmations()).isNull();

        final SignatureStatus signatureStatus2 = response.get(1);
        assertThat(signatureStatus2.getErr()).isNull();
        assertThat(signatureStatus2.getSlot()).isGreaterThan(0L);
        assertThat(signatureStatus2.getConfirmationStatus()).isEqualTo(Commitment.FINALIZED);
        assertThat(signatureStatus2.getStatus().getKey()).isEqualTo("Ok");
        // number of blocks since signature confirmation, null if rooted, as well as finalized by a supermajority of the cluster
        // since we're finalized this is going to be null
        assertThat(signatureStatus2.getConfirmations()).isNull();

        final SignatureStatus signatureStatus3 = response.get(2);
        assertThat(signatureStatus3.getErr()).isNull();
        assertThat(signatureStatus3.getSlot()).isGreaterThan(0L);
        assertThat(signatureStatus3.getConfirmationStatus()).isEqualTo(Commitment.FINALIZED);
        assertThat(signatureStatus3.getStatus().getKey()).isEqualTo("Ok");
        // number of blocks since signature confirmation, null if rooted, as well as finalized by a supermajority of the cluster
        // since we're finalized this is going to be null
        assertThat(signatureStatus3.getConfirmations()).isNull();
    }

    @Test
    void shouldGetSignatureStatusesWithSearchTransactionHistoryOptionalParam() throws SolanaJsonRpcClientException {
        // difficult to actually test the effect of this
        final SolanaClientOptionalParams optionalParams = new SolanaJsonRpcClientOptionalParams();
        optionalParams.addParam("searchTransactionHistory", true);

        final String transactionSignature1 = SOLANA_API.requestAirdrop(address, Sol.lamports(BigDecimal.ONE)).getResponse();
        waitForTransactionSuccess(transactionSignature1);
        final String transactionSignature2 = SOLANA_API.requestAirdrop(address, Sol.lamports(BigDecimal.ONE)).getResponse();
        waitForTransactionSuccess(transactionSignature2);
        final String transactionSignature3 = SOLANA_API.requestAirdrop(address, Sol.lamports(BigDecimal.ONE)).getResponse();
        waitForTransactionSuccess(transactionSignature3);

        List<String> transactionSignatures = new ArrayList<>();
        transactionSignatures.add(transactionSignature1);
        transactionSignatures.add(transactionSignature2);
        transactionSignatures.add(transactionSignature3);
        final List<SignatureStatus> response = SOLANA_API.getSignatureStatuses(transactionSignatures, optionalParams).getResponse();

        assertThat(response).hasSize(3);
    }

    @Test
    void shouldReturnErrorIfTransactionFailed() throws SolanaJsonRpcClientException {
        // this should create an error - there is an airdrop limit
        final String transactionSignature1 = SOLANA_API.requestAirdrop(address, Sol.lamports(new BigDecimal("100000000000000"))).getResponse();
        waitForTransactionSuccess(transactionSignature1);

        List<String> transactionSignatures = new ArrayList<>();
        transactionSignatures.add(transactionSignature1);
        final List<SignatureStatus> response = SOLANA_API.getSignatureStatuses(transactionSignatures).getResponse();

        assertThat(response).hasSize(1);
        // some random error
        Map<String, Object> err = new HashMap<>();
        List<Object> instructionError = new ArrayList<>();
        Map<String, Object> custom = new HashMap<>();
        custom.put("Custom", 1);
        instructionError.add(0);
        instructionError.add(custom);
        err.put("InstructionError", instructionError);
        assertThat(response.get(0).getErr()).isEqualTo(err);
    }

    @Test
    void shouldReturnNullResponseForUnknownTransactions() throws SolanaJsonRpcClientException {
        List<String> transactionSignatures = new ArrayList<>();
        transactionSignatures.add("5F3u76cRyDHyWcHkFdRq1p8JLpJK8G8Z1uFbMhsyhRThNxWe4VjhYdLEyaM1wWqGqVt2aZyKPMPj9CMKo4nLhAhN");
        final SolanaClientResponse<List<SignatureStatus>> response = SOLANA_API.getSignatureStatuses(transactionSignatures);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResponse().get(0)).isNull();
    }
}
