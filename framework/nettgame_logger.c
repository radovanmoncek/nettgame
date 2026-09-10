#include <stdio.h>

struct nettgame_logger {
    enum level {
	debug=7,
	info=6,
	error=3,
	fatal_error=2
    };

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
