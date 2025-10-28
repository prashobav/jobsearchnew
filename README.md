# Job Search Full-Stack Application

A comprehensive job search platform built with React TypeScript frontend, Spring Boot backend, and PostgreSQL database. The application securely fetches job listings from multiple APIs (Indeed via JSearch, Adzuna India) and provides advanced filtering capabilities focused on the Indian job market.

## 🏗️ Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   React (TS)    │    │   Spring Boot    │    │   PostgreSQL    │
│   Frontend      │────│   Backend API    │────│   Database      │
│   Port: 3000    │    │   Port: 8080     │    │   Port: 5432    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │  External APIs  │
                       │ • JSearch (Indeed)
                       │ • Adzuna India  │
                       └─────────────────┘
```

## 🚀 Features

- **Secure Authentication**: JWT-based authentication with role management
- **Multi-Source Job Fetching**: Aggregates jobs from Indeed (via JSearch) and Adzuna India APIs
- **Advanced Filtering**: Filter by title, company, location, salary range, remote work
- **Real-time Search**: Responsive search with pagination and sorting
- **SQL Injection Protection**: Parameterized queries and input validation
- **Rate Limiting**: API request throttling to prevent abuse
- **Responsive UI**: Modern Material-UI components with TypeScript
- **India-Focused**: Optimized for Indian job market with INR salary ranges and Indian cities

## 📋 Prerequisites

- **Java 17+**
- **Node.js 16+**
- **PostgreSQL 12+**
- **Maven 3.6+**

## 🎮 How to Use the Application

### **Backend Setup**
```bash
cd backend
mvn spring-boot:run
```
*Backend runs on http://localhost:8080*

### **Frontend Setup**
```bash
cd frontend
npm install
npm start
```
*Frontend runs on http://localhost:3000*

### **Using the Job Search UI**

#### **1. View Existing Jobs**
- Click **"Refresh All Jobs"** to load all jobs from database
- Jobs are displayed with company, location, salary (in INR), and source

#### **2. Search & Filter Jobs**
- **Job Title**: Search by specific roles (e.g., "Manager", "Developer")
- **Location**: Filter by city (e.g., "Bangalore", "Mumbai")  
- **Company**: Filter by company name
- **Salary Range**: Set minimum and maximum salary in INR
- **Remote Only**: Toggle for remote jobs only
- **Job Source**: Filter by JSearch/Indeed or Adzuna
- Click **"Search Jobs"** to apply filters

#### **3. Fetch New Jobs from External APIs**
- Enter **Job Role** (e.g., "React Developer", "Project Manager")
- Enter **Location** (e.g., "Bangalore", "Mumbai")
- Set **Max Results** (recommended: 10-20 to avoid rate limits)
- Click **"Fetch New Jobs"** 
- Wait 5-10 seconds for processing
- Click **"Refresh All Jobs"** to see newly added jobs

#### **4. Job Details**
Each job card shows:
- **Title & Company**: Job position and employer
- **Location**: City and state
- **Salary**: Range in INR (₹12L - ₹18L format)
- **Source**: JSearch (Indeed/LinkedIn) or Adzuna
- **Remote**: Remote work availability
- **Skills**: Relevant skills and keywords
- **Apply Button**: Direct link to job posting

### **Mock vs Real Services**

#### **Mock Services (Default - Recommended for Development)**
- ✅ **No Rate Limits**: Unlimited job fetching
- ✅ **No API Keys**: Works without external credentials
- ✅ **Instant Results**: 1-3 second response time
- ✅ **Indian Data**: Realistic Indian companies and salaries
- ✅ **Consistent Testing**: Same results every time

#### **Real Services (Production)**
- ⚠️ **Rate Limited**: 500 requests/month (free tier)
- ⚠️ **Requires API Keys**: Valid JSearch and Adzuna credentials
- ⚠️ **Variable Response**: Depends on external API availability
- ✅ **Live Data**: Real job postings from job boards

### **Switching Between Mock and Real Services**

**Enable Mock Services:**
```bash
# Set in application.properties
app.mock.enabled=true

# Or via API
curl -X POST http://localhost:8080/api/mock/enable
```

**Enable Real Services:**
```bash
# Set in application.properties  
app.mock.enabled=false

# Or via API
curl -X POST http://localhost:8080/api/mock/disable
```

### 1. Database Setup

```sql
-- Create database and user
CREATE DATABASE jobsearch;
CREATE USER jobsearch_user WITH PASSWORD 'jobsearch_password';
GRANT ALL PRIVILEGES ON DATABASE jobsearch TO jobsearch_user;
```

### 2. Backend Configuration

Create `.env` file in `backend/` directory:

```env
# Database
DB_USERNAME=jobsearch_user
DB_PASSWORD=jobsearch_password

