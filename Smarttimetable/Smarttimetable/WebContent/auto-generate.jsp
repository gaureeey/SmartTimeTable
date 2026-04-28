<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,java.util.Collections,com.timetable.model.TimetableEntry" %>
<%
request.setAttribute("activePage", "ai");
String successMessage = (String) request.getAttribute("successMessage");
String errorMessage = (String) request.getAttribute("errorMessage");
String model = (String) request.getAttribute("model");
String defaultModel = (String) request.getAttribute("defaultModel");
String semester = (String) request.getAttribute("semester");
String batchSection = (String) request.getAttribute("batchSection");
String days = (String) request.getAttribute("days");
String startTime = (String) request.getAttribute("startTime");
String slotsPerDay = (String) request.getAttribute("slotsPerDay");
String slotDuration = (String) request.getAttribute("slotDuration");
String breakDuration = (String) request.getAttribute("breakDuration");
String subjects = (String) request.getAttribute("subjects");
String additionalInstructions = (String) request.getAttribute("additionalInstructions");
String rawResponse = (String) request.getAttribute("rawResponse");

List<TimetableEntry> generatedEntries = (List<TimetableEntry>) request.getAttribute("generatedEntries");
if (generatedEntries == null) {
    generatedEntries = Collections.emptyList();
}

List<String> parseWarnings = (List<String>) request.getAttribute("parseWarnings");
if (parseWarnings == null) {
    parseWarnings = Collections.emptyList();
}

List<String> insertWarnings = (List<String>) request.getAttribute("insertWarnings");
if (insertWarnings == null) {
    insertWarnings = Collections.emptyList();
}

