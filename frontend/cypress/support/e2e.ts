const backendUrl = (Cypress.expose('BACKEND_URL') as string) || 'http://localhost:8080';

const apiUrl = (path: string): string => `${backendUrl}${path}`;
const randomForwardedFor = (): string =>
  `10.${Cypress._.random(0, 255)}.${Cypress._.random(0, 255)}.${Cypress._.random(1, 254)}`;

type CreatePurchaseOptions = {
  visitDate?: string;
  quantity?: number;
  ticketTypeName?: string;
};

beforeEach(() => {
  // Evita colisiones del rate limit por IP en endpoints de autenticacion durante E2E.
  cy.intercept('**/api/v1/auth/**', (req) => {
    req.headers['x-forwarded-for'] = randomForwardedFor();
  });
});

// Comando personalizado para hacer login via API (evita pasar por la UI cada vez)
Cypress.Commands.add('loginByApi', (username: string, password: string) => {
  cy.request({
    method: 'POST',
    url: apiUrl('/api/v1/auth/login'),
    headers: {
      'X-Forwarded-For': randomForwardedFor(),
    },
    body: { username, password },
    failOnStatusCode: false,
  }).then((response) => {
    expect(response.status).to.eq(200);
  });

  cy.request({
    method: 'GET',
    url: apiUrl('/api/v1/auth/me'),
    failOnStatusCode: false,
  }).then((response) => {
    expect(response.status).to.eq(200);
  });
});

// Comando para login via UI
Cypress.Commands.add('loginByUI', (username: string, password: string) => {
  cy.visit('/login');
  cy.get('[data-cy="login-username"]').type(username);
  cy.get('[data-cy="login-password"]').type(password);
  cy.get('[data-cy="login-submit"]').click();
});

Cypress.Commands.add('loginAsAdmin', () => {
  cy.session(
    'admin-session',
    () => {
      cy.loginByApi('admin', 'AdminPassword123!');
    },
    {
      cacheAcrossSpecs: true,
      validate: () => {
        cy.request({
          method: 'GET',
          url: apiUrl('/api/v1/auth/me'),
          failOnStatusCode: false,
        }).its('status').should('eq', 200);
      },
    }
  );
});

Cypress.Commands.add('loginAsUser', () => {
  cy.session(
    'user-session',
    () => {
      cy.loginByApi('user1', 'SecurePassword123!');
    },
    {
      cacheAcrossSpecs: true,
      validate: () => {
        cy.request({
          method: 'GET',
          url: apiUrl('/api/v1/auth/me'),
          failOnStatusCode: false,
        }).its('status').should('eq', 200);
      },
    }
  );
});

Cypress.Commands.add('logoutClientSession', () => {
  cy.clearCookie('token');
  cy.clearCookie('XSRF-TOKEN');
});

Cypress.Commands.add('getResetTokenForEmail', (email: string) => {
  return cy.task<string | null>('db:getLatestResetToken', { email }).then((token) => {
    expect(token, `reset token for ${email}`).to.be.a('string').and.not.be.empty;
    return token as string;
  });
});

Cypress.Commands.add('createPurchaseForEmail', (email: string, options: CreatePurchaseOptions = {}) => {
  return cy.task<number>('db:createPurchaseForUser', { email, ...options }).then((purchaseId) => {
    expect(purchaseId, `purchase id for ${email}`).to.be.a('number').and.greaterThan(0);
    return purchaseId;
  });
});

declare global {
  namespace Cypress {
    interface Chainable {
      loginByApi(username: string, password: string): Chainable<void>;
      loginByUI(username: string, password: string): Chainable<void>;
      loginAsAdmin(): Chainable<void>;
      loginAsUser(): Chainable<void>;
      logoutClientSession(): Chainable<void>;
      getResetTokenForEmail(email: string): Chainable<string>;
      createPurchaseForEmail(email: string, options?: CreatePurchaseOptions): Chainable<number>;
    }
  }
}

export {};
