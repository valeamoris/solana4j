package com.valeamoris.solana4j.encoding;

import com.valeamoris.solana4j.api.Message;
import com.valeamoris.solana4j.api.UnsignedMessageBuilder;

import java.nio.ByteBuffer;

final class SolanaUnsignedMessageBuilder implements UnsignedMessageBuilder
{
    private final ByteBuffer buffer;

    SolanaUnsignedMessageBuilder(final ByteBuffer buffer)
    {
        this.buffer = buffer;
    }

    @Override
    public Message build()
    {
        return new SolanaMessage(buffer);
    }
}
