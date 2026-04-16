import { loginAsAdmin, uniqueId } from './pa-helpers';

describe('PA-16 - Reglas de precios estacionales', () => {
  beforeEach(() => {
    loginAsAdmin();
  });

  it('crea y elimina una regla estacional', () => {
    const ruleName = uniqueId('PA16_RULE');

    cy.visit('/admin/park-config');
    cy.get('.tab-btn').eq(1).click();

    cy.get('input[type="text"]').first().clear().type(ruleName);
    cy.get('input[type="checkbox"]').first().check({ force: true });
    cy.get('input[type="number"]').first().clear().type('1.35');
    cy.get('.btn-add').click();

    cy.contains('.list-item', ruleName, { timeout: 15000 }).should('be.visible').within(() => {
      cy.get('.btn-delete').click();
    });

    cy.contains('.list-item', ruleName).should('not.exist');
  });
});
