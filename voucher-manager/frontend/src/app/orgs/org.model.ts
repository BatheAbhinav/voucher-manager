export interface Org {
  id: string;
  name: string;
  email: string;
}

export interface CreateOrgRequest {
  name: string;
  email: string;
  password: string;
}
