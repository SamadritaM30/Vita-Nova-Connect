package com.medicalaidtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MedicalAidTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicalAidTrackerApplication.class, args);
    }
}
