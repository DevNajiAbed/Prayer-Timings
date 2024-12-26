package com.naji.prayertimings.model.api.aladhan

data class AladhanResponse(
    val code: Int,
    val `data`: Data,
    val status: String
)