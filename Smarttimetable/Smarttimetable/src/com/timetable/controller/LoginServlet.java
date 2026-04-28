package com.timetable.controller;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.timetable.model.DashboardStats;
import com.timetable.model.TimetableDAO;
import com.timetable.model.User;
import com.timetable.model.UserDAO;
import com.timetable.util.WebUtil;

public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO = new UserDAO();
    private final TimetableDAO timetableDAO = new TimetableDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WebUtil.setNoCacheHeaders(response);

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("username") != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        populatePreviewStats(request);
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WebUtil.setNoCacheHeaders(response);

        String username = WebUtil.safeTrim(request.getParameter("username"));
        String password = WebUtil.safeTrim(request.getParameter("password"));

        if (username.isEmpty() || password.isEmpty()) {
            request.setAttribute("errorMessage", "Username and password are required.");
            populatePreviewStats(request);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        try {
            User user = userDAO.validateUser(username, password);
            if (user == null) {
                request.setAttribute("errorMessage", "Invalid User");
                populatePreviewStats(request);
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }

            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession newSession = request.getSession(true);
            newSession.setMaxInactiveInterval(30 * 60);
            newSession.setAttribute("username", user.getUsername());
            newSession.setAttribute("fullName", user.getFullName());

            response.sendRedirect(request.getContextPath() + "/dashboard");
        } catch (SQLException ex) {
            request.setAttribute("errorMessage",
                    "Unable to connect to the database right now. Please try again in a moment.");
            populatePreviewStats(request);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }

    private void populatePreviewStats(HttpServletRequest request) {
        try {
            DashboardStats previewStats = timetableDAO.getDashboardStats();
            request.setAttribute("previewStats", previewStats);
        } catch (SQLException ex) {
            // Keep login functional even if preview metrics are temporarily unavailable.
        }
    }
}
