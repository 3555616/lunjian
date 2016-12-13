package org.mingy.lunjian;

import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Robot {

	private WebDriver webdriver;

	public static void main(String[] args) throws Exception {
		Robot robot = new Robot();
		robot.kill(args[0]);
	}

	private void kill(String npc) throws Exception {
		webdriver = new FirefoxDriver();
		Properties properties = new Properties();
		properties.load(Robot.class.getResourceAsStream("/robot.properties"));
		System.setProperties(properties);
		webdriver.navigate().to(properties.getProperty("lunjian.url"));
		webdriver.switchTo().defaultContent();
		System.out.println("find: " + npc);
		findAndClick(npc);
		System.out.println("kill: " + npc);
		findAndClick("杀死");
		System.out.println("wait combat over");
		findAndClick(By.xpath("//a[img[@class='prev']]"));
		System.out.println("find: " + npc + "的尸体");
		findAndClick(npc + "的尸体");
		System.out.println("get corpse");
		findAndClick("搜索");
		System.out.println("robot complete");
	}

	private void findAndClick(By by) throws InterruptedException {
		for (;;) {
			try {
				webdriver.findElement(by).click();
				break;
			} catch (NoSuchElementException e) {
			}
			Thread.sleep(100);
		}
	}

	private void findAndClick(String name) throws InterruptedException {
		for (;;) {
			try {
				click(name);
				break;
			} catch (NoSuchElementException e) {
			}
			Thread.sleep(100);
		}
	}

	private void click(String name) {
		webdriver.findElement(
				By.xpath("//button[translate(normalize-space(text()),' ','')='"
						+ name + "']")).click();
	}
}
