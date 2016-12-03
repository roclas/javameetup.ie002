package com.carlos.utils;

import scala.concurrent.ExecutionContext.Implicits.global
import java.util.stream.Stream
import scala.concurrent.Future
import scala.concurrent.Promise
import collection.JavaConverters._
import akka.actor.ActorRef;




object FuturesAndActorsUtil{
 def stream2Future(stream:Stream[Object]): Future[List[Tuple3[String,String,Double]]]={
   Future.sequence(stream.iterator().asScala.toStream.toList.asInstanceOf[List[Future[Tuple3[String,String,Double]]]])
 }

 def flatt[D](f:Future[D]):Future[D]=f.mapTo[Promise[D]].flatMap(x=>x.future)
 
 def getParent(actorRef:ActorRef):String={
   val list=actorRef.path.toString.split("/")
   return "/"+list.slice(0, list.length-1).mkString("/")
 }
}
