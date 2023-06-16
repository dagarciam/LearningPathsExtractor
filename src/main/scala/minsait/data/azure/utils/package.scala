package minsait.data.azure

import minsait.data.azure.constants.Constants.UrlAzureExams
import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.hssf.usermodel.HSSFPalette
import org.apache.poi.ss.usermodel.{BorderStyle, CellStyle, CellType, CreationHelper, FillPatternType, Font}
import org.apache.poi.xssf.usermodel.{DefaultIndexedColorMap, XSSFCell, XSSFColor, XSSFFont, XSSFHyperlink, XSSFSheet, XSSFWorkbook}
import org.apache.poi.ss.usermodel.Font.{U_NONE, U_SINGLE}
import org.apache.poi.ss.util.{CellRangeAddress, WorkbookUtil}
import org.checkerframework.checker.units.qual.A

import java.awt.Color
import java.io.{FileOutputStream, OutputStream}
import scala.language.implicitConversions
import scala.util.{Failure, Try}

package object utils {

  private case class RGB(red: Short, green: Short, blue: Short)

  def getUrlLearningPaths(cert: String) = s"$UrlAzureExams/$cert"

  private def getCellStyle(fontName: String = "Calibri",
                           underline: Byte = U_NONE,
                           fontSize: Short = 11,
                           bold: Boolean = false,
                           fill: Boolean = false,
                           bgColor: RGB = RGB(255, 255, 255),
                           fontColor: RGB = RGB(0, 0, 0),
                           fmt: String = ""
                          )
                          (implicit workbook: XSSFWorkbook): CellStyle = {
    val indexColorMap: DefaultIndexedColorMap = new DefaultIndexedColorMap()

    val customFillColor: XSSFColor = new XSSFColor(new Color(bgColor.red, bgColor.green, bgColor.blue), indexColorMap)
    val customFontColor: XSSFColor = new XSSFColor(new Color(fontColor.red, fontColor.green, fontColor.blue), indexColorMap)

    val font: XSSFFont = workbook.createFont()
    font.setFontHeightInPoints(fontSize)
    font.setFontName(fontName)
    font.setUnderline(underline)
    font.setBold(bold)
    font.setColor(customFontColor)

    val style: CellStyle = workbook.createCellStyle
    if (fill) {
      style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
      style.setFillForegroundColor(customFillColor)
    }
    style.setFont(font)

    if (fmt != "") {
      Try {
        val dataFormat = workbook.createDataFormat().getFormat(fmt)
        style.setDataFormat(dataFormat)
      }
    }

    style
  }

  private def getLink(href: String)(implicit workbook: XSSFWorkbook): XSSFHyperlink = {
    val link: XSSFHyperlink = workbook.getCreationHelper
      .createHyperlink(HyperlinkType.URL)
    link.setAddress(href)

    link
  }

  def write(learningPaths: Seq[LearningPath], cert: String): Unit = {
    implicit val workbook: XSSFWorkbook = new XSSFWorkbook

    val learningPathStyle: CellStyle = getCellStyle(underline = U_SINGLE, bold = true, fill = true, bgColor = RGB(217, 225, 242))
    val moduleStyle: CellStyle = getCellStyle(underline = U_SINGLE, bold = true, fill = true, bgColor = RGB(221, 235, 247))
    val unitStyle: CellStyle = getCellStyle(underline = U_SINGLE, fontColor = RGB(5, 99, 193))
    val meanLearningPathStyle: CellStyle = getCellStyle(bold = true, fill = true, bgColor = RGB(217, 225, 242), fmt = "00.00%")
    val meanModuleStyle: CellStyle = getCellStyle(bold = true, fill = true, bgColor = RGB(221, 235, 247), fmt = "00.00%")

    val sheet: XSSFSheet = workbook.createSheet(s"Temario $cert")
    sheet.createRow(0).createCell(3).setCellValue("Example")

    var row: Int = 1
    for (learningPath <- learningPaths) {
      var moduleRows: List[String] = Nil

      val cell: XSSFCell = sheet.createRow(row).createCell(0)

      cell.setHyperlink(getLink(learningPath.href))
      cell.setCellValue(s"${learningPath.title} (${learningPath.time})")
      cell.setCellStyle(learningPathStyle)
      val meanLearningPathCell: XSSFCell = sheet.getRow(row).createCell(3)
      meanLearningPathCell.setCellStyle(meanLearningPathStyle)
      sheet.addMergedRegion(new CellRangeAddress(row, row, 0, 2))
      row = row + 1
      val tabBlankLearningPath = new CellRangeAddress(row, row, 0, 0)
      for (module <- learningPath.modules) {
        var unitRows: List[String] = Nil
        moduleRows = moduleRows :+ s"D${row + 1}"
        val cell: XSSFCell = sheet.createRow(row).createCell(1)
        cell.setCellValue(s"${module.title} (${module.time})")
        cell.setHyperlink(getLink(module.href))
        cell.setCellStyle(moduleStyle)
        sheet.addMergedRegion(new CellRangeAddress(row, row, 1, 2))
        val meanModuleCell: XSSFCell = sheet.getRow(row).createCell(3)
        meanModuleCell.setCellStyle(meanModuleStyle)
        row = row + 1
        val tabBlankModule = new CellRangeAddress(row, row, 1, 1)

        for (unit <- module.units) {
          unitRows = unitRows :+ s"D${row + 1}"
          val cell: XSSFCell = sheet.createRow(row).createCell(2)
          cell.setCellValue(s"${unit.title} (${unit.time})")
          cell.setHyperlink(getLink(unit.href))
          cell.setCellStyle(unitStyle)

          sheet.getRow(row)
            .createCell(3)
            .setCellValue(0.0)

          tabBlankModule.setLastRow(row)
          tabBlankLearningPath.setLastRow(row)
          row = row + 1
        }

        meanModuleCell.setCellFormula(s"(${unitRows.mkString("+")})/${unitRows.size}")
        sheet.addMergedRegion(tabBlankModule)
      }

      meanLearningPathCell.setCellFormula(s"(${moduleRows.mkString("+")})/${moduleRows.size}")
      sheet.addMergedRegion(tabBlankLearningPath)
    }
    sheet.autoSizeColumn(2)
    //sheet.setColumnHidden(3, true)

    Try {
      val fileOut: OutputStream = new FileOutputStream(s"Temario $cert.xlsx")
      workbook.write(fileOut)
    }
  }

}
