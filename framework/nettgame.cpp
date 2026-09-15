#include <vector>
#include <mutex>
#include <map> // rewrite framework to C - major undertaking

#include <arpa/inet.h>
#include <unistd.h>

#include "nettgame_game_session.cpp" //refactor to hpp and implementations, correct compilation inclusion
#include "headers/nettgame_internal_service_codec.h"

namespace nettgame { // remove all docs from here, leave only in .hpp
    template<class GameState>
    void nettgame_server<GameState>::make_logger_call(nettgame_logger::level level, const char *message, signed char is_master_) {
	     if (!is_master) {
		 std::lock_guard<std::mutex> log_guard(log_sync);
		 unsigned char buffer[1024-3*256]; //magic

		 memset(buffer, 0, sizeof(buffer));

		 nettgame_logger::log log;
		 log.message = (char *)message; // C style justified
		 log.message_length = 0;
		 log.is_master = is_master;

		 while (log.message[log.message_length] != '\000')
		     ++log.message_length;

		 log.message_type = level;

		 multiplex_encode(nettgame_protocol::usable::log, (void *)&log, buffer); // C style justified

		 std::lock_guard<std::mutex> internal_service_clients_guard(internal_service_sync);
		 int buffer_size = 0+4;

		 while (buffer[buffer_size] != '\000')
		     ++buffer_size;

		 for (int i = 0; i < internal_service_client_sockets_length; ++i) {
		     send(internal_service_client_sockets[i], buffer, buffer_size, 0); // improve
		 }

		 return;
	     }

	     std::lock_guard<std::mutex> log_guard(log_sync); // rewrite to C

	     switch (level) {
		 case nettgame_logger::level::info:
		     {
			 logger.log_info(message, is_master_);
		     }
		     break;
		 case nettgame_logger::level::error:
		     {
			 logger.log_error(message, is_master_);
		     }
		     break;
		 case nettgame_logger::level::fatal_error:
		     {
			 logger.log_fatal_error(message, is_master_);
		     }
		     break;
		 case nettgame_logger::level::debug:
		     {
			 logger.log_debug(message, is_master_);
		     }
		     break;
	     }
	 }

