<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Produs nou</title>
</head>
<body>
    <h1>Adauga produs</h1>

    <c:if test="${not empty erori}">
        <div style="border:1px solid red; color:red; padding:8px; margin-bottom:10px;">
            <ul>
                <c:forEach var="eroare" items="${erori}">
                    <li>${eroare}</li>
                </c:forEach>
            </ul>
        </div>
    </c:if>

    <form action="<c:url value='/produse/salveaza'/>" method="post">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        Nume: <input type="text" name="nume" value="${produs.nume}" required/><br/>
        Categorie:
        <select name="categorie">
            <c:forEach var="c" items="${categorii}">
                <option value="${c}" ${c == produs.categorie ? 'selected' : ''}>${c}</option>
            </c:forEach>
        </select><br/>
        Pret: <input type="number" step="0.01" name="pret" value="${produs.pret}" required/><br/>
        Stoc: <input type="number" name="cantitateStoc" value="${produs.cantitateStoc}" required/><br/>
        Cod EAN (8 sau 13 cifre): <input type="text" name="codEan" value="${produs.codEan}" required/><br/>
        <button type="submit">Salveaza</button>
    </form>
    <a href="<c:url value='/produse'/>">Inapoi la lista</a>
</body>
</html>
