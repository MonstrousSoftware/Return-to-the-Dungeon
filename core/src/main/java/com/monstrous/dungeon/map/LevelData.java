package com.monstrous.dungeon.map;

import com.badlogic.gdx.utils.Array;
import com.monstrous.dungeon.populus.GameObjects;


/** persistent data per dungeon level that cannot be generated from the seed.
 * Such as which areas have been visited and what game objects remain in the level */
public class LevelData {
    public int level;                       // level nr
    public Array<Room> stairPortals;        // stairs to connect to next level.
    public boolean[][] tileSeen;            // has the corridor segment been seen?
    public Array<Integer> seenRooms;        // room ids of rooms that were uncovered
    public GameObjects gameObjects;

    public LevelData(int levelNr, int w, int h) {
        this.level = levelNr;
        this.stairPortals = new Array<>();
        this.seenRooms = new Array<>();
        this.tileSeen = new boolean[h][w];
        this.gameObjects = new GameObjects(w, h);
    }
}
