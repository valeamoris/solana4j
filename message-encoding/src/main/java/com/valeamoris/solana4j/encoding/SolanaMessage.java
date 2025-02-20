package com.valeamoris.solana4j.encoding;

import com.valeamoris.solana4j.api.Message;
import com.valeamoris.solana4j.api.MessageVisitor;

import java.nio.ByteBuffer;

final class SolanaMessage implements Message
{
    private final ByteBuffer buffer;

    SolanaMessage(final ByteBuffer buffer)
    {
        this.buffer = buffer.duplicate();
    }

    public <T> T accept(final MessageVisitor<T> visitor)
    {
        final ByteBuffer duplicate = this.buffer.duplicate();
        final MessageVisitor.MessageView messageView = SolanaMessageView.fromBuffer(duplicate);

        return visitor.visit(messageView);
    }
}
