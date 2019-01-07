const state = {
    showModal : false,
    showTermModal : false,
    image: undefined,
    form:{
        imageForm: Blob,
    },
    basic_data:{
        username:"",
        firstName:"",
        lastName:"",
        email:"",
        birthdate:"",
        idCard:"",
        password:"",
        gender:"",
        pin:""
    },
    sector_data:{
        section:"",
        subSection:"",
        employeeNumber:""
    },
    verify_data:{
        idCardFile: File,
        employeeCard: File,
        photoWithEmployeeCard: File
    },
    usernameAvaliable:true
}
const getters = {
    getShowModal: (state) => state.showModal,
    getImage: (state) => state.image,
    getImageForm:(state) => state.form,
    getBasicData: (state) => state.basic_data,
    getSectorData: (state) => state.sector_data,
    getVerifyData: (state) => state.verify_data,
    getShowTermModal: (state) => state.showTermModal,
    getUsernameAvaliable: (state) => state.usernameAvaliable,
    getAccept: (state) => state.accept
}
const actions = {
}
const mutations = {
    push:(state, data)=>{
        state[data.name].push(data.val)
    },
    change:(state, data)=>{
        state[data.name] = data.val
    },
    changeNested:(state, data)=>{
        state[data.name1][data.name2] = data.val
    },
    setOfficerRegisterDefault:(state)=>{
        state = {
            showModal : false,
            showTermModal : false,
            image: undefined,
            form:{
                imageForm: Blob,
            },
            basic_data:{
                username:"",
                firstName:"",
                lastName:"",
                email:"",
                birthdate:"",
                idCard:"",
                password:"",
                gender:"",
                pin:""
            },
            sector_data:{
                section:"",
                subSection:""
            },
            verify_data:{
                idCard: File,
                employeeCard: File,
                photoWithEmployeeCard: File
            },
            usernameAvaliable:true
        }
    }
}
export default {
    state,
    getters,
    actions,
    mutations
}