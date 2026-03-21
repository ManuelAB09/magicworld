describe('Autenticación - Login', () => {
  beforeEach(() => {
    cy.visit('/login');
  });

  it('debe mostrar el formulario de login', () => {
    cy.get('.auth-form').should('be.visible');
    cy.get('#username').should('be.visible');
    cy.get('#password').should('be.visible');
    cy.get('button[type="submit"]').should('be.visible');
  });

  it('debe mostrar error con credenciales inválidas', () => {
    cy.get('#username').type('usuario_inexistente');
    cy.get('#password').type('contraseña_falsa');
    cy.get('button[type="submit"]').click();
    cy.get('.error').should('be.visible');
  });

  it('debe tener enlace a "olvidé mi contraseña"', () => {
    cy.get('a[href="/forgot-password"]').should('be.visible');
    cy.get('a[href="/forgot-password"]').click();
    cy.url().should('include', '/forgot-password');
  });

  it('debe tener botón de login con Google', () => {
    cy.get('.google-btn').should('be.visible');
  });
});

describe('Autenticación - Registro', () => {
  beforeEach(() => {
    cy.visit('/register');
  });

  it('debe mostrar el formulario de registro completo', () => {
    cy.get('.auth-form').should('be.visible');
    cy.get('#username').should('be.visible');
    cy.get('#firstname').should('be.visible');
    cy.get('#lastname').should('be.visible');
    cy.get('#email').should('be.visible');
    cy.get('#password').should('be.visible');
    cy.get('#confirmPassword').should('be.visible');
    cy.get('button[type="submit"]').should('be.visible');
  });

  it('debe mostrar error si las contraseñas no coinciden', () => {
    cy.get('#username').type('testuser');
    cy.get('#firstname').type('Test');
    cy.get('#lastname').type('User');
    cy.get('#email').type('test@example.com');
    cy.get('#password').type('Password123!');
    cy.get('#confirmPassword').type('OtraPassword456!');
    cy.get('button[type="submit"]').click();
    cy.get('.error').should('be.visible');
  });
});
