import { loginAsUser } from './pa-helpers';

describe('PA-24 - Bloqueo de fechas pasadas en compra', () => {
  beforeEach(() => {
    loginAsUser();
  });

  it('impide navegar a fechas de visita pasadas en calendario', () => {
    cy.visit('/checkout');

    cy.get('.calendar-nav').first().should('be.disabled');
    cy.get('[data-cy="calendar-day"].disabled').should('exist');
  });
});
