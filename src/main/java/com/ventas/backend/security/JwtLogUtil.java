package com.ventas.backend.security;

public final class JwtLogUtil {

    private JwtLogUtil() {

    }

    public static String enmascarar(String token) {
        if (token == null || token.length() < 20) {
            return "***";
        }
        String[] partes = token.split("\\.");
        if (partes.length == 3) {
            return partes[0] + "." + partes[1] + ".****(firma oculta)****";
        }
        return token.substring(0, 15) + "..." + token.substring(token.length() - 10);
    }
}