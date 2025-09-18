package com.monstrous.dungeon;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.monstrous.dungeon.map.Room;
import com.monstrous.dungeon.render.DungeonScenes;



/** Manage torches as a set of point lights.
* They are enabled/disabled and positioned to match the torches of the current room.
*/
public class TorchLights {

    private int roomId;
    private Array<PointLight> lights;
    private float time;

    public TorchLights(Environment environment) {
        roomId = -1;
        lights = new Array<>();
        for(int i = 0; i < DungeonScenes.MAX_TORCHES; i++){
            PointLight pointLight = new PointLight();
            pointLight.position.set(0,1,0);
            pointLight.color.set(Color.ORANGE);
            //pointLight.range = 0.5f;
            pointLight.intensity = 0f;  // off by default
            lights.add( pointLight );
            environment.add( pointLight );
        }
    }

    public void update(float delta, Room room){
        if(room == null && roomId == -1)
            return;
        if(room != null && room.id == roomId) {
            flicker(delta);
            return;
        }
        roomId = (room == null ? -1 : room.id);
        for(PointLight light : lights) {
            light.intensity = 0;
        }
        if(room == null)
            return;

        int index = 0;
        for(Vector3 pos : room.torchPositions){
            PointLight pointLight = lights.get(index);
            index++;
            pointLight.position.set(pos);
            pointLight.intensity = 4f;
        }
    }

    private void flicker(float delta ){
        time += delta;
        for(int index = 0; index < lights.size; index++){
            PointLight light = lights.get(index);
            if(light.intensity > 0)
                light.intensity = 10f - 3.5f* (float)Math.sin(index + 0.5f*time )*(float)Math.cos(3.1f*time);
        }
    }
}
