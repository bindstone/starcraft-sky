package com.bindstone.sky.bots;

import com.bindstone.sky.Strategies.SimpleKillTerranByRushStrategy;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;

public class SimpleZergRushBot extends BaseBot {

    private static final int WORKER_COUNT = 15;
    private static final int SPAN_POOL_COUNT = 1;
    private static final int HATCHERY_COUNT = 3;
    private static final int ATTACK_COUNT = 30;

    private SimpleKillTerranByRushStrategy strategy = new SimpleKillTerranByRushStrategy();

    /**
     * STEP Execution
     */
    @Override
    public void onStep() {
        buildWorker();
        buildOverloard();
        buildSpanPool();
        buildHatchery();
        buildZergling();
        searchAndDestroy();
    }

    /**
     * BUILD Worker if max is not yet reached
     */
    private void buildWorker() {
        if (countUnitType(Units.ZERG_DRONE) < WORKER_COUNT) {
            tryBuildUnit(Abilities.TRAIN_DRONE, Units.ZERG_LARVA);
        }
    }

    /**
     * BUILD some Overload's when Max Cap is reached. Could be optimized in building only one.
     */
    private void buildOverloard() {
        if (observation().getFoodUsed() >= observation().getFoodCap()) {
            tryBuildUnit(Abilities.TRAIN_OVERLORD, Units.ZERG_LARVA);
        }
    }

    /**
     * BUILD a Span Pool for producing Zerglings
     */
    private void buildSpanPool() {
        if (countUnitType(Units.ZERG_SPAWNING_POOL) < SPAN_POOL_COUNT) {
            tryBuildStructure(Abilities.BUILD_SPAWNING_POOL, Units.ZERG_DRONE);
        }
    }

    /**
     * Build Hatchery to Speed up Zerg Generation
     */
    private void buildHatchery() {
        if (countUnitType(Units.ZERG_SPAWNING_POOL) >= 1 && countUnitType(Units.ZERG_HATCHERY) < HATCHERY_COUNT) {
            tryBuildStructure(Abilities.BUILD_HATCHERY, Units.ZERG_DRONE);
        }
    }

    /**
     * BUILD Zergs, Zergs, Zergs...
     */
    private void buildZergling() {
        tryBuildUnit(Abilities.TRAIN_ZERGLING, Units.ZERG_LARVA);
    }

    /**
     * ATTACK
     */
    private void searchAndDestroy() {
        if (observation().getArmyCount() > ATTACK_COUNT) {
            observation()
                    .getUnits(Alliance.SELF, UnitInPool.isUnit(Units.ZERG_ZERGLING))
                    .forEach(unitInPool -> strategy.findTarget(observation())
                            .ifPresentOrElse(
                            attackEnemyStructureWith(unitInPool),
                            attackEnemyPositionWith(unitInPool)));
        }
    }
}
