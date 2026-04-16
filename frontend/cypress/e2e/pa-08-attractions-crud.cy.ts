import { backendUrl, loginAsAdmin, requestWithCsrf, uniqueId } from './pa-helpers';

describe('PA-08 - Gestion de atracciones', () => {
  beforeEach(() => {
    loginAsAdmin();
  });

  it('crea por API y completa edicion y borrado desde administracion', () => {
    const name = uniqueId('PA08_Attraction');
    const updatedDescription = 'Descripcion actualizada por PA-08';

    cy.intercept('PUT', `${backendUrl}/api/v1/attractions/*`).as('updateAttraction');
    cy.intercept('DELETE', `${backendUrl}/api/v1/attractions/*`).as('deleteAttraction');

    requestWithCsrf('POST', `${backendUrl}/api/v1/attractions`, {
      name,
      intensity: 'MEDIUM',
      category: 'OTHER',
      minimumHeight: 90,
      minimumAge: 6,
      minimumWeight: 20,
      description: 'Creada para prueba PA-08',
      photoUrl: '/images/attractions/pa08.png',
      isActive: true,
      mapPositionX: 21.5,
      mapPositionY: 18.5,
      openingTime: '10:00:00',
      closingTime: '18:00:00',
    }).then((response) => {
      expect(response.status).to.eq(201);
      const attractionId = response.body.id as number;

      cy.visit(`/attractions/${attractionId}`);
      cy.get('textarea[formcontrolname="description"]').clear().type(updatedDescription);
      cy.get('input[formcontrolname="isActive"]').uncheck({ force: true });
      cy.get('button[type="submit"]').click();
      cy.wait('@updateAttraction').its('response.statusCode').should('eq', 200);

      cy.on('window:confirm', () => true);

      cy.visit('/attractions');
      cy.contains('.attraction-card', name).should('be.visible').within(() => {
        cy.contains(updatedDescription).should('be.visible');
        cy.get('.btn-delete').click();
      });
      cy.wait('@deleteAttraction').its('response.statusCode').should('eq', 204);

      cy.contains('.attraction-card', name).should('not.exist');
    });
  });
});
