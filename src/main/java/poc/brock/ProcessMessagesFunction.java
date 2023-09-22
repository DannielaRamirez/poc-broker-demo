package poc.brock;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProcessMessagesFunction {

    public Connection getConnection() throws SQLException {
        String host = "localhost";
        int port = 5432;
        String database = "db-poc-brock";
        String username = "postgres";
        String password = "postgres";

        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    @FunctionName("ProcessMessages")
    public void run(
            @ServiceBusQueueTrigger(name = "message", queueName = "myqueue", connection = "AzureWebJobsServiceBus") String message,
            final ExecutionContext context
    ) {
        context.getLogger().info("Received message: " + message);
        try {
            Connection connection = getConnection();
            String sql = "SELECT * FROM domibroker.brok_message WHERE Processed = 0";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int messageId = resultSet.getInt("id");
                String messageText = resultSet.getString("message");
                context.getLogger().info("Processing message: " + messageId + ", Text: " + messageText);
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


