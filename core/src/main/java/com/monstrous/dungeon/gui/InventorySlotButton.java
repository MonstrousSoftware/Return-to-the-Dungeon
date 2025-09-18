package com.monstrous.dungeon.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.monstrous.dungeon.populus.GameObjectType;
import com.monstrous.dungeon.populus.GameObjectTypes;
import com.monstrous.dungeon.populus.Inventory;
import com.monstrous.gdx.webgpu.graphics.WgTexture;


// one slot in the inventory
// gets a reference to the corresponding Inventory.Slot an uses this to keep the visual
// representation up to date when update() is called.
//
public class InventorySlotButton extends Button {

    public static final Color BACKGROUND_COLOUR = Color.DARK_GRAY;

    private final Label countLabel;
    private final Label buffLabel;
    private final Label accuracyLabel;
    private final Inventory.Slot slot;
    private final Image image;
    private final TextureRegionDrawable placeHolder;
    private GameObjectType currentType;
    private int count;

    public InventorySlotButton(Skin skin, Inventory.Slot slot) {
        super(skin, "slot");
        this.slot = slot;
        count = 0;

        Table countTable = new Table();
        countLabel = new Label("", skin, "small");
        countTable.add(countLabel).right().bottom().expandX().expandY();    // small number in bottom right hand side, we use the table to position the label
        countTable.pack();

        Table buffTable = new Table();
        buffLabel = new Label("--", skin, "smaller");
        accuracyLabel = new Label("--", skin, "smaller");
        buffTable.add(buffLabel).left().top();    //
        buffTable.add(accuracyLabel).right().top().expandX().expandY();
        buffTable.pack();

        currentType = null;
        placeHolder = makePlaceHolder();        // for empty slot
        image = new Image(placeHolder);         // will be updated by update()

        // make a stack of layers: the icon image and on top of that the counter value

        Stack stack = new Stack();
        stack.add(image);
        stack.add(countTable);
        stack.add(buffTable);
        add(stack).pad(3);

    }


    // return true if slot contents has just changed
    public boolean update() {

        boolean updated = false;

        if(slot.count != this.count) {
            adjustCountIndicator(slot);
            this.count = slot.count;
            updated = true;
        }

        adjustBuffIndicator(slot);      // todo update only when changed

        // has the type changed? (type null means empty slot)
        if(slot.count == 0 && this.currentType != null){
            image.setDrawable(placeHolder);
            this.currentType = null;
            updated = true;
        }
        else if( slot.count > 0 && slot.object.type != this.currentType) {
            image.setDrawable(new TextureRegionDrawable(slot.object.type.icon));
            this.currentType = slot.object.type;
            updated = true;
        }
        return updated;
    }

    private void adjustCountIndicator( Inventory.Slot slot) {
        if(slot.count <= 0)
            countLabel.setText("");         // don't show a zero
        else if(!slot.object.type.isCountable)
            countLabel.setText("");         // don't show a 1 for uncountables
        else
            countLabel.setText(slot.count);
    }

    private void adjustBuffIndicator( Inventory.Slot slot) {
        if(slot.count == 1){ // single item in the slot?
            if(slot.object.type.isArmour) {
                buffLabel.setText(slot.object.protection);
                accuracyLabel.setText("");
            } else if(slot.object.type.isWeapon) {
                buffLabel.setText(slot.object.damage);
                accuracyLabel.setText(slot.object.accuracy);
            } else  {
                buffLabel.setText("");
                accuracyLabel.setText("");
            }
        }
        else {
            buffLabel.setText("");
            accuracyLabel.setText("");
        }
    }

    private TextureRegionDrawable makePlaceHolder() {
        int size = GameObjectTypes.ICON_SIZE;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(BACKGROUND_COLOUR);
        pixmap.fill();
        return new TextureRegionDrawable(new WgTexture(pixmap));
    }
}
