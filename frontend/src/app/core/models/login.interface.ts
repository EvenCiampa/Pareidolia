export interface LoginDTO {
  email: string;
  password: string;
}

export interface PasswordUpdateDTO {
  currentPassword: string;
  newPassword: string;
}