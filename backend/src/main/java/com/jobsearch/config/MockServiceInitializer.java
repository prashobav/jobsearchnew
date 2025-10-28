package com.jobsearch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MockServiceInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MockServiceInitializer.class);

    @Value("${app.mock.enabled:false}")
    private boolean mockEnabled;

    @Override
    public void run(String... args) throws Exception {
        if (mockEnabled) {
            logger.info("🔧 MOCK SERVICES ENABLED");
            logger.info("┌─────────────────────────────────────────────────────────┐");
            logger.info("│  MOCK MODE: External API calls are simulated           │");
            logger.info("│  ✅ No rate limiting issues                            │");
            logger.info("│  ✅ No API key requirements                            │");
            logger.info("│  ✅ Instant responses with Indian job data             │");
            logger.info("│  ✅ Perfect for development and testing                │");
            logger.info("│                                                         │");
            logger.info("│  Mock APIs available:                                   │");
            logger.info("│  • GET  /api/mock/status - Check mock status           │");
            logger.info("│  • POST /api/mock/enable - Enable mock mode            │");
            logger.info("│  • POST /api/mock/disable - Disable mock mode          │");
            logger.info("│  • GET  /api/mock/sample-responses - View mock data    │");
            logger.info("└─────────────────────────────────────────────────────────┘");
        } else {
            logger.info("🌐 REAL SERVICES ENABLED");
            logger.info("┌─────────────────────────────────────────────────────────┐");
            logger.info("│  PRODUCTION MODE: Using real external APIs             │");
            logger.info("│  ⚠️  Rate limiting may apply                           │");
            logger.info("│  ⚠️  API keys required                                 │");
            logger.info("│  ⚠️  Subject to external API availability             │");
            logger.info("│                                                         │");
            logger.info("│  To switch to mock mode, set:                          │");
            logger.info("│  app.mock.enabled=true                                  │");
            logger.info("│  Or use: POST /api/mock/enable                          │");
            logger.info("└─────────────────────────────────────────────────────────┘");
        }
    }
}