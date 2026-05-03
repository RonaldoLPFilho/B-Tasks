package com.example.tasksapi.service.vault;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class VaultCryptoServiceTest {
    private static final String DEV_KEY = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Test
    void shouldEncryptAndDecryptValue() {
        VaultCryptoService service = new VaultCryptoService(DEV_KEY);

        String encrypted = service.encrypt("secret-value");

        assertNotEquals("secret-value", encrypted);
        assertEquals("secret-value", service.decrypt(encrypted));
    }

    @Test
    void shouldUseDifferentIvForSamePlainText() {
        VaultCryptoService service = new VaultCryptoService(DEV_KEY);

        String first = service.encrypt("same");
        String second = service.encrypt("same");

        assertNotEquals(first, second);
        assertEquals("same", service.decrypt(first));
        assertEquals("same", service.decrypt(second));
    }

    @Test
    void shouldKeepNullValuesAsNull() {
        VaultCryptoService service = new VaultCryptoService(DEV_KEY);

        assertNull(service.encrypt(null));
        assertNull(service.decrypt(null));
    }
}
