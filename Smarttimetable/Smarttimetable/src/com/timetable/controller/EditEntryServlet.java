package com.timetable.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.timetable.model.TimetableDAO;
import com.timetable.model.TimetableEntry;
import com.timetable.util.WebUtil;

public class EditEntryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final TimetableDAO timetableDAO = new TimetableDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WebUtil.setNoCacheHeaders(response);

        if (!WebUtil.requireLogin(request, response)) {
            return;
        }

        Integer entryId = WebUtil.parseInteger(request.getParameter("id"));
        if (entryId == null || entryId.intValue() <= 0) {
            redirectWithError(response, request, "Invalid entry id.");
            return;
        }

        try {
            TimetableEntry entry = timetableDAO.getEntryById(entryId.intValue());
            if (entry == null) {
                redirectWithError(response, request, "Entry not found or inactive.");
                return;
            }

            request.setAttribute("entry", entry);
            request.getRequestDispatcher("/edit-entry.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException("Unable to load entry for editing.", ex);
        }
    }

    private void redirectWithError(HttpServletResponse response, HttpServletRequest request, String message)
            throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.name());
        response.sendRedirect(request.getContextPath() + "/view-entries?error=" + encoded);
    }
}
