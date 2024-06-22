package secret;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;


public class AES {
    private static byte[] key;

    public static void setKey() throws NoSuchAlgorithmException {
        // 获取 secret.AES 密钥生成器
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");

        // 设置密钥长度和随机源
        keyGenerator.init(128, new SecureRandom());

        // 生成密钥
        SecretKey secretKey = keyGenerator.generateKey();

        // 获取密钥内容
        key = secretKey.getEncoded();

        System.out.println("secret.AES 密钥：" + Base64.getEncoder().encodeToString(key));
    }

    public static byte[] encode(byte[] data) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return cipher.doFinal(data);
    }

    public static byte[] decode(byte[] data) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);


        return cipher.doFinal(data);
    }

    public static void main(String[] args) throws Exception {
        AES.setKey();
        String content = "Hello springdoc.cn";
        System.out.println("原文：" + content);

        byte[] ret = encode(content.getBytes());
        System.out.println("加密后的密文：" + Base64.getEncoder().encodeToString(ret));

        byte[] raw = decode(ret);
        System.out.println("解密后的原文：" + new String(raw));
    }

}