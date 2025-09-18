package com.monstrous.dungeon.populus;

import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.monstrous.dungeon.map.Direction;
import com.monstrous.dungeon.map.DungeonMap;
import com.monstrous.dungeon.map.Room;

public class Populator {


    private static void generatePopulation(int levelNr, boolean goodies, Array<GameObjectType> mandatory, Array<GameObjectType> optional) {
        int min = 0;
        int max = 0;
        for(GameObjectType type :GameObjectTypes.types)
        {
            if (levelNr >= type.startLevel && (levelNr <= type.endLevel || type.endLevel == 99)) {    // dungeon level range per type
                if (type.maxCount == 0)
                    continue;
                if (goodies == type.isEnemy)    // select goodies or enemies
                    continue;
                for (int i = 0; i < type.minCount; i++)
                    mandatory.add(type);
                for (int i = type.minCount; i < type.maxCount; i++)
                    optional.add(type);
                System.out.println(" drop " + type.name + " min:" + type.minCount + " max:" + type.maxCount);
                min += type.minCount;
                max += type.maxCount;
            }
        }
        System.out.println("Total min:"+min+" max:"+max);
    }

    public static void distributeGoodies(DungeonMap map, int levelNr, GameObjects gameObjects){
        Array<GameObjectType> mandatory = new Array<>();
        Array<GameObjectType> optional = new Array<>();
        generatePopulation(levelNr, true, mandatory, optional);

        //int numRooms = map.rooms.size;
        //int count = MathUtils.random(numRooms/2, numRooms*3);        // nr of drops depends on nr of rooms
        int attempts = 0;
        while(true){
            attempts++;
            if(attempts > 50)       // avoid endless loop
                break;

            int location = MathUtils.random(0, map.rooms.size-1);
            Room room = map.rooms.get(location);
            if(room.isStairWell)
                continue;
            // find a point inside the room (edges are walls)
            int rx = MathUtils.random(1, room.width-1);
            int ry = MathUtils.random(1, room.height-1);
            GameObject item = gameObjects.getOccupant(room.x+rx, room.y+ry);
            if(item != null)
                continue;

            GameObjectType type = null;
            if(mandatory.size > 0) {
                type = mandatory.first();
                mandatory.removeIndex(0);
            } else if (optional.size > 0) {
                int choice = MathUtils.random(0, optional.size-1);
                type = optional.get(choice);
                optional.removeIndex(choice);
            } else
                break;


            item = new GameObject(type, room.x+rx, room.y+ry, Direction.SOUTH);
            gameObjects.setOccupant(room.x+rx, room.y+ry, item);
            gameObjects.add(item);
            // seems redundant to provide x,y twice


            assert type != null;
            item.z = type.z;
            item.quantity = 1;
            if(type == GameObjectTypes.gold)
                item.quantity = MathUtils.random(1,30);
            else if(type == GameObjectTypes.arrows)
                item.quantity = MathUtils.random(3,8);
            else if(type.isArmour)
                item.protection = type.initProtection + MathUtils.random(-2, 2);
            else if(type.isWeapon) {
                if(type.isRangeWeapon) {
                    item.damage = type.initThrowDamage + MathUtils.random(-1, 3);
                    item.accuracy = type.initThrowAccuracy + MathUtils.random(-1, 3);
                } else {
                    item.damage = type.initMeleeDamage + MathUtils.random(-1, 3);
                    item.accuracy = type.initMeleeAccuracy + MathUtils.random(-1, 3);

                }
            }
        }
    }

    public static void placeSword(DungeonMap map, GameObjects gameObjects){
        while(true) {
            // choose random room
            int location = MathUtils.random(1, map.rooms.size-1);
            Room room = map.rooms.get(location);
            if(room.isStairWell)    // not a stairwell
                continue;
            // check if there is something in the centre?
            GameObject occupant = gameObjects.getOccupant(room.centre.x, room.centre.y);
            if(occupant != null)
                continue;

            GameObject sword = new GameObject(GameObjectTypes.bigSword, room.centre.x, room.centre.y, Direction.SOUTH);
            gameObjects.setOccupant(room.centre.x, room.centre.y, sword);
            gameObjects.add(sword);
            sword.z = sword.type.z;
            sword.quantity = 1;
            sword.direction = Direction.SOUTH;
            sword.damage = sword.type.initMeleeDamage;
            sword.accuracy = sword.type.initMeleeAccuracy;
            return;
        }
    }

    public static void distributeEnemies(DungeonMap map, int levelNr, GameObjects gameObjects, Enemies enemies ){
        Array<GameObjectType> mandatory = new Array<>();
        Array<GameObjectType> optional = new Array<>();
        generatePopulation(levelNr, false, mandatory, optional);

        enemies.clear();
        int numRooms = map.rooms.size;
        int count = MathUtils.random(numRooms/4, numRooms*numRooms/4);        // nr of drops depends on nr of rooms
        int attempts = 0;
        while(true){
            attempts++;
            if(attempts > 20)       // avoid endless loop
                break;

            int location = MathUtils.random(0, map.rooms.size-1);
            Room room = map.rooms.get(location);
            if(room.isStairWell)
                continue;
            // find a point inside the room (edges are walls)
            int rx = MathUtils.random(1, room.width-1);
            int ry = MathUtils.random(1, room.height-1);

            GameObject occupant = gameObjects.getOccupant(room.x+rx, room.y+ry);
            if(occupant != null)
                continue;

            GameObjectType type = null;
            if(mandatory.size > 0) {
                type = mandatory.first();
                mandatory.removeIndex(0);
            } else if (optional.size > 0) {
                int choice = MathUtils.random(0, optional.size-1);
                type = optional.get(choice);
                optional.removeIndex(choice);
            } else
                break;

            GameObject enemy = new GameObject(type, room.x+rx, room.y+ry, Direction.SOUTH);
            gameObjects.setOccupant(room.x+rx, room.y+ry, enemy);
            enemy.stats = new CharacterStats();
            //enemy.animationController = new AnimationController(enemy.scene);
            assert type != null;
            enemy.stats.experience = type.initXP * (1+MathUtils.random(levelNr*10));     // at lower levels, enemies get more experienced
            int goldAmount = MathUtils.random(0,5);
            if(goldAmount > 0){
                GameObject gold = new GameObject(GameObjectTypes.gold, goldAmount);
                enemy.stats.inventory.addItem(gold);
            }
            enemy.stats.aggressive = enemy.type.initAggressive;
            gameObjects.add(enemy);
            enemies.add(enemy);
            // seems redundant to provide x,y twice

            count--;
            if(count == 0 && mandatory.size == 0)
                return;
        }
    }

    public static GameObject placeRogue(DungeonMap map, GameObjects gameObjects){
        while(true) {
            // choose random room
            int location = MathUtils.random(1, map.rooms.size-1);
            Room room = map.rooms.get(location);
            if(room.isStairWell)    // not a stairwell
                continue;
            // check if there is something in the centre?
            GameObject occupant = gameObjects.getOccupant(room.centre.x, room.centre.y);
            if(occupant != null)
                continue;

            GameObject rogue = new GameObject(GameObjectTypes.rogue, room.centre.x, room.centre.y, Direction.SOUTH);
            gameObjects.setOccupant(room.centre.x, room.centre.y, rogue);
            rogue.stats = new CharacterStats();
            rogue.stats.experience = rogue.type.initXP;
            gameObjects.add(rogue);
            rogue.direction = Direction.SOUTH;

            return rogue;
        }
    }


}
