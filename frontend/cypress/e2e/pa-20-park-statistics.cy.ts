import { loginAsAdmin } from './pa-helpers';

describe('PA-20 - Estadisticas de parque', () => {
  beforeEach(() => {
    loginAsAdmin();
  });

  it('consulta ventas, estacionalidad y rendimiento de atracciones', () => {
    cy.visit('/admin/statistics');
    cy.get('.tab-btn').eq(1).click();

    cy.get('.park-stats', { timeout: 20000 }).should('be.visible');
    cy.get('.park-stats .sub-tabs button').eq(0).click();
    cy.get('.metric-card, .no-data').should('exist');

    cy.get('.park-stats .sub-tabs button').eq(1).click();
    cy.get('.chart-container, .no-data').should('exist');

    cy.get('.park-stats .sub-tabs button').eq(2).click();
    cy.get('.data-table, .no-data').should('exist');
  });
});