# JWT Security
JWT_SECRET=mySecretKey123456789012345678901234567890

# External APIs
ADZUNA_APP_ID=your_adzuna_app_id
ADZUNA_APP_KEY=your_adzuna_app_key
RAPIDAPI_KEY=your_rapidapi_key

# CORS
CORS_ORIGINS=http://localhost:3000,http://localhost:3001
```

### 3. API Keys Setup

#### Adzuna API
1. Register at [Adzuna Developer Portal](https://developer.adzuna.com/)
2. Create an app and get `app_id` and `app_key`

#### RapidAPI (JSearch)
1. Register at [RapidAPI](https://rapidapi.com/)
2. Subscribe to [JSearch API](https://rapidapi.com/letscrape-6bbc/api/jsearch/)
3. Get your RapidAPI key

### 4. Backend Startup

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### 5. Frontend Startup

```bash
cd frontend
npm install
npm start
```

The frontend will start on `http://localhost:3000`

## 🔧 Configuration

### Application Properties

Key configuration options in `backend/src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/jobsearch
spring.jpa.hibernate.ddl-auto=update

# Security
app.jwt.expiration-ms=86400000
app.cors.allowed-origins=http://localhost:3000

# Rate Limiting
app.rate-limit.requests-per-minute=60
app.rate-limit.api-requests-per-hour=1000
```

## 🔐 Security Features

- **JWT Authentication**: Secure token-based authentication
- **Password Encryption**: BCrypt password hashing
- **SQL Injection Prevention**: JPA parameterized queries
- **Input Validation**: Bean validation on all DTOs
- **CORS Configuration**: Configurable cross-origin settings
- **Rate Limiting**: API request throttling

## 📚 API Endpoints

### Authentication
- `POST /api/auth/signin` - User login
- `POST /api/auth/signup` - User registration

### Jobs
- `GET /api/jobs/search` - Search jobs with filters
- `POST /api/jobs/fetch` - Fetch new jobs from external APIs
- `GET /api/jobs/stats` - Get job statistics
- `GET /api/jobs/filters/locations` - Get available locations
- `GET /api/jobs/filters/companies` - Get available companies
- `GET /api/jobs/{id}` - Get job by ID

### Example API Usage

```bash
# Search jobs
curl "http://localhost:8080/api/jobs/search?title=developer&location=bangalore&page=0&size=20"

# Fetch new jobs (requires authentication)
curl -X POST "http://localhost:8080/api/jobs/fetch" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"jobTitle": "software engineer", "location": "bangalore", "maxResults": 100}'
```

## 🎨 Frontend Components

- **JobSearch**: Main search interface with filters
- **JobList**: Paginated job results display
- **JobCard**: Individual job listing component
- **FilterPanel**: Advanced filtering options
- **AuthForms**: Login and registration components

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## 📊 Database Schema

### Key Tables
- `users` - User accounts and authentication
- `roles` - User roles (USER, ADMIN)
- `user_roles` - User-role relationships
- `jobs` - Job listings from all sources
- `user_profiles` - User preferences and profiles

## 🚦 Development Workflow

1. **Start PostgreSQL** database
2. **Start Backend** (`mvn spring-boot:run`)
3. **Start Frontend** (`npm start`)
4. **Configure API keys** for external services
5. **Test authentication** and job fetching

## 🔍 Troubleshooting

### Common Issues

**Database Connection:**
```bash
# Check if PostgreSQL is running
sudo systemctl status postgresql

# Test connection
psql -h localhost -U jobsearch_user -d jobsearch
```

**API Key Issues:**
- Verify Adzuna credentials at [developer.adzuna.com](https://developer.adzuna.com/)
- Check RapidAPI subscription status
- Ensure environment variables are properly set

**CORS Errors:**
- Update `CORS_ORIGINS` in application.properties
- Restart backend after configuration changes

## 📈 Performance Optimization

- **Database Indexing**: Optimized indexes on job title, company, location
- **Async Processing**: Job fetching runs asynchronously
- **Pagination**: Large result sets are paginated
- **Connection Pooling**: Configured HikariCP for database connections

## 🛡️ Production Deployment

### Security Considerations
- Use strong JWT secrets (256-bit minimum)
- Enable HTTPS in production
- Configure proper CORS origins
- Set up rate limiting at reverse proxy level
- Use environment-specific configuration

### Database Production Setup
- Enable SSL connections
- Set up regular backups
- Configure connection pooling
- Monitor query performance

## 📄 License

This project is licensed under the MIT License.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📞 Support

For issues and questions:
- Create an issue in the repository
- Check the troubleshooting section
- Review the API documentation