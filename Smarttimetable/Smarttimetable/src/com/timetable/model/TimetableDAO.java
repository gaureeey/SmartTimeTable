package com.timetable.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TimetableDAO {

    public int insertEntry(TimetableEntry entry, String performedBy) throws SQLException {
        if (hasSlotConflict(entry, null)) {
            throw new IllegalStateException("Slot conflict detected for the same day, time, and room.");
        }

        String sql = "INSERT INTO timetable_entries "
                + "(day_of_week, start_time, end_time, subject_name, subject_code, teacher_name, room_number, "
                + "batch_section, semester, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)";

        try (Connection connection = DBConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bindEntry(statement, entry, false);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int entryId = generatedKeys.getInt(1);
                    logActivity(connection, "CREATE",
                            "Created timetable entry #" + entryId + " for " + entry.getSubjectCode(), performedBy);
                    return entryId;
                }
            }
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new IllegalStateException("Unable to create entry. Duplicate slot exists for day/time/room.", ex);
        }

        throw new SQLException("Failed to create timetable entry.");
    }

    public List<TimetableEntry> getAllEntries(String search, String day, Integer semester, boolean includeInactive)
            throws SQLException {
        List<TimetableEntry> entries = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT entry_id, day_of_week, start_time, end_time, subject_name, subject_code, teacher_name, "
                        + "room_number, batch_section, semester, is_active "
                        + "FROM timetable_entries WHERE 1 = 1 ");

        if (!includeInactive) {
            sql.append("AND is_active = TRUE ");
        }

        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (subject_name LIKE ? OR teacher_name LIKE ? OR day_of_week LIKE ?) ");
            String wildcard = "%" + search.trim() + "%";
            params.add(wildcard);
            params.add(wildcard);
            params.add(wildcard);
        }

        if (day != null && !day.trim().isEmpty()) {
            sql.append("AND day_of_week = ? ");
            params.add(day.trim());
        }

        if (semester != null) {
            sql.append("AND semester = ? ");
            params.add(semester);
        }

        sql.append("ORDER BY FIELD(day_of_week, 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'), start_time ASC");

        try (Connection connection = DBConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    entries.add(mapEntry(resultSet));
                }
            }
        }

        return entries;
    }

    public List<TimetableEntry> getEntriesByBatchAndSemester(String batchSection, Integer semester) throws SQLException {
        List<TimetableEntry> entries = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT entry_id, day_of_week, start_time, end_time, subject_name, subject_code, teacher_name, "
                        + "room_number, batch_section, semester, is_active "
                        + "FROM timetable_entries WHERE is_active = TRUE ");

        if (batchSection != null && !batchSection.trim().isEmpty()) {
            sql.append("AND batch_section = ? ");
            params.add(batchSection.trim());
        }

        if (semester != null) {
            sql.append("AND semester = ? ");
            params.add(semester);
        }

        sql.append("ORDER BY FIELD(day_of_week, 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'), start_time ASC");

        try (Connection connection = DBConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    entries.add(mapEntry(resultSet));
                }
            }
        }

        return entries;
    }

    public List<String> getDistinctBatchSections() throws SQLException {
        List<String> branches = new ArrayList<>();
        String sql = "SELECT DISTINCT batch_section FROM timetable_entries WHERE is_active = TRUE ORDER BY batch_section ASC";

        try (Connection connection = DBConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String branch = resultSet.getString("batch_section");
                if (branch != null && !branch.trim().isEmpty()) {
                    branches.add(branch.trim());
                }
            }
        }

        return branches;
    }

    public TimetableEntry getEntryById(int entryId) throws SQLException {
        String sql = "SELECT entry_id, day_of_week, start_time, end_time, subject_name, subject_code, teacher_name, "
                + "room_number, batch_section, semester, is_active "
                + "FROM timetable_entries WHERE entry_id = ? AND is_active = TRUE";

        try (Connection connection = DBConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, entryId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapEntry(resultSet);
                }
            }
        }

        return null;
    }

    public boolean updateEntry(TimetableEntry entry, String performedBy) throws SQLException {
        if (hasSlotConflict(entry, entry.getEntryId())) {
            throw new IllegalStateException("Slot conflict detected for the same day, time, and room.");
        }

        String sql = "UPDATE timetable_entries SET day_of_week = ?, start_time = ?, end_time = ?, subject_name = ?, "
                + "subject_code = ?, teacher_name = ?, room_number = ?, batch_section = ?, semester = ?, updated_at = NOW() "
                + "WHERE entry_id = ? AND is_active = TRUE";

        try (Connection connection = DBConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            bindEntry(statement, entry, true);
            int updatedRows = statement.executeUpdate();
            if (updatedRows > 0) {
                logActivity(connection, "UPDATE",
                        "Updated timetable entry #" + entry.getEntryId() + " for " + entry.getSubjectCode(),
                        performedBy);
                return true;
            }
        }

        return false;
    }

    public boolean deleteEntry(int entryId, boolean softDelete, String performedBy) throws SQLException {
        String sql;
        if (softDelete) {
            sql = "UPDATE timetable_entries SET is_active = FALSE, updated_at = NOW() WHERE entry_id = ? AND is_active = TRUE";
        } else {
            sql = "DELETE FROM timetable_entries WHERE entry_id = ?";
        }

        try (Connection connection = DBConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, entryId);
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                String action = softDelete ? "SOFT_DELETE" : "HARD_DELETE";
                logActivity(connection, action, "Deleted timetable entry #" + entryId, performedBy);
                return true;
            }
        }

        return false;
    }

    public DashboardStats getDashboardStats() throws SQLException {
        DashboardStats stats = new DashboardStats();
        stats.setTotalEntries(fetchCount("SELECT COUNT(*) FROM timetable_entries WHERE is_active = TRUE"));

        String today = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        stats.setEntriesToday(fetchCountWithStringParam(
                "SELECT COUNT(*) FROM timetable_entries WHERE is_active = TRUE AND day_of_week = ?", today));

        stats.setDistinctSubjects(fetchCount(
                "SELECT COUNT(DISTINCT subject_code) FROM timetable_entries WHERE is_active = TRUE"));
        stats.setDistinctTeachers(fetchCount(
                "SELECT COUNT(DISTINCT teacher_name) FROM timetable_entries WHERE is_active = TRUE"));
        stats.setTotalUsers(fetchCount("SELECT COUNT(*) FROM users"));
        stats.setRecentActivities(fetchRecentActivities());

        return stats;
    }

    private int fetchCount(String sql) throws SQLException {
        try (Connection connection = DBConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        }
    }

    private int fetchCountWithStringParam(String sql, String value) throws SQLException {
        try (Connection connection = DBConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                return 0;
            }
        }
    }

    private List<ActivityLog> fetchRecentActivities() throws SQLException {
        List<ActivityLog> activities = new ArrayList<>();
        String sql = "SELECT action_type, action_message, performed_by, action_time FROM activity_log "
                + "ORDER BY action_time DESC LIMIT 5";

        try (Connection connection = DBConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                ActivityLog activity = new ActivityLog();
                activity.setActionType(resultSet.getString("action_type"));
                activity.setActionMessage(resultSet.getString("action_message"));
                activity.setPerformedBy(resultSet.getString("performed_by"));
                activity.setActionTime(resultSet.getTimestamp("action_time"));
                activities.add(activity);
            }
        }

        return activities;
    }

    private boolean hasSlotConflict(TimetableEntry entry, Integer excludeEntryId) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM timetable_entries WHERE day_of_week = ? AND start_time = ? "
                        + "AND room_number = ? AND is_active = TRUE");

        if (excludeEntryId != null) {
            sql.append(" AND entry_id <> ?");
        }

        try (Connection connection = DBConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            statement.setString(1, entry.getDayOfWeek());
            statement.setTime(2, entry.getStartTime());
            statement.setString(3, entry.getRoomNumber());

            if (excludeEntryId != null) {
                statement.setInt(4, excludeEntryId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    private void bindEntry(PreparedStatement statement, TimetableEntry entry, boolean includeEntryId) throws SQLException {
        statement.setString(1, entry.getDayOfWeek());
        statement.setTime(2, entry.getStartTime());
        statement.setTime(3, entry.getEndTime());
        statement.setString(4, entry.getSubjectName());
        statement.setString(5, entry.getSubjectCode());
        statement.setString(6, entry.getTeacherName());
        statement.setString(7, entry.getRoomNumber());
        statement.setString(8, entry.getBatchSection());
        statement.setInt(9, entry.getSemester());

        if (includeEntryId) {
            statement.setInt(10, entry.getEntryId());
        }
    }

    private TimetableEntry mapEntry(ResultSet resultSet) throws SQLException {
        TimetableEntry entry = new TimetableEntry();
        entry.setEntryId(resultSet.getInt("entry_id"));
        entry.setDayOfWeek(resultSet.getString("day_of_week"));
        entry.setStartTime(resultSet.getTime("start_time"));
        entry.setEndTime(resultSet.getTime("end_time"));
        entry.setSubjectName(resultSet.getString("subject_name"));
        entry.setSubjectCode(resultSet.getString("subject_code"));
        entry.setTeacherName(resultSet.getString("teacher_name"));
        entry.setRoomNumber(resultSet.getString("room_number"));
        entry.setBatchSection(resultSet.getString("batch_section"));
        entry.setSemester(resultSet.getInt("semester"));
        entry.setActive(resultSet.getBoolean("is_active"));
        return entry;
    }

    private void logActivity(Connection connection, String actionType, String actionMessage, String performedBy)
            throws SQLException {
        String sql = "INSERT INTO activity_log (action_type, action_message, performed_by) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, actionType);
            statement.setString(2, actionMessage);
            statement.setString(3, performedBy);
            statement.executeUpdate();
        }
    }
}
