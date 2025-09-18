package com.monstrous.dungeon.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.monstrous.dungeon.MessageBox;
import com.monstrous.dungeon.Sounds;
import com.monstrous.dungeon.World;
import com.monstrous.dungeon.map.Direction;
import com.monstrous.dungeon.map.Room;
import com.monstrous.dungeon.map.TileType;
import com.monstrous.dungeon.populus.*;
import com.monstrous.dungeon.render.DungeonScenes;

public class KeyController extends InputAdapter {

    private static float KEY_DELAY = 0.5f;
    private static float KEY_REPEAT_DELAY = 0.05f;

    private final World world;
    private final DungeonScenes scenes;
    private final Camera cam;
    private boolean equipMode;  // after e
    private boolean dropMode;   // after d
    private boolean useMode;    // after u
    private boolean confirmMode;        // after reset
    private boolean throwMode;  // after t
    private boolean throwDirectionMode; // after t + item
    private int throwSlot;
    private int frozenTimer;
    private int regenTimer;
    private int digestionSpeed = 2;
    private int keyDown;
    private float downTime;
    private int turboTimer;     // counts down for duration of speed potion
    private int screenRotation = 0; // 0 to 3 (in 90 degree increments), how screen is rotated with respect to "Up is North"

    public KeyController(Camera cam, World world, DungeonScenes scenes) {
        this.world = world;
        this.scenes = scenes;
        this.cam = cam;
        equipMode = false;
        dropMode = false;
        useMode = false;
        confirmMode = false;
        throwMode = false;
        throwDirectionMode = false;
        regenTimer = 10;
        turboTimer = 0;
    }

    // used for key repeat
    public void update(float deltaTime){
        if(keyDown == 0)
            return;
        downTime -= deltaTime;
        if(downTime <= 0){
            downTime = KEY_REPEAT_DELAY;
            handleKey(keyDown); // simulate a key press
        }
    }



    @Override
    public boolean keyUp(int keycode) {
        keyDown = 0;
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        keyDown = keycode;
        downTime = KEY_DELAY;
        return handleKey(keycode);
    }

    Vector3 tmp = new Vector3();

    /** determine quadrant of screen rotation from the camera matrix */
    private void setScreenRotation(Matrix4 camMatrix){
        tmp.set(Vector3.Z);
        tmp.rot(camMatrix);
        tmp.y = 0;
        tmp.nor();
        if(Math.abs(tmp.x) > Math.abs(tmp.z)){
            screenRotation = tmp.x > 0 ? 1 : 3;
        } else {
            screenRotation = tmp.z > 0 ? 2 : 0;
        }
    }

    private boolean handleKey(int keycode){
        setScreenRotation(cam.combined);

        if (world.gameOver) { // player is dead or has beaten the game
            return false;
        }

        if(throwDirectionMode) {
            processThrowDirectionChoice(keycode);
            return true;
        }


        boolean done = true;
        if(frozenTimer > 0){    // still frozen?
            frozenTimer--;
        }
        else {
            done = false;

            // left/right keys translate to -x/+x
            // up/down to +y/-y
            //

            switch (keycode) {
                case Input.Keys.UP:
                    tryMoveRogue( 0);
                    done = true;
                    break;
                case Input.Keys.DOWN:
                    tryMoveRogue( 2);
                    done = true;
                    break;
                case Input.Keys.LEFT:
                    tryMoveRogue( 3);
                    done = true;
                    break;
                case Input.Keys.RIGHT:
                    tryMoveRogue( 1);
                    done = true;
                    break;



                case Input.Keys.SPACE:
                    world.rogue.startAnimation("Sit_Floor_Down", 1);
                    done = true;
                    break;        // do nothing
            }
        }
        if(done)
            wrapUp();
        return done;
    }



    @Override
    public boolean keyTyped(char character) {
        //System.out.println("keytyped: "+character);
        // if player is dead, only accept restart command
        if (world.gameOver) {
            if (character == 'r' || character =='R') {
                restart(character == 'R');
                world.gameOver = false;
                return true;
            }
            return false;
        }

        boolean handled = true;
        if(frozenTimer > 0){    // still frozen?
            frozenTimer--;
        }
        else {
            handled = processKey(character);
        }
        if (handled)
            wrapUp();

        return handled;
    }





