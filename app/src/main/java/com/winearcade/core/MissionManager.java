package com.winearcade.core;

import java.util.Calendar;
import java.util.Random;

public class MissionManager {
    private static final String[] POOL = {
        "Flip Fishing 3마리",
        "Flip Reaction 500ms 이하",
        "Pocket Signal 3연속 성공",
        "Flip Memory 5자리",
        "Vault Phone 4단계",
        "Secret Dial 8자리",
        "Retro Operator 5연속 성공",
        "Classic 게임 300점"
    };

    public String[] today() {
        Calendar c = Calendar.getInstance();
        long seed = c.get(Calendar.YEAR) * 1000L + c.get(Calendar.DAY_OF_YEAR);
        Random r = new Random(seed);
        String[] out = new String[3];
        for (int i=0; i<3; i++) {
            int idx;
            boolean duplicate;
            do {
                idx = r.nextInt(POOL.length);
                duplicate = false;
                for (int j=0; j<i; j++) if (POOL[idx].equals(out[j])) duplicate = true;
            } while (duplicate);
            out[i] = POOL[idx];
        }
        return out;
    }
}
