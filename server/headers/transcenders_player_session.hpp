#pragma once

#include <queue>

#include <boost/beast.hpp>

#include "../../framework/headers/nettgame.hpp"
//#include "transcenders_player_container.hpp" fix build.sh

struct game_state;

class player_session : public std::enable_shared_from_this<player_session> {
    private:
	boost::beast::tcp_stream tcp_stream;
	boost::optional<boost::beast::websocket::stream<boost::beast::tcp_stream>> web_socket;
	boost::beast::flat_buffer flat_buffer;
	std::string remote_address;
	short unsigned int remote_port;
	boost::optional<boost::beast::http::request_parser<boost::beast::http::string_body>> parser;
	std::queue<std::shared_ptr<unsigned char[]>> pending;
	bool websocket_handshake_performed=false;
	std::string document_root;
	std::vector<std::shared_ptr<player_session>> &players;
	nettgame::nettgame_server<game_state> &nettgame_server;

    public:
	player_session(boost::asio::ip::tcp::socket &&socket, boost::asio::ip::tcp::endpoint remote_endpoint, std::string document_root, std::vector<std::shared_ptr<player_session>> &players, nettgame::nettgame_server<game_state> &nettgame_server);
	boost::beast::string_view extract_path_mime_type (boost::beast::string_view path);
	template <class Body, class Allocator>
	boost::beast::http::message_generator handle_http_request(boost::beast::string_view document_root, boost::beast::http::request<Body, boost::beast::http::basic_fields<Allocator>>&& request);
	template<class Body, class Allocator>
	void perform_websocket_handshake(boost::beast::http::request<Body, boost::beast::http::basic_fields<Allocator>> request);
	void on_accept(boost::beast::error_code error_code);
	void read();
	void on_read(boost::beast::error_code error_code, std::size_t);
	void write(uint8_t *buffer, size_t length);
	void on_write(boost::beast::error_code beast_error_code, std::size_t bytes_transfered);
	bool is_open();
	void pop_pending();
	bool front_pending(unsigned char *buffer, int size);
	bool empty_pending();
	std::string address();
	short signed int port();
};

#include "../nettgame_game_player_session.cpp" // fix
