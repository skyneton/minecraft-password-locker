package net.mpoisv.locker.manager;

import net.mpoisv.locker.utils.LockData;
import net.mpoisv.locker.utils.Position;
import org.bukkit.Location;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseManager {
    private String path;
    private Connection connection;

    public DatabaseManager(String dir, String file) throws SQLException {
        var f = new File(dir, file);
        if(!f.exists()) {
            try {
                f.createNewFile();
            } catch (Exception e) {
                throw new RuntimeException("Can't create database file. "
                        + e.getLocalizedMessage() + " : " + e.getCause());
            }
        }
        path = f.getAbsolutePath();
        if(path.startsWith("file:"))
            path = path.substring(5);
        path = "jdbc:sqlite:" + path;
        try {
            connection = DriverManager.getConnection(path);
        }catch (Exception e) {
            throw new RuntimeException("Can't create database connection. "
                    + e.getLocalizedMessage() + " : " + e.getCause());
        }
        createTable();
    }

    public void close() throws SQLException {
        if(connection == null || connection.isClosed()) return;
        connection.close();
    }

    private void createTable() throws SQLException {
        var statement = connection.createStatement();
        statement.executeUpdate("create table if not exists lock (" +
                "id integer not null primary key autoincrement," +
                "x integer not null," +
                "y integer not null," +
                "z integer not null," +
                "world varchar(40) not null," +
                "password text not null default('0000')," +
                "strict_lock integer not null check (strict_lock in (0, 1))," +
                "unique (x, y, z, world));");
        statement.close();
    }

    public int insert(int x, int y, int z, UUID world, String password) throws SQLException {
        var ps = connection.prepareStatement("insert into lock(x, y, z, world, password) values(?, ?, ?, ?, ?)");
        ps.setInt(1, x);
        ps.setInt(2, y);
        ps.setInt(3, z);
        ps.setString(4, world.toString());
        ps.setString(5, password);
        var result = ps.executeUpdate();
        ps.close();
        return result;
    }

    public int insert(int x, int y, int z, UUID world, boolean strictLock) throws SQLException {
        var ps = connection.prepareStatement("insert into lock(x, y, z, world, strict_lock) values(?, ?, ?, ?, ?)");
        ps.setInt(1, x);
        ps.setInt(2, y);
        ps.setInt(3, z);
        ps.setString(4, world.toString());
        ps.setBoolean(5, strictLock);
        var result = ps.executeUpdate();
        ps.close();
        return result;
    }

    public int insert(Location location, String password) throws SQLException {
        return insert(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getUID(), password);
    }

    public int insert(Location location, boolean strictLock) throws SQLException {
        return insert(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getUID(), strictLock);
    }

    public int delete(int x, int y, int z, UUID world) throws SQLException {
        var ps = connection.prepareStatement("delete from lock where x = ? and y = ? and z = ? and world = ?;");
        ps.setInt(1, x);
        ps.setInt(2, y);
        ps.setInt(3, z);
        ps.setString(4, world.toString());
        var result = ps.executeUpdate();
        ps.close();
        return result;
    }

    public int delete(Location location) throws SQLException {
        return delete(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getUID());
    }

    public int update(int x, int y, int z, UUID world, String password) throws SQLException {
        var ps = connection.prepareStatement("update lock set password = ? where x = ? and y = ? and z = ? and world = ?;");
        ps.setString(1, password);
        ps.setInt(2, x);
        ps.setInt(3, y);
        ps.setInt(4, z);
        ps.setString(5, world.toString());
        var result = ps.executeUpdate();
        ps.close();
        return result;
    }

    public int update(Location location, String password) throws SQLException {
        return update(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getUID(), password);
    }

    public int update(int x, int y, int z, UUID world, boolean strictLock) throws SQLException {
        var ps = connection.prepareStatement("update lock set strict_lock = ? where x = ? and y = ? and z = ? and world = ?;");
        ps.setBoolean(1, strictLock);
        ps.setInt(2, x);
        ps.setInt(3, y);
        ps.setInt(4, z);
        ps.setString(5, world.toString());
        var result = ps.executeUpdate();
        ps.close();
        return result;
    }

    public int update(Location location, boolean strictLock) throws SQLException {
        return update(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getUID(), strictLock);
    }

    public LockData select(int x, int y, int z, UUID world) throws SQLException {
        var ps = connection.prepareStatement("select password, strict_lock from lock where x = ? and y = ? and z = ? and world = ?;");
        ps.setInt(1, x);
        ps.setInt(2, y);
        ps.setInt(3, z);
        ps.setString(4, world.toString());
        var rs = ps.executeQuery();
        String password = null;
        boolean strictLock = false;
        boolean find = false;
        if(rs.next()) {
            password = rs.getString(1);
            strictLock = rs.getBoolean(2);
            find = true;
        }
        rs.close();
        ps.close();
        if(!find) return null;
        return new LockData(password, strictLock, new Position(x, y, z, world));
    }

    public LockData select(Location location) throws SQLException {
        return select(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getUID());
    }

    public int getCount() throws SQLException {
        var statement = connection.createStatement();
        var rs = statement.executeQuery("select count(id) from lock;");
        int count = 0;
        if(rs.next())
            count = rs.getInt(1);
        rs.close();
        statement.close();
        return count;
    }
}
