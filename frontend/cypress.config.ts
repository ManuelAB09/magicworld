import { defineConfig } from 'cypress';
import mysql from 'mysql2/promise';
import fs from 'node:fs';
import path from 'node:path';

type ResetTokenTaskArgs = {
  email: string;
};

type CreatePurchaseTaskArgs = {
  email: string;
  visitDate?: string;
  quantity?: number;
  ticketTypeName?: string;
};

type InsertTicketTypeTaskArgs = {
  typeName: string;
  description: string;
  cost: number;
  maxPerDay: number;
  photoUrl: string;
};

type TicketTypeMaxPerDayTaskArgs = {
  typeName: string;
  maxPerDay?: number;
};

type DeleteTicketTypesByPrefixTaskArgs = {
  prefix: string;
};

const parseNumber = (value: string | undefined, fallback: number): number => {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
};

const loadDotEnvMap = (): Map<string, string> => {
  const envMap = new Map<string, string>();
  const envPath = path.resolve(__dirname, '..', '.env');

  if (!fs.existsSync(envPath)) {
    return envMap;
  }

  const lines = fs.readFileSync(envPath, 'utf8').split(/\r?\n/);
  lines.forEach((line) => {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith('#')) {
      return;
    }

    const separator = trimmed.indexOf('=');
    if (separator <= 0) {
      return;
    }

    const key = trimmed.slice(0, separator).trim();
    const rawValue = trimmed.slice(separator + 1).trim();
    const unquoted = rawValue.replace(/^"|"$/g, '').replace(/^'|'$/g, '');
    envMap.set(key, unquoted);
  });

  return envMap;
};

const dotenv = loadDotEnvMap();

const getEnvValue = (...keys: string[]): string | undefined => {
  const fromProcess = keys.find((key) => process.env[key]);
  if (fromProcess) {
    return process.env[fromProcess];
  }

  const fromDotEnv = keys.find((key) => dotenv.get(key));
  return fromDotEnv ? dotenv.get(fromDotEnv) : undefined;
};

const cypressBackendUrl = getEnvValue('CYPRESS_BACKEND_URL', 'BACKEND_URL') ?? 'http://localhost:8080';

