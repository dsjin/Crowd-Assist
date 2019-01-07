// For NSC MOCK Routing
if(type === "emergency"){
    if(data.requesterUid === "f0Eloum12bf6xjtPuRsNQZK4igm2" && key === "dupLI3S8RoUyZkEfUcCDl0dBPLO2"){
        admin.database().ref("notification/"+key+"/"+id).set(dataForSet)
        admin.database().ref("send_notification/"+id+"/"+key).set(timestamp)
    }else if(data.requesterUid === "dupLI3S8RoUyZkEfUcCDl0dBPLO2" && key === "f0Eloum12bf6xjtPuRsNQZK4igm2"){
        admin.database().ref("notification/"+key+"/"+id).set(dataForSet)
        admin.database().ref("send_notification/"+id+"/"+key).set(timestamp)
    }else if(data.requesterUid === "FIHsL9qFejSWJaS36ko6LaeWRIi1"){
        admin.database().ref("notification/"+key+"/"+id).set(dataForSet)
        admin.database().ref("send_notification/"+id+"/"+key).set(timestamp)
    }
}else{
    admin.database().ref("notification/"+key+"/"+id).set(dataForSet)
    admin.database().ref("send_notification/"+id+"/"+key).set(timestamp)
}