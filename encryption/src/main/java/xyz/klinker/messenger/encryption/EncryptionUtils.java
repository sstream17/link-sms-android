/*
 * Copyright (C) 2020 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.klinker.messenger.encryption;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Utilities for encrypting and decrypting data given the data and a secret key.
 */
public class EncryptionUtils {

    private static final String TAG = "EncryptionUtils";
    private static final String SEPARATOR = "-:-";

    private SecretKey secretKey;

    /**
     * Creates a utility that can be used to encrypt and decryptData data.
     *
     * @param secretKey the secret key to encrypt and decryptData with.
     */
    public EncryptionUtils(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * Encrypts data and formats it as a Base64 string.
     *
     * @param data the data to encrypt.
     * @return the base 64 formatted string.
     */
    public String encrypt(String data) {
        if (data == null) {
            return null;
        } else {
            try {
                return encrypt(data.getBytes(StandardCharsets.UTF_8));
            } catch (OutOfMemoryError e) {
                return data;
            }
        }
    }

    /**
     * Encrypts data and formats it as a Base64 string.
     *
     * @param data the data to encrypt.
     * @return the base 64 formatted string.
     */
    public String encrypt(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            AlgorithmParameters params = cipher.getParameters();
            String iv = Base64.encodeToString(
                    params.getParameterSpec(IvParameterSpec.class).getIV(),
                    Base64.DEFAULT);
            String ciphertext = Base64.encodeToString(cipher.doFinal(data), Base64.DEFAULT);

            return iv + SEPARATOR + ciphertext;
        } catch (InvalidKeyException | InvalidParameterSpecException |
                IllegalBlockSizeException | BadPaddingException |
                NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("could not encrypt data. Key: " +
                    Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT), e);
        }
    }

    /**
     * Decrypts data that has been encrypted and encoded as base 64.
     *
     * @param data the data to decrypt.
     * @return the plaintext string.
     */
    public String decrypt(String data) {
        if (data == null || data.equals("null")) {
            return null;
        } else {
            byte[] decrypted = decryptData(data);
            return new String(decrypted, StandardCharsets.UTF_8);
        }
    }

    /**
     * Decrypts data that has been encrypted and encoded as base 64.
     *
     * @param data the data to decryptData.
     * @return the decrypted byte array.
     */
    public byte[] decryptData(String data) {
        String dataOne = data.split(SEPARATOR)[0];
        String dataTwo = data.split(SEPARATOR)[1];

        byte[] iv;
        byte[] ciphertext;

        try {
            iv = Base64.decode(dataOne, Base64.DEFAULT);
            ciphertext = Base64.decode(dataTwo, Base64.DEFAULT);
        } catch (IllegalArgumentException e) {
            // bad base-64. Seems to come from an image
            return new byte[0];
        }

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            return cipher.doFinal(ciphertext);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException |
                IllegalBlockSizeException | BadPaddingException |
                NoSuchAlgorithmException | NoSuchPaddingException e) {
//            throw new RuntimeException("could not decryptData data. Key: " +
//                    Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT), e);
            return new byte[0];
        }
    }

}
