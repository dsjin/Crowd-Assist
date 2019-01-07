function fetchByGeoQuery(geoQuery, admin, id, data, type, timestamp, risk, time){
    return new Promise(function(resolve, reject){
        var dataForSet = {
            "requesterUid" : data.requesterUid,
            "lat": data.lat, 
            "lng": data.lng,
            "timestamp": timestamp,
            "timestamp_sort": (-1*timestamp),
            "type": type
        }
        geoQuery.on('key_entered', async function (key, loc, dist) {
            var user_info = key.split("@")
            if(user_info[0] !== data.requesterUid){
                let volunteerData = await admin.database().ref('users').child(user_info[0]).once("value")
                let volunteerAge = getAge(volunteerData.val().dateOfBirth)
                let volunteerGender = volunteerData.val().gender
                let volunteerRole = volunteerData.val().role
                if(volunteerRole === "officer" || doSending(volunteerAge,  volunteerGender, risk, time)){
                    admin.database().ref("notification/"+user_info[0]+"/"+id).set(Object.assign(dataForSet,
                        {
                            "volunteer": {
                                "username": volunteerData.val().username,
                                "token": user_info[1]
                            }
                        }))
                    admin.database().ref("send_notification/"+id+"/"+user_info[0]).set(timestamp)
                }
            }
        })
        geoQuery.on('ready', function () {
            geoQuery.cancel()
            resolve("ok")
        })
    })
}

function getAge(dateString) {
    var today = new Date();
    var birthDate = new Date(dateString);
    var age = today.getFullYear() - birthDate.getFullYear();
    var m = today.getMonth() - birthDate.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
        age--;
    }
    return age;
}

function doSending(volunteerAge, volunteerGender, risk, time){
    if(time === 1 || time === 2){
        if (risk === "low") {
            return true
        } else if (risk === "normal") {
            if (volunteerAge < 25) {
                return false
            } else {
                return true
            }
        } else {
            if (volunteerAge >= 25 && volunteerAge <= 40) {
                if (volunteerGender === "male") {
                    return true
                } else {
                    return false
                }
            } else {
                return false
            }
        }
    }else{
        if (risk === "low") {
            return true;
        } else if (risk === "normal") {
            if (volunteerAge < 25) {
                return false
            } else {
                return true
            }
        } else {
            if (volunteerAge >= 25 && volunteerAge <= 60) {
                return true
            } else {
                return false
            }
        }
    }
}

module.exports.fetch = fetchByGeoQuery