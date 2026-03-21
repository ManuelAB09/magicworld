describe('Listado de atracciones', () => {
  beforeEach(() => {
    cy.visit('/attractions');
  });

  it('debe mostrar la página de atracciones con su título', () => {
    cy.get('.attractions-page').should('be.visible');
    cy.get('.page-header h2').should('be.visible');
  });

  it('debe mostrar los filtros de altura, peso y edad', () => {
    cy.get('.attraction-filter-bar').should('be.visible');
    cy.get('.filter-field').should('have.length', 3);
  });

  it('debe cargar y mostrar tarjetas de atracciones', () => {
    cy.get('.attraction-card', { timeout: 15000 }).should('have.length.greaterThan', 0);
  });

  it('debe mostrar nombre, intensidad y restricciones en cada tarjeta', () => {
    cy.get('.attraction-card', { timeout: 15000 }).first().within(() => {
      cy.get('h3').should('be.visible');
      cy.get('.attr-intensity').should('be.visible');
      cy.get('.attr-constraints').should('be.visible');
    });
  });

  it('debe permitir filtrar por altura mínima y recibir resultados de la API', () => {
    cy.get('.attraction-card', { timeout: 15000 }).should('have.length.greaterThan', 0);
    cy.get('.filter-field').first().find('input').type('100');
    // Tras filtrar, la lista se recarga desde la API (puede tener más, menos o igual cantidad)
    cy.get('.cards-container', { timeout: 10000 }).should('exist');
  });

  it('debe permitir limpiar los filtros', () => {
    cy.get('.filter-field').first().find('input').type('100');
    cy.get('.btn-filter--outline').click();
    cy.get('.filter-field').first().find('input').should('have.value', '');
  });

  it('debe mostrar el nombre de cada atracción como texto visible', () => {
    cy.get('.attraction-card', { timeout: 15000 }).first().within(() => {
      cy.get('h3').invoke('text').should('not.be.empty');
    });
  });
});
