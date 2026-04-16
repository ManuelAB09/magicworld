import { backendUrl, loginAsAdmin, mondayWeeksAhead, pastDate, requestWithCsrf, uniqueId } from './pa-helpers';

describe('PA-29 - Ajustes de worklog solo en fechas futuras', () => {
  beforeEach(() => {
    loginAsAdmin();
  });

  it('rechaza ajuste en fecha pasada y acepta en fecha futura', () => {
    cy.request(`${backendUrl}/api/v1/employees/active`).then((employeesResponse) => {
      const employee = (employeesResponse.body as any[])[0];
      expect(employee).to.exist;

      const employeeId = employee.id as number;
      const targetDate = mondayWeeksAhead(20 + Cypress._.random(0, 20));

      const basePayload = {
        employeeId,
        action: 'ADD_OVERTIME_HOURS',
        hoursAffected: 1,
        isOvertime: true,
        reason: `PA-29 ${uniqueId('worklog')}`,
      };

      requestWithCsrf('POST', `${backendUrl}/api/v1/worklog/entry`, {
        ...basePayload,
        targetDate: pastDate(1),
      }).then((pastResponse) => {
        expect(pastResponse.status).to.not.eq(201);
      });

      requestWithCsrf('POST', `${backendUrl}/api/v1/worklog/entry`, {
        ...basePayload,
        targetDate,
      }).its('status').should('eq', 201);
    });
  });
});
