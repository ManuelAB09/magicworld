import { authRateLimitHeaders, backendUrl, createValidUserData } from './pa-helpers';

describe('PA-05 - Edicion y borrado de perfil', () => {
  it('permite editar perfil y eliminar la cuenta', () => {
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

    cy.visit('/profile');
    cy.get('[data-cy="profile-edit-button"]').click();
    cy.get('[data-cy="profile-edit-modal"]').within(() => {
      cy.get('input').eq(1).clear().type('UpdatedName');
      cy.get('[data-cy="profile-save-button"]').click();
    });

    cy.contains('UpdatedName').should('be.visible');

    cy.on('window:confirm', () => true);
    cy.get('[data-cy="profile-delete-button"]').click();

    cy.visit('/profile');
    cy.url().should('include', '/login');
  });
});
