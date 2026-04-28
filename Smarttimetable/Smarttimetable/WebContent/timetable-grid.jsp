<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,java.util.Map,java.util.Collections,com.timetable.model.TimetableEntry,com.timetable.model.TimetableSlot" %>
<%!
private String dayLabel(String day) {
    if ("Mon".equals(day)) {
        return "Monday";
    }
    if ("Tue".equals(day)) {
        return "Tuesday";
    }
    if ("Wed".equals(day)) {
        return "Wednesday";
    }
    if ("Thu".equals(day)) {
        return "Thursday";
    }
    if ("Fri".equals(day)) {
        return "Friday";
    }
    if ("Sat".equals(day)) {
        return "Saturday";
    }
    if ("Sun".equals(day)) {
        return "Sunday";
    }
    return day;
}
%>
<%
request.setAttribute("activePage", "grid");

List<String> branches = (List<String>) request.getAttribute("branches");
if (branches == null) {
    branches = Collections.emptyList();
}

String[] days = (String[]) request.getAttribute("days");
if (days == null || days.length == 0) {
    days = new String[] {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
}

String selectedBatch = (String) request.getAttribute("selectedBatch");
if (selectedBatch == null) {
    selectedBatch = "";
}

Integer selectedSemesterObj = (Integer) request.getAttribute("selectedSemester");
String selectedSemester = selectedSemesterObj == null ? "" : String.valueOf(selectedSemesterObj);

String profile = (String) request.getAttribute("profile");
if (profile == null || profile.isEmpty()) {
    profile = "branch";
}

String profileDescription = (String) request.getAttribute("profileDescription");
if (profileDescription == null) {
    profileDescription = "";
}

String gridStartTime = (String) request.getAttribute("gridStartTime");
if (gridStartTime == null || gridStartTime.isEmpty()) {
    gridStartTime = "09:00";
}

Integer slotsPerDayObj = (Integer) request.getAttribute("slotsPerDay");
int slotsPerDay = slotsPerDayObj == null ? 4 : slotsPerDayObj.intValue();

Integer slotDurationObj = (Integer) request.getAttribute("slotDuration");
int slotDuration = slotDurationObj == null ? 60 : slotDurationObj.intValue();

Integer breakDurationObj = (Integer) request.getAttribute("breakDuration");
int breakDuration = breakDurationObj == null ? 15 : breakDurationObj.intValue();

Integer sourceEntryCountObj = (Integer) request.getAttribute("sourceEntryCount");
int sourceEntryCount = sourceEntryCountObj == null ? 0 : sourceEntryCountObj.intValue();

List<TimetableSlot> slots = (List<TimetableSlot>) request.getAttribute("slots");
if (slots == null) {
    slots = Collections.emptyList();
}

Map<String, TimetableEntry> cellMap = (Map<String, TimetableEntry>) request.getAttribute("cellMap");
if (cellMap == null) {
    cellMap = Collections.emptyMap();
}

List<TimetableEntry> unplacedEntries = (List<TimetableEntry>) request.getAttribute("unplacedEntries");
if (unplacedEntries == null) {
    unplacedEntries = Collections.emptyList();
}

List<String> collisionWarnings = (List<String>) request.getAttribute("collisionWarnings");
if (collisionWarnings == null) {
    collisionWarnings = Collections.emptyList();
}
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Timetable Grid | Smart Timetable Generator</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Lato:wght@400;600;700&family=Open+Sans:wght@400;600&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
        integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/custom-taupe.css">
</head>
<body class="app-page">
    <jsp:include page="includes/header.jsp" />

    <main class="container-fluid app-container px-3 px-lg-4">
        <section class="mb-3">
            <h1 class="mb-1">Timetable Grid View</h1>
            <p class="page-subtitle mb-0">Grid adjusts dynamically by branch profile, industry profile, and slots per day.</p>
        </section>

        <section class="card panel-card mb-4">
            <div class="card-body">
                <form method="get" action="<%= request.getContextPath() %>/timetable-grid" class="row g-3 align-items-end">
                    <div class="col-md-2">
                        <label class="form-label" for="batchSection">Branch / Batch</label>
                        <select class="form-select" id="batchSection" name="batchSection">
                            <option value="">All</option>
                            <%
                            for (String branch : branches) {
                            %>
                                <option value="<%= branch %>" <%= branch.equals(selectedBatch) ? "selected" : "" %>><%= branch %></option>
                            <%
                            }
                            %>
                        </select>
                    </div>

                    <div class="col-md-2">
                        <label class="form-label" for="semester">Semester</label>
                        <select class="form-select" id="semester" name="semester">
                            <option value="">All</option>
                            <%
                            for (int i = 1; i <= 8; i++) {
                            %>
                                <option value="<%= i %>" <%= String.valueOf(i).equals(selectedSemester) ? "selected" : "" %>><%= i %></option>
                            <%
                            }
                            %>
                        </select>
                    </div>

                    <div class="col-md-2">
                        <label class="form-label" for="profile">Profile Type</label>
                        <select class="form-select" id="profile" name="profile">
                            <option value="branch" <%= "branch".equals(profile) ? "selected" : "" %>>Branch Standard</option>
                            <option value="industry" <%= "industry".equals(profile) ? "selected" : "" %>>Industry Relevant</option>
                        </select>
                    </div>

                    <div class="col-md-1">
                        <label class="form-label" for="slotsPerDay">Slots/Day</label>
                        <input type="number" min="1" max="12" class="form-control" id="slotsPerDay" name="slotsPerDay" value="<%= slotsPerDay %>">
                    </div>

                    <div class="col-md-2">
                        <label class="form-label" for="gridStartTime">Grid Start</label>
                        <input type="time" class="form-control" id="gridStartTime" name="gridStartTime" value="<%= gridStartTime %>">
                    </div>

                    <div class="col-md-1">
                        <label class="form-label" for="slotDuration">Slot Min</label>
                        <input type="number" min="30" max="180" class="form-control" id="slotDuration" name="slotDuration" value="<%= slotDuration %>">
                    </div>

                    <div class="col-md-1">
                        <label class="form-label" for="breakDuration">Break Min</label>
                        <input type="number" min="0" max="60" class="form-control" id="breakDuration" name="breakDuration" value="<%= breakDuration %>">
                    </div>

                    <div class="col-md-1 d-grid gap-2">
                        <button type="submit" class="btn btn-primary-taupe">Apply</button>
                    </div>

                    <div class="col-md-2 d-grid gap-2">
                        <button type="button" class="btn btn-secondary-taupe" onclick="downloadGridPdf()">Download PDF</button>
                    </div>
                </form>

                <div class="mt-3 small text-muted">
                    <strong>Profile:</strong> <%= profileDescription %><br>
                    <strong>Entries loaded:</strong> <%= sourceEntryCount %>
                </div>
            </div>
        </section>

        <%
        if (!collisionWarnings.isEmpty()) {
        %>
            <section class="card panel-card mb-3">
                <div class="card-header">Grid Warnings</div>
                <div class="card-body">
                    <%
                    for (String warning : collisionWarnings) {
                    %>
                        <div class="alert alert-warning py-2 mb-2"><%= warning %></div>
                    <%
                    }
                    %>
                </div>
            </section>
        <%
        }
        %>

        <section class="card panel-card mb-4">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span>Weekly Timetable Grid</span>
                <span class="badge bg-soft-taupe">Slots: <%= slotsPerDay %></span>
            </div>

            <div class="card-body p-0">
                <div class="table-responsive">
                    <table id="gridTable" class="table timetable-grid-table mb-0 align-middle text-center">
                        <thead>
                            <tr>
                                <th>Time</th>
                                <%
                                for (String day : days) {
                                %>
                                    <th><%= dayLabel(day) %></th>
                                <%
                                }
                                %>
                            </tr>
                        </thead>
                        <tbody>
                            <%
                            for (TimetableSlot slot : slots) {
                            %>
                                <tr>
                                    <th class="grid-time-col"><%= slot.getLabel() %></th>
                                    <%
                                    for (String day : days) {
                                        String key = day + "_" + slot.getIndex();
                                        TimetableEntry entry = (TimetableEntry) cellMap.get(key);
                                    %>
                                        <td class="timetable-grid-cell">
                                            <%
                                            if (entry != null) {
                                            %>
                                                <div class="grid-subject"><%= entry.getSubjectCode() %></div>
                                                <div class="grid-title"><%= entry.getSubjectName() %></div>
                                                <div class="grid-meta"><%= entry.getTeacherName() %></div>
                                                <div class="grid-meta"><%= entry.getRoomNumber() %></div>
                                                <div class="grid-meta">Sem <%= entry.getSemester() %> | <%= entry.getBatchSection() %></div>
                                            <%
                                            }
                                            %>
                                        </td>
                                    <%
                                    }
                                    %>
                                </tr>
                            <%
                            }
                            %>
                        </tbody>
                    </table>
                </div>
            </div>
        </section>

        <%
        if (!unplacedEntries.isEmpty()) {
        %>
            <section class="card panel-card mb-4">
                <div class="card-header">Entries Outside Current Grid Slots</div>
                <div class="card-body">
                    <p class="small text-muted">These entries were not mapped because their times do not fit current grid slot settings.</p>
                    <div class="table-responsive">
                        <table class="table mb-0 align-middle">
                            <thead>
                                <tr>
                                    <th>Day</th>
                                    <th>Time</th>
                                    <th>Subject</th>
                                    <th>Teacher</th>
                                    <th>Room</th>
                                </tr>
                            </thead>
                            <tbody>
                                <%
                                for (TimetableEntry entry : unplacedEntries) {
                                    String start = entry.getStartTime() != null ? entry.getStartTime().toString().substring(0, 5) : "-";
                                    String end = entry.getEndTime() != null ? entry.getEndTime().toString().substring(0, 5) : "-";
                                %>
                                    <tr>
                                        <td><%= entry.getDayOfWeek() %></td>
                                        <td><%= start %> - <%= end %></td>
                                        <td><%= entry.getSubjectCode() %> - <%= entry.getSubjectName() %></td>
                                        <td><%= entry.getTeacherName() %></td>
                                        <td><%= entry.getRoomNumber() %></td>
                                    </tr>
                                <%
                                }
                                %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>
        <%
        }
        %>
    </main>

    <jsp:include page="includes/footer.jsp" />

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
        crossorigin="anonymous"></script>
    <script src="https://cdn.jsdelivr.net/npm/jspdf@2.5.1/dist/jspdf.umd.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/jspdf-autotable@3.8.2/dist/jspdf.plugin.autotable.min.js"></script>
    <script>
        function downloadGridPdf() {
            const jsPDF = window.jspdf.jsPDF;
            const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });

            const batch = document.getElementById('batchSection').value || 'All';
            const semester = document.getElementById('semester').value || 'All';
            const profile = document.getElementById('profile').value || 'branch';

            doc.setFontSize(14);
            doc.text('Smart Timetable Grid', 40, 35);
            doc.setFontSize(10);
            doc.text('Batch: ' + batch + ' | Semester: ' + semester + ' | Profile: ' + profile, 40, 52);

            doc.autoTable({
                html: '#gridTable',
                startY: 65,
                theme: 'grid',
                styles: {
                    fontSize: 8,
                    cellPadding: 4,
                    overflow: 'linebreak'
                },
                headStyles: {
                    fillColor: [72, 60, 50],
                    textColor: [255, 255, 255]
                },
                alternateRowStyles: {
                    fillColor: [245, 240, 235]
                }
            });

            const safeBatch = batch.replace(/\s+/g, '-');
            doc.save('timetable-grid-' + safeBatch + '-sem' + semester + '.pdf');
        }
    </script>
</body>
</html>
