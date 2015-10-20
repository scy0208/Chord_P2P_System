package main.scala

import java.math.BigInteger

/**
 * Created by chunyangshen on 10/17/15.
 */
class Range(leftInclude:Boolean,leftValue:BigInt,rightValue:BigInt, rightInclude:Boolean){
  def isInclude(value:BigInt): Boolean = {

    if(leftValue==rightValue){
      if(leftInclude==false&&rightInclude==false&&value==leftValue) {
        return false
      }
      else{
        return true
      }
    }
    else if(leftValue<rightValue){
      if((value==leftValue&&leftInclude==true)||(value==rightValue&&rightInclude==true)||(value>leftValue&&value<rightValue)){
        return true
      }else{
        return false
      }
    }
    else{
      if((value==leftValue&&leftInclude==true)||(value==rightValue&&rightInclude==true)||(value>leftValue||value<rightValue)){
        return true
      }else{
        return false
      }
    }


  }

  def getEnd(): BigInt ={
    return rightValue
  }

}
