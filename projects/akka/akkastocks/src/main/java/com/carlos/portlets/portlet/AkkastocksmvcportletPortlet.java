package com.carlos.portlets.portlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import com.carlos.utils.FuturesAndActorsUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.Tuple3;
import scala.collection.Iterator;
import scala.collection.immutable.List;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.display-category=category.sample",
		"com.liferay.portlet.instanceable=true",
		"javax.portlet.display-name=akkastocks Portlet",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user"
	}, service = Portlet.class
)
public class AkkastocksmvcportletPortlet extends MVCPortlet {

    public static Timeout timeout= new Timeout((FiniteDuration) Duration.create("5 seconds"));
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
				myActor = sys1.actorOf(Props.create(MyUntypedActor.class),"myJavaActor");
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
		java.util.List<Object>actors=getStocksActors();
		renderRequest.setAttribute("actors", actors);
		Map<String,Map<String,Double>>result=getStocksMatrix();
		renderRequest.setAttribute("exchanges", result);
		renderRequest.setAttribute("portletIdentifier", randomPortletIdentifier);
		renderRequest.setAttribute("requestTime", System.currentTimeMillis()-startTime);
		super.doView(renderRequest, renderResponse);
	}

	private Map<String, Map<String, Double>> getStocksMatrix() {
		Map<String,Map<String,Double>>result= new HashMap<String,Map<String,Double>>();
		System.out.print("getting StocksMatrix");
		try {
			List<Object> present= (List<Object>) Await.result( FuturesAndActorsUtil.flatt( 
						Patterns.ask(myActor, MyUntypedActor.GET_EXCHANGES, timeout)
				),timeout.duration());

			Iterator<Object> iterator = present.iterator();
			while(iterator.hasNext()){
				Tuple3<String, String, Double> e = (Tuple3<String, String, Double>)iterator.next();
				System.out.print(String.format("getStocksMatrix(\"%s\",\"%s\",\"%s\")", e._1(),e._2(),e._3()));
				if(!result.containsKey((String)e._1())){
					result.put((String)e._1(), new HashMap<String,Double>());
				}
				if(!result.containsKey((String)e._2())){
					result.put((String)e._2(), new HashMap<String,Double>());
				}
				result.get(e._2()).put(e._1(), e._3());
				result.get(e._1()).put(e._2(), 1/e._3());
				result.get(e._1()).put(e._1(), 1.0);
				result.get(e._2()).put(e._2(), 1.0);
			}
		} catch (Exception e) {
				result.put("Error", new HashMap<String,Double>());
			System.out.println("Error: getting stocks matrix"+e.getMessage());
		}
		return result;

	}

	private java.util.List<Object> getStocksActors(){
		java.util.List<Object> result=new ArrayList<Object>();
		try { 
			result= (java.util.List<Object>) Await.result(Patterns.ask(myActor, MyUntypedActor.GET_EXCHANGERS, timeout),timeout.duration());
		} catch (Exception e) { e.printStackTrace(); }
		return result;
	}
	
}
