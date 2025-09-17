package com.monstrous.dungeon.input;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

/** camera controller to follow a third person player */
public class OrbitCameraController extends InputAdapter {

    private final Camera camera;
    private final Vector3 offset = new Vector3();
    private float distance = 5f;

    public OrbitCameraController(Camera camera ) {
        this.camera = camera;
        offset.set(0, 2, -3);
    }

    public void update ( Vector3 playerPosition, Vector3 viewDirection ) {

        camera.position.set(playerPosition);

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
}
