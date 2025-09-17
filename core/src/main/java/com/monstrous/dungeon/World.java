package com.monstrous.dungeon;


// World contains the static dungeon architecture: map
// and the items and enemies within it: gameObjects

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.monstrous.dungeon.map.DungeonMap;
import com.monstrous.dungeon.map.LevelData;
import com.monstrous.dungeon.map.Room;
import com.monstrous.dungeon.populus.Enemies;
import com.monstrous.dungeon.populus.GameObject;
import com.monstrous.dungeon.populus.GameObjectTypes;
import com.monstrous.dungeon.populus.Populator;


public class World {
    private final static int MAP_WIDTH = 30;
    private final static int MAP_HEIGHT = 20;
    public final static int DELTA_WIDTH = 6;
    public final static int DELTA_HEIGHT = 4;

    public int seed = 1234;
    public int level = 0;           // current level
    public int swordLevel;          // level where sword is (game goal)
    public int startRoomId;
    public boolean gameOver;
    public boolean gameCompleted;

    public DungeonMap map;                  // static architecture
    public GameObject rogue;                // the player
    public Enemies enemies;                 // subset of gameObjects
    public boolean isRebuilt;               // force scenes to be recreated
    public float secondsElapsed;
    public Array<LevelData> levelDataArray; // data for all levels
    public LevelData levelData;             // data for current level


    public World() {
        secondsElapsed = 0;
        seed = 1234; //MathUtils.random(1,9999);
        gameOver = false;
        gameCompleted = false;
        GameObjectTypes gameObjectTypes = new GameObjectTypes();
        levelDataArray = new Array<>();
        create();
    }

    private void create(){
        MessageBox.clear();
        rogue = null;
        level = 0;
        secondsElapsed = 0;
        levelDataArray.clear();
        randomizeSwordLevel();
        generateLevel();
    }

    public void levelDown(){
        level++;        // top level is 0, lower levels have higher numbers
        map.dispose();
        generateLevel();
    }

    public void levelUp(){
        if(level == 0)  // cannot go higher
            return;
        level--;
        map.dispose();
        generateLevel();
    }

    public void restart(){
        restart(false);
    }

    public void restart(boolean keepSeed){
        map.dispose();
        if(!keepSeed)
            seed = MathUtils.random(1,9999);
        gameOver = false;
        gameCompleted = false;
        MessageBox.addLine("World seed: "+seed);
        create();
    }



    private void randomizeSwordLevel(){
        MathUtils.random.setSeed(seed);
        // set level where the sword can be found
        swordLevel = 5 + MathUtils.random(0,2);
        //swordLevel = 1;
    }

    private void generateLevel(){
        isRebuilt = true;
        boolean newLevel;

        // map gets bigger at lower levels: keep aspect ratio 3/2
        //
        int w = MAP_WIDTH+DELTA_WIDTH*level;
        int h = MAP_HEIGHT+DELTA_HEIGHT*level;

        if(level > levelDataArray.size-1) {       // new level
            levelData = new LevelData(level, w, h);
            levelDataArray.add(levelData);
            newLevel = true;
        } else {    // existing level
            levelData = levelDataArray.get(level);
            newLevel = false;
        }
        Array<Room> stairsFromAbove;
        if(level == 0)
            stairsFromAbove = new Array<>();    // empty array: no stairs from above
        else
            stairsFromAbove = levelDataArray.get(level-1).stairPortals;

        map = new DungeonMap(seed, level, w, h, stairsFromAbove, levelData.stairPortals);

        enemies = new Enemies();
        enemies.addFromObjects(levelData.gameObjects);

        if(rogue == null) {  // don't create new rogue when changing level
            rogue = Populator.placeRogue(map, levelData.gameObjects);
            startRoomId = map.roomCode[rogue.y][rogue.x];     // remember the starting room, the rogue needs to return here
        }

        if(newLevel) {
            if (level == swordLevel)
                Populator.placeSword(map, levelData.gameObjects);
            Populator.distributeGoodies(map, level, levelData.gameObjects);
            Populator.distributeEnemies(map, level, levelData.gameObjects, enemies);
        }

        levelData.gameObjects.clearOccupant(rogue.x, rogue.y);

    }
}
