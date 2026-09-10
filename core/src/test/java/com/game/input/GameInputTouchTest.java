package com.game.input;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.ControllerManagerStub;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameInputTouchTest {
    private final Application previousApp = Gdx.app;
    private final Graphics previousGraphics = Gdx.graphics;
    private final Input previousInput = Gdx.input;
    private final String previousManager = Controllers.preferredManager;
    private final boolean[] down = new boolean[10];
    private final int[] x = new int[10];
    private final int[] y = new int[10];
    private final List<LifecycleListener> lifecycleListeners = new ArrayList<>();
    private final Viewport viewport = new Viewport() { };
    private final GameInput input = new GameInput();
    private Application.ApplicationType platform = Application.ApplicationType.Android;
    private int width = 1280;
    private int height = 720;
    private float density = 1f;
    private boolean keyboardActive;
    private int mouseButton = Input.Buttons.LEFT;
    private boolean mouseJustPressed;
    private boolean controllerConnected = true;
    private int controllerButton = -1;

    @BeforeEach
    void setUp() {
        Controllers.preferredManager = ControllerManagerStub.class.getName();
        Gdx.app = proxy(Application.class, (instance, method, args) -> switch (method.getName()) {
            case "getType" -> platform;
            case "hashCode" -> System.identityHashCode(instance);
            case "equals" -> instance == args[0];
            case "addLifecycleListener" -> { lifecycleListeners.add((LifecycleListener) args[0]); yield null; }
            case "log" -> null;
            default -> throw new UnsupportedOperationException(method.getName());
        });
        Gdx.graphics = proxy(Graphics.class, (instance, method, args) -> switch (method.getName()) {
            case "getWidth" -> width;
            case "getHeight" -> height;
            case "getDensity" -> density;
            default -> throw new UnsupportedOperationException(method.getName());
        });
        Gdx.input = proxy(Input.class, (instance, method, args) -> switch (method.getName()) {
            case "isTouched" -> down[(int) args[0]];
            case "getX" -> x[(int) args[0]];
            case "getY" -> y[(int) args[0]];
            case "isKeyJustPressed" -> keyboardActive && (int) args[0] == Input.Keys.ANY_KEY;
            case "isKeyPressed" -> false;
            case "isButtonPressed" -> down[0] && (int) args[0] == mouseButton;
            case "isButtonJustPressed" -> mouseJustPressed && (int) args[0] == mouseButton;
            case "getDeltaX", "getDeltaY" -> 0;
            default -> throw new UnsupportedOperationException(method.getName());
        });
        viewport.setScreenBounds(0, 0, width, height);
    }

    @AfterEach
    void tearDown() {
        lifecycleListeners.forEach(LifecycleListener::dispose);
        Controllers.preferredManager = previousManager;
        Gdx.app = previousApp;
        Gdx.graphics = previousGraphics;
        Gdx.input = previousInput;
    }

    @Test
    void bothEdgesWorkTogetherAtTopMiddleAndBottom() {
        for (int touchY : new int[]{0, 360, 719}) {
            touch(0, 1, touchY);
            touch(1, 1279, touchY);
            input.update(viewport);
            assertTrue(input.touchSwimPressed());
            assertTrue(input.touchFireJustPressed(null));
            down[0] = false;
            down[1] = false;
            input.update(viewport);
        }
    }

    @Test
    void swimmingStaysHeldWhileFireRequiresAnotherTap() {
        touch(0, 50, 350);
        touch(1, 1230, 350);
        input.update(viewport);
        assertTrue(input.touchSwimPressed());
        assertTrue(input.touchFireJustPressed(null));
        input.update(viewport);
        assertTrue(input.touchSwimPressed());
        assertFalse(input.touchFireJustPressed(null));
        down[1] = false;
        input.update(viewport);
        down[1] = true;
        input.update(viewport);
        assertTrue(input.touchSwimPressed());
        assertTrue(input.touchFireJustPressed(null));
        down[0] = false;
        input.update(viewport);
        assertFalse(input.touchSwimPressed());
    }

    @Test
    void widePhoneBlackSideBarsAcceptGameplayButNotMenuClicks() {
        width = 2400;
        height = 1080;
        density = 3f;
        viewport.setScreenBounds(240, 0, 1920, 1080);
        touch(0, 20, 500);
        touch(1, 2380, 500);
        input.update(viewport);
        assertTrue(input.touchSwimPressed());
        assertTrue(input.touchFireJustPressed(null));
        assertFalse(input.pointerJustPressed(new Rectangle(0, 0, 1280, 720)));
    }

    @Test
    void touchesInTheMiddleDoNotSwimOrFire() {
        touch(0, 640, 20);
        input.update(viewport);
        assertFalse(input.touchSwimPressed());
        assertFalse(input.touchFireJustPressed(null));
    }

    @Test
    void pauseTouchDoesNotFireAndAnotherFingerCanStillSwim() {
        Rectangle pause = new Rectangle(1192, 644, 76, 64);
        touch(0, 40, 360);
        touch(1, 1220, 40);
        input.update(viewport);
        assertTrue(input.pointerJustPressed(pause));
        assertFalse(input.touchFireJustPressed(pause));
        assertTrue(input.touchSwimPressed());
    }

    @Test
    void denseScreensKeepFingerSizedStripsWithoutConsumingTheMiddle() {
        density = 3f;
        touch(0, 250, 350);
        touch(1, 1030, 350);
        input.update(viewport);
        assertTrue(input.touchSwimPressed());
        assertTrue(input.touchFireJustPressed(null));
        x[0] = 400;
        x[1] = 880;
        input.update(viewport);
        assertFalse(input.touchSwimPressed());
    }

    @Test
    void menuTouchesStayBlockedUntilReleasedWithoutBlockingANewFinger() {
        touch(0, 50, 350);
        touch(1, 1230, 350);
        input.update(viewport);
        input.suppressGameplayTouchUntilRelease();
        assertFalse(input.touchSwimPressed());
        assertFalse(input.touchFireJustPressed(null));
        input.update(viewport);
        assertFalse(input.touchSwimPressed());
        touch(2, 60, 400);
        input.update(viewport);
        assertTrue(input.touchSwimPressed());
        down[1] = false;
        input.update(viewport);
        down[1] = true;
        input.update(viewport);
        assertTrue(input.touchFireJustPressed(null));
    }

    @Test
    void hintsFollowTheLastUsedDeviceOnMobileAndRecoverAfterGamepadDisconnects() {
        ControllerMapping mapping = connectController();
        input.update(viewport);
        assertTrue(input.usingTouch());
        assertFalse(input.usingController());

        controllerButton = mapping.buttonDpadUp;
        input.update(viewport);
        assertTrue(input.usingController());
        assertFalse(input.usingTouch());

        controllerConnected = false;
        input.update(viewport);
        assertFalse(input.usingController());
        assertTrue(input.usingTouch());

        keyboardActive = true;
        input.update(viewport);
        assertFalse(input.usingTouch());
        keyboardActive = false;
        touch(0, 50, 350);
        input.update(viewport);
        assertTrue(input.usingTouch());
    }

    @Test
    void desktopGamepadConnectionAloneDoesNotReplaceKeyboardHints() {
        platform = Application.ApplicationType.Desktop;
        ControllerMapping mapping = connectController();
        input.update(viewport);
        assertFalse(input.usingController());
        assertFalse(input.usingTouch());
        controllerButton = mapping.buttonDpadUp;
        input.update(viewport);
        assertTrue(input.usingController());
        keyboardActive = true;
        input.update(viewport);
        assertFalse(input.usingController());
        assertFalse(input.usingTouch());
    }

    private ControllerMapping connectController() {
        ControllerMapping mapping = new ControllerMapping(0, 1, 2, 3,
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15) { };
        Controller controller = proxy(Controller.class, (instance, method, args) -> switch (method.getName()) {
            case "isConnected" -> controllerConnected;
            case "getMapping" -> mapping;
            case "getAxisCount" -> 4;
            case "getAxis" -> 0f;
            case "getMinButtonIndex" -> 0;
            case "getMaxButtonIndex" -> 15;
            case "getButton" -> (int) args[0] == controllerButton;
            default -> throw new UnsupportedOperationException(method.getName());
        });
        Controllers.getControllers().add(controller);
        return mapping;
    }

    @Test
    void desktopMouseDoesNotActivateTouchStrips() {
        platform = Application.ApplicationType.Desktop;
        touch(0, 10, 350);
        touch(1, 1270, 350);
        input.update(viewport);
        assertFalse(input.touchSwimPressed());
        assertFalse(input.touchFireJustPressed(null));
    }

    @Test
    void menuButtonActivatesOnceOnReleaseAfterHolding() {
        Rectangle button = new Rectangle(900, 20, 180, 60);
        touch(0, 990, 670);
        input.update(viewport);
        assertTrue(input.buttonPressed(button));
        assertFalse(input.buttonJustReleased(button));
        input.update(viewport);
        assertTrue(input.buttonPressed(button));
        assertFalse(input.buttonJustReleased(button));
        down[0] = false;
        input.update(viewport);
        assertFalse(input.buttonPressed(button));
        assertTrue(input.buttonJustReleased(button));
        input.update(viewport);
        assertFalse(input.buttonJustReleased(button));
    }

    @Test
    void releasingOutsideCancelsAndDraggingToAnotherButtonDoesNotActivateIt() {
        Rectangle first = new Rectangle(900, 20, 180, 60);
        Rectangle second = new Rectangle(500, 20, 180, 60);
        touch(0, 990, 670);
        input.update(viewport);
        x[0] = 590;
        input.update(viewport);
        assertFalse(input.buttonPressed(first));
        assertFalse(input.buttonPressed(second));
        down[0] = false;
        input.update(viewport);
        assertFalse(input.buttonJustReleased(first));
        assertFalse(input.buttonJustReleased(second));
    }

    @Test
    void eachTouchRetainsItsOwnButtonGesture() {
        Rectangle left = new Rectangle(20, 20, 180, 60);
        Rectangle right = new Rectangle(900, 20, 180, 60);
        touch(0, 110, 670);
        touch(1, 990, 670);
        input.update(viewport);
        down[1] = false;
        input.update(viewport);
        assertTrue(input.buttonPressed(left));
        assertFalse(input.buttonJustReleased(left));
        assertTrue(input.buttonJustReleased(right));
        down[0] = false;
        input.update(viewport);
        assertTrue(input.buttonJustReleased(left));
        assertFalse(input.buttonJustReleased(right));
    }

    @Test
    void menuButtonRejectsGestureStartingInLetterbox() {
        width = 1600;
        viewport.setScreenBounds(160, 0, 1280, 720);
        Rectangle button = new Rectangle(0, 20, 180, 60);
        touch(0, 80, 670);
        input.update(viewport);
        x[0] = 250;
        input.update(viewport);
        assertFalse(input.buttonPressed(button));
        down[0] = false;
        input.update(viewport);
        assertFalse(input.buttonJustReleased(button));
    }

    @Test
    void desktopButtonsAcceptOnlyLeftMouseReleaseAndKeepHoverAfterRelease() {
        platform = Application.ApplicationType.Desktop;
        Rectangle button = new Rectangle(900, 20, 180, 60);
        mouseButton = Input.Buttons.RIGHT;
        touch(0, 990, 670);
        input.update(viewport);
        assertTrue(input.pointerOver(button));
        assertFalse(input.buttonPressed(button));
        down[0] = false;
        input.update(viewport);
        assertFalse(input.buttonJustReleased(button));
        mouseButton = Input.Buttons.LEFT;
        down[0] = true;
        input.update(viewport);
        assertTrue(input.buttonPressed(button));
        down[0] = false;
        input.update(viewport);
        assertTrue(input.buttonJustReleased(button));
        assertTrue(input.pointerOver(button));
    }

    @Test
    void quickMouseClickBetweenFramesStillActivatesButtonOnce() {
        platform = Application.ApplicationType.Desktop;
        Rectangle button = new Rectangle(900, 20, 180, 60);
        x[0] = 990;
        y[0] = 670;
        mouseJustPressed = true;
        input.update(viewport);
        assertFalse(input.buttonPressed(button));
        assertTrue(input.buttonJustReleased(button));
        mouseJustPressed = false;
        input.update(viewport);
        assertFalse(input.buttonJustReleased(button));
    }

    private void touch(int pointer, int screenX, int screenY) {
        down[pointer] = true;
        x[pointer] = screenX;
        y[pointer] = screenY;
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
