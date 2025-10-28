import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  Paper,
  TextField,
  Button,
  Typography,
  Box,
  Tab,
  Tabs,
  Alert,
  CircularProgress
} from '@mui/material';
import { Login as LoginIcon, PersonAdd as SignupIcon, Logout as LogoutIcon } from '@mui/icons-material';
import { loginSuccess, logout } from '../store/authSlice';
import AuthService from '../services/auth.service';
import { LoginRequest, SignupRequest, ForgotPasswordRequest, MessageResponse } from '../types';

interface RootState {
  auth: {
    user: any;
    isLoggedIn: boolean;
  };
}

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`auth-tabpanel-${index}`}
      aria-labelledby={`auth-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ p: 3 }}>
          {children}
        </Box>
      )}
    </div>
  );
}

const AuthComponent: React.FC = () => {
  const dispatch = useDispatch<any>();
  const { user: reduxUser, isLoggedIn } = useSelector((state: RootState) => state.auth);
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  
  const [tabValue, setTabValue] = useState(0);
  const [forgotPasswordEmail, setForgotPasswordEmail] = useState('');
  const [loginForm, setLoginForm] = useState({ username: '', password: '' });
  const [signupForm, setSignupForm] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  });

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
    setError(null);
    setSuccess(null);
  };

  const handleLogin = async () => {
    if (!loginForm.username || !loginForm.password) {
      setError('Please fill in all fields');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await AuthService.login(loginForm);
      dispatch(loginSuccess(response));
      localStorage.setItem('user', JSON.stringify(response));
      setLoginForm({ username: '', password: '' });
    } catch (err: any) {
      console.error('Login error details:', err);
      
      // Log the full error for debugging
      console.log('Error response:', err.response);
      console.log('Error data:', err.response?.data);
      console.log('Error status:', err.response?.status);
      
      // Extract meaningful error message
      let errorMessage = 'Login failed';
      
      if (err.response?.data?.message) {
        errorMessage = err.response.data.message;
      } else if (err.response?.status === 401) {
        errorMessage = 'Invalid username or password. Please check your credentials.';
      } else if (err.response?.status >= 500) {
        errorMessage = 'Server error. Please try again later.';
      } else if (err.message) {
        errorMessage = `Login failed: ${err.message}`;
      }
      
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleSignup = async () => {
    if (!signupForm.username || !signupForm.email || !signupForm.password) {
      setError('Please fill in all fields');
      return;
    }

    if (signupForm.password !== signupForm.confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    if (signupForm.password.length < 6) {
      setError('Password must be at least 6 characters long');
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      // Step 1: Register the user (just register, don't auto-login)
      const response = await AuthService.register(signupForm);
      console.log('Signup response:', response);
      
      setSuccess('Account created successfully! Please login with your credentials.');
      setSignupForm({ username: '', email: '', password: '', confirmPassword: '' });
      
      // Switch to login tab after successful signup
      setTimeout(() => {
        setTabValue(0); // Switch to login tab
        setSuccess(null);
      }, 2000);
      
    } catch (err: any) {
      console.error('Signup error:', err);
      const errorMessage = err.response?.data?.message || 'Signup failed';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // Debug function to test database connection
  const testDatabaseConnection = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/auth/debug/users');
      const data = await response.text();
      console.log('Database users:', data);
      alert('Check browser console for user list');
    } catch (err) {
      console.error('Database test failed:', err);
      alert('Database connection test failed - check console');
    }
  };

  const handleLogout = () => {
    dispatch(logout());
    localStorage.removeItem('user');
  };

  const handleForgotPassword = async () => {
    if (!forgotPasswordEmail) {
      setError('Please enter your email address');
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      const response = await AuthService.forgotPassword({ email: forgotPasswordEmail });
      setSuccess(response.message);
      setForgotPasswordEmail('');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to send reset email');
    } finally {
      setLoading(false);
    }
  };

  if (isLoggedIn) {
    return (
      <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h6">
            Welcome back, {reduxUser?.username}!
          </Typography>
          <Button
            variant="outlined"
            startIcon={<LogoutIcon />}
            onClick={handleLogout}
          >
            Logout
          </Button>
        </Box>
      </Paper>
    );
  }

  return (
    <Paper elevation={2} sx={{ mb: 3 }}>
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={tabValue} onChange={handleTabChange} aria-label="auth tabs">
          <Tab label="Login" icon={<LoginIcon />} />
          <Tab label="Sign Up" icon={<SignupIcon />} />
          <Tab label="Forgot Password" />
        </Tabs>
      </Box>

      {/* Debug section - remove in production */}
      <Box sx={{ m: 2, p: 1, bgcolor: 'grey.100', borderRadius: 1 }}>
        <Typography variant="caption" color="text.secondary">
          Debug Tools:
        </Typography>
        <Button 
          size="small" 
          onClick={testDatabaseConnection}
          sx={{ ml: 1 }}
          variant="outlined"
        >
          Test DB Users
        </Button>
        <Button 
          size="small" 
          onClick={() => {
            fetch('http://localhost:8080/api/auth/signin', {
              method: 'POST',
              headers: {'Content-Type': 'application/json'},
              body: JSON.stringify({username: 'admin', password: 'admin123'})
            }).then(r => r.text()).then(data => {
              console.log('Test login response:', data);
              alert('Check console for test login result');
            }).catch(err => {
              console.error('Test login error:', err);
              alert('Test login failed - check console');
            });
          }}
          sx={{ ml: 1 }}
          variant="outlined"
        >
          Test Login
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ m: 2 }}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ m: 2 }}>
          {success}
        </Alert>
      )}

      <TabPanel value={tabValue} index={0}>
        <Typography variant="h6" gutterBottom>
          Login to Your Account
        </Typography>
        <Box component="form" sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <TextField
            label="Username"
            variant="outlined"
            fullWidth
            value={loginForm.username}
            onChange={(e) => setLoginForm(prev => ({ ...prev, username: e.target.value }))}
            disabled={loading}
          />
          <TextField
            label="Password"
            type="password"
            variant="outlined"
            fullWidth
            value={loginForm.password}
            onChange={(e) => setLoginForm(prev => ({ ...prev, password: e.target.value }))}
            disabled={loading}
          />
          <Button
            variant="contained"
            size="large"
            onClick={handleLogin}
            disabled={loading}
            startIcon={loading ? <CircularProgress size={20} /> : <LoginIcon />}
          >
            {loading ? 'Logging in...' : 'Login'}
          </Button>
        </Box>
      </TabPanel>

      <TabPanel value={tabValue} index={1}>
        <Typography variant="h6" gutterBottom>
          Create New Account
        </Typography>
        <Box component="form" sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <TextField
            label="Username"
            variant="outlined"
            fullWidth
            value={signupForm.username}
            onChange={(e) => setSignupForm(prev => ({ ...prev, username: e.target.value }))}
            disabled={loading}
          />
          <TextField
            label="Email"
            type="email"
            variant="outlined"
            fullWidth
            value={signupForm.email}
            onChange={(e) => setSignupForm(prev => ({ ...prev, email: e.target.value }))}
            disabled={loading}
          />
          <TextField
            label="Password"
            type="password"
            variant="outlined"
            fullWidth
            value={signupForm.password}
            onChange={(e) => setSignupForm(prev => ({ ...prev, password: e.target.value }))}
            disabled={loading}
          />
          <TextField
            label="Confirm Password"
            type="password"
            variant="outlined"
            fullWidth
            value={signupForm.confirmPassword}
            onChange={(e) => setSignupForm(prev => ({ ...prev, confirmPassword: e.target.value }))}
            disabled={loading}
          />
          <Button
            variant="contained"
            size="large"
            onClick={handleSignup}
            disabled={loading}
            startIcon={loading ? <CircularProgress size={20} /> : <SignupIcon />}
          >
            {loading ? 'Creating Account...' : 'Sign Up'}
          </Button>
        </Box>
      </TabPanel>

      <TabPanel value={tabValue} index={2}>
        <Typography variant="h6" gutterBottom>
          Forgot Your Password?
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Enter your email address and we'll send you a link to reset your password.
        </Typography>
        <Box component="form" sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <TextField
            label="Email Address"
            type="email"
            variant="outlined"
            fullWidth
            value={forgotPasswordEmail}
            onChange={(e) => setForgotPasswordEmail(e.target.value)}
            disabled={loading}
          />
          <Button
            variant="contained"
            size="large"
            onClick={handleForgotPassword}
            disabled={loading}
            startIcon={loading ? <CircularProgress size={20} /> : undefined}
          >
            {loading ? 'Sending...' : 'Send Reset Link'}
          </Button>
        </Box>
      </TabPanel>
    </Paper>
  );
};

export default AuthComponent;