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
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
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
        "models/Dungeon/dungeon-room.gltf",
//        "models/Dungeon/dungeon-floor.gltf",
//        "models/Dungeon/dungeon-walls.gltf",
//        "models/Cube/Cube.gltf",
	};

	WgModelBatch modelBatch;
	PerspectiveCamera cam;
    CamController controller;
	Model model;
	ModelInstance instance;
	AssetManager assets;
	ScreenViewport viewport;
	WgStage stage;
	WgSkin skin;
	boolean loadedFirst;
    boolean loadedAll;
	WgSpriteBatch batch;
	WgBitmapFont font;
    Environment environment;


	// application
	public void show () {
		batch = new WgSpriteBatch();
		font = new WgBitmapFont();

		modelBatch = new WgModelBatch();
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0, 4, 4);
		cam.lookAt(0,4,0);
		cam.near = 0.1f;
		cam.far = 1000f;		// extend far distance to avoid clipping the skybox


		// queue for asynchronous loading
        // load one asset first to appear responsive
        // load the rest of the assets while the user is admiring the dragon :-)
		assets = new WgAssetManager();
		loadedAll = false;
        loadedFirst = false;

        assets.load(fileNames[0], Model.class);


        // Create an environment with lights
        environment = new Environment();

        float ambientLevel = 0.1f;
        ColorAttribute ambient =  ColorAttribute.createAmbientLight(ambientLevel, ambientLevel, ambientLevel, 1f);
        environment.set(ambient);

        DirectionalLight dirLight1 = new DirectionalLight();
        dirLight1.setDirection(.1f, -.8f, .2f);
        dirLight1.setColor(Color.WHITE);
        environment.add(dirLight1);

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

    boolean startLoading = false;

	public void render (float delta) {
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            Gdx.app.exit();
        }

        if(!loadedFirst && assets.update()) {	// advance loading
            loadedFirst = true;
            model = assets.get(fileNames[0], Model.class);
            instance = new ModelInstance(model);
            // start loading rest of assets
            for(int i = 1; i < fileNames.length; i++){
                assets.load(fileNames[i], Model.class);
            }
        }
		if(!loadedAll) {
            if( assets.update()) {    // advance loading
                loadedAll = true;
                System.out.println("Loading complete");
            }
		}

//		if(loadedFirst)
//			instance.transform.rotate(Vector3.Y, 15f*delta);

        controller.update(delta);
        //cam.update();

		WgScreenUtils.clear(Color.BLACK,true);



		modelBatch.begin(cam);

		if(loadedFirst)
			modelBatch.render(instance, environment);

		modelBatch.end();

        if(loadedAll)
		    stage.act();
		stage.draw();

		if(!loadedAll) {
			batch.begin();
			font.draw(batch, "Loading models from file...", 100, 100);
			batch.end();
		}

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
