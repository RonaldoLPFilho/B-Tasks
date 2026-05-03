package com.example.tasksapi.service.vault;

import com.example.tasksapi.exception.InvalidDataException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class VaultCryptoService {
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKeySpec keySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    public VaultCryptoService(@Value("${tasks.vault.encryption-key:MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=}") String encryptionKey) {
        byte[] key;
        try {
            key = Base64.getDecoder().decode(encryptionKey);
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("Vault encryption key must be base64 encoded");
        }
        if (key.length != 16 && key.length != 24 && key.length != 32) {
            throw new InvalidDataException("Vault encryption key must decode to 16, 24, or 32 bytes");
        }
        this.keySpec = new SecretKeySpec(key, "AES");
    }

    public String encrypt(String value) {
        if (value == null) {
            return null;
        }

        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            byte[] payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(payload);
        } catch (GeneralSecurityException e) {
            throw new InvalidDataException("Unable to encrypt vault data");
        }
    }

    public String decrypt(String payload) {
        if (payload == null) {
            return null;
        }

        try {
            byte[] bytes = Base64.getDecoder().decode(payload);
            if (bytes.length <= IV_LENGTH_BYTES) {
                throw new InvalidDataException("Invalid encrypted vault payload");
            }

            byte[] iv = Arrays.copyOfRange(bytes, 0, IV_LENGTH_BYTES);
            byte[] encrypted = Arrays.copyOfRange(bytes, IV_LENGTH_BYTES, bytes.length);

            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | GeneralSecurityException e) {
            throw new InvalidDataException("Unable to decrypt vault data");
        }
    }
}
