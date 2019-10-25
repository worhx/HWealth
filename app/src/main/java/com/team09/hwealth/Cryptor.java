package com.team09.hwealth;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class Cryptor {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String SAMPLE_ALIAS = "TOKENALIAS";
    private byte[] iv;
    private KeyStore keyStore;

    void setIv() throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey_en());
        iv = cipher.getIV();
    }

    @NonNull
    private SecretKey getSecretKey_en() throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidAlgorithmParameterException {
        final KeyGenerator keyGenerator;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
            keyGenerator.init(new KeyGenParameterSpec.Builder(Cryptor.SAMPLE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build());
            return keyGenerator.generateKey();
        } else {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
            keyGenerator.init(new KeyGenParameterSpec.Builder(Cryptor.SAMPLE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build());

        }
        return keyGenerator.generateKey();
    }

    String encryptText(String string_to_encrypt) {
        try {
            final byte[] encryptedText = encryptData(string_to_encrypt);
            return Base64.encodeToString(encryptedText, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return "";
    }

    private byte[] encryptData(final String textToEncrypt)
            throws NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, BadPaddingException,
            IllegalBlockSizeException {
        final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey_en());
        iv = cipher.getIV();
        return (cipher.doFinal(textToEncrypt.getBytes(StandardCharsets.UTF_8)));
    }

    String getIv_string() {
        return Base64.encodeToString(iv, Base64.DEFAULT);
    }

    public void initKeyStore() throws KeyStoreException, CertificateException,
            NoSuchAlgorithmException, IOException {
        keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
    }

    public String decryptText(String encrypted, String iv) throws NoSuchProviderException {
        try {
            return decryptData(encrypted, Base64.decode(iv, Base64.DEFAULT));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String decryptData(String encrypted, final byte[] encryptionIv)
            throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, NoSuchProviderException {
        final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        final GCMParameterSpec spec = new GCMParameterSpec(128, encryptionIv);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey_en(), spec);
        byte[] encryptedData = Base64.decode(encrypted, Base64.DEFAULT);
        return new String(cipher.doFinal(encryptedData), StandardCharsets.UTF_8);
    }
}
