package com.bindstone.sky.bots;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Ability;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.response.ResponseGameInfo;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Unit;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
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
        debug().debugTextOut("[SKY] Bot activated and ready for conquer", Color.BLUE);
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

    private Optional<UnitInPool> getRandomUnit(UnitType unitType) {
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

}
