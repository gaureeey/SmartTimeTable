package com.timetable.controller;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.timetable.model.DashboardStats;
import com.timetable.model.TimetableDAO;
import com.timetable.util.WebUtil;

public class DashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final TimetableDAO timetableDAO = new TimetableDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WebUtil.setNoCacheHeaders(response);

        if (!WebUtil.requireLogin(request, response)) {
            return;
        }

        try {
            DashboardStats stats = timetableDAO.getDashboardStats();
            request.setAttribute("stats", stats);
            request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException("Unable to load dashboard data.", ex);
        }
    }
}
