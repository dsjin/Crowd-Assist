import Vue from 'vue'

const state = {
    inProgress:false,
    firebaseUser: Object,
    isAuth: false,
    data:[],
    current:Object,
    inRegister:false,
    complete:false
}

const getters = {
    getAuth: (state) => state.isAuth,
    getProgress: (state) => state.inProgress,
    getData:(state) => state.data,
    getCurrent:(state) => state.current,
    getInRegister:(state) => state.inRegister,
    getComplete:(state) => state.complete
}

const actions = {
    createAccount: ({state}, payload)=>{
        state.inRegister = true;
        Vue.prototype.$firebase.auth().createUserWithEmailAndPassword(payload.email, payload.password).then(async (user)=>{
            let userInformation = {
                firstName: payload.firstName,
                lastName: payload.lastName,
                gender: payload.gender,
                role: "officer",
                verify:false,
                username: payload.username,
                section: payload.section,
                subSection: payload.subSection,
                idCard: payload.idCard,
                employeeNumber: payload.employeeNumber,
                firstTime:false,
                pin: payload.pin,
                email:payload.email,
                dateOfBirth: payload.birthdate,
                rating:{
                    count:0,
                    scores:0
                }
            }
            let verifyInformation = {
                name: payload.firstName+" "+payload.lastName,
                firstName: payload.firstName,
                lastName: payload.lastName,
                gender: payload.gender,
                role: "officer",
                timestamp: Date.now(),
                verify:false,
                username: payload.username,
                idCard: payload.idCard,
                section: payload.section,
                subSection: payload.subSection,
                employeeNumber: payload.employeeNumber,
                dateOfBirth: payload.birthdate,
                referencePath:{

                }
            }
            try{
                await Vue.prototype.$firebase.database().ref('users/'+user.uid).set(userInformation)
                await Vue.prototype.$firebase.database().ref('user_id/'+userInformation.username).set(userInformation.email)
                await Vue.prototype.$firebase.storage().ref('profile/'+user.uid+".png").put(payload.imageForm)
                await Promise.all([
                    Vue.prototype.$firebase.storage().ref('verify/'+user.uid+"/idCard.png").put(payload.idCardFile).then((snapshot) => snapshot.downloadURL),
                    Vue.prototype.$firebase.storage().ref('verify/'+user.uid+"/employeeCard.png").put(payload.employeeCard).then((snapshot) => snapshot.downloadURL),
                    Vue.prototype.$firebase.storage().ref('verify/'+user.uid+"/photoWithEmployeeCard.png").put(payload.photoWithEmployeeCard).then((snapshot) => snapshot.downloadURL)
                ]).then(async (url) => {
                    verifyInformation.referencePath.idCard = url[0]
                    verifyInformation.referencePath.employeeCard = url[1]
                    verifyInformation.referencePath.photoWithEmployeeCard = url[2]
                    await Vue.prototype.$firebase.firestore().collection("verify").doc(user.uid).set(verifyInformation)
                    Vue.prototype.$firebase.auth().signOut()
                    state.inRegister = false
                    state.complete = true
                })
            }catch(e){
                console.log(e)
            }
        }).catch((error) => {
            console.log(error.message);
        })
    },
    login: ({state}, payload)=>{
        state.inProgess = true
        Vue.prototype.$firebase.auth().signInWithEmailAndPassword(payload.username, payload.password).catch(e=>{
            console.log(e)
        })
    },
    logout: ({state})=>{
        Vue.prototype.$firebase.auth().signOut().then((result)=>{
            state.isAuth = false
        })
    },
    fetchVerifyDocument: ({state})=>{
        Vue.prototype.$firebase.firestore().collection("verify").where('verify','==',false).orderBy("timestamp").get().then((querySnapshot)=>{
            querySnapshot.forEach((doc)=> {       
                state.data.push(Object.assign({uid:doc.id}, doc.data()))
            });
        }).catch(function(error) {
            console.log("Error getting document:", error);
        })
    },
    getVerifyDocument: ({state}, value)=>{
        Vue.prototype.$firebase.firestore().collection("verify").where('username','==',value).get().then((querySnapshot)=>{
            querySnapshot.forEach((doc)=> {       
                state.current = Object.assign({uid:doc.id}, doc.data())
            });
        }).catch(function(error) {
            console.log("Error getting document:", error);
        })
    }
}

const mutations = {
    setUser: (state, userDetail) => {
        state.firebaseUser = userDetail
    },
    setAuth:(state, value) =>{
        state.isAuth = value
    },
    setProgress:(state, value) =>{
        state.inProgress = value
    },
    setComplete:(state, value) =>{
        state.complete = value
    },
    resetRegister:(state)=>{
        state.inRegister=false,
        state.complete=false
    },
    reset:(state)=>{
        state.data = []
    }
}

export default {
    getters,
    state,
    actions,
    mutations
}