package com.limelight.viewmodel

import androidx.lifecycle.ViewModel
import com.limelight.data.FAQCard


class FAQViewModel : ViewModel() {
    internal var cardsList: List<FAQCard>? = null
    internal var expandedCardState: Int = 0
    internal var subExpandedCardsState: ((Int) -> Unit)? = null

    fun initializeCardsList(cardData: List<FAQCard>) {
        cardsList = cardData
        subExpandedCardsState?.invoke(0)
    }

    fun updateCardsState(expanded: Int) {
        expandedCardState = expanded
        subExpandedCardsState?.invoke(expanded)
    }
}