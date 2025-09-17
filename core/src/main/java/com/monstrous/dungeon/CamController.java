package com.monstrous.dungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;

// based on https://monstroussoftware.github.io/2023/11/02/Tutorial-3D-step2.html
//
public class CamController extends InputAdapter {
    public int forwardKey = Input.Keys.W;
    public int backwardKey = Input.Keys.S;
    public int strafeLeftKey = Input.Keys.A;
    public int strafeRightKey = Input.Keys.D;
    public int turnLeftKey = Input.Keys.Q;
    public int turnRightKey = Input.Keys.E;
    public int runShiftKey = Input.Keys.SHIFT_LEFT;

    protected final static float degreesPerPixel = 0.1f;

    protected final Camera camera;
    protected final IntIntMap keys = new IntIntMap();   // key state
    private float bobAngle = 0;
    protected final Vector3 fwdHorizontal = new Vector3();
    protected final Vector3 tmp = new Vector3();
    private final Vector3 tmp2 = new Vector3();
    private final Vector3 tmp3 = new Vector3();

    public CamController(Camera camera) {
        this.camera = camera;
    }

    @Override
    public boolean keyDown (int keycode) {
        //System.out.println("Pressed "+keycode);
        keys.put(keycode, keycode);
        return true;
    }

    @Override
    public boolean keyUp (int keycode) {
        //System.out.println("Released "+keycode);
        keys.remove(keycode, 0);
        return true;
    }

    public void update (float deltaTime) {

        float bobSpeed= 0;

        float moveSpeed = Settings.walkSpeed;
        if(keys.containsKey(runShiftKey))
            moveSpeed *= Settings.runFactor;

        if (keys.containsKey(forwardKey)) {
            moveForward(deltaTime * moveSpeed);
            bobSpeed = moveSpeed;
        }
        if (keys.containsKey(backwardKey)) {
            moveForward(-deltaTime * moveSpeed);
            bobSpeed = moveSpeed;
        }
        if (keys.containsKey(strafeLeftKey)) {
            strafe(-deltaTime * Settings.walkSpeed);
            bobSpeed = Settings.walkSpeed;
        }
        if (keys.containsKey(strafeRightKey)) {
            strafe(deltaTime * Settings.walkSpeed);
            bobSpeed = Settings.walkSpeed;
        }
        if (keys.containsKey(turnLeftKey))
            rotateView(deltaTime*Settings.turnSpeed );
        else if (keys.containsKey(turnRightKey))
            rotateView(-deltaTime*Settings.turnSpeed );

        camera.position.y = Settings.eyeHeight + bobHeight( bobSpeed, deltaTime); // apply some head bob if we're moving
        camera.update(true);
    }



    private void moveForward( float distance ){
        fwdHorizontal.set(camera.direction).y = 0;
        fwdHorizontal.nor();
        fwdHorizontal.scl(distance);
        camera.position.add(fwdHorizontal);
    }



    private void strafe( float distance ){
        fwdHorizontal.set(camera.direction).y = 0;
        fwdHorizontal.nor();
        tmp.set(fwdHorizontal).crs(camera.up).nor().scl(distance);
        camera.position.add(tmp);
    }

    private void rotateView(float deltaX) {
        camera.direction.rotate(camera.up, deltaX);
        camera.up.set(Vector3.Y);
    }



    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        // ignore big delta jump on start up
        if(Gdx.input.getDeltaX() == screenX && Gdx.input.getDeltaY() == screenY)
            return true;

        float deltaX = -Gdx.input.getDeltaX() * degreesPerPixel;
        float deltaY = -Gdx.input.getDeltaY() * degreesPerPixel;
        if (Settings.invertLook)
            deltaY = -deltaY;
        if (!Settings.freeLook) {    // keep camera movement in the horizontal plane
            deltaY = 0;
            camera.direction.y = 0;
        }
        rotateView(deltaX, deltaY);
        return true;
    }



    private void rotateView(float deltaX, float deltaY) {
        camera.direction.rotate(camera.up, deltaX);

        // avoid gimbal lock when looking straight up or down
        Vector3 oldPitchAxis = tmp.set(camera.direction).crs(camera.up).nor();
        Vector3 newDirection = tmp2.set(camera.direction).rotate(tmp, deltaY);
        Vector3 newPitchAxis = tmp3.set(tmp2).crs(camera.up);
        if (!newPitchAxis.hasOppositeDirection(oldPitchAxis))
            camera.direction.set(newDirection);
    }



    private float bobHeight(float speed, float deltaTime ) {
        if(Math.abs(speed) < 0.1f )
            return 0f;
        bobAngle += deltaTime * speed * 0.5f * Math.PI / Settings.headBobDuration;
        // move the head up and down in a sine wave
        return (float) (Settings.headBobHeight * Math.sin(bobAngle));
    }
}
