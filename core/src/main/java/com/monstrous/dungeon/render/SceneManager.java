package com.monstrous.dungeon.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.monstrous.gdx.webgpu.graphics.g3d.WgModelBatch;
import com.monstrous.gdx.webgpu.graphics.g3d.shaders.WgDefaultShader;
import com.monstrous.gdx.webgpu.graphics.utils.WgScreenUtils;

public class SceneManager implements Disposable {
    private Array<ModelInstance> instances;
    private final WgModelBatch modelBatch;
    public Environment environment;
    public Color fogColor;

    public SceneManager() {
        WgDefaultShader.Config config = new WgDefaultShader.Config();
        config.maxPointLights = 1+DungeonScenes.MAX_TORCHES;
        config.numBones = 48;
        modelBatch = new WgModelBatch(config);
        instances = new Array<>();
        fogColor = Color.BLACK;
        environment = new Environment();
    }





    public void clear(){
        instances.clear();
    }

    public void add(ModelInstance instance){
        instances.add(instance);
    }

    public void remove(ModelInstance instance){
        instances.removeValue(instance, true);
    }


    public void render (Camera camera) {
        WgScreenUtils.clear(fogColor,true);

        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        modelBatch.end();
    }

    public void dispose(){
        clear();
        modelBatch.dispose();
    }
}
