sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Image : Screen("sync_photos")

    object Assistant : Screen("assistant")

    object Setting : Screen("setting")

}