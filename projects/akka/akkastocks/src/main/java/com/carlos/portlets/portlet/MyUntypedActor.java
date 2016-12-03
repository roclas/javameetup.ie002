package com.carlos.portlets.portlet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.carlos.utils.FuturesAndActorsUtil;
import com.carlos.utils.FuturesAndActorsUtil$;

import akka.actor.ActorRef;
import akka.actor.Kill;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.Tuple2;
import scala.Tuple3;
import scala.concurrent.Future;

public class MyUntypedActor extends UntypedActor {

		public static final String GET_EXCHANGES = "getExchanges";
		public static final String GET_EXCHANGERS = "getExchangers";
		public static final String KILL_EXCHANGERS_CHILDREN = "killExchangersChildren";
		public static final String KILL_EXCHANGERS = "killExchangers";
		public static final String KILL_ACTORS= "killActors";
		public static final String RESET = "reset";
		public static final String DISCOVER = "discover";
		public static final String HELLO = "hello";
		public static final String PING= "ping";
		public static final String PONG= "pong";
		public static final String FORWARD= "forward";
		public static final String KILL= "kill";
		public static final String EXCHAGE= "exchange";

		Timeout timeout=AkkastocksmvcportletPortlet.timeout;
		  FuturesAndActorsUtil$ futureUtil= FuturesAndActorsUtil$.MODULE$;
		  private List<ActorRef>stockActors= new ArrayList<ActorRef>();

		  public static MyUntypedActor create() { return new MyUntypedActor(); }

		  private void removeActor(String actorRef) { 
			  int[] indices=IntStream.range(0,stockActors.size()).filter(i -> (stockActors.get(i).path().toString().equals(actorRef))).toArray();
			  System.out.println("removing "+indices.length+" from stockActors");
			  for(int i:indices)stockActors.remove(i);
		  }
		  

		  public void onReceive(Object message) throws Exception {
			  System.out.println(String.format("actor %s received %s",self(),message));
			  if ( message instanceof String){
				  switch((String)message){
				  case GET_EXCHANGERS :
					  sender().tell(stockActors, self()); 
					  break;
				  case KILL_ACTORS:
					  getContext().actorSelection("/user/*").tell(Kill.getInstance(), self());
					  stockActors=new ArrayList<ActorRef>();
				  case KILL_EXCHANGERS:
					  for(ActorRef actor:stockActors)actor.tell(Kill.getInstance(), null);
					  stockActors=new ArrayList<ActorRef>();
					  break;
				  case HELLO:
					  stockActors.add(sender()); 
					  break;
				  case DISCOVER:
					  stockActors=new ArrayList<ActorRef>();
					  getContext().actorSelection("/user/*").tell(PING, self());
					  break;
				  case RESET:
					  stockActors=new ArrayList<ActorRef>();
					  break;
				  case GET_EXCHANGES:
					  Stream<Object> streamOfFutures= stockActors.stream().map(a->Patterns.ask(a,EXCHAGE,timeout));
					  Future<scala.collection.immutable.List<Tuple3<String, String, Object>>> future= FuturesAndActorsUtil.stream2Future(streamOfFutures);
					  sender().tell(future,null);
					  break;
				  default:break;
				  }
			  } else if ( message instanceof Tuple2 ){
				  Tuple2<String, Object> m=(Tuple2<String,Object>)message;
				  if (m._1().equals(HELLO)){ 
					  if(m._2() instanceof ActorRef)
						  stockActors.add((ActorRef)m._2()); 
				  }
				  else if (m._1().equals(KILL)){ 
					  if(m._2() instanceof ActorRef){
						  removeActor((String)m._2());
						  ((ActorRef)m._2()).tell(Kill.getInstance(), null);
					  }else if(m._2() instanceof String[]){
						    for(String actorSelection:(String [])m._2()){
						    	removeActor((String)actorSelection);
						    	getContext().actorSelection(actorSelection).tell(Kill.getInstance(), self());
						    }
					  }
				  }
			  } else if ( message instanceof Tuple3 ){
				  Tuple3<String, String, Object> m=(Tuple3<String,String,Object>)message;
				  if (m._1().equals(FORWARD)) getContext().actorSelection(m._2()).tell(m._3(), self()); 
				  else if (m._1().equals(PONG))stockActors.add((ActorRef)m._3());
			  }
		  }
	}