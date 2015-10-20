package main.scala

import akka.actor.{ActorRef, Props, ActorSystem}
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask


/**
 * Created by chunyangshen on 10/18/15.
 */
object Chord_Main extends App{
  val system=ActorSystem("system")
  val node0=system.actorOf(Props(classOf[Chord_Node],null), name = "1")

  //Thread.sleep(10)
  //val node2=system.actorOf(Props(classOf[Chord_Node],node1), name = "2")
  val node1=system.actorOf(Props(classOf[Chord_Node],null), name = "6")
  //val node5=system.actorOf(Props(classOf[Chord_Node],node1), name = "5")

  //Thread.sleep(10)

  val node3=system.actorOf(Props(classOf[Chord_Node],null), name = "5")


  //Thread.sleep(1000)
  node0!Join(node1)
  //Thread.sleep(1000)
  node3!Join(node0)
  Thread.sleep(1000)
  node0!Print

  Thread.sleep(1000)
  node1!Print
  //node5!Print
  Thread.sleep(1000)
  node3!Print

/*
  implicit val timeout = Timeout(2 seconds)
  val future1 = node1?Get_Successor
  val result1=Await.result(future1, timeout.duration).asInstanceOf[ActorRef]
  println(result1.toString())


  val future2 = node3?Get_Successor
  val result2=Await.result(future2, timeout.duration).asInstanceOf[ActorRef]
  println(result2.toString())


  val future3 = node3?Get_Successor
  val result3=Await.result(future3, timeout.duration).asInstanceOf[ActorRef]
  println(result3.toString())

  var future4 = node4?Get_Successor
  var result4=Await.result(future4, timeout.duration).asInstanceOf[ActorRef]
  println(result4.toString())
*/
  //system.shutdown
}
