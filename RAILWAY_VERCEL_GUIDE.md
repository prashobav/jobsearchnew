# Railway + Vercel Deployment - Step by Step

## Part 1: Deploy Backend to Railway (5 minutes)

### Step 1: Sign Up for Railway
1. Go to: https://railway.app
2. Click "Login" → "Login with GitHub"
3. Authorize Railway to access your GitHub account

### Step 2: Deploy Backend
1. Click "Deploy from GitHub repo"
2. Select: `prashobav/jobsearchnew`
3. Railway will detect multiple services - select the **backend** folder
4. Click "Deploy"

### Step 3: Add Database
1. In your Railway project dashboard, click "New Service"
2. Select "Database" → "PostgreSQL"
3. Railway will create a free PostgreSQL database
4. Copy the connection details (you'll need them)

### Step 4: Configure Environment Variables
1. Click on your backend service
2. Go to "Variables" tab
3. Add these variables:
   ```
   SPRING_PROFILES_ACTIVE=prod
   DATABASE_URL=postgresql://[username]:[password]@[host]:[port]/[database]
   JWT_SECRET=MyJWTSecret12345678901234567890123
   ```
4. Use the PostgreSQL connection details from step 3

### Step 5: Custom Start Command (if needed)
1. In "Settings" tab
2. Add custom start command: `java -jar target/jobsearch-backend-1.0.0.jar`

## Part 2: Deploy Frontend to Vercel (3 minutes)

### Step 1: Sign Up for Vercel
1. Go to: https://vercel.com
2. Click "Continue with GitHub"
3. Authorize Vercel

### Step 2: Import Project
1. Click "Add New" → "Project"
2. Import `prashobav/jobsearchnew`
3. When asked for framework, select "Create React App"
4. Set **Root Directory** to `frontend`

### Step 3: Configure Build Settings
- **Framework Preset**: Create React App
- **Root Directory**: `frontend`
- **Build Command**: `npm run build`
- **Output Directory**: `build`

### Step 4: Environment Variables
1. Add environment variable:
   ```
   REACT_APP_API_URL=https://[your-railway-backend-url]
   ```
2. Get the Railway backend URL from Railway dashboard

### Step 5: Deploy
1. Click "Deploy"
2. Wait 2-3 minutes for build to complete
3. You'll get a live URL like: `https://jobsearchnew.vercel.app`

## Part 3: Update API URL in Frontend

### Update the Frontend to Use Railway Backend
1. In your local code, update `frontend/src/services/auth.service.ts`
2. Replace the API_URL with your Railway backend URL
3. Commit and push to GitHub
4. Vercel will auto-deploy the update

## Expected Results

After deployment:
- **Frontend**: `https://your-app.vercel.app`
- **Backend API**: `https://your-backend.railway.app`
- **Database**: Managed PostgreSQL on Railway

## Benefits of This Approach

✅ **Completely Free**: Both services have generous free tiers  
✅ **Auto-deployments**: Updates when you push to GitHub  
✅ **No CLI issues**: All done through web interfaces  
✅ **Global CDN**: Vercel provides worldwide fast loading  
✅ **SSL included**: HTTPS automatically configured  
✅ **Easy scaling**: Can handle thousands of users  

## Cost Breakdown

- **Railway**: $0/month (500 hours free execution time)
- **Vercel**: $0/month (100GB bandwidth free)
- **Total**: $0/month for your usage level

This is much simpler than AWS and perfect for a job search application!