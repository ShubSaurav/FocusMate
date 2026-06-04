package com.focusmate.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class PasswordUtil {
    public static String hash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                String hexPart = Integer.toHexString(0xff & b);
                if (hexPart.length() == 1) hex.append('0');
                hex.append(hexPart);
            }
            return hex.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Unable to hash password", ex);
        }
    }
}
