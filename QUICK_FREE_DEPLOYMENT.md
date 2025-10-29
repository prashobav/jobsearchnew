# Quick Free Deployment Guide

## Option 1: Vercel + Railway (Completely Free)

### Deploy Frontend to Vercel (Free)
1. Go to: https://vercel.com
2. Sign in with GitHub
3. Import your repository: `https://github.com/prashobav/jobsearchnew`
4. Select `frontend` folder as root directory
5. Build command: `npm run build`
6. Output directory: `build`
7. Deploy!

### Deploy Backend to Railway (Free)
1. Go to: https://railway.app
2. Sign in with GitHub
3. Deploy from GitHub: Select your repository
4. Select `backend` folder
5. Railway will auto-detect Spring Boot
6. Add environment variables:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DATABASE_URL=<railway-postgres-url>`
   - `JWT_SECRET=MyJWTSecret12345678901234567890123`

### Add Database (Railway PostgreSQL - Free)
1. In Railway dashboard, click "Add Service"
2. Select "PostgreSQL"
3. Copy the connection URL
4. Update backend environment variables

## Option 2: Netlify + Render (Free)

### Frontend on Netlify
1. Go to: https://netlify.com
2. Drag and drop your `frontend/build` folder
3. Or connect GitHub repo and auto-deploy

### Backend on Render
1. Go to: https://render.com
2. Connect GitHub repository
3. Create Web Service from `backend` folder
4. Environment: Java
5. Build command: `./mvnw clean package -DskipTests`
6. Start command: `java -jar target/jobsearch-backend-1.0.0.jar`

## Option 3: Heroku (Simple but has some costs)

### Backend
```bash
# Install Heroku CLI first
heroku create jobsearch-backend-app
heroku addons:create heroku-postgresql:mini
git subtree push --prefix backend heroku main
```

### Frontend
```bash
heroku create jobsearch-frontend-app
heroku buildpacks:set mars/create-react-app
git subtree push --prefix frontend heroku main
```

## Recommended: Railway + Vercel

This is the easiest and completely free option:

1. **Frontend on Vercel**: Global CDN, automatic deployments
2. **Backend on Railway**: Free PostgreSQL included, auto-scaling
3. **Total cost**: $0/month

Would you like me to guide you through the Railway + Vercel deployment?