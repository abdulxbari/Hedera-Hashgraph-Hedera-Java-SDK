package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;

public final class ScheduleInfo {

    public final ScheduleId scheduleId;

    public final AccountId creatorAccountId;

    public final AccountId payerAccountId;

    public final KeyList signatories;

    public final Key adminKey;

    private ScheduleInfo(
        ScheduleId scheduleId,
        AccountId creatorAccountId,
        AccountId payerAccountId,
        KeyList signers,
        Key adminKey
    ) {
        this.scheduleId = scheduleId;
        this.creatorAccountId = creatorAccountId;
        this.payerAccountId = payerAccountId;
        this.signatories = signers;
        this.adminKey = adminKey;
    }

    static ScheduleInfo fromProtobuf(com.hedera.hashgraph.sdk.proto.ScheduleGetInfoResponse scheduleInfo) {
        com.hedera.hashgraph.sdk.proto.ScheduleInfo info = scheduleInfo.getScheduleInfo();


        var scheduleId = ScheduleId.fromProtobuf(info.getScheduleID());

        var creatorAccountId = AccountId.fromProtobuf(info.getCreatorAccountID());

        var payerAccountId = AccountId.fromProtobuf(info.getPayerAccountID());

        return new ScheduleInfo(
            scheduleId,
            creatorAccountId,
            payerAccountId,
            KeyList.fromProtobuf(info.getSignatories(), null),
            Key.fromProtobufKey(info.getAdminKey())
        );
    }

    public static ScheduleInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.ScheduleGetInfoResponse.parseFrom(bytes).toBuilder().build());
    }

    com.hedera.hashgraph.sdk.proto.ScheduleInfo toProtobuf() {
        var adminKey = this.adminKey.toProtobufKey() != null
            ? this.adminKey.toProtobufKey()
            : null;

        var signers = this.signatories.toProtobuf() != null
            ? this.signatories.toProtobuf()
            : null;

        var scheduleInfoBuilder = com.hedera.hashgraph.sdk.proto.ScheduleInfo.newBuilder()
            .setScheduleID(scheduleId.toProtobuf())
            .setCreatorAccountID(creatorAccountId.toProtobuf())
            .setPayerAccountID(payerAccountId.toProtobuf())
            .setSignatories(signers)
            .setAdminKey(adminKey);

        return scheduleInfoBuilder.build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("scheduleId", scheduleId)
            .add("creatorAccountId", creatorAccountId)
            .add("payerAccountId", payerAccountId)
            .add("signers", signatories)
            .add("adminKey", adminKey)
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}