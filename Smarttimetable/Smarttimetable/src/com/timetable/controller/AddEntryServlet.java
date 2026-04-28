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

public class AddEntryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final TimetableDAO timetableDAO = new TimetableDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WebUtil.setNoCacheHeaders(response);

        if (!WebUtil.requireLogin(request, response)) {
            return;
        }

        request.getRequestDispatcher("/add-entry.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WebUtil.setNoCacheHeaders(response);

        if (!WebUtil.requireLogin(request, response)) {
            return;
        }

        HttpSession session = request.getSession(false);
        String username = String.valueOf(session.getAttribute("username"));

        try {
            TimetableEntry entry = EntryFormMapper.parseAndValidate(request);
            int entryId = timetableDAO.insertEntry(entry, username);
            String success = URLEncoder.encode("Entry created successfully. ID: " + entryId,
                    StandardCharsets.UTF_8.name());
            response.sendRedirect(request.getContextPath() + "/view-entries?success=" + success);
        } catch (IllegalArgumentException ex) {
            request.setAttribute("errorMessage", ex.getMessage());
            request.setAttribute("entry", remapEntryForForm(request));
            request.getRequestDispatcher("/add-entry.jsp").forward(request, response);
        } catch (IllegalStateException ex) {
            request.setAttribute("errorMessage", ex.getMessage());
            request.setAttribute("entry", remapEntryForForm(request));
            request.getRequestDispatcher("/add-entry.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException("Unable to create timetable entry.", ex);
        }
    }

    private TimetableEntry remapEntryForForm(HttpServletRequest request) {
        TimetableEntry entry = new TimetableEntry();
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
}
