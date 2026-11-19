# CLAUDE.md

Outsera back-end evaluation: Golden Raspberry Awards (Worst Picture) REST API. The specification is
`Avaliação Outsera - Back-End.pdf` in the repository root.

## Commands

- Run: `./mvnw spring-boot:run` (Windows: `mvnw.cmd`)
- Tests: `./mvnw verify` (integration tests only, `*IT.java`, run by failsafe)
- Export the AI interaction log: `python scripts/export-ai-log.py` (run before every commit/delivery)

## Rules

- Only integration tests (spec requirement). Annotate them with `@IntegrationTest` (unique H2 database per
  Spring context); tests that change data must be `@Transactional`.
- `/api/v1/producers/award-intervals` must stay correct for any dataset: when changing the CSV parsing or
  the interval rule, add a scenario CSV in `src/test/resources/csv` and a case in `AwardIntervalScenariosIT`.
- Every AI interaction must be kept in `docs/ai-log/` (spec requirement).
- Package by feature: `movie`, `producer`, `studio`, `dataload`, `shared`.
- Hexagonal architecture inside each feature: `domain` (plain Java, no Spring/JPA), `application`
  (`port/in` use cases, `port/out` repositories, `*Service` implementations) and `adapter`
  (`in/web`, `in/csv`, `out/persistence`). Dependencies point inward only; `domain` and `application`
  never import from `adapter`.
