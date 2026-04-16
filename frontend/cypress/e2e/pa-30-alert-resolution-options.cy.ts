import { loginAsAdmin } from './pa-helpers';

describe('PA-30 - Opciones de resolucion de alertas', () => {
  beforeEach(() => {
    loginAsAdmin();
  });

  it('muestra opciones habilitadas y ejecuta una accion valida', () => {
    cy.visit('/admin/dashboard');

    cy.get('body').then(($body) => {
      if ($body.find('.alert-card').length === 0 && $body.find('.btn-start').length > 0) {
        cy.get('.btn-start').first().click();
      }
    });

    cy.get('.alert-card, .no-alerts', { timeout: 20000 }).should('exist');

    cy.get('body').then(($body) => {
      if ($body.find('.alert-card').length === 0) {
        return;
      }

      cy.get('.expand-btn').first().click();
      cy.get('.resolution-option').should('have.length.greaterThan', 0);

      cy.get('.resolution-option:not(.disabled):not(:disabled)').first().click({ force: true });

      cy.get('body').then(($afterSelect) => {
        if ($afterSelect.find('.call-btn').length > 0) {
          cy.get('.call-btn').first().click();
          return;
        }

        if ($afterSelect.find('.employee-option').length > 0) {
          cy.get('.employee-option').first().click();
          cy.get('.confirm-btn').click();
          return;
        }
      });

      cy.get('.feedback-toast, .feedback-content', { timeout: 20000 }).should('exist');
    });
  });
});
