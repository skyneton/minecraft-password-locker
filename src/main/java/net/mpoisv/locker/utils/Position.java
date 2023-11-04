package net.mpoisv.locker.utils;

import org.bukkit.Location;

import java.util.Objects;
import java.util.UUID;

public record Position(int x, int y, int z, UUID world) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y && z == position.z && Objects.equals(world, position.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, world);
    }

    public static Position CreatePosition(Location loc) {
        return new Position(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), Objects.requireNonNull(loc.getWorld()).getUID());
    }

}