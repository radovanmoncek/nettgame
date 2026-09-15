#include <set>

#include <boost/config.hpp>

#include "headers/transcenders_configuration.hpp"
#include "headers/transcenders_listener.hpp"
//#include "../framework/headers/nettgame.hpp" // use this fix
#include "../framework/nettgame.cpp" // fix

/**
 * Synopsis:
 *
 * Main entrypoint into the Transcenders game server build upon Nettgame, serving as a proof of concept.
 *
 * Attributions:
 *
 * source: StackOverflow Vinnie Falco
 *
 * author: Radovan Moncek
 */
int main(int argc, char **argv) {
    uint8_t offset = 1;
    int threads = std::max<int>(MIN_ASIO_THREADS, atoi(argv[offset++]));
    std::string document_root = argv[offset++];
    auto address_raw = argv[offset++];
    boost::asio::ip::port_type port = static_cast<boost::asio::ip::port_type>(atoi(argv[offset++]));
    boost::asio::io_context io_context{threads};
    boost::asio::ip::address address = boost::asio::ip::make_address(address_raw);
    bool is_master = atoi(argv[offset++]);
    nettgame::nettgame_server<game_state> nettgame_server(address_raw, port + 1, is_master); //maybe custom load balancer?

    try {
	if (argc < MIN_ARGV)
	    return EXIT_FAILURE;

	std::make_shared<listener>(io_context, boost::asio::ip::tcp::endpoint{address, port}, document_root, nettgame_server)->accept();

	boost::asio::signal_set signals(io_context, SIGINT, SIGTERM);

	signals.async_wait([&](boost::system::error_code const&, int){
		io_context.stop();
		nettgame_server.handle_signals();
		});

	std::vector<std::thread> thread_pool;

	thread_pool.reserve(threads-1);
	nettgame_server.log_at_level(nettgame_logger::level::info);

	for (auto thread_i = threads; thread_i > 0; --thread_i){
	    thread_pool.emplace_back([&io_context]{
		    io_context.run();
		    });
	}

	for (;offset < argc;) {
	    nettgame_server.register_topology_member(argv[offset++], port + 1);
	}

	nettgame_server.join_internal_service_network();

	for (auto &thread : thread_pool)
	    thread.join();

	return EXIT_SUCCESS;
    }
    catch(std::exception const &caught){
	nettgame_server.log_fatal_error(caught.what());

	return EXIT_FAILURE;
    }
}
