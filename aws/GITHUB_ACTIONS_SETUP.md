# Setting up Secure AWS Deployment with GitHub Actions

## 🎯 Overview
This guide shows you how to deploy your JobSearch application to AWS securely without storing AWS credentials in your repository.

## 🔧 Setup Steps

### 1. Create AWS IAM Role for GitHub Actions

First, create an IAM role that GitHub Actions can assume using OpenID Connect (OIDC):

```bash
# 1. Create the OIDC identity provider (one-time setup)
aws iam create-open-id-connect-provider \
  --url https://token.actions.githubusercontent.com \
  --client-id-list sts.amazonaws.com \
  --thumbprint-list 6938fd4d98bab03faadb97b34396831e3780aea1 \
  --thumbprint-list 1c58a3a8518e8759bf075b76b750d4f2df264fcd

# 2. Create the trust policy file
cat > github-actions-trust-policy.json << 'EOF'
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::YOUR_ACCOUNT_ID:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action": "sts:AssumeRole",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
        },
        "StringLike": {
          "token.actions.githubusercontent.com:sub": "repo:YOUR_GITHUB_USERNAME/jobsearchnew:*"
        }
      }
    }
  ]
}
EOF

# 3. Replace placeholders in the trust policy
# Replace YOUR_ACCOUNT_ID with your AWS account ID
# Replace YOUR_GITHUB_USERNAME with your GitHub username

# 4. Create the IAM role
aws iam create-role \
  --role-name GitHubActionsRole \
  --assume-role-policy-document file://github-actions-trust-policy.json \
  --description "Role for GitHub Actions to deploy JobSearch application"

# 5. Create the deployment policy
aws iam create-policy \
  --policy-name JobSearchDeploymentPolicy \
  --policy-document file://deployment-policy.json \
  --description "Policy for JobSearch application deployment"

# 6. Attach the policy to the role
aws iam attach-role-policy \
  --role-name GitHubActionsRole \
  --policy-arn arn:aws:iam::YOUR_ACCOUNT_ID:policy/JobSearchDeploymentPolicy

# 7. Get the role ARN (save this for GitHub secrets)
aws iam get-role --role-name GitHubActionsRole --query 'Role.Arn' --output text
```

### 2. Configure GitHub Repository Secrets

Go to your GitHub repository → Settings → Secrets and variables → Actions

Add these repository secrets:

| Secret Name | Value | Description |
|-------------|-------|-------------|
| `AWS_ROLE_TO_ASSUME` | `arn:aws:iam::YOUR_ACCOUNT_ID:role/GitHubActionsRole` | IAM role ARN from step 1 |
| `DATABASE_PASSWORD` | `YourSecurePassword123!` | Strong password (min 8 chars) |
| `JWT_SECRET` | `YourJWTSecret12345678901234567890123` | JWT secret (min 32 chars) |

### 3. Enable GitHub Actions

1. Go to your repository → Actions tab
2. Enable GitHub Actions if not already enabled
3. The workflow will automatically run on pushes to `main` branch

### 4. Manual Deployment

You can also trigger deployment manually:

1. Go to Actions tab → Deploy JobSearch to AWS
2. Click "Run workflow"
3. Select environment (dev/staging/prod)
4. Click "Run workflow"

## 🔐 Security Benefits

✅ **No AWS credentials stored in repository**  
✅ **Short-lived tokens (1 hour max)**  
✅ **Specific repository access only**  
✅ **Minimal required permissions**  
✅ **Full audit trail in CloudTrail**  
✅ **Role-based access control**  

## 📱 Alternative: Deploy from AWS CloudShell

If you prefer browser-based deployment:

1. Go to [AWS CloudShell](https://console.aws.amazon.com/cloudshell/)
2. Clone your repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/jobsearchnew.git
   cd jobsearchnew
   ```
3. Run deployment:
   ```bash
   chmod +x aws/deploy.sh
   ./aws/deploy.sh deploy
   ```

## 🐳 Local Development Alternative

For local testing without AWS:

```bash
# Use Docker Compose for local development
docker-compose up --build

# Access application at:
# Frontend: http://localhost:3000
# Backend: http://localhost:8080
# Database: localhost:5432
```

## 🔄 CI/CD Workflow

The GitHub Actions workflow will:

1. **Build** frontend and backend
2. **Test** application health
3. **Deploy** to AWS infrastructure
4. **Verify** deployment success
5. **Notify** of completion status

## 🛠️ Troubleshooting

### Common Issues:

**OIDC Provider Error:**
```bash
# Check if provider exists
aws iam list-open-id-connect-providers
```

**Role Assumption Error:**
```bash
# Verify role trust policy
aws iam get-role --role-name GitHubActionsRole
```

**Permission Denied:**
```bash
# Check attached policies
aws iam list-attached-role-policies --role-name GitHubActionsRole
```

## 📞 Support

If you encounter issues:

1. Check GitHub Actions logs for detailed error messages
2. Verify AWS CloudTrail for permission issues
3. Ensure all repository secrets are correctly set
4. Confirm your AWS account has sufficient free tier resources

## 🎉 What's Next?

After successful deployment:

1. **Monitor** application via CloudWatch
2. **Set up** custom domain with Route 53
3. **Enable** HTTPS with ACM certificate
4. **Configure** backup strategies
5. **Implement** monitoring and alerting