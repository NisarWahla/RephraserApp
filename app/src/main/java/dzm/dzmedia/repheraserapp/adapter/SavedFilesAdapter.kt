package dzm.dzmedia.repheraserapp.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dzm.dzmedia.repheraserapp.R
import dzm.dzmedia.repheraserapp.databinding.ActivitySavedFilesBinding
import dzm.dzmedia.repheraserapp.databinding.SavedFilesSwiperevealLayBinding
import dzm.dzmedia.repheraserapp.helpers.Utils
import dzm.dzmedia.repheraserapp.model.SavedFilesModel
import java.io.File
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.*

class SavedFilesAdapter(
    val context: Context,
    val list: ArrayList<SavedFilesModel>,
    val binding: ActivitySavedFilesBinding
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TAG = SavedFilesAdapter::class.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = SavedFilesSwiperevealLayBinding.inflate(inflater, parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = list[position]

        if (holder is MyViewHolder) {
            if (data.name.endsWith(".pdf")) {
                Glide.with(context).load(R.drawable.savedpdf).into(holder.binding.fileTypeIcon)
            } else if (data.name.endsWith(".txt")) {
                Glide.with(context).load(R.drawable.ic_savetext).into(holder.binding.fileTypeIcon)
            } else {
                Glide.with(context).load(R.drawable.savedword).into(holder.binding.fileTypeIcon)
            }

            holder.binding.ruTitle.text = data.name
            val sdf = SimpleDateFormat("HH:mm a . MMMM dd, yyyy", Locale.getDefault())
            val currentDateString = sdf.format(data.cDate)
            holder.binding.ruTime.text = currentDateString

            holder.binding.container.setOnClickListener {
                if (data.name.endsWith(".pdf")) {

                    val path = data.file_path
                    Log.d(TAG, "onBindViewHolder: $path")
                    val file = File(path)
                    val fileUri =
                        FileProvider.getUriForFile(
                            context,
                            Utils.AUTHORITY, file
                        )
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(fileUri, "application/pdf")
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        context.startActivity(intent)
                        Log.i(TAG, "onSelect: $path")
                    } catch (e: Exception) {
                        Log.i(TAG, "onSelect: ${e.message}")
                    }
                } else if (data.name.endsWith(".doc")) {

                    val path = data.file_path
                    Log.d(TAG, "onBindViewHolder: $path")
                    val file = File(path)
                    val fileUri =
                        FileProvider.getUriForFile(
                            context,
                            Utils.AUTHORITY, file
                        )
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(fileUri, "application/msword")
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        context.startActivity(intent)
                        Log.i(TAG, "onSelect: $path")
                    } catch (e: Exception) {
                        Log.i(TAG, "onSelect: ${e.message}")
                    }
                } else if (data.name.endsWith(".txt")) {

                    val path = data.file_path
                    Log.d(TAG, "onBindViewHolder: $path")
                    val file = File(path)
                    val fileUri =
                        FileProvider.getUriForFile(
                            context,
                            Utils.AUTHORITY, file
                        )
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(fileUri, "text/plain")
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        context.startActivity(intent)
                        Log.i(TAG, "onSelect: $path")
                    } catch (e: Exception) {
                        Log.i(TAG, "onSelect: ${e.message}")
                    }
                }
            }

            holder.binding.menuOptions.setOnClickListener {
                val popup = PopupMenu(
                    context,
                    holder.binding.menuOptions,
                    Gravity.END,
                    0,
                    R.style.MyPopupMenu
                )
                try {
                    val method: Method = popup.menu.javaClass.getDeclaredMethod(
                        "setOptionalIconsVisible",
                        Boolean::class.javaPrimitiveType
                    )
                    method.isAccessible = true
                    method.invoke(popup.menu, true)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                popup.inflate(R.menu.savefiles_menu_options_lay)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.delete -> {
                            val path = data.file_path
                            val file = File(path)
                            Log.i(TAG, "cancelDownloading: $path")
                            if (file.exists()) {
                                file.delete()
                                list.remove(data)
                                if (list.size == 0) {
                                    binding.nothingShow.visibility = View.VISIBLE
                                    binding.mianScrEmptyIcon.visibility = View.VISIBLE
                                }
                                notifyDataSetChanged()
                                Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            notifyDataSetChanged()
                        }

                        R.id.share -> {
                            val path = data.file_path
                            Log.d(TAG, "onBindViewHolder: $path")
                            val file = File(path)
                            val fileUri =
                                FileProvider.getUriForFile(
                                    context,
                                    Utils.AUTHORITY, file
                                )

                            try {
                                val intent = Intent(Intent.ACTION_SEND)
                                intent.setDataAndType(fileUri, "application/pdf")
                                intent.putExtra(Intent.EXTRA_SUBJECT, data.name)
                                intent.putExtra(Intent.EXTRA_STREAM, fileUri)
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                context.startActivity(Intent.createChooser(intent, "Rephraser"))
                                Log.i(TAG, "onSelect: $path")
                            } catch (e: Exception) {
                                Log.i(TAG, "onSelect: ${e.message}")
                            }
                        }
                    }
                    false
                }
                popup.show()
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(val binding: SavedFilesSwiperevealLayBinding) :
        RecyclerView.ViewHolder(binding.root)
}