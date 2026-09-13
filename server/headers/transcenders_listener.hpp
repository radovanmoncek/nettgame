#include <boost/asio.hpp>

#include "transcenders_player_session.hpp"

class listener : public std::enable_shared_from_this<listener> {
    private:
	boost::asio::io_context &listener_io_context;
	boost::asio::ip::tcp::acceptor listener_acceptor;
	std::string document_root;
	nettgame::nettgame_server<game_state> &nettgame_server;

    public:
	listener(boost::asio::io_context &external_io_context, boost::asio::ip::tcp::endpoint endpoint, std::string document_root, nettgame::nettgame_server<game_state> &nettgame_server);
	void accept();
	void on_accept(boost::beast::error_code error_code, boost::asio::ip::tcp::socket socket);
};
