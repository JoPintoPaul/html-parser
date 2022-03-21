import java.io.{BufferedWriter, File, FileWriter}
import scala.io.Source
import scala.util.matching.Regex

object HtmlParser {
  def main(args: Array[String]): Unit = {
    val messagesPath = ""
    val htmlPath = ""
    val templatePath = ""

    val inputLines: Seq[String] = getLines(htmlPath)
    val parsedLines: Seq[ParsedLine] = parseLines(inputLines)

    val templateLines: Seq[String] = parsedLines.map(_.templateLine)
    writeLinesToFile(templatePath, templateLines)

    val messageLines: Seq[String] = parsedLines.flatMap(_.message.map(_.toString))
    writeLinesToFile(messagesPath, messageLines)
  }

  private def getLines(path: String): List[String] = {
    val inputSource = Source.fromFile(path)
    val inputLines = inputSource.getLines.toList.filterNot(_.isEmpty)
    inputSource.close()
    inputLines
  }

  private def parseLines(inputLines: Seq[String]): Seq[ParsedLine] = {
    val paragraphRegex: Regex = "(<p>)(.*)(</p>)".r
    inputLines map {
      case paragraph@paragraphRegex(_, innerText, _) =>
        extractMessage(innerText) match {
          case Some(message) =>
            ParsedLine("<p class=\"govuk-body\">" + message.toHtml() + "<p>", Some(message))
          case _ => ParsedLine(paragraph, None)
        }
      case line => ParsedLine(line, None)
    }
  }

  private def extractMessage(innerText: String): Option[Message] = {
    val indexRegex = "[1-9]+(\\.)[1-9]+".r
    indexRegex.findFirstIn(innerText) match {
      case Some(index) =>
        val messagesKey = s"terms.${index.replace(".", "_")}"
        Some(Message(messagesKey, innerText))
      case None => None
    }
  }

  private def writeLinesToFile(path: String, lines: Seq[String]) = {
    val outputFile = new File(path)
    val bufferedWriter = new BufferedWriter(new FileWriter(outputFile))
    for (line <- lines) {
      bufferedWriter.write(line + "\n")
    }
    bufferedWriter.close()
  }

  case class ParsedLine(templateLine: String, message: Option[Message])
  case class Message(key: String, value: String) {
    override def toString: String = s"$key=$value"

    def toHtml(): String = s"@messages(\"${key}\")"
  }
}
