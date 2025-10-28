import React from 'react';
import { useSelector, useDispatch } from 'react-redux';
import {
  Paper,
  Card,
  CardContent,
  Typography,
  Grid,
  Chip,
  Button,
  Box,
  Pagination,
  CircularProgress,
  Alert,
  Divider
} from '@mui/material';
import {
  LocationOn as LocationIcon,
  Business as BusinessIcon,
  AttachMoney as SalaryIcon,
  OpenInNew as OpenIcon,
  Schedule as TimeIcon
} from '@mui/icons-material';
import { searchJobs } from '../store/jobSlice';
import { Job } from '../types';

interface RootState {
  jobs: {
    jobs: Job[];
    loading: boolean;
    error: string | null;
    currentPage: number;
    totalPages: number;
    totalJobs?: number;
    totalElements?: number;
  };
}

const JobResults: React.FC = () => {
  const dispatch = useDispatch<any>();
  const { jobs, loading, error, currentPage, totalPages, totalJobs } = useSelector(
    (state: RootState) => ({
      jobs: state.jobs.jobs,
      loading: state.jobs.loading,
      error: state.jobs.error,
      currentPage: state.jobs.currentPage,
      totalPages: state.jobs.totalPages,
      totalJobs: state.jobs.totalJobs || state.jobs.totalElements
    })
  );

  const handlePageChange = (event: React.ChangeEvent<unknown>, value: number) => {
    // Dispatch search with new page
    const searchParams = {
      page: value - 1, // MUI pagination is 1-indexed, backend is 0-indexed
      size: 20,
      sortBy: 'createdAt',
      sortDir: 'desc'
    };
    dispatch(searchJobs(searchParams));
  };

  const formatSalary = (min?: number, max?: number): string => {
    if (min && max) {
      return `$${min.toLocaleString()} - $${max.toLocaleString()}`;
    } else if (min) {
      return `From $${min.toLocaleString()}`;
    } else if (max) {
      return `Up to $${max.toLocaleString()}`;
    }
    return 'Salary not specified';
  };

  const formatDate = (dateString: string): string => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const getSourceColor = (source: string) => {
    switch (source) {
      case 'jsearch': return 'primary';
      case 'adzuna': return 'secondary';
      default: return 'default';
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Alert severity="error" sx={{ mb: 3 }}>
        Error loading jobs: {error}
      </Alert>
    );
  }

  if (!jobs || jobs.length === 0) {
    return (
      <Paper elevation={1} sx={{ p: 4, textAlign: 'center' }}>
        <Typography variant="h6" color="text.secondary">
          No jobs found
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Try adjusting your search criteria or fetch new jobs from external APIs
        </Typography>
      </Paper>
    );
  }

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Job Results ({totalJobs} found)
      </Typography>
      
      <Grid container spacing={3}>
        {jobs.map((job: Job) => (
          <Grid item xs={12} key={job.id}>
            <Card elevation={2} sx={{ '&:hover': { elevation: 4 } }}>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'between', alignItems: 'flex-start', mb: 2 }}>
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="h6" component="h3" gutterBottom>
                      {job.title}
                    </Typography>
                    
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1, flexWrap: 'wrap' }}>
                      {job.company && (
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                          <BusinessIcon fontSize="small" color="action" />
                          <Typography variant="body2" color="text.secondary">
                            {job.company}
                          </Typography>
                        </Box>
                      )}
                      
                      {job.location && (
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                          <LocationIcon fontSize="small" color="action" />
                          <Typography variant="body2" color="text.secondary">
                            {job.location}
                          </Typography>
                        </Box>
                      )}
                      
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                        <TimeIcon fontSize="small" color="action" />
                        <Typography variant="body2" color="text.secondary">
                          {formatDate(job.createdAt)}
                        </Typography>
                      </Box>
                    </Box>
                    
                    <Box sx={{ display: 'flex', gap: 1, mb: 2, flexWrap: 'wrap' }}>
                      <Chip 
                        label={job.source?.toUpperCase() || 'UNKNOWN'} 
                        size="small" 
                        color={getSourceColor(job.source) as any}
                      />
                      {job.isRemote && (
                        <Chip label="REMOTE" size="small" color="success" />
                      )}
                      <Chip 
                        label={`ID: ${job.id}`} 
                        size="small" 
                        variant="outlined"
                      />
                    </Box>
                  </Box>
                  
                  <Box sx={{ textAlign: 'right' }}>
                    {(job.salaryMin || job.salaryMax) && (
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mb: 1 }}>
                        <SalaryIcon fontSize="small" color="action" />
                        <Typography variant="body2" fontWeight="medium">
                          {formatSalary(job.salaryMin, job.salaryMax)}
                        </Typography>
                      </Box>
                    )}
                  </Box>
                </Box>
                
                {job.description && (
                  <Box sx={{ mb: 2 }}>
                    <Typography variant="body2" color="text.secondary" sx={{
                      display: '-webkit-box',
                      overflow: 'hidden',
                      WebkitBoxOrient: 'vertical',
                      WebkitLineClamp: 3
                    }}>
                      {job.description}
                    </Typography>
                  </Box>
                )}
                
                {job.skills && job.skills.length > 0 && (
                  <Box sx={{ mb: 2 }}>
                    <Typography variant="caption" color="text.secondary">Skills:</Typography>
                    <Box sx={{ display: 'flex', gap: 0.5, mt: 0.5, flexWrap: 'wrap' }}>
                      {job.skills.slice(0, 6).map((skill, index) => (
                        <Chip key={index} label={skill} size="small" variant="outlined" />
                      ))}
                      {job.skills.length > 6 && (
                        <Chip label={`+${job.skills.length - 6} more`} size="small" variant="outlined" />
                      )}
                    </Box>
                  </Box>
                )}
                
                <Divider sx={{ my: 1 }} />
                
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="caption" color="text.secondary">
                    Job ID: {job.id} • External ID: {job.externalId}
                  </Typography>
                  
                  {job.jobUrl && (
                    <Button
                      variant="contained"
                      size="small"
                      endIcon={<OpenIcon />}
                      href={job.jobUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      Apply
                    </Button>
                  )}
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
      
      {totalPages > 1 && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
          <Pagination
            count={totalPages}
            page={currentPage + 1} // MUI pagination is 1-indexed
            onChange={handlePageChange}
            color="primary"
            size="large"
          />
        </Box>
      )}
    </Box>
  );
};

export default JobResults;