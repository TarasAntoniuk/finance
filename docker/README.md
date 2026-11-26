# Docker Deployment

The API runs as a single container, `finance-app`, on a server that hosts several projects. nginx (TLS termination, reverse proxy), certbot (Let's Encrypt renewal) and PostgreSQL are shared services defined in the separate `server-prod` repository. They reach the app over the external Docker network `app-network`.

```
Internet ──443──> shared-nginx ──app-network──> finance-app:8080 ──app-network──> shared-postgres:5432
```

## Files

```
docker/
├── app/Dockerfile        # eclipse-temurin:21-jre-alpine, runs app.jar
└── docker-compose.yml    # finance-app service, stable profile
```

## Deployment

A push to `dev` runs the `CI/CD Deploy` workflow:

1. run the test suite;
2. build the JAR;
3. copy the JAR and these Docker files to the server;
4. `docker compose down` and `docker compose up -d --build` (a few seconds of downtime);
5. check `/actuator/health`.

## Configuration

`docker-compose.yml` sets `SPRING_PROFILES_ACTIVE=stable` and the datasource URL. Secrets come from an `.env` file next to the compose file on the server:

| Variable | Purpose |
|---|---|
| `FINANCE_DB_PASSWORD` | Password of the `finance_user` database role |
| `JWT_SECRET` | HMAC key for access tokens (32+ characters) |
| `CORS_ALLOWED_ORIGINS` | Comma-separated frontend origins |
| `GOOGLE_CLIENT_ID` | OAuth client id for Google sign-in |

The container port is bound to `127.0.0.1:8080`, so the app is reachable only through nginx on the same host.

## Operations

```bash
docker logs -f finance-app                    # application log
docker compose ps                             # container state and health
docker compose up -d --build                  # rebuild and restart
curl -s http://localhost:8080/actuator/health # health from the server
```

TLS certificates, nginx configuration and database backups are managed in `server-prod`.
