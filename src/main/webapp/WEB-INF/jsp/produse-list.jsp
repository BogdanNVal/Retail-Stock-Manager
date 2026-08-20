<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Produse</title>
</head>
<body>
    <h1>Lista produse</h1>
    <c:if test="${not empty eroareStergere}">
        <p style="color:#b00020; border:1px solid #b00020; padding:8px 12px; border-radius:4px; background:#fdecea;">
            ${eroareStergere}
        </p>
    </c:if>
    <a href="<c:url value='/produse/nou'/>">+ Adauga produs</a>
    <table border="1" cellpadding="6">
        <tr>
            <th>Nume</th>
            <th>Categorie</th>
            <th>Pret</th>
            <th>Stoc</th>
            <th>Cod EAN</th>
            <th></th>
        </tr>
        <c:forEach var="p" items="${produse}">
            <tr>
                <td>${p.nume}</td>
                <td>${p.categorie}</td>
                <td>${p.pret}</td>
                <td>${p.cantitateStoc}</td>
                <td>${p.codEan}</td>
                <td>
                    <form action="<c:url value='/produse/${p.id}/sterge'/>" method="post" style="display:inline">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        <button type="submit">Sterge</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
    </table>
</body>
</html>
