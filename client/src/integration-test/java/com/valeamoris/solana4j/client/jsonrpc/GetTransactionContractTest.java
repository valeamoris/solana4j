package com.valeamoris.solana4j.client.jsonrpc;

import com.valeamoris.solana4j.domain.Sol;
import com.valeamoris.solana4j.client.api.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// https://solana.com/docs/rpc/http/gettransaction
final class GetTransactionContractTest extends SolanaClientIntegrationTestBase {
    @Test
    void shouldGetTransactionDefaultOptionalParams() throws SolanaJsonRpcClientException {
        final String transactionSignature = SOLANA_API.requestAirdrop(PAYER, Sol.lamports(BigDecimal.ONE)).getResponse();

        final TransactionResponse response = waitForTransactionSuccess(transactionSignature);

        assertThat(response.getSlot()).isGreaterThan(0);
        assertThat(response.getBlockTime()).isGreaterThan(0);
        assertThat(response.getVersion()).isEqualTo("legacy");

        final List<String> encodedTransactionData = response.getTransactionData().getEncodedTransactionData();
        assertThat(encodedTransactionData.get(0)).isNotEmpty();
        assertThat(encodedTransactionData.get(1)).isEqualTo("base64");

        final TransactionResponse.TransactionMetadata metadata = response.getMetadata();
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

    @Test
    void shouldGetTokenTransactionDefaultOptionalParams() throws SolanaJsonRpcClientException {
        final TransactionResponse response = SOLANA_API.getTransaction(tokenMintTransactionSignature1).getResponse();

        final List<TransactionResponse.TransactionMetadata.TokenBalance> preTokenBalances = response.getMetadata().getPreTokenBalances();
        assertThat(preTokenBalances).hasSize(1);
        final TransactionResponse.TransactionMetadata.TokenBalance preTokenBalance = preTokenBalances.get(0);
        assertThat(preTokenBalance.getAccountIndex()).isEqualTo(3);
        assertThat(preTokenBalance.getOwner()).isEqualTo("7H1itW7F72uJbaXK2R4gP7J18HrQ2M683kL9YgUeeUHr");
        assertThat(preTokenBalance.getMint()).isEqualTo("2tokpcExDmewsSNRKuTLVLMUseiSkEdBQWBjeQLmuFaS");
        assertThat(preTokenBalance.getProgramId()).isEqualTo("TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb");
        final TokenAmount preUiTokenAmount = preTokenBalance.getUiTokenAmount();
        assertThat(preUiTokenAmount.getDecimals()).isEqualTo(18);

        final List<TransactionResponse.TransactionMetadata.TokenBalance> postTokenBalances = response.getMetadata().getPostTokenBalances();
        assertThat(postTokenBalances).hasSize(1);
        final TransactionResponse.TransactionMetadata.TokenBalance postTokenBalance = postTokenBalances.get(0);
        assertThat(postTokenBalance.getAccountIndex()).isEqualTo(3);
        assertThat(postTokenBalance.getOwner()).isEqualTo("7H1itW7F72uJbaXK2R4gP7J18HrQ2M683kL9YgUeeUHr");
        assertThat(postTokenBalance.getMint()).isEqualTo("2tokpcExDmewsSNRKuTLVLMUseiSkEdBQWBjeQLmuFaS");
        assertThat(postTokenBalance.getProgramId()).isEqualTo("TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb");
        final TokenAmount postUiTokenAmount = postTokenBalance.getUiTokenAmount();
        assertThat(postUiTokenAmount.getDecimals()).isEqualTo(18);
        assertThat(postUiTokenAmount.getUiAmount()).isEqualTo(0.0f);
        assertThat(postUiTokenAmount.getUiAmountString()).isEqualTo("0.00000000000000001");
        assertThat(postUiTokenAmount.getAmount()).isEqualTo("10");
    }

    @Test
    void shouldGetTransactionBase58EncodingOptionalParam() throws SolanaJsonRpcClientException {
        final String transactionSignature = SOLANA_API.requestAirdrop(PAYER, Sol.lamports(BigDecimal.ONE)).getResponse();
        final SolanaClientOptionalParams optionalParams = new SolanaJsonRpcClientOptionalParams();
        optionalParams.addParam("encoding", "base58");

        final TransactionResponse response = waitForTransactionSuccess(transactionSignature, Optional.of(optionalParams));

        final List<String> encodedTransactionData = response.getTransactionData().getEncodedTransactionData();
        assertThat(encodedTransactionData.get(0)).isNotEmpty();
        assertThat(encodedTransactionData.get(1)).isEqualTo("base58");
    }

    @Test
    void shouldGetTransactionJsonEncodingOptionalParam() throws SolanaJsonRpcClientException {
        final String transactionSignature = SOLANA_API.requestAirdrop(PAYER, Sol.lamports(BigDecimal.ONE)).getResponse();
        final SolanaClientOptionalParams optionalParams = new SolanaJsonRpcClientOptionalParams();
        optionalParams.addParam("encoding", "json");

        final TransactionResponse response = waitForTransactionSuccess(transactionSignature, Optional.of(optionalParams));

        final TransactionResponse.TransactionData.TransactionDataParsed parsedTransactionData = response.getTransactionData().getParsedTransactionData();

        final TransactionResponse.Message message = parsedTransactionData.getMessage();
        assertThat(message.getRecentBlockhash()).isNotNull();
        assertThat(message.getAccountKeys().getEncodedAccountKeys()).hasSize(3);

        final TransactionResponse.Message.Header messageHeader = message.getHeader();
        assertThat(messageHeader.getNumReadonlySignedAccounts()).isEqualTo(0);
        assertThat(messageHeader.getNumReadonlyUnsignedAccounts()).isEqualTo(1);
        assertThat(messageHeader.getNumReadonlySignedAccounts()).isEqualTo(0);

        assertThat(message.getInstructions().size()).isEqualTo(1);

        final Instruction instruction = message.getInstructions().get(0);
        assertThat(instruction.getData()).isEqualTo("3Bxs3zzLZLuLQEYX");
        assertThat(instruction.getAccounts()).hasSize(2);
        assertThat(instruction.getAccounts().get(0)).isEqualTo(0);
        assertThat(instruction.getAccounts().get(1)).isEqualTo(1);
        assertThat(instruction.getProgramIdIndex()).isEqualTo(2);
        assertThat(instruction.getStackHeight()).isEqualTo(null);

        assertThat(message.getRecentBlockhash()).isNotEmpty();

        final List<String> signatures = parsedTransactionData.getSignatures();
        assertThat(signatures).hasSize(1);
    }

    @Test
    void shouldGetTransactionJsonParsedEncodingOptionalParam() throws SolanaJsonRpcClientException {
        final String transactionSignature = SOLANA_API.requestAirdrop(PAYER, Sol.lamports(BigDecimal.ONE)).getResponse();
        final SolanaClientOptionalParams optionalParams = new SolanaJsonRpcClientOptionalParams();
        optionalParams.addParam("encoding", "jsonParsed");

        final TransactionResponse response = waitForTransactionSuccess(transactionSignature, Optional.of(optionalParams));

        final TransactionResponse.TransactionData.TransactionDataParsed parsedTransactionData = response.getTransactionData().getParsedTransactionData();

        final TransactionResponse.Message message = parsedTransactionData.getMessage();
        assertThat(message.getRecentBlockhash()).isNotNull();

        final List<TransactionResponse.Message.AccountKeys.AccountKeyParsed> parsedAccountKeys = message.getAccountKeys().getParsedAccountKeys();
        assertThat(parsedAccountKeys).hasSize(3);

        assertThat(parsedAccountKeys.get(0).isSigner()).isTrue();
        assertThat(parsedAccountKeys.get(0).isWritable()).isTrue();
        assertThat(parsedAccountKeys.get(0).getSource()).isEqualTo(TransactionResponse.Message.AccountKeys.AccountKeyParsed.KeySource.TRANSACTION);
        assertThat(parsedAccountKeys.get(0).getKey()).isNotEmpty();

        assertThat(parsedAccountKeys.get(1).isSigner()).isFalse();
        assertThat(parsedAccountKeys.get(1).isWritable()).isTrue();
        assertThat(parsedAccountKeys.get(1).getSource()).isEqualTo(TransactionResponse.Message.AccountKeys.AccountKeyParsed.KeySource.TRANSACTION);
        assertThat(parsedAccountKeys.get(1).getKey()).isNotEmpty();

        assertThat(parsedAccountKeys.get(2).isSigner()).isFalse();
        assertThat(parsedAccountKeys.get(2).isWritable()).isFalse();
        assertThat(parsedAccountKeys.get(2).getSource()).isEqualTo(TransactionResponse.Message.AccountKeys.AccountKeyParsed.KeySource.TRANSACTION);
        assertThat(parsedAccountKeys.get(2).getKey()).isEqualTo("11111111111111111111111111111111");

        assertThat(message.getHeader()).isNull();

        assertThat(message.getInstructions()).hasSize(1);

        final Instruction instruction = message.getInstructions().get(0);

        // would have been present if encoding json not jsonParsed
        assertThat(instruction.getData()).isNull();
        assertThat(instruction.getAccounts()).isNull();
        assertThat(instruction.getProgramIdIndex()).isNull();
        assertThat(instruction.getStackHeight()).isEqualTo(null);

        // i think the best we can do here is really just return a Map<String, Object> and let the user do their own parsing
        // since the parsing is very much program specific
        assertThat(instruction.getProgram()).isEqualTo("system");
        assertThat(instruction.getProgramId()).isEqualTo("11111111111111111111111111111111");

        final Map<String, Object> expected = new HashMap<>();
        expected.put("destination", "sCR7NonpU3TrqvusEiA4MAwDMLfiY1gyVPqw2b36d8V");
        expected.put("lamports", 1000000000);
        expected.put("source", "ignoredAsItChanges");
        final Map<String, Object> parsedInstruction = instruction.getInstructionParsed();
        assertThat(parsedInstruction.get("type")).isEqualTo("transfer");
        assertThat(parsedInstruction.get("info"))
                .usingRecursiveComparison()
                .ignoringFields("source")
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnNullForUnknownTransactionSignature() throws SolanaJsonRpcClientException {
        assertThat(SOLANA_API.getTransaction("3wBQpRDgEKgNhbGJGzxfELHTyFas8mvf4x6bLWC989kBpgEVXPnwWS3tg33WEhVxnqbBTVXEQjmHun2tTbxHzSo").getResponse()).isNull();
    }

    @Test
    void shouldReturnErrorForMalformedTransactionSignature() throws SolanaJsonRpcClientException {
        final SolanaClientResponse<TransactionResponse> transaction = SOLANA_API.getTransaction("iamamalformedtransactionsignature");
        assertThat(transaction.isSuccess()).isFalse();
        assertThat(transaction.getError().getErrorCode()).isEqualTo(-32602L);
        assertThat(transaction.getError().getErrorMessage()).isEqualTo("Invalid param: Invalid");
    }
}
