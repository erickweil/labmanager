/*
 * Copyright (C) 2018 Erick Leonardo Weil
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.erickweil.labamanger.common.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Random;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
//import javax.xml.bind.DatatypeConverter;


/**
 * This example program shows how AES encryption and decryption can be done in Java.
 * Please note that secret key and encrypted text is unreadable binary and hence 
 * in the following program we display it in hexadecimal format of the underlying bytes.
 * @author Jayson
 */
public class TestEncrypt {
    public static final int KEYSIZE = 256;
    public static final int ITERATIONS = 1000000;
    public static final String CIPHERMODE = "AES/CBC/PKCS5Padding";
    // https://security.stackexchange.com/questions/110084/parameters-for-pbkdf2-for-password-hashing
    public static final String KEYDERIVATION = "PBKDF2WithHmacSHA512";
    /**
     * 1. Generate a plain text for encryption
     * 2. Get a secret key (printed in hexadecimal form). In actual use this must 
     * by encrypted and kept safe. The same key is required for decryption.
     * 3. 
     * @param args
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        //String plainText = "TESTE DE MENSAGEM ENCRYPTADA";
         BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        //String passw = "1a2b3c4d";
        System.out.println("Insira a senha:");
        // https://apps.cygnius.net/passtest/
        String passw = inputReader.readLine();
        /*SecureRandom rdn = new SecureRandom();
        byte[] IV = new byte[16];
        byte[] salt = new byte[32];
        
        rdn.nextBytes(IV);
        rdn.nextBytes(salt);
        
        System.out.println("Encrypting...");
        SecretKey secKey = getSecretEncryptionKey(passw,salt,1000000,256);
        byte[] cipherText = encrypt(plainText.getBytes(Charset.forName("UTF-8")), secKey,IV);
        
        System.out.println("Decryptiing...");
        secKey = getSecretEncryptionKey(passw,salt,1000000,256);
        String decryptedText = new String(decrypt(cipherText, secKey,IV),Charset.forName("UTF-8"));
        */
        File origem_plain = new File("server_keystore.jks");
        byte[] plainText = Files.readAllBytes(origem_plain.toPath());
        
        System.out.println("Encrypting...");
        byte[] cipherText = encryptWithPassw(plainText,passw);
        //File origem_enc = new File("server_keystore.jks.aes");
        //byte[] cipherText = Files.readAllBytes(origem_enc.toPath());
        
