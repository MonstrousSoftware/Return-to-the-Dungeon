package com.monstrous.dungeon;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    public World world;

    @Override
    public void create() {

        world = new World();

        setScreen(new GameScreen(this));
    }
}
