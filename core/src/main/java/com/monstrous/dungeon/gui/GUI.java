package com.monstrous.dungeon.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.monstrous.dungeon.MessageBox;
import com.monstrous.dungeon.World;
import com.monstrous.dungeon.populus.GameObject;
import com.monstrous.dungeon.populus.Inventory;
import com.monstrous.gdx.webgpu.scene2d.WgSkin;
import com.monstrous.gdx.webgpu.scene2d.WgStage;


public class GUI implements Disposable {

    public static final int PANEL_WIDTH = 400;      // width of UI panel
    public static final int NUM_MESSAGES = 10;

    public WgStage stage;
    private Skin skin;
    private Label level;
    private Label gold;
    private Label hp;
    private Label xp;
    private Label[] messages;
    private StringBuffer sb;
    private World world;
    private GameObject equippedWeapon;
    private GameObject equippedArmour;
    private InventoryWindow inventoryWindow;
    private Inventory rogueInventory;
    private Inventory equippedInventory;
    private InventorySlotButton weaponButton;
    private InventorySlotButton armourButton;
    private Label clockLabel;
    private int elapsedSeconds;

    public GUI( World world ) {
        this.world = world;
        skin = new WgSkin(Gdx.files.internal("skin/d3.json"));
        stage = new WgStage(new ScreenViewport());
        sb = new StringBuffer();

        // rely on resize() to call rebuild()

        equippedInventory = new Inventory(2);
        inventoryWindow = new InventoryWindow("Inventory", skin, world.rogue.stats.inventory);
        rogueInventory = world.rogue.stats.inventory;
    }

    private void rebuild(){
        //Gdx.app.log("GUI", "rebuild");
        stage.clear();

        stage.addActor(inventoryWindow);



        Table uiPanel = new Table();

        Table stats = new Table();
        level = new Label("DUNGEON LEVEL: 0", skin, "small");
        gold = new Label("GOLD: 0", skin,"small");
        hp = new Label("HP: 0", skin,"small");
        xp = new Label("XP: 0", skin,"small");
        level.setColor(Color.LIGHT_GRAY);
        gold.setColor(Color.GOLD);
        hp.setColor(Color.GREEN);
        xp.setColor(Color.PURPLE);

        stats.add(level).pad(7).left().top();
        stats.row();
        stats.add(gold).pad(7).left().top();
        stats.row();
        stats.add(hp).pad(7).left().top();
        stats.row();
        stats.add(xp).pad(7).left().top();
        stats.pack();

        //uiPanel.debug();
        uiPanel.add(stats).pad(20, 20, 10, 10).left().top();
        uiPanel.row();

        Table eq = new Table();

        eq.add(new Label("EQUIPPED", skin, "small")).colspan(2);
        eq.row();
        weaponButton = new InventorySlotButton( skin, equippedInventory.slots[0]);
        eq.add(weaponButton).pad(5).right().top();
        armourButton = new InventorySlotButton( skin, equippedInventory.slots[1]);
        eq.add(armourButton).pad(5).left().top();
        eq.row();
        eq.add(new Label("Weapon", skin, "smaller"));
        eq.add(new Label("Armour", skin, "smaller"));
        eq.pack();


        uiPanel.setHeight(stage.getHeight());
        uiPanel.add(eq).center().padBottom(30);
        uiPanel.row();

        messages = new Label[NUM_MESSAGES];
        Table messageBox = new Table();
        for(int i = 0; i < NUM_MESSAGES; i++) {
            messages[i] = new Label("..", skin, "smaller");
            messages[i].setColor(Color.LIGHT_GRAY);
            messageBox.add(messages[i]).pad(3).left().row();
        }
        uiPanel.add(messageBox).pad(10).top().left().expand();
        uiPanel.row();

        Label helpLabel = new Label("Press H for help", skin, "smaller");
        helpLabel.setColor(Color.GRAY);
        uiPanel.add(helpLabel).pad(10).bottom().right();
        uiPanel.row();

        clockLabel = new Label("00:00:00", skin, "smaller");
        clockLabel.setColor(Color.GRAY);
        uiPanel.add(clockLabel).pad(10).bottom().left();
        uiPanel.pack();

        // Screen is split in 2 columns. Left for 3d view and Right for fixed width ui panel
        Table screenTable = new Table();
        screenTable.setFillParent(true);

        screenTable.add().expand();         // empty column
        screenTable.add(uiPanel).width(PANEL_WIDTH).height(stage.getHeight()).top();
        screenTable.row();

        screenTable.pack();

        stage.addActor(screenTable);
    }


