import { Routes } from '@angular/router';
import { Login } from './auth/login/login';
import { Register } from './auth/register/register';
import {AuthGuard} from './auth/AuthGuard';
import {HomeComponent} from './home/home';
import {EmailForm} from './auth/password_reset/email-form/email-form';
import {ResetPasswordForm} from './auth/password_reset/reset-password-form/reset-password-form';
import {TicketType} from './ticket-type/ticket-type';
import { DiscountList } from './discount/discount-list';
import { DiscountForm } from './discount/discount-form';
import { AdminGuard } from './auth/AdminGuard';

export const routes: Routes = [
  { path: 'login',
    component: Login,
    canActivate: [AuthGuard]},
  { path: 'register',
    component: Register,
    canActivate: [AuthGuard] },
  {
    path:'forgot-password',
    component: EmailForm,
    canActivate: [AuthGuard]
  },
  {
    path: 'reset-password',
    component: ResetPasswordForm,
    canActivate: [AuthGuard]
  },
  { path:'',
    component: HomeComponent
  },

  { path: 'discounts', component: DiscountList },
  // Crear/editar sólo admin
  { path: 'discounts/new', component: DiscountForm, canActivate: [AdminGuard] },
  { path: 'discounts/:id', component: DiscountForm, canActivate: [AdminGuard] },
  {path: 'ticket_type',
   component: TicketType}
];
