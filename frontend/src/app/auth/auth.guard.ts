import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  return authService.isAuthenticated() || inject(Router).parseUrl('/login');
};

export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  if (authService.isAuthenticated() && authService.role() === 'ADMIN') {
    return true;
  }
  return inject(Router).parseUrl('/login');
};
