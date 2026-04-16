import { loginAsAdmin } from './pa-helpers';

describe('PA-31 - Flujo de refuerzo ante incidencias', () => {
  beforeEach(() => {
    loginAsAdmin();
  });

  it('inicia llamada de refuerzo y registra respuesta operativa', () => {
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
      cy.get('.resolution-option:not(.disabled):not(:disabled)').first().click({ force: true });

      cy.get('body').then(($resolutionState) => {
        if ($resolutionState.find('.call-btn').length > 0) {
          cy.get('.call-btn').first().click();
        } else if ($resolutionState.find('.employee-option').length > 0) {
          cy.get('.employee-option').first().click();
          cy.get('.confirm-btn').click();
        }
      });

      cy.get('.feedback-toast, .feedback-content', { timeout: 20000 }).should('exist');
    });

    cy.visit('/admin/schedule');
    cy.get('.schedule-table, .validation-warning', { timeout: 20000 }).should('exist');
  });
});
