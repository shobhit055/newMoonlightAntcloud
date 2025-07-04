package com.limelight.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector


sealed class DrawerScreens(val title: String, val route: String, val icon: ImageVector) {
    object Library : DrawerScreens("Dashboard", "library", Icons.Filled.FormatListBulleted)
    object Pricing : DrawerScreens("Price Plan", "pricing", Icons.Filled.CurrencyRupee)
    object OrderHistory : DrawerScreens("Order History", "orderHistory", Icons.Filled.CurrencyRupee)
    object Support : DrawerScreens("Support", "support", Icons.Filled.HeadsetMic)
    object Account : DrawerScreens("Account", "account", Icons.Filled.Person)
    object Product : DrawerScreens("Shop", "product", Icons.Filled.Shop)
    object EarnGames : DrawerScreens("Earn Gems", "gems", Icons.Filled.Shop)
    object EarnGamesHistory : DrawerScreens("Earn Gems History", "gemsHistory", Icons.Filled.Shop)
    object Report : DrawerScreens("Report", "report", Icons.Filled.Report)
    object FAQs : DrawerScreens("FAQs", "faqs", Icons.Filled.Help)
    object Policy : DrawerScreens("Privacy Policy", "policy", Icons.Filled.PrivacyTip)
    object Terms : DrawerScreens("Terms & Conditions", "terms", Icons.Filled.Assignment)
    object LibraryDetails : DrawerScreens("Library Details", "libraryDetails", Icons.Filled.Details)
    object GameDetails : DrawerScreens("Game Details", "gameDetails", Icons.Filled.Details)
    object Tutorials : DrawerScreens("Tutorials", "tutorials", Icons.Filled.OndemandVideo)
}
