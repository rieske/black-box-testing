package io.github.rieske;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

class Repository {
    private final DataSource dataSource;

    Repository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    void saveMessage(Message message) {
        try (var connection = dataSource.getConnection();
             var stmt = connection.prepareStatement("INSERT INTO messages(id, payload) VALUES(?, ?)")) {
            stmt.setObject(1, message.id());
            stmt.setString(2, message.payload());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Message> findMessage(UUID id) {
        try (var connection = dataSource.getConnection();
             var stmt = connection.prepareStatement("SELECT * FROM messages WHERE id=?")) {
            stmt.setObject(1, id);
            try (var resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new Message(
                            resultSet.getObject("id", UUID.class),
                            resultSet.getString("payload"))
                    );
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    void deleteMessage(UUID id) {
        try (var connection = dataSource.getConnection();
             var stmt = connection.prepareStatement("DELETE FROM messages WHERE id=?")) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
