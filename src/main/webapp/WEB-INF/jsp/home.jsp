<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Retail Stock Manager</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 800px; margin: 40px auto; padding: 0 20px; color: #222; }
        h1 { margin-bottom: 4px; }
        .subtitlu { color: #666; margin-top: 0; margin-bottom: 32px; }
        .sectiune { border: 1px solid #ddd; border-radius: 8px; padding: 16px 20px; margin-bottom: 16px; }
        .sectiune h2 { margin-top: 0; font-size: 18px; }
        .sectiune p { color: #555; margin: 4px 0 12px; }
        .sectiune a { display: inline-block; margin-right: 16px; text-decoration: none; color: #0b5fff; font-weight: bold; }
        .sectiune a:hover { text-decoration: underline; }
        .sectiune.avertisment { background: #3a2f16; border-color: #6b5a1f; }
        .sectiune.avertisment h2 { color: #f0c040; }
        .tag { display: inline-block; font-size: 12px; background: #eee; color: #444; padding: 2px 8px; border-radius: 4px; margin-left: 8px; }
        code { background: #f4f4f4; padding: 1px 5px; border-radius: 4px; }
    </style>
</head>
<body>
    <h1>Retail Stock Manager</h1>
    <p class="subtitlu">Aplicatie de gestiune a stocurilor pentru un magazin retail (Java, Spring Boot, JSP, REST API)</p>

    <div class="sectiune">
        <h2>Gestiune produse <span class="tag">necesita autentificare</span></h2>
        <p>Listeaza, adauga si sterge produse (nume, categorie, pret, stoc, cod EAN).</p>
        <a href="<c:url value='/produse'/>">Vezi lista de produse</a>
        <a href="<c:url value='/produse/nou'/>">Adauga produs nou</a>
    </div>

    <div class="sectiune">
        <h2>Casa de marcat <span class="tag">necesita autentificare</span></h2>
        <p>Simuleaza o vanzare: alege produs si cantitate, discountul se aplica automat, stocul scade, iar bonul poate fi descarcat in PDF.</p>
        <a href="<c:url value='/casa-de-marcat'/>">Deschide casa de marcat</a>
    </div>

    <div class="sectiune">
        <h2>REST API <span class="tag">JSON, fara autentificare</span></h2>
        <p>Endpoint-uri pentru integrare externa sau testare cu Postman/curl.</p>
        <a href="<c:url value='/api/produse'/>">GET /api/produse</a>
        <p style="margin-top:12px">
            <code>POST /api/produse</code> &middot;
            <code>GET /api/produse/{id}</code> &middot;
            <code>DELETE /api/produse/{id}</code>
        </p>
    </div>

    <c:choose>
        <c:when test="${h2Activ}">
            <div class="sectiune">
                <h2>Baza de date (H2 console) <span class="tag">doar in dezvoltare</span></h2>
                <p>Vezi direct continutul tabelelor din baza de date in-memory.</p>
                <a href="<c:url value='/h2-console'/>">Deschide H2 console</a>
                <p style="margin-top:12px">JDBC URL: <code>jdbc:h2:mem:retaildb</code> &middot; user: <code>sa</code> &middot; parola: (goala)</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="sectiune avertisment">
                <h2>&#9888; Baza de date: MySQL (Docker) &mdash; H2 console indisponibila</h2>
                <p>
                    Aplicatia ruleaza in prezent cu <strong>MySQL</strong> (pornit prin Docker Compose),
                    nu cu H2 in-memory, deci consola H2 nu este activa si datele
                    <strong>persista</strong> intre restart-uri.
                </p>
                <p style="margin-top:12px">
                    Vezi datele direct din browser, cu <strong>phpMyAdmin</strong>
                    (pornit automat prin Docker Compose):
                </p>
                <a href="http://localhost:8081" target="_blank">Deschide phpMyAdmin</a>
                <p style="margin-top:12px">Server: <code>mysql</code> &middot; user: <code>root</code> &middot; parola: <code>parola_root</code></p>
                <p style="margin-top:12px; color:#888; font-size: 13px;">
                    Vrei sa testezi rapid cu H2 in schimb? Comenteaza sectiunea MySQL si
                    decomenteaza sectiunea H2 din <code>application.properties</code>.
                </p>
            </div>
        </c:otherwise>
    </c:choose>

    <p style="color:#999; font-size: 13px; margin-top: 32px;">
        Autentificare admin: <code>admin</code> / <code>admin123</code>
    </p>
</body>
</html>
