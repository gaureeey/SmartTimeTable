package com.timetable.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.timetable.model.TimetableDAO;
import com.timetable.model.TimetableEntry;
import com.timetable.util.OllamaClient;
import com.timetable.util.WebUtil;

public class AutoGenerateServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_OLLAMA_API = "http://127.0.0.1:11434/api/generate";
    private static final String DEFAULT_OLLAMA_MODEL = "qwen2.5-coder:7b";

    private final TimetableDAO timetableDAO = new TimetableDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WebUtil.setNoCacheHeaders(response);

        if (!WebUtil.requireLogin(request, response)) {
            return;
        }

        applyDefaults(request);
        request.getRequestDispatcher("/auto-generate.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WebUtil.setNoCacheHeaders(response);

        if (!WebUtil.requireLogin(request, response)) {
            return;
        }

        HttpSession session = request.getSession(false);
        String username = String.valueOf(session.getAttribute("username"));

        String modelInput = WebUtil.safeTrim(request.getParameter("model"));
        String semesterInput = WebUtil.safeTrim(request.getParameter("semester"));
        String batchSection = WebUtil.safeTrim(request.getParameter("batchSection"));
        String daysInput = WebUtil.safeTrim(request.getParameter("days"));
        String startTimeInput = WebUtil.safeTrim(request.getParameter("startTime"));
        String slotsPerDayInput = WebUtil.safeTrim(request.getParameter("slotsPerDay"));
        String slotDurationInput = WebUtil.safeTrim(request.getParameter("slotDuration"));
        String breakDurationInput = WebUtil.safeTrim(request.getParameter("breakDuration"));
        String subjectsInput = WebUtil.safeTrim(request.getParameter("subjects"));
        String additionalInput = WebUtil.safeTrim(request.getParameter("additionalInstructions"));

        request.setAttribute("model", modelInput);
        request.setAttribute("semester", semesterInput);
        request.setAttribute("batchSection", batchSection);
        request.setAttribute("days", daysInput);
        request.setAttribute("startTime", startTimeInput);
        request.setAttribute("slotsPerDay", slotsPerDayInput);
        request.setAttribute("slotDuration", slotDurationInput);
        request.setAttribute("breakDuration", breakDurationInput);
        request.setAttribute("subjects", subjectsInput);
        request.setAttribute("additionalInstructions", additionalInput);

        try {
            int semester = parseRangeInt(semesterInput, 1, 8, "Semester must be between 1 and 8.");
            int slotsPerDay = parseRangeInt(slotsPerDayInput, 1, 10, "Slots per day must be between 1 and 10.");
            int slotDuration = parseRangeInt(slotDurationInput, 30, 180,
                    "Slot duration must be between 30 and 180 minutes.");
            int breakDuration = parseRangeInt(breakDurationInput, 0, 60,
                    "Break duration must be between 0 and 60 minutes.");

            if (batchSection.isEmpty()) {
                throw new IllegalArgumentException("Batch/Section is required.");
            }

            if (subjectsInput.isEmpty()) {
                throw new IllegalArgumentException("Subject lines are required for AI generation.");
            }

            Time startTime = WebUtil.parseTime(startTimeInput);
            if (startTime == null) {
                throw new IllegalArgumentException("Start time is invalid.");
            }

            List<String> days = normalizeDays(daysInput);
            if (days.isEmpty()) {
                throw new IllegalArgumentException("Please provide at least one valid day.");
            }

            String model = modelInput.isEmpty() ? configuredModel() : modelInput;
            request.setAttribute("model", model);

            String prompt = buildPrompt(days, batchSection, semester, startTimeInput, slotsPerDay,
                    slotDuration, breakDuration, subjectsInput, additionalInput);

            OllamaClient ollamaClient = new OllamaClient(configuredOllamaApi(), model);
            String rawResponse = ollamaClient.generate(prompt);
                request.setAttribute("rawResponse", rawResponse);

            List<String> parseWarnings = new ArrayList<String>();
            List<TimetableEntry> generatedEntries = parseGeneratedEntries(rawResponse, batchSection, semester,
                    parseWarnings);

            if (generatedEntries.isEmpty()) {
                throw new IllegalArgumentException(
                        "Ollama returned output, but no valid timetable lines were found. Adjust prompt and try again.");
            }

            int insertedCount = 0;
            List<String> insertWarnings = new ArrayList<String>();

            for (TimetableEntry entry : generatedEntries) {
                try {
                    timetableDAO.insertEntry(entry, username);
                    insertedCount++;
                } catch (IllegalStateException ex) {
                    insertWarnings.add("Skipped " + toEntryLabel(entry) + ": " + ex.getMessage());
                } catch (SQLException ex) {
                    insertWarnings.add("Skipped " + toEntryLabel(entry) + ": database insertion failed.");
                }
            }

            request.setAttribute("generatedEntries", generatedEntries);
            request.setAttribute("parseWarnings", parseWarnings);
            request.setAttribute("insertWarnings", insertWarnings);

            if (insertedCount > 0) {
                request.setAttribute("successMessage",
                        "Generated " + generatedEntries.size() + " entries. Inserted " + insertedCount + " successfully.");
            } else {
                request.setAttribute("errorMessage",
                        "AI generation completed but no entries were inserted. Check warnings below.");
            }

        } catch (IllegalArgumentException ex) {
            request.setAttribute("errorMessage", ex.getMessage());
        } catch (IOException ex) {
            request.setAttribute("errorMessage",
                    "Ollama API call failed. Ensure `ollama serve` is running and model is pulled. Details: "
                            + ex.getMessage());
        }

        applyDefaults(request);
        request.getRequestDispatcher("/auto-generate.jsp").forward(request, response);
    }

    private String configuredOllamaApi() {
        String configured = getServletContext().getInitParameter("ollamaApiUrl");
        if (configured == null || configured.trim().isEmpty()) {
            return DEFAULT_OLLAMA_API;
        }
        return configured.trim();
    }

    private String configuredModel() {
        String configured = getServletContext().getInitParameter("ollamaModel");
        if (configured == null || configured.trim().isEmpty()) {
            return DEFAULT_OLLAMA_MODEL;
        }
        return configured.trim();
    }

    private void applyDefaults(HttpServletRequest request) {
        if (request.getAttribute("defaultModel") == null) {
            request.setAttribute("defaultModel", configuredModel());
        }

        if (request.getAttribute("model") == null) {
            request.setAttribute("model", configuredModel());
        }

        if (request.getAttribute("semester") == null) {
            request.setAttribute("semester", "3");
        }

        if (request.getAttribute("batchSection") == null) {
            request.setAttribute("batchSection", "MCA-1");
        }

        if (request.getAttribute("days") == null) {
            request.setAttribute("days", "Mon,Tue,Wed,Thu,Fri");
        }

        if (request.getAttribute("startTime") == null) {
            request.setAttribute("startTime", "09:00");
        }

        if (request.getAttribute("slotsPerDay") == null) {
            request.setAttribute("slotsPerDay", "4");
        }

        if (request.getAttribute("slotDuration") == null) {
            request.setAttribute("slotDuration", "60");
        }

        if (request.getAttribute("breakDuration") == null) {
            request.setAttribute("breakDuration", "15");
        }

        if (request.getAttribute("subjects") == null) {
            request.setAttribute("subjects",
                    "Data Structures|CS301|Dr. Sharma|LAB-1|3\n"
                            + "Database Systems|CS302|Prof. Nair|R-204|3\n"
                            + "Computer Networks|CS303|Dr. Menon|R-202|2\n"
                            + "Software Engineering|CS304|Prof. Das|R-204|2");
        }

        if (request.getAttribute("additionalInstructions") == null) {
            request.setAttribute("additionalInstructions", "Avoid same subject in consecutive slots.");
        }

        request.setAttribute("ollamaApiUrl", configuredOllamaApi());
    }

    private List<String> normalizeDays(String daysInput) {
        List<String> normalized = new ArrayList<String>();
        Set<String> seen = new HashSet<String>();

        String[] rawDays = daysInput.split(",");
        for (int i = 0; i < rawDays.length; i++) {
            String day = normalizeDay(rawDays[i]);
            if (day == null) {
                continue;
            }
            if (!seen.contains(day)) {
                seen.add(day);
                normalized.add(day);
            }
        }

        return normalized;
    }

    private String normalizeDay(String value) {
        String day = WebUtil.safeTrim(value).toLowerCase(Locale.ENGLISH);
        if (day.startsWith("mon")) {
            return "Mon";
        }
        if (day.startsWith("tue")) {
            return "Tue";
        }
        if (day.startsWith("wed")) {
            return "Wed";
        }
        if (day.startsWith("thu")) {
            return "Thu";
        }
        if (day.startsWith("fri")) {
            return "Fri";
        }
        if (day.startsWith("sat")) {
            return "Sat";
        }
        if (day.startsWith("sun")) {
            return "Sun";
        }
        return null;
    }

    private int parseRangeInt(String value, int min, int max, String errorMessage) {
        Integer parsed = WebUtil.parseInteger(value);
        if (parsed == null || parsed.intValue() < min || parsed.intValue() > max) {
            throw new IllegalArgumentException(errorMessage);
        }
        return parsed.intValue();
    }

    private String buildPrompt(List<String> days, String batchSection, int semester,
            String startTime, int slotsPerDay, int slotDuration, int breakDuration,
            String subjects, String additionalInstructions) {

        int targetEntries = calculateTargetEntries(subjects, days.size() * slotsPerDay);

        StringBuilder prompt = new StringBuilder();
        prompt.append("Create a weekly college timetable. ");
        prompt.append("Return only plain text lines with no heading and no markdown.\n");
        prompt.append("Required line format:\n");
        prompt.append("Mon|09:00|10:00|Subject Name|CS301|Teacher Name|LAB-1|MCA-1|3\n\n");
        prompt.append("Rules:\n");
        prompt.append("1) Use only these days: ").append(join(days, ", ")).append("\n");
        prompt.append("2) Batch/Section must be exactly: ").append(batchSection).append("\n");
        prompt.append("3) Semester must be exactly: ").append(semester).append("\n");
        prompt.append("4) First slot starts at ").append(startTime).append("\n");
        prompt.append("5) Max slots per day: ").append(slotsPerDay).append("\n");
        prompt.append("6) Slot duration minutes: ").append(slotDuration).append("\n");
        prompt.append("7) Break duration minutes between slots: ").append(breakDuration).append("\n");
        prompt.append("8) Do not overlap same room at same day/time.\n");
        prompt.append("9) Keep teacher schedule realistic (avoid impossible overlaps).\n");
        prompt.append("10) Generate approximately ").append(targetEntries).append(" lines.\n\n");
        prompt.append("Subjects input (Subject|Code|Teacher|Room|WeeklySessions):\n");
        prompt.append(subjects).append("\n\n");

        if (!additionalInstructions.isEmpty()) {
            prompt.append("Additional instructions:\n").append(additionalInstructions).append("\n\n");
        }

        prompt.append("Output only timetable lines in the exact 9-column pipe-separated format.");
        return prompt.toString();
    }

    private int calculateTargetEntries(String subjects, int fallback) {
        int total = 0;
        String[] lines = subjects.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = WebUtil.safeTrim(lines[i]);
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\|");
            if (parts.length >= 5) {
                Integer sessions = WebUtil.parseInteger(WebUtil.safeTrim(parts[4]));
                if (sessions != null && sessions.intValue() > 0) {
                    total += sessions.intValue();
                }
            }
        }

        return total > 0 ? total : fallback;
    }

    private List<TimetableEntry> parseGeneratedEntries(String rawResponse, String defaultBatchSection,
            int defaultSemester, List<String> warnings) {

        List<TimetableEntry> entries = new ArrayList<>();
        String[] lines = rawResponse.split("\\r?\\n");

        for (String value : lines) {
            String line = WebUtil.safeTrim(value);
            if (line.isEmpty() || line.startsWith("```") || line.toLowerCase(Locale.ENGLISH).startsWith("day|")) {
                continue;
            }

            if (line.matches("^[|\\-\\s:]+$")) {
                continue;
            }

            if (line.startsWith("|") && line.endsWith("|") && line.length() > 2) {
                line = line.substring(1, line.length() - 1);
            }

            String[] parts = splitGeneratedLine(line);
            if (parts == null || parts.length < 9) {
                warnings.add("Ignored malformed line: " + line);
                continue;
            }

            String day = normalizeDay(parts[0]);
            Time startTime = parseTimeFlexible(parts[1]);
            Time endTime = parseTimeFlexible(parts[2]);
            String subjectName = stripQuotes(WebUtil.safeTrim(parts[3]));
            String subjectCode = stripQuotes(WebUtil.safeTrim(parts[4])).toUpperCase(Locale.ENGLISH);
            String teacherName = stripQuotes(WebUtil.safeTrim(parts[5]));
            String roomNumber = stripQuotes(WebUtil.safeTrim(parts[6]));
            String batchSection = stripQuotes(WebUtil.safeTrim(parts[7]));
            Integer semester = WebUtil.parseInteger(stripQuotes(WebUtil.safeTrim(parts[8])));

            if (day == null || startTime == null || endTime == null) {
                warnings.add("Ignored invalid day/time line: " + line);
                continue;
            }

            if (!startTime.before(endTime)) {
                warnings.add("Ignored line with invalid time range: " + line);
                continue;
            }

            if (subjectName.isEmpty() || subjectCode.isEmpty() || teacherName.isEmpty() || roomNumber.isEmpty()) {
                warnings.add("Ignored line with missing subject/teacher/room: " + line);
                continue;
            }

            TimetableEntry entry = new TimetableEntry();
            entry.setDayOfWeek(day);
            entry.setStartTime(startTime);
            entry.setEndTime(endTime);
            entry.setSubjectName(subjectName);
            entry.setSubjectCode(subjectCode);
            entry.setTeacherName(teacherName);
            entry.setRoomNumber(roomNumber);
            entry.setBatchSection(batchSection.isEmpty() ? defaultBatchSection : batchSection);
            entry.setSemester(semester == null ? defaultSemester : semester);
            entry.setActive(true);
            entries.add(entry);
        }

        return entries;
    }

    private String[] splitGeneratedLine(String line) {
        String[] parts;
        if (line.contains("|")) {
            parts = line.split("\\|");
        } else if (line.contains(",")) {
            parts = line.split(",");
        } else {
            return null;
        }

        List<String> cleaned = new ArrayList<>();
        for (String part : parts) {
            String value = WebUtil.safeTrim(part);
            if (!value.isEmpty()) {
                cleaned.add(value);
            }
        }

        if (cleaned.size() < 9) {
            return null;
        }

        String[] result = new String[9];
        for (int i = 0; i < 9; i++) {
            result[i] = cleaned.get(i);
        }
        return result;
    }

    private Time parseTimeFlexible(String value) {
        Time parsed = WebUtil.parseTime(value);
        if (parsed != null) {
            return parsed;
        }

        String normalized = WebUtil.safeTrim(value).toUpperCase(Locale.ENGLISH).replace(".", "");
        Pattern amPmPattern = Pattern.compile("^(\\d{1,2}):(\\d{2})\\s*([AP]M)$");
        Matcher matcher = amPmPattern.matcher(normalized);
        if (!matcher.matches()) {
            return null;
        }

        Integer hour = WebUtil.parseInteger(matcher.group(1));
        Integer minute = WebUtil.parseInteger(matcher.group(2));
        String meridiem = matcher.group(3);
        if (hour == null || minute == null || hour < 1 || hour > 12 || minute < 0 || minute > 59) {
            return null;
        }

        int twentyFourHour = hour;
        if ("PM".equals(meridiem) && hour < 12) {
            twentyFourHour = hour + 12;
        } else if ("AM".equals(meridiem) && hour == 12) {
            twentyFourHour = 0;
        }

        String converted = String.format(Locale.ENGLISH, "%02d:%02d", Integer.valueOf(twentyFourHour), minute);
        return WebUtil.parseTime(converted);
    }

    private String stripQuotes(String value) {
        if (value == null) {
            return "";
        }

        String trimmed = value.trim();
        if (trimmed.length() >= 2 && ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
                || (trimmed.startsWith("'") && trimmed.endsWith("'")))) {
            return trimmed.substring(1, trimmed.length() - 1).trim();
        }

        return trimmed;
    }

    private String toEntryLabel(TimetableEntry entry) {
        return entry.getDayOfWeek() + " " + entry.getStartTime() + " " + entry.getSubjectCode();
    }

    private String join(List<String> values, String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(delimiter);
            }
            builder.append(values.get(i));
        }
        return builder.toString();
    }
}
