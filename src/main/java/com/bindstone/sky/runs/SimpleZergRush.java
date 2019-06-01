package com.bindstone.sky.runs;

import com.bindstone.sky.bots.SimpleZergRushBot;
import com.github.ocraft.s2client.bot.S2Coordinator;
import com.github.ocraft.s2client.protocol.game.BattlenetMap;
import com.github.ocraft.s2client.protocol.game.Difficulty;
import com.github.ocraft.s2client.protocol.game.Race;

public class SimpleZergRush {

    /**
     * EXECUTION Simple Zerg Rush Bot
     */
    public static void main(String[] args) {

        SimpleZergRushBot bot = new SimpleZergRushBot();

        S2Coordinator s2Coordinator = S2Coordinator.setup()
                .loadSettings(args)
                .setParticipants(
                        S2Coordinator.createParticipant(Race.ZERG, bot),
                        S2Coordinator.createComputer(Race.TERRAN, Difficulty.VERY_EASY))
                .launchStarcraft()
                .startGame(BattlenetMap.of("Kairos Junction LE"));

        while (s2Coordinator.update()) {
        }

        s2Coordinator.quit();
    }
}
