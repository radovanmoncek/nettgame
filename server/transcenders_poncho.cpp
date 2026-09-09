/**
 * Synopsis:
 *
 * Formerly called speed_boost, this item gives player a slight speed increase.
 *
 * Attributions:
 *
 * author: Radovan Moncek
 */
struct poncho : entity { //name collectable, add silenced pistol?
    poncho(int assigned_unique_id, std::vector<entity*> &entities): entity(entities) {
	entity_id = protocol::usable::item;
	unique_id = assigned_unique_id;
	x = static_cast<float>(generate_random(-WORLD_WIDTH/2, WORLD_WIDTH/2));
	z = static_cast<float>(generate_random(-WORLD_WIDTH/2, WORLD_WIDTH/2));
	updated = true;
    };
    void update() override {
	for (auto current_entity : entities) {
	    if (current_entity->entity_id != protocol::usable::player)
		continue;

	    if (is_point_in_rectangle(current_entity->x, current_entity->z, x-HITBOX_SIZE, z-HITBOX_SIZE, 2*HITBOX_SIZE, 2*HITBOX_SIZE)) {
		health_points = 0;
		static_cast<player*>(current_entity)->speed+=0.005f;
		updated = true;
		auto name = static_cast<player*>(current_entity)->name + "'s poncho";
		auto parsed_db = boost::json::parse(persistence_json).as_object();
		auto spawnable_pool = boost::json::value_to<std::vector<boost::json::value>>(parsed_db["spawnable_pool"]);
		boost::json::object entry;
		entry["type"] = 1;
		entry["name"] = name;

		spawnable_pool.push_back(entry);

		parsed_db["spawanble_pool"] = boost::json::value_from(spawnable_pool);
		persistence_json = boost::json::serialize(parsed_db);

		logger.log_info(("saved new entry " + name).c_str());
	    }
	}
    };
};
