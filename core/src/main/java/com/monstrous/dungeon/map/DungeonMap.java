package com.monstrous.dungeon.map;

import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ShortArray;


// This implements an algorithm described by Vazgriz (only for the 2D case) to generate dungeons
//
// https://vazgriz.com/119/procedurally-generated-dungeons/
// https://www.youtube.com/watch?v=rBY2Dzej03A
//
// 1. Place rooms at random
// 2. Use Delaunay triangulator to connect the rooms
// 3. Find minimum spanning tree to find a minimal tree to connect all rooms
// 4. Add some random edges to make it less minimal and to allow for some loops
// 5. Use A* to turn edges into corridors with a bias to join to existing corridors
//
// Multiple levels are connected by staircases.


public class    DungeonMap implements Disposable {
    public static final int MIN_SIZE = 4;           // min size of room
    public static final int MAX_SIZE = 6; //12;          // max size of room
    public static final float LOOP_FACTOR = 0.125f; // probability factor [0..1] to add some extra non-MST edge to paths

    public final int mapWidth, mapHeight;
    public final Array<Room> rooms;
    public int roomId;                      // to give each room a unique id
    public float[] vertices;                // array of x,y per room centre
    public ShortArray indices;              // index list from triangulation
    private TileType[][] grid;             // map grid for fixed architecture, walls, etc.
    public Direction [][] tileOrientation;      // orientation of tile
    public int[][] roomCode;                // id of the room

    // levelNr : 0 for top level, increasing as we go down
    // stairPortals: in= staircases from level above, out= staircases to level below
    //
    public DungeonMap(int mapSeed, int levelNr, int width, int height, Array<Room> stairsFromAbove, Array<Room>stairsToBelow) {
        this.mapWidth = width;
        this.mapHeight = height;
        rooms = new Array<>();
        roomId = 0;

        System.out.println("Generating dungeon map for level "+levelNr);

        connectStairWells(stairsFromAbove);  // connect to stairs coming down

        MathUtils.random.setSeed(getLevelSeed(mapSeed, levelNr));

        // generate stairs to the level below
        generateStairWells(mapWidth, mapHeight, stairsToBelow);    // stairs going down

        generateRooms(rooms);

        addGraph(rooms);

        connectRooms();

        findMinimumSpanningTree();

        addLoopEdges();

        fillGrid();

        makeCorridors();

        //addCorridorWalls();
    }

    // derive seed for a specific level of a map
    private int getLevelSeed(int mapSeed, int level){
        return 100* mapSeed + level;
    }

    public TileType getGrid(int x, int y){
        return grid[y][x];
    }

    // generate non-overlapping rooms of random size and position until the map is pretty full
    private void generateRooms(Array<Room> rooms){

        int attempts = 0;
        while(attempts < 150) {       // stop after N attempts to place a random room, the map must be quite full
            Room room = generateRoom(roomId);
            boolean overlap = checkOverlap(room, rooms);
            boolean adjacent = false;
            if(!overlap)
                adjacent = checkAdjacency(room, rooms);
            if(!overlap && !adjacent) {
                rooms.add(room);
                attempts = 0;
                roomId++;
            }
            else
                attempts++;
        }
    }


    private Room generateRoom(int id){
        int w = MathUtils.random(MIN_SIZE, MAX_SIZE);
        int h = MathUtils.random(MIN_SIZE, MAX_SIZE);
        return placeRoom(id, w, h);
    }


    // place some stairs going up to match stairs coming down
    private void connectStairWells( Array<Room> stairPortals){
        roomId = 0;
        for(Room stairPortal : stairPortals) {
            Room stairWell = connectStairWell(roomId, stairPortal);
            rooms.add(stairWell);
            roomId++;
        }
    }

