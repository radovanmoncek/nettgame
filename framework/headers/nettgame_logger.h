#pragma once

#include <stdio.h>
#include <time.h>

struct nettgame_logger;

enum nettgame_logger::level;

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
struct nettgame_logger::log;
    
static level nettgame_logger::to_level(int numeric_level);

nettgame_logger::level nettgame_logger::log_level = level::info;

void nettgame_logger::log_at_level(level level_);

void nettgame_logger::output_to_stdout(const char *level_string, const char *message, signed char is_master);

void nettgame_logger::log_info(const char *message, signed char is_master);

void nettgame_logger::log_debug(const char *message, signed char is_master);

void nettgame_logger::log_error(const char *message, signed char is_master);

void nettgame_logger::log_fatal_error(const char *message, signed char is_master);
