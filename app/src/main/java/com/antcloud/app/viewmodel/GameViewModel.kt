package com.antcloud.app.viewmodel



import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antcloud.app.common.GlobalData
import com.antcloud.app.common.Resource
import com.antcloud.app.data.Game
import com.antcloud.app.data.GameState
import com.google.android.exoplayer2.ExoPlayer
import com.antcloud.app.logic.game.GameLogic
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import retrofit2.http.Header
import javax.inject.Inject


@HiltViewModel
class GameViewModel @Inject constructor(private val gameLogic: GameLogic,
                                        @ApplicationContext val context: Context): ViewModel() {

    var game: Game? = null
    var id: String = ""
    var trailerWindow : Boolean = false
    var subTrailerWindowState : ((Boolean) -> Unit)? = null
    var selectedLocation: String = GlobalData.getInstance().accountData.region
    var selectedQuality: String = GlobalData.getInstance().accountData.resolution
    var selectedStore: String = ""
    var idToken: String = ""
    var exoplayer: ExoPlayer? = null
    //var regions: String = ""
    //    var regions: List<String> = GlobalData.getInstance().androidData2.regions.keys.toList()
    //  var regions: List<String>? = null

    fun updateTrailerWindowState(state: Boolean) {
        trailerWindow = state
        subTrailerWindowState?.invoke(state)
    }

   fun setGameId(gameId:String){
       id = gameId
   }

    fun initializeGame() {
        //  GlobalData.getInstance().yourGames.forEach { if (it.gameId == id) game = it }
        if (game == null && GlobalData.getInstance().ourGames.isNotEmpty()) {
            GlobalData.getInstance().ourGames[0].games.forEach { if (it.gameId == id) game = it }
        }
        //selectedStore = game!!.services[0].name
    }

    /*fun addToFavorite() {
        val newFavoriteList = favoriteList?.toMutableList()
        if (newFavoriteList!!.contains(game!!.gameId)) {
            newFavoriteList.remove(game!!.gameId)
        } else {
            newFavoriteList.add(0, game!!.gameId)
        }
        favoriteList = newFavoriteList as ArrayList<String>
        subFavoriteListState?.invoke(newFavoriteList)
    }*/


    var toolbarState: Boolean? = true
    var subToolbarState: ((Boolean) -> Unit)? = null


    fun updateToolbarState(state: Boolean) {
        toolbarState = state
        subToolbarState?.invoke(state)
    }


    private var job: Job? = null
    private val _gameState = mutableStateOf(GameState())
    val gameState: State<GameState> = _gameState


    @SuppressLint("SuspiciousIndentation")
    fun getGameData(@Header("Authorization") token : String) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            gameLogic(token).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _gameState.value = GameState(isLoading = true)
                    }
                    is Resource.Success -> {
                        _gameState.value = gameState.value.copy(
                            isLoading = false,
                            mobileGames = result.data?.body()?.mobileGames,
                            success = 1)
                    }
                    is Resource.Error -> {
                        delay(200)
                        _gameState.value = GameState(
                            error = result.message!!.toString(),
                            success = 0,
                            errorCode = result.errorCode!!,
                            isLoading = false)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
}