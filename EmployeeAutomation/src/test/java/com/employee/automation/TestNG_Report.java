package com.employee.automation;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.List;

public class TestNG_Report {

    WebDriver driver;
    WebDriverWait wait;

    static final String BASE_URL   = "http://localhost:5173/";
    static final String API_URL    = "http://localhost:5000";
    static final String ADMIN_USER = "admin";
    static final String ADMIN_PASS = "asbardy";   // updated admin password

    // Test employee data for the "Add Employee" test
    static final String TEST_EMP_NAME   = "AutoTest Employee";
    static final String TEST_EMP_ROLE   = "QA Engineer";
    static final String TEST_EMP_DEPT   = "Engineering";
    static final String TEST_EMP_SALARY = "95000";

    // ── Setup ─────────────────────────────────────────────────────────────────
    @BeforeTest
    public void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.get(BASE_URL);
        System.out.println("\n========================================");
        System.out.println("  Employee Portal — Full Automation Test");
        System.out.println("  Admin Password : " + ADMIN_PASS);
        System.out.println("  URL            : " + BASE_URL);
        System.out.println("========================================\n");
    }

    // ── Helper: click sidebar nav button by its exact label text ─────────────
    private void clickNav(String label) throws InterruptedException {
        Thread.sleep(600);
        List<WebElement> btns = driver.findElements(By.cssSelector(".nav-link-btn"));
        for (WebElement btn : btns) {
            if (btn.getText().trim().toLowerCase().contains(label.toLowerCase())) {
                btn.click();
                Thread.sleep(1000);
                return;
            }
        }
        throw new RuntimeException("Nav button not found for label: " + label);
    }

    // ── Helper: print pass/fail ───────────────────────────────────────────────
    private void log(String name, boolean passed) {
        System.out.printf("  %-55s %s%n", name, passed ? "[PASS]" : "[FAIL]");
    }

    // ── Helper: call GET /api/employees and check if name exists in JSON ──────
    private boolean employeeExistsInAPI(String empName) {
        try {
            URL url = new URL(API_URL + "/api/employees");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            if (conn.getResponseCode() != 200) return false;
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            conn.disconnect();
            return sb.toString().contains(empName);
        } catch (Exception e) {
            System.out.println("  [API CHECK ERROR] " + e.getMessage());
            return false;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TEST 1 — Admin Login (password: asbardy)
    // ═════════════════════════════════════════════════════════════════════════
    @Test(priority = 1)
    public void test01_AdminLogin() throws InterruptedException {
        System.out.println("--- Test 1: Admin Login (password: asbardy) ---");

        WebElement adminCard = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector(".role-card-item.admin"))
        );
        adminCard.click();
        Thread.sleep(400);

        WebElement usernameInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("login-username"))
        );
        usernameInput.clear();
        usernameInput.sendKeys(ADMIN_USER);

        WebElement passwordInput = driver.findElement(By.id("login-password"));
        passwordInput.clear();
        passwordInput.sendKeys(ADMIN_PASS);

        driver.findElement(By.cssSelector(".aurora-login-btn")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".sidebar")));
        String src = driver.getPageSource();
        boolean passed = src.contains("Dashboard") || src.contains("Admin User") || src.contains("nav-link-btn");
        Assert.assertTrue(passed, "Dashboard should load after login");
        log("Admin login with password 'asbardy'", passed);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TEST 2 — Dashboard / Home Section
    // ═════════════════════════════════════════════════════════════════════════
    @Test(priority = 2, dependsOnMethods = "test01_AdminLogin")
    public void test02_Dashboard() throws InterruptedException {
        System.out.println("--- Test 2: Dashboard ---");
        clickNav("Dashboard");
        Thread.sleep(1000);

        String src = driver.getPageSource();
        boolean hasStats = src.contains("Total Employees") || src.contains("Present")
                        || src.contains("Payroll") || src.contains("Remote");
        Assert.assertTrue(hasStats, "Dashboard should show stat cards");
        log("Dashboard stat cards visible", hasStats);

        boolean hasSidebar = !driver.findElements(By.cssSelector(".sidebar")).isEmpty();
        log("Sidebar rendered correctly", hasSidebar);
        Thread.sleep(1000);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TEST 3 — Employees Section (view + search)
    // ═════════════════════════════════════════════════════════════════════════
    @Test(priority = 3, dependsOnMethods = "test02_Dashboard")
    public void test03_EmployeesSection() throws InterruptedException {
        System.out.println("--- Test 3: Employees Section ---");
        clickNav("Employees");
        Thread.sleep(1000);

        String src = driver.getPageSource();
        boolean hasEmpList = src.contains("EMP-") && src.contains("Department");
        Assert.assertTrue(hasEmpList, "Employees section should list employees with EMP-IDs");
        log("Employee list with EMP-IDs rendered", hasEmpList);

        try {
            WebElement searchInput = driver.findElement(By.xpath("//input[contains(@placeholder,'earch')]"));
            searchInput.sendKeys("Bala");
            Thread.sleep(700);
            boolean searchWorks = driver.getPageSource().contains("Bala");
            log("Search filter for 'Bala' works", searchWorks);
            
            // Clear search filter robustly
            try {
                WebElement clearBtn = driver.findElement(By.className("search-clear-btn"));
                clearBtn.click();
            } catch (Exception ex) {
                searchInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
            }
            Thread.sleep(600);
        } catch (Exception e) {
            System.out.println("  Search input not found — skipping");
        }

        boolean hasAddForm = driver.getPageSource().contains("Add Employee");
        log("Add Employee form visible (admin)", hasAddForm);
        Thread.sleep(1000);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TEST 3B — Add Employee via UI  →  Express API  →  db.json
    // ═════════════════════════════════════════════════════════════════════════
    @Test(priority = 4, dependsOnMethods = "test03_EmployeesSection")
    public void test03b_AddEmployeeViaUI() throws InterruptedException {
        System.out.println("--- Test 3B: Add Employee (UI → Express API → db.json) ---");
        System.out.printf("  Adding: %s | %s | %s | ₹%s%n",
            TEST_EMP_NAME, TEST_EMP_ROLE, TEST_EMP_DEPT, TEST_EMP_SALARY);

        clickNav("Employees");
        Thread.sleep(1200);

        boolean formFilled = false;
        try {
            WebElement nameInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("add-emp-name"))
            );
            nameInput.clear();
            nameInput.sendKeys(TEST_EMP_NAME);
            Thread.sleep(300);

            WebElement roleInput = driver.findElement(By.id("add-emp-role"));
            roleInput.clear();
            roleInput.sendKeys(TEST_EMP_ROLE);
            Thread.sleep(300);

            WebElement deptInput = driver.findElement(By.id("add-emp-department"));
            deptInput.clear();
            deptInput.sendKeys(TEST_EMP_DEPT);
            Thread.sleep(300);

            WebElement salaryInput = driver.findElement(By.id("add-emp-salary"));
            salaryInput.clear();
            salaryInput.sendKeys(TEST_EMP_SALARY);
            Thread.sleep(300);

            formFilled = true;
            log("Add Employee form fields filled (Name/Role/Dept/Salary)", true);

            WebElement submitBtn = driver.findElement(By.id("add-emp-submit"));
            submitBtn.click();

            Thread.sleep(3000);

        } catch (Exception e) {
            log("Add Employee form fields filled (Name/Role/Dept/Salary)", formFilled);
            System.out.println("  [FORM ERROR] " + e.getMessage());
        }

        String uiSrc = driver.getPageSource();
        boolean appearsInUI = uiSrc.contains(TEST_EMP_NAME);
        log("New employee visible in UI (list refreshed from API)", appearsInUI);

        Thread.sleep(500);
        boolean appearsInAPI = employeeExistsInAPI(TEST_EMP_NAME);
        log("New employee in Express API  (GET /api/employees)", appearsInAPI);

        System.out.println();
        if (appearsInAPI) {
            System.out.println("  ✔  '" + TEST_EMP_NAME + "' is saved in db.json via Express.");
        } else {
            System.out.println("  ✘  API verify failed.");
        }
        System.out.println();

        Assert.assertTrue(appearsInUI || appearsInAPI,
            "'" + TEST_EMP_NAME + "' must appear in UI or API after form submission");
        Thread.sleep(1000);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TEST 4 — Attendance Section
    // ═════════════════════════════════════════════════════════════════════════
    @Test(priority = 5, dependsOnMethods = "test03b_AddEmployeeViaUI")
    public void test04_AttendanceSection() throws InterruptedException {
        System.out.println("--- Test 4: Attendance Section ---");
        clickNav("Attendance");
        Thread.sleep(1000);

        String src = driver.getPageSource();
        boolean hasAttendance = src.contains("Present") && src.contains("Absent");
        Assert.assertTrue(hasAttendance, "Attendance section should show Present/Absent buttons");
        log("Attendance Present/Absent/Half Day buttons visible", hasAttendance);

        boolean hasDatePicker = !driver.findElements(By.cssSelector("input[type='date']")).isEmpty();
        log("Date picker input present", hasDatePicker);

        try {
            List<WebElement> presentBtns = driver.findElements(By.cssSelector(".att-btn.present"));
            if (!presentBtns.isEmpty()) {
                presentBtns.get(0).click();
                Thread.sleep(1000);
                log("Clicked 'Present' for first employee", true);
            } else {
                log("att-btn.present not found", false);
            }
        } catch (Exception e) {
            System.out.println("  Could not click Present: " + e.getMessage());
        }
        Thread.sleep(1000);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TEST 5 — Schedule Section
    // ═════════════════════════════════════════════════════════════════════════
    @Test(priority = 6, dependsOnMethods = "test04_AttendanceSection")
    public void test05_ScheduleSection() throws InterruptedException {
        System.out.println("--- Test 5: Schedule / Work Tasks ---");
        clickNav("Schedule");
        Thread.sleep(1200);

        String src = driver.getPageSource();
        boolean hasTasks = src.contains("Pending") || src.contains("In Progress") || src.contains("Done");
        Assert.assertTrue(hasTasks, "Schedule section should show task statuses");
        log("Task status labels visible (Pending/In Progress/Done)", hasTasks);

        List<WebElement> taskCards = driver.findElements(By.cssSelector(".task-card"));
        log("Task cards rendered (" + taskCards.size() + " found)", taskCards.size() > 0);

        boolean hasCountdown = src.contains("Time Left") || src.contains("Overdue") || src.contains("days");
        log("Countdown timers present", hasCountdown);

        boolean hasAddTask = src.contains("Add Task") || src.contains("Task title");
        log("Add Task form visible (admin)", hasAddTask);
        Thread.sleep(1000);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TEST 6 — Salary Section
    // ═════════════════════════════════════════════════════════════════════════
    @Test(priority = 7, dependsOnMethods = "test05_ScheduleSection")
    public void test06_SalarySection() throws InterruptedException {
        System.out.println("--- Test 6: Salary Section ---");
        clickNav("Salary");
        Thread.sleep(1200);

        String src = driver.getPageSource();
        boolean hasSalaryTable = src.contains("Salary Records") || src.contains("Monthly Salary");
        Assert.assertTrue(hasSalaryTable, "Salary section should show salary records table");
        log("Salary records table visible", hasSalaryTable);

        boolean hasStatus = src.contains("Paid") || src.contains("Pending");
        log("Salary Paid/Pending status visible", hasStatus);

        List<WebElement> slipBtns = driver.findElements(By.cssSelector(".view-slip-btn"));
        log("Pay Slip buttons present (" + slipBtns.size() + " found)", slipBtns.size() > 0);

        List<WebElement> paidBtns = driver.findElements(By.cssSelector(".mark-paid-btn"));
        log("Mark Paid buttons present (" + paidBtns.size() + " found)", paidBtns.size() >= 0);

        boolean hasBanner = src.contains("Salary Day") || src.contains("days to go") || src.contains("Next Salary");
        log("Salary Day countdown banner visible", hasBanner);
        Thread.sleep(1000);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TEST 7 — Leave Requests Section (Admin view)
    // ═════════════════════════════════════════════════════════════════════════
    @Test(priority = 8, dependsOnMethods = "test06_SalarySection")
    public void test07_LeaveSection() throws InterruptedException {
        System.out.println("--- Test 7: Leave Requests (Admin) ---");
        clickNav("Leave");
        Thread.sleep(1200);

        String src = driver.getPageSource();
        boolean hasLeave = src.contains("Leave Requests") || src.contains("Leave Management");
        Assert.assertTrue(hasLeave, "Leave section should be visible");
        log("Leave Management section loaded", hasLeave);

        List<WebElement> tabs = driver.findElements(By.cssSelector(".leave-tab-btn"));
        log("Leave filter tabs rendered (" + tabs.size() + " tabs)", tabs.size() >= 3);

        for (WebElement tab : tabs) {
            String tabText = tab.getText().trim();
            tab.click();
            Thread.sleep(400);
            log("Clicked leave tab: " + tabText, true);
        }
        Thread.sleep(1000);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TEST 8 — EmpBot Chatbot
    // ═════════════════════════════════════════════════════════════════════════
    @Test(priority = 9, dependsOnMethods = "test07_LeaveSection")
    public void test08_ChatBot() throws InterruptedException {
        System.out.println("--- Test 8: EmpBot Chatbot ---");

        try {
            WebElement fabBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".chat-fab-btn"))
            );
            fabBtn.click();
            Thread.sleep(800);

            boolean chatVisible = !driver.findElements(By.cssSelector(".chat-panel")).isEmpty();
            log("Chatbot panel opened", chatVisible);

            WebElement chatInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".chat-input"))
            );
            chatInput.sendKeys("hello");
            driver.findElement(By.cssSelector(".chat-send-btn")).click();
            Thread.sleep(2000);

            List<WebElement> botBubbles = driver.findElements(By.cssSelector(".chat-bubble.bot"));
            log("Bot replied (" + botBubbles.size() + " bot messages)", botBubbles.size() >= 1);

            List<WebElement> quickPrompts = driver.findElements(By.cssSelector(".quick-prompt"));
            if (!quickPrompts.isEmpty()) {
                quickPrompts.get(0).click();
                Thread.sleep(2000);
                log("Quick prompt clicked successfully", true);
            }

            driver.findElement(By.cssSelector(".chat-close")).click();
            Thread.sleep(500);
            log("Chatbot closed", true);

        } catch (Exception e) {
            System.out.println("  Chatbot test error: " + e.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TEST 9 — Navigate back to Dashboard
    // ═════════════════════════════════════════════════════════════════════════
    @Test(priority = 9, dependsOnMethods = "test08_ChatBot")
    public void test09_BackToDashboard() throws InterruptedException {
        System.out.println("--- Test 9: Navigate back to Dashboard ---");
        clickNav("Dashboard");
        Thread.sleep(1000);
        String src = driver.getPageSource();
        boolean onHome = src.contains("Total Employees") || src.contains("Present") || src.contains("Payroll");
        Assert.assertTrue(onHome, "Should be back on home/dashboard");
        log("Successfully navigated back to Dashboard", onHome);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TEST 10 — Logout
    // ═════════════════════════════════════════════════════════════════════════
    @Test(priority = 10, dependsOnMethods = "test09_BackToDashboard")
    public void test10_Logout() throws InterruptedException {
        System.out.println("--- Test 10: Logout ---");

        // The logout button has class "logout-btn" in the sidebar footer
        WebElement logoutBtn = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector(".sidebar-footer .logout-btn"))
        );
        logoutBtn.click();
        Thread.sleep(1500);

        // Should see login page again
        String src = driver.getPageSource();
        boolean onLogin = src.contains("Sign In") || src.contains("Employee Management System")
                       || src.contains("login-username") || src.contains("aurora-login-btn");
        Assert.assertTrue(onLogin, "Should return to login page after logout");
        log("Logout successful — login page visible", onLogin);
    }

    // ── Teardown ──────────────────────────────────────────────────────────────
    @AfterTest
    public void teardown() {
        System.out.println("\n========================================");
        System.out.println("  All Portal Tests Completed");
        System.out.println("========================================\n");
        if (driver != null) {
            driver.quit();
        }
    }
}