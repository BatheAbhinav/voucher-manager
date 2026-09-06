export type Role = 'ADMIN' | 'ORG';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  role: Role;
  id: string;
  name: string;
}
