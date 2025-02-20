package com.valeamoris.solana4j.domain;

import com.valeamoris.solana4j.Solana;
import com.valeamoris.solana4j.api.PublicKey;
import com.valeamoris.solana4j.encoding.SolanaEncoding;

public class TestPublicKey
{
    private final byte[] publicKey;

    public TestPublicKey(final byte[] publicKeyBytes)
    {
        this.publicKey = publicKeyBytes;
    }

    public String getPublicKeyBase58()
    {
        return SolanaEncoding.encodeBase58(publicKey);
    }

    public PublicKey getSolana4jPublicKey()
    {
        return Solana.account(publicKey);
    }

    public byte[] getPublicKeyBytes()
    {
        return publicKey;
    }
}
