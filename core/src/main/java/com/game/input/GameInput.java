package com.game.input;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.game.GameConfig;

/** Collects keyboard, mouse, controller, and touch input behind game-level actions. */
public final class GameInput {
    private static final int MAX_POINTERS = 10;
    private static final float AXIS_DEAD_ZONE = 0.45f;

    private final Vector2[] pointerPositions = new Vector2[MAX_POINTERS];
    private final boolean[] pointerDown = new boolean[MAX_POINTERS];
    private final boolean[] pointerJustDown = new boolean[MAX_POINTERS];
    private final boolean[] pointerInsideViewport = new boolean[MAX_POINTERS];
    private final InputDeviceTracker inputDeviceTracker = new InputDeviceTracker();

    private Controller controller;
    private boolean controllerUp;
    private boolean controllerDown;
    private boolean controllerLeft;
    private boolean controllerRight;
    private boolean controllerConfirm;
    private boolean controllerBack;
    private boolean controllerPause;
    private boolean controllerHelp;
    private boolean controllerSwim;
    private boolean controllerShoot;
    private boolean controllerDash;

    private boolean previousControllerUp;
    private boolean previousControllerDown;
    private boolean previousControllerLeft;
    private boolean previousControllerRight;
    private boolean previousControllerConfirm;
    private boolean previousControllerBack;
    private boolean previousControllerPause;
    private boolean previousControllerHelp;
    private boolean previousControllerShoot;
    private boolean previousControllerDash;

    public GameInput() {
        for (int i = 0; i < pointerPositions.length; i++) {
            pointerPositions[i] = new Vector2();
        }
    }

    /** Must be called exactly once near the start of each rendered frame. */
    public void update(Viewport viewport) {
        updatePointers(viewport);
        updateController();
        updateActiveInputDevice();
    }

    public boolean menuUpJustPressed() {
        return keyJustPressed(Input.Keys.UP, Input.Keys.W)
            || controllerUp && !previousControllerUp;
    }

    public boolean menuDownJustPressed() {
        return keyJustPressed(Input.Keys.DOWN, Input.Keys.S)
            || controllerDown && !previousControllerDown;
    }

    public boolean menuLeftJustPressed() {
        return keyJustPressed(Input.Keys.LEFT, Input.Keys.A)
            || controllerLeft && !previousControllerLeft;
    }

    public boolean menuRightJustPressed() {
        return keyJustPressed(Input.Keys.RIGHT, Input.Keys.D)
            || controllerRight && !previousControllerRight;
    }

    public boolean confirmJustPressed() {
        return keyJustPressed(Input.Keys.ENTER, Input.Keys.SPACE)
            || controllerConfirm && !previousControllerConfirm;
    }

    public boolean backJustPressed() {
        return keyJustPressed(Input.Keys.ESCAPE, Input.Keys.BACK)
            || controllerBack && !previousControllerBack;
    }

    public boolean pauseJustPressed() {
        return keyJustPressed(Input.Keys.P, Input.Keys.ESCAPE, Input.Keys.BACK)
            || controllerPause && !previousControllerPause;
    }

    public boolean helpJustPressed() {
        return keyJustPressed(Input.Keys.T, Input.Keys.F1)
            || controllerHelp && !previousControllerHelp;
    }

    public boolean swimPressed() {
        boolean keyboard = Gdx.input.isKeyPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyPressed(Input.Keys.W)
            || Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean mouse = !isMobile() && Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        return keyboard || mouse || controllerSwim;
    }

