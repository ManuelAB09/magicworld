import { backendUrl, loginAsAdmin, mondayWeeksAhead, requestWithCsrf, uniqueId } from './pa-helpers';

const dayOffset: Record<string, number> = {
  MONDAY: 0,
  TUESDAY: 1,
  WEDNESDAY: 2,
  THURSDAY: 3,
  FRIDAY: 4,
  SATURDAY: 5,
  SUNDAY: 6,
};

const getDateForWeekDay = (weekStartDate: string, dayOfWeek: string): string => {
  const date = new Date(`${weekStartDate}T00:00:00`);
  date.setDate(date.getDate() + (dayOffset[dayOfWeek] ?? 0));
  return date.toISOString().slice(0, 10);
};

describe('PA-19 - Cuadrantes y registro de trabajo', () => {
  beforeEach(() => {
    loginAsAdmin();
  });

  it('asigna turno en cuadrante y registra ajuste en worklog', () => {
    const weekStartDate = mondayWeeksAhead(20 + Cypress._.random(0, 20));
    const reason = `PA-19 ajuste ${uniqueId('worklog')}`;

    requestWithCsrf('POST', `${backendUrl}/api/v1/schedules/auto-assign?weekStart=${weekStartDate}`)
      .its('status')
      .should('eq', 200);

    cy.request(`${backendUrl}/api/v1/schedules/week?weekStart=${weekStartDate}`).then((scheduleResponse) => {
      const schedules = scheduleResponse.body as Array<{ employeeId: number; dayOfWeek: string }>;
      expect(schedules).to.be.an('array').and.not.be.empty;

      const sample = schedules[0];
      const targetDate = getDateForWeekDay(weekStartDate, sample.dayOfWeek);

      requestWithCsrf('POST', `${backendUrl}/api/v1/worklog/entry`, {
        employeeId: sample.employeeId,
        action: 'ADD_OVERTIME_HOURS',
        targetDate,
        hoursAffected: 2,
        isOvertime: true,
        reason,
      }).its('status').should('eq', 201);

      cy.request(`${backendUrl}/api/v1/worklog/history/${sample.employeeId}?from=${targetDate}&to=${targetDate}`)
        .its('body')
        .should((entries) => {
          expect(entries).to.be.an('array');
          expect((entries as Array<{ reason: string }>).some((entry) => entry.reason === reason)).to.eq(true);
        });
    });
  });
});
