# CLAUDE.md — volley-live-scores

## Stack
- Primary language is Java (Spring Boot/Spring Security). Prefer Java idioms and Spring conventions for all backend work.
- Spring Boot 4 · Java 21 · Maven · Thymeleaf + vanilla JS · MongoDB Atlas · Railway (cloud hosting)

## Security Filter Configuration
- When modifying Spring Security filters, always verify filter ordering and confirm custom user services are invoked (not bypassed) in OAuth2/OIDC flows.
- Google uses the OIDC flow — hook into `OidcUserService` (not `DefaultOAuth2UserService`) and wire via `.oidcUserService()` in the security config.
- After OIDC changes, test the full auth flow end-to-end to catch 403s from bypassed custom services.
- Dashboard token filter must run before `OAuth2AuthorizationRequestRedirectFilter` or the OAuth2 redirect fires before the token is validated.

## Spring Security OAuth2/OIDC Architecture

### Security Filter Chain Order

1. `DashboardTokenFilter` — custom; runs before OAuth2 filters; validates `?token=` param and sets `ROLE_DASHBOARD` auth in session
2. `OAuth2AuthorizationRequestRedirectFilter` — intercepts `/oauth2/authorization/google`, stores auth request, redirects to Google
3. `OAuth2LoginAuthenticationFilter` — intercepts the callback (`/login/oauth2/code/google`); **the key filter**
4. `SecurityContextHolderFilter` — restores/saves SecurityContext from session
5. `AuthorizationFilter` — enforces `authorizeHttpRequests` rules (`ROLE_ADMIN`, `ROLE_SCORER`, `ROLE_DASHBOARD`)

### Why `.userService()` Is Silently Bypassed with Google

Spring Security has two separate provider/service pairs:

- **`DefaultOAuth2UserService`** — handles plain OAuth2 (no ID token); wired via `.userService()`
- **`OidcUserService`** — handles OIDC (Google always returns an ID token); wired via `.oidcUserService()`

When Google returns an `id_token`, `OAuth2LoginAuthenticationFilter` selects `OidcAuthorizationCodeAuthenticationProvider`, which calls `OidcUserService`. A custom service wired via `.userService()` is **never reached** — no error, just default `[OIDC_USER, SCOPE_*]` authorities granted instead of `ROLE_ADMIN`/`ROLE_SCORER`.

### Correct Wiring

```java
.oauth2Login(oauth2 -> oauth2
    .userInfoEndpoint(u -> u.oidcUserService(appOAuth2UserService))
)
```

`AppOAuth2UserService` extends `OidcUserService`, looks up `AppUser` by email in MongoDB, and returns `AppOidcUser` with `ROLE_ADMIN` or `ROLE_SCORER` — overriding all OIDC defaults.

---

## Deployment (Railway)
- Railway runs behind a proxy: always set `server.forward-headers-strategy=FRAMEWORK` so OAuth2 redirect URIs resolve to the correct public HTTPS URL.
- Use full Spring property name format for env vars (`SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID`) rather than short names with `${...}` placeholders — more reliably picked up by Railway.
- Watch Paths set to `src/**` and `pom.xml` only — docs-only commits do not trigger a redeploy.
