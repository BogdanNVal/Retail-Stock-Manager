<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Retail Stock Manager</title>
    <link rel="stylesheet" href="<c:url value='/css/app.css'/>"/>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
    <main class="page">
        <h1>Retail Stock Manager</h1>
        <p class="subtitlu">Aplicatie de gestiune a stocurilor pentru un magazin retail (Java, Spring Boot, JSP, REST API)</p>

        <div class="sectiune">
            <h2>Gestiune produse <span class="tag">necesita autentificare</span></h2>
            <p>Listeaza, adauga, editeaza si sterge produse (nume, categorie, pret, stoc, cod EAN).</p>
            <a href="<c:url value='/produse'/>">Vezi lista de produse</a>
            <a href="<c:url value='/produse/nou'/>">Adauga produs nou</a>
        </div>

        <div class="sectiune">
            <h2>Casa de marcat <span class="tag">necesita autentificare</span></h2>
            <p>Simuleaza o vanzare: alege produs si cantitate, discountul si TVA-ul se aplica automat, stocul scade, iar bonul poate fi descarcat in PDF.</p>
            <a href="<c:url value='/casa-de-marcat'/>">Deschide casa de marcat</a>
        </div>

        <div class="sectiune">
            <h2>REST API <span class="tag">GET public; POST/PUT/DELETE autentificate</span></h2>
            <p>Endpoint-uri JSON. Citirea e publica; crearea, actualizarea si stergerea cer HTTP Basic sau sesiune autentificata.</p>
            <a href="<c:url value='/api/produse'/>">GET /api/produse</a>
            <p style="margin-top:12px">
                <code>POST /api/produse</code> &middot;
                <code>PUT /api/produse/{id}</code> &middot;
                <code>GET /api/produse/{id}</code> &middot;
                <code>DELETE /api/produse/{id}</code>
            </p>
        </div>

        <c:choose>
            <c:when test="${h2Activ}">
                <div class="sectiune">
                    <h2>Baza de date (H2 console) <span class="tag">profil dev</span></h2>
                    <p>Vezi direct continutul tabelelor din baza de date in-memory.</p>
                    <a href="<c:url value='/h2-console'/>">Deschide H2 console</a>
                    <p style="margin-top:12px">JDBC URL: <code>jdbc:h2:mem:retaildb</code> &middot; user: <code>sa</code> &middot; parola: (goala)</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="sectiune">
                    <h2>Baza de date: MySQL (profil docker)</h2>
                    <p>
                        Aplicatia ruleaza cu <strong>MySQL</strong> (de obicei prin Docker Compose).
                        Datele <strong>persista</strong> intre restart-uri.
                    </p>
                    <p style="margin-top:12px">
                        Pentru inspectie locala poti folosi phpMyAdmin (daca e pornit via Compose)
                        la <code>http://localhost:8081</code>. Credentele DB sunt cele din variabilele de mediu
                        (<code>MYSQL_ROOT_PASSWORD</code> / <code>SPRING_DATASOURCE_*</code>).
                    </p>
                </div>
            </c:otherwise>
        </c:choose>

        <p class="meta">
            Seteaza utilizatorul/parola admin prin <code>APP_ADMIN_USERNAME</code> / <code>APP_ADMIN_PASSWORD</code>
            (implicit: <code>admin</code> / <code>admin123</code> pentru dezvoltare locala).
        </p>
    </main>
</body>
</html>
