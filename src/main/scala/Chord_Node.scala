package main.scala

import java.util.logging.Logger

import akka.actor._
import akka.actor.Actor.Receive
import com.roundeights.hasher.Implicits._

/**
 * Created by chunyangshen on 10/20/15.
 */
class Chord_Node extends Actor{



  val logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)

  var exist:ActorRef=null
  var successor=self
  var predecessor=self
  var sucHash=getHash
  var preHash=getHash
  val m=160
  var fingerTable = new Array[Finger](m)

  object RemoteAddressExtension extends ExtensionKey[RemoteAddressExtensionImpl]

  val remoteAddr = RemoteAddressExtension(context.system).address
  val remotePath: String = self.path.toStringWithAddress(remoteAddr)

  for(i <-0 until m) {
    val start=(getHash()+BigInt(2).pow(i))%(BigInt(2).pow(m))
    val end=(getHash()+BigInt(2).pow(i+1))%(BigInt(2).pow(m))
    val range= new Range(true, start,end, false)
    fingerTable(i)= new Finger(start,range,self,getHash)
  }

  //logger.info("node %s is created".format(self))

  def getHash():BigInt ={


    return BigInt.apply(remotePath.sha1.hex,16)
    //return (self.toString.charAt(25)-48)
  }

  def getHash(code:String):BigInt ={
     return BigInt.apply(code.sha1.hex,16)
    //return (code.charAt(25)-48)
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


  def init_fingers():Unit = {
    fingerTable(0).setNode(successor)
    fingerTable(0).setHash(sucHash)
    for(i<-0 until m-1){
      val range=new Range(true,getHash(),fingerTable(i).getHash(),true)
      if(range.isInclude(fingerTable(i+1).getStart())) {
       //logger.info("Node %s, finger %s: %s is in the range [%s,%s),so its finger need to be change".format(getHash(),i+1,fingerTable(i+1).getStart(),getHash(),fingerTable(i).getHash()))
        fingerTable(i+1).setNode(fingerTable(i).getNode())
        fingerTable(i+1).setHash(fingerTable(i).getHash())
      }
      else{
        //logger.info("Node %s, finger %s: %s is not in the range [%s,%s)".format(getHash(),i+1,fingerTable(i+1).getStart(),getHash(),fingerTable(i).getHash()))
        if(exist!=null){
          exist!Find_Finger(self,i+1,fingerTable(i+1).getStart())
        }
      }
    }
  }

  def update_others():Unit = {
    for(i <- 0 to m-1) {
      val position=(getHash()-BigInt(2).pow(i)+BigInt(2).pow(m)+1)%BigInt(2).pow(m)
      //logger.info("I am %s, I need node before %s to change it's %s th finger".format(getHash(),position,i))
      successor!Update_Finger(position,i,self,getHash())
    }
  }


  override def receive: Receive ={

    case Join(exist:ActorRef)=>{

      this.exist=exist
      //println("I am %S, I am asking %s to find my position".format(getHash,exist.toString().charAt(25)))
      exist!Find_Position(self,getHash)
    }

    case Found_Position(predecessor:ActorRef,successor:ActorRef,preHash:BigInt,sucHash:BigInt)=>{
      this.predecessor=predecessor
      this.successor=successor
      this.preHash=preHash
      this.sucHash=sucHash

      //logger.info("I am %s, I found my position (%s , %s)".format(getHash(),predecessor.toString().charAt(25),successor.toString().charAt(25)))
      predecessor!Set_Successor(self)//Add by myself
      successor!Set_Predecessor(self)
      init_fingers()
      update_others()
    }

    case Found_Finger(i:Int,successor:ActorRef,nodeHash:BigInt)=>{
      this.fingerTable(i).setNode(successor)
      this.fingerTable(i).setHash(nodeHash)
    }

    case Find_Position(node:ActorRef,nodeHash:BigInt)=>{
      //logger.info("A new node %s is joing in".format(exist.toString()))
      val range = new Range(false, getHash(), fingerTable(0).getHash(), true)
      if(range.isInclude(nodeHash)){
        //logger.info("I am %s, the predecessor of node %s, and my successor %s can be its successor".format(getHash(),nodeHash,successor))
        node!Found_Position(self,this.successor,getHash(),fingerTable(0).getHash())
      }else{

        val target=closest_preceding_finger(nodeHash)
        //logger.info("I am %s, successor is %s, I am not the successor of %s, I am asking %s to find".format(getHash(),successor,nodeHash,target.toString().charAt(25)))
        target!Find_Position(node,nodeHash)
      }
    }

    case Find_Finger(node:ActorRef,i:Int,start:BigInt)=>{
      val range = new Range(false, getHash(), fingerTable(0).getHash(), true)
      if(range.isInclude(start)){
        node!Found_Finger(i,fingerTable(0).getNode(),fingerTable(0).getHash())
      }else{
        val target=closest_preceding_finger(start)
        target!Find_Finger(node,i,start)
      }
    }

    case Find(node:ActorRef,code:String, step:Int)=>{
      def id=getHash(code)
      val range = new Range(false, getHash(), fingerTable(0).getHash(), true)
      if(range.isInclude(id)){
        node!Found(code,self,successor,getHash(),fingerTable(0).getHash(),step)
      }else{
        val target=closest_preceding_finger(id)
        target!Find(node,code,step+1)
      }
    }

    case Found(code:String,predecessor:ActorRef,successor:ActorRef,preHash:BigInt,sucHash:BigInt,step:Int)=>{

      println("found code %s, hash: %s on node %s , hash: %s, predecessor: %s, using %s steps".format(code,getHash(code),successor.path.toString,sucHash,preHash,step))
      val range=new Range(false,preHash,sucHash,true)
      if(range.isInclude(getHash(code)))
        println("The result is right!")
    }

    case Update_Finger(before:BigInt,i:Int,node:ActorRef,nodeHash:BigInt)=>{
      if(node!=self) {
        val range1 = new Range(false, getHash(), fingerTable(0).getHash(), true)
        if (range1.isInclude(before)) { //I am the node just before N-2^i
            val range2=new Range(false, getHash(), fingerTable(i).getHash(), false)
            if(range2.isInclude(nodeHash)){
              //logger.info("I am %s,successor %s the first node before %s, I need to change my %s th finger to %s".format(getHash(),fingerTable(0).getHash(),before,i,nodeHash))
              fingerTable(i).setNode(node)
              fingerTable(i).setHash(nodeHash)
              //logger.info("I am %s I also ask my predecessor %s to change its %s finger to %s".format(getHash(),predecessor.toString().charAt(25),i,nodeHash))
              predecessor!Update_Finger(getHash(),i,node,nodeHash)//just let my predecessor to check whether its finger at i need be changed
            }
        }else{
          val target=closest_preceding_finger(before)
          //logger.info("I am %s, I am not the first node before %s I am asking %s to update".format(getHash(),before,target.toString().charAt(25)))
          target!Update_Finger(before,i,node,nodeHash)
        }
      }
    }

    case Set_Predecessor(node:ActorRef)=>{
      //logger.info("I am %s, I need to set my predecessor to %s".format(getHash(),node.toString().charAt(25)))
      this.predecessor=node
    }

    case Set_Successor(node:ActorRef)=>{
      //logger.info("I am %s, I need to set my successor to %s".format(getHash(),node.toString().charAt(25)))
      this.successor=node
    }

    case Get_Predecessor=>{
      print("Predecessor: %s".format(predecessor.path.toStringWithAddress(remoteAddr)))
    }

    case Get_Successor=>{
      print("Successor: %s".format(successor.path.toStringWithAddress(remoteAddr)))
    }

    case Fall_Finger=>{
      for(i <-1 until m) {
        fingerTable(i).setNode(fingerTable(i-1).getNode())
      }
    }

    case Fall_Node=>{
      context.stop(self)

    }

    case Print =>{

      println("============================================")
      println("Node: %s".format(remotePath))
      println("Hash: %s".format(getHash()))
      println("Predecessor: %s".format(getHash(predecessor.path.toStringWithAddress(remoteAddr))))
      println("Successor: %s".format(getHash(successor.path.toStringWithAddress(remoteAddr))))
      println("Finger Table: ")
      for(i<- 0 until m) {
        println("   %d : ".format(i)+fingerTable(i).print)
      }
      println("============================================")
    }
  }
}

class RemoteAddressExtensionImpl(system: ExtendedActorSystem) extends Extension {
  def address = system.provider.getDefaultAddress
}
