import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  Paper,
  TextField,
  Button,
  Grid,
  FormControlLabel,
  Switch,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Box,
  Chip,
  Typography,
  CircularProgress
} from '@mui/material';
import { Search as SearchIcon, Clear as ClearIcon } from '@mui/icons-material';
import { searchJobs, fetchNewJobs, clearJobs } from '../store/jobSlice';
import { JobSearchParams } from '../types';

interface RootState {
  jobs: {
    loading: boolean;
    totalJobs?: number;
    totalElements?: number;
  };
  auth: {
    isLoggedIn: boolean;
  };
}

const JobSearchForm: React.FC = () => {
  const dispatch = useDispatch<any>();
  const { loading, totalJobs } = useSelector((state: RootState) => ({ 
    loading: state.jobs.loading, 
    totalJobs: state.jobs.totalJobs || state.jobs.totalElements 
  }));
  const isLoggedIn = useSelector((state: RootState) => state.auth.isLoggedIn);

  const [searchParams, setSearchParams] = useState<JobSearchParams>({
    title: '',
    location: '',
    company: '',
    minSalary: undefined,
    maxSalary: undefined,
    isRemote: undefined,
    source: '',
    page: 0,
    size: 20,
    sortBy: 'createdAt',
    sortDir: 'desc'
  });

  const [fetchParams, setFetchParams] = useState({
    jobTitle: '',
    location: '',
    maxResults: 50
  });

  const handleSearchParamChange = (field: keyof JobSearchParams, value: any) => {
    setSearchParams((prev: JobSearchParams) => ({
      ...prev,
      [field]: value,
      page: 0 // Reset page when changing search criteria
    }));
  };

  const handleSearch = () => {
    dispatch(searchJobs(searchParams));
  };

  const handleFetchJobs = () => {
    if (fetchParams.jobTitle.trim()) {
      dispatch(fetchNewJobs(fetchParams));
    }
  };

  const handleClearSearch = () => {
    setSearchParams({
      title: '',
      location: '',
      company: '',
      minSalary: undefined,
      maxSalary: undefined,
      isRemote: undefined,
      source: '',
      page: 0,
      size: 20,
      sortBy: 'createdAt',
      sortDir: 'desc'
    });
    dispatch(clearJobs());
  };

  return (
    <Paper elevation={3} sx={{ p: 4, mb: 4, backgroundColor: '#f8f9fa' }}>
      <Typography variant="h5" gutterBottom sx={{ fontWeight: 'bold', color: 'primary.main', mb: 3 }}>
        🔍 Search & Filter Jobs
      </Typography>
      
      <Typography variant="h6" gutterBottom sx={{ mb: 2, color: 'text.secondary' }}>
        Filter Existing Jobs
      </Typography>
      
      {/* Search existing jobs */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} md={4}>
          <TextField
            fullWidth
            label="🎯 Job Title/Role"
            value={searchParams.title}
            onChange={(e) => handleSearchParamChange('title', e.target.value)}
            placeholder="e.g. Software Developer, Data Scientist"
            variant="outlined"
            sx={{ backgroundColor: 'white' }}
          />
        </Grid>
        <Grid item xs={12} md={4}>
          <TextField
            fullWidth
            label="📍 Location"
            value={searchParams.location}
            onChange={(e) => handleSearchParamChange('location', e.target.value)}
            placeholder="e.g. New York, London, Remote"
            variant="outlined"
            sx={{ backgroundColor: 'white' }}
          />
        </Grid>
        <Grid item xs={12} md={4}>
          <TextField
            fullWidth
            label="🏢 Company"
            value={searchParams.company}
            onChange={(e) => handleSearchParamChange('company', e.target.value)}
            placeholder="e.g. Google, Microsoft"
            variant="outlined"
            sx={{ backgroundColor: 'white' }}
          />
        </Grid>
        
        <Grid item xs={12} md={3}>
          <TextField
            fullWidth
            label="💰 Min Salary"
            type="number"
            value={searchParams.minSalary || ''}
            onChange={(e) => handleSearchParamChange('minSalary', e.target.value ? parseInt(e.target.value) : undefined)}
            placeholder="50000"
            variant="outlined"
            sx={{ backgroundColor: 'white' }}
          />
        </Grid>
        <Grid item xs={12} md={3}>
          <TextField
            fullWidth
            label="💸 Max Salary"
            type="number"
            value={searchParams.maxSalary || ''}
            onChange={(e) => handleSearchParamChange('maxSalary', e.target.value ? parseInt(e.target.value) : undefined)}
            placeholder="150000"
            variant="outlined"
            sx={{ backgroundColor: 'white' }}
          />
        </Grid>
        <Grid item xs={12} md={3}>
          <FormControl fullWidth sx={{ backgroundColor: 'white' }}>
            <InputLabel>🔗 Job Source</InputLabel>
            <Select
              value={searchParams.source}
              label="🔗 Job Source"
              onChange={(e) => handleSearchParamChange('source', e.target.value)}
              variant="outlined"
            >
              <MenuItem value="">All Sources</MenuItem>
              <MenuItem value="jsearch">JSearch/Indeed</MenuItem>
              <MenuItem value="adzuna">Adzuna</MenuItem>
            </Select>
          </FormControl>
        </Grid>
        <Grid item xs={12} md={3}>
          <Paper sx={{ p: 2, backgroundColor: 'white', display: 'flex', alignItems: 'center', height: '56px' }}>
            <FormControlLabel
              control={
                <Switch
                  checked={searchParams.isRemote === true}
                  onChange={(e) => handleSearchParamChange('isRemote', e.target.checked ? true : undefined)}
                  color="primary"
                />
              }
              label="🏠 Remote Only"
              sx={{ fontWeight: 'medium' }}
            />
          </Paper>
        </Grid>
      </Grid>

      <Box sx={{ display: 'flex', gap: 2, mb: 3, flexWrap: 'wrap' }}>
        <Button
          variant="contained"
          startIcon={<SearchIcon />}
          onClick={handleSearch}
          disabled={loading}
        >
          Search Jobs
        </Button>
        <Button
          variant="outlined"
          startIcon={<ClearIcon />}
          onClick={handleClearSearch}
        >
          Clear
        </Button>
        {(totalJobs && totalJobs > 0) && (
          <Chip 
            label={`${totalJobs} jobs found`} 
            color="primary" 
            variant="outlined" 
          />
        )}
      </Box>

      {/* Fetch new jobs section */}
      <Box sx={{ borderTop: 2, borderColor: 'primary.main', pt: 4, mt: 4 }}>
        <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold', color: 'secondary.main', mb: 3 }}>
          🚀 Fetch New Jobs from External APIs
        </Typography>
        <Grid container spacing={3} sx={{ mb: 3 }}>
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              label="🎯 Job Role to Fetch"
              value={fetchParams.jobTitle}
              onChange={(e) => setFetchParams(prev => ({ ...prev, jobTitle: e.target.value }))}
              placeholder="e.g. React Developer"
              variant="outlined"
              sx={{ backgroundColor: 'white' }}
            />
          </Grid>
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              label="📍 Location to Fetch"
              value={fetchParams.location}
              onChange={(e) => setFetchParams(prev => ({ ...prev, location: e.target.value }))}
              placeholder="e.g. San Francisco"
              variant="outlined"
              sx={{ backgroundColor: 'white' }}
            />
          </Grid>
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              label="📊 Max Results"
              type="number"
              value={fetchParams.maxResults}
              onChange={(e) => setFetchParams(prev => ({ ...prev, maxResults: parseInt(e.target.value) || 50 }))}
              placeholder="50"
              variant="outlined"
              sx={{ backgroundColor: 'white' }}
            />
          </Grid>
        </Grid>
        
        <Button
          variant="contained"
          color="secondary"
          onClick={handleFetchJobs}
          disabled={loading || !fetchParams.jobTitle.trim() || !isLoggedIn}
          startIcon={loading ? <CircularProgress size={20} /> : undefined}
        >
          {loading ? 'Fetching...' : 'Fetch New Jobs'}
        </Button>
        {!isLoggedIn && (
          <Typography variant="caption" color="error" sx={{ ml: 2 }}>
            Please login to fetch new jobs
          </Typography>
        )}
      </Box>
    </Paper>
  );
};

export default JobSearchForm;