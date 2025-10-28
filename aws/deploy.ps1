# JobSearch Application AWS Deployment
# PowerShell script for Windows deployment

param(
    [Parameter()]
    [ValidateSet("deploy", "cleanup", "help")]
    [string]$Action = "deploy",
    
    [Parameter()]
    [string]$Region = "us-east-1",
    
    [Parameter()]
    [string]$ProjectName = "jobsearch"
)

# Configuration
$Environment = "prod"
$ECRRepositoryName = "$ProjectName-backend"
$Region = "us-east-2"

# Helper functions
function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Green
}

function Write-Warn {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

function Test-Prerequisites {
    Write-Info "Checking prerequisites..."
    
    # Check AWS CLI
    try {
        aws --version | Out-Null
    }
    catch {
        Write-Error "AWS CLI is not installed or not in PATH"
        exit 1
    }
    
    # Check Docker
    try {
        docker --version | Out-Null
    }
    catch {
        Write-Error "Docker is not installed or not running"
        exit 1
    }
    
    # Check AWS credentials
    try {
        aws sts get-caller-identity | Out-Null
    }
    catch {
        Write-Error "AWS credentials not configured. Run 'aws configure'"
        exit 1
    }
    
    Write-Info "Prerequisites check passed"
}

function Get-UserInputs {
    Write-Info "Getting deployment configuration..."
    
    $script:DatabasePassword = Read-Host "Enter database password (minimum 8 characters)" -AsSecureString
    $DatabasePasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($script:DatabasePassword))
    
    if ($DatabasePasswordPlain.Length -lt 8) {
        Write-Error "Database password must be at least 8 characters"
        exit 1
    }
    
    $script:JWTSecret = Read-Host "Enter JWT secret (minimum 32 characters)" -AsSecureString
    $JWTSecretPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($script:JWTSecret))
    
    if ($JWTSecretPlain.Length -lt 32) {
        Write-Error "JWT secret must be at least 32 characters"
        exit 1
    }
    
    $script:DatabasePasswordPlain = $DatabasePasswordPlain
    $script:JWTSecretPlain = $JWTSecretPlain
}

function New-ECRRepository {
    Write-Info "Creating ECR repository..."
    
    $repoExists = aws ecr describe-repositories --repository-names $ECRRepositoryName --region $Region 2>$null
    
    if ($repoExists) {
        Write-Warn "ECR repository already exists"
    }
    else {
        aws ecr create-repository --repository-name $ECRRepositoryName --region $Region --image-scanning-configuration scanOnPush=true
        Write-Info "ECR repository created"
    }
}

function Build-AndPushImage {
    Write-Info "Building and pushing Docker image..."
    
    # Get ECR login token
    $loginCommand = aws ecr get-login-password --region $Region
    $accountId = aws sts get-caller-identity --query Account --output text
    $ecrUri = "$accountId.dkr.ecr.$Region.amazonaws.com"
    
    echo $loginCommand | docker login --username AWS --password-stdin $ecrUri
    
    # Build image
    Set-Location -Path "..\backend"
    docker build -t $ECRRepositoryName .
    
    # Tag and push image
    $imageUri = "$ecrUri/$ECRRepositoryName`:latest"
    docker tag "$ECRRepositoryName`:latest" $imageUri
    docker push $imageUri
    
    Set-Location -Path "..\aws"
    Write-Info "Docker image pushed to ECR"
}

function Deploy-Infrastructure {
    Write-Info "Deploying CloudFormation stack..."
    
    aws cloudformation deploy `
        --template-file cloudformation-template.yaml `
        --stack-name "$ProjectName-infrastructure" `
        --parameter-overrides `
            "ProjectName=$ProjectName" `
            "Environment=$Environment" `
            "DatabasePassword=$script:DatabasePasswordPlain" `
            "JWTSecret=$script:JWTSecretPlain" `
        --capabilities CAPABILITY_IAM `
        --region $Region
    
    Write-Info "Infrastructure deployed successfully"
}

function Deploy-Frontend {
    Write-Info "Building and deploying frontend..."
    
    # Get stack outputs
    $s3Bucket = aws cloudformation describe-stacks `
        --stack-name "$ProjectName-infrastructure" `
        --region $Region `
        --query 'Stacks[0].Outputs[?OutputKey==`S3BucketName`].OutputValue' `
        --output text
    
    $albDns = aws cloudformation describe-stacks `
        --stack-name "$ProjectName-infrastructure" `
        --region $Region `
        --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerDNS`].OutputValue' `
        --output text
    
    $cloudFrontUrl = aws cloudformation describe-stacks `
        --stack-name "$ProjectName-infrastructure" `
        --region $Region `
        --query 'Stacks[0].Outputs[?OutputKey==`CloudFrontURL`].OutputValue' `
        --output text
    
    # Create production environment file
    Set-Location -Path "..\frontend"
    
    @"
REACT_APP_API_URL=http://$albDns/api
REACT_APP_ENV=production
REACT_APP_ENABLE_MOCK_SERVICES=false
GENERATE_SOURCEMAP=false
"@ | Out-File -FilePath ".env.production" -Encoding utf8
    
    # Build frontend
    npm run build
    
    # Deploy to S3
    aws s3 sync build/ "s3://$s3Bucket" --delete --region $Region
    
    # Invalidate CloudFront cache
    $distributionId = aws cloudfront list-distributions `
        --query "DistributionList.Items[?Comment=='CloudFront distribution for $ProjectName'].Id" `
        --output text
    
    if ($distributionId) {
        aws cloudfront create-invalidation --distribution-id $distributionId --paths "/*"
    }
    
    Set-Location -Path "..\aws"
    Write-Info "Frontend deployed successfully"
    Write-Info "Frontend URL: $cloudFrontUrl"
    Write-Info "Backend URL: http://$albDns"
}

function Invoke-Deployment {
    Write-Info "Starting deployment of JobSearch application..."
    
    Test-Prerequisites
    Get-UserInputs
    New-ECRRepository
    Build-AndPushImage
    Deploy-Infrastructure
    
    # Wait for infrastructure to be ready
    Write-Info "Waiting for infrastructure to be ready..."
    Start-Sleep -Seconds 60
    
    Deploy-Frontend
    
    Write-Info "Deployment completed successfully!"
    Write-Info "Your application is now available at the CloudFront URL above"
}

function Invoke-Cleanup {
    Write-Info "Cleaning up resources..."
    
    # Delete CloudFormation stack
    aws cloudformation delete-stack --stack-name "$ProjectName-infrastructure" --region $Region
    
    # Delete ECR repository
    aws ecr delete-repository --repository-name $ECRRepositoryName --region $Region --force
    
    Write-Info "Cleanup completed"
}

function Show-Help {
    Write-Host @"
Usage: .\deploy.ps1 [-Action <action>] [-Region <region>] [-ProjectName <name>]

Parameters:
  -Action       Action to perform: deploy, cleanup, help (default: deploy)
  -Region       AWS region (default: us-east-1)
  -ProjectName  Project name (default: jobsearch)

Examples:
  .\deploy.ps1 -Action deploy
  .\deploy.ps1 -Action cleanup
  .\deploy.ps1 -Action deploy -Region us-west-2
"@
}

# Main script logic
switch ($Action) {
    "deploy" {
        Invoke-Deployment
    }
    "cleanup" {
        Invoke-Cleanup
    }
    "help" {
        Show-Help
    }
    default {
        Write-Error "Unknown action: $Action"
        Show-Help
        exit 1
    }
}