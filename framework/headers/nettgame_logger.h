#pragma once

#include <stdio.h>
#include <time.h>

/**
 * Synopsis:
 *
 * Internal logger of nettgame.
 *
 * Description:
 *
 * Logs to stdout.
 *
 * Attributions:
 *
 * author: Radovan Moncek
 */
struct nettgame_logger {
	/**
	 * Synopsis:
	 *
	 * The level of the log.
	 *
	 * Description:
	 *
	 * Represents SYSLOG levels, as defined/specified by RFC.
	 */
	enum level {
	    debug=7,
	    info=6,
	    error=3,
	    fata_error=2
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
	struct log;

	static level to_level(int numeric_level);

	level log_level = level::info;

	void log_at_level(level level_);

	void output_to_stdout(const char *level_string, const char *message, signed char is_master);

	void log_info(const char *message, signed char is_master);

	void log_debug(const char *message, signed char is_master);

	void log_error(const char *message, signed char is_master);

	void log_fatal_error(const char *message, signed char is_master);
};
