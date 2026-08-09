Feature: Facebook Login Page

  Scenario: Successful login with username and password
    Given User opens the login page
    When user enters the username "testuser"
    And user enters the password "testpass"
    Then click the Login Button
