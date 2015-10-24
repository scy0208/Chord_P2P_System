package main.scala

import akka.actor.{ExtensionKey, ActorRef}

import com.roundeights.hasher.Implicits._



/**
 * Created by chunyangshen on 10/20/15.
 */
class Finger(start: BigInt, range: Range, var node:ActorRef, var nodehash:BigInt){



  def getStart(): BigInt = {
    return this.start
  }
  def getRange(): Range = {
    return this.range
  }
  def getNode(): ActorRef = {
    return this.node
  }
  def getHash():BigInt ={
    return nodehash
  }
  def setNode(newNode:ActorRef):Unit ={
    this.node=newNode
  }

  def setHash(nodeHash:BigInt): Unit ={
    this.nodehash=nodeHash
  }

  def print:String={
    return ("Start: %s, End: %s, Node: %s".format(start,range.getEnd,getHash()))
  }


}; //interval: [start,end)
