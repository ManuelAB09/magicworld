import { backendUrl, loginAsAdmin, requestWithCsrf, uniqueId } from './pa-helpers';

describe('PA-06 - Gestion de tipos de entrada', () => {
  beforeEach(() => {
    loginAsAdmin();
  });

  it('crea, edita y elimina un tipo de entrada', () => {
    const baseName = uniqueId('PA06_Type');
    const updatedName = `${baseName}_EDIT`;
    const anchorTypeName = 'Adult';

    // Keep PA-06 idempotent across retries/reruns.
    cy.task<number>('db:deleteTicketTypesByPrefix', { prefix: 'PA06_Type' });

    cy.task<number | null>('db:getTicketTypeMaxPerDay', { typeName: anchorTypeName }).then((anchorMaxPerDay) => {
      expect(anchorMaxPerDay).to.be.a('number').and.greaterThan(1);
      const reducedAnchorMaxPerDay = (anchorMaxPerDay as number) - 1;

      cy.task<number>('db:setTicketTypeMaxPerDay', {
        typeName: anchorTypeName,
        maxPerDay: reducedAnchorMaxPerDay,
      }).then((affectedRows) => {
        expect(affectedRows).to.be.greaterThan(0);
      });

      requestWithCsrf('POST', `${backendUrl}/api/v1/ticket-types`, {
        typeName: baseName,
        maxPerDay: 1,
        cost: 35,
        description: 'Tipo creado por test PA-06',
        photoUrl: '/images/ticket-types/pa06-ticket.png',
      }).then((createResponse) => {
        expect(createResponse.status).to.eq(201);
        const ticketTypeId = createResponse.body.id as number;

        requestWithCsrf('PUT', `${backendUrl}/api/v1/ticket-types/${ticketTypeId}`, {
          id: ticketTypeId,
          typeName: updatedName,
          maxPerDay: 1,
          cost: 35,
          description: 'Tipo actualizado por test PA-06',
          photoUrl: '/images/ticket-types/pa06-ticket.png',
        }).then((updateResponse) => {
          expect(updateResponse.status).to.eq(200);
        });

        requestWithCsrf('DELETE', `${backendUrl}/api/v1/ticket-types/${ticketTypeId}`).then((deleteResponse) => {
          expect(deleteResponse.status).to.eq(204);
        });

        cy.task<number>('db:setTicketTypeMaxPerDay', {
          typeName: anchorTypeName,
          maxPerDay: anchorMaxPerDay as number,
        }).then((affectedRows) => {
          expect(affectedRows).to.be.greaterThan(0);
        });
      });
    });
  });
});
