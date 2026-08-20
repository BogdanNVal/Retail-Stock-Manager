# Retail Stock Manager

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

Aplicație web pentru gestiunea stocurilor unui magazin retail — versiune Java/Spring Boot
a proiectului desktop [Store Management System (C#/WinForms)](#), extinsă cu vânzări, discounturi
și autentificare.

## Tehnologii

- **Java 21 LTS + Spring Boot 3** (Spring MVC, Spring Data JPA, Spring Security)
- **JSP + JSTL** — pentru a acoperi și partea "clasică" de JEE, alături de Spring
- **MySQL** (persistent, rulează într-un container Docker) — cu **H2** in-memory ca alternativă rapidă pentru testare fără Docker
- **Docker + Docker Compose** — pornește aplicația și baza de date cu o singură comandă
- **iText7** — generare PDF pentru bonuri/rapoarte
- Rulează atât cu serverul Tomcat embedded din Spring Boot, cât și ca fișier `.war`
  deployat pe un Tomcat extern

## Funcționalități

- CRUD produse (nume, categorie, preț, stoc, cod EAN) — interfață web (JSP) și **REST API (JSON)**
- Validare și calcul cifră de control pentru coduri **EAN-8 / EAN-13**
- "Casă de marcat" simplă: alegi produs + cantitate, se scade din stoc și se calculează totalul
- Autentificare (Spring Security) pentru zona de administrare
- Suită de **teste unitare JUnit 5 + Mockito** pentru validarea EAN și logica de vânzări
- Export bon de vânzare în **PDF** (iText) — disponibil direct din interfața "casă de marcat"

## REST API

| Metodă | Rută | Descriere |
|---|---|---|
| `GET` | `/api/produse` | listă produse (JSON) |
| `GET` | `/api/produse/{id}` | detalii produs |
| `POST` | `/api/produse` | creează produs |
| `DELETE` | `/api/produse/{id}` | șterge produs |

Exemplu request pentru creare produs:

```bash
curl -X POST http://localhost:8080/api/produse \
  -H "Content-Type: application/json" \
  -d '{"nume":"Paine","categorie":"ALIMENTAR","pret":5,"cantitateStoc":20,"codEan":"12345670"}'
```

## Rulare teste

```bash
mvn test
```

## Design patterns folosite

| Pattern | Unde |
|---|---|
| **Repository / DAO** | `ProdusRepository`, `VanzareRepository` (Spring Data JPA) |
| **Strategy** | `DiscountStrategy` — reguli diferite de discount pentru produse alimentare vs. nealimentare |
| **Singleton** | `AppConfigSingleton` — setări globale ale aplicației |

## Date de test (produse gata de încărcat)

În `test-data/` există un fișier `produse-test.json` cu 10 produse (cu coduri EAN deja
valide), plus un script PowerShell care le încarcă automat prin REST API, unul câte unul.

Cu aplicația pornită, rulează din rădăcina proiectului (în PowerShell):

```powershell
.\test-data\incarca-produse-test.ps1
```

Verifici rezultatul la `http://localhost:8080/produse` sau `http://localhost:8080/api/produse`.

## Rulare cu Docker (recomandat — date persistente)

```bash
docker compose up --build
```

Pornește automat trei containere: **MySQL** (cu date persistente, salvate chiar și după
oprire/repornire), **phpMyAdmin** (interfață web pentru baza de date, la `http://localhost:8081`)
și **aplicația** Spring Boot, deja conectată la baza de date.

Aplicația va fi disponibilă pe `http://localhost:8080`, la fel ca la rularea locală.

Pentru a opri totul: `docker compose down` (datele rămân salvate).
Pentru a șterge complet și datele: `docker compose down -v`.

## Rulare locală, fără Docker (din IntelliJ)

Implicit, `application.properties` e configurat pentru MySQL prin Docker. Dacă vrei să
rulezi rapid local, fără Docker și fără MySQL instalat, comută pe H2 in-memory:
în `application.properties`, comentează cele 4 linii de sub "MySQL" și decomentează
liniile de sub "Alternativa: H2". *(Notă: cu H2, datele se pierd la oprirea aplicației.)*

```bash
mvn spring-boot:run
```

Aplicația pornește pe `http://localhost:8080`.
Utilizator implicit: `admin` / `admin123`.

Pagina de start (`http://localhost:8080/`) listează toate secțiunile disponibile,
cu link-uri directe:

- `/produse` — listă și adăugare produse *(necesită autentificare)*
- `/casa-de-marcat` — înregistrare vânzări, cu descărcare bon PDF *(necesită autentificare)*
- `/api/produse` — REST API (JSON), fără autentificare
- `/h2-console` — consola bazei de date H2 (JDBC URL: `jdbc:h2:mem:retaildb`, user `sa`, fără parolă)

## Deploy ca WAR pe Tomcat extern

```bash
mvn clean package
# copiaza target/retail-stock-manager.war in webapps/ al unui Tomcat instalat local
```

## De ce acest proiect

Realizat ca proiect de studiu pentru a exersa un stack Java/JEE tipic mediilor enterprise/retail:
Spring, JSP, JPA/SQL, design patterns și deploy pe server de aplicații — pornind de la aceeași
logică de business (gestiune stoc, EAN, facturare) implementată anterior în C#/WinForms.
