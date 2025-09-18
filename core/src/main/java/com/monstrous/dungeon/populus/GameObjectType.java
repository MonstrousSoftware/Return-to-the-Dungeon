package com.monstrous.dungeon.populus;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g3d.Model;


public class GameObjectType {
    public String name;
    public boolean character;
    public boolean pickup;
    public int startLevel, endLevel;    // at which levels can they be found?
    public int minCount, maxCount;      // how many to be found per level?
    public boolean isPlayer;
    public boolean isEnemy;
    public boolean isCountable;
    public boolean isWeapon;
    public boolean isRangeWeapon;
    public boolean isArmour;
    public boolean isEdible;
    public boolean isPotion;
    public boolean isGold;
    public boolean isArrow;
    public boolean isSpellBook;
    public boolean isBigSword;
    public GameObjectType alternative;
    public int initProtection;      // for armour
    public int initMeleeAccuracy;
    public int initMeleeDamage;
    public int initThrowDamage;
    public int initThrowAccuracy;
    public int initXP;
    public boolean initAggressive;
    public float z;     // height to render at when place on the ground (to avoid some model being inside the floor)
    public Sprite icon;
    public Model sceneAsset;

    public GameObjectType( String name, boolean character, boolean pickup, int startLevel, int endLevel, int minCount, int maxCount) {
        this.name = name;
        this.character = character;
        this.pickup = pickup;
        this.startLevel = startLevel;
        this.endLevel = endLevel;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.isPlayer = false;
        this.isCountable = false;
        this.initXP = 1;
        this.z = 0f;
        this.isWeapon = false;
        this.isRangeWeapon = false;
        this.isArmour = false;
        this.isEdible = false;
        this.isPotion = false;
        this.isGold = false;
        this.isArrow = false;
        this.isSpellBook = false;
        this.initProtection = 0;
        this.initMeleeDamage = 0;
        this.initMeleeAccuracy = 0;
        this.initThrowDamage = 0;
        this.initThrowAccuracy = 0;
        this.initAggressive = false;
        this.isBigSword = false;
    }
}
