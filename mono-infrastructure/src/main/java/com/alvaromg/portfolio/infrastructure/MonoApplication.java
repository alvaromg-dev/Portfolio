package com.alvaromg.portfolio.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import com.alvaromg.portfolio.common.utils.BannerUtil;
import com.alvaromg.portfolio.common.utils.TimerUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Application bootstrap.
 */
@SpringBootApplication(scanBasePackages = "com.alvaromg.portfolio")
@Slf4j
public class MonoApplication {

    /**
     * Application entry point.
     *
     * @param args standard JVM arguments
     */
    public static void main(String[] args) {
        TimerUtil timer = TimerUtil.create().start();
        ConfigurableApplicationContext ctx = SpringApplication.run(MonoApplication.class, args);
        BannerUtil.create().name("Portfolio Backend").port(port(ctx)).time(timer.stop().getSeconds() + "s").print();
    }

    /**
     * Resolve the HTTP port bound by the embedded web server.
     *
     * @param ctx application context returned by {@link SpringApplication#run}
     * @return the bound port, or -1 if it cannot be determined
     */
    private static int port(ConfigurableApplicationContext ctx) {
        if (ctx instanceof WebServerApplicationContext wac) {
            return wac.getWebServer().getPort();
        } else {
            String p = ctx.getEnvironment().getProperty("local.server.port");
            if (p != null && !p.isBlank()) {
                try {
                    return Integer.parseInt(p.trim());
                } catch (NumberFormatException e) {
                    log.error("Failed to resolve server port from application context.", e);
                }
            }
        }
        return -1;
    }
}
