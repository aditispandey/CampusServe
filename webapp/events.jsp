<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="header.jsp" %>
<%@ include file="dbconn.jspf" %>

<h2>Events - Create / Read / Update / Delete</h2>

<%
    String msg = request.getParameter("msg");
    if (msg != null) out.println("<p class='msg'>" + esc(msg) + "</p>");
%>

<%-- ========== READ ========== --%>
<table>
  <tr><th>ID</th><th>Event</th><th>Club</th><th>Start</th><th>End</th><th>Status</th><th>Actions</th></tr>
<%
    String listSql = "SELECT e.EventID, e.EventName, c.ClubName, e.StartDate, e.EndDate, e.ApprovalStatus " +
                     "FROM Event e JOIN Club c ON c.ClubID = e.ClubID ORDER BY e.EventID";
    try (Connection conn = getConn();
         Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery(listSql)) {
        while (rs.next()) {
%>
  <tr>
    <td><%= rs.getInt("EventID") %></td>
    <td><%= esc(rs.getString("EventName")) %></td>
    <td><%= esc(rs.getString("ClubName")) %></td>
    <td><%= rs.getDate("StartDate") %></td>
    <td><%= rs.getDate("EndDate") %></td>
    <td><%= rs.getString("ApprovalStatus") %></td>
    <td>
      <a href="editEvent.jsp?id=<%= rs.getInt("EventID") %>">Edit</a>
      <form class="inline" method="post" action="eventAction.jsp" onsubmit="return confirm('Delete this event?');">
        <input type="hidden" name="action" value="delete">
        <input type="hidden" name="eventId" value="<%= rs.getInt("EventID") %>">
        <input type="submit" value="Delete">
      </form>
    </td>
  </tr>
<%
        }
    } catch (Exception e) {
        out.println("<tr><td colspan='7' style='color:red'>Error: " + esc(e.getMessage()) + "</td></tr>");
    }
%>
</table>

<%-- ========== CREATE ========== --%>
<h3>Add a new event</h3>
<form method="post" action="eventAction.jsp">
  <input type="hidden" name="action" value="add">
  Name: <input type="text" name="eventName" required>
  Club:
  <select name="clubId">
<%
    try (Connection conn = getConn();
         Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery("SELECT ClubID, ClubName FROM Club")) {
        while (rs.next()) {
            out.println("<option value='" + rs.getInt(1) + "'>" + esc(rs.getString(2)) + "</option>");
        }
    } catch (Exception e) { /* dropdown stays empty */ }
%>
  </select>
  Start: <input type="date" name="startDate" required>
  End: <input type="date" name="endDate" required>
  <input type="submit" value="Add Event">
</form>
