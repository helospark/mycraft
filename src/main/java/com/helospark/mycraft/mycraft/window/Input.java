package com.helospark.mycraft.mycraft.window;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.messages.GenericIntMessage;
import com.helospark.mycraft.mycraft.messages.KeyStateChangedMessage;
import com.helospark.mycraft.mycraft.messages.MouseMotionMessage;
import com.helospark.mycraft.mycraft.messages.MouseStateChangeMessage;
import com.helospark.mycraft.mycraft.services.GlobalParameters;
import com.helospark.mycraft.mycraft.singleton.Singleton;

@Service
public class Input {

    private static final int LEFT_MOUSE_BUTTON = 0;
    MessageHandler messager;
    private final int lastMouseX = -1;
    private final int lastMouseY = -1;
    private final GlobalParameters globalParameters;

    public Input() {
        ApplicationContext context = Singleton.getInstance().getContext();
        messager = context.getBean(MessageHandler.class);
        globalParameters = context.getBean(GlobalParameters.class);
    }

    public void update() {
        updateKeyMessages();
        updateMouseMessages();
    }

    private void updateMouseMessages() {
        while (Mouse.next()) {
            int change;
            int button = Mouse.getEventButton();
            if (button >= 0) {
                if (Mouse.getEventButtonState()) {
                    change = MouseStateChangeMessage.MOUSE_BUTTON_PRESSED;
                } else {
                    change = MouseStateChangeMessage.MOUSE_BUTTON_RELEASED;
                }
                int resolvedButton = 0;
                if (button == 0) {
                    resolvedButton = MouseStateChangeMessage.MOUSE_BUTTON_LEFT;
                } else if (button == 1) {
                    resolvedButton = MouseStateChangeMessage.MOUSE_BUTTON_RIGHT;
                } else {
                    resolvedButton = -1;
                }

                MouseStateChangeMessage mouseMessage = new MouseStateChangeMessage(
                        MessageTypes.MOUSE_STATE_CHANGED_MESSAGE, Message.MESSAGE_TARGET_ANYONE,
                        Mouse.getX(), globalParameters.initialWindowHeight - Mouse.getY(),
                        change, resolvedButton);
                messager.sendMessage(mouseMessage);
            }
        }
        int scrollAmount = Mouse.getDWheel();

        if (scrollAmount != 0) {
            int direction = 0;
            if (scrollAmount < 0) {
                direction = -1;
            } else {
                direction = 1;
            }
            GenericIntMessage mouseMessage = new GenericIntMessage(
                    MessageTypes.MOUSE_SCROLLED_MESSAGE, Message.MESSAGE_TARGET_ANYONE, direction);
            messager.sendMessage(mouseMessage);
        }

        int newX = Mouse.getX();
        int newY = globalParameters.initialWindowHeight - Mouse.getY();

        int lastX = lastMouseX;
        int lastY = lastMouseY;

        if (lastX == -1) {
            lastX = newX;
            lastY = newY;
        }

        boolean wasDragged = Mouse.isButtonDown(LEFT_MOUSE_BUTTON);

        MouseMotionMessage mouseMotionMessage = new MouseMotionMessage(
                MessageTypes.MOUSE_MOTION_MESSAGE, Message.MESSAGE_TARGET_ANYONE, lastX, lastY,
                newX, newY, wasDragged);
        messager.sendImmediateMessage(mouseMotionMessage);
        lastX = newX;
        lastY = newY;
    }

    private void updateKeyMessages() {
        while (Keyboard.next()) {
            int keyCode = Keyboard.getEventKey();
            boolean isPressed = Keyboard.getEventKeyState();

            int type = isPressed ? KeyStateChangedMessage.KEY_PRESSED
                    : KeyStateChangedMessage.KEY_RELEASED;
            messager.sendMessage(new KeyStateChangedMessage(MessageTypes.KEY_STATE_CHANGE_MESSAGE,
                    Message.MESSAGE_TARGET_ANYONE, keyCode, type, 'x'));
        }
    }
}
