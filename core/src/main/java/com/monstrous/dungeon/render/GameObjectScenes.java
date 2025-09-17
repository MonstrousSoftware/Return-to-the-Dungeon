package com.monstrous.dungeon.render;


import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.monstrous.dungeon.populus.GameObjectTypes;


/** Class to load Model per game object type */
public class GameObjectScenes {
    final static String[] fileNames = {
            "characters/Rogue_Hooded.glb",
            "characters/Skeleton_Warrior.glb",
            "characters/Skeleton_Mage.glb",
            "characters/Skeleton_Minion.glb",
            "characters/Skeleton_Rogue.glb",
            "models/coin_stack_small.gltf",
            "models/dagger.gltf",
            "models/crossbow_1handed.gltf",
            "models/smokebomb.gltf",
            "models/arrow_bundle.gltf",
            "models/arrow.gltf",
            "models/shield_round.gltf",
            "models/shield_square.gltf",
            "models/axe_1handed.gltf",
            "models/spellbook_closed.gltf",
            "models/spellbook_open.gltf",
            "models/spellbook_closed_B.gltf",
            "models/spellbook_open_B.gltf",
            "models/spellbook_closed_C.gltf",
            "models/spellbook_open_C.gltf",
            "models/spellbook_closed_D.gltf",
            "models/spellbook_open_D.gltf",
            "models/plate_food_B.gltf",
            "models/bottle_A_brown.gltf",
            "models/bottle_A_green.gltf",
            "models/bottle_B_brown.gltf",
            "models/bottle_B_green.gltf",
            "models/bottle_C_brown.gltf",
            "models/bottle_C_green.gltf",
            "models/sword_2handed_color.gltf",
    };

    /** queue assets in the asset manager to start async loading. */
    public void queueAssets(AssetManager assets){
        for(int i = 0; i < fileNames.length; i++){
            assets.load(fileNames[i], Model.class);
        }
    }

    /** load models via asset manager (need to call queueAssets() first). */
    public void loadAssets(AssetManager assets) {
        assets.finishLoading(); // just in case
        int index = 0;
        GameObjectTypes.rogue.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.warrior.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.mage.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.minion.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.imp.sceneAsset = assets.get(fileNames[index++], Model.class);

        GameObjectTypes.gold.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.knife.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.crossbow.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.explosive.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.arrows.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.arrow.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.shield1.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.shield2.sceneAsset = assets.get(fileNames[index++], Model.class);

        GameObjectTypes.axe.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.spellBookClosed.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.spellBookOpen.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.spellBookClosedB.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.spellBookOpenB.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.spellBookClosedC.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.spellBookOpenC.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.spellBookClosedD.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.spellBookOpenD.sceneAsset = assets.get(fileNames[index++], Model.class);

        GameObjectTypes.food.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.bottle_A_brown.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.bottle_A_green.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.bottle_B_brown.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.bottle_B_green.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.bottle_C_brown.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.bottle_C_green.sceneAsset = assets.get(fileNames[index++], Model.class);
        GameObjectTypes.bigSword.sceneAsset = assets.get(fileNames[index++], Model.class);

    }








}
