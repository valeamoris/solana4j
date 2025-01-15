package com.valeamoris.solana4j.client.jsonrpc;

import com.valeamoris.solana4j.client.api.SolanaClientOptionalParams;
import com.valeamoris.solana4j.client.api.SolanaClientResponse;
import com.valeamoris.solana4j.client.api.TokenAccount;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

// https://solana.com/docs/rpc/http/gettokenaccountsbyowner
final class GetTokenAccountsByOwnerContractTest extends SolanaClientIntegrationTestBase
{
    @Test
    void shouldGetTokenAccountsByOwnerWithMintDefaultOptionalParams() throws SolanaJsonRpcClientException
    {
        // 7H1itW7F72uJbaXK2R4gP7J18HrQ2M683kL9YgUeeUHr - (is the owner of the token account - see shouldGetTokenAccountInfoJsonParsedEncodingOptionalParam)
        Map.Entry<String, String> mint = new AbstractMap.SimpleEntry<>("mint", "2tokpcExDmewsSNRKuTLVLMUseiSkEdBQWBjeQLmuFaS");
        final List<TokenAccount> response = SOLANA_API.getTokenAccountsByOwner("7H1itW7F72uJbaXK2R4gP7J18HrQ2M683kL9YgUeeUHr", mint).getResponse();

        assertThat(response).hasSize(2);
        assertThat(response.get(1).getPublicKey()).isEqualTo(TOKEN_ACCOUNT_1);
        assertThat(response.get(1).getAccountInfo().getOwner()).isEqualTo("TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb");
        assertThat(response.get(0).getPublicKey()).isEqualTo(TOKEN_ACCOUNT_2);
        assertThat(response.get(0).getAccountInfo().getOwner()).isEqualTo("TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb");
    }

    @Test
    void shouldGetTokenAccountsByOwnerDataSliceOptionalParam() throws SolanaJsonRpcClientException
    {
        Map.Entry<String, String> mint = new AbstractMap.SimpleEntry<>("mint", "2tokpcExDmewsSNRKuTLVLMUseiSkEdBQWBjeQLmuFaS");
        Map<String, Object> dataSlice = new HashMap<>();
        dataSlice.put("length", 10);
        dataSlice.put("offset", 10);
        final SolanaClientOptionalParams optionalParams = new SolanaJsonRpcClientOptionalParams();
        optionalParams.addParam("dataSlice", dataSlice);
        optionalParams.addParam("encoding", "base64");

        final List<TokenAccount> response = SOLANA_API.getTokenAccountsByOwner(
                        "7H1itW7F72uJbaXK2R4gP7J18HrQ2M683kL9YgUeeUHr",
                        mint,
                        optionalParams)
                .getResponse();

        assertThat(response.get(0).getAccountInfo().getData().getAccountInfoEncoded().get(0)).isEqualTo("qewjDybPQ4VCjw==");
        assertThat(response.get(0).getAccountInfo().getData().getAccountInfoEncoded().get(1)).isEqualTo("base64");
        assertThat(response.get(0).getAccountInfo().getData().getAccountInfoParsed()).isNull();
    }

