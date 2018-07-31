package th.ac.kmitl.it.crowdassist.component

import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import th.ac.kmitl.it.crowdassist.R
import th.ac.kmitl.it.crowdassist.modal.ProfileListModel

class RecycleProfileList: RecyclerView.Adapter<RecycleProfileList.ViewHolder>{

    private var list: MutableList<ProfileListModel>? = null
    private var ctx: Context? = null
    private var mode: String? = null

    constructor(list: MutableList<ProfileListModel>?, ctx : Context?, mode : String?){
        this.list = list
        this.ctx = ctx
        this.mode = mode
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.simple_listview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataItem = list?.get(position)
        holder.title.text = (dataItem?.title)
        holder.cardView.setOnClickListener(View.OnClickListener {

        })
        holder.cardView.tag = dataItem
    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView
        var cardView: CardView

        init {
            title = itemView.findViewById(R.id.title)
            cardView = itemView.findViewById(R.id.cardView)
        }
    }
}