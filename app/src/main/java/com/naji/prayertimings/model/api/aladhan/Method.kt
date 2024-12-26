package com.naji.prayertimings.model.api.aladhan

data class Method(
    val id: Int,
    val location: Location,
    val name: String,
    val params: Params
)