import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { AuthResponse } from '../types';

interface AuthState {
  user: AuthResponse | null;
  isLoggedIn: boolean;
}

const user = JSON.parse(localStorage.getItem('user') || 'null');

const initialState: AuthState = {
  user: user,
  isLoggedIn: !!user,
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    loginSuccess: (state, action: PayloadAction<AuthResponse>) => {
      state.user = action.payload;
      state.isLoggedIn = true;
    },
    logout: (state) => {
      state.user = null;
      state.isLoggedIn = false;
    },
  },
});

export const { loginSuccess, logout } = authSlice.actions;
export default authSlice.reducer;