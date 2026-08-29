# Local Setup

## Prerequisites

- Node.js 20.19+
- npm 10+
- A running instance of the City Voice API (separate repository)

## Install

    npm install

## Development server

    npm start

Runs on `http://localhost:4200` with SSR enabled.

## SSR production build

    npm run build:ssr
    npm run serve:ssr

The Node server listens on port 4000, or the port set in the `PORT` environment variable.

## Backend URL

Set in `src/environments/environment.ts`:

    export const environment = {
      production: false,
      apiUrl: 'http://localhost:8080/api',
    };

Change `apiUrl` if the API runs on a different host or port.

The production build replaces this file with `environment.prod.ts`, which uses the relative path `/api` and assumes frontend and API are served from the same origin.

## CORS

The API must allow the origin the frontend is served from: `http://localhost:4200` in development, `http://localhost:4000` when testing the SSR production build.

## Scripts

| Command | Description |
|---|---|
| `npm start` | Development server |
| `npm run build` | Production build |
| `npm run build:ssr` | SSR production build |
| `npm run serve:ssr` | Start the SSR server |
| `npm run dev:ssr` | Build and serve SSR |
| `npm run watch` | Build in watch mode |
| `npm test` | Run tests (Vitest) |
| `npm run lint` | ESLint |
| `npx prettier --write .` | Format |
