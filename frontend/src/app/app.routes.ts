import { Routes } from '@angular/router';
import { UsersPage } from './users/users-page/users-page';
import { VouchersPage } from './vouchers/vouchers-page/vouchers-page';
import { VoucherDetail } from './vouchers/voucher-detail/voucher-detail';
import { LoginPage } from './auth/login-page/login-page';
import { authGuard, adminGuard } from './auth/auth.guard';
import { OrgsPage } from './orgs/orgs-page/orgs-page';
import { OverviewPage } from './overview/overview-page/overview-page';

export const routes: Routes = [
  { path: '', redirectTo: 'overview', pathMatch: 'full' },
  { path: 'login', component: LoginPage },
  { path: 'overview', component: OverviewPage, canActivate: [authGuard] },
  { path: 'users', component: UsersPage, canActivate: [authGuard] },
  { path: 'vouchers', component: VouchersPage, canActivate: [authGuard] },
  { path: 'vouchers/:id', component: VoucherDetail, canActivate: [authGuard] },
  { path: 'orgs', component: OrgsPage, canActivate: [adminGuard] },
];
