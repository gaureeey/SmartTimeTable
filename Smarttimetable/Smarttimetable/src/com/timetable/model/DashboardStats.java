package com.timetable.model;

import java.util.ArrayList;
import java.util.List;

public class DashboardStats {

    private int totalEntries;
    private int entriesToday;
    private int distinctSubjects;
    private int distinctTeachers;
    private int totalUsers;
    private List<ActivityLog> recentActivities = new ArrayList<>();

    public int getTotalEntries() {
        return totalEntries;
    }

    public void setTotalEntries(int totalEntries) {
        this.totalEntries = totalEntries;
    }

    public int getEntriesToday() {
        return entriesToday;
    }

    public void setEntriesToday(int entriesToday) {
        this.entriesToday = entriesToday;
    }

    public int getDistinctSubjects() {
        return distinctSubjects;
    }

    public void setDistinctSubjects(int distinctSubjects) {
        this.distinctSubjects = distinctSubjects;
    }

    public int getDistinctTeachers() {
        return distinctTeachers;
    }

    public void setDistinctTeachers(int distinctTeachers) {
        this.distinctTeachers = distinctTeachers;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public List<ActivityLog> getRecentActivities() {
        return recentActivities;
    }

    public void setRecentActivities(List<ActivityLog> recentActivities) {
        this.recentActivities = recentActivities;
    }
}