    private void update() {
        sb.setLength(0);
        sb.append("DUNGEON LEVEL: ");
        sb.append(world.level);
        level.setText(sb.toString());

        sb.setLength(0);
        sb.append("GOLD: ");
        sb.append(world.rogue.stats.inventory.countGold());
        gold.setText(sb.toString());

        sb.setLength(0);
        sb.append("HP: ");
        sb.append(world.rogue.stats.hitPoints);
        hp.setText(sb.toString());

        sb.setLength(0);
        sb.append("XP: ");
        sb.append(world.rogue.stats.experience);
        xp.setText(sb.toString());

        for(int i = 0; i < NUM_MESSAGES; i++)
            messages[i].setText(MessageBox.lines.get(MessageBox.lines.size - (NUM_MESSAGES-i)));

        setWeapon();
        setArmour();

        weaponButton.update();
        armourButton.update();

        // force rebuild of Inv window on a game restart
        if (rogueInventory != world.rogue.stats.inventory) {
            inventoryWindow.remove();
            inventoryWindow = new InventoryWindow("Inventory", skin, world.rogue.stats.inventory);
            stage.addActor(inventoryWindow);
            rogueInventory = world.rogue.stats.inventory;
        }
        inventoryWindow.update();

        if ((int)world.secondsElapsed != elapsedSeconds) {
            elapsedSeconds = (int)world.secondsElapsed;

            int hh = (int) (world.secondsElapsed / 3600);
            int mm = (int) ((world.secondsElapsed - 3600 * hh) / 60);
            int ss = (int) world.secondsElapsed - 60 * mm;
            sb.setLength(0);
            if (hh > 0) {
                sb.append(hh);
                sb.append(":");
                if (mm < 10)
                    sb.append("0");
            }
            sb.append(mm);
            sb.append(":");
            if (ss < 10)
                sb.append("0");
            sb.append(ss);
            clockLabel.setText(sb.toString());
        }

    }

    private void setWeapon(){
        if(world.rogue.stats.weaponItem != equippedWeapon){
            equippedWeapon = world.rogue.stats.weaponItem;
            if(equippedWeapon == null) {
                equippedInventory.slots[0].object = null;
                equippedInventory.slots[0].count = 0;
            } else {
                equippedInventory.slots[0].object = equippedWeapon;
                equippedInventory.slots[0].count = 1;
            }
            weaponButton.update();
        }
    }

    private void setArmour(){
        if(world.rogue.stats.armourItem != equippedArmour){
            equippedArmour = world.rogue.stats.armourItem;
            if(equippedArmour == null) {
                equippedInventory.slots[1].object = null;
                equippedInventory.slots[1].count = 0;
            } else {
                equippedInventory.slots[1].object = equippedArmour;
                equippedInventory.slots[1].count = 1;
            }
            armourButton.update();
        }
    }


    public void render(float deltaTime) {
        update();

        stage.act(deltaTime);
        stage.draw();
    }

    public void resize(int width, int height) {
        //Gdx.app.log("GUI resize", "gui " + width + " x " + height);
        stage.getViewport().update(width, height, true);
        rebuild();
    }


    @Override
    public void dispose() {
        skin.dispose();
    };
}
