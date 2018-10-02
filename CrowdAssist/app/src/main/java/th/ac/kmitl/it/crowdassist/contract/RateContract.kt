package th.ac.kmitl.it.crowdassist.contract

import android.net.Uri

interface RateContract {
    interface View{
        fun finish(resultCode : Int)
        fun setNameCardView(name : String)
        fun setRoleTextView(type : String)
        fun setImageProfile(uri : Uri)
        fun showCardView()
        fun hideCardView()
        fun showProgressBar()
        fun hideProgressBar()
        fun getRating() : Double
        fun setRating(rating : Double)
        fun getDescription() : String
        fun setDescription(description : String)
        fun setImageProfile(resId : Int)
    }
    interface Presenter{
        fun prepareAssistantData()
        fun onRateClicked()
        fun onIgnoreClicked()
    }
}