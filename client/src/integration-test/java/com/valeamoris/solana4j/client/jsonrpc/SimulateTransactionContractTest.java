package com.valeamoris.solana4j.client.jsonrpc;

import com.valeamoris.solana4j.Solana;
import com.valeamoris.solana4j.client.api.AccountInfo;
import com.valeamoris.solana4j.client.api.SimulateTransactionResponse;
import com.valeamoris.solana4j.client.api.SolanaClientOptionalParams;
import com.valeamoris.solana4j.client.api.SolanaClientResponse;
import com.valeamoris.solana4j.encoding.SolanaEncoding;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

// https://solana.com/docs/rpc/http/simulatetransaction
final class SimulateTransactionContractTest extends SolanaClientIntegrationTestBase {
    private String mintToTransactionBlobBase58;
    private String mintToTransactionBlobBase64;
    private String transactionBlobBase64BadSignatures;
    private String transactionBlobBase64ReplaceBlockhash;

    @BeforeEach
    void beforeEach() throws SolanaJsonRpcClientException {
        final String latestBlockhash = SOLANA_API.getLatestBlockhash().getResponse().getBlockhashBase58();

        List<Solana4jJsonRpcTestHelper.Signer> signers = new ArrayList<>();
        signers.add(new Solana4jJsonRpcTestHelper.Signer(Solana.account(PAYER), SolanaEncoding.decodeBase58(PAYER_PRIV)));
        signers.add(new Solana4jJsonRpcTestHelper.Signer(Solana.account(TOKEN_MINT_AUTHORITY), SolanaEncoding.decodeBase58(TOKEN_MINT_AUTHORITY_PRIV)));
        final byte[] successfulMintToTransactionBytes = Solana4jJsonRpcTestHelper.createMintToTransactionBlob(
                Solana.account(PAYER),
                Solana.blockhash(latestBlockhash),
                Solana.account(TOKEN_MINT),
                Solana.account(TOKEN_MINT_AUTHORITY),
                Solana.destination(Solana.account(TOKEN_ACCOUNT_1), 10),
                signers
        );

        mintToTransactionBlobBase58 = SolanaEncoding.encodeBase58(successfulMintToTransactionBytes);
        mintToTransactionBlobBase64 = Base64.getEncoder().encodeToString(successfulMintToTransactionBytes);

        signers.clear();
        signers.add(new Solana4jJsonRpcTestHelper.Signer(Solana.account(PAYER), SolanaEncoding.decodeBase58(PAYER)));
        signers.add(new Solana4jJsonRpcTestHelper.Signer(Solana.account(TOKEN_MINT_AUTHORITY), SolanaEncoding.decodeBase58(TOKEN_MINT_AUTHORITY)));
        final byte[] badSignaturesTransactionBytes = Solana4jJsonRpcTestHelper.createMintToTransactionBlob(
                Solana.account(PAYER),
                Solana.blockhash(latestBlockhash),
                Solana.account(TOKEN_MINT),
                Solana.account(TOKEN_MINT_AUTHORITY),
                Solana.destination(Solana.account(TOKEN_ACCOUNT_1), 10),
                signers
        );

        transactionBlobBase64BadSignatures = Base64.getEncoder().encodeToString(badSignaturesTransactionBytes);

        signers.clear();
        signers.add(new Solana4jJsonRpcTestHelper.Signer(Solana.account(PAYER), SolanaEncoding.decodeBase58(PAYER_PRIV)));
        signers.add(new Solana4jJsonRpcTestHelper.Signer(Solana.account(TOKEN_MINT_AUTHORITY), SolanaEncoding.decodeBase58(TOKEN_MINT_AUTHORITY_PRIV)));
        final byte[] replaceBlockhashTransactionBytes = Solana4jJsonRpcTestHelper.createMintToTransactionBlob(
                Solana.account(PAYER),
                Solana.blockhash("DVPPT9tfXkTVjA371ND18Vs2U27CMah8hCMbG2yofzqH"),
                Solana.account(TOKEN_MINT),
                Solana.account(TOKEN_MINT_AUTHORITY),
                Solana.destination(Solana.account(TOKEN_ACCOUNT_1), 10),
                signers
        );

        transactionBlobBase64ReplaceBlockhash = Base64.getEncoder().encodeToString(replaceBlockhashTransactionBytes);
    }

