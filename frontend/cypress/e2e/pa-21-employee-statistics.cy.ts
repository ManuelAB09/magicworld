import { loginAsAdmin } from './pa-helpers';

describe('PA-21 - Estadisticas de empleados', () => {
  beforeEach(() => {
    loginAsAdmin();
  });

  it('consulta horas, ausencias, salarios y frecuencia de posiciones', () => {
    cy.visit('/admin/statistics');
    cy.get('.tab-btn').eq(0).click();

    cy.get('.employee-stats', { timeout: 20000 }).should('be.visible');

    cy.get('.employee-stats .sub-tabs button').eq(0).click();
    cy.get('.data-table, .no-data').should('exist');

    cy.get('.employee-stats .sub-tabs button').eq(1).click();
    cy.get('.data-table, .no-data').should('exist');

    cy.get('.employee-stats .sub-tabs button').eq(2).click();
    cy.get('.data-table, .no-data').should('exist');

    cy.get('.employee-stats .sub-tabs button').eq(3).click();
    cy.get('.employee-selector select').select(1);
    cy.get('.frequency-list, .no-data').should('exist');
  });
});
