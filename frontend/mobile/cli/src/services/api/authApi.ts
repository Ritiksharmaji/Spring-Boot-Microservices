import type {AuthResponse} from '@/types/api';
import type {User} from '@/types/models';
import {normalizeUser, splitName} from '@/utils/normalize';
import {apiClient} from './client';

const toAuthResponse = (data: any): AuthResponse => ({
  token: data.accessToken,
  user: normalizeUser(data.user),
});

export const authApi = {
  /** POST /api/auth/login */
  async login(email: string, password: string): Promise<AuthResponse> {
    const {data} = await apiClient.post('/api/auth/login', {email, password});
    return toAuthResponse(data);
  },

  /** POST /api/auth/register (password: 8-72 characters with a letter and a digit) */
  async register(name: string, email: string, password: string): Promise<AuthResponse> {
    const {firstName, lastName} = splitName(name);
    const {data} = await apiClient.post('/api/auth/register', {
      firstName,
      lastName,
      email,
      password,
    });
    return toAuthResponse(data);
  },

  /** GET /api/users/me — the signed-in user's profile. */
  async me(): Promise<User> {
    const {data} = await apiClient.get('/api/users/me');
    return normalizeUser(data);
  },

  /** DELETE /api/users/me — permanently deletes the account (password re-checked). */
  async deleteAccount(password: string): Promise<void> {
    await apiClient.delete('/api/users/me', {data: {password}});
  },
};
