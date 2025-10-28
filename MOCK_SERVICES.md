# Mock Services Documentation

## Overview

The Job Search application now includes comprehensive mock services that simulate external API responses without making actual API calls. This is perfect for development, testing, and avoiding rate limit issues.

## Quick Start

### Enable Mock Services
```bash
# Set in application.properties
app.mock.enabled=true

# Or enable via API
curl -X POST http://localhost:8080/api/mock/enable
```

### Test Mock Services
```bash
# Run the test script
PowerShell -ExecutionPolicy Bypass -File test-mock-services.ps1
```

## Mock Service Features

### 🎯 **JSearch Mock Service**
- **Companies**: TCS, Infosys, Wipro, Microsoft India, Amazon India, Flipkart
- **Job Titles**: Senior Manager, Lead Manager, Manager - Technology
- **Salary Range**: ₹8L - ₹28L (realistic Indian tech salaries)
- **Remote Jobs**: 30% chance
- **Max Results**: Up to 15 jobs per request
- **Response Time**: 1-3 seconds (simulated API delay)

### 🎯 **Adzuna Mock Service**
- **Companies**: Reliance, Tata Group, Mahindra, HDFC Bank, ITC Limited
- **Job Titles**: Deputy Manager, Regional Manager, Manager - Business Operations
- **Salary Range**: ₹6L - ₹24L (realistic Indian corporate salaries)
- **Remote Jobs**: 15% chance (traditional companies)
- **Max Results**: Up to 12 jobs per request
- **Response Time**: 0.8-2.3 seconds (simulated API delay)

### 🎯 **Realistic Data Generation**
- **Locations**: Bangalore, Mumbai, Pune, Hyderabad, Chennai, Delhi
- **Skills**: Role-specific skills, leadership, communication, project management
- **Descriptions**: Professional job descriptions with growth opportunities
- **URLs**: Mock job application links
- **Timestamps**: Current date/time for realistic sorting

## API Endpoints

### Mock Control
```bash
GET  /api/mock/status              # Check if mock services are enabled
POST /api/mock/enable              # Enable mock services
POST /api/mock/disable             # Disable mock services
GET  /api/mock/sample-responses    # View mock data structure
```

### Job Operations (Auto-detects Mock/Real)
```bash
POST /api/jobs/fetch               # Fetch jobs (uses mock if enabled)
GET  /api/jobs/stats               # Get job statistics
GET  /api/jobs/all                 # Get all jobs (includes mock data)
```

## Testing Scenarios

### 1. **Rate Limit Testing**
Mock services never hit rate limits, perfect for testing your application's job fetching logic.

### 2. **Indian Job Market Testing** 
Mock data focuses on Indian companies, locations, and salary ranges for realistic testing.

### 3. **Performance Testing**
Test pagination, filtering, and search with consistent mock data.

### 4. **Error Handling Testing**
Mock services can be configured to simulate various error conditions.

## Configuration Options

```properties
# Mock Services
app.mock.enabled=true                    # Enable/disable mock services
app.mock.jsearch.max-jobs=15            # Max jobs per JSearch request
app.mock.adzuna.max-jobs=12             # Max jobs per Adzuna request  
app.mock.response-delay-min=1000        # Minimum response delay (ms)
app.mock.response-delay-max=3000        # Maximum response delay (ms)
```

## Sample Mock Responses

### JSearch Mock Response
```json
{
  "title": "Senior Manager - Technology",
  "company": "Infosys Technologies", 
  "location": "Bangalore, Karnataka",
  "salaryMin": 1500000,
  "salaryMax": 2200000,
  "source": "jsearch",
  "isRemote": false,
  "skills": ["manager", "leadership", "teamwork", "communication"]
}
```

### Adzuna Mock Response
```json
{
  "title": "Regional Manager - Business Operations",
  "company": "Tata Group",
  "location": "Mumbai, Maharashtra", 
  "salaryMin": 1200000,
  "salaryMax": 1800000,
  "source": "adzuna",
  "isRemote": false,
  "skills": ["manager", "business management", "strategic planning"]
}
```

## Benefits

### 🚀 **Development Benefits**
- No API key setup required
- No rate limiting concerns
- Consistent test data
- Instant responses
- Work offline

### 🧪 **Testing Benefits**
- Predictable data for unit tests
- Performance testing without external dependencies
- Error scenario simulation
- Pagination testing with known data sets

### 💰 **Cost Benefits**
- No API usage charges during development
- Preserve API quotas for production
- Unlimited testing without hitting limits

## Switching Between Mock and Real

### Via Configuration
```properties
# Use mock services
app.mock.enabled=true

# Use real APIs  
app.mock.enabled=false
```

### Via API Calls
```bash
# Enable mock
curl -X POST http://localhost:8080/api/mock/enable

# Disable mock
curl -X POST http://localhost:8080/api/mock/disable

# Check status
curl http://localhost:8080/api/mock/status
```

### Application Restart
After changing configuration, restart the application to ensure all services use the correct mode.

## Troubleshooting

### Mock Services Not Working
1. Check `app.mock.enabled=true` in application.properties
2. Verify application started with mock services enabled (check logs)
3. Ensure database is accessible for saving mock jobs

### Mixed Data (Mock + Real)
- Mock jobs have `source` field as "jsearch" or "adzuna" 
- Mock external IDs start with "mock_"
- Use `/api/jobs/stats` to see service type

### Performance Issues
- Mock services include realistic delays (1-3 seconds)
- Reduce `app.mock.response-delay-max` for faster testing
- Limit `maxResults` in fetch requests for quicker responses

## Production Deployment

### Disable Mock Services
```properties
app.mock.enabled=false
```

### Environment Variables
```bash
export MOCK_ENABLED=false
```

### Verification
```bash
curl http://localhost:8080/api/mock/status
# Should return: "serviceType": "REAL"
```