<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,java.util.Collections,com.timetable.model.TimetableEntry" %>
<%
request.setAttribute("activePage", "view");
List<TimetableEntry> entries = (List<TimetableEntry>) request.getAttribute("entries");
if (entries == null) {
    entries = Collections.emptyList();
}
String success = request.getParameter("success");
String error = request.getParameter("error");
String search = (String) request.getAttribute("search");
if (search == null) {
    search = "";
}
String day = (String) request.getAttribute("day");
if (day == null) {
    day = "";
}
Integer semester = (Integer) request.getAttribute("semester");
Integer totalEntriesObj = (Integer) request.getAttribute("totalEntries");
int totalEntries = totalEntriesObj == null ? entries.size() : totalEntriesObj.intValue();
Integer currentPageObj = (Integer) request.getAttribute("currentPage");
int currentPage = currentPageObj == null ? 1 : currentPageObj.intValue();
Integer totalPagesObj = (Integer) request.getAttribute("totalPages");
int totalPages = totalPagesObj == null ? 1 : totalPagesObj.intValue();
String sortBy = (String) request.getAttribute("sortBy");
if (sortBy == null || sortBy.isEmpty()) {
    sortBy = "day";
}
String sortDir = (String) request.getAttribute("sortDir");
if (sortDir == null || sortDir.isEmpty()) {
    sortDir = "asc";
}
String filterContext = (String) request.getAttribute("filterContext");
if (filterContext == null) {
    filterContext = "Showing all active entries";
}
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>View Entries | Smart Timetable Generator</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Lato:wght@400;600;700&family=Open+Sans:wght@400;600&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
        integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/custom-taupe.css">
