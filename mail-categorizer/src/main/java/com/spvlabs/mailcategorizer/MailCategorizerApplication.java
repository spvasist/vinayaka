package com.spvlabs.mailcategorizer;

import com.spvlabs.mailcategorizer.services.OverlapComputeService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;


@Component
@SpringBootApplication
public class MailCategorizerApplication {

    final OverlapComputeService overlapComputeService;

    public MailCategorizerApplication(OverlapComputeService overlapComputeService) {

        this.overlapComputeService = overlapComputeService;
        overlapComputeService.computeAndSaveOverlap();
    }

    public static void main(String[] args) {
        SpringApplication.run(MailCategorizerApplication.class, args);
    }

}
