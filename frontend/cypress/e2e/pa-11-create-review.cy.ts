import { backendUrl, futureDate, loginAsUser, uniqueId } from './pa-helpers';

describe('PA-11 - Creacion de resena', () => {
  beforeEach(() => {
    loginAsUser();
  });

  it('crea una resena para una compra disponible y la muestra en listado', () => {
    const description = `Review ${uniqueId('PA11')}`;
    const visitDate = futureDate(5);

    cy.createPurchaseForEmail('user1@example.com', { visitDate, quantity: 1, ticketTypeName: 'Adult' }).then((purchaseId) => {
      cy.request({
        method: 'GET',
        url: `${backendUrl}/api/v1/reviews/available-purchases`,
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).to.eq(200);
        expect(response.body).to.be.an('array');
        expect(response.body as number[]).to.include(purchaseId);
      });

      cy.visit('/reviews');
      cy.get('[data-cy="reviews-open-create"]').click();
      cy.get('[data-cy="reviews-purchase-select"]').select(String(purchaseId));
      cy.get('[data-cy="reviews-star-btn"]').eq(4).click();
      cy.get('[data-cy="reviews-description"]').type(description);
      cy.get('[data-cy="reviews-submit"]').click();

      cy.get('[data-cy="reviews-container"]').should('contain.text', description);
    });
  });
});
