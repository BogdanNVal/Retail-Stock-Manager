# Retail Stock Manager

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

Web application for managing the stock of a retail store — a Java/Spring Boot version
of the desktop project [Store Management System (C#/WinForms)](#), extended with sales,
discounts, and authentication.

## Technologies

- **Java 21 LTS + Spring Boot 3** (Spring MVC, Spring Data JPA, Spring Security)
- **JSP + JSTL** — to also cover the "classic" JEE side, alongside Spring
- **MySQL** (persistent, runs in a Docker container) — with **H2** in-memory as a fast alternative for testing without Docker
- **Docker + Docker Compose** — starts the application and the database with a single command
- **iText7** — PDF generation for receipts/reports
- Runs both with Spring Boot's embedded Tomcat server and as a `.war` file
  deployed on an external Tomcat

## Features

- Product CRUD (name, category, price, stock, EAN code) — web interface (JSP) and **REST API (JSON)**
- Validation and check-digit calculation for **EAN-8 / EAN-13** codes
- Simple "checkout": pick a product + quantity, stock is decremented and the total is calculated
- Authentication (Spring Security) for the admin area
- **JUnit 5 + Mockito** unit test suite for EAN validation and sales logic
- Export sales receipt as **PDF** (iText) — available directly from the "checkout" interface

## REST API

| Method | Route | Description |
|---|---|---|
| `GET` | `/api/produse` | list of products (JSON) |
| `GET` | `/api/produse/{id}` | product details |
| `POST` | `/api/produse` | create a product |
| `DELETE` | `/api/produse/{id}` | delete a product |

Example request to create a product:

```bash
curl -X POST http://localhost:8080/api/produse \
  -H "Content-Type: application/json" \
  -d '{"nume":"Paine","categorie":"ALIMENTAR","pret":5,"cantitateStoc":20,"codEan":"12345670"}'
```

## Running the tests

```bash
mvn test
```

## Design patterns used

| Pattern | Where |
|---|---|
| **Repository / DAO** | `ProdusRepository`, `VanzareRepository` (Spring Data JPA) |
| **Strategy** | `DiscountStrategy` — different discount rules for food vs. non-food products |
| **Singleton** | `AppConfigSingleton` — global application settings |

## Test data (ready-to-load products)

In `test-data/` there is a `produse-test.json` file with 10 products (with already
valid EAN codes), plus a PowerShell script that loads them automatically through the
REST API, one by one.

With the application running, run this from the project root (in PowerShell):

```powershell
.\test-data\incarca-produse-test.ps1
```

Check the result at `http://localhost:8080/produse` or `http://localhost:8080/api/produse`.

## Running with Docker (recommended — persistent data)

```bash
docker compose up --build
```

Automatically starts three containers: **MySQL** (with persistent data, saved even
after stopping/restarting), **phpMyAdmin** (web interface for the database, at
`http://localhost:8081`), and the Spring Boot **application**, already connected to the
database.

The application will be available at `http://localhost:8080`, same as when run locally.

To stop everything: `docker compose down` (data remains saved).
To completely remove the data as well: `docker compose down -v`.

## Running locally, without Docker (from IntelliJ)

By default, `application.properties` is configured for MySQL via Docker. If you want to
run quickly locally, without Docker and without MySQL installed, switch to H2 in-memory:
in `application.properties`, comment out the 4 lines under "MySQL" and uncomment the
lines under "Alternative: H2". *(Note: with H2, data is lost when the application stops.)*

```bash
mvn spring-boot:run
```

The application starts at `http://localhost:8080`.
Default user: `admin` / `admin123`.

The home page (`http://localhost:8080/`) lists all available sections, with direct links:

- `/produse` — product list and add products *(requires authentication)*
- `/casa-de-marcat` — record sales, with PDF receipt download *(requires authentication)*
- `/api/produse` — REST API (JSON), no authentication required
- `/h2-console` — H2 database console (JDBC URL: `jdbc:h2:mem:retaildb`, user `sa`, no password)

## Deploying as a WAR on an external Tomcat

```bash
mvn clean package
# copy target/retail-stock-manager.war into the webapps/ folder of a locally installed Tomcat
```

## Why this project

Built as a study project to practice a Java/JEE stack typical of enterprise/retail
environments: Spring, JSP, JPA/SQL, design patterns, and deployment on an application
server — starting from the same business logic (stock management, EAN, billing)
previously implemented in C#/WinForms.
