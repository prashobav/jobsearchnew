import React, { useState, useEffect } from 'react';
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
  CircularProgress,
  Alert
} from '@mui/material';
import { Search as SearchIcon, Clear as ClearIcon, Refresh as RefreshIcon } from '@mui/icons-material';
import { searchJobs, fetchNewJobs, clearJobs, getAllJobs } from '../store/jobSlice';
import { JobSearchParams } from '../types';

interface RootState {
  jobs: {
    loading: boolean;
    totalJobs?: number;
    totalElements?: number;
    error: string | null;
  };
  auth: {
    isLoggedIn: boolean;
  };
}

const JobSearchForm: React.FC = () => {
  const dispatch = useDispatch<any>();
  const { loading, totalJobs, error } = useSelector((state: RootState) => ({ 
    loading: state.jobs.loading, 
    totalJobs: state.jobs.totalJobs || state.jobs.totalElements,
    error: state.jobs.error
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

  const [fetchSuccess, setFetchSuccess] = useState<string | null>(null);
  const [isAutoReloading, setIsAutoReloading] = useState(false);

  // Auto-load all jobs on component mount
  useEffect(() => {
    dispatch(getAllJobs({ page: 0, size: 20 }));
  }, [dispatch]);

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

  const handleFetchJobs = async () => {
    if (fetchParams.jobTitle.trim()) {
      try {
        setFetchSuccess(null);
        const result = await dispatch(fetchNewJobs(fetchParams)).unwrap();
        setFetchSuccess(`Successfully started fetching jobs for "${fetchParams.jobTitle}". Loading results...`);
        
        // Auto-reload jobs after a delay to show newly fetched jobs
        setIsAutoReloading(true);
        setTimeout(() => {
          dispatch(getAllJobs({ page: 0, size: 20 }));
          setIsAutoReloading(false);
          setFetchSuccess(`✅ Jobs fetched and refreshed! Search for "${fetchParams.jobTitle}" to see new results.`);
        }, 5000); // 5 second delay for backend processing
        
      } catch (error) {
        console.error('Fetch failed:', error);
      }
    }
  };

  const handleRefreshJobs = () => {
    dispatch(getAllJobs({ page: 0, size: 20 }));
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
            placeholder="e.g. Bangalore, Mumbai, Pune, Remote"
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
            placeholder="e.g. Infosys, TCS, Wipro, Amazon"
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
            placeholder="500000"
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
            placeholder="1500000"
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
        <Button
          variant="outlined"
          startIcon={<RefreshIcon />}
          onClick={handleRefreshJobs}
          disabled={loading}
        >
          Refresh All Jobs
        </Button>
        {(totalJobs && totalJobs > 0) && (
          <Chip 
            label={`${totalJobs} jobs found`} 
            color="primary" 
            variant="outlined" 
          />
        )}
      </Box>

      {/* Show error message if any */}
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

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
              placeholder="e.g. Bangalore"
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
          disabled={loading || isAutoReloading || !fetchParams.jobTitle.trim()}
          startIcon={loading || isAutoReloading ? <CircularProgress size={20} /> : undefined}
        >
          {loading ? 'Fetching...' : isAutoReloading ? 'Auto-refreshing...' : 'Fetch New Jobs'}
        </Button>
        {!isLoggedIn && (
          <Typography variant="caption" color="error" sx={{ ml: 2 }}>
            Please login to fetch new jobs
          </Typography>
        )}
        
        {/* Show fetch success message */}
        {fetchSuccess && (
          <Alert severity="success" sx={{ mt: 2 }}>
            {fetchSuccess}
          </Alert>
        )}
      </Box>
    </Paper>
  );
};

export default JobSearchForm;