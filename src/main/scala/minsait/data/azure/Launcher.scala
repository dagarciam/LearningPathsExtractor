package minsait.data.azure

import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.WebDriverWait

import scala.util.{Failure, Success, Try}

object Launcher {
  def main(args: Array[String]): Unit = {
    val driver = new ChromeDriver

    Try {
      for(arg <- args) {
        val engine: Engine = new Engine(driver, arg)
        engine()
      }
    } match {
      case Success(_) =>
      case Failure(exception) =>
        exception.printStackTrace()
    }

    driver.quit()
  }
}
