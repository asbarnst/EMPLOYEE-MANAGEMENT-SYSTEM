package com.cucumber.runner;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "com.cucumber.stepdefinitions",
    plugin = {"pretty", "html:target/cucumber-report.html"}
)
public class TestRunner {

}
