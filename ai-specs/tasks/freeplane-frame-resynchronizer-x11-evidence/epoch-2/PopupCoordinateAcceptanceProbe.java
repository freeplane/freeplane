package org.freeplane.core.ui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToolTip;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLightLaf;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Disposable native-X11/XWayland acceptance probe for the frame resynchronizer
 * and owner-local popup coordinate calibration path.
 */
public final class PopupCoordinateAcceptanceProbe {
    private static final String EXPECTED_TOOLKIT = "sun.awt.X11.XToolkit";
    private static final String EXPECTED_DELEGATE = "com.formdev.flatlaf.ui.FlatPopupFactory";
    private static final String EXPECTED_HEAVYWEIGHT_POPUP = "javax.swing.Popup$HeavyWeightWindow";
    private static final long POPUP_PERSISTENCE_MILLIS = 800L;
    private static final long STATE_SETTLE_MILLIS = 1200L;
    private static final long POINTER_EVENT_TIMEOUT_MILLIS = 2000L;
    private static final int X11_SCREEN_NUMBER = 0;
    private static final int MAX_PLACEMENT_ERROR = 1;

    private final List<String> failures = new ArrayList<String>();
    private final PopupCoordinateCalibrator.GeometryProvider productionGeometryProvider =
        new PopupCoordinateCalibrator.GeometryProvider() {
            @Override
            public Rectangle getNativeFrameBounds(final JFrame frame) {
                return FrameResynchronizer.getNativeWindowBounds(frame);
            }

            @Override
            public Point getOwnerLocationWithinFrame(final Component owner, final JFrame frame) {
                return SwingUtilities.convertPoint(owner, 0, 0, frame);
            }

            @Override
            public Point getOwnerLocationOnScreen(final Component owner) {
                try {
                    if (owner == null || !owner.isShowing()) {
                        return null;
                    }
                    return owner.getLocationOnScreen();
                }
                catch (RuntimeException unavailableLocation) {
                    return null;
                }
            }
        };

    private CountingFrame frame;
    private JButton owner;
    private PopupFactory adapter;
    private PopupFactory delegate;
    private Robot robot;

    public static void main(final String[] args) {
        final PopupCoordinateAcceptanceProbe probe = new PopupCoordinateAcceptanceProbe();
        try {
            probe.run();
        }
        catch (Throwable throwable) {
            probe.fail("probe-exception=" + throwable.getClass().getName() + ": " + throwable.getMessage());
            throwable.printStackTrace(System.out);
        }
        finally {
            probe.disposeFrame();
        }
        if (probe.failures.isEmpty()) {
            log("probe-result=PASS");
            System.exit(0);
        }
        log("probe-result=FAIL");
        for (final String failure : probe.failures) {
            log("failure=" + failure);
        }
        System.exit(1);
    }

    private void run() throws Exception {
        FlatLightLaf.setup();
        log("probe=PopupCoordinateAcceptanceProbe");
        log("display=" + System.getenv("DISPLAY"));
        log("java-version=" + System.getProperty("java.version"));
        log("toolkit-class=" + Toolkit.getDefaultToolkit().getClass().getName());
        logScreenIdentity();
        require(EXPECTED_TOOLKIT.equals(Toolkit.getDefaultToolkit().getClass().getName()),
            "toolkit-class-mismatch expected=" + EXPECTED_TOOLKIT);

        createAndShowFrame();
        robot = new Robot();
        robot.setAutoDelay(30);
        logSharedPopupFactory();
        runCorrectionAssertions();
        if (adapter == null || delegate == null) {
            fail("popup-factory-not-ready");
            return;
        }
        runPopupClassParity();
        runScenario("startup-normal");
        setFrameState("maximize", Frame.MAXIMIZED_BOTH, true);
        runScenario("maximize");
        setFrameState("restore", Frame.NORMAL, false);
        runScenario("restore");
        setFrameState("remaximize", Frame.MAXIMIZED_BOTH, true);
        runScenario("remaximize");
    }

