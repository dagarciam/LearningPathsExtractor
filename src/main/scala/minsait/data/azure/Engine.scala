package minsait.data.azure

import minsait.data.azure.constants.Constants._
import minsait.data.azure.utils._
import org.openqa.selenium.{By, WebDriver}

import scala.jdk.CollectionConverters._
import scala.util.Try

class Engine(driver: WebDriver, cert: String = DP300) {
  val url: String = getUrlLearningPaths(cert)
  println(url)
  driver.get(url)
  Thread.sleep(3000)
  Try {
    driver.findElement(By.xpath(MoreTextXPathExpression)).click()
  }


  private def getLearningPaths: Seq[LearningPath] = {
    driver
      .findElement(By.id(LearningPathListId))
      .findElements(By.className(CardTittleClass))
      .asScala
      .map(card => {
        LearningPath(
          href = card.getAttribute(HrefTag),
          title = card.getText
        )
      })
      .map(learningPath => {
        driver.get(learningPath.href)
        Thread.sleep(300)
        val time: String = driver
          .findElement(By.id(TimeRemainingId))
          .getText
          .replaceAll(Restantes, EmptyString)

        val modules: Seq[Module] = getModules.map(addThemes)

        LearningPath(learningPath.title, learningPath.href, time, modules)

      }).toSeq
  }

  private def getModules: Seq[Module] = {

    driver
      .findElements(By.xpath(ModuleXPathExpression))
      .asScala
      .map(module => {

        val title = module.findElement(By.tagName(H3Tag)).getText
        val href = module.findElement(By.tagName(ATag)).getAttribute(HrefTag)

        val time = module
          .findElement(By.className(ModuleTimeRemainingClass))
          .getText.replaceAll(Restantes, EmptyString)

        Module(title, href, time)
      }).toSeq
  }

  private def addThemes(module: Module): Module = {
    driver.get(module.href)
    Thread.sleep(200)
    val units: Seq[LearningUnit] = driver
      .findElement(By.id(UnitListId))
      .findElements(By.tagName(LiTag))
      .asScala
      .map(unit => {
        val title = unit.findElement(By.tagName(ATag)).getText
        val href = unit.findElement(By.tagName(ATag)).getAttribute(HrefTag)
        val time = unit.findElement(By.tagName(SpanTag)).getText

        LearningUnit(title, href, time)
      }).toSeq

    Module(module.title, module.href, module.time, units)
  }

  def apply(): Unit = {
    write(getLearningPaths, cert)
  }

}
