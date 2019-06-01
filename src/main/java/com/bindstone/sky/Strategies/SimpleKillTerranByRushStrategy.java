package com.bindstone.sky.Strategies;

import com.github.ocraft.s2client.bot.gateway.ObservationInterface;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class SimpleKillTerranByRushStrategy {

    /**
     * Find a target in order:
     * 1) Marine, Reaper, Marauder, Hellion, Cyclone
     * 2) Worker, Mule
     * 3) Key Building (Barracks, Headquarter, Orbital Command, Fortress, Factory)
     * 4) Rest
     */
    public Optional<UnitInPool> findTarget(ObservationInterface observation) {
        Optional<UnitInPool> first = observation.getUnits(Alliance.ENEMY, isOffensiveUnit()).stream().findFirst();
        if (first.isPresent()) {
            return first;
        }
        Optional<UnitInPool> second = observation.getUnits(Alliance.ENEMY, isWorkerUnit()).stream().findFirst();
        if (second.isPresent()) {
            return second;
        }
        Optional<UnitInPool> third = observation.getUnits(Alliance.ENEMY, isKeyBuilding()).stream().findFirst();
        if (third.isPresent()) {
            return third;
        }
        return observation.getUnits(Alliance.ENEMY, isEnemyUnit()).stream().findFirst();
    }

    private Predicate<UnitInPool> isOffensiveUnit() {
        return unitInPool -> Set
                .of((UnitType) Units.TERRAN_MARINE, Units.TERRAN_REAPER, Units.TERRAN_MARAUDER, Units.TERRAN_HELLION, Units.TERRAN_CYCLONE)
                .contains(unitInPool.unit().getType());
    }

    private Predicate<UnitInPool> isWorkerUnit() {
        return unitInPool -> Set
                .of((UnitType) Units.TERRAN_SCV, Units.TERRAN_MULE)
                .contains(unitInPool.unit().getType());
    }

    private Predicate<UnitInPool> isKeyBuilding() {
        return unitInPool -> Set
                .of((UnitType) Units.TERRAN_COMMAND_CENTER, Units.TERRAN_PLANETARY_FORTRESS, Units.TERRAN_ORBITAL_COMMAND, Units.TERRAN_BARRACKS, Units.TERRAN_FACTORY)
                .contains(unitInPool.unit().getType());
    }

    private Predicate<UnitInPool> isEnemyUnit() {
        return unitInPool -> true;
    }

}
