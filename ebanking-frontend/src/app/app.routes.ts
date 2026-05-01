import { Routes } from '@angular/router';
import { adminGuard, authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./components/login/login').then((m) => m.Login),
  },
  {
    path: 'customers',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./components/customers/customers').then((m) => m.Customers),
  },
  {
    path: 'customers/new',
    canActivate: [authGuard, adminGuard],
    loadComponent: () =>
      import('./components/customer-form/customer-form').then((m) => m.CustomerForm),
  },
  {
    path: 'customers/edit/:id',
    canActivate: [authGuard, adminGuard],
    loadComponent: () =>
      import('./components/customer-form/customer-form').then((m) => m.CustomerForm),
  },
  {
    path: 'accounts',
    canActivate: [authGuard],
    loadComponent: () => import('./components/accounts/accounts').then((m) => m.Accounts),
  },
  { path: '', redirectTo: 'customers', pathMatch: 'full' },
  { path: '**', redirectTo: 'customers' },
];
