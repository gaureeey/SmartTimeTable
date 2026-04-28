<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.timetable.model.DashboardStats" %>
<%
request.setAttribute("activePage", "dashboard");
DashboardStats stats = (DashboardStats) request.getAttribute("stats");
if (stats == null) {
    stats = new DashboardStats();
}
int totalEntries = stats.getTotalEntries();
int entriesToday = stats.getEntriesToday();
int distinctSubjects = stats.getDistinctSubjects();
int distinctTeachers = stats.getDistinctTeachers();
int totalUsers = stats.getTotalUsers();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Dashboard | Smart Timetable Generator</title>
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
            <h1 class="mb-1">Dashboard</h1>
            <p class="page-subtitle mb-0">Monitor your timetable activity and access quick actions.</p>
        </section>

        <section class="row g-3 mb-4">
            <div class="col-6 col-lg-3">
                <button type="button" class="card summary-card summary-card-action is-active h-100 w-100 text-start" data-metric="total" data-label="Total Entries" data-value="<%= totalEntries %>" data-description="All active timetable entries currently stored.">
                    <div class="card-body">
                        <p class="summary-label">Total Entries</p>
                        <p class="summary-value js-stat-value" data-target="<%= totalEntries %>"><%= totalEntries %></p>
                    </div>
                </button>
            </div>
            <div class="col-6 col-lg-3">
                <button type="button" class="card summary-card summary-card-action h-100 w-100 text-start" data-metric="today" data-label="Entries Today" data-value="<%= entriesToday %>" data-description="Classes planned for today based on weekday mapping.">
                    <div class="card-body">
                        <p class="summary-label">Entries Today</p>
                        <p class="summary-value js-stat-value" data-target="<%= entriesToday %>"><%= entriesToday %></p>
                    </div>
                </button>
            </div>
            <div class="col-6 col-lg-3">
                <button type="button" class="card summary-card summary-card-action h-100 w-100 text-start" data-metric="subjects" data-label="Subjects" data-value="<%= distinctSubjects %>" data-description="Distinct subject codes available across active entries.">
                    <div class="card-body">
                        <p class="summary-label">Subjects</p>
                        <p class="summary-value js-stat-value" data-target="<%= distinctSubjects %>"><%= distinctSubjects %></p>
                    </div>
                </button>
            </div>
            <div class="col-6 col-lg-3">
                <button type="button" class="card summary-card summary-card-action h-100 w-100 text-start" data-metric="users" data-label="Registered Users" data-value="<%= totalUsers %>" data-description="Accounts currently registered on this website.">
                    <div class="card-body">
                        <p class="summary-label">Users</p>
                        <p class="summary-value js-stat-value" data-target="<%= totalUsers %>"><%= totalUsers %></p>
                    </div>
                </button>
            </div>
        </section>

        <section class="card panel-card mb-4 dashboard-command-card">
            <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
                <span>Dashboard Command Center</span>
                <div class="btn-group btn-group-sm command-view-switch" role="group" aria-label="Dashboard view modes">
                    <button type="button" class="btn btn-outline-taupe dashboard-view-btn active" data-dashboard-view="overview">Overview</button>
                    <button type="button" class="btn btn-outline-taupe dashboard-view-btn" data-dashboard-view="planning">Planning</button>
                    <button type="button" class="btn btn-outline-taupe dashboard-view-btn" data-dashboard-view="automation">Automation</button>
                </div>
            </div>
            <div class="card-body">
                <div class="row g-3">
                    <div class="col-lg-6">
                        <div class="command-focus-shell">
                            <p class="command-kicker mb-1">Current Metric</p>
                            <h2 class="h5 mb-1" id="metricFocusTitle">Total Entries</h2>
                            <p class="command-focus-value mb-1" id="metricFocusValue"><%= totalEntries %></p>
                            <p class="command-focus-text mb-0" id="metricFocusText">All active timetable entries currently stored.</p>
                        </div>

                        <div class="command-chip-grid mt-3">
                            <button type="button" class="command-metric-chip active" data-metric="total" data-label="Total Entries" data-value="<%= totalEntries %>" data-description="All active timetable entries currently stored.">Total Entries</button>
                            <button type="button" class="command-metric-chip" data-metric="today" data-label="Entries Today" data-value="<%= entriesToday %>" data-description="Classes planned for today based on weekday mapping.">Entries Today</button>
                            <button type="button" class="command-metric-chip" data-metric="subjects" data-label="Subjects" data-value="<%= distinctSubjects %>" data-description="Distinct subject codes available across active entries.">Subjects</button>
                            <button type="button" class="command-metric-chip" data-metric="users" data-label="Registered Users" data-value="<%= totalUsers %>" data-description="Accounts currently registered on this website.">Users</button>
                        </div>
                    </div>

                    <div class="col-lg-6">
                        <div class="mission-panel">
                            <p class="command-kicker mb-2">Suggested Actions</p>
                            <ul class="mission-list list-unstyled mb-0" id="dashboardMissionList">
                                <li class="mission-item">
                                    <a href="<%= request.getContextPath() %>/view-entries" class="mission-link">Review all timetable entries</a>
                                    <p class="mission-note mb-0">Use your current timetable as the baseline for updates.</p>
                                </li>
                                <li class="mission-item">
                                    <a href="<%= request.getContextPath() %>/add-entry" class="mission-link">Add new timetable entries</a>
                                    <p class="mission-note mb-0">Fill gaps for sessions that are still missing.</p>
                                </li>
                                <li class="mission-item">
                                    <a href="<%= request.getContextPath() %>/auto-generate" class="mission-link">Generate a draft with AI</a>
                                    <p class="mission-note mb-0">Create a quick schedule draft and refine it manually.</p>
                                </li>
                            </ul>
                        </div>

                        <div class="command-pulse mt-3">
                            <div class="pulse-row">
                                <span>Attention Level</span>
                                <strong id="attentionValue">0%</strong>
                            </div>
                            <div class="command-pulse-track">
                                <span class="command-pulse-fill" id="attentionFill" style="width: 0%;"></span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <section class="card panel-card mb-4">
            <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
                <span>Quick Actions</span>
                <span class="small">Visible: <strong id="quickActionVisibleCount">5</strong></span>
            </div>
            <div class="card-body border-bottom py-2">
                <div class="d-flex flex-wrap gap-2" id="quickActionFilters">
                    <button type="button" class="btn btn-sm btn-outline-taupe quick-filter-btn active" data-action-filter="all">All</button>
                    <button type="button" class="btn btn-sm btn-outline-taupe quick-filter-btn" data-action-filter="manage">Manage</button>
                    <button type="button" class="btn btn-sm btn-outline-taupe quick-filter-btn" data-action-filter="review">Review</button>
                    <button type="button" class="btn btn-sm btn-outline-taupe quick-filter-btn" data-action-filter="ai">AI</button>
                </div>
            </div>
            <div class="card-body d-flex flex-wrap gap-2" id="quickActionContainer">
                <a id="actionAddEntry" class="btn btn-primary-taupe quick-action-link" data-action-group="manage" href="<%= request.getContextPath() %>/add-entry">Add New Entry</a>
                <a id="actionViewEntries" class="btn btn-secondary-taupe quick-action-link" data-action-group="manage review" href="<%= request.getContextPath() %>/view-entries">View All Entries</a>
                <a id="actionSearch" class="btn btn-outline-taupe quick-action-link" data-action-group="review" href="<%= request.getContextPath() %>/view-entries">Search Timetable</a>
                <a id="actionGrid" class="btn btn-outline-taupe quick-action-link" data-action-group="review" href="<%= request.getContextPath() %>/timetable-grid">Grid View</a>
                <a id="actionAi" class="btn btn-outline-taupe quick-action-link" data-action-group="ai manage" href="<%= request.getContextPath() %>/auto-generate">AI Auto Generate</a>
            </div>
        </section>

        <section class="card panel-card site-info-card">
            <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
                <span>Website Information & Data Reference</span>
                <div class="btn-group btn-group-sm" role="group" aria-label="Website info tabs">
                    <button type="button" class="btn btn-outline-taupe info-tab-btn active" data-info-target="overview">Overview</button>
                    <button type="button" class="btn btn-outline-taupe info-tab-btn" data-info-target="data">Data Model</button>
                    <button type="button" class="btn btn-outline-taupe info-tab-btn" data-info-target="technology">Technology</button>
                </div>
            </div>
            <div class="card-body">
                <div class="info-panel" data-info-panel="overview">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <div class="site-info-item">
                                <h3 class="h6">What This Website Does</h3>
                                <ul class="mb-0">
                                    <li>Create, update, and manage weekly timetable entries.</li>
                                    <li>Run fast search and filter by day, semester, teacher, and subject.</li>
                                    <li>Open timetable in grid mode for planning review.</li>
                                    <li>Generate timetable drafts with AI-assisted auto generation.</li>
                                </ul>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="site-info-item">
                                <h3 class="h6">Current Dataset Snapshot</h3>
                                <ul class="mb-0">
                                    <li>Total active entries: <strong><%= totalEntries %></strong></li>
                                    <li>Entries mapped to today: <strong><%= entriesToday %></strong></li>
                                    <li>Distinct subjects: <strong><%= distinctSubjects %></strong></li>
                                    <li>Registered users: <strong><%= totalUsers %></strong></li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="info-panel d-none" data-info-panel="data">
                    <div class="table-responsive">
                        <table class="table mb-0 align-middle site-info-table">
                            <thead>
                                <tr>
                                    <th scope="col">Entity</th>
                                    <th scope="col">Purpose</th>
                                    <th scope="col">Current Indicator</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>Users</td>
                                    <td>Stores authenticated accounts for access control.</td>
                                    <td><%= totalUsers %> registered accounts</td>
                                </tr>
                                <tr>
                                    <td>Timetable Entries</td>
                                    <td>Stores class schedules with day, time, subject, teacher, and room.</td>
                                    <td><%= totalEntries %> active records</td>
                                </tr>
                                <tr>
                                    <td>Subjects Catalog</td>
                                    <td>Distinct subject coverage available in current plan.</td>
                                    <td><%= distinctSubjects %> subjects</td>
                                </tr>
                                <tr>
                                    <td>Teachers Catalog</td>
                                    <td>Distinct teacher assignments represented in timetable data.</td>
                                    <td><%= distinctTeachers %> teachers</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>

                <div class="info-panel d-none" data-info-panel="technology">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <div class="site-info-item">
                                <h3 class="h6">Architecture</h3>
                                <ul class="mb-0">
                                    <li>Java MVC with Servlets and JSP views.</li>
                                    <li>MySQL database with JDBC data access objects.</li>
                                    <li>Bootstrap-based responsive user interface.</li>
                                    <li>Session-based authentication with timeout controls.</li>
                                </ul>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="site-info-item">
                                <h3 class="h6">User Workflow</h3>
                                <ol class="mb-0 ps-3">
                                    <li>Sign in or create an account.</li>
                                    <li>Manage entries from Add and View pages.</li>
                                    <li>Validate schedule in grid mode.</li>
                                    <li>Use AI generation to speed up draft creation.</li>
                                </ol>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </main>

    <jsp:include page="includes/footer.jsp" />

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
        crossorigin="anonymous"></script>
    <script src="<%= request.getContextPath() %>/js/dashboard-interactions.js"></script>
</body>
</html>
