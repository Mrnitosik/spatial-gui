package org.tastytrash.spatialGUI.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import org.tastytrash.spatialGUI.util.MouseHandlerUtil;

public final class ControlifyCompat {
    private static final String CTRLIFY = "dev.isxander.controlify.";

    public static boolean inVirtualMove;

    private static Method renderVirtualMouse;
    private static Method getController, getMouseHandler, isVirtualMouseEnabled, getInput, getSettings;
    private static Method rawStateNow, getAxisState;
    private static Object axisRight, axisLeft, axisDown, axisUp;
    private static Field sens, hSens, vSens, hInvert, vInvert, deadzone;

    private static boolean bound;

    public static final class LookSettings {
        public float h = 1f, v = 1f;
        public boolean invH, invV;
        public float deadzone = 0.15f;

        public double shape(double raw) {
            double a = Math.abs(raw);
            if (a <= deadzone) return 0;
            double n = Math.min(1.0, (a - deadzone) / (1.0 - deadzone));
            return Math.signum(raw) * n * n;
        }

        public double[] stick(double rx, double ry) {
            return new double[]{shape(rx) * h * (invH ? -1 : 1), shape(ry) * v * (invV ? -1 : 1)};
        }
    }

    public static boolean isVirtualMouseActive() {
        if (!bound) bind();
        if (isVirtualMouseEnabled == null) return false;
        try {
            Object instance = Class.forName(CTRLIFY + "Controlify").getMethod("instance").invoke(null);
            return (boolean) isVirtualMouseEnabled.invoke(getMouseHandler.invoke(instance));
        } catch (Throwable t) {
            return false;
        }
    }

    public static void renderVirtualMouse(Object graphics, float partialTick) {
        if (!bound) bind();
        if (getMouseHandler == null) return;
        try {
            Object instance = Class.forName(CTRLIFY + "Controlify").getMethod("instance").invoke(null);
            Object handler = getMouseHandler.invoke(instance);
            if (handler == null) return;
            if (renderVirtualMouse == null) {
                for (Method m : handler.getClass().getMethods()) {
                    if (m.getName().equals("renderVirtualMouse")) { renderVirtualMouse = m; break; }
                }
                if (renderVirtualMouse == null) return;
            }
            Class<?>[] types = renderVirtualMouse.getParameterTypes();
            Object[] args = new Object[types.length];
            for (int i = 0; i < types.length; i++) {
                Class<?> t = types[i];
                if (t.isInstance(graphics)) args[i] = graphics;
                else if (t == float.class) args[i] = partialTick;
                else if (t == double.class) args[i] = (double) partialTick;
                else return;
            }
            renderVirtualMouse.invoke(handler, args);
        } catch (Throwable ignored) {}
    }

    public static LookSettings readLookSettings() {
        LookSettings s = new LookSettings();
        if (!bound) bind();
        if (hSens == null) return s;
        try {
            Object ctrl = controller();
            if (ctrl == null) return s;
            Object input = ((Optional<?>) getInput.invoke(ctrl)).orElse(null);
            if (input == null) return s;
            Object sens = ControlifyCompat.sens.get(getSettings.invoke(input));
            s.h = hSens.getFloat(sens);
            s.v = vSens.getFloat(sens);
            s.invH = hInvert != null && hInvert.getBoolean(sens);
            s.invV = vInvert.getBoolean(sens);
            s.deadzone = deadzone.getFloat(sens);
        } catch (Throwable ignored) {}
        return s;
    }

    public static double[] pollLookStick(LookSettings s) {
        if (!bound) bind();
        if (rawStateNow == null || getAxisState == null || axisRight == null) return null;
        try {
            Object ctrl = controller();
            if (ctrl == null) return null;
            Object input = ((Optional<?>) getInput.invoke(ctrl)).orElse(null);
            if (input == null) return null;
            Object raw = rawStateNow.invoke(input);

            double rx = axisRaw(raw, axisRight) - axisRaw(raw, axisLeft);
            double ry = axisRaw(raw, axisDown)  - axisRaw(raw, axisUp);
            return s.stick(rx, ry);
        } catch (Throwable t) {
            return null;
        }
    }

    private static double axisRaw(Object state, Object id) throws Exception {
        return ((Number) getAxisState.invoke(state, id)).doubleValue();
    }

    private static Object controller() throws Exception {
        Object instance = Class.forName(CTRLIFY + "Controlify").getMethod("instance").invoke(null);
        return ((Optional<?>) getController.invoke(instance)).orElse(null);
    }

    private static Method findMethod(Class<?> c, String... names) throws NoSuchMethodException {
        for (String n : names) {
            try { return c.getMethod(n); } catch (NoSuchMethodException ignored) {}
        }
        throw new NoSuchMethodException(c.getName() + " " + String.join("/", names));
    }

    private static void bind() {
        bound = true;

        try {
            Class<?> c = Class.forName(CTRLIFY + "Controlify");
            getController = findMethod(c, "getCurrentController", "currentController");
            getMouseHandler = findMethod(c, "virtualMouseHandler", "getVirtualMouseHandler");
            isVirtualMouseEnabled = findMethod(Class.forName(CTRLIFY + "virtualmouse.VirtualMouseHandler"), "isVirtualMouseEnabled");
        } catch (Throwable ignored) {}

        try {
            Class<?> ce = Class.forName(CTRLIFY + "controller.ControllerEntity");
            getInput = ce.getMethod("input");
            Class<?> ic = Class.forName(CTRLIFY + "controller.input.InputComponent");
            getSettings = ic.getMethod("settings");
            Class<?> is = Class.forName(CTRLIFY + "config.settings.profile.InputSettings");
            sens = is.getField("sensitivity");
            Class<?> ss = Class.forName(CTRLIFY + "config.settings.profile.InputSettings$SensitivitySettings");
            hSens = ss.getField("hLookSensitivity");
            vSens = ss.getField("vLookSensitivity");
            try { hInvert = ss.getField("hLookInvert"); } catch (NoSuchFieldException ignored) {}
            vInvert = ss.getField("vLookInvert");
            deadzone = ss.getField("defaultDeadzone");
        } catch (Throwable ignored) {}

        try {
            Class<?> ic = Class.forName(CTRLIFY + "controller.input.InputComponent");
            rawStateNow = ic.getMethod("rawStateNow");
            Class<?> stateClass = Class.forName(CTRLIFY + "controller.input.ControllerStateView");
            for (Method m : stateClass.getMethods()) {
                if (m.getName().equals("getAxisState") && m.getParameterCount() == 1) {
                    getAxisState = m;
                    break;
                }
            }
            if (getAxisState == null) throw new NoSuchMethodException("getAxisState");
            Class<?> gamepad = Class.forName(CTRLIFY + "controller.input.GamepadInputs");
            axisRight = gamepad.getField("RIGHT_STICK_AXIS_RIGHT").get(null);
            axisLeft = gamepad.getField("RIGHT_STICK_AXIS_LEFT").get(null);
            axisDown = gamepad.getField("RIGHT_STICK_AXIS_DOWN").get(null);
            axisUp = gamepad.getField("RIGHT_STICK_AXIS_UP").get(null);
        } catch (Throwable ignored) {}
    }

    public static void handleFreeLook(boolean crosshairModeActive) {
        inVirtualMove = false;
        if (crosshairModeActive) {
            MouseHandlerUtil.addStickLook(pollLookStick(readLookSettings()));
        } else {
            MouseHandlerUtil.resetLookClock();
        }
    }
}