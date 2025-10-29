# Manual AWS Deployment Guide

## Prerequisites
- AWS Account with appropriate permissions
- Your GitHub repository: https://github.com/prashobav/jobsearchnew

## Step 1: Deploy CloudFormation Stack via AWS Console

1. **Open AWS CloudFormation Console**:
   - Go to: https://us-east-2.console.aws.amazon.com/cloudformation/home?region=us-east-2

2. **Create New Stack**:
   - Click "Create stack" → "With new resources (standard)"
   - Choose "Upload a template file"
   - Upload the file: `aws/cloudformation-template.yaml` from your local repository

3. **Configure Stack**:
   - **Stack name**: `jobsearch-stack`
   - **Parameters**:
     - DatabasePassword: `SecurePass123!`
     - JWTSecret: `MyJWTSecret12345678901234567890123`
   - Click "Next" → "Next" → Check "I acknowledge..." → "Submit"

4. **Wait for Completion**:
   - This will take 15-20 minutes
   - Watch the "Events" tab for progress
   - Stack status should become "CREATE_COMPLETE"

## Step 2: Build and Push Docker Images

### Option A: Use GitHub Actions (Recommended)

1. **Enable GitHub Actions** in your repository
2. **Create workflow file**: `.github/workflows/deploy.yml`
3. **Add AWS credentials** to GitHub Secrets:
   - Go to your repo → Settings → Secrets and variables → Actions
   - Add secrets:
     - `AWS_ACCESS_KEY_ID`: Your AWS access key
     - `AWS_SECRET_ACCESS_KEY`: Your AWS secret key
     - `AWS_REGION`: `us-east-2`

### Option B: Use Docker Desktop (if installed)

1. **Install Docker Desktop** from https://www.docker.com/products/docker-desktop/
2. **Build and push images**:
   ```bash
   # Get ECR login token
   aws ecr get-login-password --region us-east-2 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-2.amazonaws.com
   
   # Build backend
   cd backend
   docker build -t jobsearch-backend .
   docker tag jobsearch-backend:latest <account-id>.dkr.ecr.us-east-2.amazonaws.com/jobsearch-backend:latest
   docker push <account-id>.dkr.ecr.us-east-2.amazonaws.com/jobsearch-backend:latest
   
   # Build frontend
   cd ../frontend
   docker build -t jobsearch-frontend .
   docker tag jobsearch-frontend:latest <account-id>.dkr.ecr.us-east-2.amazonaws.com/jobsearch-frontend:latest
   docker push <account-id>.dkr.ecr.us-east-2.amazonaws.com/jobsearch-frontend:latest
   ```

## Step 3: Deploy Services

1. **Go to ECS Console**:
   - https://us-east-2.console.aws.amazon.com/ecs/home?region=us-east-2

2. **Update Services**:
   - Click on your cluster → Services
   - Select backend service → Update → Force new deployment
   - Select frontend service → Update → Force new deployment

## Step 4: Get Application URLs

1. **CloudFront Distribution**:
   - Go to: https://us-east-1.console.aws.amazon.com/cloudfront/v3/home
   - Find your distribution → Copy domain name
   - Your frontend will be available at: `https://<distribution-domain>`

2. **Load Balancer**:
   - Go to: https://us-east-2.console.aws.amazon.com/ec2/home?region=us-east-2#LoadBalancers
   - Find your ALB → Copy DNS name
   - Your API will be available at: `http://<alb-dns-name>/api`

## Troubleshooting

### Time Sync Issues
If you get signature errors:
1. Sync your system time
2. Use CloudShell instead of local AWS CLI
3. Use AWS Console for manual deployment

### CloudShell Not Working
1. Try different regions (us-east-1, us-west-2)
2. Use incognito/private browser mode
3. Clear browser cache
4. Try different browser

### Stack Creation Fails
1. Check CloudFormation Events tab for errors
2. Ensure you have sufficient permissions
3. Check AWS service limits
4. Verify region availability

## Alternative: Free Hosting Options

If AWS deployment continues to have issues, consider these alternatives:

### Vercel + Supabase (Free)
1. **Frontend**: Deploy React app to Vercel
2. **Backend**: Deploy Spring Boot to Railway/Render
3. **Database**: Use Supabase PostgreSQL (free tier)

### Netlify + Heroku (Free Tiers)
1. **Frontend**: Deploy to Netlify
2. **Backend**: Deploy to Heroku
3. **Database**: Use Heroku PostgreSQL add-on

## Cost Optimization

Your current setup uses AWS free tier:
- **ECS Fargate**: 20GB-hours per month (free)
- **RDS db.t3.micro**: 750 hours per month (free)
- **ALB**: $16/month (not free, but minimal)
- **S3 + CloudFront**: Minimal costs for low traffic

Total estimated cost: ~$16-20/month after free tier expires.