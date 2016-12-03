<%@page import="javax.portlet.RenderResponse"%>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.List" %>
<%@ page import="akka.actor.ActorRef" %>

<%@ include file="/init.jsp" %>

<p>
	Portlet_identifier=<%=(int)renderRequest.getAttribute("portletIdentifier")%><br />
	Request time=<%=(long)renderRequest.getAttribute("requestTime")%>
	Advantages of actors:<br />
	-can be easily killed and created (rather than deploying and UNdeploying modules with APIS)<br />
	-they can escalate easily<br />
	actors:<br />
	<%for(ActorRef a:(List<ActorRef>)renderRequest.getAttribute("actors")){%>
		<%=a.path()%><br />
	<%} %>

	<!--  <b><liferay-ui:message key="akkastocks_Akkastocksmvcportlet.caption"/></b> -->
	<strong>Dollar exchanges:</strong><br />
	<% 
		Map<String,Map<String,Double>> exchanges=
			(Map<String,Map<String,Double>>)renderRequest.getAttribute("exchanges");
		Object[] columns=exchanges.keySet().toArray();
	%>
	<table border=1>
	<thead>
	<tr>
		<th>&nbsp;</th> <% for (Object col:columns){ %> <th><%=(String)col%></th> <% } %>
	</tr>
	</thead>
	<tbody>
	<% 
		for (Object row:columns){
		try{
			Map<String,Double> map=exchanges.get((String)row);
	%>
		<tr><td><%=row%></td>
	<% 
			//for (Map.Entry<String,Double> entry : map.getValue().entrySet()){
			for (Object column:columns){
				String value="&nbsp;";
				Double val=map.get((String)column);
				if(map!=null)if(val!=null)value=""+val;
	%>
			<td><%=value%></td>
	<% 
			}
		}catch(Exception e){}
	%>
		</tr>
	<% 
		}
	%>
	</tbody>
	</table>

	
</p>
