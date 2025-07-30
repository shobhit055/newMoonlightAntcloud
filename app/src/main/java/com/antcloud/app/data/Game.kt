package com.antcloud.app.data

import com.google.gson.annotations.SerializedName

class GameList(
    val yourGames: ArrayList<Game>,
    val ourGames: ArrayList<Game>,
    val popular: ArrayList<String>,
    var libraryRows: Map<String, ArrayList<String>>? = mapOf())

data class LibraryRows(var EditorsChoice : List<EditorsChoice>)

data class EditorsChoice(var gameId : String = "")

data class Game(
    @SerializedName("description") val description: String = "",
    @SerializedName("properties") val properties: List<Properties> = listOf(),
    @SerializedName("services") val services: List<Service> = listOf(),
    @SerializedName("name") val name: String = "",
    @SerializedName("genre") val genre: List<Genre> = listOf(),
    @SerializedName("type") val type: String = "",
    @SerializedName("gameId") val gameId: String = "",
    @SerializedName("maintenance") val maintenance: Boolean = false,
    @SerializedName("isOurGame") var isOurGame: Boolean = false,
    @SerializedName("expandable") var expandable: Boolean = false)
