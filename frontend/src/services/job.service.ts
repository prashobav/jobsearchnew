import axios, { AxiosResponse } from 'axios';
import { Job, JobSearchFilters, JobSearchRequest, PaginatedResponse, JobStats, JobSearchParams } from '../types';

const API_URL = 'http://localhost:8080/api/jobs/';

// Add auth header for requests
const authHeader = () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  if (user && user.accessToken) {
    return { Authorization: 'Bearer ' + user.accessToken };
  } else {
    return {};
  }
};

class JobService {
  searchJobs(
    filters: JobSearchFilters | JobSearchParams,
    page: number = 0,
    size: number = 20,
    sortBy: string = 'createdAt',
    sortDir: string = 'desc'
  ): Promise<PaginatedResponse<Job>> {
    const params = new URLSearchParams();
    
    if ('title' in filters && filters.title) params.append('title', filters.title);
    if ('company' in filters && filters.company) params.append('company', filters.company);
    if ('location' in filters && filters.location) params.append('location', filters.location);
    if ('minSalary' in filters && filters.minSalary) params.append('minSalary', filters.minSalary.toString());
    if ('maxSalary' in filters && filters.maxSalary) params.append('maxSalary', filters.maxSalary.toString());
    if ('isRemote' in filters && filters.isRemote !== undefined) params.append('isRemote', filters.isRemote.toString());
    if ('source' in filters && filters.source) params.append('source', filters.source);
    
    if ('page' in filters && filters.page !== undefined) {
      params.append('page', filters.page.toString());
    } else {
      params.append('page', page.toString());
    }
    
    if ('size' in filters && filters.size !== undefined) {
      params.append('size', filters.size.toString());
    } else {
      params.append('size', size.toString());
    }
    
    if ('sortBy' in filters && filters.sortBy) {
      params.append('sortBy', filters.sortBy);
    } else {
      params.append('sortBy', sortBy);
    }
    
    if ('sortDir' in filters && filters.sortDir) {
      params.append('sortDir', filters.sortDir);
    } else {
      params.append('sortDir', sortDir);
    }

    return axios
      .get(`${API_URL}search?${params.toString()}`)
      .then((response: AxiosResponse<PaginatedResponse<Job>>) => response.data);
  }

  getAllJobs(page: number = 0, size: number = 20): Promise<PaginatedResponse<Job>> {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sortBy', 'createdAt');
    params.append('sortDir', 'desc');

    return axios
      .get(`${API_URL}all?${params.toString()}`)
      .then((response: AxiosResponse<PaginatedResponse<Job>>) => response.data);
  }

  fetchJobs(request: JobSearchRequest): Promise<any> {
    return axios
      .post(API_URL.replace('/jobs/', '/jobs/') + 'fetch', request, { headers: authHeader() })
      .then((response: AxiosResponse) => response.data);
  }

  getJobStats(): Promise<JobStats> {
    return axios
      .get(API_URL + 'stats')
      .then((response: AxiosResponse<JobStats>) => response.data);
  }

  getDistinctLocations(): Promise<string[]> {
    return axios
      .get(API_URL + 'filters/locations')
      .then((response: AxiosResponse<string[]>) => response.data);
  }

  getDistinctCompanies(): Promise<string[]> {
    return axios
      .get(API_URL + 'filters/companies')
      .then((response: AxiosResponse<string[]>) => response.data);
  }

  getJobById(id: number): Promise<Job> {
    return axios
      .get(`${API_URL}${id}`)
      .then((response: AxiosResponse<Job>) => response.data);
  }
}

export const jobService = new JobService();
export default jobService;