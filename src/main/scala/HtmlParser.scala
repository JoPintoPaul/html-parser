import java.io.{BufferedWriter, File, FileWriter}
import scala.io.Source
import scala.util.matching.Regex

object HtmlParser {
  def main(args: Array[String]): Unit = {
    val htmlPath = "page-source-en.txt"
    val htmlPathCy = "page-source-cy.txt"

    val messagesPath = "messages"
    val messagesPathCy = "messages-cy"

    val templatePath = "OnlineTermsPage.txt"

    val inputLines: Seq[String] = getLines(htmlPath)
    val parsedLines: Seq[ParsedLine] = parseLines(inputLines)

    val templateLines: Seq[String] = parsedLines.map(_.templateLine)
    writeLinesToFile(templatePath, templateLines)

    val messageLines: Seq[String] = parsedLines.flatMap(_.message.map(_.toString))
    writeLinesToFile(messagesPath, messageLines)

    val inputLinesCy: Seq[String] = getLines(htmlPathCy)
    val parsedLinesCy: Seq[ParsedLine] = parseLines(inputLinesCy)
    val messageLinesCy = parsedLinesCy.flatMap(_.message.map(_.toString))
    writeLinesToFile(messagesPathCy, messageLinesCy)
  }

  private def getLines(path: String): List[String] = {
    val inputSource = Source.fromFile(path)
    val inputLines = inputSource.getLines.toList.filterNot(_.isEmpty)
    inputSource.close()
    inputLines
  }

  private def parseLines(inputLines: Seq[String]): Seq[ParsedLine] = {
    val paragraphRegex: Regex = "(<p>)(.*)(</p>)".r
    val headingLargeRegex: Regex = "<h3 id=\"(.*)\">(.*)</h3>".r
    val headingMediumRegex: Regex = "<h4(.*)?>(.*)</h4>".r

    inputLines map {
      case paragraph@paragraphRegex(_, innerText, _) =>
        extractParagraphMessage(innerText) match {
          case Some(message) =>
            ParsedLine("<p class=\"govuk-body\">" + message.toHtml() + "<p>", Some(message))
          case _ => ParsedLine(paragraph, None)
        }
      case headingLarge@headingLargeRegex(id, innerText) =>
        extractHeaderMessage(innerText, Some(id)) match {
          case Some(message) =>
            ParsedLine(s"<h3 id=\"$id\" class=\"govuk-heading-l\">" + message.toHtml() + "<h3>", Some(message))
          case _ => ParsedLine(headingLarge, None)
        }

      case headingMedium@headingMediumRegex(id, innerText) =>
        extractHeaderMessage(innerText, None) match {
          case Some(message) =>
            ParsedLine(s"<h4${id} class=\"govuk-heading-m\">" + message.toHtml() + "<h4>", Some(message))
          case _ => ParsedLine(headingMedium, None)
        }
      case line => ParsedLine(line, None)
    }
  }

  private def extractParagraphMessage(innerText: String): Option[Message] = {
    val indexRegex = "[1-9]+(\\.)[1-9]+".r
    indexRegex.findFirstIn(innerText) match {
      case Some(index) =>
        val messagesKey = s"terms.${index.replace(".", "_")}"
        Some(Message(messagesKey, innerText))
      case None => None
    }
  }

  private def extractHeaderMessage(innerText: String, id: Option[String]): Option[Message] = {
    val messagesKey = id match {
      case Some(i) => s"heading.$i"
      case None => s"heading.${innerText.toLowerCase.replace(" ", "-").replace(",", "")}"
    }
    Some(Message(messagesKey, innerText))
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

    def toHtml(): String = s"@messages(\"$key\")"
  }
}
