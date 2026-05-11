package com.cinema.springcinema.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class StartupInfoListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(StartupInfoListener.class);

    private final Environment env;

    public StartupInfoListener(Environment env) {
        this.env = env;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String protocol = "true".equalsIgnoreCase(env.getProperty("server.ssl.enabled")) ? "https" : "http";
        String serverPort = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "/");
        if (!contextPath.endsWith("/")) {
            contextPath = contextPath + "/";
        }

        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("Could not determine host address, defaulting to 'localhost'");
        }

        String appName = env.getProperty("spring.application.name", "Spring Cinema");
        String profiles = env.getActiveProfiles().length == 0
            ? String.join(", ", env.getDefaultProfiles())
            : String.join(", ", env.getActiveProfiles());

        String banner = AnsiOutput.toString(
            "\n",
            AnsiColor.GREEN, "----------------------------------------------------------\n",
            AnsiColor.GREEN, "  ", AnsiStyle.BOLD, appName, AnsiStyle.NORMAL,
            AnsiColor.GREEN, " is running! Access URLs:\n",
            AnsiColor.GREEN, "  Local:       ", AnsiColor.DEFAULT,
            protocol, "://localhost:", serverPort, contextPath, "\n",
            AnsiColor.GREEN, "  External:    ", AnsiColor.DEFAULT,
            protocol, "://", hostAddress, ":", serverPort, contextPath, "\n",
            AnsiColor.GREEN, "  API:         ", AnsiColor.DEFAULT,
            protocol, "://localhost:", serverPort, "/api\n",
            AnsiColor.GREEN, "  Actuator:    ", AnsiColor.DEFAULT,
            protocol, "://localhost:", serverPort, "/actuator\n",
            AnsiColor.GREEN, "  Profile(s):  ", AnsiColor.DEFAULT, profiles, "\n",
            AnsiColor.GREEN, "  Angular:     ", AnsiColor.DEFAULT,
            "http://localhost:4200  ", AnsiStyle.FAINT, "(run 'ng serve' in frontend/)\n",
            AnsiColor.GREEN, "----------------------------------------------------------",
            AnsiColor.DEFAULT);

        log.info(banner);
    }
}
