package net.mcbjd.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.zip.GZIPInputStream;

public final class SkinHashes {
    private static final int MAX_IMAGE_BYTES = 1024 * 1024;

    private SkinHashes() { }

    public static String fromUpload(String encoded) {
        if (encoded == null || encoded.length() > MAX_IMAGE_BYTES * 2) {
            throw new IllegalArgumentException("Missing or oversized skin data");
        }
        try (GZIPInputStream input = new GZIPInputStream(
                new ByteArrayInputStream(Base64.getDecoder().decode(encoded)))) {
            byte[] image = input.readNBytes(MAX_IMAGE_BYTES + 1);
            if (image.length == 0 || image.length > MAX_IMAGE_BYTES || image.length % 4 != 0) {
                throw new IllegalArgumentException("Invalid skin image size");
            }
            return ofImage(image);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid compressed skin data", e);
        }
    }

    public static String ofImage(byte[] image) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("MD5").digest(Base64.getEncoder().encode(image)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean exempt(boolean persona, String skinId) {
        return persona || skinId != null && (skinId.endsWith(".NonsyncCustom") || skinId.endsWith(".NonsyncCustomSlim"));
    }
}
