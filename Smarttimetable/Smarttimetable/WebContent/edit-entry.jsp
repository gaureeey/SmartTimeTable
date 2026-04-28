<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.timetable.model.TimetableEntry" %>
<%
request.setAttribute("activePage", "view");
TimetableEntry entry = (TimetableEntry) request.getAttribute("entry");
String errorMessage = (String) request.getAttribute("errorMessage");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Edit Entry | Smart Timetable Generator</title>
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
            <h1 class="mb-1">Edit Timetable Entry</h1>
            <p class="page-subtitle mb-0">Update details and save changes.</p>
        </section>

        <section class="card panel-card">
            <div class="card-body p-4">
                <%
                if (entry == null) {
                %>
                    <div class="alert alert-danger" role="alert">
                        Entry data is unavailable.
                    </div>
                    <a href="<%= request.getContextPath() %>/view-entries" class="btn btn-outline-taupe">Back to Entries</a>
                <%
                } else {
                    String startTime = entry.getStartTime() != null ? entry.getStartTime().toString().substring(0, 5) : "";
                    String endTime = entry.getEndTime() != null ? entry.getEndTime().toString().substring(0, 5) : "";
                %>

                <%
                if (errorMessage != null) {
                %>
                    <div class="alert alert-danger" role="alert"><%= errorMessage %></div>
                <%
                }
                %>

                <form action="<%= request.getContextPath() %>/update-entry" method="post" class="needs-validation-custom" novalidate>
                    <input type="hidden" name="entryId" value="<%= entry.getEntryId() %>">

                    <div class="row g-3">
                        <div class="col-md-4">
                            <label class="form-label" for="dayOfWeek">Day of Week <span class="required">*</span></label>
                            <select class="form-select" id="dayOfWeek" name="dayOfWeek" required>
                                <option value="">Select Day</option>
                                <%
                                String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                                for (String day : days) {
                                %>
                                    <option value="<%= day %>" <%= day.equals(entry.getDayOfWeek()) ? "selected" : "" %>><%= day %></option>
                                <%
                                }
                                %>
                            </select>
                            <div class="invalid-feedback">Please select day of week.</div>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label" for="startTime">Start Time <span class="required">*</span></label>
                            <input type="time" class="form-control" id="startTime" name="startTime" required value="<%= startTime %>">
                            <div class="invalid-feedback">Please provide start time.</div>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label" for="endTime">End Time <span class="required">*</span></label>
                            <input type="time" class="form-control" id="endTime" name="endTime" required value="<%= endTime %>">
                            <div class="invalid-feedback">Please provide valid end time.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label" for="subjectName">Subject Name <span class="required">*</span></label>
                            <input type="text" class="form-control" id="subjectName" name="subjectName" required maxlength="100"
                                value="<%= entry.getSubjectName() %>">
                            <div class="invalid-feedback">Subject name is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label" for="subjectCode">Subject Code <span class="required">*</span></label>
                            <input type="text" class="form-control" id="subjectCode" name="subjectCode" required maxlength="20"
                                pattern="[A-Za-z0-9-]{2,20}" value="<%= entry.getSubjectCode() %>">
                            <div class="invalid-feedback">Use 2-20 characters (A-Z, 0-9, -).</div>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label" for="teacherName">Teacher Name <span class="required">*</span></label>
                            <input type="text" class="form-control" id="teacherName" name="teacherName" required maxlength="100"
                                value="<%= entry.getTeacherName() %>">
                            <div class="invalid-feedback">Teacher name is required.</div>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label" for="roomNumber">Room/Lab Number <span class="required">*</span></label>
                            <input type="text" class="form-control" id="roomNumber" name="roomNumber" required maxlength="20"
                                value="<%= entry.getRoomNumber() %>">
                            <div class="invalid-feedback">Room/Lab number is required.</div>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label" for="batchSection">Batch/Section <span class="required">*</span></label>
                            <input type="text" class="form-control" id="batchSection" name="batchSection" required maxlength="20"
                                value="<%= entry.getBatchSection() %>">
                            <div class="invalid-feedback">Batch/Section is required.</div>
                        </div>

                        <div class="col-md-3">
                            <label class="form-label" for="semester">Semester <span class="required">*</span></label>
                            <select class="form-select" id="semester" name="semester" required>
                                <option value="">Select Semester</option>
                                <%
                                for (int i = 1; i <= 8; i++) {
                                %>
                                    <option value="<%= i %>" <%= i == entry.getSemester() ? "selected" : "" %>><%= i %></option>
                                <%
                                }
                                %>
                            </select>
                            <div class="invalid-feedback">Please choose semester.</div>
                        </div>
                    </div>

                    <div class="mt-4 d-flex gap-2">
                        <button type="submit" class="btn btn-primary-taupe px-4">Update Entry</button>
                        <a href="<%= request.getContextPath() %>/view-entries" class="btn btn-outline-taupe px-4">Cancel</a>
                    </div>
                </form>
                <%
                }
                %>
            </div>
        </section>
    </main>

    <jsp:include page="includes/footer.jsp" />

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
        crossorigin="anonymous"></script>
    <script src="<%= request.getContextPath() %>/js/validation.js"></script>
</body>
</html>
