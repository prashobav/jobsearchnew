import axios from 'axios';
import { AuthResponse, LoginRequest, SignupRequest, ForgotPasswordRequest, ResetPasswordRequest, MessageResponse } from '../types';

const API_URL = 'http://localhost:8080/api/auth/';

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

  forgotPassword(request: ForgotPasswordRequest): Promise<MessageResponse> {
    return axios.post(API_URL + 'forgot-password', request)
      .then(response => response.data);
  }

  resetPassword(request: ResetPasswordRequest): Promise<MessageResponse> {
    return axios.post(API_URL + 'reset-password', request)
      .then(response => response.data);
  }

  validateResetToken(token: string): Promise<MessageResponse> {
    return axios.get(API_URL + 'validate-reset-token', {
      params: { token }
    }).then(response => response.data);
  }
}

export default new AuthService();