/**
  * Synopsis:
  *
  * internal_service_protocol.
  *
  * Attributions:
  *
  * author: Radovan Moncek
  */
//#include "nettgame_protocol.c"
//#include "nettgame_logger.c"
#include "headers/nettgame_internal_service_codec.h"
#include /*"headers/transferable_game_state.hpp" "transferable_game_state.cpp"*/"headers/nettgame_transferable_game_state.hpp" // make .h
//#include <string> // fix C

/**
  * Synopsis:
  *
  * Encodes data based on type.
  *
  * Propositons:
  *
  * 1: Automatic type inference?
  *
  * Attributions:
  *
  * author: Radovan Moncek
  */
/*void*/int multiplex_encode(const signed int type, const void *data, unsigned char *destination) {
    int offset = 0;

    switch (type) {
	case nettgame_protocol::usable::log:
	    {
		nettgame_logger::log log = *((nettgame_logger::log*)data);
		destination[offset++] = nettgame_protocol::usable::log;
		destination[offset++] = log.message_type;
		destination[offset++] = log.message_length;
		destination[offset++] = log.is_master;

		memcpy(&destination[offset], log.message, log.message_length);

		offset += log.message_length;
	    }

	    break;
	
	case nettgame_protocol::usable::game_state_advertisment:
	    {
		const nettgame::transferable_game_state game_state = *((nettgame::transferable_game_state*)data);
		destination[offset++] = nettgame_protocol::usable::game_state_advertisment;
		// add game session unique identifier
		memcpy(&destination[offset], /*=*/ game_state.unique_identifier, 8);
		
		offset += 8; //magic (maybe strlen)
		// add server address (maybe unique id)
		memcpy(&destination[offset] /*=*/, game_state.server_address, 4*3+3); //magic
		
		offset += 4*3+3;
		// add serializable state
		memcpy(&destination[offset], game_state.serialized_game_state.c_str(), 512); // use game_state.transferify() directly, magic

		offset += 512;
		// add clients (variadic)

		//destination[offset] = atoi();
		int client_count = game_state.clients.size();
		
		memcpy(&destination[offset], &client_count, 4); //magic

		offset += 4;

		for (int i = 0; i < game_state.clients.size(); ++i) {
		    memcpy(&destination[offset], game_state.clients[i].c_str(), 4*3+3+6); // port might be shorter

		    offset += 4*3+3+/*6*/1+5; //magic
		}
	    }

	    break;
    }

    return offset;
}

/*void*/int multiplex_decode(int *type_, const unsigned char *data, void *destination) {
    int offset = 0;
    int type = data[offset++];
    *type_ = type;

    switch (type) {
	case nettgame_protocol::usable::log:
	    {
		nettgame_logger::log *log = (nettgame_logger::log *)destination;
		log->message_type = nettgame_logger::to_level(data[offset++]);
		log->message_length = data[offset++];
		log->is_master = data[offset++];

		memcpy(log->message, &data[offset], log->message_length);

		offset += log->message_length;
	    }

	    break;

	case nettgame_protocol::usable::game_state_advertisment:
	    {
		/*const*/ nettgame::transferable_game_state *game_state = (nettgame::transferable_game_state *)destination;
		memcpy(game_state->unique_identifier, &data[offset], 8); // magic

		offset+=8;

		memcpy(game_state->server_address, &data[offset], 4*3+3); // magic

		offset += 4*3+3;

		char serialized[512];

		memcpy(/*game_state->serialized_game_state*/serialized, &data[offset], 512); // magic

		game_state->serialized_game_state = serialized; // make C
		offset += 512; // fix magic
		int client_count = 0;

		memcpy(&client_count, &data[offset], 4); // magic

		offset += 4; //magic

		for (int i = 0; i < client_count; ++i) {
		    char /***/client[4*3+3+1+5]; // magic

		    memcpy(client, &data[offset], 4*3+3+1+5); // magic

		    std::string client_string = client; // make C

		    game_state->clients.emplace_back(/*std::string(*/client_string/*)*/); // C dynamic array

		    offset += 4*3+3+1+5; //magic
		}
	    }

	    break;
    }

    return offset;
}
