package com.valeamoris.solana4j.encoding;

import com.valeamoris.solana4j.api.AccountLookupEntry;
import com.valeamoris.solana4j.api.Blockhash;
import com.valeamoris.solana4j.api.MessageVisitor;
import com.valeamoris.solana4j.api.MessageVisitor.InstructionView;
import com.valeamoris.solana4j.api.PublicKey;
import com.valeamoris.solana4j.api.References;
import com.valeamoris.solana4j.api.TransactionInstruction;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

final class SolanaMessageFormattingCommon
{
    private final ByteBuffer buffer;

    SolanaMessageFormattingCommon(final ByteBuffer buffer)
    {
        this.buffer = requireNonNull(buffer);
    }

    void writeByte(final byte value)
    {
        buffer.put(value);
    }

    byte readByte()
    {
        return buffer.get();
    }

    void writeLong(final long value)
    {
        SolanaShortVec.write(value, buffer);
    }

    long readLong()
    {
        return SolanaShortVec.readLong(buffer);
    }

    void writeInt(final int value)
    {
        SolanaShortVec.write(value, buffer);
    }

    int readInt()
    {
        return SolanaShortVec.readInt(buffer);
    }

    void writeInstructions(final List<TransactionInstruction> transaction, final References references)
    {
        SolanaShortVec.write(transaction.size(), buffer);
        for (final TransactionInstruction instruction : transaction)
        {
            writeInstruction(instruction, references);
        }
    }

    private void writeInstruction(final TransactionInstruction instruction, final References references)
    {
        buffer.put((byte) references.indexOfAccount(instruction.program()));

        SolanaShortVec.write(instruction.accountReferences().size(), buffer);

        for (final TransactionInstruction.AccountReference accref : instruction.accountReferences())
        {
            final int indexOfAccount = references.indexOfAccount(accref.account());
            if (indexOfAccount == -1)
            {
                throw new RuntimeException("Should have found the account.");
            }
            buffer.put((byte) indexOfAccount);
        }

        SolanaShortVec.write(instruction.datasize(), buffer);

        instruction.data().accept(buffer);
    }

    List<InstructionView> readInstructions()
    {
        final int count = SolanaShortVec.readInt(buffer);
        final List<InstructionView> instructions = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
        {
            instructions.add(readInstruction());
        }
        return instructions;
    }

    private InstructionView readInstruction()
    {
        final int program = SolanaShortVec.readInt(buffer);
        final int countAccRefs = SolanaShortVec.readInt(buffer);
        final List<Integer> accRefs = new ArrayList<Integer>();
        for (int i = 0; i < countAccRefs; i++)
        {
            accRefs.add(SolanaShortVec.readInt(buffer));
        }
        final int countData = SolanaShortVec.readInt(buffer);
        final ByteBuffer ro = buffer.asReadOnlyBuffer();
        ro.limit(ro.position() + countData);
        final ByteBuffer data = ro.slice();

        buffer.position(buffer.position() + countData);

        return new SolanaInstructionView(program, accRefs, data);
    }

    void writeBlockHash(final SolanaBlockhash blockHash)
    {
        blockHash.write(buffer);
    }

    Blockhash readBlockHash()
    {
        final byte[] bytes = new byte[32];
        buffer.get(bytes);
        return new SolanaBlockhash(bytes);
    }

    void writeAccountLookups(final List<AccountLookupEntry> accountLookups)
    {
        SolanaShortVec.write(accountLookups.size(), buffer);
        for (final AccountLookupEntry accountLookup : accountLookups)
        {
            writeAccountLookup(accountLookup);
        }
    }

    private void writeAccountLookup(final AccountLookupEntry accountLookup)
    {
        writeAccount(accountLookup.getLookupTableAddress());

        SolanaShortVec.write(accountLookup.getReadWriteLookupEntrys().size(), buffer);
        for (int i = 0; i < accountLookup.getReadWriteLookupEntrys().size(); i++)
        {
            SolanaShortVec.write(accountLookup.getReadWriteLookupEntrys().get(i).getIndex(), buffer);
        }

        SolanaShortVec.write(accountLookup.getReadOnlyLookupEntrys().size(), buffer);
        for (int i = 0; i < accountLookup.getReadOnlyLookupEntrys().size(); i++)
        {
            SolanaShortVec.write(accountLookup.getReadOnlyLookupEntrys().get(i).getIndex(), buffer);
        }
    }

    private void writeAccount(final PublicKey account)
    {
        account.write(buffer);
    }

    List<MessageVisitor.AccountLookupView> readAccountLookups()
    {
        final int count = SolanaShortVec.readInt(buffer);
        final List<MessageVisitor.AccountLookupView> entries = new ArrayList<>();

        for (int i = 0; i < count; i++)
        {
            final byte[] bytes = new byte[32];
            buffer.get(bytes);
            final SolanaAccount accountLookup = new SolanaAccount(bytes);
            final int countReadWrite = SolanaShortVec.readInt(buffer);
            final List<Integer> readWriteIndexes = new ArrayList<>();
            for (int j = 0; j < countReadWrite; j++)
            {
                readWriteIndexes.add(SolanaShortVec.readInt(buffer));
            }
            final int countReadoOnly = SolanaShortVec.readInt(buffer);
            final List<Integer> readOnlyIndexes = new ArrayList<>();
            for (int j = 0; j < countReadoOnly; j++)
            {
                readOnlyIndexes.add(SolanaShortVec.readInt(buffer));
            }
            entries.add(new SolanaAccountLookupView(accountLookup, readWriteIndexes, readOnlyIndexes));
        }

        return entries;
    }

    void reserveSignatures(final int count)
    {
        SolanaShortVec.write(count, buffer);
        buffer.position(buffer.position() + (64 * count));
    }

    List<ByteBuffer> readSignatures()
    {
        final int count = SolanaShortVec.readInt(buffer);
        final List<ByteBuffer> signatures = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
        {
            final byte[] bytes = new byte[64];
            buffer.get(bytes);
            signatures.add(ByteBuffer.wrap(bytes));
        }
        return signatures;
    }

    void writeStaticAccounts(final List<PublicKey> accounts)
    {
        SolanaShortVec.write(accounts.size(), buffer);

        for (final PublicKey account : accounts)
        {
            account.write(buffer);
        }
    }

    public List<PublicKey> readStaticAccounts()
    {
        final int count = SolanaShortVec.readInt(buffer);
        final List<PublicKey> publicKeys = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
        {
            final byte[] bytes = new byte[32];
            buffer.get(bytes);
            publicKeys.add(new SolanaAccount(bytes));
        }
        return publicKeys;
    }
}
