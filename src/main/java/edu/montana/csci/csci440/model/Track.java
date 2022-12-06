package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;
import edu.montana.csci.csci440.util.Web;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Track extends Model {

    private Long trackId;
    private Long albumId;
    private Long mediaTypeId;
    private Long genreId;
    private String name;
    private Long milliseconds;
    private Long bytes;
    private BigDecimal unitPrice;
    private String artist;
    private String album;

    public static final String REDIS_CACHE_KEY = "cs440-tracks-count-cache";

    public Track() {
        mediaTypeId = 1l;
        genreId = 1l;
        milliseconds  = 0l;
        bytes  = 0l;
        unitPrice = new BigDecimal("0");
    }

    private Track(ResultSet results) throws SQLException {
        name = results.getString("Name");
        milliseconds = results.getLong("Milliseconds");
        bytes = results.getLong("Bytes");
        unitPrice = results.getBigDecimal("UnitPrice");
        trackId = results.getLong("TrackId");
        albumId = results.getLong("AlbumId");
        mediaTypeId = results.getLong("MediaTypeId");
        genreId = results.getLong("GenreId");
        artist = results.getString("ArtistName");
        album = results.getString("AlbumTitle");
//        artist = this.getArtistName();
//        album = this.getAlbumTitle();
    }

    public static Track find(long i) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT tracks.*, albums.Title AS AlbumTitle, artists.Name AS ArtistName " +
                             "FROM tracks " +
                             "INNER JOIN albums ON tracks.AlbumId = albums.AlbumId " +
                             "INNER JOIN artists ON albums.ArtistId = artists.ArtistId " +
                             "WHERE TrackId=?;"
             )) {
            stmt.setLong(1, i);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Track(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static Long count() {
        Jedis redisClient = new Jedis(); // use this class to access redis and create a cache
        String cache = redisClient.get(REDIS_CACHE_KEY);
        if (cache != null) {
            return Long.parseLong(cache);
        }
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) as Count FROM tracks")) {
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                redisClient.set(REDIS_CACHE_KEY, String.valueOf(results.getLong("Count")));
                return results.getLong("Count");
            } else {
                throw new IllegalStateException("Should find a count!");
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public Album getAlbum() {
        return Album.find(albumId);
    }

    public MediaType getMediaType() {
        return null;
    }
    public Genre getGenre() {
        return null;
    }
    public List<Playlist> getPlaylists(){
        return Playlist.forTracks(this.getTrackId());
    }

    public Long getTrackId() {
        return trackId;
    }

    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(Long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public void setAlbum(Album album) {
        this.album = album.getTitle();
        albumId = album.getAlbumId();
    }

    public Long getMediaTypeId() {
        return mediaTypeId;
    }

    public void setMediaTypeId(Long mediaTypeId) {
        this.mediaTypeId = mediaTypeId;
    }

    public Long getGenreId() {
        return genreId;
    }

    public void setGenreId(Long genreId) {
        this.genreId = genreId;
    }

    public String getArtistName() {
        return this.artist;
    }

    public String getAlbumTitle() {
        return this.album;
    }

    public static List<Track> advancedSearch(int page, int count,
                                             String search, Integer artistId, Integer albumId,
                                             Integer maxRuntime, Integer minRuntime) {
        LinkedList<Object> args = new LinkedList<>();

        String query = "SELECT tracks.*, albums.Title AS AlbumTitle, artists.Name AS ArtistName FROM tracks " +
                "INNER JOIN albums ON tracks.AlbumId = albums.AlbumId " +
                "INNER JOIN artists ON albums.ArtistId = artists.ArtistId " +
                "WHERE tracks.Name LIKE ?";
        args.add("%" + search + "%");

        // Here is an example of how to conditionally
        if (artistId != null) {
            query += " AND artists.ArtistId=? ";
            args.add(artistId);
        }
        if (albumId != null) {
            query += " AND albums.AlbumId=? ";
            args.add(albumId);
        }
        if (maxRuntime != null) {
            query += " AND tracks.Milliseconds < ? ";
            args.add(maxRuntime);
        }
        if (minRuntime != null) {
            query += " AND tracks.Milliseconds > ? ";
            args.add(minRuntime);
        }

        //  include the limit (you should include the page too :)
        query += " LIMIT ?";
        args.add(count);
        query += " OFFSET ?";
        args.add((page - 1) * count);

        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < args.size(); i++) {
                Object arg = args.get(i);
                stmt.setObject(i + 1, arg);
            }
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Track> search(int page, int count, String orderBy, String search) {
        String query = "SELECT * FROM tracks WHERE name LIKE ? ORDER BY " + orderBy + " LIMIT ?";
        search = "%" + search + "%";
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, search);
            stmt.setInt(2, count);
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Track> forAlbum(Long albumId) {
        String query = "SELECT * FROM tracks WHERE AlbumId=?";
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, albumId);
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    // Sure would be nice if java supported default parameter values
    public static List<Track> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Track> all(int page, int count) {
        return all(page, count, "TrackId");
    }

    public static List<Track> all(int page, int count, String orderBy) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT tracks.*, albums.Title as AlbumTitle, artists.Name AS ArtistName " +
                             "FROM tracks " +
                             "INNER JOIN albums ON tracks.AlbumId = albums.AlbumId " +
                             "INNER JOIN artists ON albums.ArtistId = artists.ArtistId " +
                             "ORDER BY " + orderBy + " LIMIT ? OFFSET ?;"
             )) {
            stmt.setInt(1, count);
            stmt.setInt(2, (page - 1) * count);
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public boolean verify() {
        _errors.clear(); // clear any existing errors
        if (name == null || "".equals(name)) {
            addError("Name can't be null or blank!");
        }
        if (milliseconds == null) {
            addError("Milliseconds can't be null!");
        }
        if (bytes == null) {
            addError("Bytes can't be null!");
        }
        if (unitPrice == null) {
            addError("UnitPrice can't be null!");
        }
        if (album == null || "".equals(album)) {
            addError("Album can't be null or blank!");
        }
        return !hasErrors();
    }

    public boolean update() {
        Jedis redisClient = new Jedis(); // use this class to access redis and create a cache
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE tracks " +
                                 "SET Name=?, Milliseconds=?, Bytes=?, UnitPrice=?, AlbumId=?, MediaTypeId=?, GenreId=? " +
                                 "WHERE TrackId=?;")) {
                stmt.setString(1, this.getName());
                stmt.setLong(2, this.getMilliseconds());
                stmt.setLong(3, this.getBytes());
                stmt.setBigDecimal(4, this.getUnitPrice());
                stmt.setLong(5, this.getAlbumId());
                stmt.setLong(6, this.getMediaTypeId());
                stmt.setLong(7, this.getGenreId());
                stmt.setLong(8, this.getTrackId());
                stmt.executeUpdate();
                redisClient.del(REDIS_CACHE_KEY);
                return true;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

    public boolean create() {
        Jedis redisClient = new Jedis(); // use this class to access redis and create a cache
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO tracks " +
                                 "(Name, Milliseconds, Bytes, UnitPrice, AlbumId, MediaTypeId, GenreId) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?);")) {
                stmt.setString(1, this.getName());
                stmt.setLong(2, this.getMilliseconds());
                stmt.setLong(3, this.getBytes());
                stmt.setBigDecimal(4, this.getUnitPrice());
                stmt.setLong(5, this.getAlbumId());
                stmt.setLong(6, this.getMediaTypeId());
                stmt.setLong(7, this.getGenreId());
                stmt.executeUpdate();
                trackId = DB.getLastID(conn);
                redisClient.del(REDIS_CACHE_KEY);
                return true;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

    public void delete() {
        Jedis redisClient = new Jedis(); // use this class to access redis and create a cache
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM tracks WHERE TrackId = ?;")) {
            stmt.setLong(1, this.getTrackId());
            stmt.executeUpdate();
            redisClient.del(REDIS_CACHE_KEY);
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Track> forPlaylist(Long playlistId) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT tracks.*, albums.Title AS AlbumTitle, artists.Name AS ArtistName FROM tracks " +
                             "INNER JOIN albums ON tracks.AlbumId = albums.AlbumId " +
                             "INNER JOIN artists ON albums.ArtistId = artists.ArtistId " +
                             "INNER JOIN playlist_track ON tracks.TrackId = playlist_track.TrackId " +
                             "INNER JOIN playlists ON playlist_track.PlaylistId = playlists.PlaylistId " +
                             "WHERE playlists.PlaylistId = ?" +
                             "ORDER BY Name;"
             )) {
            stmt.setLong(1, playlistId);
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }
}
