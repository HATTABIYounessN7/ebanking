export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  roles: string;
}

export interface AppUser {
  username: string;
  roles: string;
  token: string;
}
