import { loginAsAdmin, uniqueId } from './pa-helpers';

describe('PA-18 - Gestion de empleados', () => {
  beforeEach(() => {
    loginAsAdmin();
  });

  it('crea, edita y da de baja un empleado', () => {
    const uid = uniqueId('PA18');
    const firstName = 'Auto';
    const lastName = 'Employee';
    const email = `${uid}@magicworld.com`;

    cy.visit('/admin/employees');
    cy.contains('button', '+').click();

    cy.get('input[name="firstName"]').type(firstName);
    cy.get('input[name="lastName"]').type(lastName);
    cy.get('input[name="email"]').type(email);
    cy.get('input[name="phone"]').type('612345999');
    cy.get('select[name="role"]').select('OPERATOR');
    cy.get('button[type="submit"]').click();

    cy.contains('.employee-card', email, { timeout: 15000 }).should('be.visible').within(() => {
      cy.get('.btn-edit').click();
    });

    cy.get('input[name="phone"]').clear().type('612345111');
    cy.get('button[type="submit"]').click();

    cy.contains('.employee-card', email).should('be.visible').within(() => {
      cy.get('.btn-terminate').click();
    });

    cy.on('window:confirm', () => true);
    cy.contains('.employee-card', email).should('not.exist');
  });
});
