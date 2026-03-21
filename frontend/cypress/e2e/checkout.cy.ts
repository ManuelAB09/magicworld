describe('Proceso de compra de entradas', () => {
  it('debe mostrar la página de checkout al acceder', () => {
    cy.visit('/checkout');
    cy.url().should('include', '/checkout');
  });

  it('debe poder llegar al checkout desde la home', () => {
    cy.visit('/');
    cy.get('.buy-tickets-btn').click();
    cy.url().should('include', '/checkout');
  });
});