</head>
<body class="app-page">
    <jsp:include page="includes/header.jsp" />

    <main class="container app-container">
        <section class="d-flex flex-column flex-lg-row justify-content-between align-items-lg-center mb-4 gap-2">
            <div>
                <h1 class="mb-1">Timetable Entries</h1>
                <p class="page-subtitle mb-0"><%= filterContext %></p>
            </div>
            <a href="<%= request.getContextPath() %>/add-entry" class="btn btn-primary-taupe">Add New Entry</a>
        </section>

        <%
        if (success != null) {
        %>
            <div class="alert alert-success" role="alert"><%= success %></div>
        <%
        }
        if (error != null) {
        %>
            <div class="alert alert-danger" role="alert"><%= error %></div>
        <%
        }
        %>

        <section class="card panel-card mb-4">
            <div class="card-body">
                <form method="get" action="<%= request.getContextPath() %>/view-entries" class="row g-3">
                    <input type="hidden" name="page" value="1">

                    <div class="col-md-3">
                        <label class="form-label" for="searchInput">Search</label>
                        <input type="text" id="searchInput" name="search" class="form-control"
                            placeholder="Subject, teacher, or day" value="<%= search %>">
                    </div>

                    <div class="col-md-2">
                        <label class="form-label" for="dayFilter">Day</label>
                        <select id="dayFilter" name="day" class="form-select">
                            <option value="">All Days</option>
                            <%
                            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                            for (String d : days) {
                            %>
                                <option value="<%= d %>" <%= d.equals(day) ? "selected" : "" %>><%= d %></option>
                            <%
                            }
                            %>
                        </select>
                    </div>

                    <div class="col-md-2">
                        <label class="form-label" for="semesterFilter">Semester</label>
                        <select id="semesterFilter" name="semester" class="form-select">
                            <option value="">All</option>
                            <%
                            for (int i = 1; i <= 8; i++) {
                            %>
                                <option value="<%= i %>" <%= semester != null && semester.intValue() == i ? "selected" : "" %>><%= i %></option>
                            <%
                            }
                            %>
                        </select>
                    </div>

                    <div class="col-md-2">
                        <label class="form-label" for="sortBy">Sort By</label>
                        <select id="sortBy" name="sortBy" class="form-select">
                            <option value="day" <%= "day".equals(sortBy) ? "selected" : "" %>>Day/Time</option>
                            <option value="subject" <%= "subject".equals(sortBy) ? "selected" : "" %>>Subject</option>
                            <option value="teacher" <%= "teacher".equals(sortBy) ? "selected" : "" %>>Teacher</option>
                            <option value="semester" <%= "semester".equals(sortBy) ? "selected" : "" %>>Semester</option>
                        </select>
                    </div>

                    <div class="col-md-1">
                        <label class="form-label" for="sortDir">Order</label>
                        <select id="sortDir" name="sortDir" class="form-select">
                            <option value="asc" <%= "asc".equals(sortDir) ? "selected" : "" %>>Asc</option>
                            <option value="desc" <%= "desc".equals(sortDir) ? "selected" : "" %>>Desc</option>
                        </select>
                    </div>

                    <div class="col-md-2 d-flex align-items-end gap-2">
                        <button type="submit" class="btn btn-secondary-taupe w-100">Apply</button>
                        <a href="<%= request.getContextPath() %>/view-entries" class="btn btn-outline-taupe w-100">Reset</a>
                    </div>
                </form>
            </div>
        </section>

        <section class="card panel-card">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span>All Entries (Total matches: <%= totalEntries %>)</span>
                <span class="badge bg-soft-taupe">Visible: <span id="visibleCount"><%= entries.size() %></span></span>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table mb-0 align-middle">
                        <thead>
                            <tr>
                                <th>Day</th>
                                <th>Time Slot</th>
                                <th>Subject</th>
                                <th>Teacher</th>
                                <th>Room</th>
                                <th>Batch</th>
                                <th>Semester</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody id="entriesTableBody">
                            <%
                            if (entries.isEmpty()) {
                            %>
                                <tr>
                                    <td colspan="8" class="text-center py-4">No entries found for selected filters.</td>
                                </tr>
                            <%
                            } else {
                                for (TimetableEntry entry : entries) {
                                    String start = entry.getStartTime() != null ? entry.getStartTime().toString().substring(0, 5) : "-";
                                    String end = entry.getEndTime() != null ? entry.getEndTime().toString().substring(0, 5) : "-";
                                    String searchable = (entry.getSubjectName() + " " + entry.getTeacherName() + " " + entry.getDayOfWeek()).toLowerCase();
                            %>
                                <tr data-entry-id="<%= entry.getEntryId() %>"
                                    data-search="<%= searchable %>"
                                    data-day="<%= entry.getDayOfWeek() %>"
                                    data-semester="<%= entry.getSemester() %>">
                                    <td><%= entry.getDayOfWeek() %></td>
                                    <td><%= start %> - <%= end %></td>
                                    <td><strong><%= entry.getSubjectCode() %></strong><br><small><%= entry.getSubjectName() %></small></td>
                                    <td><%= entry.getTeacherName() %></td>
                                    <td><%= entry.getRoomNumber() %></td>
                                    <td><%= entry.getBatchSection() %></td>
                                    <td><%= entry.getSemester() %></td>
                                    <td class="d-flex flex-wrap gap-1">
                                        <a href="<%= request.getContextPath() %>/edit-entry?id=<%= entry.getEntryId() %>" class="btn btn-sm btn-secondary-taupe">Edit</a>
                                        <button type="button" class="btn btn-sm btn-danger-soft btn-delete-entry"
                                            data-bs-toggle="modal" data-bs-target="#deleteConfirmModal"
                                            data-entry-id="<%= entry.getEntryId() %>" data-subject="<%= entry.getSubjectCode() %>">
                                            Delete
                                        </button>
                                    </td>
                                </tr>
                            <%
                                }
                            }
                            %>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="card-footer bg-white border-top">
                <form method="get" action="<%= request.getContextPath() %>/view-entries" class="d-flex justify-content-between align-items-center flex-wrap gap-2">
                    <div class="text-muted">Page <strong><%= currentPage %></strong> of <strong><%= totalPages %></strong></div>

                    <input type="hidden" name="search" value="<%= search %>">
                    <input type="hidden" name="day" value="<%= day %>">
                    <input type="hidden" name="sortBy" value="<%= sortBy %>">
                    <input type="hidden" name="sortDir" value="<%= sortDir %>">
                    <input type="hidden" name="semester" value="<%= semester != null ? semester : "" %>">

                    <div class="d-flex gap-2">
                        <button type="submit" class="btn btn-outline-taupe"
                            name="page" value="<%= currentPage - 1 %>" <%= currentPage <= 1 ? "disabled" : "" %>>Previous</button>
                        <button type="submit" class="btn btn-outline-taupe"
                            name="page" value="<%= currentPage + 1 %>" <%= currentPage >= totalPages ? "disabled" : "" %>>Next</button>
                    </div>
                </form>
            </div>
        </section>
    </main>

    <div class="modal fade" id="deleteConfirmModal" tabindex="-1" aria-labelledby="deleteConfirmModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <form method="post" action="<%= request.getContextPath() %>/delete-entry">
                    <div class="modal-header">
                        <h5 class="modal-title" id="deleteConfirmModalLabel">Delete Confirmation</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <p id="deleteEntryLabel" class="mb-3">Are you sure you want to delete this entry?</p>
                        <input type="hidden" name="entryId" id="deleteEntryId" value="">

                        <label class="form-label" for="deleteMode">Delete Mode</label>
                        <select class="form-select" name="deleteMode" id="deleteMode">
                            <option value="soft" selected>Soft delete (mark inactive)</option>
                            <option value="hard">Hard delete (permanent)</option>
                        </select>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-taupe" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-danger-soft">Confirm Delete</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <jsp:include page="includes/footer.jsp" />

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
        crossorigin="anonymous"></script>
    <script src="<%= request.getContextPath() %>/js/search-filter.js"></script>
</body>
</html>
