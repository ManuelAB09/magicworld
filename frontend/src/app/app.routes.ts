import { Routes } from '@angular/router';
import { Login } from './auth/login/login';
import { Register } from './auth/register/register';
import {AuthGuard} from './auth/AuthGuard';
import {HomeComponent} from './home/home';

export const routes: Routes = [
  { path: 'login',
    component: Login,
    canActivate: [AuthGuard]},
  { path: 'register',
    component: Register,
    canActivate: [AuthGuard] },
  { path:'',
    component: HomeComponent

  }
];