    @Test
    void shouldSimulateTransactionDefaultOptionalParams() throws SolanaJsonRpcClientException {
        final SimulateTransactionResponse response = SOLANA_API.simulateTransaction(mintToTransactionBlobBase64).getResponse();

        Assertions.assertThat(response.getUnitsConsumed()).isEqualTo(958);
        Assertions.assertThat(response.getLogs()).hasSize(4);
        Assertions.assertThat(response.getInnerInstructions()).isNull();
        Assertions.assertThat(response.getReplacementBlockhash()).isNull();
        Assertions.assertThat(response.getAccounts()).isNull();
        Assertions.assertThat(response.getReturnData()).isNull();
        Assertions.assertThat(response.getErr()).isNull();
    }

    @Test
    void shouldSimulateTransactionSigVerifyTrueOptionalParam() throws SolanaJsonRpcClientException {
        final SolanaClientOptionalParams optionalParams = new SolanaJsonRpcClientOptionalParams();
        optionalParams.addParam("encoding", "base64");
        optionalParams.addParam("sigVerify", true);

        final SolanaClientResponse<SimulateTransactionResponse> response = SOLANA_API.simulateTransaction(transactionBlobBase64BadSignatures, optionalParams);

        Assertions.assertThat(response.isSuccess()).isFalse();
        // error because the signatures are invalid
        Assertions.assertThat(response.getError().getErrorCode()).isEqualTo(-32003L);
        Assertions.assertThat(response.getError().getErrorMessage()).isEqualTo("Transaction signature verification failure");
    }

    @Test
    void shouldSimulateTransactionSigVerifyFalseOptionalParam() throws SolanaJsonRpcClientException {
        final SolanaClientOptionalParams optionalParams = new SolanaJsonRpcClientOptionalParams();
        optionalParams.addParam("encoding", "base64");
        optionalParams.addParam("sigVerify", false);

        final SolanaClientResponse<SimulateTransactionResponse> response = SOLANA_API.simulateTransaction(transactionBlobBase64BadSignatures, optionalParams);

        // sigVerify false so not looking at signatures
        Assertions.assertThat(response.isSuccess()).isTrue();
    }

    @Test
    void shouldSimulateTransactionReplaceRecentBlockhashFalseOptionalParam() throws SolanaJsonRpcClientException {
        final SolanaClientOptionalParams optionalParams = new SolanaJsonRpcClientOptionalParams();
        optionalParams.addParam("encoding", "base64");
        optionalParams.addParam("replaceRecentBlockhash", false);

        final SolanaClientResponse<SimulateTransactionResponse> response = SOLANA_API.simulateTransaction(transactionBlobBase64ReplaceBlockhash, optionalParams);

        Assertions.assertThat(response.isSuccess()).isTrue();
        // error because the invalid blockhash not replaced
        Assertions.assertThat(response.getResponse().getErr()).isNotNull();
    }


    @Test
    void shouldSimulateTransactionReplaceRecentBlockhashTrueOptionalParam() throws SolanaJsonRpcClientException {
        final SolanaClientOptionalParams optionalParams = new SolanaJsonRpcClientOptionalParams();
        optionalParams.addParam("encoding", "base64");
        optionalParams.addParam("replaceRecentBlockhash", true);

        final SolanaClientResponse<SimulateTransactionResponse> response = SOLANA_API.simulateTransaction(transactionBlobBase64ReplaceBlockhash, optionalParams);

        Assertions.assertThat(response.isSuccess()).isTrue();
        // no error because the invalid blockhash replaced
        Assertions.assertThat(response.getResponse().getErr()).isNull();
    }

    @Test
    void shouldSimulateTransactionEncodingBase58OptionalParam() throws SolanaJsonRpcClientException {
        final SolanaClientOptionalParams optionalParams = new SolanaJsonRpcClientOptionalParams();
        optionalParams.addParam("encoding", "base58");

        final SolanaClientResponse<SimulateTransactionResponse> response = SOLANA_API.simulateTransaction(mintToTransactionBlobBase58, optionalParams);

        Assertions.assertThat(response.getResponse().getUnitsConsumed()).isEqualTo(958);
        Assertions.assertThat(response.getResponse().getLogs()).hasSize(4);
        Assertions.assertThat(response.getResponse().getErr()).isNull();
    }

