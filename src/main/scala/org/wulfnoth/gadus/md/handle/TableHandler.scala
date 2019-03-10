package org.wulfnoth.gadus.md.handle

import org.apache.commons.lang3.StringUtils

object TableHandler {

	def transform(elements: Array[String]): String = {
		if (elements.length > 1) {
			val sb = new StringBuilder
			sb.append("<p><table>\n")
			val format = elements(1).trim.split("\\|").map {
				case ":-" => "left"
				case ":-:" => "center"
				case "-:" => "right"
				case _ => "error"
			}
			if (format.contains("error")) {
				elements.mkString("\n")
			}

			sb.append("<tr>\n")
			var cells = elements(0).trim.split("\\|")

//			elements(1).split("\\|") foreach println

			for (i <- cells.indices) {
				if (!cells(i).isEmpty)
					sb.append("<th align=\"").append(format(i)).append("\">").append(cells(i)).append("</th>\n")
			}

			sb.append("</tr>\n")

			for (line <- elements.slice(2, elements.length)) {
				sb.append("<tr>\n")
				cells = line.trim.split("\\|")
				for (i <- cells.indices) {
					if (!cells(i).isEmpty)
						sb.append("<td align=\"").append(format(i)).append("\">").append(cells(i)).append("</td>\n")
				}
				sb.append("</tr>\n")
			}
			sb.append("</table></p>").toString()
		} else {
			elements.mkString("\n")
		}
	}

	def handle(content: String): String = {

		val l = Seq(1, 2, 3)


		if (StringUtils.isEmpty(content))
			return ""

		val lines = content.split("\n")
		val sb = new StringBuilder()
		var startLine = -1
		var inTable = false

		for (i <- lines.indices) {
			val line = lines(i).trim
			if (line.startsWith("|"))
				println("")
			if (line.startsWith("|") && line.endsWith("|") && !inTable) {
				startLine = i
				inTable = true
			} else if (inTable && (!line.startsWith("|") || !line.endsWith("|"))) {
				sb.append(transform(lines.slice(startLine, i))).append("\n")
				sb.append(line)
				inTable = false
			} else if (!inTable) {
				sb.append(line).append("\n")
			}
		}

		if (inTable)
			sb.append(transform(lines.slice(startLine, lines.length))).append("\n")

		return sb.substring(0, sb.length-1)
	}

//	def main(args: Array[String]): Unit = {
//		val t = """|$t$|$R_(t)$|$R(T_t)$|$g(t)$|
//		   |:-|:-:|:-:|:-|
//		   |$t_1$|$\cfrac{8}{16} \cdot \cfrac{16}{16}$|$T_{t_1}$ - the entire tree all leaves are pure $R(T_{t_1}) = 0$|$\cfrac{8/16 - 0}{4 - 1} = \cfrac{1}{6}$|
//		   |$t_2$|$\cfrac{4}{12} \cdot \cfrac{12}{16} = \cfrac{4}{16}$(there are 12 records, 4 $\blacksquare$ + 8 $\bigcirc$ )|$R(T_{t_2}) = 0$|$\cfrac{4/16 - 0}{3 - 1} = \cfrac{1}{8}$|
//		   |$t_3$|$\cfrac{2}{6} \cdot \cfrac{6}{16} = \cfrac{2}{16}$|$R(T_{t_3}) = 0$|$\cfrac{2/16 - 0}{3 - 1} = \cfrac{1}{8}$|"""
//		println(handle(handle(t)))
//	}

}