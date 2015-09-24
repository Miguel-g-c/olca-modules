package com.greendelta.cloud.model.data;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.openlca.core.model.ModelType;

import com.greendelta.cloud.util.NullSafe;

public class DatasetIdentifier {

	private ModelType type;
	private String refId;
	private String version;
	private long lastChange;

	public ModelType getType() {
		return type;
	}

	public void setType(ModelType type) {
		this.type = type;
	}

	public String getRefId() {
		return refId;
	}

	public void setRefId(String refId) {
		this.refId = refId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public long getLastChange() {
		return lastChange;
	}

	public void setLastChange(long lastChange) {
		this.lastChange = lastChange;
	}

	public String getHashId() {
		String fullId = getFullId();
		try {
			byte[] digest = MessageDigest.getInstance("MD5").digest(fullId.getBytes());
			char[] md5Chars = Hex.encodeHex(digest);
			return new String(md5Chars);
		} catch (NoSuchAlgorithmException e) {
			// can be ignored
			return null;
		}
	}

	private String getFullId() {
		String lastChange = Long.toString(this.lastChange);
		int length = type.name().length() + refId.length() + version.length() + lastChange.length();
		StringBuilder fullId = new StringBuilder(length);
		fullId.append(type.name());
		fullId.append(refId);
		fullId.append(version);
		fullId.append(lastChange);
		return fullId.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof DatasetIdentifier))
			return false;
		DatasetIdentifier other = (DatasetIdentifier) obj;
		if (!NullSafe.equal(getType(), other.getType()))
			return false;
		if (!NullSafe.equal(getRefId(), other.getRefId()))
			return false;
		if (!NullSafe.equal(getVersion(), other.getVersion()))
			return false;
		if (!NullSafe.equal(getLastChange(), other.getLastChange()))
			return false;
		return true;
	}

}
