describe('PA-14 - Visualizacion de resenas', () => {
  it('muestra listado de resenas publicas con valoracion y descripcion', () => {
    cy.visit('/reviews');

    cy.get('[data-cy="reviews-container"]', { timeout: 15000 }).should('be.visible');
    cy.get('[data-cy="review-card"]').should('have.length.greaterThan', 0);
    cy.get('.review-stars .star.filled').should('exist');
    cy.get('.review-description').first().should('not.be.empty');
  });
});
