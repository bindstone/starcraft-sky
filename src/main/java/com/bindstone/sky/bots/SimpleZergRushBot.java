package com.bindstone.sky.bots;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.raw.StartRaw;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SimpleZergRushBot extends BaseBot {

    private static final int WORKER_COUNT = 15;
    private static final int SPAN_POOL_COUNT = 1;
    private static final int ATTACK_COUNT = 30;

    /**
     * STEP Execution
     */
    @Override
    public void onStep() {
        buildOverloard();
        buildWorker();
        buildSpanPool();
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
        if (observation().getFoodUsed() == observation().getFoodCap()) {
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
                    .forEach(unitInPool -> findTarget().ifPresentOrElse(
                            attackEnemyStructureWith(unitInPool),
                            attackEnemyPositionWith(unitInPool)));
        }
    }

    /**
     * Find a target in order:
     * 1) Marine
     * 2) Worker
     * 3) Key Building (Barracks, Headquarter)
     * 4) Rest
     */
    private Optional<UnitInPool> findTarget() {
        Optional<UnitInPool> first = observation().getUnits(Alliance.ENEMY, isOffensiveUnit()).stream().findFirst();
        if(first.isPresent()) {
            return first;
        }
        Optional<UnitInPool> second = observation().getUnits(Alliance.ENEMY, isWorkerUnit()).stream().findFirst();
        if(second.isPresent()) {
            return second;
        }
        Optional<UnitInPool> third = observation().getUnits(Alliance.ENEMY, isKeyBuilding()).stream().findFirst();
        if(third.isPresent()) {
            return third;
        }
        return observation().getUnits(Alliance.ENEMY, isEnemyUnit()).stream().findFirst();
    }
    
    private Predicate<UnitInPool> isOffensiveUnit() {
        return  unitInPool -> Set
                .of((UnitType) Units.TERRAN_MARINE)
                .contains(unitInPool.unit().getType());
    }
    private Predicate<UnitInPool> isWorkerUnit() {
        return  unitInPool -> Set
                .of((UnitType) Units.TERRAN_SCV)
                .contains(unitInPool.unit().getType());
    }
    private Predicate<UnitInPool> isKeyBuilding() {
        return  unitInPool -> Set
                .of((UnitType) Units.TERRAN_COMMAND_CENTER, Units.TERRAN_BARRACKS)
                .contains(unitInPool.unit().getType());
    }
    private Predicate<UnitInPool> isEnemyUnit() {
        return  unitInPool -> true;
    }

    private Consumer<UnitInPool> attackEnemyStructureWith(UnitInPool unitInPool) {
        return enemyUnit -> actions().unitCommand(unitInPool.unit(), Abilities.ATTACK, enemyUnit.unit(), false);
    }

    private Runnable attackEnemyPositionWith(UnitInPool unitInPool) {
        return () -> findEnemyPosition()
                .ifPresent(point2d -> actions().unitCommand(unitInPool.unit(), Abilities.SMART, point2d, false));
    }

    // Tries to find a random location that can be pathed to on the map.
    // Returns Point2d if a new, random location has been found that is pathable by the unit.
    private Optional<Point2d> findEnemyPosition() {
        Optional<StartRaw> startRaw = gameInfo.getStartRaw();
        if (startRaw.isPresent()) {
            Set<Point2d> startLocations = new HashSet<>(startRaw.get().getStartLocations());
            startLocations.remove(observation().getStartLocation().toPoint2d());
            if (startLocations.isEmpty()) return Optional.empty();
            return Optional.of(new ArrayList<>(startLocations)
                    .get(ThreadLocalRandom.current().nextInt(startLocations.size())));
        } else {
            return Optional.empty();
        }
    }
}
