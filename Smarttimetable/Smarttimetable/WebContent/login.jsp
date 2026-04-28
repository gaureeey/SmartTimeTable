<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.timetable.model.DashboardStats" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Login | Smart Timetable Generator</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Lato:wght@400;600;700&family=Open+Sans:wght@400;600&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
        integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/custom-taupe.css">
</head>
<body class="login-page">
    <%
    String errorMessage = (String) request.getAttribute("errorMessage");
    String signupErrorMessage = (String) request.getAttribute("signupErrorMessage");
    String showSignup = (String) request.getAttribute("showSignup");
    String error = request.getParameter("error");
    String logout = request.getParameter("logout");
    String tab = request.getParameter("tab");
    boolean openSignup = "1".equals(showSignup) || signupErrorMessage != null || "signup".equalsIgnoreCase(tab);

    DashboardStats previewStats = (DashboardStats) request.getAttribute("previewStats");
    int previewTotalEntries = previewStats != null ? previewStats.getTotalEntries() : 42;
    int previewEntriesToday = previewStats != null ? previewStats.getEntriesToday() : 8;
    int previewSubjects = previewStats != null ? previewStats.getDistinctSubjects() : 14;
    int previewTotalUsers = previewStats != null ? previewStats.getTotalUsers() : 1;
    %>

    <div class="login-shell login-shell-wide">
        <div class="row g-4 align-items-stretch login-layout">
            <div class="col-lg-6">
                <section class="card login-preview-card h-100">
                    <div class="card-body p-4 p-md-5 text-start">
                        <p class="preview-kicker mb-2">Quick Snapshot</p>
                        <h1 class="app-title mb-2">Smart Timetable Generator</h1>
                        <p class="page-subtitle mb-4">Explore a live dashboard preview before signing in.</p>

                        <div class="row g-3 mb-4">
                            <div class="col-6">
                                <button type="button" class="preview-stat-card w-100 active" data-label="Total Entries" data-value="<%= previewTotalEntries %>" data-description="All timetable records currently available.">
                                    <span class="preview-stat-label">Total Entries</span>
                                    <span class="preview-stat-value" data-preview-target="<%= previewTotalEntries %>"><%= previewTotalEntries %></span>
                                </button>
                            </div>
                            <div class="col-6">
                                <button type="button" class="preview-stat-card w-100" data-label="Entries Today" data-value="<%= previewEntriesToday %>" data-description="Classes planned for the current day.">
                                    <span class="preview-stat-label">Entries Today</span>
                                    <span class="preview-stat-value" data-preview-target="<%= previewEntriesToday %>"><%= previewEntriesToday %></span>
                                </button>
                            </div>
                            <div class="col-6">
                                <button type="button" class="preview-stat-card w-100" data-label="Subjects" data-value="<%= previewSubjects %>" data-description="Unique subjects in this timetable setup.">
                                    <span class="preview-stat-label">Subjects</span>
                                    <span class="preview-stat-value" data-preview-target="<%= previewSubjects %>"><%= previewSubjects %></span>
                                </button>
                            </div>
                            <div class="col-6">
                                <button type="button" class="preview-stat-card w-100" data-label="Registered Users" data-value="<%= previewTotalUsers %>" data-description="Accounts currently registered on this website.">
                                    <span class="preview-stat-label">Users</span>
                                    <span class="preview-stat-value" data-preview-target="<%= previewTotalUsers %>"><%= previewTotalUsers %></span>
                                </button>
                            </div>
                        </div>

                        <div class="preview-focus-box mb-4">
                            <p class="preview-focus-label mb-1">Selected Metric</p>
                            <p class="preview-focus-value mb-0" id="previewFocusText">Total Entries: <%= previewTotalEntries %>. All timetable records currently available.</p>
                        </div>

                        <div class="preview-activity-panel">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <h3 class="h6 mb-0">Platform Highlights</h3>
                                <span class="badge bg-soft-taupe">Website Info</span>
                            </div>

                            <ul class="preview-activity-list list-unstyled mb-0" id="previewActivityList">
                                <li>Manage weekly timetables with duplicate slot protection.</li>
                                <li>Search and filter classes by subject, teacher, day, and semester.</li>
                                <li>Use the grid view to inspect week plans quickly.</li>
                                <li>Registered users currently in system: <strong><%= previewTotalUsers %></strong>.</li>
                            </ul>

                            <div class="mt-3 small text-muted">
                                Stack: Java Servlets, JSP MVC, MySQL, Bootstrap 5.
                            </div>
                        </div>
                    </div>
                </section>
            </div>

            <div class="col-lg-6">
                <div class="card login-card h-100">
                    <div class="card-body p-4 p-md-5 text-start">
                        <h2 class="h4 mb-1">Welcome</h2>
                        <p class="page-subtitle mb-4">Sign in or create a new account to manage your timetable.</p>

                        <%
                        if (errorMessage != null) {
                        %>
                            <div class="alert alert-danger" role="alert"><%= errorMessage %></div>
                        <%
                        } else if ("session".equals(error)) {
                        %>
                            <div class="alert alert-danger" role="alert">Session expired. Please log in again.</div>
                        <%
                        }

                        if (signupErrorMessage != null) {
                        %>
                            <div class="alert alert-danger" role="alert"><%= signupErrorMessage %></div>
                        <%
                        }

                        if ("1".equals(logout)) {
                        %>
                            <div class="alert alert-success" role="alert">You have been logged out successfully.</div>
                        <%
                        }
                        %>

                        <ul class="nav nav-pills auth-tabs mb-3" id="authTabs" role="tablist">
                            <li class="nav-item" role="presentation">
                                <button class="nav-link <%= openSignup ? "" : "active" %>" id="signin-tab" data-bs-toggle="pill" data-bs-target="#signin-panel" type="button" role="tab" aria-controls="signin-panel" aria-selected="<%= openSignup ? "false" : "true" %>">Sign In</button>
                            </li>
                            <li class="nav-item" role="presentation">
                                <button class="nav-link <%= openSignup ? "active" : "" %>" id="signup-tab" data-bs-toggle="pill" data-bs-target="#signup-panel" type="button" role="tab" aria-controls="signup-panel" aria-selected="<%= openSignup ? "true" : "false" %>">Sign Up</button>
                            </li>
                        </ul>

                        <div class="tab-content" id="authTabsContent">
                            <div class="tab-pane fade <%= openSignup ? "" : "show active" %>" id="signin-panel" role="tabpanel" aria-labelledby="signin-tab">
                                <form action="<%= request.getContextPath() %>/login" method="post" class="needs-validation-custom" novalidate>
                                    <div class="mb-3">
                                        <label for="loginUsername" class="form-label">Username <span class="required">*</span></label>
                                        <input type="text" class="form-control" id="loginUsername" name="username" required maxlength="50" autocomplete="username">
                                        <div class="invalid-feedback">Please enter your username.</div>
                                    </div>

                                    <div class="mb-3">
                                        <label for="loginPassword" class="form-label">Password <span class="required">*</span></label>
                                        <input type="password" class="form-control" id="loginPassword" name="password" required maxlength="64" autocomplete="current-password">
                                        <div class="invalid-feedback">Please enter your password.</div>
                                    </div>

                                    <button type="submit" class="btn btn-primary-taupe w-100 mt-2">Login</button>
                                </form>
                            </div>

                            <div class="tab-pane fade <%= openSignup ? "show active" : "" %>" id="signup-panel" role="tabpanel" aria-labelledby="signup-tab">
                                <form action="<%= request.getContextPath() %>/signup" method="post" class="needs-validation-custom" novalidate>
                                    <div class="mb-3">
                                        <label for="signupFullName" class="form-label">Full Name <span class="required">*</span></label>
                                        <input type="text" class="form-control" id="signupFullName" name="fullName" required minlength="2" maxlength="100" autocomplete="name">
                                        <div class="invalid-feedback">Please enter your full name.</div>
                                    </div>

                                    <div class="mb-3">
                                        <label for="signupUsername" class="form-label">Username <span class="required">*</span></label>
                                        <input type="text" class="form-control" id="signupUsername" name="username" required minlength="3" maxlength="50" pattern="[A-Za-z0-9._-]{3,50}" autocomplete="username">
                                        <div class="invalid-feedback">Use 3-50 characters: letters, numbers, dot, underscore, or hyphen.</div>
                                    </div>

                                    <div class="mb-3">
                                        <label for="signupPassword" class="form-label">Password <span class="required">*</span></label>
                                        <input type="password" class="form-control" id="signupPassword" name="password" required minlength="6" maxlength="64" autocomplete="new-password">
                                        <div class="invalid-feedback">Password must be at least 6 characters.</div>
                                    </div>

                                    <div class="mb-3">
                                        <label for="signupConfirmPassword" class="form-label">Confirm Password <span class="required">*</span></label>
                                        <input type="password" class="form-control" id="signupConfirmPassword" name="confirmPassword" required minlength="6" maxlength="64" data-match-target="#signupPassword" data-match-message="Passwords must match." autocomplete="new-password">
                                        <div class="invalid-feedback">Passwords must match.</div>
                                    </div>

                                    <button type="submit" class="btn btn-secondary-taupe w-100 mt-2">Create Account</button>
                                </form>
                            </div>
                        </div>

                        <small class="text-muted d-block mt-3">Demo login credentials: admin / admin123</small>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
        crossorigin="anonymous"></script>
    <script src="<%= request.getContextPath() %>/js/validation.js"></script>
    <script src="<%= request.getContextPath() %>/js/login-dashboard-preview.js"></script>
</body>
</html>
