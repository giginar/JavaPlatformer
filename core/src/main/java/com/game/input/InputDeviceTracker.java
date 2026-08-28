package com.game.input;

/** Tracks the most recently used input family without depending on libGDX globals. */
final class InputDeviceTracker {
    enum Device {
        KEYBOARD_MOUSE,
        CONTROLLER,
        TOUCH
    }

    private Device activeDevice = Device.KEYBOARD_MOUSE;
    private boolean initialized;

    void update(boolean mobile, boolean keyboardMouseActive,
                boolean controllerActive, boolean touchActive) {
        if (!initialized) {
            activeDevice = mobile ? Device.TOUCH : Device.KEYBOARD_MOUSE;
            initialized = true;
        }

        if (controllerActive) {
            activeDevice = Device.CONTROLLER;
        }
        if (touchActive) {
            activeDevice = Device.TOUCH;
        }
        if (keyboardMouseActive) {
            activeDevice = Device.KEYBOARD_MOUSE;
        }
    }

    boolean is(Device device) {
        return activeDevice == device;
    }
}
