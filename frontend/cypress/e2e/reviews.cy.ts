describe('Página de reseñas', () => {
  beforeEach(() => {
    cy.visit('/reviews');
  });

  it('debe cargar la página de reseñas correctamente', () => {
    cy.url().should('include', '/reviews');
  });
});
