package minsait.data.azure

import minsait.data.azure.constants.Constants.*
import minsait.data.azure.utils.*
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.{By, WebDriver, WebElement}

import java.time.Duration
import scala.jdk.CollectionConverters.*
import scala.util.Try

class Engine(driver: WebDriver, cert: String = "dp-203") {
  val url: String = getUrlLearningPaths(cert)
  println(url)
  driver.get(url)
  Thread.sleep(3000)
  Try {
    driver.findElement(By.xpath("//span[@class='show-more-text']")).click()
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
          .findElement(By.id("time-remaining"))
          .getText
          .replaceAll(Restantes, EmptyString)

        val modules: Seq[Module] = getModules.map(addThemes)

        LearningPath(learningPath.title, learningPath.href, time, modules)

      }).toSeq
  }

  private def getModules: Seq[Module] = {

    driver
      .findElements(By.xpath("//div[@data-bi-name='module']"))
      .asScala
      .map(module => {

        val title = module.findElement(By.tagName(H3Tag)).getText
        val href = module.findElement(By.tagName(ATag)).getAttribute(HrefTag)

        val time = module
          .findElement(By.className("module-time-remaining"))
          .getText.replaceAll(Restantes, EmptyString)

        Module(title, href, time)
      }).toSeq
  }

  private def addThemes(module: Module): Module = {
    driver.get(module.href)
    Thread.sleep(200)
    val units: Seq[LearningUnit] = driver
      .findElement(By.id("unit-list"))
      .findElements(By.tagName("li"))
      .asScala
      .map(unit => {
        val title = unit.findElement(By.tagName("a")).getText
        val href = unit.findElement(By.tagName("a")).getAttribute(HrefTag)
        val time = unit.findElement(By.tagName("span")).getText

        LearningUnit(title, href, time)
      }).toSeq

    Module(module.title, module.href, module.time, units)
  }

  def apply(): Unit = {
    write(getLearningPaths, cert)
  }

}
