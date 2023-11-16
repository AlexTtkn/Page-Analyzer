package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.controller.RootController;
import hexlet.code.repository.BaseRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import lombok.extern.slf4j.Slf4j;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
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

    private static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates", classLoader);
        return TemplateEngine.create(codeResolver, ContentType.Html);
    }

    public static Javalin getApp() throws SQLException {
        var hikariConfig = new HikariConfig();
        setDataBase(hikariConfig);

        var dataSource = new HikariDataSource(hikariConfig);
        var urlStream = App.class.getClassLoader().getResourceAsStream("schema.sql");
        var reader = new BufferedReader(new InputStreamReader(urlStream));
        var sql = reader.lines().collect(Collectors.joining());

        log.info(sql);
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute(sql);
        }
        BaseRepository.dataSource = dataSource;

        var app = Javalin.create(config -> config.plugins.enableDevLogging());

        JavalinJte.init(createTemplateEngine());

        app.get(NamedRoutes.rootPath(), RootController::index);
        return app;
    }
}