        File destino = new File("server_keystore.jks.aes");
        Files.write(destino.toPath(), cipherText);
        long startTime = System.nanoTime();  
        System.out.println("Decrypting...");
        byte[] decryptedText = decryptWithPassw(cipherText,passw);
        long estimatedTime = System.nanoTime() - startTime;
        double milis_estimated = (double)estimatedTime / 1000000.0;
        double secs_estimated = (double)milis_estimated / 1000.0;
        System.out.println("Tempo(ms): "+(milis_estimated)+" ms\nTempo(s): "+(secs_estimated)+" secs");
        if(Arrays.equals(plainText, decryptedText))
        {
            System.out.println("Encryptado com Sucesso!");
        }
        else
        {
            System.out.println("Algoritmo mal implementado");
        }
        //System.out.println("Original Text  :" + bytesToHex(plainText));
        //System.out.println("AES Key (Hex Form):"+bytesToHex(secKey.getEncoded()));
        //System.out.println("Encrypted Text :"+bytesToHex(cipherText));
        //System.out.println("Descrypted Text:"+bytesToHex(decryptedText));
        
    }
    
    public static byte[] encryptWithPassw(byte[] plainText,String passw) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException
    {
        byte[] IV = new byte[16];
        byte[] salt = new byte[32];
        
        new SecureRandom().nextBytes(IV);
        new SecureRandom().nextBytes(salt);

        SecretKey secKey = getSecretEncryptionKey(passw,salt,ITERATIONS,KEYSIZE);
        byte[] cipherText = encrypt(plainText, secKey, IV);
        
        byte[] finalEncrypted = new byte[cipherText.length + IV.length + salt.length];
        System.arraycopy(IV, 0, finalEncrypted, 0, IV.length);
        System.arraycopy(salt, 0, finalEncrypted, IV.length, salt.length);
        System.arraycopy(cipherText, 0, finalEncrypted, IV.length + salt.length, cipherText.length);
        
        return finalEncrypted;
    }
    
    public static byte[] decryptWithPassw(byte[] finalEncrypted,String passw) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException
    {
        byte[] IV = new byte[16];
        byte[] salt = new byte[32];
        byte[] cipherText = new byte[finalEncrypted.length - (IV.length + salt.length)];

        System.arraycopy(finalEncrypted, 0, IV, 0, IV.length);
        System.arraycopy(finalEncrypted, IV.length, salt, 0, salt.length);
        System.arraycopy(finalEncrypted, IV.length + salt.length, cipherText, 0, cipherText.length);
        
        SecretKey secKey = getSecretEncryptionKey(passw,salt,ITERATIONS,KEYSIZE);
        byte[] plainText = decrypt(cipherText, secKey,IV);
        return plainText;
    }
    
    /**
     * gets the AES encryption key. In your actual programs, this should be safely
     * stored.
     * @param passphrase
     * @param salt
     * @param keysize
     * @param iteration
     * @return
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.spec.InvalidKeySpecException
     */
    public static SecretKey getSecretEncryptionKey(String passphrase,byte[] salt, int iteration, int keysize) throws NoSuchAlgorithmException, InvalidKeySpecException {
        //KeyGenerator generator = KeyGenerator.getInstance("AES");
        //generator.init(128); // The AES key size in number of bits
        //SecretKey secKey = generator.generateKey();
        //return secKey;
        //String salt = BCrypt.gensalt();
        //SecureRandom rdn = new SecureRandom();
        //rdn.setSeed(0);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEYDERIVATION);
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, iteration, keysize);
        SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        return key;
    }
    
    /**
     * Encrypts plainText in AES using the secret key
     * @param plainText
     * @param secKey
     * @param IV
     * @return
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.InvalidKeyException
     * @throws javax.crypto.IllegalBlockSizeException
     * @throws java.security.InvalidAlgorithmParameterException
     * @throws javax.crypto.BadPaddingException
     * @throws javax.crypto.NoSuchPaddingException
     */
    public static byte[] encrypt(byte[] plainText,SecretKey secKey, byte[] IV) throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException {
		// AES defaults to AES/ECB/PKCS5Padding in Java 7
        //Cipher aesCipher = Cipher.getInstance("AES");
        Cipher aesCipher = Cipher.getInstance(CIPHERMODE);
        aesCipher.init(Cipher.ENCRYPT_MODE, secKey, new IvParameterSpec(IV));
        byte[] byteCipherText = aesCipher.doFinal(plainText);
        return byteCipherText;
    }
    
    /**
     * Decrypts encrypted byte array using the key used for encryption.
     * @param byteCipherText
     * @param secKey
     * @param IV
     * @return
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.InvalidKeyException
     * @throws java.security.InvalidAlgorithmParameterException
     * @throws javax.crypto.IllegalBlockSizeException
     * @throws javax.crypto.BadPaddingException 
     * @throws javax.crypto.NoSuchPaddingException 
     */
    public static byte[] decrypt(byte[] byteCipherText, SecretKey secKey, byte[] IV) throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {
		// AES defaults to AES/ECB/PKCS5Padding in Java 7
        Cipher aesCipher = Cipher.getInstance(CIPHERMODE);
        aesCipher.init(Cipher.DECRYPT_MODE, secKey, new IvParameterSpec(IV));
        byte[] bytePlainText = aesCipher.doFinal(byteCipherText);
        return bytePlainText;
    }
    
    /**
     * Convert a binary byte array into readable hex form
     * @param hash
     * @return 
     */
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    //private static String bytesToHex(byte[] hash) {
    //    return DatatypeConverter.printHexBinary(hash);
    //}
    
    //private static byte[] hexToBytes(String hex) {
    //    return DatatypeConverter.parseHexBinary(hex);
    //}
    public static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        return result;
    }

    public static long bytesToLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }
}