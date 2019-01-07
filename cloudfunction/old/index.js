/**
 * fetchUser, fetUserGeneral from Database Reference "location" with radius ( km )
 * wait trigger from "/emergency/{request_id}"
 */
/*
exports.fetchUser = functions.database
  .ref("/emergency/{request_id}")
  .onCreate(async event => {
    if (
      !event.data.val() ||
      typeof event.data.val().timestamp === "undefined"
    ) {
      return
    }
    let data = event.data.val()
    let id = event.params.request_id
    let ref = admin.database().ref("location")
    let geoFire = new GeoFire(ref)
    let latlng = [data.lat, data.lng]
    let geoQuery = geoFire.query({
      center: latlng,
      radius: convert.radius(data.time)
    })
    let result = await fetchUser.fetch(geoQuery, admin, id, data, "emergency")
    return result
  })

exports.fetchUserGeneral = functions.database
  .ref("/non_emergency/{request_id}")
  .onCreate(async event => {
    if (
      !event.data.val() ||
      typeof event.data.val().timestamp === "undefined"
    ) {
      return
    }
    let data = event.data.val()
    let id = event.params.request_id
    let ref = admin.database().ref("location")
    let geoFire = new GeoFire(ref)
    let latlng = [data.lat, data.lng]
    let geoQuery = geoFire.query({
      center: latlng,
      radius: convert.radius(data.time)
    })
    let result = await fetchUser.fetch(
      geoQuery,
      admin,
      id,
      data,
      "non_emergency"
    )
    return result
  })
*/
/*
export let fetch = functions.https.onRequest(async (req, res) => {
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
    center: latlng,
    radius: 2
  })
  let username = await admin
      .database()
      .ref("users/" + event.data.val().requesterUid + "/username")
      .once("value")
  function query(geoQuery) {
    return new Promise(function(resolve, reject) {
      var data = []
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
})*/