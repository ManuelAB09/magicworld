import { backendUrl, futureDate, loginAsUser } from './pa-helpers';

describe('PA-27 - Fallo de pago en pasarela', () => {
  beforeEach(() => {
    loginAsUser();
  });

  it('muestra error y no confirma compra cuando Stripe rechaza el pago', () => {
    const visitDate = futureDate(21);

    cy.request(`${backendUrl}/api/v1/purchases/my-purchases`).then((beforePurchases) => {
      const previousCount = (beforePurchases.body as any[]).length;

      cy.request({
        method: 'POST',
        url: `${backendUrl}/api/v1/payment/process`,
        body: {
          visitDate,
          items: [{ ticketTypeName: 'Adult', quantity: 1 }],
          discountCodes: [],
          email: 'user1@example.com',
          firstName: 'User',
          lastName: '1',
          stripePaymentMethodId: 'pm_card_chargeDeclined',
        },
        failOnStatusCode: false,
      }).then((paymentResponse) => {
        expect([200, 400]).to.include(paymentResponse.status);
        if (paymentResponse.status === 200) {
          expect(paymentResponse.body?.success).to.eq(false);
        }
      });

      cy.request(`${backendUrl}/api/v1/purchases/my-purchases`).its('body').should((afterPurchases) => {
        expect((afterPurchases as any[]).length).to.eq(previousCount);
      });
    });
  });
});
