#!/bin/bash

# JobSearch Application Deployment Script for AWS
# This script deploys the application using AWS CLI and CloudFormation

set -e

# Configuration
PROJECT_NAME="jobsearch"
ENVIRONMENT="prod"
AWS_REGION="us-east-2"
ECR_REPOSITORY_NAME="${PROJECT_NAME}-backend"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

echo_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

echo_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    echo_info "Checking prerequisites..."
    
    if ! command -v aws &> /dev/null; then
        echo_error "AWS CLI is not installed"
        exit 1
    fi
    
    if ! command -v docker &> /dev/null; then
        echo_error "Docker is not installed"
        exit 1
    fi
    
    if ! aws sts get-caller-identity &> /dev/null; then
        echo_error "AWS credentials not configured"
        exit 1
    fi
    
    echo_info "Prerequisites check passed"
}

# Get user inputs
get_inputs() {
    echo_info "Getting deployment configuration..."
    
    read -p "Enter your AWS region (default: us-east-1): " input_region
    AWS_REGION=${input_region:-$AWS_REGION}
    
    read -s -p "Enter database password (minimum 8 characters): " DB_PASSWORD
    echo
    if [ ${#DB_PASSWORD} -lt 8 ]; then
        echo_error "Database password must be at least 8 characters"
        exit 1
    fi
    
    read -s -p "Enter JWT secret (minimum 32 characters): " JWT_SECRET
    echo
    if [ ${#JWT_SECRET} -lt 32 ]; then
        echo_error "JWT secret must be at least 32 characters"
        exit 1
    fi
}

# Create ECR repository
create_ecr_repository() {
    echo_info "Creating ECR repository..."
    
    if aws ecr describe-repositories --repository-names $ECR_REPOSITORY_NAME --region $AWS_REGION &> /dev/null; then
        echo_warn "ECR repository already exists"
    else
        aws ecr create-repository \
            --repository-name $ECR_REPOSITORY_NAME \
            --region $AWS_REGION \
            --image-scanning-configuration scanOnPush=true
        echo_info "ECR repository created"
    fi
}

# Build and push Docker image
build_and_push_image() {
    echo_info "Building and pushing Docker image..."
    
    # Get ECR login token
    aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $(aws sts get-caller-identity --query Account --output text).dkr.ecr.$AWS_REGION.amazonaws.com
    
    # Build image
    cd ../backend
    docker build -t $ECR_REPOSITORY_NAME .
    
    # Tag image
    ECR_URI=$(aws sts get-caller-identity --query Account --output text).dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY_NAME:latest
    docker tag $ECR_REPOSITORY_NAME:latest $ECR_URI
    
    # Push image
    docker push $ECR_URI
    
    cd ../aws
    echo_info "Docker image pushed to ECR"
}

# Deploy CloudFormation stack
deploy_infrastructure() {
    echo_info "Deploying CloudFormation stack..."
    
    aws cloudformation deploy \
        --template-file cloudformation-template.yaml \
        --stack-name $PROJECT_NAME-infrastructure \
        --parameter-overrides \
            ProjectName=$PROJECT_NAME \
            Environment=$ENVIRONMENT \
            DatabasePassword=$DB_PASSWORD \
            JWTSecret=$JWT_SECRET \
        --capabilities CAPABILITY_IAM \
        --region $AWS_REGION
    
    echo_info "Infrastructure deployed successfully"
}

# Build and deploy frontend
deploy_frontend() {
    echo_info "Building and deploying frontend..."
    
    # Get stack outputs
    S3_BUCKET=$(aws cloudformation describe-stacks \
        --stack-name $PROJECT_NAME-infrastructure \
        --region $AWS_REGION \
        --query 'Stacks[0].Outputs[?OutputKey==`S3BucketName`].OutputValue' \
        --output text)
    
    ALB_DNS=$(aws cloudformation describe-stacks \
        --stack-name $PROJECT_NAME-infrastructure \
        --region $AWS_REGION \
        --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerDNS`].OutputValue' \
        --output text)
    
    CLOUDFRONT_URL=$(aws cloudformation describe-stacks \
        --stack-name $PROJECT_NAME-infrastructure \
        --region $AWS_REGION \
        --query 'Stacks[0].Outputs[?OutputKey==`CloudFrontURL`].OutputValue' \
        --output text)
    
    # Create production environment file
    cd ../frontend
    cat > .env.production << EOF
REACT_APP_API_URL=http://$ALB_DNS/api
REACT_APP_ENV=production
REACT_APP_ENABLE_MOCK_SERVICES=false
GENERATE_SOURCEMAP=false
EOF
    
    # Build frontend
    npm run build:prod
    
    # Deploy to S3
    aws s3 sync build/ s3://$S3_BUCKET --delete --region $AWS_REGION
    
    # Invalidate CloudFront cache
    DISTRIBUTION_ID=$(aws cloudfront list-distributions \
        --query "DistributionList.Items[?Comment=='CloudFront distribution for $PROJECT_NAME'].Id" \
        --output text)
    
    if [ ! -z "$DISTRIBUTION_ID" ]; then
        aws cloudfront create-invalidation \
            --distribution-id $DISTRIBUTION_ID \
            --paths "/*"
    fi
    
    cd ../aws
    echo_info "Frontend deployed successfully"
    echo_info "Frontend URL: $CLOUDFRONT_URL"
    echo_info "Backend URL: http://$ALB_DNS"
}

# Main deployment function
deploy() {
    echo_info "Starting deployment of JobSearch application..."
    
    check_prerequisites
    get_inputs
    create_ecr_repository
    build_and_push_image
    deploy_infrastructure
    
    # Wait for infrastructure to be ready
    echo_info "Waiting for infrastructure to be ready..."
    sleep 60
    
    deploy_frontend
    
    echo_info "Deployment completed successfully!"
    echo_info "Your application is now available at the CloudFront URL above"
}

# Cleanup function
cleanup() {
    echo_info "Cleaning up resources..."
    
    # Delete CloudFormation stack
    aws cloudformation delete-stack \
        --stack-name $PROJECT_NAME-infrastructure \
        --region $AWS_REGION
    
    # Delete ECR repository
    aws ecr delete-repository \
        --repository-name $ECR_REPOSITORY_NAME \
        --region $AWS_REGION \
        --force
    
    echo_info "Cleanup completed"
}

# Help function
show_help() {
    echo "Usage: $0 [OPTION]"
    echo ""
    echo "Options:"
    echo "  deploy    Deploy the application to AWS"
    echo "  cleanup   Remove all AWS resources"
    echo "  help      Show this help message"
    echo ""
    echo "Example:"
    echo "  $0 deploy"
}

# Main script logic
case "${1:-deploy}" in
    deploy)
        deploy
        ;;
    cleanup)
        cleanup
        ;;
    help)
        show_help
        ;;
    *)
        echo_error "Unknown option: $1"
        show_help
        exit 1
        ;;
esac