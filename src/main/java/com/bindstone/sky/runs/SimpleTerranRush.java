package com.bindstone.sky.runs;

import com.bindstone.sky.bots.SimpleTerranRushBot;
import com.github.ocraft.s2client.bot.S2Coordinator;
import com.github.ocraft.s2client.protocol.game.BattlenetMap;
import com.github.ocraft.s2client.protocol.game.Difficulty;
import com.github.ocraft.s2client.protocol.game.Race;

public class SimpleTerranRush {

    /**
     * EXECUTION Simple Terran Rush Bot
     */
    public static void main(String[] args) {

        SimpleTerranRushBot bot = new SimpleTerranRushBot();

        S2Coordinator s2Coordinator = S2Coordinator.setup()
                .loadSettings(args)
                .setParticipants(
                        S2Coordinator.createParticipant(Race.TERRAN, bot),
                        S2Coordinator.createComputer(Race.TERRAN, Difficulty.VERY_EASY))
                .launchStarcraft()
                .startGame(BattlenetMap.of("Kairos Junction LE"));

        while (s2Coordinator.update()) {
        }

        s2Coordinator.quit();
    }
}
