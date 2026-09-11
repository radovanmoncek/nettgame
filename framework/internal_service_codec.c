/**
  * Synopsis:
  *
  * internal_service_protocol.
  *
  * Attributions:
  *
  * author: Radovan Moncek
  */
#include "nettgame_protocol.c"
#include "nettgame_logger.c"

void multiplex_encode(const signed int type, const void *data, const signed int size, unsigned char *destination) {
    int offset = 0;

    switch (type) {
	case nettgame_protocol::usable::log:
	    {
		nettgame_logger::log log = *((nettgame_logger::log*)data);
		destination[offset++] = nettgame_protocol::usable::log;
		destination[offset++] = log.message_type;
		destination[offset++] = log.message_length;

		memcpy(&destination[offset], log.message, /*size*/log.message_length);

		offset += /*size*/log.message_length;
	    }
	    break;
    }
}

void multiplex_decode(const unsigned char *data, const signed int size, void *destination) {
    int offset = 0;
    int type = data[offset++];

    switch (type) {
	case nettgame_protocol::usable::log:
	    {
		nettgame_logger::log *log = (nettgame_logger::log *)data;
		log/*.*/->message_type = nettgame_logger::/*level::*/to_level(data[offset++]);
		log/*.*/->message_length = data[offset++];
		//((char *)destination) = data;

		memcpy(log/*.*/->message, &data[offset], log/*.*/->message_length);

		offset+=log/*.*/->message_length;

		//memcpy(destination, log, sizeof(log));

		//offset+=sizeof(log);
	    }
	    break;
    }
}
