package com.osgi

/**
  * Created by carlos on 12/09/16.
  */

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers


trait Parser extends RegexParsers{

	override val skipWhitespace = false

	def pAnything = """.*""".r ^^ { _.toString }
	def pEnd= """\s*$""".r ^^ { _.toString }
	def blank= """\s+""".r ^^ { _.toString }

	//def pLiffey = (pHelp |pUser | pRole | pVersion |pDownload | pReplace | pScheduler) <~ pEnd
	def pLiffey = (pHelp |pUser ) <~ pEnd

	def pHelp=  """(help|usage)""".r ^^ { _=> HelpCommand }
	def pUser=  s"(list\\s+${user}|${user}\\s+list)".r ^^ { _=> ListUsers}|
		s"(create\\s+${user}|${user}\\s+create)".r ~>pUserOpts ^^ { CreateUser(_)}|
		((user~>blank~>pDelete)|(pDelete~>blank~>user)) ~> pUserDeleteOpts ^^ { DeleteUser(_) }
	//((user~>pShow)|(pShow~>user)) ~> pUserShowOpts ^^ { ShowUser(_) }

	val user: Regex = """users?""".r
	private val pCreate: String = "create"
	private val pUpdate: String = "update"
	private val pDelete: String = "delete"
	private val pShow: String = "show"
	private val pList: String = "list"

	val emptyStr: String =""
	val anyChars: String ="""\S+"""

	val password: String = "password"
	val passwordOpt: String = "-password="

	val roles: String = "roles"
	val rolesOpt: String = "-roles="

	val email: String = "email"
	val emailOpt: String = "-email="

	val username: String = "username"
	val usernameOpt: String = "-username="

	val lastname: String = "lastname"
	val lastnameOpt: String = "-lastname="

	val firstname: String = "firstname"
	val firstnameOpt: String = "-firstname="

	val id: String = "id"
	val idOpt: String = "-id="

	def pPassword=(passwordOpt+anyChars).r ^^{x=>{val r=x.toString replaceFirst (passwordOpt,emptyStr);((password,r))}}
	def pId =(idOpt+anyChars).r ^^{x=>{val r=x.toString replaceFirst (idOpt,emptyStr); ((id,r))}}
	def pUserName =(usernameOpt+anyChars).r ^^{x=>{val r=x.toString replaceFirst(usernameOpt,emptyStr);((username,r))}}
	def pFirstName=(firstnameOpt+anyChars).r ^^{x=>{val r=x.toString replaceFirst(firstnameOpt,emptyStr);((firstname,r))}}
	def pLastName=(lastnameOpt+anyChars).r ^^{x=>{val r=x.toString replaceFirst(lastnameOpt,emptyStr);((lastname,r))}}
	def pEmail=(emailOpt+anyChars).r ^^{x=>{val r=x.toString replaceFirst(emailOpt,emptyStr);((email,r))}}
	def pRoles: Parser[Tuple2[String,Any]]=(rolesOpt + anyChars).r ^^ {x=>{val r=x.toString replaceFirst(rolesOpt,emptyStr);((roles,r))}}

	def pUserOpt: Parser[(String, Any)] =(blank ^^ {_.toString})~>(pPassword|pUserName|pFirstName|pLastName|pEmail|pRoles)
	def pUserOpts:Parser[Map[String,Any]]= pUserOpt.* ^^ { _.toMap }
	def pUserShowOpt: Parser[(String, Any)] =(blank ^^ {_.toString})~>(pUserName|pId|pEmail)
	def pUserShowOpts:Parser[Map[String,Any]]= pUserShowOpt.* ^^ { _.toMap }
	def pUserDeleteOpts=pUserShowOpts

	def messageCommand=(currencyValueAssignment | currency) <~ end
	def currencyValueAssignment: Parser[(String,Float)] = currency ~ blank ~floatValue ^^ {case c~b~v =>(c,v)}
	def currency: Parser[String] = """[A-Z]{3}""".r ^^ { _.toString }
	def floatValue: Parser[Float] = """([0-9]*[.])?[0-9]+""".r ^^ { _.toFloat}
	def end= """\s*$""".r ^^ { _.toString }


}
