import { backendUrl } from './pa-helpers';

describe('PA-23 - OAuth2 Google con finalizacion mockeada', () => {
  beforeEach(() => {
    cy.logoutClientSession();
  });

  it('simula flujo oauth2 y completa registro federado', () => {
    cy.visit('/login');
    cy.get('[data-cy="login-google"]').should('be.visible');

    cy.intercept('POST', `${backendUrl}/api/v1/auth/oauth2/complete-registration`, {
      statusCode: 201,
      body: { token: null },
    }).as('oauth2Complete');

    cy.visit('/oauth2-set-password');
    cy.get('[data-cy="oauth2-password"]').type('SecureOAuth123!');
    cy.get('[data-cy="oauth2-confirm-password"]').type('SecureOAuth123!');
    cy.get('[data-cy="oauth2-submit"]').click();

    cy.wait('@oauth2Complete');
    cy.get('.error').should('not.exist');
  });
});