    // arrive here after having made a move or skipping a turn
    private void wrapUp(){
        // time-out on increased awareness
        if(world.rogue.stats.increasedAwareness > 0){
            world.rogue.stats.increasedAwareness--;
            if(world.rogue.stats.increasedAwareness == 0)
                MessageBox.addLine("Your increased awareness wore off.");
        }
        // regenerate HP
        if(--regenTimer <= 0){
            regenTimer = Math.max(20-2*world.level, 3);
            if(world.rogue.stats.hitPoints < CharacterStats.MAX_HITPOINTS)
                world.rogue.stats.hitPoints++;
        }
        // move enemies

        if(turboTimer > 0) {
            turboTimer--;
            if(turboTimer == 0)
                MessageBox.addLine("Your reflexes are back to normal.");
        }

        // if turbo timer is active, then only move enemies every other step
        if(turboTimer % 2 == 0)
            world.enemies.step(world, scenes);     // move enemies

        digestFood();

        // check for death
        if (world.rogue.stats.hitPoints <= 0) {
            MessageBox.addLine("You are dead. Press R to restart.");
            world.gameOver = true;
            world.rogue.startAnimation("Death_A", 1);
        }
    }

    private void digestFood(){
        //System.out.println("food: "+world.rogue.stats.food);
        world.rogue.stats.food -= digestionSpeed;
        if(world.rogue.stats.food == 30) {
            Sounds.stomachRumble();
            MessageBox.addLine("You feel hungry.");
        }
        else if(world.rogue.stats.food == 10) {
            Sounds.stomachRumble();
            MessageBox.addLine("You're so hungry you feel faint.");
        }
        else if(world.rogue.stats.food == 0){
            Sounds.stomachRumble();
            MessageBox.addLine("You're so faint you can't move.");
            world.rogue.startAnimation("Lie_Idle", -1);
            frozenTimer = 5;
            world.rogue.stats.food = CharacterStats.REPLENISH_FOOD;
        }
    }

    private boolean processKey(char character) {
        if(equipMode)
            return processEquipChoice(character);
        if(dropMode)
            return processDropChoice(character);
        if(useMode)
            return processUseChoice(character);
        if(confirmMode)
            return processConfirmation(character);
        if(throwMode)
            return processThrowChoice(character);

        //System.out.println("Character: "+character);
        switch (Character.toLowerCase(character)) {

            case 'z':
                turnRogue(false); return true;
            case 'c':
                turnRogue(true); return true;
            case 'e':
                equip();
                return false;
            case 'd':
                drop();
                return false;
            case 'u':
                use();
                return false;
            case 't':
                throwItem();
                return false;       // return false because it's not enemy's move yet
            case 'r':
                confirmMode = true;
                MessageBox.addLine("Confirm with Y to restart.");
                return false;

            case ']':   // cheat code
                world.rogue.stats.haveBookOfMaps = true;
                return true;
            default:
                return false;
        }
    }

    private void restart( boolean keepSeed ) {
        world.restart(keepSeed);
        scenes.clear();
        scenes.uncoverAreaInPlayerView(world);
        int roomId = world.map.roomCode[world.rogue.y][world.rogue.x];
        Room room = world.map.rooms.get(roomId);
        scenes.showRoom(world.map, world.levelData, room);
        scenes.populateRoom(world, room);
    }

    private void turnRogue(boolean clockWise) {
        int dir = world.rogue.direction.ordinal();
        if(clockWise)
            dir = (dir + 1) % 4;
        else
            dir = (dir+3) % 4;

        scenes.turnObject(world.rogue, Direction.values()[dir], world.rogue.x, world.rogue.y);    // turn towards moving direction
    }

    /** 180 degree turn */
    private void reverseRogue() {
        int dir = world.rogue.direction.ordinal();
        dir = (dir + 2) % 4;
        scenes.turnObject(world.rogue, Direction.values()[dir], world.rogue.x, world.rogue.y);    // turn towards moving direction
    }



