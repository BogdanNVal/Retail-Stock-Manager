<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Produse</title>
    <link rel="stylesheet" href="<c:url value='/css/app.css'/>"/>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
    <main class="page">
        <h1>Lista produse</h1>
        <c:if test="${not empty eroareStergere}">
            <p class="alert"><c:out value="${eroareStergere}"/></p>
        </c:if>
        <div class="toolbar">
            <a class="btn" href="<c:url value='/produse/nou'/>">+ Adauga produs</a>
        </div>
        <table class="data-table">
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
                    <td><c:out value="${p.nume}"/></td>
                    <td><c:out value="${p.categorie}"/></td>
                    <td><c:out value="${p.pret}"/></td>
                    <td><c:out value="${p.cantitateStoc}"/></td>
                    <td><c:out value="${p.codEan}"/></td>
                    <td class="actions">
                        <a href="<c:url value='/produse/${p.id}/editeaza'/>">Editeaza</a>
                        <form action="<c:url value='/produse/${p.id}/sterge'/>" method="post" style="display:inline">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <button class="btn-danger" type="submit">Sterge</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </main>
</body>
</html>
