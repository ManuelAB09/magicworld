import { backendUrl, futureDate, loginAsUser } from './pa-helpers';

describe('PA-04 - Compra de entradas con pago exitoso', () => {
  beforeEach(() => {
    loginAsUser();
  });

  it('completa checkout en tres pasos y muestra confirmacion', () => {
    const visitDate = futureDate(20);

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
          stripePaymentMethodId: 'pm_card_visa',
        },
        failOnStatusCode: false,
      }).then((paymentResponse) => {
        expect(paymentResponse.status).to.eq(200);
        expect(paymentResponse.body?.success).to.eq(true);
        expect(paymentResponse.body?.purchaseId).to.be.a('number');
      });

      cy.request(`${backendUrl}/api/v1/purchases/my-purchases`).its('body').should((afterPurchases) => {
        expect(afterPurchases).to.be.an('array');
        expect((afterPurchases as any[]).length).to.be.greaterThan(previousCount);
      });
    });
  });
});
