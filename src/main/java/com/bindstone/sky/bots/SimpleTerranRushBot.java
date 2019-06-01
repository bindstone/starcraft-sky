package com.bindstone.sky.bots;

import com.bindstone.sky.Strategies.SimpleKillTerranByRushStrategy;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;

import java.util.function.Consumer;

public class SimpleTerranRushBot extends BaseBot {

    private static final int WORKER_COUNT = 15;
    private static final int BARRACKS_COUNT = 3;
    private static final int ATTACK_COUNT = 25;

    private SimpleKillTerranByRushStrategy strategy = new SimpleKillTerranByRushStrategy();

    /**
     * STEP Execution
     */
    @Override
    public void onStep() {
        buildWorker();
        buildDepots();
        buildBarracks();
        buildMarines();
        searchAndDestroy();
    }

    /**
     * BUILD Worker if max is not yet reached
     */
    private void buildWorker() {
        if (countUnitType(Units.TERRAN_SCV) < WORKER_COUNT) {
            tryBuildUnit(Abilities.TRAIN_SCV, Units.TERRAN_COMMAND_CENTER);
        }
    }

    /**
     * BUILD a Depot
     */
    private void buildDepots() {
        if (observation().getFoodUsed() >= observation().getFoodCap()) {
            tryBuildStructure(Abilities.BUILD_SUPPLY_DEPOT, Units.TERRAN_SCV);
        }
    }

    /**
     * BUILD Barracks
     */
    private void buildBarracks() {
        if (countUnitType(Units.TERRAN_MARINE) < BARRACKS_COUNT) {
            tryBuildStructure(Abilities.BUILD_BARRACKS, Units.TERRAN_SCV);
        }
    }

    /**
     * BUILD Marines
     */
    private void buildMarines() {
        tryBuildUnit(Abilities.TRAIN_MARINE, Units.TERRAN_BARRACKS);
    }

    /**
     * Motivate Units to do something
     */
    @Override
    public void onUnitIdle(UnitInPool unitInPool) {
        switch ((Units) unitInPool.unit().getType()) {
            case TERRAN_SCV:
                getRandomUnit(Units.TERRAN_COMMAND_CENTER).ifPresent(mineIdleWorker(unitInPool));
                break;
        }
    }

    /**
     * Action: Mine
     */
    private Consumer<UnitInPool> mineIdleWorker(UnitInPool unitInPool) {
        return commandCenter -> findNearestMineralPatch(commandCenter.unit().getPosition().toPoint2d())
                .ifPresent(mineralPath ->
                        actions().unitCommand(unitInPool.unit(), Abilities.HARVEST_GATHER, mineralPath, false));
    }

    /**
     * ATTACK
     */
    private void searchAndDestroy() {
        if (observation().getArmyCount() > ATTACK_COUNT) {
            observation()
                    .getUnits(Alliance.SELF, UnitInPool.isUnit(Units.TERRAN_MARINE))
                    .forEach(unitInPool -> strategy.findTarget(observation())
                            .ifPresentOrElse(
                                    attackEnemyStructureWith(unitInPool),
                                    attackEnemyPositionWith(unitInPool)));
        }
    }
}
