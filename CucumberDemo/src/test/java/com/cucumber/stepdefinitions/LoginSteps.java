package com.cucumber.stepdefinitions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.bonigarcia.wdm.WebDriverManager;

public class LoginSteps {

    WebDriver driver;

@Given("User opens the login page")
public void user_opens_the_login_page() {

 WebDriverManager.chromedriver().setup();
 driver = new ChromeDriver();
 driver.get("http://169.254.100.16:5173/"); 
}
@When("user enters the username {string}")
public void user_enters_the_username(String user) {

    driver.findElement(By.id("login-username")).sendKeys(user);
   
}
@When("user enters the password {string}")
public void user_enters_the_password(String pass) {
    driver.findElement(By.id("login-password")).sendKeys(pass);
   
}
@Then("click the Login Button")
public void click_the_login_button() {

   driver.findElement(By.className("aurora-login-btn")).click();
   driver.quit();
}

}
