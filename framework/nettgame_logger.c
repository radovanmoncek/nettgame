#pragma once

#include <stdio.h>

struct nettgame_logger {
    enum level {
	debug=7,
	info=6,
	error=3,
	fatal_error=2
    };

    /**
      * Propositions:
      *
      * 1: message_type should be message_level.
      */
    struct log {
	signed int message_length;
	level message_type;
	char *message;
    };

    static level to_level(int numeric_level) {
	switch (numeric_level) {
	    case level::debug:
		{
		    return level::debug;
		}
		break;
	    case info:
		{
		    return level::info;
		}
		break;
	    case error:
		{
		    return level::error;
		}
		break;
	    case fatal_error:
		{
		    return level::fatal_error;
		}
		break;
	}

	return /*numeric_level*/level::info;
    }

    level log_level = level::info;
    signed char is_master = 1;

    void log_at_level(level level_) {
	log_level=level_;
    }

    void output_to_stdout(const char *level_string, const char *message) {
	printf("[%s] from %s server: %s\r\n", level_string, is_master? "master" : "slave", message); //add date
    }

    void log_info(const char *message) {
	if (log_level >= level::info)
	    output_to_stdout("INFO", message);
    }

    void log_debug(const char *message) {
	if (log_level >= level::debug)
	    output_to_stdout("DEBUG", message);
    }

    void log_error(const char *message) {
	if (log_level >= level::error)
	    output_to_stdout("ERROR", message);
    }

    void log_fatal_error(const char *message) {
	if (log_level >= level::fatal_error)
	    output_to_stdout("FATAL_ERROR", message);
    }
};
