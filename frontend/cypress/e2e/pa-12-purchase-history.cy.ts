import { futureDate, loginAsUser } from './pa-helpers';

describe('PA-12 - Listado de compras del usuario', () => {
  beforeEach(() => {
    loginAsUser();
  });

  it('muestra historial con lineas e importes', () => {
    cy.createPurchaseForEmail('user1@example.com', { visitDate: futureDate(6), quantity: 2, ticketTypeName: 'Adult' }).then((purchaseId) => {
      cy.visit('/profile');
      cy.get('[data-cy="profile-tab-purchases"]').click();

      cy.contains('[data-cy="purchase-card"]', `#${purchaseId}`, { timeout: 15000 })
        .should('be.visible')
        .within(() => {
          cy.get('.line-row').should('have.length.greaterThan', 0);
          cy.get('.purchase-total .total-amount').should('be.visible');
        });
    });
  });
});
