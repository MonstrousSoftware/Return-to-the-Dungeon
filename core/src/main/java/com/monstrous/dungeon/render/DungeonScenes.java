package com.monstrous.dungeon.render;

// class to add Scenes to SceneManager to reflect the dungeon rooms

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.monstrous.dungeon.World;
import com.monstrous.dungeon.map.*;
import com.monstrous.dungeon.populus.GameObject;
import com.monstrous.gdx.webgpu.graphics.g3d.WgModel;


public class DungeonScenes  {
    private final static float SCALE = 4f;
    public final static int MAX_TORCHES = 4;
    final static String[] fileNames = {
        "models/floor_wood_large.gltf",
        "models/floor_tile_large.gltf",
        "models/floor_dirt_large.gltf",
        "models/wall.gltf",
        "models/wall_arched.gltf",
        "models/wall_archedwindow_gated.gltf",
        "models/wall_archedwindow_gated_scaffold.gltf",
        "models/wall_gated.gltf",
        "models/wall_open_scaffold.gltf",
        "models/wall_corner.gltf",
        "models/wall_Tsplit.gltf",
        "models/wall_crossing.gltf",
        "models/stairs.gltf",
        "models/torch_lit_mounted.gltf"
    };

    private SceneManager sceneManager;

    private Model sceneAssetFloor;
    private Model sceneAssetFloorA;
    private Model sceneAssetFloorB;
    private Model sceneAssetFloorC;
    private Model sceneAssetWall;
    private Model sceneAssetWall2;
    private Model sceneAssetWall3;
    private Model sceneAssetWall4;
    private Model sceneAssetWall5;
    private Model sceneAssetDoorWay;
    private Model sceneAssetCorner;
    private Model sceneAssetWallTsplit;
    private Model sceneAssetWallCrossing;
    private Model sceneAssetStairs;
    private Model sceneAssetTorch;

    private final boolean useFogOfWar = false;

