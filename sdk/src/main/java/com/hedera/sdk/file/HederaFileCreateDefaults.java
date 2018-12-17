package com.hedera.sdk.file;

import org.spongycastle.util.encoders.Hex;

import com.hedera.sdk.common.HederaKey;
import com.hedera.sdk.common.HederaKey.KeyType;
/**
 * This class holds default values for an account creation
 * You can optionally create an instance of this class to set different default values
 * and re-use it on every account creation by passing it into the HederaCryptoCurrency.CreateAccount 
 * method for the account to override defaults
 */
public class HederaFileCreateDefaults {
	private HederaFile fileDefaultsFromClass = new HederaFile();

	private HederaKey newRealmAdminPublicKey = fileDefaultsFromClass.newRealmAdminKey;
	/**
	 * The expiration time for a file in seconds
	 */
	public long expirationTimeSeconds = fileDefaultsFromClass.expirationTime.getEpochSecond();
	/**
	 * The nanoseconds element of the file's expiration time 
	 */
	public int expirationTimeNanos = 0;

	// Methods
	/**
	 * if realmID is -1, then this the admin key for the new realm that will be created
	 * it is ignored otherwise
	 * @param keyType the type of key
	 * @param newRealmAdminKey the new realm admin key
	 */
	public void setNewRealmAdminPublicKey(KeyType keyType,byte[] newRealmAdminKey) {
<<<<<<< HEAD:sdk/src/main/java/com/hedera/sdk/file/HederaFileCreateDefaults.java
		this.newRealmAdminPublicKey = new HederaKey(keyType, newRealmAdminKey);
=======
		this.newRealmAdminPublicKey = new HederaKeyPair(keyType, newRealmAdminKey, null);
>>>>>>> f76e9c4... Unit tests pass:src/main/java/com/hedera/sdk/file/HederaFileCreateDefaults.java
	}
	/**
	 * if realmID is -1, then this the admin key for the new realm that will be created
	 * it is ignored otherwise
	 * @param keyType the type of key
	 * @param newRealmAdminKeyHex the new realm admin key in string hex format
	 */
	public void setNewRealmAdminPublicKey(KeyType keyType,String newRealmAdminKeyHex) {
		this.newRealmAdminPublicKey = new HederaKey(keyType, Hex.decode(newRealmAdminKeyHex));
	}
	/**
	 * Gets the new realm admin key
	 * @return {@link HederaKey}
	 */
	public HederaKey getNewRealmAdminPublicKey() {
		return this.newRealmAdminPublicKey;
	}
}
