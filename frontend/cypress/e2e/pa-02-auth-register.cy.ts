import { createValidUserData } from './pa-helpers';

describe('PA-02 - Registro de nuevo usuario', () => {
  beforeEach(() => {
    cy.logoutClientSession();
    cy.visit('/register');
  });

  it('registra usuario y permite acceder como autenticado', () => {
    const user = createValidUserData();

    cy.get('[data-cy="register-username"]').type(user.username);
    cy.get('[data-cy="register-firstname"]').type(user.firstname);
    cy.get('[data-cy="register-lastname"]').type(user.lastname);
    cy.get('[data-cy="register-email"]').type(user.email);
    cy.get('[data-cy="register-password"]').type(user.password);
    cy.get('[data-cy="register-confirm-password"]').type(user.password);
    cy.get('[data-cy="register-submit"]').click();

    cy.url().should('include', '/');
    cy.visit('/profile');
    cy.url().should('include', '/profile');
  });
});
