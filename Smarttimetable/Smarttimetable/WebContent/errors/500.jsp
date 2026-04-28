<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>500 | Smart Timetable Generator</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/custom-taupe.css">
</head>
<body class="app-page">
    <main class="container app-container">
        <div class="card panel-card mx-auto" style="max-width: 640px;">
            <div class="card-body text-center p-5">
                <h1>500</h1>
                <p class="page-subtitle mb-4">An internal server error occurred. Please retry shortly.</p>
                <a href="<%= request.getContextPath() %>/dashboard" class="btn btn-primary-taupe">Go to Dashboard</a>
            </div>
        </div>
    </main>
</body>
</html>
