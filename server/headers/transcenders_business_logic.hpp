#include "transcenders_game_state.hpp"

/**
 * Synopsis:
 *
 * Performs business logic for each tick.
 *
 * Description:
 *
 * This callback is passed to the nettgame_server game_session.
 *
 * Attributions:
 *
 * source: https://stackoverflow.com/questions/5585532
 *
 * author: Radovan Moncek
 */
void broadcast_do_for_all(game_state *state_, nettgame::game_session<game_state> *game_session);
