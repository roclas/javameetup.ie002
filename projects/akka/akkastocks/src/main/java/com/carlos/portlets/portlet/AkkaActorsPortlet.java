package com.carlos.portlets.portlet;

import java.io.IOException;

import scala.Tuple2;
import scala.Tuple3;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.util.ParamUtil;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Kill;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.display-category=category.sample",
		"com.liferay.portlet.instanceable=true",
		"javax.portlet.display-name=akka actors Portlet",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view_actors.jsp",
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user"
	}, 
	service = Portlet.class
)
public class AkkaActorsPortlet extends MVCPortlet {

    public static Timeout timeout= new Timeout((FiniteDuration) Duration.create("10 seconds"));
    private ActorSystem sys1;
    private ActorRef myActor;
    private int randomPortletIdentifier;

	@Activate
	public void activate(BundleContext context) throws Exception{
		System.out.println("\n\n\n\n\nactivating\n\n\n\n\n");
		randomPortletIdentifier= (new Random()).nextInt();
		try{
			ServiceReference[] refs =context.getAllServiceReferences("akka.actor.ActorSystemImpl",null);
			if(refs!=null){ 
				sys1=(ActorSystem)context.getService(refs[0]); 
				myActor = sys1.actorOf(Props.create(MyUntypedActor.class),"explorerActor");
				myActor.tell(MyUntypedActor.DISCOVER, null); // populates its list of exchange-rate actors
			}
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
	}

	@Deactivate
	public void deactivate(BundleContext context) throws Exception{
		if(sys1!=null)sys1.stop(myActor);
	}
	
	@Override
	public void doView(RenderRequest renderRequest, RenderResponse renderResponse)throws IOException, PortletException {
		long startTime=System.currentTimeMillis();
		if(sys1==null)return;
		java.util.List<Object>result=getStocksActors();
		renderRequest.setAttribute("actors", result);
		renderRequest.setAttribute("portletIdentifier", randomPortletIdentifier);
		renderRequest.setAttribute("requestTime", System.currentTimeMillis()-startTime);
		super.doView(renderRequest, renderResponse);
	}

	private java.util.List<Object> getStocksActors(){
		List<Object> result=new ArrayList<Object>();
		try { 
			result= (List<Object>) Await.result(Patterns.ask(myActor, MyUntypedActor.GET_EXCHANGERS, timeout),timeout.duration());
		} catch (Exception e) { e.printStackTrace(); }
		return result;
	}

	public void reset(ActionRequest req, ActionResponse res) throws IOException, PortletException {
		//myActor.tell(MyUntypedActor.DISCOVER, null);
		myActor.tell(new Tuple3(MyUntypedActor.FORWARD,"/user/myJavaActor",MyUntypedActor.DISCOVER),null);
		myActor.tell(new Tuple3(MyUntypedActor.FORWARD,"/user/explorerActor",MyUntypedActor.DISCOVER),null);
	}

	public void destroyExchangers(ActionRequest req, ActionResponse res) throws IOException, PortletException {
		//myActor.tell(MyUntypedActor.KILL_EXCHANGERS, null);
		myActor.tell(new Tuple3(MyUntypedActor.FORWARD,"/user/myJavaActor",MyUntypedActor.KILL_EXCHANGERS),null);
		myActor.tell(new Tuple3(MyUntypedActor.FORWARD,"/user/explorerActor",MyUntypedActor.KILL_EXCHANGERS),null);
	}
	public void destroyAll(ActionRequest req, ActionResponse res) throws IOException, PortletException {
		//myActor.tell(MyUntypedActor.KILL_ACTORS, null);
		myActor.tell(new Tuple3(MyUntypedActor.FORWARD,"/user/myJavaActor",MyUntypedActor.KILL_ACTORS),null);
		myActor.tell(new Tuple3(MyUntypedActor.FORWARD,"/user/explorerActor",MyUntypedActor.KILL_ACTORS),null);
	}

	public void applyToActors(ActionRequest req, ActionResponse res) throws IOException, PortletException {
		Enumeration<String> names = req.getParameterNames();
		String []actors = ParamUtil.getParameterValues(req, "actors");
		String action= ParamUtil.getString(req, "action");
		String message= ParamUtil.getString(req, "message");
		String actorSelection= ParamUtil.getString(req, "actorSelection");
		if(actorSelection!=null && actorSelection.length()!=0){
			actors=Arrays.copyOf(actors, actors.length + 1);
			actors[actors.length-1]=actorSelection;
		}
		switch(action){
		case "kill":
			myActor.tell(new Tuple3(MyUntypedActor.FORWARD,"/user/myJavaActor",new Tuple2(MyUntypedActor.KILL,actors)),null);
			myActor.tell(new Tuple3(MyUntypedActor.FORWARD,"/user/explorerActor",new Tuple2(MyUntypedActor.KILL,actors)),null);
			break;
		case "send message":
			for(String actor:actors){
				System.out.println(String.format("portlet sending %s to %s", message,actor));
				myActor.tell(new Tuple3(MyUntypedActor.FORWARD,actor,message),null);
			}
			break;
		default: break;
		}
	}
	
}
