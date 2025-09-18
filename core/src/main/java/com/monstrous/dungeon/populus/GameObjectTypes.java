package com.monstrous.dungeon.populus;


import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;


public class GameObjectTypes  {
    public static final int ICON_SIZE = 64;

    public static GameObjectType rogue;

    public static GameObjectType warrior;
    public static GameObjectType mage;
    public static GameObjectType minion;
    public static GameObjectType imp;

    public static GameObjectType gold;
    public static GameObjectType knife;
    public static GameObjectType crossbow;
    public static GameObjectType explosive;
    public static GameObjectType shield1;
    public static GameObjectType shield2;
    public static GameObjectType arrows;
    public static GameObjectType arrow;
    public static GameObjectType axe;

    public static GameObjectType spellBookOpen;
    public static GameObjectType spellBookClosed;
    public static GameObjectType spellBookOpenB;
    public static GameObjectType spellBookClosedB;
    public static GameObjectType spellBookOpenC;
    public static GameObjectType spellBookClosedC;
    public static GameObjectType spellBookOpenD;
    public static GameObjectType spellBookClosedD;
    public static GameObjectType food;

    public static GameObjectType bottle_A_brown;
    public static GameObjectType bottle_A_green;
    public static GameObjectType bottle_B_brown;
    public static GameObjectType bottle_B_green;
    public static GameObjectType bottle_C_brown;
    public static GameObjectType bottle_C_green;

    public static GameObjectType cactus;

    public static GameObjectType bigSword;

    public static Array<GameObjectType> types;
    public static Sprite emptyIcon;


