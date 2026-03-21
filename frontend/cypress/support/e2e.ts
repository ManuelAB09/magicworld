// ***********************************************************
// Archivo de soporte para Cypress E2E
// Se ejecuta antes de cada archivo de spec
// https://docs.cypress.io/guides/core-concepts/writing-and-organizing-tests#Support-file
// ***********************************************************

// Comando personalizado para hacer login via API (evita pasar por la UI cada vez)
Cypress.Commands.add('loginByApi', (username: string, password: string) => {
  cy.request({
    method: 'POST',
    url: '/api/auth/login',
    body: { username, password },
  }).then((response) => {
    expect(response.status).to.eq(200);
    const token = response.body.token;
    window.localStorage.setItem('token', token);
  });
});

// Comando para login via UI
Cypress.Commands.add('loginByUI', (username: string, password: string) => {
  cy.visit('/login');
  cy.get('#username').type(username);
  cy.get('#password').type(password);
  cy.get('button[type="submit"]').click();
});

declare global {
  namespace Cypress {
    interface Chainable {
      loginByApi(username: string, password: string): Chainable<void>;
      loginByUI(username: string, password: string): Chainable<void>;
    }
  }
}

export {};
