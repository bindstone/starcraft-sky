package com.bindstone.sky.bots;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Ability;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.game.raw.StartRaw;
import com.github.ocraft.s2client.protocol.response.ResponseGameInfo;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Unit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class BaseBot extends S2Agent {

    // General Games Info Object
    protected ResponseGameInfo gameInfo;

    /**
     * START Game
     */
    @Override
    public void onGameStart() {
        System.out.println("Start SKY Bot...");
        debug().debugTextOut("[SKY] Bot activated and ready to conquer the world...", Color.BLUE);
        gameInfo = observation().getGameInfo();
        debug().sendDebug();
    }

    /**
     * END Game
     */
    @Override
    public void onGameEnd() {
        System.out.println("Game ended...");
        control().dumpProtoUsage();
    }

    /**
     * Count Units By Type
     */
    protected int countUnitType(Units unitType) {
        return observation().getUnits(Alliance.SELF, UnitInPool.isUnit(unitType)).size();
    }

    protected boolean tryBuildUnit(Ability abilityTypeForUnit, UnitType unitType) {
        Optional<UnitInPool> unitInPool = getRandomUnit(unitType);
        if (unitInPool.isPresent()) {
            Unit unit = unitInPool.get().unit();
            if (!unit.getOrders().isEmpty()) {
                return false;
            }
            actions().unitCommand(unit, abilityTypeForUnit, false);
            return true;
        } else {
            return false;
        }
    }

    protected Optional<UnitInPool> getRandomUnit(UnitType unitType) {
        List<UnitInPool> units = observation().getUnits(Alliance.SELF, UnitInPool.isUnit(unitType));
        return units.isEmpty()
                ? Optional.empty()
                : Optional.of(units.get(ThreadLocalRandom.current().nextInt(units.size())));
    }

    protected boolean tryBuildStructure(Ability abilityTypeForStructure, UnitType unitType) {
        // If a unit already is building a supply structure of this type, do nothing.
        if (!observation().getUnits(Alliance.SELF, doesBuildWith(abilityTypeForStructure)).isEmpty()) {
            return false;
        }

        // Just try a random location near the unit.
        Optional<UnitInPool> unitInPool = getRandomUnit(unitType);
        if (unitInPool.isPresent()) {
            Unit unit = unitInPool.get().unit();
            actions().unitCommand(
                    unit,
                    abilityTypeForStructure,
                    unit.getPosition().toPoint2d().add(Point2d.of(getRandomScalar(), getRandomScalar()).mul(15.0f)),
                    false);
            return true;
        } else {
            return false;
        }
    }

    private Predicate<UnitInPool> doesBuildWith(Ability abilityTypeForStructure) {
        return unitInPool -> unitInPool.unit()
                .getOrders()
                .stream()
                .anyMatch(unitOrder -> abilityTypeForStructure.equals(unitOrder.getAbility()));
    }

    private float getRandomScalar() {
        return ThreadLocalRandom.current().nextFloat() * 2 - 1;
    }


    // Tries to find a random location that can be pathed to on the map.
    // Returns Point2d if a new, random location has been found that is pathable by the unit.
    protected Optional<Point2d> findEnemyPosition() {
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

    protected Optional<Unit> findNearestMineralPatch(Point2d start) {
        List<UnitInPool> units = observation().getUnits(Alliance.NEUTRAL);
        double distance = Double.MAX_VALUE;
        Unit target = null;
        for (UnitInPool unitInPool : units) {
            Unit unit = unitInPool.unit();
            if (unit.getType().equals(Units.NEUTRAL_MINERAL_FIELD)) {
                double d = unit.getPosition().toPoint2d().distance(start);
                if (d < distance) {
                    distance = d;
                    target = unit;
                }
            }
        }
        return Optional.ofNullable(target);
    }

    protected Consumer<UnitInPool> attackEnemyStructureWith(UnitInPool unitInPool) {
        return enemyUnit -> actions().unitCommand(unitInPool.unit(), Abilities.ATTACK, enemyUnit.unit(), false);
    }

    protected Runnable attackEnemyPositionWith(UnitInPool unitInPool) {
        return () -> findEnemyPosition()
                .ifPresent(point2d -> actions().unitCommand(unitInPool.unit(), Abilities.SMART, point2d, false));
    }

}
