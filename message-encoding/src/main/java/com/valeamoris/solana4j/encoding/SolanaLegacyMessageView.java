package com.valeamoris.solana4j.encoding;

import com.valeamoris.solana4j.api.Blockhash;
import com.valeamoris.solana4j.api.MessageVisitor;
import com.valeamoris.solana4j.api.MessageVisitor.LegacyMessageView;
import com.valeamoris.solana4j.api.PublicKey;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

final class SolanaLegacyMessageView extends SolanaMessageView implements LegacyMessageView
{
    private final MessageVisitor.LegacyAccountsView accountsView;
    private final List<MessageVisitor.InstructionView> instructions;

    SolanaLegacyMessageView(
            final int countAccountsSigned,
            final int countAccountsSignedReadOnly,
            final int countAccountsUnsignedReadOnly,
            final MessageVisitor.LegacyAccountsView accountsView,
            final ByteBuffer transaction,
            final List<ByteBuffer> signatures,
            final PublicKey feePayer,
            final Blockhash recentBlockHash,
            final List<MessageVisitor.InstructionView> instructions)
    {
        super(countAccountsSigned, countAccountsSignedReadOnly, countAccountsUnsignedReadOnly, accountsView, transaction, signatures, feePayer, recentBlockHash);
        this.accountsView = accountsView;
        this.instructions = instructions;
    }

    @Override
    public List<MessageVisitor.LegacyInstructionView> instructions()
    {
        return instructions.stream()
                .map(instructionView ->
                        new SolanaLegacyInstructionView(
                                instructionView.programIndex(),
                                instructionView.accountIndexes(),
                                instructionView.data(),
                                accountsView)
                ).collect(Collectors.toList());
    }

    @Override
    public boolean isWriter(final PublicKey account)
    {
        final int index = accountsView.staticAccounts().indexOf(account);
        if (index == -1)
        {
            return false;
        }

        final boolean isSignerWriter = index < countAccountsSigned() - countAccountsSignedReadOnly();
        final boolean isNonSigner = index >= countAccountsSigned();
        final boolean isNonSignerReadonly = index >= (accountsView.staticAccounts().size() - countAccountsUnsignedReadOnly());
        final boolean isNonSignerWriter = isNonSigner && !isNonSignerReadonly;

        return isSignerWriter || isNonSignerWriter;
    }
}
