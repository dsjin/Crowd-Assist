function _asyncToGenerator(fn) { return function () { var self = this, args = arguments; return new Promise(function (resolve, reject) { var gen = fn.apply(self, args); function step(key, arg) { try { var info = gen[key](arg); var value = info.value; } catch (error) { reject(error); return; } if (info.done) { resolve(value); } else { Promise.resolve(value).then(_next, _throw); } } function _next(value) { step("next", value); } function _throw(err) { step("throw", err); } _next(); }); }; }

const functions = require("firebase-functions");

const admin = require("firebase-admin");

const GeoFire = require("geofire");

const fetchUser = require("./fetch");

const convert = require("./convert");

admin.initializeApp(functions.config().firebase);
exports.generalRequest = functions.database.ref("/non_emergency/{request_id}").onCreate(
/*#__PURE__*/
function () {
  var _ref = _asyncToGenerator(function* (event) {
    let data = event.data.val();
    let ref_request = admin.database().ref("/non_emergency/" + event.params.request_id + "/timestamp");
    let ref_count = admin.database().ref("/users/" + data.requesterUid + "/numOfDoc/general");
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

    let timestamp = admin.database.ServerValue.TIMESTAMP;
    yield ref_request.set(timestamp);
    yield ref_count.transaction(current => {
      return (current || 0) + 1;
    });
    let id = event.params.request_id;
    let ref = admin.database().ref("location");
    /*
    let databaseTimestamp = await admin
      .database()
      .ref("/non_emergency/" + event.params.request_id + "/timestamp")
      .once("value")*/

    let risk = yield admin.database().ref("/risk/" + data.area).once("value");
    let geoFire = new GeoFire(ref);
    let latlng = [data.lat, data.lng];
    let geoQuery = geoFire.query({
      center: latlng,
      radius: convert.radius(1)
    });
    let result = yield fetchUser.fetch(geoQuery, admin, id, data, "non_emergency", timestamp, risk.val(), 1);
    return result;
  });

  return function (_x) {
    return _ref.apply(this, arguments);
  };
}());
exports.emergencyRequest = functions.database.ref("/emergency/{request_id}").onCreate(
/*#__PURE__*/
function () {
  var _ref2 = _asyncToGenerator(function* (event) {
    let data = event.data.val();
    let ref_request = admin.database().ref("/emergency/" + event.params.request_id + "/timestamp");
    let ref_count = admin.database().ref("/users/" + data.requesterUid + "/numOfDoc/emergency");
    let timestamp = admin.database.ServerValue.TIMESTAMP;
    yield ref_request.set(timestamp);
    yield ref_count.transaction(current => {
      return (current || 0) + 1;
    });
    let id = event.params.request_id;
    let ref = admin.database().ref("location");
    /*
    let databaseTimestamp = await admin
      .database()
      .ref("/emergency/" + event.params.request_id + "/timestamp")
      .once("value")*/

    let risk = yield admin.database().ref("/risk/" + data.area).once("value");
    let geoFire = new GeoFire(ref);
    let latlng = [data.lat, data.lng];
    let geoQuery = geoFire.query({
      center: latlng,
      radius: convert.radius(1)
    });
    let result = yield fetchUser.fetch(geoQuery, admin, id, data, "emergency", timestamp, risk.val(), 1);
    return result;
  });

  return function (_x2) {
    return _ref2.apply(this, arguments);
  };
}());
exports.fetchEmergency = functions.database.ref("/emergency/{request_id}/time").onUpdate(
/*#__PURE__*/
function () {
  var _ref3 = _asyncToGenerator(function* (event) {
    if (!event.data.val()) {
      return;
    }

    if (event.data.val() === 1) {
      return;
    }

    let time = event.data.val();
    let requestData = yield admin.database().ref("/emergency/" + event.params.request_id).once("value");
    let id = event.params.request_id;
    let risk = yield admin.database().ref("/risk/" + requestData.val().area).once("value");
    let ref = admin.database().ref("location");
    let geoFire = new GeoFire(ref);
    let latlng = [requestData.val().lat, requestData.val().lng];
    let geoQuery = geoFire.query({
      center: latlng,
      radius: convert.radius(time)
    });
    let result = yield fetchUser.fetch(geoQuery, admin, id, requestData.val(), "emergency", requestData.val().timestamp, risk.val(), time);
    return result;
  });

  return function (_x3) {
    return _ref3.apply(this, arguments);
  };
}());
exports.fetchNonEmergency = functions.database.ref("/non-emergency/{request_id}/time").onUpdate(
/*#__PURE__*/
function () {
  var _ref4 = _asyncToGenerator(function* (event) {
    if (!event.data.val()) {
      return;
    }

    if (event.data.val() === 1) {
      return;
    }

    let time = event.data.val();
    let requestData = yield admin.database().ref("/emergency/" + event.params.request_id).once("value");
    let id = event.params.request_id;
    let risk = yield admin.database().ref("/risk/" + requestData.val().area).once("value");
    let ref = admin.database().ref("location");
    let geoFire = new GeoFire(ref);
    let latlng = [requestData.val().lat, requestData.val().lng];
    let geoQuery = geoFire.query({
      center: latlng,
      radius: convert.radius(time)
    });
    let result = yield fetchUser.fetch(geoQuery, admin, id, requestData.val(), "non-emergency", requestData.val().timestamp, risk.val(), time);
    return result;
  });

  return function (_x4) {
    return _ref4.apply(this, arguments);
  };
}());
exports.notification = functions.database.ref("/notification/{user_id}/{request_id}").onCreate(
/*#__PURE__*/
function () {
  var _ref5 = _asyncToGenerator(function* (event) {
    const payload = {
      notification: {
        title: "คุณมีการแจ้งเตือนใหม่",
        icon: "myicon",
        sound: "default"
      },
      data: {
        request: true,
        request_uid: event.params.request_id
      }
    };
    let user = yield admin.database().ref("users/" + event.data.val().requesterUid).once("value");
    let ref = admin.database().ref("/notification/" + event.params.user_id + "/" + event.params.request_id + "/user");
    return Promise.all([ref.set({
      userId: user.val().username,
      firstName: user.val().firstName,
      lastName: user.val().lastName,
      gender: user.val().gender,
      dateOfBirth: user.val().dateOfBirth,
      gender: user.val().gender
    }), admin.messaging().sendToTopic(user.val().username + "." + event.data.val().user.token, payload)]);
  });

  return function (_x5) {
    return _ref5.apply(this, arguments);
  };
}());
exports.changeEmergencyStatus = functions.database.ref("/emergency_assistance/{request_id}").onCreate(event => {
  let ref = admin.database().ref("/emergency/" + event.params.request_id + "/status");
  return ref.set("open");
});
exports.changeNonEmergencyStatus = functions.database.ref("/non_emergency_assistance/{request_id}").onCreate(event => {
  let ref = admin.database().ref("/non_emergency/" + event.params.request_id + "/status");
  return ref.set("open");
});
exports.countEmergencyStatus = functions.database.ref("/emergency_assistance/{request_id}/user/{user_id}").onCreate(event => {
  let data = event.data.val();
  var countType;

  if (data.role === "officer") {
    countType = "officerCount";
  } else {
    countType = "userCount";
  }

  let ref = admin.database().ref("/emergency_assistance/" + event.params.request_id + "/" + countType);
  let count_ref = admin.database().ref("/users/" + event.params.user_id + "/numOfDoc/assistance");
  return Promise.all([ref.transaction(current => {
    return (current || 0) + 1;
  }).then(() => {
    return console.log("Counter updated.");
  }), count_ref.transaction(current => {
    return (current || 0) + 1;
  })]);
});
exports.countNonEmergencyStatus = functions.database.ref("/non_emergency_assistance/{request_id}/user/{user_id}").onCreate(event => {
  let data = event.data.val();
  var countType;

  if (data.role === "officer") {
    countType = "officerCount";
  } else {
    countType = "userCount";
  }

  let ref = admin.database().ref("/non_emergency_assistance/" + event.params.request_id + "/" + countType);
  let count_ref = admin.database().ref("/users/" + event.params.user_id + "/numOfDoc/assistance");
  return Promise.all([ref.transaction(current => {
    return (current || 0) + 1;
  }).then(() => {
    return console.log("Counter updated.");
  }), count_ref.transaction(current => {
    return (current || 0) + 1;
  })]);
});
exports.deleteEmergencyNotification = functions.database.ref("/emergency/{request_id}/status").onWrite(
/*#__PURE__*/
function () {
  var _ref6 = _asyncToGenerator(function* (event) {
    let data = event.data.val();

    if (data !== "close") {
      retrun;
    }

    let getSendingNotification = yield admin.database().ref("/send_notification/" + event.params.request_id).once("value");
    getSendingNotification.forEach(data => {
      admin.database().ref("notification/" + data.key + "/" + event.params.request_id).remove();
    });
    return "OK";
  });

  return function (_x6) {
    return _ref6.apply(this, arguments);
  };
}());
exports.deleteNonEmergencyNotification = functions.database.ref("/non_emergency/{request_id}/status").onWrite(
/*#__PURE__*/
function () {
  var _ref7 = _asyncToGenerator(function* (event) {
    let data = event.data.val();

    if (data !== "close") {
      retrun;
    }

    let getSendingNotification = yield admin.database().ref("/send_notification/" + event.params.request_id).once("value");
    getSendingNotification.forEach(data => {
      admin.database().ref("notification/" + data.key + "/" + event.params.request_id).remove();
    });
    return "OK";
  });

  return function (_x7) {
    return _ref7.apply(this, arguments);
  };
}());
exports.rating = functions.firestore.document("request/{request_id}/{user_rated_id}/{rater_id}").onCreate(event => {
  let rating = event.data.data().rating;
  let userRatedId = event.params.user_rated_id;
  let scoreRef = admin.database().ref("/users/" + userRatedId + "/rating/scores");
  let countUserRef = admin.database().ref("/users/" + userRatedId + "/rating/counts");
  return Promise.all([scoreRef.transaction(current => {
    return (current || 0) + rating;
  }).then(() => {
    return console.log("Score updated.");
  }), countUserRef.transaction(current => {
    return (current || 0) + 1;
  }).then(() => {
    return console.log("Count updated.");
  })]);
});
exports.appUninstall = functions.analytics.event("app_remove").onLog(event => {
  const user = event.data.user.userProperties;
  const token = user.token.value;
  const username = user.username.value;

  if (!token || !username) {
    return;
  }

  let ref = admin.database().ref("location");
  let geoFire = new GeoFire(ref);
  return geoFire.remove(username + "@" + token);
});