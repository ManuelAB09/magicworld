import { backendUrl, futureDate, loginAsAdmin, uniqueId } from './pa-helpers';

describe('PA-26 - Integracion descuento + estacionalidad', () => {
  it('calcula total aplicando promocion activa y regla estacional', () => {
    const code = uniqueId('PA26').toUpperCase().slice(0, 18);
    const startDate = futureDate(5);
    const endDate = futureDate(20);
    const visitDate = futureDate(10);

    loginAsAdmin();

    cy.visit('/discounts/new');
    cy.get('input[formcontrolname="discountCode"]').type(code);
    cy.get('input[formcontrolname="discountPercentage"]').clear().type('20');
    cy.get('input[formcontrolname="expiryDate"]').type(futureDate(40));
    cy.get('.check-item__real').first().check({ force: true });
    cy.get('button[type="submit"]').click();

    cy.request({
      method: 'POST',
      url: `${backendUrl}/api/v1/seasonal-pricing`,
      body: {
        name: uniqueId('PA26_SEASON'),
        startDate,
        endDate,
        multiplier: 1.5,
        applyOnWeekdays: true,
        applyOnWeekends: true,
      },
      failOnStatusCode: false,
    }).its('status').should('eq', 201);

    cy.request({
      method: 'POST',
      url: `${backendUrl}/api/v1/payment/calculate`,
      body: {
        items: [{ ticketTypeName: 'Adult', quantity: 2 }],
        discountCodes: [code],
        visitDate,
      },
      failOnStatusCode: false,
    }).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body.validDiscountCodes).to.include(code);
      expect(Number(response.body.discountAmount)).to.be.greaterThan(0);
      expect(Number(response.body.total)).to.be.greaterThan(0);
    });
  });
});