    private Room connectStairWell( int roomId, Room stairPortal ){
        Direction direction = Direction.opposite( stairPortal.stairsDirection); // staircase goes opposite direction to one coming down, e.g. North becomes South

        int x = stairPortal.x;
        int y = stairPortal.y;
        // offset stairwell one cell (the landing) compared to the floor above
        switch(direction){
            case NORTH:     y--; break;
            case EAST:      x--; break;
            case SOUTH:     y++; break;
            case WEST:      x++; break;
        }
        Room stairWell =  new Room(roomId, x, y, stairPortal.width, stairPortal.height);
        stairWell.stairsDirection = direction;
        stairWell.isStairWell = true;
        stairWell.stairType = TileType.STAIRS_UP;

        // set 'centre' to the landing, this is where paths will connect to
        switch(stairWell.stairsDirection) {
            case NORTH:
            case EAST:     // pointing east
                stairWell.centre.set(stairWell.x, stairWell.y); // centre connection node on the landing
                break;
            case SOUTH:
                stairWell.centre.set(stairWell.x, stairWell.y+2); // centre connection node on the landing
                break;
            case WEST:     // pointing west
                stairWell.centre.set(stairWell.x+2, stairWell.y); // centre connection node on the landing
                break;
        }
        return stairWell;
    }

    // place some stair wells going down
    private void generateStairWells( int mapW, int mapH, Array<Room> stairPortals){
        stairPortals.clear();
        int count = MathUtils.random(1, 2); // how many stair wells to generate?
        while(count > 0){
            Room stairWell = generateStairWell(roomId,  mapW, mapH);

            boolean overlap = checkOverlap(stairWell, rooms);
            if(!overlap) {
                rooms.add(stairWell);
                stairPortals.add(stairWell);
                roomId++;
                count--;
            }
        }
    }

    // a stair well is a special type of room of fixed size with stair tiles inside.
    private Room generateStairWell(int id, int mapW, int mapH){
        int d = MathUtils.random(0, 3); // random direction NESW
        Direction direction = Direction.values()[d];

        // place horizontal or vertical
        int w = (direction == Direction.EAST || direction == Direction.WEST) ? 3 : 1;
        int h = (direction == Direction.EAST || direction == Direction.WEST) ? 1 : 3;
        Room stairWell = placeRoom(id, w, h, mapW, mapH);
        stairWell.stairsDirection = direction;
        stairWell.stairType = TileType.STAIRS_DOWN;
        stairWell.isStairWell = true;

        switch(stairWell.stairsDirection) {
            case NORTH:
            case EAST:     // pointing east
                stairWell.centre.set(stairWell.x, stairWell.y); // centre connection node on the landing
                break;
            case SOUTH:
                stairWell.centre.set(stairWell.x, stairWell.y+2); // centre connection node on the landing
                break;
            case WEST:     // pointing west
                stairWell.centre.set(stairWell.x+2, stairWell.y); // centre connection node on the landing
                break;
        }

        return stairWell;
    }

    private Room placeRoom(int id, int w, int h){
        return placeRoom(id, w, h, mapWidth, mapHeight);
    }

    private Room placeRoom(int id, int w, int h, int mapW, int mapH){
        // leave three cell margin from the edge of the map for the walls to go and to allow for corridors on the outside with a wall of its own
        int x = MathUtils.random(3, mapW - (w+4));
        int y = MathUtils.random(3, mapH - (h+4));
        return new Room(id, x, y, w, h);
    }



    private boolean checkOverlap(Room newRoom, Array<Room> rooms){
        for(Room room : rooms ){
            if(room.overlaps(newRoom))
                return true;
        }
        return false;
    }

    // avoid rooms that are immediately next to each other with 2 separate walls.  They should share a common wall or it looks weird.
    //
    private boolean checkAdjacency(Room newRoom, Array<Room> rooms){

        for(Room room : rooms ){
            if(room.x+room.width == newRoom.x-1)
                return true;
            if(room.x-1 == newRoom.x+newRoom.width)
                return true;
            if(room.y+room.height == newRoom.y-1)
                return true;
            if(room.y-1 == newRoom.y+newRoom.height)
                return true;
        }
        return false;
    }

    // Triangulate all room centres using Delaunay
    // The triangulation is represented by vertices + indices.
    private void addGraph(Array<Room> rooms){
        DelaunayTriangulator triangulator = new DelaunayTriangulator();

        vertices = new float[2*rooms.size];
        int index = 0;
        for(Room room : rooms ){
            vertices[index++] = room.centre.x;
            vertices[index++] = room.centre.y;
        }

        indices = triangulator.computeTriangles(vertices, 0, 2*rooms.size, false);
    }

