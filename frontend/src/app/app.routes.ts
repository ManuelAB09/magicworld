import { Routes } from '@angular/router';
import { Login } from './auth/login/login';
import { Register } from './auth/register/register';
import {AuthGuard} from './auth/AuthGuard';
import {HomeComponent} from './home/home';
import {EmailForm} from './auth/password_reset/email-form/email-form';
import {ResetPasswordForm} from './auth/password_reset/reset-password-form/reset-password-form';
import { DiscountList } from './discount/discount-list';
import { DiscountForm } from './discount/discount-form';
import { AdminGuard } from './auth/AdminGuard';
import { TicketTypeList } from './ticket-type/ticket-type-list';
import { TicketTypeForm } from './ticket-type/ticket-type-form';
import { AttractionList } from './attraction/attraction-list';
import { AttractionForm } from './attraction/attraction-form';
import { AttractionDetailComponent } from './attraction/attraction-detail/attraction-detail';
import { ChatbotComponent } from './chatbot/chatbot';
import { CheckoutStep1Component } from './checkout/step1/checkout-step1';
import { CheckoutStep2Component } from './checkout/step2/checkout-step2';
import { CheckoutConfirmationComponent } from './checkout/confirmation/checkout-confirmation';
import { ParkMapComponent } from './park-map/park-map';
import { ReviewListComponent } from './review/review-list';
import { ProfileComponent } from './profile/profile';
import { RequireAuthGuard } from './auth/RequireAuthGuard';

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

  { path: 'park-map', component: ParkMapComponent },

  { path: 'discounts', component: DiscountList },
  { path: 'discounts/new', component: DiscountForm, canActivate: [AdminGuard] },
  { path: 'discounts/:id', component: DiscountForm, canActivate: [AdminGuard] },


  { path: 'ticket-types', component: TicketTypeList, canActivate: [AdminGuard] },
  { path: 'ticket-types/new', component: TicketTypeForm, canActivate: [AdminGuard] },
  { path: 'ticket-types/:id', component: TicketTypeForm, canActivate: [AdminGuard] },


  { path: 'attractions', component: AttractionList },
  { path: 'attraction/:id', component: AttractionDetailComponent },
  { path: 'attractions/new', component: AttractionForm, canActivate: [AdminGuard] },
  { path: 'attractions/:id', component: AttractionForm, canActivate: [AdminGuard] },


  { path: 'chatbot', component: ChatbotComponent, canActivate: [AdminGuard] },

  { path: 'checkout', component: CheckoutStep1Component },
  { path: 'checkout/step2', component: CheckoutStep2Component },
  { path: 'checkout/confirmation', component: CheckoutConfirmationComponent },

  { path: 'reviews', component: ReviewListComponent },
  { path: 'profile', component: ProfileComponent, canActivate: [RequireAuthGuard] }
];
