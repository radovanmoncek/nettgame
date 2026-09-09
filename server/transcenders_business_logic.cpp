/**
 * Synopsis:
 *
 * Performs business logic for each tick.
 *
 * Attributions:
 *
 * source: https://stackoverflow.com/questions/5585532
 *
 * author: Radovan Moncek
 */
void broadcast_do_for_all(game_state *state_, nettgame::game_session<game_state> *game_session){
    auto convert_bytes_to_float32 = [](unsigned char *bytes, int offset){
	float result=0.f;

	memcpy(&result, &bytes[offset], sizeof(float));

	return result;
    };
    auto convert_float32_to_bytes = [](float input, unsigned char *bytes, int offset){
	memcpy(&bytes[offset],&input,sizeof(input));
    };
    auto multiplex_encode = [&](uint8_t protocol_id, void *content, uint8_t *buffer){
	uint8_t offset = 0;

	switch (protocol_id) {
	    case protocol::reserved::join_new:
		{
		    buffer[offset++]=protocol::reserved::join_new;
		    buffer[offset++]=*static_cast<uint8_t*>(content);
		}
		break;
	    case protocol::reserved::join_existing:
		{
		    buffer[offset++]=protocol::reserved::join_existing;
		    buffer[offset++]=*static_cast<uint8_t*>(content);
		}
		break;
	    case protocol::reserved::delete_request:
		{
		    buffer[offset++]=protocol::reserved::delete_request;
		    buffer[offset++]=*static_cast<uint8_t*>(content);
		}
		break;
	    case protocol::usable::player:
		{
		    auto player_entity = *static_cast<player*>(content);
		    buffer[offset++]=protocol::usable::player;
		    buffer[offset++]=player_entity.unique_id;

		    convert_float32_to_bytes(player_entity.x, buffer, offset);

		    offset+=4;

		    convert_float32_to_bytes(player_entity.y, buffer, offset);

		    offset+=4;

		    convert_float32_to_bytes(player_entity.z, buffer, offset);

		    offset+=4;

		    convert_float32_to_bytes(player_entity.azimuth, buffer, offset);

		    offset+=4;

		    memcpy(&buffer[offset], player_entity.name.c_str(), MAX_PLAYER_NAME_LENGTH);

		    offset+=MAX_PLAYER_NAME_LENGTH;
		}
		break;
	    case protocol::usable::enemy_threat:
		{
		    auto enemy = *static_cast<enemy_threat*>(content);
		    buffer[offset++]=protocol::usable::enemy_threat;
		    buffer[offset++]=enemy.unique_id;

		    convert_float32_to_bytes(enemy.x, buffer, offset);

		    offset+=4;

		    convert_float32_to_bytes(enemy.y, buffer, offset);

		    offset+=4;

		    convert_float32_to_bytes(enemy.z, buffer, offset);

		    offset+=4;

		    convert_float32_to_bytes(enemy.azimuth, buffer, offset);

		    offset+=4;
		}
		break;
	    case protocol::usable::item:
		{
		    auto poncho_ = *static_cast<poncho*>(content);
		    buffer[offset++]=protocol::usable::item;
		    buffer[offset++]=poncho_.unique_id;

		    convert_float32_to_bytes(poncho_.x, buffer, offset);

		    offset+=4;

		    convert_float32_to_bytes(poncho_.y, buffer, offset);

		    offset+=4;

		    convert_float32_to_bytes(poncho_.z, buffer, offset);

		    offset+=4;
		}
		break;
	}
    };
    auto multiplex_decode = [&](uint8_t *buffer, void *result_content){
	uint8_t offset = 0;
	auto protocol_id = buffer[offset++];

	switch (protocol_id) {
	    case protocol::reserved::join_new:
		{
		    *static_cast<uint8_t*>(result_content) = buffer[offset++];
		}
		break;
	    case protocol::reserved::join_existing:
		{
		    *static_cast<uint8_t*>(result_content) = buffer[offset++];
		}
		break;
	    case protocol::usable::player:
		{
		    auto player_entity = static_cast<player*>(result_content);

		    player_entity->unique_id=buffer[offset++];
		    player_entity->x=convert_bytes_to_float32(buffer, offset);
		    offset+=4;
		    player_entity->y=convert_bytes_to_float32(buffer, offset);
		    offset+=4;
		    player_entity->z=convert_bytes_to_float32(buffer, offset);
		    offset+=4;
		    player_entity->azimuth=convert_bytes_to_float32(buffer, offset);
		    offset+=4;
		    player_entity->firing=buffer[offset++];
		    char name[MAX_PLAYER_NAME_LENGTH];

		    memcpy(name, &buffer[offset], MAX_PLAYER_NAME_LENGTH);

		    player_entity->name=name;
		}
		break;
	}
    };
    auto assign_unique_id = [&]{
	uint8_t unique_id=*state_->unique_id_pool.begin();

	state_->unique_id_pool.erase(unique_id);

	return unique_id;
    };

    if (state_->ticks_until_exit == 0) {
	for (auto entity : state_->entities) {
	    if (entity->entity_id == protocol::usable::player)
		delete static_cast<player*>(entity);

	    if (entity->entity_id == protocol::usable::enemy_threat)
		delete static_cast<enemy_threat*>(entity);
	}

	game_session->game_session_code = nettgame::game_session<game_state>::game_session_code::exit;
    }

    if (!state_->player_count||!game_session->affinities_count())
	--state_->ticks_until_exit;

    game_session->synchronize([&]{
	    uint8_t buffer[MAX_TRANSFER_BUFFER_SIZE];

	    for (auto &current_player : players) {
	    uint8_t offset = 0;

	    if (!current_player->is_open()) {
	    if (!game_session->has_affinity(current_player->address(), current_player->port()))
	    continue;

	    logger.log_info("player has disconnected, and their resources are beign deallocated.");

	    int unique_id = game_session->remove_affinity(current_player->address(), current_player->port());

	    state_->unique_id_pool.insert(unique_id);

	    for (auto current = state_->entities.begin(); current!=state_->entities.end();) {
	    if ((*current)->unique_id==unique_id) {
	    auto to_delete = static_cast<player*>(*current);

	    state_->entities.erase(current);

	    delete to_delete;
	    }
	    else
		++current;
	    }

	    --state_->player_count;

	    continue;
	    }

	    if (current_player->front_pending(buffer, sizeof(buffer)) && buffer[offset]==protocol::reserved::join_existing) {
		if(buffer[++offset]!=state_->game_session_id) {
		    continue;
		}

		if (state_->player_count > MAX_PLAYERS)
		    continue;

		if (game_session->has_affinity(current_player->address(), current_player->port())) {
		    logger.log_info("player just reconnected");

		    auto affinity_arbitrary = game_session->retrieve_affinity(current_player->address(), current_player->port());

		    multiplex_encode(protocol::reserved::join_existing, &affinity_arbitrary, buffer);
		    current_player->write(buffer, sizeof(buffer));

		    continue;
		}

		uint8_t unique_id = assign_unique_id();

		game_session->assign_affinity_to_slot(current_player->address(), current_player->port(), unique_id);
		multiplex_encode(protocol::reserved::join_existing, &unique_id, buffer);
		state_->entities.push_back(new player(unique_id, state_->entities));
		current_player->write(buffer, sizeof(buffer));

		++state_->player_count;

		current_player->pop_pending();

		std::string log = "player joined, current player count is "+std::to_string(state_->player_count);

		logger.log_info(log.c_str());

		if (!state_->initializing)
		    continue;

		logger.log_info("initializing game session");
		logger.log_info("step 1: adding enemies");

		for (auto i = 0; i < MAX_THREATHS; ++i) {
		    state_->entities.push_back(new enemy_threat(assign_unique_id(), state_->entities));
		}

		logger.log_info("step 1 done: enemies added");
		logger.log_info("step 2: adding items/weapons");

		auto parsed_db = boost::json::parse(persistence_json).as_object();
		auto spawnable_pool = boost::json::value_to<std::vector<boost::json::value>>(parsed_db["spawnable_pool"]);

		for (auto i = 0; i < MAX_SPAWNED_ITEMS_WEAPONS; ++i) {
		    auto random_entry = spawnable_pool[generate_random(0, 3)%MAX_SPAWNED_ITEMS_WEAPONS].as_object();

		    switch (boost::json::value_to<int>(random_entry["type"])) {
			case 1:
			    {
				state_->entities.push_back(new poncho(assign_unique_id(), state_->entities));
			    }
			    break;
		    }
		}

		logger.log_info("step 2 done: items/weapons added");

		state_->initializing=false;
	    }

	    if (!game_session->has_affinity(current_player->address(), current_player->port()))
		continue;

	    if (current_player->front_pending(buffer, sizeof(buffer))) {
		if (buffer[offset++] == protocol::usable::player) {
		    player decoded(0, state_->entities);

		    multiplex_decode(buffer, &decoded);

		    for (auto current_entity : state_->entities) {
			if (current_entity->unique_id==decoded.unique_id)
			    static_cast<player*>(current_entity)->update(&decoded);
		    }
		}

		current_player->pop_pending();
	    }
	    }

	    for (auto current_entity = state_->entities.begin(); current_entity != state_->entities.end();) {
		if ((*current_entity)->entity_id == protocol::usable::player)
		    static_cast<player*>(*current_entity)->update();

		if ((*current_entity)->entity_id == protocol::usable::enemy_threat)
		    static_cast<enemy_threat*>(*current_entity)->update();

		if ((*current_entity)->entity_id == protocol::usable::item)
		    static_cast<poncho*>(*current_entity)->update();

		if (!(*current_entity)->updated||(*current_entity)->deleted) {
		    ++current_entity;

		    continue;
		}

		if ((*current_entity)->health_points<=0) {
		    logger.log_info("entity just died");

		    (*current_entity)->deleted=true;
		}

		for (auto &current_player : players) {
		    if (!current_player->is_open()||!game_session->has_affinity(current_player->address(), current_player->port()))
			continue;

		    if ((*current_entity)->deleted) {
			multiplex_encode(protocol::reserved::delete_request, static_cast<void*>(&(*current_entity)->unique_id), buffer);
			current_player->write(buffer, sizeof(buffer));

			continue;
		    }

		    multiplex_encode((*current_entity)->entity_id, static_cast<void*>(*current_entity), buffer);
		    current_player->write(buffer, sizeof(buffer));
		}

		if ((*current_entity)->deleted) {
		    state_->entities.erase(current_entity);

		    continue;
		}

		(*current_entity)->updated=false;
		++current_entity;
	    }
    });
}
