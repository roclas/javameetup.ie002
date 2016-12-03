/**
  * Created by carlos on 09/09/16.
  */
import com.osgi.Activator

import collection.mutable.Stack
import org.scalatest._

class Misc extends FlatSpec with Matchers {

  val activator=new Activator

  "An Activator" should "not be able to parse the command halp" in {
    val matches=activator.parse(activator.pLiffey,"halp")
    matches.successful should not be true
  }

  val commands=List(
    "help"
    ,"usage"
    ,"list users"
    ,"user list"
    ,"create users -username=carlos1 -password=mysecret -email=mail@gmail.com"
    ,"delete user"
    ,"delete user -email=mail@gmail.com"
  )
  commands.foreach{c=>
    it should s"be able to parse the command $c" in {
      val matches=activator.parse(activator.pLiffey,c)
      matches.successful shouldEqual true
    }
  }

  "A stack" should "throw NoSuchElementException if an empty stack is popped" in {
    val emptyStack = new Stack[Int]
    a [NoSuchElementException] should be thrownBy {
      emptyStack.pop()
    }
  }

}
