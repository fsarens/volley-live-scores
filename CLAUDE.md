# CLAUDE.md — volley-live-scores

## Stack
- Primary language is Java (Spring Boot/Spring Security). Prefer Java idioms and Spring conventions for all backend work.
- Spring Boot 4 · Java 21 · Maven · Thymeleaf + vanilla JS · MongoDB Atlas · Railway (cloud hosting)

## Security Filter Configuration
- When modifying Spring Security filters, always verify filter ordering and confirm custom user services are invoked (not bypassed) in OAuth2/OIDC flows.
- Google uses the OIDC flow — hook into `OidcUserService` (not `DefaultOAuth2UserService`) and wire via `.oidcUserService()` in the security config.
- After OIDC changes, test the full auth flow end-to-end to catch 403s from bypassed custom services.
- Dashboard token filter must run before `OAuth2AuthorizationRequestRedirectFilter` or the OAuth2 redirect fires before the token is validated.

## Deployment (Railway)
- Railway runs behind a proxy: always set `server.forward-headers-strategy=FRAMEWORK` so OAuth2 redirect URIs resolve to the correct public HTTPS URL.
- Use full Spring property name format for env vars (`SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID`) rather than short names with `${...}` placeholders — more reliably picked up by Railway.
- Watch Paths set to `src/**` and `pom.xml` only — docs-only commits do not trigger a redeploy.
