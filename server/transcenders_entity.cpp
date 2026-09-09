struct entity {
    float x=0.f, y=1.f, z=0.f, azimuth=0.f, velocity=0.f;
    int entity_id, unique_id, health_points=100;
    bool updated=false, deleted=false;
    std::vector<entity*> &entities;

    entity(std::vector<entity*> &entities): entities(entities) {};
    virtual void update() {};
    virtual void update(entity*) {};
};
