package com.danesh.knine;

import com.pos.util.HexUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 3DES Tools
 */
public class DES3Utils {


    public static byte[] encrypt3DES(byte[] src, byte[] key, boolean isCbc) throws Exception {
        // Do not log key/ciphertext material.
        if (isCbc) {
            return SecurityUtil.getInstance().encryptCBC(key, new byte[8], src);
        } else {
            return encrypt3DESECB(src, key);
        }
    }

    public static byte[] decrypt3DES(byte[] src, byte[] key, boolean isCbc) throws Exception {
        if (isCbc) {
            return SecurityUtil.getInstance().decryptCBC(key, new byte[8], src);
        } else {
            return decrypt3DESECB(src, key);
        }
    }

    public static byte[] encrypt3DESECB(byte[] src, byte[] key) throws Exception {
        if (key.length == 16) {
            byte[] result = new byte[24];
            System.arraycopy(key, 0, result, 0, 16);
            System.arraycopy(key, 0, result, 16, 8);
            key = result;
        } else if (key.length < 24) {
            throw new RuntimeException("Secret key length not supported");
        }
        final DESedeKeySpec dks = new DESedeKeySpec(key);
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
        final SecretKey securekey = keyFactory.generateSecret(dks);
        final Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, securekey);
        return cipher.doFinal(src);
    }

    public static String encrypt3DESECB(String src, String key) throws Exception {
        if (key.length() == 32) {
            key = key + key.substring(0, 16);
        } else if (key.length() < 48) {
            throw new RuntimeException("Secret key length not supported");
        }
        return byte2HexStr(encrypt3DESECB(hexStrToByte(src), hexStrToByte(key)));
    }

    public static byte[] decrypt3DESECB(byte[] src, byte[] key) throws Exception {
        if (key.length == 16) {
            byte[] result = new byte[24];
            System.arraycopy(key, 0, result, 0, 16);
            System.arraycopy(key, 0, result, 16, 8);
            key = result;
        } else if (key.length < 24) {
            throw new RuntimeException("Secret key length not supported");
        }
        final DESedeKeySpec dks = new DESedeKeySpec(key);
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
        final SecretKey securekey = keyFactory.generateSecret(dks);
        final Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, securekey);
        return cipher.doFinal(src);
    }

    public static String decrypt3DESECB(String src, String key) throws Exception {
        if (key.length() == 32) {
            key = key + key.substring(0, 16);
        } else if (key.length() < 48) {
            throw new RuntimeException("Secret key length not supported");
        }
        return byte2HexStr(decrypt3DESECB(HexUtils.hexStringToByte(src), HexUtils.hexStringToByte(key)));
    }


    public static byte[] encryptDESb(byte[] value, byte[] key)
            throws Exception {
        SecretKeySpec spec = new SecretKeySpec(key, "DES/ECB/NoPadding");
        Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, spec);
        byte[] encryptedData = cipher.doFinal(value);
        return encryptedData;
    }

    public static String encryptDES(String value, String key)
            throws Exception {
        return byte2HexStr(encryptDESb(hexStrToByte(value), hexStrToByte(key)));
    }


    public static byte[] decryptDESb(byte[] value, byte[] key)
            throws Exception {
        byte[] byteMi = value;
        SecretKeySpec spec = new SecretKeySpec(key, "DES/ECB/NoPadding");
        Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, spec);
        byte[] decryptedData = cipher.doFinal(byteMi);
        return decryptedData;
    }


    public static String decryptDES(String value, String key) throws Exception {
        return byte2HexStr(decryptDESb(hexStrToByte(value), hexStrToByte(key)));
    }

    /**
     * Calc the key's kcv(Key Check Value)
     *
     * @param key The key
     * @return Key Check Value
     * @throws Exception e
     */
    public static String getCheckValue(String key) throws Exception {
        return encrypt3DESECB("0000000000000000", key).substring(0, 8);
    }

    private static String byte2HexStr(byte[] bcds) {
        char[] ascii = "0123456789ABCDEF".toCharArray();
        byte[] temp = new byte[bcds.length * 2];
        for (int i = 0 ;i < bcds.length ;++i) {
            temp[i * 2] = (byte) (bcds[i] >> 4 & 15);
            temp[i * 2 + 1] = (byte) (bcds[i] & 15);
        }
        StringBuilder res = new StringBuilder(bcds.length * 2);
        for (int i = 0; i < temp.length; ++i) {
            res.append(ascii[temp[i]]);
        }
        return res.toString();
    }

    private static byte[] hexStrToByte(String hex) {
        hex = hex.toUpperCase();
        int len = hex.length() / 2;
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; ++i) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static byte toByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
}