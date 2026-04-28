package com.timetable.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.timetable.model.TimetableDAO;
import com.timetable.util.WebUtil;

public class DeleteEntryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final TimetableDAO timetableDAO = new TimetableDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WebUtil.setNoCacheHeaders(response);

        if (!WebUtil.requireLogin(request, response)) {
            return;
        }

        Integer entryId = WebUtil.parseInteger(request.getParameter("entryId"));
        if (entryId == null || entryId.intValue() <= 0) {
            redirect(response, request, "error", "Invalid entry id.");
            return;
        }

        HttpSession session = request.getSession(false);
        String username = String.valueOf(session.getAttribute("username"));

        String configuredMode = WebUtil.safeTrim(getServletContext().getInitParameter("deleteMode"));
        String requestedMode = WebUtil.safeTrim(request.getParameter("deleteMode"));
        String mode = requestedMode.isEmpty() ? configuredMode : requestedMode;
        boolean softDelete = !"hard".equalsIgnoreCase(mode);

        try {
            boolean deleted = timetableDAO.deleteEntry(entryId.intValue(), softDelete, username);
            if (deleted) {
                String message = softDelete ? "Entry moved to inactive state." : "Entry permanently deleted.";
                redirect(response, request, "success", message);
            } else {
                redirect(response, request, "error", "Entry could not be deleted.");
            }
        } catch (SQLException ex) {
            redirect(response, request, "error", "Deletion failed. Please verify dependencies and try again.");
        }
    }

    private void redirect(HttpServletResponse response, HttpServletRequest request, String key, String message)
            throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.name());
        response.sendRedirect(request.getContextPath() + "/view-entries?" + key + "=" + encoded);
    }
}
