#include <vector>
#include <mutex>
#include <map> // rewrite framework to C - major undertaking
#include <arpa/inet.h>
#include <unistd.h>

#include "nettgame_game_session.cpp" //refactor to hpp and implementations, correct compilation inclusion
#include "internal_service_codec.c"

namespace nettgame {
    /**
     * Synopsis:
     *
     *
     *
     * Description:
     *
     * Attributions:
     *
     * author: Radovan Moncek
     *
     */
    template<class GameState>
	 void nettgame_server<GameState>::make_logger_call(nettgame_logger::level level, const char *message) {
	     if (!is_master) {
		 unsigned char buffer[1024]; //magic constant
		 nettgame_logger::log log;
		 log.message = (char *)message;
		 log.message_length = 128; //fix
		 log.message_type = level;

		 multiplex_encode(nettgame_protocol::usable::log, (void *)&log, sizeof(buffer), buffer);

		 std::lock_guard<std::mutex> internal_service_clients_guard(internal_service_sync);

		 for (int i = 0; i < internal_service_client_sockets_length; ++i) {
		     send(internal_service_client_sockets[i], buffer, sizeof(buffer), 0);
		 }

		 return;
	     }

	     switch (level) {
		 case nettgame_logger::level::info:
		     {
			 logger.log_info(message);
		     }
		     break;
		 case nettgame_logger::level::error:
		     {
			 logger.log_error(message);
		     }
		     break;
		 case nettgame_logger::level::fatal_error:
		     {
			 logger.log_fatal_error(message);
		     }
		     break;
		 case nettgame_logger::level::debug:
		     {
			 logger.log_debug(message);
		     }
		     break;
	     }
	 }

    template<class GameState>
    void nettgame_server<GameState>::handle_signals() {
	signal_received = true;
    }

    template<class GameState>
	nettgame_server<GameState>::nettgame_server(const char *address, short signed int port, bool is_master):
	    is_master(is_master)
    {
	    //add entry to shared_affinities?
	    logger.is_master = is_master; 
	    internal_service_listener = std::thread(&nettgame_server<GameState>::start_internal_service_acceptor, this, port);
	}
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
    template<class GameState>
    void nettgame_server<GameState>::register_topology_member(const char *address, short signed int port) {
	//add socket from address and port to internal_service_client_sockets
	struct sockaddr_in client_address;
	client_address.sin_family = AF_INET;
	client_address.sin_addr.s_addr = inet_addr(address);
	client_address.sin_port = htons(port);

	if (connect(internal_service_socket, (struct sockaddr *)&client_address, sizeof(client_address)) < 0) {
	    log_error("failed to connect to internal_service_socket peer");

	    return;
	}
	//++internal_service_client_sockets_length;
    };
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
     * IS thread safe.
     * 
     * Attributions:
     *
     * author: Radovan Moncek
     */
    template<class GameState>
    void nettgame_server<GameState>::join_internal_service_network() {
	unsigned char buffer[1024]; //magic constant

	while(!signal_received) {
	    //memset(buffer, 0, 1024);

	    for (int i = 0; i < internal_service_client_sockets_length; ++i) {
		memset(buffer, 0, sizeof(buffer));
		recv(internal_service_client_sockets[i], buffer, 1024, 0); //magic constant

		if (buffer[0] == nettgame_protocol::usable::log) {
		    //char log_message[buffer[1]];//move to codec, or null terminate \0?
		    nettgame_logger::log log;

		    multiplex_decode(buffer, sizeof(buffer), /*log_message*/&log);
		    //make_logger_call(buffer[2], log_message);
		    make_logger_call(log.message_type, log.message);
		}
	    }
	}
    };
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
    template<typename GameState>
	void nettgame_server<GameState>::start_new_game_session(std::chrono::milliseconds tick_rate, void (*perform_business_logic)(GameState*, game_session<GameState>*), GameState *state) {
	    runners.emplace_back([tick_rate, perform_business_logic, state, this](){
		    game_sessions.push_back(std::make_shared<game_session<GameState>>(std::this_thread::get_id(), tick_rate, perform_business_logic, static_cast<void*>(state), game_session_sync, static_cast<void*>(this)));
		    game_sessions.back()->run();
		    });
	}
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
    template<typename GameState>
	int nettgame_server<GameState>::remove_affinity(std::string address, short unsigned int port) {
	    for (auto current_game_session : game_sessions) {
		if (current_game_session->has_affinity(address, port)) {
		    return current_game_session->remove_affinity(address, port);
		}
	    }

	    return INT_MIN;
	}
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
     * Blocks, until the thread_unsafe_sync finishes its execution.
     *
     * Thread safety:
     *
     * IS thread safe.
     *
     * Attributions:
     *
     * Author: Radovan Moncek
     */
    template<typename GameState>
	void nettgame_server<GameState>::synchronize(const std::function<void()> &thread_unsafe_action) {
	    std::lock_guard<std::mutex> game_session_guard(game_session_sync);

	    thread_unsafe_action();
	}
    template<typename GameState>
	void nettgame_server<GameState>::log_info(const char *message) {
	    make_logger_call(nettgame_logger::level::info, message);
	}
    template<typename GameState>
	void nettgame_server<GameState>::log_debug(const char *message) {
	    make_logger_call(nettgame_logger::level::debug, message);
	}
    template<typename GameState>
	void nettgame_server<GameState>::log_error(const char *message) {
	    make_logger_call(nettgame_logger::level::error, message);
	}
    template<typename GameState>
	void nettgame_server<GameState>::log_fatal_error(const char *message) {
	    make_logger_call(nettgame_logger::level::fatal_error, message);
	}
    template<typename GameState>
	void nettgame_server<GameState>::log_at_level(nettgame_logger::level level) {
	    logger.log_at_level(level);
	}
}
