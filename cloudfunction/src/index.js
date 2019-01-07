const functions = require("firebase-functions")
const admin = require("firebase-admin")
const GeoFire = require("geofire")
const fetchUser = require("./fetch")
const convert = require("./convert")
admin.initializeApp(functions.config().firebase)

exports.generalRequest = functions.database
  .ref("/non_emergency/{request_id}")
  .onCreate(async event => {
    let data = event.data.val()
    let ref_request = admin
      .database()
      .ref("/non_emergency/" + event.params.request_id + "/timestamp")
    let ref_count = admin
      .database()
      .ref("/users/" + data.requesterUid + "/numOfDoc/general")
    /*
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
    })*/
    let timestamp = admin.database.ServerValue.TIMESTAMP
    await ref_request.set(timestamp)
    await ref_count.transaction(current => {
      return (current || 0) + 1
    })
    let id = event.params.request_id
    let ref = admin.database().ref("location")
    let databaseTimestamp = await admin
      .database()
      .ref("/non_emergency/" + event.params.request_id + "/timestamp")
      .once("value")
    let risk = await admin
      .database()
      .ref("/area/" + data.area)
      .once("value")
    let geoFire = new GeoFire(ref)
    let latlng = [data.lat, data.lng]
    let geoQuery = geoFire.query({
      center: latlng,
      radius: convert.radius(1)
    })
    let result = await fetchUser.fetch(
      geoQuery,
      admin,
      id,
      data,
      "non_emergency",
      databaseTimestamp.val(),
      risk.val(),
      1
    )
    return result
  })

exports.emergencyRequest = functions.database
  .ref("/emergency/{request_id}")
  .onCreate(async event => {
    let data = event.data.val()
    let ref_request = admin
      .database()
      .ref("/emergency/" + event.params.request_id + "/timestamp")
    let ref_count = admin
      .database()
      .ref("/users/" + data.requesterUid + "/numOfDoc/emergency")
    let timestamp = admin.database.ServerValue.TIMESTAMP
    await ref_request.set(timestamp)
    await ref_count.transaction(current => {
      return (current || 0) + 1
    })
    let id = event.params.request_id
    let ref = admin.database().ref("location")
    let databaseTimestamp = await admin
      .database()
      .ref("/emergency/" + event.params.request_id + "/timestamp")
      .once("value")
    let risk = await admin
      .database()
      .ref("/area/" + data.area)
      .once("value")
    let geoFire = new GeoFire(ref)
    let latlng = [data.lat, data.lng]
    let geoQuery = geoFire.query({
      center: latlng,
      radius: convert.radius(1)
    })
    let result = await fetchUser.fetch(
      geoQuery,
      admin,
      id,
      data,
      "emergency",
      databaseTimestamp.val(),
      risk.val(),
      1
    )
    return result
  })

exports.fetchEmergency = functions.database
  .ref("/emergency/{request_id}/time")
  .onUpdate(async event => {
    if (!event.data.val()) {
      return
    }
    if (event.data.val() === 1) {
      return
    }
    let time = event.data.val()
    let requestData = await admin
      .database()
      .ref("/emergency/" + event.params.request_id)
      .once("value")
    let id = event.params.request_id
    let risk = await admin
      .database()
      .ref("/area/" + requestData.val().area)
      .once("value")
    let ref = admin.database().ref("location")
    let geoFire = new GeoFire(ref)
    let latlng = [requestData.val().lat, requestData.val().lng]
    let geoQuery = geoFire.query({
      center: latlng,
      radius: convert.radius(time)
    })
    let result = await fetchUser.fetch(
      geoQuery,
      admin,
      id,
      requestData.val(),
      "emergency",
      requestData.val().timestamp,
      risk.val(),
      time
    )
    return result
  })

exports.fetchNonEmergency = functions.database
  .ref("/non-emergency/{request_id}/time")
  .onUpdate(async event => {
    if (!event.data.val()) {
      return
    }
    if (event.data.val() === 1) {
      return
    }
    let time = event.data.val()
    let requestData = await admin
      .database()
      .ref("/non-emergency/" + event.params.request_id)
      .once("value")
    let id = event.params.request_id
    let risk = await admin
      .database()
      .ref("/area/" + requestData.val().area)
      .once("value")
    let ref = admin.database().ref("location")
    let geoFire = new GeoFire(ref)
    let latlng = [requestData.val().lat, requestData.val().lng]
    let geoQuery = geoFire.query({
      center: latlng,
      radius: convert.radius(time)
    })
    let result = await fetchUser.fetch(
      geoQuery,
      admin,
      id,
      requestData.val(),
      "non-emergency",
      requestData.val().timestamp,
      risk.val(),
      time
    )
    return result
  })
