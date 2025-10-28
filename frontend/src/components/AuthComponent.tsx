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
import { LoginRequest, SignupRequest } from '../types';

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
  
  const [tabValue, setTabValue] = useState(0);
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
      setError(err.response?.data?.message || 'Login failed');
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

    setLoading(true);
    setError(null);

    try {
      const response = await AuthService.register(signupForm);
      dispatch(loginSuccess(response));
      localStorage.setItem('user', JSON.stringify(response));
      setSignupForm({ username: '', email: '', password: '', confirmPassword: '' });
    } catch (err: any) {
      setError(err.response?.data?.message || 'Signup failed');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    dispatch(logout());
    localStorage.removeItem('user');
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
        </Tabs>
      </Box>

      {error && (
        <Alert severity="error" sx={{ m: 2 }}>
          {error}
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
    </Paper>
  );
};

export default AuthComponent;