package com.valeamoris.solana4j.encoding;

import com.valeamoris.solana4j.api.ProgramDerivedAddress;
import com.valeamoris.solana4j.api.PublicKey;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.valeamoris.solana4j.api.PublicKey.PUBLIC_KEY_LENGTH;
import static java.util.Objects.requireNonNull;

final class SolanaProgramDerivedAddress implements ProgramDerivedAddress
{
    public static final byte[] PROGRAM_DERIVED_ADDRESS_BYTES = "ProgramDerivedAddress".getBytes(StandardCharsets.UTF_8);
    private static final int BUMP_LENGTH = 1;

    final PublicKey address;
    final PublicKey programAccount;
    final int nonce;

    static ProgramDerivedAddress deriveProgramAddress(final List<byte[]> seeds, final PublicKey programId)
    {
        final int seedLength = seeds.stream().mapToInt(seed -> seed.length).sum();
        final int byteLength = seedLength + PUBLIC_KEY_LENGTH + PROGRAM_DERIVED_ADDRESS_BYTES.length + BUMP_LENGTH;

        int bumpSeed = 255;
        while (bumpSeed > 0)
        {
            final ByteBuffer seedsBuffer = ByteBuffer.allocate(byteLength);
            seeds.forEach(seedsBuffer::put);
            seedsBuffer.put(new byte[]{(byte) bumpSeed});

            programId.write(seedsBuffer);
            seedsBuffer.put(PROGRAM_DERIVED_ADDRESS_BYTES);

            final byte[] programAddress = hash(seedsBuffer.array());

            if (isOffCurve(programAddress))
            {
                return new SolanaProgramDerivedAddress(new SolanaAccount(programAddress), programId, bumpSeed);
            }
            bumpSeed--;
        }
        throw new RuntimeException("Could not find a program address off the curve.");
    }

    private static byte[] hash(final byte[] input)
    {
        final MessageDigest digest;
        try
        {
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch (final NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
        return digest.digest(input);
    }

    private static boolean isOffCurve(final byte[] programAddress)
    {
        try
        {
            return !Ed25519.isOnCurve(programAddress);
        }
        catch (final IllegalArgumentException e)
        {
            return true;
        }
    }

    public static ProgramDerivedAddress deriveProgramAddress(final PublicKey owner, final PublicKey programId)
    {
        List<byte[]> seeds = new ArrayList<>();
        seeds.add(owner.bytes());
        return deriveProgramAddress(seeds, programId);
    }

    SolanaProgramDerivedAddress(final PublicKey address, final PublicKey programAccount, final int nonce)
    {
        this.address = requireNonNull(address, "The address public key must be specified, but was null");
        this.programAccount = requireNonNull(programAccount, "The programId public key must be specified, but was null");
        this.nonce = nonce;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final SolanaProgramDerivedAddress that = (SolanaProgramDerivedAddress) o;
        return nonce == that.nonce && Objects.equals(address, that.address) && Objects.equals(programAccount, that.programAccount);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(address, programAccount, nonce);
    }

    @Override
    public String toString()
    {
        return "SolanaProgramDerivedAccount{" +
                "address=" + address +
                ", programAccount=" + programAccount +
                ", nonce=" + nonce +
                '}';
    }

    @Override
    public PublicKey address()
    {
        return address;
    }

    @Override
    public PublicKey programId()
    {
        return programAccount;
    }

    @Override
    public int nonce()
    {
        return nonce;
    }
}
