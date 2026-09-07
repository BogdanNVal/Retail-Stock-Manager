# Retail Stock Manager

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

Web application for managing the stock of a retail store — a Java/Spring Boot version
of the desktop project [Store Management System (C#/WinForms)](https://github.com/BogdanNVal/c-sharp),
extended with sales, discounts, VAT (TVA), and authentication.

## Technologies

- **Java 21 LTS + Spring Boot 3** (Spring MVC, Spring Data JPA, Spring Security)
- **JSP + JSTL** — classic JEE views alongside Spring
- **MySQL** (Docker profile) or **H2** in-memory (default `dev` profile)
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
| `PUT` | `/api/produse/{id}` | required | update product |
| `DELETE` | `/api/produse/{id}` | required | delete product |

Mutating endpoints accept HTTP Basic or an authenticated browser session.
CSRF is disabled for `/api/**` so scripts can call the API with Basic auth.

Example (create):

```bash
curl -u admin:admin123 -X POST http://localhost:8080/api/produse \
  -H "Content-Type: application/json" \
  -d '{"nume":"Paine","categorie":"ALIMENTAR","pret":5,"cantitateStoc":20,"codEan":"12345670"}'
```

## Profiles

| Profile | When | Database |
|---|---|---|
| `dev` (default) | `mvn spring-boot:run` | H2 in-memory |
| `docker` | Docker Compose sets `SPRING_PROFILES_ACTIVE=docker` | MySQL |

```bash
# local H2 (default)
mvn spring-boot:run

# local MySQL
SPRING_PROFILES_ACTIVE=docker mvn spring-boot:run
```

## Credentials

Configure via environment variables (do not commit production secrets):

| Variable | Default (local demo) |
|---|---|
| `APP_ADMIN_USERNAME` | `admin` |
| `APP_ADMIN_PASSWORD` | `admin123` |
| `MYSQL_ROOT_PASSWORD` | `parola_root` |
| `SPRING_DATASOURCE_*` | see `application-docker.properties` |

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

```bash
docker compose up --build
```

Starts MySQL, phpMyAdmin (`http://localhost:8081`), and the app (`http://localhost:8080`)
with profile `docker`. Override passwords with `MYSQL_ROOT_PASSWORD` and `APP_ADMIN_PASSWORD`.

```bash
docker compose down      # keep data
docker compose down -v  # also remove the MySQL volume
```

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

## Why this project

Built as a study project for a Java/JEE stack typical of enterprise/retail environments:
Spring, JSP, JPA/SQL, design patterns, and deployment — starting from the same business logic
(stock, EAN, billing) previously implemented in C#/WinForms.
