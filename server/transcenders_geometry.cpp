auto is_point_in_rectangle = [](float point_x, float point_z, float rectangle_x, float rectangle_z, float width, float height) {
    return (rectangle_x < point_x && point_x < rectangle_x+width) && (rectangle_z < point_z && point_z < rectangle_z+height); 
};
