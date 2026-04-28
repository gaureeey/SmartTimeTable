package com.timetable.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

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

public class SignupServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO = new UserDAO();
    private final TimetableDAO timetableDAO = new TimetableDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WebUtil.setNoCacheHeaders(response);
        response.sendRedirect(request.getContextPath() + "/login?tab=signup");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WebUtil.setNoCacheHeaders(response);

        HttpSession activeSession = request.getSession(false);
        if (activeSession != null && activeSession.getAttribute("username") != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        String username = WebUtil.safeTrim(request.getParameter("username"));
        String fullName = WebUtil.safeTrim(request.getParameter("fullName"));
        String password = WebUtil.safeTrim(request.getParameter("password"));
        String confirmPassword = WebUtil.safeTrim(request.getParameter("confirmPassword"));

        if (username.isEmpty() || fullName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            forwardWithError(request, response, "All signup fields are required.");
            return;
        }

        if (!username.matches("^[A-Za-z0-9._-]{3,50}$")) {
            forwardWithError(request, response,
                    "Username must be 3-50 characters and can only include letters, numbers, dot, underscore, and hyphen.");
            return;
        }

        if (fullName.length() < 2 || fullName.length() > 100) {
            forwardWithError(request, response, "Full name must be between 2 and 100 characters.");
            return;
        }

        if (password.length() < 6 || password.length() > 64) {
            forwardWithError(request, response, "Password must be between 6 and 64 characters.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            forwardWithError(request, response, "Password and confirm password do not match.");
            return;
        }

        try {
            if (userDAO.usernameExists(username)) {
                forwardWithError(request, response, "Username already exists. Please choose another one.");
                return;
            }

            User user = userDAO.createUser(username, fullName, password);

            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession newSession = request.getSession(true);
            newSession.setMaxInactiveInterval(30 * 60);
            newSession.setAttribute("username", user.getUsername());
            newSession.setAttribute("fullName", user.getFullName());

            response.sendRedirect(request.getContextPath() + "/dashboard");
        } catch (SQLIntegrityConstraintViolationException ex) {
            forwardWithError(request, response, "Username already exists. Please choose another one.");
        } catch (SQLException ex) {
            forwardWithError(request, response,
                    "Unable to connect to the database right now. Please try again in a moment.");
        }
    }

    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("signupErrorMessage", message);
        request.setAttribute("showSignup", "1");
        populatePreviewStats(request);
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    private void populatePreviewStats(HttpServletRequest request) {
        try {
            DashboardStats previewStats = timetableDAO.getDashboardStats();
            request.setAttribute("previewStats", previewStats);
        } catch (SQLException ex) {
            // Keep signup error flow functional even if preview metrics are unavailable.
        }
    }
}