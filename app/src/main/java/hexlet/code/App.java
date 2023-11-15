package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.repository.BaseRepository;
import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.stream.Collectors;

@Slf4j
public final class App {
    private static final String PORT = "8080";
    private static final String JDBC_URL = "jdbc:h2:mem:hexlet_project;DB_CLOSE_DELAY=-1;";

    public static void main(String[] args) throws SQLException, IOException {
        Javalin app = getApp();
        app.start(getPort());
    }

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", PORT);
        return Integer.parseInt(port);
    }

    private static String getJdbcUrl() {
        return System.getenv().getOrDefault("JDBC_DATABASE_URL", JDBC_URL);
    }

    private static boolean isProd() {
        return System.getenv().getOrDefault("APP_ENV", "dev").equals("production");
    }

    private static void setDataBase(HikariConfig hikariConfig) {
        hikariConfig.setJdbcUrl(getJdbcUrl());
        if (isProd()) {
            var userName = System.getenv("JDBS_DATABASE_USERNAME");
            var password = System.getenv("JDBS_DATABASE_PASSWORD");
            hikariConfig.setUsername(userName);
            hikariConfig.setPassword(password);
        }
    }

    public static Javalin getApp() throws SQLException, IOException {
        var hikariConfig = new HikariConfig();
        setDataBase(hikariConfig);

        var dataSource = new HikariDataSource(hikariConfig);
        var url = App.class.getClassLoader().getResource("schema.sql");
        var file = new File(url.getFile());
        var sql = Files.lines(file.toPath())
                .collect(Collectors.joining("\n"));

        log.info(sql);
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute(sql);
        }
        BaseRepository.dataSource = dataSource;

        var app = Javalin.create(config -> config.plugins.enableDevLogging());

        app.get("/", ctx -> ctx.result("Hello, World!"));
        return app;
    }
}