export default defineConfig({
  allowCypressEnv: false,
  expose: {
    BACKEND_URL: cypressBackendUrl,
  },
  retries: {
    runMode: 2,
    openMode: 0,
  },
  e2e: {
    baseUrl: 'http://localhost:4200',
    supportFile: 'cypress/support/e2e.ts',
    specPattern: 'cypress/e2e/**/*.cy.ts',
    viewportWidth: 1280,
    viewportHeight: 720,
    video: false,
    screenshotOnRunFailure: true,
    defaultCommandTimeout: 10000,
    requestTimeout: 10000,
    setupNodeEvents(on, config) {
      const dbHost = getEnvValue('CYPRESS_DB_HOST') ?? '127.0.0.1';
      const dbPort = parseNumber(getEnvValue('CYPRESS_DB_PORT'), 3306);
      const dbUser = getEnvValue('CYPRESS_DB_USER', 'SPRING_DATASOURCE_USERNAME') ?? 'root';
      const dbPassword = getEnvValue('CYPRESS_DB_PASSWORD', 'SPRING_DATASOURCE_PASSWORD') ?? 'root';
      const dbName = getEnvValue('CYPRESS_DB_NAME') ?? 'magicworld';

      on('task', {
        async 'db:getLatestResetToken'(args: ResetTokenTaskArgs) {
          if (!args?.email) {
            throw new Error('db:getLatestResetToken requires a non-empty email');
          }

          const connection = await mysql.createConnection({
            host: dbHost,
            port: dbPort,
            user: dbUser,
            password: dbPassword,
            database: dbName,
          });

          try {
            const [rows] = await connection.query(
              `SELECT prt.token
               FROM password_reset_token prt
               JOIN users u ON u.id = prt.user_id
               WHERE u.email = ?
               ORDER BY prt.expiry_date DESC
               LIMIT 1`,
              [args.email]
            );

            if (!Array.isArray(rows) || rows.length === 0) {
              return null;
            }

            const token = (rows[0] as { token?: string }).token;
            return token ?? null;
          } finally {
            await connection.end();
          }
        },
        async 'db:createPurchaseForUser'(args: CreatePurchaseTaskArgs) {
          if (!args?.email) {
            throw new Error('db:createPurchaseForUser requires a non-empty email');
          }

          const connection = await mysql.createConnection({
            host: dbHost,
            port: dbPort,
            user: dbUser,
            password: dbPassword,
            database: dbName,
          });

          const visitDate = args.visitDate ?? new Date().toISOString().slice(0, 10);
          const quantity = Number.isFinite(args.quantity) && (args.quantity as number) > 0
            ? Math.floor(args.quantity as number)
            : 1;
          const ticketTypeName = args.ticketTypeName ?? 'Adult';

          try {
            await connection.beginTransaction();

            const [userRows] = await connection.query(
              `SELECT id
               FROM users
               WHERE email = ?
               LIMIT 1`,
              [args.email]
            );

            if (!Array.isArray(userRows) || userRows.length === 0) {
              throw new Error(`User with email ${args.email} not found`);
            }

            const userId = Number((userRows[0] as { id: number }).id);

            const [ticketRows] = await connection.query(
              `SELECT cost
               FROM ticket_type
               WHERE type_name = ?
               LIMIT 1`,
              [ticketTypeName]
            );

            if (!Array.isArray(ticketRows) || ticketRows.length === 0) {
              throw new Error(`Ticket type ${ticketTypeName} not found`);
            }

            const unitCost = Number((ticketRows[0] as { cost: number | string }).cost);
            const totalCost = Number((unitCost * quantity).toFixed(2));

            const [purchaseInsert] = await connection.query(
              `INSERT INTO purchase (purchase_date, buyer_id)
               VALUES (?, ?)`,
              [visitDate, userId]
            );

            const purchaseId = Number((purchaseInsert as { insertId: number }).insertId);

            await connection.query(
              `INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
               VALUES (?, ?, ?, ?, ?)`,
              [visitDate, quantity, purchaseId, totalCost, ticketTypeName]
            );

            await connection.commit();
            return purchaseId;
          } catch (error) {
            await connection.rollback();
            throw error;
          } finally {
            await connection.end();
          }
        },
        async 'db:insertTicketType'(args: InsertTicketTypeTaskArgs) {
          if (!args?.typeName || !args?.description || !args?.photoUrl) {
            throw new Error('db:insertTicketType requires typeName, description and photoUrl');
          }

          const connection = await mysql.createConnection({
            host: dbHost,
            port: dbPort,
            user: dbUser,
            password: dbPassword,
            database: dbName,
          });

          try {
            const [result] = await connection.query(
              `INSERT INTO ticket_type (cost, type_name, description, max_per_day, photo_url)
               VALUES (?, ?, ?, ?, ?)`,
              [args.cost, args.typeName, args.description, args.maxPerDay, args.photoUrl]
            );

            return Number((result as { insertId: number }).insertId);
          } finally {
            await connection.end();
          }
        },
        async 'db:getTicketTypeMaxPerDay'(args: TicketTypeMaxPerDayTaskArgs) {
          if (!args?.typeName) {
            throw new Error('db:getTicketTypeMaxPerDay requires typeName');
          }

          const connection = await mysql.createConnection({
            host: dbHost,
            port: dbPort,
            user: dbUser,
            password: dbPassword,
            database: dbName,
          });

          try {
            const [rows] = await connection.query(
              `SELECT max_per_day
               FROM ticket_type
               WHERE type_name = ?
               LIMIT 1`,
              [args.typeName]
            );

            if (!Array.isArray(rows) || rows.length === 0) {
              return null;
            }

            return Number((rows[0] as { max_per_day: number }).max_per_day);
          } finally {
            await connection.end();
          }
        },
        async 'db:setTicketTypeMaxPerDay'(args: TicketTypeMaxPerDayTaskArgs) {
          if (!args?.typeName || args.maxPerDay === undefined) {
            throw new Error('db:setTicketTypeMaxPerDay requires typeName and maxPerDay');
          }

          const connection = await mysql.createConnection({
            host: dbHost,
            port: dbPort,
            user: dbUser,
            password: dbPassword,
            database: dbName,
          });

          try {
            const [result] = await connection.query(
              `UPDATE ticket_type
               SET max_per_day = ?
               WHERE type_name = ?`,
              [args.maxPerDay, args.typeName]
            );

            return Number((result as { affectedRows: number }).affectedRows);
          } finally {
            await connection.end();
          }
        },
        async 'db:deleteTicketTypesByPrefix'(args: DeleteTicketTypesByPrefixTaskArgs) {
          if (!args?.prefix) {
            throw new Error('db:deleteTicketTypesByPrefix requires prefix');
          }

          const connection = await mysql.createConnection({
            host: dbHost,
            port: dbPort,
            user: dbUser,
            password: dbPassword,
            database: dbName,
          });

          try {
            const [result] = await connection.query(
              `DELETE FROM ticket_type
               WHERE type_name LIKE ?`,
              [`${args.prefix}%`]
            );

            return Number((result as { affectedRows: number }).affectedRows);
          } finally {
            await connection.end();
          }
        },
      });

      return config;
    },
  },
});
