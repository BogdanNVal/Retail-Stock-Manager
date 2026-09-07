<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title><c:out value="${titluFormular}"/></title>
    <link rel="stylesheet" href="<c:url value='/css/app.css'/>"/>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
    <main class="page">
        <h1><c:out value="${titluFormular}"/></h1>

        <c:if test="${not empty erori}">
            <div class="alert">
                <ul>
                    <c:forEach var="eroare" items="${erori}">
                        <li><c:out value="${eroare}"/></li>
                    </c:forEach>
                </ul>
            </div>
        </c:if>

        <form class="form-grid" action="<c:url value='/produse/salveaza'/>" method="post">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <c:if test="${not empty produs.id}">
                <input type="hidden" name="id" value="${produs.id}"/>
                <input type="hidden" name="version" value="${produs.version}"/>
            </c:if>

            <label for="nume">Nume</label>
            <input id="nume" type="text" name="nume" value="<c:out value='${produs.nume}'/>" required/>

            <label for="categorie">Categorie</label>
            <select id="categorie" name="categorie">
                <c:forEach var="c" items="${categorii}">
                    <option value="${c}" ${c == produs.categorie ? 'selected' : ''}><c:out value="${c}"/></option>
                </c:forEach>
            </select>

            <label for="pret">Pret</label>
            <input id="pret" type="number" step="0.01" name="pret" value="<c:out value='${produs.pret}'/>" required/>

            <label for="cantitateStoc">Stoc</label>
            <input id="cantitateStoc" type="number" name="cantitateStoc" value="<c:out value='${produs.cantitateStoc}'/>" required/>

            <label for="codEan">Cod EAN (8 sau 13 cifre)</label>
            <input id="codEan" type="text" name="codEan" value="<c:out value='${produs.codEan}'/>" required/>

            <p style="margin-top:16px">
                <button type="submit">Salveaza</button>
                <a class="btn btn-secondary" href="<c:url value='/produse'/>" style="margin-left:8px">Inapoi la lista</a>
            </p>
        </form>
    </main>
</body>
</html>