    private final int[] deltaX = {0, 1, 0, -1  };
    private final int[] deltaY = {1, 0, -1, 0  };
    private final Direction[] dirs = { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };

//    private void tryMoveForwardRogue(){
//        Direction dir = world.rogue.direction;
//        int dx = deltaX[dir.ordinal()];
//        int dy = deltaY[dir.ordinal()];
//        tryMoveRogue(dx, dy, dir);
//    }

    private void tryMoveRogue(int screenRelativeDirection){
        int direction = (screenRelativeDirection + screenRotation) % 4;
        int dx = deltaX[direction];
        int dy = deltaY[direction];

        Direction dir = dirs[direction];    // yeah, maybe enums are not so great....

        // if on bottom of stairs and moving forward, move down a level
        if(world.map.getGrid(world.rogue.x,world.rogue.y) == TileType.STAIRS_DOWN_DEEP &&
            dir == world.map.tileOrientation[world.rogue.y][world.rogue.x]){
            world.levelDown();
            // continue to make the move off the bottom step
        }
        else if(world.map.getGrid(world.rogue.x,world.rogue.y) == TileType.STAIRS_UP_HIGH &&
            dir == Direction.opposite(world.map.tileOrientation[world.rogue.y][world.rogue.x])){
            world.levelUp();
            // continue to make the move off the top step
        }

        world.rogue.tryMove(world, scenes, dx, dy, dir);

        discoverMap( world.rogue);

//        int x = world.rogue.x;
//        int y = world.rogue.y;
//        // show the room if this is the first time we enter it
//        int roomId = world.map.roomCode[y][x];
//
//        //Gdx.app.log("Rogue on tile", world.map.getGrid(x,y).toString());
//        if(roomId >= 0) {
//
//            Room room = world.map.rooms.get(roomId);
//            if (!world.levelData.seenRooms.contains(roomId, true)) {
//                scenes.showRoom(world.map, world.levelData, room);
//                scenes.populateRoom(world, room);
//            }
//        } else if( world.map.getGrid(x,y) == TileType.CORRIDOR){
//            scenes.visitCorridorSegment(world, x, y);
//        }

        scenes.moveObject( world.rogue, world.rogue.x, world.rogue.y, world.rogue.z);

        // Did we return to the first room with the Sword?
        CharacterStats stats = world.rogue.stats;
        int roomId = world.map.roomCode[world.rogue.y][world.rogue.x];
        if(world.level == 0 && roomId == world.startRoomId &&  ((stats.weaponItem != null && stats.weaponItem.type == GameObjectTypes.bigSword) || stats.inventory.contains(GameObjectTypes.bigSword)) ) {
            // clear message box
            for(int i = 0; i < 10; i++)
                MessageBox.addLine("");
            MessageBox.addLine("You have completed the quest!");
            MessageBox.addLine("The Sword of Yobled was recovered.");
            MessageBox.addLine("Congratulations!");

            scenes.turnObject(world.rogue, Direction.SOUTH, world.rogue.x, world.rogue.y);    // turn towards the camera

            world.rogue.startAnimation("Cheer", 20 );
            world.gameOver = true;
            world.gameCompleted = true;
//            world.enemies.hideAll(scenes);  // hide any leftover enemies
            // todo more fanfare
        }

    }

    public void discoverMap( GameObject character ){
        int x = character.x;
        int y = character.y;
        // show the room if this is the first time we enter it
        int roomId = world.map.roomCode[y][x];

        //Gdx.app.log("Rogue on tile", world.map.getGrid(x,y).toString());
        if(roomId >= 0) {

            Room room = world.map.rooms.get(roomId);
            if (!world.levelData.seenRooms.contains(roomId, true)) {
                scenes.showRoom(world.map, world.levelData, room);
                scenes.populateRoom(world, room);
            }
            if(room.isStairWell)
                scenes.visitCorridorSegment(world, x, y);
        } else if( world.map.getGrid(x,y) == TileType.CORRIDOR){
            scenes.visitCorridorSegment(world, x, y);
        }
    }


    private void equip(){
        MessageBox.addLine("Equip what? (0-9) or Esc");
        equipMode = true;
    }

