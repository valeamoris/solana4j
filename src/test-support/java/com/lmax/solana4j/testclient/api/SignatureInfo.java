package com.lmax.solana4j.testclient.api;

public interface SignatureInfo
{
    String getSignature();

    long getSlot();

    Object getErr();

    String getMemo();

    Long getBlockTime();

    Commitment getConfirmationStatus();
}
