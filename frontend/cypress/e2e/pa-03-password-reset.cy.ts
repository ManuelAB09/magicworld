import { authRateLimitHeaders, backendUrl, createValidUserData } from './pa-helpers';

describe('PA-03 - Recuperacion de contrasena por token', () => {
  it('permite recuperar contrasena con token valido y autenticar de nuevo', () => {
    const user = createValidUserData();
    const newPassword = 'NewSecurePass123!';

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

    cy.logoutClientSession();
    cy.visit('/forgot-password');
    cy.get('[data-cy="forgot-password-email"]').type(user.email);
    cy.get('[data-cy="forgot-password-submit"]').click();

    cy.getResetTokenForEmail(user.email).then((token) => {
      cy.visit(`/reset-password?token=${token}`);
      cy.get('[data-cy="reset-password-input"]').type(newPassword);
      cy.get('[data-cy="reset-password-confirm-input"]').type(newPassword);
      cy.get('[data-cy="reset-password-submit"]').click();
    });

    cy.visit('/login');
    cy.get('[data-cy="login-username"]').type(user.username);
    cy.get('[data-cy="login-password"]').type(newPassword);
    cy.get('[data-cy="login-submit"]').click();

    cy.visit('/profile');
    cy.url().should('include', '/profile');
  });
});
