package com.team09.hwealth;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class Decryptor {

    public static String decryptMsg(String cipherText, String key){
        Cipher cipher = null;
        String decryptedString;
        SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES_256");
        String[] splitCipherText = cipherText.split(":");
        IvParameterSpec iv = new IvParameterSpec(splitCipherText[0].getBytes(StandardCharsets.UTF_8));
        byte[] encryptedByte = Base64.decode(splitCipherText[1].getBytes(), Base64.DEFAULT);
        try{
            cipher = Cipher.getInstance("AES/OFB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            decryptedString = new String(cipher.doFinal(encryptedByte), StandardCharsets.UTF_8);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            return e.toString();
        }

        return decryptedString;
    }


}
