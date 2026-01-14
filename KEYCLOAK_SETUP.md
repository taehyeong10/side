# Keycloak Setup Guide

This guide will help you set up Keycloak authentication for the Side application.

## Quick Start

### 1. Start Services

```bash
./gradlew bootRun
```

This will automatically start:
- PostgreSQL (port 1234) with two databases: `side` and `keycloak`
- Elasticsearch (port 9200)
- Kibana (port 5601)
- Keycloak (port 8180)

### 2. Access Keycloak Admin Console

- URL: http://localhost:8180
- Username: `admin`
- Password: `admin`

### 3. Create a Realm

1. Click on the dropdown at the top left (currently shows "master")
2. Click "Create Realm"
3. Set Realm name: `side-realm`
4. Click "Create"

### 4. Create a Client

1. In the left sidebar, click "Clients"
2. Click "Create client"
3. Fill in the details:
   - **Client ID**: `side-client`
   - **Client authentication**: ON (this makes it a confidential client)
   - **Authorization**: OFF (unless you need fine-grained authorization)
4. Click "Next"
5. Configure the following settings:
   - **Valid redirect URIs**: `http://localhost:8080/*`
   - **Web origins**: `http://localhost:8080`
6. Click "Save"

### 5. Get Client Secret

1. Go to the "Credentials" tab of your `side-client`
2. Copy the "Client secret" value
3. Update `application.yaml`:

```yaml
keycloak:
  credentials:
    secret: <paste-your-client-secret-here>
```

### 6. Create a Test User

1. In the left sidebar, click "Users"
2. Click "Add user"
3. Fill in:
   - **Username**: `testuser`
   - **Email**: `test@example.com`
   - **First name**: `Test`
   - **Last name**: `User`
4. Click "Create"
5. Go to the "Credentials" tab
6. Click "Set password"
7. Set password: `password` (or your choice)
8. Turn OFF "Temporary" toggle
9. Click "Save"

### 7. Assign Roles (Optional)

1. In the left sidebar, click "Realm roles"
2. Click "Create role"
3. Create roles like: `USER`, `ADMIN`, etc.
4. Go back to "Users" → Select your test user
5. Click "Role mapping" tab
6. Click "Assign role"
7. Select the roles you want to assign

## Testing Authentication

### Get Access Token

```bash
curl -X POST 'http://localhost:8180/realms/side-realm/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=side-client' \
  -d 'client_secret=<your-client-secret>' \
  -d 'username=testuser' \
  -d 'password=password' \
  -d 'grant_type=password'
```

This will return a JSON response with an `access_token`.

### Use Token in API Requests

```bash
curl -X GET 'http://localhost:8080/member/1' \
  -H 'Authorization: Bearer <your-access-token>'
```

## Security Configuration

The application is configured to:
- **Require authentication** for all endpoints except:
  - `/actuator/**`
  - `/health/**`
  - `/swagger-ui/**`
  - `/v3/api-docs/**`

To modify this, edit `SecurityConfig.java`.

## Database Information

### PostgreSQL
- **Host**: localhost:1234
- **Databases**:
  - `side` - Application database
  - `keycloak` - Keycloak database
- **Username**: postgres
- **Password**: postgres

### Keycloak Database
Keycloak automatically creates its tables in the `keycloak` database on first startup.

## Troubleshooting

### Keycloak doesn't start
- Check Docker logs: `docker logs side-keycloak`
- Ensure PostgreSQL is healthy: `docker ps`

### "Invalid issuer" error
- Make sure the realm name in `application.yaml` matches exactly
- Check that Keycloak is running: http://localhost:8180

### "Invalid token" error
- Token may have expired (default: 5 minutes)
- Request a new token using the curl command above
- Verify client secret is correct in `application.yaml`

### Database connection issues
- If upgrading from older version, delete volumes: `docker-compose down -v`
- This will recreate fresh databases

## Development vs Production

**Current configuration is for DEVELOPMENT ONLY:**
- `ssl-required: none`
- `KC_HTTP_ENABLED: true`
- Default admin credentials

**For production:**
1. Enable HTTPS/TLS
2. Change all default passwords
3. Use proper secrets management
4. Configure appropriate CORS settings
5. Set `ssl-required: external` or `all`
