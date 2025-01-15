package com.valeamoris.solana4j.transaction;

import com.valeamoris.solana4j.Solana;
import com.valeamoris.solana4j.api.*;
import com.valeamoris.solana4j.domain.TestKeyPair;
import com.valeamoris.solana4j.domain.TokenProgram;
import com.valeamoris.solana4j.encoding.SolanaEncoding;
import com.valeamoris.solana4j.programs.AddressLookupTableProgram;
import com.valeamoris.solana4j.programs.AssociatedTokenProgram;
import com.valeamoris.solana4j.programs.BpfLoaderUpgradeableProgram;
import com.valeamoris.solana4j.programs.ComputeBudgetProgram;
import com.valeamoris.solana4j.programs.SystemProgram;
import com.valeamoris.solana4j.programs.TokenProgramBase;
import com.valeamoris.solana4j.sign.BouncyCastleSigner;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.valeamoris.solana4j.programs.SystemProgram.SYSTEM_PROGRAM_ACCOUNT;

public class LegacyTransactionBlobFactory implements TransactionBlobFactory
{
    @Override
    public String solTransfer(
            final PublicKey from,
            final PublicKey to,
            final long amount,
            final Blockhash blockhash,
            final PublicKey payer,
            final List<TestKeyPair> signers,
            final List<AddressLookupTable> addressLookupTables)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        List<TransactionInstruction> instructions = new ArrayList<>();
        instructions.add(SystemProgram.transfer(
                from,
                to,
                amount)
        );
        Solana.builder(buffer)
                .legacy()
                .recent(blockhash)
                .prebuiltInstructions(instructions)
                .payer(payer)
                .seal()
                .unsigned()
                .build();

