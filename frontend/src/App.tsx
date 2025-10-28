import React from 'react';
import { Provider } from 'react-redux';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import { store } from './store';
import JobSearchForm from './components/JobSearchForm';
import JobResults from './components/JobResults';
import AuthComponent from './components/AuthComponent';
import './index.css';

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

function App() {
  return (
    <Provider store={store}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <Container maxWidth="xl">
          <Box sx={{ my: 4 }}>
            <Typography variant="h3" component="h1" gutterBottom align="center" sx={{ fontWeight: 'bold', color: 'primary.main' }}>
              🔍 Job Search Platform
            </Typography>
            <Typography variant="h6" align="center" color="text.secondary" sx={{ mb: 4 }}>
              Find your dream job from multiple sources with advanced filtering
            </Typography>
            
            <AuthComponent />
            <JobSearchForm />
            <JobResults />
          </Box>
        </Container>
      </ThemeProvider>
    </Provider>
  );
}

export default App;