    @Test
    void shouldGetTokenAccountsByOwnerBase58EncodingOptionalParam() throws SolanaJsonRpcClientException
    {
        Map.Entry<String, String> mint = new AbstractMap.SimpleEntry<>("mint", "2tokpcExDmewsSNRKuTLVLMUseiSkEdBQWBjeQLmuFaS");
        final SolanaClientOptionalParams optionalParams = new SolanaJsonRpcClientOptionalParams();
        optionalParams.addParam("encoding", "base58");

        final SolanaClientResponse<List<TokenAccount>> response = SOLANA_API.getTokenAccountsByOwner(
                "7H1itW7F72uJbaXK2R4gP7J18HrQ2M683kL9YgUeeUHr",
                mint,
                optionalParams);

        // base58 encoding account data to be less than 128 bytes - wouldn't this always basically be the case for token accounts?
        // not sure why they'd offer base58 encoding here at all, but whatever
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError().getErrorCode()).isEqualTo(-32600L);
        assertThat(response.getError().getErrorMessage()).isEqualTo("Encoded binary (base 58) data should be less than 128 bytes, please use Base64 encoding.");
    }

    @Test
    void shouldGetTokenAccountsByOwnerBase64ZstdEncodingOptionalParam() throws SolanaJsonRpcClientException
    {
        Map.Entry<String, String> mint = new AbstractMap.SimpleEntry<>("mint", "2tokpcExDmewsSNRKuTLVLMUseiSkEdBQWBjeQLmuFaS");
        final SolanaClientOptionalParams optionalParams = new SolanaJsonRpcClientOptionalParams();
        optionalParams.addParam("encoding", "base64+zstd");

        final List<TokenAccount> response = SOLANA_API.getTokenAccountsByOwner(
                        "7H1itW7F72uJbaXK2R4gP7J18HrQ2M683kL9YgUeeUHr",
                        mint,
                        optionalParams)
                .getResponse();

        assertThat(response.get(0).getAccountInfo().getData().getAccountInfoEncoded().get(0)).isEqualTo(
                "KLUv/QBYbQIANAQcISzCHy60JdAGqewjDybPQ4VCj03tsvHdZNXHmjqXi11AOymqthEW3kDjLUvosc102c5Dsh08e2SpgX9n5rTVAAEAAgAEJwaY4Aw=");
        assertThat(response.get(0).getAccountInfo().getData().getAccountInfoEncoded().get(1)).isEqualTo("base64+zstd");
        assertThat(response.get(0).getAccountInfo().getData().getAccountInfoParsed()).isNull();
    }

    @Test
    void shouldGetTokenAccountsByOwnerJsonParsedEncodingOptionalParam() throws SolanaJsonRpcClientException
    {
        Map.Entry<String, String> mint = new AbstractMap.SimpleEntry<>("mint", "2tokpcExDmewsSNRKuTLVLMUseiSkEdBQWBjeQLmuFaS");
        final SolanaClientOptionalParams optionalParams = new SolanaJsonRpcClientOptionalParams();
        optionalParams.addParam("encoding", "jsonParsed");

        final List<TokenAccount> response = SOLANA_API.getTokenAccountsByOwner(
                        "7H1itW7F72uJbaXK2R4gP7J18HrQ2M683kL9YgUeeUHr",
                       mint,
                        optionalParams)
                .getResponse();

        assertThat(response.get(0).getAccountInfo().getData().getAccountInfoEncoded()).isNull();
        // already checked these fields in another test
        assertThat(response.get(0).getAccountInfo().getData().getAccountInfoParsed()).isNotNull();
    }

    @Test
    void shouldGetTokenAccountsByOwnerWithProgramIdDefaultOptionalParams() throws SolanaJsonRpcClientException
    {
        Map.Entry<String, String> programId = new AbstractMap.SimpleEntry<>("programId", "TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb");
        final List<TokenAccount> response = SOLANA_API.getTokenAccountsByOwner(
                        "7H1itW7F72uJbaXK2R4gP7J18HrQ2M683kL9YgUeeUHr",
                        programId)
                .getResponse();

        assertThat(response).hasSize(2);
        assertThat(response.get(1).getPublicKey()).isEqualTo(TOKEN_ACCOUNT_1);
        assertThat(response.get(1).getAccountInfo().getOwner()).isEqualTo("TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb");
        assertThat(response.get(0).getPublicKey()).isEqualTo(TOKEN_ACCOUNT_2);
        assertThat(response.get(0).getAccountInfo().getOwner()).isEqualTo("TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb");
    }

    @Test
    void shouldReturnErrorForUnrecognisedTokenMint() throws SolanaJsonRpcClientException
    {
        Map.Entry<String, String> mint = new AbstractMap.SimpleEntry<>("mint", "FakeMintwXuSgYf3AyUgmw1vLq1XawuqxeXNwbda4Kg");
        final SolanaClientResponse<List<TokenAccount>> response = SOLANA_API.getTokenAccountsByOwner(
                "7H1itW7F72uJbaXK2R4gP7J18HrQ2M683kL9YgUeeUHr",
                mint);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError().getErrorCode()).isEqualTo(-32602L);
        assertThat(response.getError().getErrorMessage()).isEqualTo("Invalid param: could not find mint");
    }

    @Test
    void shouldReturnErrorForUnrecognisedTokenProgramId() throws SolanaJsonRpcClientException
    {
        Map.Entry<String, String> programId = new AbstractMap.SimpleEntry<>("programId", "TokenFakeNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb");
        final SolanaClientResponse<List<TokenAccount>>  response = SOLANA_API.getTokenAccountsByOwner(
                "7H1itW7F72uJbaXK2R4gP7J18HrQ2M683kL9YgUeeUHr",
                programId);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError().getErrorCode()).isEqualTo(-32602L);
        assertThat(response.getError().getErrorMessage()).isEqualTo("Invalid param: unrecognized Token program id");
    }

    @Test
    void shouldReturnErrorForMalformedAccountDelegate() throws SolanaJsonRpcClientException
    {
        Map.Entry<String, String> mint = new AbstractMap.SimpleEntry<>("mint", "FakeMintwXuSgYf3AyUgmw1vLq1XawuqxeXNwbda4Kg");
        final SolanaClientResponse<List<TokenAccount>>  response = SOLANA_API.getTokenAccountsByOwner("InvalidKeyXYZ123!@#%^&*()NotAValidPublicKey", mint);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError().getErrorCode()).isEqualTo(-32602L);
        assertThat(response.getError().getErrorMessage()).isEqualTo("Invalid param: Invalid");
    }

    @Test
    void shouldReturnErrorForMinContextSlotNotReached() throws SolanaJsonRpcClientException
    {
        Map.Entry<String, String> mint = new AbstractMap.SimpleEntry<>("mint", "2tokpcExDmewsSNRKuTLVLMUseiSkEdBQWBjeQLmuFaS");
        final SolanaClientOptionalParams optionalParams = new SolanaJsonRpcClientOptionalParams();
        optionalParams.addParam("minContextSlot", 10000000000L);

        final SolanaClientResponse<List<TokenAccount>>  response = SOLANA_API.getTokenAccountsByOwner(
                "7H1itW7F72uJbaXK2R4gP7J18HrQ2M683kL9YgUeeUHr",
                mint,
                optionalParams);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError().getErrorCode()).isEqualTo(-32016L);
        assertThat(response.getError().getErrorMessage()).isEqualTo("Minimum context slot has not been reached");
    }
}
