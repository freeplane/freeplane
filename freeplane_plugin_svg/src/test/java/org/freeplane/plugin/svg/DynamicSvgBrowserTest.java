package org.freeplane.plugin.svg;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;

import org.freeplane.features.export.mindmapmode.IExportEngine;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.map.FirstGroupNodeFlag;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNodeFlag;
import org.freeplane.view.swing.map.DynamicSvgTestMap;
import org.freeplane.view.swing.map.MapViewExportCapture;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.By;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.WheelInput;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariDriverService;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@RunWith(Parameterized.class)
public class DynamicSvgBrowserTest {
    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> browsers() {
        return Arrays.asList(new Object[]{"Chrome", "chromedriver"}, new Object[]{"Firefox", "geckodriver"},
                new Object[]{"Safari", "safaridriver"}, new Object[]{"Edge", "msedgedriver"});
    }

    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    private final String browser;
    private final String executable;
    private WebDriver driver;
    private DynamicSvgTestMap map;
    private Path directory;
    private Path output;
    private NodeModel parent;
    private NodeModel leaf;
    private NodeModel hidden;
    private NodeModel throughHidden;
    private NodeModel internal;
    private NodeModel relative;
    private NodeModel custom;
    private NodeModel outside;
    private NodeModel summary;
    private NodeModel summaryLabel;

    public DynamicSvgBrowserTest(String browser, String executable) { this.browser = browser; this.executable = executable; }

