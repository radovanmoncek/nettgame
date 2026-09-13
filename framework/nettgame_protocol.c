struct nettgame_protocol {
    /**
     * Synopsis:
     *
     * Control part of the nettgame internal protocol.
     *
     * Description:
     *
     * Range is [0x1, 0x8].
     *
     * Attributions:
     * 
     * author: Radovan Moncek
     */
    struct reserved {
    };

    /**
     * Synopsis:
     *
     * Logic part of the nettgame internal protocol.
     *
     * Description:
     *
     * Range is (0x8, 0x16].
     *
     * Attributions:
     * 
     * author: Radovan Moncek
     */
    struct usable {
	/**
	  * Synopsis:
	  *
	  * Structure of the protocol message is to be as follows: 0x9|level|message_length|message.
	  *
	  * Propositions:
	  *
	  * 2: codec result is log struct containing all parts.
	  *
	  * 1: make into struct.
	  */
	static const signed int log = 0x9;
    };
};
