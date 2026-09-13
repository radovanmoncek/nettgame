#pragma once

#include "nettgame_game_session.hpp"

namespace nettgame {
    /**
     * Synopsis:
     *
     * nettgame_server is a "server" that provisions emphemeral game_session instances via threads, and is not build upon any preexisting library/framework, only the standard library of the C++ language standard (std).
     *
     * Begin example:
     *
     * #include <nettgame.cpp>
     *
     * int main() {
     *     nettgame::nettgame_server<YourStateSerializableType> nettgame_server("127.0.0.1", 4321, 1); // [1]
     *
     *     nettgame_server.start_new_game_session(std::chrono::milliseconds(16), your_lambda_closure_with_business_logic, YourStateSerializable); // [2]
     * }
     *
     * Begin comments:
     *
     * [1]: start a new nettgame_server, and bind internal_service_socket to 127.0.0.1:4321:tcp. This server is a 1 - master (0 would stand for slave).
     *
     * [2]: starts managing a new encapsulated-thread at tick_rate 1/60, run your_lambda_closure_with_business_logic, and pass YourStateSerializable to each tick iteration.
     *
     * End comments.
     *
     * End example.
     *
     * Longer description:
     *
     * The ip, and port passed to the constructor are of pure logical sentiment, and do not, in any way, bind, as in not performing the bind action of the operating system, this "server" to them.
     *
     * This server in meant to run inside the environment of K8s/K3s.
     *
     * Responsibilities of this nettgame_server:
     *
     * - provisioning of game_session instances via runner threads,
     * - handling thread unsafe operations within runner threads,
     * - automatic failover,
     * - load balancing,
     * - transfering game_session stateful information to other replicas without information loss,
     * -  and management of stateful information within game_session instances.
     *
     * Responsibilities of the implementor:
     *
     * - business logic,
     * - calling the synchronize member functions when performing thread unsafe work within game sessions/runner threads,
     * - transport layer protocol provisioning via TCP/UDP/KCP/RUDP, etc. (player session containers, listeners, etc.),
     * -  and application layer protocol provisioning (data transfer, codecs, etc.).
     *
     * Attributions:
     *
     * source: Tsoding, terrific C programmer, creator of nob.h (you have forever my biggest gratitude)
     * source: Yegor256, OOP ideologist, programmer, and public speaker on matters of "real OOP" (you have forever my biggest gratitude)
     *
     * author: Radovan Moncek
     */
    template<class GameState>
	class nettgame_server {
	    private:
		nettgame_logger logger;
		std::vector<std::thread> runners;
		std::vector<std::shared_ptr<game_session<GameState>>> game_sessions;
		std::mutex game_session_sync;
		std::map<std::tuple<std::string, short signed int>, std::vector<std::string>> shared_affinities;
		bool is_master;
		int internal_service_socket = 0x0;
		int internal_service_client_sockets[4]; // formerly std::vector<int>, replace magic constant
		int internal_service_client_sockets_length = 0;
		std::thread internal_service_listener;
		std::mutex internal_service_sync;
		std::mutex log_sync;
		bool signal_received = false;

		/**
		 * Synopsis:
		 *
		 * Actually performs the call to logger.
		 *
		 * Description:
		 *
		 * If the current server is not master, the logger call is sent over the internal service p2p network to the master server, where the actual log is sent to the print stream.
		 *
		 * I/O:
		 *
		 * Does not block.
		 * 
		 * Thread safety:
		 *
		 * IS thread safe.
		 *
		 * Propositions:
		 * 
		 * Attributions:
		 *
		 * author: Radovan Moncek
		 */
		void make_logger_call(nettgame_logger::level level, const char *message, signed char is_master);
		/**
		 * Synopsis:
		 *
		 * Starts internal_service event loop.
		 *
		 * Listenes to other insternal service p2p network members, and connects them with this server.
		 *
		 * Mode of server is of no concern.
		 *
		 * Description:
		 *
		 * TBD.
		 * This member function is meant to be run inside of a "listener thread/lightweight process".
		 *
		 * I/O:
		 *
		 * Blocks indefinitely.
		 * 
		 * Thread safety:
		 *
		 * IS NOT thread safe.
		 *
		 * Propositions:
		 *
		 * 1: formerly was in nettgame constructor - move to this thread.
		 * 
		 * Attributions:
		 *
		 * author: Radovan Moncek
		 */
		void start_internal_service_acceptor(const char *address, short signed int port) {
		    internal_service_socket = socket(AF_INET, SOCK_STREAM, 0);

		    if (internal_service_socket == -1) {
			log_fatal_error("failed to create internal_service_socket");

			return;
		    }

		    struct sockaddr_in server_address;
		    server_address.sin_family = AF_INET;
		    server_address.sin_addr.s_addr = inet_addr(address);
		    server_address.sin_port = htons(port);
		    int bind_result = bind(internal_service_socket, (struct sockaddr *)&server_address, sizeof(server_address)); // C style justified

		    if (bind_result == -1) { //C-style cast, besause I want to eventually re-write to C, see above
			close(internal_service_socket);
			log_fatal_error(("failed to bind to internal_service_socket" + std::to_string(errno)).c_str()); // change to C style strcat?

			return;
		    }

		    if (listen(internal_service_socket, 4*2) < 0) { //set magic constant 4 to number of topology registerees
			log_fatal_error("failed to listen on internal_service_socket");

			return;
		    }

		    log_info("internal_service_socket is listening");

		    //start internal_service event loop, and connect to registered topology p2p members
		    while (1) { //parametrize
			struct sockaddr_in client_address;
			socklen_t client_len = sizeof(client_address);
			int client_socket = accept(internal_service_socket, (struct sockaddr *)&client_address, &client_len);

			{
			    std::lock_guard<std::mutex> internal_service_clients_guard(internal_service_sync);

			    if (client_socket < 0) {
				log_error("failed to accept socket");

				continue;
			    }

			    internal_service_client_sockets[internal_service_client_sockets_length++] = client_socket;
			}

			log_info("successfuly connected with a peer after accepting");
		    }
		}