    @Test
    void shouldSimulateTransactionInnerInstructionsOptionalParam() throws SolanaJsonRpcClientException {
        final SolanaClientOptionalParams optionalParams = new SolanaJsonRpcClientOptionalParams();
        optionalParams.addParam("encoding", "base64");
        optionalParams.addParam("innerInstructions", true);

        final SolanaClientResponse<SimulateTransactionResponse> response = SOLANA_API.simulateTransaction(mintToTransactionBlobBase64, optionalParams);

        // both fields require cross program interactions deemed out of scope of this test
        // inner instructions will be empty and return data will be null
        Assertions.assertThat(response.getResponse().getInnerInstructions()).hasSize(0);
        Assertions.assertThat(response.getResponse().getReturnData()).isNull();
    }

    @Test
    void shouldSimulateTransactionAccountsConfigurationBase64OptionalParam() throws SolanaJsonRpcClientException {
        final SolanaClientOptionalParams optionalParams = new SolanaJsonRpcClientOptionalParams();
        optionalParams.addParam("encoding", "base64");

        Map<String, Object> accounts = new HashMap<>();
        accounts.put("encoding", "base64");
        List<String> addrs = new ArrayList<>();
        addrs.add(TOKEN_ACCOUNT_1);
        accounts.put("addresses", addrs);
        optionalParams.addParam("accounts", accounts);

        final SolanaClientResponse<SimulateTransactionResponse> response = SOLANA_API.simulateTransaction(mintToTransactionBlobBase64, optionalParams);

        assertThat(response.isSuccess()).isTrue();

        Assertions.assertThat(response.getResponse().getAccounts()).hasSize(1);

        final AccountInfo accountInfo = response.getResponse().getAccounts().get(0);
        assertThat(accountInfo.getSpace()).isEqualTo(165L);
        assertThat(accountInfo.getRentEpoch()).isEqualTo("18446744073709551615");
        assertThat(accountInfo.getOwner()).isEqualTo("TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb");
        assertThat(accountInfo.getLamports()).isEqualTo(400000L);

        final AccountInfo.AccountInfoData data = accountInfo.getData();
        assertThat(data.getAccountInfoParsed()).isNull();
        assertThat(data.getAccountInfoEncoded().get(0)).isEqualTo(
                "HCEswh8utCXQBqnsIw8mz0OFQo9N7bLx3WTVx5o6l4tdQDspqrYRFt5A4y1L6LHNdNnOQ7IdPHtkqYF/Z+a01" +
                        "RQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAA" +
                        "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        assertThat(data.getAccountInfoEncoded().get(1)).isEqualTo("base64");
    }

    @Test
    void shouldSimulateTransactionAccountsConfigurationBase58OptionalParam() throws SolanaJsonRpcClientException {
        final SolanaClientOptionalParams optionalParams = new SolanaJsonRpcClientOptionalParams();
        optionalParams.addParam("encoding", "base64");
        Map<String, Object> accounts = new HashMap<>();
        accounts.put("encoding", "base58");
        List<String> addrs = new ArrayList<>();
        addrs.add(TOKEN_ACCOUNT_1);
        accounts.put("addresses", addrs);
        optionalParams.addParam("accounts", accounts);

        final SolanaClientResponse<SimulateTransactionResponse> response = SOLANA_API.simulateTransaction(mintToTransactionBlobBase64, optionalParams);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError().getErrorCode()).isEqualTo(-32602L);
        assertThat(response.getError().getErrorMessage()).isEqualTo("base58 encoding not supported");
    }

