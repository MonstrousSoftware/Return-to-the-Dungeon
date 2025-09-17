package com.monstrous.dungeon.populus;

import com.monstrous.dungeon.map.Direction;

public class Inventory {

    public Slot[] slots;
    public int numSlots;

    public static class Slot {
        public GameObject object;
        public int count;

        public Slot() {
            object = null;
            count = 0;
        }

        // may return null if slot was empty
        public GameObject removeItem() {
            if (count <= 0)
                return null;
            count--;
            GameObject item;
            if (count == 0) {
                item = object;      // return object from the slot
                item.quantity = 1;
                object = null;
            } else {
                // return new object of same type as in the slot
                GameObjectType type = object.type;
                if (object.type == GameObjectTypes.arrows) {
                    type = type.alternative; // extract a single arrow
                    if(count == 1)
                        object.type = object.type.alternative;            // change slot icon to singular arrow
                }
                item = new GameObject(type, 0, 0, Direction.NORTH);
            }
            return item;
        }

        public boolean isEmpty(){
            return count == 0;
        }

        public void addItem(GameObject item){
            if(object == null){
                assert count == 0;
                object = item;
            }
            count+=item.quantity;

            if(object.type.isArrow && count > 1)
                object.type = GameObjectTypes.arrows;  // bundle of arrows
        }
    }


    public Inventory(int numSlots) {
        this.numSlots = numSlots;
        slots = new Slot[numSlots];
        for(int i = 0 ; i < numSlots;i++)
            slots[i] = new Slot();
    }

    public boolean addItem(GameObject item) {

        GameObjectType type = item.type;

        // find slot of matching type if this is a fungible item such as gold
        if(type.isCountable) {
            for (int i = 0; i < numSlots; i++) {
                if (!slots[i].isEmpty() && slots[i].object.type == type) {
                    slots[i].addItem(item);
                    //Gdx.app.log("Inventory", "slot " + i + " type: " + type.name + " count:" + slots[i].count);
                    return true;
                }
                if (!slots[i].isEmpty() && slots[i].object.type.isArrow && item.type.isArrow) {
                    slots[i].addItem(item);
                    //Gdx.app.log("Inventory", "slot " + i + " type: " + type.name + " count:" + slots[i].count);
                    return true;
                }
            }
        }
        // find first free slot
        for(int i = 0; i < numSlots; i++) {
            if(slots[i].isEmpty() ){
                slots[i].addItem(item);
                //Gdx.app.log("Inventory", "slot "+i+" type: "+type.name+" count:"+slots[i].count);
                return true;
            }
        }
        //Gdx.app.error("inventory full", "");
        return false;
    }

    public GameObject removeItem(int slot) {
        assert slot >= 0 && slot < numSlots;
        return slots[slot].removeItem();
    }

    public GameObject removeItem(GameObject item) {
        for(int i = 0; i < numSlots; i++) {
            if(!slots[i].isEmpty() && slots[i].object.type == item.type && slots[i].count > 0)
                return removeItem(i);
        }
        return null;
    }

    public boolean contains( GameObjectType type ){
        for(int i = 0; i < numSlots; i++) {
            if(!slots[i].isEmpty() && slots[i].object.type == type)
                return true;
        }
        return false;
    }

    public int removeGold() {
        for(int i = 0; i < numSlots; i++) {
            if(!slots[i].isEmpty() && slots[i].object.type.isGold){
                int count = slots[i].count;
                slots[i].count = 0;
                slots[i].object = null;
                return count;
            }
        }
        return 0;
    }

    public int countGold() {
        // gold can only be in one slot, because more gold will always be added to the same slot (as it is countable).
        for(int i = 0; i < numSlots; i++) {
            if(!slots[i].isEmpty() && slots[i].object.type.isGold){
                return slots[i].count;
            }
        }
        return 0;
    }
}
