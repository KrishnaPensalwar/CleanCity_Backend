# Neon PostgreSQL setup (Spring Boot)

This backend uses the standard PostgreSQL JDBC driver + Spring Data JPA/Hibernate.
There is no Neon SDK. Point `DATABASE_*` env vars at your Neon project.

## Required environment variables

| Variable | Example (placeholders only) |
|----------|-------------------------------|
| `DATABASE_URL` | `jdbc:postgresql://HOST:5432/DATABASE?sslmode=require` |
| `DATABASE_USERNAME` | `YOUR_NEON_USERNAME` |
| `DATABASE_PASSWORD` | `YOUR_NEON_PASSWORD` |

### Optional

| Variable | Default | Notes |
|----------|---------|-------|
| `JPA_DDL_AUTO` | `update` | Use `update` for first empty Neon schema bootstrap. Switch to `validate` after schema is confirmed. |
| `PORT` | `8080` | HTTP listen port |
| `JWT_SECRET` | _(required)_ | Min 32 bytes |
| `SHOW_SQL` | `false` | Hibernate SQL logging |

Also set your existing AWS/Firebase/JWT vars as needed (see `.env.example`).

## JDBC URL format (Neon)

Neon console strings often look like:

```text
postgresql://USER:PASSWORD@ep-xxxx.region.aws.neon.tech/neondb?sslmode=require
```

For Spring JDBC, use:

```text
jdbc:postgresql://ep-xxxx.region.aws.neon.tech:5432/neondb?sslmode=require
```

Put `USER` / `PASSWORD` in `DATABASE_USERNAME` / `DATABASE_PASSWORD` (do not embed them in chat or git).

`sslmode=require` is required for Neon.

## Local run (Gradle)

```bash
export DATABASE_URL="jdbc:postgresql://HOST:5432/DATABASE?sslmode=require"
export DATABASE_USERNAME="YOUR_NEON_USERNAME"
export DATABASE_PASSWORD="YOUR_NEON_PASSWORD"
export JWT_SECRET="YOUR_32+_BYTE_SECRET"
# optional first-time schema:
export JPA_DDL_AUTO=update

./gradlew bootRun
```

Or copy `.env.example` → `.env`, fill placeholders locally (`.env` is gitignored), then:

```bash
./gradlew bootRun
```

> Note: `spring.config.import=optional:file:.env` is present; if your local `.env` is not picked up as Spring properties, export the variables in your shell as shown above.

## After first successful Neon schema create

1. Confirm tables exist in the Neon SQL editor.
2. Switch Hibernate to validate:

```bash
export JPA_DDL_AUTO=validate
```

or in `application.properties`:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

3. For production, prefer enabling Flyway (`spring.flyway.enabled=true`) with versioned migrations under `src/main/resources/db/migration` instead of relying on `ddl-auto=update`.

## Docker run (placeholders)

```bash
docker run --rm -p 8080:8080 \
  -e PORT=8080 \
  -e DATABASE_URL='jdbc:postgresql://HOST:5432/DATABASE?sslmode=require' \
  -e DATABASE_USERNAME='YOUR_NEON_USERNAME' \
  -e DATABASE_PASSWORD='YOUR_NEON_PASSWORD' \
  -e JPA_DDL_AUTO=update \
  -e JWT_SECRET='YOUR_32+_BYTE_SECRET' \
  -e AWS_S3_BUCKET_NAME='YOUR_BUCKET' \
  -e AWS_ACCESS_KEY_ID='YOUR_AWS_KEY' \
  -e AWS_SECRET_ACCESS_KEY='YOUR_AWS_SECRET' \
  cleancity-backend:latest
```
