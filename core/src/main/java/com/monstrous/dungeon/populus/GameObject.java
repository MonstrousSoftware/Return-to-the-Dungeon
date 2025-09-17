package com.monstrous.dungeon.populus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.monstrous.dungeon.MessageBox;
import com.monstrous.dungeon.Sounds;
import com.monstrous.dungeon.World;
import com.monstrous.dungeon.map.Direction;
import com.monstrous.dungeon.map.TileType;
import com.monstrous.dungeon.render.DungeonScenes;


public class GameObject {

    public GameObjectType type;
    public int x, y;
    public float z;       // above/below ground level, e.g. when walking stairs
    public Direction direction;
    public ModelInstance scene;
    public CharacterStats stats;        // only for rogue and enemies
    public int quantity;                // e.g. amount of gold for a gold object
    public GameObject attackedBy;       // normally null
    public int protection;              // for armour
    public int damage;                  // for weapons
    public int accuracy;                // for weapons
    public boolean hasFocus;


    public GameObject(GameObjectType type, int quantity) {
        this(type, 0, 0, Direction.NORTH);   // for object in inventory we don't care about position
        this.quantity = quantity;
    }

    public GameObject(GameObjectType type, int x, int y, Direction direction) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.quantity = 1;
        this.attackedBy = null;
        this.protection = 0;
        this.damage = 0;
        this.accuracy = 0;
    }

    // NPC step
    public void step(World world, DungeonScenes scenes) {
        if (attackedBy != null) {     // don't move while being attacked
            defend(world, scenes);
            return;
        }

        if (lookForLoot(world, scenes))
            return;


        // warrior switches aggression on and off
        if (type == GameObjectTypes.warrior) {
            if (MathUtils.random(20) < 1)
                stats.aggressive = !stats.aggressive;
        }


        if (stats.aggressive && world.rogue.stats.hitPoints > 0 && MathUtils.random(2) > 1) {
            // move towards the player

            int dx = (int) Math.signum(world.rogue.x - x);
            int dy = (int) Math.signum(world.rogue.y - y);
            if (dx != 0 && dy != 0) { // avoid diagonals
                if (MathUtils.random(1) > 0)
                    dx = 0;
                else
                    dy = 0;
            }
            Direction dir = Direction.NORTH;
            if (dx < 0)
                dir = Direction.WEST;
            else if (dx > 0)
                dir = Direction.EAST;
            else if (dy < 0)
                dir = Direction.SOUTH;
            tryMove(world, scenes, dx, dy, dir);
        } else {
            int action = MathUtils.random(0, 3);
            switch (action) {
                // left/right keys translate to -x/+x
                // up/down to +y/-y
                //
                case 0:
                    tryMove(world, scenes, 0, 1, Direction.NORTH);
                    break;
                case 1:
                    tryMove(world, scenes, -1, 0, Direction.WEST);
                    break;
                case 2:
                    tryMove(world, scenes, 0, -1, Direction.SOUTH);
                    break;
                case 3:
                    tryMove(world, scenes, 1, 0, Direction.EAST);
                    break;
            }
        }
    }

    // check for valuables nearby and move towards it
    public boolean lookForLoot(World world, DungeonScenes scenes) {
        if (grabLoot(world, scenes, -1, 0, Direction.WEST))
            return true;
        if (grabLoot(world, scenes, 1, 0, Direction.EAST))
            return true;
        if (grabLoot(world, scenes, 0, 1, Direction.NORTH))
            return true;
        if (grabLoot(world, scenes, 0, -1, Direction.SOUTH))
            return true;
        return false;
    }

    public boolean grabLoot(World world, DungeonScenes scenes, int dx, int dy, Direction dir) {
        GameObject occupant = world.levelData.gameObjects.getOccupant(x + dx, y + dy);
        TileType tile = world.map.getGrid(x + dx, y + dy);
        if (occupant != null && (occupant.type.isGold || occupant.type.isWeapon || occupant.type.isArmour || occupant.type.isArrow || tile == TileType.DOORWAY)) {
            // gravitate towards doorways to encourage exploration (not sure this helps)

            tryMove(world, scenes, dx, dy, dir);
            return true;
        }
        return false;
    }

    public void defend(World world, DungeonScenes scenes) {
        fight(world, scenes, attackedBy);
        attackedBy = null;
    }

    public void tryMove(World world, DungeonScenes scenes, int dx, int dy, Direction dir) {

        scenes.turnObject(this, dir, x, y);    // turn towards moving direction
        int tx = x + dx;
        int ty = y + dy;
        TileType from = world.map.getGrid(x, y);
        TileType cell = world.map.getGrid(tx, ty);
        if (!TileType.walkable(cell, from)) {
            // todo
//            if(scene != null) {
//                scene.animationController.setAnimation(null);   // remove previous animation
//                scene.animationController.setAnimation("Idle", 1);
//            }
            return;     // don't move to non walkable cell
        }


        // what is in the target cell? can be enemy, pickup or nothing
        GameObject occupant = world.levelData.gameObjects.getOccupant(tx, ty);

        if (occupant != null && occupant.type == GameObjectTypes.bigSword && !type.isPlayer) {  // don't let monsters pick up sword or walk over sword
            return;
        }


//        if(type.isPlayer)
//            System.out.println("Occupant: "+(occupant == null? "--" : occupant.type.name));

        GameObject opponent = null;
        if (occupant != null && occupant.type.isEnemy) {
            opponent = occupant;
        }
        if (!type.isPlayer && tx == world.rogue.x && ty == world.rogue.y && world.rogue.stats.hitPoints > 0) {
            opponent = world.rogue;
        }
        if (opponent != null) {
            if (type == GameObjectTypes.imp && MathUtils.random(6) >= 3)
                rob(world, opponent);
            else
                fight(world, scenes, opponent);
            return;
        }

        // vacate old tile
        if (!type.isPlayer) {
            world.levelData.gameObjects.clearOccupant(x, y);
        }
        // move to new tile
        x = tx;
        y = ty;
        switch (world.map.getGrid(x, y)) {
            case STAIRS_DOWN:
                z = -2;
                break;
            case STAIRS_DOWN_DEEP:
                z = -6;
                break;
            case STAIRS_UP:
                z = 2;
                break;
            case STAIRS_UP_HIGH:
                z = 6;
                break;
            default:
                z = 0;
                break;
        }
        scenes.moveObject(this, x, y, z);

        boolean pickingUpAnimation = false;
        if (occupant != null && occupant.type.pickup) {
            pickUp(world, scenes, occupant);
            pickingUpAnimation = true;

        }
        if (!type.isPlayer) {
            world.levelData.gameObjects.setOccupant(x, y, this);

            // if enemy goes into fog of war, hide it
            if (scene != null && !world.levelData.tileSeen[y][x]) {
                scenes.removeScene(this);
            }
            // and vice versa
            if (scene == null && world.levelData.tileSeen[y][x]) {
                scenes.addScene(this);
            }
        }
//        if(scene != null && !pickingUpAnimation) {
//            scene.animationController.setAnimation(null);   // remove previous animation
//            scene.animationController.setAnimation("Walking_A", 1);
//        }

    }

    // this character will steal all victim's gold
    private void rob(World world, GameObject victim) {

        int amount = victim.stats.inventory.removeGold();
        if (amount > 0) {
            GameObject gold = new GameObject(GameObjectTypes.gold, amount);
            stats.inventory.addItem(gold);
            if (hasFocus || victim.hasFocus || world.rogue.stats.increasedAwareness > 0) {
                Sounds.pickup();
                MessageBox.addLine(type.name + " stole " + amount + " gold from " + victim.type.name);
            }
        }
    }


    private void pickUp(World world, DungeonScenes scenes, GameObject item) {
        Gdx.app.log("Pickup", item.type.name);

        if (stats.inventory.addItem(item)) {  // if there is room in the inventory
            if (scene != null) {
//                scene.animationController.setAnimation(null);   // remove previous animation
//                scene.animationController.setAnimation("PickUp", 1);
            }

            String name = type.name;
            if (type.isPlayer)
                name = "You";

            // with increased awareness player is informed of all events
            // otherwise report only on player actions
            //
            if (hasFocus || world.rogue.stats.increasedAwareness > 0) {
                Sounds.pickup();
                if (item.type.isCountable)
                    MessageBox.addLine(name + " picked up " + item.quantity + " " + item.type.name);
                else
                    MessageBox.addLine(name + " picked up " + item.type.name);
            }
            if (item.scene != null)
                scenes.remove(item.scene);
            world.levelData.gameObjects.clearOccupant(x, y);
            world.levelData.gameObjects.remove(item);

            if (!type.isPlayer)
                autoEquip(scenes, item);
            if (item.type == GameObjectTypes.bigSword) {
                MessageBox.addLine("This is what you came for!");
                MessageBox.addLine("Now return it to the start.");
//                if(scene!= null) {
//                    scene.animationController.setAnimation(null);   // remove previous animation
//                    scene.animationController.setAnimation("Cheer", 3);
            }
        }
    }


    // used by enemies to equip weapons or armour
    // they will always equip the best weapon/armour
    //
    private void autoEquip( DungeonScenes scenes, GameObject item ){

        if(item.type.isArmour){
            GameObject prev = stats.armourItem;
            // if it better that currently equipped one? or nothing equipped yet?
            if(prev == null || item.protection > prev.protection) {
                stats.armourItem = item;
                // remove item from inventory
                stats.inventory.removeItem(item);
                // put old one back in inventory
                if (prev != null) {
//                    if(scene !=null)
//                        scenes.detachModel(scene, "handslot.l", prev);
                    stats.inventory.addItem(prev);
                }
//                if(scene!=null)
//                    scenes.attachModel(scene, "handslot.l",  item);
            }
        } else if(item.type.isWeapon){
            GameObject prev = stats.weaponItem;
            if(prev == null || item.damage > prev.damage) {
                stats.weaponItem = item;
                stats.inventory.removeItem(item);
//                if (prev != null) {
//                    if(scene !=null)
//                        scenes.detachModel(scene, "handslot.r", prev);
//                    stats.inventory.addItem(prev);
//                }
//                if(scene !=null)
//                    scenes.attachModel(scene, "handslot.r",  item);
            }
        }
    }

    private void fight(World world, DungeonScenes scenes, GameObject other) {

        if(other.stats.hitPoints<=0)
            return; // don't fight a corpse

        if(other == this)
            Gdx.app.error("fight", "Fighting oneself");

        Gdx.app.log("fight", type.name+" hp:"+stats.hitPoints+" vs "+other.type.name+" hp:"+other.stats.hitPoints);

        if (type.isPlayer || other.type.isPlayer) {
            GameObject enemy = this;
            if (type.isPlayer)
                enemy = other;

            System.out.println("enemy "+enemy.type.name+ " xp:" + enemy.stats.experience + " hp:" + enemy.stats.hitPoints + " wp:" + (enemy.stats.weaponItem==null?"none":enemy.stats.weaponItem.type.name) +
                " armour:" + (enemy.stats.armourItem == null?"none":enemy.stats.armourItem.type.name));
        }

        if(scene != null){
//            scene.animationController.setAnimation(null);   // remove previous animation
//            scene.animationController.setAnimation("Unarmed_Melee_Attack_Punch_A", 1);
        }

        other.attackedBy = this;

        // accuracy determines if the attack even hits
        // 40% baseline + up to 40% on XP + a few percent from weapon
        // where XP impact maxes out from 200 onwards, i.e. 10 warrior kills
        //
        int accuracy = 40 + Math.min(stats.experience/10, 55);
        if(stats.weaponItem != null && !stats.weaponItem.type.isRangeWeapon)
            accuracy += stats.weaponItem.accuracy;

        // experienced enemies can dodge the attack
        int defensiveSkills = Math.min(other.stats.experience/10, 20);
        int rnd = MathUtils.random(90 + defensiveSkills);
        System.out.println("accuracy "+accuracy+" vs RND("+(100+defensiveSkills)+") rolls: "+rnd+ "misses? "+(accuracy < rnd ));

        if(accuracy < rnd  ){
            if(hasFocus || other.hasFocus ) {
//                Sounds.swoosh();
//                MessageBox.addLine(type.name + " misses.");
            }
        }
        else {
            int hp = 1;
            String verb = "hits";
            if(stats.weaponItem != null && !stats.weaponItem.type.isRangeWeapon) {
                hp += stats.weaponItem.damage;
                verb = "attacks";
            }
            hp += Math.min(stats.experience /20, 20);     // experience bonus
            System.out.println("attack hp "+hp+" vs protection "+(other.stats.armourItem == null? 0 : other.stats.armourItem.protection));

//            if(hasFocus || other.hasFocus )
////                Sounds.fight();

            if (other.stats.armourItem != null && other.stats.armourItem.protection > hp) {
//                MessageBox.addLine("The " + other.type.name + " blocks the attack");
//                if (hp > other.stats.armourItem.protection/2) {
//                    other.stats.armourItem.protection--;        // armour takes damage
//                    MessageBox.addLine("The armour takes damage.");
//                }
//                if(stats.weaponItem != null){
//                    MessageBox.addLine("The weapon takes damage.");
//                    stats.weaponItem.accuracy = Math.max(0, stats.weaponItem.accuracy-1);
//                }
            } else {
                if (other.stats.armourItem != null)
                    hp-= other.stats.armourItem.protection;     // armour reduces the attack force

                if(hp > 0) {
                    // not missed, not blocked
                    if (other.stats.hitPoints < 5)
                        other.stats.hitPoints = Math.max(0, other.stats.hitPoints - hp);
                    else
                        other.stats.hitPoints = Math.max(2, other.stats.hitPoints - hp);        // avoid one-hit kills

                    // with increased awareness player is informed of all events
//                    if (type.isPlayer || other.type.isPlayer || world.rogue.stats.increasedAwareness > 0) {
//                        MessageBox.addLine(type.name + " " + verb + " the " + other.type.name + "(HP: " + other.stats.hitPoints + ")");
//                    }
                }
            }
        }
        if(other.stats.hitPoints <= 0){
            defeat(world, scenes, other);
        }
    }

    // something was thrown at the target
    public void hits(World world, DungeonScenes scenes, GameObject thrower, GameObject target){
        //if(hasFocus || target.hasFocus )
            //Sounds.fight();

        int hp = 1;

        if(type == GameObjectTypes.knife)
            hp = 3;
        else if(type == GameObjectTypes.explosive) {
            hp += damage;
        }
        else if(type == GameObjectTypes.arrow) {
            // if crossbow is equipped arrows do more damage
            if(thrower.stats.weaponItem != null && thrower.stats.weaponItem.type == GameObjectTypes.crossbow)
                hp += thrower.stats.weaponItem.damage;
            else
                hp += damage;
        }
        else if(type == GameObjectTypes.bottle_C_green){
            hp = 3;     // poison
        } else if(type == GameObjectTypes.bottle_B_green) {
            hp = -3;        // invigorating
        }

        target.stats.hitPoints = Math.max(0, target.stats.hitPoints-hp);
        // with increased awareness player is informed of all events
        if(target.hasFocus || thrower.hasFocus || world.rogue.stats.increasedAwareness > 0) {
            MessageBox.addLine(type.name + " hits the " + target.type.name + "(HP: " + target.stats.hitPoints + ")");
        }
        if(target.stats.hitPoints <= 0){
            thrower.defeat(world, scenes, target);
        }
    }


    private void defeat(World world, DungeonScenes scenes, GameObject enemy){
        // play sound effect if player was involved
        if(hasFocus || enemy.hasFocus || world.rogue.stats.increasedAwareness > 0) {
            Sounds.monsterDeath();
            MessageBox.addLine(type.name + " defeated the " + enemy.type.name + ". (XP +" + enemy.type.initXP + ")");
        }
        // remove enemy visually and logically
        if(!enemy.type.isPlayer && enemy.scene != null)
            scenes.remove(enemy.scene);
        world.levelData.gameObjects.clearOccupant(enemy.x, enemy.y);
        world.enemies.remove(enemy);
        world.levelData.gameObjects.remove(enemy);

        // victor gets XP
        stats.experience += enemy.type.initXP;      // base XP, not the actual XP

        // any gold gets dropped on the floor
        int goldAmount = enemy.stats.inventory.countGold();
        if(goldAmount > 0) {
            enemy.stats.inventory.removeGold();
            GameObject gold = new GameObject(GameObjectTypes.gold, enemy.x, enemy.y, Direction.NORTH);
            gold.quantity = goldAmount;
            gold.z = gold.type.z;
            if(enemy.scene != null)
                scenes.addScene(gold);
            world.levelData.gameObjects.add(gold);
            world.levelData.gameObjects.setOccupant(gold.x, gold.y, gold);
            if(type.isPlayer || enemy.type.isPlayer || world.rogue.stats.increasedAwareness > 0)
                MessageBox.addLine(enemy.type.name+ " drops their gold. ("+gold.quantity+")");
        }
    }

}
