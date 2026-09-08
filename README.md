# Retail Stock Manager

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

Web application for managing the stock of a retail store — a Java/Spring Boot version
of the desktop project [Store Management System (C#/WinForms)](https://github.com/BogdanNVal/c-sharp),
extended with sales, discounts, VAT (TVA), and authentication.

## Live demo

Hosted from branch `cursor/retail-live-demo-f498` (not `main`).

**URL:** https://retail-stock-manager.onrender.com

Render prints **Your service is live** as soon as the container starts — that is **not** when the shop is ready. The Docker entrypoint opens `$PORT` immediately (before the JVM) and shows **Retail Stock Manager is starting** until Spring Boot finishes. Wait until the homepage appears, or look in the logs for:

```text
Entrypoint HTTP bind on 0.0.0.0:10000
Started RetailApplication
```

If logs ever say **New primary port detected… Restarting deploy**, that mid-boot restart causes 502s — redeploy this branch so the entrypoint binds `$PORT` before Java. If the browser spins with no page while logs show `[early-proxy] GET / 200`, redeploy the fix that fails fast when Tomcat accepts TCP before Spring can answer. After idle, the free service sleeps; the next request can take 30–60 seconds (starting page, then the shop).

Demo login: `admin` / `admin123`.

## Screenshots

### Home

![Home page](docs/screenshots/home.png)

### Product list (CRUD)

![Product list](docs/screenshots/produse.png)

### Checkout with discount, TVA, and PDF receipt

![Checkout / casa de marcat](docs/screenshots/casa-marcat.png)

## Technologies

- **Java 21 LTS + Spring Boot 3** (Spring MVC, Spring Data JPA, Spring Security)
- **JSP + JSTL** — classic JEE views alongside Spring
- **MySQL** (Docker profile), **PostgreSQL** (hosted `prod` profile), or **H2** in-memory (default `dev` profile)
- **Docker + Docker Compose** — app, MySQL, and phpMyAdmin
- **iText7** — PDF receipts
- Runs with embedded Tomcat (`java -jar`) or as a `.war` on external Tomcat

## Features

- Product CRUD (name, category, price, stock, EAN) — JSP UI and REST API
- EAN-8 / EAN-13 validation with check digit
- Checkout with category discounts and standard TVA (19%) on the receipt
- Spring Security for admin UI and mutating API calls
- Optimistic locking (`@Version`) on stock-bearing products
- JUnit 5 + Mockito tests (service, controllers, PDF smoke test)
- PDF receipt download from checkout

### Discount rules

| Category | Rule |
|---|---|
| `ALIMENTAR` | 5% off when quantity ≥ 5 |
| `NEALIMENTAR` | 10% off when quantity ≥ 3 |

TVA is applied on the discounted total using `AppConfigSingleton.NivelTva` (default **STANDARD** = 19%).

## REST API

| Method | Route | Auth | Description |
|---|---|---|---|
| `GET` | `/api/produse` | public | list products |
| `GET` | `/api/produse/{id}` | public | product details |
| `POST` | `/api/produse` | required | create product |
| `PUT` | `/api/produse/{id}` | required | update product (**`version` required** for optimistic locking) |
| `DELETE` | `/api/produse/{id}` | required | delete product |

Mutating endpoints accept HTTP Basic or an authenticated browser session.
CSRF is disabled for `/api/**` so scripts can call the API with Basic auth.

Example (create) — **Linux / macOS / Git Bash**:

```bash
curl -u admin:admin123 -X POST http://localhost:8080/api/produse \
  -H "Content-Type: application/json" \
  -d '{"nume":"Paine","categorie":"ALIMENTAR","pret":5,"cantitateStoc":20,"codEan":"12345670"}'
```

**Windows PowerShell** — use `curl.exe` (not `curl`, which is an alias for `Invoke-WebRequest`)
or `Invoke-RestMethod`. The bash-style `'{"json":...}'` quoting often causes a generic HTTP 400 in PowerShell.

```powershell
curl.exe -u admin:admin123 -X POST "http://localhost:8080/api/produse" `
  -H "Content-Type: application/json" `
  -d "{\"nume\":\"Paine\",\"categorie\":\"ALIMENTAR\",\"pret\":5,\"cantitateStoc\":20,\"codEan\":\"12345670\"}"
```

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/produse" -Method Post `
  -Headers @{ Authorization = "Basic " + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin123")) } `
  -ContentType "application/json; charset=utf-8" `
  -Body '{"nume":"Paine","categorie":"ALIMENTAR","pret":5,"cantitateStoc":20,"codEan":"12345670"}'
```

List products:

```powershell
curl.exe http://localhost:8080/api/produse
```

## Profiles

You do **not** need to edit `application.properties` to switch databases (unlike older setups that
commented H2 vs MySQL lines). Use Spring profiles:

| Profile | When | Database |
|---|---|---|
| `dev` (default) | `mvn spring-boot:run` | H2 in-memory |
| `docker` | Docker Compose sets `SPRING_PROFILES_ACTIVE=docker` | MySQL |
| `prod` | Render / Cloud Run (`SPRING_PROFILES_ACTIVE=prod`) | PostgreSQL (Neon or other hosted DB) |

**Linux / macOS:**

```bash
# local H2 (default)
mvn spring-boot:run

# app on host + MySQL (MySQL must already be running, e.g. via Compose)
SPRING_PROFILES_ACTIVE=docker mvn spring-boot:run
```

**Windows PowerShell:**

```powershell
# local H2 (default) — requires JDK 21 + Maven on PATH
mvn spring-boot:run

# app on host + MySQL
$env:SPRING_PROFILES_ACTIVE = "docker"
mvn spring-boot:run
```

If PowerShell says `mvn` is not recognized, install JDK 21 and Maven (or use Chocolatey:
`choco install openjdk21 maven`), reopen the terminal, and run `mvn -version`.
Alternatively skip Maven on the host and use Docker Compose below.

## Credentials

Configure via environment variables (do not commit production secrets).
A `.env` file is **optional** — defaults work for local demos.

| Variable | Default (local demo) |
|---|---|
| `APP_ADMIN_USERNAME` | `admin` |
| `APP_ADMIN_PASSWORD` | `admin123` |
| `MYSQL_ROOT_PASSWORD` | `parola_root` |
| `SPRING_DATASOURCE_*` | see `application-docker.properties` |

Optional project-root `.env` for Docker Compose overrides:

```env
MYSQL_ROOT_PASSWORD=parola_root
APP_ADMIN_USERNAME=admin
APP_ADMIN_PASSWORD=admin123
```

## Running the tests

```bash
mvn test
```

CI runs the same suite on every push/PR (`.github/workflows/ci.yml`).

## Design patterns used

| Pattern | Where |
|---|---|
| **Repository / DAO** | `ProdusRepository`, `VanzareRepository` (Spring Data JPA) |
| **Strategy** | `DiscountStrategy` — food vs non-food discount rules |
| **Singleton** | `AppConfigSingleton` — store name and TVA rate |

## Test data

`test-data/produse-test.json` includes 10 products with valid EANs.

```bash
# Linux / macOS
chmod +x test-data/incarca-produse-test.sh
./test-data/incarca-produse-test.sh
```

```powershell
.\test-data\incarca-produse-test.ps1
```

Both scripts authenticate with `APP_ADMIN_USERNAME` / `APP_ADMIN_PASSWORD`.

## Running with Docker (persistent data)

Requires [Docker Desktop](https://www.docker.com/products/docker-desktop/) (Windows/macOS) or Docker Engine (Linux).

```bash
docker compose up --build
```

Same command works in PowerShell. Starts MySQL, phpMyAdmin (`http://localhost:8081`), and the app
(`http://localhost:8080`) with profile `docker`. Override passwords via a `.env` file or
`MYSQL_ROOT_PASSWORD` / `APP_ADMIN_PASSWORD`.

```bash
docker compose down      # keep data
docker compose down -v  # also remove the MySQL volume
```

H2 data is not migrated when you switch to Docker/MySQL — re-seed products if needed.

## Running locally without Docker

Default profile is `dev` (H2). Data is lost when the process stops.

```bash
mvn spring-boot:run
```

Open `http://localhost:8080`:

- `/produse` — product list / create / edit *(auth)*
- `/casa-de-marcat` — checkout + PDF *(auth)*
- `/api/produse` — REST API (GET public; POST/PUT/DELETE auth)
- `/h2-console` — H2 console (`jdbc:h2:mem:retaildb`, user `sa`, empty password)

## Deploying as a WAR on external Tomcat

```bash
mvn clean package
# copy target/retail-stock-manager.war into Tomcat webapps/
```

The executable WAR also includes Jasper so `java -jar target/retail-stock-manager.war` can render JSPs.

## Hosting (Render or Cloud Run)

Do **not** use the `dev` H2 database on a free host — the filesystem is ephemeral and the catalog disappears on every sleep/redeploy (same lesson as [ApplyLog](https://github.com/BogdanNVal/Applylog)). Profile `prod` talks to **hosted Postgres**.

Set these environment variables:

| Variable | Purpose |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DATABASE_URL` | Neon/Render-style `postgresql://user:pass@host/db?sslmode=require` (converted to JDBC on startup) |
| `SPRING_DATASOURCE_URL` | Optional override if you prefer a ready `jdbc:postgresql://…` URL |
| `APP_ADMIN_USERNAME` / `APP_ADMIN_PASSWORD` | Demo login (defaults `admin` / `admin123`) |
| `PORT` | Set by the host (Render default `10000`). The Docker entrypoint binds `0.0.0.0:$PORT` before Java; Tomcat listens on loopback |
| `JAVA_TOOL_OPTIONS` | Already in the Docker image (Serial GC, RAM cap, IPv4) |

The JVM is capped so a **512 MB** instance has a chance to boot. If Render’s free web service OOMs or never becomes healthy, use Cloud Run instead (do not pay for Render Starter unless both fail).

### Render (first try)

1. Create a Neon Postgres database (free plan does not expire the way Render’s free Postgres does).
2. In Render: **New → Web Service** from this GitHub repo.
3. Select branch **`cursor/retail-live-demo-f498`**, not `main`.
4. Runtime: Docker. Leave **Health Check Path empty** (do not use `/healthz`; `/` also works but is slower because the homepage is ready only after full Spring startup).
5. Paste `DATABASE_URL` from Neon and set `SPRING_PROFILES_ACTIVE=prod`. The name must be exactly `DATABASE_URL` (not `DATABASE_UR`). Include `?sslmode=require`.
6. Optional: this repo’s [render.yaml](render.yaml) is a blueprint; `DATABASE_URL` is `sync: false` so you paste it in the dashboard.

Redeploy after pulling this branch. Logs should show `Entrypoint HTTP bind` **before** the Spring banner. The starting page should appear in 1–2 seconds (not a long blank spinner). Then look for:

```text
prod datasource configured at host …
Started RetailApplication
[early-proxy] backend ready
```

If you see `prod profile requires DATABASE_URL`, fix the env var. If boot stops at Hibernate with no `Started RetailApplication`, copy the later ERROR lines. First request after idle sleep can take 30–60 seconds while Render wakes the instance. Do **not** set a custom Health Check Path.

### Cloud Run (if Render free cannot boot)

Needs a Google account. From this directory, after `gcloud auth login` and a project with billing (Cloud Run’s free tier still covers a sleeping demo):

```bash
gcloud builds submit --tag REGION-docker.pkg.dev/PROJECT/retail/retail-stock-manager
gcloud run deploy retail-stock-manager \
  --image REGION-docker.pkg.dev/PROJECT/retail/retail-stock-manager \
  --region REGION \
  --memory 512Mi \
  --allow-unauthenticated \
  --set-env-vars SPRING_PROFILES_ACTIVE=prod,APP_ADMIN_USERNAME=admin,APP_ADMIN_PASSWORD=admin123 \
  --set-secrets DATABASE_URL=DATABASE_URL:latest
```

If 512Mi is still too small, retry with `--memory 1Gi`. Scale-to-zero means you are not paying for idle time. Cold start is similar to Render.

An always-on alternative (not the first attempt): Oracle Cloud Always Free ARM VM + `docker compose` with MySQL.

## Why this project

Built as a study project for a Java/JEE stack typical of enterprise/retail environments:
Spring, JSP, JPA/SQL, design patterns, and deployment — starting from the same business logic
(stock, EAN, billing) previously implemented in C#/WinForms.
