package com.timetable.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.timetable.model.TimetableDAO;
import com.timetable.model.TimetableEntry;
import com.timetable.util.WebUtil;

public class ViewEntriesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 10;

    private final TimetableDAO timetableDAO = new TimetableDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WebUtil.setNoCacheHeaders(response);

        if (!WebUtil.requireLogin(request, response)) {
            return;
        }

        String search = WebUtil.safeTrim(request.getParameter("search"));
        String day = WebUtil.safeTrim(request.getParameter("day"));
        Integer semester = WebUtil.parseInteger(request.getParameter("semester"));
        String sortBy = normalizeSortBy(WebUtil.safeTrim(request.getParameter("sortBy")));
        String sortDir = normalizeSortDir(WebUtil.safeTrim(request.getParameter("sortDir")));
        Integer requestedPage = WebUtil.parseInteger(request.getParameter("page"));
        int currentPage = requestedPage == null || requestedPage < 1 ? 1 : requestedPage;

        try {
            List<TimetableEntry> entries = timetableDAO.getAllEntries(search, day, semester, false);
            sortEntries(entries, sortBy, sortDir);

            int totalEntries = entries.size();
            int totalPages = (int) Math.ceil(totalEntries / (double) PAGE_SIZE);
            if (totalPages < 1) {
                totalPages = 1;
            }
            if (currentPage > totalPages) {
                currentPage = totalPages;
            }

            int fromIndex = (currentPage - 1) * PAGE_SIZE;
            int toIndex = Math.min(fromIndex + PAGE_SIZE, totalEntries);
            List<TimetableEntry> pageEntries = entries.subList(fromIndex, toIndex);

            request.setAttribute("entries", pageEntries);
            request.setAttribute("totalEntries", Integer.valueOf(totalEntries));
            request.setAttribute("currentPage", Integer.valueOf(currentPage));
            request.setAttribute("totalPages", Integer.valueOf(totalPages));
            request.setAttribute("sortBy", sortBy);
            request.setAttribute("sortDir", sortDir);
            request.setAttribute("search", search);
            request.setAttribute("day", day);
            request.setAttribute("semester", semester);
            request.setAttribute("filterContext", buildFilterContext(search, day, semester, sortBy, sortDir));

            request.getRequestDispatcher("/view-entries.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException("Unable to fetch timetable entries.", ex);
        }
    }

    private String buildFilterContext(String search, String day, Integer semester, String sortBy, String sortDir) {
        List<String> clauses = new ArrayList<>();

        if (!search.isEmpty()) {
            clauses.add("Search: \"" + search + "\"");
        }

        if (!day.isEmpty()) {
            clauses.add("Day: " + day);
        }

        if (semester != null) {
            clauses.add("Semester: " + semester);
        }

        clauses.add("Sort: " + humanReadableSort(sortBy) + " (" + sortDir.toUpperCase() + ")");

        StringBuilder context = new StringBuilder("Filtered by ");
        for (int i = 0; i < clauses.size(); i++) {
            if (i > 0) {
                context.append(" | ");
            }
            context.append(clauses.get(i));
        }
        return context.toString();
    }

    private String normalizeSortBy(String sortBy) {
        if ("subject".equalsIgnoreCase(sortBy)) {
            return "subject";
        }
        if ("teacher".equalsIgnoreCase(sortBy)) {
            return "teacher";
        }
        if ("semester".equalsIgnoreCase(sortBy)) {
            return "semester";
        }
        return "day";
    }

    private String normalizeSortDir(String sortDir) {
        return "desc".equalsIgnoreCase(sortDir) ? "desc" : "asc";
    }

    private String humanReadableSort(String sortBy) {
        if ("subject".equals(sortBy)) {
            return "Subject";
        }
        if ("teacher".equals(sortBy)) {
            return "Teacher";
        }
        if ("semester".equals(sortBy)) {
            return "Semester";
        }
        return "Day/Time";
    }

    private void sortEntries(List<TimetableEntry> entries, String sortBy, String sortDir) {
        Comparator<TimetableEntry> comparator;

        if ("subject".equals(sortBy)) {
            comparator = Comparator.comparing(
                    TimetableEntry::getSubjectName,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("teacher".equals(sortBy)) {
            comparator = Comparator.comparing(
                    TimetableEntry::getTeacherName,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("semester".equals(sortBy)) {
            comparator = Comparator.comparingInt(TimetableEntry::getSemester)
                    .thenComparing(TimetableEntry::getDayOfWeek, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else {
            comparator = Comparator
                    .comparingInt((TimetableEntry e) -> dayOrder(e.getDayOfWeek()))
                    .thenComparing(TimetableEntry::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()));
        }

        if ("desc".equals(sortDir)) {
            comparator = comparator.reversed();
        }

        Collections.sort(entries, comparator);
    }

    private int dayOrder(String day) {
        if ("Mon".equals(day)) {
            return 1;
        }
        if ("Tue".equals(day)) {
            return 2;
        }
        if ("Wed".equals(day)) {
            return 3;
        }
        if ("Thu".equals(day)) {
            return 4;
        }
        if ("Fri".equals(day)) {
            return 5;
        }
        if ("Sat".equals(day)) {
            return 6;
        }
        if ("Sun".equals(day)) {
            return 7;
        }
        return 8;
    }
}
