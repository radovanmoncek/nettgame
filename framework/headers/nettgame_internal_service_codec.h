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

/**
  * Synopsis:
  *
  * Multiplex codec/encoder for encoding internal service messages sent over the p2p network.
  *
  * Propositons:
  *
  * 1: Automatic type inference?
  *
  * Attributions:
  *
  * author: Radovan Moncek
  */
void multiplex_encode(const signed int type, const void *data, unsigned char *destination);

/**
 * Synopsis:
 * 
 * Multiplex codec/decoder for decoding internal service messages sent over the p2p network.
 *
 * Attributions:
 *
 * author: Radovan Moncek
 */
void multiplex_decode(int *type_, const unsigned char *data, void *destination);