    private boolean processEquipChoice(int character){
        equipMode = false;
        if(character >= '0' && character <= '9'){
            equipSlot(slotNumber(character));
            return true;
        }
        return false;
    }

    private boolean processConfirmation(int character){
        confirmMode = false;

        if(character ==  'y' ||  character == 'Y'){
            restart(character == 'Y');
        }
        return false;
    }

    private void drop(){
        MessageBox.addLine("Drop what? (0-9) or Esc");
        dropMode = true;
    }

    private boolean processDropChoice(int character){
        dropMode = false;
        if(character >= '0' && character <= '9'){
            dropSlot(slotNumber(character));
            return true;
        }
        return false;
    }

    private void use(){
        MessageBox.addLine("Use what? (0-9) or Esc");
        useMode = true;
    }

    private boolean processUseChoice(int character){
        useMode = false;
        if(character >= '0' && character <= '9'){
            useSlot(slotNumber(character));
            return true;
        }
        return false;
    }



    private void throwItem(){
        MessageBox.addLine("Throw what? (0-9) or Esc");
        throwMode = true;
    }

    private boolean processThrowChoice(int character){
        throwMode = false;
        if(character >= '0' && character <= '9'){
            throwSlot = slotNumber(character);
            // ask direction
            MessageBox.addLine("Which direction? (arrow keys)");
            throwDirectionMode = true;
            return false;       // return false because it's not the enemy's move yet
        }
        return false;
    }

    // after t + item + direction
    private boolean processThrowDirectionChoice(int keycode){
        throwDirectionMode = false;
        switch(keycode){
            case Input.Keys.LEFT:
                throwIt(throwSlot, -1, 0, Direction.WEST);
                break;
            case Input.Keys.RIGHT:
                throwIt(throwSlot, 1, 0, Direction.EAST);
                break;
            case Input.Keys.UP:
                throwIt(throwSlot, 0, 1, Direction.NORTH);
                break;
            case Input.Keys.DOWN:
                throwIt(throwSlot, 0, -1, Direction.SOUTH);
                break;
        }
        return false;
    }

    private boolean throwIt(int slotNr, int dx, int dy, Direction dir){
        System.out.println("Throw "+slotNr+" to dx:"+dx+", dy:"+dy);
        world.rogue.startAnimation("Throw", 1);

        Inventory.Slot slot = world.rogue.stats.inventory.slots[slotNr];
        if(slot.isEmpty())
            return true;
        // turn rogue in direction of throw
        scenes.turnObject(world.rogue, dir, world.rogue.x, world.rogue.y);
        // take item from inventory slot
        GameObject item = slot.removeItem();
        MessageBox.addLine("You throw "+item.type.name+".");

        int tx = world.rogue.x;
        int ty = world.rogue.y;
        while(true) {
            // next tile
            int nx = tx + dx;
            int ny = ty + dy;
            if(nx < 0 || nx > world.map.mapWidth || ny < 0 || ny > world.map.mapHeight)
                return true;

            GameObject occupant = world.levelData.gameObjects.getOccupant(nx, ny);
            if(occupant != null && occupant.type.isEnemy){
                MessageBox.addLine("You hit "+occupant.type.name+".");
                item.hits(world, scenes, world.rogue, occupant);
                return true;
            }
            // move as long as we are over floor or corridor
            // i.e. don't go through walls, but you can throw through doorways
            TileType tile = world.map.getGrid(nx, ny);
            if(!TileType.walkable(tile,  world.map.getGrid(tx, ty))) {
                // drop item short of the wall
//                scenes.dropObject(world.map, world.levelData.gameObjects, item, tx, ty);
                return true;
            }
            tx = nx;
            ty = ny;
        }
    }

    private int slotNumber(int k){
        return ((k-'0')+9) % 10;    // '1', '2', '3' maps to 0,1,2
    }

