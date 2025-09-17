package com.monstrous.dungeon.populus;

import com.badlogic.gdx.utils.Array;


// Set of GameObjects - can either be iterated as array or queried as 2d grid

// occupant of a grid cell is an item that is present there, e.g. gold or
// an enemy character. There can only be zero or one occupant per grid cell.
// Architecture and the player are never occupants.

public class GameObjects {
    public Array<GameObject> gameObjects;
    private GameObject[][] occupant;

    public GameObjects(int width, int height) {
        gameObjects = new Array<>();
        occupant = new GameObject[height][width];
    }

    public void add(GameObject go){
        gameObjects.add(go);
    }

    public void remove(GameObject go){
        gameObjects.removeValue(go, true);
    }

    // may be null
    public GameObject getOccupant(int x, int y){
        return occupant[y][x];
    }

    public void setOccupant(int x, int y, GameObject go){
        occupant[y][x] = go;
    }

    public void clearOccupant(int x, int y){
        occupant[y][x] = null;
    }

}
