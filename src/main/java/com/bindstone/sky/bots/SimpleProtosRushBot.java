package com.bindstone.sky.bots;

import com.bindstone.sky.Strategies.SimpleKillTerranByRushStrategy;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;

import java.util.function.Consumer;

public class SimpleProtosRushBot extends BaseBot {

    private static final int WORKER_COUNT = 15;
    private static final int WARP_GATE_COUNT = 2;
    private static final int ATTACK_COUNT = 10;

    private SimpleKillTerranByRushStrategy strategy = new SimpleKillTerranByRushStrategy();

    /**
     * STEP Execution
     */
    @Override
    public void onStep() {
        buildWorker();
        buildPylon();
        buildWarpGate();
        buildZealots();
        searchAndDestroy();
    }

    /**
     * BUILD Worker if max is not yet reached
     */
    private void buildWorker() {
        if (countUnitType(Units.PROTOSS_PROBE) < WORKER_COUNT) {
            tryBuildUnit(Abilities.TRAIN_PROBE, Units.PROTOSS_NEXUS);
        }
    }

    /**
     * BUILD Pylon
     */
    private void buildPylon() {
        if (observation().getFoodUsed() >= observation().getFoodCap()) {
            tryBuildStructure(Abilities.BUILD_PYLON, Units.PROTOSS_PROBE);
        }
    }

    /**
     * BUILD a Warp Gate
     */
    private void buildWarpGate() {
        if (countUnitType(Units.PROTOSS_GATEWAY) < WARP_GATE_COUNT) {
            tryBuildStructure(Abilities.BUILD_GATEWAY, Units.PROTOSS_PROBE);
        }
    }

    /**
     * BUILD Zergs, Zergs, Zergs...
     */
    private void buildZealots() {
        tryBuildUnit(Abilities.TRAIN_ZEALOT, Units.PROTOSS_GATEWAY);
    }

    /**
     * Motivate Units to do something
     */
    @Override
    public void onUnitIdle(UnitInPool unitInPool) {
        switch ((Units) unitInPool.unit().getType()) {
            case PROTOSS_PROBE:
                getRandomUnit(Units.PROTOSS_NEXUS).ifPresent(mineIdleWorker(unitInPool));
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
                    .getUnits(Alliance.SELF, UnitInPool.isUnit(Units.PROTOSS_ZEALOT))
                    .forEach(unitInPool -> strategy.findTarget(observation())
                            .ifPresentOrElse(
                                    attackEnemyStructureWith(unitInPool),
                                    attackEnemyPositionWith(unitInPool)));
        }
    }
}
