import React, { useState, useEffect } from 'react';
import {
  Paper,
  TextField,
  Button,
  Typography,
  Box,
  Alert,
  CircularProgress,
  Container
} from '@mui/material';
import { LockReset as ResetIcon } from '@mui/icons-material';
import AuthService from '../services/auth.service';

interface PasswordResetProps {
  token?: string;
  onBackToLogin?: () => void;
}

const PasswordResetComponent: React.FC<PasswordResetProps> = ({ 
  token: propToken, 
  onBackToLogin 
}) => {
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<boolean>(false);
  const [validatingToken, setValidatingToken] = useState(true);
  const [tokenValid, setTokenValid] = useState(false);
  
  const [formData, setFormData] = useState({
    newPassword: '',
    confirmPassword: ''
  });

  // Get token from props or URL parameters
  const urlParams = new URLSearchParams(window.location.search);
  const token = propToken || urlParams.get('token');

  useEffect(() => {
    if (!token) {
      setError('No reset token provided');
      setValidatingToken(false);
      return;
    }

    // Validate the token on component mount
    validateToken();
  }, [token]);

  const validateToken = async () => {
    if (!token) return;

    try {
      await AuthService.validateResetToken(token);
      setTokenValid(true);
    } catch (err: any) {
      setError('Invalid or expired reset link');
      setTokenValid(false);
    } finally {
      setValidatingToken(false);
    }
  };

  const handleResetPassword = async () => {
    if (!formData.newPassword || !formData.confirmPassword) {
      setError('Please fill in all fields');
      return;
    }

    if (formData.newPassword !== formData.confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    if (formData.newPassword.length < 6) {
      setError('Password must be at least 6 characters long');
      return;
    }

    if (!token) {
      setError('No reset token available');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await AuthService.resetPassword({
        token,
        newPassword: formData.newPassword
      });
      
      setSuccess(true);
      setFormData({ newPassword: '', confirmPassword: '' });
      
      // Redirect to login after successful reset
      setTimeout(() => {
        if (onBackToLogin) {
          onBackToLogin();
        } else {
          window.location.href = '/';
        }
      }, 3000);
      
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to reset password');
    } finally {
      setLoading(false);
    }
  };

  if (validatingToken) {
    return (
      <Container maxWidth="sm" sx={{ mt: 8 }}>
        <Paper elevation={3} sx={{ p: 4, textAlign: 'center' }}>
          <CircularProgress sx={{ mb: 2 }} />
          <Typography variant="h6">Validating reset link...</Typography>
        </Paper>
      </Container>
    );
  }

  if (!tokenValid) {
    return (
      <Container maxWidth="sm" sx={{ mt: 8 }}>
        <Paper elevation={3} sx={{ p: 4, textAlign: 'center' }}>
          <Typography variant="h5" color="error" gutterBottom>
            Invalid Reset Link
          </Typography>
          <Typography variant="body1" sx={{ mb: 3 }}>
            This password reset link is invalid or has expired.
          </Typography>
          <Button 
            variant="contained" 
            onClick={() => onBackToLogin ? onBackToLogin() : window.location.href = '/'}
          >
            Back to Login
          </Button>
        </Paper>
      </Container>
    );
  }

  return (
    <Container maxWidth="sm" sx={{ mt: 8 }}>
      <Paper elevation={3} sx={{ p: 4 }}>
        <Box sx={{ textAlign: 'center', mb: 3 }}>
          <ResetIcon sx={{ fontSize: 48, color: 'primary.main', mb: 1 }} />
          <Typography variant="h4" gutterBottom>
            Reset Your Password
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Enter your new password below
          </Typography>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {success && (
          <Alert severity="success" sx={{ mb: 2 }}>
            Password reset successfully!
            <br />
            <Typography variant="body2" sx={{ mt: 1 }}>
              Redirecting to login...
            </Typography>
          </Alert>
        )}

        <Box component="form" sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <TextField
            label="New Password"
            type="password"
            variant="outlined"
            fullWidth
            value={formData.newPassword}
            onChange={(e) => setFormData(prev => ({ ...prev, newPassword: e.target.value }))}
            disabled={loading || success}
            helperText="Password must be at least 6 characters long"
          />
          
          <TextField
            label="Confirm New Password"
            type="password"
            variant="outlined"
            fullWidth
            value={formData.confirmPassword}
            onChange={(e) => setFormData(prev => ({ ...prev, confirmPassword: e.target.value }))}
            disabled={loading || success}
          />
          
          <Button
            variant="contained"
            size="large"
            onClick={handleResetPassword}
            disabled={loading || success}
            startIcon={loading ? <CircularProgress size={20} /> : <ResetIcon />}
            sx={{ mt: 2 }}
          >
            {loading ? 'Resetting...' : 'Reset Password'}
          </Button>
          
          <Button
            variant="text"
            onClick={() => onBackToLogin ? onBackToLogin() : window.location.href = '/'}
            disabled={loading}
          >
            Back to Login
          </Button>
        </Box>
      </Paper>
    </Container>
  );
};

export default PasswordResetComponent;