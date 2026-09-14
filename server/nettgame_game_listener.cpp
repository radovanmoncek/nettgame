#include <boost/asio.hpp>

//#include "headers/transcenders_player_session.hpp"
//#include "headers/transcenders_listener.hpp"

//class listener : public std::enable_shared_from_this<listener> {
    /*private:
	boost::asio::io_context &listener_io_context;
	boost::asio::ip::tcp::acceptor listener_acceptor;
	std::string document_root;
	nettgame::nettgame_server<game_state> &nettgame_server;*/

    //public:
	listener::listener(boost::asio::io_context &external_io_context, boost::asio::ip::tcp::endpoint endpoint, std::string document_root, nettgame::nettgame_server<game_state> &nettgame_server): 
	    listener_io_context(external_io_context), 
	    listener_acceptor(external_io_context),
	    document_root(document_root),
	    nettgame_server(nettgame_server)
    {
	boost::beast::error_code beast_error_code;

	listener_acceptor.open(endpoint.protocol(), beast_error_code);

	if (beast_error_code) {
	    nettgame_server.log_error(beast_error_code.what().c_str());

	    return;
	}

	listener_acceptor.set_option(boost::asio::socket_base::reuse_address(true), beast_error_code);

	if (beast_error_code) {
	    nettgame_server.log_error(beast_error_code.what().c_str());

	    return;
	}

	listener_acceptor.bind(endpoint, beast_error_code);

	if (beast_error_code) {
	    nettgame_server.log_error(beast_error_code.what().c_str());

	    return;
	}

	listener_acceptor.listen(boost::asio::socket_base::max_listen_connections, beast_error_code);

	if (beast_error_code) {
	    nettgame_server.log_error(beast_error_code.what().c_str());

	    return;
	}
    }
	void listener::accept() {
	    listener_acceptor.async_accept(boost::asio::make_strand(listener_io_context), boost::beast::bind_front_handler(&listener::on_accept, shared_from_this()));
	}
	void listener::on_accept(boost::beast::error_code error_code, boost::asio::ip::tcp::socket socket) {
	    boost::system::error_code error_code_;

	    auto remote_endpoint = socket.remote_endpoint(error_code_);

	    if (error_code_) {
		nettgame_server.log_error(error_code.message().c_str());

		return;
	    }

	    if (error_code){
		nettgame_server.log_error(error_code.message().c_str());

		return;
	    }
	    else {
		auto joined_player = std::make_shared<player_session>(std::move(socket), socket.remote_endpoint(), document_root, players, nettgame_server);

		joined_player->read();

		std::string log = "handling new connection " + remote_endpoint.address().to_string() + ":" + std::to_string(remote_endpoint.port());

		nettgame_server.log_debug(log.c_str());
	    }

	    accept();
	}
//};
