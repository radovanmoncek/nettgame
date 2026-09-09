#include <mutex>
#include <map>
#include <chrono>
#include <thread>

#include "nettgame_logger.c"
#include "nettgame_transferable_game_state.cpp"

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
		    ok,
		    idle //lower game session tick_rate to 1/4
		};

		game_session<GameState>::game_session_code game_session_code = game_session_code::ok;

		game_session(std::thread::id unique, std::chrono::milliseconds tick_rate, void (*perform_business_logic)(GameState*, game_session<GameState>*), void *state, std::mutex &game_session_sync, nettgame_logger logger):
		    unique(unique),
		    tick_rate(tick_rate), 
		    perform_business_logic(perform_business_logic), 
		    state(state),
		    game_session_sync(game_session_sync),
		    logger(logger) {
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
		void run() {
		    logger.log_info(("statring new game session with tick rate of " + std::to_string(tick_rate.count())).c_str());

		    auto last_time = std::chrono::steady_clock::now();

		    while (running) {
			{
			    perform_business_logic(static_cast<GameState*>(state), this);

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
			    }
			}

			auto time = std::chrono::steady_clock::now();
			std::chrono::duration<double, std::milli> delta_time=time-last_time;
			std::chrono::duration<double, std::milli> additional_sleep=tick_rate-delta_time;

			std::this_thread::sleep_for(additional_sleep);

			last_time = std::chrono::steady_clock::now();
		    }
		}
		void synchronize(const std::function<void()> &thread_unsafe_action) {
		    std::lock_guard<std::mutex> game_session_guard(game_session_sync);

		    thread_unsafe_action();
		}
		void assign_affinity_to_slot(std::string address, short signed int port, int arbitrary) {
		    if (has_affinity(address, port))
			return;

		    affinities[std::make_tuple(address, port)] = arbitrary;
		}
		int retrieve_affinity(std::string address, short signed int port) {
		    if (!has_affinity(address, port))
			return INT_MIN;

		    return affinities[std::make_tuple(address, port)];
		}
		bool has_affinity(std::string address, short signed int port) {
		    if (auto value = affinities.find(std::make_tuple(address, port)); value!=affinities.end()) {
			return true;
		    }

		    return false;
		}
		int remove_affinity(std::string address, short signed int port) {
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
		int affinities_count() {
		    return affinities.size();
		}
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
		}
		~game_session(){
		    logger.log_info(std::string("game session stateful memory will be gracefuly deleted/deallocated").c_str());

		    delete static_cast<GameState*>(state);
		}
	    private:
		nettgame_logger logger;
		std::chrono::milliseconds tick_rate = std::chrono::milliseconds(16);
		void (*perform_business_logic)(GameState*, game_session<GameState>*);
		std::mutex &game_session_sync;
		void *state;
		bool running = true;
		std::map<std::tuple<std::string, short signed int>, int> affinities;
		std::thread::id unique; //use better unique identifier
	};
}
