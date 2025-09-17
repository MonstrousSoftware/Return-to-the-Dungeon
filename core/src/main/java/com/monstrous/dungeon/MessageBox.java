package com.monstrous.dungeon;

import com.badlogic.gdx.utils.Array;


/** Messages shown to the user in chronological order. */
public class MessageBox {
    public static Array<String> lines;

    public static void addLine(String message){
        if(lines == null)
            lines = new Array<>();
        lines.add(message);
    }

    public static void clear(){
        if(lines == null)
            lines = new Array<>();
        lines.clear();
        for(int i = 0; i < 10; i++)
            addLine("");
    }
}
