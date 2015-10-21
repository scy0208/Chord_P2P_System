package main.scala

import akka.actor.ActorRef

/**
 * Created by chunyangshen on 10/20/15.
 */

case class Join(exist:ActorRef)

case class Find_Position(node:ActorRef,nodeHash:BigInt)

case class Found_Position(predecessor:ActorRef,successor:ActorRef)

case class Find_Finger(node:ActorRef,i:Int,start:BigInt)

case class Found_Finger(i:Int,successor:ActorRef)

case class Update_Finger(before:BigInt,i:Int,node:ActorRef,nodeHash:BigInt)

case class Find(node:ActorRef,code:String, step:Int)

case class Found(code: String,predecessor:ActorRef,successor:ActorRef,step:Int)

case class Set_Predecessor(node:ActorRef)

case class Set_Successor(node:ActorRef)

case object Print