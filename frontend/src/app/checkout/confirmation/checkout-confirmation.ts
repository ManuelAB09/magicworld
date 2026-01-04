import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

interface CheckoutResult {
  success: boolean;
  purchaseId?: number;
  totalAmount?: number;
  discountAmount?: number;
  visitDate?: string;
  errorMessage?: string;
}

@Component({
  selector: 'app-checkout-confirmation',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  templateUrl: './checkout-confirmation.html',
  styleUrl: './checkout-confirmation.css'
})
export class CheckoutConfirmationComponent implements OnInit {
  result: CheckoutResult | null = null;
  showModal = false;

  constructor(private router: Router) {}

  ngOnInit(): void {
    const resultData = sessionStorage.getItem('checkout_result');
    if (resultData) {
      this.result = JSON.parse(resultData);
      this.showModal = true;
      sessionStorage.removeItem('checkout_result');
    } else {
      this.router.navigate(['/']);
    }
  }

  goBack(): void {
    this.showModal = false;
    this.router.navigate(['/checkout']);
  }

  goHome(): void {
    this.showModal = false;
    this.router.navigate(['/']);
  }
}

