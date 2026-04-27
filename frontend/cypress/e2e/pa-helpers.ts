export const backendUrl =
  (Cypress.expose('BACKEND_URL') as string) || 'http://localhost:8080';

export const authRateLimitHeaders = (): Record<string, string> => ({
  'X-Forwarded-For': `10.${Cypress._.random(0, 255)}.${Cypress._.random(0, 255)}.${Cypress._.random(1, 254)}`,
});

export const requestWithCsrf = (
  method: 'POST' | 'PUT' | 'PATCH' | 'DELETE',
  url: string,
  body?: Record<string, unknown>
): Cypress.Chainable<Cypress.Response<any>> => {
  return cy.request(`${backendUrl}/api/v1/auth/csrf-token`).then((csrfResponse) => {
    const token = String(csrfResponse.headers['x-xsrf-token'] || '');
    const requestOptions = {
      method,
      url,
      headers: {
        'X-XSRF-TOKEN': token,
      },
      failOnStatusCode: false,
      ...(body !== undefined ? { body } : {}),
    };

    return cy.request(requestOptions);
  });
};

export const nextMonday = (): string => {
  const date = new Date();
  const day = date.getDay();
  const daysUntilMonday = (8 - day) % 7 || 7;
  date.setDate(date.getDate() + daysUntilMonday);
  return date.toISOString().slice(0, 10);
};

export const mondayWeeksAhead = (weeksAhead: number): string => {
  const monday = new Date(`${nextMonday()}T00:00:00`);
  monday.setDate(monday.getDate() + weeksAhead * 7);
  return monday.toISOString().slice(0, 10);
};

export const isoDayOfWeek = (isoDate: string):
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY' => {
  const date = new Date(`${isoDate}T00:00:00`);
  const map = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'] as const;
  const value = map[date.getDay()];
  return value;
};

export const uniqueId = (prefix: string): string => {
  const now = Date.now();
  const random = Math.floor(Math.random() * 1_000_000);
  return `${prefix}_${now}_${random}`;
};

export const futureDate = (daysAhead: number): string => {
  const date = new Date();
  date.setDate(date.getDate() + daysAhead);
  return date.toISOString().slice(0, 10);
};

export const pastDate = (daysBack: number): string => {
  const date = new Date();
  date.setDate(date.getDate() - daysBack);
  return date.toISOString().slice(0, 10);
};

export const loginAsAdmin = (): void => {
  cy.loginAsAdmin();
  cy.visit('/');
};

export const loginAsUser = (): void => {
  cy.loginAsUser();
  cy.visit('/');
};

export const selectFirstAvailableCalendarDay = (): void => {
  cy.get('[data-cy="calendar-day"]').not('.empty').not('.disabled').first().click();
};

export const createValidUserData = () => {
  const id = uniqueId('pa_user');
  return {
    username: id,
    firstname: 'PA',
    lastname: 'User',
    email: `${id}@example.com`,
    password: 'SecurePassword123!',
  };
};

export const safeVisit = (path: string): void => {
  cy.visit(path, {
    failOnStatusCode: false,
  });
};
