package com.valeamoris.solana4j.encoding;

import com.valeamoris.solana4j.api.Blockhash;
import com.valeamoris.solana4j.api.MessageVisitor;
import com.valeamoris.solana4j.api.MessageVisitor.MessageView;
import com.valeamoris.solana4j.api.PublicKey;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.NoSuchElementException;

abstract class SolanaMessageView implements MessageView
{
    private final int countAccountsSigned;
    private final int countAccountsSignedReadOnly;
    private final int countAccountsUnsignedReadOnly;

    private final MessageVisitor.AccountsView accountsView;
    private final List<ByteBuffer> signatures;
    private final PublicKey feePayer;
    private final Blockhash recentBlockHash;

    private final ByteBuffer transaction;

    SolanaMessageView(
            final int countAccountsSigned,
            final int countAccountsSignedReadOnly,
            final int countAccountsUnsignedReadOnly,
            final MessageVisitor.AccountsView accountsView,
            final ByteBuffer transaction,
            final List<ByteBuffer> signatures,
            final PublicKey feePayer,
            final Blockhash recentBlockHash)
    {
        this.countAccountsSigned = countAccountsSigned;
        this.countAccountsSignedReadOnly = countAccountsSignedReadOnly;
        this.countAccountsUnsignedReadOnly = countAccountsUnsignedReadOnly;
        this.accountsView = accountsView;
        this.transaction = transaction;
        this.signatures = signatures;
        this.feePayer = feePayer;
        this.recentBlockHash = recentBlockHash;
    }

    static MessageView fromBuffer(final ByteBuffer buffer)
    {
        final SolanaMessageFormattingCommon formatter = new SolanaMessageFormattingCommon(buffer);

        final List<ByteBuffer> signatures = formatter.readSignatures();
        final ByteBuffer transaction = buffer.slice();
        final byte first = formatter.readByte();

        boolean v0Format = false;
        boolean legacyFormat = false;

        final int version;
        final int countAccountsSigned;

        if (first == (byte) 0x80)
        {
            v0Format = true;
            version = 0;
            countAccountsSigned = formatter.readByte() & 0xff;
        }
        else if ((first & 0x80) == 0x00)
        {
            legacyFormat = true;
            countAccountsSigned = first & 0xff;
        }
        else
        {
            throw new RuntimeException("unsupported message format");
        }

        final int countAccountsSignedReadOnly = formatter.readByte() & 0xff; //convert ignoring the sign bit
        final int countAccountsUnsignedReadOnly = formatter.readByte() & 0xff;

        final List<PublicKey> staticAccounts = formatter.readStaticAccounts();

        // write recent-block-header or NONCE
        final Blockhash blockhash = formatter.readBlockHash();

        // write transaction instructions
        final List<MessageVisitor.InstructionView> instructions = formatter.readInstructions();

        if (legacyFormat)
        {
            return new SolanaLegacyMessageView(
                    countAccountsSigned,
                    countAccountsSignedReadOnly,
                    countAccountsUnsignedReadOnly,
                    new SolanaLegacyAccountsView(staticAccounts),
                    transaction,
                    signatures,
                    staticAccounts.get(0),
                    blockhash,
                    instructions);
        }
        else if (v0Format)
        {
            final List<MessageVisitor.AccountLookupView> accountLookups = formatter.readAccountLookups();

            return new SolanaV0MessageView(
                    countAccountsSigned,
                    countAccountsSignedReadOnly,
                    countAccountsUnsignedReadOnly,
                    new SolanaV0AccountsView(staticAccounts, accountLookups),
                    transaction,
                    signatures,
                    staticAccounts.get(0),
                    blockhash,
                    instructions,
                    accountLookups);
        }
        else
        {
            throw new RuntimeException("unsupported message format");
        }
    }

    @Override
    public int countAccountsSigned()
    {
        return countAccountsSigned;
    }

    @Override
    public int countAccountsSignedReadOnly()
    {
        return countAccountsSignedReadOnly;
    }

    @Override
    public int countAccountsUnsignedReadOnly()
    {
        return countAccountsUnsignedReadOnly;
    }

    @Override
    public ByteBuffer transaction()
    {
        return transaction.duplicate();
    }

    @Override
    public ByteBuffer signature(final PublicKey account)
    {
        int index = 0;
        for (final ByteBuffer signature : signatures)
        {
            if (accountsView.staticAccounts().get(index).equals(account))
            {
                return signature;
            }
            index++;
        }
        throw new NoSuchElementException();
    }

    @Override
    public PublicKey feePayer()
    {
        return feePayer;
    }

    @Override
    public Blockhash recentBlockHash()
    {
        return recentBlockHash;
    }

    @Override
    public boolean isSigner(final PublicKey account)
    {
        final int index = accountsView.staticAccounts().indexOf(account);

        if (index == -1)
        {
            return false;
        }

        return index < countAccountsSigned;
    }

    @Override
    public List<PublicKey> signers()
    {
        return accountsView.staticAccounts().subList(0, countAccountsSigned);
    }

    @Override
    public List<PublicKey> staticAccounts()
    {
        return accountsView.staticAccounts();
    }

}
