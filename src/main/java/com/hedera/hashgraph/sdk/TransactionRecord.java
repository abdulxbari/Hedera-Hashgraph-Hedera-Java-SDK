package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hederahashgraph.api.proto.java.AccountAmountOrBuilder;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public final class TransactionRecord {
    private final com.hederahashgraph.api.proto.java.TransactionRecord inner;

    TransactionRecord(com.hederahashgraph.api.proto.java.TransactionRecord inner) {
        this.inner = inner;
    }

    public TransactionId getTransactionId() {
        return new TransactionId(inner.getTransactionIDOrBuilder());
    }

    public long getTransactionFee() {
        return inner.getTransactionFee();
    }

    public TransactionReceipt getReceipt() {
        return new TransactionReceipt(inner.getReceipt());
    }

    @Nullable
    public byte[] getTransactionHash() {
        ByteString hash = inner.getTransactionHash();
        // proto specifies hash is not provided if the transaction failed due to a duplicate ID
        return !hash.isEmpty() ? hash.toByteArray() : null;
    }

    @Nullable
    public Instant getConsensusTimestamp() {
        return inner.hasConsensusTimestamp() ? TimestampHelper.timestampTo(inner.getConsensusTimestamp()) : null;
    }

    @Nullable
    public String getMemo() {
        String memo = inner.getMemo();
        return !memo.isEmpty() ? memo : null;
    }

    @Nullable
    public FunctionResult getContractExecuteResult() {
        return inner.hasContractCallResult() ? new FunctionResult(inner.getContractCallResultOrBuilder()) : null;
    }

    @Nullable
    public FunctionResult getContractCreateResult() {
        return inner.hasContractCreateResult() ? new FunctionResult(inner.getContractCreateResultOrBuilder()) : null;
    }

    /**
     * @deprecated use {@link #getContractExecuteResult()} instead.
     */
    @Deprecated
    @Nullable
    public FunctionResult getCallResult() {
        return getContractExecuteResult();
    }

    /**
     * @deprecated use {@link #getContractCreateResult()} instead.
     */
    @Deprecated
    @Nullable
    public FunctionResult getCreateResult() {
        return getContractCreateResult();
    }

    public List<Transfer> getTransfers() {
        return inner.hasTransferList()
            ? inner.getTransferList()
                .getAccountAmountsList()
                .stream()
                .map(Transfer::new)
                .collect(Collectors.toList())
            : Collections.emptyList();
    }

    public final static class Transfer {
        public final AccountId account;
        public final long amount;

        private Transfer(AccountAmountOrBuilder accountAmount) {
            account = new AccountId(accountAmount.getAccountIDOrBuilder());
            amount = accountAmount.getAmount();
        }
    }
}
