describe('PA-07 - Filtro de atracciones', () => {
  it('aplica filtros de altura, peso y edad y permite limpiar', () => {
    cy.visit('/attractions');

    cy.get('.filter-field').eq(0).find('input').clear().type('100');
    cy.get('.filter-field').eq(1).find('input').clear().type('20');
    cy.get('.filter-field').eq(2).find('input').clear().type('5');

    cy.get('.cards-container').should('exist');
    cy.get('.attraction-card').its('length').should('be.greaterThan', 0);

    cy.get('.btn-filter--outline').click();
    cy.get('.filter-field').eq(0).find('input').should('have.value', '');
    cy.get('.filter-field').eq(1).find('input').should('have.value', '');
    cy.get('.filter-field').eq(2).find('input').should('have.value', '');
  });
});