    // Use the triangulation data to connect joining rooms
    // This translates the graphical triangle data to logical node connections
    private void connectRooms(){
        for( int tri = 0; tri < indices.size; tri+= 3 ){
            int i1 = indices.get(tri);
            int i2 = indices.get(tri+1);
            int i3 = indices.get(tri+2);
            float x1 = vertices[2*i1];
            float y1 = vertices[2*i1+1];
            float x2 = vertices[2*i2];
            float y2 = vertices[2*i2+1];
            float x3 = vertices[2*i3];
            float y3 = vertices[2*i3+1];

            Room r1 = findRoomByPosition(x1, y1);
            Room r2 = findRoomByPosition(x2, y2);
            Room r3 = findRoomByPosition(x3, y3);
            r1.addNeighbour(r2);
            r1.addNeighbour(r3);
            r2.addNeighbour(r1);
            r2.addNeighbour(r3);
            r3.addNeighbour(r1);
            r3.addNeighbour(r2);

        }
    }

    private Room findRoomByPosition(float x, float y){
        for(Room room : rooms ){
            if( MathUtils.isEqual(room.centre.x, x, 0.1f) &&  MathUtils.isEqual(room.centre.y, y, 0.1f))
                return room;
        }
        throw new RuntimeException("Room cannot be found by position");
    }

    private void findMinimumSpanningTree(){
        // Using Prim-Dijkstra algorithm
        //
        Array<Room> connected = new Array<>();      // the tree, initially empty
        Array<Room> unconnected = new Array<>();    // nodes not in the tree, initially all of them
        for(Room room : rooms )
            unconnected.add(room);

        // choose random room to start with
        int root = MathUtils.random(0, rooms.size-1);


        Room closestRoom = rooms.get(root);

        while(closestRoom != null) {

            // move room from unconnected to connected, because now it is in the tree
            unconnected.removeValue(closestRoom, true);
            connected.add(closestRoom);


            // find unconnected room with the smallest distance to (any branch in) the tree
            closestRoom = null;
            Room connectingBranch = null;
            float smallestDistance = Float.MAX_VALUE;
            for (Room node : connected) {
                for (int i = 0; i < node.nbors.size; i++) {
                    Room nbor = node.nbors.get(i);
                    if (connected.contains(nbor, true))  // neighbour already in the tree? skip
                        continue;
                    float distance = node.distances.get(i);
                    if (distance < smallestDistance) {    // closest so far
                        smallestDistance = distance;
                        closestRoom = nbor;
                        connectingBranch = node;
                    }
                }
            }
            if(closestRoom == null)
                break;

            // store the link from both sides
            closestRoom.addCloseNeighbour(connectingBranch);
            connectingBranch.addCloseNeighbour(closestRoom);
        }
    }

    // Add some random edges from the triangulation to the ones selected by the minimum spanning tree
    // to allow for some loops and more interesting connectivity.
    //
    private void addLoopEdges(){
        for(Room room : rooms ){
            for(Room nbor : room.nbors ){
                if(room.closeNeighbours.contains(nbor, true))       // is already a close neighbour
                    continue;
                if(MathUtils.random(1.0f) < LOOP_FACTOR) { // with some probability
                    room.addCloseNeighbour(nbor);
                    nbor.addCloseNeighbour(room);
                }
            }
        }
    }

    private void fillGrid(){
        grid = new TileType[mapHeight][mapWidth];
        tileOrientation = new Direction[mapHeight][mapWidth];
        roomCode = new int[mapHeight][mapWidth];

        // init whole grid to empty space
        for(int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                grid[y][x] = TileType.VOID;
                tileOrientation[y][x] = Direction.NORTH;
                roomCode[y][x] = -1;
            }
        }

