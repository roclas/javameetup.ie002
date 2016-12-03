package com.osgi

import org.osgi.framework.{BundleActivator, BundleContext, ServiceReference}
import java.io.{BufferedReader, IOException, InputStreamReader, PrintWriter}
import java.net.{ServerSocket, Socket}

import com.helpers.UserHelper
import com.liferay.portal.kernel.model.{Company, User}
import com.liferay.portal.kernel.service.{CompanyLocalServiceUtil, UserLocalService, UserLocalServiceUtil}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConversions._
import scala.concurrent.Future
import akka.actor._
import akka.osgi.{ActorSystemActivator, OsgiActorSystemFactory}
import com.typesafe.config.ConfigFactory
import org.osgi.service.component.annotations.Reference
import org.osgi.util.tracker.ServiceTracker

import scala.concurrent.duration.Duration
import scala.concurrent.Promise

case class CreateUser(options:Map[String,Any])
case class DeleteUser(options:Map[String,Any])
case class ShowUser(options:Map[String,Any])
case class ListUsers()
case class HelpCommand()

class Activator  extends ActorSystemActivator with BundleActivator  with Parser {


	def configure(context: BundleContext, system: ActorSystem) {
		println(s"\n\nmy actor system name is called ${system.name} \n\n ")
    // optionally register the ActorSystem in the OSGi Service Registry
    //registerService(context,system)
    context.registerService(system.getClass.getName,system,null)
		println(s"Configuring Akka")
  }

	override def start(context: BundleContext){
    super.start(context)
		implicit val cont=context
		println("Starting server")
		Future{runServer}
	}

	override def stop(context: BundleContext): Unit ={
		println("Stopping server")
	}

	private val portNumber=4321
	private val keepRunning=true

	def usage=s"""
							 			|
							 			|Usage examples:
							 			|	${usageExamples.foldLeft("")((s,acc)=>s"$s$acc \r\t")}
							 			|	<there will be more implemented soon>
		""".stripMargin

	//TODO: add schedule jobs usage example??
	val usageExamples=List(
		"help",
		"user(s) create -username=<my_desired_username> -password=<my_password> -email=<my_email>",
		"create user -username=<my_desired_username> -password=<my_password> -email=<my_email>",
		"user delete -username=<my_desired_username>",
		"delete users -email=<my_email>",
		"users list",
		"list users # order doesn't matter here",
		"user count"
		//"download <http://mysite.com/myjar.jar>"
	)




	def runServer (implicit cont:BundleContext)={
    val references=cont.getAllServiceReferences("akka.actor.ActorSystemImpl",null)
    println(s"found = ${references.size} references to akka.actor.ActorSystemImpl")
		//val mySystem = OsgiActorSystemFactory(cont, getActorSystemConfiguration(cont)).createActorSystem("osgiActorSystem")
		val mySystem:ActorSystem=cont.getService(references(0)).asInstanceOf[ActorSystem]
    println(s"\n\n\n\nactor system name = ${mySystem.name}\n\n\n\n")

		val helloActor = mySystem.actorOf(Props[HelloActor], name = "helloActor")
		helloActor ! "hello"
		val serverSocket = new ServerSocket(portNumber)
		val clientSocket = serverSocket.accept()
		implicit val out = new PrintWriter(clientSocket.getOutputStream(), true)
		out.print("_________________________________\nWelcome to the FAKE gogo shell!!\n\n\nf! ")
		out.flush()
		val in = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()))
		var line: String =in.readLine()
		val uservice: UserLocalService =UserLocalServiceUtil.getService
		while ( line != null && keepRunning) {
			if(line.nonEmpty){
				parse(pLiffey, line.mkString) match {
					case Success(matched,_) => {
						implicit val usersservice:UserLocalService=uservice
						matched match{
							case CreateUser(opts)=> UserHelper.createUser(opts)
							case DeleteUser(opts)=> UserHelper.deleteUser(opts)
							case ShowUser(opts)=> UserHelper.showUser(opts)
							case ListUsers=> UserHelper.listUsers
							case HelpCommand=> out.println(s"HELP COMMAND\n$usage")
							case _=> out.println( s"$matched UNIMPLEMENTED OPTION (please report the bug)" )
						}
					}
					case _=> out.println( s"UNRECOGNIZED COMMAND: $line" )
				}
			}
			out.print("f! ")
			out.flush()
			line=in.readLine()

		}
		serverSocket.close()
		clientSocket.close()
	}

	def findUser(email: String,out: PrintWriter): Unit={
		try{
			for(c<- CompanyLocalServiceUtil.getCompanies()){
				val u= UserLocalServiceUtil.getUserByEmailAddress( c.getCompanyId() , email)
				out.println(s"${u.getFirstName} - ${u.getCompanyId}")
			}
		}catch{ case e:Throwable => out.println("") }
	}

}

class HelloActor extends Actor {
  def receive = {
    case "hello world" =>
			sender ! "hello my friend"
			println("hello back at you")
    case _       => println("huh?")
  }
}

