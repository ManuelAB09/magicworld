// @ts-check
const eslint = require("@eslint/js");
const tseslint = require("typescript-eslint");
const angular = require("angular-eslint");

module.exports = tseslint.config(
  {
    files: ["**/*.ts"],
    extends: [
      eslint.configs.recommended,
      ...tseslint.configs.recommended,
      ...tseslint.configs.stylistic,
      ...angular.configs.tsRecommended,
    ],
    processor: angular.processInlineTemplates,
    rules: {
      // ─────────────────────────────────────────────────
      // Reglas del config recomendado que NO son parte
      // de los requisitos: se desactivan para no generar
      // ruido en el linteo.
      // ─────────────────────────────────────────────────
      "@angular-eslint/prefer-inject": "off",
      "@typescript-eslint/no-explicit-any": "off",
      "@typescript-eslint/consistent-indexed-object-style": "off",
      "@typescript-eslint/consistent-generic-constructors": "off",
      "@typescript-eslint/no-empty-function": "off",
      "@typescript-eslint/array-type": "off",
      "@typescript-eslint/no-inferrable-types": "off",
      "@typescript-eslint/no-empty-object-type": "off",
      "no-empty": "off",

      // ─────────────────────────────────────────────────
      // Selectores de Angular (directivas y componentes)
      // ─────────────────────────────────────────────────
      "@angular-eslint/directive-selector": [
        "error",
        { type: "attribute", prefix: "app", style: "camelCase" },
      ],
      "@angular-eslint/component-selector": [
        "error",
        { type: "element", prefix: "app", style: "kebab-case" },
      ],

      // ─────────────────────────────────────────────────
      // 1. Sin importaciones / variables no utilizadas
      // ─────────────────────────────────────────────────
      "@typescript-eslint/no-unused-vars": [
        "error",
        {
          "argsIgnorePattern": "^_",
          "varsIgnorePattern": "^_",
          "caughtErrorsIgnorePattern": "^_"
        }
      ],

      // ─────────────────────────────────────────────────
      // 2-4. Convenciones de nombres
      // ─────────────────────────────────────────────────
      "@typescript-eslint/naming-convention": [
        "error",

        // PascalCase para clases, interfaces, enums y type aliases
        { "selector": "typeLike", "format": ["PascalCase"] },
        { "selector": "class",    "format": ["PascalCase"] },

        // camelCase para métodos
        { "selector": "classMethod", "format": ["camelCase"] },
        { "selector": "typeMethod",  "format": ["camelCase"] },

        // camelCase para parámetros (_ inicial permitido para ignorar)
        {
          "selector": "parameter",
          "format": ["camelCase"],
          "leadingUnderscore": "allow"
        },

        // Propiedades de clase static+readonly → UPPER_CASE / camelCase / PascalCase
        {
          "selector": "classProperty",
          "modifiers": ["static", "readonly"],
          "format": ["UPPER_CASE", "camelCase", "PascalCase"]
        },
        // Propiedades de clase readonly (no static) → camelCase O UPPER_CASE (constantes de instancia)
        {
          "selector": "classProperty",
          "modifiers": ["readonly"],
          "format": ["camelCase", "UPPER_CASE"]
        },
        // Resto de propiedades de clase → camelCase
        { "selector": "classProperty", "format": ["camelCase"] },

        // const global exportada → UPPER_CASE | camelCase | PascalCase
        {
          "selector": "variable",
          "modifiers": ["const", "global", "exported"],
          "format": ["UPPER_CASE", "camelCase", "PascalCase"]
        },
        // const global (no exportada) → UPPER_CASE | camelCase | PascalCase
        {
          "selector": "variable",
          "modifiers": ["const", "global"],
          "format": ["UPPER_CASE", "camelCase", "PascalCase"]
        },
        // Resto de variables → camelCase | UPPER_CASE | PascalCase
        {
          "selector": "variable",
          "format": ["camelCase", "UPPER_CASE", "PascalCase"]
        }
      ]
    },
  },
  {
    files: ["**/*.html"],
    extends: [
      ...angular.configs.templateRecommended,
      ...angular.configs.templateAccessibility,
    ],
    rules: {
      // Desactivar reglas de accesibilidad de plantillas no solicitadas
      "@angular-eslint/template/label-has-associated-control": "off",
      "@angular-eslint/template/click-events-have-key-events": "off",
      "@angular-eslint/template/interactive-supports-focus": "off",
    },
  }
);