    public boolean shootJustPressed() {
        boolean keyboard = keyJustPressed(Input.Keys.Z, Input.Keys.X);
        boolean mouse = !isMobile() && Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);
        return keyboard || mouse || controllerShoot && !previousControllerShoot;
    }

    public boolean dashJustPressed() {
        return keyJustPressed(Input.Keys.C)
            || controllerDash && !previousControllerDash;
    }

    public boolean pointerJustPressed(Rectangle bounds) {
        for (int i = 0; i < MAX_POINTERS; i++) {
            if (pointerJustDown[i] && pointerInsideViewport[i] && bounds.contains(pointerPositions[i])) {
                return true;
            }
        }
        return false;
    }

    public boolean pointerPressed(Rectangle bounds) {
        for (int i = 0; i < MAX_POINTERS; i++) {
            if (pointerDown[i] && pointerInsideViewport[i] && bounds.contains(pointerPositions[i])) {
                return true;
            }
        }
        return false;
    }

    public boolean pointerOver(Rectangle bounds) {
        if (isMobile()) {
            return pointerPressed(bounds);
        }
        return pointerInsideViewport[0] && bounds.contains(pointerPositions[0]);
    }

    public boolean isMobile() {
        Application.ApplicationType type = Gdx.app.getType();
        return type == Application.ApplicationType.Android || type == Application.ApplicationType.iOS;
    }

    public boolean hasController() {
        return controller != null && controller.isConnected();
    }

    /** True only when a connected controller supplied the most recent input. */
    public boolean usingController() {
        return hasController() && inputDeviceTracker.is(InputDeviceTracker.Device.CONTROLLER);
    }

    /** True when touch supplied the most recent input (the default on mobile). */
    public boolean usingTouch() {
        return inputDeviceTracker.is(InputDeviceTracker.Device.TOUCH);
    }

    public void vibrateController(int durationMillis, float strength) {
        if (hasController() && controller.canVibrate()) {
            controller.startVibration(durationMillis, strength);
        }
    }

    private void updatePointers(Viewport viewport) {
        int screenX = viewport.getScreenX();
        int screenY = viewport.getScreenY();
        int screenWidth = viewport.getScreenWidth();
        int screenHeight = viewport.getScreenHeight();
        boolean mouseJustPressed = !isMobile()
            && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);

        for (int i = 0; i < MAX_POINTERS; i++) {
            boolean wasDown = pointerDown[i];
            boolean isDown = Gdx.input.isTouched(i);
            pointerDown[i] = isDown;
            pointerJustDown[i] = (isDown && !wasDown) || (i == 0 && mouseJustPressed);

            float localX = Gdx.input.getX(i) - screenX;
            float localY = Gdx.graphics.getHeight() - Gdx.input.getY(i) - screenY;
            pointerInsideViewport[i] = localX >= 0f && localY >= 0f
                && localX <= screenWidth && localY <= screenHeight;
            pointerPositions[i].set(
                screenWidth == 0 ? 0f : localX * GameConfig.WORLD_WIDTH / screenWidth,
                screenHeight == 0 ? 0f : localY * GameConfig.WORLD_HEIGHT / screenHeight
            );
        }
    }

    private void updateController() {
        previousControllerUp = controllerUp;
        previousControllerDown = controllerDown;
        previousControllerLeft = controllerLeft;
        previousControllerRight = controllerRight;
        previousControllerConfirm = controllerConfirm;
        previousControllerBack = controllerBack;
        previousControllerPause = controllerPause;
        previousControllerHelp = controllerHelp;
        previousControllerShoot = controllerShoot;
        previousControllerDash = controllerDash;

        selectController();
        if (controller == null) {
            controllerUp = false;
            controllerDown = false;
            controllerLeft = false;
            controllerRight = false;
            controllerConfirm = false;
            controllerBack = false;
            controllerPause = false;
            controllerHelp = false;
            controllerSwim = false;
            controllerShoot = false;
            controllerDash = false;
            return;
        }

        ControllerMapping mapping = controller.getMapping();
        float horizontal = axis(mapping.axisLeftX);
        float vertical = axis(mapping.axisLeftY);
        controllerUp = vertical < -AXIS_DEAD_ZONE || button(mapping.buttonDpadUp);
        controllerDown = vertical > AXIS_DEAD_ZONE || button(mapping.buttonDpadDown);
        controllerLeft = horizontal < -AXIS_DEAD_ZONE || button(mapping.buttonDpadLeft);
        controllerRight = horizontal > AXIS_DEAD_ZONE || button(mapping.buttonDpadRight);
        controllerConfirm = button(mapping.buttonA);
        controllerBack = button(mapping.buttonB) || button(mapping.buttonBack);
        controllerPause = button(mapping.buttonStart);
        controllerHelp = button(mapping.buttonY);
        controllerSwim = controllerUp || button(mapping.buttonA);
        controllerShoot = button(mapping.buttonX) || button(mapping.buttonB)
            || button(mapping.buttonR1) || button(mapping.buttonR2);
        controllerDash = button(mapping.buttonL1);
    }

    private void updateActiveInputDevice() {
        boolean mobile = isMobile();
        boolean pointerActive = false;
        for (boolean justDown : pointerJustDown) {
            if (justDown) {
                pointerActive = true;
                break;
            }
        }

        boolean mouseActive = !mobile && (pointerActive
            || Gdx.input.getDeltaX() != 0 || Gdx.input.getDeltaY() != 0
            || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)
            || Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)
            || Gdx.input.isButtonJustPressed(Input.Buttons.MIDDLE));
        boolean keyboardMouseActive = Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)
            || mouseActive;
        boolean controllerActive = controllerUp && !previousControllerUp
            || controllerDown && !previousControllerDown
            || controllerLeft && !previousControllerLeft
            || controllerRight && !previousControllerRight
            || controllerConfirm && !previousControllerConfirm
            || controllerBack && !previousControllerBack
            || controllerPause && !previousControllerPause
            || controllerHelp && !previousControllerHelp
            || controllerShoot && !previousControllerShoot
            || controllerDash && !previousControllerDash;

        inputDeviceTracker.update(mobile, keyboardMouseActive,
            controllerActive, mobile && pointerActive);
    }

    private void selectController() {
        if (controller != null && controller.isConnected()) {
            return;
        }
        controller = null;
        Array<Controller> controllers = Controllers.getControllers();
        for (Controller candidate : controllers) {
            if (candidate.isConnected()) {
                controller = candidate;
                break;
            }
        }
    }

    private boolean button(int buttonCode) {
        return buttonCode != ControllerMapping.UNDEFINED
            && buttonCode >= controller.getMinButtonIndex()
            && buttonCode <= controller.getMaxButtonIndex()
            && controller.getButton(buttonCode);
    }

    private float axis(int axisCode) {
        if (axisCode == ControllerMapping.UNDEFINED
            || axisCode < 0 || axisCode >= controller.getAxisCount()) {
            return 0f;
        }
        return controller.getAxis(axisCode);
    }

    private boolean keyJustPressed(int... keys) {
        for (int key : keys) {
            if (Gdx.input.isKeyJustPressed(key)) {
                return true;
            }
        }
        return false;
    }
}