    private void equipSlot(int slotNr ){
        Inventory.Slot slot = world.rogue.stats.inventory.slots[slotNr];
        if(slot.isEmpty())
            return;
        if(slot.object.type.isArmour){
            GameObject prev = world.rogue.stats.armourItem;
            world.rogue.stats.armourItem = slot.removeItem();
            if(prev != null) {
                world.rogue.stats.inventory.addItem(prev);
                scenes.detachModel(world.rogue, "handslot.l",  prev);
            }
            scenes.attachModel(world.rogue, "handslot.l",  world.rogue.stats.armourItem);

        } else if(slot.object.type.isWeapon){
            GameObject prev = world.rogue.stats.weaponItem;
            world.rogue.stats.weaponItem = slot.removeItem();
            if(prev != null) {
                world.rogue.stats.inventory.addItem(prev);
                scenes.detachModel(world.rogue, "handslot.r",  prev);
            }
            scenes.attachModel(world.rogue, "handslot.r",  world.rogue.stats.weaponItem);
        }

    }

    private void dropSlot(int slotNr ){
        Inventory.Slot slot = world.rogue.stats.inventory.slots[slotNr];
        if(slot.isEmpty())
            return;
        GameObject item = slot.removeItem();
        MessageBox.addLine("You dropped "+item.type.name+".");
        scenes.dropObject(world.map, world.levelData.gameObjects, item, world.rogue.x, world.rogue.y);
    }


    private void useSlot(int slotNr ){
        Inventory.Slot slot = world.rogue.stats.inventory.slots[slotNr];
        if(slot.isEmpty())
            return;
        if(slot.object.type.isEdible) {
            GameObject item = slot.removeItem();
            MessageBox.addLine("You eat the food.");
            world.rogue.stats.food = CharacterStats.MAX_FOOD;
        } else if(slot.object.type.isPotion) {
            GameObject potion = slot.removeItem();
            drinkPotion(potion);
        } else if(slot.object.type.isSpellBook) {
            readSpell(slot.object.type);
            slot.object.type = slot.object.type.alternative;

        } else if(slot.object.type == GameObjectTypes.spellBookOpen) {
            MessageBox.addLine("Can only read spell book once.");
        } else {
            MessageBox.addLine("Can't use "+slot.object.type.name+".");
        }
    }

    private void readSpell(GameObjectType type){
        MessageBox.addLine("You read the spell book.");
        if(type == GameObjectTypes.spellBookClosed) { // purple
            MessageBox.addLine("The paper makes you feel sad.");
        } else if(type == GameObjectTypes.spellBookClosedB) { // red
            MessageBox.addLine("It is a book of maps.");
            world.rogue.stats.haveBookOfMaps = true;
        } else if(type == GameObjectTypes.spellBookClosedC) { // black
            if(world.level == world.swordLevel || (world.rogue.stats.weaponItem != null && world.rogue.stats.weaponItem.type.isBigSword)
                    || world.rogue.stats.inventory.contains(GameObjectTypes.bigSword) )
                MessageBox.addLine("The Sword of Yobled is at this level.");
            else
                MessageBox.addLine("The Sword of Yobled is not at this level.");
        } else if(type == GameObjectTypes.spellBookClosedD) { // green
            MessageBox.addLine("It has no effect.");
        }
    }

    private void drinkPotion(GameObject potion){
        MessageBox.addLine("You drink the "+potion.type.name+".");
        if(potion.type == GameObjectTypes.bottle_A_brown){
            world.rogue.stats.increasedAwareness = 100;
            MessageBox.addLine("Your awareness is increased.");
        } else if(potion.type == GameObjectTypes.bottle_C_green){
            world.rogue.stats.hitPoints = Math.max(1, world.rogue.stats.hitPoints-10);
            MessageBox.addLine("It is poison. You lose health.");
        } else if(potion.type == GameObjectTypes.bottle_B_green){
            world.rogue.stats.hitPoints = Math.min(CharacterStats.MAX_HITPOINTS, world.rogue.stats.hitPoints+3);
            MessageBox.addLine("You feel invigorated.");
        } else if(potion.type == GameObjectTypes.bottle_A_green) {
            digestionSpeed = 1;
            MessageBox.addLine("This aids your digestion.");
        } else if(potion.type == GameObjectTypes.bottle_C_brown) {
            turboTimer = 30;
            MessageBox.addLine("Your reflexes become faster.");
        } else {
            MessageBox.addLine("It has no effect.");
        }
        // todo some effect
    }
}
