const state = {
    imageViewer:{
        showModal : false,
        path:""
    }
}
const getters = {
    getImageViewer: (state) => state.imageViewer
}
const actions = {
}
const mutations = {
    changePath:(state, val)=>{
        state.imageViewer.path = val
    },
    changeNestedAdmin:(state, data)=>{
        state[data.name1][data.name2] = data.val
    }
}
export default {
    state,
    getters,
    actions,
    mutations
}