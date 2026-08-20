# Authorization Service

Spring Boot authorization service for user registration, credential login, JWT access tokens, and persisted opaque refresh tokens.

## Responsibilities

- Register users after validating their email and password.
- Authenticate users with BCrypt-hashed passwords.
- Issue short-lived JWT access tokens containing the user's roles.
- Store opaque refresh tokens in DynamoDB.
- Validate, revoke, and rotate refresh tokens.
- Protect non-`/auth/**` endpoints with a stateless JWT security filter.

## Authentication flows

### Login

`POST /auth/login`

```json
{
  "username": "user@example.com",
  "password": "Password1!"
}
```

The response contains an access JWT, an opaque refresh token, the access-token lifetime in seconds, the username, and the user's roles.

### Refresh

`POST /auth/refresh`

```json
{
  "refreshToken": "<opaque-refresh-token>"
}
```

The service validates the supplied token, obtains the username from the persisted token record, issues a new access JWT, revokes the old refresh token, and returns a replacement refresh token. The username must not be trusted from the request body.

### Register

`POST /auth/register`

```json
{
  "username": "user@example.com",
  "alias": "User",
  "password": "Password1!",
  "roles": ["ROLE_USER"]
}
```

### Logout

The refresh-token service supports revoking one token or all tokens belonging to a user. A logout endpoint can call these operations according to the application's session policy.

## Configuration

The default local configuration is in `src/main/resources/application.yml`:

```yaml
jwt:
  secret: use-a-32-bytes-HS256-secret
  expiration: 5m
  refreshExpiration: 7d

aws:
  region: us-east-1
  endpoint: http://localhost:8000
```

`service.yml` is a deployment-oriented example and contains placeholder SMTP credentials. Do not commit real secrets. Supply the JWT secret and infrastructure credentials through environment-specific configuration or a secret manager.

The service expects DynamoDB Local at `http://localhost:8000` by default. The configured tables are:

- `dpm-users`: user records, partitioned by username.
- `dpm-refresh-tokens`: refresh-token records, partitioned by username and sorted by token ID.

## Running locally

1. Start DynamoDB Local on port `8000`.
2. Start the application with the `local` profile if required by the deployment setup.
3. Run the service:

```text
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd spring-boot:run`.

## Testing

Run all tests with:

```text
./mvnw test
```

The repository tests use Mockito and verify DynamoDB delegation, key construction, scans, queries, and token lookup behavior.

## Project structure

```text
com.depuramente.auth
├── config       Spring Security, JWT, AWS, and request-filter configuration
├── controller   HTTP endpoints
├── dto          Request and response records
├── model        DynamoDB entities, roles, and Spring Security principal
├── repository   Persistence contracts and DynamoDB implementations
├── service      Authentication, JWT, refresh-token, and user services
└── util         Input validators
```

## Security notes

- Access JWTs are signed with HS256 and include the username and role claims.
- Refresh tokens are opaque random values and are checked against DynamoDB; they are not JWTs.
- Refresh-token rotation invalidates the token used for the refresh request.
- The example JWT secret is for local development only.
- Production deployments should use HTTPS, a strong externally supplied secret, secure secret storage, and carefully restricted CORS origins.
