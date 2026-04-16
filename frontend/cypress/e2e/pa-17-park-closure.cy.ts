import { backendUrl, futureDate, loginAsAdmin, uniqueId } from './pa-helpers';

describe('PA-17 - Gestion de dias de cierre', () => {
  beforeEach(() => {
    loginAsAdmin();
  });

  it('registra dia de cierre y verifica bloqueo por API', () => {
    const closureDate = futureDate(90 + Cypress._.random(0, 30));
    const reason = `PA17 ${uniqueId('closure')}`;

    cy.intercept('POST', `${backendUrl}/api/v1/park-closures`).as('createClosure');

    cy.visit('/admin/park-config');
    cy.get('.tab-btn').eq(0).click();
    cy.get('input[type="date"]').first().clear().type(closureDate);
    cy.get('input[type="text"]').first().clear().type(reason);
    cy.get('.btn-add').click();
    cy.wait('@createClosure').its('response.statusCode').should('eq', 201);

    cy.contains('.list-item', reason, { timeout: 15000 }).should('be.visible');

    cy.request(`${backendUrl}/api/v1/park-closures/check?date=${closureDate}`).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body).to.eq(true);
    });
  });
});
