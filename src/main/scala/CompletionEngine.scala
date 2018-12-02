package org.scalavista

import com.typesafe.scalalogging.LazyLogging

import scala.reflect.internal.util.{Position, _}
import scala.tools.nsc.Settings
import scala.tools.nsc.interactive.Global
import scala.tools.nsc.reporters.StoreReporter

object ScalaCompletionEngine {

  trait Dummy

  def apply(): ScalaCompletionEngine = {

    val settings = new Settings()

    settings.embeddedDefaults[Dummy] // why is this needed?
    settings.usejavacp.value = true // what does this do exactly?

    val reporter = new StoreReporter()

    new ScalaCompletionEngine(settings, reporter)

  }

}

class ScalaCompletionEngine(settings: Settings, reporter: StoreReporter)
    extends Global(settings, reporter)
    with LazyLogging {

  def reloadFiles(files: List[SourceFile]): Unit = {

    val reloadResponse = new Response[Unit]
    askReload(files, reloadResponse)
    getResult(reloadResponse)
    logger.info(
      s"unitsOfFile after reloadFile: ${unitOfFile.map(f => f._1.toString).mkString("\n")}"
    )

  }

  def getErrors: List[(String, String, String, String)] = {

    //logger.info(s"getErrors: ${reporter.infos.mkString("\n")}")
    reporter.infos
      .map(
        info =>
          (
            info.pos.source.path.toString,
            info.pos.line.toString,
            info.msg,
            info.severity.toString
        )
      )
      .toList

  }

  def getTypeAt(pos: Position): String = {

    logger.info(Position.formatMessage(pos, "Getting type at position:", false))
    val askTypeAtResponse = new Response[Tree]
    askTypeAt(pos, askTypeAtResponse)
    val result = getResult(askTypeAtResponse) match {
      case Some(tree) =>
        logger.info(
          s"tree: ${ask(() => tree.toString)}\n raw tree: ${ask(() => showRaw(tree))}"
        )
        ask(() => tree.symbol.tpe.toLongString)
      // if (tree.isType) {
      //   ask(() => tree.toString)
      // } else {
      //   tree match {
      //     case ValDef(_, _, tpt, _)       => ask(() => tpt.toString)
      //     case DefDef(_, _, _, _, tpt, _) => ask(() => tpt.toString)
      //     case ModuleDef(_, name, _)      => ask(() => name.toString)
      //     case _                          => "?"
      //   }
      // }
      case None => "Failed to get type."
    }
    logger.info(s"getTypeAt: $result")
    result

  }

  def getPosAt(pos: Position): Position= {
    logger.info(
      Position.formatMessage(pos, "Getting position of symbol at :", false)
    )
    val askTypeAtResponse = new Response[Tree]
    askTypeAt(pos, askTypeAtResponse)
    val symbol = getResult(askTypeAtResponse) match {
      case Some(tree) => ask(() => tree.symbol)
      case None => throw new RuntimeException("failed to get position")
    }
    logger.info(s"looking for definition of ${symbol.fullNameString}")
    val positions = 
    for (u <- unitOfFile) yield {
      logger.info(s"looking at file ${u._1.toString}")
      val response = new Response[Position]
      askLinkPos(symbol, u._2.source, response)
      val position = getResult(response) match {
        case Some(p) => p
        case _ => NoPosition
      }
      logger.info(s"${u._1.toString} -> ${position.source.toString}, ${position.line}, ${position.column}")
      position
    }
    positions.find(_ != NoPosition).getOrElse(NoPosition)
  }

  def getTypeCompletion(pos: Position): List[(String, String)] = {
    logger.info(
      Position.formatMessage(pos, "Getting type completion at position:", false)
    )
    val response = new Response[List[Member]]
    askTypeCompletion(pos, response)
    val result = getResult(response) match {
      case Some(ml) => ml
      case None     => Nil
    }
    val res = ask(
      () => result.map(member => (member.sym.nameString, member.infoString)))
    logger.info(res.mkString("\n"))
    res

  }

  def getScopeCompletion(pos: Position): List[(String, String)] = {
    logger.info(
      Position
        .formatMessage(pos, "Getting scope completion at position:", false)
    )
    val response = new Response[List[Member]]
    askScopeCompletion(pos, response)
    val result = getResult(response) match {
      case Some(ml) => ml
      case None     => Nil
    }
    val res = ask(
      () => result.map(member => (member.sym.nameString, member.infoString)))
    logger.info(res.mkString("\n"))
    res

  }

  private def getResult[T](res: Response[T]): Option[T] = {
    val TIMEOUT = 10000 // ms
    res.get(TIMEOUT.toLong) match {
      case Some(Left(t)) => Some(t)
      case Some(Right(ex)) =>
        ex.printStackTrace()
        println(ex)
        None
      case None =>
        None
    }
  }

}
