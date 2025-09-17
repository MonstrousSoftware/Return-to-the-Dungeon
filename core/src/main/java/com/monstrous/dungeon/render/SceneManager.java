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
import com.monstrous.gdx.webgpu.graphics.utils.WgScreenUtils;

public class SceneManager implements Disposable {
    private Array<ModelInstance> instances;
    private final WgModelBatch modelBatch;
    private Environment environment;
    private final Color fogColor;

    public SceneManager() {
        modelBatch = new WgModelBatch();
        instances = new Array<>();
        fogColor = Color.BLACK;
        setEnvironment();
    }


    private void setEnvironment(){
        // Create an environment with lights
        environment = new Environment();

        float ambientLevel = 0.02f;
        ColorAttribute ambient =  ColorAttribute.createAmbientLight(ambientLevel, ambientLevel, ambientLevel, 1f);
        environment.set(ambient);

        DirectionalLight dirLight1 = new DirectionalLight();
        dirLight1.setDirection(.5f, -.4f, .5f);
        dirLight1.setColor(setIntensity(Color.ORANGE, 3.6f));
        environment.add(dirLight1);

        DirectionalLight dirLight2 = new DirectionalLight();
        dirLight2.setDirection(-.5f, .4f, -.5f);
        dirLight2.setColor(setIntensity(Color.PURPLE, 0.3f));
        environment.add(dirLight2);

        PointLight pointLight2 = new PointLight();
        pointLight2.setPosition(1f, 1f, 1f);
        pointLight2.setColor(Color.RED);
        pointLight2.setIntensity(20f);
        environment.add(pointLight2);

        environment.set(new ColorAttribute(ColorAttribute.Fog,fogColor));

    }

    private final Color tmpColor = new Color();

    Color setIntensity(Color color, float intensity){
        tmpColor.set(color);
        tmpColor.r *= intensity;
        tmpColor.g *= intensity;
        tmpColor.b *= intensity;
        return tmpColor;
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


    public void render (Camera cam) {
        WgScreenUtils.clear(Color.BLACK,true);

        modelBatch.begin(cam);
        modelBatch.render(instances, environment);
        modelBatch.end();
    }

    public void dispose(){
        clear();
        modelBatch.dispose();
    }
}
