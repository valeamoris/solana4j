package com.valeamoris.solana4j.api;

import java.nio.ByteBuffer;

/**
 * Interface representing an entity capable of signing transactions represented as {@link ByteBuffer}.
 * <p>
 * Implementations of this interface provide the ability to sign a transaction and write the corresponding signature.
 * </p>
 */
public interface ByteBufferSigner
{

    /**
     * Signs the given transaction and writes the signature to the provided buffer.
     *
     * @param transaction the {@link ByteBuffer} containing the transaction to be signed
     * @param signature   the {@link ByteBuffer} where the generated signature will be written
     */
    void sign(ByteBuffer transaction, ByteBuffer signature);
}
