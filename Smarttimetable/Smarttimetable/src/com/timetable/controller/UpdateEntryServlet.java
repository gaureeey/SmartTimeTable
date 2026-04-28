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
import com.timetable.model.TimetableEntry;
import com.timetable.util.EntryFormMapper;
import com.timetable.util.WebUtil;

public class UpdateEntryServlet extends HttpServlet {

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
            redirectWithError(response, request, "Invalid entry id.");
            return;
        }

        HttpSession session = request.getSession(false);
        String username = String.valueOf(session.getAttribute("username"));

        try {
            TimetableEntry entry = EntryFormMapper.parseAndValidate(request);
            entry.setEntryId(entryId.intValue());

            boolean updated = timetableDAO.updateEntry(entry, username);
            if (updated) {
                String success = URLEncoder.encode("Entry #" + entryId + " updated successfully.",
                        StandardCharsets.UTF_8.name());
                response.sendRedirect(request.getContextPath() + "/view-entries?success=" + success);
            } else {
                redirectWithError(response, request, "Entry could not be updated.");
            }
        } catch (IllegalArgumentException ex) {
            request.setAttribute("errorMessage", ex.getMessage());
            request.setAttribute("entry", remapEntryForForm(request, entryId.intValue()));
            request.getRequestDispatcher("/edit-entry.jsp").forward(request, response);
        } catch (IllegalStateException ex) {
            request.setAttribute("errorMessage", ex.getMessage());
            request.setAttribute("entry", remapEntryForForm(request, entryId.intValue()));
            request.getRequestDispatcher("/edit-entry.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException("Unable to update timetable entry.", ex);
        }
    }

    private TimetableEntry remapEntryForForm(HttpServletRequest request, int entryId) {
        TimetableEntry entry = new TimetableEntry();
        entry.setEntryId(entryId);
        entry.setDayOfWeek(WebUtil.safeTrim(request.getParameter("dayOfWeek")));
        entry.setStartTime(WebUtil.parseTime(request.getParameter("startTime")));
        entry.setEndTime(WebUtil.parseTime(request.getParameter("endTime")));
        entry.setSubjectName(WebUtil.safeTrim(request.getParameter("subjectName")));
        entry.setSubjectCode(WebUtil.safeTrim(request.getParameter("subjectCode")));
        entry.setTeacherName(WebUtil.safeTrim(request.getParameter("teacherName")));
        entry.setRoomNumber(WebUtil.safeTrim(request.getParameter("roomNumber")));
        entry.setBatchSection(WebUtil.safeTrim(request.getParameter("batchSection")));

        Integer semester = WebUtil.parseInteger(request.getParameter("semester"));
        entry.setSemester(semester == null ? 0 : semester.intValue());
        return entry;
    }

    private void redirectWithError(HttpServletResponse response, HttpServletRequest request, String message)
            throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.name());
        response.sendRedirect(request.getContextPath() + "/view-entries?error=" + encoded);
    }
}
