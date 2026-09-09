auto generate_random = [](int min, int max) {
    std::random_device random_device;
    std::default_random_engine default_random_engine(random_device());
    std::uniform_int_distribution<int> uniform_distribution(min, max);

    return uniform_distribution(default_random_engine);
};
