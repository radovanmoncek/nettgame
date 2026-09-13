#pragma once

#include <stdio.h>
#include <time.h>

struct nettgame_logger {
    enum level {
	debug=7,
	info=6,
	error=3,
	fatal_error=2
    };

    /**
      * Synopsis:
      *
      * Represents a singular log message sent over the internal service p2p network.
      *
      * Propositions:
      *
      * 2: use log struct instead of parameters/arguments.
      *
      * 1: message_type should be message_level.
      *
      * Attributions:
      *
      * author: Radovan Moncek
      */
    struct log {
	signed int message_length = 0;
	level message_type = level::info;
	char *message;
	signed char is_master = 0x1;
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

	return level::info;
    }

    level log_level = level::info;

    void log_at_level(level level_) {
	log_level=level_;
    }

    void output_to_stdout(const char *level_string, const char *message, signed char is_master) {
	time_t time_ = time(NULL);
	char *text_time = asctime(gmtime(&time_)); // why is there an implicit newline?
	signed int text_time_index = 0;

	while (text_time[text_time_index] != '\000') {
	    if (text_time[text_time_index] == '\n')
		text_time[text_time_index] = '\000';

	    ++text_time_index;
	}

	printf("%s %s %s | %s\n", level_string, text_time, is_master? "master" : "slave", message); //add date, add new master/slave marking system
    }

    void log_info(const char *message, signed char is_master) {
	if (log_level >= level::info)
	    output_to_stdout("INFO", message, is_master);
    }

    void log_debug(const char *message, signed char is_master) {
	if (log_level >= level::debug)
	    output_to_stdout("DEBUG", message, is_master);
    }

    void log_error(const char *message, signed char is_master) {
	if (log_level >= level::error)
	    output_to_stdout("ERROR", message, is_master);
    }

    void log_fatal_error(const char *message, signed char is_master) {
	if (log_level >= level::fatal_error)
	    output_to_stdout("FATAL_ERROR", message, is_master);
    }
};
