#include "transcenders_game_state.cpp"

/**
 * 1/60.f is approx. 16ms (960)
 */
#define GAME_SESSION_TICK_RATE std::chrono::milliseconds(16)

player_session::player_session(boost::asio::ip::tcp::socket &&socket, boost::asio::ip::tcp::endpoint remote_endpoint, std::string document_root, std::vector<std::shared_ptr<player_session>> &players, nettgame::nettgame_server<game_state> &nettgame_server):
    tcp_stream(std::move(socket)),
    remote_address(remote_endpoint.address().to_string()),
    remote_port(remote_endpoint.port()),
    document_root(std::move(document_root)),
    players(players),
    nettgame_server(nettgame_server)
{
}
boost::beast::string_view player_session::extract_path_mime_type (boost::beast::string_view path) {
    auto const extension = [&] {
	auto const position = path.rfind(".");

	if (position == boost::beast::string_view::npos) {
	    return boost::beast::string_view {};
	}

	return path.substr(position);
    }();

    if (boost::beast::iequals(extension, ".html"))
	return "text/html";

    if (boost::beast::iequals(extension, ".js"))
	return "application/javascript";

    return "application/text";
}
template <class Body, class Allocator>
boost::beast::http::message_generator player_session::handle_http_request(boost::beast::string_view document_root, boost::beast::http::request<Body, boost::beast::http::basic_fields<Allocator>>&& request) {
    auto const construct_response = [&](boost::beast::string_view reason, boost::beast::http::status status){
	boost::beast::http::response<boost::beast::http::string_body> response {status, request.version()};

	response.set(boost::beast::http::field::server, BOOST_BEAST_VERSION_STRING);
	response.set(boost::beast::http::field::content_type, "text/html");
	response.keep_alive(request.keep_alive());

	response.body() = std::string(reason);

	response.prepare_payload();

	return response;
    };
    auto const cat_paths = [&](boost::beast::string_view base, boost::beast::string_view path) {
	if (base.empty())
	    return std::string(path);

	std::string result(base);
	char constexpr path_separator = '/';

	if (result.back()==path_separator)
	    result.resize(result.size()-1);

	result.append(path.data(), path.size());

	return result;
    };

    if (request.method()!=boost::beast::http::verb::get&&request.method()!=boost::beast::http::verb::head)
	return construct_response("This HTTP-method is not supported by this server", boost::beast::http::status::bad_request);

    if (request.target().empty() || request.target()[0]!='/' || request.target().find("..")!=boost::beast::string_view::npos)
	return construct_response("Illegal request-target", boost::beast::http::status::bad_request);

    std::string path = cat_paths(document_root, request.target());

    if (request.target().back() == '/') {
	boost::beast::http::response<boost::beast::http::string_body> response{boost::beast::http::status::ok, request.version()};

	response.set(boost::beast::http::field::server, std::string("Nettgame/")+=BOOST_BEAST_VERSION_STRING);
	response.set(boost::beast::http::field::content_type, "text/html");
	response.keep_alive(request.keep_alive());

	response.body() = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\"/><title>Transcenders</title></head><body><script type=\"module\" src=\"transcenders.js\"></script></body></head>";

	response.prepare_payload();

	return response;
    }

    boost::beast::error_code error_code;
    boost::beast::http::file_body::value_type body;

    body.open(path.c_str(), boost::beast::file_mode::scan, error_code);

    if (error_code==boost::system::errc::no_such_file_or_directory)
	return construct_response(request.target(), boost::beast::http::status::not_found);

    if (error_code)
	return construct_response(error_code.message(), boost::beast::http::status::internal_server_error);

    auto const size = body.size();

    if (request.method()==boost::beast::http::verb::head) {
	logger.log_debug("handling HEAD-request");

	boost::beast::http::response<boost::beast::http::empty_body> response{boost::beast::http::status::ok, request.version()};

	response.set(boost::beast::http::field::server, BOOST_BEAST_VERSION_STRING);
	response.set(boost::beast::http::field::content_type, extract_path_mime_type (path));
	response.content_length(size);
	response.keep_alive(request.keep_alive());

	return response; 
    }

    logger.log_debug("handling GET-request");

    boost::beast::http::response<boost::beast::http::file_body> response{std::piecewise_construct, std::make_tuple(std::move(body)), std::make_tuple(boost::beast::http::status::ok, request.version())};

    response.set(boost::beast::http::field::server, BOOST_BEAST_VERSION_STRING);
    response.set(boost::beast::http::field::content_type, extract_path_mime_type (path));
    response.content_length(size);
    response.keep_alive(request.keep_alive());

    return response;
}
template<class Body, class Allocator>
void player_session::perform_websocket_handshake(boost::beast::http::request<Body, boost::beast::http::basic_fields<Allocator>> request) {
    web_socket->set_option(boost::beast::websocket::stream_base::timeout::suggested(boost::beast::role_type::server));
    web_socket->set_option(boost::beast::websocket::stream_base::decorator([](boost::beast::websocket::response_type &response){
		response.set(boost::beast::http::field::server, std::string(BOOST_BEAST_VERSION_STRING)+" websocket-server-async-game");
		}));
    web_socket->binary(true);
    web_socket->async_accept(request, boost::beast::bind_front_handler(&player_session::on_accept, shared_from_this()));

    websocket_handshake_performed=true;

    nettgame_server.synchronize([&]{
	    players.push_back(shared_from_this());
	    logger.log_info("switched to WebSocket");
	    });
}
void player_session::on_accept(boost::beast::error_code error_code) {
    if (error_code) {
	logger.log_error(error_code.message().c_str());

	return;
    }

    read();
}
void player_session::read() {
    if (!websocket_handshake_performed) {
	parser.emplace();
	parser->body_limit(10000);
	tcp_stream.expires_after(std::chrono::seconds(30));
	boost::beast::http::async_read(tcp_stream, flat_buffer, parser->get(), boost::beast::bind_front_handler(&player_session::on_read, shared_from_this()));

	return;
    }

    web_socket->async_read(flat_buffer, boost::beast::bind_front_handler(&player_session::on_read, shared_from_this()));
}
void player_session::on_read(boost::beast::error_code error_code, std::size_t) {
    if (websocket_handshake_performed) {
	if (error_code == boost::beast::websocket::error::closed) {
	    logger.log_info(error_code.message().c_str());

	    nettgame_server.synchronize([&]{
		    for (auto current = players.begin(); current != players.end();) {
		    if (*current==shared_from_this()) {
		    players.erase(current);
		    nettgame_server.remove_affinity(address(), port());

		    break;
		    }

		    ++current;
		    }
		    });

	    return;
	}

	if (error_code) {
	    logger.log_error(error_code.message().c_str());

	    return;
	}

	std::shared_ptr<uint8_t[]> buffer(new uint8_t[MAX_TRANSFER_BUFFER_SIZE]);

	memcpy(buffer.get(), flat_buffer.data().data(), flat_buffer.data().size());

	uint8_t offset = 0;

	if (buffer.get()[offset]==protocol::reserved::ping_pong) {
	    uint8_t pong[MAX_TRANSFER_BUFFER_SIZE]{protocol::reserved::ping_pong};

	    write(pong, sizeof(pong));

	    nettgame_server.synchronize([&]{
		    pop_pending();
		    });
	}
	else if (buffer.get()[offset]==protocol::reserved::join_new) {
	    auto game_state_ = new game_state;
	    game_state_->game_session_id=buffer.get()[++offset];

	    nettgame_server.synchronize([&]{
		    nettgame_server.start_new_game_session(GAME_SESSION_TICK_RATE, &broadcast_do_for_all, game_state_);
	    }); 

	    uint8_t join_new_buffer[MAX_TRANSFER_BUFFER_SIZE]{protocol::reserved::join_new, buffer.get()[offset++]};//todo shared game session id pool with set

	    write(join_new_buffer, sizeof(join_new_buffer));

	    nettgame_server.synchronize([&]{
		    pop_pending();
		    }); 
	}
	else {
	    nettgame_server.synchronize([&]{
		    pending.push(buffer);
		    });
	}

	flat_buffer.consume(flat_buffer.data().size());
	read();

	return;
    }

    if (error_code == boost::beast::http::error::end_of_stream){
	tcp_stream.socket().shutdown(boost::asio::ip::tcp::socket::shutdown_send, error_code);

	return;
    }

    if (error_code) {
	logger.log_error(error_code.message().c_str());

	return;
    }

    if (boost::beast::websocket::is_upgrade(parser->get())) {
	web_socket.emplace(std::move(tcp_stream.release_socket()));
	perform_websocket_handshake(parser->release());

	return;
    }

    boost::beast::http::message_generator message = handle_http_request(document_root, parser->release());
    bool keep_alive = message.keep_alive();
    auto self = shared_from_this();

    boost::beast::async_write(tcp_stream, std::move(message), [self, keep_alive](boost::beast::error_code error_code, std::size_t bytes){
	    if (error_code) {
	    logger.log_error(error_code.message().c_str());

	    return;
	    }

	    if (!keep_alive) {
	    self->tcp_stream.socket().shutdown(boost::asio::ip::tcp::socket::shutdown_send, error_code);

	    return;
	    }

	    self->read();
	    });
}
void player_session::write(uint8_t *buffer, size_t length) {
    boost::beast::error_code error_code;

    web_socket->write(boost::asio::buffer(buffer, length));

    if (error_code) {
	logger.log_error(error_code.message().c_str());
    }
}
void player_session::on_write(boost::beast::error_code beast_error_code, std::size_t bytes_transfered){
    boost::ignore_unused(bytes_transfered);

    if (beast_error_code) {
	logger.log_error(beast_error_code.what().c_str());

	return;
    }
}
bool player_session::is_open() {
    return websocket_handshake_performed&&web_socket->is_open();
}
void player_session::pop_pending() {
    if (empty_pending())
	return;

    pending.pop();
}
bool player_session::front_pending(unsigned char *buffer, int size) {
    if (empty_pending())
	return false;

    memcpy(buffer, pending.front().get(), size);

    return true;
}
bool player_session::empty_pending() {
    return pending.empty();
}
std::string player_session::address() {
    return remote_address;
}
short signed int player_session::port(){
    return remote_port;
}
