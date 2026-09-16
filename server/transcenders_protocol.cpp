struct protocol {
    /**
      * [0x1, 0x8]
      */
    struct reserved {
	static const uint8_t ping_pong = 0x1;
	static const uint8_t join_new = 0x2;
	static const uint8_t join_existing = 0x3;
	static const uint8_t error = 0x4;
	static const uint8_t delete_request = 0x5;
    };

    /**
      * [0x9, 0x20]
      */
    struct usable {
	static const uint8_t player = 0x9;
	static const uint8_t enemy_threat = 0xA;
	static const uint8_t weapon = 0xB;
	static const uint8_t item = 0xC;
    };
};
