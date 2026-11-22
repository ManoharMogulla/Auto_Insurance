# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.

## Insurance services

The Vite development server sends all `/api/*` and `/customer/*` requests through the API Gateway at `http://localhost:8080` by default. Set `VITE_API_GATEWAY_URL` in a local `.env` file to override the gateway URL. The gateway uses Eureka to locate the registered authentication, customer, policy, payment, claims, and document services.

The frontend calls `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`, and `POST /api/auth/logout`; authentication and customer profile services must share `JWT_SECRET`, and both services must share `INTERNAL_SERVICE_TOKEN`. Production deployments should expose the gateway at the frontend's origin or configure a reverse proxy for these API paths.
