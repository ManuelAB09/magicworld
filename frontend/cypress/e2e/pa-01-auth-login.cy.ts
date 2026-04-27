import { authRateLimitHeaders, backendUrl, createValidUserData } from './pa-helpers';

describe('PA-01 - Autenticacion con credenciales validas', () => {
  beforeEach(() => {
    cy.logoutClientSession();
    cy.visit('/login');
  });

  it('autentica al usuario y permite acceso a ruta protegida', () => {
    const user = createValidUserData();

    cy.request({
      method: 'POST',
      url: `${backendUrl}/api/v1/auth/register`,
      headers: authRateLimitHeaders(),
      body: {
        username: user.username,
        firstname: user.firstname,
        lastname: user.lastname,
        email: user.email,
        password: user.password,
        confirmPassword: user.password,
      },
      failOnStatusCode: false,
    }).its('status').should('eq', 201);

    cy.get('[data-cy="login-username"]').type(user.username);
    cy.get('[data-cy="login-password"]').type(user.password);
    cy.get('[data-cy="login-submit"]').click();

    cy.location('pathname').should('eq', '/');
    cy.visit('/profile');
    cy.location('pathname').should('eq', '/profile');
    cy.get('[data-cy="profile-page"]').should('be.visible');
  });
});
