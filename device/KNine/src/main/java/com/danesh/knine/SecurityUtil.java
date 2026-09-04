
package com.danesh.knine;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class SecurityUtil {


	private static final String ALGORITHM = "DES";
	private static SecurityUtil util = new SecurityUtil();
	private static String ZERO_16="000000000000";

	public static SecurityUtil getInstance() {
		return util;
	}

	public String encryptDES(String key, String source) {
		return bcd2str(encryptDes(hexStringToByte(convertToMultiple8(key).toUpperCase()), hexStringToByte(convertToMultiple8(source).toUpperCase())));

	}


	public String convertToMultiple8(String string){
		int divisor = string.length()/16;
		int remainder = string.length()%16;
		if(remainder>0){
			string += ZERO_16;
			string = string.substring(0,(divisor+1)*16);
		}
		return string;
	}



	public static byte[] encryptDes(byte[] keybyte, byte[] src) {
		try {
			// 生成密钥
			SecretKey deskey = new SecretKeySpec(keybyte, ALGORITHM);
			// 加密
			Cipher cipher = Cipher.getInstance(ALGORITHM + "/ECB/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, deskey);
			return cipher.doFinal(src);
		}
		catch (Exception e3) {
			e3.printStackTrace();
		}
		return null;
	}


	public static String decryptDES(String key, String source) {
		return bcd2str(decryptDes(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())));
	}


	public static byte[] decryptDes(byte[] keybyte, byte[] src) {
		try {
			// 生成密钥
			SecretKey deskey = new SecretKeySpec(keybyte, ALGORITHM);
			// 解密
			Cipher cipher = Cipher.getInstance(ALGORITHM + "/ECB/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, deskey);
			return cipher.doFinal(src);
		}
		catch (Exception e3) {
			e3.printStackTrace();
		}
		return null;
	}


	public String encrype3DES(String key, String source) {
		return bcd2str(encrype3Des(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())));
	}


	public static byte[] encrype3Des(byte[] key, byte[] source) {
		//初始化加密数据块
		byte[] cursorSourceBytes = new byte[8];
		System.arraycopy(source, 0, cursorSourceBytes, 0, 8);
		//初始化左半部分密钥
		byte[] keyLeft = new byte[8];
		System.arraycopy(key, 0, keyLeft, 0, 8);
		//初始化右半部分密钥
		byte[] keyRight = new byte[8];
		System.arraycopy(key, 8, keyRight, 0, 8);
		//第一步 : 用左半部分密钥对数据进行DES加密
		byte[] encryptResultBytes = encryptDes(keyLeft, cursorSourceBytes);
		//第二步 : 用右半部分密钥对第一步加密结果进行DES解密
		byte[] decryptResultbytes = decryptDes(keyRight, encryptResultBytes);
		//第三步 : 用左半部分密钥对第三步解密结果进行DES加密
		byte[] cursorResultBytes = encryptDes(keyLeft, decryptResultbytes);
		if(source.length>8) {//判断是否有多个8字节数据块
			//初始化下一个数据块
			byte[] tempSourceBytes = new byte[source.length-8];
			System.arraycopy(source, 8, tempSourceBytes, 0, source.length-8);
			//下一个数据库加密结果
			byte[] subRelultBytes = encrype3Des(key, tempSourceBytes);
			byte[] resultBytes = new byte[cursorResultBytes.length + subRelultBytes.length];
			//合并加密结果
			System.arraycopy(cursorResultBytes, 0, resultBytes, 0, cursorResultBytes.length);
			System.arraycopy(subRelultBytes, 0, resultBytes, cursorResultBytes.length, subRelultBytes.length);
			return resultBytes;
		}
		return cursorResultBytes;
	}


	public String decrypt3DES(String key, String source) {
		return bcd2str(decrypt3Des(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())));
	}


	public static byte[] decrypt3Des(byte[] key, byte[] source) {
		//将16字节密钥分解为各8字节的两个子密钥
		byte[] keyleft = new byte[8];
		System.arraycopy(key, 0, keyleft, 0, 8);
		byte[] keyright = new byte[8];
		System.arraycopy(key, 8, keyright, 0, 8);
		//初始化当前密文
		byte[] cursorSrouceBytes = new byte[8];
		System.arraycopy(source, 0, cursorSrouceBytes, 0, 8);
		//加密步骤一：第一个子密钥对当前密文解密
		byte[] leftencrypt1 = decryptDes(keyleft, cursorSrouceBytes);
		//加密步骤二：第二个子密钥对步骤一加密结果进行加密
		byte[] rightdecrypt2 = encryptDes(keyright, leftencrypt1);
		//加密步骤三A：第一个子密钥对步骤三结果进行解密
		byte[] leftencrypt3 = decryptDes(keyleft, rightdecrypt2);
		if(source.length>8) {//判断是否含下一个8字节密文数据块
			//初始化下一个密文数据块
			byte[] subSourceBytes = new byte[source.length-8];
			System.arraycopy(source, 8, subSourceBytes, 0, source.length-8);
			//下一个密文数据库解密结果
			byte[] subResultBytes = decrypt3Des(key, subSourceBytes);
			//生成解密结果
			byte[] resultBytes = new byte[subResultBytes.length + leftencrypt3.length];
			System.arraycopy(leftencrypt3, 0, resultBytes, 0, leftencrypt3.length);
			System.arraycopy(subResultBytes, 0, resultBytes, leftencrypt3.length, subResultBytes.length);
			return resultBytes;
		}
		return leftencrypt3;
	}


	public String ansiMacDES(String key, String vector, String source, boolean isHex) throws Exception {
		return mac(isHex?hexStringToByte(source.toUpperCase()):source.getBytes(), hexStringToByte(key.toUpperCase()), hexStringToByte(vector.toUpperCase()));
	}
	public static byte[] pboc3desmac(byte[] source,byte[] key,byte[] vector) throws Exception{
		byte[] orginal = vector;//原始值
		byte[] leftKey = new byte[8];//左半部分密钥
		byte[] rightKey = new byte[8];//右部分密钥
		System.arraycopy(key, 8, rightKey, 0, 8);
		System.arraycopy(key, 0, leftKey, 0, 8);
		for(int i=0;i<source.length;i+=8){
			byte[]temp = new byte[8];
			System.arraycopy(source, i, temp, 0, temp.length);
			orginal = xor(orginal,temp);//异或
			orginal = encryptDes(leftKey, orginal);//用左半部分DES加密
		}
		byte[] debyrightKeySrc = decryptDes(rightKey, orginal);//用右半部分密钥解密
		byte[] encryptLeftKeySrc = encryptDes(leftKey, debyrightKeySrc);//再用左半部分密钥加密
		byte[] result = new byte[8];
		System.arraycopy(encryptLeftKeySrc, 0, result, 0, 8);//得到8字节的结果值
		return result;
	}

	public String mac(byte[] source, byte[] key, byte[] vector) throws Exception {
		byte[] cursorSourceBytes = new byte[8];
		System.arraycopy(source, 0, cursorSourceBytes, 0, (source.length>8 ? 8:source.length));
		byte[] sourceLeftXor = xor(cursorSourceBytes, vector);
		byte[] sourceLeftEncrypt = encryptDes(key, sourceLeftXor);
		if(source.length>8) {
			byte[] tempBytes = new byte[source.length - 8];
			System.arraycopy(source, 8, tempBytes, 0, source.length-8);
			return mac(tempBytes, key, sourceLeftEncrypt);
		}
		return bcd2str(sourceLeftEncrypt);
	}


	public String pbocMacDES(String key, String vector, String source, boolean isHex) throws Exception {
		byte[] sourceFilledBytes = fillBytes(isHex ? hexStringToByte(source.toUpperCase()) : source.getBytes());
		return mac(sourceFilledBytes, hexStringToByte(key.toUpperCase()), hexStringToByte(vector.toUpperCase()));
	}


	public static  byte[] fillBytes(byte[] sourceBytes) {
		int mod = sourceBytes.length%8;
		byte[] sourceFilledBytes = new byte[sourceBytes.length + (8-mod)];
		System.arraycopy(sourceBytes, 0, sourceFilledBytes, 0, sourceBytes.length);
		if(mod==0) {
			byte[] fillBytes = hexStringToByte("8000000000000000");
			System.arraycopy(fillBytes, 0, sourceFilledBytes, sourceBytes.length, fillBytes.length);
		}else {
			for(int i=0; i<(8-mod);i++) {
				sourceFilledBytes[sourceBytes.length + i] = hexStringToByte(i==0?"80":"00")[0];
			}
		}
		return sourceFilledBytes;
	}


	public String ansiMac3DES(String source, String key, String vector, boolean isHex) throws Exception {
		return mac3Des(isHex?hexStringToByte(source.toUpperCase()):source.getBytes(), hexStringToByte(key.toUpperCase()), hexStringToByte(vector.toUpperCase()));
	}


	public String pbocMac3DES(String key, String vector, String source, boolean isHex) throws Exception {
		byte[] sourceFilledBytes = fillBytes(isHex ? hexStringToByte(source.toUpperCase()) : source.getBytes());
		return mac3Des(sourceFilledBytes, hexStringToByte(key.toUpperCase()), hexStringToByte(vector.toUpperCase()));
	}


	public String mac3Des(byte[] source, byte[] key, byte[] vector) throws Exception {
		byte[] cursorSourceBytes = new byte[8];
		System.arraycopy(source, 0, cursorSourceBytes, 0, (source.length>=8 ? 8:source.length));
		byte[] cursorSourceXor = xor(cursorSourceBytes, vector);
		if(source.length>8) {
			byte[] cursorKey = new byte[8];
			System.arraycopy(key, 0, cursorKey, 0, 8);
			byte[] sourceLeftEncrypt = encryptDes(cursorKey, cursorSourceXor);
			byte[] tempBytes = new byte[source.length - 8];
			System.arraycopy(source, 8, tempBytes, 0, source.length-8);
			return mac3Des(tempBytes, key, sourceLeftEncrypt);
		}
		return bcd2str(encrype3Des(key, cursorSourceXor));
	}

	/**
	 * Diversify密钥分散算法
	 * @param source 源数据
	 * @param key 主控密钥MK
	 * @return string 分散密钥DK(16进制数字符串)
	 */
	public String diversify(String key, String source) {
		return bcd2str(diversify(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())));
	}

	/**
	 * Diversify密钥分散算法
	 * @param source 源数据
	 * @param key 主控密钥MK
	 * @return byte[] 分散密钥DK(16进制数字符串)
	 */
	public byte[] diversify(byte[]key, byte[] source) {
		//当前分散数据
		byte[] cursorSourceBytes = new byte[8];
		System.arraycopy(source, 0, cursorSourceBytes, 0, source.length>8?8:source.length);
		//推导左半部分key
		byte[] leftDivBytes = encrype3Des(key, cursorSourceBytes);
		//当前分散数据取反
		for (int i = 0; i < cursorSourceBytes.length; i++) {
			cursorSourceBytes[i] = (byte) ~ cursorSourceBytes[i];
		}
		//推导右半部分key
		byte[] rightDivBytes = encrype3Des(key, cursorSourceBytes);
		//合并
		byte[] resultBytes = new byte[leftDivBytes.length + rightDivBytes.length ];
		System.arraycopy(leftDivBytes, 0, resultBytes, 0, leftDivBytes.length);
		System.arraycopy(rightDivBytes, 0, resultBytes, leftDivBytes.length, rightDivBytes.length);
		if(source.length>8) {//判断是否二次分散运算
			byte[] tempBytes = new byte[source.length-8];
			System.arraycopy(source, 8, tempBytes, 0, source.length-8);
			return diversify(resultBytes, tempBytes);
		}
		return resultBytes;
	}

	/**
	 * Double-One-Way分散算法
	 * @param source 源数据
	 * @param key 主控密钥MK
	 * @return string 分散密钥DK(16进制数字符串)
	 * @throws Exception
	 */
	public String diversifyByDoubleOneWay(String source, String key) throws Exception {
		return diversifyDouble(source, key);
	}

	/**
	 * Double-One-Way分散运算
	 * @param source 源数据
	 * @param key 主控密钥MK
	 * @return string 分散密钥DK(16进制数字符串)
	 * @throws Exception
	 */
	private String diversifyDouble(String source, String key) throws Exception {
		byte[] keyleft = hexStringToByte(key.substring(0, key.length()/2).toUpperCase());
		byte[] keyright = hexStringToByte(key.substring(key.length()/2).toUpperCase());
		byte[] sourceBytes = hexStringToByte(source.toUpperCase());
		byte[] sourceUnDes = decryptDes(keyleft, sourceBytes);
		byte[] sourceunDesDes = encryptDes(keyright, sourceUnDes);
		byte[] sourceunDesDesUnDes = decryptDes(keyleft, sourceunDesDes);
		byte[] keyleftXor = xor(sourceBytes, sourceunDesDesUnDes);
		return bcd2str(keyleftXor);
	}


	public String xor(String xor1, String xor2) throws Exception  {
		return bcd2str(xor(hexStringToByte(xor1), hexStringToByte(xor2)));
	}


	public static  byte[] xor(byte[] hexSource1, byte[] hexSource2) throws Exception {
		int length =  hexSource1.length;
		byte[] xor = new byte[length];
		for (int i = 0; i < length; i++) {
			xor[i] = (byte) (hexSource1[i]^hexSource2[i]);
		}
		return xor;
	}


	public static String bcd2str(byte[] bcds) {
		char[] ascii = "0123456789abcdef".toCharArray();
		byte[] temp = new byte[bcds.length * 2];
		for (int i = 0; i < bcds.length; i++) {
			temp[i * 2] = (byte) ((bcds[i] >> 4) & 0x0f);
			temp[i * 2 + 1] = (byte) (bcds[i] & 0x0f);
		}
		StringBuffer res = new StringBuffer();

		for (int i = 0; i < temp.length; i++) {
			res.append(ascii[temp[i]]);
		}
		return res.toString().toUpperCase();
	}


	public static byte[] hexStringToByte(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}


	public String encryptECB(String key, String source) {
		return bcd2str(encryptECB(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())));
	}


	public byte[] encryptECB(byte[] key, byte[] source) {
		byte[] cursorSourntBytes = new byte[8];
		System.arraycopy(source, 0, cursorSourntBytes, 0, source.length>8?8:source.length);
		byte[] currorEncryptResult = key.length>8?encrype3Des(key, cursorSourntBytes):encryptDes(key, cursorSourntBytes);
		if(source.length>8) {
			byte[] nextSource = new byte[source.length-8];
			System.arraycopy(source, 8, nextSource, 0, source.length-8);
			byte[] subEncryptResult = encryptECB(key, nextSource);
			byte[] encryptResult =  new byte[currorEncryptResult.length + subEncryptResult.length];
			System.arraycopy(currorEncryptResult, 0, encryptResult, 0, currorEncryptResult.length);
			System.arraycopy(subEncryptResult, 0, encryptResult, currorEncryptResult.length, subEncryptResult.length);
			return encryptResult;
		}
		return currorEncryptResult;
	}


	public String decryptECB(String key, String source) {
		return bcd2str(decryptECB(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())));
	}


	public byte[] decryptECB(byte[] key, byte[] source) {
		byte[] cursorSourntBytes = new byte[8];
		System.arraycopy(source, 0, cursorSourntBytes, 0, source.length>8?8:source.length);
		byte[] currorDecryptResult = key.length>8?decrypt3Des(key, cursorSourntBytes):decryptDes(key, cursorSourntBytes);
		if(source.length>8) {
			byte[] nextSource = new byte[source.length-8];
			System.arraycopy(source, 8, nextSource, 0, source.length-8);
			byte[] subEncryptResult = decryptECB(key, nextSource);
			byte[] encryptResult =  new byte[currorDecryptResult.length + subEncryptResult.length];
			System.arraycopy(currorDecryptResult, 0, encryptResult, 0, currorDecryptResult.length);
			System.arraycopy(subEncryptResult, 0, encryptResult, currorDecryptResult.length, subEncryptResult.length);
			return encryptResult;
		}
		return currorDecryptResult;
	}


	public String encryptCBC(String key, String vector, String source) throws Exception {
		return bcd2str(encryptCBC(hexStringToByte(key.toUpperCase()), hexStringToByte(vector!=null?vector:"0000000000000000"), hexStringToByte(source.toUpperCase())));
	}


	public byte[] encryptCBC(byte[] key, byte[] vector, byte[] source) throws Exception {
		byte[] cursorSourntBytes = new byte[8];
		System.arraycopy(source, 0, cursorSourntBytes, 0, source.length>8?8:source.length);
		byte[] xorResultBytes = xor(cursorSourntBytes, vector);
		byte[] currorEncryptResult = key.length>8?encrype3Des(key, xorResultBytes):encryptDes(key, xorResultBytes);
		if(source.length>8) {
			byte[] nextSource = new byte[source.length-8];
			System.arraycopy(source, 8, nextSource, 0, source.length-8);
			byte[] subEncryptResult = encryptCBC(key, currorEncryptResult, nextSource);
			byte[] encryptResult =  new byte[currorEncryptResult.length + subEncryptResult.length];
			System.arraycopy(currorEncryptResult, 0, encryptResult, 0, currorEncryptResult.length);
			System.arraycopy(subEncryptResult, 0, encryptResult, currorEncryptResult.length, subEncryptResult.length);
			return encryptResult;
		}
		return currorEncryptResult;
	}


	public String decryptCBC(String key, String vector, String source) throws Exception {
		return bcd2str(decryptCBC(hexStringToByte(key.toUpperCase()), hexStringToByte(vector!=null?vector:"0000000000000000"), hexStringToByte(source.toUpperCase())));
	}


	public byte[] decryptCBC(byte[] key, byte[] vector, byte[] source) throws Exception {
		byte[] decryptBytes = new byte[8];
		System.arraycopy(source, 0, decryptBytes, 0, source.length>8?8:source.length);

		byte[] decryptResult = key.length>8?decrypt3Des(key, decryptBytes):decryptDes(key, decryptBytes);
		byte[] result = xor(decryptResult, vector);
		if(source.length>8) {
			byte[] nextSource = new byte[source.length-8];
			System.arraycopy(source, 8, nextSource, 0, source.length-8);
			byte[] subDecryptResult = decryptCBC(key, decryptBytes, nextSource);
			byte[] encryptResult =  new byte[result.length + subDecryptResult.length];
			System.arraycopy(result, 0, encryptResult, 0, result.length);
			System.arraycopy(subDecryptResult, 0, encryptResult, result.length, subDecryptResult.length);
			return encryptResult;
		}
		return result;
	}


	private static byte toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}


	public static boolean isHexademical(String name, String value, int length) throws Exception {
		if(null==value || (value.length()!=length && length>0)) {
			throw new Exception(name + "长度应为" + length);
		}
		String texts = "0123456789abcdefABCDEF";
		int len = value.length();
		for(int i=0; i<len; i++) {
			if(texts.indexOf(value.charAt(i)) == -1) {
				throw new Exception(name + "包含的字符应为16进制字符");
			}
		}
		return true;
	}


	public static boolean isAlphanumeric(String name, String value, int length) throws Exception {
		if(null==value || (value.length()!=length && length>0)) {
			throw new Exception(name + "长度应为" + length);
		}
		String texts = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		int len = value.length();
		for(int i=0; i<len; i++) {
			if(texts.indexOf(value.charAt(i)) == -1) {
				throw new Exception(name + "包含的字符应为数字或字母");
			}
		}
		return true;
	}

	public String xorDes(String source, String key) throws Exception {
		return xorDes(hexStringToByte(source.toUpperCase()), hexStringToByte(key.toUpperCase()));
	}
	public String xorDes(byte[] source, byte[] key) throws Exception {
		int position = 0;
		byte[] oper1 = new byte[8];
		System.arraycopy(source, position, oper1, 0, 8);
		position += 8;
		for (int i = 1; i < source.length / 8; i++) {
			byte[] oper2 = new byte[8];
			System.arraycopy(source, position, oper2, 0, 8);
			oper1 = xor(oper1, oper2);
			position += 8;
		}
		byte[] sourceLeftEncrypt = encryptDes(key, oper1);
		return bcd2str(sourceLeftEncrypt);
	}
}
