import { loginAsAdmin, uniqueId } from './pa-helpers';

describe('PA-09 - Chatbot administrativo', () => {
  beforeEach(() => {
    loginAsAdmin();
  });

  it('envia instruccion y recibe respuesta operativa', () => {
    cy.visit('/chatbot');
    cy.get('.message-input').should('be.visible');

    cy.get('.message-input').type(`Consulta PA-09 ${uniqueId('chatbot')}`);
    cy.get('.send-btn').click();

    cy.get('.message.assistant', { timeout: 30000 }).should('have.length.greaterThan', 0);
    cy.get('.messages-container').should('be.visible');
  });
});
