package dzm.dzmedia.repheraserapp.interfaces

import dzm.dzmedia.repheraserapp.model.CustomSpinnerModel

interface CustomSpinnerListener {
    fun OnClickSpinner(data: List<CustomSpinnerModel>, pos: Int)
}