    public DungeonScenes(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    /** queue assets in the asset manager to start async loading. */
    public void queueAssets(AssetManager assets){
        for(int i = 0; i < fileNames.length; i++){
            assets.load(fileNames[i], Model.class);
        }
    }

    /** load models via asset manager (need to call queueAssets() first). */
    public void loadAssets(AssetManager assets){
        assets.finishLoading(); // just in case

        int index = 0;
        sceneAssetFloorA        = assets.get(fileNames[index++], Model.class);
        sceneAssetFloorB        = assets.get(fileNames[index++], Model.class);
        sceneAssetFloorC        = assets.get(fileNames[index++], Model.class);
        sceneAssetWall          = assets.get(fileNames[index++], Model.class);
        sceneAssetWall2         = assets.get(fileNames[index++], Model.class);
        sceneAssetWall3         = assets.get(fileNames[index++], Model.class);
        sceneAssetWall4         = assets.get(fileNames[index++], Model.class);
        sceneAssetWall5         = assets.get(fileNames[index++], Model.class);
        sceneAssetDoorWay       = assets.get(fileNames[index++], Model.class);
        sceneAssetCorner        = assets.get(fileNames[index++], Model.class);
        sceneAssetWallTsplit    = assets.get(fileNames[index++], Model.class);
        sceneAssetWallCrossing  = assets.get(fileNames[index++], Model.class);
        sceneAssetStairs        = assets.get(fileNames[index++], Model.class);
        sceneAssetTorch         = assets.get(fileNames[index++], Model.class);

        sceneAssetFloor  = sceneAssetFloorA;    // alias
    }

    public void selectFloorType(int level){
        // floor type depends on level
        if(level == 0)
            sceneAssetFloor  = sceneAssetFloorA;
        else if (level < 4)
            sceneAssetFloor  = sceneAssetFloorB;
        else
            sceneAssetFloor  = sceneAssetFloorC;
    }

    public void showMap(DungeonMap map, LevelData levelData){
        selectFloorType(levelData.level);
        for(Room room: map.rooms)
            if(!useFogOfWar || levelData.seenRooms.contains(room.id, true)) // only show visited rooms
                showRoom(map, levelData, room);
        showCorridors(map, levelData);
    }

    public void showRoom(DungeonMap map, LevelData levelData, Room room){
        int numTorches = 0;
        room.torchPositions.clear();
        levelData.seenRooms.add(room.id);   // mark this room as seen
        //room.uncovered = true;

        for(int x = room.x; x <= room.x + room.width; x++){ // for every tile of the room
            for(int y = room.y; y <= room.y + room.height; y++){
                levelData.tileSeen[y][x] = true;

                // first the ground tile
                ModelInstance tile;
                TileType cell = map.getGrid(x,y);
                // floor tile, e.g. under wall
                if(cell!= TileType.VOID && cell != TileType.STAIRS_DOWN && cell != TileType.STAIRS_DOWN_DEEP){
                    tile = new ModelInstance(sceneAssetFloor);
                    setTransform(tile.transform, x, y, 0, Direction.NORTH);
                    sceneManager.add(tile);
                }
                // then any walls or other structures for the same tile
                tile = null;
                int z = 0;  // default height (adjusted for stairs)
                if(cell == TileType.WALL){
                    // randomize appearance of wall
                    if(MathUtils.random(1.0f) < 0.1f)
                        tile = new ModelInstance(sceneAssetWall2);
                    else if(MathUtils.random(1.0f) < 0.1f)
                        tile = new ModelInstance(sceneAssetWall3);
                    else if(MathUtils.random(1.0f) < 0.1f)
                        tile = new ModelInstance(sceneAssetWall4);
                    else if(MathUtils.random(1.0f) < 0.1f)
                        tile = new ModelInstance(sceneAssetWall5);
                    else {
                        // randomly put some torches on north and west walls (the ones facing the viewer)
                        //
                        if(numTorches < MAX_TORCHES-1 && (MathUtils.random(1.0f) < 0.5f)
                            && (map.tileOrientation[y][x] == Direction.NORTH || map.tileOrientation[y][x] == Direction.WEST)) {


                            // put a lit torch on the wall
                            numTorches++;
                            ModelInstance torch = new ModelInstance(sceneAssetTorch);
                            setTransform(torch.transform, x, y, z, Direction.opposite(map.tileOrientation[y][x]));
                            sceneManager.add(torch);
                            // keep track of torch positions for the lighting
                            Vector3 pos = new Vector3();
                            torch.transform.getTranslation(pos);
                            pos.y += 3.14f;
                            pos.x += 0.5f;
                            pos.z += 0.5f;
                            room.torchPositions.add( pos );
                        }
                        tile = new ModelInstance(sceneAssetWall);
                    }
                }
                else if(cell == TileType.DOORWAY){
                    tile = new ModelInstance(sceneAssetDoorWay);
                }
                else if(cell == TileType.WALL_CORNER){
                    tile = new ModelInstance(sceneAssetCorner);
                }
                else if(cell == TileType.WALL_T_SPLIT){
                    tile = new ModelInstance(sceneAssetWallTsplit);
                }
                else if(cell == TileType.WALL_CROSSING){
                    tile = new ModelInstance(sceneAssetWallCrossing);
                }
                else if(cell == TileType.STAIRS_DOWN){
                    tile = new ModelInstance(sceneAssetWallCrossing);   // support for stairs from level below
                    z = -8;
                    setTransform(tile.transform, x, y, z, map.tileOrientation[y][x]);
                    sceneManager.add(tile);
                    tile = new ModelInstance(sceneAssetStairs);
                    z = -4;
                }
                else if(cell == TileType.STAIRS_DOWN_DEEP){
                    tile = new ModelInstance(sceneAssetStairs);
                    z = -8;
                }
                else if(cell == TileType.STAIRS_UP){
                    tile = new ModelInstance(sceneAssetStairs);
                    z = 0;
                }
                else if(cell == TileType.STAIRS_UP_HIGH){
                    tile = new ModelInstance(sceneAssetWallCrossing);   // support pillar for stairs
                    setTransform(tile.transform, x, y, z, map.tileOrientation[y][x]);
                    sceneManager.add(tile);
                    tile = new ModelInstance(sceneAssetStairs);
                    z = 4;
                }

                if(tile != null) {
                    setTransform(tile.transform, x, y, z, map.tileOrientation[y][x]);
                    sceneManager.add(tile);
                }
            }
        }
    }

    // The next two methods should be the only place where we convert logical x,y to a transform by applying SCALE
    //
    private void setTransform(Matrix4 transform, int x, int y, float z, Direction dir){
        transform.setToRotation(Vector3.Y, 180-dir.ordinal() * 90);
        transform.setTranslation(SCALE*x, z, -SCALE*y);
    }

    private void setTransform(Matrix4 transform, float x, float y, float z, Direction dir){
        transform.setToRotation(Vector3.Y, 180-dir.ordinal() * 90);
        transform.setTranslation(SCALE*x, z, -SCALE*y);
    }


    // leave orientation as it is
    private void setTransform(Matrix4 transform, int x, int y, float z){
        transform.setTranslation(SCALE*x, z, -SCALE*y);
    }



    // show corridor segment if not seen before, unmask a 3x3 grid of corridors and doorway tiles
    public void visitCorridorSegment(World world, int x, int y){
        for(int dx = -1; dx <= 1; dx++){
            for(int dy = -1; dy <= 1; dy++){
                showCorridorSegment(world, x+dx, y+dy);
            }
        }
    }

    public void showCorridorSegment(World world, int x, int y){
        if(world.levelData.tileSeen[y][x])
            return;
        if( !TileType.hasFloor(world.map.getGrid(x,y)))
            return;

        world.levelData.tileSeen[y][x] = true;

        // add floor tile scene
        ModelInstance tile = new ModelInstance(sceneAssetFloor);
        setTransform(tile.transform, x, y, 0, Direction.NORTH);
        sceneManager.add(tile);

        // add doorway if needed
        // note: could be duplicated by showRoom()
        TileType cell = world.map.getGrid(x,y);
        if(cell == TileType.DOORWAY){
            ModelInstance doorway = new ModelInstance(sceneAssetDoorWay);
            setTransform(doorway.transform, x, y, 0, world.map.tileOrientation[y][x]);
            sceneManager.add(doorway);
        }

        // add any items on floor
        GameObject occupant = world.levelData.gameObjects.getOccupant(x,y);
        if(occupant != null && occupant.scene == null){
            addScene(occupant);
        }
    }

    public void showCorridors(DungeonMap map, LevelData levelData){
        for(int x = 0; x < map.mapWidth; x++){
            for(int y = 0; y < map.mapHeight; y++){
                if(!useFogOfWar || levelData.tileSeen[y][x]){
                    TileType cell = map.getGrid(x,y);
                    if(TileType.hasFloor(cell)){                // todo: also adds tiles for rooms (duped?)
                        ModelInstance tile = new ModelInstance(sceneAssetFloor);
                        setTransform(tile.transform, x, y, 0, Direction.NORTH);
                        sceneManager.add(tile);
                    }
                }
            }
        }
    }

    public void populateMap(World world, LevelData levelData){
        for(GameObject enemy: world.enemies.enemies)
            enemy.scene = null;

        for(int x = 0; x < world.map.mapWidth; x++) {
            for (int y = 0; y < world.map.mapHeight; y++) {
                if(levelData.tileSeen[y][x]) {
                    GameObject occupant = world.levelData.gameObjects.getOccupant(x, y);
                    if (occupant != null && occupant.scene == null) {
                        // note monsters could be seen before outside this room
                        addScene(occupant);
                    }
                }
            }
        }
    }

    public void populateRoom(World world, Room room){
        for(int x = room.x; x < room.x+room.width; x++){
            for(int y = room.y; y < room.y + room.height; y++){
                GameObject occupant = world.levelData.gameObjects.getOccupant(x,y);
                if(occupant != null && occupant.scene == null){
                    // note monsters could be seen before outside this room
                    addScene(occupant);
                }
            }
        }
    }
//
//    private int[] dx = { 0, -1, 1, 0, 0, -1, -1, 1, 1 };
//    private int[] dy = { 0, 0, 0, -1, 1, 1, -1, 1, -1 };
//
//    // drop item at location x,y
//    // if there is already something there of the same type, add it to the pile
//    // if there is something else there try a nearby tile
//    //
//    public void dropObject(DungeonMap map, GameObjects gameObjects, GameObject item, int x, int y){
//
//        for(int offset = 0; offset < 9; offset++){
//            int tx = x+dx[offset];
//            int ty = y+dy[offset];
//            TileType tile = map.getGrid(tx, ty);
//            if(!TileType.droppable(tile))       // don't drop item inside a wall, etc.
//                continue;
//            GameObject occupant = gameObjects.getOccupant(tx,ty);
//            // anything already there?
//            if(occupant == null) {  // empty spot
//                placeObject(gameObjects, item, tx, ty);
//                return;
//            }
//            else if(item.type.isCountable && (occupant.type == item.type || occupant.type.isArrow && item.type.isArrow)) {   // same type
//                occupant.quantity+= item.quantity;        // add to the pile
//                if(occupant.type.isArrow && occupant.quantity > 0){ // change single arrow to bundle
//                    removeScene(occupant);
//                    occupant.type = GameObjectTypes.arrows;
//                    addScene(occupant);
//                }
//                return;
//            }
//        }
//        // can't place
//        MessageBox.addLine(item.type.name+ " dropped and disappeared.");
//    }
//
//    public void placeObject(GameObjects gameObjects, GameObjectType type, int x, int y){
//        GameObject go = new GameObject(type, x, y, Direction.SOUTH);
//        placeObject(gameObjects, go, x, y);
//    }
//
//    public void placeObject(GameObjects gameObjects, GameObject item, int x, int y){
//        item.x = x;
//        item.y = y;
//        item.z = item.type.z;
//
//        addScene(item);
//        gameObjects.add(item);
//        if(!item.type.isPlayer)
//            gameObjects.setOccupant(x, y, item);
//        // how to handle enemies walking over gold etc.
//    }
//
    public void addScene(GameObject gameObject){
        ModelInstance item = null;
        if(gameObject.type.isPlayer)
            item = new ModelInstance(gameObject.type.sceneAsset, "Scene", "Rig");
        else
            item = new ModelInstance(gameObject.type.sceneAsset);
        setTransform(item.transform, gameObject.x, gameObject.y, gameObject.z, gameObject.direction);
        sceneManager.add(item);
        gameObject.scene = item;
//        if(gameObject.type.isEnemy)
//            gameObject.scene.animationController.setAnimation("Idle", -1);
    }


    public void removeScene(GameObject gameObject){
        sceneManager.remove(gameObject.scene);
        gameObject.scene = null;
    }

    public void createRogueModel(World world){
        GameObject rogue = world.rogue;
        addScene(rogue);
        //adaptModel(rogue.scene, rogue.stats);
        //rogue.scene.animationController.setAnimation("Idle", -1);


//        String armature = "Rig";
//        if(armature != null) {
//            Gdx.app.log("GameObjectType",  " armature: "+armature + " animations: "+rogue.scene.modelInstance.animations.size);
//            for(int i = 0; i < rogue.scene.modelInstance.animations.size; i++) {
//                String id = rogue.scene.modelInstance.animations.get(i).id;
//                Gdx.app.log(" animation :", id);
//            }
//        }
    }
//
//    public void attachModel(Scene character, String nodeName, GameObject item){
//        if(item == null || item.scene == null)
//            return;
//        for(Node node : character.modelInstance.nodes){
//            attachToNode( node, nodeName, item.scene);
//        }
//    }
//
//    // recursive method to attach weapons
//    private void attachToNode( Node node, String nodeName, Scene weapon ){
//
//        if(node.id.contentEquals(nodeName)){
//            node.addChild(weapon.modelInstance.nodes.first());
//        }
//        else {
//            for (Node n : node.getChildren()) {
//                attachToNode( n, nodeName, weapon);
//            }
//        }
//    }
//    public void detachModel(Scene character, String nodeName, GameObject item){
//        if(item == null || item.scene == null)
//            return;
//        for(Node node : character.modelInstance.nodes){
//            detachFromNode( node, nodeName, item.scene);
//        }
//    }
//
//    // recursive method to attach weapons
//    private void detachFromNode( Node node, String nodeName, Scene weapon ){
//
//        if(node.id.contentEquals(nodeName)){
//            node.removeChild(weapon.modelInstance.nodes.first());
//        }
//        else {
//            for (Node n : node.getChildren()) {
//                detachFromNode( n, nodeName, weapon);
//            }
//        }
//    }
//
    // mark the room or corridor segment where Rogue is as 'uncovered'
    public void uncoverAreaInPlayerView(World world){
        int roomId = world.map.roomCode[world.rogue.y][world.rogue.x];
        if(roomId >= 0) {
            Room room = world.map.rooms.get(roomId);
            //room.uncovered = true;
            world.levelData.seenRooms.add(roomId);
        }
        else
            visitCorridorSegment(world, world.rogue.x, world.rogue.y);
    }




//
//    // obsolete
//    public void adaptModel(Scene rogue, CharacterStats stats){
//        ModelInstance instance = rogue.modelInstance;
//        for(Node node : instance.nodes){
//            //checkNode(1, node, stats.weaponItem);
//            checkNode(1, node, null);
//        }
//
//        attachModel(rogue, "handslot.l",  stats.armourItem);
//        attachModel(rogue, "handslot.r",  stats.weaponItem);
//    }
//
//    // recursive method to enable/disable weapons
//    private void checkNode(int level, Node node, GameObject weapon ){
//        //Gdx.app.log("Node", "level "+ level + " : "+node.id+ " nodeparts: "+node.parts.size);
//        if(node.id.contentEquals("Knife"))
//            setNodeParts(node, (weapon != null && weapon.type == GameObjectTypes.knife));
//        else if(node.id.contains("Knife_Offhand"))
//            setNodeParts(node, false);
//        else if(node.id.contains("Crossbow"))
//            setNodeParts(node, (weapon != null && weapon.type == GameObjectTypes.crossbow));
//        else if(node.id.contains("Throwable"))
//            setNodeParts(node, (weapon != null && weapon.type == GameObjectTypes.explosive));
//
//        for(Node n : node.getChildren()){
//            checkNode(level+1, n, weapon);
//        }
//    }
//
//    private void setNodeParts(Node node, boolean enabled){
//        for(NodePart part : node.parts)
//            part.enabled = enabled;
//    }
//
//
    public void turnObject(GameObject go, Direction dir, int x, int y ){
        go.direction = dir;
        if(go.scene != null)
            setTransform(go.scene.transform, x, y, go.z, dir);
    }

    public void moveObject(GameObject go, int x, int y, float z){
        go.x = x;
        go.y = y;
        go.z = z;

        if(go.scene != null)    // characters can move offscreen, only update model instance if it exists
            setTransform(go.scene.transform, x, y, z);
    }

    public void remove(ModelInstance scene){
        if(scene != null)
            sceneManager.remove(scene);
    }

    public void clear(){
        sceneManager.clear();
    }


}
