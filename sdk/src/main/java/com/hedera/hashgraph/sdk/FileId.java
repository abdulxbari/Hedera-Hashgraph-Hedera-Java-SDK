package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.FileID;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * The ID for a file on Hedera.
 */
public final class FileId {
    /**
     * The shard number
     */
    @Nonnegative
    public final long shard;

    /**
     * The realm number
     */
    @Nonnegative
    public final long realm;

    /**
     * The id number
     */
    @Nonnegative
    public final long num;

    @Nullable
    private final String checksum;

    /**
     * The public node address book for the current network.
     */
    public static final FileId ADDRESS_BOOK = new FileId(0, 0, 102);

    /**
     * The current fee schedule for the network.
     */
    public static final FileId FEE_SCHEDULE = new FileId(0, 0, 111);

    /**
     * The current exchange rate of HBAR to USD.
     */
    public static final FileId EXCHANGE_RATES = new FileId(0, 0, 112);

    public FileId(@Nonnegative long num) {
        this(0, 0, num);
    }

    @SuppressWarnings("InconsistentOverloads")
    public FileId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        this(shard, realm, num, null);
    }

    @SuppressWarnings("InconsistentOverloads")
    FileId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num, @Nullable String checksum) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.checksum = checksum;
    }

    public static FileId fromString(String id) {
        return EntityIdHelper.fromString(id, FileId::new);
    }

    public static FileId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(FileID.parseFrom(bytes).toBuilder().build());
    }

    static FileId fromProtobuf(FileID fileId) {
        Objects.requireNonNull(fileId);
        return new FileId(fileId.getShardNum(), fileId.getRealmNum(), fileId.getFileNum());
    }

    FileID toProtobuf() {
        return FileID.newBuilder()
            .setShardNum(shard)
            .setRealmNum(realm)
            .setFileNum(num)
            .build();
    }

    /**
     * @param client
     * @throws BadEntityIdException
     * @deprecated Use {@link #validateChecksum(Client)} instead.
     */
    @Deprecated
    public void validate(Client client) throws BadEntityIdException {
        validateChecksum(client);
    }

    public void validateChecksum(Client client) throws BadEntityIdException {
        EntityIdHelper.validate(shard, realm, num, client, checksum);
    }

    @Nullable
    public String getChecksum() {
        return checksum;
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public String toString() {
        return EntityIdHelper.toString(shard, realm, num);
    }

    public String toStringWithChecksum(Client client) {
        return EntityIdHelper.toStringWithChecksum(shard, realm, num, client, checksum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard, realm, num);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileId)) return false;

        FileId otherId = (FileId) o;
        return shard == otherId.shard && realm == otherId.realm && num == otherId.num;
    }
}
