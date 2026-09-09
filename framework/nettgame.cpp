#pragma once

#include <vector>
#include <mutex>
#include <map>

#include "nettgame_game_session.cpp"

namespace nettgame {
    /**
     * Synopsis:
     *
     * nettgame_server is a "server" that provisions emphemeral game_session instances via threads, and is not build upon any preexisting library/framework, only the standard library of the C++ language standard (std).
     *
     * Example:
     *
     * #include <nettgame.cpp>
     *
     * int main() {
     *     nettgame::nettgame_server<YourStateSerializableType> nettgame_server("127.0.0.1", 4321);
     *
     *     nettgame_server.start_new_game_session(std::chrono::milliseconds(16), your_lambda_closure_with_business_logic, YourStateSerializable);
     * }
     *
     * End of example.
     *
     * Longer description:
     *
     * The ip, and port passed to the constructor are of pure logical sentiment, and do not, in any way, bind, as in not performing the bind action of the operating system, this "server" to them.
     *
     * This server in meant to run inside the environment of K8s/K3s.
     *
     * Responsibilities of this nettgame_server:
     *
     * - provisioning of game_session instances via runner threads,
     * - handling thread unsafe operations within runner threads,
     * - automatic failover,
     * - load balancing,
     * - transfering game_session stateful information to other replicas without information loss,
     * -  and management of stateful information within game_session instances.
     *
     * Responsibilities of the implementor:
     *
     * - business logic,
     * - calling the synchronize member functions when performing thread unsafe work within game sessions/runner threads,
     * - transport layer protocol provisioning via TCP/UDP/KCP/RUDP, etc. (player session containers, listeners, etc.),
     * -  and application layer protocol provisioning (data transfer, codecs, etc.).
     *
     * Attributions (you have forever my biggest gratitude):
     *
     * - Tsoding,
     * -  and Yegor256.
     */
    template<typename GameState>
	 class nettgame_server {
	     private:
		 nettgame_logger logger;
		 std::vector<std::thread> runners;
		 std::vector<std::shared_ptr<game_session<GameState>>> game_sessions;
		 std::mutex game_session_sync;
		 std::map<std::tuple<std::string, short signed int>, std::vector<std::string>> shared_affinities;

	     public:
		 nettgame_server(std::string address, short signed int port) {
		     //todo
		 }
		 /**
		  * Synopsis:
		  *
		  * Starts a new game session by creating a runner std::thread/inferior process, and a new game_session class instance.
		  *
		  * Description:
		  *
		  * TBD.
		  *
		  * I/O:
		  *
		  * Should not block longer than the duration required for creating an instance of the runner std::thread/inferior process, therefore, this member function should not block.
		  * 
		  * Thread safety:
		  *
		  * IS NOT thread safe.
		  * 
		  * Attributions:
		  *
		  * Author: Radovan Moncek
		  */
		 void start_new_game_session(std::chrono::milliseconds tick_rate, void (*perform_business_logic)(GameState*, game_session<GameState>*), GameState *state) {
		     runners.emplace_back([tick_rate, perform_business_logic, state, this](){
			     game_sessions.push_back(std::make_shared<game_session<GameState>>(std::this_thread::get_id(), tick_rate, perform_business_logic, static_cast<void*>(state), game_session_sync, logger));
			     game_sessions.back()->run();
			     });
		 }
		 /**
		  * Synopsis:
		  *
		  * Global version of the remove_affinity member function.
		  * 
		  * I/O:
		  *
		  * Blocks, for the duration of linear O(n) for cycle execution, best case scenario is constant O(1).
		  *
		  * Thread safety:
		  *
		  * IS NOT thread safe.
		  * 
		  * Attributions:
		  *
		  * Author: Radovan Moncek
		  */
		 int remove_affinity(std::string address, short unsigned int port) {
		     for (auto current_game_session : game_sessions) {
			 if (current_game_session->has_affinity(address, port)) {
			     return current_game_session->remove_affinity(address, port);
			 }
		     }

		     return INT_MIN;
		 }
		 /**
		  * Synopsis:
		  *
		  * Global version of the synchronize member function.
		  *
		  * Description:
		  *
		  * Acquires an std::mutex game_session_sync, and performs the thread_unsafe_action in the synchronized scope.
		  *
		  * I/O:
		  *
		  * Blocks, until the thread_unsafe_sync finishes its execution.
		  *
		  * Thread safety:
		  *
		  * IS thread safe.
		  *
		  * Attributions:
		  *
		  * Author: Radovan Moncek
		  */
		 void synchronize(const std::function<void()> &thread_unsafe_action) {
		     std::lock_guard<std::mutex> game_session_guard(game_session_sync);

		     thread_unsafe_action();
		 }
	 };
}
