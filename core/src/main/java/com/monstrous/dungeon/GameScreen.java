/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.monstrous.dungeon;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.monstrous.gdx.webgpu.assets.WgAssetManager;
import com.monstrous.gdx.webgpu.graphics.g2d.WgBitmapFont;
import com.monstrous.gdx.webgpu.graphics.g2d.WgSpriteBatch;
import com.monstrous.gdx.webgpu.graphics.g3d.WgModelBatch;
import com.monstrous.gdx.webgpu.graphics.utils.WgScreenUtils;
import com.monstrous.gdx.webgpu.scene2d.WgSkin;
import com.monstrous.gdx.webgpu.scene2d.WgStage;

/** Test model loading via asset manager for OBJ, G3DJ and G3DB formats */


public class GameScreen extends ScreenAdapter {

	final static String[] fileNames = {
//        "models/Cube/Cube.gltf",
        "models/DungeonModular/dungeonmodular.gltf",
        "characters/Warrior.gltf",
//        "models/Dungeon/dungeon-room.gltf",
//        "models/Dungeon/dungeon-floor.gltf",
//        "models/Dungeon/dungeon-walls.gltf",

	};

    Color fogColor = Color.BLACK;

	WgModelBatch modelBatch;
	PerspectiveCamera cam;
    CamController controller;
	Model model;
    private Array<ModelInstance> instances;
//	ModelInstance RoomInstance;
//    ModelInstance CharacterInstance;
	AssetManager assets;
	ScreenViewport viewport;
	WgStage stage;
	WgSkin skin;
	boolean loadedFirst;
    boolean loadedAll;
	WgSpriteBatch batch;
	WgBitmapFont font;
    Environment environment;
    ModelInstance instance, instance2;


	// application
	public void show () {
		batch = new WgSpriteBatch();
		font = new WgBitmapFont();

		modelBatch = new WgModelBatch();
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0, 4, 4);
		cam.lookAt(0,4,0);
		cam.near = 0.1f;
		cam.far = 20f;		// also affects fog distance


		// queue for asynchronous loading
        // load one asset first to appear responsive
        // load the rest of the assets while the user is admiring the dragon :-)
		assets = new WgAssetManager();
		loadedAll = false;
        for(int i = 0; i < fileNames.length; i++){
            assets.load(fileNames[i], Model.class);
        }

        instances = new Array<>();

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

		controller = new CamController(cam);
		Gdx.input.setInputProcessor(controller);

        // hide the mouse cursor and fix it to screen centre, so it doesn't go out the window canvas
        Gdx.input.setCursorCatched(true);
        Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);


        // Add some GUI
		//
		viewport = new ScreenViewport();
		stage = new WgStage(viewport);
		//stage.setDebugAll(true);

		InputMultiplexer im = new InputMultiplexer();
		Gdx.input.setInputProcessor(im);
		im.addProcessor(stage);
		im.addProcessor(controller);

		skin = new WgSkin(Gdx.files.internal("ui/uiskin.json"));

		Table screenTable = new Table();
		screenTable.setFillParent(true);
		Table controls = new Table();
		controls.add(new Label("File: ", skin));
		controls.add(new Label(fileNames[0], skin)).row();
		screenTable.add(controls).left().top().expand();


		stage.addActor(screenTable);

	}

    private final Color tmpColor = new Color();

    Color setIntensity(Color color, float intensity){
        tmpColor.set(color);
        tmpColor.r *= intensity;
        tmpColor.g *= intensity;
        tmpColor.b *= intensity;
        return tmpColor;
    }

    boolean startLoading = false;

	public void render (float delta) {
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            Gdx.app.exit();
        }

        if(!loadedAll) {
            if(assets.update()) {    // advance loading
                loadedAll = true;
                Model model = assets.get(fileNames[1], Model.class);
                instance = new ModelInstance(model, 0,0, 0);
                instances.add(instance);
                Model model2 = assets.get(fileNames[0], Model.class);
                instance2 = new ModelInstance(model2, 0, 0, 0);
                instances.add(instance2);
            } else {
                WgScreenUtils.clear(Color.BLACK,true);
                batch.begin();
                font.draw(batch, "Loading models...", 100, 100);
                batch.end();
            }
            return;
        }
        // if we arrive here, all assets are loaded


        controller.update(delta);


		WgScreenUtils.clear(Color.BLACK,true);



		modelBatch.begin(cam);
		modelBatch.render(instance, environment);
        modelBatch.render(instance2, environment);

		modelBatch.end();

	    stage.act();
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		cam.viewportWidth = width;
		cam.viewportHeight = height;
		cam.update(true);
        stage.getViewport().update(width, height, true);

	}

	@Override
	public void dispose () {
		modelBatch.dispose();
		skin.dispose();
		stage.dispose();
		assets.dispose();
		batch.dispose();
		font.dispose();
	}


}
