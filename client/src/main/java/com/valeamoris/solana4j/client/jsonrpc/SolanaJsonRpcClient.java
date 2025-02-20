package com.valeamoris.solana4j.client.jsonrpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.valeamoris.solana4j.client.api.*;
import okhttp3.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.valeamoris.solana4j.client.jsonrpc.SolanaJsonRpcClientOptionalParams.defaultOptionalParams;

/**
 * Implementation of the {@link SolanaApi} interface for interacting with the Solana blockchain via JSON-RPC.
 * This client provides methods to perform various operations such as requesting airdrops, sending transactions,
 * retrieving account information, and more.
 */
public class SolanaJsonRpcClient implements SolanaApi {
    private final String rpcUrl;
    private final OkHttpClient httpClient;
    private final SolanaCodec solanaCodec;
    public static final MediaType JSON = MediaType.get("application/json");

    /**
     * Constructs a new {@code SolanaJsonRpcClient} with the specified HTTP client and RPC URL.
     *
     * @param httpClient the {@link OkHttpClient} instance to use for sending requests.
     *                   This allows customization of HTTP settings such as connection pooling,
     *                   SSL context, and timeout configurations.
     * @param rpcUrl     the URL of the Solana JSON-RPC node.
     */
    public SolanaJsonRpcClient(
            final OkHttpClient httpClient,
            final String rpcUrl) {
        this.rpcUrl = rpcUrl;
        this.httpClient = httpClient;
        this.solanaCodec = new SolanaCodec(false);
    }

    SolanaJsonRpcClient(
            final OkHttpClient httpClient,
            final String rpcUrl,
            final boolean failOnUnknownProperties) {
        this.rpcUrl = rpcUrl;
        this.httpClient = httpClient;
        this.solanaCodec = new SolanaCodec(failOnUnknownProperties);
    }

