package simplecli

import com.thoughtworks.paranamer.BytecodeReadingParanamer
import java.lang.{Object => JObject}
import java.lang.reflect.{Method => JMethod, Type => JType}
import scala.collection.mutable

// Support of command line application
trait CliSupport {
  
  // Program entry point of simplecli
  def main(args: Array[String]): Unit = {
    val entry = getEntryPoint()
    val invokeArgs = adaptArgs(parseRawArgs(args), parseCliArgs(entry))
    val runtimeArgs = invokeArgs.map(_._2).toArray
    entry.invoke(this, runtimeArgs: _*)
  }

  // Throw when value not found
  class ValueNotFoundException(msg: String) extends RuntimeException(msg)

  // Throw when cannot convert string to specified type
  class UnsupportedTypeConvertException(msg: String) extends RuntimeException(msg)

  // Adapt arguments
  private def adaptArgs(rawArgs: Map[String, String], cliArgs: List[(String, JType)]): List[(String, AnyRef)] = {
    
    // get value by key
    def getValue(key: String): String = rawArgs.get(key) match {
      case Some(value) => value
      case _ => throw new ValueNotFoundException("value [" + key + "] not found")
    }

    // class literals
    val classInt = classOf[Int]
    val classString = classOf[String]

    // try convert string to specified type
    def tryConvert(key: String, t: JType): AnyRef = t match {
      case `classInt` => getValue(key).toInt.asInstanceOf[AnyRef]
      case `classString` => getValue(key) 
      case _ => throw new UnsupportedTypeConvertException("unable to convert string to [" + t + "]")
    }

    cliArgs.map{case (k, v) => (k, tryConvert(k, v))}
  }

  // Parse command line arguments of entry point
  private def parseCliArgs(entry: JMethod): List[(String, JType)] = {
    val names = new BytecodeReadingParanamer().lookupParameterNames(entry, false).toList
    val types = entry.getGenericParameterTypes
    (names, types).zipped.toList
  }

  // Throw when entry point not found
  class EntryPointNotFoundException(msg: String) extends RuntimeException(msg)

  // Find user defined entry point
  private def getEntryPoint(): JMethod = getClass.getMethods.filter{m =>
    // user defined entry point must named "main"
    // and don't use Array[String] as parameters
    val paramTypes = m.getParameterTypes
    m.getName == "main" && (paramTypes.length != 1 || paramTypes(0) != classOf[Array[String]])
  } match {
    case ms if ms.size == 1 => ms(0)
    case _ => throw new EntryPointNotFoundException("entry point not found or multiply entry point found")
  }

  // Throw when unexpected parameter count. e.g odd count
  class UnexpectedParameterCountException(msg: String) extends RuntimeException(msg: String)

  // Parse raw arguments to formated parameter pairs
  private def parseRawArgs(args: Array[String]): Map[String, String] = {
    val length = args.size
    if(length % 2 != 0) throw new UnexpectedParameterCountException("expect even of parameter count")
    val map = mutable.Map[String, String]()
    for(i <- (0 to (length - 1) by 2)) map(args(i).substring(2)) = args(i + 1)
    map.toMap
  }
}
