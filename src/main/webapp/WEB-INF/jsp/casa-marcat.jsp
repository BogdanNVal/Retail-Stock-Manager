<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Casa de marcat</title>
    <style>
        table { border-collapse: collapse; margin-bottom: 10px; }
        td, th { padding: 4px 8px; }
        .rand-produs select { min-width: 220px; }
        .rand-produs input[type=number] { width: 70px; }
    </style>
</head>
<body>
    <h1>Casa de marcat</h1>

    <c:if test="${not empty eroare}">
        <div style="border:1px solid red; color:red; padding:8px; margin-bottom:10px;">
            ${eroare}
        </div>
    </c:if>

    <c:if test="${not empty bon}">
        <div style="border:1px solid #ccc; padding:10px; margin-bottom:20px;">
            <p><b>Bon inregistrat! (#${bon.id})</b></p>
            <table border="1">
                <tr>
                    <th>Produs</th>
                    <th>Cantitate</th>
                    <th>Total fara discount</th>
                    <th>Discount</th>
                    <th>Total cu discount</th>
                </tr>
                <c:forEach var="linie" items="${bon.linii}">
                    <tr>
                        <td>${linie.produs.nume}</td>
                        <td>${linie.cantitate}</td>
                        <td>${linie.totalFaraDiscount} lei</td>
                        <td>${linie.discountValoare} lei</td>
                        <td>${linie.totalCuDiscount} lei</td>
                    </tr>
                </c:forEach>
            </table>
            <p>
                Subtotal: ${bon.totalFaraDiscount} lei<br/>
                Discount total: ${bon.totalDiscount} lei<br/>
                <b>Total de plata: ${bon.totalCuDiscount} lei</b>
            </p>
            <p><a href="<c:url value='/casa-de-marcat/${bon.id}/bon-pdf'/>">Descarca bonul (PDF)</a></p>
        </div>
    </c:if>

    <form action="<c:url value='/casa-de-marcat/vinde'/>" method="post" id="formVanzare">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

        <table id="tabelProduse">
            <tr>
                <th>Produs</th>
                <th>Cantitate</th>
                <th></th>
            </tr>
            <tr class="rand-produs">
                <td>
                    <select name="produsId" required>
                        <c:forEach var="p" items="${produse}">
                            <option value="${p.id}">${p.nume} (stoc: ${p.cantitateStoc})</option>
                        </c:forEach>
                    </select>
                </td>
                <td><input type="number" name="cantitate" min="1" value="1" required/></td>
                <td><button type="button" onclick="stergeRand(this)">Sterge</button></td>
            </tr>
        </table>

        <button type="button" onclick="adaugaRand()">+ Adauga produs</button>
        <br/><br/>
        <button type="submit">Vinde (genereaza bon)</button>
    </form>

    <!-- Sablon ascuns folosit pentru a adauga randuri noi de produs -->
    <table style="display:none;">
        <tr class="rand-produs" id="randSablon">
            <td>
                <select name="produsId" required>
                    <c:forEach var="p" items="${produse}">
                        <option value="${p.id}">${p.nume} (stoc: ${p.cantitateStoc})</option>
                    </c:forEach>
                </select>
            </td>
            <td><input type="number" name="cantitate" min="1" value="1" required/></td>
            <td><button type="button" onclick="stergeRand(this)">Sterge</button></td>
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
            // pastram cel putin un rand in formular
            if (randuri.length > 1) {
                buton.closest('tr').remove();
            }
        }
    </script>

    <a href="<c:url value='/produse'/>">Gestiune produse</a>
</body>
</html>
