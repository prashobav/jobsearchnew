# Secure AWS Deployment without Personal Credentials

## 🔐 Option 1: AWS IAM Roles (Recommended)

### Step 1: Create Deployment IAM Role
```bash
# Create a deployment role with minimal permissions
aws iam create-role \
  --role-name JobSearchDeploymentRole \
  --assume-role-policy-document '{
    "Version": "2012-10-17",
    "Statement": [
      {
        "Effect": "Allow",
        "Principal": {
          "AWS": "arn:aws:iam::YOUR_ACCOUNT_ID:root"
        },
        "Action": "sts:AssumeRole",
        "Condition": {
          "StringEquals": {
            "sts:ExternalId": "your-unique-external-id"
          }
        }
      }
    ]
  }'
```

### Step 2: Attach Deployment Policy
```bash
# Create policy with minimal required permissions
aws iam create-policy \
  --policy-name JobSearchDeploymentPolicy \
  --policy-document file://deployment-policy.json

# Attach policy to role
aws iam attach-role-policy \
  --role-name JobSearchDeploymentRole \
  --policy-arn arn:aws:iam::YOUR_ACCOUNT_ID:policy/JobSearchDeploymentPolicy
```

### Step 3: Use Role in Deployment
```bash
# Assume role for deployment
aws sts assume-role \
  --role-arn arn:aws:iam::YOUR_ACCOUNT_ID:role/JobSearchDeploymentRole \
  --role-session-name deployment-session \
  --external-id your-unique-external-id
```

## 🏢 Option 2: AWS Organizations & Cross-Account Roles

### Create Dedicated Deployment Account
```bash
# Create separate AWS account for deployments
aws organizations create-account \
  --account-name "JobSearch-Deployment" \
  --email "jobsearch-deploy@yourdomain.com"
```

## 🔑 Option 3: Temporary Credentials with STS

### Generate Short-lived Credentials
```bash
# Generate temporary credentials (1-12 hours)
aws sts get-session-token \
  --duration-seconds 3600 \
  --serial-number arn:aws:iam::YOUR_ACCOUNT_ID:mfa/YOUR_MFA_DEVICE \
  --token-code 123456
```

## 🤖 Option 4: GitHub Actions with OIDC (No Credentials Stored)

### Setup GitHub OIDC Provider
```bash
# Create OIDC identity provider
aws iam create-open-id-connect-provider \
  --url https://token.actions.githubusercontent.com \
  --client-id-list sts.amazonaws.com \
  --thumbprint-list 6938fd4d98bab03faadb97b34396831e3780aea1
```

### GitHub Actions Workflow (No secrets needed)
```yaml
# .github/workflows/deploy.yml
name: Deploy to AWS
on:
  push:
    branches: [main]

permissions:
  id-token: write
  contents: read

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v2
        with:
          role-to-assume: arn:aws:iam::YOUR_ACCOUNT_ID:role/GitHubActionsRole
          aws-region: us-east-1
          
      - name: Deploy application
        run: ./aws/deploy.sh deploy
```

## 🐳 Option 5: Local Docker Deployment

### Run Locally with Docker Compose
```yaml
# docker-compose.yml
version: '3.8'
services:
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DATABASE_URL=jdbc:postgresql://db:5432/jobsearch
    depends_on:
      - db
      
  frontend:
    build: ./frontend
    ports:
      - "3000:80"
    depends_on:
      - backend
      
  db:
    image: postgres:13
    environment:
      - POSTGRES_DB=jobsearch
      - POSTGRES_USER=jobsearch_user
      - POSTGRES_PASSWORD=password123
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

## ☁️ Option 6: Cloud Development Environments

### Use AWS Cloud9
```bash
# Deploy from AWS Cloud9 (uses instance role)
# No credentials needed - uses EC2 instance role
./aws/deploy.sh deploy
```

### Use GitHub Codespaces
```bash
# Deploy from GitHub Codespaces with OIDC
# Configure repository secrets for OIDC role
```

## 🔐 Option 7: AWS CLI Profiles with Limited Scope

### Create Deployment-Only Profile
```bash
# Create limited-scope credentials
aws configure set aws_access_key_id DEPLOYMENT_KEY_ID --profile jobsearch-deploy
aws configure set aws_secret_access_key DEPLOYMENT_SECRET --profile jobsearch-deploy
aws configure set region us-east-1 --profile jobsearch-deploy

# Use specific profile for deployment
AWS_PROFILE=jobsearch-deploy ./aws/deploy.sh deploy
```

## 🏗️ Option 8: Infrastructure as Code with Terraform Cloud

### Use Terraform Cloud (Free Tier)
```hcl
# terraform/main.tf
terraform {
  cloud {
    organization = "your-org"
    workspaces {
      name = "jobsearch-prod"
    }
  }
}

# Configure AWS provider with assume role
provider "aws" {
  assume_role {
    role_arn = "arn:aws:iam::YOUR_ACCOUNT_ID:role/TerraformRole"
  }
}
```

## 📱 Option 9: Mobile/Web-based Deployment

### AWS CloudShell (Browser-based)
```bash
# Deploy directly from AWS CloudShell (no local credentials)
git clone https://github.com/yourusername/jobsearchnew.git
cd jobsearchnew
./aws/deploy.sh deploy
```

## 🎯 **Recommended Approach for You**

Based on your situation, I recommend **Option 4 (GitHub Actions with OIDC)**:

1. **No credentials stored anywhere**
2. **Automatic deployments on code push**
3. **Audit trail of deployments**
4. **Free on GitHub**
5. **Industry best practice**

Would you like me to set up the GitHub Actions workflow with OIDC authentication for your repository?