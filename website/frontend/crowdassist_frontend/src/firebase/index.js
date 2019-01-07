import firebase from 'firebase'
require("firebase/firestore");

/* Create config.js and add firebase config in same directory */

export default {
  install: function(Vue) {
    Object.defineProperty(Vue.prototype, '$firebase', { value: firebase });
  }
}
