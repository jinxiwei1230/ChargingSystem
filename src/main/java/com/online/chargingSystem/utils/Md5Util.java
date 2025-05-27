package com.online.chargingSystem.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5加密工具类
 * 提供字符串的MD5加密及校验功能
 */
public class Md5Util {

    /**
     * 十六进制字符数组，用于将字节转换为十六进制字符串
     * Apache校验文件完整性使用的也是这个组合
     */
    private static final char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    // MD5消息摘要实例
    private static MessageDigest messageDigest;

    // 静态初始化块，加载MD5算法实例
    static {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.err.println(Md5Util.class.getName() + "初始化失败，MessageDigest不支持MD5算法。");
            e.printStackTrace();
        }
    }

    /**
     * 生成字符串的MD5哈希值
     * @param str 要加密的字符串
     * @return 32位MD5哈希字符串
     */
    public static String getMD5String(String str) {
        return getMD5String(str.getBytes());
    }

    /**
     * 校验字符串的MD5哈希值是否匹配
     * @param password   待校验的原始字符串
     * @param md5PwdStr 已知的MD5哈希值
     * @return 如果匹配返回true，否则返回false
     */
    public static boolean checkPassword(String password, String md5PwdStr) {
        String actualMd5 = getMD5String(password);
        return actualMd5.equals(md5PwdStr);
    }

    /**
     * 生成字节数组的MD5哈希值
     * @param bytes 要加密的字节数组
     * @return 32位MD5哈希字符串
     */
    public static String getMD5String(byte[] bytes) {
        messageDigest.update(bytes);
        return bufferToHex(messageDigest.digest());
    }

    /**
     * 将整个字节数组转换为十六进制字符串
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bufferToHex(byte[] bytes) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    /**
     * 将字节数组的部分内容转换为十六进制字符串
     * @param bytes 字节数组
     * @param offset 起始偏移量
     * @param length 要转换的长度
     * @return 十六进制字符串
     */
    private static String bufferToHex(byte[] bytes, int offset, int length) {
        StringBuilder builder = new StringBuilder(2 * length);
        int end = offset + length;

        for (int i = offset; i < end; i++) {
            appendHexPair(bytes[i], builder);
        }
        return builder.toString();
    }

    /**
     * 将单个字节转换为十六进制字符并追加到字符串缓冲区
     * @param byteValue 字节值
     * @param builder 字符串缓冲区
     */
    private static void appendHexPair(byte byteValue, StringBuilder builder) {
        // 取字节高4位转换为字符
        char high = HEX_DIGITS[(byteValue & 0xF0) >> 4];
        // 取字节低4位转换为字符
        char low = HEX_DIGITS[byteValue & 0xF];
        builder.append(high);
        builder.append(low);
    }
}