        sign(buffer, signers);
        return base64Encode(buffer);
    }

    @Override
    public String tokenTransfer(
            final TokenProgram tokenProgram,
            final PublicKey from,
            final PublicKey to,
            final PublicKey owner,
            final long amount,
            final Blockhash blockhash,
            final PublicKey payer,
            final List<TestKeyPair> signers,
            final List<AddressLookupTable> addressLookupTables)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        List<TransactionInstruction> instructions = new ArrayList<>();
        instructions.add(tokenProgram.getTokenProgram().transfer(
                from,
                to,
                owner,
                amount,
                signers.stream().map(TestKeyPair::getSolana4jPublicKey).collect(Collectors.toList())));
        Solana.builder(buffer)
                .legacy()
                .recent(blockhash)
                .prebuiltInstructions(instructions)
                .payer(payer)
                .seal()
                .unsigned()
                .build();

        sign(buffer, signers);
        return base64Encode(buffer);
    }

    @Override
    public String mintTo(
            final TokenProgram tokenProgram,
            final PublicKey mint,
            final PublicKey authority,
            final Destination destination,
            final Blockhash blockhash,
            final PublicKey payer,
            final List<TestKeyPair> signers,
            final List<AddressLookupTable> addressLookupTables)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);
        List<TransactionInstruction> instructions = new ArrayList<>();
        instructions.add(tokenProgram.getTokenProgram().mintTo(
                mint,
                authority,
                destination));
        Solana.builder(buffer)
                .legacy()
                .recent(blockhash)
                .prebuiltInstructions(instructions)
                .payer(payer)
                .seal()
                .unsigned()
                .build();

        sign(buffer, signers);
        return base64Encode(buffer);
    }

    @Override
    public String createMintAccount(
            final TokenProgram tokenProgram,
            final PublicKey account,
            final int decimals,
            final PublicKey mintAuthority,
            final PublicKey freezeAuthority,
            final long rentExemption,
            final int accountSpan,
            final Blockhash blockhash,
            final PublicKey payer,
            final List<TestKeyPair> signers,
            final List<AddressLookupTable> addressLookupTables)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);
        List<TransactionInstruction> instructions = new ArrayList<>();
        instructions.add(SystemProgram.createAccount(
                payer,
                account,
                rentExemption,
                accountSpan,
                tokenProgram.getProgram()));
        instructions.add(tokenProgram.getTokenProgram().initializeMint(
                account,
                (byte) decimals,
                mintAuthority,
                Optional.of(freezeAuthority)));
        Solana.builder(buffer)
                .legacy()
                .recent(blockhash)
                .prebuiltInstructions(instructions)
                .payer(payer)
                .seal()
                .unsigned()
                .build();

        sign(buffer, signers);
        return base64Encode(buffer);
    }

    @Override
    public String createMultiSigAccount(
            final TokenProgram tokenProgram,
            final PublicKey account,
            final List<PublicKey> multiSigSigners,
            final int requiredSigners,
            final long rentExemption,
            final int accountSpan,
            final Blockhash blockhash,
            final PublicKey payer,
            final List<TestKeyPair> signers,
            final List<AddressLookupTable> addressLookupTables)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);
        List<TransactionInstruction> instructions = new ArrayList<>();
        instructions.add(SystemProgram.createAccount(
                payer,
                account,
                rentExemption,
                accountSpan,
                tokenProgram.getProgram()));
        instructions.add(tokenProgram.getTokenProgram().initializeMultisig(
                account,
                multiSigSigners,
                requiredSigners));
        Solana.builder(buffer)
                .legacy()
                .recent(blockhash)
                .prebuiltInstructions(instructions)
                .payer(payer)
                .seal()
                .unsigned()
                .build();

        sign(buffer, signers);
        return base64Encode(buffer);
    }

    @Override
    public String createNonce(
            final PublicKey nonce,
            final PublicKey authority,
            final Blockhash blockhash,
            final long rentExemption,
            final int accountSpan,
            final PublicKey payer,
            final List<TestKeyPair> signers,
            final List<AddressLookupTable> addressLookupTables)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);
        List<TransactionInstruction> instructions = new ArrayList<>();
        instructions.add(SystemProgram.createAccount(
                authority,
                nonce,
                rentExemption,
                accountSpan,
                SYSTEM_PROGRAM_ACCOUNT));
        instructions.add(SystemProgram.nonceInitialize(nonce, authority));
        Solana.builder(buffer)
                .legacy()
                .recent(blockhash)
                .prebuiltInstructions(instructions)
                .payer(payer)
                .seal()
                .unsigned()
                .build();

        sign(buffer, signers);
        return base64Encode(buffer);
    }

    @Override
    public String createTokenAccount(
            final TokenProgram tokenProgram,
            final long rentExemption,
            final int accountSpan,
            final PublicKey account,
            final PublicKey owner,
            final PublicKey mint,
            final Blockhash blockhash,
            final PublicKey payer,
            final List<TestKeyPair> signers,
            final List<AddressLookupTable> addressLookupTables)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);
        List<TransactionInstruction> instructions = new ArrayList<>();
        instructions.add(SystemProgram.createAccount(
                payer,
                account,
                rentExemption,
                accountSpan,
                tokenProgram.getProgram()));
        instructions.add(tokenProgram.getTokenProgram().initializeAccount(
                account,
                mint,
                owner));
        Solana.builder(buffer)
                .legacy()
                .recent(blockhash)
                .prebuiltInstructions(instructions)
                .payer(payer)
                .seal()
                .unsigned()
                .build();

        sign(buffer, signers);
        return base64Encode(buffer);
    }

    @Override
    public String createAddressLookupTable(
            final ProgramDerivedAddress programDerivedAddress,
            final PublicKey authority,
            final Slot recentSlot,
            final Blockhash blockhash,
            final PublicKey payer,
            final List<TestKeyPair> signers)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        List<TransactionInstruction> instructions = new ArrayList<>();
        instructions.add( AddressLookupTableProgram.createLookupTable(
                programDerivedAddress,
                authority,
                payer,
                recentSlot));
        Solana.builder(buffer)
                .legacy()
                .recent(blockhash)
                .prebuiltInstructions(instructions)
                .payer(payer)
                .seal()
                .unsigned()
                .build();

        sign(buffer, signers);
        return base64Encode(buffer);
    }

    @Override
    public String extendAddressLookupTable(
            final PublicKey lookupAddress,
            final PublicKey authority,
            final List<PublicKey> addressesToAdd,
            final Blockhash blockhash,
            final PublicKey payer,
            final List<TestKeyPair> signers,
            final List<AddressLookupTable> addressLookupTables)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        List<TransactionInstruction> instructions = new ArrayList<>();
        instructions.add(AddressLookupTableProgram.extendLookupTable(
                lookupAddress,
                authority,
                payer,
                addressesToAdd));
        Solana.builder(buffer)
                .legacy()
                .recent(blockhash)
                .prebuiltInstructions(instructions)
                .payer(payer)
                .seal()
                .unsigned()
                .build();

        sign(buffer, signers);
        return base64Encode(buffer);
    }

    @Override
    public String advanceNonce(
            final PublicKey account,
            final PublicKey authority,
            final Blockhash blockhash,
            final PublicKey payer,
            final List<TestKeyPair> signers,
            final List<AddressLookupTable> addressLookupTables)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        List<TransactionInstruction> instructions = new ArrayList<>();
        instructions.add(SystemProgram.nonceAdvance(account, authority));
        Solana.builder(buffer)
                .legacy()
                .recent(blockhash)
                .prebuiltInstructions(instructions)
                .payer(payer)
                .seal()
                .unsigned()
                .build();

        sign(buffer, signers);
        return base64Encode(buffer);
    }

    @Override
    public String createAssociatedTokenAccount(
            final TokenProgram tokenProgram,
            final PublicKey owner,
            final ProgramDerivedAddress associatedTokenAddress,
            final Blockhash blockhash,
            final PublicKey mint,
            final boolean idempotent,
            final PublicKey payer,
            final List<TestKeyPair> signers,
            final List<AddressLookupTable> addressLookupTables)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        List<TransactionInstruction> instructions = new ArrayList<>();
        instructions.add(AssociatedTokenProgram.createAssociatedTokenAccount(
                associatedTokenAddress,
                mint,
                owner,
                payer,
                tokenProgram.getProgram(),
                idempotent));
        Solana.builder(buffer)
                .legacy()
                .recent(blockhash)
                .prebuiltInstructions(instructions)
                .payer(payer)
                .seal()
                .unsigned()
                .build();

        sign(buffer, signers);
        return base64Encode(buffer);
    }

    @Override
    public String setTokenAccountAuthority(
            final TokenProgram tokenProgram,
            final PublicKey tokenAccount,
            final PublicKey tokenAccountOldAuthority,
            final PublicKey tokenAccountNewAuthority,
            final TokenProgramBase.AuthorityType authorityType,
            final Blockhash blockhash,
            final TestKeyPair payer,
            final List<TestKeyPair> signers,
            final List<AddressLookupTable> addressLookupTables)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        List<TransactionInstruction> instructions = new ArrayList<>();
        instructions.add(tokenProgram.getTokenProgram().setAuthority(
                tokenAccount,
                tokenAccountNewAuthority,
                tokenAccountOldAuthority,
                signers.stream().map(TestKeyPair::getSolana4jPublicKey).collect(Collectors.toList()),
                authorityType));
        Solana.builder(buffer)
                .legacy()
                .recent(blockhash)
                .prebuiltInstructions(instructions)
                .payer(payer.getSolana4jPublicKey())
                .seal()
                .unsigned()
                .build();

        sign(buffer, signers);
        return base64Encode(buffer);
    }

    @Override
    public String setComputeUnits(final int computeUnitLimit, final long computeUnitPrice, final Blockhash blockhash, final PublicKey payer, final List<TestKeyPair> signers)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        List<TransactionInstruction> instructions = new ArrayList<>();
        instructions.add(ComputeBudgetProgram.setComputeUnitLimit(computeUnitLimit));
        instructions.add(ComputeBudgetProgram.setComputeUnitPrice(computeUnitPrice));
        Solana.builder(buffer)
                .legacy()
                .recent(blockhash)
                .prebuiltInstructions(instructions)
                .payer(payer)
                .seal()
                .unsigned()
                .build();

        sign(buffer, signers);
        return base64Encode(buffer);
    }

    @Override
    public String setBpfUpgradeableProgramUpgradeAuthority(
            final PublicKey program,
            final PublicKey oldUpgradeAuthority,
            final PublicKey newUpgradeAuthority,
            final Blockhash blockhash,
            final PublicKey payer,
            final List<TestKeyPair> signers,
            final List<AddressLookupTable> addressLookupTables)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        List<TransactionInstruction> instructions = new ArrayList<>();
        instructions.add(BpfLoaderUpgradeableProgram.setUpgradeAuthority(
                program,
                oldUpgradeAuthority,
                Optional.of(newUpgradeAuthority)));
        Solana.builder(buffer)
                .legacy()
                .recent(blockhash)
                .prebuiltInstructions(instructions)
                .payer(payer)
                .seal()
                .unsigned()
                .build();

        sign(buffer, signers);

        return base64Encode(buffer);
    }

    private static void sign(final ByteBuffer buffer, final List<TestKeyPair> signers)
    {
        final SignedMessageBuilder signedMessageBuilder = Solana.forSigning(buffer);
        for (final TestKeyPair signer : signers)
        {
            signedMessageBuilder.by(signer.getSolana4jPublicKey(), (transaction, signature) -> BouncyCastleSigner.sign(signer.getPrivateKeyBytes(), transaction, signature));
        }
        signedMessageBuilder.build();
    }

    private static String base64Encode(final ByteBuffer bytes)
    {
        return Base64.getEncoder().encodeToString(SolanaEncoding.copyBuffer(bytes));
    }
}