exports.notification = functions.database
  .ref("/notification/{user_id}/{request_id}")
  .onCreate(async event => {
    const payload = {
      notification: {
        title: "แจ้งเตือนเหตุ!",
        body: "มีผู้ใช้งานต้องการความช่วยเหลือในตอนนี้!",
        icon: "myicon",
        sound: "default"
      },
      data: {
        request: "true",
        request_uid: event.params.request_id
      }
    }
    let user = await admin
      .database()
      .ref("users/" + event.data.val().requesterUid)
      .once("value")
    let ref = admin
      .database()
      .ref(
        "/notification/" +
          event.params.user_id +
          "/" +
          event.params.request_id +
          "/user"
      )
    return Promise.all([
      ref.set({
        userId: user.val().username,
        firstName: user.val().firstName,
        lastName: user.val().lastName,
        gender: user.val().gender,
        dateOfBirth: user.val().dateOfBirth,
        gender: user.val().gender
      }),
      admin
        .messaging()
        .sendToTopic(
          event.data.val().volunteer.username +
            "." +
            event.data.val().volunteer.token,
          payload
        )
    ])
  })

exports.changeEmergencyStatus = functions.database
  .ref("/emergency_assistance/{request_id}")
  .onCreate(event => {
    let ref = admin
      .database()
      .ref("/emergency/" + event.params.request_id + "/status")
    return ref.set("open")
  })

exports.changeNonEmergencyStatus = functions.database
  .ref("/non_emergency_assistance/{request_id}")
  .onCreate(event => {
    let ref = admin
      .database()
      .ref("/non_emergency/" + event.params.request_id + "/status")
    return ref.set("open")
  })

exports.countEmergencyStatus = functions.database
  .ref("/emergency_assistance/{request_id}/user/{user_id}")
  .onCreate(event => {
    let data = event.data.val()
    var countType
    if (data.role === "officer") {
      countType = "officerCount"
    } else {
      countType = "userCount"
    }
    let ref = admin
      .database()
      .ref("/emergency_assistance/" + event.params.request_id + "/" + countType)
    let count_ref = admin
      .database()
      .ref("/users/" + event.params.user_id + "/numOfDoc/assistance")
    return Promise.all([
      ref
        .transaction(current => {
          return (current || 0) + 1
        })
        .then(() => {
          return console.log("Counter updated.")
        }),
      count_ref.transaction(current => {
        return (current || 0) + 1
      })
    ])
  })

exports.countNonEmergencyStatus = functions.database
  .ref("/non_emergency_assistance/{request_id}/user/{user_id}")
  .onCreate(event => {
    let data = event.data.val()
    var countType
    if (data.role === "officer") {
      countType = "officerCount"
    } else {
      countType = "userCount"
    }
    let ref = admin
      .database()
      .ref(
        "/non_emergency_assistance/" + event.params.request_id + "/" + countType
      )
    let count_ref = admin
      .database()
      .ref("/users/" + event.params.user_id + "/numOfDoc/assistance")
    return Promise.all([
      ref
        .transaction(current => {
          return (current || 0) + 1
        })
        .then(() => {
          return console.log("Counter updated.")
        }),
      count_ref.transaction(current => {
        return (current || 0) + 1
      })
    ])
  })

exports.deleteEmergencyNotification = functions.database
  .ref("/emergency/{request_id}/status")
  .onWrite(async event => {
    let data = event.data.val()
    if (data !== "close") {
      retrun
    }
    let getSendingNotification = await admin
      .database()
      .ref("/send_notification/" + event.params.request_id)
      .once("value")
    getSendingNotification.forEach(data => {
      admin
        .database()
        .ref("notification/" + data.key + "/" + event.params.request_id)
        .remove()
    })
    return "OK"
  })

exports.deleteNonEmergencyNotification = functions.database
  .ref("/non_emergency/{request_id}/status")
  .onWrite(async event => {
    let data = event.data.val()
    if (data !== "close") {
      retrun
    }
    let getSendingNotification = await admin
      .database()
      .ref("/send_notification/" + event.params.request_id)
      .once("value")
    getSendingNotification.forEach(data => {
      admin
        .database()
        .ref("notification/" + data.key + "/" + event.params.request_id)
        .remove()
    })
    return "OK"
  })

exports.rating = functions.firestore
  .document("request/{request_id}/{user_rated_id}/{rater_id}")
  .onCreate(event => {
    let rating = event.data.data().rating
    let userRatedId = event.params.user_rated_id
    let scoreRef = admin
      .database()
      .ref("/users/" + userRatedId + "/rating/scores")
    let countUserRef = admin
      .database()
      .ref("/users/" + userRatedId + "/rating/counts")
    return Promise.all([
      scoreRef
        .transaction(current => {
          return (current || 0) + rating
        })
        .then(() => {
          return console.log("Score updated.")
        }),
      countUserRef
        .transaction(current => {
          return (current || 0) + 1
        })
        .then(() => {
          return console.log("Count updated.")
        })
    ])
  })
exports.appUninstall = functions.analytics.event("app_remove").onLog(event => {
  const user = event.data.user.userProperties
  const token = user.token.value
  const username = user.username.value
  if (!token || !username) {
    return "false"
  }
  let ref = admin.database().ref("location")
  let geoFire = new GeoFire(ref)
  return geoFire.remove(username + "@" + token)
})
