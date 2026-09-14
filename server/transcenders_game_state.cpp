//#include "nettgame_game_player_session.hpp"
//#include "headers/transcenders_player_session.hpp"
#include "transcenders_protocol.c"
#include "transcenders_entity.cpp"
#include "transcenders_player.cpp"
#include "transcenders_enemy_threat.cpp"
#include "transcenders_poncho.cpp"
#include "transcenders_configuration.cpp"
#include "transcenders_geometry.cpp"
#include "transcenders_player_container.cpp"

struct game_state : nettgame::transferable_game_state {
    int game_session_id, player_count=0, ticks_until_exit=60;
    std::vector<entity*> entities;
    std::set<uint8_t> unique_id_pool;
    bool initializing = true;

    game_state() {
	for (uint8_t i = 1; i < 255; ++i)
	    unique_id_pool.insert(i);
    };
    void transferify(uint8_t *transfer_buffer) override {
	boost::json::object to_transfer;
	to_transfer["game_session_id"]=game_session_id;
	to_transfer["player_count"]=player_count;
	to_transfer["ticks_until_exit"]=ticks_until_exit;
	auto serialized = boost::json::serialize(to_transfer);
    };
};