    template<class GameState>
    void nettgame_server<GameState>::make_multicast_state_call(/*const GameState*/transferable_game_state game_state) { // make multicast member function to peers
	unsigned char buffer[1024]; // magic
	//game_state.serialized_game_state = game_state.transferify(); // use Polymorphism
	/*unsigned*/ char game_state_buffer[512]; // magic

	game_state.transferify(/*game_state.serialized_game_state*/(unsigned char *)game_state_buffer); // C style cast justified
	
	game_state.serialized_game_state = game_state_buffer;

	int buffer_size = multiplex_encode(nettgame_protocol::usable::/*tran*/game_state_advertisment, /*buffer*/(void *)&game_state, buffer); // C style justified
	std::lock_guard<std::mutex> /*peers_lock*/peers_guard(internal_service_sync);

	for (int i = 0; i < internal_service_client_sockets_length; ++i) {
	    send(internal_service_client_sockets[i], buffer, buffer_size, 0); // magic
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
	    internal_service_listener = std::thread(&nettgame_server<GameState>::start_internal_service_acceptor, this, address, port);
	}
    template<class GameState>
    void nettgame_server<GameState>::register_topology_member(const char *address, short signed int port) {
	int client_socket = socket(AF_INET, SOCK_STREAM, 0);
	struct sockaddr_in client_address;
	client_address.sin_family = AF_INET;
	client_address.sin_addr.s_addr = inet_addr(address);
	client_address.sin_port = htons(port);

	if (connect(client_socket, (struct sockaddr *)&client_address, sizeof(client_address)) < 0) {
	    log_error(("failed to connect to internal_service_socket peer when registering, errno: " + std::to_string(errno)).c_str()); // use strcat? make errno mapping utility

	    return;
	}

	{
	    std::lock_guard<std::mutex> internal_service_client_sockets_guard(internal_service_sync);
	    internal_service_client_sockets[internal_service_client_sockets_length++] = client_socket;
	}

	log_info("registered a new p2p member"); // better wording required here
    };

    template<class GameState>
    void nettgame_server<GameState>::join_internal_service_network() {
	unsigned char buffer[1024-3*256]; //magic

	while(!signal_received) {
	    //std::lock_guard<std::mutex> internal_service_clients_guard(internal_service_sync); // to C, also fix

	    for (int i = 0; i < internal_service_client_sockets_length; ++i) {
		memset(buffer, 0, sizeof(buffer));

		int recv_result = recv(internal_service_client_sockets[i], buffer, sizeof(buffer), 0); //magic

		if (recv_result /*>*/<= 0)
		    return;// && buffer[0] == nettgame_protocol::usable::log) {

		switch (/*type*/buffer[0]) {
		    case nettgame_protocol::usable::log:
			{
		    nettgame_logger::log log;
		    char log_message_buffer[256]; // magic

		    memset(log_message_buffer, /*NULL*/0x0, sizeof(log_message_buffer));

		    log.message = log_message_buffer;
		    int type;

		    multiplex_decode(&type, buffer, &log);
		    log_debug("received log from peer");

		    //switch(type) {
			//case nettgame_protocol::usable::log:
			    //{
				make_logger_call(log.message_type, log.message, log.is_master);
			    }

			    break;

			case nettgame_protocol::usable::game_state_advertisment:
			    {
				log_info("game_state_advertisment received");
				int type;
				GameState game_state;

				multiplex_decode( &type, buffer, &game_state);

				// add game_state to shared_affinities, or update, if it already exists
				// if there is a new client for existing game session, and a different nettgame server was assigned to it already, recreate it
			    }

			    break;
		    //}
		}
	    }
	}
    };

    template<typename GameState>
    void nettgame_server<GameState>::start_new_game_session(std::chrono::milliseconds tick_rate, void (*perform_business_logic)(GameState*, game_session<GameState>*), GameState *state) {
	runners.emplace_back([tick_rate, perform_business_logic, state, this](){
		game_sessions.push_back(std::make_shared<game_session<GameState>>(std::this_thread::get_id(), tick_rate, perform_business_logic, static_cast<void*>(state), game_session_sync, static_cast<void*>(this)));
		game_sessions.back()->run();
	});
    }

    template<typename GameState>
    int nettgame_server<GameState>::remove_affinity(std::string address, short unsigned int port) {
	for (auto current_game_session : game_sessions) {
		if (current_game_session->has_affinity(address, port)) {
		    return current_game_session->remove_affinity(address, port);
		}
	}

	return INT_MIN;
    }

    template<typename GameState>
    void nettgame_server<GameState>::synchronize(const std::function<void()> &thread_unsafe_action) {
	std::lock_guard<std::mutex> game_session_guard(game_session_sync);

	thread_unsafe_action();
    }

    template<typename GameState>
	void nettgame_server<GameState>::log_info(const char *message) {
	    make_logger_call(nettgame_logger::level::info, message, is_master);
	}

    template<typename GameState>
	void nettgame_server<GameState>::log_debug(const char *message) {
	    make_logger_call(nettgame_logger::level::debug, message, is_master);
	}

    template<typename GameState>
	void nettgame_server<GameState>::log_error(const char *message) {
	    make_logger_call(nettgame_logger::level::error, message, is_master);
	}

    template<typename GameState>
	void nettgame_server<GameState>::log_fatal_error(const char *message) {
	    make_logger_call(nettgame_logger::level::fatal_error, message, is_master);
	}

    template<typename GameState>
	void nettgame_server<GameState>::log_at_level(nettgame_logger::level level) {
	    logger.log_at_level(level);
	}
}
