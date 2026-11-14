# BazaarReady

A launch agent for solo sellers and creators. It takes a business from a description or its WhatsApp history to a live shop, proves that customers can actually buy, and guards the inbox against fake enquiries. The owner approves; the agent does the work.

Built for the GoDaddy Inclusive Innovation Hackathon 2026. The full design is in [docs/DESIGN.md](docs/DESIGN.md).

## Stack

- Java 17, Spring Boot 3.5, PostgreSQL 16, Flyway
- React 18 with TypeScript and Vite for the owner's screens; Thymeleaf for the public shop pages
- Claude API, GoDaddy APIs
- Docker

## Run locally

1. Copy `.env.example` to `.env` and fill in `DB_PASSWORD` and `ANTHROPIC_API_KEY`.
2. Start Docker Desktop.
3. Run `docker compose up --build`.
4. Open http://localhost:8090. Health check: http://localhost:8090/actuator/health

## Secrets

Secrets live only in `.env`, which is listed in `.gitignore`. Nothing secret goes in the code or the repository.

## Built with

Claude Code was used as an assistant for both the design and the code.
