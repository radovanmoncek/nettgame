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

/**
  * Synopsis:
  *
  *
  *
  * Propositons:
  *
  * 1: Automatic type inference?
  *
  * Attributions:
  *
  * author: Radovan Moncek
  */
void multiplex_encode(const signed int type, const void *data, unsigned char *destination) {
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
    }
}

void multiplex_decode(int *type_, const unsigned char *data, void *destination) {
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
    }
}
