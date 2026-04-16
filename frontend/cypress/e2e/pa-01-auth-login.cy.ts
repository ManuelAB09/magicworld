describe('PA-01 - Autenticacion con credenciales validas', () => {
  beforeEach(() => {
    cy.logoutClientSession();
    cy.visit('/login');
  });

  it('autentica al usuario y permite acceso a ruta protegida', () => {
    cy.get('[data-cy="login-username"]').type('user1');
    cy.get('[data-cy="login-password"]').type('SecurePassword123!');
    cy.get('[data-cy="login-submit"]').click();

    cy.url().should('include', '/');
    cy.visit('/profile');
    cy.url().should('include', '/profile');
    cy.get('[data-cy="profile-page"]').should('be.visible');
  });
});
