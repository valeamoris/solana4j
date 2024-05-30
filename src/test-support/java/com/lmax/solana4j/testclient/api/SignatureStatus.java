package com.lmax.solana4j.testclient.api;

public interface SignatureStatus
{

    Long getConfirmations();

    long getSlot();

    Object getErr();

    Commitment getConfirmationStatus();
}
