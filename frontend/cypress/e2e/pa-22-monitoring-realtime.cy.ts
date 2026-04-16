import { loginAsAdmin } from './pa-helpers';

describe('PA-22 - Monitorizacion y alertas en tiempo real', () => {
  beforeEach(() => {
    loginAsAdmin();
  });

  it('abre dashboard, inicia simulador y muestra alertas activas', () => {
    cy.visit('/admin/dashboard');

    cy.get('.dashboard-container', { timeout: 20000 }).should('be.visible');
    cy.get('.alert-list, .no-alerts').should('exist');

    cy.get('body').then(($body) => {
      if ($body.find('.btn-start').length > 0) {
        cy.get('.btn-start').first().click();
      }
    });

    cy.get('.simulator-control', { timeout: 20000 }).should('be.visible');
    cy.get('.status-text').should('exist');
  });
});
