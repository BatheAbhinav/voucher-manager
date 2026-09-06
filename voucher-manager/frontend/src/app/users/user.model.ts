export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';

export interface User {
  id: string;
  orgId: string;
  email: string;
  name: string | null;
  status: UserStatus;
  attributes: Record<string, unknown>;
}

export interface CreateUserRequest {
  email: string;
  name?: string;
  status?: UserStatus;
  attributes?: Record<string, unknown>;
  orgId?: string;
}
