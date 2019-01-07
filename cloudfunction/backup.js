const functions = require("firebase-functions")
const admin = require("firebase-admin")
const GeoFire = require("geofire")
const fetchUser = require("./fetch")
admin.initializeApp(functions.config().firebase)

exports.generalRequest = functions.database
  .ref("/non_emergency/{request_id}")
  .onCreate(event => {
    let data = event.data.val()
    let ref = admin
      .database()
      .ref("/non_emergency/" + event.params.request_id + "/timestamp")
    let refName = admin
      .database()
      .ref(
        "users/" +
          data.requesterUid +
          "/non_emergency/" +
          data.type +
          "/" +
          event.params.request_id
      )
    let timestamp = admin.database.ServerValue.TIMESTAMP
    return Promise.all([
      ref.set(timestamp),
      refName.set({
        timestamp: timestamp,
        title: data.title
      })
    ]).then(result => {
      return result
    })
  })

/**
 * fetchUser from Database Reference "location" with radius ( km )
 * wait trigger from "/emergency/{request_id}"
 */
/*
exports.fetchUser = functions.database.ref("/emergency/{request_id}").onWrite(async (event) => {
    if (!event.data.val()) {
        return;
    }
    let data = event.data.val()
    let id = event.params.request_id
    let ref = admin.database().ref("location")
    let geoFire = new GeoFire(ref)
    let latlng = [data.lat, data.lng]
    let geoQuery = geoFire.query({
        center : latlng,
        radius : 1 // Will Change Later
    })
    let result = await fetchUser.fetch(geoQuery, admin, id, data)
    return result
});*/

exports.notification = functions.database
  .ref("/notification/{user_id}/{request_id}")
  .onCreate(event => {
    /*
    const payload = {
        notification:{
          title : "คุณมีการแจ้งเตือนใหม่",
          icon: "myicon",
          sound: "default"
        }
      };
    return admin.messaging().sendToTopic(event.params.user_id, payload);*/
    console.log(event.params.user_id)
    console.log(event.params.request_id)
    console.log(event.data.val())
    return
  })

exports.fetchUser = functions.https.onRequest(async (req, res) => {
  if (
    typeof req.query.lat === "undefined" &&
    typeof req.query.lng === "undefined"
  ) {
    res.send("Please Enter Lat and Lng")
  }
  let ref = admin.database().ref("location")
  let geoFire = new GeoFire(ref)
  let latlng = [parseFloat(req.query.lat), parseFloat(req.query.lng)]
  let geoQuery = geoFire.query({
    center: [13.729896, 100.779316],
    radius: 2
  })

  function query(geoQuery) {
    return new Promise(function(resolve, reject) {
      data = []
      geoQuery.on("key_entered", function(key, loc, dist) {
        admin
          .database()
          .ref("test/" + key)
          .set("testja")
        data.push(key)
      })
      geoQuery.on("ready", function() {
        geoQuery.cancel()
        resolve(data)
      })
    })
  }
  let name = await query(geoQuery)
  res.send(name)
})
