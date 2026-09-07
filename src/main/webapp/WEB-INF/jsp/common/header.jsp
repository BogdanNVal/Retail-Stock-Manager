<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<header class="site-header">
    <a class="brand" href="<c:url value='/'/>">Retail Stock Manager</a>
    <nav class="site-nav">
        <a href="<c:url value='/'/>">Acasa</a>
        <a href="<c:url value='/produse'/>">Produse</a>
        <a href="<c:url value='/casa-de-marcat'/>">Casa de marcat</a>
        <a href="<c:url value='/api/produse'/>">API</a>
        <sec:authorize access="isAuthenticated()">
            <form action="<c:url value='/logout'/>" method="post">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <button type="submit">Logout</button>
            </form>
        </sec:authorize>
        <sec:authorize access="!isAuthenticated()">
            <a href="<c:url value='/login'/>">Login</a>
        </sec:authorize>
    </nav>
</header>
