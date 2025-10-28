# AWS Deployment Guide for JobSearch Application

## 🎯 Overview
This guide will help you deploy your JobSearch application to AWS using free tier services.

## 🏗️ Architecture
- **Frontend**: S3 (Static Hosting) + CloudFront (CDN)
- **Backend**: ECS Fargate (Containerized)
- **Database**: RDS PostgreSQL (db.t3.micro)
- **Load Balancer**: Application Load Balancer
- **Monitoring**: CloudWatch

## 💰 AWS Free Tier Usage
- **RDS**: db.t3.micro (1 year free)
- **ECS**: 50GB-hours/month of Fargate compute
- **S3**: 5GB storage
- **CloudFront**: 50GB data transfer
- **ALB**: 750 hours/month

## 🚀 Deployment Steps

### Prerequisites
1. **AWS CLI**: [Install AWS CLI](https://aws.amazon.com/cli/)
2. **Docker**: [Install Docker](https://www.docker.com/)
3. **AWS Account**: With appropriate permissions
4. **Node.js**: For frontend build

### 1. Configure AWS CLI
```bash
aws configure
# Enter your AWS Access Key ID, Secret Access Key, and region
```

### 2. Deploy Using PowerShell (Windows)
```powershell
cd aws
.\deploy.ps1 -Action deploy
```

### 3. Deploy Using Bash (Linux/Mac)
```bash
cd aws
chmod +x deploy.sh
./deploy.sh deploy
```

### 4. Manual Deployment Steps

#### 4.1. Create ECR Repository
```bash
aws ecr create-repository --repository-name jobsearch-backend --region us-east-1
```

#### 4.2. Build and Push Docker Image
```bash
# Get login command
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

# Build and tag image
cd backend
docker build -t jobsearch-backend .
docker tag jobsearch-backend:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/jobsearch-backend:latest

# Push image
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/jobsearch-backend:latest
```

#### 4.3. Deploy Infrastructure
```bash
aws cloudformation deploy \
  --template-file cloudformation-template.yaml \
  --stack-name jobsearch-infrastructure \
  --parameter-overrides \
    ProjectName=jobsearch \
    Environment=prod \
    DatabasePassword=YourSecurePassword123! \
    JWTSecret=YourJWTSecretKey12345678901234567890 \
  --capabilities CAPABILITY_IAM \
  --region us-east-1
```

#### 4.4. Build and Deploy Frontend
```bash
cd frontend

# Create production environment file
cat > .env.production << EOF
REACT_APP_API_URL=http://<alb-dns>/api
REACT_APP_ENV=production
REACT_APP_ENABLE_MOCK_SERVICES=false
GENERATE_SOURCEMAP=false
EOF

# Build and deploy
npm run build
aws s3 sync build/ s3://<bucket-name> --delete
```

## 🔧 Configuration Files

### Environment Variables for Production
Create `.env.production` in frontend directory:
```env
REACT_APP_API_URL=http://your-alb-dns/api
REACT_APP_ENV=production
REACT_APP_ENABLE_MOCK_SERVICES=false
GENERATE_SOURCEMAP=false
```

### Database Configuration
The application automatically switches to PostgreSQL in production using `application-prod.properties`.

### Security Configuration
- JWT secrets are configured via environment variables
- CORS is configured for CloudFront URLs
- Health check endpoints are exposed for load balancer

## 📊 Monitoring and Health Checks

### Health Check Endpoints
- `/api/health` - Application health status
- `/api/ready` - Readiness probe
- `/api/live` - Liveness probe
- `/api/actuator/health` - Spring Boot actuator health

### CloudWatch Monitoring
- ECS task metrics
- RDS database metrics
- ALB request metrics
- Custom application metrics

## 🛡️ Security Best Practices

### 1. Environment Variables
- Store sensitive data in AWS Systems Manager Parameter Store
- Use IAM roles instead of access keys

### 2. Network Security
- Private subnets for database
- Security groups with minimal required access
- HTTPS enforcement (add SSL certificate)

### 3. Database Security
- Encrypted storage
- Automated backups
- Connection pooling

## 🔄 CI/CD Pipeline (Optional)

### GitHub Actions Workflow
Create `.github/workflows/deploy.yml`:
```yaml
name: Deploy to AWS
on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v1
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: us-east-1
      - name: Deploy to AWS
        run: ./aws/deploy.sh deploy
```

## 🧹 Cleanup

### Remove All Resources
```bash
# Using script
./deploy.sh cleanup

# Or manually
aws cloudformation delete-stack --stack-name jobsearch-infrastructure
aws ecr delete-repository --repository-name jobsearch-backend --force
```

## 📝 Notes

### Cost Optimization
- Use FARGATE_SPOT for non-production workloads
- Enable S3 lifecycle policies
- Set up CloudWatch alarms for cost monitoring

### Scaling
- ECS service auto-scaling based on CPU/memory
- RDS read replicas for read-heavy workloads
- CloudFront caching for better performance

### Troubleshooting
- Check ECS task logs in CloudWatch
- Verify security group configurations
- Test health check endpoints
- Monitor RDS connections

## 🔗 Useful Commands

```bash
# Check stack status
aws cloudformation describe-stacks --stack-name jobsearch-infrastructure

# View ECS service status
aws ecs describe-services --cluster jobsearch-cluster --services jobsearch-backend-service

# Get RDS endpoint
aws rds describe-db-instances --db-instance-identifier jobsearch-db

# Update ECS service
aws ecs update-service --cluster jobsearch-cluster --service jobsearch-backend-service --force-new-deployment
```

## 📞 Support
For issues or questions:
1. Check CloudWatch logs
2. Verify AWS service quotas
3. Review security group configurations
4. Test connectivity between services