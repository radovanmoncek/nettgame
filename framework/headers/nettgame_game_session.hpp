#pragma once

#include <map>
#include <functional>
#include <climits>

#include "nettgame_transferable_game_state.hpp"

namespace nettgame {
    /**
     * Synopsis:
     *
     * game_session is a logical wrapper of an emphemeral thread/inferior process that serves stateful memory to a business logic callback closure/lambda at a specified tick_rate.
     *
     * Description:
     *
     * Any game_session instance is not bound to a specific Nettgame server, and will be automatically migrated upon crash, or load balancing requirements.
     *
     * Affinity is an arbitrary representation of a connected player, represented by a tuple that holds an IP address and an ephemeral port number.
     * The tuple is thus effectively an endpoint.
     * 
     * Every game session holds any number of arbitrary connections called *affinities*.
     *
     * Attributions:
     *
     * Author: Radovan Moncek
     */
    template<class GameState>
    class game_session {
	    public:
		/**
		 * Synopsis:
		 *
		 * A code set by business logic callback that is evaluated after each server tick.
		 *
		 * Description:
		 *
		 * The codes indicate action to be taken which alters game_session lifecycle.
		 */
		enum game_session_code {
		    /**
		     * Synopsis:
		     *
		     * game_session will exit after finishing the cycle in which this code was set.
		     */
		    exit,
		    /**
		    * Synopsis:
		    *
		    * game_session will continue standard operation.
		    *
		    * Description:
		    *
		    * Serves as an aknowledgement of correct operation.
		    *
		    * Attributions:
		    *
		    * author: Radovan Moncek
		    */
		    ok,
		    idle, //lower game session tick_rate to 1/4
		    /**
		      * Synopsis:
		      *
		      * Notifies about game_state update.
		      *
		      * Attributions:
		      *
		      * author: Radovan Moncek
		      */
		    updated
		};

		game_session<GameState>::game_session_code game_session_code = game_session_code::ok;

		game_session(std::thread::id unique, std::chrono::milliseconds tick_rate, void (*perform_business_logic)(GameState*, game_session<GameState>*), void *state, std::mutex &game_session_sync, void *nettgame_server);
		/**
		 * Synopsis:
		 *
		 * Run this game_session.
		 *
		 * Description:
		 *
		 * game_session runs until running becomes false.
		 *
		 * It is guaranteed that each tick will take at least tick_rate time.
		 *
		 * It is guaranteed that stateful information will be passed, unaltered, to each callback call.
		 *
		 * It is guaranteed that, in the instane of an outage of the executing nettgame server, the game_session will be re-run on a different nettgame executing server, if there is one available, and any stateful information will be transfered, as well, as per the previous guarantee.
		 *
		 * I/O:
		 *
		 * Blocks, until running becomes false.
		 *
		 * Thread safety:
		 *
		 * IS NOT thread safe.
		 *
		 * Purity:
		 *
		 * TBD.
		 *
		 * Attributions:
		 *
		 * Author: Radovan Moncek
		 */
		void run();
		void perform(void (*perform)(void*));
		void synchronize(const std::function<void()> &thread_unsafe_action);
		void assign_affinity_to_slot(std::string address, short signed int port, int arbitrary);
		int retrieve_affinity(std::string address, short signed int port);
		bool has_affinity(std::string address, short signed int port);
		int remove_affinity(std::string address, short signed int port);
		int affinities_count();
		/**
		 * Attributions:
		 *
		 * source: the Java platform
		 * source: https://stackoverflow.com/questions/4421706/what-are-the-basic-rules-and-idioms-for-operator-overloading/77055231#77055231
		 *
		 * author: Radovan Moncek
		 */
		friend bool operator==(game_session<GameState> &first, game_session<GameState> &second) {
		    return first.unique == second.unique;
		};
		~game_session();
		void log_info(const char *message);
		void log_debug(const char *message);
		void log_error(const char *message);
		void log_fatal_error(const char *message);

	    private:
		void *server;
		std::chrono::milliseconds tick_rate = std::chrono::milliseconds(16);
		void (*perform_business_logic)(GameState*, game_session<GameState>*);
		std::mutex &game_session_sync;
		void *state;
		bool running = true;
		std::map<std::tuple<std::string, short signed int>, int> affinities;
		std::thread::id unique; //use better unique identifier
	};
}

//#include "../nettgame_game_session.cpp"
