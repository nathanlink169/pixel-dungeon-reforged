package com.shatteredpixel.shatteredpixeldungeon.utils;

public class PDRHelpers {
    public static String DifficultyIntToString(int difficulty) {
        switch (difficulty)
        {
            case 1: return "Easy";
            case 2: return "Medium";
            case 3: return "Hard";
            case 4: return "Impossible";
        }
        return "INVALID_DIFFICULTY";
    }
}
