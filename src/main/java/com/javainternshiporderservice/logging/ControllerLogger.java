package com.javainternshiporderservice.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class ControllerLogger {

    private static final Logger log = LoggerFactory.getLogger(ControllerLogger.class);

    public void methodCalled(String controller, String method, Object... args) {
        if (args == null || args.length == 0) {
            log.info("{}.{} called", controller, method);
            return;
        }
        
        log.info("{}.{} called ({} args)", controller, method, args.length);

       
        if (log.isDebugEnabled()) {
            log.debug("{}.{} args={}", controller, method, sanitizeArgs(args));
        }
    }

    private static String sanitizeArgs(Object[] args) {
        return Arrays.stream(args)
                .map(ControllerLogger::sanitizeArg)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static String sanitizeArg(Object arg) {
        if (arg == null) {
            return "null";
        }
        if (arg instanceof String s) {
            return "\"" + abbreviate(s) + "\"";
        }
        return String.valueOf(arg);
    }

    private static String abbreviate(String input) {
        final int maxLen = 64;
        if (input.length() <= maxLen) {
            return input;
        }
        return input.substring(0, maxLen) + "...";
    }
}
