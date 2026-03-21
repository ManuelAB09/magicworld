describe('Navegación general', () => {
  it('debe navegar a la página de atracciones', () => {
    cy.visit('/');
    cy.get('a[routerLink="/attractions"]').click();
    cy.url().should('include', '/attractions');
    cy.get('.attractions-page').should('be.visible');
  });

  it('debe navegar a la página de reseñas', () => {
    cy.visit('/');
    cy.get('a[routerLink="/reviews"]').click();
    cy.url().should('include', '/reviews');
  });

  it('debe navegar a la página del mapa del parque', () => {
    cy.visit('/');
    cy.get('a[routerLink="/park-map"]').click();
    cy.url().should('include', '/park-map');
  });

  it('debe navegar al checkout desde la home', () => {
    cy.visit('/');
    cy.get('.buy-tickets-btn').click();
    cy.url().should('include', '/checkout');
  });

  it('debe navegar al login desde la navbar', () => {
    cy.visit('/');
    cy.get('a[routerLink="/login"]').click();
    cy.url().should('include', '/login');
    cy.get('.auth-form').should('be.visible');
  });

  it('debe navegar al registro desde la navbar', () => {
    cy.visit('/');
    cy.get('a[routerLink="/register"]').click();
    cy.url().should('include', '/register');
    cy.get('.auth-form').should('be.visible');
  });

  it('debe permitir cambiar el idioma', () => {
    cy.visit('/');
    cy.get('.lang-select').should('be.visible');
    cy.get('.lang-select').select('en');
  });

  it('debe volver a la home al hacer clic en el logo', () => {
    cy.visit('/attractions');
    cy.get('.brand').click();
    cy.url().should('eq', Cypress.config().baseUrl + '/');
  });
});
