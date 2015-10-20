package main.scala

/**
 * Created by chunyangshen on 10/15/15.
 */

import akka.actor.{Actor,ActorRef}

case object Get_Predecessor

case object Get_Successor

case class Find(id:BigInt,step:Int)

case class Found(predecessor:ActorRef,successor: ActorRef,id:BigInt,step:Int)//the answer message of Find

case class Update_Fingertable(node:ActorRef, nodeHash:BigInt,i:Int)

case class Set_Predecessor(node:ActorRef)

case class Set_Successor(node:ActorRef)

case class Join(exist:ActorRef)

case object Print