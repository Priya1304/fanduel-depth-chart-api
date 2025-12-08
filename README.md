# FanDuel Depth Chart API
A Spring Boot REST API for managing a team’s depth chart — supports adding/removing players, listing backups, and retrieving the full chart by position.

## Run
- Requires JDK 17+.
- Start API: `./gradlew bootRun`
- Base URL: `http://localhost:8080/api/v1`

### Docker
- Build image: `docker build -t depth-chart .`
- Run container: `docker run -p 8080:8080 depth-chart`

## API Reference

### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/{league}/teams/{team}/depth-chart/{position}` | Add a player |
| DELETE | `/api/v1/{league}/teams/{team}/depth-chart/{position}` | Remove a player |
| POST | `/api/v1/{league}/teams/{team}/depth-chart/{position}/backups` | Get backups |
| GET | `/api/v1/{league}/teams/{team}/depth-chart` | Full depth chart |

### Example
- Add player (append): `curl -X POST -H "Content-Type: application/json" -d '{"number":12,"name":"Tom Brady"}' http://localhost:8080/api/v1/NFL/teams/TB/depth-chart/QB`
- Add at depth 1: `.../depth-chart/QB?position_depth=1`
- Remove: `curl -X DELETE -H "Content-Type: application/json" -d '{"number":12,"name":"Tom Brady"}' http://localhost:8080/api/v1/NFL/teams/TB/depth-chart/QB`
- Backups: `curl -X POST -H "Content-Type: application/json" -d '{"number":12,"name":"Tom Brady"}' http://localhost:8080/api/v1/NFL/teams/TB/depth-chart/QB/backups`
- Full chart: `curl http://localhost:8080/api/v1/NFL/teams/TB/depth-chart`

## Configuration & Data
- League/team/position metadata is configured in `application.yml`.
- A few sample depth charts in `src/main/resources/data/` are loaded on startup.
  (Disabled in tests for a clean slate.)

## Tests
- Tests can be run using: `./gradlew test`
- Test coverage includes a few layers:
  - **Unit**: service/business rules and controller validation.
  - **Component**: full Spring context with MockMvc to ensure the wiring works.
  - **Blackbox**: end-to-end flow over HTTP.

- Some edge cases covered:
  - Invalid league/team/position - returns 400 from validation
  - Negative position_depth is rejected 
  - Adding an existing player shifts the ordering correctly
  - No backups / missing players return an empty list ([])

## Postman
- **Collection**: `src/test/resources/postman/DepthChartAPI.postman_collection.json` (kept under test/resources alongside tests).
- We can also run the collection using **Newman**:

  #### newman installation:
  `npm install -g newman`
  #### To run the collection:
  `newman run "src/test/resources/postman/DepthChartAPI.postman_collection.json"`

## API Docs
- Swagger UI is enabled: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Assumptions & Behavior
- Player numbers are unique within a team, but a player can still appear at multiple positions.
- When adding a player with a specific position_depth, players below that depth are shifted down.
- If position_depth is missing or larger than the list size, the player is added to the end.
- Invalid league/team/position values are rejected with a 400 response.
- Cases where a player isn’t found (remove/backups) return an empty list instead of an error.

## Production Considerations (next steps)
- **Persistence**: move in-memory store to Postgres or Redis (in-memory stays as a fast cache)
- **Change events**: publish roster updates so other services (if any) can react in real-time
- **Resilience**: timeouts + retries + circuit breaker once external services come in
- **Observability**:
  - Correlation ID already logged
  - Add metrics & structured logs
- **Operations**: Add basic authentication and request limits if the API is ever exposed outside the internal network.

## Data Model, if we persist in database
For database, we can use tables like
- league (id, code, name)
- team (id, league_id, code, name)
- position (id, league_id, code, name)
- player (id, team_id, number, name) — number stays unique per team
- depth_chart_entry (team_id, position_id, player_id, depth_index, created_at)

That mirrors the current model: one team → many positions → ordered players. 