if (model == null || model.isEmpty()) {
    model = defaultModel != null ? defaultModel : "llama2:7b";
}
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>AI Auto Generate | Smart Timetable Generator</title>
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
        <section class="mb-4">
            <h1 class="mb-1">AI Timetable Generation</h1>
            <p class="page-subtitle mb-0">Generate timetable entries using your local Ollama 7B model and insert them automatically.</p>
        </section>

        <section class="card panel-card mb-4">
            <div class="card-body">
                <%
                if (successMessage != null) {
                %>
                    <div class="alert alert-success" role="alert"><%= successMessage %></div>
                <%
                }
                if (errorMessage != null) {
                %>
                    <div class="alert alert-danger" role="alert"><%= errorMessage %></div>
                <%
                }
                %>

                <form method="post" action="<%= request.getContextPath() %>/auto-generate" class="row g-3">
                    <div class="col-md-4">
                        <label class="form-label" for="model">Ollama Model</label>
                        <input type="text" class="form-control" id="model" name="model" value="<%= model %>" required>
                        <small class="text-muted">Example: llama2:7b, mistral:7b</small>
                    </div>

                    <div class="col-md-2">
                        <label class="form-label" for="semester">Semester</label>
                        <select class="form-select" id="semester" name="semester" required>
                            <%
                            for (int i = 1; i <= 8; i++) {
                                String selected = String.valueOf(i).equals(semester) ? "selected" : "";
                            %>
                                <option value="<%= i %>" <%= selected %>><%= i %></option>
                            <%
                            }
                            %>
                        </select>
                    </div>

                    <div class="col-md-3">
                        <label class="form-label" for="batchSection">Batch/Section</label>
                        <input type="text" class="form-control" id="batchSection" name="batchSection" value="<%= batchSection %>" required>
                    </div>

                    <div class="col-md-3">
                        <label class="form-label" for="days">Days (comma separated)</label>
                        <input type="text" class="form-control" id="days" name="days" value="<%= days %>" required>
                    </div>

                    <div class="col-md-3">
                        <label class="form-label" for="startTime">Start Time</label>
                        <input type="time" class="form-control" id="startTime" name="startTime" value="<%= startTime %>" required>
                    </div>

                    <div class="col-md-3">
                        <label class="form-label" for="slotsPerDay">Slots/Day</label>
                        <input type="number" min="1" max="10" class="form-control" id="slotsPerDay" name="slotsPerDay" value="<%= slotsPerDay %>" required>
                    </div>

                    <div class="col-md-3">
                        <label class="form-label" for="slotDuration">Slot Duration (min)</label>
                        <input type="number" min="30" max="180" class="form-control" id="slotDuration" name="slotDuration" value="<%= slotDuration %>" required>
                    </div>

                    <div class="col-md-3">
                        <label class="form-label" for="breakDuration">Break Duration (min)</label>
                        <input type="number" min="0" max="60" class="form-control" id="breakDuration" name="breakDuration" value="<%= breakDuration %>" required>
                    </div>

                    <div class="col-12">
                        <label class="form-label" for="subjects">Subjects (one per line)</label>
                        <textarea class="form-control" id="subjects" name="subjects" rows="6" required><%= subjects %></textarea>
                        <small class="text-muted">Format: Subject Name|Subject Code|Teacher Name|Room|WeeklySessions</small>
                    </div>

                    <div class="col-12">
                        <label class="form-label" for="additionalInstructions">Additional Instructions (optional)</label>
                        <textarea class="form-control" id="additionalInstructions" name="additionalInstructions" rows="3"><%= additionalInstructions %></textarea>
                    </div>

                    <div class="col-12 d-flex gap-2">
                        <button type="submit" class="btn btn-primary-taupe">Generate & Insert Timetable</button>
                        <a href="<%= request.getContextPath() %>/timetable-grid?batchSection=<%= batchSection %>&semester=<%= semester %>&profile=industry" class="btn btn-secondary-taupe">Open Grid View</a>
                        <a href="<%= request.getContextPath() %>/view-entries" class="btn btn-outline-taupe">View Entries</a>
                    </div>
                </form>
            </div>
        </section>

        <%
        if (!parseWarnings.isEmpty() || !insertWarnings.isEmpty()) {
        %>
            <section class="card panel-card mb-4">
                <div class="card-header">Generation Warnings</div>
                <div class="card-body">
                    <%
                    for (String warning : parseWarnings) {
                    %>
                        <div class="alert alert-warning py-2 mb-2"><%= warning %></div>
                    <%
                    }
                    for (String warning : insertWarnings) {
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

        <%
        if (!generatedEntries.isEmpty()) {
        %>
            <section class="card panel-card mb-4">
                <div class="card-header">Generated Entries Preview</div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table mb-0 align-middle">
                            <thead>
                                <tr>
                                    <th>Day</th>
                                    <th>Time</th>
                                    <th>Subject</th>
                                    <th>Teacher</th>
                                    <th>Room</th>
                                    <th>Batch</th>
                                    <th>Semester</th>
                                </tr>
                            </thead>
                            <tbody>
                                <%
                                for (TimetableEntry entry : generatedEntries) {
                                    String start = entry.getStartTime() != null ? entry.getStartTime().toString().substring(0, 5) : "-";
                                    String end = entry.getEndTime() != null ? entry.getEndTime().toString().substring(0, 5) : "-";
                                %>
                                    <tr>
                                        <td><%= entry.getDayOfWeek() %></td>
                                        <td><%= start %> - <%= end %></td>
                                        <td><strong><%= entry.getSubjectCode() %></strong><br><small><%= entry.getSubjectName() %></small></td>
                                        <td><%= entry.getTeacherName() %></td>
                                        <td><%= entry.getRoomNumber() %></td>
                                        <td><%= entry.getBatchSection() %></td>
                                        <td><%= entry.getSemester() %></td>
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

        <%
        if (rawResponse != null && !rawResponse.isEmpty()) {
        %>
            <section class="card panel-card mb-4">
                <div class="card-header">Raw Ollama Response</div>
                <div class="card-body">
                    <pre class="raw-ollama-output"><%= rawResponse %></pre>
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
</body>
</html>
