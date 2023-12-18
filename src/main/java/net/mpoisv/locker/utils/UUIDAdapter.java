package net.mpoisv.locker.utils;

import java.util.UUID;

public class UUIDAdapter {
    public static UUID fromString(String uuid) {
        if(uuid.contains("-")) return UUID.fromString(uuid);
        if(uuid.length() != 32) return null;
        var builder = new StringBuilder();
        builder.append(uuid.substring(8))
                .append('-')
                .append(uuid, 8, 12)
                .append('-')
                .append(uuid, 12, 16)
                .append('-')
                .append(uuid, 16, 20)
                .append('-')
                .append(uuid, 20, 36);
        return UUID.fromString(builder.toString());
    }
}
