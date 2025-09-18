

package com.monstrous.dungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.monstrous.dungeon.gui.GUI;
import com.monstrous.dungeon.input.KeyController;
import com.monstrous.dungeon.input.OrbitCameraController;
import com.monstrous.dungeon.populus.GameObject;
import com.monstrous.dungeon.render.DungeonScenes;
import com.monstrous.dungeon.render.GameObjectScenes;
import com.monstrous.dungeon.render.SceneManager;
import com.monstrous.gdx.webgpu.assets.WgAssetManager;
import com.monstrous.gdx.webgpu.graphics.g2d.WgBitmapFont;
import com.monstrous.gdx.webgpu.graphics.g2d.WgSpriteBatch;
import com.monstrous.gdx.webgpu.graphics.g3d.loaders.WgGLTFModelLoader;
import com.monstrous.gdx.webgpu.graphics.g3d.loaders.WgModelLoader;
import com.monstrous.gdx.webgpu.graphics.utils.WgScreenUtils;


public class TestScreen extends ScreenAdapter {


    Main game;
    SceneManager sceneManager;
    OrthographicCamera camera;
    PerspectiveCamera cam;
    Model model;


    public TestScreen(Main game) {
        this.game = game;
    }

    // application
	public void show () {
        sceneManager = new SceneManager();

        cam = new PerspectiveCamera(67, 64, 64); //Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(1f, 1f, 1f);
        cam.lookAt(0,0,0);
        cam.near = 0.001f;
        cam.far = 100f;
        cam.update();

        // setup camera
        camera = new OrthographicCamera();
        camera.near = 0.1f;
        camera.far = 500;
        camera.position.set(5,5, 5);
        camera.zoom = 0.025f;
        camera.up.set(Vector3.Y);
        camera.lookAt( new Vector3(0, 0f, 0));
        camera.update();


        String modelFileName = "models/axe_1handed.gltf";

        WgModelLoader.ModelParameters params = new WgModelLoader.ModelParameters();
        params.textureParameter.genMipMaps = true;

        System.out.println("Start loading");
        long startLoad = System.currentTimeMillis();
        FileHandle file = Gdx.files.internal(modelFileName);
        model = new WgGLTFModelLoader().loadModel(file, params);


        ModelInstance scene = null;
        if(model != null) {
            scene = new ModelInstance(model);
            sceneManager.add(scene);
        }

        camera.lookAt( new Vector3(0, 0f, 0));
        camera.update();


	}


	public void render (float delta) {
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            Gdx.app.exit();
        }



        sceneManager.render(cam);
	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void dispose () {

	}


}
