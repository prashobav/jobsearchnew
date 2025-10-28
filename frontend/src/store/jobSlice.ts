import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import jobService from '../services/job.service';
import { Job, JobSearchFilters, JobSearchRequest, JobSearchParams, PaginatedResponse } from '../types';

interface JobState {
  jobs: Job[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  loading: boolean;
  error: string | null;
  filters: JobSearchFilters;
  totalJobs: number;
  searchParams: JobSearchParams | null;
}

const initialState: JobState = {
  jobs: [],
  totalElements: 0,
  totalPages: 0,
  currentPage: 0,
  loading: false,
  error: null,
  filters: {},
  totalJobs: 0,
  searchParams: null,
};

// Async thunks
export const searchJobs = createAsyncThunk(
  'jobs/search',
  async (searchParams: JobSearchParams) => {
    const response = await jobService.searchJobs(searchParams);
    return { data: response, params: searchParams };
  }
);

export const fetchNewJobs = createAsyncThunk(
  'jobs/fetchNew',
  async (request: JobSearchRequest) => {
    const response = await jobService.fetchJobs(request);
    return response;
  }
);

export const getAllJobs = createAsyncThunk(
  'jobs/getAll',
  async (params: { page?: number; size?: number }) => {
    const response = await jobService.getAllJobs(params.page || 0, params.size || 20);
    return response;
  }
);

const jobSlice = createSlice({
  name: 'jobs',
  initialState,
  reducers: {
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.loading = action.payload;
    },
    setJobs: (state, action: PayloadAction<PaginatedResponse<Job>>) => {
      state.jobs = action.payload.content;
      state.totalElements = action.payload.totalElements;
      state.totalPages = action.payload.totalPages;
      state.currentPage = action.payload.number;
      state.totalJobs = action.payload.totalElements;
      state.loading = false;
      state.error = null;
    },
    setError: (state, action: PayloadAction<string>) => {
      state.error = action.payload;
      state.loading = false;
    },
    setFilters: (state, action: PayloadAction<JobSearchFilters>) => {
      state.filters = action.payload;
    },
    clearJobs: (state) => {
      state.jobs = [];
      state.totalElements = 0;
      state.totalPages = 0;
      state.currentPage = 0;
      state.totalJobs = 0;
      state.searchParams = null;
    },
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Search jobs
      .addCase(searchJobs.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(searchJobs.fulfilled, (state, action) => {
        state.loading = false;
        const response = action.payload.data as PaginatedResponse<Job>;
        state.jobs = response.content || [];
        state.totalElements = response.totalElements || 0;
        state.totalJobs = response.totalElements || 0;
        state.currentPage = response.number || 0;
        state.totalPages = response.totalPages || 0;
        state.searchParams = action.payload.params;
      })
      .addCase(searchJobs.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to search jobs';
      })
      // Fetch new jobs
      .addCase(fetchNewJobs.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchNewJobs.fulfilled, (state, action) => {
        state.loading = false;
        // After fetching new jobs, we could trigger a new search
      })
      .addCase(fetchNewJobs.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch new jobs';
      })
      // Get all jobs
      .addCase(getAllJobs.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getAllJobs.fulfilled, (state, action) => {
        state.loading = false;
        const response = action.payload as PaginatedResponse<Job>;
        state.jobs = response.content || [];
        state.totalElements = response.totalElements || 0;
        state.totalJobs = response.totalElements || 0;
        state.currentPage = response.number || 0;
        state.totalPages = response.totalPages || 0;
      })
      .addCase(getAllJobs.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to get jobs';
      });
  },
});

export const { setLoading, setJobs, setError, setFilters, clearJobs, clearError } = jobSlice.actions;
export default jobSlice.reducer;