	    public:
		/**
		 * Synopsis:
		 *
		 * TBD.
		 *
		 * Description:
		 *
		 * TBD.
		 *
		 * I/O:
		 *
		 * Does not block.
		 * 
		 * Thread safety:
		 *
		 * IS thread safe.
		 *
		 * Propositions:
		 * 
		 * Attributions:
		 *
		 * author: Radovan Moncek
		 */
		void handle_signals();
		/**
		 * Synopsis:
		 *
		 * Constructs a new nettgame_server instance, constructs the address and port into internal_service_socket, and sets this server into master/slave mode.
		 *
		 * Description:
		 *
		 * TBD.
		 *
		 * I/O:
		 *
		 * Does not block.
		 * 
		 * Thread safety:
		 *
		 * IS thread safe.
		 * 
		 * Attributions:
		 *
		 * author: Radovan Moncek
		 */
		nettgame_server(const char *address, short signed int port, bool is_master = true); //formerly std::string, std::vector<std::tuple<std::string, short signed int>>
		/**
		 * Synopsis:
		 *
		 * Adds a new topology member to the internal_service_client_sockets list.
		 *
		 * This new topology member will be connected to the internal/service p2p (peer-to-peer) network, and, in-turn, with this server.
		 *
		 * Description:
		 *
		 * TBD.
		 *
		 * I/O:
		 *
		 * Does not block.
		 * 
		 * Thread safety:
		 *
		 * IS NOT thread safe.
		 * 
		 * Attributions:
		 *
		 * author: Radovan Moncek
		 */
		void register_topology_member(const char *address, short signed int port);
		/**
		 * Synopsis:
		 *
		 * Starts internal_service event loop.
		 *
		 * Description:
		 *
		 * TBD.
		 *
		 * I/O:
		 *
		 * Blocks indefinitely.
		 * 
		 * Thread safety:
		 *
		 * IS NOT thread safe.
		 * 
		 * Attributions:
		 *
		 * author: Radovan Moncek
		 */
		void join_internal_service_network();
		/**
		 * Synopsis:
		 *
		 * Starts a new game session by creating a runner std::thread/inferior process, and a new game_session class instance.
		 *
		 * Description:
		 *
		 * TBD.
		 *
		 * I/O:
		 *
		 * Should not block longer than the duration required for creating an instance of the runner std::thread/inferior process, therefore, this member function should not block.
		 * 
		 * Thread safety:
		 *
		 * IS NOT thread safe.
		 * 
		 * Attributions:
		 *
		 * Author: Radovan Moncek
		 */
		void start_new_game_session(std::chrono::milliseconds tick_rate, void (*perform_business_logic)(GameState*, game_session<GameState>*), GameState *state);
		/**
		 * Synopsis:
		 *
		 * Global version of the remove_affinity member function.
		 * 
		 * I/O:
		 *
		 * Blocks, for the duration of linear O(n) for cycle execution, best case scenario is constant O(1).
		 *
		 * Thread safety:
		 *
		 * IS NOT thread safe.
		 * 
		 * Attributions:
		 *
		 * Author: Radovan Moncek
		 */
		int remove_affinity(std::string address, short unsigned int port);
		/**
		 * Synopsis:
		 *
		 * Global version of the synchronize member function.
		 *
		 * Description:
		 *
		 * Acquires an std::mutex game_session_sync, and performs the thread_unsafe_action in the synchronized scope.
		 *
		 * I/O:
		 *
		 * Blocks, until the thread_unsafe_sync finishes its execution, or if it is needed to await the mutex acquisition.
		 *
		 * Thread safety:
		 *
		 * IS thread safe.
		 *
		 * Attributions:
		 *
		 * Author: Radovan Moncek
		 */
		void synchronize(const std::function<void()> &thread_unsafe_action);
		/**
		  *
		  */
		void log_info(const char *message);
		/**
		  *
		  */
		void log_debug(const char *message);
		/**
		  *
		  */
		void log_error(const char *message);
		/**
		  *
		  */
		void log_fatal_error(const char *message);
		/**
		  * Synopsis:
		  *
		  * Tell the internal logger to only log messages up to the level specified by nettgame_logger::level.
		  *
		  * For example, if the info level is set, debug messages will go unlogged.
		  *
		  * Attributions:
		  *
		  * author: Radovan Moncek
		  */
		void log_at_level(nettgame_logger::level level);
	};
}
