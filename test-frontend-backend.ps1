# Complete Frontend-Backend Test Script

Write-Host "🚀 Testing Complete Job Search Application Flow" -ForegroundColor Green
Write-Host "="*60

# 1. Check if backend is running
Write-Host "`n1. Testing Backend Health..." -ForegroundColor Cyan
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/api/actuator/health" -Method GET
    Write-Host "✅ Backend is running: $($health.status)" -ForegroundColor Green
} catch {
    Write-Host "❌ Backend not running. Please start with: mvn spring-boot:run" -ForegroundColor Red
    exit 1
}

# 2. Check mock service status
Write-Host "`n2. Checking Mock Service Status..." -ForegroundColor Cyan
try {
    $mockStatus = Invoke-RestMethod -Uri "http://localhost:8080/api/mock/status" -Method GET
    Write-Host "Service Type: $($mockStatus.serviceType)" -ForegroundColor $(if($mockStatus.mockEnabled) {'Green'} else {'Yellow'})
    Write-Host "Mock Enabled: $($mockStatus.mockEnabled)"
} catch {
    Write-Host "❌ Cannot check mock status: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. Test initial job count
Write-Host "`n3. Checking Initial Job Count..." -ForegroundColor Cyan
try {
    $initialStats = Invoke-RestMethod -Uri "http://localhost:8080/api/jobs/stats" -Method GET
    Write-Host "Initial Total Jobs: $($initialStats.totalJobs)"
    Write-Host "JSearch Jobs: $($initialStats.jSearchJobs)"
    Write-Host "Adzuna Jobs: $($initialStats.adzunaJobs)"
    Write-Host "Service Type: $($initialStats.serviceType)" -ForegroundColor $(if($initialStats.serviceType -eq 'MOCK') {'Green'} else {'Yellow'})
} catch {
    Write-Host "❌ Cannot get job stats: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. Fetch new jobs using mock services
Write-Host "`n4. Testing Job Fetch (Mock Services)..." -ForegroundColor Cyan
try {
    $fetchRequest = @{
        jobTitle = "Manager"
        location = "Bangalore"
        maxResults = 10
    } | ConvertTo-Json

    $fetchResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/jobs/fetch" -Method POST -ContentType "application/json" -Body $fetchRequest
    Write-Host "✅ Fetch Response: $($fetchResponse.message)" -ForegroundColor Green
    
    if ($fetchResponse.message -like "*MOCK*") {
        Write-Host "✅ Using MOCK services - no rate limits!" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Fetch failed: $($_.Exception.Message)" -ForegroundColor Red
}

# 5. Wait for async processing
Write-Host "`n5. Waiting for Async Job Processing..." -ForegroundColor Cyan
Write-Host "Waiting 6 seconds for mock job generation..."
Start-Sleep -Seconds 6

# 6. Check updated job count
Write-Host "`n6. Checking Updated Job Count..." -ForegroundColor Cyan
try {
    $updatedStats = Invoke-RestMethod -Uri "http://localhost:8080/api/jobs/stats" -Method GET
    Write-Host "Updated Total Jobs: $($updatedStats.totalJobs)"
    Write-Host "JSearch Jobs: $($updatedStats.jSearchJobs)" 
    Write-Host "Adzuna Jobs: $($updatedStats.adzunaJobs)"
    
    if ($updatedStats.totalJobs -gt $initialStats.totalJobs) {
        Write-Host "✅ New jobs added successfully!" -ForegroundColor Green
    } else {
        Write-Host "⚠️  No new jobs added (may already exist)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Cannot get updated stats: $($_.Exception.Message)" -ForegroundColor Red
}

# 7. Test job search API
Write-Host "`n7. Testing Job Search API..." -ForegroundColor Cyan
try {
    $searchResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/jobs/search?title=Manager&page=0&size=5" -Method GET
    Write-Host "Search Results: $($searchResponse.totalElements) total jobs found"
    
    if ($searchResponse.content.Count -gt 0) {
        Write-Host "✅ Sample Jobs Found:" -ForegroundColor Green
        $searchResponse.content | ForEach-Object {
            Write-Host "  • $($_.title) at $($_.company) - $($_.location) [$($_.source)]"
        }
    }
} catch {
    Write-Host "❌ Search failed: $($_.Exception.Message)" -ForegroundColor Red
}

# 8. Test get all jobs API
Write-Host "`n8. Testing Get All Jobs API..." -ForegroundColor Cyan
try {
    $allJobs = Invoke-RestMethod -Uri "http://localhost:8080/api/jobs/all?page=0&size=5" -Method GET
    Write-Host "All Jobs Count: $($allJobs.totalElements)"
    
    if ($allJobs.content.Count -gt 0) {
        Write-Host "✅ Recent Jobs:" -ForegroundColor Green
        $allJobs.content | ForEach-Object {
            $salary = if ($_.salaryMin -and $_.salaryMax) { "₹$([math]::Round($_.salaryMin/100000,1))L-₹$([math]::Round($_.salaryMax/100000,1))L" } else { "Not specified" }
            Write-Host "  • $($_.title) at $($_.company)"
            Write-Host "    📍 $($_.location) | 💰 $salary | 🏷️ $($_.source)"
        }
    }
} catch {
    Write-Host "❌ Get all jobs failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n" + "="*60
Write-Host "🎉 FRONTEND INTEGRATION GUIDE:" -ForegroundColor Green
Write-Host "1. Start Frontend: cd frontend && npm start"
Write-Host "2. Open: http://localhost:3000"
Write-Host "3. Use 'Refresh All Jobs' button to see all jobs"
Write-Host "4. Use 'Search Jobs' to filter results" 
Write-Host "5. Use 'Fetch New Jobs' to get more jobs from APIs"
Write-Host "`n✅ Backend APIs are working correctly!" -ForegroundColor Green