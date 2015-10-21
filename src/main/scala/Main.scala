package main.scala

import akka.actor.{Props, ActorSystem}

/**
 * Created by chunyangshen on 10/20/15.
 */
object Main extends App{
  val system=ActorSystem("system")
  val node0=system.actorOf(Props[Chord_Node], name = "0")

  //Thread.sleep(1000)
  val node2=system.actorOf(Props[Chord_Node], name = "3")
  val node1=system.actorOf(Props[Chord_Node], name = "2")
  val node4=system.actorOf(Props[Chord_Node], name = "7")

  //Thread.sleep(1000)

  val node3=system.actorOf(Props[Chord_Node], name = "6")


  Thread.sleep(100)
  node1!Join(node0)
  Thread.sleep(100)
  node3!Join(node0)
  Thread.sleep(100)
  node2!Join(node0)
  Thread.sleep(100)
  node4!Join(node0)



  Thread.sleep(1000)

  node0!Print
  Thread.sleep(100)
  node1!Print
  Thread.sleep(100)
  node2!Print
  Thread.sleep(100)
  node3!Print
  Thread.sleep(100)
  node4!Print

  Thread.sleep(1000)

  node4!Find(node4,"55555555555555555555555555555555",0)

  Thread.sleep(1000)

  system.shutdown()
}
