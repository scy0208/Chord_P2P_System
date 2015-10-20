package main.scala

//import akka.actor.Actor.Receive
import akka.actor.{Actor,ActorRef}
import akka.util.Timeout
import com.roundeights.hasher.Implicits._
import main.scala.Update_Fingertable
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask



/**
 * Created by chunyangshen on 10/15/15.
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

class Chord_Node( var exist: ActorRef) extends Actor{
  var successor=self
  var predecessor=self
  val m=3
  //val N=BigInt.apply(self.toString().sha1.hex,16)
  var fingerTable = new Array[Finger](m)
  implicit val timeout = Timeout(2 seconds)

  for(i <-0 until m) {
    val start=(getHash()+BigInt(2).pow(i))%(BigInt(2).pow(m))
    val end=(getHash()+BigInt(2).pow(i+1))%(BigInt(2).pow(m))
    val range= new Range(true, start,end, false)
    fingerTable(i)= new Finger(start,range,self)
  }

  if(exist!=null){
    init_self()
    update_others()


  }


  def getHash():BigInt ={
    //return BigInt.apply(self.toString().sha1.hex,16)
    return (self.toString.charAt(25)-48).toInt
  }


  def getHash(code:String):BigInt ={
   // return BigInt.apply(code.sha1.hex,16)
    return (code.charAt(25)-48).toInt
  }

  def init_self(): Unit = {
    val future = exist?Find(getHash(),0)
    val result=Await.result(future, timeout.duration).asInstanceOf[Found]
    result match {
      case Found(pred:ActorRef,succ:ActorRef,id:BigInt,step:Int)=>{
        //println("I am %s, I found my successor %s".format(getHash(),succ.toString().charAt(25)))
        //println("I am %s, I found pre %s, succ %s".format(getHash(),pred, succ))
        predecessor=pred
        successor=succ
      }
    }
    predecessor!Set_Successor(self)//this command is added by myself
    successor!Set_Predecessor(self)
    fingerTable(0).setNode(successor)
    for(i<-0 until m-1){
      val range=new Range(true,getHash(),fingerTable(i).getHash(),true)
      if(range.isInclude(fingerTable(i+1).getStart())) {
        fingerTable(i+1).setNode(fingerTable(i).getNode())
      }else{
        val future=exist?Find(fingerTable(i+1).getStart(),0)
        val result=Await.result(future, timeout.duration).asInstanceOf[Found]
        result match {
          case Found(pred:ActorRef,succ:ActorRef,id:BigInt,step:Int)=>{
            //println("I am %s, in iteration %s, I need %s, I found %s".format(getHash(),i,fingerTable(i).getHash(),succ))
            fingerTable(i+1).setNode(succ)
          }
        }
      }
    }
  }

  def update_others(): Unit = {
    for(i <- 0 until m) {
      //val position=getHash()-BigInt(2).pow(i)
      val hash=getHash()
      val position=(getHash()-BigInt(2).pow(i)+BigInt(2).pow(m)+1)%BigInt(2).pow(m)
      //println("I am %s I need my pre %s predecessor (before value %s) to change finger at %s".format(getHash(),i,position,i))
      val future=exist?Find((getHash()-BigInt(2).pow(i)+BigInt(2).pow(m)+1)%BigInt(2).pow(m),0)
      val result=Await.result(future, timeout.duration).asInstanceOf[Found]
      result match {
        case Found(pred:ActorRef,succ:ActorRef,id:BigInt,step:Int)=>{

          println("I am %s I need my pre %s predecessor (before value %s): %s to change finger at %s".format(getHash(),i,position,pred.toString().charAt(25),i))
          pred!Update_Fingertable(self,getHash(),i)
        }
      }
    }
  }


  def update_finger_table(node:ActorRef, nodeHash:BigInt,i:Int): Unit = {
    val range = new Range(false, getHash(),fingerTable(i).getHash(),false)
    if(range.isInclude(nodeHash)) {
      println("I am %s, I was asked to change my %s th finger (range: (%s,%s)) to %s".format(getHash(),i, getHash(),fingerTable(i).getHash(),nodeHash))
      fingerTable(i).setNode(node)
      predecessor!Update_Fingertable(node,nodeHash,i)

    }

  }


  def closest_preceding_finger(id:BigInt): ActorRef = {
    val range=new Range(false,getHash(),id,false)
    for(i <- m-1 to 0 by -1){
      if(range.isInclude(fingerTable(i).getHash())) {
        return fingerTable(i).node;
      }
    }
    return self;
  }

  override def receive: Receive ={
    case Get_Successor=>{
      sender!successor
    }
    case Get_Predecessor=>{
      sender!predecessor
    }
    case Find(id:BigInt, step:Int) =>{
      if(id!=getHash()) {
        val range = new Range(false, getHash(), fingerTable(0).getHash(), true)
        if (range.isInclude(id)) {
          sender ! Found(self, successor, id, step)
        } else {
          val target = closest_preceding_finger(id)
          if (target == self) {
            sender ! Found(self, successor, id, step)
          } else {
            println("I am %s, I am asking %s to find %s".format(getHash, target.toString().charAt(25),id))
            val future = target ? Find(id, step + 1)
            val result = Await.result(future, timeout.duration).asInstanceOf[Found]
            //println("I am %s, I found node: for id:  %s".format(getHash, id))
            sender ! result
          }
        }
      }else{
        sender !Found(predecessor,self,id,step)
      }
    }
    case Update_Fingertable(node:ActorRef, nodeHash:BigInt,i:Int) =>{
      update_finger_table(node,nodeHash,i)
    }
    case Set_Predecessor(node:ActorRef)=>{
      predecessor=node

      //println("I am %s".format(self.toString()))
      //println("now I receive node %s and my current predecessor is %s".format(node.toString(),predecessor.toString()))
      //println
    }
    case Set_Successor(node:ActorRef)=>{
      successor=node
    }
    case Join(node:ActorRef)=>{
      this.exist=node
      init_self()
      update_others()
    }
    case Print=>{
      println("============================================")
      println("Node: %s".format(self.toString()))
      println("Hash: %s".format(getHash()))
      println("Predecessor: %s".format(getHash(predecessor.toString())))
      println("Successor: %s".format(getHash(successor.toString())))
      println("Finger Table: ")
      for(i<- 0 until m) {
        println("   %d : ".format(i)+fingerTable(i).print)
      }
      println("============================================")
    }



  }
}
