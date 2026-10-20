<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- 1) DIRECTIVES: page (above) and include (below) --%>
<%@ include file="header.jsp" %>
<%@ include file="dbconn.jspf" %>

<%-- 2) DECLARATION: variable/method at class level (shared by all requests) --%>
<%!
    int pageHits = 0;
    String greet(String name) { return "Welcome to CampusServe, " + name + "!"; }
%>

<%-- 3) SCRIPTLET: ordinary Java code --%>
<%
    pageHits++;
    String name = request.getParameter("name");           // implicit object: request
    if (name == null || name.trim().isEmpty()) name = "Guest";

    Integer visits = (Integer) session.getAttribute("visits");   // implicit object: session
    visits = (visits == null) ? 1 : visits + 1;
    session.setAttribute("visits", visits);
%>

<h2>JSP Basics Demo</h2>

<%-- 4) EXPRESSIONS: print a value --%>
<p><%= greet(name) %></p>
<p>Today is: <%= new java.util.Date() %></p>
<p>This page was opened <b><%= pageHits %></b> times since the server started (declaration variable).</p>
<p>You have opened it <b><%= visits %></b> times in this session (session object).</p>
<p>Your browser: <%= esc(request.getHeader("User-Agent")) %></p>

<h3>Loop in a scriptlet</h3>
<ul>
<% for (int i = 1; i <= 3; i++) { %>
    <li>Sample shift #<%= i %></li>
<% } %>
</ul>

<h3>Form handling (request parameter)</h3>
<form method="get" action="index.jsp">
    Your name: <input type="text" name="name">
    <input type="submit" value="Greet me">
</form>

<h3>Database check</h3>
<%
    try (Connection c = getConn();
         Statement st = c.createStatement();
         ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM Event")) {
        rs.next();
        out.println("<p>Connected to MySQL. Events in database: <b>" + rs.getInt(1) + "</b></p>");   // implicit object: out
    } catch (Exception e) {
        out.println("<p style='color:red'>Database not reachable: " + esc(e.getMessage()) + "</p>");
    }
%>
