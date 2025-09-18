package com.monstrous.dungeon.gui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.monstrous.dungeon.populus.Inventory;


public class InventoryWindow extends Window {

    private InventorySlotButton[] buttons;

    public InventoryWindow(String title, Skin skin, Inventory inventory) {
        super(title, skin, "default");

        int numSlots = inventory.numSlots;
        buttons = new InventorySlotButton[numSlots];

        Table gridTable = new Table();

        int index = 0;
        for(int x = 0; x < numSlots; x++) {
            InventorySlotButton b = new InventorySlotButton( skin, inventory.slots[index]);
            buttons[index++] = b;
            gridTable.add(b);
        }
        gridTable.row();
        for(int x = 0; x < numSlots; x++) {
            gridTable.add(new Label(""+(x+1)%10, skin, "small"));
        }
        add(gridTable);
        pack();
    }

    public void update() {
        for(int i = 0; i < buttons.length; i++){
            buttons[i].update();
        }
    }
}
