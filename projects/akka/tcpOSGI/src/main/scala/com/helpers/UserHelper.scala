package com.helpers

import java.io.PrintWriter
import java.util

import com.liferay.counter.kernel.service.CounterLocalServiceUtil

import scala.collection.JavaConverters._
import com.liferay.portal.kernel.dao.orm.QueryUtil
import com.liferay.portal.kernel.model.{Group, User}
import com.liferay.portal.kernel.service.{ServiceContext, UserLocalService}

/**
 * Created by carlos on 20/01/16.
 */
object UserHelper extends EntityHelper{

  override def toMap(cc: AnyRef): Map[String, Any] ={
    val user=cc.asInstanceOf[User]
    Map(
      "id"->user.getUserId,
      "email"->user.getEmailAddress,
      "username"->user.getScreenName
    )
  }

  def getAnyOneUser(implicit usersservice:UserLocalService)={
    usersservice.getUsers(QueryUtil.ALL_POS, QueryUtil.ALL_POS).get(0)
  }


  def getUser(params:Map[String,Any])(implicit usersservice:UserLocalService): Option[User]={
    List(
      ("id",(s:String)=>{usersservice.getUserById(java.lang.Long.parseLong(s))}),
      ("username",(s:String)=>{usersservice.getUserByScreenName(getAnyOneUser.getCompanyId,s)}),
      ("email",(s:String)=>{usersservice.getUserByEmailAddress(getAnyOneUser.getCompanyId,s)})
    ).map{case(k,f)=>((k,f),params.get(k))}.
      filter{case(x,None)=>false;case _=>true}.headOption match{
        case Some(((_,f:((String)=>User)),v:Some[String]))=>Some(f(v.get))
        case _ =>None
    }
  }


  def showUser(params:Map[String,Any])(implicit usersservice:UserLocalService, out: PrintWriter): Unit ={
    out.println(getUser(params).getOrElse("Not found").toString.replaceAll(", ","\n")) //TODO: improve format??
  }

  def listUsers(implicit usersservice:UserLocalService,out: PrintWriter): Unit ={
    val table: util.List[User] =usersservice.getUsers(QueryUtil.ALL_POS, QueryUtil.ALL_POS)
    //printTable(table.toArray.toSeq)
    printFilteredTable(table.toArray.toSeq,List("id","email","username"))
  }

  def countUsers(implicit usersservice:UserLocalService, out: PrintWriter): Unit ={
    val count=usersservice.getCompanyUsersCount(getAnyOneUser.getCompanyId)
    out.println(s"count=${count} users")
  }


  def deleteUser(params:Map[String,Any])(implicit usersservice:UserLocalService, out: PrintWriter): Unit ={
      val user: Option[User] =getUser(params)
      out.println(s"deleting ${user.getOrElse("nothing (user not found)")}")
      user.map{x=> usersservice.deleteUser(x.getUserId) }
  }


  def createUser(params:Map[String,Any])(implicit usersservice:UserLocalService, out: PrintWriter): Unit ={
    out.println(s"creating actual user ${params}")

    val gpId:Long = CounterLocalServiceUtil.increment( classOf[Group].getName )
    val companyId: Long =getAnyOneUser.getCompanyId

    val serviceContext = new ServiceContext()
    val roleIds: Array[Long] = Array()

    val myuser:User = usersservice.addUser(
      usersservice.getDefaultUser(companyId).getUserId(),
      companyId,
      false,
      params.getOrElse("password","test").asInstanceOf[String],
      params.getOrElse("password","test").asInstanceOf[String],
      false,
      params.getOrElse("username","test").asInstanceOf[String],
      params.getOrElse("email","").asInstanceOf[String],
      0,//TODO: dehardcode facebookId,
      "",//TODO: dehardcode  openId,
      serviceContext.getLocale(),
      params.getOrElse("firstname","test").asInstanceOf[String],
      null,
      params.getOrElse("lastname","test").asInstanceOf[String],
      0,//TODO: dehardcode  prefixId,
      0,//TODO: dehardcode   suffixId,
      true,//TODO:dehardcode isMale
      1,//TODO:dehardcode bMonth,
      1,//TODO:dehardcode bDay,
      1970,//TODO:dehardcode bYear,
      "blogger",//TODO:dehardcode jobTitle
      Array[Long](),//TODO:dehardcode  groupIds,
      Array[Long](),//TODO:dehardcode  organizationIds,
      roleIds,
      Array[Long](),//TODO:dehardcode   userGroupIds,
      false,
      serviceContext
    )
    //val userId = myuser.getUserId()
  }
}
