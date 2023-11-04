package net.mpoisv.locker.utils;

public record LockData(String password, boolean strictLock, Position position) {
}
