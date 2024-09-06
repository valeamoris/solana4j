package com.lmax.solana4j;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.lmax.solana4j.Solana.account;
import static com.lmax.solana4j.Solana.blockhash;
import static com.lmax.solana4j.Solana4jTestHelper.ACCOUNT1;
import static com.lmax.solana4j.Solana4jTestHelper.ACCOUNT2;
import static com.lmax.solana4j.Solana4jTestHelper.ACCOUNT3;
import static com.lmax.solana4j.Solana4jTestHelper.ACCOUNT4;
import static com.lmax.solana4j.Solana4jTestHelper.ACCOUNT5;
import static com.lmax.solana4j.Solana4jTestHelper.ACCOUNT6;
import static com.lmax.solana4j.Solana4jTestHelper.ACCOUNT7;
import static com.lmax.solana4j.Solana4jTestHelper.ACCOUNT8;
import static com.lmax.solana4j.Solana4jTestHelper.ACCOUNT_LENGTH;
import static com.lmax.solana4j.Solana4jTestHelper.ADDRESS_LOOK_TABLE1;
import static com.lmax.solana4j.Solana4jTestHelper.ADDRESS_LOOK_TABLE2;
import static com.lmax.solana4j.Solana4jTestHelper.BLOCKHASH;
import static com.lmax.solana4j.Solana4jTestHelper.DATA1;
import static com.lmax.solana4j.Solana4jTestHelper.DATA3;
import static com.lmax.solana4j.Solana4jTestHelper.LOOKUP_TABLE_ADDRESS1;
import static com.lmax.solana4j.Solana4jTestHelper.LOOKUP_TABLE_ADDRESS2;
import static com.lmax.solana4j.Solana4jTestHelper.PAYER;
import static com.lmax.solana4j.Solana4jTestHelper.PROGRAM1;
import static com.lmax.solana4j.Solana4jTestHelper.PROGRAM2;
import static com.lmax.solana4j.Solana4jTestHelper.SIGNATURE1;
import static com.lmax.solana4j.Solana4jTestHelper.SIGNATURE2;
import static com.lmax.solana4j.Solana4jTestHelper.SIGNATURE5;
import static com.lmax.solana4j.Solana4jTestHelper.SIGNATURE_LENGTH;
import static com.lmax.solana4j.Solana4jTestHelper.SIGNATURE_PAYER;
import static com.lmax.solana4j.Solana4jTestHelper.SIGNINGS;
import static com.lmax.solana4j.Solana4jTestHelper.UNSIGNED;
import static com.lmax.solana4j.Solana4jTestHelper.fromAccountsTable;
import static com.lmax.solana4j.Solana4jTestHelper.fromBlockhash;
import static com.lmax.solana4j.Solana4jTestHelper.fromInstruction;
import static com.lmax.solana4j.Solana4jTestHelper.fromInstructions;
import static com.lmax.solana4j.Solana4jTestHelper.fromLookupAccountsTable;
import static com.lmax.solana4j.Solana4jTestHelper.fromSignatures;
import static com.lmax.solana4j.Solana4jTestHelper.fromTransactionHeader;
import static com.lmax.solana4j.Solana4jTestHelper.indexOfAccount;
import static com.lmax.solana4j.Solana4jTestHelper.jumpToAccount;
import static com.lmax.solana4j.Solana4jTestHelper.jumpToInstruction;
import static com.lmax.solana4j.Solana4jTestHelper.jumpToSignature;
import static com.lmax.solana4j.Solana4jTestHelper.jumpToTransactionHeader;
import static com.lmax.solana4j.Solana4jTestHelper.reader;
import static com.lmax.solana4j.Solana4jTestHelper.writeComplexPartiallySignedLegacyMessage;
import static com.lmax.solana4j.Solana4jTestHelper.writeComplexPartiallySignedV0Message;
import static com.lmax.solana4j.Solana4jTestHelper.writeComplexSignedLegacyMessageWithNoSignatures;
import static com.lmax.solana4j.Solana4jTestHelper.writeComplexSignedV0MessageWithNoSignatures;
import static com.lmax.solana4j.Solana4jTestHelper.writeComplexUnsignedLegacyMessage;
import static com.lmax.solana4j.Solana4jTestHelper.writeComplexUnsignedV0Message;
import static com.lmax.solana4j.Solana4jTestHelper.writeSimpleFullySignedLegacyMessage;
import static com.lmax.solana4j.Solana4jTestHelper.writeSimpleFullySignedV0Message;
import static com.lmax.solana4j.Solana4jTestHelper.writeSimpleSignedLegacyMessageWithNoSignatures;
import static com.lmax.solana4j.Solana4jTestHelper.writeSimpleSignedV0MessageWithNoSignatures;
import static com.lmax.solana4j.Solana4jTestHelper.writeSimpleUnsignedLegacyMessage;
import static com.lmax.solana4j.Solana4jTestHelper.writeSimpleUnsignedLegacyMessageWithBigData;
import static com.lmax.solana4j.Solana4jTestHelper.writeSimpleUnsignedV0Message;
import static com.lmax.solana4j.Solana4jTestHelper.writeSimpleUnsignedV0MessageWithBigData;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SolanaMessageBuilderConformanceTest
{

    @Test
    void requiresByteBuffer()
    {
        assertThatThrownBy(() ->
                Solana.builder(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsTooSmallBuffer()
    {
        assertThatThrownBy(() ->
                writeSimpleUnsignedLegacyMessage(ByteBuffer.allocate(20)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsWhenPayerMissing()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        assertThatThrownBy(() -> Solana.builder(buffer)
                    .legacy()
                    .seal()
                    .unsigned()
                    .build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void writesToBuffer()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeSimpleUnsignedLegacyMessage(buffer);

        assertThat(buffer.position()).isEqualTo(0);
        assertThat(buffer.limit()).isLessThan(Solana.MAX_MESSAGE_SIZE);
    }

    @Test
    void checkSimpleUnsignedLegacyMessageHasThreeSigners()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeSimpleUnsignedLegacyMessage(buffer);

        reader(buffer).expect("number of signers", (byte) 3);
    }

    @Test
    void checkSimpleUnsignedV0MessageHasThreeSigners()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeSimpleUnsignedV0Message(buffer);

        reader(buffer).expect("number of signers", (byte) 3);
    }

    @Test
    void checkComplexUnsignedLegacyMessageHasSixSigners()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeComplexUnsignedLegacyMessage(buffer);

        reader(buffer).expect("number of signers", (byte) 6);
    }

    @Test
    void checkComplexUnsignedV0MessageHasSixSigners()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeComplexUnsignedV0Message(buffer);

        reader(buffer).expect("number of signers", (byte) 6);
    }

    @Test
    void checkSimpleUnsignedLegacyMessageSignaturesAreUntouched()
    {
        final var bytes = new byte[Solana.MAX_MESSAGE_SIZE];
        Arrays.fill(bytes, (byte) -77);
        final var buffer = ByteBuffer.wrap(bytes);

        writeSimpleUnsignedLegacyMessage(buffer);

        final var signatures = new byte[3 * SIGNATURE_LENGTH];
        buffer.position(1).get(signatures);

        final var expected = new byte[3 * SIGNATURE_LENGTH];
        Arrays.fill(expected, (byte) -77);

        assertThat(signatures).containsExactly(expected);
    }

    @Test
    void checkSimpleUnsignedV0MessageSignaturesAreUntouched()
    {
        final var bytes = new byte[Solana.MAX_MESSAGE_SIZE];
        Arrays.fill(bytes, (byte) -77);
        final var buffer = ByteBuffer.wrap(bytes);

        writeSimpleUnsignedV0Message(buffer);

        final var signatures = new byte[3 * SIGNATURE_LENGTH];
        buffer.position(1).get(signatures);

        final var expected = new byte[3 * SIGNATURE_LENGTH];
        Arrays.fill(expected, (byte) -77);

        assertThat(signatures).containsExactly(expected);
    }

    @Test
    void checkSimpleSignedLegacyMessageWithNoSignersThatSignaturesAreUntouched()
    {
        final var bytes = new byte[Solana.MAX_MESSAGE_SIZE];
        Arrays.fill(bytes, (byte) -77);
        final var buffer = ByteBuffer.wrap(bytes);

        writeSimpleSignedLegacyMessageWithNoSignatures(buffer);

        final var signatures = new byte[3 * SIGNATURE_LENGTH];
        buffer.position(1).get(signatures);

        final var expected = new byte[3 * SIGNATURE_LENGTH];
        Arrays.fill(expected, (byte) -77);

        assertThat(signatures).containsExactly(expected);
    }

    @Test
    void checkSimpleSignedV0MessageWithNoSignersThatSignaturesAreUntouched()
    {
        final var bytes = new byte[Solana.MAX_MESSAGE_SIZE];
        Arrays.fill(bytes, (byte) -77);
        final var buffer = ByteBuffer.wrap(bytes);

        writeSimpleSignedV0MessageWithNoSignatures(buffer);

        final var signatures = new byte[3 * SIGNATURE_LENGTH];
        buffer.position(1).get(signatures);

        final var expected = new byte[3 * SIGNATURE_LENGTH];
        Arrays.fill(expected, (byte) -77);

        assertThat(signatures).containsExactly(expected);
    }

    @Test
    void checkSimpleFullySignedLegacyMessageSignaturesAreAllUpdated()
    {
        final var bytes = new byte[Solana.MAX_MESSAGE_SIZE];
        Arrays.fill(bytes, (byte) -77);
        final var buffer = ByteBuffer.wrap(bytes);

        writeSimpleFullySignedLegacyMessage(buffer);

        final var signatures = new byte[3 * SIGNATURE_LENGTH];
        buffer.position(1).get(signatures);

        jumpToTransactionHeader(buffer);

        final var signersCount = buffer.get();

        for (int i = 0; i < signersCount; i++)
        {
            jumpToAccount(buffer, i);
            final var account = new byte[ACCOUNT_LENGTH];
            buffer.get(account);

            jumpToSignature(buffer, i);
            final var actualSignature = new byte[SIGNATURE_LENGTH];
            buffer.get(actualSignature);

            final var expectedSignature = SIGNINGS.getOrDefault(account(account), UNSIGNED);

            assertThat(actualSignature).isEqualTo(expectedSignature);
        }
    }

    @Test
    void checkSimpleFullyV0SignedMessageSignaturesAreAllUpdated()
    {
        final var bytes = new byte[Solana.MAX_MESSAGE_SIZE];
        Arrays.fill(bytes, (byte) -77);
        final var buffer = ByteBuffer.wrap(bytes);

        writeSimpleFullySignedV0Message(buffer);

        final var signatures = new byte[3 * SIGNATURE_LENGTH];
        buffer.position(1).get(signatures);

        jumpToTransactionHeader(buffer);

        final var signersCount = buffer.get();

        for (int i = 0; i < signersCount; i++)
        {
            jumpToAccount(buffer, i);
            final var account = new byte[ACCOUNT_LENGTH];
            buffer.get(account);

            jumpToSignature(buffer, i);
            final var actualSignature = new byte[SIGNATURE_LENGTH];
            buffer.get(actualSignature);

            final var expectedSignature = SIGNINGS.getOrDefault(account(account), UNSIGNED);

            assertThat(actualSignature).isEqualTo(expectedSignature);
        }
    }

    @Test
    void checkComplexUnsignedLegacyMessageSignaturesAreUntouched()
    {
        final var bytes = new byte[Solana.MAX_MESSAGE_SIZE];
        Arrays.fill(bytes, (byte) -77);
        final var buffer = ByteBuffer.wrap(bytes);

        writeComplexUnsignedLegacyMessage(buffer);

        final var signatures = new byte[6 * SIGNATURE_LENGTH];
        buffer.position(1).get(signatures);

        final var expected = new byte[6 * SIGNATURE_LENGTH];
        Arrays.fill(expected, (byte) -77);

        assertThat(signatures).containsExactly(expected);
    }

    @Test
    void checkComplexUnsignedV0MessageSignaturesAreUntouched()
    {
        final var bytes = new byte[Solana.MAX_MESSAGE_SIZE];
        Arrays.fill(bytes, (byte) -77);
        final var buffer = ByteBuffer.wrap(bytes);

        writeComplexUnsignedV0Message(buffer);

        final var signatures = new byte[6 * SIGNATURE_LENGTH];
        buffer.position(1).get(signatures);

        final var expected = new byte[6 * SIGNATURE_LENGTH];
        Arrays.fill(expected, (byte) -77);

        assertThat(signatures).containsExactly(expected);
    }

    @Test
    void checkComplexSignedLegacyMessageWithNoSignersThatSignaturesAreUntouched()
    {
        final var bytes = new byte[Solana.MAX_MESSAGE_SIZE];
        Arrays.fill(bytes, (byte) -77);
        final var buffer = ByteBuffer.wrap(bytes);

        writeComplexSignedLegacyMessageWithNoSignatures(buffer);

        final var signatures = new byte[6 * SIGNATURE_LENGTH];
        buffer.position(1).get(signatures);

        final var expected = new byte[6 * SIGNATURE_LENGTH];
        Arrays.fill(expected, (byte) -77);

        assertThat(signatures).containsExactly(expected);
    }

    @Test
    void checkComplexSignedV0MessageWithNoSignersThatSignaturesAreUntouched()
    {
        final var bytes = new byte[Solana.MAX_MESSAGE_SIZE];
        Arrays.fill(bytes, (byte) -77);
        final var buffer = ByteBuffer.wrap(bytes);

        writeComplexSignedV0MessageWithNoSignatures(buffer);

        final var signatures = new byte[6 * SIGNATURE_LENGTH];
        buffer.position(1).get(signatures);

        final var expected = new byte[6 * SIGNATURE_LENGTH];
        Arrays.fill(expected, (byte) -77);

        assertThat(signatures).containsExactly(expected);
    }

    @Test
    void checkComplexPartiallySignedLegacyMessageSignaturesArePartiallyUpdated()
    {
        final var bytes = new byte[Solana.MAX_MESSAGE_SIZE];
        Arrays.fill(bytes, (byte) -77);
        final var buffer = ByteBuffer.wrap(bytes);

        writeComplexPartiallySignedLegacyMessage(buffer);

        final var signatures = new byte[6 * SIGNATURE_LENGTH];
        buffer.position(1).get(signatures);

        jumpToTransactionHeader(buffer);

        final var signersCount = buffer.get();

        final var expectedSigners = Set.of(account(PAYER), account(ACCOUNT1), account(ACCOUNT2), account(ACCOUNT7));

        for (int i = 0; i < signersCount; i++)
        {
            jumpToAccount(buffer, i);
            final var account = new byte[ACCOUNT_LENGTH];
            buffer.get(account);

            jumpToSignature(buffer, i);
            final var actualSignature = new byte[SIGNATURE_LENGTH];
            buffer.get(actualSignature);

            final var expectedSignature = SIGNINGS.get(account(account));

            assertThat(expectedSignature).as("unknown account").isNotNull();

            assertThat(actualSignature)
                    .isEqualTo(expectedSigners.contains(account(account))
                               ? expectedSignature
                               : UNSIGNED);
        }
    }

    @Test
    void checkComplexPartiallySignedV0MessageSignaturesArePartiallyUpdated()
    {
        final var bytes = new byte[Solana.MAX_MESSAGE_SIZE];
        Arrays.fill(bytes, (byte) -77);
        final var buffer = ByteBuffer.wrap(bytes);

        writeComplexPartiallySignedV0Message(buffer);

        final var signatures = new byte[6 * SIGNATURE_LENGTH];
        buffer.position(1).get(signatures);

        jumpToTransactionHeader(buffer);

        final var signersCount = buffer.get();

        final var expectedSigners = Set.of(account(PAYER), account(ACCOUNT1), account(ACCOUNT2), account(ACCOUNT7));

        for (int i = 0; i < signersCount; i++)
        {
            jumpToAccount(buffer, i);
            final var account = new byte[ACCOUNT_LENGTH];
            buffer.get(account);

            jumpToSignature(buffer, i);
            final var actualSignature = new byte[SIGNATURE_LENGTH];
            buffer.get(actualSignature);

            final var expectedSignature = SIGNINGS.get(account(account));

            assertThat(expectedSignature).as("unknown account").isNotNull();

            assertThat(actualSignature)
                    .isEqualTo(expectedSigners.contains(account(account))
                            ? expectedSignature
                            : UNSIGNED);
        }
    }

    @Test
    void checkSimpleUnsignedLegacyMessageIsLegacyFormat()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeSimpleUnsignedLegacyMessage(buffer);

        jumpToTransactionHeader(buffer);

        // first byte of header
        final var first = buffer.get();

        // high bit must be clear for legacy transactions
        assertThat(first & 0x80).isZero();
    }

    @Test
    void checkSimpleUnsignedV0MessageIsV0Format()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeSimpleUnsignedV0Message(buffer);

        jumpToTransactionHeader(buffer);

        // first byte of header
        final var first = buffer.get();

        // high bit must be clear for legacy transactions
        assertThat(first & 0x80).isEqualTo(0x80);
    }

    @Test
    void checkComplexUnsignedLegacyMessageIsLegacyFormat()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeComplexUnsignedLegacyMessage(buffer);

        jumpToTransactionHeader(buffer);

        // first byte of header
        final var first = buffer.get();

        // high bit must be clear for legacy transactions
        assertThat(first & 0x80).isZero();
    }

    @Test
    void checkComplexUnsignedV0MessageIsV0Format()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeComplexUnsignedV0Message(buffer);

        jumpToTransactionHeader(buffer);

        // first byte of header
        final var first = buffer.get();

        // high bit must be clear for legacy transactions
        assertThat(first & 0x80).isEqualTo(0x80);
    }

    @Test
    void checkSimpleUnsignedLegacyMessageHeader()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeSimpleUnsignedLegacyMessage(buffer);

        fromTransactionHeader(buffer)
                .expect("count signed accounts", (byte) 3)
                .expect("count signed read-only accounts", (byte) 1)
                .expect("count unsigned read-only accounts", (byte) 2);
    }

    @Test
    void checkSimpleUnsignedV0MessageHeader()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeSimpleUnsignedV0Message(buffer);

        fromTransactionHeader(buffer)
                .expect("version header", (byte) 128)
                .expect("count signed accounts", (byte) 3)
                .expect("count signed read-only accounts", (byte) 1)
                .expect("count unsigned read-only accounts", (byte) 2);
    }

    @Test
    void checkComplexUnsignedLegacyMessageHeader()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeComplexUnsignedLegacyMessage(buffer);

        fromTransactionHeader(buffer)
                .expect("count signed accounts", (byte) 6)
                .expect("count signed read-only accounts", (byte) 2)
                .expect("count unsigned read-only accounts", (byte) 4);
    }

    @Test
    void checkComplexUnsignedV0MessageHeader()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeComplexUnsignedV0Message(buffer);

        fromTransactionHeader(buffer)
                .expect("version header", (byte) 128)
                .expect("count signed accounts", (byte) 6)
                .expect("count signed read-only accounts", (byte) 2)
                .expect("count unsigned read-only accounts", (byte) 4);
    }

    @Test
    void checkSimpleUnsignedLegacyMessageAccountsTable()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeSimpleUnsignedLegacyMessage(buffer);

        // (There does not appear to be a defined sequence for accounts within
        //  the same subsection of the accounts table.
        //  Some TypeScript client code sorts the account keys Base58 encoded
        //  lexicographically within the same subsection. (?!))
        fromAccountsTable(buffer)
                .expectShortVecInteger("count accounts", 6)
        // signed read-write
                .expect("payer account", PAYER) // (payer must be first account)
                .expect("second account", ACCOUNT1)
        // signed read-only
                .expect("third account", ACCOUNT2)
        // unsigned read-write
                .expect("fourth account", ACCOUNT3);
        // unsigned read-only
        final var unsignedReadOnlyAccounts = new HashSet<>();

        reader(buffer)
                .expectOneOf("fifth account", actual -> unsignedReadOnlyAccounts.add(account(actual)), PROGRAM1, ACCOUNT4)
                .expectOneOf("sixth account", actual -> unsignedReadOnlyAccounts.add(account(actual)), PROGRAM1, ACCOUNT4);

        assertThat(unsignedReadOnlyAccounts)
                .containsExactlyInAnyOrder(account(PROGRAM1), account(ACCOUNT4));
    }

    @Test
    void checkSimpleUnsignedV0MessageAccountsTable()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeSimpleUnsignedV0Message(buffer);

        // (There does not appear to be a defined sequence for accounts within
        //  the same subsection of the accounts table.
        //  Some TypeScript client code sorts the account keys Base58 encoded
        //  lexicographically within the same subsection. (?!))
        fromAccountsTable(buffer)
                .expectShortVecInteger("count accounts", 4)
                // signed read-write
                .expect("payer account", PAYER) // (payer must be first account)
                .expect("second account", ACCOUNT1)
                // signed read-only
                .expect("third account", ACCOUNT2);
        // unsigned read-only
        final var unsignedReadOnlyAccounts = new HashSet<>();

        reader(buffer)
                .expectOneOf("fourth account", actual -> unsignedReadOnlyAccounts.add(account(actual)), PROGRAM1);

        assertThat(unsignedReadOnlyAccounts)
                .containsExactlyInAnyOrder(account(PROGRAM1));

        fromLookupAccountsTable(buffer)
                // number of lookup accounts
                .expectShortVecInteger("number of account lookups", 2)
                // the order of lookup accounts is defined simply by the order they appear in the following
                // final var allAccountReferences = mergeAccountReferences(instructions, payerReference);
                // first look up account is account3 which is in lookup_table_address_1
                .expect("lookup table address relating to table containing account 8", LOOKUP_TABLE_ADDRESS1)
                // only account3 appears in this lookup table and is writable
                .expectShortVecInteger("number of writable accounts referenced in lookup table", 1)
                // index of the account in the table
                .expectShortVecInteger("the index of account3 in the table", 1)
                // only account3 appears in this lookup table but is writable so not readable only
                .expectShortVecInteger("number of readable accounts referenced in lookup table", 0)
                // second look up account is account4 which is in lookup_table_address_2
                .expect("lookup table address relating to table containing account 4", LOOKUP_TABLE_ADDRESS2)
                // only account4 appears in this lookup table but it is not writable
                .expectShortVecInteger("number of writable accounts referenced in lookup table", 0)
                // only account4 appears in this lookup table and is readable only
                .expectShortVecInteger("number of readable accounts referenced in lookup table", 1)
                // index of the account in the table
                .expectShortVecInteger("the index of account4 in the table", 1);
    }

    @Test
    void checkComplexUnsignedLegacyMessageAccountsTable()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeComplexUnsignedLegacyMessage(buffer);

        // (There does not appear to be a defined sequence for accounts within
        //  the same subsection of the accounts table.
        //  Some TypeScript client code sorts the account keys Base58 encoded
        //  lexicographically within the same subsection. (?!))

        fromAccountsTable(buffer)
                .expectShortVecInteger("count accounts", 11)
                // signed read-write
                .expect("payer account", PAYER) // (payer must be first account)
                .expect("second account", ACCOUNT1)
                .expect("third account", ACCOUNT6)
                .expect("fourth account", ACCOUNT3)
                // signed read-only
                .expect("fifth account", ACCOUNT2)
                .expect("sixth account", ACCOUNT7)
                // unsigned read-write
                .expect("seventh account", ACCOUNT8);

        // unsigned read-only
        final var unsignedReadOnlyAccounts = new HashSet<>();

        reader(buffer)
                .expectOneOf("eighth account", actual -> unsignedReadOnlyAccounts.add(account(actual)), PROGRAM1, ACCOUNT4, PROGRAM2, ACCOUNT5)
                .expectOneOf("ninth account", actual -> unsignedReadOnlyAccounts.add(account(actual)), PROGRAM1, ACCOUNT4, PROGRAM2, ACCOUNT5)
                .expectOneOf("tenth account", actual -> unsignedReadOnlyAccounts.add(account(actual)), PROGRAM1, ACCOUNT4, PROGRAM2, ACCOUNT5)
                .expectOneOf("eleventh account", actual -> unsignedReadOnlyAccounts.add(account(actual)), PROGRAM1, ACCOUNT4, PROGRAM2, ACCOUNT5);

        assertThat(unsignedReadOnlyAccounts)
                .containsExactlyInAnyOrder(
                        account(PROGRAM1), account(ACCOUNT4), account(PROGRAM2), account(ACCOUNT5));
    }

    @Test
    void checkComplexUnsignedV0MessageAccountsTable()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeComplexUnsignedV0Message(buffer);

        // (There does not appear to be a defined sequence for accounts within
        //  the same subsection of the accounts table.
        //  Some TypeScript client code sorts the account keys Base58 encoded
        //  lexicographically within the same subsection. (?!))

        fromAccountsTable(buffer)
                .expectShortVecInteger("count accounts", 9)
                // signed read-write
                .expect("payer account", PAYER) // (payer must be first account)
                .expect("second account", ACCOUNT1)
                .expect("third account", ACCOUNT6)
                .expect("fourth account", ACCOUNT3)
                // signed read-only
                .expect("fifth account", ACCOUNT2)
                .expect("sixth account", ACCOUNT7);

        // unsigned read-only
        final var unsignedReadOnlyAccounts = new HashSet<>();

        reader(buffer)
                .expectOneOf("seventh account", actual -> unsignedReadOnlyAccounts.add(account(actual)), PROGRAM1, PROGRAM2, ACCOUNT5)
                .expectOneOf("eigth account", actual -> unsignedReadOnlyAccounts.add(account(actual)), PROGRAM1, PROGRAM2, ACCOUNT5)
                .expectOneOf("ninth account", actual -> unsignedReadOnlyAccounts.add(account(actual)), PROGRAM1, PROGRAM2, ACCOUNT5);

        assertThat(unsignedReadOnlyAccounts)
                .containsExactlyInAnyOrder(
                        account(PROGRAM1), account(PROGRAM2), account(ACCOUNT5));

        fromLookupAccountsTable(buffer)
                // number of lookup accounts
                .expectShortVecInteger("number of account lookups", 2)
                // the order of lookup accounts is defined simply by the order they appear in the following
                // final var allAccountReferences = mergeAccountReferences(instructions, payerReference);
                // first look up account is account8 which is in lookup_table_address_1
                .expect("lookup table address relating to table containing account 8", LOOKUP_TABLE_ADDRESS1)
                // only account8 appears in this lookup table and is writable
                .expectShortVecInteger("number of writable accounts referenced in lookup table", 1)
                // index of the account in the table
                .expectShortVecInteger("the index of account8 in the table", 0)
                // only account8 appears in this lookup table but is writable so not readable only
                .expectShortVecInteger("number of readable accounts referenced in lookup table", 0)
                // second look up account is account4 which is in lookup_table_address_2
                .expect("lookup table address relating to table containing account 4", LOOKUP_TABLE_ADDRESS2)
                // only account4 appears in this lookup table but it is not writable
                .expectShortVecInteger("number of writable accounts referenced in lookup table", 0)
                // only account4 appears in this lookup table and is readable only
                .expectShortVecInteger("number of readable accounts referenced in lookup table", 1)
                // index of the account in the table
                .expectShortVecInteger("the index of account4 in the table", 1);
    }

    @Test
    void checkSimpleUnsignedLegacyMessageBlockhash()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeSimpleUnsignedLegacyMessage(buffer);

        fromBlockhash(buffer)
                .expect("block hash", BLOCKHASH);
    }

    @Test
    void checkSimpleUnsignedV0MessageBlockhash()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeSimpleUnsignedV0Message(buffer);

        fromBlockhash(buffer)
                .expect("block hash", BLOCKHASH);
    }

    @Test
    void checkComplexUnsignedLegacyMessageBlockhash()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeComplexUnsignedLegacyMessage(buffer);

        fromBlockhash(buffer)
                .expect("block hash", BLOCKHASH);
    }

    @Test
    void checkComplexUnsignedV0MessageBlockhash()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeComplexUnsignedV0Message(buffer);

        fromBlockhash(buffer)
                .expect("block hash", BLOCKHASH);
    }


    @Test
    void checkSimpleUnsignedLegacyMessageTransactionInstructionCount()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeSimpleUnsignedLegacyMessage(buffer);

        fromInstructions(buffer)
                .expectShortVecInteger("count instructions", 1);
    }

    @Test
    void checkSimpleUnsignedV0MessageTransactionInstructionCount()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeSimpleUnsignedV0Message(buffer);

        fromInstructions(buffer).expectShortVecInteger("count instructions", 1);
    }


    @Test
    void checkComplexUnsignedLegacyMessageTransactionInstructionCount()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeComplexUnsignedLegacyMessage(buffer);

        fromInstructions(buffer)
                .expectShortVecInteger("count instructions", 3);
    }

    @Test
    void checkComplexUnsignedV0MessageTransactionInstructionCount()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeComplexUnsignedV0Message(buffer);

        fromInstructions(buffer)
                .expectShortVecInteger("count instructions", 3);
    }

    @Test
    void checkPartiallySignedLegacyMessageAddFirstSignature()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);
        Arrays.fill(buffer.array(), (byte) -77);

        writeSimpleUnsignedLegacyMessage(buffer);

        Solana.forSigning(buffer)
                    .by(account(PAYER), (transaction, signature) -> signature.put(SIGNATURE_PAYER))
                    .build();

        fromSignatures(buffer)
                .expectShortVecInteger("number of signatures", 3)
                .expect("first signature (payer)", SIGNATURE_PAYER)
                .expect("second signature", UNSIGNED)
                .expect("third signature", UNSIGNED);
    }

    @Test
    void checkPartiallySignedV0MessageAddFirstSignature()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);
        Arrays.fill(buffer.array(), (byte) -77);

        writeSimpleUnsignedV0Message(buffer);

        Solana.forSigning(buffer)
                .by(account(PAYER), (transaction, signature) -> signature.put(SIGNATURE_PAYER))
                .build();

        fromSignatures(buffer)
                .expectShortVecInteger("number of signatures", 3)
                .expect("first signature (payer)", SIGNATURE_PAYER)
                .expect("second signature", UNSIGNED)
                .expect("third signature", UNSIGNED);
    }

    @Test
    void checkPartiallySignedLegacyMessageAddMidSignature()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);
        Arrays.fill(buffer.array(), (byte) -77);

        writeSimpleUnsignedLegacyMessage(buffer);

        Solana.forSigning(buffer)
                .by(account(ACCOUNT1), (transaction, signature) -> signature.put(SIGNATURE1))
                .build();

        fromSignatures(buffer)
                .expectShortVecInteger("number of signatures", 3)
                .expect("first signature (payer)", UNSIGNED)
                .expect("second signature", SIGNATURE1)
                .expect("third signature", UNSIGNED);
    }

    @Test
    void checkPartiallySignedV0MessageAddMidSignature()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);
        Arrays.fill(buffer.array(), (byte) -77);

        writeSimpleUnsignedV0Message(buffer);

        Solana.forSigning(buffer)
                .by(account(ACCOUNT1), (transaction, signature) -> signature.put(SIGNATURE1))
                .build();

        fromSignatures(buffer)
                .expectShortVecInteger("number of signatures", 3)
                .expect("first signature (payer)", UNSIGNED)
                .expect("second signature", SIGNATURE1)
                .expect("third signature", UNSIGNED);
    }

    @Test
    void checkPartiallySignedLegacyMessageAddEndSignature()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);
        Arrays.fill(buffer.array(), (byte) -77);

        writeSimpleUnsignedLegacyMessage(buffer);

        Solana.forSigning(buffer)
                .by(account(ACCOUNT2), (transaction, signature) -> signature.put(SIGNATURE2))
                .build();

        fromSignatures(buffer)
                .expectShortVecInteger("number of signatures", 3)
                .expect("first signature (payer)", UNSIGNED)
                .expect("second signature", UNSIGNED)
                .expect("third signature", SIGNATURE2);
    }

    @Test
    void checkPartiallySignedV0MessageAddEndSignature()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);
        Arrays.fill(buffer.array(), (byte) -77);

        writeSimpleUnsignedV0Message(buffer);

        Solana.forSigning(buffer)
                .by(account(ACCOUNT2), (transaction, signature) -> signature.put(SIGNATURE2))
                .build();

        fromSignatures(buffer)
                .expectShortVecInteger("number of signatures", 3)
                .expect("first signature (payer)", UNSIGNED)
                .expect("second signature", UNSIGNED)
                .expect("third signature", SIGNATURE2);
    }

    @Test
    void checkPartiallySignedLegacyMessageAddMultipleSignatures()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);
        Arrays.fill(buffer.array(), (byte) -77);

        writeSimpleUnsignedLegacyMessage(buffer);

        Solana.forSigning(buffer)
                .by(account(PAYER), (transaction, signature) -> signature.put(SIGNATURE_PAYER))
                .by(account(ACCOUNT2), (transaction, signature) -> signature.put(SIGNATURE2))
                .build();

        fromSignatures(buffer)
                .expectShortVecInteger("number of signatures", 3)
                .expect("first signature (payer)", SIGNATURE_PAYER)
                .expect("second signature", UNSIGNED)
                .expect("third signature", SIGNATURE2);
    }

    @Test
    void checkPartiallySignedV0MessageAddMultipleSignatures()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);
        Arrays.fill(buffer.array(), (byte) -77);

        writeSimpleUnsignedV0Message(buffer);

        Solana.forSigning(buffer)
                .by(account(PAYER), (transaction, signature) -> signature.put(SIGNATURE_PAYER))
                .by(account(ACCOUNT2), (transaction, signature) -> signature.put(SIGNATURE2))
                .build();

        fromSignatures(buffer)
                .expectShortVecInteger("number of signatures", 3)
                .expect("first signature (payer)", SIGNATURE_PAYER)
                .expect("second signature", UNSIGNED)
                .expect("third signature", SIGNATURE2);
    }

    @Test
    void checkPartiallySignedLegacyMessageAddAllSignatures()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);
        Arrays.fill(buffer.array(), (byte) -77);

        writeSimpleUnsignedLegacyMessage(buffer);

        Solana.forSigning(buffer)
                .by(account(PAYER), (transaction, signature) -> signature.put(SIGNATURE_PAYER))
                .by(account(ACCOUNT2), (transaction, signature) -> signature.put(SIGNATURE2))
                .by(account(ACCOUNT1), (transaction, signature) -> signature.put(SIGNATURE1))
                .build();

        fromSignatures(buffer)
                .expectShortVecInteger("number of signatures", 3)
                .expect("first signature (payer)", SIGNATURE_PAYER)
                .expect("second signature", SIGNATURE1)
                .expect("third signature", SIGNATURE2);
    }

    @Test
    void checkPartiallySignedV0MessageAddAllSignatures()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);
        Arrays.fill(buffer.array(), (byte) -77);

        writeSimpleUnsignedV0Message(buffer);

        Solana.forSigning(buffer)
                .by(account(PAYER), (transaction, signature) -> signature.put(SIGNATURE_PAYER))
                .by(account(ACCOUNT2), (transaction, signature) -> signature.put(SIGNATURE2))
                .by(account(ACCOUNT1), (transaction, signature) -> signature.put(SIGNATURE1))
                .build();

        fromSignatures(buffer)
                .expectShortVecInteger("number of signatures", 3)
                .expect("first signature (payer)", SIGNATURE_PAYER)
                .expect("second signature", SIGNATURE1)
                .expect("third signature", SIGNATURE2);
    }

    @Test
    void checkPartiallySignedLegacyMessageChangeSignature()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeSimpleFullySignedLegacyMessage(buffer);

        Solana.forSigning(buffer)
                .by(account(ACCOUNT1), (transaction, signature) -> signature.put(SIGNATURE5))
                .build();

        fromSignatures(buffer)
                .expectShortVecInteger("number of signatures", 3)
                .expect("first signature (payer)", SIGNATURE_PAYER)
                .expect("second signature", SIGNATURE5)
                .expect("third signature", SIGNATURE2);
    }

    @Test
    void checkPartiallySignedV0MessageChangeSignature()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeSimpleFullySignedV0Message(buffer);

        Solana.forSigning(buffer)
                .by(account(ACCOUNT1), (transaction, signature) -> signature.put(SIGNATURE5))
                .build();

        fromSignatures(buffer)
                .expectShortVecInteger("number of signatures", 3)
                .expect("first signature (payer)", SIGNATURE_PAYER)
                .expect("second signature", SIGNATURE5)
                .expect("third signature", SIGNATURE2);
    }

    @Test
    void checkSimpleLegacyMessageInstructionAccountReferences()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeSimpleUnsignedLegacyMessage(buffer);

        fromInstructions(buffer)
                .expectShortVecInteger("number of instructions", 1)
                .expect("program index", indexOfAccount(buffer, PROGRAM1))
                .expect("number of accounts", (byte) 4)
                .expect("first account index", indexOfAccount(buffer, ACCOUNT4))
                .expect("second account index", indexOfAccount(buffer, ACCOUNT1))
                .expect("third account index", indexOfAccount(buffer, ACCOUNT2))
                .expect("fourth account index", indexOfAccount(buffer, ACCOUNT3));
    }

    @Test
    void checkSimpleV0MessageInstructionAccountReferences()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeSimpleUnsignedV0Message(buffer);

        fromInstructions(buffer)
                .expectShortVecInteger("number of instructions", 1)
                .expect("program index", indexOfAccount(buffer, PROGRAM1, List.of(ADDRESS_LOOK_TABLE1, ADDRESS_LOOK_TABLE2)))
                .expect("number of accounts", (byte) 4)
                .expect("first account index", indexOfAccount(buffer, ACCOUNT4, List.of(ADDRESS_LOOK_TABLE1, ADDRESS_LOOK_TABLE2)))
                .expect("second account index", indexOfAccount(buffer, ACCOUNT1, List.of(ADDRESS_LOOK_TABLE1, ADDRESS_LOOK_TABLE2)))
                .expect("third account index", indexOfAccount(buffer, ACCOUNT2, List.of(ADDRESS_LOOK_TABLE1, ADDRESS_LOOK_TABLE2)))
                .expect("fourth account index", indexOfAccount(buffer, ACCOUNT3, List.of(ADDRESS_LOOK_TABLE1, ADDRESS_LOOK_TABLE2)));
    }

    @Test
    void checkComplexLegacyMessageInstructionAccountReferences()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeComplexUnsignedLegacyMessage(buffer);

        fromInstruction(buffer, 0)
                .expect("program index", indexOfAccount(buffer, PROGRAM1))
                .expect("number of accounts", (byte) 4)
                .expect("first account index", indexOfAccount(buffer, ACCOUNT4))
                .expect("second account index", indexOfAccount(buffer, ACCOUNT1))
                .expect("third account index", indexOfAccount(buffer, ACCOUNT2))
                .expect("fourth account index", indexOfAccount(buffer, ACCOUNT3));

        fromInstruction(buffer, 1)
                .expect("program index", indexOfAccount(buffer, PROGRAM2))
                .expect("number of accounts", (byte) 7)
                .expect("first account index", indexOfAccount(buffer, ACCOUNT5))
                .expect("second account index", indexOfAccount(buffer, ACCOUNT1))
                .expect("third account index", indexOfAccount(buffer, ACCOUNT6))
                .expect("fourth account index", indexOfAccount(buffer, ACCOUNT2))
                .expect("fifth account index", indexOfAccount(buffer, ACCOUNT7))
                .expect("sixth account index", indexOfAccount(buffer, ACCOUNT3))
                .expect("seventh account index", indexOfAccount(buffer, ACCOUNT8));

        fromInstruction(buffer, 2)
                .expect("program index", indexOfAccount(buffer, PROGRAM1))
                .expect("number of accounts", (byte) 1)
                .expect("fourth account index", indexOfAccount(buffer, ACCOUNT3));
    }

    @Test
    void checkComplexV0MessageInstructionAccountReferences()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeComplexUnsignedV0Message(buffer);

        fromInstruction(buffer, 0)
                .expect("program index", indexOfAccount(buffer, PROGRAM1, List.of(ADDRESS_LOOK_TABLE1, ADDRESS_LOOK_TABLE2)))
                .expect("number of accounts", (byte) 4)
                .expect("first account index", indexOfAccount(buffer, ACCOUNT4, List.of(ADDRESS_LOOK_TABLE1, ADDRESS_LOOK_TABLE2)))
                .expect("first account index", indexOfAccount(buffer, ACCOUNT1, List.of(ADDRESS_LOOK_TABLE1, ADDRESS_LOOK_TABLE2)))
                .expect("second account index", indexOfAccount(buffer, ACCOUNT2, List.of(ADDRESS_LOOK_TABLE1, ADDRESS_LOOK_TABLE2)))
                .expect("fourth account index", indexOfAccount(buffer, ACCOUNT3, List.of(ADDRESS_LOOK_TABLE1, ADDRESS_LOOK_TABLE2)));

        fromInstruction(buffer, 1)
                .expect("program index", indexOfAccount(buffer, PROGRAM2, List.of(ADDRESS_LOOK_TABLE1, ADDRESS_LOOK_TABLE2)))
                .expect("number of accounts", (byte) 7)
                .expect("first account index", indexOfAccount(buffer, ACCOUNT5, List.of(ADDRESS_LOOK_TABLE1, ADDRESS_LOOK_TABLE2)))
                .expect("second account index", indexOfAccount(buffer, ACCOUNT1, List.of(ADDRESS_LOOK_TABLE1, ADDRESS_LOOK_TABLE2)))
                .expect("third account index", indexOfAccount(buffer, ACCOUNT6, List.of(ADDRESS_LOOK_TABLE1, ADDRESS_LOOK_TABLE2)))
                .expect("fourth account index", indexOfAccount(buffer, ACCOUNT2, List.of(ADDRESS_LOOK_TABLE1, ADDRESS_LOOK_TABLE2)))
                .expect("fifth account index", indexOfAccount(buffer, ACCOUNT7, List.of(ADDRESS_LOOK_TABLE1, ADDRESS_LOOK_TABLE2)))
                .expect("sixth account index", indexOfAccount(buffer, ACCOUNT3, List.of(ADDRESS_LOOK_TABLE1, ADDRESS_LOOK_TABLE2)))
                .expect("seventh account index", indexOfAccount(buffer, ACCOUNT8, List.of(ADDRESS_LOOK_TABLE1, ADDRESS_LOOK_TABLE2)));

        fromInstruction(buffer, 2)
                .expect("program index", indexOfAccount(buffer, PROGRAM1, List.of(ADDRESS_LOOK_TABLE1, ADDRESS_LOOK_TABLE2)))
                .expect("number of accounts", (byte) 1)
                .expect("fourth account index", indexOfAccount(buffer, ACCOUNT3, List.of(ADDRESS_LOOK_TABLE1, ADDRESS_LOOK_TABLE2)));
    }

    @Test
    void checkThatDataIsBytePerfectWhenInstructionIsZeroForLegacyMessage()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        final byte[] none = new byte[0];
        Solana.builder(buffer)
                .legacy()
                .payer(account(PAYER))
                .recent(blockhash(BLOCKHASH))
                .instructions(tb -> tb
                        .append(ib -> ib
                                .program(account(PROGRAM1))
                                .data(none.length, w -> w.put(none))))
                .seal()
                .unsigned()
                .build();

        fromInstruction(buffer, 0)
                .expect("program", indexOfAccount(buffer, PROGRAM1))
                .expect("number of accounts", (byte) 0)
                .expectShortVecInteger("data length", none.length);
    }

    @Test
    void checkThatDataIsBytePerfectWhenInstructionIsZeroForV0Message()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        final byte[] none = new byte[0];
        Solana.builder(buffer)
                .v0()
                .payer(account(PAYER))
                .recent(blockhash(BLOCKHASH))
                .instructions(tb -> tb
                        .append(ib -> ib
                                .program(account(PROGRAM1))
                                .data(none.length, w -> w.put(none))))
                .lookups(List.of(Solana.addressLookupTable(Solana.account(LOOKUP_TABLE_ADDRESS1), List.of(Solana.account(ACCOUNT1), Solana.account(PROGRAM1)))))
                .seal()
                .unsigned()
                .build();

        fromLookupAccountsTable(buffer)
                .expect("number of account lookups", (byte) 1)
                .expect("address lookup table", LOOKUP_TABLE_ADDRESS1)
                .expect("number of writable account keys", (byte) 0)
                .expect("number of readable account keys", (byte) 1)
                .expect("index of readable account key in lookup table", (byte) 1);
    }

    @Test
    void checkThatDataIsBytePerfectWhenInstructionIsSmallForLegacyMessage()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeSimpleUnsignedLegacyMessage(buffer);

        jumpToInstruction(buffer, 0);
        buffer.position(buffer.position() + 6);

        reader(buffer)
                .expectShortVecInteger("data length", DATA1.length)
                .expect("data", DATA1);
    }

    @Test
    void checkThatDataIsBytePerfectWhenInstructionIsSmallForV0Message()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeSimpleUnsignedV0Message(buffer);

        jumpToInstruction(buffer, 0);
        buffer.position(buffer.position() + 6);

        reader(buffer)
                .expectShortVecInteger("data length", DATA1.length)
                .expect("data", DATA1);
    }

    @Test
    void checkThatDataIsBytePerfectWhenInstructionIsMediumForLegacyMessage()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeSimpleUnsignedLegacyMessageWithBigData(buffer);

        jumpToInstruction(buffer, 0);
        buffer.position(buffer.position() + 2);

        reader(buffer)
                .expectShortVecInteger("data length", DATA3.length)
                .expect("data", DATA3);

    }

    @Test
    void checkThatDataIsBytePerfectWhenInstructionIsMediumForV0Message()
    {
        final var buffer = ByteBuffer.allocate(Solana.MAX_MESSAGE_SIZE);

        writeSimpleUnsignedV0MessageWithBigData(buffer);

        jumpToInstruction(buffer, 0);
        buffer.position(buffer.position() + 2);

        reader(buffer)
                .expectShortVecInteger("data length", DATA3.length)
                .expect("data", DATA3);

    }
}
