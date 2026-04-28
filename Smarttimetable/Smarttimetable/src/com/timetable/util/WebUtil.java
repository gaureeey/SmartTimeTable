package com.timetable.util;

import java.io.IOException;
import java.sql.Time;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public final class WebUtil {

    private WebUtil() {
    }

    public static void setNoCacheHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    public static boolean requireLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=session");
            return false;
        }
        return true;
    }

    public static Integer parseInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    public static Time parseTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() == 5) {
            trimmed = trimmed + ":00";
        }
        try {
            return Time.valueOf(trimmed);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
