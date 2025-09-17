package com.monstrous.dungeon.map;

public enum Direction {
    NORTH, EAST, SOUTH, WEST;


    public static Direction opposite( Direction d ){
        switch(d){
            case NORTH:     return SOUTH;
            case EAST:      return WEST;
            case SOUTH:     return NORTH;
            case WEST:      return EAST;
        }
        return NORTH;   // can't happen
    }
}
