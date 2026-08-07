package org.senai.metrodoc.features.report.model

sealed interface SavedState {
    object Saved : SavedState
    object Unsaved : SavedState
    object Saving : SavedState
    object JustSaved : SavedState
}