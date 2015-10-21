package main.scala

import akka.actor.ActorRef

/**
 * Created by chunyangshen on 10/20/15.
 */
class Finger(start: BigInt, range: Range, var node:ActorRef){
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
    //return BigInt.apply(node.toString().sha1.hex,16)
    (node.toString.charAt(25)-48).toInt
  }
  def setNode(newNode:ActorRef):Unit ={
    this.node=newNode
  }

  def print:String={
    return ("Start: %s, End: %s, Node: %s".format(start,range.getEnd,getHash()))
  }


}; //interval: [start,end)
