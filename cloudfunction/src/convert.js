function ConvertToRadius(time){
    radius = 0.0
    switch(time){
        case 1:
            radius = 1.0;
            break;
        case 2:
            radius = 1.5;
            break;
        case 3:
            radius = 2.0;
            break;
        default:
            radius = 3.0;
    }
    return radius
}

module.exports.radius = ConvertToRadius