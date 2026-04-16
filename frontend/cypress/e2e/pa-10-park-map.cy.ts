describe('PA-10 - Mapa interactivo del parque', () => {
  it('permite interactuar con controles de zoom y reset', () => {
    cy.visit('/park-map');

    cy.get('.park-map__container', { timeout: 20000 }).should('be.visible');
    cy.get('.park-map__controls .control-btn').should('have.length.at.least', 3);

    cy.get('.park-map__controls .control-btn').eq(0).click();
    cy.get('.park-map__controls .control-btn').eq(1).click();
    cy.get('.park-map__controls .control-btn').eq(2).click();

    cy.get('.park-map__error').should('not.exist');
  });
});
