# Retail Stock Manager

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

I ported the desktop [Store Management System (C#/WinForms)](https://github.com/BogdanNVal/c-sharp)
to Java and Spring Boot, then added checkout, category discounts, TVA, and a small admin login.

## Live demo

**[https://retail-stock-manager.onrender.com](https://retail-stock-manager.onrender.com)**

[![Live demo](https://img.shields.io/badge/demo-online-brightgreen)](https://retail-stock-manager.onrender.com)

Hosted from branch `cursor/retail-live-demo-f498` (not `main`). Demo login: `admin` / `admin123`.

On Render’s free plan the service sleeps when idle. The first request after that can take 30–60 seconds;
you may see a short starting page, then the shop.

## Screenshots

### Home

![Home page](docs/screenshots/home.png)

### Product list

![Product list](docs/screenshots/produse.png)

### Checkout (discount, TVA, PDF receipt)

![Checkout / casa de marcat](docs/screenshots/casa-marcat.png)

## Stack

- Java 21 LTS, Spring Boot 3 (MVC, Data JPA, Security)
- Thymeleaf for the UI
- MySQL (Docker), PostgreSQL (`prod`), or H2 in-memory (`dev`)
- iText 7 for PDF receipts
- Docker Compose for the local MySQL stack

## What it does

- Product CRUD (name, category, price, stock, EAN) in the browser and over REST
- EAN-8 / EAN-13 check-digit validation
- Checkout with category discounts and 19% TVA on the receipt
- PDF receipt download
- Optimistic locking (`@Version`) on products
- JUnit 5 + Mockito coverage for services, controllers, and a PDF smoke test

### Discount rules

| Category | Rule |
|---|---|
| `ALIMENTAR` | 5% off when quantity ≥ 5 |
| `NEALIMENTAR` | 10% off when quantity ≥ 3 |

TVA is applied on the discounted total (`AppConfigSingleton.NivelTva`, default **STANDARD** = 19%).

## REST API

| Method | Route | Auth | Description |
|---|---|---|---|
| `GET` | `/api/produse` | public | list products |
| `GET` | `/api/produse/{id}` | public | product details |
| `POST` | `/api/produse` | required | create product |
| `PUT` | `/api/produse/{id}` | required | update product (`version` required) |
| `DELETE` | `/api/produse/{id}` | required | delete product |

Mutating calls accept HTTP Basic or a logged-in browser session. CSRF is off for `/api/**`.

```bash
curl -u admin:admin123 -X POST http://localhost:8080/api/produse \
  -H "Content-Type: application/json" \
  -d '{"nume":"Paine","categorie":"ALIMENTAR","pret":5,"cantitateStoc":20,"codEan":"12345670"}'
```

On Windows PowerShell use `curl.exe` (plain `curl` is `Invoke-WebRequest`).

## Run it

Needs **JDK 21** and Maven, or Docker. Profiles pick the database — you do not edit `application.properties` to switch:

| Profile | When | Database |
|---|---|---|
| `dev` (default) | `mvn spring-boot:run` | H2 in-memory |
| `docker` | Compose sets `SPRING_PROFILES_ACTIVE=docker` | MySQL |
| `prod` | Render (`SPRING_PROFILES_ACTIVE=prod`) | PostgreSQL |

### Docker (persistent MySQL)

```bash
docker compose up --build
```

App at `http://localhost:8080`, phpMyAdmin at `http://localhost:8081`.
`docker compose down` keeps the volume; `docker compose down -v` drops it.

### Local without Docker

```bash
mvn spring-boot:run
```

H2 data is gone when the process stops. Useful routes:

- `/produse` — catalog *(auth)*
- `/casa-de-marcat` — checkout + PDF *(auth)*
- `/api/produse` — REST (GET public; writes need auth)
- `/h2-console` — `jdbc:h2:mem:retaildb`, user `sa`, empty password

### Tests

```bash
mvn test
```

CI runs the same suite on every push (`.github/workflows/ci.yml`).

## Credentials

Set via environment variables. Defaults are fine for local demos; do not ship them as production secrets.

| Variable | Default |
|---|---|
| `APP_ADMIN_USERNAME` | `admin` |
| `APP_ADMIN_PASSWORD` | `admin123` |
| `MYSQL_ROOT_PASSWORD` | `parola_root` |

## Test data

`test-data/produse-test.json` has 10 products with valid EANs:

```bash
./test-data/incarca-produse-test.sh
```

There is a PowerShell twin in the same folder. Both use `APP_ADMIN_USERNAME` / `APP_ADMIN_PASSWORD`.

## Hosting on Render

Use profile `prod` and a real Postgres URL — H2 on a free host disappears on every sleep.

1. Select branch **`cursor/retail-live-demo-f498`**, Docker runtime.
2. Set `SPRING_PROFILES_ACTIVE=prod` and `DATABASE_URL` (`postgresql://…?sslmode=require`).
3. Leave the health-check path empty. The Docker entrypoint binds `$PORT` before Java starts.

First boot after idle can take 30–60 seconds. Optional env: `APP_ADMIN_USERNAME` / `APP_ADMIN_PASSWORD`.
The [render.yaml](render.yaml) blueprint is a starting point; paste `DATABASE_URL` in the dashboard (`sync: false`).

## WAR on external Tomcat

`mvn clean package` then copy `target/retail-stock-manager.war` into Tomcat’s `webapps/`. Views are Thymeleaf templates on the classpath.
