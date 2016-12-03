<%@page import="javax.portlet.RenderResponse"%>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="akka.actor.ActorRef" %>

<%@ include file="/init.jsp" %>

<p>
<portlet:actionURL var="applyToActors" windowState="normal" name="applyToActors" >
</portlet:actionURL>

<form action="<%=applyToActors%>" name="applyToActorsForm"  method="POST">
<aui:select id="choices" name="actors" multiple="true">
	<%for(ActorRef a:(List<ActorRef>)renderRequest.getAttribute("actors")){%>
	    <option value="<%=a.path()%>"><%=a.path()%></option>
	<%} %>
</aui:select>
<br />
Actor Selection:<input type text  name="<portlet:namespace/>actorSelection"> </input><br />
<input type="radio" name="<portlet:namespace/>action" value="kill" checked>kill<br />
<input type="radio" name="<portlet:namespace/>action" value="send message">send message
<input type text  name="<portlet:namespace/>message"> </input><br />

<input type="submit" name="goButton" id="goButton" value="GO"/>
</form>

<portlet:actionURL name="reset" var="resetURL" />
<portlet:actionURL name="destroyExchangers" var="destroyExchangersURL" />
<portlet:actionURL name="destroyAll" var="destroyAllURL" />



<br />
<a href="<%=resetURL%>">[Discover Actors]</a> 
<a href="<%=destroyExchangersURL%>">[Destroy Exchangers]</a> 
<a href="<%=destroyAllURL%>">[Destroy All Actors]</a> <br/>
	

</p>
