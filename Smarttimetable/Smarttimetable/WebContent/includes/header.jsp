<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
String username = (String) session.getAttribute("username");
String fullName = (String) session.getAttribute("fullName");
if (username == null) {
    response.sendRedirect(request.getContextPath() + "/login?error=session");
    return;
}
String activePage = (String) request.getAttribute("activePage");
if (activePage == null) {
    activePage = "";
}
%>
<nav class="navbar navbar-expand-lg navbar-dark fixed-top taupe-navbar">
    <div class="container">
        <a class="navbar-brand fw-bold" href="<%= request.getContextPath() %>/dashboard">Smart Timetable Generator</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNavbar"
            aria-controls="mainNavbar" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="mainNavbar">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item">
                    <a class="nav-link <%= "dashboard".equals(activePage) ? "active" : "" %>" href="<%= request.getContextPath() %>/dashboard">Dashboard</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <%= "add".equals(activePage) ? "active" : "" %>" href="<%= request.getContextPath() %>/add-entry">Add Entry</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <%= "view".equals(activePage) ? "active" : "" %>" href="<%= request.getContextPath() %>/view-entries">View Entries</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <%= "grid".equals(activePage) ? "active" : "" %>" href="<%= request.getContextPath() %>/timetable-grid">Grid View</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <%= "ai".equals(activePage) ? "active" : "" %>" href="<%= request.getContextPath() %>/auto-generate">Auto Generate</a>
                </li>
            </ul>

            <div class="d-flex align-items-center gap-3">
                <span class="navbar-text">Signed in as: <strong><%= fullName != null ? fullName : username %></strong></span>
                <a href="<%= request.getContextPath() %>/logout" class="btn btn-outline-light rounded-pill px-3">Logout</a>
            </div>
        </div>
    </div>
</nav>
