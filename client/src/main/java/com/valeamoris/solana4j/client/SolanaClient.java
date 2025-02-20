package com.valeamoris.solana4j.client;

import com.valeamoris.solana4j.client.api.SolanaApi;
import com.valeamoris.solana4j.client.jsonrpc.SolanaJsonRpcClient;
import okhttp3.OkHttpClient;


/**
 * The {@code SolanaClient} class provides a static factory method to interact with the Solana blockchain.
 * This class simplifies the creation of a {@link SolanaApi} instance,
 * which allows interaction with a Solana node.
 */
public final class SolanaClient
{
    private SolanaClient()
    {
    }

    /**
     * Creates a new {@code SolanaApi} instance using the specified HTTP client and RPC URL.
     *
     * @param httpClient  the {@link OkHttpClient} instance to use for sending requests.
     *                    This allows customization of HTTP-related configurations such as
     *                    connection pooling, SSL context, and timeouts.
     * @param rpcUrl      the URL of the Solana JSON-RPC endpoint.
     * @return            a new {@link SolanaApi} instance configured with the provided HTTP client
     *                    and RPC URL.
     */
    public static SolanaApi create(final OkHttpClient httpClient, final String rpcUrl)
    {
        return new SolanaJsonRpcClient(httpClient, rpcUrl);
    }
}