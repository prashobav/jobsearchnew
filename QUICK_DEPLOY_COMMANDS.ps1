# Quick Manual Deployment Commands for us-east-2

# 1. Create ECR Repository
aws ecr create-repository --repository-name jobsearch-backend --region us-east-2

# 2. Get ECR login and build image
$AccountId = (aws sts get-caller-identity --query Account --output text)
aws ecr get-login-password --region us-east-2 | docker login --username AWS --password-stdin "$AccountId.dkr.ecr.us-east-2.amazonaws.com"

# 3. Build and push Docker image
cd backend
docker build -t jobsearch-backend .
docker tag jobsearch-backend:latest "$AccountId.dkr.ecr.us-east-2.amazonaws.com/jobsearch-backend:latest"
docker push "$AccountId.dkr.ecr.us-east-2.amazonaws.com/jobsearch-backend:latest"
cd ..

# 4. Deploy CloudFormation stack
aws cloudformation deploy `
  --template-file aws/cloudformation-template.yaml `
  --stack-name jobsearch-infrastructure `
  --parameter-overrides `
    ProjectName=jobsearch `
    Environment=prod `
    DatabasePassword=SecurePass123! `
    JWTSecret=MyJWTSecret12345678901234567890123 `
  --capabilities CAPABILITY_IAM `
  --region us-east-2

# 5. Get stack outputs
$S3Bucket = aws cloudformation describe-stacks --stack-name jobsearch-infrastructure --region us-east-2 --query 'Stacks[0].Outputs[?OutputKey==`S3BucketName`].OutputValue' --output text
$ALBDns = aws cloudformation describe-stacks --stack-name jobsearch-infrastructure --region us-east-2 --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerDNS`].OutputValue' --output text

# 6. Build and deploy frontend
cd frontend
@"
REACT_APP_API_URL=http://$ALBDns/api
REACT_APP_ENV=production
REACT_APP_ENABLE_MOCK_SERVICES=false
GENERATE_SOURCEMAP=false
"@ | Out-File -FilePath ".env.production" -Encoding utf8

npm run build
aws s3 sync build/ "s3://$S3Bucket" --delete --region us-east-2

Write-Host "Deployment completed!"
Write-Host "Frontend URL: https://d1234567890.cloudfront.net (check CloudFormation outputs)"
Write-Host "Backend URL: http://$ALBDns"