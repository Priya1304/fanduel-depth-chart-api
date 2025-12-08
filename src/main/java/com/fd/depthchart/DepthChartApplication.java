package com.fd.depthchart;

import com.fd.depthchart.config.LeagueCatalogProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(LeagueCatalogProperties.class)
public class DepthChartApplication {

    public static void main(String[] args) {
        SpringApplication.run(DepthChartApplication.class, args);
    }

}