    @Override
    public SolanaClientResponse<String> requestAirdrop(final String address, final long amountLamports) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<String>>() {
                              },
                dto -> dto, "requestAirdrop", address, amountLamports,
                defaultOptionalParams());
    }

    @Override
    public SolanaClientResponse<String> requestAirdrop(final String address, final long amountLamports, final SolanaClientOptionalParams optionalParams) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<String>>() {
                              },
                dto -> dto, "requestAirdrop", address, amountLamports,
                optionalParams.getParams());
    }

    @Override
    public SolanaClientResponse<String> sendTransaction(final String transactionBlob) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<String>>() {
                              },
                dto -> dto,
                "sendTransaction",
                transactionBlob,
                defaultOptionalParams());
    }

    @Override
    public SolanaClientResponse<String> sendTransaction(final String transactionBlob, final SolanaClientOptionalParams optionalParams) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<String>>() {
                              },
                dto -> dto,
                "sendTransaction",
                transactionBlob,
                optionalParams.getParams());
    }

    @Override
    public SolanaClientResponse<TransactionResponse> getTransaction(final String transactionSignature) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<TransactionResponseDTO>>() {
                              },
                dto -> dto, "getTransaction", transactionSignature,
                defaultOptionalParams());
    }

    @Override
    public SolanaClientResponse<TransactionResponse> getTransaction(final String transactionSignature, final SolanaClientOptionalParams optionalParams) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<TransactionResponseDTO>>() {
                              },
                dto -> dto, "getTransaction", transactionSignature,
                optionalParams.getParams());
    }

    @Override
    public SolanaClientResponse<Long> getBalance(final String address) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<BalanceDTO>>() {
                              },
                BalanceDTO::getValue, "getBalance", address,
                defaultOptionalParams());
    }

    @Override
    public SolanaClientResponse<Long> getBalance(final String address, final SolanaClientOptionalParams optionalParams) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<BalanceDTO>>() {
                              },
                BalanceDTO::getValue, "getBalance", address,
                optionalParams.getParams());
    }

    @Override
    public SolanaClientResponse<TokenAmount> getTokenAccountBalance(final String address) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<TokenAmountDTO>>() {
                              },
                TokenAmountDTO::getValue, "getTokenAccountBalance", address,
                defaultOptionalParams());
    }

    @Override
    public SolanaClientResponse<TokenAmount> getTokenAccountBalance(final String address, final SolanaClientOptionalParams optionalParams) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<TokenAmountDTO>>() {
                              },
                TokenAmountDTO::getValue, "getTokenAccountBalance", address,
                optionalParams.getParams());
    }

    @Override
    public SolanaClientResponse<AccountInfo> getAccountInfo(final String address) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<AccountInfoDTO>>() {
                              },
                AccountInfoDTO::getValue, "getAccountInfo", address,
                defaultOptionalParams()
        );
    }

    @Override
    public SolanaClientResponse<AccountInfo> getAccountInfo(final String address, final SolanaClientOptionalParams solanaClientOptionalParams) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<AccountInfoDTO>>() {
                              },
                AccountInfoDTO::getValue, "getAccountInfo", address,
                solanaClientOptionalParams.getParams()
        );
    }

    @Override
    public SolanaClientResponse<Long> getBlockHeight() throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<Long>>() {
                              },
                dto -> dto, "getBlockHeight",
                defaultOptionalParams());
    }

    @Override
    public SolanaClientResponse<Long> getBlockHeight(final SolanaClientOptionalParams optionalParams) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<Long>>() {
                              },
                dto -> dto, "getBlockHeight",
                optionalParams.getParams());
    }

    @Override
    public SolanaClientResponse<Long> getSlot() throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<Long>>() {
                              },
                dto -> dto, "getSlot",
                defaultOptionalParams());
    }

    @Override
    public SolanaClientResponse<Long> getSlot(final SolanaClientOptionalParams optionalParams) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<Long>>() {
                              },
                dto -> dto, "getSlot",
                optionalParams.getParams());
    }

    @Override
    public SolanaClientResponse<Blockhash> getLatestBlockhash() throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<BlockhashDTO>>() {
                              },
                BlockhashDTO::getValue, "getLatestBlockhash",
                defaultOptionalParams());
    }

    @Override
    public SolanaClientResponse<Blockhash> getLatestBlockhash(final SolanaClientOptionalParams optionalParams) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<BlockhashDTO>>() {
                              },
                BlockhashDTO::getValue, "getLatestBlockhash",
                optionalParams.getParams());
    }

    @Override
    public SolanaClientResponse<Long> getMinimumBalanceForRentExemption(final int size) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<Long>>() {
                              },
                dto -> dto, "getMinimumBalanceForRentExemption", size,
                defaultOptionalParams());
    }

    @Override
    public SolanaClientResponse<Long> getMinimumBalanceForRentExemption(final int size, final SolanaClientOptionalParams optionalParams) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<Long>>() {
                              },
                dto -> dto, "getMinimumBalanceForRentExemption", size,
                optionalParams.getParams());
    }

    @Override
    public SolanaClientResponse<Long> minimumLedgerSlot() throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<Long>>() {
                              },
                dto -> dto, "minimumLedgerSlot");
    }

    @Override
    public SolanaClientResponse<String> getHealth() throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<String>>() {
                              },
                dto -> dto, "getHealth");
    }

    @Override
    public SolanaClientResponse<List<SignatureForAddress>> getSignaturesForAddress(final String addressBase58) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<List<SignatureForAddressDTO>>>() {
                              },
                ArrayList::new, "getSignaturesForAddress", addressBase58);
    }

    @Override
    public SolanaClientResponse<List<SignatureForAddress>> getSignaturesForAddress(
            final String addressBase58,
            final SolanaClientOptionalParams optionalParams) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<List<SignatureForAddressDTO>>>() {
                              },
                ArrayList::new, "getSignaturesForAddress", addressBase58,
                optionalParams.getParams());
    }

    @Override
    public SolanaClientResponse<List<SignatureStatus>> getSignatureStatuses(final List<String> transactionSignatures) throws SolanaJsonRpcClientException {
        final Map<String, Object> defaultOptionalParams = defaultOptionalParams();
        // it's not such an optional field, apparently
        defaultOptionalParams.put("searchTransactionHistory", false);

        return queryForObject(new TypeReference<RpcWrapperDTO<SignatureStatusesDTO>>() {
                              },
                SignatureStatusesDTO::getValue, "getSignatureStatuses", transactionSignatures,
                defaultOptionalParams);
    }

    @Override
    public SolanaClientResponse<List<SignatureStatus>> getSignatureStatuses(
            final List<String> transactionSignatures,
            final SolanaClientOptionalParams optionalParams) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<SignatureStatusesDTO>>() {
                              },
                SignatureStatusesDTO::getValue, "getSignatureStatuses", transactionSignatures,
                optionalParams.getParams());
    }

    @Override
    public SolanaClientResponse<List<TokenAccount>> getTokenAccountsByOwner(final String accountDelegate, final Map.Entry<String, String> filter) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<TokenAccountsByOwnerDTO>>() {
                              },
                TokenAccountsByOwnerDTO::getValue, "getTokenAccountsByOwner", accountDelegate, filter,
                defaultOptionalParams());
    }

    @Override
    public SolanaClientResponse<List<TokenAccount>> getTokenAccountsByOwner(
            final String accountDelegate,
            final Map.Entry<String, String> filter,
            final SolanaClientOptionalParams optionalParams) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<TokenAccountsByOwnerDTO>>() {
                              },
                TokenAccountsByOwnerDTO::getValue, "getTokenAccountsByOwner", accountDelegate, filter,
                optionalParams.getParams());
    }

    @Override
    public SolanaClientResponse<SimulateTransactionResponse> simulateTransaction(final String transaction) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<SimulateTransactionResponseDTO>>() {
                              },
                SimulateTransactionResponseDTO::getValue, "simulateTransaction", transaction,
                defaultOptionalParams());
    }

    @Override
    public SolanaClientResponse<SimulateTransactionResponse> simulateTransaction(final String transaction, final SolanaClientOptionalParams optionalParams) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<SimulateTransactionResponseDTO>>() {
                              },
                SimulateTransactionResponseDTO::getValue, "simulateTransaction", transaction,
                optionalParams.getParams());
    }

    @Override
    public SolanaClientResponse<BlockResponse> getBlock(long slot) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<BlockResponseDTO>>() {
                              },
                dto -> dto, "getBlock", slot,
                defaultOptionalParams());
    }

    @Override
    public SolanaClientResponse<BlockResponse> getBlock(long slot, SolanaClientOptionalParams optionalParams) throws SolanaJsonRpcClientException {
        return queryForObject(new TypeReference<RpcWrapperDTO<BlockResponseDTO>>() {
                              },
                dto -> dto, "getBlock", slot,
                optionalParams.getParams());
    }

    private <S, T> SolanaClientResponse<S> queryForObject(
            final TypeReference<RpcWrapperDTO<T>> type,
            final Function<T, S> dtoMapper,
            final String method,
            final Object... params) throws SolanaJsonRpcClientException {
        final Request request = prepareRequest(method, params);
        final Response httpResponse = sendRequest(request);

        final Result<SolanaClientResponse.SolanaClientError, T> response = decodeResponse(type, httpResponse);
        if (response.isError()) {
            return SolanaJsonRpcClientResponse.creatErrorResponse(response.getError());
        }

        return SolanaJsonRpcClientResponse.createSuccessResponse(dtoMapper.apply(response.getSuccess()));
    }

    private Request prepareRequest(final String method, final Object[] params) throws SolanaJsonRpcClientException {
        try {
            return buildPostRequest(solanaCodec.encodeRequest(method, params));
        } catch (final JsonProcessingException e) {
            throw new SolanaJsonRpcClientException(String.format("An error occurred building the JSON RPC request for method %s.", method), e);
        }
    }

    private Response sendRequest(final Request request) throws SolanaJsonRpcClientException {
        try {
            final Response httpResponse = httpClient.newCall(request).execute();
            if (httpResponse.code() != 200) {
                throw new SolanaJsonRpcClientException(String.format("Unexpected status code %s returned from the JSON RPC for request %s.", httpResponse.code(), request));
            } else {
                return httpResponse;
            }
        } catch (final IOException e) {
            throw new SolanaJsonRpcClientException(String.format("Unable to communicate with the JSON RPC for request %s.", request), e, true);
        }
    }

    private <T> Result<SolanaClientResponse.SolanaClientError, T> decodeResponse(
            final TypeReference<RpcWrapperDTO<T>> type,
            final Response httpResponse) throws SolanaJsonRpcClientException {
        try {
            final ResponseBody body = httpResponse.body();
            // copy body to avoid closing it
            final byte[] bytes = body != null ? body.bytes() : null;
            RpcWrapperDTO<T> rpcResult = new RpcWrapperDTO<>(null, null, 0, null);
            if (body != null) {
                rpcResult = solanaCodec.decodeResponse(bytes, type);
            }
            if (rpcResult.getError() != null) {
                return Result.error(new SolanaJsonRpcClientError(rpcResult.getError().getCode(), rpcResult.getError().getMessage()));
            }
            return Result.success(rpcResult.getResult());
        } catch (final IOException e) {
            throw new SolanaJsonRpcClientException(String.format("Unable to decode JSON RPC response %s.", httpResponse), e);
        }
    }

    private Request buildPostRequest(final String payload) {
        RequestBody body = RequestBody.create(payload, JSON);
        Request.Builder builder = new Request.Builder();
        builder.url(rpcUrl)
                .post(body);
        return builder.build();
    }
}
