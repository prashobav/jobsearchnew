# Mock Service Testing Script
# Run these commands to test the mock services

# 1. Check current mock status
Write-Host "🔍 Checking mock service status..."
try {
    $status = Invoke-RestMethod -Uri "http://localhost:8080/api/mock/status" -Method GET
    Write-Host "Mock Status: $($status.serviceType)" -ForegroundColor $(if($status.mockEnabled) {'Green'} else {'Yellow'})
    Write-Host "Description: $($status.description)"
} catch {
    Write-Host "❌ Error checking mock status: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n" + "="*60

# 2. Test mock sample responses
Write-Host "📋 Getting sample response information..."
try {
    $samples = Invoke-RestMethod -Uri "http://localhost:8080/api/mock/sample-responses" -Method GET
    Write-Host "JSearch Mock: $($samples.jSearch.maxJobs) jobs, Salary: $($samples.jSearch.salaryRange)"
    Write-Host "Adzuna Mock: $($samples.adzuna.maxJobs) jobs, Salary: $($samples.adzuna.salaryRange)"
    Write-Host "Companies: $($samples.jSearch.companies -join ', ')"
} catch {
    Write-Host "❌ Error getting sample data: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n" + "="*60

# 3. Test job fetch with mock data
Write-Host "🚀 Testing job fetch with mock services..."
try {
    $fetchRequest = @{
        jobTitle = "Manager"
        location = "Bangalore"
        maxResults = 10
    } | ConvertTo-Json

    $fetchResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/jobs/fetch" -Method POST -ContentType "application/json" -Body $fetchRequest
    Write-Host "Fetch Response: $($fetchResponse.message)" -ForegroundColor Green
    
    # Wait a moment for async processing
    Start-Sleep -Seconds 5
    
    # Check stats
    $stats = Invoke-RestMethod -Uri "http://localhost:8080/api/jobs/stats" -Method GET
    Write-Host "Total Jobs: $($stats.totalJobs)"
    Write-Host "JSearch Jobs: $($stats.jSearchJobs)"
    Write-Host "Adzuna Jobs: $($stats.adzunaJobs)"
    Write-Host "Service Type: $($stats.serviceType)" -ForegroundColor $(if($stats.serviceType -eq 'MOCK') {'Green'} else {'Yellow'})
    
} catch {
    Write-Host "❌ Error testing job fetch: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n" + "="*60

# 4. Get some actual job results
Write-Host "📊 Getting recent job results..."
try {
    $jobs = Invoke-RestMethod -Uri "http://localhost:8080/api/jobs/all?page=0&size=5" -Method GET
    Write-Host "Found $($jobs.totalElements) total jobs in database"
    
    if ($jobs.content.Count -gt 0) {
        Write-Host "`nRecent jobs:"
        $jobs.content | ForEach-Object {
            Write-Host "  • $($_.title) at $($_.company) - $($_.location) [$($_.source)]"
        }
    }
} catch {
    Write-Host "❌ Error getting job results: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n" + "="*60
Write-Host "✅ Mock service testing complete!" -ForegroundColor Green
Write-Host "`nTo toggle mock services:"
Write-Host "  Enable:  Invoke-RestMethod -Uri 'http://localhost:8080/api/mock/enable' -Method POST"
Write-Host "  Disable: Invoke-RestMethod -Uri 'http://localhost:8080/api/mock/disable' -Method POST"
Write-Host "  Status:  Invoke-RestMethod -Uri 'http://localhost:8080/api/mock/status' -Method GET"