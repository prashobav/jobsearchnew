#!/bin/bash
# CloudShell Deployment Commands for JobSearch Application
# Copy and paste these commands one by one into AWS CloudShell

echo "=== JobSearch AWS CloudShell Deployment ==="
echo "Starting deployment process..."

# 1. Clone your repository
echo "Step 1: Cloning repository..."
git clone https://github.com/yourusername/jobsearchnew.git
cd jobsearchnew

# 2. Verify we're in the right region
echo "Step 2: Verifying AWS region..."
aws configure get region
aws sts get-caller-identity

# 3. Install Docker (if needed)
echo "Step 3: Checking Docker..."
if ! command -v docker &> /dev/null; then
    echo "Installing Docker..."
    sudo yum update -y
    sudo yum install -y docker
    sudo service docker start
    sudo usermod -a -G docker cloudshell-user
fi

# 4. Make deployment script executable
echo "Step 4: Preparing deployment script..."
chmod +x aws/deploy.sh

# 5. Run deployment
echo "Step 5: Starting deployment..."
echo "You'll be prompted for:"
echo "- Database password (min 8 chars): SecurePass123!"
echo "- JWT secret (min 32 chars): MyJWTSecret12345678901234567890123"
echo ""
echo "Running deployment script..."
./aws/deploy.sh deploy

echo "Deployment completed!"
echo "Check the output for your application URLs"