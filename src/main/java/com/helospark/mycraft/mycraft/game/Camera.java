package com.helospark.mycraft.mycraft.game;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.Input;
import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.mathutils.BoundingBox;
import com.helospark.mycraft.mycraft.mathutils.CullingResult;
import com.helospark.mycraft.mycraft.mathutils.Frustum;
import com.helospark.mycraft.mycraft.messages.GenericIntMessage;
import com.helospark.mycraft.mycraft.messages.GenericMessage;
import com.helospark.mycraft.mycraft.messages.MouseMotionMessage;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.transformation.Transformation;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class Camera implements MessageListener {
    private Vector3f position = new Vector3f(0, 0, 0);
    private Vector3f rotation = new Vector3f(0, 0, 0);
    private final Vector3f offset = new Vector3f(0.5f, 1.5f, 0.5f);
    private final Transformation transformation;
    private final float fieldOfView;
    private final Frustum frustum;
    private float yaw, pitch;
    float moveSpeed, lookSpeed;
    boolean isMouseLocked, isCameraMoved;
    MessageHandler messager;

    public Camera(float fieldOfView) {
        ApplicationContext context = Singleton.getInstance().getContext();
        messager = context.getBean(MessageHandler.class);
        messager.registerListener(this, MessageTypes.TURN_ON_CURSOR);
        messager.registerListener(this, MessageTypes.TURN_OFF_CURSOR);
        transformation = context.getBean(Transformation.class);
        this.fieldOfView = fieldOfView;
        frustum = new Frustum();
        position = new Vector3f(0.0f, 0.0f, 0.0f);
        pitch = 0.0f;
        yaw = 0.0f;
        moveSpeed = 0.1f;
        lookSpeed = 0.2f;
        isMouseLocked = false;
        isCameraMoved = false;
        // messager.registerListener(this, MessageTypes.MOUSE_MOTION_MESSAGE);
    }

    public void updateOpenglTransformation() {
        int previousMatrixMode = transformation.getCurrentMatrixMode();
        transformation.setMatrixMode(Transformation.MODEL_MATRIX);
        transformation.loadIdentity();
        transformation.setMatrixMode(Transformation.VIEW_MATRIX);
        transformation.loadIdentity();
        transformation.translate(new Vector3f(-position.x, -position.y, -position.z));
        transformation.rotateZ((float) Math.toRadians(-rotation.z));
        transformation.rotateY((float) Math.toRadians(-rotation.y));
        transformation.rotateX((float) Math.toRadians(-rotation.x));

        transformation.setMatrixMode(Transformation.PROJECTION_MATRIX);
        transformation.loadIdentity();
        transformation.perspective(fieldOfView, (float) Display.getWidth() / Display.getHeight(),
                0.1f, 1000.0f);
        frustum.extractPlanes(this);
        transformation.setMatrixMode(previousMatrixMode);
    }

    void lockCamera() {
        if (pitch > 90.0f)
            pitch = 90.0f;
        if (pitch < -90.0f)
            pitch = -90.0f;
        if (yaw < 0.0f)
            yaw += 360.0f;
        if (yaw > 360.0f)
            yaw -= 360.0f;
    }

    void moveCamera(float dir) {
        float rad = (float) ((yaw + dir) * Math.PI / 180.0f);
        position.x -= (float) Math.sin(rad) * moveSpeed;
        position.z -= (float) Math.cos(rad) * moveSpeed;
        messager.sendMessage(new GenericMessage(MessageTypes.ACTOR_MOVED,
                Message.MESSAGE_TARGET_ANYONE));
    }

    void moveCameraUp(float dir) {
        float rad = (float) ((pitch + dir) * Math.PI / 180.0f);
        position.y += Math.sin(rad) * moveSpeed;
    }

    void update() {
        if (isMouseLocked) {
            int screenCenterX = Display.getWidth() / 2;
            int screenCenterY = Display.getHeight() / 2;

            int newCursorPositionX = Mouse.getX();
            int newCursorPositionY = Mouse.getY();
            yaw += lookSpeed * (screenCenterX - newCursorPositionX);
            pitch -= lookSpeed * (screenCenterY - newCursorPositionY);
            lockCamera();
            Mouse.setCursorPosition(screenCenterX, screenCenterY);
            isCameraMoved = false;
            if (Keyboard.isKeyDown(Input.KEY_W)) {
                isCameraMoved = true;
                if (pitch != 90 && pitch != -90)
                    moveCamera(0.0f);
                // moveCameraUp(0.0f);
            } else if (Keyboard.isKeyDown(Input.KEY_S)) {
                isCameraMoved = true;
                if (pitch != 90.0f && pitch != -90.0f)
                    moveCamera(180.0f);
                // moveCameraUp(180.0f);
            }
            if (Keyboard.isKeyDown(Input.KEY_A)) {
                isCameraMoved = true;
                moveCamera(90.0f);
            } else if (Keyboard.isKeyDown(Input.KEY_D)) {
                isCameraMoved = true;
                moveCamera(270.0f);
            }
        }
        if (Keyboard.isKeyDown(Input.KEY_F1)) {
            if (isMouseLocked) {
                Mouse.setGrabbed(false);
                messager.sendMessage(new GenericIntMessage(MessageTypes.CURSOR_SHOW_STATUS,
                        Message.MESSAGE_TARGET_ANYONE, 1));
            }
            isMouseLocked = false;
        } else if (Keyboard.isKeyDown(Input.KEY_SPACE)) {
            if (!isMouseLocked) {
                Mouse.setGrabbed(true);
                messager.sendMessage(new GenericIntMessage(MessageTypes.CURSOR_SHOW_STATUS,
                        Message.MESSAGE_TARGET_ANYONE, 0));
            }
            isMouseLocked = true;
        }
        rotation.x = pitch;
        rotation.y = yaw;
    }

    float getPitch() {
        return pitch;
    }

    float getYaw() {
        return yaw;
    }

    float getMoveSpeed() {
        return moveSpeed;
    }

    float getMousevel() {
        return lookSpeed;
    }

    boolean isMouseLocked() {
        return isMouseLocked;
    }

    void lookAt(float pitch, float yaw) {
        this.pitch = pitch;
        this.yaw = yaw;
    }

    void setSpeed(float mv, float mov) {
        moveSpeed = mv;
        lookSpeed = mov;
    }

    boolean isMoved() {
        return isCameraMoved;
    }

    public CullingResult containsBox(BoundingBox boundingBox) {
        return CullingResult.FULLY_IN;
    }

    public Frustum getFrustum() {
        return frustum;
    }

    public Vector3f getPosition() {
        return new Vector3f(position.x - offset.x, position.y - offset.y, position.z - offset.z);
    }

    public void setPosition(Vector3f position) {
        this.position = new Vector3f(position.x + offset.x, position.y + offset.y, position.z
                + offset.z);
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = rotation;
    }

    public Vector3f getOrientation() {
        return new Vector3f(rotation);
    }

    @Override
    public boolean receiveMessage(Message message) {
        if (message.getType() == MessageTypes.TURN_ON_CURSOR) {
            isMouseLocked = false;
            Mouse.setGrabbed(false);
        } else if (message.getType() == MessageTypes.TURN_OFF_CURSOR) {
            isMouseLocked = true;
            Mouse.setGrabbed(true);
        } else if (message.getType() == MessageTypes.MOUSE_MOTION_MESSAGE) {
            MouseMotionMessage mouseMessage = (MouseMotionMessage) message;
            // newCursorPositionX = mouseMessage.getNewX();
            // newCursorPositionY = mouseMessage.getNewY();
            // System.out.println("GOT: " + newCursorPositionX + " " +
            // newCursorPositionY);
        }
        return false;
    }
}
