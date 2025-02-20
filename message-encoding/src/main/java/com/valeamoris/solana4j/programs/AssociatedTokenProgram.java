package com.valeamoris.solana4j.programs;

import com.valeamoris.solana4j.Solana;
import com.valeamoris.solana4j.api.ProgramDerivedAddress;
import com.valeamoris.solana4j.api.PublicKey;
import com.valeamoris.solana4j.api.TransactionBuilder;
import com.valeamoris.solana4j.api.TransactionInstruction;
import com.valeamoris.solana4j.encoding.SolanaEncoding;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static com.valeamoris.solana4j.encoding.SysVar.RENT;
import static com.valeamoris.solana4j.programs.SystemProgram.SYSTEM_PROGRAM_ACCOUNT;
import static java.util.Objects.requireNonNull;

/**
 * Program for managing associated token accounts on the Solana blockchain.
 */
public final class AssociatedTokenProgram
{
    /**
     * The program ID for the associated token program.
     * <p>
     * This constant holds the program ID used to identify the associated token program.
     * </p>
     */
    private static final byte[] ASSOCIATED_TOKEN_PROGRAM_ID = SolanaEncoding.decodeBase58("ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL");

    /**
     * The public key for the associated token program account.
     * <p>
     * This constant defines the public key associated with the Solana account for the associated token program.
     * It is set to the value returned by {@link Solana#account(byte[])} using the {@link #ASSOCIATED_TOKEN_PROGRAM_ID}.
     * </p>
     */
    public static final PublicKey ASSOCIATED_TOKEN_PROGRAM_ACCOUNT = Solana.account(ASSOCIATED_TOKEN_PROGRAM_ID);

    /**
     * The instruction code for creating an account.
     * <p>
     * This constant defines the instruction code used to create a new token account in Solana.
     * </p>
     */
    public static final int CREATE_INSTRUCTION = 0;

    /**
     * The instruction code for idempotently creating an account.
     * <p>
     * This constant defines the instruction code used to create a new token account idempotently,
     * ensuring that if the creation is attempted multiple times, it will have the same effect as if it were done once.
     * </p>
     */
    public static final int IDEMPOTENT_CREATE_INSTRUCTION = 1;

    /**
     * Private constructor to prevent instantiation.
     */
    private AssociatedTokenProgram()
    {
    }

    /**
     * Factory method for creating a new instance of {@code AssociatedTokenProgramFactory}.
     *
     * @param tb the transaction builder to append instructions to
     * @return a new instance of {@code AssociatedTokenProgramFactory}
     */
    public static AssociatedTokenProgramFactory factory(final TransactionBuilder tb)
    {
        return new AssociatedTokenProgramFactory(tb);
    }

    /**
     * Inner factory class for building associated token program instructions using a {@link TransactionBuilder}.
     */
    public static final class AssociatedTokenProgramFactory
    {

        private final TransactionBuilder tb;

        /**
         * Private constructor to initialize the factory with the given transaction builder.
         *
         * @param tb the transaction builder
         */
        private AssociatedTokenProgramFactory(final TransactionBuilder tb)
        {
            this.tb = tb;
        }

        /**
         * Creates a new associated token account.
         *
         * @param programDerivedAddress the program derived address
         * @param mint                  the public key of the mint
         * @param owner                 the public key of the owner
         * @param payer                 the public key of the payer
         * @param programId             the public key of the token program used to create the programDerivedAddress
         * @param idempotent            whether the creation should be idempotent
         * @return this {@code AssociatedTokenProgramFactory} instance
         */
        public AssociatedTokenProgramFactory createAssociatedTokenAccount(
                final ProgramDerivedAddress programDerivedAddress,
                final PublicKey mint,
                final PublicKey owner,
                final PublicKey payer,
                final PublicKey programId,
                final boolean idempotent)
        {
            tb.append(AssociatedTokenProgram.createAssociatedTokenAccount(programDerivedAddress, mint, owner, payer, programId, idempotent));
            return this;
        }
    }

    /**
     * Creates a new associated token account.
     *
     * @param programDerivedAddress the program derived address
     * @param mint                  the public key of the mint
     * @param owner                 the public key of the owner
     * @param payer                 the public key of the payer
     * @param programId             the public key of the token program used to create the programDerivedAddress
     * @param idempotent            whether the creation should be idempotent
     * @return A {@code TransactionInstruction} representing the created instruction
     */
    public static TransactionInstruction createAssociatedTokenAccount(
            final ProgramDerivedAddress programDerivedAddress,
            final PublicKey mint,
            final PublicKey owner,
            final PublicKey payer,
            final PublicKey programId,
            final boolean idempotent)
    {
        return Solana.instruction(ib -> ib
                .program(ASSOCIATED_TOKEN_PROGRAM_ACCOUNT)
                .account(payer, true, true)
                .account(programDerivedAddress.address(), false, true)
                .account(owner, false, false)
                .account(mint, false, false)
                .account(SYSTEM_PROGRAM_ACCOUNT, false, false)
                .account(programId, false, false)
                .account(RENT, false, false)
                .data(1, bb -> bb.order(ByteOrder.LITTLE_ENDIAN)
                        .put((byte) (idempotent ? IDEMPOTENT_CREATE_INSTRUCTION : CREATE_INSTRUCTION)))
        );
    }

    /**
     * Derives a program address for a given owner, token program account, and mint.
     * <p>
     * This method generates a program-derived address based on the combination of the owner's public key,
     * the token program account's public key, and the mint's public key, using the associated token program account.
     * </p>
     *
     * @param owner               the owner's public key; must not be null
     * @param tokenProgramAccount the token program account's public key; must not be null
     * @param mint                the mint's public key; must not be null
     * @return the derived program address based on the provided owner, token program account, and mint public keys
     * @throws NullPointerException if the owner, tokenProgramAccount, or mint is null
     */
    public static ProgramDerivedAddress deriveAddress(final PublicKey owner, final PublicKey tokenProgramAccount, final PublicKey mint)
    {
        requireNonNull(owner, "The owner public key must be specified, but was null");
        requireNonNull(tokenProgramAccount, "The token program public key must be specified, but was null");
        requireNonNull(mint, "The mint public key must be specified, but was null");

        final List<byte[]> seeds = new ArrayList<>();
        seeds.add(owner.bytes());
        seeds.add(tokenProgramAccount.bytes());
        seeds.add(mint.bytes());
        return SolanaEncoding.deriveProgramAddress(
                seeds,
                ASSOCIATED_TOKEN_PROGRAM_ACCOUNT
        );
    }
}
