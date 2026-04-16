import { futureDate, loginAsAdmin, uniqueId } from './pa-helpers';

describe('PA-15 - Creacion de promociones', () => {
  beforeEach(() => {
    loginAsAdmin();
  });

  it('crea y elimina una promocion asociada a tipos de entrada', () => {
    const code = uniqueId('PA15').toUpperCase().slice(0, 18);

    cy.visit('/discounts/new');
    cy.get('input[formcontrolname="discountCode"]').type(code);
    cy.get('input[formcontrolname="discountPercentage"]').clear().type('15');
    cy.get('input[formcontrolname="expiryDate"]').type(futureDate(30));
    cy.get('.check-item__real').first().check({ force: true });
    cy.get('button[type="submit"]').click();

    cy.visit('/discounts');
    cy.contains('.discount-row', code).should('be.visible').within(() => {
      cy.get('.btn-delete').click();
    });

    cy.contains('.discount-row', code).should('not.exist');
  });
});
