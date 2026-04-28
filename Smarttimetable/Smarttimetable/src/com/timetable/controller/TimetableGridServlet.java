package com.timetable.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.timetable.model.TimetableDAO;
import com.timetable.model.TimetableEntry;
import com.timetable.model.TimetableSlot;
import com.timetable.util.WebUtil;

public class TimetableGridServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] DAYS = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };

    private final TimetableDAO timetableDAO = new TimetableDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WebUtil.setNoCacheHeaders(response);

        if (!WebUtil.requireLogin(request, response)) {
            return;
        }

        try {
            List<String> branches = timetableDAO.getDistinctBatchSections();

            String selectedBatch = WebUtil.safeTrim(request.getParameter("batchSection"));

            Integer selectedSemester = WebUtil.parseInteger(request.getParameter("semester"));
            if (selectedSemester != null && (selectedSemester.intValue() < 1 || selectedSemester.intValue() > 8)) {
                selectedSemester = null;
            }

            String profile = normalizeProfile(WebUtil.safeTrim(request.getParameter("profile")));

            LocalTime defaultStart = "industry".equals(profile) ? LocalTime.of(8, 0) : LocalTime.of(9, 0);
            LocalTime startTime = parseStartTime(WebUtil.safeTrim(request.getParameter("gridStartTime")), defaultStart);

            Integer slotDurationParam = WebUtil.parseInteger(request.getParameter("slotDuration"));
            Integer breakDurationParam = WebUtil.parseInteger(request.getParameter("breakDuration"));
            int slotDuration = clamp(slotDurationParam == null ? ("industry".equals(profile) ? 50 : 60)
                    : slotDurationParam.intValue(), 30, 180);
            int breakDuration = clamp(breakDurationParam == null ? ("industry".equals(profile) ? 10 : 15)
                    : breakDurationParam.intValue(), 0, 60);

            List<TimetableEntry> entries = timetableDAO.getEntriesByBatchAndSemester(selectedBatch, selectedSemester);

            Integer slotsPerDayParam = WebUtil.parseInteger(request.getParameter("slotsPerDay"));
            int suggestedSlots = recommendSlotsPerDay(entries, selectedBatch, profile);
            int slotsPerDay = clamp(slotsPerDayParam == null ? suggestedSlots : slotsPerDayParam.intValue(), 1, 12);

            List<TimetableSlot> slots = buildSlots(startTime, slotsPerDay, slotDuration, breakDuration);
            Map<String, TimetableEntry> cellMap = new HashMap<String, TimetableEntry>();
            List<TimetableEntry> unplacedEntries = new ArrayList<TimetableEntry>();
            List<String> collisionWarnings = new ArrayList<String>();

            for (TimetableEntry entry : entries) {
                int slotIndex = resolveSlotIndex(entry, slots);
                if (slotIndex < 0) {
                    unplacedEntries.add(entry);
                    continue;
                }

                String key = entry.getDayOfWeek() + "_" + slotIndex;
                if (cellMap.containsKey(key)) {
                    collisionWarnings.add("Conflict at " + entry.getDayOfWeek() + " " + slots.get(slotIndex).getLabel()
                            + ". Showing first entry and skipping duplicate.");
                    continue;
                }

                cellMap.put(key, entry);
            }

            request.setAttribute("branches", branches);
            request.setAttribute("days", DAYS);
            request.setAttribute("selectedBatch", selectedBatch);
            request.setAttribute("selectedSemester", selectedSemester);
            request.setAttribute("profile", profile);
            request.setAttribute("gridStartTime", startTime.format(TIME_FORMAT));
            request.setAttribute("slotsPerDay", Integer.valueOf(slotsPerDay));
            request.setAttribute("slotDuration", Integer.valueOf(slotDuration));
            request.setAttribute("breakDuration", Integer.valueOf(breakDuration));
            request.setAttribute("slots", slots);
            request.setAttribute("cellMap", cellMap);
            request.setAttribute("sourceEntryCount", Integer.valueOf(entries.size()));
            request.setAttribute("unplacedEntries", unplacedEntries);
            request.setAttribute("collisionWarnings", collisionWarnings);
            request.setAttribute("profileDescription", profileDescription(profile));

            request.getRequestDispatcher("/timetable-grid.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException("Unable to load timetable grid.", ex);
        }
    }

    private String normalizeProfile(String input) {
        return "industry".equalsIgnoreCase(input) ? "industry" : "branch";
    }

    private String profileDescription(String profile) {
        if ("industry".equals(profile)) {
            return "Industry Relevant profile uses compact slot timing and typically more practical sessions.";
        }
        return "Branch Standard profile uses regular academic slot timing.";
    }

    private int recommendSlotsPerDay(List<TimetableEntry> entries, String batchSection, String profile) {
        Map<String, Integer> countByDay = new HashMap<String, Integer>();

        for (TimetableEntry entry : entries) {
            String day = entry.getDayOfWeek();
            Integer count = countByDay.get(day);
            countByDay.put(day, Integer.valueOf(count == null ? 1 : count.intValue() + 1));
        }

        int maxCount = 0;
        for (Integer count : countByDay.values()) {
            if (count.intValue() > maxCount) {
                maxCount = count.intValue();
            }
        }

        if (maxCount > 0) {
            return maxCount;
        }

        if ("industry".equals(profile)) {
            return 6;
        }

        String batch = batchSection == null ? "" : batchSection.toUpperCase(Locale.ENGLISH);
        if (batch.contains("MCA-2") || batch.contains("CSE") || batch.contains("IT")) {
            return 5;
        }

        return 4;
    }

    private LocalTime parseStartTime(String value, LocalTime fallback) {
        Time parsed = WebUtil.parseTime(value);
        if (parsed == null) {
            return fallback;
        }
        return parsed.toLocalTime();
    }

    private int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private List<TimetableSlot> buildSlots(LocalTime startTime, int slotsPerDay, int slotDuration, int breakDuration) {
        List<TimetableSlot> slots = new ArrayList<TimetableSlot>();

        LocalTime cursor = startTime;
        for (int i = 0; i < slotsPerDay; i++) {
            LocalTime end = cursor.plusMinutes(slotDuration);
            String label = cursor.format(TIME_FORMAT) + " - " + end.format(TIME_FORMAT);
            slots.add(new TimetableSlot(i, cursor, end, label));
            cursor = end.plusMinutes(breakDuration);
        }

        return slots;
    }

    private int resolveSlotIndex(TimetableEntry entry, List<TimetableSlot> slots) {
        if (entry.getStartTime() == null || entry.getDayOfWeek() == null) {
            return -1;
        }

        LocalTime entryStart = entry.getStartTime().toLocalTime();

        for (int i = 0; i < slots.size(); i++) {
            TimetableSlot slot = slots.get(i);
            if ((entryStart.equals(slot.getStartTime()) || entryStart.isAfter(slot.getStartTime()))
                    && entryStart.isBefore(slot.getEndTime())) {
                return i;
            }
        }

        return -1;
    }

    @SuppressWarnings("unused")
    private List<String> asList(String[] values) {
        if (values == null) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<String>();
        for (String value : values) {
            result.add(value);
        }
        return result;
    }
}
