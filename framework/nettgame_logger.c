#pragma once

#include <stdio.h>

enum level {
    debug=7,
    info=6,
    error=3,
    fatal_error=2
};

struct nettgame_logger {
    level log_level = level::info;

    void log_at_level(level level_) {
	log_level=level_;
    }

    void log_info(const char *message) {
	if (log_level >= level::info)
	    printf("[INFO]: %s\n", message);
    }

    void log_debug(const char *message) {
	if (log_level >= level::debug)
	    printf("[DEBUG]: %s\n", message);
    }

    void log_error(const char *message) {
	if (log_level >= level::error)
	    printf("[ERROR]: %s\n", message);
    }

    void log_fatal_error(const char *message) {
	if (log_level >= level::fatal_error)
	    printf("[FATAL_ERROR]: %s\n", message);
    }
};
