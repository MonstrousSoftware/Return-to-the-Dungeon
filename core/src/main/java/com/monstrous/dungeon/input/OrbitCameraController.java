package com.monstrous.dungeon.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.monstrous.dungeon.Settings;

/** camera controller to follow a third person player */
public class OrbitCameraController extends InputAdapter {

    protected final static float degreesPerPixel = 0.1f;

    private final Camera camera;
    private final Vector3 offset = new Vector3();
    private float distance = 5f;
    protected final Vector3 viewDirection;
    private final Vector3 tmp = new Vector3();
    private final Vector3 tmp2 = new Vector3();
    private final Vector3 tmp3 = new Vector3();

    public OrbitCameraController(Camera camera ) {
        this.camera = camera;
        offset.set(0, 2, -3);
        viewDirection = new Vector3(Vector3.Z);
    }

    public void update ( Vector3 playerPosition, Vector3 inViewDirection ) {

        camera.position.set(playerPosition);
        //Vector3 viewDirection = new Vector3(Vector3.Z);

        // offset of camera from player position
        offset.set(viewDirection).scl(-1);      // invert view direction
        offset.y = .5f; //Math.max(0, offset.y);             // but slightly from above
        offset.nor().scl(distance);                   // scale for camera distance
        camera.position.add(offset);

        camera.lookAt(playerPosition);
        camera.up.set(Vector3.Y);

        camera.update(true);
    }

    @Override
    public boolean scrolled (float amountX, float amountY) {
        return zoom(amountY );
    }

    private boolean zoom (float amount) {
        if(amount < 0 && distance < 5f)
            return false;
        if(amount > 0 && distance > 50f)
            return false;
        distance += amount;
        return true;
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
            viewDirection.y = 0;
        }
        rotateView(deltaX, deltaY);
        return true;
    }



    private void rotateView(float deltaX, float deltaY) {
        viewDirection.rotate(camera.up, deltaX);

        // avoid gimbal lock when looking straight up or down
        Vector3 oldPitchAxis = tmp.set(viewDirection).crs(camera.up).nor();
        Vector3 newDirection = tmp2.set(viewDirection).rotate(tmp, deltaY);
        Vector3 newPitchAxis = tmp3.set(tmp2).crs(camera.up);
        if (!newPitchAxis.hasOppositeDirection(oldPitchAxis))
            viewDirection.set(newDirection);
    }
}
