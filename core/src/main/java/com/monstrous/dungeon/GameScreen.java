

package com.monstrous.dungeon;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.monstrous.dungeon.input.CamController;
import com.monstrous.dungeon.input.KeyController;
import com.monstrous.dungeon.input.OrbitCameraController;
import com.monstrous.dungeon.populus.GameObject;
import com.monstrous.dungeon.render.DungeonScenes;
import com.monstrous.dungeon.render.GameObjectScenes;
import com.monstrous.dungeon.render.SceneManager;
import com.monstrous.gdx.webgpu.assets.WgAssetManager;
import com.monstrous.gdx.webgpu.graphics.g2d.WgBitmapFont;
import com.monstrous.gdx.webgpu.graphics.g2d.WgSpriteBatch;
import com.monstrous.gdx.webgpu.graphics.utils.WgScreenUtils;
import com.monstrous.gdx.webgpu.scene2d.WgSkin;
import com.monstrous.gdx.webgpu.scene2d.WgStage;



public class GameScreen extends ScreenAdapter {

	final static String[] fileNames = {
//        "models/Cube/Cube.gltf",
        "models/DungeonModular/dungeonmodular.gltf",
        "characters/Warrior.gltf",
//        "models/Dungeon/dungeon-room.gltf",
//        "models/Dungeon/dungeon-floor.gltf",
//        "models/Dungeon/dungeon-walls.gltf",

	};

    Main game;
    SceneManager sceneManager;


	PerspectiveCamera cam;
    OrbitCameraController camController;
    KeyController keyController;
	AssetManager assets;
	ScreenViewport viewport;
	WgStage stage;
	WgSkin skin;
    boolean loadedAll;
	WgSpriteBatch batch;
	WgBitmapFont font;
    DungeonScenes dungeonScenes;
    GameObjectScenes gameObjectScenes;
    private GameObject focalActor;    // who the camera is following, normally the Rogue

    public GameScreen(Main game) {
        this.game = game;
    }

    // application
	public void show () {
        sceneManager = new SceneManager();
        dungeonScenes = new DungeonScenes(sceneManager);
        gameObjectScenes = new GameObjectScenes();
		batch = new WgSpriteBatch();
		font = new WgBitmapFont();

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
        dungeonScenes.queueAssets(assets);
        gameObjectScenes.queueAssets(assets);




        // hide the mouse cursor and fix it to screen centre, so it doesn't go out the window canvas
        Gdx.input.setCursorCatched(true);
        Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);


        // Add some GUI
		//
		viewport = new ScreenViewport();
		stage = new WgStage(viewport);
		//stage.setDebugAll(true);

        camController = new OrbitCameraController(cam);
        keyController = new KeyController(cam, game.world, dungeonScenes);
		InputMultiplexer im = new InputMultiplexer();
		Gdx.input.setInputProcessor(im);
        im.addProcessor(keyController);
		im.addProcessor(stage);
		im.addProcessor(camController);

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

    private final Vector3 focus = new Vector3();
    private final Vector3 playerDirection = new Vector3();
    private final Matrix4 mat = new Matrix4();

	public void render (float delta) {
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            Gdx.app.exit();
        }

        if(!loadedAll) {
            if(assets.update()) {    // advance loading
                loadedAll = true;
                Model model = assets.get(fileNames[1], Model.class);
                ModelInstance instance = new ModelInstance(model, 0,0, 0);
                sceneManager.add(instance);
                Model model2 = assets.get(fileNames[0], Model.class);
                ModelInstance instance2 = new ModelInstance(model2, 0, 0, 0);
                sceneManager.add(instance2);
                dungeonScenes.loadAssets(assets);
                gameObjectScenes.loadAssets(assets);
                //dungeonScenes.showMap(game.world.map, game.world.levelData);
                game.world.isRebuilt = true;





            } else {
                WgScreenUtils.clear(Color.BLACK,true);
                batch.begin();
                font.draw(batch, "Loading models...", 100, 100);
                batch.end();
            }
            return;
        }
        // if we arrive here, all assets are loaded
        if(game.world.isRebuilt){
            game.world.isRebuilt = false;

            // refill scene manager
            sceneManager.clear();
//            for(GameObject object: world.levelData.gameObjects.gameObjects)
//                object.scene = null;

            dungeonScenes.createRogueModel( game.world );
            dungeonScenes.uncoverAreaInPlayerView( game.world );
            dungeonScenes.showMap( game.world.map, game.world.levelData );
            dungeonScenes.populateMap(game.world, game.world.levelData);
            focalActor = game.world.rogue;
            focalActor.hasFocus = true;
        }

        focalActor.scene.transform.getTranslation(focus);

        mat.setToRotation(Vector3.Y, 180-focalActor.direction.ordinal() * 90);
        playerDirection.set(Vector3.Z).mul(mat);

        camController.update(focus, playerDirection);

        sceneManager.render(cam);

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
		skin.dispose();
		stage.dispose();
		assets.dispose();
		batch.dispose();
		font.dispose();
        sceneManager.dispose();
	}


}
