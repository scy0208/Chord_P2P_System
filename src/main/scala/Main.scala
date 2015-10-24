package main.scala

import java.net.NetworkInterface

import akka.actor.{Props, ActorSystem,ActorRef}

import java.util.logging.Logger

import com.typesafe.config.ConfigFactory

import scala.concurrent.Await

//import scala.actors.ActorRef

import scala.concurrent.duration._
import akka.util.Timeout
import java.util.logging.FileHandler
import java.util.logging.SimpleFormatter
import scala.io.StdIn


/**
 * Created by chunyangshen on 10/20/15.
 */
object Main extends{


  def main(args: Array[String]): Unit = {

    var logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)
    logger.setUseParentHandlers(false);

    var fileTxt = new FileHandler("log.txt");
    var formatterTxt = new SimpleFormatter();
    fileTxt.setFormatter(formatterTxt);
    logger.addHandler(fileTxt);

    print("Actor System Name:")
    var asn = scala.io.StdIn.readLine()
    print("Actor Name:")
    var an = scala.io.StdIn.readLine()
    var p:Int = 0
    while(p == 0){
      print("Port Number:")
      try{
        p = scala.io.StdIn.readLine().toInt
      }
      catch{
        case e:Exception=>println("You should input a number as port number."); p = 0
      }
    }


    var net_ip:String = null
    val interfaces = NetworkInterface.getNetworkInterfaces
    while (interfaces.hasMoreElements) {
      val element = interfaces.nextElement
      //println(element.getDisplayName)
      if (element.getDisplayName.equalsIgnoreCase("en0")||element.getDisplayName.equalsIgnoreCase("wlan0")){
        //println(element.getDisplayName +":"+element.getInterfaceAddresses.get(1).toString())
        net_ip = ((element.getInterfaceAddresses.get(1).toString()).split("/"))(1)
        //println(net_ip)
      }
    }
    if(net_ip == null){
      net_ip = "127.0.0.1"
      logger.info("Didn't connect to the internet, using local ip address:127.0.0.1")
    }



    val system = ActorSystem(asn, ConfigFactory.load(ConfigFactory.parseString("""
    akka {
    loglevel = "INFO"
    log-config-on-start = "off"
    debug {
    receive = on
    }
    actor {
    provider = "akka.remote.RemoteActorRefProvider"
    }
    remote {
    enabled-transports = ["akka.remote.netty.tcp"]
    netty.tcp {
    hostname = """+net_ip +
      """
      port = """+p+
      """
    }
    log-sent-messages = on
    log-received-messages = on
   }
  }
      """)))

    val chord=system.actorOf(Props[Chord_Node],name=an)

    while(true) {
      print(">")
      val ln = scala.io.StdIn.readLine().toLowerCase().trim()
      if (ln.startsWith("join")) {
        var url = "akka.tcp://"
        print("Actor System Name:")
        url += scala.io.StdIn.readLine().trim()
        print("Host Name:")
        url += "@" + scala.io.StdIn.readLine().trim()
        print("Port Number:")
        url += ":" + scala.io.StdIn.readLine().trim()
        url += "/user/"
        print("Actor Name:")
        url += scala.io.StdIn.readLine().trim()
        implicit val timeout = Timeout(2 seconds)
        val exsitNode = Await.result(system.actorSelection(url).resolveOne(), timeout.duration)
        chord ! Join(exsitNode)

      }
      else if (ln.equals("print")) {
        chord ! Print
      }
      else if(ln.equals("predecessor")) {
        chord ! Get_Predecessor
      }
      else if(ln.equals("successor")) {
        chord ! Get_Successor
      }
      else if(ln.equals("find")) {

        print("Key:")
        val word = scala.io.StdIn.readLine().trim()
        chord ! Find(chord,word,0)
      }


    }

  }



}







