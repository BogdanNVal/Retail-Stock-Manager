<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Casa de marcat</title>
    <link rel="stylesheet" href="<c:url value='/css/app.css'/>"/>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
    <main class="page">
        <h1>Casa de marcat</h1>

        <c:if test="${not empty eroare}">
            <div class="alert"><c:out value="${eroare}"/></div>
        </c:if>

        <c:if test="${not empty bon}">
            <div class="sectiune">
                <p><b>Bon inregistrat! (#<c:out value="${bon.id}"/>)</b></p>
                <table class="data-table">
                    <tr>
                        <th>Produs</th>
                        <th>Cantitate</th>
                        <th>Total fara discount</th>
                        <th>Discount</th>
                        <th>Total cu discount</th>
                    </tr>
                    <c:forEach var="linie" items="${bon.linii}">
                        <tr>
                            <td><c:out value="${linie.produs.nume}"/></td>
                            <td><c:out value="${linie.cantitate}"/></td>
                            <td><c:out value="${linie.totalFaraDiscount}"/> lei</td>
                            <td><c:out value="${linie.discountValoare}"/> lei</td>
                            <td><c:out value="${linie.totalCuDiscount}"/> lei</td>
                        </tr>
                    </c:forEach>
                </table>
                <p>
                    Subtotal: <c:out value="${bon.totalFaraDiscount}"/> lei<br/>
                    Discount total: <c:out value="${bon.totalDiscount}"/> lei<br/>
                    Total cu discount: <c:out value="${bon.totalCuDiscount}"/> lei<br/>
                    TVA (<c:out value="${bon.procentTva}"/>%): <c:out value="${bon.totalTva}"/> lei<br/>
                    <b>Total de plata (cu TVA): <c:out value="${bon.totalCuTva}"/> lei</b>
                </p>
                <p><a href="<c:url value='/casa-de-marcat/${bon.id}/bon-pdf'/>">Descarca bonul (PDF)</a></p>
            </div>
        </c:if>

        <form action="<c:url value='/casa-de-marcat/vinde'/>" method="post" id="formVanzare">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

            <table class="data-table" id="tabelProduse">
                <tr>
                    <th>Produs</th>
                    <th>Cantitate</th>
                    <th></th>
                </tr>
                <tr class="rand-produs">
                    <td>
                        <select name="produsId" required>
                            <c:forEach var="p" items="${produse}">
                                <option value="${p.id}"><c:out value="${p.nume}"/> (stoc: <c:out value="${p.cantitateStoc}"/>)</option>
                            </c:forEach>
                        </select>
                    </td>
                    <td><input type="number" name="cantitate" min="1" value="1" required/></td>
                    <td><button class="btn-secondary" type="button" onclick="stergeRand(this)">Sterge</button></td>
                </tr>
            </table>

            <button class="btn-secondary" type="button" onclick="adaugaRand()">+ Adauga produs</button>
            <br/><br/>
            <button type="submit">Vinde (genereaza bon)</button>
        </form>

        <table style="display:none;">
            <tr class="rand-produs" id="randSablon">
                <td>
                    <select name="produsId" required>
                        <c:forEach var="p" items="${produse}">
                            <option value="${p.id}"><c:out value="${p.nume}"/> (stoc: <c:out value="${p.cantitateStoc}"/>)</option>
                        </c:forEach>
                    </select>
                </td>
                <td><input type="number" name="cantitate" min="1" value="1" required/></td>
                <td><button class="btn-secondary" type="button" onclick="stergeRand(this)">Sterge</button></td>
            </tr>
        </table>

        <script>
            function adaugaRand() {
                var sablon = document.getElementById('randSablon');
                var randNou = sablon.cloneNode(true);
                randNou.removeAttribute('id');
                document.getElementById('tabelProduse').appendChild(randNou);
            }

            function stergeRand(buton) {
                var tabel = document.getElementById('tabelProduse');
                var randuri = tabel.querySelectorAll('.rand-produs');
                if (randuri.length > 1) {
                    buton.closest('tr').remove();
                }
            }
        </script>
    </main>
</body>
</html>
