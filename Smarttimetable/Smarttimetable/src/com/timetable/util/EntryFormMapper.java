package com.timetable.util;

import java.sql.Time;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import com.timetable.model.TimetableEntry;

public final class EntryFormMapper {

    private static final Set<String> VALID_DAYS = new HashSet<>(
            Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"));

    private EntryFormMapper() {
    }

    public static TimetableEntry parseAndValidate(HttpServletRequest request) {
        TimetableEntry entry = new TimetableEntry();
        entry.setDayOfWeek(WebUtil.safeTrim(request.getParameter("dayOfWeek")));
        entry.setStartTime(WebUtil.parseTime(request.getParameter("startTime")));
        entry.setEndTime(WebUtil.parseTime(request.getParameter("endTime")));
        entry.setSubjectName(WebUtil.safeTrim(request.getParameter("subjectName")));
        entry.setSubjectCode(WebUtil.safeTrim(request.getParameter("subjectCode")).toUpperCase(Locale.ENGLISH));
        entry.setTeacherName(WebUtil.safeTrim(request.getParameter("teacherName")));
        entry.setRoomNumber(WebUtil.safeTrim(request.getParameter("roomNumber")));
        entry.setBatchSection(WebUtil.safeTrim(request.getParameter("batchSection")));

        Integer semester = WebUtil.parseInteger(request.getParameter("semester"));
        entry.setSemester(semester == null ? 0 : semester.intValue());

        validate(entry);
        return entry;
    }

    private static void validate(TimetableEntry entry) {
        if (!VALID_DAYS.contains(entry.getDayOfWeek())) {
            throw new IllegalArgumentException("Please select a valid day of week.");
        }

        Time start = entry.getStartTime();
        Time end = entry.getEndTime();
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start time and end time are required.");
        }

        if (!start.before(end)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }

        require(entry.getSubjectName(), "Subject name is required.");
        require(entry.getSubjectCode(), "Subject code is required.");
        require(entry.getTeacherName(), "Teacher name is required.");
        require(entry.getRoomNumber(), "Room/Lab number is required.");
        require(entry.getBatchSection(), "Batch/Section is required.");

        if (!entry.getSubjectCode().matches("[A-Z0-9-]{2,20}")) {
            throw new IllegalArgumentException("Subject code must be 2-20 characters (A-Z, 0-9, -).");
        }

        if (entry.getSemester() < 1 || entry.getSemester() > 8) {
            throw new IllegalArgumentException("Semester must be between 1 and 8.");
        }
    }

    private static void require(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
}
