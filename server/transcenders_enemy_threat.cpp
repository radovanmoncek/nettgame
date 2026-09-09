struct enemy_threat : entity {
    int idle_ticks = THREAT_IDLE_TICKS, movement_duration = 0, random_axis = 0;
    float random_movement;

    enemy_threat(int assigned_unique_id, std::vector<entity*> &entities): entity(entities) {
	entity_id = protocol::usable::enemy_threat;
	unique_id = assigned_unique_id;
	x = static_cast<float>(generate_random(-WORLD_WIDTH/2, WORLD_WIDTH/2));
	z = static_cast<float>(generate_random(-WORLD_WIDTH/2, WORLD_WIDTH/2));

	update();
    };
    void update() override {
	++idle_ticks;
	idle_ticks%=THREAT_IDLE_TICKS;
	++movement_duration;
	movement_duration%=THREAT_MOVEMENT_DURATION;

	if (movement_duration == THREAT_MOVEMENT_DURATION-1){
	    random_axis = generate_random(1, 2);
	    random_movement = generate_random(1, 2) == 1? 0.01:-0.01;
	}

	if (idle_ticks != THREAT_IDLE_TICKS-1)
	    return;

	for (auto current_entity : entities) {
	    if (current_entity->entity_id != protocol::usable::player)
		continue;

	    if (is_point_in_rectangle(current_entity->x, current_entity->z, x-HITBOX_SIZE, z-HITBOX_SIZE, 2*HITBOX_SIZE, 2*HITBOX_SIZE)) {
		if (current_entity->x!=x && current_entity->z!=z) {
		    x+=(current_entity->x-x)*THREAT_SPEED;
		    z+=(current_entity->z-z)*THREAT_SPEED;
		    updated = true;
		}

		return;
	    }
	}

	if (random_axis == 1)
	    x+=random_movement;
	else
	    z+=random_movement;

	if (x > WORLD_WIDTH/2)
	    x = WORLD_WIDTH/2;

	if (z > WORLD_WIDTH/2)
	    z = WORLD_WIDTH/2;

	updated = true;
    };
};
