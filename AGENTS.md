# MyRecipes development instructions

## Current application

MyRecipes is an existing Spring Boot MVC application.

Current technologies:
- Java 11
- Spring Boot 2.7.6
- Spring MVC and Thymeleaf
- Spring Security with form login
- Spring Data JPA and Hibernate
- MySQL in production
- H2 for tests
- Maven
- Gemini API for AI nutrition estimates

The current branch is `refactor/rest-api`.
It was created from the tagged working version `mvc-ai-final`.

## Migration goal

Migrate the backend incrementally to a pure JSON REST API under `/api/v1`.
A separate Angular frontend will be created later.

## Important constraints

- Do not delete the existing MVC controllers or Thymeleaf templates until their REST replacements are implemented and tested.
- Do not upgrade Java, Spring Boot, Hibernate, or javax packages during this migration.
- Do not change the database schema unless the current task explicitly requires it.
- Do not return JPA entities directly from REST controllers.
- Use dedicated request and response DTOs.
- Never expose passwords, tokens, lazy JPA relationships, or internal entity fields.
- Use constructor injection for new and modified classes.
- Keep controllers thin; business logic belongs in services.
- Obtain the authenticated user from Spring Security, never from a client-supplied userId.
- New REST endpoints must use `/api/v1`.
- Preserve the existing AI nutrition functionality.
- Invalidate cached nutrition when recipe ingredients or portions change.
- Never commit API keys, passwords, or local configuration.
- Add tests for every new controller or service behavior.
- Run the Maven test suite after every task.
- Make only the changes requested in the current task.
- Explain important design decisions and list changed files.