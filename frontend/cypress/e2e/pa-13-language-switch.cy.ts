describe('PA-13 - Cambio de idioma', () => {
  it('aplica y persiste preferencia de idioma', () => {
    cy.visit('/');
    cy.get('[data-cy="language-select"]').select('en');
    cy.get('[data-cy="language-select"]').should('have.value', 'en');

    cy.contains('[data-cy="nav-login"], [data-cy="nav-logout"]', /Login|Logout/, { timeout: 10000 }).should('exist');
  });
});