    @Before
    public void openNativeExportInActualBrowser() throws Exception {
        Path path = driverOnPath(executable);
        Assume.assumeTrue("BLOCKED: " + browser + " requires an installed browser and " + executable + " on PATH", path != null);
        try {
            switch (browser) {
            case "Chrome": driver = new ChromeDriver(new ChromeDriverService.Builder().usingDriverExecutable(path.toFile()).build()); break;
            case "Firefox": driver = new FirefoxDriver(new GeckoDriverService.Builder().usingDriverExecutable(path.toFile()).build(),
                    new FirefoxOptions().setAcceptInsecureCerts(false)); break;
            case "Safari": driver = new SafariDriver(new SafariDriverService.Builder().usingDriverExecutable(path.toFile()).build()); break;
            case "Edge": driver = new EdgeDriver(new EdgeDriverService.Builder().usingDriverExecutable(path.toFile()).build()); break;
            default: throw new AssertionError(browser);
            }
        } catch (WebDriverException prerequisite) {
            Assume.assumeNoException("BLOCKED: " + browser + " browser/driver startup; Safari requires existing remote-automation permission", prerequisite);
        }
        assertThat(((HasCapabilities) driver).getCapabilities().getCapability("acceptInsecureCerts")).isEqualTo(false);
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(1280, 900));
        map = new DynamicSvgTestMap();
        NodeModel root = map.model.getRootNode();
        parent = map.add(root, "parent", true);
        leaf = map.add(parent, "search target", false);
        map.details(leaf, "detail match");
        hidden = map.add(root, "EXCLUDED_ANCESTOR", true);
        throughHidden = map.add(hidden, "visible through hidden", false);
        NodeModel excluded = map.add(root, "EXCLUDED_OUTSIDE", false);
        NodeModel start = map.add(root, "", false);
        start.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
        NodeModel member = map.add(root, "member", false);
        summary = map.add(root, "", false);
        summary.addExtension(SummaryNodeFlag.SUMMARY);
        summaryLabel = map.add(summary, "summary label", false);
        internal = map.add(root, "internal link", false);
        relative = map.add(root, "relative link", false);
        custom = map.add(root, "custom link", false);
        outside = map.add(root, "outside link", false);
        map.hyperlink(internal, "#" + leaf.getID());
        map.hyperlink(relative, "linked.html");
        map.hyperlink(custom, "freeplane-test:open?value=1&next=2");
        map.hyperlink(outside, "#" + excluded.getID());
        map.connect(leaf, member);
        map.connect(member, summaryLabel);
        map.attach();
        map.filter(new Filter(node -> node != hidden && node != excluded, false, false, false, false, Filter.FilteredElement.NODE, null));
        Icon artwork = new Icon() {
            public int getIconWidth() { return 20; }
            public int getIconHeight() { return 20; }
            public void paintIcon(Component component, Graphics g, int x, int y) {
                Graphics2D graphics = (Graphics2D) g.create();
                try {
                    graphics.setPaint(new GradientPaint(x, y, Color.RED, x + 20, y + 20, Color.BLUE));
                    graphics.fillRect(x, y, 20, 20);
                    graphics.drawImage(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), x, y, null);
                } finally { graphics.dispose(); }
            }
        };
        map.icon(root, artwork);
        map.icon(member, artwork);
        directory = temporary.newFolder("source").toPath();
        output = directory.resolve("map.html");
        Files.write(directory.resolve("linked.html"), "<!doctype html><title>Relative link fixture</title>linked".getBytes(StandardCharsets.UTF_8));
        IExportEngine engine = map.installExports(new Activator()).values().stream().filter(ExportDynamicSvg.class::isInstance).findFirst().get();
        map.onEdt(() -> { engine.export(Collections.singletonList(root), output.toFile()); return null; });
        assertThat(Files.exists(output)).isTrue();
        open(output);
    }

    @After
    public void closeBrowserAndMap() throws Exception {
        try { if (driver != null) driver.quit(); }
        finally { if (map != null) map.close(); }
    }

    @Test
    public void opensLocallyWithInitialFolding() {
        assertThat(driver.getCurrentUrl()).startsWith("file:");
        assertThat(node(parent).isDisplayed()).isTrue();
        assertThat(node(leaf).isDisplayed()).isFalse();
        assertThat(node(summaryLabel).isDisplayed()).isTrue();
        assertThat(driver.findElements(By.cssSelector(".crosslink"))).hasSize(1);
        assertThat(driver.findElements(By.cssSelector(".summary-edge[data-summary-id]"))).hasSize(1);
        assertThat(js("return performance.getEntriesByType('resource').filter(e => /^https?:/.test(e.name)).length")).isEqualTo(0L);
        assertNoPageErrors();
    }

    @Test
    public void foldControlsUpdateTheRenderedMap() {
        node(parent).findElement(By.cssSelector(".toggle circle")).click();
        assertThat(node(leaf).isDisplayed()).isTrue();
        assertThat(driver.findElements(By.cssSelector(".crosslink"))).hasSize(2);
        node(parent).findElement(By.cssSelector(".toggle")).sendKeys(Keys.SPACE);
        assertThat(node(leaf).isDisplayed()).isFalse();
        node(summary).findElement(By.cssSelector(".toggle circle")).click();
        assertThat(node(summaryLabel).isDisplayed()).isFalse();
        assertThat(driver.findElements(By.cssSelector(".crosslink"))).isEmpty();
        driver.findElement(By.id("toggle-all")).click();
        assertThat(node(leaf).isDisplayed()).isTrue();
        assertThat(node(throughHidden).isDisplayed()).isTrue();
        assertThat(node(summaryLabel).isDisplayed()).isTrue();
        assertThat(driver.findElements(By.cssSelector("polygon[data-arrow-end]"))).hasSize(4);
        driver.findElement(By.id("toggle-all")).click();
        assertThat(node(leaf).isDisplayed()).isFalse();
        assertThat(node(throughHidden).isDisplayed()).isFalse();
        assertNoPageErrors();
    }

    @Test
    public void searchFindsTextAndDetailsInsideFoldedBranches() {
        driver.findElement(By.id("search")).sendKeys("detail match");
        assertThat(node(leaf).isDisplayed()).isTrue();
        assertThat(node(leaf).findElement(By.cssSelector(".node-highlight")).getAttribute("visibility")).isEqualTo("visible");
        assertCentered(leaf);
        driver.findElement(By.id("next")).click();
        assertCentered(leaf);
        driver.findElement(By.id("search")).clear();
        driver.findElement(By.id("search")).sendKeys("EXCLUDED_");
        assertThat(driver.findElement(By.id("status")).getText()).contains("No matches");
        assertNoPageErrors();
    }

    @Test
    public void internalLinksRevealAndCenterTargets() {
        node(internal).findElement(By.cssSelector("a.node-link .link-hitarea")).click();
        assertThat(node(leaf).isDisplayed()).isTrue();
        assertCentered(leaf);
        assertThat(driver.getWindowHandles()).hasSize(1);
        assertThat(js("return document.getElementById('canvas').classList.contains('dragging')")).isEqualTo(false);
        assertNoPageErrors();
    }

    @Test
    public void preservesBrowserNavigationForOtherLinks() {
        for (NodeModel link : Arrays.asList(custom, outside)) {
            WebElement anchor = node(link).findElement(By.cssSelector("a.node-link"));
            String href = anchor.getDomAttribute("href");
            assertThat(href).isEqualTo(org.freeplane.features.link.NodeLinks.getLinkAsString(link));
            js("window.navigationWasPrevented=null; arguments[0].addEventListener('click', e => { window.navigationWasPrevented=e.defaultPrevented; e.preventDefault(); });", anchor);
            anchor.findElement(By.cssSelector(".link-hitarea")).click();
            assertThat(js("return window.navigationWasPrevented")).isEqualTo(false);
        }
        assertNoPageErrors();
    }

    @Test
    public void relativeLinksWorkAfterMovingTheDirectoryTree() throws Exception {
        Path moved = temporary.getRoot().toPath().resolve("moved");
        Files.move(directory, moved);
        open(moved.resolve("map.html"));
        String originalWindow = driver.getWindowHandle();
        WebElement anchor = node(relative).findElement(By.cssSelector("a.node-link"));
        assertThat(anchor.getDomAttribute("href")).isEqualTo("linked.html");
        anchor.findElement(By.cssSelector(".link-hitarea")).click();
        waitFor().until(browser -> browser.getWindowHandles().size() == 2);
        driver.switchTo().window(driver.getWindowHandles().stream().filter(handle -> !handle.equals(originalWindow)).findFirst().get());
        assertThat(driver.getCurrentUrl()).isEqualTo(moved.resolve("linked.html").toUri().toString());
        assertThat(driver.getTitle()).isEqualTo("Relative link fixture");
    }

    @Test
    public void panZoomAndFitUpdateTheRenderedViewport() {
        WebElement viewport = driver.findElement(By.id("viewport"));
        String fitted = viewport.getAttribute("transform");
        driver.findElement(By.id("zoom-in")).click();
        assertThat(viewport.getAttribute("transform")).isNotEqualTo(fitted);
        driver.findElement(By.id("zoom-out")).click();
        String beforeWheel = viewport.getAttribute("transform");
        WebElement canvas = driver.findElement(By.id("canvas"));
        new Actions(driver).scrollFromOrigin(WheelInput.ScrollOrigin.fromElement(canvas), 0, 200).perform();
        assertThat(viewport.getAttribute("transform")).isNotEqualTo(beforeWheel);
        String beforePan = viewport.getAttribute("transform");
        new Actions(driver).moveToElement(canvas, -canvas.getSize().width / 2 + 10, -canvas.getSize().height / 2 + 10)
                .clickAndHold().moveByOffset(30, 20).release().perform();
        assertThat(viewport.getAttribute("transform")).isNotEqualTo(beforePan);
        driver.findElement(By.id("fit")).click();
        assertThat(viewport.getAttribute("transform")).isEqualTo(fitted);
        assertNoPageErrors();
    }

    @Test
    public void loadsEmbeddedFontsAndKeepsFallbackTextUsable() throws Exception {
        byte[] bytes = DynamicSvgTestMap.gentiumBytes();
        Font font = Font.createFont(Font.TRUETYPE_FONT, new java.io.ByteArrayInputStream(bytes)).deriveFont(12f);
        map.model.getRootNode().setUserObject("alpha");
        map.node(map.model.getRootNode()).getMainView().setText("alpha");
        map.node(map.model.getRootNode()).getMainView().setFont(font);
        DynamicSvgDocument document = map.onEdt(() -> new MapViewExportCapture(map.view).capture(new ExportDynamicSvg()::capture));
        JsonObject face = new JsonObject();
        face.addProperty("family", font.getFamily()); face.addProperty("style", "normal"); face.addProperty("weight", 400);
        face.addProperty("format", "truetype"); face.addProperty("data", Base64.getEncoder().encodeToString(bytes));
        JsonArray faces = new JsonArray(); faces.add(face);
        document.write(output, new DynamicSvgFonts.Embedding(faces, Collections.emptyList()));
        open(output);
        assertThat(js("return [...document.fonts].some(font => font.status==='loaded' && font.family.includes(arguments[0]))", font.getFamily())).isEqualTo(true);
        assertThat(js("return getComputedStyle(document.querySelector('#nodes text')).fontFamily")).asString().contains(font.getFamily());
        face.addProperty("data", "AQIDBA==");
        document.write(output, new DynamicSvgFonts.Embedding(faces, Collections.emptyList()));
        open(output);
        assertThat(driver.findElement(By.id("status")).getText()).contains("Font embedding incomplete");
        assertThat(node(map.model.getRootNode()).isDisplayed()).isTrue();
        driver.findElement(By.id("zoom-in")).click();
        assertNoPageErrors();
    }

    @Test
    public void keepsArtworkReferencesLocalAndMapScriptsInactive() throws Exception {
        DynamicSvgDocument document = map.onEdt(() -> new MapViewExportCapture(map.view).capture(new ExportDynamicSvg()::capture));
        document.manifest.getAsJsonArray("nodes").forEach(record -> {
            JsonObject node = record.getAsJsonObject();
            if (node.get("contentVisible").getAsBoolean())
                node.addProperty("artwork", node.get("artwork").getAsString().replace("</svg", "<script>window.mapScriptRan=true</script><rect onload=\"window.mapScriptRan=true\"/> </svg"));
        });
        document.write(output, new DynamicSvgFonts.Embedding(new JsonArray(), Collections.emptyList()));
        open(output);
        assertThat(js("return Boolean(window.mapScriptRan)")).isEqualTo(false);
        assertThat(js("return [...document.querySelectorAll('#nodes [id]')].map(e=>e.id).length === new Set([...document.querySelectorAll('#nodes [id]')].map(e=>e.id)).size")).isEqualTo(true);
        assertThat(driver.findElements(By.cssSelector("#nodes linearGradient"))).hasSizeGreaterThanOrEqualTo(2);
        assertThat(js("return [...document.querySelectorAll('#nodes image')].every(e => (e.getAttribute('href') || e.getAttributeNS('http://www.w3.org/1999/xlink','href')).startsWith('data:image/'))")).isEqualTo(true);
        assertThat(driver.findElement(By.id("nodes")).getText()).doesNotContain("EXCLUDED_");
        assertThat(driver.findElements(By.cssSelector("#nodes script, #nodes [onload]"))).isEmpty();
        assertNoPageErrors();
    }

    private void open(Path file) throws Exception {
        // Test-only error capture; the production viewer has no test API.
        String html = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        Matcher script = Pattern.compile("<script>([\\s\\S]*?)</script>").matcher(html);
        assertThat(script.find()).isTrue();
        String body = "window.testPageErrors=[]; window.addEventListener('error',e=>window.testPageErrors.push(e.message));\n" + script.group(1);
        String hash = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(body.getBytes(StandardCharsets.UTF_8)));
        html = html.substring(0, script.start(1)) + body + html.substring(script.end(1));
        html = html.replaceFirst("script-src 'sha256-[^']+'", Matcher.quoteReplacement("script-src 'sha256-" + hash + "'"));
        Files.write(file, html.getBytes(StandardCharsets.UTF_8));
        driver.get(file.toUri().toString());
        waitFor().until(browser -> browser.findElement(By.id("status")).getText().contains(" nodes"));
        assertNoPageErrors();
    }

    private void assertNoPageErrors() { assertThat((List<?>) js("return window.testPageErrors")).isEmpty(); }
    private Object js(String script, Object... arguments) { return ((JavascriptExecutor) driver).executeScript(script, arguments); }
    private WebDriverWait waitFor() { return new WebDriverWait(driver, Duration.ofSeconds(15)); }
    private WebElement node(NodeModel node) { return driver.findElement(By.cssSelector("[data-node-id='" + node.getID() + "']")); }
    private void assertCentered(NodeModel node) {
        assertThat((Boolean) js("const n=arguments[0].getBoundingClientRect(), c=document.querySelector('#canvas').getBoundingClientRect(); return Math.abs((n.left+n.right-c.left-c.right)/2)<30 && Math.abs((n.top+n.bottom-c.top-c.bottom)/2)<30;", node(node))).isTrue();
    }
    private Path driverOnPath(String executable) {
        String path = System.getenv("PATH");
        if (path != null) for (String directory : path.split(java.io.File.pathSeparator)) {
            Path candidate = Paths.get(directory, executable);
            if (Files.isRegularFile(candidate) && Files.isExecutable(candidate)) return candidate;
        }
        return null;
    }
}
