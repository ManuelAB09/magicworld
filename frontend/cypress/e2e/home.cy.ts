describe('Página de inicio', () => {
  beforeEach(() => {
    cy.visit('/');
  });

  it('debe mostrar el hero con el vídeo promocional', () => {
    cy.get('.home-hero').should('be.visible');
    cy.get('video.hero-video-bg').should('exist');
  });

  it('debe mostrar el botón de comprar entradas que lleva a /checkout', () => {
    cy.get('.buy-tickets-btn').should('be.visible');
    cy.get('.buy-tickets-btn').click();
    cy.url().should('include', '/checkout');
  });

  it('debe mostrar la barra de navegación con los enlaces principales', () => {
    cy.get('.custom-navbar').should('be.visible');
    cy.get('a[routerLink="/attractions"]').should('exist');
    cy.get('a[routerLink="/reviews"]').should('exist');
    cy.get('a[routerLink="/park-map"]').should('exist');
  });

  it('debe mostrar los enlaces de login y registro cuando no está autenticado', () => {
    cy.get('a[routerLink="/login"]').should('be.visible');
    cy.get('a[routerLink="/register"]').should('be.visible');
  });
});
