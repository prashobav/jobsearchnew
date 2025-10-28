import axios from 'axios';
import { AuthResponse, LoginRequest, SignupRequest } from '../types';

const API_URL = '/api/auth/';

class AuthService {
  login(loginRequest: LoginRequest): Promise<AuthResponse> {
    return axios
      .post(API_URL + 'signin', loginRequest)
      .then(response => {
        if (response.data.accessToken) {
          localStorage.setItem('user', JSON.stringify(response.data));
        }
        return response.data;
      });
  }

  register(signupRequest: SignupRequest): Promise<any> {
    return axios.post(API_URL + 'signup', signupRequest);
  }

  logout() {
    localStorage.removeItem('user');
  }

  getCurrentUser(): AuthResponse | null {
    const userStr = localStorage.getItem('user');
    if (userStr) return JSON.parse(userStr);
    return null;
  }
}

export default new AuthService();