        for(Room room : rooms ){
            if(room.isStairWell)
                addStairWell(room);
            else
                addRoom(room);
        }
    }

    private void addStairWell(Room room){

        int rx = room.x;
        int ry = room.y;
        int rw = room.width;
        int rh = room.height;


        Direction dir = room.stairsDirection;
        if(room.stairType == TileType.STAIRS_UP)
            dir = Direction.opposite(dir);
        for(int x = 0; x < rw; x++){
            for(int y = 0; y < rh; y++){
                roomCode[ry+y][rx+x] = room.id;
                tileOrientation[ry+y][rx+x] = dir;
            }
        }

        TileType t2 = TileType.STAIRS_DOWN_DEEP;
        if(room.stairType == TileType.STAIRS_UP)
            t2 = TileType.STAIRS_UP_HIGH;

        switch(room.stairsDirection){
            case NORTH:
                grid[room.y][room.x] = TileType.ROOM;
                grid[room.y+1][room.x] = room.stairType;
                grid[room.y+2][room.x] = t2;
                break;
            case SOUTH:
                grid[room.y+2][room.x] = TileType.ROOM;
                grid[room.y+1][room.x] = room.stairType;
                grid[room.y][room.x] = t2;
                break;
            case EAST:
                grid[room.y][room.x] = TileType.ROOM;
                grid[room.y][room.x+1] = room.stairType;
                grid[room.y][room.x+2] = t2;
                break;
            case WEST:
                grid[room.y][room.x+2] = TileType.ROOM;
                grid[room.y][room.x+1] = room.stairType;
                grid[room.y][room.x] = t2;
                break;
        }


    }

    private void addRoom(Room room){

        int rx = room.x;
        int ry = room.y;
        int rw = room.width;
        int rh = room.height;

        for(int x = 0; x <= rw; x++){
            for(int y = 0; y <= rh; y++){
                roomCode[ry+y][rx+x] = room.id;
            }
        }

        // the walls are placed around the perimeter (inside the room)
        // so effective size of the room area is (rw-2)*(rh-2)
        //
        for(int x = 1; x < rw; x++){
            for(int y = 1; y < rh; y++){
                grid[ry+y][rx+x] = TileType.ROOM;
            }
        }

        for(int x = 1; x < rw; x++){
            placeWall(rx+x, ry, Direction.SOUTH);
            placeWall(rx+x, ry+rh, Direction.NORTH);
        }
        for(int y = 1; y < rh; y++){
            placeWall(rx, ry+y, Direction.WEST);
            placeWall(rx+rw, ry+y, Direction.EAST);
        }
        // rotate the wall corner model as needed
        //
        //    E---S
        //    |   |
        //    N---W
        //
        placeCorner(rx, ry, Direction.NORTH);
        placeCorner(rx, ry+rh, Direction.EAST);
        placeCorner(rx+rw, ry, Direction.WEST);
        placeCorner(rx+rw, ry+rh, Direction.SOUTH);
    }

    private void placeWall(int x, int y, Direction dir){
        if(grid[y][x] != TileType.VOID) {
            if(grid[y][x] == TileType.WALL && (tileOrientation[y][x] == dir || tileOrientation[y][x] == Direction.opposite(dir)) )
                return;     // aligned with existing wall
//            System.out.println("Wall into non-void " + x + " , " + y + " type:" + grid[y][x]);
            grid[y][x] = TileType.WALL_T_SPLIT;
            tileOrientation[y][x] = dir;
        } else {
            grid[y][x] = TileType.WALL;
            tileOrientation[y][x] = dir;
        }
    }

    private void placeCorner(int x, int y, Direction dir){
        if(grid[y][x] != TileType.VOID) {
//            System.out.println("Corner into non-void " + x + " , " + y + " type:" + grid[y][x]+" in dir "+tileOrientation[y][x]);
            if(grid[y][x] == TileType.WALL)
                grid[y][x] = TileType.WALL_T_SPLIT; // keep wall orientation
            else if (grid[y][x] == TileType.WALL_CORNER)    // 2 corners            should sometime be a T
                grid[y][x] = TileType.WALL_CROSSING;
        } else {
            grid[y][x] = TileType.WALL_CORNER;
            tileOrientation[y][x] = dir;
        }
    }

    private void addDoor(int x, int y, Direction direction){
        grid[y][x] = TileType.DOORWAY; tileOrientation[y][x] = direction;
    }

    private void makeCorridors(){
        for(Room room : rooms){
            for(Room nbor : room.closeNeighbours){
                if(room.id < nbor.id)   // avoid doing edges twice
                    makeCorridor(room, nbor);
            }
        }
    }

    private static class Node{
        int x, y;
        int cost;
        Node parent;

        public Node(int x, int y, int cost, Node parent) {
            this.x = x;
            this.y = y;
            this.cost = cost;
            this.parent = parent;
        }
    }

    private final int[] dx = { 0, 1, 0, -1 };
    private final int[] dy = { 1, 0, -1, 0 };

    private void makeCorridor(Room A, Room B){
        int x = A.centre.x;
        int y = A.centre.y;
        int targetX = B.centre.x;
        int targetY = B.centre.y;
        Node root = new Node(x, y, 0, null);
        Array<Node> closed = new Array<>();
        Array<Node> fringe = new Array<>();

        fringe.add(root);

        while(fringe.size > 0){

            // find closest node in the fringe
            int minCost = Integer.MAX_VALUE;
            Node current = null;
            for(Node n : fringe){
                if(n.cost < minCost){
                    minCost = n.cost;
                    current = n;
                }
            }
            assert current != null;

            // did we reach the goal?
            if(current.x == targetX && current.y == targetY) { // found target
                // back trace the steps and update the grid
                while(current != null) {
                    if (grid[current.y][current.x] == TileType.VOID)
                        grid[current.y][current.x] = TileType.CORRIDOR;
                    if (grid[current.y][current.x] == TileType.WALL  )
                        grid[current.y][current.x] = TileType.DOORWAY;
                    current = current.parent;
                }
                return; // finished
            }

            // remove node from fringe and add it to the closed set
            fringe.removeValue(current, true);
            closed.add(current);

            // for each neighbour of current
            for(int dir = 0; dir < 4; dir++){
                int nx = current.x + dx[dir];
                int ny = current.y + dy[dir];
                if(nx < 0 || nx >= mapWidth || ny < 0 || ny >= mapHeight)
                    continue;

                // if neighbour in closed set, skip it
                boolean found = false;
                for(Node n : closed){
                    if(n.x == nx && n.y == ny) {
                        found = true;
                        break;
                    }
                }
                if(found)
                    continue;

                // calculate cost to neighbour via current
                int cost = current.cost;
                switch(grid[ny][nx]){
                    case VOID:     cost += 5; break;
                    case ROOM:      cost += 10; break;
                    case CORRIDOR:  cost += 1; break;       // preferred
                    case WALL:      cost += 20; break;

                    // never path through the following tiles:
                    case WALL_CORNER:
                    case WALL_T_SPLIT:
                    case WALL_CROSSING:
                    case STAIRS_DOWN:
                    case STAIRS_DOWN_DEEP:
                    case STAIRS_UP:
                    case STAIRS_UP_HIGH:
                        cost += 500; break;
                }
                Node nbor = null;
                for(Node n : fringe){
                    if(n.x == nx && n.y == ny) {
                        nbor = n;
                        break;
                    }
                }
                if(nbor == null) {
                    nbor = new Node(nx, ny, cost, current);
                    fringe.add(nbor);
                } else {
                    if(cost < nbor.cost){
                        nbor.cost = cost;       // found a shorter path for neighbour
                        nbor.parent = current;
                    }
                }
            }
        }
    }

    private final int[] ddx = { -1 , 0, 1, -1, 1, -1, 0, 1 };
    private final int[] ddy = { -1, -1, -1, 0, 0, 1, 1, 1 };


    // put walls around corridors where necessary, i.e. where next to an empty cell, including diagonals
    private void addCorridorWalls(){
        for(int x = 0; x < mapWidth; x++){
            for(int y = 0; y < mapHeight; y++){
                if(grid[y][x] == TileType.CORRIDOR){
                    for(int dir = 0; dir < 8; dir++){
                        if(grid[y+ddy[dir]][x+ddx[dir]] == TileType.VOID) {
                            if(dir == 1 || dir == 3 || dir == 4 || dir == 6){
                                grid[y + ddy[dir]][x + ddx[dir]] = TileType.WALL;
                                if(dir == 3 || dir ==4)
                                    tileOrientation[y + ddy[dir]][x + ddx[dir]] = Direction.EAST;
                            }
//                            else {
//                                grid[y + ddy[dir]][x + ddx[dir]] = CORNER;
//                            }
//                            if(dir == 0 || dir == 3 || dir == 4 || dir == 5)
//                                orientation[y + ddy[dir]][x + ddx[dir]] = 1;

                        }
                    }
                }
            }
        }
    }


    @Override
    public void dispose() {
        indices.clear();
    }
}
