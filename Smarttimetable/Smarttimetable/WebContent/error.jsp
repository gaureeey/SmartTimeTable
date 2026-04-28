<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Error | Smart Timetable Generator</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/custom-taupe.css">
</head>
<body class="app-page">
    <main class="container app-container">
        <div class="card panel-card mx-auto" style="max-width: 720px;">
            <div class="card-body p-4">
                <h1 class="mb-2">Something went wrong</h1>
                <p class="page-subtitle">Please try again or contact the administrator if the issue persists.</p>
                <p class="mb-4"><strong>Details:</strong> <%= exception != null ? exception.getMessage() : "Unexpected server error." %></p>
                <a href="<%= request.getContextPath() %>/dashboard" class="btn btn-primary-taupe">Back to Dashboard</a>
                <a href="<%= request.getContextPath() %>/login" class="btn btn-outline-taupe ms-2">Back to Login</a>
            </div>
        </div>
    </main>
</body>
</html>