    @Test
    void shouldSimulateTransactionAccountsConfigurationBase64ZstdOptionalParam() throws SolanaJsonRpcClientException {
        final SolanaClientOptionalParams optionalParams = new SolanaJsonRpcClientOptionalParams();
        optionalParams.addParam("encoding", "base64");
        Map<String, Object> accounts = new HashMap<>();
        accounts.put("encoding", "base64+zstd");
        List<String> addrs = new ArrayList<>();
        addrs.add(TOKEN_ACCOUNT_1);
        accounts.put("addresses", addrs);
        optionalParams.addParam("accounts", accounts);

        final SolanaClientResponse<SimulateTransactionResponse> response = SOLANA_API.simulateTransaction(mintToTransactionBlobBase64, optionalParams);

        assertThat(response.isSuccess()).isTrue();

        Assertions.assertThat(response.getResponse().getAccounts()).hasSize(1);

        final AccountInfo.AccountInfoData data = response.getResponse().getAccounts().get(0).getData();
        assertThat(data.getAccountInfoParsed()).isNull();
        assertThat(data.getAccountInfoEncoded().get(0)).isEqualTo(
                "KLUv/QBYdQIARAQcISzCHy60JdAGqewjDybPQ4VCj03tsvHdZNXHmjqXi11A" +
                        "OymqthEW3kDjLUvosc102c5Dsh08e2SpgX9n5rTVFAABAAIABCcKIXAG");
        assertThat(data.getAccountInfoEncoded().get(1)).isEqualTo("base64+zstd");
    }

    @Test
    void shouldSimulateTransactionAccountsConfigurationJsonParsedOptionalParam() throws SolanaJsonRpcClientException {
        final SolanaClientOptionalParams optionalParams = new SolanaJsonRpcClientOptionalParams();
        optionalParams.addParam("encoding", "base64");
        Map<String, Object> accounts = new HashMap<>();
        accounts.put("encoding", "jsonParsed");
        List<String> addrs = new ArrayList<>();
        addrs.add(TOKEN_ACCOUNT_1);
        accounts.put("addresses", addrs);
        optionalParams.addParam("accounts", accounts);

        final SolanaClientResponse<SimulateTransactionResponse> response = SOLANA_API.simulateTransaction(mintToTransactionBlobBase64, optionalParams);

        assertThat(response.isSuccess()).isTrue();

        Assertions.assertThat(response.getResponse().getAccounts()).hasSize(1);

        final AccountInfo.AccountInfoData data = response.getResponse().getAccounts().get(0).getData();
        assertThat(data.getAccountInfoEncoded()).isNull();

        final AccountInfo.AccountInfoData.AccountInfoParsedData parsedAccountInfo = data.getAccountInfoParsed();
        assertThat(parsedAccountInfo.getProgram()).isEqualTo("spl-token-2022");
        assertThat(parsedAccountInfo.getSpace()).isEqualTo(165);

        // i think the best we can do here is really just return a Map<String, Object> and let the user do their own parsing
        // since the parsing is very much program specific
        Assertions.assertThat(parsedAccountInfo.getParsedData().get("type")).isEqualTo("account");
        Map<String, Object> expected = new HashMap<>();
        expected.put("isNative", false);
        expected.put("mint", "2tokpcExDmewsSNRKuTLVLMUseiSkEdBQWBjeQLmuFaS");
        expected.put("owner", "7H1itW7F72uJbaXK2R4gP7J18HrQ2M683kL9YgUeeUHr");
        expected.put("state", "initialized");
        Map<String, Object> tokenAmount = new HashMap<>();
        tokenAmount.put("amount", "20");
        tokenAmount.put("decimals", 18);
        tokenAmount.put("uiAmount", 2.0E-17);
        tokenAmount.put("uiAmountString", "0.00000000000000002");
        expected.put("tokenAmount", tokenAmount);

        Assertions.assertThat(parsedAccountInfo.getParsedData().get("info")).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void shouldReturnErrorForMinContextSlotNotReached() throws SolanaJsonRpcClientException {
        final SolanaClientOptionalParams optionalParams = new SolanaJsonRpcClientOptionalParams();
        optionalParams.addParam("encoding", "base64");
        optionalParams.addParam("minContextSlot", 10000000000L);

        final SolanaClientResponse<String> response = SOLANA_API.sendTransaction(mintToTransactionBlobBase64, optionalParams);

        Assertions.assertThat(response.isSuccess()).isFalse();
        Assertions.assertThat(response.getError().getErrorCode()).isEqualTo(-32016L);
        Assertions.assertThat(response.getError().getErrorMessage()).isEqualTo("Minimum context slot has not been reached");
    }
}

