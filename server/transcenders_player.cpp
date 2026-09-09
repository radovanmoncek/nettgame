struct player : entity {
    std::string name="";
    uint8_t classification, firing = 0x0;
    float hitscan_shot_x, hitscan_shot_y, hitscan_shot_z, speed=1.f;

    player(int assigned_unique_id, std::vector<entity*> &entities): entity(entities) {
	entity_id = protocol::usable::player;
	unique_id=assigned_unique_id;
	updated=true;
    };
    void update() override {
	y-=velocity;//calculate dy

	if (y>=MAX_JUMP_HEIGHT-0.8f)
	    velocity=0.005f;

	if (y<=1)
	    velocity=0.f;

	if (firing && is_point_in_rectangle(hitscan_shot_x, hitscan_shot_z, -WORLD_WIDTH/2, -WORLD_WIDTH/2, WORLD_WIDTH, WORLD_WIDTH)) {
	    hitscan_shot_x+=HITSCAN_SHOT_SPEED*std::sin(azimuth);
	    hitscan_shot_z+=HITSCAN_SHOT_SPEED*-std::cos(azimuth);

	    for (auto current_entity : entities) {
		if (current_entity->entity_id==protocol::usable::player)
		    continue;

		if (is_point_in_rectangle(hitscan_shot_x, hitscan_shot_z, current_entity->x-HITBOX_SIZE, current_entity->z-HITBOX_SIZE, 2*HITBOX_SIZE, 2*HITBOX_SIZE)) {
		    current_entity->health_points-=100;
		    hitscan_shot_x=hitscan_shot_z=0;
		    firing=0x0;
		}
	    }
	}

	if (!is_point_in_rectangle(hitscan_shot_x, hitscan_shot_z, -WORLD_WIDTH/2, -WORLD_WIDTH/2, WORLD_WIDTH, WORLD_WIDTH)) {
	    hitscan_shot_x=hitscan_shot_z=0;
	    firing=0x0;
	}

	if (y > 1)
	    updated = true;
    }
    void update(entity *request_entity) override {
	float dx = std::abs(x-request_entity->x), dz = std::abs(z-request_entity->z), dazimuth=std::abs(azimuth-request_entity->azimuth);

	if (dx!=0) {
	    x=request_entity->x*speed;
	    updated=true;
	}

	if(request_entity->y==MAX_JUMP_HEIGHT&&y<=1) {
	    velocity=-0.005f;
	    updated=true;
	}

	if(dz!=0) {
	    z=request_entity->z*speed;
	    updated=true;
	}

	if (dazimuth!=0) {
	    azimuth=request_entity->azimuth;
	    updated=true;
	}

	if (!firing) {
	    firing=static_cast<player*>(request_entity)->firing;
	    hitscan_shot_z=z;
	}

	if (name!=static_cast<player*>(request_entity)->name) {
	  name=static_cast<player*>(request_entity)->name;
	  updated=true;
	}
    }
};
