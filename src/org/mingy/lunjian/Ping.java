package org.mingy.lunjian;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Ping {

	public static void main(String[] args) throws Exception {
		Properties properties = new Properties();
		if (args.length > 0) {
			properties.load(new InputStreamReader(new FileInputStream(args[0]),
					"utf-8"));
		} else {
			properties.load(CommandLine.class
					.getResourceAsStream("/lunjian.properties"));
		}
		WebDriver webdriver = null;
		String browser = properties.getProperty("webdriver.browser");
		if (browser == null || "firefox".equalsIgnoreCase(browser)) {
			System.setProperty("webdriver.firefox.bin",
					properties.getProperty("webdriver.firefox.bin"));
			webdriver = new FirefoxDriver();
		} else if ("chrome".equalsIgnoreCase(browser)) {
			System.setProperty("webdriver.chrome.driver",
					properties.getProperty("webdriver.chrome.driver"));
			webdriver = new ChromeDriver();
		}
		webdriver.manage().window().setSize(new Dimension(1052, 768));
		webdriver.navigate().to("http://web2.qq.com");
		webdriver.switchTo().defaultContent();
		Thread.sleep(120000);
		while (true) {
			Thread.sleep(25000);
			WebElement e = webdriver.findElement(By.id("chat_textarea"));
			e.clear();
			e.sendKeys("1");
			webdriver.findElement(By.id("send_chat_btn")).click();
		}
	}
}
