package dev.delous.linkservice.linking;

import org.apache.commons.codec.digest.DigestUtils;
import java.math.BigInteger;
import java.util.UUID;

public class Link {
    private static final char[] BASE62 =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    private static String toBase62(byte[] bytes) {
        BigInteger n = new BigInteger(1, bytes);
        if (n.equals(BigInteger.ZERO)) return "0";

        BigInteger base = BigInteger.valueOf(62);
        StringBuilder sb = new StringBuilder();
        while (n.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divRem = n.divideAndRemainder(base);
            sb.append(BASE62[divRem[1].intValue()]);
            n = divRem[0];
        }
        return sb.reverse().toString();
    }

    public static String getShort(String sourceUrl, UUID uuid) {
        String input = sourceUrl + "|" + uuid.toString().toLowerCase();

        byte[] sha = DigestUtils.sha256(input);
        String base62 = toBase62(sha);

        if (base62.length() >= 8) return base62.substring(0, 8);
        return "0".repeat(8 - base62.length()) + base62;
    }
}
