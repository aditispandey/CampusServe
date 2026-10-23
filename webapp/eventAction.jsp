<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="dbconn.jspf" %>
<%-- This page has no HTML. It does the INSERT / UPDATE / DELETE, then redirects back to events.jsp --%>
<%
    request.setCharacterEncoding("UTF-8");
    String action = request.getParameter("action");
    String msg;

    try (Connection conn = getConn()) {
        if ("add".equals(action)) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Event (EventName, ClubID, StartDate, EndDate) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, request.getParameter("eventName"));
                ps.setInt(2, Integer.parseInt(request.getParameter("clubId")));
                ps.setDate(3, Date.valueOf(request.getParameter("startDate")));
                ps.setDate(4, Date.valueOf(request.getParameter("endDate")));
                ps.executeUpdate();
            }
            msg = "Event added.";

        } else if ("update".equals(action)) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Event SET EventName = ?, StartDate = ?, EndDate = ?, ApprovalStatus = ? WHERE EventID = ?")) {
                ps.setString(1, request.getParameter("eventName"));
                ps.setDate(2, Date.valueOf(request.getParameter("startDate")));
                ps.setDate(3, Date.valueOf(request.getParameter("endDate")));
                ps.setString(4, request.getParameter("status"));
                ps.setInt(5, Integer.parseInt(request.getParameter("eventId")));
                ps.executeUpdate();
            }
            msg = "Event updated.";

        } else if ("delete".equals(action)) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Event WHERE EventID = ?")) {
                ps.setInt(1, Integer.parseInt(request.getParameter("eventId")));
                ps.executeUpdate();
            }
            msg = "Event deleted.";

        } else {
            msg = "Unknown action.";
        }
    } catch (Exception e) {
        msg = "Error: " + e.getMessage();
    }
    response.sendRedirect("events.jsp?msg=" + java.net.URLEncoder.encode(msg, "UTF-8"));
%>
