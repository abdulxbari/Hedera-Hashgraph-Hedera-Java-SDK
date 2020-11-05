package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.Function;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>A transaction specifically to append data to a file on the network.
 *
 * <p>If a file has multiple keys, all keys must sign to modify its contents.
 * (See {@link FileCreateTransaction#setKeys(Key...)} for more information.)
 */
public final class FileAppendTransaction extends Transaction<FileAppendTransaction> implements WithExecuteAll {
    private static final int CHUNK_SIZE = 4096;

    private final FileAppendTransactionBody.Builder builder;

    /**
     * Maximum number of chunks this message will get broken up into when
     * its frozen.
     */
    private int maxChunks = 10;

    private List<Transaction<SingleFileAppendTransaction>> chunkTransactions = Collections.emptyList();

    public FileAppendTransaction() {
        builder = FileAppendTransactionBody.newBuilder();
    }

    FileAppendTransaction(TransactionBody body) {
        super(body);

        builder = body.getFileAppend().toBuilder();
    }

    @Nullable
    public FileId getFileId() {
        return builder.hasFileID() ? FileId.fromProtobuf(builder.getFileID()) : null;
    }

    /**
     * <p>Set the ID of the file to append to. Required.
     *
     * @param fileId the ID of the file to append to.
     * @return {@code this}
     */
    public FileAppendTransaction setFileId(FileId fileId) {
        requireNotFrozen();
        builder.setFileID(fileId.toProtobuf());
        return this;
    }

    @Nullable
    public ByteString getContents() {
        return builder.getContents();
    }

    /**
     * <p>Set the contents to append to the file as identified by {@link #setFileId(FileId)}.
     *
     * <p>Note that total size for a given transaction is limited to 6KiB (as of March 2020) by the
     * network; if you exceed this you may receive a {@link com.hedera.hashgraph.sdk.HederaPreCheckStatusException}
     * with {@link com.hedera.hashgraph.sdk.Status#TRANSACTION_OVERSIZE}.
     *
     * <p>If you want to append more than ~6KiB of data, you will need to break it into multiple chunks
     * and use a separate {@link FileAppendTransaction} for each.
     *
     * @param contents the contents to append to the file.
     * @return {@code this}
     * @see #setContents(String) for an overload which takes {@link String}.
     */
    public FileAppendTransaction setContents(byte[] contents) {
        requireNotFrozen();
        builder.setContents(ByteString.copyFrom(contents));
        return this;
    }

    /**
     * <p>Encode the given {@link String} as UTF-8 and append it to file as identified by
     * {@link #setFileId(FileId)}.
     *
     * <p>If the whole file is UTF-8 encoded, the string can later be recovered from
     * {@link FileContentsQuery#execute(Client)} via
     * {@link String#String(byte[], java.nio.charset.Charset)} using
     * {@link java.nio.charset.StandardCharsets#UTF_8}.
     *
     * <p>Note that total size for a given transaction is limited to 6KiB (as of March 2020) by the
     * network; if you exceed this you may receive a {@link com.hedera.hashgraph.sdk.HederaPreCheckStatusException}
     * with {@link com.hedera.hashgraph.sdk.Status#TRANSACTION_OVERSIZE}.
     *
     * <p>If you want to append more than ~6KiB of data, you will need to break it into multiple chunks
     * and use a separate {@link FileAppendTransaction} for each.
     *
     * @param text The String to be set as the contents of the file
     * @return {@code this}
     * @see #setContents(byte[]) for appending arbitrary data.
     */
    public FileAppendTransaction setContents(String text) {
        requireNotFrozen();
        builder.setContents(ByteString.copyFromUtf8(text));
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return FileServiceGrpc.getAppendContentMethod();
    }

    @Override
    public FileAppendTransaction signWith(PublicKey publicKey, Function<byte[], byte[]> transactionSigner) {
        if (!isFrozen()) {
            freeze();
        }

        for (var transaction : chunkTransactions) {
            transaction.signWith(publicKey, transactionSigner);
        }
        return this;
    }

    @Override
    protected boolean isFrozen() {
        return !chunkTransactions.isEmpty();
    }

    @Override
    public FileAppendTransaction freezeWith(@Nullable Client client) {
        var wasFrozen = isFrozen();

        if (!bodyBuilder.hasNodeAccountID()) {
            if (client != null) {
                // if there is no defined node ID, we need to pick a set of nodes
                // up front so each chunk's nodes are consistent
                nodeIds = client.getNodeAccountIdsForTransaction();

                for (AccountId nodeId : nodeIds) {
                    nodeIds.add(nodeId);
                }
            } else {
                throw new IllegalStateException("`client` must be provided or `nodeId` must be set");
            }
        }

        super.freezeWith(client);

        if (!wasFrozen) {
            for (var chunkTx : chunkTransactions) {
                chunkTx.freezeWith(client);
            }
        }

        return this;
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        var initialTransactionId = bodyBuilder.getTransactionID();
        var content = builder.getContents();
        var fileId = builder.getFileID();
        var totalContentSize = content.size();
        var requiredChunks = (totalContentSize + (CHUNK_SIZE - 1)) / CHUNK_SIZE;

        if (requiredChunks > maxChunks) {
            throw new IllegalArgumentException(
                "content of " + totalContentSize + " bytes requires " + requiredChunks
                    + " chunks but the maximum allowed chunks is " + maxChunks + ", try using setMaxChunks");
        }

        chunkTransactions = new ArrayList<>(requiredChunks);
        @Var var nextTransactionId = initialTransactionId.toBuilder();
        for (int i = 0; i < requiredChunks; i++) {
            @Var var startIndex = i * CHUNK_SIZE;
            @Var var endIndex = startIndex + CHUNK_SIZE;

            if (endIndex > totalContentSize) {
                endIndex = totalContentSize;
            }

            var chunkMessage = content.substring(startIndex, endIndex);

            bodyBuilder.setTransactionID(nextTransactionId.build());

            chunkTransactions.add(new SingleFileAppendTransaction(
                nodeIds,
                bodyBuilder.clone(),
                fileId,
                chunkMessage));

            // add 1 ns to the validStart to make cascading transaction IDs
            var nextValidStart = nextTransactionId.getTransactionValidStart().toBuilder();
            nextValidStart.setNanos(nextValidStart.getNanos() + 1);

            nextTransactionId.setTransactionValidStart(nextValidStart);
        }
        return false;
    }

    @Override
    public CompletableFuture<com.hedera.hashgraph.sdk.TransactionResponse> executeAsync(Client client) {
        return executeAllAsync(client).thenApply(responses -> responses.get(0));
    }

    @Override
    public CompletableFuture<List<com.hedera.hashgraph.sdk.TransactionResponse>> executeAllAsync(Client client) {
        if (!isFrozen()) {
            freezeWith(client);
        }

        var operatorId = client.getOperatorAccountId();

        if (operatorId != null && operatorId.equals(getTransactionId().accountId)) {
            // on execute, sign each transaction with the operator, if present
            // and we are signing a transaction that used the default transaction ID
            signWithOperator(client);
        }

        CompletableFuture<?>[] futures = new CompletableFuture[chunkTransactions.size()];

        for (var i = 0; i < chunkTransactions.size(); i++) {
            futures[i] = chunkTransactions.get(i).executeAsync(client)
                .thenCompose(response -> response.getReceiptAsync(client)
                    .thenCompose(receipt -> CompletableFuture.completedFuture(response)));
        }

        return CompletableFuture.allOf(futures).thenApply(v -> {
            List<com.hedera.hashgraph.sdk.TransactionResponse> responses = new ArrayList<>(futures.length);

            for (var fut : futures) {
                responses.add((com.hedera.hashgraph.sdk.TransactionResponse) fut.join());
            }

            return responses;
        });
    }
}