    private void createAndShowFrame() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                frame = new CountingFrame();
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
                owner = new JButton("Popup owner");
                panel.add(owner);
                frame.add(panel, BorderLayout.CENTER);
                frame.setSize(900, 600);
                frame.setLocation(120, 120);
                // This is the production lifecycle: install before the peer is shown.
                FrameResynchronizer.install(frame);
                frame.setVisible(true);
                frame.toFront();
                frame.requestFocus();
                owner.requestFocusInWindow();
                frame.setTracking(true);
            }
        });
        waitFor("frame-visible", new Condition() {
            @Override
            public boolean isSatisfied() {
                return frame != null && frame.isShowing() && owner != null && owner.isShowing();
            }
        }, 5000L);
        activateFrame("initial");
        waitFor("frame-native-bounds", new Condition() {
            @Override
            public boolean isSatisfied() {
                return FrameResynchronizer.getNativeWindowBounds(frame) != null;
            }
        }, 5000L);
    }

    private void logScreenIdentity() {
        try {
            final GraphicsConfiguration configuration =
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
            final Rectangle bounds = configuration.getBounds();
            final Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(configuration);
            log("screen-bounds=" + formatRectangle(bounds));
            log("screen-insets=" + formatInsets(insets));
            log("screen-size=" + Toolkit.getDefaultToolkit().getScreenSize().width + "x"
                + Toolkit.getDefaultToolkit().getScreenSize().height);
        }
        catch (RuntimeException unavailableScreenIdentity) {
            fail("screen-identity-unavailable=" + unavailableScreenIdentity.getClass().getName());
        }
    }

    private void logSharedPopupFactory() {
        final PopupFactory shared = PopupFactory.getSharedInstance();
        log("popup-factory-class=" + (shared == null ? "null" : shared.getClass().getName()));
        if (!(shared instanceof X11PopupFactory)) {
            fail("popup-factory-class-mismatch expected=" + X11PopupFactory.class.getName());
            return;
        }
        adapter = shared;
        delegate = ((X11PopupFactory) shared).getDelegate();
        log("popup-factory-delegate-class=" + delegate.getClass().getName());
        log("popup-factory-delegate-is-flatpopupfactory=" + EXPECTED_DELEGATE.equals(delegate.getClass().getName()));
        require(EXPECTED_DELEGATE.equals(delegate.getClass().getName()),
            "popup-factory-delegate-mismatch expected=" + EXPECTED_DELEGATE);
    }

    private void runPopupClassParity() throws Exception {
        recordPopupClassParity("heavyweight-menu", showMenuForFactory(adapter, false),
            showMenuForFactory(delegate, false));
        recordPopupClassParity("lightweight-enabled-menu", showMenuForFactory(adapter, true),
            showMenuForFactory(delegate, true));
        recordPopupClassParity("tooltip", showToolTipForFactory(adapter), showToolTipForFactory(delegate));
        setSharedFactory(adapter);
    }

    private void recordPopupClassParity(final String kind, final String adapterClass, final String delegateClass) {
        final boolean equal = adapterClass != null && adapterClass.equals(delegateClass);
        log("popup-class-parity=" + kind + " adapter-class=" + adapterClass + " delegate-class=" + delegateClass
            + " equal=" + equal + " expected=" + EXPECTED_HEAVYWEIGHT_POPUP);
        require(equal, kind + " popup-class-parity-mismatch");
        require(EXPECTED_HEAVYWEIGHT_POPUP.equals(adapterClass), kind + " adapter-popup-class-mismatch");
        require(EXPECTED_HEAVYWEIGHT_POPUP.equals(delegateClass), kind + " delegate-popup-class-mismatch");
    }

    private String showMenuForFactory(final PopupFactory factory, final boolean lightweight) throws Exception {
        final String[] popupClass = new String[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                setSharedFactory(factory);
                final JPopupMenu menu = new JPopupMenu();
                menu.setLightWeightPopupEnabled(lightweight);
                menu.add(new JMenuItem(lightweight ? "Lightweight enabled" : "Heavyweight"));
                try {
                    menu.show(owner, 0, owner.getHeight());
                    Window popupWindow = SwingUtilities.getWindowAncestor(menu);
                    if (popupWindow == frame) {
                        popupWindow = findVisiblePopupWindow();
                    }
                    popupClass[0] = popupWindow == null ? null : popupWindow.getClass().getName();
                }
                finally {
                    menu.setVisible(false);
                    setSharedFactory(adapter);
                }
            }
        });
        return popupClass[0];
    }

    private String showToolTipForFactory(final PopupFactory factory) throws Exception {
        final String[] popupClass = new String[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                setSharedFactory(factory);
                final JToolTip toolTip = new JToolTip();
                toolTip.setTipText("Acceptance tooltip");
                Popup popup = null;
                try {
                    final Point screen = owner.getLocationOnScreen();
                    popup = factory.getPopup(owner, toolTip, screen.x, screen.y);
                    popup.show();
                    final Window popupWindow = findVisiblePopupWindow();
                    popupClass[0] = popupWindow == null ? null : popupWindow.getClass().getName();
                }
                finally {
                    if (popup != null) {
                        popup.hide();
                    }
                    setSharedFactory(adapter);
                }
            }
        });
        return popupClass[0];
    }

    private Window findVisiblePopupWindow() {
        final Window[] windows = Window.getWindows();
        for (int i = windows.length - 1; i >= 0; i--) {
            final Window candidate = windows[i];
            if (candidate != frame && candidate.isShowing()
                    && candidate.getClass().getName().startsWith("javax.swing.Popup$")) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Queries a popup shell directly because the production frame query rejects
     * implausibly small windows; heavyweight popup windows are normally only
     * about 32 pixels high. The frame query and all calibration inputs remain
     * the production implementations.
     */
    private static Rectangle getNativePopupBounds(final Component component) {
        X11.Display display = null;
        try {
            final long shell = getNativeShell(component);
            if (shell == 0L) {
                return null;
            }
            display = X11.INSTANCE.XOpenDisplay(null);
            if (display == null) {
                return null;
            }
            final X11.Window root = X11.INSTANCE.XDefaultRootWindow(display);
            final X11.Window shellWindow = new X11.Window(shell);
            final IntByReference x = new IntByReference();
            final IntByReference y = new IntByReference();
            final X11.WindowByReference child = new X11.WindowByReference();
            if (!X11.INSTANCE.XTranslateCoordinates(display, shellWindow, root, 0, 0, x, y, child)) {
                return null;
            }
            final IntByReference rootX = new IntByReference();
            final IntByReference rootY = new IntByReference();
            final IntByReference width = new IntByReference();
            final IntByReference height = new IntByReference();
            final IntByReference border = new IntByReference();
            final IntByReference depth = new IntByReference();
            final X11.WindowByReference geometryRoot = new X11.WindowByReference();
            final int status = X11.INSTANCE.XGetGeometry(display, new X11.Drawable(shell), geometryRoot,
                rootX, rootY, width, height, border, depth);
            if (status == 0) {
                return null;
            }
            final int[] extents = getFrameExtents(display, shellWindow);
            return new Rectangle(x.getValue() - extents[0], y.getValue() - extents[2],
                width.getValue() + extents[0] + extents[1], height.getValue() + extents[2] + extents[3]);
        }
        catch (Throwable unavailableNativePopupBounds) {
            return null;
        }
        finally {
            if (display != null) {
                try {
                    X11.INSTANCE.XCloseDisplay(display);
                }
                catch (Throwable ignored) {
                }
            }
        }
    }

    private static long getNativeShell(final Component component) {
        if (component == null) {
            return 0L;
        }
        try {
            final Class<?> awtAccessor = Class.forName("sun.awt.AWTAccessor");
            final Object componentAccessor = awtAccessor.getMethod("getComponentAccessor").invoke(null);
            final Method getPeer = Class.forName("sun.awt.AWTAccessor$ComponentAccessor")
                .getMethod("getPeer", Component.class);
            getPeer.setAccessible(true);
            final Object peer = getPeer.invoke(componentAccessor, component);
            if (peer == null) {
                return 0L;
            }
            try {
                final Method getShell = peer.getClass().getMethod("getShell");
                getShell.setAccessible(true);
                return ((Number) getShell.invoke(peer)).longValue();
            }
            catch (NoSuchMethodException noShellMethod) {
                Class<?> peerClass = peer.getClass();
                while (peerClass != null) {
                    try {
                        final Field windowField = peerClass.getDeclaredField("window");
                        windowField.setAccessible(true);
                        return windowField.getLong(peer);
                    }
                    catch (NoSuchFieldException noWindowField) {
                        peerClass = peerClass.getSuperclass();
                    }
                }
            }
        }
        catch (Throwable unavailableNativeShell) {
            return 0L;
        }
        return 0L;
    }

    private static int[] getFrameExtents(final X11.Display display, final X11.Window window) {
        final int[] result = new int[4];
        try {
            final X11.Atom atom = X11.INSTANCE.XInternAtom(display, "_NET_FRAME_EXTENTS", false);
            final X11.AtomByReference actualType = new X11.AtomByReference();
            final IntByReference actualFormat = new IntByReference();
            final NativeLongByReference nitems = new NativeLongByReference();
            final NativeLongByReference bytesAfter = new NativeLongByReference();
            final PointerByReference property = new PointerByReference();
            final int status = X11.INSTANCE.XGetWindowProperty(display, window, atom,
                new com.sun.jna.NativeLong(0), new com.sun.jna.NativeLong(4), false, new X11.Atom(0),
                actualType, actualFormat, nitems, bytesAfter, property);
            if (status == 0 && actualFormat.getValue() == 32 && property.getValue() != null
                    && nitems.getValue().longValue() >= 4L) {
                final long[] values = property.getValue().getLongArray(0, 4);
                for (int i = 0; i < result.length; i++) {
                    result[i] = (int) values[i];
                }
            }
            if (property.getValue() != null) {
                X11.INSTANCE.XFree(property.getValue());
            }
        }
        catch (Throwable ignored) {
        }
        return result;
    }

    private void runCorrectionAssertions() {
        final Point splitCacheCorrection = PopupCoordinateCalibrator.calculateCorrection(owner, frame,
            syntheticGeometry(new Rectangle(0, 0, 2240, 1400), new Point(1035, 5), new Point(1275, 185)));
        log("correction-assertion=split-cache expected=(-240,-180) actual=" + formatPoint(splitCacheCorrection)
            + " result=" + new Point(-240, -180).equals(splitCacheCorrection));
        require(new Point(-240, -180).equals(splitCacheCorrection),
            "split-cache correction mismatch");

        final Point matchedCorrection = PopupCoordinateCalibrator.calculateCorrection(owner, frame,
            syntheticGeometry(new Rectangle(240, 180, 960, 600), new Point(10, 20), new Point(250, 200)));
        log("correction-assertion=matched-geometry expected=(0,0) actual=" + formatPoint(matchedCorrection)
            + " result=" + new Point(0, 0).equals(matchedCorrection));
        require(new Point(0, 0).equals(matchedCorrection),
            "matched-geometry correction mismatch");
    }

    private PopupCoordinateCalibrator.GeometryProvider syntheticGeometry(final Rectangle nativeBounds,
            final Point ownerWithinFrame, final Point ownerOnScreen) {
        return new PopupCoordinateCalibrator.GeometryProvider() {
            @Override
            public Rectangle getNativeFrameBounds(final JFrame ignoredFrame) {
                return new Rectangle(nativeBounds);
            }

            @Override
            public Point getOwnerLocationWithinFrame(final Component ignoredOwner, final JFrame ignoredFrame) {
                return new Point(ownerWithinFrame);
            }

            @Override
            public Point getOwnerLocationOnScreen(final Component ignoredOwner) {
                return new Point(ownerOnScreen);
            }
        };
    }

    private void setFrameState(final String transition, final int state, final boolean maximized) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                frame.setExtendedState(state);
            }
        });
        waitFor(transition + "-state", new Condition() {
            @Override
            public boolean isSatisfied() {
                final boolean isMaximized = (frame.getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;
                return isMaximized == maximized;
            }
        }, 5000L);
        waitFor(transition + "-native-bounds", new Condition() {
            @Override
            public boolean isSatisfied() {
                return FrameResynchronizer.getNativeWindowBounds(frame) != null;
            }
        }, 5000L);
        Thread.sleep(STATE_SETTLE_MILLIS);
    }

    private void runScenario(final String scenario) throws Exception {
        final ScenarioEvidence evidence = new ScenarioEvidence(scenario);
        final SelectionCapture selection = new SelectionCapture();
        final JPopupMenu[] menuHolder = new JPopupMenu[1];
        final JMenuItem[] itemHolder = new JMenuItem[1];
        evidence.frameCorrectionCountBefore = frame.getRectangleSetBoundsCalls();

        evidence.frameActive = activateFrame(scenario);
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                evidence.ownerScreen = owner.getLocationOnScreen();
                evidence.ownerWithinFrame = SwingUtilities.convertPoint(owner, 0, 0, frame);
                evidence.nativeFrameBounds = FrameResynchronizer.getNativeWindowBounds(frame);
                if (evidence.nativeFrameBounds != null) {
                    evidence.expectedOwner = new Point(
                        evidence.nativeFrameBounds.x + evidence.ownerWithinFrame.x,
                        evidence.nativeFrameBounds.y + evidence.ownerWithinFrame.y);
                }
                evidence.correction = PopupCoordinateCalibrator.calculateCorrection(owner, frame,
                    productionGeometryProvider);
                evidence.popupLocal = new Point(0, owner.getHeight());
                evidence.requestedPopup = new Point(evidence.ownerScreen.x + evidence.popupLocal.x,
                    evidence.ownerScreen.y + evidence.popupLocal.y);
                if (evidence.correction != null) {
                    evidence.expectedPopup = new Point(evidence.requestedPopup.x + evidence.correction.x,
                        evidence.requestedPopup.y + evidence.correction.y);
                }

                final JPopupMenu menu = new JPopupMenu();
                final JMenuItem item = new JMenuItem("selected-" + scenario);
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent event) {
                        if (selection.pointerPhase) {
                            selection.pointerSelected = true;
                            selection.pointerText = item.getText();
                            log("popup-pointer-selected=" + item.getText());
                        }
                        else {
                            selection.doClickFallbackSelected = true;
                            selection.doClickFallbackText = item.getText();
                            log("diagnostic-only popup-doClick-fallback-selected=" + item.getText());
                        }
                    }
                });
                menu.add(item);
                menuHolder[0] = menu;
                itemHolder[0] = item;
                menu.show(owner, evidence.popupLocal.x, evidence.popupLocal.y);
                evidence.shownNanos = System.nanoTime();
                evidence.popupWindow = SwingUtilities.getWindowAncestor(menu);
                if (evidence.popupWindow == frame) {
                    evidence.popupWindow = findVisiblePopupWindow();
                }
            }
        });

        final JPopupMenu menu = menuHolder[0];
        final JMenuItem item = itemHolder[0];
        try {
            Thread.sleep(POPUP_PERSISTENCE_MILLIS + 100L);
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    evidence.menuShowingAtPersistenceCheckpoint = menu.isShowing();
                    if (evidence.popupWindow == null) {
                        evidence.popupWindow = SwingUtilities.getWindowAncestor(menu);
                    }
                    if (evidence.popupWindow != null) {
                        evidence.popupAwtBounds = new Rectangle(evidence.popupWindow.getBounds());
                        evidence.popupNativeBounds = getNativePopupBounds(evidence.popupWindow);
                        evidence.popupWindowClass = evidence.popupWindow.getClass().getName();
                    }
                    evidence.menuScreenBounds = getScreenBounds(menu);
                    evidence.itemScreenBounds = getScreenBounds(item);
                    evidence.itemLocationWithinMenu = item.getLocation();
                }
            });
            evidence.persistenceMillis = (System.nanoTime() - evidence.shownNanos) / 1000000L;
            require(evidence.persistenceMillis >= POPUP_PERSISTENCE_MILLIS,
                scenario + " persistence-duration-too-short");
            require(evidence.menuShowingAtPersistenceCheckpoint,
                scenario + " menu-not-showing-after-800ms");
            log("popup-persistence scenario=" + scenario
                + " duration-ms=" + evidence.persistenceMillis
                + " minimum-ms=" + POPUP_PERSISTENCE_MILLIS
                + " menu-showing=" + evidence.menuShowingAtPersistenceCheckpoint);

            selectWithPointer(scenario, menu, item, selection, evidence);

            log("diagnostic-only doClick-fallback-start scenario=" + scenario);
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    item.doClick();
                }
            });
            evidence.doClickFallbackSelected = selection.doClickFallbackSelected;
            evidence.doClickFallbackText = selection.doClickFallbackText;
            require(evidence.doClickFallbackSelected, scenario + " doClick-fallback-selection-missing");
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    evidence.menuShowingAfterFallback = menu.isShowing();
                }
            });
            log("menu-showing-after-fallback scenario=" + scenario
                + " value=" + evidence.menuShowingAfterFallback);

            if (evidence.expectedPopup == null || evidence.popupNativeBounds == null) {
                require(false, scenario + " native-placement-evidence-unavailable");
            }
            else {
                evidence.placementErrorX = evidence.popupNativeBounds.x - evidence.expectedPopup.x;
                evidence.placementErrorY = evidence.popupNativeBounds.y - evidence.expectedPopup.y;
                evidence.placementError = Math.max(Math.abs(evidence.placementErrorX),
                    Math.abs(evidence.placementErrorY));
                require(evidence.placementError <= MAX_PLACEMENT_ERROR,
                    "PRODUCT: popup misplaced scenario=" + scenario
                        + " placement-error=(" + evidence.placementErrorX + "," + evidence.placementErrorY + ")");
            }
        }
        finally {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    menu.setVisible(false);
                }
            });
        }
        evidence.frameCorrectionCountAfter = frame.getRectangleSetBoundsCalls();
        evidence.frameCorrectionCount = evidence.frameCorrectionCountAfter - evidence.frameCorrectionCountBefore;
        log("scenario=" + scenario
            + " owner-awt-screen=" + formatPoint(evidence.ownerScreen)
            + " owner-location-within-frame=" + formatPoint(evidence.ownerWithinFrame)
            + " native-outer-frame-bounds=" + formatRectangle(evidence.nativeFrameBounds)
            + " expected-native-owner-origin=" + formatPoint(evidence.expectedOwner)
            + " correction=" + formatPoint(evidence.correction)
            + " requested-popup-coordinates=" + formatPoint(evidence.requestedPopup)
            + " popup-local-offset=" + formatPoint(evidence.popupLocal)
            + " expected-popup-native-origin=" + formatPoint(evidence.expectedPopup)
            + " popup-window-class=" + evidence.popupWindowClass
            + " popup-awt-bounds=" + formatRectangle(evidence.popupAwtBounds)
            + " popup-native-bounds=" + formatRectangle(evidence.popupNativeBounds)
            + " popup-menu-screen-bounds=" + formatRectangle(evidence.menuScreenBounds)
            + " popup-item-screen-bounds=" + formatRectangle(evidence.itemScreenBounds)
            + " pointer-click-screen-point=" + formatPoint(evidence.pointerClickScreen)
            + " placement-error=(" + evidence.placementErrorX + "," + evidence.placementErrorY + ")"
            + " placement-error-max=" + evidence.placementError
            + " persistence-ms=" + evidence.persistenceMillis
            + " menu-showing-before-pointer=" + evidence.menuShowingBeforePointer
            + " menu-showing-after-pointer=" + evidence.menuShowingAfterPointer
            + " menu-showing-after-fallback=" + evidence.menuShowingAfterFallback
            + " pointer-selection=" + evidence.pointerSelected
            + " pointer-selected-text=" + evidence.pointerSelectedText
            + " doClick-fallback-selection=" + evidence.doClickFallbackSelected
            + " doClick-fallback-text=" + evidence.doClickFallbackText
            + " frame-correction-setBounds-delta=" + evidence.frameCorrectionCount);
    }

    private boolean activateFrame(final String scenario) throws Exception {
        for (int attempt = 1; attempt <= 5; attempt++) {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    frame.toFront();
                    frame.requestFocus();
                    owner.requestFocusInWindow();
                }
            });
            if (waitUntil(new Condition() {
                @Override
                public boolean isSatisfied() {
                    return frame.isActive();
                }
            }, 1000L)) {
                log("frame-active scenario=" + scenario + " value=true attempt=" + attempt);
                return true;
            }
        }
        log("frame-active scenario=" + scenario + " value=false");
        fail("ENVIRONMENT: frame could not be activated scenario=" + scenario + " timeout-ms=5000");
        return false;
    }

    private void selectWithPointer(final String scenario, final JPopupMenu menu, final JMenuItem item,
            final SelectionCapture selection, final ScenarioEvidence evidence) throws Exception {
        if (!evidence.frameActive) {
            fail("ENVIRONMENT: pointer path skipped because frame could not be activated scenario=" + scenario);
            return;
        }
        evidence.pointerClickScreen = calculateExpectedPointerClick(evidence);
        log("popup-pointer-target=" + scenario
            + " clicked-screen-point=" + formatPoint(evidence.pointerClickScreen)
            + " menu-screen-bounds=" + formatRectangle(evidence.menuScreenBounds)
            + " item-screen-bounds=" + formatRectangle(evidence.itemScreenBounds));
        require(evidence.menuScreenBounds != null, scenario + " menu-screen-bounds-unavailable");
        require(evidence.itemScreenBounds != null, scenario + " item-screen-bounds-unavailable");
        require(evidence.pointerClickScreen != null, scenario + " pointer-click-point-unavailable");
        if (evidence.pointerClickScreen == null) {
            return;
        }
        final boolean pointerClickInsideItem = evidence.itemScreenBounds.contains(evidence.pointerClickScreen);
        log("pointer-click-inside-item-bounds scenario=" + scenario + " value=" + pointerClickInsideItem);
        require(pointerClickInsideItem, scenario + " pointer-click-outside-item-bounds");

        selection.pointerPhase = true;
        selection.pointerEventQueueDrained = false;
        try {
            robot.mouseMove(evidence.pointerClickScreen.x, evidence.pointerClickScreen.y);
            // XTest motion synchronizes the server pointer on both isolated X11 and XWayland.
            synchronizeXWaylandPointer(evidence.pointerClickScreen);
            Thread.sleep(100L);
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    evidence.menuShowingBeforePointer = menu.isShowing();
                }
            });
            log("menu-showing-before-pointer scenario=" + scenario
                + " value=" + evidence.menuShowingBeforePointer);
            require(evidence.menuShowingBeforePointer, scenario + " menu-not-showing-before-pointer");
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            robot.waitForIdle();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    selection.pointerEventQueueDrained = true;
                }
            });
            waitUntil(new Condition() {
                @Override
                public boolean isSatisfied() {
                    return selection.pointerEventQueueDrained && selection.pointerSelected;
                }
            }, POINTER_EVENT_TIMEOUT_MILLIS);
            if (!selection.pointerSelected) {
                fail("ENVIRONMENT: pointer event dispatch or selection failed scenario=" + scenario
                    + " timeout-ms=" + POINTER_EVENT_TIMEOUT_MILLIS);
            }
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    evidence.menuShowingAfterPointer = menu.isShowing();
                }
            });
            log("menu-showing-after-pointer scenario=" + scenario
                + " value=" + evidence.menuShowingAfterPointer);
        }
        finally {
            selection.pointerPhase = false;
        }
        evidence.pointerSelected = selection.pointerSelected;
        evidence.pointerSelectedText = selection.pointerText;
        if (!evidence.pointerSelected) {
            fail("ENVIRONMENT: pointer selection missing scenario=" + scenario);
        }
    }

    private void synchronizeXWaylandPointer(final Point point) {
        X11.Display display = null;
        try {
            display = X11.INSTANCE.XOpenDisplay(null);
            require(display != null, "xwayland-pointer-sync-display-unavailable");
            if (display != null) {
                final boolean moved = XTestLibrary.INSTANCE.XTestFakeMotionEvent(display, X11_SCREEN_NUMBER,
                    point.x, point.y, new NativeLong(0));
                require(moved, "xwayland-pointer-sync-failed");
                X11.INSTANCE.XFlush(display);
            }
        }
        finally {
            if (display != null) {
                X11.INSTANCE.XCloseDisplay(display);
            }
        }
    }

    private Point calculateExpectedPointerClick(final ScenarioEvidence evidence) {
        // The actual first-item screen bounds are recorded above. Derive the click from the
        // expected calibrated popup origin so a displaced-popup mutant cannot click itself.
        if (evidence.expectedPopup == null || evidence.itemLocationWithinMenu == null
                || evidence.itemScreenBounds == null) {
            return null;
        }
        return new Point(
            evidence.expectedPopup.x + evidence.itemLocationWithinMenu.x + evidence.itemScreenBounds.width / 2,
            evidence.expectedPopup.y + evidence.itemLocationWithinMenu.y + evidence.itemScreenBounds.height / 2);
    }

    private Rectangle getScreenBounds(final Component component) {
        if (component == null || !component.isShowing()) {
            return null;
        }
        try {
            return new Rectangle(component.getLocationOnScreen(), component.getSize());
        }
        catch (RuntimeException unavailableLocation) {
            return null;
        }
    }
    private void setSharedFactory(final PopupFactory factory) {
        PopupFactory.setSharedInstance(factory);
    }

    private boolean waitUntil(final Condition condition, final long timeoutMillis) throws Exception {
        final long deadline = System.nanoTime() + timeoutMillis * 1000000L;
        while (System.nanoTime() < deadline) {
            if (condition.isSatisfied()) {
                return true;
            }
            Thread.sleep(50L);
        }
        return false;
    }

    private void waitFor(final String label, final Condition condition, final long timeoutMillis) throws Exception {
        if (!waitUntil(condition, timeoutMillis)) {
            fail(label + " timeout-ms=" + timeoutMillis);
        }
    }

    private void require(final boolean condition, final String description) {
        if (!condition) {
            fail(description);
        }
    }

    private void fail(final String description) {
        if (!failures.contains(description)) {
            failures.add(description);
            log("assertion-failure=" + description);
        }
    }

    private void disposeFrame() {
        if (frame == null) {
            return;
        }
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    frame.dispose();
                }
            });
        }
        catch (Exception ignored) {
            log("cleanup-failure=" + ignored.getClass().getName() + ": " + ignored.getMessage());
        }
    }

    private static String formatPoint(final Point point) {
        return point == null ? "null" : "(" + point.x + "," + point.y + ")";
    }

    private static String formatRectangle(final Rectangle rectangle) {
        return rectangle == null ? "null" : "(" + rectangle.x + "," + rectangle.y + ","
            + rectangle.width + "," + rectangle.height + ")";
    }

    private static String formatInsets(final Insets insets) {
        return insets == null ? "null" : "(" + insets.left + "," + insets.top + ","
            + insets.right + "," + insets.bottom + ")";
    }

    private static void log(final String message) {
        System.out.println(message);
        System.out.flush();
    }

    private interface Condition {
        boolean isSatisfied() throws Exception;
    }

    private interface XTestLibrary extends Library {
        XTestLibrary INSTANCE = Native.load("Xtst", XTestLibrary.class);

        boolean XTestFakeMotionEvent(X11.Display display, int screenNumber, int x, int y, NativeLong delay);
    }

    private static final class SelectionCapture {
        private volatile boolean pointerPhase;
        private volatile boolean pointerEventQueueDrained;
        private volatile boolean pointerSelected;
        private volatile String pointerText;
        private volatile boolean doClickFallbackSelected;
        private volatile String doClickFallbackText;
    }

    private static final class ScenarioEvidence {
        private final String name;
        private boolean frameActive;
        private Point ownerScreen;
        private Point ownerWithinFrame;
        private Rectangle nativeFrameBounds;
        private Point expectedOwner;
        private Point correction;
        private Point popupLocal;
        private Point requestedPopup;
        private Point expectedPopup;
        private Window popupWindow;
        private String popupWindowClass;
        private Rectangle popupAwtBounds;
        private Rectangle popupNativeBounds;
        private Rectangle menuScreenBounds;
        private Rectangle itemScreenBounds;
        private Point itemLocationWithinMenu;
        private Point pointerClickScreen;
        private int placementErrorX;
        private int placementErrorY;
        private int placementError = Integer.MAX_VALUE;
        private long shownNanos;
        private long persistenceMillis;
        private boolean menuShowingAtPersistenceCheckpoint;
        private boolean menuShowingBeforePointer;
        private boolean menuShowingAfterPointer;
        private boolean menuShowingAfterFallback;
        private boolean pointerSelected;
        private String pointerSelectedText;
        private boolean doClickFallbackSelected;
        private String doClickFallbackText;
        private int frameCorrectionCountBefore;
        private int frameCorrectionCountAfter;
        private int frameCorrectionCount;

        private ScenarioEvidence(final String name) {
            this.name = name;
        }
    }

    private static final class CountingFrame extends JFrame {
        private static final long serialVersionUID = 1L;
        private volatile boolean tracking;
        private volatile int rectangleSetBoundsCalls;

        @Override
        public void setBounds(final Rectangle bounds) {
            if (tracking) {
                rectangleSetBoundsCalls++;
            }
            super.setBounds(bounds);
        }

        private void setTracking(final boolean tracking) {
            this.tracking = tracking;
        }

        private int getRectangleSetBoundsCalls() {
            return rectangleSetBoundsCalls;
        }
    }
}
