#include <mutex>
#include <map>
#include <chrono>
#include <thread>

//#include "nettgame_logger.c" // fix to .h header from headers
#include "headers/nettgame.hpp"
//#include "headers/nettgame_game_session.hpp"

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
	game_session<GameState>::game_session(std::thread::id unique, std::chrono::milliseconds tick_rate, void (*perform_business_logic)(GameState*, game_session<GameState>*), void *state, std::mutex &game_session_sync, void *server):
	    unique(unique),
	    tick_rate(tick_rate), 
	    perform_business_logic(perform_business_logic), 
	    state(state),
	    game_session_sync(game_session_sync),
	    server(server) {
	    }
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
    template<class GameState>
	void game_session<GameState>::run() {
	    static_cast<nettgame_server<GameState>*>(server)->log_info(("statring new game session with tick rate of " + std::to_string(tick_rate.count())).c_str());

	    auto last_time = std::chrono::steady_clock::now();

	    while (running) {
		{
		    perform_business_logic(static_cast<GameState*>(state), this); // make C style cast

		    switch (game_session_code) {
			case game_session_code::exit: 
			    {
				running = false;

				break;
			    }
			case game_session_code::ok:
			    {
				break;
			    }
			case game_session_code::updated:
			    {
				/*nettgame_*/static_cast<nettgame_server<GameState/***/>*>(server)/*.*/->make_multicast_state_call(*static_cast<GameState*>(state)); // make C style cast
			    }

			    break;
		    }
		}

		auto time = std::chrono::steady_clock::now();
		std::chrono::duration<double, std::milli> delta_time=time-last_time;
		std::chrono::duration<double, std::milli> additional_sleep=tick_rate-delta_time;

		std::this_thread::sleep_for(additional_sleep);

		last_time = std::chrono::steady_clock::now();
	    }
	}

    template<class GameState>
    void game_session<GameState>::perform(void (*action)(void*)) {
	perform(state);
    }

    template<class GameState>
	void game_session<GameState>::synchronize(const std::function<void()> &thread_unsafe_action) {
	    std::lock_guard<std::mutex> game_session_guard(game_session_sync);

	    thread_unsafe_action();
	}
    template<class GameState>
	void game_session<GameState>::assign_affinity_to_slot(std::string address, short signed int port, int arbitrary) {
	    if (has_affinity(address, port))
		return;

	    affinities[std::make_tuple(address, port)] = arbitrary;
	}
    template<class GameState>
	int game_session<GameState>::retrieve_affinity(std::string address, short signed int port) {
	    if (!has_affinity(address, port))
		return INT_MIN;

	    return affinities[std::make_tuple(address, port)];
	}
    template<class GameState>
	bool game_session<GameState>::has_affinity(std::string address, short signed int port) {
	    if (auto value = affinities.find(std::make_tuple(address, port)); value!=affinities.end()) {
		return true;
	    }

	    return false;
	}
    template<class GameState>
	int game_session<GameState>::remove_affinity(std::string address, short signed int port) {
	    for (auto current = affinities.begin(); current!=affinities.end();) {
		if (current->first==std::make_tuple(address, port)) {
		    auto arbitrary = current->second;

		    affinities.erase(current);

		    return arbitrary;
		}
		else
		    ++current;
	    }

	    return INT_MIN;
	}
    template<class GameState>
	int game_session<GameState>::affinities_count() {
	    return affinities.size();
	}
    template<class GameState>
	game_session<GameState>::~game_session(){
	    log_info(std::string("game session stateful memory will be gracefuly deleted/deallocated").c_str());

	    delete static_cast<GameState*>(state);
	}
    template<class GameState>
	void game_session<GameState>::log_info(const char *message) {
	    static_cast<nettgame_server<GameState>*>(server)->log_info(message);
	}
    template<class GameState>
	void game_session<GameState>::log_debug(const char *message) {
	    static_cast<nettgame_server<GameState>*>(server)->log_debug(message);
	}
    template<class GameState>
	void game_session<GameState>::log_error(const char *message) {
	    static_cast<nettgame_server<GameState>*>(server)->log_error(message);
	}
    template<class GameState>
	void game_session<GameState>::log_fatal_error(const char *message) {
	    static_cast<nettgame_server<GameState>*>(server)->log_fatal_error(message);
	}
}

//#include "../nettgame_game_session.cpp"
