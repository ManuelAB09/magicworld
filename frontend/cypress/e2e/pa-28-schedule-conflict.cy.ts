import { backendUrl, loginAsAdmin, mondayWeeksAhead, requestWithCsrf } from './pa-helpers';

describe('PA-28 - Conflicto por turnos duplicados', () => {
  beforeEach(() => {
    loginAsAdmin();
  });

  it('rechaza segunda asignacion incompatible del mismo empleado y dia', () => {
    const weekStartDate = mondayWeeksAhead(20 + Cypress._.random(0, 20));

    requestWithCsrf('POST', `${backendUrl}/api/v1/schedules/auto-assign?weekStart=${weekStartDate}`)
      .its('status')
      .should('eq', 200);

    cy.request(`${backendUrl}/api/v1/schedules/week?weekStart=${weekStartDate}`).then((scheduleResponse) => {
      const schedules = scheduleResponse.body as Array<{
        employeeId: number;
        dayOfWeek: string;
        breakGroup: string;
        assignedAttractionId?: number;
        assignedZoneId?: number;
      }>;
      expect(schedules).to.be.an('array').and.not.be.empty;

      const existing = schedules[0];
      const payload: Record<string, unknown> = {
        employeeId: existing.employeeId,
        weekStartDate,
        dayOfWeek: existing.dayOfWeek,
        shift: 'FULL_DAY',
        breakGroup: existing.breakGroup || 'A',
      };

      if (existing.assignedAttractionId) {
        payload.assignedAttractionId = existing.assignedAttractionId;
      }
      if (existing.assignedZoneId) {
        payload.assignedZoneId = existing.assignedZoneId;
      }

      requestWithCsrf('POST', `${backendUrl}/api/v1/schedules`, payload).then((response) => {
        expect(response.status).to.not.eq(201);
      });
    });
  });
});
