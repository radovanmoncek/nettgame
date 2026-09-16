#include <string>
#include <vector>

namespace nettgame {
    /**
     * Synopsis:
     *
     * transferable_game_state must be derived by a type/class that is to be transfered to other Nettgame containers, when load-balancing/fail-over occurs.
     *
     * Description:
     *
     * This is, basically, a "serialization contract/dictating interface".
     *
     * Attributions:
     *
     * author: Radovan Moncek
     */
    struct transferable_game_state {
	char unique_identifier_[/*8*/9]; // magic
	char server_address[4*3+3+1] = "0.0.0.0\000"; //magic
	std::string serialized_game_state; // make C
	std::vector<std::string> clients; // make C, C dynamic array - structure address:port (socket - transport protocol)

	/**
	 * Synopsis:
	 *
	 * This member function is called when the game_session content must be transfered to other Nettgame containers.
	 *
	 * Description:
	 *
	 * The return of this member function will be sent to other Nettgame containers. It is entirely the implementors responsibility to include all information in the give game_session, any data, that is left out, will be ommited from the stateful update.
	 *
	 * Attributions:
	 *
	 * author: Radovan Moncek
	 */
	virtual void transferify(unsigned char *buffer) {}; //find out, why this virtual member function must have a body, and the program won't link otherwise.
	/**
	  * Synopsis:
	  *
	  * Deserialize game_state data into a new working game_session.
	  *
	  * Attributions:
	  *
	  * author: Radovan Moncek
	  */
	virtual void detransferify(std::string serialized_game_state_) {};
    };
}