    public GameObjectTypes() {
        if(rogue != null)
            return;
        types = new Array<>();

        rogue = new GameObjectType("Rogue", true, false, 0, 0, 0, 0);
        //rogue.sceneAsset = new GLBLoader().load(Gdx.files.internal("characters/Rogue_Hooded.glb"));
        rogue.isPlayer = true;
        rogue.initXP = 0;
        types.add(rogue);

        warrior = new GameObjectType("Warrior", true, false, 1, 99, 3, 10);
        //warrior.sceneAsset = new GLBLoader().load(Gdx.files.internal("characters/Skeleton_Warrior.glb"));
        warrior.isEnemy = true;
        warrior.initXP = 20;
        warrior.initAggressive = true;
        types.add(warrior);

        mage = new GameObjectType("Mage", true, false, 1, 99, 3, 5);
        //mage.sceneAsset = new GLBLoader().load(Gdx.files.internal("characters/Skeleton_Mage.glb"));
        mage.isEnemy = true;
        mage.initXP = 5;
        types.add(mage);

        minion = new GameObjectType("Minion", true, false, 0, 99, 0, 6);
        //minion.sceneAsset = new GLBLoader().load(Gdx.files.internal("characters/Skeleton_Minion.glb"));
        minion.isEnemy = true;
        minion.initXP = 10;
        types.add(minion);

        imp = new GameObjectType("Imp", true, false, 2, 99, 2, 5);
        //imp.sceneAsset = new GLBLoader().load(Gdx.files.internal("characters/Skeleton_Rogue.glb"));
        imp.isEnemy = true;
        imp.initXP = 8;
        types.add(imp);

        // easter egg on level 0
        cactus = new GameObjectType("a Furry Cactus", false, true, 0, 0, 1, 1);
        types.add(cactus);

        gold = new GameObjectType("Gold", false, true, 0, 99, 2, 10);
        //gold.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/coin_stack_small.gltf"));
        gold.isCountable = true;
        gold.isGold = true;
        types.add(gold);

        knife = new GameObjectType("a Knife", false, true, 0, 99, 1, 2);
        //knife.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/dagger.gltf"));
        knife.isWeapon = true;
        knife.initMeleeDamage = 5;
        knife.initMeleeAccuracy = 5;
        knife.initThrowDamage = 5;
        knife.initThrowAccuracy = 5;
        types.add(knife);

        crossbow = new GameObjectType("a Crossbow", false, true, 2, 99, 1, 1);
        //crossbow.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/crossbow_1handed.gltf"));
        crossbow.isWeapon = true;
        crossbow.initThrowDamage = 8;
        crossbow.initThrowAccuracy = 2;
        crossbow.isRangeWeapon = true;
        types.add(crossbow);

        explosive = new GameObjectType("an Explosive", false, true, 2, 99, 1, 5);
        //explosive.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/smokebomb.gltf"));
        explosive.isWeapon = true;
        explosive.initThrowDamage = 12;
        explosive.initThrowAccuracy = 2;
        crossbow.isRangeWeapon = true;
        types.add(explosive);

        arrows = new GameObjectType("arrows", false, true, 1, 99, 1, 3);
        //arrows.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/arrow_bundle.gltf"));
        arrows.z = 1f;
        arrows.isCountable = true;
        arrows.isArrow = true;
        types.add(arrows);

        arrow = new GameObjectType("an arrow", false, true, 1, 99, 1, 3);
        //arrow.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/arrow.gltf"));
        arrow.z = 1f;
        arrow.isCountable = true;
        arrow.isArrow = true;
        arrow.initThrowDamage = 3;
        arrow.initThrowAccuracy = 2;
        crossbow.isRangeWeapon = true;
        types.add(arrow);
        arrows.alternative = arrow; // to change model as quantity drops to 1
        arrow.alternative = arrows;

        shield1 = new GameObjectType("a Round Shield", false, true, 0, 99, 1, 3);
        //shield1.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/shield_round.gltf"));
        shield1.z = 1f;
        shield1.isArmour = true;
        shield1.initProtection = 5;
        types.add(shield1);

        shield2 = new GameObjectType("a Square Shield", false, true, 3, 99, 1, 2);
        //hield2.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/shield_square.gltf"));
        shield2.z = 1f;
        shield2.isArmour = true;
        shield2.initProtection = 10;
        types.add(shield2);

        axe = new GameObjectType("an Axe", false, true, 4, 99, 1, 3);
        //axe.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/axe_1handed.gltf"));
        axe.isWeapon = true;
        axe.initMeleeDamage = 10;
        axe.initMeleeAccuracy = 5;
        axe.initThrowDamage = 10;
        axe.initThrowAccuracy = 5;
        types.add(axe);

        spellBookClosed = new GameObjectType("a purple Spellbook", false, true, 0, 2, 1, 1);
        //spellBookClosed.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/spellbook_closed.gltf"));
        spellBookClosed.isSpellBook = true;
        spellBookClosed.z = 1;
        types.add(spellBookClosed);

        spellBookOpen = new GameObjectType("a Spellbook (open)", false, true, 0, 0, 0, 0);
        //spellBookOpen.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/spellbook_open.gltf"));
        types.add(spellBookOpen);
        spellBookOpen.z = 1;
        spellBookClosed.alternative = spellBookOpen;

        spellBookClosedB = new GameObjectType("a red Spellbook", false, true, 3, 99, 0, 1);
        //spellBookClosedB.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/spellbook_closed_B.gltf"));
        spellBookClosedB.isSpellBook = true;
        spellBookClosedB.z = 1;
        types.add(spellBookClosedB);

        spellBookOpenB = new GameObjectType("a Spellbook (open)", false, true, 0, 0, 0, 0);
        //spellBookOpenB.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/spellbook_open_B.gltf"));
        types.add(spellBookOpenB);
        spellBookOpenB.z = 1;
        spellBookClosedB.alternative = spellBookOpenB;

        spellBookClosedC = new GameObjectType("a black Spellbook", false, true, 0, 99, 0, 2);
        //spellBookClosedC.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/spellbook_closed_C.gltf"));
        spellBookClosedC.isSpellBook = true;
        spellBookClosedC.z = 1;
        types.add(spellBookClosedC);

        spellBookOpenC = new GameObjectType("a Spellbook (open)", false, true, 0, 0, 0, 0);
        //spellBookOpenC.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/spellbook_open_C.gltf"));
        types.add(spellBookOpenC);
        spellBookOpenC.z = 1;
        spellBookClosedC.alternative = spellBookOpenC;

        spellBookClosedD = new GameObjectType("a green Spellbook", false, true, 0, 99, 0, 1);
        //spellBookClosedD.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/spellbook_closed_D.gltf"));
        spellBookClosedD.isSpellBook = true;
        spellBookClosedD.z = 1;
        types.add(spellBookClosedD);

        spellBookOpenD = new GameObjectType("a Spellbook (open)", false, true, 0, 0, 0, 0);
        //spellBookOpenD.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/spellbook_open_D.gltf"));
        types.add(spellBookOpenD);
        spellBookOpenD.z = 1;
        spellBookClosedD.alternative = spellBookOpenD;

        food = new GameObjectType("a plate of food", false, true, 0, 99, 2, 5);
        //food.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/plate_food_B.gltf"));
        food.isEdible = true;
        types.add(food);

        bottle_A_brown = new GameObjectType("an Amber Potion", false, true, 0, 99, 0, 4);
        //bottle_A_brown.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/bottle_A_brown.gltf"));
        bottle_A_brown.isPotion = true;
        types.add(bottle_A_brown);

        bottle_A_green = new GameObjectType("a Jade Potion", false, true,1, 99, 0, 1);
        //bottle_A_green.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/bottle_A_green.gltf"));
        bottle_A_green.isPotion = true;
        types.add(bottle_A_green);

        bottle_B_brown = new GameObjectType("a Murky Potion", false, true, 2, 99, 0, 4);
        //bottle_B_brown.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/bottle_B_brown.gltf"));
        bottle_B_brown.isPotion = true;
        types.add(bottle_B_brown);

        bottle_B_green = new GameObjectType("a Foul-Smelling Potion", false, true, 3, 99, 0, 4);
        //bottle_B_green.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/bottle_B_green.gltf"));
        bottle_B_green.isPotion = true;
        types.add(bottle_B_green);

        bottle_C_brown = new GameObjectType("a Bubbly Potion", false, true, 1, 99, 0, 4);
        //bottle_C_brown.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/bottle_C_brown.gltf"));
        bottle_C_brown.isPotion = true;
        types.add(bottle_C_brown);

        bottle_C_green = new GameObjectType("a Sweet Potion", false, true, 3, 99, 0, 4);
        //bottle_C_green.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/bottle_C_green.gltf"));
        bottle_C_green.isPotion = true;
        types.add(bottle_C_green);



        bigSword = new GameObjectType("the Sword of Yobled!", false, true, 5, 7, 0, 0);     // needs some external logic to ensure there is only one
        //bigSword.sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/sword_2handed_color.gltf"));
        bigSword.isWeapon = true;
        bigSword.initMeleeDamage = 25;
        bigSword.initMeleeAccuracy = 10;
        bigSword.isBigSword = true;
        types.add(bigSword);


        //addIcons();
     }

     // generate icons for each type
//     private void addIcons(){
//        ShowCase showCase = new ShowCase();
//        emptyIcon = showCase.makeIcon(null, ICON_SIZE, ICON_SIZE, false);
//        for(GameObjectType type : types ){
//            Sprite icon = showCase.makeIcon(type.sceneAsset, ICON_SIZE, ICON_SIZE, type.isEnemy || type.isPlayer || type == GameObjectTypes.bigSword);
//            type.icon = icon;
//        }
//     }


}
