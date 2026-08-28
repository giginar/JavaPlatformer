package com.game.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class InputDeviceTrackerTest {
    @Test
    void desktopDefaultsToKeyboardEvenWhenControllerIsMerelyConnected() {
        InputDeviceTracker tracker = new InputDeviceTracker();

        tracker.update(false, false, false, false);

        assertTrue(tracker.is(InputDeviceTracker.Device.KEYBOARD_MOUSE));
    }

    @Test
    void mobileDefaultsToTouch() {
        InputDeviceTracker tracker = new InputDeviceTracker();

        tracker.update(true, false, false, false);

        assertTrue(tracker.is(InputDeviceTracker.Device.TOUCH));
    }

    @Test
    void controllerBecomesActiveOnlyAfterControllerInput() {
        InputDeviceTracker tracker = new InputDeviceTracker();

        tracker.update(false, false, false, false);
        tracker.update(false, false, true, false);

        assertTrue(tracker.is(InputDeviceTracker.Device.CONTROLLER));
    }

    @Test
    void keyboardTakesTheHintBackFromAConnectedController() {
        InputDeviceTracker tracker = new InputDeviceTracker();

        tracker.update(false, false, true, false);
        tracker.update(false, true, false, false);

        assertTrue(tracker.is(InputDeviceTracker.Device.KEYBOARD_MOUSE));
    }

    @Test
    void touchTakesTheHintBackFromAControllerOnMobile() {
        InputDeviceTracker tracker = new InputDeviceTracker();

        tracker.update(true, false, true, false);
        tracker.update(true, false, false, true);

        assertTrue(tracker.is(InputDeviceTracker.Device.TOUCH));
    }

    @Test
    void localKeyboardOrTouchWinsIfInputsArriveInTheSameFrame() {
        InputDeviceTracker desktopTracker = new InputDeviceTracker();
        InputDeviceTracker mobileTracker = new InputDeviceTracker();

        desktopTracker.update(false, true, true, false);
        mobileTracker.update(true, false, true, true);

        assertTrue(desktopTracker.is(InputDeviceTracker.Device.KEYBOARD_MOUSE));
        assertTrue(mobileTracker.is(InputDeviceTracker.Device.TOUCH));
    }
}
