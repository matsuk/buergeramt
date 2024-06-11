package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static org.example.TerminChecker.ElementsXPath.*;

public class TerminChecker {
    public static final List<String> USER_AGENTS = List.of(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 Edg/123.0.2420.81",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 14.4; rv:124.0) Gecko/20100101 Firefox/124.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_4_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4.1 Safari/605.1.15",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Linux i686; rv:124.0) Gecko/20100101 Firefox/124.0"
    );

    public static final String ERROR_MESSAGE = "Für die gewählte Dienstleistung sind aktuell keine Termine frei! Bitte \n" +
            "versuchen Sie es zu einem späteren Zeitpunkt erneut";


    static class ElementsXPath {
        public static final String BUTTON_BOOK = "//*[@id=\"mainForm\"]/div/div/div/div/div/div/div/div/div/div[1]/div[1]/div[2]/a";
        public static final String CHECKBOX_ACCEPT = "//*[@id=\"xi-cb-1\"]";
        public static final String BUTTON_NEXT = "//*[@id=\"applicationForm:managedForm:proceed\"]";
        public static final String COUNTRY = "//*[@id=\"xi-sel-400\"]";
        public static final String RUSSIA = "Russische Föderation";
        public static final String PEOPLE = "//*[@id=\"xi-sel-422\"]";
        public static final String EINE = "eine Person";
        public static final String CHILDREN = "//*[@id=\"xi-sel-427\"]";
        public static final String NEIN = "nein";
        public static final String CATEGORY = "//*[@id=\"xi-div-30\"]/div[3]";
        public static final String SUB_CATEGORY = "//*[@id=\"SERVICEWAHL_DE160-0-3-99-326798\"]";
        public static final String ERROR = "//*[@id=\"messagesBox\"]";
        public static final String REJECT_YT = "//*[@id=\"content\"]/div[2]/div[6]/div[1]/ytd-button-renderer[1]/yt-button-shape/button/yt-touch-feedback-shape/div/div[2]";
        public static final String HEADER = "//*[@id=\"header\"]";
        public static final String DATEPICKER = "//*[@id=\"xi-div-2\"]/div/div[1]/table";
        public static final String EXPIRED = "//h2[contains(text(),'Sitzungsende')]";
        public static final String SELECTABLE_DAY = "//td[@data-handler=\"selectDay\"]";
        public static final String TIME_SELECT = "//select[@label=\"Bitte wählen Sie einen Tag\"]";
        public static final String TIME_OPTIONS = "//select[@label=\"Bitte wählen Sie einen Tag\"]/option[contains(text(),\":\")]";
//       selector //*[@id="xi-sel-3"]
    }

    private static int MIN_DAY = 12;

    private final ChromeDriver driver;
    private final Random random = new Random();

    public TerminChecker() {
        this.driver = new ChromeDriver(createOptions());
    }

    public void run() throws InterruptedException {
        boolean expired = false;
        System.out.println(driver.executeScript("return navigator.userAgent"));

        driver.get("https://otv.verwalt-berlin.de/ams/TerminBuchen");

        clickWait(BUTTON_BOOK);
        clickWait(CHECKBOX_ACCEPT);
        clickWait(BUTTON_NEXT);

        selectWait(COUNTRY, RUSSIA);
        selectWait(PEOPLE, EINE);
        selectWait(CHILDREN, NEIN);

        clickWait(CATEGORY);
        clickWait(SUB_CATEGORY);
        clickWait(BUTTON_NEXT);

        while (!expired) {
            try {
                waitPage();
                driver.findElement(By.xpath(ERROR));
                System.out.println(LocalDateTime.now() + " - failed");
                try {
                    Thread.sleep(15000L);
                    waitRandom(1000L);
                    clickWait(BUTTON_NEXT);
                } catch (NoSuchElementException | TimeoutException e) {
                    if (isExpired()) {
                        expired = true;
                    }
                }
            } catch (NoSuchElementException e) {
                if (isTermin()) {
//                    int today = LocalDate.now().getDayOfMonth();
                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(SELECTABLE_DAY)));
                    List<WebElement> elements = driver.findElements(By.xpath(SELECTABLE_DAY));
                    try {
                        int minAcceptableDay = elements.stream()
                                .map(WebElement::getText)
                                .mapToInt(Integer::parseInt)
                                .filter(it -> it >= MIN_DAY)
                                .min()
                                .orElseThrow();
                        WebElement dateElement = elements.stream()
                                .filter(it -> Integer.parseInt(it.getText()) == minAcceptableDay)
                                .findFirst()
                                .orElseThrow();
                        dateElement.click();

                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(TIME_SELECT)));
                        List<WebElement> options = driver.findElements(By.xpath(TIME_SELECT));
                        if (!options.isEmpty()) {
                            Select select = new Select(driver.findElement(By.xpath(TIME_SELECT)));
                            select.selectByVisibleText(options.getFirst().getText());
                            System.out.println(LocalDateTime.now() + " - success");
                            driver.switchTo().newWindow(WindowType.TAB);
                            driver.get("https://www.youtube.com/watch?v=_S7WEVLbQ-Y&ab_channel=FicLord");
                            waitRandom(5000);
                            new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.elementToBeClickable(By.xpath(REJECT_YT))).click();
                            return;
                        }
                    } catch (java.util.NoSuchElementException ignored) {
                    }
                    catch (NoSuchElementException | TimeoutException nested) {
                        if (isExpired()) {
                            expired = true;
                        }
                    }
                }
            }
        }
    }

    public ChromeOptions createOptions() {
        ChromeOptions options = new ChromeOptions();

        // bypass bot guard
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        // headless
        //options.addArguments("--headless");

        // set random user-agent
        String userAgent = USER_AGENTS.get(random.nextInt(USER_AGENTS.size()));
        options.addArguments(String.format("--user-agent=%s", userAgent));
        return options;
    }

    private void waitRandom() throws InterruptedException {
        Thread.sleep(Duration.ofMillis(1000L + random.nextLong(4000L)));
    }

    private void waitRandom(long millis) throws InterruptedException {
        Thread.sleep(Duration.ofMillis(1000L + random.nextLong(millis)));
    }

    private void clickWait(String xpath) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
//        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
//        waitRandom();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath))).click();
    }

    private void selectWait(String xpath, String option) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        waitRandom();

        Select select = new Select(driver.findElement(By.xpath(xpath)));
        select.selectByVisibleText(option);
    }

    private boolean isTermin() {
        waitPage();

        try {
            driver.findElement(By.xpath(DATEPICKER));
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private boolean isExpired() {
        waitPage();

        try {
            driver.findElement(By.xpath(EXPIRED));
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public void waitPage() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(HEADER)));
    }
}
