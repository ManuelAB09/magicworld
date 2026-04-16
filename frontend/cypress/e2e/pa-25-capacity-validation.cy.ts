import { backendUrl, futureDate, loginAsUser } from './pa-helpers';

describe('PA-25 - Validacion de aforo insuficiente', () => {
  beforeEach(() => {
    loginAsUser();
  });

  it('rechaza calculo con cantidad muy superior al aforo disponible', () => {
    cy.request({
      method: 'POST',
      url: `${backendUrl}/api/v1/payment/process`,
      body: {
        visitDate: futureDate(5),
        items: [{ ticketTypeName: 'Adult', quantity: 999999 }],
        discountCodes: [],
        email: 'user1@example.com',
        firstName: 'User',
        lastName: 'One',
        stripePaymentMethodId: 'pm_card_visa',
      },
      failOnStatusCode: false,
    }).then((response) => {
      expect(response.status).to.eq(400);
      expect(response.body?.code || response.body?.message).to.match(/error\.payment\./);
    });
  });
});
