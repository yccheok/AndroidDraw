package com.divyanshu.draw.activity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.divyanshu.draw.R

class CancelOrSaveArrayAdapter(context: Context?) : ArrayAdapter<String>(
        context,
        R.layout.cancel_or_save_array_adapter,
        arrayOf(
                context?.getString(android.R.string.cancel),
                context?.getString(R.string.action_save)
        )
) {

    private class ViewHolder(view: View) {
        val textView: TextView

        init {
            textView = view.findViewById(R.id.text_view)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View

        if (convertView == null) {
            val inflator = LayoutInflater.from(this.context)
            view = inflator.inflate(R.layout.cancel_or_save_array_adapter, null)
            val viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
        }

        val holder = view.tag as ViewHolder
        val string = getItem(position)

        // TODO: Typeface?

        holder.textView.setText(string)

        return view
    }
}
