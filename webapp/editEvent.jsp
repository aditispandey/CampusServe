<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="header.jsp" %>
<%@ include file="dbconn.jspf" %>

<h2>Edit Event (UPDATE)</h2>
<%
    String id = request.getParameter("id");
    try (Connection conn = getConn();
         PreparedStatement ps = conn.prepareStatement(
             "SELECT EventName, StartDate, EndDate, ApprovalStatus FROM Event WHERE EventID = ?")) {
        ps.setInt(1, Integer.parseInt(id));
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String status = rs.getString("ApprovalStatus");
%>
<form method="post" action="eventAction.jsp">
  <input type="hidden" name="action" value="update">
  <input type="hidden" name="eventId" value="<%= esc(id) %>">
  Name: <input type="text" name="eventName" value="<%= esc(rs.getString("EventName")) %>" required><br>
  Start: <input type="date" name="startDate" value="<%= rs.getDate("StartDate") %>" required><br>
  End: <input type="date" name="endDate" value="<%= rs.getDate("EndDate") %>" required><br>
  Status:
  <select name="status">
    <option <%= "PENDING".equals(status)  ? "selected" : "" %>>PENDING</option>
    <option <%= "APPROVED".equals(status) ? "selected" : "" %>>APPROVED</option>
    <option <%= "REJECTED".equals(status) ? "selected" : "" %>>REJECTED</option>
  </select><br><br>
  <input type="submit" value="Save changes"> <a href="events.jsp">Cancel</a>
</form>
<%
            } else {
                out.println("<p>Event not found.</p>");
            }
        }
    } catch (Exception e) {
        out.println("<p style='color:red'>Error: " + esc(e.getMessage()) + "</p>");
    }
%>
