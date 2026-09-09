# Retail Stock Manager

I had a C# desktop store app ([Store Management System](https://github.com/BogdanNVal/c-sharp)) and I moved it to Java / Spring Boot. Products, checkout, category discounts, TVA, a PDF receipt, and a small admin login.

## Live demo

**[https://retail-stock-manager.onrender.com](https://retail-stock-manager.onrender.com)**

Login: `admin` / `admin123`

It's on Render's free plan, so after a while it sleeps. First open can take 30–60 seconds. You'll get a short starting page, then the shop.

## Screenshots

### Home

![Home page](docs/screenshots/home.png)

### Product list

![Product list](docs/screenshots/produse.png)

### Checkout

![Checkout / casa de marcat](docs/screenshots/casa-marcat.png)

## What it does

- Add, edit, delete products (name, category, price, stock, EAN)
- Checks EAN-8 / EAN-13
- Checkout with discounts and 19% TVA
- Download the receipt as PDF
- Two people can't overwrite the same product by accident (`@Version`)

Discounts:

| Category | Rule |
|---|---|
| `ALIMENTAR` | 5% off if quantity ≥ 5 |
| `NEALIMENTAR` | 10% off if quantity ≥ 3 |

TVA is 19% on the total after discount.

## Run it

JDK 21 + Maven, or Docker. Profile picks the database, you don't have to edit properties.

| Profile | How | Database |
|---|---|---|
| `dev` | `mvn spring-boot:run` | H2 (in memory) |
| `docker` | `docker compose up --build` | MySQL |
| `prod` | Render | PostgreSQL |

### Docker

```bash
docker compose up --build
```

App: http://localhost:8080  
phpMyAdmin: http://localhost:8081

`docker compose down` keeps the data. `docker compose down -v` deletes it.

### Without Docker

```bash
mvn spring-boot:run
```

H2 is empty when you stop it.

- `/produse` — catalog (login)
- `/casa-de-marcat` — checkout + PDF (login)
- `/api/produse` — GET is public, writes need login
- `/h2-console` — `jdbc:h2:mem:retaildb`, user `sa`, no password

```bash
curl.exe -u admin:admin123 -X POST http://localhost:8080/api/produse \
  -H "Content-Type: application/json" \
  -d '{"nume":"Paine","categorie":"ALIMENTAR","pret":5,"cantitateStoc":20,"codEan":"12345670"}'
```

(`curl.exe` on Windows, not `curl`)

### Tests

```bash
mvn test
```

## Login / passwords

| Variable | Default |
|---|---|
| `APP_ADMIN_USERNAME` | `admin` |
| `APP_ADMIN_PASSWORD` | `admin123` |
| `MYSQL_ROOT_PASSWORD` | `parola_root` |

## Sample products

```bash
./test-data/incarca-produse-test.sh
```

Or the `.ps1` next to it. Sign up / start the app first. Same username and password as above.

## Render

Profile `prod` and a real Postgres URL. H2 on a free host disappears every time it sleeps.

<<<<<<< HEAD
1. Deploy repo as a Docker service.
2. Set `SPRING_PROFILES_ACTIVE=prod` and paste `DATABASE_URL` in the dashboard (`postgresql://…?sslmode=require`).
3. Leave the health-check path empty. The entrypoint binds `$PORT` before Java starts.
=======
- Docker web service from this repo
- `SPRING_PROFILES_ACTIVE=prod`
- `DATABASE_URL` in the dashboard (`postgresql://…?sslmode=require`)
- leave the health check path empty
>>>>>>> ede9081 (Rewrite README)

See [render.yaml](render.yaml).
